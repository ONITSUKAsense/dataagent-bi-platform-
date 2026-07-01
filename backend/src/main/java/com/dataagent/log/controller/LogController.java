package com.dataagent.log.controller;

import com.dataagent.common.model.PageResult;
import com.dataagent.common.model.R;
import com.dataagent.log.service.LogService;
import com.dataagent.log.vo.AgentLogVO;
import com.dataagent.log.vo.TokenStatsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
@Tag(name = "日志中心")
public class LogController {

    private final LogService logService;

    @GetMapping("/agent")
    @Operation(summary = "Agent 日志列表")
    @PreAuthorize("hasAuthority('log:list')")
    public R<PageResult<AgentLogVO>> agentLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String agentType,
            @RequestParam(required = false) Integer status) {
        return R.ok(logService.pageAgentLogs(page, pageSize, agentType, status));
    }

    @GetMapping("/agent/{id}")
    @Operation(summary = "Agent 日志详情")
    @PreAuthorize("hasAuthority('log:list')")
    public R<AgentLogVO> agentLogDetail(@PathVariable Long id) {
        AgentLogVO vo = logService.getAgentLogDetail(id);
        return vo != null ? R.ok(vo) : R.error("日志不存在");
    }

    @GetMapping("/error")
    @Operation(summary = "错误日志")
    @PreAuthorize("hasAuthority('log:list')")
    public R<PageResult<AgentLogVO>> errorLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return R.ok(logService.pageErrorLogs(page, pageSize));
    }

    @GetMapping("/sql")
    @Operation(summary = "SQL 日志")
    @PreAuthorize("hasAuthority('log:list')")
    public R<PageResult<AgentLogVO>> sqlLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return R.ok(logService.pageSQLLogs(page, pageSize));
    }

    @GetMapping("/token-stats")
    @Operation(summary = "Token 用量统计")
    @PreAuthorize("hasAuthority('log:list')")
    public R<List<TokenStatsVO>> tokenStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return R.ok(logService.getTokenStats(startDate, endDate));
    }
}
