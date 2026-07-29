import type { ReactNode } from 'react'

export interface CollapsiblePanelProps {
  open: boolean
  children: ReactNode
  className?: string
  innerClassName?: string
}

/**
 * Reusable Collapsible Panel with smooth CSS Grid Accordion animation ("đổ từ từ").
 */
export function CollapsiblePanel({
  open,
  children,
  className = '',
  innerClassName = 'p-4 bg-white border border-line rounded-xl space-y-4 text-sm shadow-sm'
}: CollapsiblePanelProps) {
  return (
    <div
      className={`grid transition-all duration-300 ease-in-out ${
        open
          ? 'grid-rows-[1fr] opacity-100 mb-4'
          : 'grid-rows-[0fr] opacity-0 mb-0 pointer-events-none'
      } ${className}`}
    >
      <div className="overflow-hidden">
        <div className={innerClassName}>
          {children}
        </div>
      </div>
    </div>
  )
}
