import { apiRequest } from '../../../services/apiClient'
import { endpoints } from '../../../services/endpoints'
import type {
  ProjectMeeting,
  CreateMeetingRequest,
  UpdateGoogleMeetLinkRequest,
} from '../models/meeting.model'

export const getProjectMeetings = (projectId: string) =>
  apiRequest<ProjectMeeting[]>(`${endpoints.projects}/${projectId}/meetings`)

export const getProjectMeetingDetail = (projectId: string, meetingId: string) =>
  apiRequest<ProjectMeeting>(`${endpoints.projects}/${projectId}/meetings/${meetingId}`)

export const createProjectMeeting = (projectId: string, data: CreateMeetingRequest) =>
  apiRequest<ProjectMeeting>(`${endpoints.projects}/${projectId}/meetings`, {
    method: 'POST',
    body: JSON.stringify(data),
  })

export const updateGoogleMeetLink = (
  projectId: string,
  meetingId: string,
  googleMeetLink: string
) =>
  apiRequest<ProjectMeeting>(`${endpoints.projects}/${projectId}/meetings/${meetingId}/google-meet-link`, {
    method: 'PATCH',
    body: JSON.stringify({ googleMeetLink } as UpdateGoogleMeetLinkRequest),
  })
