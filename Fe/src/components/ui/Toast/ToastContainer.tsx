import { useEffect, useState } from 'react'
import { CheckCircle2, AlertCircle, AlertTriangle, Info, X } from 'lucide-react'
import { toast, type ToastItem } from './toast'

export function ToastContainer() {
  const [toasts, setToasts] = useState<ToastItem[]>([])

  useEffect(() => {
    const unsubscribe = toast.subscribe(newToast => {
      setToasts(prev => [...prev, newToast])

      setTimeout(() => {
        setToasts(prev => prev.filter(t => t.id !== newToast.id))
      }, newToast.duration || 4000)
    })
    return unsubscribe
  }, [])

  const removeToast = (id: string) => {
    setToasts(prev => prev.filter(t => t.id !== id))
  }

  if (toasts.length === 0) return null

  return (
    <div className="fixed top-5 right-5 z-[999999] flex flex-col gap-2.5 max-w-sm w-full pointer-events-none px-4 sm:px-0">
      {toasts.map(item => {
        let borderClass = 'border-slate-200 bg-white text-slate-800 shadow-xl'
        let icon = <Info size={18} className="text-sky-500 shrink-0 mt-0.5" />
        let badgeTitle = item.title || 'Thông báo'

        if (item.type === 'success') {
          borderClass = 'border-emerald-200/90 bg-emerald-50/95 text-emerald-950 shadow-xl shadow-emerald-500/5'
          icon = <CheckCircle2 size={18} className="text-emerald-600 shrink-0 mt-0.5" />
          badgeTitle = item.title || 'Thành công'
        } else if (item.type === 'error') {
          borderClass = 'border-rose-200/90 bg-rose-50/95 text-rose-950 shadow-xl shadow-rose-500/5'
          icon = <AlertCircle size={18} className="text-rose-600 shrink-0 mt-0.5" />
          badgeTitle = item.title || 'Có lỗi xảy ra'
        } else if (item.type === 'warning') {
          borderClass = 'border-amber-200/90 bg-amber-50/95 text-amber-950 shadow-xl shadow-amber-500/5'
          icon = <AlertTriangle size={18} className="text-amber-600 shrink-0 mt-0.5" />
          badgeTitle = item.title || 'Cảnh báo'
        }

        return (
          <div
            key={item.id}
            className={`pointer-events-auto rounded-2xl border p-3.5 sm:p-4 shadow-xl backdrop-blur-md flex items-start gap-3 transition-all animate-enter ${borderClass}`}
          >
            {icon}
            <div className="flex-1 pr-1 overflow-hidden">
              <h5 className="font-extrabold text-sm tracking-tight leading-tight">{badgeTitle}</h5>
              <p className="text-xs font-medium mt-1 leading-relaxed break-words opacity-90">{item.message}</p>
            </div>
            <button
              type="button"
              onClick={() => removeToast(item.id)}
              className="p-1 rounded-lg text-slate-400 hover:text-slate-700 hover:bg-black/5 transition shrink-0"
              title="Đóng"
            >
              <X size={15} />
            </button>
          </div>
        )
      })}
    </div>
  )
}
