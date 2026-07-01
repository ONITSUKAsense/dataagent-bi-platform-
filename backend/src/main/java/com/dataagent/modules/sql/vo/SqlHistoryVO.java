package com.dataagent.modules.sql.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SqlHistoryVO {
    private Long id;
    private String originalQuestion;
    private String generatedSql;
    private Integer rowCount;
    private Integer costMs;
    private Integer status;
    private Boolean isFavorite;
    private Integer tokenCost;
    private LocalDateTime createdAt;
}
