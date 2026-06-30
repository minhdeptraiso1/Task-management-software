import type { ButtonHTMLAttributes, ReactNode, ElementType } from 'react'

export type ButtonVariant = 
  | 'primary' 
  | 'secondary' 
  | 'ghost' 
  | 'danger'
  | 'solid-blue'
  | 'solid-green'
  | 'solid-red'
  | 'outline-blue'
  | 'outline-green'
  | 'outline-red'
  | 'outline-amber'
  | 'outline-indigo'
  | 'outline-teal'

export interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement | HTMLLabelElement> {
  as?: ElementType
  variant?: ButtonVariant
  size?: 'sm' | 'md'
  loading?: boolean
  /** @deprecated Dùng leadingIcon để vị trí icon rõ ràng hơn. */
  icon?: ReactNode
  leadingIcon?: ReactNode
  trailingIcon?: ReactNode
  iconOnly?: boolean
}
