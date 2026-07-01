import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  const sidebarCollapsed = ref(false)
  const breadcrumb = ref<{ label: string; path?: string }[]>([])

  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  function setBreadcrumb(items: { label: string; path?: string }[]) {
    breadcrumb.value = items
  }

  return { sidebarCollapsed, breadcrumb, toggleSidebar, setBreadcrumb }
})
