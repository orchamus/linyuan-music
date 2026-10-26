package com.linyuan.music.agent;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.linyuan.music.mapper.ArtistMapper;
import com.linyuan.music.mapper.SongMapper;
import com.linyuan.music.model.entity.Artist;
import com.linyuan.music.model.entity.Song;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 提供给 AI 音乐助手调用的曲库工具（Function Calling）。
 *
 * <p>模型根据用户问题自行决定调用哪个工具，工具内部查询真实数据库并返回文本结果。
 */
@Component
@RequiredArgsConstructor
public class MusicAgentTools {

    private final SongMapper songMapper;
    private final ArtistMapper artistMapper;

    @Tool(description = "按关键词（歌名或专辑名）搜索歌曲，返回歌名、歌手、专辑、风格")
    public String searchSongs(@ToolParam(description = "搜索关键词，例如歌名片段或专辑名") String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return "关键词为空，请向用户确认要搜索的歌曲。";
        }
        String kw = keyword.trim();
        List<Song> songs = songMapper.selectList(new QueryWrapper<Song>()
                .like("name", kw).or().like("album", kw)
                .last("limit 5"));
        return format(songs, "没有搜到与「" + kw + "」相关的歌曲");
    }

    @Tool(description = "按音乐风格推荐歌曲，例如 轻音乐、电子、民谣、氛围、钢琴")
    public String recommendByStyle(@ToolParam(description = "音乐风格关键词") String style) {
        if (style == null || style.isBlank()) {
            return "风格为空，请向用户确认想要的风格。";
        }
        List<Song> songs = songMapper.selectList(new QueryWrapper<Song>()
                .like("style", style.trim())
                .last("limit 5"));
        return format(songs, "曲库里暂时没有「" + style + "」风格的歌曲");
    }

    @Tool(description = "查询平台最热门的歌曲，用于用户没有明确需求时的兜底推荐")
    public String hotSongs() {
        List<Song> songs = songMapper.selectList(new QueryWrapper<Song>().last("limit 5"));
        return format(songs, "曲库暂时没有歌曲");
    }

    private String format(List<Song> songs, String emptyTip) {
        if (songs == null || songs.isEmpty()) {
            return emptyTip;
        }
        Map<Long, String> artistNames = artistNames(songs);
        StringBuilder sb = new StringBuilder("查询结果（共 ").append(songs.size()).append(" 首）：\n");
        for (Song s : songs) {
            sb.append("- ").append(s.getSongName())
                    .append(" - ").append(artistNames.getOrDefault(s.getArtistId(), "未知歌手"))
                    .append("，专辑《").append(s.getAlbum()).append("》");
            if (s.getStyle() != null && !s.getStyle().isBlank()) {
                sb.append("，风格：").append(s.getStyle());
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    private Map<Long, String> artistNames(List<Song> songs) {
        List<Long> ids = songs.stream().map(Song::getArtistId).filter(Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        return artistMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(Artist::getArtistId, Artist::getArtistName, (a, b) -> a));
    }
}
