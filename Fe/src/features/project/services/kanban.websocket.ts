import { tokenStore } from '../../../services/apiClient'

export type KanbanRealtimeEventType =
  | 'TASK_CREATED'
  | 'TASK_UPDATED'
  | 'TASK_STATUS_CHANGED'
  | 'TASK_POSITION_CHANGED'
  | 'TASK_ASSIGNED'
  | 'TASK_UNASSIGNED'
  | 'TASK_DELETED'
  | 'TASK_COMMENTED'
  | 'TASK_TIME_LOGGED'

export interface KanbanRealtimeEvent {
  eventType: KanbanRealtimeEventType
  projectId: string
  sprintId: string | null
  taskId: string
  backlogItemId: string | null
  oldStatus: string | null
  newStatus: string | null
  oldPosition: number | null
  newPosition: number | null
  actorUserId: string
  actorUsername: string | null
  occurredAt: string
}

type RealtimeStatus = 'connected' | 'disconnected' | 'error'

interface SubscribeOptions {
  projectId: string
  sprintId: string
  onKanbanEvent: (event: KanbanRealtimeEvent) => void
  onStatusChange?: (status: RealtimeStatus) => void
}

function getWebSocketUrls(): string[] {
  const token = tokenStore.access()
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const query = token ? `?access_token=${encodeURIComponent(token)}` : ''

  return [
    `${protocol}//${window.location.host}/api/ws${query}`,
    `${protocol}//${window.location.host}/api/ws/websocket${query}`,
  ]
}

function stompFrame(command: string, headers: Record<string, string> = {}, body = '') {
  const headerLines = Object.entries(headers).map(([key, value]) => `${key}:${value}`)
  return `${command}\n${headerLines.join('\n')}\n\n${body}\0`
}

function parseStompMessage(frame: string) {
  const bodyStart = frame.indexOf('\n\n')
  if (bodyStart === -1) return null

  return frame.slice(bodyStart + 2).replace(/\0$/, '')
}

function extractStompFrames(rawData: unknown): { isSockJs: boolean; frames: string[] } {
  if (typeof rawData !== 'string') return { isSockJs: false, frames: [] }

  if (rawData === 'o' || rawData === 'h') {
    return { isSockJs: true, frames: [] }
  }

  if (rawData.startsWith('a')) {
    try {
      const parsed = JSON.parse(rawData.slice(1)) as string[]
      if (Array.isArray(parsed)) {
        return { isSockJs: true, frames: parsed }
      }
    } catch {}
  }

  return { isSockJs: false, frames: [rawData] }
}

export function subscribeKanbanEvents({ projectId, sprintId, onKanbanEvent, onStatusChange }: SubscribeOptions) {
  if (!projectId || !sprintId) return () => {}

  let socket: WebSocket | null = null
  let connected = false
  let isClosedManually = false
  let isSockJsMode = false
  let reconnectTimer: ReturnType<typeof setTimeout> | null = null
  let urlIndex = 0

  const connect = () => {
    if (isClosedManually) return

    const urls = getWebSocketUrls()
    const targetUrl = urls[urlIndex % urls.length]

    try {
      socket = new WebSocket(targetUrl)

      const sendFrame = (frame: string) => {
        if (socket && socket.readyState === WebSocket.OPEN) {
          if (isSockJsMode) {
            socket.send(JSON.stringify([frame]))
          } else {
            socket.send(frame)
          }
        }
      }

      socket.addEventListener('open', () => {
        sendFrame(stompFrame('CONNECT', {
          'accept-version': '1.2',
          'heart-beat': '10000,10000',
        }))
      })

      socket.addEventListener('message', event => {
        const { isSockJs, frames } = extractStompFrames(event.data)
        if (isSockJs) isSockJsMode = true

        for (const frame of frames) {
          if (frame.startsWith('CONNECTED')) {
            connected = true
            onStatusChange?.('connected')
            sendFrame(stompFrame('SUBSCRIBE', {
              id: `kanban-${projectId}-${sprintId}`,
              destination: `/topic/projects/${projectId}/sprints/${sprintId}/kanban`,
            }))
            continue
          }

          if (!frame.startsWith('MESSAGE')) continue

          const body = parseStompMessage(frame)
          if (!body) continue

          try {
            const eventData = JSON.parse(body) as KanbanRealtimeEvent
            onKanbanEvent(eventData)
          } catch {}
        }
      })

      socket.addEventListener('close', () => {
        if (isClosedManually) return
        onStatusChange?.(connected ? 'disconnected' : 'error')
        urlIndex++
        reconnectTimer = setTimeout(connect, 3000)
      })

      socket.addEventListener('error', () => {
        onStatusChange?.('error')
      })
    } catch {
      onStatusChange?.('error')
      urlIndex++
      reconnectTimer = setTimeout(connect, 4000)
    }
  }

  connect()

  return () => {
    isClosedManually = true
    if (reconnectTimer) clearTimeout(reconnectTimer)
    if (socket && socket.readyState === WebSocket.OPEN) {
      try {
        const disconnectPayload = stompFrame('DISCONNECT')
        socket.send(isSockJsMode ? JSON.stringify([disconnectPayload]) : disconnectPayload)
      } catch {}
      socket.close()
    }
  }
}
