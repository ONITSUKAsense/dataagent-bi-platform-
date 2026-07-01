package com.dataagent.modules.prompt.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dataagent.common.model.PageResult;
import com.dataagent.common.util.SecurityUtil;
import com.dataagent.modules.prompt.entity.PromptEntity;
import com.dataagent.modules.prompt.mapper.PromptMapper;
import com.dataagent.modules.prompt.vo.PromptVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PromptService {

    private final PromptMapper promptMapper;
    private final ObjectMapper objectMapper;

    public PageResult<PromptVO> page(int page, int pageSize, String keyword) {
        IPage<PromptEntity> promptPage = promptMapper.selectPage(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<PromptEntity>()
                        .like(keyword != null, PromptEntity::getName, keyword)
                        .or(keyword != null, w -> w.like(PromptEntity::getCode, keyword))
                        .orderByDesc(PromptEntity::getUpdatedAt));

        return PageResult.of(promptPage.convert(this::toVO));
    }

    public PromptVO getById(Long id) {
        PromptEntity entity = promptMapper.selectById(id);
        return entity != null ? toVO(entity) : null;
    }

    public PromptVO getByCode(String code) {
        PromptEntity entity = promptMapper.selectOne(
                new LambdaQueryWrapper<PromptEntity>().eq(PromptEntity::getCode, code));
        return entity != null ? toVO(entity) : null;
    }

    public void add(PromptVO vo) {
        PromptEntity entity = new PromptEntity();
        entity.setName(vo.getName());
        entity.setCode(vo.getCode());
        entity.setDescription(vo.getDescription());
        entity.setContent(vo.getContent());
        entity.setVariables(toJson(vo.getVariables()));
        entity.setVersion(1);
        entity.setStatus(0);
        entity.setCreatedBy(SecurityUtil.getCurrentUserId());
        promptMapper.insert(entity);
    }

    public void update(Long id, PromptVO vo) {
        PromptEntity entity = promptMapper.selectById(id);
        if (entity != null) {
            entity.setName(vo.getName());
            entity.setCode(vo.getCode());
            entity.setDescription(vo.getDescription());
            entity.setContent(vo.getContent());
            entity.setVariables(toJson(vo.getVariables()));
            promptMapper.updateById(entity);
        }
    }

    public void publish(Long id) {
        PromptEntity entity = promptMapper.selectById(id);
        if (entity != null) {
            entity.setStatus(1);
            entity.setVersion(entity.getVersion() + 1);
            promptMapper.updateById(entity);
        }
    }

    public void delete(Long id) {
        promptMapper.deleteById(id);
    }

    public String testRender(Long id, Map<String, String> variables) {
        PromptEntity entity = promptMapper.selectById(id);
        if (entity == null) return null;

        String content = entity.getContent();
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            content = content.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return content;
    }

    public List<PromptVO> getPublished() {
        return promptMapper.selectList(
                new LambdaQueryWrapper<PromptEntity>()
                        .eq(PromptEntity::getStatus, 1)
                        .orderByDesc(PromptEntity::getUpdatedAt))
                .stream().map(this::toVO).collect(Collectors.toList());
    }

    private PromptVO toVO(PromptEntity entity) {
        PromptVO vo = new PromptVO();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setCode(entity.getCode());
        vo.setDescription(entity.getDescription());
        vo.setContent(entity.getContent());
        vo.setVariables(parseVariables(entity.getVariables()));
        vo.setVersion(entity.getVersion());
        vo.setStatus(entity.getStatus());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }

    @SuppressWarnings("unchecked")
    private List<PromptVO.PromptVariable> parseVariables(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return objectMapper.readValue(json, new TypeReference<List<PromptVO.PromptVariable>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }

    private String toJson(List<PromptVO.PromptVariable> variables) {
        if (variables == null || variables.isEmpty()) return null;
        try {
            return objectMapper.writeValueAsString(variables);
        } catch (Exception e) {
            return null;
        }
    }
}
