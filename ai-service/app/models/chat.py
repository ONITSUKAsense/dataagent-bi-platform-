from pydantic import BaseModel
from typing import Optional, Any

class AskRequest(BaseModel):
    question: str
    session_id: Optional[str] = None

class AskResponse(BaseModel):
    session_id: str
    answer: str
    chart_option: Optional[dict] = None
    chart_type: Optional[str] = None
    sql: Optional[str] = None
    tables: Optional[list] = None
    report_md: Optional[str] = None
    token_usage: Optional[dict] = None
