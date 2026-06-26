export type BacklogItemStatus = 'DRAFT' | 'READY' | 'IN_SPRINT' | 'DONE' | 'CANCELLED'
export type BacklogItemType = 'EPIC' | 'USER_STORY' | 'FEATURE' | 'TECHNICAL'
export type BacklogPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT'
export type SprintStatus = 'PLANNING' | 'ACTIVE' | 'COMPLETED' | 'CANCELLED'

export interface BacklogItem {
  id: string
  projectId: string
  sprintId: string | null
  title: string
  description: string | null
  type: BacklogItemType
  status: BacklogItemStatus
  priority: BacklogPriority
  storyPoints: number | null
  position: number
  createdByUserId: string
  createdAt: string
  updatedAt: string | null
}

export interface BacklogItemPage {
  content: BacklogItem[]
  totalElements: number
  totalPages: number
  number: number
  size: number
  numberOfElements: number
  first: boolean
  last: boolean
  empty: boolean
}

export interface Sprint {
  id: string
  projectId: string
  name: string
  goal: string | null
  status: SprintStatus
  startDate: string | null
  endDate: string | null
  startedAt: string | null
  completedAt: string | null
  createdByUserId: string
  backlogItemCount: number
  createdAt: string
  updatedAt: string | null
}

export interface SprintPage {
  content: Sprint[]
  totalElements: number
  totalPages: number
  number: number
  size: number
  numberOfElements: number
  first: boolean
  last: boolean
  empty: boolean
}

export const backlogStatusLabels: Record<BacklogItemStatus, string> = {
  DRAFT: 'Nháp',
  READY: 'Sẵn sàng',
  IN_SPRINT: 'Trong sprint',
  DONE: 'Hoàn thành',
  CANCELLED: 'Đã hủy',
}

export const backlogTypeLabels: Record<BacklogItemType, string> = {
  EPIC: 'Epic',
  USER_STORY: 'User Story',
  FEATURE: 'Feature',
  TECHNICAL: 'Technical',
}

export const backlogPriorityLabels: Record<BacklogPriority, string> = {
  LOW: 'Thấp',
  MEDIUM: 'Vừa',
  HIGH: 'Cao',
  URGENT: 'Khẩn cấp',
}

export const sprintStatusLabels: Record<SprintStatus, string> = {
  PLANNING: 'Lên kế hoạch',
  ACTIVE: 'Đang chạy',
  COMPLETED: 'Hoàn thành',
  CANCELLED: 'Đã hủy',
}
