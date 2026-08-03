export interface AdminUserSummaryResponse {
  totalUsers: number
  activeUsers: number
  disabledUsers: number
  adminUsers: number
  managerUsers: number
  employeeUsers: number
}

export interface AdminProjectSummaryResponse {
  totalProjects: number
  planningProjects: number
  activeProjects: number
  onHoldProjects: number
  completedProjects: number
  cancelledProjects: number
  archivedProjects: number
}

export interface AdminSprintSummaryResponse {
  totalSprints: number
  planningSprints: number
  activeSprints: number
  completedSprints: number
  cancelledSprints: number
}

export interface AdminTaskSummaryResponse {
  totalTasks: number
  todoTasks: number
  inProgressTasks: number
  inReviewTasks: number
  blockedTasks: number
  doneTasks: number
  cancelledTasks: number
  overdueTasks: number
}

export interface AdminBugSummaryResponse {
  totalBugs: number
  openBugs: number
  inProgressBugs: number
  resolvedBugs: number
  closedBugs: number
  criticalBugs: number
}

export interface AdminAttachmentSummaryResponse {
  totalAttachments: number
  totalSizeBytes: number
}

export interface AdminSystemSummaryResponse {
  generatedAt: string
  timezone: string
}

export interface ActuatorHealthComponent {
  status: string
  details?: Record<string, unknown>
}

export interface ActuatorHealthResponse {
  status: string
  components?: Record<string, ActuatorHealthComponent>
}

export interface ActuatorInfoResponse {
  app?: {
    name?: string
    description?: string
    version?: string
  }
}

export interface AdminSystemStatusResponse {
  health: ActuatorHealthResponse
  info: ActuatorInfoResponse
  checkedAt: string
}

export interface AdminDashboardResponse {
  userSummary: AdminUserSummaryResponse
  projectSummary: AdminProjectSummaryResponse
  sprintSummary: AdminSprintSummaryResponse
  taskSummary: AdminTaskSummaryResponse
  bugSummary: AdminBugSummaryResponse
  attachmentSummary: AdminAttachmentSummaryResponse
  systemSummary: AdminSystemSummaryResponse
}

export interface AdminUserResponse {
  id: string
  username: string
  email: string
  role: 'ADMIN' | 'MANAGER' | 'EMPLOYEE'
  enabled: boolean
  createdAt: string
  updatedAt: string
}

export interface AdminUserActivityResponse {
  id: string
  projectId: string | null
  entityType: string
  entityId: string
  action: string
  performedByUserId: string
  oldValueJson: string | null
  newValueJson: string | null
  createdAt: string
}

export interface AdminUserActivityPageResponse {
  content: AdminUserActivityResponse[]
  totalElements: number
  totalPages: number
  number: number
  size: number
  numberOfElements: number
  first: boolean
  last: boolean
  empty: boolean
}

export interface SystemAuditLogResponse {
  id: string
  actorUserId: string | null
  actorUsername: string | null
  actorEmail: string | null
  action: string
  resourceType: string
  resourceId: string | null
  ipAddress: string | null
  userAgent: string | null
  oldValueJson: string | null
  newValueJson: string | null
  success: boolean
  errorMessage: string | null
  createdAt: string
}

export interface SystemAuditLogPageResponse {
  content: SystemAuditLogResponse[]
  totalElements: number
  totalPages: number
  number: number
  size: number
  numberOfElements: number
  first: boolean
  last: boolean
  empty: boolean
}

export interface AdminAuditSummaryItem {
  name: string
  count: number
}

export interface AdminAuditDailySummary {
  date: string
  count: number
}

export interface AdminAuditSummaryResponse {
  totalLogs: number
  authLogs: number
  userLogs: number
  projectLogs: number
  fileLogs: number
  importLogs: number
  byAction: AdminAuditSummaryItem[]
  byResourceType: AdminAuditSummaryItem[]
  byDate: AdminAuditDailySummary[]
}

export interface AdminImportAuditResponse {
  batchId: string
  projectId: string
  projectCode: string
  projectName: string
  sprintId: string | null
  sprintName: string | null
  importedByUserId: string
  importedByUsername: string
  fileName: string
  status: string
  totalRows: number
  successRows: number
  failedRows: number
  startedAt: string | null
  completedAt: string | null
  createdAt: string
}

export interface DailyDigestRunResponse {
  businessDate: string
  scannedUsers: number
  sentDigests: number
  skippedUsers: number
}

export interface AdminImportAuditPageResponse {
  content: AdminImportAuditResponse[]
  totalElements: number
  totalPages: number
  number: number
  size: number
  numberOfElements: number
  first: boolean
  last: boolean
  empty: boolean
}

export const systemAuditActionLabels: Record<string, string> = {
  LOGIN_SUCCESS: 'Đăng nhập thành công',
  LOGIN_FAILED: 'Đăng nhập thất bại',
  LOGOUT: 'Đăng xuất',
  REFRESH_TOKEN_USED: 'Sử dụng Refresh Token',
  REFRESH_TOKEN_REVOKED: 'Thu hồi Refresh Token',
  USER_CREATED: 'Tạo tài khoản',
  USER_ENABLED: 'Kích hoạt tài khoản',
  USER_DISABLED: 'Vô hiệu hóa tài khoản',
  USER_ROLE_CHANGED: 'Thay đổi vai trò',
  PASSWORD_CHANGED: 'Đổi mật khẩu',
  FILE_DOWNLOADED: 'Tải file đính kèm',
  FILE_DELETED: 'Xóa file đính kèm',
  IMPORT_EXECUTED: 'Thực thi Import',
  IMPORT_FAILED: 'Import thất bại',
  ADMIN_VIEWED_USER_ACTIVITY: 'Xem nhật ký user',
}
