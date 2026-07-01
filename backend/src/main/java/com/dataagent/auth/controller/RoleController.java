package com.dataagent.auth.controller;

import com.dataagent.auth.entity.Role;
import com.dataagent.auth.service.RoleService;
import com.dataagent.auth.vo.RoleVO;
import com.dataagent.common.model.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Tag(name = "角色管理")
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    @Operation(summary = "角色列表")
    @PreAuthorize("hasAuthority('sys:role:list')")
    public R<List<RoleVO>> list() {
        return R.ok(roleService.list());
    }

    @GetMapping("/{id}")
    @Operation(summary = "角色详情")
    @PreAuthorize("hasAuthority('sys:role:detail')")
    public R<RoleVO> getById(@PathVariable Long id) {
        return R.ok(roleService.getById(id));
    }

    @PostMapping
    @Operation(summary = "新增角色")
    @PreAuthorize("hasAuthority('sys:role:add')")
    public R<Void> create(@RequestBody Role role) {
        roleService.create(role);
        return R.ok();
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新角色")
    @PreAuthorize("hasAuthority('sys:role:edit')")
    public R<Void> update(@PathVariable Long id, @RequestBody Role role) {
        roleService.update(id, role);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除角色")
    @PreAuthorize("hasAuthority('sys:role:delete')")
    public R<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return R.ok();
    }

    @PutMapping("/{id}/permissions")
    @Operation(summary = "分配权限")
    @PreAuthorize("hasAuthority('sys:role:assign')")
    public R<Void> assignPermissions(@PathVariable Long id, @RequestBody Map<String, List<Long>> body) {
        roleService.assignPermissions(id, body.get("permissionIds"));
        return R.ok();
    }
}
