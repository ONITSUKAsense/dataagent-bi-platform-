from fastapi import APIRouter, HTTPException
from app.models.chat import AskRequest
from app.models.agent import AgentResult
from app.agent.workflow import run_workflow
from app.utils.logger import logger

router = APIRouter()


@router.post("/run", response_model=AgentResult)
async def run_agent(request: AskRequest):
    """Execute the full Agent workflow: Memory → Prompt → RAG → Tool Calling → SQL → Chart → Report"""
    try:
        logger.info(f"Agent run requested: {request.question[:100]}")
        result = run_workflow(
            question=request.question,
            session_id=request.session_id,
        )
        return result
    except Exception as e:
        logger.error(f"Agent workflow failed: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@router.post("/sql/generate")
async def generate_sql_endpoint(request: AskRequest):
    """Generate SQL from natural language (standalone)."""
    try:
        from app.agent.tools import get_table_schema
        from app.agent.sql_generator import generate_sql as gen_sql

        schema = get_table_schema()
        sql = gen_sql(request.question, schema)
        return {"sql": sql, "session_id": request.session_id}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.post("/chart/generate")
async def generate_chart_endpoint(request: AskRequest):
    """Generate ECharts config from query result (standalone)."""
    try:
        from app.agent.chart_generator import generate_chart_config

        chart_type, option = generate_chart_config(request.question, [])
        return {"chartType": chart_type, "option": option}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.post("/report/generate")
async def generate_report_endpoint(request: AskRequest):
    """Generate markdown report from analysis result (standalone)."""
    try:
        from app.agent.report_generator import generate_report as gen_report

        report = gen_report(request.question, "", [])
        return {"report_md": report, "session_id": request.session_id}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
