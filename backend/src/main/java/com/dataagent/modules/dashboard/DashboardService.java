package com.dataagent.modules.dashboard;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dataagent.auth.entity.User;
import com.dataagent.auth.mapper.UserMapper;
import com.dataagent.modules.dashboard.vo.*;
import com.dataagent.report.mapper.ReportMapper;
import com.dataagent.report.entity.Report;
import com.dataagent.log.mapper.AgentLogMapper;
import com.dataagent.log.entity.AgentLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserMapper userMapper;
    private final ReportMapper reportMapper;
    private final AgentLogMapper agentLogMapper;

    public DashboardStatsVO getStats() {
        DashboardStatsVO vo = new DashboardStatsVO();

        vo.setUserCount(userMapper.selectCount(null));
        vo.setReportCount(reportMapper.selectCount(null));

        // Today's agent calls
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LambdaQueryWrapper<AgentLog> todayWrapper = new LambdaQueryWrapper<AgentLog>()
                .ge(AgentLog::getCreatedAt, todayStart);
        vo.setTodayCallCount(agentLogMapper.selectCount(todayWrapper));

        // Total agent calls
        vo.setAgentCallCount(agentLogMapper.selectCount(null));

        // Total tokens consumed
        List<AgentLog> allLogs = agentLogMapper.selectList(null);
        long totalTokens = allLogs.stream()
                .mapToLong(log -> log.getTotalTokens() != null ? log.getTotalTokens() : 0)
                .sum();
        vo.setTokenConsumed(totalTokens);

        return vo;
    }

    public List<RecentReportVO> getRecentReports() {
        List<Report> reports = reportMapper.selectList(
                new LambdaQueryWrapper<Report>()
                        .orderByDesc(Report::getCreatedAt)
                        .last("LIMIT 10"));

        return reports.stream().map(r -> {
            RecentReportVO vo = new RecentReportVO();
            vo.setId(r.getId());
            vo.setTitle(r.getTitle());
            vo.setStatus(r.getStatus());
            vo.setVersion(r.getVersion());

            User user = userMapper.selectById(r.getCreatedBy());
            vo.setCreatedBy(user != null ? user.getUsername() : "未知");

            vo.setCreatedAt(r.getCreatedAt() != null ? r.getCreatedAt().toString() : "");
            return vo;
        }).collect(Collectors.toList());
    }

    public List<TrendItemVO> getAgentTrend() {
        LocalDate start = LocalDate.now().minusDays(30);
        List<Map<String, Object>> results = agentLogMapper.selectCountByDate(start.atStartOfDay());

        Map<String, Long> dateMap = new HashMap<>();
        for (Map<String, Object> row : results) {
            dateMap.put(String.valueOf(row.get("date")), ((Number) row.get("count")).longValue());
        }

        List<TrendItemVO> trend = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd");
        for (int i = 29; i >= 0; i--) {
            LocalDate d = LocalDate.now().minusDays(i);
            String key = d.toString();
            Long count = dateMap.getOrDefault(key, 0L);
            TrendItemVO item = new TrendItemVO();
            item.setDate(d.format(fmt));
            item.setCount(count.intValue());
            trend.add(item);
        }
        return trend;
    }

    public List<TrendItemVO> getTokenTrend() {
        LocalDate start = LocalDate.now().minusDays(30);
        List<Map<String, Object>> results = agentLogMapper.selectTokenSumByDate(start.atStartOfDay());

        Map<String, Long> dateMap = new HashMap<>();
        for (Map<String, Object> row : results) {
            dateMap.put(String.valueOf(row.get("date")), ((Number) row.get("tokens")).longValue());
        }

        List<TrendItemVO> trend = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd");
        for (int i = 29; i >= 0; i--) {
            LocalDate d = LocalDate.now().minusDays(i);
            String key = d.toString();
            Long tokens = dateMap.getOrDefault(key, 0L);
            TrendItemVO item = new TrendItemVO();
            item.setDate(d.format(fmt));
            item.setCount(tokens.intValue());
            trend.add(item);
        }
        return trend;
    }

    public Map<String, Object> getUserStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userMapper.selectCount(null));

        LocalDateTime weekAgo = LocalDate.now().minusDays(7).atStartOfDay();
        LambdaQueryWrapper<User> weekWrapper = new LambdaQueryWrapper<User>()
                .ge(User::getCreatedAt, weekAgo);
        stats.put("newUsersThisWeek", userMapper.selectCount(weekWrapper));

        return stats;
    }
}
