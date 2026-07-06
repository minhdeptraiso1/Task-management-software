import { apiRequest } from '../../../services/apiClient'
import { projectPath } from '../../../services/endpoints'
import type {
  ProjectMemberReportResponse,
  ProjectTimeReportResponse,
  SprintReportResponse
} from '../models/report.model'

export const getSprintReport = (projectId: string, sprintId: string) =>
  apiRequest<SprintReportResponse>(`${projectPath(projectId)}/reports/sprints/${sprintId}`)

export function getMemberReport(projectId: string, fromDate?: string, toDate?: string) {
  const params = new URLSearchParams()
  if (fromDate) params.set('fromDate', fromDate)
  if (toDate) params.set('toDate', toDate)
  return apiRequest<ProjectMemberReportResponse>(`${projectPath(projectId)}/reports/members?${params}`)
}

export function getTimeReport(projectId: string, fromDate?: string, toDate?: string, userId?: string, taskId?: string) {
  const params = new URLSearchParams()
  if (fromDate) params.set('fromDate', fromDate)
  if (toDate) params.set('toDate', toDate)
  if (userId) params.set('userId', userId)
  if (taskId) params.set('taskId', taskId)
  return apiRequest<ProjectTimeReportResponse>(`${projectPath(projectId)}/reports/time?${params}`)
}
