import type { TaskPriority } from './task.model'

export type BugSeverity = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'
export type BugStatus = 'OPEN' | 'ASSIGNED' | 'IN_PROGRESS' | 'RESOLVED' | 'VERIFIED' | 'REOPENED' | 'CLOSED' | 'CANCELLED'

export interface Bug {
  id: string
  projectId: string
  backlogItemId: string | null
  taskId: string | null
  title: string
  description: string | null
  severity: BugSeverity
  priority: TaskPriority
  status: BugStatus
  assigneeUserId: string | null
  assigneeUsername: string | null
  assigneeEmail: string | null
  reporterUserId: string
  reporterUsername: string | null
  reporterEmail: string | null
  reproductionSteps: string | null
  expectedResult: string | null
  actualResult: string | null
  resolvedAt: string | null
  closedAt: string | null
  targetUrl: string | null
  overdue: boolean
  createdAt: string
  updatedAt: string | null
}

export interface BugPage {
  content: Bug[]
  totalElements: number
  totalPages: number
  number: number
  size: number
  numberOfElements: number
  first: boolean
  last: boolean
  empty: boolean
}

export interface BugSummary {
  projectId: string
  totalBugs: number
  openBugs: number
  inProgressBugs: number
  resolvedBugs: number
  reopenedBugs: number
  closedBugs: number
  cancelledBugs: number
  criticalBugs: number
  highBugs: number
  assignedBugs: number
  unassignedBugs: number
  byStatus: Record<string, number>
  bySeverity: Record<string, number>
}

export interface BugSearchFilters {
  keyword?: string
  status?: BugStatus | ''
  severity?: BugSeverity | ''
  priority?: TaskPriority | ''
  assigneeUserId?: string
  reporterUserId?: string
  taskId?: string
  backlogItemId?: string
  sprintId?: string
  linkedTaskId?: string
  reopenedOnly?: boolean
  overdueOnly?: boolean
  dueDateFrom?: string
  dueDateTo?: string
  createdFrom?: string
  createdTo?: string
}

export const bugSeverityLabels: Record<BugSeverity, string> = {
  LOW: 'Thấp',
  MEDIUM: 'Trung bình',
  HIGH: 'Cao',
  CRITICAL: 'Khẩn cấp'
}

export const bugStatusLabels: Record<BugStatus, string> = {
  OPEN: 'Mở',
  ASSIGNED: 'Đã phân công',
  IN_PROGRESS: 'Đang sửa',
  RESOLVED: 'Đã giải quyết',
  VERIFIED: 'Đã xác minh',
  REOPENED: 'Mở lại',
  CLOSED: 'Đã đóng',
  CANCELLED: 'Đã hủy'
}

export interface BugComment {
  id: string
  bugId: string
  parentId: string | null
  authorUserId: string
  authorUsername: string | null
  authorEmail: string | null
  content: string
  canEdit: boolean
  canDelete: boolean
  createdAt: string
  updatedAt: string | null
  replies?: BugComment[]
}

export interface BugEvidence {
  id: string
  bugId: string
  createdByUserId: string
  createdByUsername: string | null
  title: string
  stepsToReproduce: string | null
  expectedResult: string | null
  actualResult: string | null
  environment: string | null
  note: string | null
  canEdit: boolean
  canDelete: boolean
  createdAt: string
  updatedAt: string | null
}

export interface BugAttachment {
  id: string
  bugId: string
  uploadedByUserId: string
  uploadedByUsername: string | null
  originalFileName: string
  contentType: string
  sizeBytes: number
  downloadUrl: string
  canDelete: boolean
  createdAt: string
}

export interface BugCountByStatus {
  status: BugStatus
  total: number
}

export interface BugCountBySeverity {
  severity: BugSeverity
  total: number
}

export interface BugCountByPriority {
  priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT'
  total: number
}

export interface BugAssigneeSummary {
  assigneeUserId: string
  username: string
  email: string
  totalBugs: number
  openBugs: number
  resolvedBugs: number
  closedBugs: number
  overdueBugs: number
  criticalBugs: number
  reopenedBugs: number
}

export interface BugDashboard {
  projectId: string
  projectCode: string
  projectName: string
  totalBugs: number
  openBugs: number
  inProgressBugs: number
  resolvedBugs: number
  closedBugs: number
  cancelledBugs: number
  overdueBugs: number
  criticalBugs: number
  reopenedBugs: number
  resolveRate: number
  byStatus: BugCountByStatus[]
  bySeverity: BugCountBySeverity[]
  byPriority: BugCountByPriority[]
  byAssignee: BugAssigneeSummary[]
}

export interface BugReport {
  projectId: string
  sprintId: string | null
  fromDate: string
  toDate: string
  totalBugs: number
  openBugs: number
  resolvedBugs: number
  closedBugs: number
  overdueBugs: number
  criticalBugs: number
  reopenedBugs: number
  resolveRate: number
  overdueRate: number
  byStatus: BugCountByStatus[]
  bySeverity: BugCountBySeverity[]
  byPriority: BugCountByPriority[]
  byAssignee: BugAssigneeSummary[]
}

export interface QaMetrics {
  projectId: string
  fromDate: string
  toDate: string
  totalBugsReported: number
  totalBugsResolved: number
  totalBugsClosed: number
  totalBugsReopened: number
  totalCriticalBugs: number
  totalOverdueBugs: number
  resolveRate: number
  reopenRate: number
  overdueRate: number
  criticalRate: number
}

export interface BugReportFilters {
  fromDate?: string
  toDate?: string
  sprintId?: string
  assigneeUserId?: string
  status?: string
  severity?: string
  priority?: string
  overdueOnly?: boolean
}
