package com.dataagent.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "更新用户请求")
public class UserUpdateRequest {
    private String nickname;
    private String email;
    private String phone;
    private Long roleId;
    private Integer status;
}
