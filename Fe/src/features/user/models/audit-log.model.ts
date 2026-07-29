export interface AuditLog {
  id: string
  userId: string
  username?: string | null
  email?: string | null
  action: string
  createdAt: string
}

export interface AuditLogPage {
  content: AuditLog[]
  totalElements: number
  totalPages: number
  number: number
  size: number
  numberOfElements: number
  first: boolean
  last: boolean
  empty: boolean
}

export const auditActionLabels: Record<string, string> = {
  LOGIN: 'Đăng nhập',
  LOGOUT: 'Đăng xuất',
  CHANGE_PASSWORD: 'Đổi mật khẩu',
  LOGOUT_ALL: 'Đăng xuất mọi thiết bị',
}
