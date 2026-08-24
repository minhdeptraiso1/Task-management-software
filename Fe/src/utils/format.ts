export function formatDate(value?: string | null) {
  if (!value) return 'Chưa đặt'
  const dateStr = value.split('T')[0]
  const parts = dateStr.split('-')
  if (parts.length === 3 && parts[0].length === 4) {
    const [year, month, day] = parts
    return `${day.padStart(2, '0')}/${month.padStart(2, '0')}/${year}`
  }
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
