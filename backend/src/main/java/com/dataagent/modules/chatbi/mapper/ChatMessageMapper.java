package com.dataagent.modules.chatbi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dataagent.modules.chatbi.entity.ChatMessage;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface ChatMessageMapper extends BaseMapper<ChatMessage> {

    @Select("SELECT session_id, MIN(content) as title, COUNT(*) as message_count, " +
            "MIN(created_at) as created_at, MAX(created_at) as updated_at " +
            "FROM chat_message WHERE user_id = #{userId} " +
            "GROUP BY session_id ORDER BY updated_at DESC")
    List<Map<String, Object>> selectSessionsByUserId(Long userId);
}
