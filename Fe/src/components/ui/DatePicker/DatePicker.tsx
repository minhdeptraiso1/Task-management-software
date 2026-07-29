import { useState, useRef, useEffect, forwardRef, type ReactNode } from 'react'
import { createPortal } from 'react-dom'
import { Calendar as CalendarIcon, ChevronLeft, ChevronRight, X, Clock } from 'lucide-react'

export interface DatePickerProps {
  value?: string
  onChange?: (e: { target: { value: string; name?: string } }) => void
  label?: string
  error?: string
  hint?: string
  placeholder?: string
  required?: boolean
  disabled?: boolean
  name?: string
  id?: string
  className?: string
  min?: string
  max?: string
  leadingIcon?: ReactNode
}

const VIETNAMESE_MONTHS = [
  'Tháng 1', 'Tháng 2', 'Tháng 3', 'Tháng 4', 'Tháng 5', 'Tháng 6',
  'Tháng 7', 'Tháng 8', 'Tháng 9', 'Tháng 10', 'Tháng 11', 'Tháng 12'
]

const VIETNAMESE_WEEKDAYS = ['T2', 'T3', 'T4', 'T5', 'T6', 'T7', 'CN']

function formatDateToString(d: Date): string {
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function parseStringToDate(str?: string): Date | null {
  if (!str) return null
  const parts = str.split('-')
  if (parts.length !== 3) return null
  const y = parseInt(parts[0], 10)
  const m = parseInt(parts[1], 10) - 1
  const d = parseInt(parts[2], 10)
  if (isNaN(y) || isNaN(m) || isNaN(d)) return null
  return new Date(y, m, d)
}

function formatDisplayString(str?: string): string {
  const d = parseStringToDate(str)
  if (!d) return ''
  const day = String(d.getDate()).padStart(2, '0')
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const year = d.getFullYear()
  return `${day}/${month}/${year}`
}

export const DatePicker = forwardRef<HTMLInputElement, DatePickerProps>(function DatePicker(
  {
    value = '',
    onChange,
    label,
    error,
    hint,
    placeholder = 'dd/mm/yyyy',
    required,
    disabled,
    name,
    id,
    className = '',
    min,
    max,
    leadingIcon
  },
  ref
) {
  const [open, setOpen] = useState(false)
  const triggerRef = useRef<HTMLDivElement>(null)
  const popoverRef = useRef<HTMLDivElement>(null)
  const [popoverCoords, setPopoverCoords] = useState<{ top: number; left: number }>({ top: 0, left: 0 })

  // Current viewing month & year in calendar popup
  const initialDate = parseStringToDate(value) || new Date()
  const [viewYear, setViewYear] = useState(initialDate.getFullYear())
  const [viewMonth, setViewMonth] = useState(initialDate.getMonth())

  // Keep view in sync when value changes externally
  useEffect(() => {
    const d = parseStringToDate(value)
    if (d) {
      setViewYear(d.getFullYear())
      setViewMonth(d.getMonth())
    }
  }, [value])

  // Position portal popover when open
  useEffect(() => {
    if (open && triggerRef.current) {
      const rect = triggerRef.current.getBoundingClientRect()
      const popoverHeight = 350
      const popoverWidth = 320
      const spaceBelow = window.innerHeight - rect.bottom
      const placeAbove = spaceBelow < popoverHeight && rect.top > popoverHeight

      const top = placeAbove ? rect.top - popoverHeight - 8 : rect.bottom + 6
      const left = Math.min(Math.max(12, rect.left), window.innerWidth - popoverWidth - 12)

      setPopoverCoords({ top, left })
    }
  }, [open])

  // Close popup on outside click
  useEffect(() => {
    function handleClickOutside(e: MouseEvent) {
      const isTrigger = triggerRef.current && triggerRef.current.contains(e.target as Node)
      const isPopover = popoverRef.current && popoverRef.current.contains(e.target as Node)
      if (!isTrigger && !isPopover) {
        setOpen(false)
      }
    }
    if (open) {
      document.addEventListener('mousedown', handleClickOutside)
      window.addEventListener('scroll', () => {
        if (triggerRef.current) {
          const rect = triggerRef.current.getBoundingClientRect()
          const popoverHeight = 350
          const popoverWidth = 320
          const spaceBelow = window.innerHeight - rect.bottom
          const placeAbove = spaceBelow < popoverHeight && rect.top > popoverHeight
          const top = placeAbove ? rect.top - popoverHeight - 8 : rect.bottom + 6
          const left = Math.min(Math.max(12, rect.left), window.innerWidth - popoverWidth - 12)
          setPopoverCoords({ top, left })
        }
      }, true)
    }
    return () => {
      document.removeEventListener('mousedown', handleClickOutside)
    }
  }, [open])

  const handleSelectDate = (dateStr: string) => {
    if (disabled) return
    if (onChange) {
      onChange({ target: { value: dateStr, name } })
    }
    setOpen(false)
  }

  const handleClear = (e: React.MouseEvent) => {
    e.stopPropagation()
    if (disabled) return
    if (onChange) {
      onChange({ target: { value: '', name } })
    }
  }

  const handleSelectToday = () => {
    const todayStr = formatDateToString(new Date())
    handleSelectDate(todayStr)
  }

  const prevMonth = () => {
    if (viewMonth === 0) {
      setViewMonth(11)
      setViewYear(v => v - 1)
    } else {
      setViewMonth(v => v - 1)
    }
  }

  const nextMonth = () => {
    if (viewMonth === 11) {
      setViewMonth(0)
      setViewYear(v => v + 1)
    } else {
      setViewMonth(v => v + 1)
    }
  }

  // Generate 42 calendar grid days
  const firstDayOfMonth = new Date(viewYear, viewMonth, 1)
  let startingDayOfWeek = firstDayOfMonth.getDay() - 1
  if (startingDayOfWeek < 0) startingDayOfWeek = 6

  const daysInMonth = new Date(viewYear, viewMonth + 1, 0).getDate()
  const daysInPrevMonth = new Date(viewYear, viewMonth, 0).getDate()

  const calendarDays: Array<{ dateStr: string; dayNum: number; isCurrentMonth: boolean }> = []

  // Prev month padding days
  for (let i = startingDayOfWeek - 1; i >= 0; i--) {
    const dayNum = daysInPrevMonth - i
    const m = viewMonth === 0 ? 11 : viewMonth - 1
    const y = viewMonth === 0 ? viewYear - 1 : viewYear
    const dStr = formatDateToString(new Date(y, m, dayNum))
    calendarDays.push({ dateStr: dStr, dayNum, isCurrentMonth: false })
  }

  // Current month days
  for (let d = 1; d <= daysInMonth; d++) {
    const dStr = formatDateToString(new Date(viewYear, viewMonth, d))
    calendarDays.push({ dateStr: dStr, dayNum: d, isCurrentMonth: true })
  }

  // Next month padding days
  const remaining = 42 - calendarDays.length
  for (let d = 1; d <= remaining; d++) {
    const m = viewMonth === 11 ? 0 : viewMonth + 1
    const y = viewMonth === 11 ? viewYear + 1 : viewYear
    const dStr = formatDateToString(new Date(y, m, d))
    calendarDays.push({ dateStr: dStr, dayNum: d, isCurrentMonth: false })
  }

  const todayStr = formatDateToString(new Date())
  const inputId = id ?? name

  return (
    <div className="w-full relative">
      {label && (
        <label className="mb-2 block text-sm font-medium text-[#3f3f46]" htmlFor={inputId}>
          {label}
          {required && <span className="ml-1 text-danger" aria-hidden="true">*</span>}
        </label>
      )}

      {/* Hidden input for ref / form compatibility */}
      <input
        ref={ref}
        type="hidden"
        id={inputId}
        name={name}
        value={value}
        required={required}
        disabled={disabled}
      />

      {/* Custom Input Trigger (Styled exactly like standard Input/Select) */}
      <div
        ref={triggerRef}
        onClick={() => !disabled && setOpen(!open)}
        className={`h-11 w-full rounded-lg border bg-white px-3.5 text-sm outline-none transition flex items-center justify-between cursor-pointer select-none ${
          open ? 'border-brand ring-2 ring-brand/20' : error ? 'border-danger' : 'border-line hover:border-slate-300'
        } ${disabled ? 'border-line bg-panel text-muted cursor-not-allowed' : ''} ${className}`}
      >
        <div className="flex items-center gap-2.5 overflow-hidden">
          {leadingIcon ? (
            <span className="text-[#879087] shrink-0">{leadingIcon}</span>
          ) : (
            <CalendarIcon size={16} className={`shrink-0 transition-colors ${value ? 'text-brand' : 'text-[#879087]'}`} />
          )}
          <span className={`truncate text-sm ${value ? 'text-ink font-medium' : 'text-muted font-normal'}`}>
            {value ? formatDisplayString(value) : placeholder}
          </span>
        </div>

        <div className="flex items-center gap-1.5 shrink-0 ml-2">
          {value && !disabled && (
            <button
              type="button"
              onClick={handleClear}
              className="p-1 rounded-full text-slate-400 hover:text-slate-700 hover:bg-slate-100 transition"
              title="Xóa ngày"
            >
              <X size={14} />
            </button>
          )}
        </div>
      </div>

      {(error || hint) && (
        <p className={`mt-1.5 text-xs font-medium ${error ? 'text-danger' : 'text-slate-500'}`}>{error || hint}</p>
      )}

      {/* High-Contrast Portal Popover Calendar Dropdown */}
      {open &&
        createPortal(
          <div
            ref={popoverRef}
            style={{ top: `${popoverCoords.top}px`, left: `${popoverCoords.left}px` }}
            className="fixed z-[999999] w-80 rounded-2xl border-2 border-slate-300 bg-white p-4.5 shadow-2xl animate-enter text-slate-900 select-none"
          >
            {/* Calendar Header (Month/Year & Arrows) */}
            <div className="flex items-center justify-between mb-3 px-1">
              <span className="font-black text-slate-900 text-base">
                {VIETNAMESE_MONTHS[viewMonth]} năm {viewYear}
              </span>

              <div className="flex items-center gap-1.5">
                <button
                  type="button"
                  onClick={prevMonth}
                  className="size-8 rounded-xl border border-slate-300 bg-slate-50 text-slate-800 hover:bg-slate-200 transition flex items-center justify-center font-bold"
                  title="Tháng trước"
                >
                  <ChevronLeft size={18} />
                </button>
                <button
                  type="button"
                  onClick={nextMonth}
                  className="size-8 rounded-xl border border-slate-300 bg-slate-50 text-slate-800 hover:bg-slate-200 transition flex items-center justify-center font-bold"
                  title="Tháng sau"
                >
                  <ChevronRight size={18} />
                </button>
              </div>
            </div>

            {/* Weekdays Header */}
            <div className="grid grid-cols-7 gap-1 text-center mb-1.5">
              {VIETNAMESE_WEEKDAYS.map((day, idx) => (
                <div key={idx} className="text-xs font-black text-slate-800 py-1 uppercase tracking-wider">
                  {day}
                </div>
              ))}
            </div>

            {/* Days Grid */}
            <div className="grid grid-cols-7 gap-1">
              {calendarDays.map((item, idx) => {
                const isSelected = item.dateStr === value
                const isToday = item.dateStr === todayStr
                const isDisabled = disabled || (min ? item.dateStr < min : false) || (max ? item.dateStr > max : false)

                let btnClass = 'h-9 w-full rounded-xl text-sm font-extrabold transition-all flex items-center justify-center relative cursor-pointer '

                if (isDisabled) {
                  btnClass = 'h-9 w-full rounded-xl text-sm font-semibold text-slate-300 bg-slate-50 cursor-not-allowed opacity-40'
                } else if (isSelected) {
                  btnClass += 'bg-brand text-white shadow-lg shadow-brand/30 scale-105 font-black z-10'
                } else if (isToday) {
                  btnClass += 'border-2 border-brand text-brand-dark font-black bg-brand-soft/70'
                } else if (!item.isCurrentMonth) {
                  btnClass += 'text-slate-400 hover:bg-slate-100 hover:text-slate-700 font-medium'
                } else {
                  btnClass += 'text-slate-900 hover:bg-brand-soft hover:text-brand-dark font-bold'
                }

                return (
                  <button
                    key={idx}
                    type="button"
                    disabled={isDisabled}
                    onClick={() => !isDisabled && handleSelectDate(item.dateStr)}
                    className={btnClass}
                  >
                    {item.dayNum}
                  </button>
                )
              })}
            </div>

            {/* Quick Actions Footer */}
            <div className="mt-3.5 pt-3 border-t border-slate-200 flex items-center justify-between text-xs">
              <button
                type="button"
                onClick={handleSelectToday}
                className="flex items-center gap-1.5 font-black text-brand hover:text-brand-dark transition px-2.5 py-1.5 rounded-xl hover:bg-brand-soft"
              >
                <Clock size={14} />
                <span>Hôm nay</span>
              </button>

              {value && (
                <button
                  type="button"
                  onClick={handleClear}
                  className="font-bold text-slate-500 hover:text-slate-800 transition px-2 py-1 rounded-xl hover:bg-slate-100"
                >
                  Xóa ngày
                </button>
              )}
            </div>
          </div>,
          document.body
        )}
    </div>
  )
})
