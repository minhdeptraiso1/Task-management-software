import { X } from 'lucide-react'
import { useEffect, type ReactNode } from 'react'
import { Button } from '../Button'

interface ModalProps {
  open: boolean
  title: string
  description?: string
  children: ReactNode
  onClose: () => void
  showClose?: boolean
  actions?: ReactNode
}

export function Modal({ open, title, description, children, onClose, showClose = true, actions }: ModalProps) {
  useEffect(() => {
    if (!open) return
    const handleKeyDown = (event: KeyboardEvent) => { if (event.key === 'Escape') onClose() }
    window.addEventListener('keydown', handleKeyDown)
    return () => window.removeEventListener('keydown', handleKeyDown)
  }, [open, onClose])

  if (!open) return null

  return <div className="fixed inset-0 z-50 grid place-items-center bg-brand-black/55 p-4" onMouseDown={onClose}>
    <section role="dialog" aria-modal="true" aria-labelledby="modal-title" className="animate-enter flex max-h-[92vh] w-full max-w-[600px] flex-col overflow-hidden rounded-xl bg-white shadow-2xl" onMouseDown={event => event.stopPropagation()}>
      <header className="flex shrink-0 items-start justify-between gap-4 border-b border-orange-100/60 bg-gradient-to-r from-amber-50/80 to-orange-50/40 px-6 py-5">
        <div>
          <h2 id="modal-title" className="text-[20px] font-semibold leading-[28px] text-ink">{title}</h2>
          {description && <p className="mt-1 text-sm text-muted">{description}</p>}
        </div>
        {(actions || showClose) && <div className="flex shrink-0 items-center gap-1">
          {actions}
          {showClose && <Button variant="ghost" size="sm" iconOnly leadingIcon={<X size={18} />} onClick={onClose} aria-label="Đóng cửa sổ" />}
        </div>}
      </header>
      <div className="min-h-0 flex-1 overflow-y-auto p-6">
        {children}
      </div>
    </section>
  </div>
}
