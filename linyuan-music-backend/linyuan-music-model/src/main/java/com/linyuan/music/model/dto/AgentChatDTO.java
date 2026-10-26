package com.linyuan.music.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * AI 音乐助手对话请求体。
 */
@Data
public class AgentChatDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户问题
     */
    private String query;

    /**
     * 会话 ID（用于多轮记忆隔离）
     */
    private String conversationId;
}
