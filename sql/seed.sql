-- DataAgent 初始数据
USE dataagent;

-- 默认角色
INSERT INTO sys_role (name, code, sort, status, remark) VALUES
('超级管理员', 'ROLE_ADMIN', 1, 1, '系统超级管理员'),
('普通用户', 'ROLE_USER', 2, 1, '普通用户');

-- 默认权限
INSERT INTO sys_permission (name, code, type, parent_id, sort) VALUES
('Dashboard', 'dashboard:view', 1, 0, 1),
('智能分析', 'chatbi:view', 1, 0, 2),
('智能分析-提问', 'chatbi:ask', 2, 0, 1),
('智能分析-删除', 'chatbi:delete', 2, 0, 2),
('报告中心', 'report:list', 1, 0, 3),
('报告中心-详情', 'report:detail', 2, 0, 1),
('报告中心-编辑', 'report:edit', 2, 0, 2),
('报告中心-删除', 'report:delete', 2, 0, 3),
('报告中心-导出', 'report:export', 2, 0, 4),
('报告中心-分享', 'report:share', 2, 0, 5),
('报告中心-版本', 'report:version', 2, 0, 6),
('知识库', 'knowledge:list', 1, 0, 4),
('知识库-上传', 'knowledge:add', 2, 0, 1),
('知识库-删除', 'knowledge:delete', 2, 0, 2),
('知识库-检索', 'knowledge:search', 2, 0, 3),
('系统管理', 'sys:manage', 1, 0, 10),
('用户管理', 'sys:user:list', 1, 0, 1),
('用户管理-新增', 'sys:user:add', 2, 0, 1),
('用户管理-编辑', 'sys:user:edit', 2, 0, 2),
('用户管理-删除', 'sys:user:delete', 2, 0, 3),
('角色管理', 'sys:role:list', 1, 0, 1),
('角色管理-新增', 'sys:role:add', 2, 0, 1),
('角色管理-编辑', 'sys:role:edit', 2, 0, 2),
('角色管理-删除', 'sys:role:delete', 2, 0, 3),
('角色管理-分配权限', 'sys:role:assign', 2, 0, 4),
('菜单管理', 'sys:menu:list', 1, 0, 1),
('菜单管理-新增', 'sys:menu:add', 2, 0, 1),
('菜单管理-编辑', 'sys:menu:edit', 2, 0, 2),
('菜单管理-删除', 'sys:menu:delete', 2, 0, 3),
('权限管理', 'sys:perm:list', 1, 0, 1);

-- 文件中心
INSERT INTO sys_permission (name, code, type, parent_id, sort) VALUES
('文件中心', 'file:list', 1, 0, 5),
('文件中心-上传', 'file:upload', 2, 0, 1),
('文件中心-删除', 'file:delete', 2, 0, 2);

-- 上下文管理
INSERT INTO sys_permission (name, code, type, parent_id, sort) VALUES
('上下文管理', 'context:list', 1, 0, 6),
('上下文管理-新增', 'context:add', 2, 0, 1),
('上下文管理-编辑', 'context:update', 2, 0, 2),
('上下文管理-删除', 'context:delete', 2, 0, 3);

-- Prompt管理
INSERT INTO sys_permission (name, code, type, parent_id, sort) VALUES
('Prompt管理', 'prompt:list', 1, 0, 7),
('Prompt管理-新增', 'prompt:add', 2, 0, 1),
('Prompt管理-编辑', 'prompt:edit', 2, 0, 2),
('Prompt管理-删除', 'prompt:delete', 2, 0, 3);

-- 日志中心
INSERT INTO sys_permission (name, code, type, parent_id, sort) VALUES
('日志中心', 'log:list', 1, 0, 8);

-- 默认管理员 (密码: admin123)
INSERT INTO sys_user (username, password, nickname, email, role_id, status) VALUES
('admin', '$2b$12$krdNUmVhJMrS5WwNRSno0OtHMfJSKds97ouaKNhsiLsAd6gzQ/4sS', '管理员', 'admin@dataagent.com', 1, 1);

-- 分配管理员权限
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 1, id FROM sys_permission;

-- 默认菜单
INSERT INTO sys_menu (name, permission_code, path, component, icon, parent_id, sort, type) VALUES
('工作台', 'dashboard:view', '/dashboard', 'views/dashboard/DashboardPage.vue', 'Odometer', 0, 1, 1),
('智能分析', 'chatbi:view', '/chatbi', 'views/chatbi/ChatBIPage.vue', 'ChatDotSquare', 0, 2, 1),
('报告中心', 'report:list', '/reports', 'views/report/ReportListPage.vue', 'Document', 0, 3, 1),
('知识库', 'knowledge:list', '/knowledge', 'views/knowledge/KnowledgePage.vue', 'Collection', 0, 4, 1),
('上下文管理', NULL, '/context', 'views/context/ContextPage.vue', 'SetUp', 0, 5, 1),
('Prompt管理', NULL, '/prompts', 'views/prompt/PromptListPage.vue', 'EditPen', 0, 6, 1),
('SQL管理', NULL, '/sql', 'views/sql/SQLHistoryPage.vue', 'Monitor', 0, 7, 1),
('日志中心', NULL, '/logs', 'views/log/LogCenterPage.vue', 'Tickets', 0, 8, 1),
('文件中心', NULL, '/files', 'views/file/FileListPage.vue', 'Folder', 0, 9, 1),
('系统管理', 'sys:manage', NULL, NULL, 'Setting', 0, 10, 0),
('用户管理', 'sys:user:list', '/system/users', 'views/system/UserListPage.vue', 'User', 10, 1, 1),
('角色管理', 'sys:role:list', '/system/roles', 'views/system/RoleListPage.vue', 'Avatar', 10, 2, 1),
('菜单管理', 'sys:menu:list', '/system/menus', 'views/system/MenuListPage.vue', 'Menu', 10, 3, 1),
('权限管理', 'sys:perm:list', '/system/permissions', 'views/system/PermissionListPage.vue', 'Key', 10, 4, 1);

-- 默认 Prompt 模板
INSERT INTO prompt_template (name, code, description, content, variables, version, status) VALUES
('SQL生成', 'SQL_GENERATOR', '将自然语言问题生成SQL查询语句', '你是一个数据分析专家。请根据以下数据库表结构，将用户问题转换为SQL查询语句。\n\n数据库表结构：\n{table_schema}\n\n参考文档：\n{reference_docs}\n\n用户问题：{question}\n\n请直接返回SQL语句，不需要额外解释。', '[{"name":"question","type":"string","required":true,"description":"用户问题"},{"name":"table_schema","type":"string","required":true,"description":"数据库表结构"},{"name":"reference_docs","type":"string","required":false,"description":"参考文档"}]', 1, 1),
('图表生成', 'CHART_GENERATOR', '根据数据生成ECharts图表配置', '你是一个数据分析可视化专家。请根据用户问题和查询结果，生成合适的ECharts图表配置。\n\n用户问题：{question}\n\n查询数据：{data}\n\n请返回ECharts option JSON配置，包含title、xAxis、yAxis、series等字段。', '[{"name":"question","type":"string","required":true,"description":"用户问题"},{"name":"data","type":"string","required":true,"description":"查询数据"}]', 1, 1),
('报告生成', 'REPORT_GENERATOR', '生成分析报告Markdown', '你是一个数据分析报告专家。请根据以下信息生成一份专业的分析报告。\n\n用户问题：{question}\n\n查询SQL：{sql}\n\n查询结果：{data}\n\n图表类型：{chart_type}\n\n请使用Markdown格式，包含：\n1. 分析摘要\n2. 数据概览\n3. 关键发现\n4. 结论与建议', '[{"name":"question","type":"string","required":true,"description":"用户问题"},{"name":"sql","type":"string","required":true,"description":"执行的SQL"},{"name":"data","type":"string","required":true,"description":"查询数据"},{"name":"chart_type","type":"string","required":false,"description":"图表类型"}]', 1, 1);
