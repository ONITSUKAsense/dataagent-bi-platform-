package com.dataagent.modules.knowledge.vo;

import lombok.Data;

@Data
public class ChunkVO {
    private Long id;
    private Long docId;
    private Integer chunkIndex;
    private String content;
}
