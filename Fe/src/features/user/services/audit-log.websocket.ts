import { tokenStore } from '../../../services/apiClient'
import type { AuditLog } from '../models/audit-log.model'

type RealtimeStatus = 'connected' | 'disconnected' | 'error'

interface SubscribeOptions {
  onLog: (log: AuditLog) => void
  onStatusChange?: (status: RealtimeStatus) => void
}

function getWebSocketUrl() {
  const token = tokenStore.access()
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const query = token ? `?access_token=${encodeURIComponent(token)}` : ''

  return `${protocol}//${window.location.host}/api/ws/websocket${query}`
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

export function subscribeAuditLogs({ onLog, onStatusChange }: SubscribeOptions) {
  const socket = new WebSocket(getWebSocketUrl())
  let connected = false

  const sendSockJsFrame = (frame: string) => {
    if (socket.readyState === WebSocket.OPEN) socket.send(JSON.stringify([frame]))
  }

  socket.addEventListener('open', () => {
    sendSockJsFrame(stompFrame('CONNECT', {
      'accept-version': '1.2',
      'heart-beat': '10000,10000',
    }))
  })

  socket.addEventListener('message', event => {
    if (event.data === 'o') return
    if (event.data === 'h') return

    if (typeof event.data !== 'string' || !event.data.startsWith('a')) return

    try {
      const frames = JSON.parse(event.data.slice(1)) as string[]
      for (const frame of frames) {
        if (frame.startsWith('CONNECTED')) {
          connected = true
          onStatusChange?.('connected')
          sendSockJsFrame(stompFrame('SUBSCRIBE', {
            id: 'audit-logs',
            destination: '/topic/audit-logs',
          }))
          continue
        }

        if (!frame.startsWith('MESSAGE')) continue

        const body = parseStompMessage(frame)
        if (!body) continue

        onLog(JSON.parse(body) as AuditLog)
      }
    } catch {
      onStatusChange?.('error')
    }
  })

  socket.addEventListener('close', () => {
    onStatusChange?.(connected ? 'disconnected' : 'error')
  })

  socket.addEventListener('error', () => {
    onStatusChange?.('error')
  })

  return () => {
    if (socket.readyState === WebSocket.OPEN) {
      sendSockJsFrame(stompFrame('DISCONNECT'))
    }
    socket.close()
  }
}
