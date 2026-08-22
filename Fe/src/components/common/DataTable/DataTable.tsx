import { type ReactNode } from 'react'
import { ChevronLeft, ChevronRight, Inbox } from 'lucide-react'
import { Button } from '../../ui'

export interface Column<T> {
  key: string
  title: ReactNode
  render?: (item: T, index: number) => ReactNode
  align?: 'left' | 'center' | 'right'
  className?: string
  width?: string | number
}

export interface DataTablePagination {
  currentPage: number
  totalPages: number
  pageSize: number
  totalElements: number
  onPageChange: (page: number) => void
}

export interface DataTableProps<T> {
  columns: Column<T>[]
  data: T[]
  loading?: boolean
  keyExtractor: (item: T) => string | number
  pagination?: DataTablePagination
  emptyMessage?: string
  emptyAction?: ReactNode
  selectedKeySet?: Set<string | number>
  onRowClick?: (item: T) => void
  className?: string
}

export function DataTable<T>({
  columns,
  data,
  loading = false,
  keyExtractor,
  pagination,
  emptyMessage = 'Không tìm thấy dữ liệu phù hợp',
  emptyAction,
  selectedKeySet,
  onRowClick,
  className = '',
}: DataTableProps<T>) {
  const getAlignClass = (align?: 'left' | 'center' | 'right') => {
    if (align === 'center') return 'text-center justify-center'
    if (align === 'right') return 'text-right justify-end font-mono tabular-nums'
    return 'text-left justify-start'
  }

  return (
    <div className={`w-full flex flex-col bg-white rounded-2xl border border-line shadow-2xs overflow-hidden ${className}`}>
      {/* Table Container */}
      <div className="w-full overflow-x-auto">
        <table className="w-full border-collapse text-sm text-left">
          {/* Header */}
          <thead>
            <tr className="border-b border-line bg-panel">
              {columns.map(col => (
                <th
                  key={col.key}
                  style={{ width: col.width }}
                  className={`px-4 py-3 text-xs font-semibold text-slate-700 uppercase tracking-wider ${getAlignClass(
                    col.align
                  )} ${col.className || ''}`}
                >
                  {col.title}
                </th>
              ))}
            </tr>
          </thead>

          {/* Body */}
          <tbody className="divide-y divide-line/60">
            {loading ? (
              // Skeleton loading state
              Array.from({ length: 5 }).map((_, rIdx) => (
                <tr key={`skel-row-${rIdx}`} className="animate-pulse">
                  {columns.map((_, cIdx) => (
                    <td key={`skel-col-${cIdx}`} className="px-4 py-3.5">
                      <div className="h-4 w-3/4 rounded-md bg-slate-200/70" />
                    </td>
                  ))}
                </tr>
              ))
            ) : data.length === 0 ? (
              // Empty state
              <tr>
                <td colSpan={columns.length} className="px-4 py-12 text-center">
                  <div className="flex flex-col items-center justify-center gap-2 text-muted">
                    <Inbox size={36} className="text-line stroke-[1.5]" />
                    <p className="text-sm font-medium">{emptyMessage}</p>
                    {emptyAction && <div className="mt-2">{emptyAction}</div>}
                  </div>
                </td>
              </tr>
            ) : (
              // Data Rows
              data.map((item, idx) => {
                const key = keyExtractor(item)
                const isSelected = selectedKeySet?.has(key)

                return (
                  <tr
                    key={key}
                    onClick={() => onRowClick?.(item)}
                    className={`transition-colors ${
                      isSelected ? 'bg-brand/5 hover:bg-brand/10' : 'hover:bg-canvas'
                    } ${onRowClick ? 'cursor-pointer' : ''}`}
                  >
                    {columns.map(col => (
                      <td
                        key={col.key}
                        className={`px-4 py-3.5 text-sm text-ink ${getAlignClass(col.align)} ${
                          col.className || ''
                        }`}
                      >
                        {col.render ? col.render(item, idx) : (item as any)[col.key]}
                      </td>
                    ))}
                  </tr>
                )
              })
            )}
          </tbody>
        </table>
      </div>

      {/* Pagination Footer */}
      {pagination && pagination.totalPages > 1 && (
        <div className="flex flex-wrap items-center justify-between gap-3 px-4 py-3 border-t border-line/60 bg-white text-xs text-muted">
          <span>
            Hiển thị{' '}
            <strong className="text-ink">
              {Math.min(
                (pagination.currentPage - 1) * pagination.pageSize + 1,
                pagination.totalElements
              )}
            </strong>{' '}
            -{' '}
            <strong className="text-ink">
              {Math.min(pagination.currentPage * pagination.pageSize, pagination.totalElements)}
            </strong>{' '}
            trong tổng số <strong className="text-ink">{pagination.totalElements}</strong> kết quả
          </span>

          <div className="flex items-center gap-1.5 ml-auto">
            <Button
              variant="secondary"
              size="sm"
              iconOnly
              leadingIcon={<ChevronLeft size={16} />}
              disabled={pagination.currentPage <= 1 || loading}
              onClick={() => pagination.onPageChange(pagination.currentPage - 1)}
              aria-label="Trang trước"
              title="Trang trước"
            />

            <span className="px-2 font-medium text-ink">
              Trang {pagination.currentPage} / {pagination.totalPages}
            </span>

            <Button
              variant="secondary"
              size="sm"
              iconOnly
              leadingIcon={<ChevronRight size={16} />}
              disabled={pagination.currentPage >= pagination.totalPages || loading}
              onClick={() => pagination.onPageChange(pagination.currentPage + 1)}
              aria-label="Trang sau"
              title="Trang sau"
            />
          </div>
        </div>
      )}
    </div>
  )
}
