package com.linyuan.music.controller.agent;

import com.linyuan.music.agent.AgentPrompt;
import com.linyuan.music.agent.MusicAgentTools;
import com.linyuan.music.model.dto.AgentChatDTO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;

/**
 * AI 音乐助手（Agent）接口：工具调用 + 多轮会话 + SSE 流式输出。
 */
@Slf4j
@RestController
@RequestMapping("/agent")
public class AgentController {

    private final ChatClient chatClient;
    private final String apiKey;

    public AgentController(ChatModel chatModel,
                           MusicAgentTools musicAgentTools,
                           @Value("${spring.ai.dashscope.api-key:}") String apiKey) {
        this.apiKey = apiKey;
        this.chatClient = ChatClient.builder(chatModel)
                .defaultSystem(AgentPrompt.SYSTEM)
                // 滑动窗口会话记忆：保留最近若干轮，避免上下文无限增长
                .defaultAdvisors(MessageChatMemoryAdvisor
                        .builder(MessageWindowChatMemory.builder().maxMessages(20).build())
                        .build())
                .defaultTools(musicAgentTools)
                .build();
        log.info("AI 音乐助手已初始化（工具：搜歌 / 风格推荐 / 热门歌曲）");
    }

    /**
     * 流式对话（SSE），供前端聊天窗口使用。
     */
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@RequestBody AgentChatDTO dto, HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache, no-transform");
        response.setHeader("X-Accel-Buffering", "no");

        SseEmitter emitter = new SseEmitter(0L);
        if (isKeyMissing()) {
            try {
                emitter.send(SseEmitter.event().name("text")
                        .data("AI 音乐助手未启用：请配置环境变量 DASHSCOPE_API_KEY 后重启后端。"));
                emitter.complete();
            } catch (IOException ignored) {
            }
            return emitter;
        }

        String conversationId = dto.getConversationId() == null || dto.getConversationId().isBlank()
                ? "default" : dto.getConversationId();

        Flux<String> content = chatClient.prompt(dto.getQuery())
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .stream()
                .content();

        content.subscribe(
                delta -> {
                    try {
                        emitter.send(SseEmitter.event().name("text").data(delta));
                    } catch (IOException e) {
                        log.warn("SSE 客户端已断开: {}", e.getMessage());
                        emitter.complete();
                    }
                },
                error -> {
                    log.error("AI 流式输出失败", error);
                    emitter.completeWithError(error);
                },
                emitter::complete
        );
        return emitter;
    }

    /**
     * 非流式对话，便于接口自测。
     */
    @GetMapping("/simple/chat")
    public String simpleChat(@RequestParam(value = "query") String query,
                             @RequestParam(value = "conversationId", required = false) String conversationId) {
        if (isKeyMissing()) {
            return "AI 音乐助手未启用：请配置环境变量 DASHSCOPE_API_KEY 后重启后端。";
        }
        String cid = conversationId == null || conversationId.isBlank() ? "default" : conversationId;
        return chatClient.prompt(query)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, cid))
                .call()
                .content();
    }

    private boolean isKeyMissing() {
        return apiKey == null || apiKey.isBlank() || "sk-demo".equals(apiKey);
    }
}
