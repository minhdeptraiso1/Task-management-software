import { Activity, ChevronLeft, ChevronRight, Clock3, RefreshCw, ShieldCheck, UserRound } from 'lucide-react'
import { Button } from '../../../components/ui'
import { auditActionLabels, type AuditLogPage } from '../models/audit-log.model'

interface AuditLogViewProps {
  logs: AuditLogPage
  loading: boolean
  realtimeStatus: 'connected' | 'disconnected' | 'error'
  error: string
  page: number
  onPageChange: (page: number) => void
  onRefresh: () => void
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat('vi-VN', {
    dateStyle: 'short',
    timeStyle: 'medium',
  }).format(new Date(value))
}

export function AuditLogView({ logs, loading, realtimeStatus, error, page, onPageChange, onRefresh }: AuditLogViewProps) {
  const realtimeLabel = realtimeStatus === 'connected'
    ? 'Realtime đang bật'
    : realtimeStatus === 'error'
      ? 'Realtime chưa kết nối'
      : 'Realtime tạm ngắt'

  return <section className="overflow-hidden rounded-xl border border-line bg-white">
    <div className="flex flex-col gap-4 border-b border-line p-5 sm:flex-row sm:items-center sm:justify-between">
      <div className="flex items-start gap-3">
        <span className="grid size-11 shrink-0 place-items-center rounded-xl bg-[#fef3e2] text-brand">
          <Activity size={22} />
        </span>
        <div>
          <h2 className="font-bold">Quản lý hoạt động</h2>
          <p className="mt-1 text-sm text-muted">Theo dõi lịch sử cũ bằng API và nhận hoạt động mới qua WebSocket.</p>
        </div>
      </div>
      <div className="flex flex-wrap items-center gap-3">
        <span className={`inline-flex items-center gap-2 rounded-full px-3 py-1.5 text-xs font-semibold ${realtimeStatus === 'connected' ? 'bg-[#ecfdf3] text-success' : 'bg-panel text-muted'}`}>
          <i className={`size-2 rounded-full ${realtimeStatus === 'connected' ? 'bg-success' : 'bg-muted'}`} />
          {realtimeLabel}
        </span>
        <Button variant="secondary" leadingIcon={<RefreshCw size={17} />} loading={loading} onClick={onRefresh}>Tải lại</Button>
      </div>
    </div>

    {error && <p className="m-5 rounded-xl bg-[#fff0ed] p-3 text-sm text-[#ad3e2f]">{error}</p>}

    <div className="overflow-x-auto">
      <table className="w-full min-w-[760px] text-left text-sm">
        <thead className="sticky top-0 z-10 bg-panel text-xs font-semibold uppercase tracking-wider text-[#3f3f46]">
          <tr>
            <th className="px-5 py-3.5">Hoạt động</th>
            <th className="px-5 py-3.5">Người dùng</th>
            <th className="px-5 py-3.5">Thời gian</th>
            <th className="px-5 py-3.5">Mã log</th>
          </tr>
        </thead>
        <tbody className={loading ? 'opacity-45' : ''}>
          {logs.content.map(log => (
            <tr key={log.id} className="border-t border-line hover:bg-canvas">
              <td className="px-5 py-4">
                <div className="flex items-center gap-3">
                  <span className="grid size-9 place-items-center rounded-lg bg-brand-black text-brand">
                    <ShieldCheck size={17} />
                  </span>
                  <div>
                    <p className="font-semibold">{auditActionLabels[log.action] ?? log.action}</p>
                    <p className="mt-0.5 text-xs text-muted">{log.action}</p>
                  </div>
                </div>
              </td>
              <td className="px-5 py-4">
                <span className="inline-flex items-center gap-2 rounded-full bg-[#fef3e2] px-2.5 py-1 text-xs font-semibold text-brand-dark">
                  <UserRound size={14} />
                  {log.userId}
                </span>
              </td>
              <td className="px-5 py-4">
                <span className="inline-flex items-center gap-2 text-muted">
                  <Clock3 size={15} />
                  {formatDate(log.createdAt)}
                </span>
              </td>
              <td className="px-5 py-4 font-mono text-xs text-muted">{log.id.slice(0, 8)}</td>
            </tr>
          ))}
          {!logs.content.length && !loading && (
            <tr>
              <td colSpan={4} className="px-5 py-16 text-center text-muted">Chưa có hoạt động nào được ghi nhận.</td>
            </tr>
          )}
        </tbody>
      </table>
    </div>

    <footer className="flex items-center justify-between border-t border-line px-5 py-4 text-sm text-muted">
      <p>Trang {page + 1} / {Math.max(logs.totalPages, 1)}</p>
      <div className="flex gap-2">
        <Button variant="secondary" size="sm" disabled={page === 0} leadingIcon={<ChevronLeft size={16} />} onClick={() => onPageChange(page - 1)}>Trước</Button>
        <Button variant="secondary" size="sm" disabled={page + 1 >= logs.totalPages} trailingIcon={<ChevronRight size={16} />} onClick={() => onPageChange(page + 1)}>Sau</Button>
      </div>
    </footer>
  </section>
}
