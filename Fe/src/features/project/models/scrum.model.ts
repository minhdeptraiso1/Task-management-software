export type BacklogItemStatus = 'DRAFT' | 'READY' | 'IN_SPRINT' | 'DONE' | 'CANCELLED'
export type BacklogItemType = 'EPIC' | 'USER_STORY' | 'FEATURE' | 'TECHNICAL'
export type BacklogPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT'
export type SprintStatus = 'PLANNING' | 'ACTIVE' | 'COMPLETED' | 'CANCELLED'

export interface BacklogItem {
  id: string
  projectId: string
  sprintId: string | null
  title: string
  description: string | null
  type: BacklogItemType
  status: BacklogItemStatus
  priority: BacklogPriority
  storyPoints: number | null
  position: number
  createdByUserId: string
  createdAt: string
  updatedAt: string | null
}

export interface BacklogItemPage {
  content: BacklogItem[]
  totalElements: number
  totalPages: number
  number: number
  size: number
  numberOfElements: number
  first: boolean
  last: boolean
  empty: boolean
}

export interface Sprint {
  id: string
  projectId: string
  name: string
  goal: string | null
  status: SprintStatus
  startDate: string | null
  endDate: string | null
  startedAt: string | null
  completedAt: string | null
  createdByUserId: string
  backlogItemCount: number
  taskCount?: number
  completedTaskCount?: number
  completionRate?: number
  createdAt: string
  updatedAt: string | null
}

export interface SprintPage {
  content: Sprint[]
  totalElements: number
  totalPages: number
  number: number
  size: number
  numberOfElements: number
  first: boolean
  last: boolean
  empty: boolean
}

export const backlogStatusLabels: Record<BacklogItemStatus, string> = {
  DRAFT: 'Nháp',
  READY: 'Sẵn sàng',
  IN_SPRINT: 'Trong sprint',
  DONE: 'Hoàn thành',
  CANCELLED: 'Đã hủy',
}

export const backlogTypeLabels: Record<BacklogItemType, string> = {
  EPIC: 'Epic',
  USER_STORY: 'User Story',
  FEATURE: 'Feature',
  TECHNICAL: 'Technical',
}

export const backlogPriorityLabels: Record<BacklogPriority, string> = {
  LOW: 'Thấp',
  MEDIUM: 'Vừa',
  HIGH: 'Cao',
  URGENT: 'Khẩn cấp',
}

export const sprintStatusLabels: Record<SprintStatus, string> = {
  PLANNING: 'Lên kế hoạch',
  ACTIVE: 'Đang chạy',
  COMPLETED: 'Hoàn thành',
  CANCELLED: 'Đã hủy',
}

export type SprintHealthStatus = 'GOOD' | 'WARNING' | 'CRITICAL'
export type SprintRiskType =
  | 'OVER_CAPACITY'
  | 'LOW_PROGRESS'
  | 'OVERDUE_TASK'
  | 'BLOCKED_TASK'
  | 'NO_ASSIGNEE'
  | 'NO_ESTIMATE'
  | 'SPRINT_ENDING_SOON'
  | 'SPRINT_OVERDUE'

export type SprintRiskSeverity = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'

export interface SprintCapacityMemberResponse {
  userId: string
  username: string
  email: string
  systemRole: string
  projectRole: string
  capacityMinutes: number
  assignedEstimatedMinutes: number
  spentMinutes: number
  remainingCapacityMinutes: number
  utilizationRate: number
  overCapacity: boolean
  overCapacityMinutes: number
  totalTasks: number
  todoTasks: number
  inProgressTasks: number
  inReviewTasks: number
  doneTasks: number
  blockedTasks: number
  cancelledTasks: number
}

export interface SprintCapacityResponse {
  projectId: string
  sprintId: string
  sprintName: string
  sprintStatus: SprintStatus
  startDate: string | null
  endDate: string | null
  sprintDays: number
  memberCount: number
  totalCapacityMinutes: number
  totalEstimatedMinutes: number
  totalSpentMinutes: number
  remainingCapacityMinutes: number
  utilizationRate: number
  overCapacity: boolean
  overCapacityMinutes: number
  members: SprintCapacityMemberResponse[]
}

export interface SprintRiskResponse {
  type: SprintRiskType
  severity: SprintRiskSeverity
  message: string
  taskId: string | null
  taskTitle: string | null
  userId: string | null
  username: string | null
  suggestedAction: string | null
}

export interface SprintHealthResponse {
  projectId: string
  sprintId: string
  sprintName: string
  sprintStatus: SprintStatus
  healthStatus: SprintHealthStatus
  startDate: string | null
  endDate: string | null
  totalDays: number
  elapsedDays: number
  remainingDays: number
  totalTasks: number
  completedTasks: number
  unfinishedTasks: number
  blockedTasks: number
  overdueTasks: number
  noAssigneeTasks: number
  noEstimateTasks: number
  progressRate: number
  expectedProgressRate: number
  timeUsageRate: number
  overCapacity: boolean
  totalCapacityMinutes: number
  totalEstimatedMinutes: number
  totalSpentMinutes: number
  riskCount: number
  criticalRiskCount: number
  topRisks: SprintRiskResponse[]
}

export const sprintHealthLabels: Record<SprintHealthStatus, string> = {
  GOOD: 'Tốt',
  WARNING: 'Cảnh báo',
  CRITICAL: 'Nguy kịch',
}

export const sprintRiskTypeLabels: Record<SprintRiskType, string> = {
  OVER_CAPACITY: 'Quá tải Capacity',
  LOW_PROGRESS: 'Tiến độ chậm',
  OVERDUE_TASK: 'Task quá hạn',
  BLOCKED_TASK: 'Task bị chặn',
  NO_ASSIGNEE: 'Task chưa giao',
  NO_ESTIMATE: 'Task chưa ước lượng',
  SPRINT_ENDING_SOON: 'Sprint sắp kết thúc',
  SPRINT_OVERDUE: 'Sprint quá hạn',
}

export const sprintRiskSeverityLabels: Record<SprintRiskSeverity, string> = {
  LOW: 'Thấp',
  MEDIUM: 'Vừa',
  HIGH: 'Cao',
  CRITICAL: 'Khẩn cấp',
}

export interface SprintReviewResponse {
  id: string
  projectId: string
  sprintId: string
  goalAchieved: boolean
  demoSummary: string | null
  stakeholderFeedback: string | null
  acceptedItemSummary: string | null
  rejectedItemSummary: string | null
  note: string | null
  createdAt: string
  updatedAt: string | null
}

export interface SprintRetroActionItemResponse {
  content: string
  assigneeUserId: string
  assigneeUsername: string | null
  assigneeEmail: string | null
  dueDate: string | null
  done: boolean
}

export interface SprintRetroActionItemRequest {
  content: string
  assigneeUserId: string
  dueDate: string | null
  done: boolean
}

export interface SprintRetrospectiveResponse {
  id: string
  projectId: string
  sprintId: string
  wentWell: string | null
  wentWrong: string | null
  improvement: string | null
  actionItems: SprintRetroActionItemResponse[]
  note: string | null
  createdAt: string
  updatedAt: string | null
}

export interface SprintClosingReportResponse {
  projectId: string
  sprintId: string
  sprintName: string
  sprintGoal: string | null
  sprintStatus: SprintStatus
  startDate: string | null
  endDate: string | null
  startedAt: string | null
  completedAt: string | null
  backlogItemCount: number
  completedBacklogItemCount: number
  unfinishedBacklogItemCount: number
  totalStoryPoints: number
  taskStatistics: any
  burndown: any
  review: SprintReviewResponse | null
  retrospective: SprintRetrospectiveResponse | null
  generatedAt: string
}

export interface SprintProgressDaily {
  date: string
  completedOnDate: number
  cumulativeCompletedTasks: number
  remainingTasks: number
  idealRemainingTasks: number
}

export interface SprintProgress {
  projectId: string
  sprintId: string
  sprintName: string
  sprintStatus: SprintStatus
  startDate: string | null
  endDate: string | null
  today: string | null
  totalDays: number
  elapsedDays: number
  daysRemaining: number
  totalTasks: number
  completedTasks: number
  unfinishedTasks: number
  blockedTasks: number
  overdueTasks: number
  actualCompletionRate: number
  expectedProgressRate: number
  progressGap: number
  behindSchedule: boolean
  endingSoon: boolean
  overdueSprint: boolean
  dailyProgress: SprintProgressDaily[]
}

export interface SprintReminderResponse {
  projectId: string
  sprintId: string
  reminderType: string
  targetItemCount: number
  recipientCount: number
  recipientUserIds: string[]
  sentAt: string
}
