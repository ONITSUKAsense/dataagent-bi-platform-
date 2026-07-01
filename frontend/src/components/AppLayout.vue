<template>
  <el-container class="app-layout">
    <Sidebar />
    <el-container>
      <Navbar />
      <el-main class="app-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import Sidebar from './Sidebar.vue'
import Navbar from './Navbar.vue'
</script>

<style scoped>
.app-layout {
  height: 100vh;
}

.app-main {
  background: var(--bg-color);
  padding: 20px;
  overflow-y: auto;
}
</style>
