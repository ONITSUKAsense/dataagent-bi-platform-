"""Qwen API wrapper using DashScope SDK."""
import json
from typing import Optional
import http.client
import json

from app.config import QWEN_API_KEY, QWEN_MODEL
from app.utils.logger import logger


def call_qwen(
    prompt: str,
    system_prompt: Optional[str] = None,
    temperature: float = 0.3,
    max_tokens: int = 4096,
) -> str:
    """Call Qwen API directly via HTTP (no DashScope SDK dependency issues)."""
    if not QWEN_API_KEY:
        logger.warning("QWEN_API_KEY not set, returning mock response")
        return _mock_response(prompt)

    try:
        conn = http.client.HTTPSConnection("dashscope.aliyuncs.com")
        messages = []
        if system_prompt:
            messages.append({"role": "system", "content": system_prompt})
        messages.append({"role": "user", "content": prompt})

        payload = json.dumps({
            "model": QWEN_MODEL,
            "input": {"messages": messages},
            "parameters": {
                "temperature": temperature,
                "max_tokens": max_tokens,
                "result_format": "message"
            }
        })

        headers = {
            'Authorization': f'Bearer {QWEN_API_KEY}',
            'Content-Type': 'application/json'
        }

        conn.request("POST", "/api/v1/services/aigc/text-generation/generation",
                     payload, headers)
        res = conn.getresponse()
        data = json.loads(res.read().decode())

        if data.get("output") and data["output"].get("choices"):
            return data["output"]["choices"][0]["message"]["content"]
        else:
            logger.error(f"Qwen API error: {data}")
            return _mock_response(prompt)

    except Exception as e:
        logger.error(f"Qwen API call failed: {e}")
        return _mock_response(prompt)


def _mock_response(prompt: str) -> str:
    """Return mock response when API key is not configured."""
    if "SQL" in prompt or "sql" in prompt or "查询" in prompt:
        return "SELECT DATE_FORMAT(created_at, '%Y-%m-%d') as date, COUNT(*) as count, SUM(amount) as total FROM report GROUP BY DATE_FORMAT(created_at, '%Y-%m-%d') ORDER BY date DESC LIMIT 30"
    if "图表" in prompt or "chart" in prompt or "echarts" in prompt.lower():
        return json.dumps({
            "chartType": "line",
            "option": {
                "title": {"text": "数据趋势分析"},
                "tooltip": {"trigger": "axis"},
                "xAxis": {"type": "category", "data": ["1月", "2月", "3月"]},
                "yAxis": {"type": "value"},
                "series": [{"name": "数值", "type": "line", "data": [120, 200, 150]}]
            }
        })
    if "报告" in prompt or "report" in prompt:
        return """## 数据分析报告

### 数据概览
本次分析共涉及 **3** 个月的数据。

### 关键发现
- 整体趋势呈现波动上升
- 2月份达到峰值

### 结论与建议
建议持续关注数据变化趋势。"""
    return "这是一个模拟响应。请配置 QWEN_API_KEY 以获取真实 AI 回复。"
