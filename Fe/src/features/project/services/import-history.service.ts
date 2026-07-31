import { apiRequest } from '../../../services/apiClient'
import type {
  TaskImportBatchPageResponse,
  TaskImportBatchDetailResponse,
  TaskImportErrorPageResponse,
  TaskImportHistorySearchRequest,
} from '../models/import-history.model'

export async function getImportHistory(
  projectId: string,
  searchParams: TaskImportHistorySearchRequest = {}
): Promise<TaskImportBatchPageResponse> {
  const queryParams = new URLSearchParams()
  if (searchParams.status) queryParams.set('status', searchParams.status)
  if (searchParams.sprintId) queryParams.set('sprintId', searchParams.sprintId)
  if (searchParams.importedByUserId) queryParams.set('importedByUserId', searchParams.importedByUserId)
  if (searchParams.fromDate) queryParams.set('fromDate', searchParams.fromDate)
  if (searchParams.toDate) queryParams.set('toDate', searchParams.toDate)
  queryParams.set('page', String(searchParams.page ?? 0))
  queryParams.set('size', String(searchParams.size ?? 15))
  queryParams.set('sort', 'createdAt,desc')

  return apiRequest<TaskImportBatchPageResponse>(`/projects/${projectId}/task-imports?${queryParams.toString()}`)
}

export async function getImportBatchDetail(
  projectId: string,
  batchId: string
): Promise<TaskImportBatchDetailResponse> {
  return apiRequest<TaskImportBatchDetailResponse>(`/projects/${projectId}/task-imports/${batchId}`)
}

export async function getImportErrors(
  projectId: string,
  batchId: string,
  page = 0,
  size = 50
): Promise<TaskImportErrorPageResponse> {
  return apiRequest<TaskImportErrorPageResponse>(
    `/projects/${projectId}/task-imports/${batchId}/errors?page=${page}&size=${size}`
  )
}
