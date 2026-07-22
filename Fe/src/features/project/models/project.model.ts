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
  startDateFrom?: string
  startDateTo?: string
  endDateFrom?: string
  endDateTo?: string
  createdFrom?: string
  createdTo?: string
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
  performedByUsername: string
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

export interface ProjectActivityDetailResponse {
  id: string
  projectId: string
  entityType: string
  entityId: string
  action: string
  performedByUserId: string | null
  performedByUsername: string
  performedByEmail: string | null
  oldValueJson: string | null
  newValueJson: string | null
  displayMessage: string
  createdAt: string
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
  PROJECT_STATUS_CHANGED: 'Đổi trạng thái dự án',
  PROJECT_DELETED: 'Xóa dự án',
  MEMBER_ADDED: 'Thêm thành viên',
  MEMBER_REMOVED: 'Xóa thành viên',
  MEMBER_ROLE_CHANGED: 'Đổi vai trò thành viên',
  BACKLOG_ITEM_CREATED: 'Tạo Backlog Item',
  BACKLOG_ITEM_UPDATED: 'Cập nhật Backlog Item',
  BACKLOG_ITEM_DELETED: 'Xóa Backlog Item',
  BACKLOG_ITEM_STATUS_CHANGED: 'Đổi trạng thái Backlog Item',
  BACKLOG_ITEM_PRIORITY_CHANGED: 'Đổi ưu tiên Backlog Item',
  BACKLOG_ITEM_ADDED_TO_SPRINT: 'Thêm vào Sprint',
  BACKLOG_ITEM_REMOVED_FROM_SPRINT: 'Xóa khỏi Sprint',
  SPRINT_CREATED: 'Tạo Sprint',
  SPRINT_UPDATED: 'Cập nhật Sprint',
  SPRINT_DELETED: 'Xóa Sprint',
  SPRINT_STARTED: 'Bắt đầu Sprint',
  SPRINT_COMPLETED: 'Hoàn thành Sprint',
  SPRINT_CANCELLED: 'Hủy Sprint',
  TASK_CREATED: 'Tạo Task',
  TASK_UPDATED: 'Cập nhật Task',
  TASK_DELETED: 'Xóa Task',
  TASK_STATUS_CHANGED: 'Đổi trạng thái Task',
  TASK_ASSIGNED: 'Phân công Task',
  TASK_UNASSIGNED: 'Gỡ phân công Task',
  TASK_IMPORTED: 'Import Task',
  TASK_PRIORITY_CHANGED: 'Đổi ưu tiên Task',
  TASK_POSITION_CHANGED: 'Đổi vị trí Task',
  TASK_MOVED_TO_SPRINT: 'Chuyển Task vào Sprint',
  TASK_REMOVED_FROM_SPRINT: 'Gỡ Task khỏi Sprint',
  TASK_BLOCKED: 'Chặn Task',
  TASK_REOPENED: 'Mở lại Task',
  TASK_CANCELLED: 'Hủy Task',
  TASK_UNBLOCKED: 'Mở chặn Task',
  TASK_DEPENDENCY_ADDED: 'Thêm liên kết phụ thuộc',
  TASK_DEPENDENCY_REMOVED: 'Xóa liên kết phụ thuộc',
  BACKLOG_ITEM_POSITION_CHANGED: 'Đổi vị trí Backlog Item',
  BUG_CREATED: 'Báo lỗi',
  COMMENT_CREATED: 'Thêm bình luận',
  COMMENT_UPDATED: 'Cập nhật bình luận',
  COMMENT_DELETED: 'Xóa bình luận',
  TIME_LOG_CREATED: 'Log thời gian',
  TIME_LOG_UPDATED: 'Cập nhật log thời gian',
  TIME_LOG_DELETED: 'Xóa log thời gian',
}

export const entityTypeLabels: Record<string, string> = {
  PROJECT: 'Dự án',
  PROJECT_MEMBER: 'Thành viên',
  SPRINT: 'Sprint',
  BACKLOG_ITEM: 'Backlog Item',
  TASK: 'Task',
  TASK_COMMENT: 'Bình luận',
  TASK_TIME_LOG: 'Time Log',
}
