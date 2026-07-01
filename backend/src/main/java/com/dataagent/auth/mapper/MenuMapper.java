package com.dataagent.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dataagent.auth.entity.Menu;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface MenuMapper extends BaseMapper<Menu> {

    @Select("SELECT m.* FROM sys_menu m " +
            "LEFT JOIN sys_role_menu rm ON m.id = rm.menu_id " +
            "WHERE rm.role_id = #{roleId} AND m.status = 1 AND m.visible = 1 " +
            "ORDER BY m.sort")
    List<Menu> selectMenusByRoleId(Long roleId);
}
