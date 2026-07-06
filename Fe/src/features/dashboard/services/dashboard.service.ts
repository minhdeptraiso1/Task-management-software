import { apiRequest } from '../../../services/apiClient'
import { endpoints, projectPath } from '../../../services/endpoints'
import type {
  MyDashboardResponse,
  MyTaskPageResponse,
  MyTimeSummaryResponse,
  ProjectDashboardActivityResponse,
  ProjectDashboardMemberWorkloadResponse,
  ProjectDashboardResponse
} from '../models/dashboard.model'

export const getMyDashboard = () =>
  apiRequest<MyDashboardResponse>(`${endpoints.dashboard}/me`)

export function getMyTasks(page = 0, size = 10) {
  return apiRequest<MyTaskPageResponse>(`${endpoints.dashboard}/me/tasks?page=${page}&size=${size}`)
}

export const getMyTimeSummary = () =>
  apiRequest<MyTimeSummaryResponse>(`${endpoints.dashboard}/me/time-summary`)

export const getProjectDashboard = (projectId: string) =>
  apiRequest<ProjectDashboardResponse>(`${projectPath(projectId)}/dashboard`)

export const getProjectWorkload = (projectId: string) =>
  apiRequest<ProjectDashboardMemberWorkloadResponse[]>(`${projectPath(projectId)}/dashboard/workload`)

export const getProjectRecentActivities = (projectId: string, limit = 10) =>
  apiRequest<ProjectDashboardActivityResponse[]>(`${projectPath(projectId)}/dashboard/recent-activities?limit=${limit}`)
