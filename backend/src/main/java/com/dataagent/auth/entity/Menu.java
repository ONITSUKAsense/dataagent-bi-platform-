package com.dataagent.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("sys_menu")
public class Menu {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
    private String permissionCode;
    private String path;
    private String component;
    private String icon;
    private Long parentId;
    private Integer sort;
    private Integer type;
    private Integer visible;
    private Integer status;

    @TableField(exist = false)
    private List<Menu> children;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
