package com.dataagent.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dataagent.auth.entity.Menu;
import com.dataagent.auth.mapper.MenuMapper;
import com.dataagent.auth.vo.MenuVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuMapper menuMapper;

    public List<MenuVO> list() {
        List<Menu> all = menuMapper.selectList(
                new LambdaQueryWrapper<Menu>().orderByAsc(Menu::getSort));
        return buildTree(all, 0L);
    }

    public List<MenuVO> getUserMenus(Long userId) {
        List<Menu> menus = menuMapper.selectMenusByRoleId(userId);
        return buildTree(menus, 0L);
    }

    public void create(Menu menu) {
        menuMapper.insert(menu);
    }

    public void update(Long id, Menu menu) {
        menu.setId(id);
        menuMapper.updateById(menu);
    }

    public void delete(Long id) {
        menuMapper.deleteById(id);
        // Delete children
        menuMapper.delete(new LambdaQueryWrapper<Menu>().eq(Menu::getParentId, id));
    }

    private List<MenuVO> buildTree(List<Menu> menus, Long parentId) {
        return menus.stream()
                .filter(m -> m.getParentId() != null && m.getParentId().equals(parentId))
                .map(m -> {
                    MenuVO vo = toVO(m);
                    vo.setChildren(buildTree(menus, m.getId()));
                    return vo;
                })
                .sorted(Comparator.comparingInt(MenuVO::getSort))
                .collect(Collectors.toList());
    }

    private MenuVO toVO(Menu menu) {
        MenuVO vo = new MenuVO();
        vo.setId(menu.getId());
        vo.setName(menu.getName());
        vo.setPermissionCode(menu.getPermissionCode());
        vo.setPath(menu.getPath());
        vo.setComponent(menu.getComponent());
        vo.setIcon(menu.getIcon());
        vo.setParentId(menu.getParentId());
        vo.setSort(menu.getSort());
        vo.setType(menu.getType());
        vo.setVisible(menu.getVisible() == 1);
        vo.setStatus(menu.getStatus());
        return vo;
    }
}
