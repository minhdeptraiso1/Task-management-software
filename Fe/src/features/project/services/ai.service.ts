import { apiRequest } from '../../../services/apiClient'
import { endpoints } from '../../../services/endpoints'
import type {
  ProjectAiAskRequest,
  ProjectAiAskResponse,
  AiMeetingSuggestionRequest,
  AiMeetingSuggestionResponse,
  AiMeetingMinutesRequest,
  AiMeetingMinutesResponse,
  AiActionItemRequest,
  AiActionItemsResponse,
} from '../models/ai.model'

export const askProjectAi = (projectId: string, payload: ProjectAiAskRequest | string) => {
  const body = typeof payload === 'string' ? { question: payload } : payload
  return apiRequest<ProjectAiAskResponse>(`${endpoints.projects}/${projectId}/ai/ask`, {
    method: 'POST',
    body: JSON.stringify(body),
  })
}

export const getAiMeetingSuggestions = (projectId: string, request: AiMeetingSuggestionRequest) =>
  apiRequest<AiMeetingSuggestionResponse>(`${endpoints.projects}/${projectId}/ai/meeting-suggestions`, {
    method: 'POST',
    body: JSON.stringify(request),
  })

export const generateAiMeetingMinutes = (projectId: string, request: AiMeetingMinutesRequest) =>
  apiRequest<AiMeetingMinutesResponse>(`${endpoints.projects}/${projectId}/ai/meeting-minutes`, {
    method: 'POST',
    body: JSON.stringify(request),
  })

export const getAiActionItems = (projectId: string, request: AiActionItemRequest) =>
  apiRequest<AiActionItemsResponse>(`${endpoints.projects}/${projectId}/ai/action-items`, {
    method: 'POST',
    body: JSON.stringify(request),
  })



