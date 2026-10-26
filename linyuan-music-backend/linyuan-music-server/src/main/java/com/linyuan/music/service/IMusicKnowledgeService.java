package com.linyuan.music.service;

import com.linyuan.music.model.vo.RagAnswerVO;

/**
 * 音乐知识库 RAG 问答。
 */
public interface IMusicKnowledgeService {

    /**
     * 基于曲库资料回答问题
     *
     * @param question 用户问题
     * @param topK     召回条数，为空取配置默认值
     * @return 回答 + 来源
     */
    RagAnswerVO ask(String question, Integer topK);

    /**
     * 重建知识库索引（资料变更后调用）
     *
     * @return 入索引的文档片段数
     */
    int rebuildIndex();

    /**
     * 知识库是否已就绪
     */
    boolean isReady();
}
