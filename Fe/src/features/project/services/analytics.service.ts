import { apiRequest } from '../../../services/apiClient'
import { projectPath } from '../../../services/endpoints'
import type {
  ProjectAnalyticsSummaryResponse,
  VelocityChartResponse,
  BurnupChartResponse,
  CumulativeFlowResponse
} from '../models/analytics.model'

export const getProjectAnalyticsSummary = (projectId: string) =>
  apiRequest<ProjectAnalyticsSummaryResponse>(`${projectPath(projectId)}/analytics/summary`)

export const getProjectVelocity = (projectId: string) =>
  apiRequest<VelocityChartResponse>(`${projectPath(projectId)}/analytics/velocity`)

export const getProjectBurnup = (projectId: string) =>
  apiRequest<BurnupChartResponse>(`${projectPath(projectId)}/analytics/burnup`)

export const getSprintBurnup = (projectId: string, sprintId: string) =>
  apiRequest<BurnupChartResponse>(`${projectPath(projectId)}/sprints/${sprintId}/analytics/burnup`)

export const getSprintCumulativeFlow = (projectId: string, sprintId: string) =>
  apiRequest<CumulativeFlowResponse>(`${projectPath(projectId)}/sprints/${sprintId}/analytics/cumulative-flow`)
