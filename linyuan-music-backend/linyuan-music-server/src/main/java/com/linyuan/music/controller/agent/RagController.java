package com.linyuan.music.controller.agent;

import com.linyuan.music.model.dto.RagChatDTO;
import com.linyuan.music.model.vo.RagAnswerVO;
import com.linyuan.music.result.Result;
import com.linyuan.music.service.IMusicKnowledgeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * RAG 知识库问答接口：基于曲库资料回答，并返回命中来源。
 */
@Slf4j
@RestController
@RequestMapping("/rag")
@RequiredArgsConstructor
public class RagController {

    private final IMusicKnowledgeService musicKnowledgeService;

    /**
     * 知识库问答
     */
    @PostMapping("/ask")
    public Result<RagAnswerVO> ask(@RequestBody RagChatDTO dto) {
        try {
            return Result.success(musicKnowledgeService.ask(dto.getQuestion(), dto.getTopK()));
        } catch (IllegalStateException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 重建索引（曲库资料变更后调用）
     */
    @PostMapping("/rebuild")
    public Result<Integer> rebuild() {
        try {
            return Result.success("索引重建完成", musicKnowledgeService.rebuildIndex());
        } catch (Exception e) {
            return Result.error("索引重建失败：" + e.getMessage());
        }
    }

    /**
     * 健康检查
     */
    @GetMapping("/status")
    public Result<Boolean> status() {
        return Result.success(musicKnowledgeService.isReady());
    }
}
