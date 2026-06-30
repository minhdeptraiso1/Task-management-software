import { useEffect, useRef, useState, type ReactNode } from 'react'
import { Settings } from 'lucide-react'

export interface ActionMenuProps {
  children: ReactNode
  tone?: 'light' | 'dark'
  icon?: ReactNode
  label?: string
}

export function ActionMenu({ children, tone = 'light', icon = <Settings size={16} />, label }: ActionMenuProps) {
  const [open, setOpen] = useState(false)
  const menuRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    if (!open) return
    const handlePointerDown = (event: PointerEvent) => {
      if (!menuRef.current?.contains(event.target as Node)) setOpen(false)
    }
    window.addEventListener('pointerdown', handlePointerDown)
    return () => window.removeEventListener('pointerdown', handlePointerDown)
  }, [open])

  return (
    <div className="relative inline-block" ref={menuRef}>
      <button
        type="button"
        className={`inline-flex cursor-pointer items-center justify-center rounded-lg transition-all duration-200 focus:outline-none focus-visible:ring-2 focus-visible:ring-brand focus-visible:ring-offset-2 ${
          label ? 'h-11 gap-2 border border-line bg-white px-4 text-sm font-semibold text-ink hover:border-brand/35 hover:bg-canvas' : 'size-9'
        } ${
          tone === 'dark' 
            ? 'text-white/75 hover:bg-white/10 hover:text-white' 
            : (!label ? 'text-muted hover:bg-panel hover:text-brand' : '')
        } ${open ? (tone === 'dark' ? 'bg-white/10 text-white' : (label ? 'border-brand/35 bg-canvas' : 'bg-panel text-brand')) : ''}`}
        aria-label={label || 'Tùy chọn'}
        aria-expanded={open}
        aria-haspopup="menu"
        onClick={() => setOpen(value => !value)}
      >
        <span className={`flex transition-transform duration-300 ${open ? 'rotate-90' : 'rotate-0'}`}>
          {icon}
        </span>
        {label && <span>{label}</span>}
      </button>

      {/* Backdrop for mobile */}
      {open && <div className="fixed inset-0 z-40 bg-black/5 sm:hidden" onClick={() => setOpen(false)} />}
      
      {open && (
        <div 
          className="absolute right-0 z-50 mt-2 min-w-[200px] origin-top-right rounded-2xl border border-white/40 bg-white/90 p-2 text-ink shadow-[0_12px_40px_-12px_rgba(0,0,0,0.15)] backdrop-blur-xl ring-1 ring-black/5" 
          onClick={() => setOpen(false)}
          style={{ animation: 'actionMenuScaleIn 0.15s ease-out forwards' }}
        >
          {children}
        </div>
      )}
    </div>
  )
}

export function ActionItem({ children, danger = false, onClick }: { children: ReactNode; danger?: boolean; onClick: () => void }) {
  return (
    <button 
      type="button" 
      className={`group flex w-full items-center gap-3 rounded-xl px-3.5 py-2.5 text-left text-[14px] font-medium transition-all duration-200 ${
        danger 
          ? 'text-gray-700 hover:bg-red-50 hover:text-red-600 [&>svg]:text-red-400 hover:[&>svg]:text-red-600 hover:[&>svg]:scale-110 [&>svg]:transition-transform' 
          : 'text-gray-700 hover:bg-slate-50 hover:text-brand [&>svg]:text-gray-400 hover:[&>svg]:text-brand hover:[&>svg]:scale-110 [&>svg]:transition-transform'
      }`} 
      onClick={onClick}
    >
      {children}
    </button>
  )
}
