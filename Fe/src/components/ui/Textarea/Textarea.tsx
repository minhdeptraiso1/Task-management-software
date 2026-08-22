import { useId, type TextareaHTMLAttributes } from 'react'
import { AlertCircle } from 'lucide-react'

export interface TextareaProps extends TextareaHTMLAttributes<HTMLTextAreaElement> {
  label?: string
  error?: string
  helperText?: string
  required?: boolean
  containerClassName?: string
}

export function Textarea({
  label,
  error,
  helperText,
  required,
  id,
  className = '',
  containerClassName = '',
  rows = 3,
  disabled,
  ...props
}: TextareaProps) {
  const generatedId = useId()
  const textareaId = id || generatedId

  return (
    <div className={`w-full ${containerClassName}`}>
      {label && (
        <label htmlFor={textareaId} className="mb-1.5 block text-xs font-semibold text-ink">
          {label}
          {required && <span className="ml-1 text-danger" aria-hidden="true">*</span>}
        </label>
      )}

      <textarea
        id={textareaId}
        rows={rows}
        disabled={disabled}
        className={`w-full rounded-xl border bg-white px-3.5 py-2.5 text-sm text-ink placeholder:text-muted transition-all duration-200 focus:outline-none focus:ring-2 disabled:bg-panel disabled:text-muted disabled:cursor-not-allowed ${
          error
            ? 'border-danger focus:border-danger focus:ring-danger/20'
            : 'border-line hover:border-muted focus:border-brand focus:ring-brand/20'
        } ${className}`}
        {...props}
      />

      {error ? (
        <p className="mt-1 flex items-center gap-1 text-xs font-medium text-danger" role="alert">
          <AlertCircle size={13} className="shrink-0" />
          {error}
        </p>
      ) : helperText ? (
        <p className="mt-1 text-xs text-muted">{helperText}</p>
      ) : null}
    </div>
  )
}
