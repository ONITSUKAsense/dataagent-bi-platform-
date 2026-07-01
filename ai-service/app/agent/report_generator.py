"""Generate markdown report from analysis results using LLM."""
from typing import Optional
from app.utils.llm_client import call_qwen
from app.utils.logger import logger

SYSTEM_PROMPT = """你是一个数据分析报告专家。请根据分析结果生成专业的数据分析报告。

报告格式要求（Markdown）：
1. **分析摘要** - 用一句话概括分析目的和结论
2. **数据概览** - 关键数据指标和统计
3. **关键发现** - 分点列出重要发现
4. **结论与建议** - 数据驱动的建议

要求：
- 语言简洁专业
- 数据说话，引用具体数值
- 使用Markdown格式
- 重点突出，条理清晰"""


def generate_report(question: str, sql: str, data: list[dict], chart_type: Optional[str] = None) -> str:
    """Generate a markdown analysis report."""
    if not data:
        return f"""## 数据分析报告

### 分析摘要
针对问题「{question}」的分析已完成。

### 数据概览
本次查询未返回数据。

### 结论与建议
请检查数据源或调整查询条件。"""

    # For simple data, generate a basic report without LLM
    if len(data) <= 20 and len(data) > 0:
        import json
        prompt = f"""用户问题：{question}
执行的SQL：{sql}
查询结果：{json.dumps(data, ensure_ascii=False, indent=2)}
图表类型：{chart_type or '无'}

请生成Markdown格式的分析报告。"""

        report = call_qwen(prompt, system_prompt=SYSTEM_PROMPT, temperature=0.3).strip()

        if report.startswith("```"):
            report = report.split('\n', 1)[-1] if '\n' in report else report.replace("```markdown", "").replace("```", "")
        if report.endswith("```"):
            report = report.rsplit('\n', 1)[0] if '\n' in report else report.replace("```", "")

        return report.strip()

    # Fallback: generate a simple structured report from data
    first_row = data[0]
    keys = list(first_row.keys())
    value_keys = [k for k in keys if isinstance(first_row[k], (int, float))]

    summary_lines = [f"### 分析摘要\n针对问题「{question}」的分析已完成。"]
    overview = "### 数据概览\n| 指标 | 数值 |\n|------|------|\n"
    overview += f"| 数据行数 | {len(data)} |\n"
    if value_keys:
        total = sum(float(row[k] or 0) for row in data for k in value_keys)
        overview += f"| 合计 | {total:,.2f} |\n"
    overview += f"| 图表类型 | {chart_type or '无'} |\n"

    findings = "### 关键发现\n"
    if value_keys and len(data) > 1:
        vals = [sum(float(row[k] or 0) for k in value_keys) for row in data]
        findings += f"- 最大值出现在第 {vals.index(max(vals)) + 1} 行，值为 {max(vals):,.2f}\n"
        findings += f"- 最小值出现在第 {vals.index(min(vals)) + 1} 行，值为 {min(vals):,.2f}\n"
        findings += f"- 数据覆盖 {len(data)} 条记录\n"

    conclusion = "### 结论与建议\n- 建议持续监控数据趋势\n- 可进一步细分维度进行分析\n"

    return '\n\n'.join(summary_lines + [overview, findings, conclusion])
