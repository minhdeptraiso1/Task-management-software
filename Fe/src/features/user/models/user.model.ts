export type UserRole = 'ADMIN' | 'MANAGER' | 'EMPLOYEE'

export interface User {
  id: string
  username: string
  email: string
  enabled: boolean
  role: UserRole
}

export interface UserPage {
  content: User[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

export interface UserFilters {
  keyword: string
  role: '' | UserRole
  enabled: '' | 'true' | 'false'
}

export interface CreateUserData {
  username: string
  email: string
  password: string
  role: UserRole
}

export interface UpdateUserData {
  password?: string
  role?: UserRole
  enabled?: boolean
}

export const roleLabels: Record<UserRole, string> = {
  ADMIN: 'Quản trị viên',
  MANAGER: 'Quản lý',
  EMPLOYEE: 'Nhân viên',
}

export const roleDescriptions: Record<UserRole, string> = {
  ADMIN: 'Quản lý tài khoản, audit log và cấu hình toàn hệ thống.',
  MANAGER: 'Tạo dự án, điều phối dự án và quản lý thành viên trong phạm vi được quyền.',
  EMPLOYEE: 'Tham gia các dự án được thêm vào và thao tác theo vai trò trong từng dự án.',
}
