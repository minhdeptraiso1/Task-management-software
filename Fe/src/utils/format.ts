export function formatDate(value?: string | null) {
  if (!value) return 'Chưa đặt'
  return new Intl.DateTimeFormat('vi-VN').format(new Date(value))
}

export function formatDateTime(value?: string | null) {
  if (!value) return 'Chưa đặt'
  return new Intl.DateTimeFormat('vi-VN', {
    dateStyle: 'short',
    timeStyle: 'medium',
  }).format(new Date(value))
}

export function formatShortDate(value?: string | null) {
  if (!value) return ''
  const date = new Date(value)
  const day = date.getDate().toString().padStart(2, '0')
  const month = (date.getMonth() + 1).toString().padStart(2, '0')
  return `${day}/${month}`
}
