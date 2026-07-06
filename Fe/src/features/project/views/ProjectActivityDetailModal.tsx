import { useEffect, useState } from 'react'
import { Modal } from '../../../components/ui'
import { getProjectActivityById } from '../services/project.service'
import type { ProjectActivityDetailResponse } from '../models/project.model'
import { Activity, Clock, User, FileText } from 'lucide-react'

const formatValue = (val: any): string => {
  if (val === null || val === undefined) return 'Trống'
  if (typeof val === 'boolean') return val ? 'Có' : 'Không'
  if (typeof val === 'object') return JSON.stringify(val)
  const str = String(val)
  if (/^[A-Z_]+$/.test(str) && str.length > 2) {
    return str.split('_').map(w => w.charAt(0) + w.slice(1).toLowerCase()).join(' ')
  }
  return str
}

const formatKey = (key: string): string => {
  const labels: Record<string, string> = {
    title: 'Tiêu đề',
    type: 'Loại',
    priority: 'Mức độ ưu tiên',
    status: 'Trạng thái',
    storyPoints: 'Story Points',
    description: 'Mô tả',
    name: 'Tên',
    goal: 'Mục tiêu',
    startDate: 'Ngày bắt đầu',
    endDate: 'Ngày kết thúc',
    role: 'Vai trò'
  }
  if (labels[key]) return labels[key]
  return key.replace(/([A-Z])/g, ' $1').replace(/^./, str => str.toUpperCase())
}

function JsonDataViewer({ jsonStr }: { jsonStr: string }) {
  try {
    const data = JSON.parse(jsonStr)
    if (!data || typeof data !== 'object') {
      return <span>{String(data)}</span>
    }
    
    return (
      <ul className="space-y-2">
        {Object.entries(data).map(([key, value]) => (
          <li key={key} className="flex flex-col sm:flex-row sm:gap-2">
            <span className="font-semibold text-muted min-w-[130px] shrink-0">&bull; {formatKey(key)}:</span>
            <span className="text-ink font-medium break-words">{formatValue(value)}</span>
          </li>
        ))}
      </ul>
    )
  } catch (e) {
    return <span className="text-rose-500">Dữ liệu không hợp lệ</span>
  }
}

interface Props {
  open: boolean
  projectId: string
  activityId: string | null
  onClose: () => void
}

export function ProjectActivityDetailModal({ open, projectId, activityId, onClose }: Props) {
  const [detail, setDetail] = useState<ProjectActivityDetailResponse | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    if (open && activityId) {
      setLoading(true)
      setError('')
      getProjectActivityById(projectId, activityId)
        .then(setDetail)
        .catch(err => setError(err instanceof Error ? err.message : 'Lỗi tải chi tiết hoạt động'))
        .finally(() => setLoading(false))
    } else {
      setDetail(null)
    }
  }, [open, activityId, projectId])

  return (
    <Modal open={open} onClose={onClose} title="Chi tiết hoạt động" size="lg">
      <div className="space-y-4">
        {loading ? (
          <div className="grid h-32 place-items-center">
            <span className="size-8 animate-spin rounded-full border-4 border-brand border-r-transparent" />
          </div>
        ) : error ? (
          <div className="rounded-lg bg-rose-50 p-4 text-center text-rose-600">{error}</div>
        ) : detail ? (
          <>
            <div className="flex items-center gap-3 rounded-lg border border-line bg-canvas p-4">
              <div className="grid size-10 place-items-center rounded-full bg-blue-100 text-blue-600">
                <Activity size={20} />
              </div>
              <div>
                <p className="font-semibold text-ink">{detail.displayMessage}</p>
                <div className="mt-1 flex items-center gap-4 text-sm text-muted">
                  <span className="flex items-center gap-1"><User size={14} /> {detail.performedByUsername}</span>
                  <span className="flex items-center gap-1"><Clock size={14} /> {new Date(detail.createdAt).toLocaleString('vi-VN')}</span>
                </div>
              </div>
            </div>

            <div className="grid gap-4 sm:grid-cols-2">
              <div className="rounded-lg border border-line p-4">
                <h4 className="flex items-center gap-2 font-semibold text-muted">
                  <FileText size={16} /> Dữ liệu cũ
                </h4>
                <div className="mt-2 max-h-60 overflow-y-auto rounded bg-canvas p-4 text-sm text-ink">
                  {detail.oldValueJson ? (
                    <JsonDataViewer jsonStr={detail.oldValueJson} />
                  ) : (
                    <span className="italic text-muted">Không có dữ liệu</span>
                  )}
                </div>
              </div>

              <div className="rounded-lg border border-line p-4">
                <h4 className="flex items-center gap-2 font-semibold text-muted">
                  <FileText size={16} /> Dữ liệu mới
                </h4>
                <div className="mt-2 max-h-60 overflow-y-auto rounded bg-canvas p-4 text-sm text-ink">
                  {detail.newValueJson ? (
                    <JsonDataViewer jsonStr={detail.newValueJson} />
                  ) : (
                    <span className="italic text-muted">Không có dữ liệu</span>
                  )}
                </div>
              </div>
            </div>
          </>
        ) : null}
      </div>
    </Modal>
  )
}
