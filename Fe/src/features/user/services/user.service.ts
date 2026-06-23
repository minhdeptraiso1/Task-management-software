import { apiRequest } from '../../../services/apiClient'
import { endpoints } from '../../../services/endpoints'
import type { CreateUserData, UpdateUserData, User, UserFilters, UserPage, UserRole } from '../models/user.model'

export const getMe = () => apiRequest<User>(endpoints.me)
export const getUserRoles = () => apiRequest<UserRole[]>(endpoints.userRoles)
export function searchUsers(filters: UserFilters, page = 0, size = 8) {
  const params = new URLSearchParams({ page: String(page), size: String(size), sort: 'username,asc' })
  if (filters.keyword) params.set('keyword', filters.keyword)
  if (filters.role) params.set('role', filters.role)
  if (filters.enabled) params.set('enabled', filters.enabled)
  return apiRequest<UserPage>(`${endpoints.userSearch}?${params}`)
}
export const createUser = (data: CreateUserData) => apiRequest<User>(endpoints.users, { method: 'POST', body: JSON.stringify(data) })
export const updateUser = (id: string, data: UpdateUserData) => apiRequest<User>(`${endpoints.users}/${id}`, { method: 'PUT', body: JSON.stringify(data) })
export const deleteUser = (id: string) => apiRequest<void>(`${endpoints.users}/${id}`, { method: 'DELETE' })
