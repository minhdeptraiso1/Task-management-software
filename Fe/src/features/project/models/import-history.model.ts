export type TaskImportStatus = 'EXECUTED' | 'VALIDATION_FAILED' | 'COMPLETED' | 'FAILED'

export interface TaskImportBatchResponse {
  id: string
  projectId: string
  sprintId: string | null
  sprintName: string | null
  fileName: string
  status: TaskImportStatus
  totalRows: number | null
  successRows: number | null
  failedRows: number | null
  importedByUserId: string
  importedByUsername: string
  importedByEmail: string | null
  startedAt: string | null
  completedAt: string | null
  createdAt: string
}

export interface TaskImportErrorResponse {
  id: string
  batchId: string
  rowNumber: number
  columnName: string | null
  rawValue: string | null
  errorMessage: string
  createdAt: string
}

export interface TaskImportBatchDetailResponse {
  batch: TaskImportBatchResponse
  recentErrors: TaskImportErrorResponse[]
}

export interface TaskImportBatchPageResponse {
  content: TaskImportBatchResponse[]
  totalElements: number
  totalPages: number
  number: number
  size: number
  numberOfElements: number
  first: boolean
  last: boolean
  empty: boolean
}

export interface TaskImportErrorPageResponse {
  content: TaskImportErrorResponse[]
  totalElements: number
  totalPages: number
  number: number
  size: number
  numberOfElements: number
  first: boolean
  last: boolean
  empty: boolean
}

export interface TaskImportHistorySearchRequest {
  status?: TaskImportStatus | ''
  sprintId?: string
  importedByUserId?: string
  fromDate?: string
  toDate?: string
  page?: number
  size?: number
}
