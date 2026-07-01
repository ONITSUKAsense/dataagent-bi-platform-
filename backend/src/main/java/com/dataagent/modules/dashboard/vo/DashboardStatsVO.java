package com.dataagent.modules.dashboard.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Dashboard 统计")
public class DashboardStatsVO {
    private long userCount;
    private long reportCount;
    private long agentCallCount;
    private long tokenConsumed;
    private long todayCallCount;
}
