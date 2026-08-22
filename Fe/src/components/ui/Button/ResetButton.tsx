import { useState } from 'react'
import { RotateCw } from 'lucide-react'
import { Button } from './Button'
import type { ButtonProps } from './Button.types'

export interface ResetButtonProps extends Omit<ButtonProps, 'onClick'> {
  onReset?: () => void | Promise<void>
  resetting?: boolean
  label?: string
  onClick?: () => void | Promise<void>
}

export function ResetButton({
  onReset,
  onClick,
  resetting: externalResetting,
  label = 'Đặt lại',
  variant = 'outline-amber',
  size = 'sm',
  iconOnly = false,
  title = 'Đặt lại bộ lọc',
  className = '',
  children,
  ...props
}: ResetButtonProps) {
  const [internalResetting, setInternalResetting] = useState(false)
  const isSpinning = externalResetting ?? internalResetting

  const handleClick = async () => {
    const resetHandler = onReset || onClick
    if (resetHandler) {
      setInternalResetting(true)
      try {
        await resetHandler()
      } finally {
        setTimeout(() => setInternalResetting(false), 500)
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
      leadingIcon={<RotateCw size={size === 'sm' ? 14 : 16} className={isSpinning ? 'animate-spin' : ''} />}
      {...props}
    >
      {!iconOnly && (children || label)}
    </Button>
  )
}
