import type { UserRole } from '../../user/models/user.model'
import type { ProjectMemberRole, ProjectStatus } from '../../project/models/project.model'
import type { TaskPriority, TaskStatus } from '../../project/models/task.model'
import type { SprintStatus } from '../../project/models/scrum.model'

export interface MyTaskSummaryResponse {
  totalTasks: number
  todoTasks: number
  inProgressTasks: number
  inReviewTasks: number
  blockedTasks: number
  doneTasks: number
  cancelledTasks: number
  overdueTasks: number
  dueSoonTasks: number
}

export interface MyTimeSummaryResponse {
  currentDate: string
  weekStartDate: string
  weekEndDate: string
  monthStartDate: string
  monthEndDate: string
  todayMinutes: number
  weekMinutes: number
  monthMinutes: number
}

export interface MyUpcomingTaskResponse {
  taskId: string
  projectId: string
  projectCode: string
  projectName: string
  backlogItemId: string | null
  currentSprintId: string | null
  title: string
  status: TaskStatus
  priority: TaskPriority
  startDate: string | null
  dueDate: string | null
  overdue: boolean
  dueSoon: boolean
  daysUntilDue: number
  targetUrl: string
}

export interface MyTaskPageResponse {
  content: MyUpcomingTaskResponse[]
  totalElements: number
  totalPages: number
  number: number
  size: number
  numberOfElements: number
  first: boolean
  last: boolean
  empty: boolean
}

export interface MyDashboardResponse {
  userId: string
  username: string
  email: string
  systemRole: UserRole
  adminDashboard: boolean
  projectCount: number
  unreadNotifications: number
  taskSummary: MyTaskSummaryResponse
  timeSummary: MyTimeSummaryResponse
  overdueTasks: MyUpcomingTaskResponse[]
  upcomingTasks: MyUpcomingTaskResponse[]
}

// ================= PROJECT DASHBOARD =================

export interface ProjectDashboardProjectResponse {
  projectId: string
  code: string
  name: string
  description: string
  status: ProjectStatus
  startDate: string | null
  endDate: string | null
  memberCount: number
}

export interface ProjectDashboardSprintResponse {
  sprintId: string
  name: string
  goal: string
  status: SprintStatus
  startDate: string | null
  endDate: string | null
  startedAt: string | null
  completedAt: string | null
  backlogItemCount: number
  taskCount: number
  completedTaskCount: number
  completionRate: number
}

export interface ProjectDashboardBacklogSummaryResponse {
  totalItems: number
  productBacklogItems: number
  inSprintItems: number
  draftItems: number
  readyItems: number
  doneItems: number
  cancelledItems: number
  totalStoryPoints: number
}

export interface ProjectDashboardTaskStatusResponse {
  status: TaskStatus
  title: string
  count: number
  percentage: number
}

export interface ProjectDashboardTaskSummaryResponse {
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
  byStatus: ProjectDashboardTaskStatusResponse[]
}

export interface ProjectDashboardMemberWorkloadResponse {
  userId: string
  username: string
  email: string
  systemRole: UserRole
  projectRole: ProjectMemberRole
  totalTasks: number
  activeTasks: number
  todoTasks: number
  inProgressTasks: number
  inReviewTasks: number
  doneTasks: number
  blockedTasks: number
  cancelledTasks: number
  overdueTasks: number
  estimatedMinutes: number
  spentMinutes: number
  completionRate: number
  overEstimated: boolean
  overEstimatedMinutes: number
}

export interface ProjectDashboardActivityResponse {
  activityId: string
  entityType: string
  entityId: string
  action: string
  performedByUserId: string | null
  performedByUsername: string
  displayMessage: string
  createdAt: string
}

export interface ProjectDashboardResponse {
  project: ProjectDashboardProjectResponse
  currentSprint: ProjectDashboardSprintResponse | null
  backlogSummary: ProjectDashboardBacklogSummaryResponse
  taskSummary: ProjectDashboardTaskSummaryResponse
  workload: ProjectDashboardMemberWorkloadResponse[]
  recentActivities: ProjectDashboardActivityResponse[]
}
