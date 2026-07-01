package com.dataagent.modules.chatbi.vo;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChatMessageVO {
    private Long id;
    private String sessionId;
    private String role;
    private String content;
    private String messageType;
    private JsonNode metadata;
    private LocalDateTime createdAt;
}
