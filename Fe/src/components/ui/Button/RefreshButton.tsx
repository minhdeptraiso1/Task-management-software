import { useState } from 'react'
import { RefreshCw } from 'lucide-react'
import { Button } from './Button'
import type { ButtonProps } from './Button.types'

export interface RefreshButtonProps extends Omit<ButtonProps, 'onClick'> {
  onRefresh?: () => void | Promise<void>
  refreshing?: boolean
  label?: string
  onClick?: () => void | Promise<void>
}

export function RefreshButton({
  onRefresh,
  onClick,
  refreshing: externalRefreshing,
  label = 'Làm mới',
  variant = 'outline-amber',
  size = 'sm',
  iconOnly = false,
  title = 'Làm mới dữ liệu',
  className = '',
  children,
  ...props
}: RefreshButtonProps) {
  const [internalRefreshing, setInternalRefreshing] = useState(false)
  const isSpinning = externalRefreshing ?? internalRefreshing

  const handleClick = async () => {
    const refreshHandler = onRefresh || onClick
    if (refreshHandler) {
      setInternalRefreshing(true)
      try {
        await refreshHandler()
      } finally {
        setTimeout(() => setInternalRefreshing(false), 500)
      }
    }
  }

  return (
    <Button
      variant={variant}
      size={size}
      iconOnly={iconOnly}
      title={title}
      aria-label={props['aria-label'] || title}
      onClick={handleClick}
      className={className}
      leadingIcon={<RefreshCw size={size === 'sm' ? 14 : 16} className={isSpinning ? 'animate-spin' : ''} />}
      {...props}
    >
      {!iconOnly && (children || label)}
    </Button>
  )
}
