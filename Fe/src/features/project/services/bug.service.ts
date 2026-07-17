import { apiRequest, downloadExcelFile } from '../../../services/apiClient'
import type { Bug, BugPage, BugSummary, BugSearchFilters, BugComment, BugEvidence, BugDashboard, BugReport, QaMetrics, BugReportFilters, BugAttachment } from '../models/bug.model'

function buildQueryString(params: BugSearchFilters, page?: number, size?: number): string {
  const query = new URLSearchParams()
  if (params.keyword) query.append('keyword', params.keyword)
  if (params.status) query.append('status', params.status)
  if (params.severity) query.append('severity', params.severity)
  if (params.priority) query.append('priority', params.priority)
  if (params.assigneeUserId) query.append('assigneeUserId', params.assigneeUserId)
  if (params.reporterUserId) query.append('reporterUserId', params.reporterUserId)
  if (params.taskId) query.append('taskId', params.taskId)
  if (params.backlogItemId) query.append('backlogItemId', params.backlogItemId)
  if (page !== undefined) query.append('page', String(page))
  if (size !== undefined) query.append('size', String(size))
  query.append('sort', 'createdAt,desc')
  const str = query.toString()
  return str ? `?${str}` : ''
}

export const createBug = (projectId: string, data: Partial<Bug>) =>
  apiRequest<Bug>(`/projects/${projectId}/bugs`, {
    method: 'POST',
    body: JSON.stringify(data)
  })

export const searchBugs = (projectId: string, params: BugSearchFilters, page?: number, size?: number) =>
  apiRequest<BugPage>(`/projects/${projectId}/bugs${buildQueryString(params, page, size)}`)

export const getBugById = (projectId: string, bugId: string) =>
  apiRequest<Bug>(`/projects/${projectId}/bugs/${bugId}`)

export const updateBug = (projectId: string, bugId: string, data: Partial<Bug>) =>
  apiRequest<Bug>(`/projects/${projectId}/bugs/${bugId}`, {
    method: 'PATCH',
    body: JSON.stringify(data)
  })

export const assignBug = (projectId: string, bugId: string, assigneeUserId: string | null) =>
  apiRequest<Bug>(`/projects/${projectId}/bugs/${bugId}/assign`, {
    method: 'PATCH',
    body: JSON.stringify({ assigneeUserId })
  })

export const updateBugStatus = (projectId: string, bugId: string, status: string) =>
  apiRequest<Bug>(`/projects/${projectId}/bugs/${bugId}/status`, {
    method: 'PATCH',
    body: JSON.stringify({ status })
  })

export const unassignBug = (projectId: string, bugId: string) =>
  apiRequest<Bug>(`/projects/${projectId}/bugs/${bugId}/unassign`, {
    method: 'PATCH'
  })

export const updateBugSeverity = (projectId: string, bugId: string, severity: string) =>
  apiRequest<Bug>(`/projects/${projectId}/bugs/${bugId}/severity`, {
    method: 'PATCH',
    body: JSON.stringify({ severity })
  })

export const updateBugPriority = (projectId: string, bugId: string, priority: string) =>
  apiRequest<Bug>(`/projects/${projectId}/bugs/${bugId}/priority`, {
    method: 'PATCH',
    body: JSON.stringify({ priority })
  })

export const deleteBug = (projectId: string, bugId: string) =>
  apiRequest<void>(`/projects/${projectId}/bugs/${bugId}`, {
    method: 'DELETE'
  })

export const getBugSummary = (projectId: string) =>
  apiRequest<BugSummary>(`/projects/${projectId}/bugs/summary`)

export const getBugComments = (projectId: string, bugId: string) =>
  apiRequest<BugComment[]>(`/projects/${projectId}/bugs/${bugId}/comments`)

export const createBugComment = (projectId: string, bugId: string, data: { content: string; parentId?: string }) =>
  apiRequest<BugComment>(`/projects/${projectId}/bugs/${bugId}/comments`, {
    method: 'POST',
    body: JSON.stringify(data)
  })

export const updateBugComment = (projectId: string, bugId: string, commentId: string, content: string) =>
  apiRequest<BugComment>(`/projects/${projectId}/bugs/${bugId}/comments/${commentId}`, {
    method: 'PATCH',
    body: JSON.stringify({ content })
  })

export const deleteBugComment = (projectId: string, bugId: string, commentId: string) =>
  apiRequest<void>(`/projects/${projectId}/bugs/${bugId}/comments/${commentId}`, {
    method: 'DELETE'
  })

export const getBugEvidences = (projectId: string, bugId: string) =>
  apiRequest<BugEvidence[]>(`/projects/${projectId}/bugs/${bugId}/evidences`)

export const createBugEvidence = (projectId: string, bugId: string, data: Partial<BugEvidence>) =>
  apiRequest<BugEvidence>(`/projects/${projectId}/bugs/${bugId}/evidences`, {
    method: 'POST',
    body: JSON.stringify(data)
  })

export const updateBugEvidence = (projectId: string, bugId: string, evidenceId: string, data: Partial<BugEvidence>) =>
  apiRequest<BugEvidence>(`/projects/${projectId}/bugs/${bugId}/evidences/${evidenceId}`, {
    method: 'PATCH',
    body: JSON.stringify(data)
  })

export const deleteBugEvidence = (projectId: string, bugId: string, evidenceId: string) =>
  apiRequest<void>(`/projects/${projectId}/bugs/${bugId}/evidences/${evidenceId}`, {
    method: 'DELETE'
  })

function buildReportQueryString(params?: BugReportFilters): string {
  if (!params) return ''
  const query = new URLSearchParams()
  if (params.fromDate) query.append('fromDate', params.fromDate)
  if (params.toDate) query.append('toDate', params.toDate)
  if (params.sprintId) query.append('sprintId', params.sprintId)
  if (params.assigneeUserId) query.append('assigneeUserId', params.assigneeUserId)
  if (params.status) query.append('status', params.status)
  if (params.severity) query.append('severity', params.severity)
  if (params.priority) query.append('priority', params.priority)
  if (params.overdueOnly !== undefined) query.append('overdueOnly', String(params.overdueOnly))
  const str = query.toString()
  return str ? `?${str}` : ''
}

export const getBugDashboard = (projectId: string) =>
  apiRequest<BugDashboard>(`/projects/${projectId}/bugs/dashboard`)

export const getBugReport = (projectId: string, filters?: BugReportFilters) =>
  apiRequest<BugReport>(`/projects/${projectId}/bugs/report${buildReportQueryString(filters)}`)

export const getSprintBugReport = (projectId: string, sprintId: string, filters?: BugReportFilters) =>
  apiRequest<BugReport>(`/projects/${projectId}/sprints/${sprintId}/bugs/report${buildReportQueryString(filters)}`)

export const getQaMetrics = (projectId: string, filters?: BugReportFilters) =>
  apiRequest<QaMetrics>(`/projects/${projectId}/bugs/metrics/qa${buildReportQueryString(filters)}`)

export const exportBugReportExcel = (projectId: string, filters?: BugReportFilters) =>
  downloadExcelFile(`/projects/${projectId}/bugs/reports/excel${buildReportQueryString(filters)}`, 'bug-report.xlsx')

export const getBugAttachments = (projectId: string, bugId: string) =>
  apiRequest<BugAttachment[]>(`/projects/${projectId}/bugs/${bugId}/attachments`)

export const uploadBugAttachment = (projectId: string, bugId: string, file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  return apiRequest<BugAttachment>(`/projects/${projectId}/bugs/${bugId}/attachments`, {
    method: 'POST',
    body: formData
  })
}

export const deleteBugAttachment = (projectId: string, bugId: string, attachmentId: string) =>
  apiRequest<void>(`/projects/${projectId}/bugs/${bugId}/attachments/${attachmentId}`, {
    method: 'DELETE'
  })
