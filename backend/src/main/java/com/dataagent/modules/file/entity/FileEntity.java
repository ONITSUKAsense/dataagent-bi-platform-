package com.dataagent.modules.file.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("file")
public class FileEntity {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String originalName;
    private String storedName;
    private String path;
    private Long size;
    private String type;
    private String ext;
    private String storageType;
    private String url;
    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
