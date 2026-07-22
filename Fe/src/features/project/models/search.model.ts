export type SearchEntityType =
  | 'PROJECT'
  | 'SPRINT'
  | 'BACKLOG_ITEM'
  | 'TASK'
  | 'BUG'
  | 'COMMENT'
  | 'ATTACHMENT'

export interface SearchResultItem {
  entityType: SearchEntityType
  entityId: string
  title: string
  description: string | null
  projectId: string
  projectCode: string
  projectName: string
  targetUrl: string
  matchedText: string | null
  updatedAt: string
}

export interface GlobalSearchResponse {
  keyword: string
  totalElements: number
  results: SearchResultItem[]
}
