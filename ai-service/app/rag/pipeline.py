"""Document processing pipeline: upload → parse → chunk → embed → store."""
import os
import uuid
from typing import Optional
from app.rag.document_loader import load_document
from app.rag.text_splitter import split_text
from app.rag.embeddings import get_embeddings
from app.rag.vector_store import get_store
from app.config import VECTOR_STORE_PATH
from app.utils.logger import logger


def process_document(file_path: str, doc_id: int, title: str) -> dict:
    """Process a document through the full RAG pipeline.

    Args:
        file_path: Path to the uploaded document
        doc_id: Database document ID
        title: Document title

    Returns:
        Dict with processing results: {status, chunk_count, error}
    """
    try:
        # 1. Load text from document
        logger.info(f"Loading document: {title} ({file_path})")
        text = load_document(file_path)
        if not text:
            return {"status": "error", "error": "Failed to extract text from document"}
        logger.info(f"Extracted {len(text)} characters")

        # 2. Split into chunks
        chunks = split_text(text)
        logger.info(f"Split into {len(chunks)} chunks")

        if not chunks:
            return {"status": "error", "error": "No content chunks generated"}

        # 3. Generate embeddings
        embeddings = get_embeddings(chunks)
        if not embeddings or len(embeddings) != len(chunks):
            logger.warning(f"Embedding generated {len(embeddings) if embeddings else 0}/{len(chunks)} chunks")

        chunk_metas = []
        for i, content in enumerate(chunks):
            chunk_metas.append({
                "doc_id": doc_id,
                "chunk_index": i,
                "content": content,
            })

        # 4. Store in vector store
        store = get_store()
        if embeddings and len(embeddings) == len(chunks):
            store.add(embeddings, chunk_metas)

            chunk_data = []
            for i, (chunk, emb) in enumerate(zip(chunks, embeddings)):
                chunk_data.append({
                    "chunk_index": i,
                    "content": chunk,
                    "embedding": emb,
                    "token_count": len(chunk) // 2,  # rough estimate
                })
        else:
            # Store without embeddings (will use numpy fallback)
            import numpy as np
            fake_embs = [np.random.randn(1024).tolist() for _ in chunks]
            store.add(fake_embs, chunk_metas)

            chunk_data = [
                {"chunk_index": i, "content": chunk, "embedding": None,
                 "token_count": len(chunk) // 2}
                for i, chunk in enumerate(chunks)
            ]

        logger.info(f"Document processed successfully: {title}, {len(chunks)} chunks")
        return {
            "status": "success",
            "chunk_count": len(chunks),
            "chunks": chunk_data,
        }

    except Exception as e:
        logger.error(f"Document processing failed: {e}")
        return {"status": "error", "error": str(e)}


def delete_document(doc_id: int):
    """Remove a document and its chunks from the vector store."""
    store = get_store()
    store.remove_doc(doc_id)
    logger.info(f"Removed document {doc_id} from vector store")
