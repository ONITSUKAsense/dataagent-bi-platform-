package com.dataagent.modules.knowledge.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class KnowledgeDocVO {
    private Long id;
    private String title;
    private String docType;
    private Integer status;
    private Integer chunkCount;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
