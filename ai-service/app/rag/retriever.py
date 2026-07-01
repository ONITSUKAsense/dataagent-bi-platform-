"""Document retriever combining embedding and vector search."""
from typing import List, Optional
from app.rag.embeddings import get_embeddings
from app.rag.vector_store import get_store
from app.utils.logger import logger


def retrieve(query: str, top_k: int = 5) -> List[dict]:
    """Retrieve relevant document chunks for a query.

    Args:
        query: Natural language query
        top_k: Number of results to return

    Returns:
        List of {doc_id, chunk_index, content, score} dicts
    """
    store = get_store()
    if not store.chunks:
        logger.info("Vector store is empty")
        return []

    # Get query embedding
    embeddings = get_embeddings([query])
    if not embeddings or len(embeddings) == 0:
        logger.warning("Failed to get query embedding")
        return []

    query_embedding = embeddings[0]

    # Search vector store
    results = store.search(query_embedding, top_k)

    return [
        {
            "doc_id": chunk.get("doc_id"),
            "chunk_index": chunk.get("chunk_index"),
            "content": chunk.get("content"),
            "score": round(score, 4),
        }
        for chunk, score in results
    ]


def format_retrieved_context(results: List[dict], max_chars: int = 2000) -> str:
    """Format retrieved chunks into a context string for LLM prompt."""
    if not results:
        return ""

    parts = ["参考文档："]

    for i, r in enumerate(results, 1):
        content = r.get("content", "")
        if len(content) > max_chars // len(results):
            content = content[:max_chars // len(results)] + "..."

        parts.append(f"\n[{i}] (相关度: {r.get('score', 0)})\n{content}")

    context = '\n'.join(parts)

    if len(context) > max_chars:
        context = context[:max_chars] + "\n...(截断)"

    return context
