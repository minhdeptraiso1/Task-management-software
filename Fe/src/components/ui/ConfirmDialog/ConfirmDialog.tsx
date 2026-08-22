import { AlertTriangle } from 'lucide-react'
import { Button } from '../Button'
import { Modal } from '../Modal'

interface ConfirmDialogProps {
  open: boolean
  title: string
  description: string
  confirmLabel?: string
  loading?: boolean
  onCancel: () => void
  onConfirm: () => void
}

export function ConfirmDialog({ open, title, description, confirmLabel = 'Xác nhận', loading, onCancel, onConfirm }: ConfirmDialogProps) {
  return <Modal open={open} title={title} description={description} onClose={onCancel} showClose={false}>
    <div className="flex items-start gap-3 rounded-xl border border-danger/20 bg-danger/10 p-4 text-danger font-medium"><AlertTriangle className="mt-0.5 shrink-0" size={20} /><p className="text-sm">Hành động này có thể ảnh hưởng đến quyền truy cập và không thể tự hoàn tác.</p></div>
    <div className="mt-6 flex justify-end gap-3"><Button variant="secondary" onClick={onCancel}>Hủy</Button><Button variant="danger" loading={loading} onClick={onConfirm}>{confirmLabel}</Button></div>
  </Modal>
}
