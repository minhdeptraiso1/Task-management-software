import { useEffect, useState, useCallback } from 'react'
import { getProjectAttachmentUsage, getFileSecuritySummary, getAllProjectAttachments, deleteAttachment } from '../services/attachment.service'
import type { AttachmentUsage, FileSecuritySummary, AttachmentPage } from '../models/attachment.model'
import { AlertCircle, ShieldCheck, Database, FileCode, Check, Ban, RefreshCcw, Download, Trash2, FileText, Image as ImageIcon, Video, FileArchive, FileSpreadsheet, File, ChevronLeft, ChevronRight, Paperclip } from 'lucide-react'
import { Button } from '../../../components/ui'
import { downloadExcelFile } from '../../../services/apiClient'

export function ProjectAttachmentsTab({ projectId }: { projectId: string }) {
  const [usage, setUsage] = useState<AttachmentUsage | null>(null)
  const [security, setSecurity] = useState<FileSecuritySummary | null>(null)
  const [attachments, setAttachments] = useState<AttachmentPage | null>(null)
  const [filePage, setFilePage] = useState(0)
  const [loading, setLoading] = useState(true)
  const [fileLoading, setFileLoading] = useState(false)
  const [error, setError] = useState('')

  const loadFiles = useCallback(async (pageNum: number) => {
    setFileLoading(true)
    try {
      const data = await getAllProjectAttachments(projectId, pageNum, 10)
      setAttachments(data)
    } catch (err) {
      console.error('Lỗi tải danh sách file dự án:', err)
    } finally {
      setFileLoading(false)
    }
  }, [projectId])

  const loadData = useCallback(async (showLoading = true) => {
    if (showLoading) setLoading(true)
    setError('')
    try {
      const [usageData, securityData, filesData] = await Promise.all([
        getProjectAttachmentUsage(projectId),
        getFileSecuritySummary(projectId),
        getAllProjectAttachments(projectId, filePage, 10)
      ])
      setUsage(usageData)
      setSecurity(securityData)
      setAttachments(filesData)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Lỗi tải thông tin dung lượng và bảo mật file')
    } finally {
      if (showLoading) setLoading(false)
    }
  }, [projectId, filePage])

  useEffect(() => {
    void loadData(true)
  }, [loadData])

  const handlePageChange = (newPage: number) => {
    setFilePage(newPage)
    void loadFiles(newPage)
  }

  const handleDeleteFile = async (attachmentId: string) => {
    if (!confirm('Bạn có chắc chắn muốn xóa tệp tin này khỏi dự án?')) return
    try {
      await deleteAttachment(projectId, attachmentId)
      void loadData(false)
    } catch (err) {
      alert(err instanceof Error ? err.message : 'Xóa tệp tin thất bại')
    }
  }

  const formatBytes = (bytes: number, decimals = 2) => {
    if (bytes <= 0) return '0 Bytes'
    const k = 1024
    const dm = decimals < 0 ? 0 : decimals
    const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB']
    const i = Math.floor(Math.log(bytes) / Math.log(k))
    return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + ' ' + sizes[i]
  }

  const getFileIcon = (ext: string) => {
    const e = ext ? ext.toLowerCase() : ''
    if (['png', 'jpg', 'jpeg', 'gif', 'svg', 'webp'].includes(e)) return <ImageIcon size={16} className="text-emerald-500" />
    if (['pdf'].includes(e)) return <FileText size={16} className="text-rose-500" />
    if (['doc', 'docx', 'txt'].includes(e)) return <FileText size={16} className="text-sky-500" />
    if (['xls', 'xlsx', 'csv'].includes(e)) return <FileSpreadsheet size={16} className="text-emerald-600" />
    if (['zip', 'rar', '7z', 'tar', 'gz'].includes(e)) return <FileArchive size={16} className="text-amber-500" />
    if (['mp4', 'avi', 'mov', 'mkv'].includes(e)) return <Video size={16} className="text-purple-500" />
    return <File size={16} className="text-slate-400" />
  }

  if (loading) {
    return (
      <div className="flex h-64 items-center justify-center">
        <div className="flex flex-col items-center gap-3">
          <RefreshCcw className="h-8 w-8 animate-spin text-brand" />
          <p className="text-sm text-muted">Đang tải cấu hình lưu trữ & bảo mật...</p>
        </div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="rounded-xl border border-danger/20 bg-danger/5 p-4 text-sm text-danger flex items-start gap-3">
        <AlertCircle className="shrink-0 mt-0.5" size={18} />
        <div>
          <h4 className="font-bold">Không thể tải thông tin</h4>
          <p className="mt-1 opacity-90">{error}</p>
          <Button variant="secondary" size="sm" onClick={() => loadData(true)} className="mt-3">Thử lại</Button>
        </div>
      </div>
    )
  }

  const getProgressColor = (rate: number) => {
    if (rate >= 85) return 'bg-danger'
    if (rate >= 50) return 'bg-warning'
    return 'bg-success'
  }

  const getProgressTextColor = (rate: number) => {
    if (rate >= 85) return 'text-danger'
    if (rate >= 50) return 'text-warning'
    return 'text-success'
  }

  const getEntityLabel = (type: string) => {
    switch (type) {
      case 'TASK': return 'Nhiệm vụ'
      case 'BUG': return 'Lỗi QA'
      case 'COMMENT': return 'Bình luận Task'
      case 'BUG_COMMENT': return 'Bình luận Bug'
      case 'BUG_EVIDENCE': return 'Bằng chứng Bug'
      default: return type
    }
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h3 className="text-lg font-bold text-ink">Lưu trữ & Bảo mật File</h3>
          <p className="text-sm text-muted">Thông tin dung lượng lưu trữ dự án và các quy tắc bảo mật tệp đính kèm.</p>
        </div>
        <Button
          variant="secondary"
          size="sm"
          leadingIcon={<RefreshCcw size={14} />}
          onClick={() => loadData(false)}
        >
          Làm mới
        </Button>
      </div>

      <div className="grid gap-6 md:grid-cols-2">
        {/* Left Column: Storage Usage */}
        {usage && (
          <div className="rounded-2xl border border-brand-line/70 bg-white p-6 shadow-sm space-y-6 transition hover:shadow-md">
            <div className="flex items-center gap-3 border-b border-line/60 pb-4">
              <div className="rounded-lg bg-indigo-50 p-2 text-indigo-500">
                <Database size={20} />
              </div>
              <div>
                <h4 className="font-bold text-ink text-sm">Dung lượng lưu trữ dự án</h4>
                <p className="text-xs text-muted">Tổng dung lượng tệp đính kèm đã tải lên dự án này.</p>
              </div>
            </div>

            <div className="space-y-2">
              <div className="flex items-center justify-between text-xs font-semibold text-muted-dark">
                <span>Dung lượng đã dùng</span>
                <span className={`font-bold ${getProgressTextColor(usage.usageRate)}`}>
                  {usage.usageRate.toFixed(1)}% ({formatBytes(usage.usedBytes)} / {formatBytes(usage.maxBytes)})
                </span>
              </div>
              <div className="h-3 w-full rounded-full bg-slate-100 overflow-hidden">
                <div
                  className={`h-full rounded-full transition-all duration-500 ${getProgressColor(usage.usageRate)}`}
                  style={{ width: `${Math.min(usage.usageRate, 100)}%` }}
                />
              </div>
            </div>

            <div className="grid grid-cols-2 gap-4 pt-2 text-xs">
              <div className="rounded-xl bg-slate-50 p-3 border border-line/40">
                <span className="block text-muted mb-0.5">Dung lượng còn trống</span>
                <strong className="text-sm font-bold text-ink">{formatBytes(usage.remainingBytes)}</strong>
              </div>
              <div className="rounded-xl bg-slate-50 p-3 border border-line/40">
                <span className="block text-muted mb-0.5">Giới hạn tối đa</span>
                <strong className="text-sm font-bold text-ink">{formatBytes(usage.maxBytes)}</strong>
              </div>
            </div>
          </div>
        )}

        {/* Right Column: File Security Summary */}
        {security && (
          <div className="rounded-2xl border border-brand-line/70 bg-white p-6 shadow-sm space-y-6 transition hover:shadow-md">
            <div className="flex items-center gap-3 border-b border-line/60 pb-4">
              <div className="rounded-lg bg-emerald-50 p-2 text-emerald-500">
                <ShieldCheck size={20} />
              </div>
              <div>
                <h4 className="font-bold text-ink text-sm">Cấu hình Bảo mật & Xác thực</h4>
                <p className="text-xs text-muted">Quy tắc tải tệp đính kèm của hệ thống.</p>
              </div>
            </div>

            <div className="grid grid-cols-2 gap-4 text-xs">
              <div className="space-y-1">
                <span className="text-muted block">Dung lượng tối đa 1 file</span>
                <strong className="text-ink text-sm font-bold">{formatBytes(security.maxFileSizeBytes)}</strong>
              </div>
              <div className="space-y-1">
                <span className="text-muted block">Số lượng file tối đa / Đối tượng</span>
                <strong className="text-ink text-sm font-bold">{security.maxFilesPerEntity} tệp tin</strong>
              </div>
              <div className="space-y-1">
                <span className="text-muted block">Giới hạn dự án</span>
                <strong className="text-ink text-sm font-bold">{formatBytes(security.maxProjectStorageBytes)}</strong>
              </div>
              <div className="space-y-1">
                <span className="text-muted block">Lưu file đã xóa mềm</span>
                <strong className="text-ink text-sm font-bold">{security.keepDeletedFileDays} ngày</strong>
              </div>
            </div>

            {/* Allowed Whitelist */}
            <div className="space-y-2 border-t border-line/60 pt-4">
              <div className="flex items-center gap-1.5 text-xs font-semibold text-emerald-600">
                <Check size={14} className="stroke-[3px]" />
                <span>Định dạng tệp được phép (Whitelist)</span>
              </div>
              <div className="flex flex-wrap gap-1.5">
                {security.allowedExtensions.length > 0 ? (
                  security.allowedExtensions.map(ext => (
                    <span key={ext} className="inline-flex items-center rounded-lg bg-emerald-50 px-2 py-0.5 text-xs font-semibold text-emerald-700 border border-emerald-100">
                      {ext.toUpperCase()}
                    </span>
                  ))
                ) : (
                  <span className="text-xs text-muted italic">Mở tất cả định dạng</span>
                )}
              </div>
            </div>

            {/* Blocked Blacklist */}
            {security.blockedExtensions && security.blockedExtensions.length > 0 && (
              <div className="space-y-2 border-t border-line/60 pt-4">
                <div className="flex items-center gap-1.5 text-xs font-semibold text-rose-600">
                  <Ban size={14} className="stroke-[3px]" />
                  <span>Định dạng tệp bị cấm (Blacklist)</span>
                </div>
                <div className="flex flex-wrap gap-1.5">
                  {security.blockedExtensions.map(ext => (
                    <span key={ext} className="inline-flex items-center rounded-lg bg-rose-50 px-2 py-0.5 text-xs font-semibold text-rose-700 border border-rose-100 animate-enter">
                      {ext.toUpperCase()}
                    </span>
                  ))}
                </div>
              </div>
            )}
          </div>
        )}
      </div>

      {/* Project Files List Table Section */}
      <div className="rounded-2xl border border-line bg-white shadow-sm overflow-hidden">
        <div className="flex items-center justify-between border-b border-line px-6 py-4">
          <div className="flex items-center gap-2">
            <Paperclip className="text-brand" size={18} />
            <h4 className="font-bold text-ink text-sm">Danh sách tệp tin đính kèm trong Dự án</h4>
            <span className="rounded-full bg-brand-soft text-brand-dark px-2.5 py-0.5 text-xs font-extrabold">
              {attachments?.totalElements || 0}
            </span>
          </div>
        </div>

        {fileLoading ? (
          <div className="p-12 text-center text-xs text-muted">
            <RefreshCcw className="mx-auto h-6 w-6 animate-spin text-brand mb-2" />
            Đang tải danh sách file...
          </div>
        ) : !attachments || attachments.content.length === 0 ? (
          <div className="py-12 text-center text-muted text-xs">
            <Paperclip className="mx-auto text-muted/30 mb-2" size={32} />
            <p className="font-bold text-ink">Chưa có tệp tin đính kèm nào</p>
            <p className="mt-1">Dự án này chưa có tệp tin đính kèm nào được tải lên Task hoặc Bug.</p>
          </div>
        ) : (
          <>
            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs">
                <thead className="bg-slate-50 text-muted-dark border-b border-line font-bold">
                  <tr>
                    <th className="px-5 py-3">Tên tệp tin</th>
                    <th className="px-5 py-3">Thuộc thực thể</th>
                    <th className="px-5 py-3">Kích thước</th>
                    <th className="px-5 py-3">Người tải lên / Ngày</th>
                    <th className="px-5 py-3 text-center">Thao tác</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-line/60">
                  {attachments.content.map(att => (
                    <tr key={att.id} className="hover:bg-slate-50/80 transition">
                      <td className="px-5 py-3 font-semibold text-ink">
                        <div className="flex items-center gap-2 max-w-xs">
                          {getFileIcon(att.extension)}
                          <span className="truncate" title={att.originalFileName}>{att.originalFileName}</span>
                          <span className="rounded bg-slate-100 px-1.5 py-0.5 text-[10px] font-mono text-muted uppercase">
                            {att.extension}
                          </span>
                        </div>
                      </td>
                      <td className="px-5 py-3 text-muted-dark">
                        <span className="rounded-md bg-white border border-line px-2 py-1 text-[11px] font-medium shadow-2xs">
                          {getEntityLabel(att.entityType)}
                        </span>
                      </td>
                      <td className="px-5 py-3 font-mono text-muted-dark">
                        {formatBytes(att.sizeBytes)}
                      </td>
                      <td className="px-5 py-3 text-muted">
                        <div>
                          <p className="font-semibold text-ink">{att.uploadedByUsername || 'Hệ thống'}</p>
                          <p className="text-[10px]">{new Date(att.createdAt).toLocaleDateString('vi-VN')} {new Date(att.createdAt).toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' })}</p>
                        </div>
                      </td>
                      <td className="px-5 py-3 text-center">
                        <div className="flex items-center justify-center gap-1">
                          <Button
                            variant="ghost"
                            size="sm"
                            iconOnly
                            leadingIcon={<Download size={15} />}
                            title="Tải xuống"
                            aria-label="Tải xuống"
                            onClick={() => downloadExcelFile(att.downloadUrl, att.originalFileName)}
                          />
                          {att.canDelete && (
                            <Button
                              variant="ghost"
                              size="sm"
                              iconOnly
                              className="!text-danger hover:!bg-rose-50"
                              leadingIcon={<Trash2 size={15} />}
                              title="Xóa tệp tin"
                              aria-label="Xóa tệp tin"
                              onClick={() => handleDeleteFile(att.id)}
                            />
                          )}
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {/* Pagination Footer */}
            {attachments.totalPages > 1 && (
              <div className="flex items-center justify-between border-t border-line px-5 py-3 text-xs text-muted">
                <p>Trang {attachments.number + 1} / {attachments.totalPages}</p>
                <div className="flex gap-2">
                  <Button
                    variant="secondary"
                    size="sm"
                    disabled={attachments.first}
                    leadingIcon={<ChevronLeft size={14} />}
                    onClick={() => handlePageChange(attachments.number - 1)}
                  >
                    Trước
                  </Button>
                  <Button
                    variant="secondary"
                    size="sm"
                    disabled={attachments.last}
                    trailingIcon={<ChevronRight size={14} />}
                    onClick={() => handlePageChange(attachments.number + 1)}
                  >
                    Sau
                  </Button>
                </div>
              </div>
            )}
          </>
        )}
      </div>

      {/* Security Scanning Info Alert */}
      <div className="rounded-xl border border-line bg-brand-cream/40 p-4 text-xs text-muted-dark flex items-start gap-3">
        <FileCode className="shrink-0 mt-0.5 text-brand" size={18} />
        <div>
          <h4 className="font-bold text-ink">Quy trình kiểm tra an toàn tệp tin (Deep Verification)</h4>
          <p className="mt-1 leading-relaxed">
            Hệ thống áp dụng cơ chế xác minh sâu: Khi tệp tin được tải lên, ngoài việc kiểm tra phần mở rộng và định dạng MIME khai báo, hệ thống sẽ thực hiện phân tích <strong>chữ ký nhị phân (magic bytes)</strong> trực tiếp của nội dung tệp. Mọi hành vi sửa đổi phần mở rộng tệp tin không hợp lệ đều bị chặn để đảm bảo an toàn cho dự án.
          </p>
        </div>
      </div>
    </div>
  )
}
