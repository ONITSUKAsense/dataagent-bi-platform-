package com.dataagent.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dataagent.auth.entity.User;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT p.code FROM sys_user u " +
            "LEFT JOIN sys_role_permission rp ON u.role_id = rp.role_id " +
            "LEFT JOIN sys_permission p ON rp.permission_id = p.id " +
            "WHERE u.id = #{userId} AND p.code IS NOT NULL")
    List<String> selectPermissionsByUserId(Long userId);
}
