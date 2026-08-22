import React, { type ReactNode } from 'react'

export type BadgeVariant = 'brand' | 'success' | 'danger' | 'warning' | 'info' | 'neutral'
export type BadgeSize = 'sm' | 'md'

export interface BadgeProps extends React.HTMLAttributes<HTMLSpanElement> {
  variant?: BadgeVariant
  size?: BadgeSize
  dot?: boolean
  leadingIcon?: ReactNode
  children: ReactNode
  className?: string
}

const VARIANT_MAP: Record<BadgeVariant, string> = {
  brand: 'bg-brand/10 text-brand border-brand/20',
  success: 'bg-success/10 text-success border-success/20',
  danger: 'bg-danger/10 text-danger border-danger/20',
  warning: 'bg-warning/15 text-brand-dark border-warning/30',
  info: 'bg-info/10 text-info border-info/20',
  neutral: 'bg-panel text-muted border-line',
}

const SIZE_MAP: Record<BadgeSize, string> = {
  sm: 'px-2 py-0.5 text-[11px]',
  md: 'px-2.5 py-1 text-xs',
}

export function Badge({
  variant = 'neutral',
  size = 'md',
  dot = false,
  leadingIcon,
  children,
  className = '',
  ...props
}: BadgeProps) {
  return (
    <span
      className={`inline-flex items-center gap-1.5 rounded-full font-bold border transition-colors ${VARIANT_MAP[variant]} ${SIZE_MAP[size]} ${className}`}
      {...props}
    >
      {dot && <span className="size-1.5 rounded-full bg-current shrink-0 animate-pulse" />}
      {leadingIcon && <span className="shrink-0">{leadingIcon}</span>}
      <span className="truncate">{children}</span>
    </span>
  )
}
