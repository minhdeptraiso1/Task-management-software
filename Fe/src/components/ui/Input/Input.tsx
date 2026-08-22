import { forwardRef, type InputHTMLAttributes, type ReactNode } from 'react'
import { DatePicker } from '../DatePicker/DatePicker'

interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  label?: string
  error?: string
  hint?: string
  leadingIcon?: ReactNode
  trailing?: ReactNode
}

export const Input = forwardRef<HTMLInputElement, InputProps>(function Input({ label, error, hint, leadingIcon, trailing, className = '', id, ...props }, ref) {
  if (props.type === 'date') {
    return (
      <DatePicker
        ref={ref}
        id={id}
        label={label}
        error={error}
        hint={hint}
        leadingIcon={leadingIcon}
        className={className}
        value={String(props.value ?? props.defaultValue ?? '')}
        onChange={props.onChange as any}
        name={props.name}
        required={props.required}
        disabled={props.disabled}
        placeholder={props.placeholder}
        min={props.min ? String(props.min) : undefined}
        max={props.max ? String(props.max) : undefined}
      />
    )
  }

  const inputId = id ?? props.name
  return (
    <div className="w-full">
      {label && <label className="mb-2 block text-sm font-medium text-ink" htmlFor={inputId}>{label}{props.required && <span className="ml-1 text-danger" aria-hidden="true">*</span>}</label>}
      <div className="relative">
        {leadingIcon && <span className="pointer-events-none absolute left-3.5 top-1/2 -translate-y-1/2 text-muted">{leadingIcon}</span>}
        <input ref={ref} id={inputId} className={`h-11 w-full rounded-lg border bg-white px-3.5 text-sm text-ink outline-none transition placeholder:text-muted focus:border-brand focus:ring-2 focus:ring-brand/20 disabled:border-line disabled:bg-panel disabled:text-muted ${leadingIcon ? 'pl-10' : ''} ${trailing ? 'pr-11' : ''} ${error ? 'border-danger' : 'border-line'} ${className}`} {...props} />
        {trailing && <span className="absolute right-2 top-1/2 -translate-y-1/2">{trailing}</span>}
      </div>
      {(error || hint) && <p className={`mt-1.5 text-xs ${error ? 'text-danger' : 'text-muted'}`}>{error || hint}</p>}
    </div>
  )
})
