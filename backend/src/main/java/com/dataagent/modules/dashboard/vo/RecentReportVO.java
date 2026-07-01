package com.dataagent.modules.dashboard.vo;

import lombok.Data;

@Data
public class RecentReportVO {
    private Long id;
    private String title;
    private String createdBy;
    private String createdAt;
    private Integer status;
    private Integer version;
}
