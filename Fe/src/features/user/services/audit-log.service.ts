import { apiRequest } from '../../../services/apiClient'
import { endpoints } from '../../../services/endpoints'
import type { AuditLogPage } from '../models/audit-log.model'

export function getAuditLogs(page = 0, size = 12) {
  const params = new URLSearchParams({
    page: String(page),
    size: String(size),
    sort: 'createdAt,desc',
  })

  return apiRequest<AuditLogPage>(`${endpoints.auditLogs}?${params}`)
}
