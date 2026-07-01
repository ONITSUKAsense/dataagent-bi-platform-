package com.dataagent.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

@Data
@Schema(description = "菜单信息")
public class MenuVO {
    private Long id;
    private String name;
    private String permissionCode;
    private String path;
    private String component;
    private String icon;
    private Long parentId;
    private Integer sort;
    private Integer type;
    private Boolean visible;
    private Integer status;
    private List<MenuVO> children;
}
