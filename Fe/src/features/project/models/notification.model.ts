export interface NotificationItem {
  id: string
  type: string
  title: string
  content: string
  actorUserId: string | null
  projectId: string | null
  entityType: string | null
  entityId: string | null
  createdAt: string
  deliveredAt: string | null
  readAt: string | null
  read: boolean
}

export interface NotificationPage {
  content: NotificationItem[]
  totalElements: number
  totalPages: number
  number: number
  size: number
  numberOfElements: number
  first: boolean
  last: boolean
  empty: boolean
}
