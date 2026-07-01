package com.dataagent.log.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dataagent.log.entity.AgentLog;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface AgentLogMapper extends BaseMapper<AgentLog> {

    @Select("SELECT DATE(created_at) as date, COUNT(*) as count FROM agent_log " +
            "WHERE created_at >= #{start} GROUP BY DATE(created_at) ORDER BY DATE(created_at)")
    List<Map<String, Object>> selectCountByDate(@Param("start") LocalDateTime start);

    @Select("SELECT DATE(created_at) as date, SUM(total_tokens) as tokens FROM agent_log " +
            "WHERE created_at >= #{start} GROUP BY DATE(created_at) ORDER BY DATE(created_at)")
    List<Map<String, Object>> selectTokenSumByDate(@Param("start") LocalDateTime start);
}
