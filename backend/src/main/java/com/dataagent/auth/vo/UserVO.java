package com.dataagent.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "用户信息")
public class UserVO {
    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String phone;
    private Long roleId;
    private String roleName;
    private Integer status;
    private LocalDateTime createdAt;
}
