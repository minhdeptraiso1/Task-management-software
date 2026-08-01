import { useState, useEffect } from 'react'
import { cleanupDeletedFiles, cleanupOrphanFiles } from '../../project/services/attachment.service'
import { getFileAudits } from '../services/admin.service'
import type { FileCleanupResult } from '../../project/models/attachment.model'
import { Button, Input } from '../../../components/ui'
import { ShieldAlert, Trash2, ShieldCheck, RefreshCcw, AlertTriangle } from 'lucide-react'

export function AdminFileCleanupView() {
  const [limit, setLimit] = useState(100)
  
  const [deletedLoading, setDeletedLoading] = useState(false)
  const [deletedResult, setDeletedResult] = useState<FileCleanupResult | null>(null)
  const [deletedError, setDeletedError] = useState('')

  const [orphanLoading, setOrphanLoading] = useState(false)
  const [orphanResult, setOrphanResult] = useState<FileCleanupResult | null>(null)
  const [orphanError, setOrphanError] = useState('')

  const handleCleanupDeleted = async () => {
    setDeletedLoading(true)
    setDeletedResult(null)
    setDeletedError('')
    try {
      const res = await cleanupDeletedFiles(limit)
      setDeletedResult(res)
    } catch (err) {
      setDeletedError(err instanceof Error ? err.message : 'Dọn dẹp tệp tin xóa mềm thất bại')
    } finally {
      setDeletedLoading(false)
    }
  }

  const handleCleanupOrphans = async () => {
    setOrphanLoading(true)
    setOrphanResult(null)
    setOrphanError('')
    try {
      const res = await cleanupOrphanFiles(limit)
      setOrphanResult(res)
    } catch (err) {
      setOrphanError(err instanceof Error ? err.message : 'Dọn dẹp tệp tin mồ côi thất bại')
    } finally {
      setOrphanLoading(false)
    }
  }

  return (
    <div className="space-y-6">
      <div className="rounded-xl border border-line bg-white p-5 shadow-sm">
        <h2 className="text-base font-bold text-ink flex items-center gap-2">
          <ShieldAlert className="text-brand" size={20} />
          Quản lý & Dọn dẹp Hệ thống Tệp Tin (File Storage Administrator)
        </h2>
        <p className="mt-1 text-sm text-muted">
          Công cụ quản trị hệ thống để giải phóng dung lượng đĩa cứng bằng cách dọn dẹp các tệp tin vật lý dư thừa.
        </p>

        <div className="mt-4 rounded-xl border border-brand-line/60 bg-brand-cream/30 p-4 text-xs text-muted-dark space-y-1.5">
          <p className="font-bold text-ink">⏰ Lập lịch tự động dọn dẹp (System Cron Job):</p>
          <p>
            Hệ thống tự động thực hiện tiến trình dọn dẹp ngầm hằng ngày vào lúc <strong>02:30 AM (Múi giờ Asia/Ho_Chi_Minh)</strong>. Quản trị viên chỉ cần sử dụng công cụ dưới đây khi cần dọn dẹp thủ công khẩn cấp.
          </p>
        </div>
      </div>

      <div className="grid gap-6 md:grid-cols-2">
        {/* Cleanup Soft-Deleted Files */}
        <div className="rounded-2xl border border-line bg-white p-6 shadow-sm flex flex-col justify-between">
          <div className="space-y-4">
            <h3 className="font-bold text-ink text-sm flex items-center gap-2">
              <Trash2 className="text-rose-500" size={18} />
              Dọn dẹp tệp tin đã xóa mềm
            </h3>
            <p className="text-xs text-muted leading-relaxed">
              Khi người dùng xóa tài liệu đính kèm (Task, Bug, Comment), hệ thống chỉ đánh dấu xóa mềm trong CSDL. Tệp tin vật lý trên ổ cứng sẽ được giữ lại theo số ngày cấu hình trước khi tiến trình này thực hiện xóa vĩnh viễn.
            </p>

            <div className="pt-2">
              <label className="block text-[11px] font-bold text-muted-dark mb-1">Giới hạn quét tối đa (Limit)</label>
              <Input
                type="number"
                min={1}
                max={500}
                value={limit}
                onChange={e => setLimit(Math.max(1, Number(e.target.value)))}
                placeholder="Số file tối đa..."
              />
            </div>

            {deletedResult && (
              <div className="rounded-xl bg-success/5 border border-success/20 p-4 text-xs text-success space-y-1 animate-enter">
                <p className="font-bold flex items-center gap-1">
                  <ShieldCheck size={14} /> Dọn dẹp thành công!
                </p>
                <p>Số tệp đã quét: {deletedResult.scannedFiles}</p>
                <p>Số tệp vật lý đã xóa: {deletedResult.deletedFiles}</p>
                <p>Số tệp lỗi: {deletedResult.failedFiles}</p>
              </div>
            )}

            {deletedError && (
              <div className="rounded-xl bg-danger/5 border border-danger/20 p-4 text-xs text-danger animate-enter">
                <p className="font-bold flex items-center gap-1">
                  <AlertTriangle size={14} /> Có lỗi xảy ra
                </p>
                <p className="mt-1">{deletedError}</p>
              </div>
            )}
          </div>

          <div className="pt-6 border-t border-line/60 mt-6 flex justify-end">
            <Button
              variant="danger"
              size="sm"
              loading={deletedLoading}
              onClick={handleCleanupDeleted}
            >
              Kích hoạt dọn dẹp
            </Button>
          </div>
        </div>

        {/* Cleanup Orphaned Files */}
        <div className="rounded-2xl border border-line bg-white p-6 shadow-sm flex flex-col justify-between">
          <div className="space-y-4">
            <h3 className="font-bold text-ink text-sm flex items-center gap-2">
              <RefreshCcw className="text-amber-500" size={18} />
              Dọn dẹp tệp tin mồ côi (Orphans)
            </h3>
            <p className="text-xs text-muted leading-relaxed">
              Quét thư mục lưu trữ vật lý và đối chiếu với CSDL. Bất kỳ tệp tin nào tồn tại trên ổ đĩa vật lý nhưng không có thông tin metadata đăng ký trong bảng CSDL (do lỗi hệ thống hoặc gián đoạn lưu trữ) sẽ bị xóa bỏ hoàn toàn.
            </p>

            <div className="pt-2">
              <label className="block text-[11px] font-bold text-muted-dark mb-1">Giới hạn quét tối đa (Limit)</label>
              <Input
                type="number"
                min={1}
                max={500}
                value={limit}
                onChange={e => setLimit(Math.max(1, Number(e.target.value)))}
                placeholder="Số file tối đa..."
              />
            </div>

            {orphanResult && (
              <div className="rounded-xl bg-success/5 border border-success/20 p-4 text-xs text-success space-y-1 animate-enter">
                <p className="font-bold flex items-center gap-1">
                  <ShieldCheck size={14} /> Dọn dẹp mồ côi thành công!
                </p>
                <p>Số tệp đã quét: {orphanResult.scannedFiles}</p>
                <p>Số tệp vật lý đã xóa: {orphanResult.deletedFiles}</p>
                <p>Số tệp lỗi: {orphanResult.failedFiles}</p>
              </div>
            )}

            {orphanError && (
              <div className="rounded-xl bg-danger/5 border border-danger/20 p-4 text-xs text-danger animate-enter">
                <p className="font-bold flex items-center gap-1">
                  <AlertTriangle size={14} /> Có lỗi xảy ra
                </p>
                <p className="mt-1">{orphanError}</p>
              </div>
            )}
          </div>

          <div className="pt-6 border-t border-line/60 mt-6 flex justify-end">
            <Button
              variant="solid-blue"
              size="sm"
              loading={orphanLoading}
              onClick={handleCleanupOrphans}
            >
              Kích hoạt dọn dẹp mồ côi
            </Button>
          </div>
        </div>
      </div>

      {/* System-wide File Audits Section */}
      <AdminFileAuditList />
    </div>
  )
}

function AdminFileAuditList() {
  const [fileAudits, setFileAudits] = useState<any>(null)
  const [loading, setLoading] = useState(false)
  const [page, setPage] = useState(0)

  const fetchFileAudits = async () => {
    setLoading(true)
    try {
      const res = await getFileAudits(page, 10)
      setFileAudits(res)
    } catch {
      // Ignore
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    void Promise.resolve().then(fetchFileAudits)
  }, [page])

  const items = fileAudits?.content || []

  return (
    <div className="rounded-2xl border border-line bg-white p-6 shadow-sm space-y-4">
      <div className="flex items-center justify-between">
        <div>
          <h3 className="font-bold text-ink text-sm flex items-center gap-2">
            <ShieldCheck className="text-emerald-600" size={18} />
            Nhật ký kiểm toán Tệp tin Toàn hệ thống (System-wide File Audits)
          </h3>
          <p className="text-xs text-muted mt-0.5">
            Lịch sử thao tác tải xuống (FILE_DOWNLOADED) và xóa (FILE_DELETED) tệp tin của người dùng.
          </p>
        </div>
        <Button variant="secondary" size="sm" leadingIcon={<RefreshCcw size={14} className={loading ? 'animate-spin' : ''} />} onClick={fetchFileAudits}>
          Tải lại
        </Button>
      </div>

      <div className="overflow-x-auto rounded-xl border border-line/60">
        <table className="w-full text-left text-xs">
          <thead className="bg-slate-50 text-[10px] font-extrabold uppercase text-slate-500 border-b border-line/60">
            <tr>
              <th className="px-4 py-3">HÀNH ĐỘNG</th>
              <th className="px-4 py-3">NGƯỜI THỰC HIỆN</th>
              <th className="px-4 py-3">FILE / TÀI NGUYÊN</th>
              <th className="px-4 py-3">IP ADDRESS</th>
              <th className="px-4 py-3">THỜI GIAN</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-line/40">
            {items.map((log: any) => (
              <tr key={log.id} className="hover:bg-slate-50/80">
                <td className="px-4 py-3 font-bold">
                  <span className={`inline-block px-2.5 py-0.5 rounded-full text-[10px] ${
                    log.action === 'FILE_DELETED'
                      ? 'bg-rose-50 text-rose-700 border border-rose-200'
                      : 'bg-emerald-50 text-emerald-700 border border-emerald-200'
                  }`}>
                    {log.action === 'FILE_DELETED' ? 'Xóa file đính kèm' : 'Tải file đính kèm'}
                  </span>
                </td>
                <td className="px-4 py-3">
                  <p className="font-bold text-slate-800">{log.actorUsername || 'Hệ thống'}</p>
                  {log.actorEmail && <p className="text-[10px] text-slate-400">{log.actorEmail}</p>}
                </td>
                <td className="px-4 py-3 font-mono text-[11px] text-slate-600">
                  {log.resourceType}: {log.resourceId || 'N/A'}
                </td>
                <td className="px-4 py-3 font-mono text-slate-500">{log.ipAddress || 'Không có'}</td>
                <td className="px-4 py-3 text-slate-500">{new Date(log.createdAt).toLocaleString('vi-VN')}</td>
              </tr>
            ))}
            {!items.length && !loading && (
              <tr>
                <td colSpan={5} className="p-8 text-center text-slate-400 font-medium">
                  Chưa có nhật ký kiểm toán tệp tin nào.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      {fileAudits && fileAudits.totalPages > 1 && (
        <div className="flex items-center justify-between text-xs text-slate-500 pt-2">
          <span>Trang <strong>{page + 1}</strong> / {fileAudits.totalPages}</span>
          <div className="flex gap-2">
            <Button variant="secondary" size="sm" disabled={page === 0 || loading} onClick={() => setPage(p => p - 1)}>
              Trước
            </Button>
            <Button variant="secondary" size="sm" disabled={page + 1 >= fileAudits.totalPages || loading} onClick={() => setPage(p => p + 1)}>
              Sau
            </Button>
          </div>
        </div>
      )}
    </div>
  )
}
