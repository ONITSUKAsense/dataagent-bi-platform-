package com.dataagent.auth.controller;

import com.dataagent.auth.dto.LoginRequest;
import com.dataagent.auth.dto.RegisterRequest;
import com.dataagent.auth.service.AuthService;
import com.dataagent.auth.vo.LoginVO;
import com.dataagent.auth.vo.UserInfoVO;
import com.dataagent.common.model.R;
import com.dataagent.common.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "登录")
    public R<LoginVO> login(@Valid @RequestBody LoginRequest request) {
        return R.ok(authService.login(request));
    }

    @PostMapping("/register")
    @Operation(summary = "注册")
    public R<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return R.ok();
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新Token")
    public R<LoginVO> refresh(@RequestBody RefreshRequest request) {
        return R.ok(authService.refresh(request.refreshToken()));
    }

    @PostMapping("/logout")
    @Operation(summary = "登出")
    public R<Void> logout() {
        return R.ok();
    }

    @GetMapping("/me")
    @Operation(summary = "获取当前用户信息")
    public R<UserInfoVO> me() {
        Long userId = SecurityUtil.getCurrentUserId();
        return R.ok(authService.getCurrentUser(userId));
    }

    record RefreshRequest(String refreshToken) {}
}
