import { apiRequest } from '../../../services/apiClient'
import { endpoints } from '../../../services/endpoints'
import type {
  AddProjectMemberData,
  CreateProjectData,
  Project,
  ProjectActivityDetailResponse,
  ProjectActivityPage,
  ProjectFilters,
  ProjectMember,
  ProjectMemberRole,
  ProjectPage,
  ProjectStatus,
  UpdateProjectData,
} from '../models/project.model'

export interface ProjectActivityFilters {
  keyword?: string
  performedByUserId?: string
  action?: string
  entityType?: string
  fromDate?: string
  toDate?: string
}

export function searchProjects(filters: ProjectFilters, page = 0, size = 9) {
  const params = new URLSearchParams({ page: String(page), size: String(size), sort: 'updatedAt,desc' })
  if (filters.keyword) params.set('keyword', filters.keyword)
  if (filters.status) params.set('status', filters.status)
  return apiRequest<ProjectPage>(`${endpoints.projects}?${params}`)
}

export const createProject = (data: CreateProjectData) =>
  apiRequest<Project>(endpoints.projects, { method: 'POST', body: JSON.stringify(data) })

export const getProject = (projectId: string) =>
  apiRequest<Project>(`${endpoints.projects}/${projectId}`)

export const updateProject = (projectId: string, data: UpdateProjectData) =>
  apiRequest<Project>(`${endpoints.projects}/${projectId}`, { method: 'PUT', body: JSON.stringify(data) })

export const updateProjectStatus = (projectId: string, status: ProjectStatus) =>
  apiRequest<Project>(`${endpoints.projects}/${projectId}/status`, { method: 'PATCH', body: JSON.stringify({ status }) })

export const deleteProject = (projectId: string) =>
  apiRequest<void>(`${endpoints.projects}/${projectId}`, { method: 'DELETE' })

export const getProjectMembers = (projectId: string) =>
  apiRequest<ProjectMember[]>(`${endpoints.projects}/${projectId}/members`)

export const addProjectMember = (projectId: string, data: AddProjectMemberData) =>
  apiRequest<ProjectMember>(`${endpoints.projects}/${projectId}/members`, { method: 'POST', body: JSON.stringify(data) })

export const updateProjectMemberRole = (projectId: string, memberId: string, role: ProjectMemberRole) =>
  apiRequest<ProjectMember>(`${endpoints.projects}/${projectId}/members/${memberId}/role`, { method: 'PATCH', body: JSON.stringify({ role }) })

export const removeProjectMember = (projectId: string, memberId: string) =>
  apiRequest<void>(`${endpoints.projects}/${projectId}/members/${memberId}`, { method: 'DELETE' })

export function getProjectActivities(projectId: string, page = 0, size = 10, filters: ProjectActivityFilters = {}) {
  let url = `${endpoints.projects}/${projectId}/activities?page=${page}&size=${size}&sort=createdAt,desc`
  if (filters.keyword) url += `&keyword=${encodeURIComponent(filters.keyword)}`
  if (filters.performedByUserId) url += `&performedByUserId=${encodeURIComponent(filters.performedByUserId)}`
  if (filters.action) url += `&action=${encodeURIComponent(filters.action)}`
  if (filters.entityType) url += `&entityType=${encodeURIComponent(filters.entityType)}`
  if (filters.fromDate) url += `&fromDate=${encodeURIComponent(filters.fromDate)}`
  if (filters.toDate) url += `&toDate=${encodeURIComponent(filters.toDate)}`
  return apiRequest<ProjectActivityPage>(url)
}

export const getProjectActivityById = (projectId: string, activityId: string) =>
  apiRequest<ProjectActivityDetailResponse>(`${endpoints.projects}/${projectId}/activities/${activityId}`)
