export interface ProjectAiAskRequest {
  question: string
  additionalContext?: string
}

export interface ProjectAiAskResponse {
  projectId?: string
  question?: string
  answer: string
  keyPoints?: string[] | string
  relatedTasks?: RelatedTaskInfo[] | string[] | string
  risks?: string[] | string
  suggestions?: string[] | string
  limitation?: string
  provider?: string
  model?: string
  createdAt?: string
  [key: string]: any
}

export interface ChatMessage {
  id: string
  sender: 'user' | 'ai'
  text: string
  timestamp: string
  provider?: string
  model?: string
  loading?: boolean
  error?: string
  structuredData?: ProjectAiAskResponse
}

export type AiMeetingType = 'DAILY' | 'SPRINT_PLANNING' | 'SPRINT_REVIEW' | 'RETROSPECTIVE' | 'ISSUE_RESOLUTION'

export interface AiMeetingSuggestionRequest {
  meetingType: AiMeetingType
  additionalNote?: string
}

export interface RelatedTaskInfo {
  id?: string
  code?: string
  title?: string
  status?: string
}

export interface AiMeetingSuggestionResponse {
  title?: string
  meetingTitle?: string
  objective?: string
  goal?: string
  agenda?: string[] | string
  discussionQuestions?: string[] | string
  questions?: string[] | string
  relatedRisks?: string[] | string
  risks?: string[] | string
  relatedTasks?: RelatedTaskInfo[] | string[] | string
  tasks?: RelatedTaskInfo[] | string[] | string
  summary?: string
  overview?: string
  [key: string]: any
}

export interface AiMeetingMinutesRequest {
  meetingType?: AiMeetingType
  meetingTitle?: string
  meetingTime?: string
  participants?: string
  rawNotes: string
}

export interface ActionItemDraft {
  content?: string
  suggestedAssignee?: string
  suggestedDueDate?: string
  title?: string
  assignee?: string
  dueDate?: string
  priority?: string
  description?: string
  [key: string]: any
}

export interface AiMeetingMinutesResponse {
  title?: string
  meetingTitle?: string
  meetingType?: string
  meetingTime?: string
  participants?: string
  overview?: string
  discussedTopics?: string[] | string
  topics?: string[] | string
  decisions?: string[] | string
  unresolvedIssues?: string[] | string
  issues?: string[] | string
  relatedRisks?: string[] | string
  risks?: string[] | string
  actionItemDrafts?: ActionItemDraft[] | string[] | string
  actionItems?: ActionItemDraft[] | string[] | string
  summary?: string
  [key: string]: any
}

export interface AiActionItemRequest {
  meetingType?: AiMeetingType
  meetingTitle?: string
  meetingContent: string
  additionalNote?: string
}

export interface AiActionItemCandidate {
  temporaryId?: string
  id?: string
  title?: string
  name?: string
  description?: string
  proposedAssignee?: string
  suggestedAssigneeName?: string
  assignee?: string
  proposedDueDate?: string
  suggestedDueDate?: string
  dueDate?: string
  priority?: string
  relatedTasks?: string[] | string
  reasoning?: string
  reason?: string
  relatedTaskTitle?: string
  confidence?: number
  warningMessage?: string
  warning?: string
  [key: string]: any
}

export type AiActionItemsResponse = AiActionItemCandidate[] | {
  actionItems?: AiActionItemCandidate[]
  items?: AiActionItemCandidate[]
  suggestions?: AiActionItemCandidate[]
  [key: string]: any
}


