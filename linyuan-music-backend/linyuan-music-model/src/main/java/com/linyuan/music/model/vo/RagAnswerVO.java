package com.linyuan.music.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * RAG 问答结果：回答 + 命中的知识片段来源。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RagAnswerVO {

    /**
     * 模型回答
     */
    private String answer;

    /**
     * 命中的资料片段（含来源标注），用于「回答可追溯」
     */
    private List<String> sources;

    /**
     * 召回条数
     */
    private Integer hitCount;
}
