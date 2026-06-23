import type { ButtonProps } from './Button.types'

const variants = {
  primary: 'bg-brand text-white hover:bg-brand-dark active:bg-brand-active shadow-sm',
  secondary: 'border border-line bg-white text-ink hover:border-brand/35 hover:bg-canvas',
  ghost: 'text-brand hover:bg-[#fef3e2] hover:text-brand-dark',
  danger: 'bg-danger text-white hover:bg-[#b42318]',
}

export function Button({ variant = 'primary', size = 'md', loading, icon, leadingIcon, trailingIcon, iconOnly = false, className = '', children, disabled, ...props }: ButtonProps) {
  const startIcon = leadingIcon ?? icon
  return (
    <button
      className={`inline-flex shrink-0 items-center justify-center gap-2 whitespace-nowrap rounded-lg font-semibold transition-all focus:outline-none focus:ring-2 focus:ring-brand/35 disabled:cursor-not-allowed disabled:opacity-55 ${iconOnly ? (size === 'sm' ? 'size-9 p-0' : 'size-11 p-0') : (size === 'sm' ? 'h-9 px-3 text-sm' : 'h-11 px-4 text-sm')} ${variants[variant]} ${className}`}
      disabled={disabled || loading}
      {...props}
    >
      {loading ? <span className="size-4 shrink-0 animate-spin rounded-full border-2 border-current border-r-transparent" aria-hidden="true" /> : startIcon && <span className="shrink-0" aria-hidden="true">{startIcon}</span>}
      {!iconOnly && children}
      {!loading && trailingIcon && <span className="shrink-0" aria-hidden="true">{trailingIcon}</span>}
    </button>
  )
}
