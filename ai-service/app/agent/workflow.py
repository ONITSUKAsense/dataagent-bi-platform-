"""Agent Workflow orchestrator.

流程: 用户问题 → Memory → Prompt → RAG → Tool Calling → SQL Generator → SQL Execute → Chart Generator → Report Generator → 返回
"""
import json
import time
import uuid
from typing import Optional

from app.agent.memory import load_history, save_history
from app.agent.tools import get_table_schema, execute_sql
from app.agent.sql_generator import generate_sql, validate_sql
from app.agent.chart_generator import generate_chart_config
from app.agent.report_generator import generate_report
from app.rag.retriever import retrieve, format_retrieved_context
from app.models.agent import AgentResult, AgentStep
from app.utils.logger import logger


def run_workflow(question: str, session_id: Optional[str] = None) -> AgentResult:
    """Execute the full Agent workflow."""
    if not session_id:
        session_id = str(uuid.uuid4())

    start_time = time.time()
    steps: list[AgentStep] = []
    total_tokens = 0

    logger.info(f"Starting workflow: session={session_id}, question={question}")

    # Step 1: Memory - Load conversation history
    step = AgentStep(name="Memory")
    try:
        history = load_history(session_id)
        step.output = f"Loaded {len(history)} history messages"
        steps.append(step)
    except Exception as e:
        step.output = f"Memory load failed: {e}"
        steps.append(step)
        history = []

    # Step 1.5: RAG - Retrieve relevant knowledge
    step = AgentStep(name="RAG")
    reference_docs = ""
    try:
        rag_results = retrieve(question, top_k=3)
        if rag_results:
            reference_docs = format_retrieved_context(rag_results)
            step.output = f"Retrieved {len(rag_results)} relevant chunks"
        else:
            step.output = "No relevant knowledge found"
        steps.append(step)
    except Exception as e:
        step.output = f"RAG retrieval failed: {e}"
        steps.append(step)

    # Step 2: Tool Calling - Get table schema
    step = AgentStep(name="Tool Calling")
    try:
        table_schema = get_table_schema()
        step.output = f"Loaded schema for database"
        steps.append(step)
    except Exception as e:
        table_schema = ""
        step.output = f"Schema load failed: {e}"
        steps.append(step)

    # Step 3: SQL Generator
    step = AgentStep(name="SQL Generator")
    sql = None
    try:
        # Build context from history
        history_context = ""
        if history:
            history_context = "\n".join([
                f"{'用户' if m.get('role') == 'user' else 'AI'}: {m.get('content', '')[:200]}"
                for m in history[-4:]
            ])

        reference_docs = ""
        if history_context:
            reference_docs = f"对话历史：\n{history_context}"

        sql = generate_sql(question, table_schema, reference_docs)
        step.output = sql[:200]
        steps.append(step)
    except Exception as e:
        step.output = f"SQL generation failed: {e}"
        steps.append(step)

    # Step 4: SQL Execute
    step = AgentStep(name="SQL Execute")
    sql_result = []
    try:
        if sql and validate_sql(sql):
            sql_result = execute_sql(sql)
            step.output = f"Returned {len(sql_result)} rows"
        else:
            step.output = "SQL validation failed or no SQL generated"
            sql_result = []
        steps.append(step)
    except Exception as e:
        step.output = f"SQL execution failed: {e}"
        sql_result = []
        steps.append(step)

    # Step 5: Chart Generator
    step = AgentStep(name="Chart Generator")
    chart_type = None
    chart_option = None
    try:
        chart_type, chart_option = generate_chart_config(question, sql_result)
        step.output = f"Generated {chart_type} chart"
        steps.append(step)
    except Exception as e:
        step.output = f"Chart generation failed: {e}"
        steps.append(step)

    # Step 6: Report Generator
    step = AgentStep(name="Report Generator")
    report_md = ""
    try:
        report_md = generate_report(question, sql or "", sql_result, chart_type)
        step.output = f"Generated report ({len(report_md)} chars)"
        steps.append(step)
    except Exception as e:
        report_md = f"## 分析报告\n\n### 分析摘要\n针对问题「{question}」的分析已完成。\n\n### 结论\n报告生成过程中出现错误，请重试。"
        step.output = f"Report generation failed: {e}"
        steps.append(step)

    # Save conversation history
    history.append({"role": "user", "content": question})
    history.append({"role": "assistant", "content": report_md[:500]})
    try:
        save_history(session_id, history)
    except Exception as e:
        logger.warning(f"Failed to save history: {e}")

    total_cost_ms = int((time.time() - start_time) * 1000)

    # Update step timing
    for s in steps:
        s.cost_ms = total_cost_ms // len(steps)

    result = AgentResult(
        session_id=session_id,
        sql=sql,
        sql_result=sql_result,
        chart_option=chart_option,
        chart_type=chart_type,
        report_md=report_md,
        steps=steps,
        total_tokens=total_tokens,
        total_cost_ms=total_cost_ms,
    )

    logger.info(f"Workflow completed: {total_cost_ms}ms, {len(steps)} steps")
    return result
