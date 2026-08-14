export type ProjectMeetingType =
  | 'DAILY'
  | 'SPRINT_PLANNING'
  | 'SPRINT_REVIEW'
  | 'RETROSPECTIVE'
  | 'ISSUE_RESOLUTION'
  | 'OTHER'

export interface ProjectMeeting {
  id: string
  projectId: string
  title: string
  meetingType: ProjectMeetingType
  description?: string
  startTime: string
  endTime: string
  googleMeetLink?: string
  hasGoogleMeetLink: boolean
  createdByUserId?: string
  createdByName?: string
  createdAt?: string
  updatedAt?: string
}

export interface CreateMeetingRequest {
  title: string
  meetingType: ProjectMeetingType
  description?: string
  startTime: string
  endTime: string
  googleMeetLink?: string
}

export interface UpdateGoogleMeetLinkRequest {
  googleMeetLink: string
}
