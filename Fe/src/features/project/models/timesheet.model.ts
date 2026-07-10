export interface TimesheetEntry {
  id: string
  projectId: string
  projectCode: string
  projectName: string
  taskId: string
  taskTitle: string
  userId: string
  username: string
  email: string
  workDate: string
  minutes: number
  description: string
  canEdit: boolean
  canDelete: boolean
  createdAt: string
  updatedAt: string
}

export interface TimesheetDailySummary {
  workDate: string
  totalMinutes: number
  logCount: number
}

export interface TimesheetUserSummary {
  userId: string
  username: string
  email: string
  totalMinutes: number
  logCount: number
  taskCount: number
}

export interface TimesheetSummary {
  projectId: string | null
  userId: string | null
  fromDate: string
  toDate: string
  totalMinutes: number
  totalLogs: number
  taskCount: number
  userCount: number
  byDate: TimesheetDailySummary[]
  byUser: TimesheetUserSummary[]
}

export interface TimesheetPage {
  content: TimesheetEntry[]
  totalElements: number
  totalPages: number
  number: number
  size: number
  numberOfElements: number
  first: boolean
  last: boolean
  empty: boolean
}
