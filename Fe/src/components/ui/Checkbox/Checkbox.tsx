import { useId, type InputHTMLAttributes, type ReactNode } from 'react'

export interface CheckboxProps extends Omit<InputHTMLAttributes<HTMLInputElement>, 'type'> {
  label?: ReactNode
  description?: string
  error?: string
  containerClassName?: string
}

export function Checkbox({
  label,
  description,
  error,
  id,
  className = '',
  containerClassName = '',
  disabled,
  ...props
}: CheckboxProps) {
  const generatedId = useId()
  const checkboxId = id || generatedId

  return (
    <div className={`flex flex-col gap-1 ${containerClassName}`}>
      <label
        htmlFor={checkboxId}
        className={`inline-flex items-start gap-2.5 select-none ${
          disabled ? 'cursor-not-allowed opacity-60' : 'cursor-pointer'
        }`}
      >
        <input
          type="checkbox"
          id={checkboxId}
          disabled={disabled}
          className={`size-4 mt-0.5 rounded border-line text-brand focus:ring-2 focus:ring-brand/20 accent-brand cursor-pointer transition-colors ${className}`}
          {...props}
        />
        {(label || description) && (
          <div className="flex flex-col">
            {label && <span className="text-xs font-semibold text-ink leading-tight">{label}</span>}
            {description && <span className="text-[11px] text-muted leading-tight mt-0.5">{description}</span>}
          </div>
        )}
      </label>
      {error && <p className="text-xs font-medium text-danger">{error}</p>}
    </div>
  )
}
