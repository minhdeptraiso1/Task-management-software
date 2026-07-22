import { apiRequest } from '../../../services/apiClient'
import type {
  Attachment,
  AttachmentPage,
  AttachmentEntityType,
  AttachmentUsage,
  FileSecuritySummary,
  FileCleanupResult
} from '../models/attachment.model'

export const getAttachments = (
  projectId: string,
  entityType: AttachmentEntityType,
  entityId: string,
  page = 0,
  size = 20
) =>
  apiRequest<AttachmentPage>(
    `/projects/${projectId}/attachments/${entityType}/${entityId}?page=${page}&size=${size}`
  )

export const uploadAttachment = (
  projectId: string,
  entityType: AttachmentEntityType,
  entityId: string,
  file: File
) => {
  const formData = new FormData()
  formData.append('file', file)
  return apiRequest<Attachment>(
    `/projects/${projectId}/attachments/${entityType}/${entityId}`,
    {
      method: 'POST',
      body: formData
    }
  )
}

export const deleteAttachment = (projectId: string, attachmentId: string) =>
  apiRequest<void>(`/projects/${projectId}/attachments/${attachmentId}`, {
    method: 'DELETE'
  })

export const getProjectAttachmentUsage = (projectId: string) =>
  apiRequest<AttachmentUsage>(`/projects/${projectId}/attachments/usage`)

export const getAllProjectAttachments = (projectId: string, page = 0, size = 10) =>
  apiRequest<AttachmentPage>(`/projects/${projectId}/attachments?page=${page}&size=${size}`)

export const getFileSecuritySummary = (projectId: string) =>
  apiRequest<FileSecuritySummary>(`/projects/${projectId}/attachments/security-summary`)

export const cleanupDeletedFiles = (limit = 100) =>
  apiRequest<FileCleanupResult>(`/admin/files/cleanup/deleted?limit=${limit}`, {
    method: 'POST'
  })

export const cleanupOrphanFiles = (limit = 100) =>
  apiRequest<FileCleanupResult>(`/admin/files/cleanup/orphans?limit=${limit}`, {
    method: 'POST'
  })
