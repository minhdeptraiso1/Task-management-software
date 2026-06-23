export type UserRole =
  | 'ADMIN'
  | 'PROJECT_MANAGER'
  | 'SCRUM_MASTER'
  | 'PRODUCT_OWNER'
  | 'DEVELOPER'
  | 'TESTER'
  | 'VIEWER'

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
  PROJECT_MANAGER: 'Quản lý dự án',
  SCRUM_MASTER: 'Scrum Master',
  PRODUCT_OWNER: 'Product Owner',
  DEVELOPER: 'Developer',
  TESTER: 'Tester',
  VIEWER: 'Người xem',
}

export const roleDescriptions: Record<UserRole, string> = {
  ADMIN: 'Quản trị toàn bộ hệ thống, người dùng và phân quyền.',
  PROJECT_MANAGER: 'Quản lý dự án, thành viên và tiến độ.',
  SCRUM_MASTER: 'Điều phối sprint, daily meeting và quy trình Scrum.',
  PRODUCT_OWNER: 'Quản lý product backlog và ưu tiên yêu cầu.',
  DEVELOPER: 'Thực hiện công việc phát triển trong dự án.',
  TESTER: 'Kiểm thử, xác nhận chất lượng và phản hồi lỗi.',
  VIEWER: 'Chỉ xem dữ liệu được cấp quyền.',
}
