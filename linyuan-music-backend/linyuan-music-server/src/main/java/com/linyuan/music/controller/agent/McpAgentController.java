package com.linyuan.music.controller.agent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * 通过 MCP（Model Context Protocol）接入的 AI 音乐助手。
 *
 * <p>与 {@link AgentController} 的区别：这里的工具不是 Spring 里注册的 Bean，
 * 而是由 Spring AI MCP Client 通过 stdio 拉起本地 MCP Server（mcp-server/index.js），
 * 再把它暴露的标准 MCP 工具聚合成本地 ToolCallback 交给模型调用。
 */
@Slf4j
@RestController
@RequestMapping("/agent/mcp")
public class McpAgentController {

    private final ChatClient chatClient;
    private final ToolCallbackProvider toolCallbackProvider;
    private final String apiKey;

    public McpAgentController(ChatModel chatModel,
                              ObjectProvider<ToolCallbackProvider> provider,
                              @Value("${spring.ai.dashscope.api-key:}") String apiKey) {
        this.apiKey = apiKey;
        this.toolCallbackProvider = provider.getIfAvailable();
        ChatClient.Builder builder = ChatClient.builder(chatModel)
                .defaultSystem("""
                        你是「林渊音乐」的 AI 音乐助手。
                        你可以调用 MCP 提供的工具查询曲库（搜索歌曲、按歌手查歌、热门歌曲）。
                        必须依据工具返回的真实数据回答，不要编造歌名或歌手；回答用中文，控制在 3~5 句。
                        """);
        if (toolCallbackProvider != null) {
            builder.defaultToolCallbacks(toolCallbackProvider.getToolCallbacks());
            log.info("MCP 工具已注册到 AI 音乐助手：{}", toolNames());
        } else {
            log.warn("未发现 MCP ToolCallbackProvider，MCP 工具不可用（检查 spring.ai.mcp.client 配置）");
        }
        this.chatClient = builder.build();
    }

    /**
     * 通过 MCP 工具回答（非流式，便于验证）
     */
    @GetMapping("/simple/chat")
    public String simpleChat(@RequestParam("query") String query) {
        if (isKeyMissing()) {
            return "未配置 DASHSCOPE_API_KEY，MCP 助手暂不可用。";
        }
        return chatClient.prompt(query).call().content();
    }

    /**
     * 当前 MCP Server 暴露的工具列表，用于验证 MCP 连接是否成功
     */
    @GetMapping("/tools")
    public List<String> tools() {
        if (toolCallbackProvider == null) {
            return List.of();
        }
        return Arrays.stream(toolCallbackProvider.getToolCallbacks())
                .map(ToolCallback::getToolDefinition)
                .map(def -> def.name() + " - " + def.description())
                .toList();
    }

    private List<String> toolNames() {
        if (toolCallbackProvider == null) {
            return List.of();
        }
        return Arrays.stream(toolCallbackProvider.getToolCallbacks())
                .map(cb -> cb.getToolDefinition().name())
                .toList();
    }

    private boolean isKeyMissing() {
        return apiKey == null || apiKey.isBlank() || "sk-demo".equals(apiKey);
    }
}
