package com.dataagent.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dataagent.auth.entity.Role;
import com.dataagent.auth.entity.RolePermission;
import com.dataagent.auth.mapper.RoleMapper;
import com.dataagent.auth.mapper.RolePermissionMapper;
import com.dataagent.auth.vo.RoleVO;
import com.dataagent.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleMapper roleMapper;
    private final RolePermissionMapper rolePermissionMapper;

    public List<RoleVO> list() {
        return roleMapper.selectList(null).stream().map(this::toVO).collect(Collectors.toList());
    }

    public RoleVO getById(Long id) {
        Role role = roleMapper.selectById(id);
        if (role == null) throw new BusinessException("角色不存在");
        return toVO(role);
    }

    public void create(Role role) {
        roleMapper.insert(role);
    }

    public void update(Long id, Role role) {
        role.setId(id);
        roleMapper.updateById(role);
    }

    public void delete(Long id) {
        roleMapper.deleteById(id);
    }

    @Transactional
    public void assignPermissions(Long roleId, List<Long> permissionIds) {
        rolePermissionMapper.delete(new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRoleId, roleId));
        for (Long permId : permissionIds) {
            RolePermission rp = new RolePermission();
            rp.setRoleId(roleId);
            rp.setPermissionId(permId);
            rolePermissionMapper.insert(rp);
        }
    }

    private RoleVO toVO(Role role) {
        RoleVO vo = new RoleVO();
        vo.setId(role.getId());
        vo.setName(role.getName());
        vo.setCode(role.getCode());
        vo.setSort(role.getSort());
        vo.setStatus(role.getStatus());
        vo.setRemark(role.getRemark());
        vo.setCreatedAt(role.getCreatedAt());

        List<RolePermission> rps = rolePermissionMapper.selectList(
                new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRoleId, role.getId()));
        vo.setPermissionIds(rps.stream().map(RolePermission::getPermissionId).collect(Collectors.toList()));
        return vo;
    }
}
