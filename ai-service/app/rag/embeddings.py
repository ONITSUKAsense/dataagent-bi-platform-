"""Embedding generation using Qwen API."""
import json
import http.client
from typing import List, Optional
from app.config import QWEN_API_KEY, EMBEDDING_MODEL
from app.utils.logger import logger


def get_embeddings(texts: List[str]) -> Optional[List[List[float]]]:
    """Generate embeddings for a list of texts using Qwen API.

    Returns list of embedding vectors, or None on failure.
    """
    if not texts:
        return []

    if not QWEN_API_KEY:
        logger.warning("QWEN_API_KEY not set, returning random embeddings")
        import numpy as np
        rng = np.random.RandomState(42)
        return [rng.randn(1024).tolist() for _ in texts]

    try:
        conn = http.client.HTTPSConnection("dashscope.aliyuncs.com")

        payload = json.dumps({
            "model": EMBEDDING_MODEL,
            "input": {"texts": texts},
            "parameters": {"text_type": "document"}
        })

        headers = {
            'Authorization': f'Bearer {QWEN_API_KEY}',
            'Content-Type': 'application/json'
        }

        conn.request("POST", "/api/v1/services/embeddings/text-embedding/text-embedding",
                     payload, headers)
        res = conn.getresponse()
        data = json.loads(res.read().decode())

        if data.get("output") and data["output"].get("embeddings"):
            embeddings = [item["embedding"] for item in data["output"]["embeddings"]]
            return embeddings
        else:
            logger.error(f"Embedding API error: {data}")
            return None

    except Exception as e:
        logger.error(f"Embedding API call failed: {e}")
        return None
