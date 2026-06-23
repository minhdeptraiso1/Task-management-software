import { X } from 'lucide-react'
import { useEffect, type ReactNode } from 'react'
import { Button } from '../Button'

interface ModalProps { open: boolean; title: string; description?: string; children: ReactNode; onClose: () => void }
export function Modal({ open, title, description, children, onClose }: ModalProps) {
  useEffect(() => {
    if (!open) return
    const handleKeyDown = (event: KeyboardEvent) => { if (event.key === 'Escape') onClose() }
    window.addEventListener('keydown', handleKeyDown)
    return () => window.removeEventListener('keydown', handleKeyDown)
  }, [open, onClose])
  if (!open) return null
  return <div className="fixed inset-0 z-50 grid place-items-center bg-brand-black/55 p-4" onMouseDown={onClose}>
    <section role="dialog" aria-modal="true" aria-labelledby="modal-title" className="animate-enter max-h-[92vh] w-full max-w-[600px] overflow-y-auto rounded-xl bg-white p-6 shadow-2xl" onMouseDown={e => e.stopPropagation()}>
      <header className="mb-6 flex items-start justify-between gap-4">
        <div><h2 id="modal-title" className="text-[22px] font-semibold leading-[30px]">{title}</h2>{description && <p className="mt-1 text-sm text-muted">{description}</p>}</div>
        <Button variant="ghost" size="sm" iconOnly leadingIcon={<X size={18} />} onClick={onClose} aria-label="Đóng cửa sổ" />
      </header>
      {children}
    </section>
  </div>
}
