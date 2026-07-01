package com.dataagent.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "登录响应")
public class LoginVO {
    @Schema(description = "访问Token")
    private String token;

    @Schema(description = "刷新Token")
    private String refreshToken;
}
