"""LLM API wrapper supporting Qwen (DashScope) and DeepSeek."""
import json
from typing import Optional
import http.client
import json

from app.config import get_llm_config, QWEN_API_KEY
from app.utils.logger import logger


def call_llm(
    prompt: str,
    system_prompt: Optional[str] = None,
    temperature: float = 0.3,
    max_tokens: int = 4096,
) -> str:
    """Call LLM API based on configured provider."""
    config = get_llm_config()

    if not config["api_key"]:
        logger.warning(f"{config['provider']} API key not set, returning mock response")
        return _mock_response(prompt)

    if config["provider"] == "qwen":
        return _call_qwen(prompt, system_prompt, temperature, max_tokens, config)
    elif config["provider"] == "deepseek":
        return _call_deepseek(prompt, system_prompt, temperature, max_tokens, config)

    logger.warning(f"Unknown LLM provider: {config['provider']}")
    return _mock_response(prompt)


def _call_qwen(prompt: str, system_prompt: Optional[str], temperature: float,
               max_tokens: int, config: dict) -> str:
    """Call Qwen (DashScope) API."""
    try:
        conn = http.client.HTTPSConnection("dashscope.aliyuncs.com")
        messages = []
        if system_prompt:
            messages.append({"role": "system", "content": system_prompt})
        messages.append({"role": "user", "content": prompt})

        payload = json.dumps({
            "model": config["model"],
            "input": {"messages": messages},
            "parameters": {
                "temperature": temperature,
                "max_tokens": max_tokens,
                "result_format": "message"
            }
        })

        headers = {
            'Authorization': f'Bearer {config["api_key"]}',
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


def _call_deepseek(prompt: str, system_prompt: Optional[str], temperature: float,
                   max_tokens: int, config: dict) -> str:
    """Call DeepSeek API (OpenAI-compatible)."""
    try:
        conn = http.client.HTTPSConnection("api.deepseek.com")
        messages = []
        if system_prompt:
            messages.append({"role": "system", "content": system_prompt})
        messages.append({"role": "user", "content": prompt})

        payload = json.dumps({
            "model": config["model"],
            "messages": messages,
            "temperature": temperature,
            "max_tokens": max_tokens,
        })

        headers = {
            'Authorization': f'Bearer {config["api_key"]}',
            'Content-Type': 'application/json'
        }

        conn.request("POST", "/v1/chat/completions", payload, headers)
        res = conn.getresponse()
        data = json.loads(res.read().decode())

        if data.get("choices"):
            return data["choices"][0]["message"]["content"]
        else:
            logger.error(f"DeepSeek API error: {data}")
            return _mock_response(prompt)

    except Exception as e:
        logger.error(f"DeepSeek API call failed: {e}")
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
    return "这是一个模拟响应。请配置 LLM API Key 以获取真实 AI 回复。如需配置请查看 README.md 中 LLM API 配置说明。"
