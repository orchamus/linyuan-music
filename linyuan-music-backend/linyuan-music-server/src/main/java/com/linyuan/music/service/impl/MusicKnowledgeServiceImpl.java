package com.linyuan.music.service.impl;

import com.linyuan.music.mapper.ArtistMapper;
import com.linyuan.music.mapper.SongMapper;
import com.linyuan.music.model.entity.Artist;
import com.linyuan.music.model.entity.Song;
import com.linyuan.music.model.vo.RagAnswerVO;
import com.linyuan.music.service.IMusicKnowledgeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 音乐知识库 RAG：Tika 解析资料 → 切分 → Embedding → SimpleVectorStore → Top-K 召回 → 生成回答。
 *
 * <p>资料来自曲库（歌手 / 歌曲），首次使用时导出为 txt 文档再由 Tika 解析，
 * 这样文档解析、切分、向量化、检索整条链路都是真实执行的。
 */
@Slf4j
@Service
public class MusicKnowledgeServiceImpl implements IMusicKnowledgeService {

    private static final String SYSTEM_PROMPT = """
            你是「林渊音乐」平台的音乐知识库助手。
            下面是检索到的资料片段，必须严格依据资料回答，不要编造资料中没有的信息。
            如果资料不足以回答，直接说明“资料里没有找到相关信息”。
            回答中使用 [编号] 标注引用的资料，例如 [1][2]。
            """;

    private final EmbeddingModel embeddingModel;
    private final SongMapper songMapper;
    private final ArtistMapper artistMapper;
    private final ChatClient chatClient;
    private final String apiKey;

    @Value("${linyuan-music.ai.rag.knowledge-dir:./uploads/knowledge}")
    private String knowledgeDir;

    @Value("${linyuan-music.ai.rag.top-k:5}")
    private int defaultTopK;

    @Value("${linyuan-music.ai.rag.similarity-threshold:0.55}")
    private double similarityThreshold;

    private volatile SimpleVectorStore vectorStore;
    private volatile boolean indexed = false;

    public MusicKnowledgeServiceImpl(EmbeddingModel embeddingModel,
                                     SongMapper songMapper,
                                     ArtistMapper artistMapper,
                                     ChatModel chatModel,
                                     @Value("${spring.ai.dashscope.api-key:}") String apiKey) {
        this.embeddingModel = embeddingModel;
        this.songMapper = songMapper;
        this.artistMapper = artistMapper;
        this.apiKey = apiKey;
        this.chatClient = ChatClient.builder(chatModel).defaultSystem(SYSTEM_PROMPT).build();
    }

    @Override
    public synchronized int rebuildIndex() {
        indexed = false;
        vectorStore = SimpleVectorStore.builder(embeddingModel).build();
        Path dir = Paths.get(knowledgeDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(dir);
            exportKnowledgeDocs(dir);

            List<Document> documents = new ArrayList<>();
            try (Stream<Path> files = Files.walk(dir)) {
                List<Path> docs = files.filter(Files::isRegularFile)
                        .filter(p -> p.toString().endsWith(".txt") || p.toString().endsWith(".md"))
                        .toList();
                for (Path file : docs) {
                    // Apache Tika 解析文档内容
                    List<Document> parsed = new TikaDocumentReader(new FileSystemResource(file)).get();
                    for (Document d : parsed) {
                        d.getMetadata().put("source", file.getFileName().toString());
                    }
                    documents.addAll(parsed);
                }
            }
            if (documents.isEmpty()) {
                log.warn("知识库为空，跳过索引构建");
                indexed = true;
                return 0;
            }
            // 按 token 切分，控制单段长度，避免召回片段过大
            TokenTextSplitter splitter = new TokenTextSplitter();
            List<Document> chunks = splitter.apply(documents);
            vectorStore.add(chunks);
            indexed = true;
            log.info("RAG 知识库索引完成：{} 个片段（来源 {} 个文件）", chunks.size(), documents.size());
            return chunks.size();
        } catch (Exception e) {
            log.error("构建 RAG 索引失败: {}", e.getMessage());
            throw new IllegalStateException("知识库索引构建失败：" + e.getMessage(), e);
        }
    }

    @Override
    public RagAnswerVO ask(String question, Integer topK) {
        if (!StringUtils.hasText(question)) {
            throw new IllegalArgumentException("问题不能为空");
        }
        ensureIndex();

        int k = topK == null || topK <= 0 ? defaultTopK : topK;
        List<Document> hits = vectorStore.similaritySearch(SearchRequest.builder()
                .query(question)
                .topK(k)
                .similarityThreshold(similarityThreshold)
                .build());

        if (hits == null || hits.isEmpty()) {
            return new RagAnswerVO("资料里没有找到与「" + question + "」相关的内容，可以换个问法或先扩充知识库。",
                    List.of(), 0);
        }

        StringBuilder context = new StringBuilder();
        List<String> sources = new ArrayList<>();
        for (int i = 0; i < hits.size(); i++) {
            Document d = hits.get(i);
            String source = String.valueOf(d.getMetadata().getOrDefault("source", "unknown"));
            String text = d.getText() == null ? "" : d.getText().trim();
            context.append("[").append(i + 1).append("]（来源：").append(source).append("）\n")
                    .append(text).append("\n\n");
            sources.add("[" + (i + 1) + "] " + source + "：" + abbreviate(text, 80));
        }

        String answer = chatClient.prompt()
                .user(u -> u.text("资料：\n{context}\n\n问题：{question}")
                        .param("context", context.toString())
                        .param("question", question))
                .call()
                .content();

        return new RagAnswerVO(answer, sources, hits.size());
    }

    @Override
    public boolean isReady() {
        return indexed;
    }

    private void ensureIndex() {
        if (indexed) {
            return;
        }
        if (apiKey == null || apiKey.isBlank() || "sk-demo".equals(apiKey)) {
            throw new IllegalStateException("未配置 DASHSCOPE_API_KEY，无法完成向量化，请配置后重试");
        }
        rebuildIndex();
    }

    /**
     * 把曲库数据导出成文档，供 Tika 解析（已存在则跳过）。
     */
    private void exportKnowledgeDocs(Path dir) throws IOException {
        Path artistFile = dir.resolve("artists.txt");
        Path songFile = dir.resolve("songs.txt");
        if (Files.exists(artistFile) && Files.exists(songFile)) {
            return;
        }

        List<Artist> artists = artistMapper.selectList(null);
        StringBuilder sb = new StringBuilder("林渊音乐 · 歌手资料\n\n");
        for (Artist a : artists) {
            sb.append("歌手：").append(a.getArtistName()).append('\n');
            if (a.getArea() != null) {
                sb.append("地区：").append(a.getArea()).append('\n');
            }
            if (a.getIntroduction() != null && !a.getIntroduction().isBlank()) {
                sb.append("简介：").append(a.getIntroduction()).append('\n');
            }
            sb.append('\n');
        }
        Files.writeString(artistFile, sb.toString(), StandardCharsets.UTF_8);

        List<Song> songs = songMapper.selectList(null);
        Map<Long, String> artistNames = artists.stream()
                .collect(Collectors.toMap(Artist::getArtistId, Artist::getArtistName, (x, y) -> x));
        StringBuilder sb2 = new StringBuilder("林渊音乐 · 曲库资料\n\n");
        for (Song s : songs) {
            sb2.append("歌曲：").append(s.getSongName()).append('\n')
                    .append("歌手：").append(artistNames.getOrDefault(s.getArtistId(), "未知歌手")).append('\n')
                    .append("专辑：").append(s.getAlbum()).append('\n');
            if (s.getStyle() != null) {
                sb2.append("风格：").append(s.getStyle()).append('\n');
            }
            if (s.getDuration() != null) {
                sb2.append("时长：").append(s.getDuration()).append(" 秒\n");
            }
            if (s.getReleaseTime() != null) {
                sb2.append("发行时间：").append(s.getReleaseTime()).append('\n');
            }
            sb2.append('\n');
        }
        Files.writeString(songFile, sb2.toString(), StandardCharsets.UTF_8);
        log.info("已生成知识库文档：{} / {}", artistFile.getFileName(), songFile.getFileName());
    }

    private String abbreviate(String text, int max) {
        String oneLine = text.replaceAll("\\s+", " ").trim();
        return oneLine.length() <= max ? oneLine : oneLine.substring(0, max) + "…";
    }
}
