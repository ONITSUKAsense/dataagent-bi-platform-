package com.dataagent.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dataagent.auth.dto.UserCreateRequest;
import com.dataagent.auth.dto.UserUpdateRequest;
import com.dataagent.auth.entity.Role;
import com.dataagent.auth.entity.User;
import com.dataagent.auth.mapper.RoleMapper;
import com.dataagent.auth.mapper.UserMapper;
import com.dataagent.auth.vo.UserVO;
import com.dataagent.common.exception.BusinessException;
import com.dataagent.common.model.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;

    public PageResult<UserVO> page(int page, int pageSize, String username, Integer status) {
        IPage<User> userPage = userMapper.selectPage(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<User>()
                        .like(username != null, User::getUsername, username)
                        .eq(status != null, User::getStatus, status)
                        .orderByDesc(User::getCreatedAt));

        return PageResult.of(userPage.convert(this::toVO));
    }

    public UserVO getById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) throw new BusinessException("用户不存在");
        return toVO(user);
    }

    public void create(UserCreateRequest request) {
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername()));
        if (count > 0) throw new BusinessException("用户名已存在");

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRoleId(request.getRoleId());
        user.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        userMapper.insert(user);
    }

    public void update(Long id, UserUpdateRequest request) {
        User user = userMapper.selectById(id);
        if (user == null) throw new BusinessException("用户不存在");

        user.setNickname(request.getNickname());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRoleId(request.getRoleId());
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
        userMapper.updateById(user);
    }

    public void delete(Long id) {
        userMapper.deleteById(id);
    }

    public void updateStatus(Long id, Integer status) {
        User user = userMapper.selectById(id);
        if (user == null) throw new BusinessException("用户不存在");
        user.setStatus(status);
        userMapper.updateById(user);
    }

    private UserVO toVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setRoleId(user.getRoleId());
        vo.setStatus(user.getStatus());
        vo.setCreatedAt(user.getCreatedAt());

        if (user.getRoleId() != null) {
            Role role = roleMapper.selectById(user.getRoleId());
            if (role != null) {
                vo.setRoleName(role.getName());
            }
        }
        return vo;
    }
}
