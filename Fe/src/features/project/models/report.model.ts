import type { ProjectMemberRole } from './project.model'
import type { UserRole } from '../../user/models/user.model'
import type { SprintStatus } from './scrum.model'
import type { TaskStatus } from './task.model'

// ================= PROJECT MEMBER REPORT =================

export interface ProjectMemberReportItemResponse {
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
  timeUsageRate: number
  overEstimated: boolean
  overEstimatedMinutes: number
  firstWorkDate: string | null
  lastWorkDate: string | null
}

export interface ProjectMemberReportResponse {
  projectId: string
  projectCode: string
  projectName: string
  fromDate: string | null
  toDate: string | null
  totalMembers: number
  membersWithTasks: number
  membersWithTimeLogs: number
  totalAssignedTasks: number
  totalEstimatedMinutes: number
  totalSpentMinutes: number
  members: ProjectMemberReportItemResponse[]
  generatedAt: string
}

// ================= SPRINT REPORT =================

export interface SprintBurndownPoint {
  date: string
  idealRemainingTasks: number
  actualRemainingTasks: number
  completedTasks: number
  completedOnDate: number
}

export interface SprintBurndownResponse {
  sprintId: string
  sprintName: string
  sprintStatus: SprintStatus
  startDate: string
  endDate: string
  totalTasks: number
  points: SprintBurndownPoint[]
}

export interface SprintTaskStatisticsResponse {
  projectId: string
  sprintId: string
  sprintName: string
  sprintStatus: SprintStatus
  startDate: string
  endDate: string
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
}

export interface SprintReportResponse {
  projectId: string
  sprintId: string
  sprintName: string
  sprintGoal: string
  sprintStatus: SprintStatus
  startDate: string | null
  endDate: string | null
  startedAt: string | null
  completedAt: string | null
  backlogItemCount: number
  totalStoryPoints: number
  statistics: SprintTaskStatisticsResponse
  burndown: SprintBurndownResponse
  generatedAt: string
}

// ================= TIME REPORT =================

export interface ProjectTimeDailyResponse {
  workDate: string
  spentMinutes: number
  logCount: number
}

export interface ProjectTimeMemberResponse {
  userId: string
  username: string
  email: string
  spentMinutes: number
  logCount: number
  taskCount: number
  percentage: number
}

export interface ProjectTimeTaskResponse {
  taskId: string
  taskTitle: string
  taskStatus: TaskStatus
  assigneeUserId: string | null
  estimatedMinutes: number | null
  spentMinutes: number
  logCount: number
  contributorCount: number
  timeUsageRate: number
  overEstimated: boolean
  overEstimatedMinutes: number
  targetUrl: string
}

export interface ProjectTimeReportResponse {
  projectId: string
  projectCode: string
  projectName: string
  fromDate: string | null
  toDate: string | null
  filteredUserId: string | null
  filteredTaskId: string | null
  totalMinutes: number
  totalLogs: number
  taskCount: number
  contributorCount: number
  totalEstimatedMinutes: number
  remainingEstimatedMinutes: number
  timeUsageRate: number
  overEstimated: boolean
  overEstimatedMinutes: number
  byDate: ProjectTimeDailyResponse[]
  byMember: ProjectTimeMemberResponse[]
  byTask: ProjectTimeTaskResponse[]
  generatedAt: string
}
