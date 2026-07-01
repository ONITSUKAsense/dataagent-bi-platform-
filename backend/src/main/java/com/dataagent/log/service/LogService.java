package com.dataagent.log.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dataagent.common.model.PageResult;
import com.dataagent.log.entity.AgentLog;
import com.dataagent.log.mapper.AgentLogMapper;
import com.dataagent.log.vo.AgentLogVO;
import com.dataagent.log.vo.TokenStatsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LogService {

    private final AgentLogMapper agentLogMapper;

    public PageResult<AgentLogVO> pageAgentLogs(int page, int pageSize, String agentType, Integer status) {
        IPage<AgentLog> logPage = agentLogMapper.selectPage(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<AgentLog>()
                        .eq(agentType != null, AgentLog::getAgentType, agentType)
                        .eq(status != null, AgentLog::getStatus, status)
                        .orderByDesc(AgentLog::getCreatedAt));

        return PageResult.of(logPage.convert(this::toVO));
    }

    public AgentLogVO getAgentLogDetail(Long id) {
        AgentLog log = agentLogMapper.selectById(id);
        return log != null ? toVO(log) : null;
    }

    public PageResult<AgentLogVO> pageErrorLogs(int page, int pageSize) {
        IPage<AgentLog> logPage = agentLogMapper.selectPage(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<AgentLog>()
                        .eq(AgentLog::getStatus, 1)
                        .orderByDesc(AgentLog::getCreatedAt));

        return PageResult.of(logPage.convert(this::toVO));
    }

    public PageResult<AgentLogVO> pageSQLLogs(int page, int pageSize) {
        IPage<AgentLog> logPage = agentLogMapper.selectPage(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<AgentLog>()
                        .eq(AgentLog::getAgentType, "sql_generator")
                        .orderByDesc(AgentLog::getCreatedAt));

        return PageResult.of(logPage.convert(this::toVO));
    }

    public List<TokenStatsVO> getTokenStats(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : LocalDateTime.now().minusDays(7);
        List<Map<String, Object>> rows = agentLogMapper.selectTokenSumByDate(start);

        return rows.stream().map(row -> {
            TokenStatsVO vo = new TokenStatsVO();
            vo.setDate(String.valueOf(row.get("date")));
            vo.setTotalTokens(toLong(row.get("tokens")));
            return vo;
        }).collect(Collectors.toList());
    }

    private AgentLogVO toVO(AgentLog log) {
        AgentLogVO vo = new AgentLogVO();
        vo.setId(log.getId());
        vo.setUserId(log.getUserId());
        vo.setSessionId(log.getSessionId());
        vo.setAgentType(log.getAgentType());
        vo.setInput(log.getInput());
        vo.setOutput(log.getOutput());
        vo.setSteps(log.getSteps());
        vo.setLlmModel(log.getLlmModel());
        vo.setPromptTokens(log.getPromptTokens());
        vo.setCompletionTokens(log.getCompletionTokens());
        vo.setTotalTokens(log.getTotalTokens());
        vo.setCostMs(log.getCostMs());
        vo.setStatus(log.getStatus());
        vo.setErrorMsg(log.getErrorMsg());
        vo.setCreatedAt(log.getCreatedAt());
        return vo;
    }

    private Long toLong(Object value) {
        if (value == null) return 0L;
        if (value instanceof Number) return ((Number) value).longValue();
        return 0L;
    }
}
