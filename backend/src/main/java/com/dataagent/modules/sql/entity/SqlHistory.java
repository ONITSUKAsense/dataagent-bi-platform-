package com.dataagent.modules.sql.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sql_history")
public class SqlHistory {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String sessionId;
    private String originalQuestion;
    private String generatedSql;
    private String executeResult;
    private Integer rowCount;
    private Integer costMs;
    private Integer status;
    private String errorMsg;
    private Integer isFavorite;
    private Integer tokenCost;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
