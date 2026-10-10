import { useEffect, useState, type FormEvent } from 'react'
import { Plus } from 'lucide-react'
import { Button, Input, Modal, Select, Textarea } from '../../../components/ui'
import type { User } from '../../user/models/user.model'

export interface ProjectCreateFormData {
  code: string
  name: string
  description: string
  startDate: string
  endDate: string
  ownerUserId?: string
}

interface Props {
  open: boolean
  saving: boolean
  isAdmin: boolean
  managerCandidates: User[]
  managerCandidateLoading: boolean
  onManagerSearch: (keyword: string) => void
  onClose: () => void
  onSave: (data: ProjectCreateFormData) => void
}

export function ProjectCreateModal({
  open,
  saving,
  isAdmin,
  managerCandidates,
  managerCandidateLoading,
  onManagerSearch,
  onClose,
  onSave,
}: Props) {
  const [code, setCode] = useState('')
  const [name, setName] = useState('')
  const [description, setDescription] = useState('')
  const [startDate, setStartDate] = useState('')
  const [endDate, setEndDate] = useState('')
  const [managerKeyword, setManagerKeyword] = useState('')
  const [ownerUserId, setOwnerUserId] = useState('')

  useEffect(() => {
    if (!open) return
    setCode('')
    setName('')
    setDescription('')
    setStartDate('')
    setEndDate('')
    setManagerKeyword('')
    setOwnerUserId('')
  }, [open])

  const submit = (event: FormEvent) => {
    event.preventDefault()
    onSave({ code, name, description, startDate, endDate, ownerUserId: isAdmin ? ownerUserId : undefined })
  }

  return <Modal
    open={open}
    onClose={onClose}
    title="Tạo dự án mới"
    description={isAdmin ? 'Chọn một tài khoản quản lý để nhận dự án với vai trò OWNER.' : 'Bạn sẽ tự động là OWNER của dự án.'}
    showClose={false}
  >
    <form className="space-y-4" onSubmit={submit}>
      <div className="grid gap-4 sm:grid-cols-2">
        <Input label="Mã dự án" value={code} onChange={event => setCode(event.target.value)} required placeholder="BICAS-ERP" />
        <Input label="Tên dự án" value={name} onChange={event => setName(event.target.value)} required placeholder="Hệ thống ERP nội bộ" />
      </div>
      <Textarea label="Mô tả" value={description} onChange={event => setDescription(event.target.value)} placeholder="Mục tiêu, phạm vi, ghi chú..." />
      <div className="grid gap-4 sm:grid-cols-2">
        <Input label="Ngày bắt đầu" type="date" value={startDate} onChange={event => setStartDate(event.target.value)} />
        <Input label="Ngày kết thúc" type="date" value={endDate} onChange={event => setEndDate(event.target.value)} />
      </div>
      {isAdmin && <div className="space-y-3 rounded-xl border border-brand/20 bg-brand/5 p-4">
        <div>
          <p className="text-sm font-semibold text-ink">Quản lý nhận dự án</p>
          <p className="mt-1 text-sm text-muted">Tài khoản được chọn sẽ là OWNER và chịu trách nhiệm điều phối dự án.</p>
        </div>
        <div className="flex items-end gap-2">
          <Input
            label="Tìm theo tên hoặc email"
            value={managerKeyword}
            onChange={event => setManagerKeyword(event.target.value)}
            placeholder="Nhập thông tin quản lý"
          />
          <Button type="button" variant="secondary" loading={managerCandidateLoading} onClick={() => onManagerSearch(managerKeyword)}>
            Tìm
          </Button>
        </div>
        <Select
          label="Tài khoản quản lý"
          value={ownerUserId}
          onChange={event => setOwnerUserId(event.target.value)}
          required
          options={[
            { value: '', label: managerCandidates.length ? 'Chọn quản lý nhận dự án' : 'Chưa có kết quả phù hợp' },
            ...managerCandidates.map(manager => ({ value: manager.id, label: `${manager.username} — ${manager.email}` })),
          ]}
        />
      </div>}
      <div className="flex justify-end gap-3 pt-2">
        <Button type="button" variant="secondary" onClick={onClose}>Hủy</Button>
        <Button type="submit" loading={saving} leadingIcon={<Plus size={17} />}>Tạo dự án</Button>
      </div>
    </form>
  </Modal>
}
