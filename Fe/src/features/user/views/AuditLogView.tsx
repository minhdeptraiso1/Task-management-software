import { useState } from 'react'
import { Activity, ChevronLeft, ChevronRight, Clock3, ShieldCheck, Search, Eye, CheckCircle2, XCircle, Globe } from 'lucide-react'
import { Button, Input, Select } from '../../../components/ui'
import { systemAuditActionLabels, type SystemAuditLogResponse, type SystemAuditLogPageResponse, type AdminAuditSummaryResponse } from '../models/admin.model'
import { formatDateTime } from '../../../utils/format'
import { AdminAuditDetailModal } from './AdminAuditDetailModal'

interface AuditLogViewProps {
  logs: SystemAuditLogPageResponse | any
  summary?: AdminAuditSummaryResponse | null
  loading: boolean
  realtimeStatus: 'connected' | 'disconnected' | 'error'
  error: string
  page: number
  usersMap?: Record<string, { username: string; email?: string }>
  onPageChange: (page: number) => void
  onRefresh?: () => void
  onFilterChange?: (filters: { action?: string; keyword?: string }) => void
}

export function AuditLogView({ logs, summary, loading, realtimeStatus, error, page, usersMap = {}, onPageChange, onFilterChange }: AuditLogViewProps) {
  const [selectedLog, setSelectedLog] = useState<SystemAuditLogResponse | null>(null)
  const [actionFilter, setActionFilter] = useState('')
  const [keyword, setKeyword] = useState('')

  const isConnected = realtimeStatus === 'connected'
  const realtimeLabel = isConnected
    ? 'Realtime đang bật'
    : realtimeStatus === 'error'
      ? 'Realtime chưa kết nối'
      : 'Realtime ngắt'

  const getActionBadge = (action: string) => {
    if (action.startsWith('LOGIN_SUCCESS') || action === 'USER_ENABLED') {
      return 'bg-emerald-50 text-emerald-700 border-emerald-200/80'
    }
    if (action.startsWith('LOGIN_FAILED') || action === 'USER_DISABLED' || action === 'FILE_DELETED') {
      return 'bg-rose-50 text-rose-700 border-rose-200/80'
    }
    if (action === 'USER_ROLE_CHANGED' || action === 'PASSWORD_CHANGED') {
      return 'bg-amber-50 text-amber-700 border-amber-200/80'
    }
    if (action.startsWith('REFRESH_TOKEN')) {
      return 'bg-blue-50 text-blue-700 border-blue-200/80'
    }
    return 'bg-brand-soft text-brand-dark border-brand-line/60'
  }

  const actionOptions = [
    { label: 'Tất cả hành động', value: '' },
    { label: 'Đăng nhập thành công', value: 'LOGIN_SUCCESS' },
    { label: 'Đăng nhập thất bại', value: 'LOGIN_FAILED' },
    { label: 'Kích hoạt tài khoản', value: 'USER_ENABLED' },
    { label: 'Vô hiệu hóa tài khoản', value: 'USER_DISABLED' },
    { label: 'Thay đổi vai trò', value: 'USER_ROLE_CHANGED' },
    { label: 'Tải file đính kèm', value: 'FILE_DOWNLOADED' },
    { label: 'Xóa file đính kèm', value: 'FILE_DELETED' },
    { label: 'Xem nhật ký user', value: 'ADMIN_VIEWED_USER_ACTIVITY' },
  ]

  const items: SystemAuditLogResponse[] = logs?.content || []

  return (
    <section className="overflow-hidden rounded-2xl border border-line/70 bg-white shadow-xs">
      <div className="flex flex-col gap-4 border-b border-line/60 p-5 sm:flex-row sm:items-center sm:justify-between">
        <div className="flex items-start gap-3">
          <span className="grid size-11 shrink-0 place-items-center rounded-xl bg-brand-soft text-brand border border-brand-line/50">
            <Activity size={22} />
          </span>
          <div>
            <h2 className="font-extrabold text-base text-ink">Nhật ký Kiểm toán Hệ thống (System Audit Logs)</h2>
            <p className="mt-0.5 text-xs text-muted font-medium">Theo dõi lịch sử truy cập, bảo mật tài khoản và thao tác nhạy cảm toàn hệ thống HICAS ONE.</p>
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
        </div>
      </div>

      {/* Summary KPI Cards from GET /admin/audit-logs/summary */}
      {summary && (
        <div className="grid grid-cols-2 gap-3 border-b border-line/60 bg-slate-50/70 p-4 sm:grid-cols-3 lg:grid-cols-6">
          <div className="rounded-xl border border-slate-200 bg-white p-3 shadow-2xs">
            <span className="text-[10px] font-bold uppercase text-slate-400">Tổng Audit Log</span>
            <p className="mt-0.5 text-lg font-black text-slate-900">{summary.totalLogs}</p>
          </div>
          <div className="rounded-xl border border-blue-100 bg-blue-50/40 p-3 shadow-2xs">
            <span className="text-[10px] font-bold uppercase text-blue-500">Xác thực (Auth)</span>
            <p className="mt-0.5 text-lg font-black text-blue-700">{summary.authLogs}</p>
          </div>
          <div className="rounded-xl border border-purple-100 bg-purple-50/40 p-3 shadow-2xs">
            <span className="text-[10px] font-bold uppercase text-purple-500">Người dùng (User)</span>
            <p className="mt-0.5 text-lg font-black text-purple-700">{summary.userLogs}</p>
          </div>
          <div className="rounded-xl border border-emerald-100 bg-emerald-50/40 p-3 shadow-2xs">
            <span className="text-[10px] font-bold uppercase text-emerald-500">Dự án (Project)</span>
            <p className="mt-0.5 text-lg font-black text-emerald-700">{summary.projectLogs}</p>
          </div>
          <div className="rounded-xl border border-amber-100 bg-amber-50/40 p-3 shadow-2xs">
            <span className="text-[10px] font-bold uppercase text-amber-500">Tệp tin (File)</span>
            <p className="mt-0.5 text-lg font-black text-amber-700">{summary.fileLogs}</p>
          </div>
          <div className="rounded-xl border border-teal-100 bg-teal-50/40 p-3 shadow-2xs">
            <span className="text-[10px] font-bold uppercase text-teal-500">Import Excel</span>
            <p className="mt-0.5 text-lg font-black text-teal-700">{summary.importLogs}</p>
          </div>
        </div>
      )}

      {/* Filter Row */}
      <div className="flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-3 p-4 bg-slate-50/50 border-b border-slate-100">
        <div className="sm:w-64">
          <Input
            placeholder="Tìm từ khóa, IP, User ID..."
            leadingIcon={<Search size={16} />}
            value={keyword}
            onChange={e => {
              const val = e.target.value
              setKeyword(val)
              onFilterChange?.({ action: actionFilter, keyword: val })
            }}
          />
        </div>
        <div className="sm:ml-auto sm:w-56 shrink-0">
          <Select
            value={actionFilter}
            onChange={e => {
              const val = e.target.value
              setActionFilter(val)
              onFilterChange?.({ action: val, keyword })
            }}
            options={actionOptions}
          />
        </div>
      </div>

      {error && (
        <div className="m-5 rounded-xl bg-rose-50 border border-rose-200 p-3.5 text-xs font-bold text-rose-700 animate-enter">
          {error}
        </div>
      )}

      <div className="overflow-x-auto">
        <table className="w-full min-w-[850px] text-left text-xs">
          <thead className="sticky top-0 z-10 bg-panel text-[11px] font-extrabold uppercase tracking-wider text-muted-dark border-b border-line/60">
            <tr>
              <th className="px-5 py-3.5">HÀNH ĐỘNG</th>
              <th className="px-5 py-3.5">NGƯỜI THỰC HIỆN</th>
              <th className="px-5 py-3.5">TRẠNG THÁI & IP</th>
              <th className="px-5 py-3.5">THỜI GIAN</th>
              <th className="px-5 py-3.5 text-center">CHI TIẾT</th>
            </tr>
          </thead>
          <tbody className={loading ? 'opacity-40 transition-opacity' : ''}>
            {items.map(log => {
              const username = log.actorUsername || (log.actorUserId ? (usersMap[log.actorUserId]?.username || log.actorUserId) : 'Hệ thống (System)')
              const email = log.actorEmail || (log.actorUserId ? usersMap[log.actorUserId]?.email : null)
              const initials = username ? username.slice(0, 2).toUpperCase() : 'SYS'
              const badgeClass = getActionBadge(log.action)
              const isSuccess = log.success !== false

              return (
                <tr key={log.id} className="border-t border-line/50 hover:bg-canvas/80 transition-colors">
                  <td className="px-5 py-3.5">
                    <div className="flex items-center gap-3">
                      <span className="grid size-8 place-items-center rounded-lg bg-brand-black text-brand shrink-0">
                        <ShieldCheck size={16} />
                      </span>
                      <div>
                        <span className={`inline-block px-2.5 py-0.5 rounded-full text-[11px] font-extrabold border ${badgeClass}`}>
                          {systemAuditActionLabels[log.action] ?? log.action}
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
                    <div className="space-y-0.5">
                      <div className="flex items-center gap-1 font-bold text-[11px]">
                        {isSuccess ? (
                          <span className="text-emerald-600 flex items-center gap-1">
                            <CheckCircle2 size={13} /> Thành công
                          </span>
                        ) : (
                          <span className="text-rose-600 flex items-center gap-1">
                            <XCircle size={13} /> Thất bại
                          </span>
                        )}
                      </div>
                      {log.ipAddress && (
                        <p className="text-[10px] font-mono text-slate-500 flex items-center gap-1">
                          <Globe size={11} className="text-slate-400" /> {log.ipAddress}
                        </p>
                      )}
                    </div>
                  </td>
                  <td className="px-5 py-3.5">
                    <span className="inline-flex items-center gap-1.5 font-medium text-muted">
                      <Clock3 size={14} className="text-muted/70" />
                      {formatDateTime(log.createdAt)}
                    </span>
                  </td>
                  <td className="px-5 py-3.5 text-center">
                    <Button
                      variant="ghost"
                      size="sm"
                      iconOnly
                      className="!size-8 !rounded-lg border border-slate-200 bg-white text-slate-600 hover:bg-slate-100 hover:text-slate-900"
                      leadingIcon={<Eye size={15} />}
                      title="Xem chi tiết Audit Log"
                      onClick={() => setSelectedLog(log)}
                    />
                  </td>
                </tr>
              )
            })}
            {!items.length && !loading && (
              <tr>
                <td colSpan={5} className="px-5 py-16 text-center text-muted font-medium">
                  Chưa có hoạt động audit log nào được ghi nhận.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      <footer className="flex items-center justify-between border-t border-line/60 px-5 py-3.5 text-xs text-muted font-medium">
        <p>Trang <span className="font-bold text-ink">{page + 1}</span> / {Math.max(logs?.totalPages || 1, 1)}</p>
        <div className="flex gap-2">
          <Button variant="secondary" size="sm" disabled={page === 0} leadingIcon={<ChevronLeft size={15} />} onClick={() => onPageChange(page - 1)}>
            Trước
          </Button>
          <Button variant="secondary" size="sm" disabled={page + 1 >= (logs?.totalPages || 1)} trailingIcon={<ChevronRight size={15} />} onClick={() => onPageChange(page + 1)}>
            Sau
          </Button>
        </div>
      </footer>

      {selectedLog && (
        <AdminAuditDetailModal
          log={selectedLog}
          onClose={() => setSelectedLog(null)}
        />
      )}
    </section>
  )
}
