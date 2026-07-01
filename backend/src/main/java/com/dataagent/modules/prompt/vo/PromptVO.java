package com.dataagent.modules.prompt.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PromptVO {
    private Long id;
    private String name;
    private String code;
    private String description;
    private String content;
    private List<PromptVariable> variables;
    private Integer version;
    private Integer status;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    public static class PromptVariable {
        private String name;
        private String type;
        private String defaultValue;
        private Boolean required;
        private String description;
    }
}
