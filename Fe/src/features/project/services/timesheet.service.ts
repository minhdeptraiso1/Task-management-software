import { apiRequest } from '../../../services/apiClient'
import type { TimesheetPage, TimesheetSummary } from '../models/timesheet.model'

export interface TimesheetSearchParams {
  fromDate?: string
  toDate?: string
  userId?: string
  taskId?: string
  page?: number
  size?: number
}

function buildQueryString(params: TimesheetSearchParams): string {
  const query = new URLSearchParams()
  if (params.fromDate) query.append('fromDate', params.fromDate)
  if (params.toDate) query.append('toDate', params.toDate)
  if (params.userId) query.append('userId', params.userId)
  if (params.taskId) query.append('taskId', params.taskId)
  if (params.page !== undefined) query.append('page', String(params.page))
  if (params.size !== undefined) query.append('size', String(params.size))
  query.append('sort', 'workDate,desc')
  const str = query.toString()
  return str ? `?${str}` : ''
}

export const getMyTimesheet = (params: TimesheetSearchParams) =>
  apiRequest<TimesheetPage>(`/timesheets/me${buildQueryString(params)}`)

export const getMyTimesheetSummary = (params: TimesheetSearchParams) =>
  apiRequest<TimesheetSummary>(`/timesheets/me/summary${buildQueryString(params)}`)

export const getProjectTimesheet = (projectId: string, params: TimesheetSearchParams) =>
  apiRequest<TimesheetPage>(`/projects/${projectId}/timesheets${buildQueryString(params)}`)

export const getProjectTimesheetSummary = (projectId: string, params: TimesheetSearchParams) =>
  apiRequest<TimesheetSummary>(`/projects/${projectId}/timesheets/summary${buildQueryString(params)}`)
