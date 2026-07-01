from loguru import logger
import sys

logger.remove()
logger.add(
    sys.stdout,
    format="<green>{time:YYYY-MM-DD HH:mm:ss}</green> | <level>{level}</level> | <cyan>{name}</cyan> - <level>{message}</level>",
    level="INFO",
)
logger.add("logs/ai-service.log", rotation="1 day", retention="7 days", level="DEBUG")

__all__ = ["logger"]
