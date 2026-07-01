package com.dataagent.modules.context.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ContextVO {
    private Long id;
    private String sessionId;
    private String contextKey;
    private String contextValue;
    private LocalDateTime expireAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
