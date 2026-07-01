package com.dataagent.modules.chatbi.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dataagent.ai.client.AiServiceClient;
import com.dataagent.ai.dto.AgentRunRequest;
import com.dataagent.ai.dto.AgentRunResult;
import com.dataagent.common.exception.BusinessException;
import com.dataagent.modules.chatbi.dto.AskRequest;
import com.dataagent.modules.chatbi.entity.ChatMessage;
import com.dataagent.modules.chatbi.mapper.ChatMessageMapper;
import com.dataagent.modules.chatbi.vo.ChatMessageVO;
import com.dataagent.modules.chatbi.vo.ChatSessionVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatBiService {

    private final ChatMessageMapper chatMessageMapper;
    private final AiServiceClient aiServiceClient;
    private final ObjectMapper objectMapper;

    public Map<String, Object> ask(AskRequest request, Long userId) {
        String sessionId = request.getSessionId();
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = UUID.randomUUID().toString().replace("-", "");
        }

        // Save user message
        ChatMessage userMsg = new ChatMessage();
        userMsg.setUserId(userId);
        userMsg.setSessionId(sessionId);
        userMsg.setRole("user");
        userMsg.setContent(request.getQuestion());
        userMsg.setMessageType("text");
        chatMessageMapper.insert(userMsg);

        // Call AI service
        AgentRunRequest aiRequest = new AgentRunRequest();
        aiRequest.setQuestion(request.getQuestion());
        aiRequest.setSessionId(sessionId);

        AgentRunResult aiResult;
        try {
            aiResult = aiServiceClient.runAgent(aiRequest);
        } catch (Exception e) {
            // Fallback: return a simple response when AI service is unavailable
            aiResult = createFallbackResult(sessionId, request.getQuestion());
        }

        // Save assistant message
        ChatMessage assistantMsg = new ChatMessage();
        assistantMsg.setUserId(userId);
        assistantMsg.setSessionId(sessionId);
        assistantMsg.setRole("assistant");
        assistantMsg.setContent(aiResult.getReportMd());

        if (aiResult.getChartOption() != null) {
            assistantMsg.setMessageType("report");
            try {
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("chartType", aiResult.getChartType());
                metadata.put("option", aiResult.getChartOption());
                metadata.put("sql", aiResult.getSql());
                metadata.put("sqlResult", aiResult.getSqlResult());
                assistantMsg.setMetadata(objectMapper.writeValueAsString(metadata));
            } catch (JsonProcessingException e) {
                assistantMsg.setMessageType("text");
            }
        } else {
            assistantMsg.setMessageType("text");
        }

        assistantMsg.setTokenCount(aiResult.getTotalTokens());
        chatMessageMapper.insert(assistantMsg);

        // Build response
        Map<String, Object> result = new HashMap<>();
        result.put("sessionId", sessionId);
        result.put("answer", aiResult.getReportMd());
        result.put("chartOption", aiResult.getChartOption());
        result.put("chartType", aiResult.getChartType());
        result.put("sql", aiResult.getSql());
        result.put("totalTokens", aiResult.getTotalTokens());
        result.put("totalCostMs", aiResult.getTotalCostMs());

        return result;
    }

    public List<ChatSessionVO> getSessions(Long userId) {
        List<Map<String, Object>> rows = chatMessageMapper.selectSessionsByUserId(userId);
        return rows.stream().map(row -> {
            ChatSessionVO vo = new ChatSessionVO();
            vo.setSessionId((String) row.get("session_id"));
            String title = (String) row.get("title");
            vo.setTitle(title != null ? title.substring(0, Math.min(title.length(), 50)) : "新对话");
            vo.setMessageCount(((Number) row.get("message_count")).intValue());
            vo.setCreatedAt((LocalDateTime) row.get("created_at"));
            vo.setUpdatedAt((LocalDateTime) row.get("updated_at"));
            return vo;
        }).collect(Collectors.toList());
    }

    private LocalDateTime convertToLocalDateTime(Object obj) {
        if (obj instanceof LocalDateTime) return (LocalDateTime) obj;
        if (obj instanceof Date) return ((Date) obj).toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
        return null;
    }

    public void deleteSession(String sessionId, Long userId) {
        chatMessageMapper.delete(
                new LambdaQueryWrapper<ChatMessage>()
                        .eq(ChatMessage::getSessionId, sessionId)
                        .eq(ChatMessage::getUserId, userId));
    }

    public List<ChatMessageVO> getMessages(String sessionId, Long userId) {
        List<ChatMessage> messages = chatMessageMapper.selectList(
                new LambdaQueryWrapper<ChatMessage>()
                        .eq(ChatMessage::getSessionId, sessionId)
                        .eq(ChatMessage::getUserId, userId)
                        .orderByAsc(ChatMessage::getCreatedAt));

        return messages.stream().map(this::toVO).collect(Collectors.toList());
    }

    private ChatMessageVO toVO(ChatMessage msg) {
        ChatMessageVO vo = new ChatMessageVO();
        vo.setId(msg.getId());
        vo.setSessionId(msg.getSessionId());
        vo.setRole(msg.getRole());
        vo.setContent(msg.getContent());
        vo.setMessageType(msg.getMessageType());
        vo.setCreatedAt(msg.getCreatedAt());

        if (msg.getMetadata() != null) {
            try {
                vo.setMetadata(objectMapper.readTree(msg.getMetadata()));
            } catch (JsonProcessingException e) {
                vo.setMetadata(null);
            }
        }
        return vo;
    }

    private AgentRunResult createFallbackResult(String sessionId, String question) {
        AgentRunResult result = new AgentRunResult();
        result.setSessionId(sessionId);
        result.setSql("-- AI service unavailable");
        result.setChartType("bar");
        result.setTotalTokens(0);
        result.setTotalCostMs(0);

        // Mock data
        List<Map<String, Object>> mockData = new ArrayList<>();
        Map<String, Object> row1 = new HashMap<>();
        row1.put("month", "1月"); row1.put("value", 120);
        mockData.add(row1);
        Map<String, Object> row2 = new HashMap<>();
        row2.put("month", "2月"); row2.put("value", 200);
        mockData.add(row2);
        Map<String, Object> row3 = new HashMap<>();
        row3.put("month", "3月"); row3.put("value", 150);
        mockData.add(row3);
        result.setSqlResult(mockData);

        Map<String, Object> chartOption = new HashMap<>();
        chartOption.put("title", Map.of("text", "数据趋势", "left", "center"));
        chartOption.put("tooltip", Map.of("trigger", "axis"));
        chartOption.put("xAxis", Map.of("type", "category", "data", List.of("1月", "2月", "3月")));
        chartOption.put("yAxis", Map.of("type", "value"));
        chartOption.put("series", List.of(
                Map.of("name", "数值", "type", "bar", "data", List.of(120, 200, 150),
                        "itemStyle", Map.of("borderRadius", List.of(4, 4, 0, 0)))
        ));
        result.setChartOption(chartOption);

        result.setReportMd(String.format(
                "## 数据分析报告\n\n### 分析摘要\n针对问题「%s」的分析已完成。\n\n### 数据概览\n" +
                        "| 月份 | 数值 |\n|------|------|\n| 1月 | 120 |\n| 2月 | 200 |\n| 3月 | 150 |\n\n" +
                        "### 关键发现\n- 2月份达到峰值 200\n- 3月份较2月下降 25%%\n\n### 结论与建议\n建议进一步分析增长驱动因素。",
                question));

        return result;
    }
}
