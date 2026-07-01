from pydantic import BaseModel
from typing import Optional

class RetrieveRequest(BaseModel):
    query: str
    top_k: int = 5

class ChunkResult(BaseModel):
    doc_id: int
    chunk_index: int
    content: str
    score: float
