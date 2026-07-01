package com.dataagent.report.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("report_version")
public class ReportVersion {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long reportId;
    private Integer version;
    private String contentMd;
    private String contentHtml;
    private String chartConfig;
    private String changeNote;
    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
