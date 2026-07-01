import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/LoginPage.vue'),
    meta: { title: '登录', noAuth: true },
  },
  {
    path: '/',
    component: () => import('@/components/AppLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/DashboardPage.vue'),
        meta: { title: '工作台', icon: 'Odometer' },
      },
      {
        path: 'chatbi',
        name: 'ChatBI',
        component: () => import('@/views/chatbi/ChatBIPage.vue'),
        meta: { title: '智能分析', icon: 'ChatDotSquare' },
      },
      {
        path: 'chatbi/:sessionId',
        name: 'ChatBISession',
        component: () => import('@/views/chatbi/ChatBISessionPage.vue'),
        meta: { title: '对话', hidden: true },
      },
      {
        path: 'reports',
        name: 'Reports',
        component: () => import('@/views/report/ReportListPage.vue'),
        meta: { title: '报告中心', icon: 'Document' },
      },
      {
        path: 'reports/:id',
        name: 'ReportDetail',
        component: () => import('@/views/report/ReportDetailPage.vue'),
        meta: { title: '报告详情', hidden: true },
      },
      {
        path: 'knowledge',
        name: 'Knowledge',
        component: () => import('@/views/knowledge/KnowledgePage.vue'),
        meta: { title: '知识库', icon: 'Collection' },
      },
      {
        path: 'context',
        name: 'Context',
        component: () => import('@/views/context/ContextPage.vue'),
        meta: { title: '上下文管理', icon: 'SetUp' },
      },
      {
        path: 'prompts',
        name: 'Prompts',
        component: () => import('@/views/prompt/PromptListPage.vue'),
        meta: { title: 'Prompt 管理', icon: 'EditPen' },
      },
      {
        path: 'prompts/:id',
        name: 'PromptEdit',
        component: () => import('@/views/prompt/PromptEditPage.vue'),
        meta: { title: '编辑 Prompt', hidden: true },
      },
      {
        path: 'sql',
        name: 'SQLHistory',
        component: () => import('@/views/sql/SQLHistoryPage.vue'),
        meta: { title: 'SQL 管理', icon: 'Monitor' },
      },
      {
        path: 'logs',
        name: 'LogCenter',
        component: () => import('@/views/log/LogCenterPage.vue'),
        meta: { title: '日志中心', icon: 'Tickets' },
      },
      {
        path: 'files',
        name: 'Files',
        component: () => import('@/views/file/FileListPage.vue'),
        meta: { title: '文件中心', icon: 'Folder' },
      },
      {
        path: 'system/users',
        name: 'Users',
        component: () => import('@/views/system/UserListPage.vue'),
        meta: { title: '用户管理', icon: 'User' },
      },
      {
        path: 'system/roles',
        name: 'Roles',
        component: () => import('@/views/system/RoleListPage.vue'),
        meta: { title: '角色管理', icon: 'Avatar' },
      },
      {
        path: 'system/menus',
        name: 'Menus',
        component: () => import('@/views/system/MenuListPage.vue'),
        meta: { title: '菜单管理', icon: 'Menu' },
      },
      {
        path: 'system/permissions',
        name: 'Permissions',
        component: () => import('@/views/system/PermissionListPage.vue'),
        meta: { title: '权限管理', icon: 'Key' },
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// Navigation guard
router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('token')
  if (to.meta.noAuth) {
    next()
  } else if (!token) {
    next('/login')
  } else {
    next()
  }
})

export default router
