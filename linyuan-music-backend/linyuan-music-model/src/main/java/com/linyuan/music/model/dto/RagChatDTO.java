package com.linyuan.music.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * RAG 知识库问答请求体。
 */
@Data
public class RagChatDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户问题
     */
    private String question;

    /**
     * 召回条数，为空时取配置默认值
     */
    private Integer topK;
}
