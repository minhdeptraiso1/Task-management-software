import type { SelectHTMLAttributes } from 'react'

interface SelectProps extends SelectHTMLAttributes<HTMLSelectElement> {
  label?: string
  options: { label: string; value: string }[]
}

export function Select({ label, options, className = '', id, ...props }: SelectProps) {
  const selectId = id ?? props.name
  return <div className="w-full">
    {label && <label className="mb-2 block text-sm font-medium text-ink" htmlFor={selectId}>{label}{props.required && <span className="ml-1 text-danger" aria-hidden="true">*</span>}</label>}
    <select id={selectId} className={`h-11 w-full rounded-lg border border-line bg-white px-3 text-sm text-ink outline-none focus:border-brand focus:ring-2 focus:ring-brand/20 disabled:bg-panel disabled:text-muted ${className}`} {...props}>
      {options.map(option => <option key={option.value} value={option.value}>{option.label}</option>)}
    </select>
  </div>
}
