package com.dataagent.log.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AgentLogVO {
    private Long id;
    private Long userId;
    private String username;
    private String sessionId;
    private String agentType;
    private String llmModel;
    private Integer promptTokens;
    private Integer completionTokens;
    private Integer totalTokens;
    private Integer costMs;
    private Integer status;
    private String errorMsg;
    private String input;
    private String output;
    private String steps;
    private LocalDateTime createdAt;
}
