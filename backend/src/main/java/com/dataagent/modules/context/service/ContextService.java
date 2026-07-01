package com.dataagent.modules.context.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dataagent.common.util.SecurityUtil;
import com.dataagent.modules.context.entity.ContextEntity;
import com.dataagent.modules.context.mapper.ContextMapper;
import com.dataagent.modules.context.vo.ContextVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContextService {

    private final ContextMapper contextMapper;

    public List<ContextVO> getBySession(String sessionId) {
        return contextMapper.selectList(
                new LambdaQueryWrapper<ContextEntity>()
                        .eq(ContextEntity::getSessionId, sessionId)
                        .orderByDesc(ContextEntity::getCreatedAt))
                .stream()
                .map(this::toVO)
                .toList();
    }

    public List<ContextVO> getByUser() {
        Long userId = SecurityUtil.getCurrentUserId();
        return contextMapper.selectList(
                new LambdaQueryWrapper<ContextEntity>()
                        .eq(ContextEntity::getUserId, userId)
                        .orderByDesc(ContextEntity::getCreatedAt))
                .stream()
                .map(this::toVO)
                .toList();
    }

    public void save(String sessionId, String contextKey, String contextValue, LocalDateTime expireAt) {
        ContextEntity entity = new ContextEntity();
        entity.setSessionId(sessionId);
        entity.setUserId(SecurityUtil.getCurrentUserId());
        entity.setContextKey(contextKey);
        entity.setContextValue(contextValue);
        entity.setExpireAt(expireAt);
        contextMapper.insert(entity);
    }

    public void updateValue(Long id, String contextValue) {
        ContextEntity entity = contextMapper.selectById(id);
        if (entity != null) {
            entity.setContextValue(contextValue);
            contextMapper.updateById(entity);
        }
    }

    public void setExpire(Long id, LocalDateTime expireAt) {
        ContextEntity entity = contextMapper.selectById(id);
        if (entity != null) {
            entity.setExpireAt(expireAt);
            contextMapper.updateById(entity);
        }
    }

    public void deleteBySession(String sessionId) {
        contextMapper.delete(new LambdaQueryWrapper<ContextEntity>()
                .eq(ContextEntity::getSessionId, sessionId));
    }

    public void delete(Long id) {
        contextMapper.deleteById(id);
    }

    public void cleanExpired() {
        contextMapper.delete(new LambdaQueryWrapper<ContextEntity>()
                .lt(ContextEntity::getExpireAt, LocalDateTime.now()));
    }

    private ContextVO toVO(ContextEntity entity) {
        ContextVO vo = new ContextVO();
        vo.setId(entity.getId());
        vo.setSessionId(entity.getSessionId());
        vo.setContextKey(entity.getContextKey());
        vo.setContextValue(entity.getContextValue());
        vo.setExpireAt(entity.getExpireAt());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }
}
