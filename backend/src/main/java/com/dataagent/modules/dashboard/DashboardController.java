package com.dataagent.modules.dashboard;

import com.dataagent.common.model.R;
import com.dataagent.modules.dashboard.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    @Operation(summary = "统计卡片")
    @PreAuthorize("hasAuthority('dashboard:view')")
    public R<DashboardStatsVO> stats() {
        return R.ok(dashboardService.getStats());
    }

    @GetMapping("/recent-reports")
    @Operation(summary = "最近报告")
    @PreAuthorize("hasAuthority('dashboard:view')")
    public R<List<RecentReportVO>> recentReports() {
        return R.ok(dashboardService.getRecentReports());
    }

    @GetMapping("/agent-trend")
    @Operation(summary = "Agent 调用趋势")
    @PreAuthorize("hasAuthority('dashboard:view')")
    public R<List<TrendItemVO>> agentTrend() {
        return R.ok(dashboardService.getAgentTrend());
    }

    @GetMapping("/token-trend")
    @Operation(summary = "Token 消耗趋势")
    @PreAuthorize("hasAuthority('dashboard:view')")
    public R<List<TrendItemVO>> tokenTrend() {
        return R.ok(dashboardService.getTokenTrend());
    }

    @GetMapping("/user-stats")
    @Operation(summary = "用户统计")
    @PreAuthorize("hasAuthority('dashboard:view')")
    public R<Map<String, Object>> userStats() {
        return R.ok(dashboardService.getUserStats());
    }
}
