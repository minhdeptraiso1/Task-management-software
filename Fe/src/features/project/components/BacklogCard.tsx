import { useState, type DragEvent } from 'react'
import { Pencil, Save, Trash2 } from 'lucide-react'
import { ActionItem, ActionMenu, Button, Input, Select, Modal } from '../../../components/ui'
import type { BacklogItem, BacklogItemStatus, BacklogItemType, BacklogPriority } from '../models/scrum.model'
import { backlogPriorityLabels, backlogStatusLabels, backlogTypeLabels } from '../models/scrum.model'

const itemTypes: BacklogItemType[] = ['EPIC', 'USER_STORY', 'FEATURE', 'TECHNICAL']
const priorities: BacklogPriority[] = ['URGENT', 'HIGH', 'MEDIUM', 'LOW']
const statuses: BacklogItemStatus[] = ['DRAFT', 'READY', 'IN_SPRINT', 'DONE', 'CANCELLED']

export function priorityClass(priority: BacklogPriority) {
  switch (priority) {
    case 'LOW': return 'bg-panel text-muted border border-line font-medium'
    case 'MEDIUM': return 'bg-info/10 text-info border border-info/20 font-medium'
    case 'HIGH': return 'bg-brand/10 text-brand-dark border border-brand/20 font-bold'
    case 'URGENT': return 'bg-danger/10 text-danger border border-danger/20 font-extrabold'
    default: return 'bg-panel text-muted border border-line'
  }
}

export function statusClass(status: BacklogItemStatus) {
  switch (status) {
    case 'DRAFT': return 'bg-brand/10 text-brand-dark border border-brand/30 font-semibold'
    case 'READY': return 'bg-info/10 text-info border border-info/20 font-semibold'
    case 'IN_SPRINT': return 'bg-info/10 text-info border border-info/30 font-semibold'
    case 'DONE': return 'bg-success/10 text-success border border-success/20 font-bold'
    case 'CANCELLED': return 'bg-panel text-muted border border-line line-through opacity-70'
    default: return 'bg-panel text-muted border border-line'
  }
}

export function typeClass(type: BacklogItemType) {
  if (type === 'EPIC') return 'bg-info/10 text-info border border-info/30 font-bold'
  if (type === 'FEATURE') return 'bg-brand/10 text-brand-dark border border-brand/20'
  if (type === 'USER_STORY') return 'bg-info/10 text-info border border-info/20 font-semibold'
  return 'bg-panel text-muted border border-line'
}

function priorityIndicatorClass(priority: BacklogPriority) {
  if (priority === 'URGENT') return 'bg-danger'
  if (priority === 'HIGH') return 'bg-brand'
  if (priority === 'MEDIUM') return 'bg-info'
  return 'bg-muted'
}

export interface BacklogCardProps {
  item: BacklogItem
  canManage: boolean
  dragging: boolean
  onDragStart: (event: DragEvent<HTMLElement>, item: BacklogItem) => void
  onDragEnd: () => void
  onUpdate: (id: string, data: { title?: string; description?: string; type?: BacklogItemType; priority?: BacklogPriority; storyPoints?: number }) => void
  onDelete: (id: string) => void
  onStatusChange: (id: string, status: BacklogItemStatus) => void
  onPriorityChange: (id: string, priority: BacklogPriority) => void
  onAskConfirm: (title: string, description: string, confirmLabel: string, onConfirm: () => void) => void
}

export function BacklogCard({
  item,
  canManage,
  dragging,
  onDragStart,
  onDragEnd,
  onUpdate,
  onDelete,
  onStatusChange,
  onPriorityChange,
  onAskConfirm,
}: BacklogCardProps) {
  const [editing, setEditing] = useState(false)
  const [title, setTitle] = useState(item.title)
  const [description, setDescription] = useState(item.description ?? '')
  const [type, setType] = useState<BacklogItemType>(item.type)
  const [storyPoints, setStoryPoints] = useState(item.storyPoints ?? 0)
  const [status, setStatus] = useState<BacklogItemStatus>(item.status)
  const [priority, setPriority] = useState<BacklogPriority>(item.priority)

  const save = () => {
    onUpdate(item.id, { title, description, type, storyPoints })
    if (status !== item.status) {
      onStatusChange(item.id, status)
    }
    if (priority !== item.priority) {
      onPriorityChange(item.id, priority)
    }
    setEditing(false)
  }

  // Filter valid statuses based on whether item is in a sprint
  const availableStatuses = statuses.filter(s => {
    if (s === item.status) return true
    if (item.sprintId) {
      return ['IN_SPRINT', 'DONE', 'CANCELLED'].includes(s)
    }
    return ['DRAFT', 'READY', 'CANCELLED'].includes(s)
  })

  return (
    <>
      <article
        draggable={canManage}
        onDragStart={event => onDragStart(event, item)}
        onDragEnd={onDragEnd}
        className={`group relative flex rounded-xl border bg-white transition-all duration-300 ${
          canManage ? 'cursor-grab active:cursor-grabbing' : ''
        } ${
          dragging 
            ? '!border-brand/40 !bg-brand/5 opacity-70 ring-4 ring-brand/10 scale-95 shadow-2xl z-10' 
            : 'hover:border-brand/30 hover:shadow-lg hover:-translate-y-0.5 border-slate-200'
        }`}
      >
        {/* Priority Indicator Bar */}
        <div className={`w-1.5 shrink-0 rounded-l-xl transition-colors ${priorityIndicatorClass(item.priority)} group-hover:opacity-90`} />

      <div className="flex flex-1 items-start gap-3 p-4">
        <div className="min-w-0 flex-1">
          {/* Vertical Metadata Layout requested by User */}
          <div className="mb-2.5 flex items-start justify-between gap-2 w-full">
            <div className="flex flex-col items-start gap-1.5 min-w-0 flex-1">
              {/* Row 1: Type + Story Points */}
              <div className="flex flex-wrap items-center gap-1.5">
                <span className={`rounded-full px-2.5 py-0.5 text-[11px] font-semibold tracking-wide ${typeClass(item.type)}`}>
                  {backlogTypeLabels[item.type]}
                </span>
                {item.storyPoints !== null && item.storyPoints > 0 && (
                  <div className="flex items-center gap-1.5 rounded-full bg-amber-50 px-2 py-0.5 text-[11px] font-bold text-amber-600 ring-1 ring-amber-500/20">
                    <span className="h-1.5 w-1.5 rounded-full bg-amber-500"></span>
                    {item.storyPoints} pt
                  </div>
                )}
              </div>

              {/* Row 2: Priority */}
              <div>
                <span className={`rounded-full px-2.5 py-0.5 text-[11px] font-semibold tracking-wide ${priorityClass(item.priority)}`}>
                  Ưu tiên: {backlogPriorityLabels[item.priority]}
                </span>
              </div>

              {/* Row 3: Status */}
              <div>
                <span className={`rounded-full px-2.5 py-0.5 text-[11px] font-semibold tracking-wide ${statusClass(item.status)}`}>
                  Trạng thái: {backlogStatusLabels[item.status]}
                </span>
              </div>
            </div>

            {canManage && !editing && (
              <div className="shrink-0 mr-2 mt-0.5">
                <ActionMenu>
                  <ActionItem onClick={() => setEditing(true)}>
                    <Pencil size={15} /> Sửa
                  </ActionItem>
                  <ActionItem danger onClick={() => onAskConfirm('Xóa backlog item?', `Backlog "${item.title}" sẽ bị xóa mềm khỏi dự án.`, 'Xóa backlog', () => onDelete(item.id))}>
                    <Trash2 size={15} /> Xóa
                  </ActionItem>
                </ActionMenu>
              </div>
            )}
          </div>

          <h4 className="text-[15px] font-bold text-slate-800 leading-snug transition-colors group-hover:text-brand truncate max-w-full" title={item.title}>
            {item.title}
          </h4>
          {item.description && (
            <p className="mt-1.5 line-clamp-2 text-[13px] leading-relaxed text-slate-500 break-all">{item.description}</p>
          )}

        </div>
      </div>
    </article>

      <Modal open={editing} onClose={() => setEditing(false)} title="Sửa Backlog Item" description="Cập nhật thông tin chi tiết của công việc này." showClose={false}>
        <div className="space-y-4">
          <Input label="Tiêu đề" aria-label="Tiêu đề backlog" value={title} onChange={event => setTitle(event.target.value)} />
          <div>
            <label className="mb-1.5 block text-sm font-medium text-ink">Mô tả</label>
            <textarea 
              aria-label="Mô tả backlog" 
              className="min-h-[100px] w-full rounded-lg border border-line bg-white px-3.5 py-2.5 text-sm outline-none transition-all focus:border-brand focus:ring-2 focus:ring-brand/20" 
              value={description} 
              onChange={event => setDescription(event.target.value)}
              placeholder="Thêm mô tả chi tiết..."
            />
          </div>
          <div className="grid gap-4 sm:grid-cols-2">
            <Select label="Loại công việc" aria-label="Loại backlog" value={type} onChange={event => setType(event.target.value as BacklogItemType)} options={itemTypes.map(item => ({ label: backlogTypeLabels[item], value: item }))} />
            <Input label="Story point" aria-label="Story point" type="number" min={0} value={storyPoints} onChange={event => setStoryPoints(Number(event.target.value))} />
          </div>
          <div className="grid gap-4 sm:grid-cols-2">
            <Select 
              label="Trạng thái"
              aria-label="Trạng thái item" 
              value={status} 
              onChange={event => setStatus(event.target.value as BacklogItemStatus)} 
              options={availableStatuses.map(s => ({ label: backlogStatusLabels[s], value: s }))} 
            />
            <Select 
              label="Mức độ ưu tiên"
              aria-label="Ưu tiên item" 
              value={priority} 
              onChange={event => setPriority(event.target.value as BacklogPriority)} 
              options={priorities.map(p => ({ label: backlogPriorityLabels[p], value: p }))} 
            />
          </div>
          <div className="mt-6 flex justify-end gap-3">
            <Button variant="secondary" onClick={() => setEditing(false)}>Hủy</Button>
            <Button leadingIcon={<Save size={16} />} onClick={save}>Lưu thay đổi</Button>
          </div>
        </div>
      </Modal>
    </>
  )
}
