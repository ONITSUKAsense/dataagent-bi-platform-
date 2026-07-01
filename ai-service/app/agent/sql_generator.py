"""Generate SQL from natural language using LLM."""
from typing import Optional
from app.utils.llm_client import call_qwen
from app.utils.logger import logger

SYSTEM_PROMPT = """你是一个数据分析专家。请根据用户的问题和数据库表结构，生成对应的SQL查询语句。

规则：
1. 只生成SELECT查询语句，不允许INSERT/UPDATE/DELETE/DROP/ALTER
2. 使用标准MySQL语法
3. 字段名用反引号包裹
4. 返回纯粹的SQL语句，不要任何解释或markdown标记
5. 如果无法生成安全的SQL，只返回 "ERROR: 原因"
6. 优先使用中文别名
7. 关注近期数据，默认最近30天"""


def generate_sql(question: str, table_schema: str, reference_docs: str = "") -> str:
    """Generate SQL from natural language question."""
    prompt_parts = [f"用户问题：{question}\n\n数据库表结构：\n{table_schema}"]

    if reference_docs:
        prompt_parts.append(f"\n参考文档：\n{reference_docs}")

    prompt_parts.append("\n请直接生成SQL查询语句：")

    prompt = '\n'.join(prompt_parts)
    logger.info(f"Generating SQL for question: {question}")

    sql = call_qwen(prompt, system_prompt=SYSTEM_PROMPT, temperature=0.1).strip()

    # Clean up markdown code blocks if present
    if sql.startswith("```"):
        sql = sql.split('\n', 1)[-1] if '\n' in sql else sql.replace("```sql", "").replace("```", "")
    if sql.endswith("```"):
        sql = sql.rsplit('\n', 1)[0] if '\n' in sql else sql.replace("```", "")
    sql = sql.strip()

    logger.info(f"Generated SQL: {sql}")
    return sql


def validate_sql(sql: str) -> bool:
    """Validate that SQL is a safe SELECT query."""
    sql_upper = sql.strip().upper()
    if not sql_upper.startswith("SELECT"):
        return False
    dangerous_keywords = ["INSERT", "UPDATE", "DELETE", "DROP", "ALTER", "TRUNCATE",
                          "CREATE", "GRANT", "REVOKE", "INTO OUTFILE", "INTO DUMPFILE"]
    for kw in dangerous_keywords:
        if kw in sql_upper:
            return False
    return True
