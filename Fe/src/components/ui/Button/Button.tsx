import type { ButtonProps, ButtonTone } from './Button.types'

const variants: Record<string, string> = {
  primary: 'bg-brand text-white hover:bg-brand-dark active:bg-brand-active shadow-sm border border-transparent focus:ring-brand/35',
  secondary: 'border border-line bg-white text-ink hover:border-brand/35 hover:bg-canvas focus:ring-brand/35',
  danger: 'bg-danger text-white hover:bg-danger/80 border border-transparent focus:ring-danger/35',
  'solid-blue': 'bg-info text-white hover:bg-info/90 border border-transparent shadow-sm focus:ring-info/35',
  'solid-green': 'bg-success text-white hover:bg-success/90 border border-transparent shadow-sm focus:ring-success/35',
  'solid-red': 'bg-danger text-white hover:bg-danger/80 border border-transparent shadow-sm focus:ring-danger/35',
  'outline-blue': 'border border-info/60 bg-white text-info hover:bg-info/10 focus:ring-info/35',
  'outline-green': 'border border-success/60 bg-white text-success hover:bg-success/10 focus:ring-success/35',
  'outline-red': 'border border-danger/60 bg-white text-danger hover:bg-danger/10 focus:ring-danger/35',
  'outline-amber': 'border border-brand/60 bg-white text-brand-dark hover:bg-brand/10 focus:ring-brand/35',
  'outline-indigo': 'border border-info/40 bg-white text-info hover:bg-info/10 focus:ring-info/35',
  'outline-teal': 'border border-success/60 bg-white text-success hover:bg-success/10 focus:ring-success/35',
}

const ghostTones: Record<ButtonTone, string> = {
  brand: 'text-brand hover:bg-brand/10 hover:text-brand-dark border border-transparent focus:ring-brand/35',
  danger: 'text-danger hover:bg-danger/10 hover:text-danger-strong border border-transparent focus:ring-danger/35',
  success: 'text-success hover:bg-success/10 border border-transparent focus:ring-success/35',
  info: 'text-info hover:bg-info/10 border border-transparent focus:ring-info/35',
  warning: 'text-warning-dark hover:bg-warning/15 border border-transparent focus:ring-warning/35',
  neutral: 'text-ink hover:bg-panel border border-transparent focus:ring-muted/35',
  dark: 'text-slate-300 hover:bg-slate-800/80 hover:text-white border border-transparent focus:ring-slate-400/35',
}

const outlineTones: Record<ButtonTone, string> = {
  brand: 'border border-brand/60 bg-white text-brand-dark hover:bg-brand/10 focus:ring-brand/35',
  danger: 'border border-danger/60 bg-white text-danger hover:bg-danger/10 focus:ring-danger/35',
  success: 'border border-success/60 bg-white text-success hover:bg-success/10 focus:ring-success/35',
  info: 'border border-info/60 bg-white text-info hover:bg-info/10 focus:ring-info/35',
  warning: 'border border-warning/60 bg-white text-warning-dark hover:bg-warning/15 focus:ring-warning/35',
  neutral: 'border border-line bg-white text-ink hover:bg-panel focus:ring-muted/35',
  dark: 'border border-slate-700 bg-slate-800/80 text-slate-300 hover:bg-slate-700 hover:text-white focus:ring-slate-400/35',
}

export function Button({
  as: Component = 'button',
  variant = 'primary',
  tone = 'brand',
  active = false,
  size = 'md',
  loading,
  icon,
  leadingIcon,
  trailingIcon,
  iconOnly = false,
  className = '',
  children,
  disabled,
  title,
  ...props
}: ButtonProps) {
  const startIcon = leadingIcon ?? icon

  let variantClass = variants[variant] || variants.primary
  if (variant === 'ghost') {
    variantClass = ghostTones[tone] || ghostTones.brand
  } else if (variant === 'outline') {
    variantClass = outlineTones[tone] || outlineTones.brand
  } else if (variant === 'secondary' && active) {
    variantClass = 'bg-brand text-white border border-transparent shadow-xs focus:ring-brand/35'
  }

  return (
    <Component
      className={`relative group inline-flex shrink-0 items-center justify-center gap-2 whitespace-nowrap rounded-lg font-semibold transition-all focus:outline-none focus:ring-2 active:scale-[0.98] disabled:cursor-not-allowed disabled:opacity-55 disabled:active:scale-100 ${
        iconOnly
          ? size === 'sm'
            ? 'size-9 p-0'
            : 'size-11 p-0'
          : size === 'sm'
            ? 'h-9 px-3 text-sm'
            : 'h-11 px-4 text-sm'
      } ${variantClass} ${className}`}
      disabled={disabled || loading}
      title={title}
      {...props}
    >
      {loading ? (
        <span className="size-4 shrink-0 animate-spin rounded-full border-2 border-current border-r-transparent" aria-hidden="true" />
      ) : (
        startIcon && <span className="shrink-0" aria-hidden="true">{startIcon}</span>
      )}
      {!iconOnly && children}
      {!loading && trailingIcon && <span className="shrink-0" aria-hidden="true">{trailingIcon}</span>}
      {title && (
        <span className="absolute -top-9 left-1/2 -translate-x-1/2 hidden group-hover:flex items-center bg-brand-black/95 text-white text-[11px] font-semibold px-2.5 py-1 rounded-md shadow-md whitespace-nowrap z-50 pointer-events-none border border-white/10">
          {title}
        </span>
      )}
    </Component>
  )
}
