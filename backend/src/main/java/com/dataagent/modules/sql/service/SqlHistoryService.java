package com.dataagent.modules.sql.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dataagent.modules.sql.entity.SqlHistory;
import com.dataagent.modules.sql.mapper.SqlHistoryMapper;
import com.dataagent.modules.sql.vo.SqlHistoryVO;
import com.dataagent.common.model.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SqlHistoryService {

    private final SqlHistoryMapper sqlHistoryMapper;

    public void record(SqlHistory history) {
        sqlHistoryMapper.insert(history);
    }

    public PageResult<SqlHistoryVO> page(int page, int pageSize, String question, Long userId) {
        IPage<SqlHistory> sqlPage = sqlHistoryMapper.selectPage(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<SqlHistory>()
                        .eq(SqlHistory::getUserId, userId)
                        .like(question != null, SqlHistory::getOriginalQuestion, question)
                        .orderByDesc(SqlHistory::getCreatedAt));

        return PageResult.of(sqlPage.convert(this::toVO));
    }

    public SqlHistoryVO getById(Long id) {
        SqlHistory history = sqlHistoryMapper.selectById(id);
        return history != null ? toVO(history) : null;
    }

    public void toggleFavorite(Long id) {
        SqlHistory history = sqlHistoryMapper.selectById(id);
        if (history != null) {
            history.setIsFavorite(history.getIsFavorite() == 1 ? 0 : 1);
            sqlHistoryMapper.updateById(history);
        }
    }

    public Map<String, Object> getStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();

        long totalCount = sqlHistoryMapper.selectCount(
                new LambdaQueryWrapper<SqlHistory>().eq(SqlHistory::getUserId, userId));
        stats.put("totalCount", totalCount);

        long successCount = sqlHistoryMapper.selectCount(
                new LambdaQueryWrapper<SqlHistory>()
                        .eq(SqlHistory::getUserId, userId)
                        .eq(SqlHistory::getStatus, 0));
        stats.put("successCount", successCount);

        long favoriteCount = sqlHistoryMapper.selectCount(
                new LambdaQueryWrapper<SqlHistory>()
                        .eq(SqlHistory::getUserId, userId)
                        .eq(SqlHistory::getIsFavorite, 1));
        stats.put("favoriteCount", favoriteCount);

        // Average cost
        Double avgCost = sqlHistoryMapper.selectList(
                        new LambdaQueryWrapper<SqlHistory>()
                                .eq(SqlHistory::getUserId, userId)
                                .eq(SqlHistory::getStatus, 0))
                .stream()
                .collect(Collectors.averagingInt(SqlHistory::getCostMs));
        stats.put("avgCostMs", Math.round(avgCost));

        return stats;
    }

    private SqlHistoryVO toVO(SqlHistory history) {
        SqlHistoryVO vo = new SqlHistoryVO();
        vo.setId(history.getId());
        vo.setOriginalQuestion(history.getOriginalQuestion());
        vo.setGeneratedSql(history.getGeneratedSql());
        vo.setRowCount(history.getRowCount());
        vo.setCostMs(history.getCostMs());
        vo.setStatus(history.getStatus());
        vo.setIsFavorite(history.getIsFavorite() == 1);
        vo.setTokenCost(history.getTokenCost());
        vo.setCreatedAt(history.getCreatedAt());
        return vo;
    }
}
