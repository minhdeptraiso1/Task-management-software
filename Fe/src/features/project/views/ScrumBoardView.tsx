import { useState, type DragEvent, type FormEvent } from 'react'
import { CalendarDays, GripVertical, ListPlus, Play, Plus, RotateCcw, Target, Trophy } from 'lucide-react'
import { Button, Input, Modal, Select } from '../../../components/ui'
import type { BacklogItem, BacklogItemStatus, BacklogItemType, BacklogPriority, Sprint } from '../models/scrum.model'
import { backlogPriorityLabels, backlogStatusLabels, backlogTypeLabels, sprintStatusLabels } from '../models/scrum.model'

const itemTypes: BacklogItemType[] = ['USER_STORY', 'FEATURE', 'TECHNICAL', 'EPIC']
const priorities: BacklogPriority[] = ['LOW', 'MEDIUM', 'HIGH', 'URGENT']
const statuses: BacklogItemStatus[] = ['DRAFT', 'READY', 'IN_SPRINT', 'DONE', 'CANCELLED']

interface ScrumBoardViewProps {
  backlogItems: BacklogItem[]
  sprints: Sprint[]
  sprintItems: Record<string, BacklogItem[]>
  loading: boolean
  saving: boolean
  canManage: boolean
  onCreateBacklog: (data: { title: string; description: string; type: BacklogItemType; priority: BacklogPriority; storyPoints: number }) => void
  onCreateSprint: (data: { name: string; goal: string; startDate: string; endDate: string }) => void
  onMoveToSprint: (itemId: string, sprintId: string, sourceSprintId?: string | null) => void
  onMoveToBacklog: (itemId: string, sprintId: string) => void
  onStatusChange: (itemId: string, status: BacklogItemStatus) => void
  onPriorityChange: (itemId: string, priority: BacklogPriority) => void
  onStartSprint: (sprintId: string) => void
  onCompleteSprint: (sprintId: string) => void
  onCancelSprint: (sprintId: string) => void
}

function priorityClass(priority: BacklogPriority) {
  if (priority === 'URGENT') return 'bg-[#fff0ed] text-danger'
  if (priority === 'HIGH') return 'bg-[#fef3e2] text-brand-dark'
  if (priority === 'MEDIUM') return 'bg-[#eff6ff] text-info'
  return 'bg-panel text-muted'
}

function createDragPreview(title: string) {
  const preview = document.createElement('div')
  preview.textContent = title
  preview.style.position = 'fixed'
  preview.style.top = '-1000px'
  preview.style.left = '-1000px'
  preview.style.maxWidth = '280px'
  preview.style.padding = '10px 12px'
  preview.style.borderRadius = '12px'
  preview.style.background = '#1A1A1A'
  preview.style.color = '#FFFFFF'
  preview.style.font = '600 13px Inter, ui-sans-serif, system-ui, sans-serif'
  preview.style.boxShadow = '0 18px 44px rgba(26, 26, 26, 0.22)'
  preview.style.pointerEvents = 'none'
  preview.style.zIndex = '9999'
  document.body.appendChild(preview)
  return preview
}

function CreateBacklogModal({ open, saving, onClose, onSave }: { open: boolean; saving: boolean; onClose: () => void; onSave: ScrumBoardViewProps['onCreateBacklog'] }) {
  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [type, setType] = useState<BacklogItemType>('USER_STORY')
  const [priority, setPriority] = useState<BacklogPriority>('MEDIUM')
  const [storyPoints, setStoryPoints] = useState(0)

  const submit = (event: FormEvent) => {
    event.preventDefault()
    onSave({ title, description, type, priority, storyPoints })
    setTitle('')
    setDescription('')
  }

  return <Modal open={open} onClose={onClose} title="Tạo backlog item" description="Backlog mới sẽ nằm trong Product Backlog và có thể kéo vào Sprint.">
    <form className="space-y-4" onSubmit={submit}>
      <Input label="Tiêu đề" value={title} onChange={event => setTitle(event.target.value)} required placeholder="Người dùng đăng nhập bằng email" />
      <div>
        <label className="mb-2 block text-sm font-medium text-[#3f3f46]">Mô tả</label>
        <textarea className="min-h-24 w-full rounded-lg border border-line bg-white px-3.5 py-3 text-sm outline-none focus:border-brand focus:ring-2 focus:ring-brand/20" value={description} onChange={event => setDescription(event.target.value)} />
      </div>
      <div className="grid gap-4 sm:grid-cols-3">
        <Select label="Loại" value={type} onChange={event => setType(event.target.value as BacklogItemType)} options={itemTypes.map(item => ({ label: backlogTypeLabels[item], value: item }))} />
        <Select label="Ưu tiên" value={priority} onChange={event => setPriority(event.target.value as BacklogPriority)} options={priorities.map(item => ({ label: backlogPriorityLabels[item], value: item }))} />
        <Input label="Point" type="number" min={0} value={storyPoints} onChange={event => setStoryPoints(Number(event.target.value))} />
      </div>
      <div className="flex justify-end gap-3"><Button type="button" variant="secondary" onClick={onClose}>Hủy</Button><Button type="submit" loading={saving} leadingIcon={<ListPlus size={17} />}>Tạo item</Button></div>
    </form>
  </Modal>
}

function CreateSprintModal({ open, saving, onClose, onSave }: { open: boolean; saving: boolean; onClose: () => void; onSave: ScrumBoardViewProps['onCreateSprint'] }) {
  const [name, setName] = useState('')
  const [goal, setGoal] = useState('')
  const [startDate, setStartDate] = useState('')
  const [endDate, setEndDate] = useState('')

  const submit = (event: FormEvent) => {
    event.preventDefault()
    onSave({ name, goal, startDate, endDate })
    setName('')
    setGoal('')
  }

  return <Modal open={open} onClose={onClose} title="Tạo Sprint" description="Sprint mới ở trạng thái Lên kế hoạch và có thể nhận backlog item.">
    <form className="space-y-4" onSubmit={submit}>
      <Input label="Tên Sprint" value={name} onChange={event => setName(event.target.value)} required placeholder="Sprint 1" />
      <Input label="Mục tiêu" value={goal} onChange={event => setGoal(event.target.value)} placeholder="Hoàn thiện login và quản lý tài khoản" />
      <div className="grid gap-4 sm:grid-cols-2">
        <Input label="Ngày bắt đầu" type="date" value={startDate} onChange={event => setStartDate(event.target.value)} />
        <Input label="Ngày kết thúc" type="date" value={endDate} onChange={event => setEndDate(event.target.value)} />
      </div>
      <div className="flex justify-end gap-3"><Button type="button" variant="secondary" onClick={onClose}>Hủy</Button><Button type="submit" loading={saving} leadingIcon={<Target size={17} />}>Tạo Sprint</Button></div>
    </form>
  </Modal>
}

function BacklogCard({
  item,
  canManage,
  dragging,
  onDragStart,
  onDragEnd,
  onStatusChange,
  onPriorityChange,
}: {
  item: BacklogItem
  canManage: boolean
  dragging: boolean
  onDragStart: (event: DragEvent<HTMLElement>, item: BacklogItem) => void
  onDragEnd: () => void
  onStatusChange: ScrumBoardViewProps['onStatusChange']
  onPriorityChange: ScrumBoardViewProps['onPriorityChange']
}) {
  return <article
    className={`group rounded-xl border border-line bg-white p-4 shadow-sm transition hover:-translate-y-0.5 hover:border-brand hover:shadow-md ${dragging ? 'opacity-45 ring-2 ring-brand/20' : ''}`}
  >
    <div className="flex items-start gap-2">
      <button
        type="button"
        draggable={canManage}
        aria-label={`Kéo backlog item ${item.title}`}
        disabled={!canManage}
        onDragStart={event => onDragStart(event, item)}
        onDragEnd={onDragEnd}
        className="mt-0.5 shrink-0 rounded-md p-1 text-muted opacity-60 transition hover:bg-panel hover:text-brand disabled:cursor-not-allowed disabled:opacity-30 enabled:cursor-grab enabled:active:cursor-grabbing"
      >
        <GripVertical size={18} />
      </button>
      <div className="min-w-0 flex-1">
        <div className="mb-2 flex flex-wrap gap-1.5">
          <span className="rounded-full bg-panel px-2 py-0.5 text-[11px] font-semibold text-muted">{backlogTypeLabels[item.type]}</span>
          <span className={`rounded-full px-2 py-0.5 text-[11px] font-semibold ${priorityClass(item.priority)}`}>{backlogPriorityLabels[item.priority]}</span>
          {item.storyPoints !== null && <span className="rounded-full bg-[#fef3e2] px-2 py-0.5 text-[11px] font-semibold text-brand-dark">{item.storyPoints} pt</span>}
        </div>
        <h4 className="font-semibold leading-5 text-ink">{item.title}</h4>
        {item.description && <p className="mt-2 line-clamp-2 text-xs leading-5 text-muted">{item.description}</p>}
        <div className="mt-3 grid gap-2 sm:grid-cols-2">
          <Select aria-label="Trạng thái item" value={item.status} onChange={event => onStatusChange(item.id, event.target.value as BacklogItemStatus)} disabled={!canManage} options={statuses.map(status => ({ label: backlogStatusLabels[status], value: status }))} />
          <Select aria-label="Ưu tiên item" value={item.priority} onChange={event => onPriorityChange(item.id, event.target.value as BacklogPriority)} disabled={!canManage} options={priorities.map(priority => ({ label: backlogPriorityLabels[priority], value: priority }))} />
        </div>
      </div>
    </div>
  </article>
}

export function ScrumBoardView({
  backlogItems,
  sprints,
  sprintItems,
  loading,
  saving,
  canManage,
  onCreateBacklog,
  onCreateSprint,
  onMoveToSprint,
  onMoveToBacklog,
  onStatusChange,
  onPriorityChange,
  onStartSprint,
  onCompleteSprint,
  onCancelSprint,
}: ScrumBoardViewProps) {
  const [backlogOpen, setBacklogOpen] = useState(false)
  const [sprintOpen, setSprintOpen] = useState(false)
  const [dragOver, setDragOver] = useState<string | null>(null)
  const [draggingId, setDraggingId] = useState<string | null>(null)

  const getPayload = (event: DragEvent) => JSON.parse(event.dataTransfer.getData('application/json')) as { itemId: string; sprintId: string | null }
  const handleCardDragStart = (event: DragEvent<HTMLElement>, item: BacklogItem) => {
    event.dataTransfer.effectAllowed = 'move'
    event.dataTransfer.setData('application/json', JSON.stringify({ itemId: item.id, sprintId: item.sprintId }))
    setDraggingId(item.id)

    const preview = createDragPreview(item.title)
    event.dataTransfer.setDragImage(preview, 16, 16)
    window.requestAnimationFrame(() => preview.remove())
  }
  const handleCardDragEnd = () => {
    setDraggingId(null)
    setDragOver(null)
  }

  return <section className="space-y-4">
    <div className="flex flex-col gap-3 rounded-xl border border-line bg-canvas p-4 sm:flex-row sm:items-center sm:justify-between">
      <div>
        <h3 className="font-bold">Sprint Board</h3>
        <p className="mt-1 text-sm text-muted">Kéo backlog item vào Sprint để lập kế hoạch; kéo về Product Backlog để gỡ khỏi Sprint.</p>
      </div>
      {canManage && <div className="flex flex-wrap gap-2">
        <Button variant="secondary" leadingIcon={<ListPlus size={17} />} onClick={() => setBacklogOpen(true)}>Tạo item</Button>
        <Button leadingIcon={<Plus size={17} />} onClick={() => setSprintOpen(true)}>Tạo Sprint</Button>
      </div>}
    </div>

    <div className={`flex gap-4 overflow-x-auto pb-3 ${loading ? 'opacity-50' : ''}`}>
      <div
        onDragOver={event => { event.preventDefault(); setDragOver('backlog') }}
        onDragLeave={() => setDragOver(null)}
        onDrop={event => {
          event.preventDefault()
          setDragOver(null)
          setDraggingId(null)
          const payload = getPayload(event)
          if (payload.sprintId) onMoveToBacklog(payload.itemId, payload.sprintId)
        }}
        className={`min-h-[560px] w-[340px] shrink-0 rounded-2xl border bg-[#f8fafc] p-3 transition ${dragOver === 'backlog' ? 'border-brand ring-2 ring-brand/20' : 'border-line'}`}
      >
        <div className="mb-3 flex items-center justify-between">
          <div><p className="font-bold">Product Backlog</p><p className="text-xs text-muted">{backlogItems.length} item chưa vào sprint</p></div>
          <RotateCcw size={18} className="text-brand" />
        </div>
        <div className="space-y-3">
          {backlogItems.map(item => <BacklogCard key={item.id} item={item} canManage={canManage} dragging={draggingId === item.id} onDragStart={handleCardDragStart} onDragEnd={handleCardDragEnd} onStatusChange={onStatusChange} onPriorityChange={onPriorityChange} />)}
          {!backlogItems.length && <p className="rounded-xl border border-dashed border-line p-6 text-center text-sm text-muted">Thả item từ Sprint về đây.</p>}
        </div>
      </div>

      {sprints.map(sprint => (
        <div
          key={sprint.id}
          onDragOver={event => { event.preventDefault(); setDragOver(sprint.id) }}
          onDragLeave={() => setDragOver(null)}
          onDrop={event => {
            event.preventDefault()
            setDragOver(null)
            setDraggingId(null)
            const payload = getPayload(event)
            if (payload.sprintId !== sprint.id) onMoveToSprint(payload.itemId, sprint.id, payload.sprintId)
          }}
          className={`min-h-[560px] w-[360px] shrink-0 rounded-2xl border bg-white p-3 transition ${dragOver === sprint.id ? 'border-brand ring-2 ring-brand/20' : 'border-line'}`}
        >
          <div className="mb-3 rounded-xl bg-brand-black p-4 text-white">
            <div className="flex items-start justify-between gap-3">
              <div><p className="font-bold">{sprint.name}</p><p className="mt-1 text-xs text-white/55">{sprint.goal || 'Chưa có mục tiêu sprint'}</p></div>
              <span className="rounded-full bg-brand/15 px-2.5 py-1 text-xs font-semibold text-accent">{sprintStatusLabels[sprint.status]}</span>
            </div>
            <div className="mt-3 flex items-center gap-2 text-xs text-white/55"><CalendarDays size={14} />{sprint.startDate || '...'} → {sprint.endDate || '...'}</div>
            {canManage && <div className="mt-3 flex flex-wrap gap-2">
              <Button size="sm" variant="secondary" leadingIcon={<Play size={14} />} disabled={sprint.status !== 'PLANNING'} onClick={() => onStartSprint(sprint.id)}>Start</Button>
              <Button size="sm" variant="secondary" leadingIcon={<Trophy size={14} />} disabled={sprint.status !== 'ACTIVE'} onClick={() => onCompleteSprint(sprint.id)}>Done</Button>
              <Button size="sm" variant="ghost" className="!text-white/70 hover:!bg-white/10" disabled={sprint.status === 'COMPLETED' || sprint.status === 'CANCELLED'} onClick={() => onCancelSprint(sprint.id)}>Hủy</Button>
            </div>}
          </div>
          <div className="space-y-3">
            {(sprintItems[sprint.id] ?? []).map(item => <BacklogCard key={item.id} item={item} canManage={canManage} dragging={draggingId === item.id} onDragStart={handleCardDragStart} onDragEnd={handleCardDragEnd} onStatusChange={onStatusChange} onPriorityChange={onPriorityChange} />)}
            {!(sprintItems[sprint.id] ?? []).length && <p className="rounded-xl border border-dashed border-line p-6 text-center text-sm text-muted">Kéo backlog item vào Sprint này.</p>}
          </div>
        </div>
      ))}
    </div>

    <CreateBacklogModal open={backlogOpen} saving={saving} onClose={() => setBacklogOpen(false)} onSave={data => { onCreateBacklog(data); setBacklogOpen(false) }} />
    <CreateSprintModal open={sprintOpen} saving={saving} onClose={() => setSprintOpen(false)} onSave={data => { onCreateSprint(data); setSprintOpen(false) }} />
  </section>
}
