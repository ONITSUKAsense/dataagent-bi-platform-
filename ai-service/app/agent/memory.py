"""Conversation memory management using Redis."""
import json
import redis
from typing import Optional
from app.config import REDIS_HOST, REDIS_PORT, MAX_HISTORY_ROUNDS
from app.utils.logger import logger

redis_client: Optional[redis.Redis] = None

def get_redis() -> redis.Redis:
    global redis_client
    if redis_client is None:
        try:
            redis_client = redis.Redis(
                host=REDIS_HOST, port=REDIS_PORT, db=0,
                decode_responses=True, socket_timeout=3
            )
        except Exception as e:
            logger.warning(f"Redis connection failed: {e}")
            redis_client = None
    return redis_client


def load_history(session_id: str) -> list[dict]:
    """Load recent conversation history."""
    r = get_redis()
    if r is None:
        return []
    try:
        key = f"context:session:{session_id}"
        history_json = r.get(key)
        if history_json:
            history = json.loads(history_json)
            return history[-MAX_HISTORY_ROUNDS * 2:]  # Last N rounds
    except Exception as e:
        logger.warning(f"Failed to load history: {e}")
    return []


def save_history(session_id: str, messages: list[dict]):
    """Save conversation history."""
    r = get_redis()
    if r is None:
        return
    try:
        key = f"context:session:{session_id}"
        r.setex(key, 3600, json.dumps(messages))  # 1h TTL
    except Exception as e:
        logger.warning(f"Failed to save history: {e}")
