import { apiRequest } from '../../../services/apiClient'
import type { GlobalSearchResponse, SearchEntityType } from '../models/search.model'

export const searchGlobal = (
  q: string,
  entityType?: SearchEntityType,
  projectId?: string
) => {
  const query = new URLSearchParams({ q })
  if (entityType) {
    query.append('entityType', entityType)
  }
  if (projectId) {
    query.append('projectId', projectId)
  }
  return apiRequest<GlobalSearchResponse>(`/search?${query.toString()}`)
}

export const searchInProject = (
  projectId: string,
  q: string,
  entityType?: SearchEntityType
) => {
  const query = new URLSearchParams({ q })
  if (entityType) {
    query.append('entityType', entityType)
  }
  return apiRequest<GlobalSearchResponse>(`/projects/${projectId}/search?${query.toString()}`)
}
