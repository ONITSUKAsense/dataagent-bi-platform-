package com.dataagent.log.vo;

import lombok.Data;

@Data
public class TokenStatsVO {
    private String date;
    private Long promptTokens;
    private Long completionTokens;
    private Long totalTokens;
}
