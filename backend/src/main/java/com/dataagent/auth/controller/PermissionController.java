package com.dataagent.auth.controller;

import com.dataagent.auth.service.PermissionService;
import com.dataagent.auth.vo.PermissionVO;
import com.dataagent.common.model.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
@Tag(name = "权限管理")
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping
    @Operation(summary = "权限树")
    @PreAuthorize("hasAuthority('sys:perm:list')")
    public R<List<PermissionVO>> list() {
        return R.ok(permissionService.list());
    }

    @GetMapping("/all")
    @Operation(summary = "全部权限")
    public R<List<PermissionVO>> all() {
        return R.ok(permissionService.list());
    }
}
