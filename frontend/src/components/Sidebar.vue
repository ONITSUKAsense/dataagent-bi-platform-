<template>
  <el-menu
    :default-active="route.path"
    :collapse="appStore.sidebarCollapsed"
    :router="true"
    class="sidebar-menu"
    background-color="#001529"
    text-color="#ffffff99"
    active-text-color="#ffffff"
  >
    <div class="sidebar-logo">
      <span class="logo-text" v-show="!appStore.sidebarCollapsed">DataAgent</span>
      <span class="logo-icon">D</span>
    </div>

    <el-menu-item index="/dashboard">
      <el-icon><Odometer /></el-icon>
      <span>工作台</span>
    </el-menu-item>
    <el-menu-item index="/chatbi">
      <el-icon><ChatDotSquare /></el-icon>
      <span>智能分析</span>
    </el-menu-item>

    <el-sub-menu index="sub-report">
      <template #title>
        <el-icon><Document /></el-icon>
        <span>报告中心</span>
      </template>
      <el-menu-item index="/reports">报告列表</el-menu-item>
    </el-sub-menu>

    <el-menu-item index="/knowledge">
      <el-icon><Collection /></el-icon>
      <span>知识库</span>
    </el-menu-item>
    <el-menu-item index="/context">
      <el-icon><SetUp /></el-icon>
      <span>上下文管理</span>
    </el-menu-item>
    <el-menu-item index="/prompts">
      <el-icon><EditPen /></el-icon>
      <span>Prompt 管理</span>
    </el-menu-item>
    <el-menu-item index="/sql">
      <el-icon><Monitor /></el-icon>
      <span>SQL 管理</span>
    </el-menu-item>
    <el-menu-item index="/logs">
      <el-icon><Tickets /></el-icon>
      <span>日志中心</span>
    </el-menu-item>
    <el-menu-item index="/files">
      <el-icon><Folder /></el-icon>
      <span>文件中心</span>
    </el-menu-item>

    <el-sub-menu index="sub-system">
      <template #title>
        <el-icon><Setting /></el-icon>
        <span>系统管理</span>
      </template>
      <el-menu-item index="/system/users">用户管理</el-menu-item>
      <el-menu-item index="/system/roles">角色管理</el-menu-item>
      <el-menu-item index="/system/menus">菜单管理</el-menu-item>
      <el-menu-item index="/system/permissions">权限管理</el-menu-item>
    </el-sub-menu>
  </el-menu>
</template>

<script setup lang="ts">
import { useRoute } from 'vue-router'
import { useAppStore } from '@/stores/app'

const route = useRoute()
const appStore = useAppStore()
</script>

<style scoped>
.sidebar-menu {
  height: 100vh;
  border-right: none;
  overflow-y: auto;
  overflow-x: hidden;
}

.sidebar-logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 20px;
  font-weight: bold;
  border-bottom: 1px solid #ffffff1a;
}

.logo-icon {
  display: none;
}

.el-menu--collapse .logo-text {
  display: none;
}
.el-menu--collapse .logo-icon {
  display: block;
}
</style>
