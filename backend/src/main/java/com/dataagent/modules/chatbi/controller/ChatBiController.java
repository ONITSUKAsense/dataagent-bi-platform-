package com.dataagent.modules.chatbi.controller;

import com.dataagent.common.model.R;
import com.dataagent.common.util.SecurityUtil;
import com.dataagent.modules.chatbi.dto.AskRequest;
import com.dataagent.modules.chatbi.service.ChatBiService;
import com.dataagent.modules.chatbi.vo.ChatMessageVO;
import com.dataagent.modules.chatbi.vo.ChatSessionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chatbi")
@RequiredArgsConstructor
@Tag(name = "ChatBI 智能分析")
public class ChatBiController {

    private final ChatBiService chatBiService;

    @PostMapping("/ask")
    @Operation(summary = "提问（同步）")
    @PreAuthorize("hasAuthority('chatbi:ask')")
    public R<Map<String, Object>> ask(@Valid @RequestBody AskRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        return R.ok(chatBiService.ask(request, userId));
    }

    @GetMapping("/sessions")
    @Operation(summary = "会话列表")
    @PreAuthorize("hasAuthority('chatbi:view')")
    public R<List<ChatSessionVO>> sessions() {
        Long userId = SecurityUtil.getCurrentUserId();
        return R.ok(chatBiService.getSessions(userId));
    }

    @DeleteMapping("/sessions/{sessionId}")
    @Operation(summary = "删除会话")
    @PreAuthorize("hasAuthority('chatbi:delete')")
    public R<Void> deleteSession(@PathVariable String sessionId) {
        Long userId = SecurityUtil.getCurrentUserId();
        chatBiService.deleteSession(sessionId, userId);
        return R.ok();
    }

    @GetMapping("/messages")
    @Operation(summary = "消息列表")
    @PreAuthorize("hasAuthority('chatbi:view')")
    public R<List<ChatMessageVO>> messages(@RequestParam String sessionId) {
        Long userId = SecurityUtil.getCurrentUserId();
        return R.ok(chatBiService.getMessages(sessionId, userId));
    }
}
