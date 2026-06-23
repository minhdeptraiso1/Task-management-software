import type { ButtonHTMLAttributes, ReactNode } from 'react'

export type ButtonVariant = 'primary' | 'secondary' | 'ghost' | 'danger'
export interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: ButtonVariant
  size?: 'sm' | 'md'
  loading?: boolean
  /** @deprecated Dùng leadingIcon để vị trí icon rõ ràng hơn. */
  icon?: ReactNode
  leadingIcon?: ReactNode
  trailingIcon?: ReactNode
  iconOnly?: boolean
}
