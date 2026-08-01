import { apiRequest } from '../../../services/apiClient'
import type {
  AdminDashboardResponse,
  AdminUserResponse,
  AdminUserActivityPageResponse,
  SystemAuditLogPageResponse,
  SystemAuditLogResponse,
  AdminAuditSummaryResponse,
  AdminImportAuditPageResponse,
  DailyDigestRunResponse,
} from '../models/admin.model'
import type { UserRole } from '../models/user.model'

export async function getAdminDashboard(): Promise<AdminDashboardResponse> {
  return apiRequest<AdminDashboardResponse>('/admin/dashboard')
}

export async function enableUser(userId: string): Promise<AdminUserResponse> {
  return apiRequest<AdminUserResponse>(`/admin/users/${userId}/enable`, {
    method: 'PATCH',
  })
}

export async function disableUser(userId: string): Promise<AdminUserResponse> {
  return apiRequest<AdminUserResponse>(`/admin/users/${userId}/disable`, {
    method: 'PATCH',
  })
}

export async function updateUserRole(userId: string, role: UserRole): Promise<AdminUserResponse> {
  return apiRequest<AdminUserResponse>(`/admin/users/${userId}/role`, {
    method: 'PATCH',
    body: JSON.stringify({ role }),
  })
}

export async function getUserActivities(userId: string, page = 0, size = 20): Promise<AdminUserActivityPageResponse> {
  return apiRequest<AdminUserActivityPageResponse>(`/admin/users/${userId}/activities?page=${page}&size=${size}`)
}

export interface SystemAuditSearchRequest {
  actorUserId?: string
  action?: string
  resourceType?: string
  resourceId?: string
  success?: boolean
  fromDate?: string
  toDate?: string
  keyword?: string
  page?: number
  size?: number
}

export async function searchSystemAuditLogs(params: SystemAuditSearchRequest = {}): Promise<SystemAuditLogPageResponse> {
  const queryParams = new URLSearchParams()
  if (params.actorUserId) queryParams.set('actorUserId', params.actorUserId)
  if (params.action) queryParams.set('action', params.action)
  if (params.resourceType) queryParams.set('resourceType', params.resourceType)
  if (params.resourceId) queryParams.set('resourceId', params.resourceId)
  if (params.success !== undefined) queryParams.set('success', String(params.success))
  if (params.fromDate) queryParams.set('fromDate', params.fromDate)
  if (params.toDate) queryParams.set('toDate', params.toDate)
  if (params.keyword) queryParams.set('keyword', params.keyword)
  queryParams.set('page', String(params.page ?? 0))
  queryParams.set('size', String(params.size ?? 12))
  queryParams.set('sort', 'createdAt,desc')

  return apiRequest<SystemAuditLogPageResponse>(`/admin/audit-logs?${queryParams.toString()}`)
}

export async function getSystemAuditLogById(auditLogId: string): Promise<SystemAuditLogResponse> {
  return apiRequest<SystemAuditLogResponse>(`/admin/audit-logs/${auditLogId}`)
}

export async function getAuditSummary(params: SystemAuditSearchRequest = {}): Promise<AdminAuditSummaryResponse> {
  const queryParams = new URLSearchParams()
  if (params.keyword) queryParams.set('keyword', params.keyword)
  if (params.fromDate) queryParams.set('fromDate', params.fromDate)
  if (params.toDate) queryParams.set('toDate', params.toDate)
  return apiRequest<AdminAuditSummaryResponse>(`/admin/audit-logs/summary?${queryParams.toString()}`)
}

export async function getImportAudits(page = 0, size = 15, keyword = ''): Promise<AdminImportAuditPageResponse> {
  const queryParams = new URLSearchParams({ page: String(page), size: String(size), sort: 'createdAt,desc' })
  if (keyword) queryParams.set('keyword', keyword)
  return apiRequest<AdminImportAuditPageResponse>(`/admin/import-audits?${queryParams.toString()}`)
}

export async function getFileAudits(page = 0, size = 15, keyword = ''): Promise<SystemAuditLogPageResponse> {
  const queryParams = new URLSearchParams({ page: String(page), size: String(size), sort: 'createdAt,desc' })
  if (keyword) queryParams.set('keyword', keyword)
  return apiRequest<SystemAuditLogPageResponse>(`/admin/file-audits?${queryParams.toString()}`)
}

export async function runTaskDueReminders(): Promise<void> {
  return apiRequest<void>('/admin/schedulers/task-due-reminders/run', {
    method: 'POST',
  })
}

export async function runDailyDigest(businessDate?: string): Promise<DailyDigestRunResponse> {
  const queryParams = new URLSearchParams()
  if (businessDate) queryParams.set('businessDate', businessDate)
  const url = queryParams.toString() ? `/admin/schedulers/daily-digest/run?${queryParams.toString()}` : '/admin/schedulers/daily-digest/run'
  return apiRequest<DailyDigestRunResponse>(url, {
    method: 'POST',
  })
}
