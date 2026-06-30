import type { ReactNode } from 'react'
import { Funnel, Plus } from 'lucide-react'
import { Button } from '../Button'

interface ToolbarActionsProps {
  filterLabel?: string
  createLabel?: string
  loading?: boolean
  canCreate?: boolean
  createIcon?: ReactNode
  onFilter: () => void
  onCreate?: () => void
}

export function ToolbarActions({
  filterLabel = 'Lọc',
  createLabel = 'Tạo mới',
  loading,
  canCreate = true,
  createIcon,
  onFilter,
  onCreate,
}: ToolbarActionsProps) {
  return <div className="flex flex-wrap items-center gap-3">
    <Button variant="outline-indigo" leadingIcon={<Funnel size={17} />} loading={loading} onClick={onFilter}>
      {filterLabel}
    </Button>
    {canCreate && onCreate && (
      <Button leadingIcon={createIcon ?? <Plus size={18} />} onClick={onCreate}>
        {createLabel}
      </Button>
    )}
  </div>
}
