from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.api import agent_routes, rag_routes, health_routes

app = FastAPI(
    title="DataAgent AI Service",
    description="AI Agent service for DataAgent BI Platform",
    version="1.0.0",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(health_routes.router, prefix="/api", tags=["Health"])
app.include_router(agent_routes.router, prefix="/api/agent", tags=["Agent"])
app.include_router(rag_routes.router, prefix="/api/knowledge", tags=["RAG"])

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
