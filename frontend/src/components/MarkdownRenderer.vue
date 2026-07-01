<template>
  <div class="markdown-body" v-html="renderedHtml"></div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { marked } from 'marked'
import hljs from 'highlight.js'
import 'highlight.js/styles/github.css'

const props = defineProps<{
  content: string
}>()

// Configure marked with highlight.js
marked.setOptions({
  breaks: true,
  gfm: true,
  highlight(code: string, lang: string) {
    if (lang && hljs.getLanguage(lang)) {
      return hljs.highlight(code, { language: lang }).value
    }
    return hljs.highlightAuto(code).value
  },
} as any)

const renderedHtml = computed(() => {
  if (!props.content) return ''
  return marked(props.content) as string
})
</script>

<style scoped>
.markdown-body {
  line-height: 1.7;
  color: #303133;
  font-size: 14px;
}
.markdown-body :deep(h1) { font-size: 1.5em; margin: 0.67em 0; }
.markdown-body :deep(h2) { font-size: 1.3em; margin: 0.83em 0; border-bottom: 1px solid #eee; padding-bottom: 6px; }
.markdown-body :deep(h3) { font-size: 1.15em; margin: 1em 0; }
.markdown-body :deep(p) { margin: 0.5em 0; }
.markdown-body :deep(ul), .markdown-body :deep(ol) { padding-left: 2em; }
.markdown-body :deep(li) { margin: 0.25em 0; }
.markdown-body :deep(table) { border-collapse: collapse; width: 100%; margin: 1em 0; }
.markdown-body :deep(th), .markdown-body :deep(td) { border: 1px solid #e0e0e0; padding: 8px 12px; text-align: left; }
.markdown-body :deep(th) { background: #f5f7fa; font-weight: 600; }
.markdown-body :deep(code) { background: #f5f7fa; padding: 2px 6px; border-radius: 3px; font-size: 0.9em; }
.markdown-body :deep(pre code) { background: none; padding: 0; }
.markdown-body :deep(pre) { background: #f6f8fa; padding: 16px; border-radius: 6px; overflow-x: auto; }
.markdown-body :deep(blockquote) { border-left: 4px solid #409eff; padding-left: 16px; color: #606266; margin: 1em 0; }
.markdown-body :deep(strong) { font-weight: 600; }
</style>
