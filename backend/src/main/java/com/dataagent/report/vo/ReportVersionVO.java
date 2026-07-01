package com.dataagent.report.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReportVersionVO {
    private Long id;
    private Long reportId;
    private Integer version;
    private String contentMd;
    private String changeNote;
    private String createdBy;
    private LocalDateTime createdAt;
}
