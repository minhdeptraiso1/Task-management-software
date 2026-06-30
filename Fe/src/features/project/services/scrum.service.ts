import { apiRequest } from '../../../services/apiClient'
import { endpoints } from '../../../services/endpoints'
import type { BacklogItem, BacklogItemPage, BacklogItemStatus, BacklogItemType, BacklogPriority, Sprint, SprintPage } from '../models/scrum.model'

const projectPath = (projectId: string) => `${endpoints.projects}/${projectId}`

export function getBacklogItems(projectId: string, options: { sprintId?: string; unscheduledOnly?: boolean } = {}) {
  const params = new URLSearchParams({ page: '0', size: '200', sort: 'position,asc' })
  if (options.sprintId) params.set('sprintId', options.sprintId)
  if (options.unscheduledOnly) params.set('unscheduledOnly', 'true')
  return apiRequest<BacklogItemPage>(`${projectPath(projectId)}/backlog-items?${params}`)
}

export const createBacklogItem = (projectId: string, data: { title: string; description?: string; type: BacklogItemType; priority?: BacklogPriority; storyPoints?: number }) =>
  apiRequest<BacklogItem>(`${projectPath(projectId)}/backlog-items`, { method: 'POST', body: JSON.stringify(data) })

export const getBacklogItem = (projectId: string, itemId: string) =>
  apiRequest<BacklogItem>(`${projectPath(projectId)}/backlog-items/${itemId}`)

export const updateBacklogItem = (projectId: string, itemId: string, data: { title?: string; description?: string; type?: BacklogItemType; priority?: BacklogPriority; storyPoints?: number }) =>
  apiRequest<BacklogItem>(`${projectPath(projectId)}/backlog-items/${itemId}`, { method: 'PATCH', body: JSON.stringify(data) })

export const updateBacklogItemStatus = (projectId: string, itemId: string, status: BacklogItemStatus) =>
  apiRequest<BacklogItem>(`${projectPath(projectId)}/backlog-items/${itemId}/status`, { method: 'PATCH', body: JSON.stringify({ status }) })

export const updateBacklogItemPriority = (projectId: string, itemId: string, priority: BacklogPriority) =>
  apiRequest<BacklogItem>(`${projectPath(projectId)}/backlog-items/${itemId}/priority`, { method: 'PATCH', body: JSON.stringify({ priority }) })

export const deleteBacklogItem = (projectId: string, itemId: string) =>
  apiRequest<void>(`${projectPath(projectId)}/backlog-items/${itemId}`, { method: 'DELETE' })

export const updateBacklogItemPosition = (projectId: string, itemId: string, position: number) =>
  apiRequest<BacklogItem>(`${projectPath(projectId)}/backlog-items/${itemId}/position`, { method: 'PATCH', body: JSON.stringify({ position }) })

export function getSprints(projectId: string) {
  const params = new URLSearchParams({ page: '0', size: '50', sort: 'createdAt,desc' })
  return apiRequest<SprintPage>(`${projectPath(projectId)}/sprints?${params}`)
}

export const getSprint = (projectId: string, sprintId: string) =>
  apiRequest<Sprint>(`${projectPath(projectId)}/sprints/${sprintId}`)

export const createSprint = (projectId: string, data: { name: string; goal?: string; startDate?: string; endDate?: string }) =>
  apiRequest<Sprint>(`${projectPath(projectId)}/sprints`, { method: 'POST', body: JSON.stringify(data) })

export const updateSprint = (projectId: string, sprintId: string, data: { name?: string; goal?: string; startDate?: string; endDate?: string }) =>
  apiRequest<Sprint>(`${projectPath(projectId)}/sprints/${sprintId}`, { method: 'PATCH', body: JSON.stringify(data) })

export const deleteSprint = (projectId: string, sprintId: string) =>
  apiRequest<void>(`${projectPath(projectId)}/sprints/${sprintId}`, { method: 'DELETE' })

export const addBacklogItemToSprint = (projectId: string, sprintId: string, itemId: string) =>
  apiRequest<BacklogItem>(`${projectPath(projectId)}/sprints/${sprintId}/backlog-items/${itemId}`, { method: 'POST' })

export const removeBacklogItemFromSprint = (projectId: string, sprintId: string, itemId: string) =>
  apiRequest<BacklogItem>(`${projectPath(projectId)}/sprints/${sprintId}/backlog-items/${itemId}`, { method: 'DELETE' })

export const startSprint = (projectId: string, sprintId: string) =>
  apiRequest<Sprint>(`${projectPath(projectId)}/sprints/${sprintId}/start`, { method: 'PATCH' })

export const completeSprint = (projectId: string, sprintId: string) =>
  apiRequest<Sprint>(`${projectPath(projectId)}/sprints/${sprintId}/complete`, { method: 'PATCH' })

export const cancelSprint = (projectId: string, sprintId: string) =>
  apiRequest<Sprint>(`${projectPath(projectId)}/sprints/${sprintId}/cancel`, { method: 'PATCH' })
