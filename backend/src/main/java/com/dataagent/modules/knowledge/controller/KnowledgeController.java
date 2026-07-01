package com.dataagent.modules.knowledge.controller;

import com.dataagent.common.model.PageResult;
import com.dataagent.common.model.R;
import com.dataagent.modules.knowledge.entity.KnowledgeDoc;
import com.dataagent.modules.knowledge.service.KnowledgeService;
import com.dataagent.modules.knowledge.vo.ChunkVO;
import com.dataagent.modules.knowledge.vo.KnowledgeDocVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
@Tag(name = "知识库")
public class KnowledgeController {

    private final KnowledgeService knowledgeService;

    @GetMapping("/docs")
    @Operation(summary = "文档列表")
    @PreAuthorize("hasAuthority('knowledge:list')")
    public R<PageResult<KnowledgeDocVO>> docs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return R.ok(knowledgeService.page(page, pageSize));
    }

    @PostMapping("/docs")
    @Operation(summary = "创建文档记录")
    @PreAuthorize("hasAuthority('knowledge:add')")
    public R<Long> create(@RequestBody KnowledgeDoc doc) {
        return R.ok(knowledgeService.create(doc));
    }

    @DeleteMapping("/docs/{id}")
    @Operation(summary = "删除文档")
    @PreAuthorize("hasAuthority('knowledge:delete')")
    public R<Void> delete(@PathVariable Long id) {
        knowledgeService.delete(id);
        return R.ok();
    }

    @GetMapping("/docs/{id}/chunks")
    @Operation(summary = "文档切片列表")
    @PreAuthorize("hasAuthority('knowledge:list')")
    public R<List<ChunkVO>> chunks(@PathVariable Long id) {
        return R.ok(knowledgeService.getChunks(id));
    }
}
