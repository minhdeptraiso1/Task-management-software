export type AttachmentEntityType = 'TASK' | 'COMMENT' | 'BUG' | 'BUG_COMMENT' | 'BUG_EVIDENCE'

export interface Attachment {
  id: string
  projectId: string
  entityType: AttachmentEntityType
  entityId: string
  uploadedByUserId: string
  uploadedByUsername: string | null
  uploadedByEmail: string | null
  originalFileName: string
  contentType: string
  extension: string
  sizeBytes: number
  downloadUrl: string
  canDelete: boolean
  createdAt: string
}

export interface AttachmentPage {
  content: Attachment[]
  totalElements: number
  totalPages: number
  number: number
  size: number
  numberOfElements: number
  first: boolean
  last: boolean
  empty: boolean
}

export interface AttachmentUsage {
  projectId: string
  usedBytes: number
  maxBytes: number
  remainingBytes: number
  usageRate: number
}

export interface FileSecuritySummary {
  maxFileSizeBytes: number
  maxFilesPerEntity: number
  maxProjectStorageBytes: number
  keepDeletedFileDays: number
  allowedExtensions: string[]
  blockedExtensions: string[]
}

export interface FileCleanupResult {
  scannedFiles: number
  deletedFiles: number
  failedFiles: number
}
