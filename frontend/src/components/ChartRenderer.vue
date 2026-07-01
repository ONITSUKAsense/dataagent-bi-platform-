<template>
  <div ref="chartRef" :style="{ height: height + 'px' }" class="chart-renderer"></div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, shallowRef } from 'vue'
import * as echarts from 'echarts'

const props = defineProps<{
  option: Record<string, any>
  height?: number
}>()

const height = props.height || 300
const chartRef = ref<HTMLElement>()
const chart = shallowRef<echarts.ECharts | null>(null)

function initChart() {
  if (!chartRef.value) return
  chart.value = echarts.init(chartRef.value)
  chart.value.setOption(props.option)
}

function handleResize() {
  chart.value?.resize()
}

watch(() => props.option, (newOpt) => {
  if (chart.value && newOpt) {
    chart.value.setOption(newOpt, true)
  }
}, { deep: true })

onMounted(() => {
  initChart()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chart.value?.dispose()
})
</script>

<style scoped>
.chart-renderer { width: 100%; }
</style>
