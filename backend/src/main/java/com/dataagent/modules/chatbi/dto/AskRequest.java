package com.dataagent.modules.chatbi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "ChatBI 提问请求")
public class AskRequest {
    @NotBlank(message = "问题不能为空")
    @Schema(description = "用户问题")
    private String question;

    @Schema(description = "会话ID，为空则创建新会话")
    private String sessionId;
}
