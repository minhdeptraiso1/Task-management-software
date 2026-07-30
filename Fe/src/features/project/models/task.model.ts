export type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'IN_REVIEW' | 'DONE' | 'BLOCKED' | 'CANCELLED'
export type TaskPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT'
export type TaskType = 'DEVELOPMENT' | 'TESTING' | 'DESIGN' | 'DOCUMENTATION' | 'RESEARCH' | 'DEVOPS' | 'OTHER'

export interface Task {
  id: string
  projectId: string
  backlogItemId: string
  currentSprintId: string | null
  title: string
  description: string | null
  type: TaskType
  status: TaskStatus
  priority: TaskPriority
  assigneeUserId: string | null
  assigneeUsername: string | null
  assigneeEmail: string | null
  reporterUserId: string
  estimatedMinutes: number | null
  spentMinutes: number
  startDate: string | null
  dueDate: string | null
  completedAt: string | null
  blockReason: string | null
  blockedAt: string | null
  blockedByUserId: string | null
  position: number
  overdue: boolean
  blocked: boolean
  targetUrl: string | null
  createdAt: string
  updatedAt: string | null
}

export interface TaskPage {
  content: Task[]
  totalElements: number
  totalPages: number
  number: number
  size: number
  numberOfElements: number
  first: boolean
  last: boolean
  empty: boolean
}

export interface KanbanTask {
  id: string
  backlogItemId: string
  backlogItemTitle: string | null
  title: string
  type: TaskType
  status: TaskStatus
  priority: TaskPriority
  assigneeUserId: string | null
  assigneeUsername: string | null
  assigneeEmail: string | null
  estimatedMinutes: number | null
  spentMinutes: number
  startDate: string | null
  dueDate: string | null
  position: number
  overdue: boolean
  blocked: boolean
}

export interface KanbanColumn {
  status: TaskStatus
  title: string
  taskCount: number
  tasks: KanbanTask[]
}

export interface KanbanSummary {
  total: number
  todo: number
  inProgress: number
  inReview: number
  done: number
  blocked: number
  cancelled: number
}

export interface KanbanBoard {
  projectId: string
  sprintId: string
  sprintName: string
  sprintStatus: 'PLANNING' | 'ACTIVE' | 'COMPLETED' | 'CANCELLED'
  startDate: string | null
  endDate: string | null
  summary: KanbanSummary
  columns: KanbanColumn[]
}

export interface TaskCommentReply {
  id: string
  taskId: string
  userId: string
  username: string
  email: string
  parentCommentId: string
  content: string
  edited: boolean
  editedAt: string | null
  createdAt: string
  updatedAt: string | null
  canEdit: boolean
  canDelete: boolean
  mentionedUsernames?: string[]
}

export interface TaskComment {
  id: string
  taskId: string
  userId: string
  username: string
  email: string
  content: string
  edited: boolean
  editedAt: string | null
  createdAt: string
  updatedAt: string | null
  canEdit: boolean
  canDelete: boolean
  replyCount: number
  replies: TaskCommentReply[]
  mentionedUsernames?: string[]
}

export interface TaskCommentPage {
  content: TaskComment[]
  totalElements: number
  totalPages: number
  number: number
  size: number
  numberOfElements: number
  first: boolean
  last: boolean
  empty: boolean
}

export interface TaskTimeLog {
  id: string
  taskId: string
  userId: string
  username: string
  email: string
  workDate: string
  minutes: number
  description: string | null
  canEdit: boolean
  canDelete: boolean
  createdAt: string
  updatedAt: string | null
}

export interface TaskTimeLogPage {
  content: TaskTimeLog[]
  totalElements: number
  totalPages: number
  number: number
  size: number
  numberOfElements: number
  first: boolean
  last: boolean
  empty: boolean
}

export interface TaskTimeUserSummary {
  userId: string
  username: string
  email: string
  spentMinutes: number
}

export interface TaskTimeSummary {
  taskId: string
  estimatedMinutes: number | null
  spentMinutes: number
  remainingMinutes: number
  progressPercentage: number
  overEstimated: boolean
  overEstimatedMinutes: number
  byUser: TaskTimeUserSummary[]
}

export interface TaskStatusStatistic {
  status: TaskStatus
  title: string
  count: number
  percentage: number
}

export interface TaskAssigneeStatistic {
  userId: string | null
  username: string | null
  email: string | null
  totalTasks: number
  todoTasks: number
  inProgressTasks: number
  inReviewTasks: number
  doneTasks: number
  blockedTasks: number
  cancelledTasks: number
  estimatedMinutes: number
  spentMinutes: number
  completionRate: number
  overEstimated: boolean
  overEstimatedMinutes: number
}

export interface BacklogItemTaskStatistic {
  backlogItemId: string
  backlogItemTitle: string
  totalTasks: number
  completedTasks: number
  unfinishedTasks: number
  blockedTasks: number
  estimatedMinutes: number
  spentMinutes: number
  completionRate: number
}

export interface SprintTaskStatistics {
  projectId: string
  sprintId: string
  sprintName: string
  sprintStatus: 'PLANNING' | 'ACTIVE' | 'COMPLETED' | 'CANCELLED'
  startDate: string | null
  endDate: string | null
  totalTasks: number
  completedTasks: number
  unfinishedTasks: number
  blockedTasks: number
  cancelledTasks: number
  overdueTasks: number
  completionRate: number
  estimatedMinutes: number
  spentMinutes: number
  remainingEstimatedMinutes: number
  timeUsageRate: number
  overEstimated: boolean
  overEstimatedMinutes: number
  byStatus: TaskStatusStatistic[]
  byAssignee: TaskAssigneeStatistic[]
  byBacklogItem: BacklogItemTaskStatistic[]
}

export interface BurndownPoint {
  date: string
  idealRemainingTasks: number
  actualRemainingTasks: number
  completedTasks: number
  completedOnDate: number
}

export interface SprintBurndown {
  projectId: string
  sprintId: string
  sprintName: string
  sprintStatus: 'PLANNING' | 'ACTIVE' | 'COMPLETED' | 'CANCELLED'
  startDate: string | null
  endDate: string | null
  totalTasks: number
  points: BurndownPoint[]
}

export interface TaskImportError {
  rowNumber: number
  fieldName: string | null
  rawValue: string | null
  message: string
}

export interface TaskImportResult {
  importBatchId: string
  status: 'VALIDATING' | 'VALIDATION_FAILED' | 'IMPORTING' | 'COMPLETED' | 'FAILED'
  totalRows: number
  successRows: number
  failedRows: number
  importedTaskIds: string[]
  errors: TaskImportError[]
}

export const taskStatusLabels: Record<TaskStatus, string> = {
  TODO: 'Cần làm',
  IN_PROGRESS: 'Đang làm',
  IN_REVIEW: 'Đang review',
  DONE: 'Hoàn thành',
  BLOCKED: 'Đang chờ',
  CANCELLED: 'Đã hủy',
}

export const taskPriorityLabels: Record<TaskPriority, string> = {
  LOW: 'Thấp',
  MEDIUM: 'Vừa',
  HIGH: 'Cao',
  URGENT: 'Khẩn cấp',
}

export const taskTypeLabels: Record<TaskType, string> = {
  DEVELOPMENT: 'Dev',
  TESTING: 'Test',
  DESIGN: 'Design',
  DOCUMENTATION: 'Docs',
  RESEARCH: 'Research',
  DEVOPS: 'DevOps',
  OTHER: 'Khác',
}

export interface TaskDependency {
  id: string
  taskId: string
  dependsOnTaskId: string
  dependsOnTaskTitle: string
  dependsOnTaskStatus: TaskStatus
  dependsOnTaskPriority: TaskPriority
  dependsOnTaskAssigneeUserId: string | null
  dependsOnTaskDueDate: string | null
  dependencyCompleted: boolean
  createdAt: string
}

export type TaskRiskLevel = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'

export type TaskRiskReason = 'NONE' | 'DUE_SOON' | 'OVERDUE' | 'BLOCKED' | 'DEPENDENCY_NOT_DONE' | 'BLOCKING_OTHER_TASKS' | 'OVERDUE_AND_BLOCKED'

export interface TaskRisk {
  taskId: string
  projectId: string
  sprintId: string | null
  title: string
  status: TaskStatus
  priority: TaskPriority
  assigneeUserId: string | null
  assigneeUsername: string | null
  assigneeEmail: string | null
  dueDate: string | null
  overdue: boolean
  dueSoon: boolean
  blocked: boolean
  daysUntilDue: number
  blockingTaskCount: number
  unresolvedDependencyCount: number
  riskLevel: TaskRiskLevel
  reasons: TaskRiskReason[]
  targetUrl: string | null
}

export interface TaskRiskSummary {
  projectId: string
  sprintId: string | null
  totalRiskTasks: number
  lowRiskTasks: number
  mediumRiskTasks: number
  highRiskTasks: number
  criticalRiskTasks: number
  overdueTasks: number
  dueSoonTasks: number
  blockedTasks: number
  dependencyRiskTasks: number
  topRisks: TaskRisk[]
}

export interface TaskRiskScan {
  projectId: string
  scannedTasks: number
  riskTasks: number
  highRiskTasks: number
  criticalRiskTasks: number
  notificationsCreated: number
  scannedAt: string
}

export interface MyTaskRiskSummary {
  totalRiskTasks: number
  mediumRiskTasks: number
  highRiskTasks: number
  criticalRiskTasks: number
  overdueTasks: number
  dueSoonTasks: number
  blockedTasks: number
  topRisks: TaskRisk[]
}

export interface ProjectTaskRiskPage {
  content: TaskRisk[]
  totalElements: number
  totalPages: number
  number: number
  size: number
  numberOfElements: number
  first: boolean
  last: boolean
  empty: boolean
}

export const taskRiskLevelLabels: Record<TaskRiskLevel, string> = {
  LOW: 'Thấp',
  MEDIUM: 'Vừa',
  HIGH: 'Cao',
  CRITICAL: 'Nguy kịch',
}

export const taskRiskReasonLabels: Record<TaskRiskReason, string> = {
  NONE: 'Không có rủi ro',
  DUE_SOON: 'Sắp đến hạn',
  OVERDUE: 'Đã quá hạn',
  BLOCKED: 'Đang chờ',
  DEPENDENCY_NOT_DONE: 'Phụ thuộc chưa hoàn thành',
  BLOCKING_OTHER_TASKS: 'Đang làm nghẽn task khác',
  OVERDUE_AND_BLOCKED: 'Quá hạn và Đang chờ',
}
