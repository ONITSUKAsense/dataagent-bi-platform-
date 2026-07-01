package com.dataagent.log.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("agent_log")
public class AgentLog {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String sessionId;
    private String agentType;
    private String input;
    private String output;
    private String steps;
    private String llmModel;
    private Integer promptTokens;
    private Integer completionTokens;
    private Integer totalTokens;
    private Integer costMs;
    private Integer status;
    private String errorMsg;
    private String ip;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
