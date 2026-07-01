import os

# LLM Provider: qwen / deepseek
LLM_PROVIDER = os.getenv("LLM_PROVIDER", "qwen")

# Qwen API
QWEN_API_KEY = os.getenv("QWEN_API_KEY", "")
QWEN_MODEL = os.getenv("QWEN_MODEL", "qwen-turbo")
EMBEDDING_MODEL = os.getenv("EMBEDDING_MODEL", "text-embedding-v2")

# DeepSeek API
DEEPSEEK_API_KEY = os.getenv("DEEPSEEK_API_KEY", "")
DEEPSEEK_MODEL = os.getenv("DEEPSEEK_MODEL", "deepseek-chat")

# Get active LLM config based on provider
def get_llm_config():
    if LLM_PROVIDER == "deepseek":
        return {
            "provider": "deepseek",
            "api_key": DEEPSEEK_API_KEY,
            "model": DEEPSEEK_MODEL,
            "base_url": "https://api.deepseek.com/v1",
        }
    return {
        "provider": "qwen",
        "api_key": QWEN_API_KEY,
        "model": QWEN_MODEL,
        "base_url": "https://dashscope.aliyuncs.com/compatible-mode/v1",
    }

# Redis
REDIS_HOST = os.getenv("REDIS_HOST", "localhost")
REDIS_PORT = int(os.getenv("REDIS_PORT", "6379"))

# MySQL
MYSQL_HOST = os.getenv("MYSQL_HOST", "localhost")
MYSQL_USER = os.getenv("MYSQL_USER", "root")
MYSQL_PASSWORD = os.getenv("MYSQL_PASSWORD", "root")
MYSQL_DATABASE = os.getenv("MYSQL_DATABASE", "dataagent")

# Vector Store
VECTOR_STORE_PATH = os.getenv("VECTOR_STORE_PATH", "vector_store")

# Agent settings
MAX_HISTORY_ROUNDS = 5
SQL_MAX_ROWS = 200
