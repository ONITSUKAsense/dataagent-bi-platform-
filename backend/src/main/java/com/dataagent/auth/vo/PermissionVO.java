package com.dataagent.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

@Data
@Schema(description = "权限信息")
public class PermissionVO {
    private Long id;
    private String name;
    private String code;
    private Integer type;
    private Long parentId;
    private Integer sort;
    private List<PermissionVO> children;
}
