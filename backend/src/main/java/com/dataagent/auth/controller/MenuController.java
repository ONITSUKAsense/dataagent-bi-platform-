package com.dataagent.auth.controller;

import com.dataagent.auth.entity.Menu;
import com.dataagent.auth.service.MenuService;
import com.dataagent.auth.vo.MenuVO;
import com.dataagent.common.model.R;
import com.dataagent.common.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
@Tag(name = "菜单管理")
public class MenuController {

    private final MenuService menuService;

    @GetMapping
    @Operation(summary = "菜单树")
    @PreAuthorize("hasAuthority('sys:menu:list')")
    public R<List<MenuVO>> list() {
        return R.ok(menuService.list());
    }

    @GetMapping("/user")
    @Operation(summary = "当前用户菜单")
    public R<List<MenuVO>> getUserMenus() {
        Long userId = SecurityUtil.getCurrentUserId();
        return R.ok(menuService.getUserMenus(userId));
    }

    @PostMapping
    @Operation(summary = "新增菜单")
    @PreAuthorize("hasAuthority('sys:menu:add')")
    public R<Void> create(@RequestBody Menu menu) {
        menuService.create(menu);
        return R.ok();
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新菜单")
    @PreAuthorize("hasAuthority('sys:menu:edit')")
    public R<Void> update(@PathVariable Long id, @RequestBody Menu menu) {
        menuService.update(id, menu);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除菜单")
    @PreAuthorize("hasAuthority('sys:menu:delete')")
    public R<Void> delete(@PathVariable Long id) {
        menuService.delete(id);
        return R.ok();
    }
}
