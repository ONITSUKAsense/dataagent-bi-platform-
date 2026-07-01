-- DataAgent 智能 BI 报告平台 数据库初始化脚本
-- MySQL 8.0+

CREATE DATABASE IF NOT EXISTS dataagent DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE dataagent;

-- ==================== 系统管理 ====================

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码(BCrypt)',
    nickname VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    avatar VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    role_id BIGINT DEFAULT NULL COMMENT '角色ID',
    status TINYINT DEFAULT 1 COMMENT '状态: 0禁用 1启用',
    remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE COMMENT '角色名称',
    code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态',
    remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 权限表
CREATE TABLE IF NOT EXISTS sys_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '权限名称',
    code VARCHAR(100) NOT NULL UNIQUE COMMENT '权限编码',
    type TINYINT NOT NULL COMMENT '类型: 1菜单 2按钮 3接口',
    parent_id BIGINT DEFAULT 0 COMMENT '父级ID',
    sort INT DEFAULT 0 COMMENT '排序',
    remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 菜单表
CREATE TABLE IF NOT EXISTS sys_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '菜单名称',
    permission_code VARCHAR(100) DEFAULT NULL COMMENT '权限编码',
    path VARCHAR(200) DEFAULT NULL COMMENT '路由路径',
    component VARCHAR(200) DEFAULT NULL COMMENT '组件路径',
    icon VARCHAR(100) DEFAULT NULL COMMENT '图标',
    parent_id BIGINT DEFAULT 0 COMMENT '父菜单ID',
    sort INT DEFAULT 0 COMMENT '排序',
    type TINYINT NOT NULL COMMENT '类型: 0目录 1菜单 2按钮',
    visible TINYINT DEFAULT 1 COMMENT '是否可见',
    status TINYINT DEFAULT 1 COMMENT '状态',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单表';

-- 角色-权限关联表
CREATE TABLE IF NOT EXISTS sys_role_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    UNIQUE KEY uk_role_perm (role_id, permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- 角色-菜单关联表
CREATE TABLE IF NOT EXISTS sys_role_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    UNIQUE KEY uk_role_menu (role_id, menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

-- ==================== ChatBI ====================

-- 聊天记录表
CREATE TABLE IF NOT EXISTS chat_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    session_id VARCHAR(64) NOT NULL COMMENT '会话ID',
    role VARCHAR(20) NOT NULL COMMENT 'user/assistant/system',
    content TEXT NOT NULL COMMENT '消息内容',
    message_type VARCHAR(20) DEFAULT 'text' COMMENT 'text/chart/report',
    metadata JSON DEFAULT NULL COMMENT '附加元数据',
    token_count INT DEFAULT 0 COMMENT 'Token数',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_session_id (session_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天记录表';

-- 上下文表
CREATE TABLE IF NOT EXISTS chat_context (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id VARCHAR(64) NOT NULL COMMENT '会话ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    context_key VARCHAR(255) NOT NULL COMMENT '上下文键',
    context_value TEXT NOT NULL COMMENT '上下文值(JSON)',
    expire_at DATETIME DEFAULT NULL COMMENT '过期时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_session_id (session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='上下文表';

-- ==================== Prompt ====================

CREATE TABLE IF NOT EXISTS prompt_template (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '模板名称',
    code VARCHAR(100) NOT NULL UNIQUE COMMENT '模板编码',
    description VARCHAR(500) DEFAULT NULL COMMENT '描述',
    content TEXT NOT NULL COMMENT '模板内容',
    variables JSON DEFAULT NULL COMMENT '变量定义',
    version INT DEFAULT 1 COMMENT '版本号',
    status TINYINT DEFAULT 0 COMMENT '0草稿 1发布',
    created_by BIGINT DEFAULT NULL COMMENT '创建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Prompt模板表';

-- ==================== RAG ====================

-- 知识库文档表
CREATE TABLE IF NOT EXISTS knowledge_doc (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL COMMENT '文档标题',
    doc_type VARCHAR(20) NOT NULL COMMENT 'pdf/word/markdown',
    file_id BIGINT DEFAULT NULL COMMENT '关联文件ID',
    content LONGTEXT DEFAULT NULL COMMENT '原始文本内容',
    chunk_count INT DEFAULT 0 COMMENT '切片数',
    status TINYINT DEFAULT 0 COMMENT '0处理中 1已就绪 2失败',
    created_by BIGINT DEFAULT NULL COMMENT '上传人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库文档表';

-- 文档切片表
CREATE TABLE IF NOT EXISTS knowledge_chunk (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    doc_id BIGINT NOT NULL COMMENT '文档ID',
    chunk_index INT NOT NULL COMMENT '切片序号',
    content TEXT NOT NULL COMMENT '切片文本',
    embedding_id VARCHAR(64) DEFAULT NULL COMMENT 'FAISS向量ID',
    token_count INT DEFAULT 0 COMMENT 'Token数',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_doc_id (doc_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文档切片表';

-- ==================== 报告 ====================

-- 报告表
CREATE TABLE IF NOT EXISTS report (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL COMMENT '报告标题',
    description VARCHAR(500) DEFAULT NULL COMMENT '描述',
    content_md LONGTEXT DEFAULT NULL COMMENT 'Markdown内容',
    content_html LONGTEXT DEFAULT NULL COMMENT 'HTML内容',
    chart_config JSON DEFAULT NULL COMMENT 'ECharts配置',
    data_json JSON DEFAULT NULL COMMENT '原始数据',
    session_id VARCHAR(64) DEFAULT NULL COMMENT '关联会话',
    status TINYINT DEFAULT 0 COMMENT '0生成中 1已生成 2失败',
    version INT DEFAULT 1 COMMENT '当前版本',
    created_by BIGINT DEFAULT NULL COMMENT '创建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报告表';

-- 报告版本表
CREATE TABLE IF NOT EXISTS report_version (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    report_id BIGINT NOT NULL COMMENT '报告ID',
    version INT NOT NULL COMMENT '版本号',
    content_md LONGTEXT DEFAULT NULL COMMENT 'Markdown内容',
    content_html LONGTEXT DEFAULT NULL COMMENT 'HTML内容',
    chart_config JSON DEFAULT NULL COMMENT '图表配置',
    change_note VARCHAR(500) DEFAULT NULL COMMENT '变更说明',
    created_by BIGINT DEFAULT NULL COMMENT '创建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_report_id (report_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报告版本表';

-- ==================== 文件 ====================

CREATE TABLE IF NOT EXISTS file (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    original_name VARCHAR(255) NOT NULL COMMENT '原文件名',
    stored_name VARCHAR(255) NOT NULL COMMENT '存储文件名',
    path VARCHAR(500) NOT NULL COMMENT '存储路径',
    size BIGINT NOT NULL COMMENT '文件大小(byte)',
    type VARCHAR(50) NOT NULL COMMENT 'MIME类型',
    ext VARCHAR(20) DEFAULT NULL COMMENT '扩展名',
    storage_type VARCHAR(20) DEFAULT 'local' COMMENT 'local/oss',
    url VARCHAR(500) DEFAULT NULL COMMENT '访问URL',
    created_by BIGINT DEFAULT NULL COMMENT '上传人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件表';

-- ==================== SQL ====================

CREATE TABLE IF NOT EXISTS sql_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '执行人',
    session_id VARCHAR(64) DEFAULT NULL COMMENT '关联会话',
    original_question VARCHAR(500) DEFAULT NULL COMMENT '原始问题',
    generated_sql TEXT NOT NULL COMMENT '生成的SQL',
    execute_result JSON DEFAULT NULL COMMENT '执行结果',
    row_count INT DEFAULT NULL COMMENT '影响/返回行数',
    cost_ms INT DEFAULT NULL COMMENT '执行耗时(ms)',
    status TINYINT DEFAULT 0 COMMENT '0成功 1失败',
    error_msg TEXT DEFAULT NULL COMMENT '错误信息',
    is_favorite TINYINT DEFAULT 0 COMMENT '是否收藏',
    token_cost INT DEFAULT 0 COMMENT 'Token消耗',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_session_id (session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SQL执行历史表';

-- ==================== 日志 ====================

CREATE TABLE IF NOT EXISTS agent_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT DEFAULT NULL COMMENT '用户ID',
    session_id VARCHAR(64) DEFAULT NULL COMMENT '会话ID',
    agent_type VARCHAR(50) NOT NULL COMMENT 'Agent类型',
    input TEXT DEFAULT NULL COMMENT '输入内容',
    output TEXT DEFAULT NULL COMMENT '输出内容',
    steps JSON DEFAULT NULL COMMENT 'Workflow各步骤详情',
    llm_model VARCHAR(50) DEFAULT NULL COMMENT 'LLM模型名',
    prompt_tokens INT DEFAULT 0 COMMENT 'Prompt Token数',
    completion_tokens INT DEFAULT 0 COMMENT '生成Token数',
    total_tokens INT DEFAULT 0 COMMENT '总Token数',
    cost_ms INT DEFAULT NULL COMMENT '总耗时',
    status TINYINT DEFAULT 0 COMMENT '0成功 1失败',
    error_msg TEXT DEFAULT NULL COMMENT '错误信息',
    ip VARCHAR(50) DEFAULT NULL COMMENT '请求IP',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Agent日志表';
