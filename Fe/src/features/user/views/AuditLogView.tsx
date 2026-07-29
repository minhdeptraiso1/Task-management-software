import { Activity, ChevronLeft, ChevronRight, Clock3, RefreshCw, ShieldCheck } from 'lucide-react'
import { Button } from '../../../components/ui'
import { auditActionLabels, type AuditLogPage } from '../models/audit-log.model'
import { formatDateTime } from '../../../utils/format'

interface AuditLogViewProps {
  logs: AuditLogPage
  loading: boolean
  realtimeStatus: 'connected' | 'disconnected' | 'error'
  error: string
  page: number
  usersMap?: Record<string, { username: string; email?: string }>
  onPageChange: (page: number) => void
  onRefresh: () => void
}

export function AuditLogView({ logs, loading, realtimeStatus, error, page, usersMap = {}, onPageChange, onRefresh }: AuditLogViewProps) {
  const isConnected = realtimeStatus === 'connected'
  const realtimeLabel = isConnected
    ? 'Realtime đang bật'
    : realtimeStatus === 'error'
      ? 'Realtime chưa kết nối'
      : 'Realtime tạm ngắt'

  const getActionBadge = (action: string) => {
    switch (action) {
      case 'LOGIN':
        return 'bg-blue-50 text-blue-700 border-blue-200/80'
      case 'LOGOUT':
        return 'bg-slate-100 text-slate-600 border-slate-200'
      case 'CHANGE_PASSWORD':
        return 'bg-amber-50 text-amber-700 border-amber-200/80'
      case 'LOGOUT_ALL':
        return 'bg-rose-50 text-rose-700 border-rose-200/80'
      default:
        return 'bg-brand-soft text-brand-dark border-brand-line/60'
    }
  }

  return (
    <section className="overflow-hidden rounded-2xl border border-line/70 bg-white shadow-xs">
      <div className="flex flex-col gap-4 border-b border-line/60 p-5 sm:flex-row sm:items-center sm:justify-between">
        <div className="flex items-start gap-3">
          <span className="grid size-11 shrink-0 place-items-center rounded-xl bg-brand-soft text-brand border border-brand-line/50">
            <Activity size={22} />
          </span>
          <div>
            <h2 className="font-extrabold text-base text-ink">Quản lý hoạt động hệ thống (Audit Logs)</h2>
            <p className="mt-0.5 text-xs text-muted font-medium">Theo dõi lịch sử truy cập CSDL và cập nhật tức thì qua kết nối WebSocket realtime.</p>
          </div>
        </div>
        <div className="flex flex-wrap items-center gap-3">
          <span className={`inline-flex items-center gap-2 rounded-full px-3 py-1.5 text-xs font-extrabold border transition-all ${
            isConnected
              ? 'bg-emerald-50 text-emerald-700 border-emerald-200/80 shadow-2xs'
              : 'bg-slate-100 text-slate-500 border-slate-200'
          }`}>
            <span className="relative flex size-2.5">
              {isConnected && (
                <span className="absolute inline-flex h-full w-full animate-ping rounded-full bg-emerald-400 opacity-75" />
              )}
              <span className={`relative inline-flex size-2.5 rounded-full ${isConnected ? 'bg-emerald-500' : 'bg-slate-400'}`} />
            </span>
            {realtimeLabel}
          </span>
          <Button variant="secondary" size="sm" leadingIcon={<RefreshCw size={15} />} loading={loading} onClick={onRefresh}>
            Tải lại
          </Button>
        </div>
      </div>

      {error && (
        <div className="m-5 rounded-xl bg-rose-50 border border-rose-200 p-3.5 text-xs font-bold text-rose-700 animate-enter">
          {error}
        </div>
      )}

      <div className="overflow-x-auto">
        <table className="w-full min-w-[760px] text-left text-xs">
          <thead className="sticky top-0 z-10 bg-panel text-[11px] font-extrabold uppercase tracking-wider text-muted-dark border-b border-line/60">
            <tr>
              <th className="px-5 py-3.5">HOẠT ĐỘNG</th>
              <th className="px-5 py-3.5">NGƯỜI DÙNG</th>
              <th className="px-5 py-3.5">THỜI GIAN</th>
              <th className="px-5 py-3.5">MÃ LOG</th>
            </tr>
          </thead>
          <tbody className={loading ? 'opacity-40 transition-opacity' : ''}>
            {logs.content.map(log => {
              const username = log.username || usersMap[log.userId]?.username || log.userId
              const email = log.email || usersMap[log.userId]?.email
              const initials = username ? username.slice(0, 2).toUpperCase() : 'US'
              const badgeClass = getActionBadge(log.action)

              return (
                <tr key={log.id} className="border-t border-line/50 hover:bg-canvas/80 transition-colors">
                  <td className="px-5 py-3.5">
                    <div className="flex items-center gap-3">
                      <span className="grid size-8 place-items-center rounded-lg bg-brand-black text-brand shrink-0">
                        <ShieldCheck size={16} />
                      </span>
                      <div>
                        <span className={`inline-block px-2.5 py-0.5 rounded-full text-[11px] font-extrabold border ${badgeClass}`}>
                          {auditActionLabels[log.action] ?? log.action}
                        </span>
                        <p className="mt-0.5 text-[10px] font-mono text-muted">{log.action}</p>
                      </div>
                    </div>
                  </td>
                  <td className="px-5 py-3.5">
                    <div className="flex items-center gap-2.5">
                      <span className="grid size-8 place-items-center rounded-full bg-brand-soft text-brand-dark font-extrabold text-xs border border-brand-line/50 shrink-0">
                        {initials}
                      </span>
                      <div>
                        <p className="font-bold text-ink text-xs">{username}</p>
                        {email && <p className="text-[11px] text-muted font-medium">{email}</p>}
                      </div>
                    </div>
                  </td>
                  <td className="px-5 py-3.5">
                    <span className="inline-flex items-center gap-1.5 font-medium text-muted">
                      <Clock3 size={14} className="text-muted/70" />
                      {formatDateTime(log.createdAt)}
                    </span>
                  </td>
                  <td className="px-5 py-3.5 font-mono text-[11px] text-muted">
                    <span className="rounded-md bg-slate-100 px-2 py-1 border border-slate-200/80">
                      {log.id.slice(0, 8)}
                    </span>
                  </td>
                </tr>
              )
            })}
            {!logs.content.length && !loading && (
              <tr>
                <td colSpan={4} className="px-5 py-16 text-center text-muted font-medium">
                  Chưa có hoạt động nào được ghi nhận.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      <footer className="flex items-center justify-between border-t border-line/60 px-5 py-3.5 text-xs text-muted font-medium">
        <p>Trang <span className="font-bold text-ink">{page + 1}</span> / {Math.max(logs.totalPages, 1)}</p>
        <div className="flex gap-2">
          <Button variant="secondary" size="sm" disabled={page === 0} leadingIcon={<ChevronLeft size={15} />} onClick={() => onPageChange(page - 1)}>
            Trước
          </Button>
          <Button variant="secondary" size="sm" disabled={page + 1 >= logs.totalPages} trailingIcon={<ChevronRight size={15} />} onClick={() => onPageChange(page + 1)}>
            Sau
          </Button>
        </div>
      </footer>
    </section>
  )
}

