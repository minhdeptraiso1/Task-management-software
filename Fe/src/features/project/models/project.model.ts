export type ProjectStatus = 'PLANNING' | 'ACTIVE' | 'ON_HOLD' | 'COMPLETED' | 'CANCELLED' | 'ARCHIVED'
export type ProjectMemberRole = 'OWNER' | 'PROJECT_MANAGER' | 'SCRUM_MASTER' | 'PRODUCT_OWNER' | 'DEVELOPER' | 'TESTER' | 'VIEWER'
export type ProjectActivityAction =
  | 'PROJECT_CREATED'
  | 'PROJECT_UPDATED'
  | 'PROJECT_STATUS_CHANGED'
  | 'PROJECT_DELETED'
  | 'MEMBER_ADDED'
  | 'MEMBER_REMOVED'
  | 'MEMBER_ROLE_CHANGED'
  | string

export interface Project {
  id: string
  code: string
  name: string
  description: string | null
  status: ProjectStatus
  startDate: string | null
  endDate: string | null
  createdByUserId: string
  currentUserRole: ProjectMemberRole | null
  createdAt: string
  updatedAt: string | null
}

export interface ProjectPage {
  content: Project[]
  totalElements: number
  totalPages: number
  number: number
  size: number
  numberOfElements: number
  first: boolean
  last: boolean
  empty: boolean
}

export interface ProjectFilters {
  keyword: string
  status: '' | ProjectStatus
}

export interface CreateProjectData {
  code: string
  name: string
  description?: string
  startDate?: string
  endDate?: string
}

export interface UpdateProjectData {
  name?: string
  description?: string
  startDate?: string
  endDate?: string
}

export interface ProjectMember {
  id: string
  projectId: string
  userId: string
  username: string
  email: string
  systemRole: 'ADMIN' | 'MANAGER' | 'EMPLOYEE'
  projectRole: ProjectMemberRole
  joinedAt: string
  createdAt: string
}

export interface AddProjectMemberData {
  userId: string
  role: ProjectMemberRole
}

export interface ProjectActivity {
  id: string
  projectId: string
  entityType: string
  entityId: string | null
  action: ProjectActivityAction
  performedByUserId: string | null
  oldValueJson: string | null
  newValueJson: string | null
  createdAt: string
}

export interface ProjectActivityPage {
  content: ProjectActivity[]
  totalElements: number
  totalPages: number
  number: number
  size: number
  numberOfElements: number
  first: boolean
  last: boolean
  empty: boolean
}

export const projectStatusLabels: Record<ProjectStatus, string> = {
  PLANNING: 'Lên kế hoạch',
  ACTIVE: 'Đang triển khai',
  ON_HOLD: 'Tạm dừng',
  COMPLETED: 'Hoàn thành',
  CANCELLED: 'Đã hủy',
  ARCHIVED: 'Lưu trữ',
}

export const projectMemberRoleLabels: Record<ProjectMemberRole, string> = {
  OWNER: 'Owner',
  PROJECT_MANAGER: 'Project Manager',
  SCRUM_MASTER: 'Scrum Master',
  PRODUCT_OWNER: 'Product Owner',
  DEVELOPER: 'Developer',
  TESTER: 'Tester',
  VIEWER: 'Viewer',
}

export const projectActivityLabels: Record<string, string> = {
  PROJECT_CREATED: 'Tạo dự án',
  PROJECT_UPDATED: 'Cập nhật dự án',
  PROJECT_STATUS_CHANGED: 'Đổi trạng thái',
  PROJECT_DELETED: 'Xóa dự án',
  MEMBER_ADDED: 'Thêm thành viên',
  MEMBER_REMOVED: 'Xóa thành viên',
  MEMBER_ROLE_CHANGED: 'Đổi vai trò thành viên',
}
