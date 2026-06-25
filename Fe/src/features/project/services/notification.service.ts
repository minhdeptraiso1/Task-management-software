import { apiRequest } from '../../../services/apiClient'
import { endpoints } from '../../../services/endpoints'
import type { NotificationItem, NotificationPage } from '../models/notification.model'

export function getNotifications(page = 0, size = 6) {
  const params = new URLSearchParams({ page: String(page), size: String(size), sort: 'createdAt,desc' })
  return apiRequest<NotificationPage>(`${endpoints.notifications}?${params}`)
}

export const getUnreadCount = () =>
  apiRequest<{ unreadCount: number }>(endpoints.unreadNotifications)

export const markNotificationRead = (notificationId: string) =>
  apiRequest<NotificationItem>(`${endpoints.notifications}/${notificationId}/read`, { method: 'PATCH' })

export const markAllNotificationsRead = () =>
  apiRequest<void>(`${endpoints.notifications}/read-all`, { method: 'PATCH' })
