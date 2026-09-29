import type { ReactNode } from 'react'
import { Inbox } from 'lucide-react'

export interface EmptyStateProps {
  icon?: ReactNode
  title: string
  description?: string
  action?: ReactNode
  bordered?: boolean
  className?: string
}

export function EmptyState({
  icon,
  title,
  description,
  action,
  bordered = true,
  className = '',
}: EmptyStateProps) {
  return (
    <div
      className={`flex flex-col items-center justify-center p-8 sm:p-12 text-center select-none ${
        bordered ? 'rounded-2xl border border-dashed border-line/80 bg-white/50' : ''
      } ${className}`}
    >
      <div className="mb-3 grid size-12 place-items-center rounded-2xl bg-panel text-muted shadow-2xs">
        {icon || <Inbox size={24} />}
      </div>
      <h3 className="text-sm font-bold text-ink">{title}</h3>
      {description && (
        <p className="mt-1 max-w-sm text-xs font-medium text-muted leading-relaxed">
          {description}
        </p>
      )}
      {action && <div className="mt-4">{action}</div>}
    </div>
  )
}
