package com.dataagent.modules.sql.controller;

import com.dataagent.common.model.PageResult;
import com.dataagent.common.model.R;
import com.dataagent.common.util.SecurityUtil;
import com.dataagent.modules.sql.service.SqlHistoryService;
import com.dataagent.modules.sql.vo.SqlHistoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/sql")
@RequiredArgsConstructor
@Tag(name = "SQL 管理")
public class SqlHistoryController {

    private final SqlHistoryService sqlHistoryService;

    @GetMapping("/history")
    @Operation(summary = "SQL 执行历史")
    @PreAuthorize("hasAuthority('sql:list')")
    public R<PageResult<SqlHistoryVO>> history(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String question) {
        Long userId = SecurityUtil.getCurrentUserId();
        return R.ok(sqlHistoryService.page(page, pageSize, question, userId));
    }

    @GetMapping("/history/{id}")
    @Operation(summary = "SQL 详情")
    @PreAuthorize("hasAuthority('sql:detail')")
    public R<SqlHistoryVO> detail(@PathVariable Long id) {
        return R.ok(sqlHistoryService.getById(id));
    }

    @PutMapping("/history/{id}/favorite")
    @Operation(summary = "收藏/取消收藏")
    @PreAuthorize("hasAuthority('sql:favorite')")
    public R<Void> toggleFavorite(@PathVariable Long id) {
        sqlHistoryService.toggleFavorite(id);
        return R.ok();
    }

    @GetMapping("/stats")
    @Operation(summary = "SQL 统计")
    @PreAuthorize("hasAuthority('sql:stats')")
    public R<Map<String, Object>> stats() {
        Long userId = SecurityUtil.getCurrentUserId();
        return R.ok(sqlHistoryService.getStats(userId));
    }
}
