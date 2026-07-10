import { ApiRequestError, apiFetch, apiRequest } from '../../../services/apiClient'
import { endpoints } from '../../../services/endpoints'
import type {
  KanbanBoard,
  SprintBurndown,
  SprintTaskStatistics,
  Task,
  TaskComment,
  TaskCommentPage,
  TaskCommentReply,
  TaskImportResult,
  TaskPage,
  TaskPriority,
  TaskStatus,
  TaskTimeLog,
  TaskTimeLogPage,
  TaskTimeSummary,
  TaskType,
  TaskDependency,
  TaskRisk,
  TaskRiskSummary,
  TaskRiskScan,
} from '../models/task.model'

const projectPath = (projectId: string) => `${endpoints.projects}/${projectId}`
const taskPath = (projectId: string, taskId: string) => `${projectPath(projectId)}/tasks/${taskId}`

export interface TaskSearchOptions {
  keyword?: string
  backlogItemId?: string
  sprintId?: string
  assigneeUserId?: string
  status?: TaskStatus
  priority?: TaskPriority
  type?: TaskType
  unassignedOnly?: boolean
  page?: number
  size?: number
  sort?: string
}

function query(options: Record<string, string | number | boolean | undefined | null>) {
  const params = new URLSearchParams()
  Object.entries(options).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') params.set(key, String(value))
  })
  return params.toString()
}

function getBackendErrorMessage(body: unknown, fallback: string) {
  if (body && typeof body === 'object' && 'error' in body) {
    const error = (body as { error?: { message?: unknown } }).error
    if (typeof error?.message === 'string' && error.message.trim()) return error.message
  }
  return fallback
}

function getBackendErrorCode(body: unknown) {
  if (body && typeof body === 'object' && 'error' in body) {
    const error = (body as { error?: { code?: unknown } }).error
    if (typeof error?.code === 'number') return error.code
  }
  return undefined
}

export function searchTasks(projectId: string, options: TaskSearchOptions = {}) {
  const params = query({
    page: options.page ?? 0,
    size: options.size ?? 100,
    sort: options.sort ?? 'position,asc',
    keyword: options.keyword,
    backlogItemId: options.backlogItemId,
    sprintId: options.sprintId,
    assigneeUserId: options.assigneeUserId,
    status: options.status,
    priority: options.priority,
    type: options.type,
    unassignedOnly: options.unassignedOnly,
  })
  return apiRequest<TaskPage>(`${projectPath(projectId)}/tasks?${params}`)
}

export const getTask = (projectId: string, taskId: string) =>
  apiRequest<Task>(taskPath(projectId, taskId))

export const createTask = (projectId: string, data: {
  backlogItemId: string
  title: string
  description?: string
  type?: TaskType
  priority?: TaskPriority
  assigneeUserId?: string
  estimatedMinutes?: number
  startDate?: string
  dueDate?: string
}) => apiRequest<Task>(`${projectPath(projectId)}/tasks`, { method: 'POST', body: JSON.stringify(data) })

export const updateTask = (projectId: string, taskId: string, data: {
  title?: string
  description?: string
  type?: TaskType
  priority?: TaskPriority
  estimatedMinutes?: number
  startDate?: string
  dueDate?: string
}) => apiRequest<Task>(taskPath(projectId, taskId), { method: 'PATCH', body: JSON.stringify(data) })

export const assignTask = (projectId: string, taskId: string, assigneeUserId: string) =>
  apiRequest<Task>(`${taskPath(projectId, taskId)}/assign`, { method: 'PATCH', body: JSON.stringify({ assigneeUserId }) })

export const unassignTask = (projectId: string, taskId: string) =>
  apiRequest<Task>(`${taskPath(projectId, taskId)}/unassign`, { method: 'PATCH' })

export const deleteTask = (projectId: string, taskId: string) =>
  apiRequest<void>(taskPath(projectId, taskId), { method: 'DELETE' })

export const getSprintKanban = (projectId: string, sprintId: string) =>
  apiRequest<KanbanBoard>(`${projectPath(projectId)}/sprints/${sprintId}/kanban`)

export const updateTaskStatus = (projectId: string, taskId: string, status: TaskStatus, position?: number) =>
  apiRequest<Task>(`${taskPath(projectId, taskId)}/status`, { method: 'PATCH', body: JSON.stringify({ status, position }) })

export const blockTask = (projectId: string, taskId: string, reason: string) =>
  apiRequest<Task>(`${taskPath(projectId, taskId)}/block`, { method: 'PATCH', body: JSON.stringify({ reason }) })

export const reopenTask = (projectId: string, taskId: string, targetStatus: TaskStatus, reason?: string) =>
  apiRequest<Task>(`${taskPath(projectId, taskId)}/reopen`, { method: 'PATCH', body: JSON.stringify({ targetStatus, reason }) })

export const updateTaskPosition = (projectId: string, taskId: string, position: number) =>
  apiRequest<Task>(`${taskPath(projectId, taskId)}/position`, { method: 'PATCH', body: JSON.stringify({ position }) })

export function getTaskComments(projectId: string, taskId: string, page = 0) {
  return apiRequest<TaskCommentPage>(`${taskPath(projectId, taskId)}/comments?page=${page}&size=20&sort=createdAt,desc`)
}

export const createTaskComment = (projectId: string, taskId: string, data: { content: string; parentCommentId?: string }) =>
  apiRequest<TaskComment>(`${taskPath(projectId, taskId)}/comments`, { method: 'POST', body: JSON.stringify(data) })

export const updateTaskComment = (projectId: string, taskId: string, commentId: string, content: string) =>
  apiRequest<TaskComment>(`${taskPath(projectId, taskId)}/comments/${commentId}`, { method: 'PATCH', body: JSON.stringify({ content }) })

export const deleteTaskComment = (projectId: string, taskId: string, commentId: string) =>
  apiRequest<void>(`${taskPath(projectId, taskId)}/comments/${commentId}`, { method: 'DELETE' })

export const getTaskCommentReplies = (projectId: string, taskId: string, commentId: string) =>
  apiRequest<TaskCommentReply[]>(`${taskPath(projectId, taskId)}/comments/${commentId}/replies`)

export const createTaskCommentReply = (projectId: string, taskId: string, commentId: string, content: string) =>
  apiRequest<TaskCommentReply>(`${taskPath(projectId, taskId)}/comments/${commentId}/replies`, {
    method: 'POST',
    body: JSON.stringify({ content })
  })

export function getTaskTimeLogs(projectId: string, taskId: string, page = 0) {
  return apiRequest<TaskTimeLogPage>(`${taskPath(projectId, taskId)}/time-logs?page=${page}&size=20&sort=workDate,desc`)
}

export const createTaskTimeLog = (projectId: string, taskId: string, data: { workDate: string; minutes: number; description?: string }) =>
  apiRequest<TaskTimeLog>(`${taskPath(projectId, taskId)}/time-logs`, { method: 'POST', body: JSON.stringify(data) })

export const updateTaskTimeLog = (projectId: string, taskId: string, timeLogId: string, data: { workDate?: string; minutes?: number; description?: string }) =>
  apiRequest<TaskTimeLog>(`${taskPath(projectId, taskId)}/time-logs/${timeLogId}`, { method: 'PATCH', body: JSON.stringify(data) })

export const deleteTaskTimeLog = (projectId: string, taskId: string, timeLogId: string) =>
  apiRequest<void>(`${taskPath(projectId, taskId)}/time-logs/${timeLogId}`, { method: 'DELETE' })

export const getTaskTimeSummary = (projectId: string, taskId: string) =>
  apiRequest<TaskTimeSummary>(`${taskPath(projectId, taskId)}/time-summary`)

export const getSprintTaskStatistics = (projectId: string, sprintId: string) =>
  apiRequest<SprintTaskStatistics>(`${projectPath(projectId)}/sprints/${sprintId}/task-statistics`)

export const getSprintBurndown = (projectId: string, sprintId: string) =>
  apiRequest<SprintBurndown>(`${projectPath(projectId)}/sprints/${sprintId}/burndown`)

export async function downloadTaskExcelTemplate(projectId: string, sprintId: string) {
  const response = await apiFetch(`${projectPath(projectId)}/sprints/${sprintId}/tasks/excel-template`)
  if (!response.ok) {
    const body = await response.json().catch(() => null)
    throw new ApiRequestError(getBackendErrorMessage(body, 'Không tải được file mẫu Task'), response.status, body, getBackendErrorCode(body))
  }

  const blob = await response.blob()
  const disposition = response.headers.get('content-disposition')
  const fileName = disposition?.match(/filename\*=UTF-8''([^;]+)/)?.[1]
    ?? disposition?.match(/filename="?([^"]+)"?/)?.[1]
    ?? 'task-template.xlsx'
  return { blob, fileName: decodeURIComponent(fileName) }
}

export async function importTasksFromExcel(projectId: string, sprintId: string, file: File) {
  const formData = new FormData()
  formData.append('file', file)

  const response = await apiFetch(`${projectPath(projectId)}/sprints/${sprintId}/tasks/import`, { method: 'POST', body: formData })
  const body = await response.json().catch(() => null)
  if (!response.ok || !body?.success) {
    throw new ApiRequestError(getBackendErrorMessage(body, 'Không import được Task'), response.status, body, getBackendErrorCode(body))
  }
  return body.data as TaskImportResult
}

export const getTaskDependencies = (projectId: string, taskId: string) =>
  apiRequest<TaskDependency[]>(`${taskPath(projectId, taskId)}/dependencies`)

export const addTaskDependency = (projectId: string, taskId: string, dependsOnTaskId: string) =>
  apiRequest<TaskDependency>(`${taskPath(projectId, taskId)}/dependencies`, {
    method: 'POST',
    body: JSON.stringify({ dependsOnTaskId }),
  })

export const removeTaskDependency = (projectId: string, taskId: string, dependencyId: string) =>
  apiRequest<void>(`${taskPath(projectId, taskId)}/dependencies/${dependencyId}`, { method: 'DELETE' })

export const unblockTask = (projectId: string, taskId: string, targetStatus: TaskStatus) =>
  apiRequest<Task>(`${taskPath(projectId, taskId)}/unblock`, {
    method: 'PATCH',
    body: JSON.stringify({ targetStatus }),
  })

export const getTaskRisk = (projectId: string, taskId: string) =>
  apiRequest<TaskRisk>(`${taskPath(projectId, taskId)}/risk`)

export const getProjectTaskRisks = (projectId: string) =>
  apiRequest<TaskRisk[]>(`${projectPath(projectId)}/task-risks`)

export const getProjectRiskSummary = (projectId: string) =>
  apiRequest<TaskRiskSummary>(`${projectPath(projectId)}/task-risks/summary`)

export const getSprintRiskSummary = (projectId: string, sprintId: string) =>
  apiRequest<TaskRiskSummary>(`${projectPath(projectId)}/sprints/${sprintId}/task-risks/summary`)

export const scanProjectRisks = (projectId: string) =>
  apiRequest<TaskRiskScan>(`${projectPath(projectId)}/task-risks/scan`, { method: 'POST' })
