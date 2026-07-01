"""Generate ECharts configuration from query results using LLM."""
import json
from typing import Optional
from app.utils.llm_client import call_llm
from app.utils.logger import logger

SYSTEM_PROMPT = """你是一个数据可视化专家。请根据用户的问题和查询数据，生成最合适的ECharts图表配置。

规则：
1. 返回纯JSON格式的ECharts option配置
2. 不要返回任何markdown标记或解释
3. 根据数据特征选择最合适的图表类型（line/bar/pie/scatter）
4. 包含title, tooltip, xAxis, yAxis, series等字段
5. series中的data只包含数值数组
6. 使用中文标题和标签
7. 时间序列数据优先使用折线图
8. 分类对比数据优先使用柱状图
9. 占比数据优先使用饼图"""


def generate_chart_config(question: str, data: list[dict]) -> tuple[Optional[str], Optional[dict]]:
    """Generate ECharts chart configuration from query results.

    Returns:
        Tuple of (chart_type, echarts_option_dict)
    """
    if not data:
        return None, None

    prompt = f"用户问题：{question}\n\n查询数据（JSON）：\n{json.dumps(data, ensure_ascii=False, indent=2)}"
    prompt += "\n\n请直接生成ECharts配置JSON："

    result = call_llm(prompt, system_prompt=SYSTEM_PROMPT, temperature=0.2).strip()

    # Clean up markdown code blocks
    if "```json" in result:
        result = result.split("```json", 1)[1]
    elif "```" in result:
        result = result.split("```", 1)[1]
    if "```" in result:
        result = result.rsplit("```", 1)[0]
    result = result.strip()

    try:
        config = json.loads(result)
        chart_type = config.get("series", [{}])[0].get("type", "bar") if config.get("series") else "bar"
        logger.info(f"Generated chart config: type={chart_type}")
        return chart_type, config
    except json.JSONDecodeError as e:
        logger.warning(f"Failed to parse chart config: {e}")
        return _default_chart(data)


def _default_chart(data: list[dict]) -> tuple[str, dict]:
    """Fallback to a default chart when LLM parsing fails."""
    if not data:
        return "bar", {}

    first_row = data[0]
    keys = list(first_row.keys())

    # Find numeric and category columns
    category_key = keys[0]
    value_key = next((k for k in keys if isinstance(first_row[k], (int, float))), keys[-1])

    categories = [str(row[category_key]) for row in data]
    values = [float(row[value_key]) if row[value_key] is not None else 0 for row in data]

    chart_type = "pie" if len(categories) <= 8 else "bar"

    option = {
        "title": {"text": "数据可视化", "left": "center"},
        "tooltip": {"trigger": "item" if chart_type == "pie" else "axis"},
        "xAxis": {"type": "category", "data": categories, "axisLabel": {"rotate": 30}},
        "yAxis": {"type": "value"},
        "series": [{
            "name": value_key,
            "type": chart_type,
            "data": values if chart_type != "pie" else [{"name": c, "value": v} for c, v in zip(categories, values)],
            "itemStyle": {"borderRadius": 4},
        }],
    }
    if chart_type == "pie":
        option.pop("xAxis")
        option.pop("yAxis")

    return chart_type, option
