import type { ButtonProps } from './Button.types'

const variants = {
  primary: 'bg-brand text-white hover:bg-brand-dark active:bg-brand-active shadow-sm border border-transparent',
  secondary: 'border border-line bg-white text-ink hover:border-brand/35 hover:bg-canvas',
  ghost: 'text-brand hover:bg-[#fef3e2] hover:text-brand-dark border border-transparent',
  danger: 'bg-danger text-white hover:bg-[#b42318] border border-transparent',
  'solid-blue': 'bg-blue-600 text-white hover:bg-blue-700 border border-transparent shadow-sm',
  'solid-green': 'bg-emerald-500 text-white hover:bg-emerald-600 border border-transparent shadow-sm',
  'solid-red': 'bg-rose-500 text-white hover:bg-rose-600 border border-transparent shadow-sm',
  'outline-blue': 'border border-blue-400 bg-white text-blue-700 hover:bg-blue-50',
  'outline-green': 'border border-emerald-400 bg-white text-emerald-700 hover:bg-emerald-50',
  'outline-red': 'border border-rose-400 bg-white text-rose-700 hover:bg-rose-50',
  'outline-amber': 'border border-amber-400 bg-white text-amber-700 hover:bg-amber-50',
  'outline-indigo': 'border border-indigo-300 bg-white text-indigo-700 hover:bg-indigo-50',
  'outline-teal': 'border border-emerald-400 bg-white text-emerald-700 hover:bg-emerald-50',
}

export function Button({ as: Component = 'button', variant = 'primary', size = 'md', loading, icon, leadingIcon, trailingIcon, iconOnly = false, className = '', children, disabled, ...props }: ButtonProps) {
  const startIcon = leadingIcon ?? icon
  return (
    <Component
      className={`inline-flex shrink-0 items-center justify-center gap-2 whitespace-nowrap rounded-lg font-semibold transition-all focus:outline-none focus:ring-2 focus:ring-brand/35 disabled:cursor-not-allowed disabled:opacity-55 ${iconOnly ? (size === 'sm' ? 'size-9 p-0' : 'size-11 p-0') : (size === 'sm' ? 'h-9 px-3 text-sm' : 'h-11 px-4 text-sm')} ${variants[variant]} ${className}`}
      disabled={disabled || loading}
      {...props}
    >
      {loading ? <span className="size-4 shrink-0 animate-spin rounded-full border-2 border-current border-r-transparent" aria-hidden="true" /> : startIcon && <span className="shrink-0" aria-hidden="true">{startIcon}</span>}
      {!iconOnly && children}
      {!loading && trailingIcon && <span className="shrink-0" aria-hidden="true">{trailingIcon}</span>}
    </Component>
  )
}
