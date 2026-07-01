"""Tool definitions for database schema querying."""
import pymysql
from typing import Optional
from app.config import MYSQL_HOST, MYSQL_USER, MYSQL_PASSWORD, MYSQL_DATABASE
from app.utils.logger import logger

_connection: Optional[pymysql.Connection] = None


def get_connection() -> Optional[pymysql.Connection]:
    global _connection
    try:
        if _connection is None or not _connection.open:
            _connection = pymysql.connect(
                host=MYSQL_HOST,
                user=MYSQL_USER,
                password=MYSQL_PASSWORD,
                database=MYSQL_DATABASE,
                charset='utf8mb4',
                cursorclass=pymysql.cursors.DictCursor,
                connect_timeout=5,
            )
    except Exception as e:
        logger.error(f"Database connection failed: {e}")
        return None
    return _connection


def get_table_schema() -> str:
    """Get database table schemas for LLM context."""
    conn = get_connection()
    if conn is None:
        return _default_schema()

    try:
        with conn.cursor() as cursor:
            cursor.execute("""
                SELECT TABLE_NAME, TABLE_COMMENT
                FROM information_schema.TABLES
                WHERE TABLE_SCHEMA = %s AND TABLE_TYPE = 'BASE TABLE'
            """, (MYSQL_DATABASE,))
            tables = cursor.fetchall()

            schema_parts = []
            for t in tables:
                table_name = t['TABLE_NAME']
                table_comment = t['TABLE_COMMENT'] or ''
                schema_parts.append(f"\n### {table_name} ({table_comment})")

                cursor.execute("""
                    SELECT COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE, COLUMN_COMMENT
                    FROM information_schema.COLUMNS
                    WHERE TABLE_SCHEMA = %s AND TABLE_NAME = %s
                    ORDER BY ORDINAL_POSITION
                """, (MYSQL_DATABASE, table_name))
                columns = cursor.fetchall()

                for col in columns:
                    nullable = "NULL" if col['IS_NULLABLE'] == 'YES' else "NOT NULL"
                    comment = col['COLUMN_COMMENT'] or ''
                    schema_parts.append(
                        f"  - {col['COLUMN_NAME']} {col['COLUMN_TYPE']} {nullable} {comment}"
                    )
            return '\n'.join(schema_parts)
    except Exception as e:
        logger.error(f"Failed to get schema: {e}")
        return _default_schema()


def _default_schema() -> str:
    """Fallback schema when DB is unavailable."""
    return """
### sys_user (用户表)
  - id BIGINT PK
  - username VARCHAR(50) 用户名
  - password VARCHAR(255) 密码
  - nickname VARCHAR(50) 昵称
  - email VARCHAR(100) 邮箱
  - role_id BIGINT 角色ID
  - status TINYINT 状态
  - created_at DATETIME 创建时间

### report (报告表)
  - id BIGINT PK
  - title VARCHAR(200) 报告标题
  - content_md LONGTEXT Markdown内容
  - status TINYINT 状态
  - created_by BIGINT 创建人
  - created_at DATETIME 创建时间

### agent_log (Agent日志表)
  - id BIGINT PK
  - user_id BIGINT 用户ID
  - session_id VARCHAR(64) 会话ID
  - agent_type VARCHAR(50) Agent类型
  - total_tokens INT Token数
  - cost_ms INT 耗时
  - created_at DATETIME 创建时间
"""


def execute_sql(sql: str, max_rows: int = 200) -> list[dict]:
    """Execute generated SQL and return results."""
    conn = get_connection()
    if conn is None:
        return [{"message": "Database unavailable"}]

    try:
        with conn.cursor() as cursor:
            cursor.execute(sql)
            result = cursor.fetchmany(max_rows)
            return [dict(row) for row in result]
    except Exception as e:
        logger.error(f"SQL execution failed: {e}")
        return [{"error": str(e)}]
