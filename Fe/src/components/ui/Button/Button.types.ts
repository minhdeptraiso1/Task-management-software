import type { ButtonHTMLAttributes, ReactNode, ElementType } from 'react'

export type ButtonVariant = 
  | 'primary' 
  | 'secondary' 
  | 'ghost' 
  | 'outline'
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

export type ButtonTone = 'brand' | 'danger' | 'success' | 'warning' | 'info' | 'neutral' | 'dark'

export interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement | HTMLLabelElement> {
  as?: ElementType
  variant?: ButtonVariant
  tone?: ButtonTone
  active?: boolean
  size?: 'sm' | 'md'
  loading?: boolean
  /** @deprecated Dùng leadingIcon để vị trí icon rõ ràng hơn. */
  icon?: ReactNode
  leadingIcon?: ReactNode
  trailingIcon?: ReactNode
  iconOnly?: boolean
}
