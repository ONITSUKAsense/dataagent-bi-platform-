package com.dataagent.ai.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class AgentRunResult {
    private String sessionId;
    private String sql;
    private List<Map<String, Object>> sqlResult;
    private Map<String, Object> chartOption;
    private String chartType;
    private String reportMd;
    private List<AgentStep> steps;
    private int totalTokens;
    private int totalCostMs;

    @Data
    public static class AgentStep {
        private String name;
        private String input;
        private String output;
        private Integer costMs;
        private Integer tokens;
    }
}
