import type { ReactNode } from 'react'

export interface TabItem {
  key: string
  label: ReactNode
  icon?: ReactNode
  count?: number | string
  disabled?: boolean
}

export interface TabsProps {
  items: TabItem[]
  activeKey: string
  onChange: (key: string) => void
  variant?: 'pill' | 'underline' | 'button'
  size?: 'sm' | 'md'
  className?: string
}

export function Tabs({
  items,
  activeKey,
  onChange,
  variant = 'button',
  size = 'md',
  className = '',
}: TabsProps) {
  const isSm = size === 'sm'

  if (variant === 'pill') {
    return (
      <div
        className={`inline-flex items-center gap-1 rounded-full bg-panel p-1 border border-line select-none ${className}`}
      >
        {items.map(item => {
          const isActive = item.key === activeKey
          return (
            <button
              key={item.key}
              type="button"
              disabled={item.disabled}
              onClick={() => onChange(item.key)}
              className={`inline-flex items-center gap-2 rounded-full font-bold transition-all cursor-pointer disabled:opacity-40 disabled:cursor-not-allowed ${
                isSm ? 'px-3 py-1 text-xs' : 'px-4 py-1.5 text-xs'
              } ${
                isActive
                  ? 'bg-brand text-white shadow-xs'
                  : 'text-muted hover:text-ink hover:bg-white/50'
              }`}
            >
              {item.icon && <span className="shrink-0">{item.icon}</span>}
              <span>{item.label}</span>
              {item.count !== undefined && (
                <span
                  className={`rounded-full px-1.5 py-0.2 text-[10px] font-extrabold ${
                    isActive ? 'bg-white/20 text-white' : 'bg-line text-muted'
                  }`}
                >
                  {item.count}
                </span>
              )}
            </button>
          )
        })}
      </div>
    )
  }

  if (variant === 'underline') {
    return (
      <div className={`flex items-center border-b border-line select-none gap-6 ${className}`}>
        {items.map(item => {
          const isActive = item.key === activeKey
          return (
            <button
              key={item.key}
              type="button"
              disabled={item.disabled}
              onClick={() => onChange(item.key)}
              className={`relative inline-flex items-center gap-2 pb-3 pt-1 text-xs font-bold transition-all cursor-pointer disabled:opacity-40 disabled:cursor-not-allowed ${
                isActive ? 'text-brand' : 'text-muted hover:text-ink'
              }`}
            >
              {item.icon && <span className="shrink-0">{item.icon}</span>}
              <span>{item.label}</span>
              {item.count !== undefined && (
                <span className="rounded-full bg-panel px-1.5 py-0.2 text-[10px] font-bold text-muted border border-line">
                  {item.count}
                </span>
              )}
              {isActive && (
                <span className="absolute bottom-0 left-0 right-0 h-0.5 bg-brand rounded-full" />
              )}
            </button>
          )
        })}
      </div>
    )
  }

  // Default: button style
  return (
    <div className={`flex flex-wrap items-center gap-2 select-none ${className}`}>
      {items.map(item => {
        const isActive = item.key === activeKey
        return (
          <button
            key={item.key}
            type="button"
            disabled={item.disabled}
            onClick={() => onChange(item.key)}
            className={`inline-flex items-center gap-2 rounded-lg font-semibold border transition-all cursor-pointer disabled:opacity-40 disabled:cursor-not-allowed ${
              isSm ? 'px-3 py-1.5 text-xs' : 'px-4 py-2 text-sm'
            } ${
              isActive
                ? 'bg-brand text-white border-transparent shadow-xs'
                : 'bg-white text-ink border-line hover:bg-slate-50'
            }`}
          >
            {item.icon && <span className="shrink-0">{item.icon}</span>}
            <span>{item.label}</span>
            {item.count !== undefined && (
              <span
                className={`rounded-full px-1.5 py-0.2 text-[10px] font-bold ${
                  isActive ? 'bg-white/25 text-white' : 'bg-panel text-muted border border-line'
                }`}
              >
                {item.count}
              </span>
            )}
          </button>
        )
      })}
    </div>
  )
}
