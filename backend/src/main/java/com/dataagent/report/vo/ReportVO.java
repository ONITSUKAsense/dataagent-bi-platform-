package com.dataagent.report.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReportVO {
    private Long id;
    private String title;
    private String description;
    private String contentMd;
    private String contentHtml;
    private String sessionId;
    private Integer status;
    private Integer version;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
