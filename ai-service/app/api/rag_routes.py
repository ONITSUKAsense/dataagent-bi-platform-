import os
import uuid
from fastapi import APIRouter, UploadFile, File, HTTPException, BackgroundTasks
from app.models.knowledge import RetrieveRequest, ChunkResult
from app.rag.pipeline import process_document, delete_document
from app.rag.retriever import retrieve
from app.utils.logger import logger

router = APIRouter()
UPLOAD_DIR = "data/uploads"


@router.post("/docs/upload")
async def upload_document(file: UploadFile = File(...)):
    """Upload and process a document (PDF/Word/Markdown) through the RAG pipeline."""
    os.makedirs(UPLOAD_DIR, exist_ok=True)
    ext = os.path.splitext(file.filename or "")[1].lower()

    if ext not in ('.pdf', '.docx', '.md', '.txt'):
        raise HTTPException(status_code=400, detail=f"Unsupported file type: {ext}")

    # Save file
    file_id = str(uuid.uuid4())
    save_path = os.path.join(UPLOAD_DIR, f"{file_id}{ext}")
    content = await file.read()

    with open(save_path, "wb") as f:
        f.write(content)

    # Process through RAG pipeline (synchronously for now)
    result = process_document(
        file_path=save_path,
        doc_id=hash(file_id) % (2**31),  # Simple ID mapping
        title=file.filename or "untitled",
    )

    return {
        "id": file_id,
        "filename": file.filename,
        "size": len(content),
        "save_path": save_path,
        "processing": result,
    }


@router.post("/retrieve", response_model=list[ChunkResult])
async def retrieve_knowledge(request: RetrieveRequest):
    """Retrieve relevant knowledge chunks using embedding + FAISS search."""
    try:
        results = retrieve(query=request.query, top_k=request.top_k)
        return [
            ChunkResult(
                doc_id=r["doc_id"],
                chunk_index=r["chunk_index"],
                content=r["content"],
                score=r["score"],
            )
            for r in results
        ]
    except Exception as e:
        logger.error(f"Retrieval failed: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@router.delete("/docs/{doc_id}")
async def delete_document_route(doc_id: int):
    """Delete a document and its chunks from the vector store."""
    try:
        delete_document(doc_id)
        return {"status": "deleted", "doc_id": doc_id}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
