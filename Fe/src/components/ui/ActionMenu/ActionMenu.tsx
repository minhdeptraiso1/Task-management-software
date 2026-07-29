import { useEffect, useRef, useState, type ReactNode } from 'react'
import { Settings } from 'lucide-react'

export interface ActionMenuProps {
  children: ReactNode
  tone?: 'light' | 'dark'
  icon?: ReactNode
  label?: string
  expandOnClick?: boolean
  expandOnHover?: boolean
}

export function ActionMenu({
  children,
  tone = 'light',
  icon = <Settings size={17} />,
  label,
  expandOnClick = true,
  expandOnHover,
}: ActionMenuProps) {
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

  const isExpandMode = expandOnClick || expandOnHover

  return (
    <div className="relative inline-block" ref={menuRef}>
      <button
        type="button"
        className={
          isExpandMode
            ? `inline-flex cursor-pointer items-center justify-center rounded-full border border-slate-200 bg-white h-9 text-xs font-bold text-slate-700 transition-all duration-350 ease-in-out hover:border-brand/40 hover:bg-slate-50 shadow-2xs shrink-0 ${
                open
                  ? label 
                    ? 'border-brand bg-slate-50 px-3.5 shadow-xs text-brand ring-2 ring-brand/15' 
                    : 'border-brand bg-slate-50 w-9 px-0 shadow-xs text-brand ring-2 ring-brand/15'
                  : expandOnHover
                  ? 'group hover:px-3.5 w-9 hover:w-auto'
                  : 'w-9 px-0 hover:scale-105'
              }`
            : `inline-flex cursor-pointer items-center justify-center rounded-lg transition-all duration-300 focus:outline-none focus-visible:ring-2 focus-visible:ring-brand focus-visible:ring-offset-2 ${
                label ? 'h-11 gap-2 border border-line bg-white px-4 text-sm font-semibold text-ink hover:border-brand/35 hover:bg-canvas' : 'size-9'
              } ${
                tone === 'dark' 
                  ? 'text-white/75 hover:bg-white/10 hover:text-white' 
                  : (!label ? 'text-muted hover:bg-panel hover:text-brand' : '')
              } ${open ? (tone === 'dark' ? 'bg-white/10 text-white' : (label ? 'border-brand/35 bg-canvas' : 'bg-panel text-brand')) : ''}`
        }
        aria-label={label || 'Tùy chọn'}
        aria-expanded={open}
        aria-haspopup="menu"
        onClick={() => setOpen(value => !value)}
      >
        <span className={`flex transition-transform duration-350 ease-in-out shrink-0 ${open ? 'rotate-90 text-brand' : 'text-slate-600'}`}>
          {icon}
        </span>
        {label && isExpandMode && (
          <span
            className={`inline-block overflow-hidden whitespace-nowrap ${
              open
                ? 'max-w-[80px] opacity-100 ml-1.5'
                : expandOnHover
                ? 'max-w-0 opacity-0 group-hover:max-w-[80px] group-hover:opacity-100 group-hover:ml-1.5 ml-0'
                : 'max-w-0 opacity-0 ml-0'
            }`}
            style={{
              transitionProperty: 'max-width, opacity, margin',
              transitionDuration: open ? '350ms' : '200ms',
              transitionTimingFunction: open ? 'cubic-bezier(0, 0, 0.2, 1)' : 'ease-out',
            }}
          >
            {label}
          </span>
        )}
        {label && !isExpandMode && <span>{label}</span>}
      </button>

      {/* Backdrop for mobile */}
      {open && <div className="fixed inset-0 z-40 bg-black/5 sm:hidden" onClick={() => setOpen(false)} />}
      
      <div 
        className={`absolute right-0 z-50 mt-2 min-w-[200px] origin-top-right rounded-2xl border border-white/40 bg-white/95 p-2 text-ink shadow-[0_12px_40px_-12px_rgba(0,0,0,0.15)] backdrop-blur-xl ring-1 ring-black/5 transition-all duration-300 ease-in-out ${
          open 
            ? 'opacity-100 scale-100 translate-y-0 pointer-events-auto' 
            : 'opacity-0 scale-95 -translate-y-1 pointer-events-none'
        }`}
        onClick={() => setOpen(false)}
      >
        {children}
      </div>
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
