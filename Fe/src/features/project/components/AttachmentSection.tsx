import { useState, useEffect, useCallback } from 'react'
import { Paperclip, Trash2, Download, FileText, Image as ImageIcon, Video, FileArchive, FileSpreadsheet, File } from 'lucide-react'
import { getAttachments, uploadAttachment, deleteAttachment } from '../services/attachment.service'
import type { Attachment, AttachmentEntityType } from '../models/attachment.model'
import { downloadExcelFile } from '../../../services/apiClient'
import { ConfirmDialog } from '../../../components/ui'

interface AttachmentSectionProps {
  projectId: string
  entityType: AttachmentEntityType
  entityId: string
  isEditable?: boolean
}

export default function AttachmentSection({
  projectId,
  entityType,
  entityId,
  isEditable = true
}: AttachmentSectionProps) {
  const [attachments, setAttachments] = useState<Attachment[]>([])
  const [loading, setLoading] = useState(false)
  const [uploading, setUploading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [deleteConfirm, setDeleteConfirm] = useState<{ open: boolean; attachmentId: string | null; fileName?: string }>({ open: false, attachmentId: null })

  const handleLoadAttachments = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const pageResponse = await getAttachments(projectId, entityType, entityId, 0, 100)
      setAttachments(pageResponse.content || [])
    } catch (err: any) {
      console.error(err)
      setError(err?.message || 'Không thể tải danh sách tệp đính kèm')
    } finally {
      setLoading(false)
    }
  }, [projectId, entityType, entityId])

  useEffect(() => {
    handleLoadAttachments()
  }, [handleLoadAttachments])

  const handleUploadFile = async (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0]
    if (!file) return

    setUploading(true)
    setError(null)
    try {
      await uploadAttachment(projectId, entityType, entityId, file)
      handleLoadAttachments()
    } catch (err: any) {
      console.error(err)
      setError(err?.message || 'Tải tệp tin lên thất bại')
    } finally {
      setUploading(false)
      // Reset input value to allow uploading same file again
      event.target.value = ''
    }
  }

  const handleConfirmDeleteFile = async () => {
    if (!deleteConfirm.attachmentId) return
    const id = deleteConfirm.attachmentId
    setDeleteConfirm({ open: false, attachmentId: null })
    try {
      await deleteAttachment(projectId, id)
      handleLoadAttachments()
    } catch (err: any) {
      console.error(err)
      setError(err?.message || 'Xóa tài liệu đính kèm thất bại')
    }
  }

  const getFileIcon = (contentType: string) => {
    const type = contentType.toLowerCase()
    if (type.startsWith('image/')) return <ImageIcon className="text-blue-500" size={18} />
    if (type.startsWith('video/')) return <Video className="text-purple-500" size={18} />
    if (type.includes('spreadsheet') || type.includes('excel') || type.includes('csv')) {
      return <FileSpreadsheet className="text-emerald-500" size={18} />
    }
    if (type.includes('zip') || type.includes('rar') || type.includes('tar') || type.includes('compressed')) {
      return <FileArchive className="text-amber-500" size={18} />
    }
    if (type.includes('pdf') || type.includes('document') || type.startsWith('text/')) {
      return <FileText className="text-sky-500" size={18} />
    }
    return <File className="text-slate-400" size={18} />
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h5 className="text-sm font-bold text-ink flex items-center gap-2">
          <Paperclip size={16} className="text-brand" />
          Tài liệu đính kèm ({attachments.length})
        </h5>

        {isEditable && (
          <label className={`inline-flex items-center gap-2 cursor-pointer rounded-lg bg-brand px-3.5 py-2 text-xs font-bold text-white shadow hover:bg-brand/90 transition-all ${uploading ? 'opacity-50 pointer-events-none' : ''}`}>
            {uploading ? 'Đang tải lên...' : 'Tải tệp lên'}
            <input
              type="file"
              className="hidden"
              onChange={handleUploadFile}
              disabled={uploading}
            />
          </label>
        )}
      </div>

      {error && (
        <div className="rounded-lg bg-rose-50 p-3 text-xs text-rose-600 border border-rose-200">
          {error}
        </div>
      )}

      {loading ? (
        <div className="text-center py-6">
          <p className="text-xs text-muted animate-pulse">Đang tải danh sách tài liệu...</p>
        </div>
      ) : attachments.length === 0 ? (
        <div className="rounded-xl border border-line bg-white p-8 text-center space-y-2">
          <Paperclip size={32} className="mx-auto text-muted/65" />
          <h6 className="font-bold text-ink text-xs">Chưa có tài liệu đính kèm</h6>
          <p className="text-[11px] text-muted max-w-xs mx-auto">
            Hỗ trợ hình ảnh chụp lỗi, tài liệu đặc tả (PDF, Word, Excel), hoặc các tệp nén zip/rar liên quan.
          </p>
        </div>
      ) : (
        <div className="border border-line rounded-xl overflow-hidden bg-white">
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse text-xs">
              <thead>
                <tr className="border-b border-line bg-canvas text-muted font-bold">
                  <th className="p-3">Tên File</th>
                  <th className="p-3">Kích thước</th>
                  <th className="p-3">Người tải lên</th>
                  <th className="p-3">Ngày tạo</th>
                  <th className="p-3 text-right">Thao tác</th>
                </tr>
              </thead>
              <tbody>
                {attachments.map(att => (
                  <tr key={att.id} className="border-b border-line last:border-0 hover:bg-canvas">
                    <td className="p-3 font-semibold text-ink flex items-center gap-2 max-w-[240px]">
                      {getFileIcon(att.contentType)}
                      <span className="truncate break-all" title={att.originalFileName}>
                        {att.originalFileName}
                      </span>
                    </td>
                    <td className="p-3 text-muted">
                      {att.sizeBytes > 1024 * 1024
                        ? `${(att.sizeBytes / (1024 * 1024)).toFixed(1)} MB`
                        : `${(att.sizeBytes / 1024).toFixed(1)} KB`}
                    </td>
                    <td className="p-3 text-muted">
                      {att.uploadedByUsername || 'Chưa rõ'}
                    </td>
                    <td className="p-3 text-muted">
                      {new Date(att.createdAt).toLocaleDateString('vi-VN')}
                    </td>
                    <td className="p-3 text-right space-x-2">
                      <button
                        type="button"
                        onClick={() => downloadExcelFile(att.downloadUrl, att.originalFileName)}
                        className="text-brand hover:underline font-bold inline-flex items-center gap-1"
                      >
                        <Download size={13} />
                        Tải về
                      </button>
                      {att.canDelete && isEditable && (
                        <button
                          type="button"
                          onClick={() => setDeleteConfirm({ open: true, attachmentId: att.id, fileName: att.originalFileName })}
                          className="text-rose-600 hover:underline font-bold inline-flex items-center gap-1 ml-2"
                        >
                          <Trash2 size={13} />
                          Xóa
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      <ConfirmDialog
        open={deleteConfirm.open}
        title="Xóa tệp tin đính kèm?"
        description={
          deleteConfirm.fileName
            ? `Tệp tin "${deleteConfirm.fileName}" sẽ bị xóa khỏi hệ thống. Bạn có chắc chắn muốn tiếp tục?`
            : 'Tệp tin đính kèm này sẽ bị xóa khỏi hệ thống. Bạn có chắc chắn muốn tiếp tục?'
        }
        confirmLabel="Xóa tệp tin"
        onCancel={() => setDeleteConfirm({ open: false, attachmentId: null })}
        onConfirm={handleConfirmDeleteFile}
      />
    </div>
  )
}
