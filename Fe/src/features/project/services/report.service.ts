import { apiRequest, downloadExcelFile, downloadPdfFile } from '../../../services/apiClient'
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

export interface ProjectExcelReportFilters {
  fromDate?: string
  toDate?: string
  sprintId?: string
  userId?: string
}

export function exportSprintExcelReport(projectId: string, sprintId: string) {
  return downloadExcelFile(`${projectPath(projectId)}/sprints/${sprintId}/reports/excel`, `sprint-report.xlsx`)
}

export function exportProjectExcelReport(projectId: string, filters?: ProjectExcelReportFilters) {
  const params = new URLSearchParams()
  if (filters?.fromDate) params.set('fromDate', filters.fromDate)
  if (filters?.toDate) params.set('toDate', filters.toDate)
  if (filters?.sprintId) params.set('sprintId', filters.sprintId)
  if (filters?.userId) params.set('userId', filters.userId)

  const queryString = params.toString() ? `?${params.toString()}` : ''
  return downloadExcelFile(`${projectPath(projectId)}/reports/excel${queryString}`, `project-report.xlsx`)
}

export function exportSprintPdfReport(projectId: string, sprintId: string) {
  return downloadPdfFile(`${projectPath(projectId)}/sprints/${sprintId}/reports/pdf`, `sprint-report.pdf`)
}

export function exportProjectPdfReport(projectId: string, filters?: ProjectExcelReportFilters) {
  const params = new URLSearchParams()
  if (filters?.fromDate) params.set('fromDate', filters.fromDate)
  if (filters?.toDate) params.set('toDate', filters.toDate)
  if (filters?.sprintId) params.set('sprintId', filters.sprintId)
  if (filters?.userId) params.set('userId', filters.userId)

  const queryString = params.toString() ? `?${params.toString()}` : ''
  return downloadPdfFile(`${projectPath(projectId)}/reports/pdf${queryString}`, `project-report.pdf`)
}
