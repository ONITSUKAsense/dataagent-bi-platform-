package com.dataagent.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dataagent.auth.entity.Permission;
import com.dataagent.auth.mapper.PermissionMapper;
import com.dataagent.auth.vo.PermissionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionMapper permissionMapper;

    public List<PermissionVO> list() {
        List<Permission> all = permissionMapper.selectList(
                new LambdaQueryWrapper<Permission>().orderByAsc(Permission::getSort));
        return buildTree(all, 0L);
    }

    private List<PermissionVO> buildTree(List<Permission> permissions, Long parentId) {
        return permissions.stream()
                .filter(p -> p.getParentId() != null && p.getParentId().equals(parentId))
                .map(p -> {
                    PermissionVO vo = toVO(p);
                    vo.setChildren(buildTree(permissions, p.getId()));
                    return vo;
                })
                .sorted(Comparator.comparingInt(PermissionVO::getSort))
                .collect(Collectors.toList());
    }

    private PermissionVO toVO(Permission p) {
        PermissionVO vo = new PermissionVO();
        vo.setId(p.getId());
        vo.setName(p.getName());
        vo.setCode(p.getCode());
        vo.setType(p.getType());
        vo.setParentId(p.getParentId());
        vo.setSort(p.getSort());
        return vo;
    }
}
