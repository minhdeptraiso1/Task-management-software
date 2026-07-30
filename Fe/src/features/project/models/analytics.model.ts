import type { SprintStatus } from './scrum.model'

export interface ProjectAnalyticsSummaryResponse {
  projectId: string
  projectCode: string
  projectName: string
  totalSprints: number
  completedSprints: number
  activeSprints: number
  totalBacklogItems: number
  completedBacklogItems: number
  totalTasks: number
  completedTasks: number
  blockedTasks: number
  overdueTasks: number
  estimatedMinutes: number
  spentMinutes: number
  taskCompletionRate: number
  backlogCompletionRate: number
}

export interface VelocityPointResponse {
  sprintId: string
  sprintName: string
  sprintStatus: SprintStatus
  startDate: string | null
  endDate: string | null
  committedItems: number
  completedItems: number
  committedStoryPoints: number
  completedStoryPoints: number
  completionRate: number
}

export interface VelocityChartResponse {
  projectId: string
  projectCode: string
  projectName: string
  averageCompletedItems: number
  averageCompletedStoryPoints: number
  points: VelocityPointResponse[]
}

export interface BurnupPointResponse {
  date: string
  totalScope: number
  completedScope: number
  completionRate: number
}

export interface BurnupChartResponse {
  projectId: string
  sprintId: string | null
  sprintName: string | null
  startDate: string | null
  endDate: string | null
  points: BurnupPointResponse[]
}

export interface CumulativeFlowPointResponse {
  date: string
  todo: number
  inProgress: number
  inReview: number
  blocked: number
  done: number
  cancelled: number
}

export interface CumulativeFlowResponse {
  projectId: string
  sprintId: string
  sprintName: string
  points: CumulativeFlowPointResponse[]
}
