package com.dataagent.modules.context.controller;

import com.dataagent.common.model.R;
import com.dataagent.modules.context.service.ContextService;
import com.dataagent.modules.context.vo.ContextVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/context")
@RequiredArgsConstructor
@Tag(name = "上下文管理")
public class ContextController {

    private final ContextService contextService;

    @GetMapping("/session/{sessionId}")
    @Operation(summary = "获取会话上下文")
    @PreAuthorize("hasAuthority('context:list')")
    public R<List<ContextVO>> getBySession(@PathVariable String sessionId) {
        return R.ok(contextService.getBySession(sessionId));
    }

    @GetMapping("/my")
    @Operation(summary = "获取我的上下文")
    @PreAuthorize("hasAuthority('context:list')")
    public R<List<ContextVO>> getMyContext() {
        return R.ok(contextService.getByUser());
    }

    @PostMapping
    @Operation(summary = "添加上下文")
    @PreAuthorize("hasAuthority('context:add')")
    public R<Void> add(@RequestBody Map<String, Object> body) {
        String sessionId = (String) body.get("sessionId");
        String contextKey = (String) body.get("contextKey");
        String contextValue = (String) body.get("contextValue");
        String expireAtStr = (String) body.get("expireAt");
        LocalDateTime expireAt = expireAtStr != null ? LocalDateTime.parse(expireAtStr) : null;
        contextService.save(sessionId, contextKey, contextValue, expireAt);
        return R.ok();
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新上下文")
    @PreAuthorize("hasAuthority('context:update')")
    public R<Void> update(@PathVariable Long id, @RequestBody Map<String, String> body) {
        contextService.updateValue(id, body.get("contextValue"));
        return R.ok();
    }

    @PutMapping("/{id}/expire")
    @Operation(summary = "设置过期时间")
    @PreAuthorize("hasAuthority('context:update')")
    public R<Void> setExpire(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String expireAt = body.get("expireAt");
        if (expireAt != null) {
            contextService.setExpire(id, LocalDateTime.parse(expireAt));
        }
        return R.ok();
    }

    @DeleteMapping("/session/{sessionId}")
    @Operation(summary = "清空会话上下文")
    @PreAuthorize("hasAuthority('context:delete')")
    public R<Void> clearSession(@PathVariable String sessionId) {
        contextService.deleteBySession(sessionId);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除上下文")
    @PreAuthorize("hasAuthority('context:delete')")
    public R<Void> delete(@PathVariable Long id) {
        contextService.delete(id);
        return R.ok();
    }

    @PostMapping("/clean-expired")
    @Operation(summary = "清理过期上下文")
    @PreAuthorize("hasAuthority('context:delete')")
    public R<Void> cleanExpired() {
        contextService.cleanExpired();
        return R.ok();
    }
}
