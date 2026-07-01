package com.dataagent.ai.dto;

import lombok.Data;

@Data
public class AgentRunRequest {
    private String question;
    private String sessionId;
}
