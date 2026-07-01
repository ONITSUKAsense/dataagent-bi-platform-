package com.dataagent.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "角色信息")
public class RoleVO {
    private Long id;
    private String name;
    private String code;
    private Integer sort;
    private Integer status;
    private String remark;
    private LocalDateTime createdAt;
    private List<Long> permissionIds;
}
