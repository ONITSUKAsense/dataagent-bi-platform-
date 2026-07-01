from pydantic import BaseModel
from typing import Optional, Any

class AgentStep(BaseModel):
    name: str
    input: Optional[Any] = None
    output: Optional[Any] = None
    cost_ms: Optional[int] = None
    tokens: Optional[int] = None

class AgentResult(BaseModel):
    session_id: str
    sql: Optional[str] = None
    sql_result: Optional[list] = None
    chart_option: Optional[dict] = None
    chart_type: Optional[str] = None
    report_md: str
    steps: list[AgentStep] = []
    total_tokens: int = 0
    total_cost_ms: int = 0
