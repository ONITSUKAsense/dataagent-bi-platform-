package com.dataagent.modules.file.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FileVO {
    private Long id;
    private String originalName;
    private Long size;
    private String type;
    private String ext;
    private String url;
    private LocalDateTime createdAt;
}
