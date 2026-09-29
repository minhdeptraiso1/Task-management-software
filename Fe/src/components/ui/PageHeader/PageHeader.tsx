import type { ReactNode } from 'react'

export interface PageHeaderProps {
  title: ReactNode
  description?: ReactNode
  icon?: ReactNode
  badge?: ReactNode
  actions?: ReactNode
  className?: string
}

export function PageHeader({
  title,
  description,
  icon,
  badge,
  actions,
  className = '',
}: PageHeaderProps) {
  return (
    <header
      className={`flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-line/60 pb-5 mb-5 ${className}`}
    >
      <div className="flex items-start sm:items-center gap-3.5 min-w-0">
        {icon && (
          <span className="grid size-11 shrink-0 place-items-center rounded-xl bg-brand/10 text-brand border border-brand/20 shadow-2xs">
            {icon}
          </span>
        )}
        <div className="min-w-0">
          <div className="flex flex-wrap items-center gap-2.5">
            <h1 className="text-xl sm:text-2xl font-black tracking-tight text-ink">
              {title}
            </h1>
            {badge && <div>{badge}</div>}
          </div>
          {description && (
            <p className="mt-1 text-xs sm:text-sm font-medium text-muted leading-relaxed max-w-2xl">
              {description}
            </p>
          )}
        </div>
      </div>

      {actions && (
        <div className="flex flex-wrap items-center gap-2.5 shrink-0 self-start sm:self-center">
          {actions}
        </div>
      )}
    </header>
  )
}
