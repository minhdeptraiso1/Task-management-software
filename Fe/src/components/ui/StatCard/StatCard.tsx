import { type ReactNode } from 'react'

export type StatCardVariant = 'brand' | 'success' | 'danger' | 'warning' | 'info' | 'neutral'

export interface StatCardProps {
  title: string
  value: string | number
  icon: ReactNode
  variant?: StatCardVariant
  subtext?: string
  trend?: {
    value: string
    isPositive?: boolean
  }
  action?: ReactNode
  className?: string
}

const ICON_BOX_MAP: Record<StatCardVariant, string> = {
  brand: 'bg-brand/10 text-brand',
  success: 'bg-success/10 text-success',
  danger: 'bg-danger/10 text-danger',
  warning: 'bg-warning/15 text-brand-dark',
  info: 'bg-info/10 text-info',
  neutral: 'bg-panel text-muted',
}

export function StatCard({
  title,
  value,
  icon,
  variant = 'brand',
  subtext,
  trend,
  action,
  className = '',
}: StatCardProps) {
  return (
    <div className={`bg-white rounded-2xl border border-line p-5 shadow-2xs hover:shadow-xs transition-all duration-200 flex flex-col justify-between ${className}`}>
      <div className="flex items-center justify-between gap-3">
        <div className="space-y-1">
          <p className="text-xs font-semibold text-muted uppercase tracking-wider">{title}</p>
          <h3 className="text-2xl font-extrabold text-ink tabular-nums leading-tight">{value}</h3>
        </div>
        <div className={`size-11 rounded-xl flex items-center justify-center shrink-0 ${ICON_BOX_MAP[variant]}`}>
          {icon}
        </div>
      </div>

      {(subtext || trend || action) && (
        <div className="mt-4 pt-3 border-t border-line/60 flex items-center justify-between text-xs text-muted">
          {subtext && <span className="truncate">{subtext}</span>}

          {trend && (
            <span className={`font-bold tabular-nums ${trend.isPositive ? 'text-success' : 'text-danger'}`}>
              {trend.isPositive ? '↑' : '↓'} {trend.value}
            </span>
          )}

          {action && <div className="ml-auto">{action}</div>}
        </div>
      )}
    </div>
  )
}
