export interface ToastItem {
  id: string
  title?: string
  message: string
  type: 'success' | 'error' | 'warning' | 'info'
  duration?: number
}

export type ToastOptions = Omit<ToastItem, 'id'>

type ToastListener = (toast: ToastItem) => void

const listeners = new Set<ToastListener>()

export const toast = {
  show: (opts: ToastOptions | string, type: ToastItem['type'] = 'info') => {
    const item: ToastItem = {
      id: Math.random().toString(36).substring(2, 9),
      message: typeof opts === 'string' ? opts : opts.message,
      title: typeof opts === 'string' ? undefined : opts.title,
      type: typeof opts === 'string' ? type : opts.type || 'info',
      duration: typeof opts === 'string' ? 4000 : opts.duration || 4000
    }
    listeners.forEach(fn => fn(item))
  },
  success: (message: string, title?: string) => toast.show({ message, title, type: 'success' }),
  error: (message: string, title?: string) => toast.show({ message, title, type: 'error' }),
  warning: (message: string, title?: string) => toast.show({ message, title, type: 'warning' }),
  info: (message: string, title?: string) => toast.show({ message, title, type: 'info' }),
  subscribe: (fn: ToastListener) => {
    listeners.add(fn)
    return () => {
      listeners.delete(fn)
    }
  }
}
