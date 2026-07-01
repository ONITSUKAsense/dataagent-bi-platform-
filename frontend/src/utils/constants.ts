export const REPORT_STATUS = {
  0: { label: '生成中', type: 'warning' },
  1: { label: '已生成', type: 'success' },
  2: { label: '失败', type: 'danger' },
} as const

export const PROMPT_STATUS = {
  0: { label: '草稿', type: 'info' },
  1: { label: '已发布', type: 'success' },
} as const

export const SQL_STATUS = {
  0: { label: '成功', type: 'success' },
  1: { label: '失败', type: 'danger' },
} as const

export const DOC_STATUS = {
  0: { label: '处理中', type: 'warning' },
  1: { label: '已就绪', type: 'success' },
  2: { label: '失败', type: 'danger' },
} as const
