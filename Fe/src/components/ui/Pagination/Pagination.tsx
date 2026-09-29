import { ChevronLeft, ChevronRight } from 'lucide-react'
import { Button } from '../Button'

export interface PaginationProps {
  page: number // 0-indexed
  totalPages: number
  onPageChange: (page: number) => void
  disabled?: boolean
  totalElements?: number
  showText?: boolean
  compact?: boolean
  className?: string
}

export function Pagination({
  page,
  totalPages,
  onPageChange,
  disabled = false,
  totalElements,
  showText = true,
  compact = false,
  className = '',
}: PaginationProps) {
  const safeTotalPages = Math.max(totalPages || 1, 1)
  const isFirst = page <= 0
  const isLast = page + 1 >= safeTotalPages

  return (
    <footer
      className={`flex items-center justify-between border-t border-line/60 px-5 py-3 text-xs text-muted font-medium select-none ${className}`}
    >
      {showText && (
        <p>
          Trang <span className="font-bold text-ink">{page + 1}</span> / {safeTotalPages}
          {typeof totalElements === 'number' && (
            <span className="ml-1.5 text-muted/80">({totalElements.toLocaleString()} mục)</span>
          )}
        </p>
      )}

      <div className="flex items-center gap-2">
        <Button
          variant="secondary"
          size="sm"
          disabled={disabled || isFirst}
          leadingIcon={<ChevronLeft size={compact ? 16 : 14} />}
          iconOnly={compact}
          onClick={() => onPageChange(Math.max(0, page - 1))}
          aria-label="Trang trước"
        >
          {!compact && 'Trước'}
        </Button>
        <Button
          variant="secondary"
          size="sm"
          disabled={disabled || isLast}
          trailingIcon={compact ? undefined : <ChevronRight size={14} />}
          leadingIcon={compact ? <ChevronRight size={16} /> : undefined}
          iconOnly={compact}
          onClick={() => onPageChange(Math.min(safeTotalPages - 1, page + 1))}
          aria-label="Trang sau"
        >
          {!compact && 'Sau'}
        </Button>
      </div>
    </footer>
  )
}
