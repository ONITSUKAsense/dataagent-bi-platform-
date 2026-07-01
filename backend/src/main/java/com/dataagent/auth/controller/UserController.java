package com.dataagent.auth.controller;

import com.dataagent.auth.dto.UserCreateRequest;
import com.dataagent.auth.dto.UserUpdateRequest;
import com.dataagent.auth.service.UserService;
import com.dataagent.auth.vo.UserVO;
import com.dataagent.common.model.PageResult;
import com.dataagent.common.model.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "用户管理")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "用户分页")
    @PreAuthorize("hasAuthority('sys:user:list')")
    public R<PageResult<UserVO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer status) {
        return R.ok(userService.page(page, pageSize, username, status));
    }

    @GetMapping("/{id}")
    @Operation(summary = "用户详情")
    @PreAuthorize("hasAuthority('sys:user:detail')")
    public R<UserVO> getById(@PathVariable Long id) {
        return R.ok(userService.getById(id));
    }

    @PostMapping
    @Operation(summary = "新增用户")
    @PreAuthorize("hasAuthority('sys:user:add')")
    public R<Void> create(@Valid @RequestBody UserCreateRequest request) {
        userService.create(request);
        return R.ok();
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新用户")
    @PreAuthorize("hasAuthority('sys:user:edit')")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        userService.update(id, request);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户")
    @PreAuthorize("hasAuthority('sys:user:delete')")
    public R<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return R.ok();
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新用户状态")
    @PreAuthorize("hasAuthority('sys:user:edit')")
    public R<Void> updateStatus(@PathVariable Long id, @RequestBody StatusRequest request) {
        userService.updateStatus(id, request.status());
        return R.ok();
    }

    record StatusRequest(Integer status) {}
}
