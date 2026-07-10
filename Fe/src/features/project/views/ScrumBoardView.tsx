import { useEffect, useState, type ChangeEvent, type DragEvent, type FormEvent } from 'react'
import { CalendarDays, ChevronLeft, Clock3, Download, FolderKanban, GripVertical, Import, ListPlus, MessageSquare, Pencil, Play, Plus, Save, Target, Trash2, Trophy, UserRound, X, BarChart3, CheckCheck, ShieldAlert, AlertTriangle } from 'lucide-react'
import { ActionMenu, ActionItem, Button, ConfirmDialog, Input, Modal, Select } from '../../../components/ui'
import { SprintStatisticsView } from '../components/SprintStatisticsView'
import { SprintClosingView } from '../components/SprintClosingView'
import { SprintProgressView } from '../components/SprintProgressView'
import { BacklogCard, priorityClass, typeClass, statusClass } from '../components/BacklogCard'

function UserStoryHorizontalCard({
  item,
  isActive,
  taskCount,
  onClick
}: {
  item: BacklogItem
  isActive: boolean
  taskCount: number
  onClick: () => void
}) {
  return (
    <button
      type="button"
      className={`h-[120px] w-[280px] shrink-0 rounded-xl border p-3 text-left transition hover:border-brand flex flex-col justify-between ${isActive ? 'border-brand bg-brand-soft shadow-sm' : 'border-line bg-white hover:bg-slate-50'}`}
      onClick={onClick}
    >
      <div className="flex items-start justify-between gap-3 w-full min-h-0">
        <p className="line-clamp-2 font-semibold text-ink leading-snug">{item.title}</p>
        <span className={`shrink-0 rounded-full px-2 py-1 text-xs font-bold ${isActive ? 'bg-brand/20 text-brand-dark' : 'bg-slate-100 text-slate-500'}`}>{taskCount}</span>
      </div>
      <div className="mt-2 flex flex-wrap gap-1.5 shrink-0">
        <span className={`rounded-md px-2 py-0.5 text-[11px] font-semibold tracking-wide ${typeClass(item.type)}`}>{backlogTypeLabels[item.type]}</span>
        <span className={`rounded-md px-2 py-0.5 text-[11px] font-semibold tracking-wide ${priorityClass(item.priority)}`}>{backlogPriorityLabels[item.priority]}</span>
        <span className={`rounded-md px-2 py-0.5 text-[11px] font-semibold tracking-wide ${statusClass(item.status)}`}>{backlogStatusLabels[item.status]}</span>
        {item.storyPoints !== null && item.storyPoints > 0 && (
          <span className="rounded-md bg-amber-50 px-2 py-0.5 text-[11px] font-bold text-amber-600 ring-1 ring-amber-500/20">{item.storyPoints} pt</span>
        )}
      </div>
    </button>
  )
}
import type { BacklogItem, BacklogItemStatus, BacklogItemType, BacklogPriority, Sprint, SprintCapacityResponse, SprintHealthResponse, SprintRiskResponse, SprintProgress } from '../models/scrum.model'
import { backlogPriorityLabels, backlogStatusLabels, backlogTypeLabels, sprintStatusLabels } from '../models/scrum.model'
import type { KanbanBoard, KanbanTask, SprintBurndown, SprintTaskStatistics, Task, TaskCommentPage, TaskImportResult, TaskPriority, TaskStatus, TaskTimeLogPage, TaskTimeSummary, TaskType, TaskDependency, TaskRisk, TaskRiskSummary } from '../models/task.model'
import { taskPriorityLabels, taskStatusLabels, taskTypeLabels, taskRiskLevelLabels } from '../models/task.model'
import type { ProjectMember } from '../models/project.model'

const itemTypes: BacklogItemType[] = ['USER_STORY', 'FEATURE', 'TECHNICAL', 'EPIC']
const priorities: BacklogPriority[] = ['LOW', 'MEDIUM', 'HIGH', 'URGENT']
const taskTypes: TaskType[] = ['DEVELOPMENT', 'TESTING', 'DESIGN', 'DOCUMENTATION', 'RESEARCH', 'DEVOPS', 'OTHER']
const taskPriorities: TaskPriority[] = ['LOW', 'MEDIUM', 'HIGH', 'URGENT']
type AskConfirm = (title: string, description: string, confirmLabel: string, onConfirm: () => void) => void

interface ScrumBoardViewProps {
  projectId: string
  backlogItems: BacklogItem[]
  sprints: Sprint[]
  sprintItems: Record<string, BacklogItem[]>
  kanbanBoards: Record<string, KanbanBoard>
  selectedSprintId: string | null
  sprintStatistics: SprintTaskStatistics | null
  sprintBurndown: SprintBurndown | null
  sprintCapacity: SprintCapacityResponse | null
  sprintHealth: SprintHealthResponse | null
  sprintRisks: SprintRiskResponse[] | null
  sprintProgress: SprintProgress | null
  sprintTaskRiskSummary: TaskRiskSummary | null
  selectedTask: Task | null
  taskComments: TaskCommentPage
  taskTimeLogs: TaskTimeLogPage
  taskTimeSummary: TaskTimeSummary | null
  taskImportResult: TaskImportResult | null
  taskDependencies: TaskDependency[]
  taskRisk: TaskRisk | null
  members: ProjectMember[]
  loading: boolean
  taskDetailLoading: boolean
  saving: boolean
  canManage: boolean
  onCreateBacklog: (data: { title: string; description: string; type: BacklogItemType; priority: BacklogPriority; storyPoints: number }) => void
  onCreateSprint: (data: { name: string; goal: string; startDate: string; endDate: string }) => void
  onUpdateBacklog: (itemId: string, data: { title?: string; description?: string; type?: BacklogItemType; priority?: BacklogPriority; storyPoints?: number }) => void
  onDeleteBacklog: (itemId: string) => void
  onUpdateSprint: (sprintId: string, data: { name?: string; goal?: string; startDate?: string; endDate?: string }) => void
  onDeleteSprint: (sprintId: string) => void
  onMoveToSprint: (itemId: string, sprintId: string, sourceSprintId?: string | null) => void
  onMoveToBacklog: (itemId: string, sprintId: string) => void
  onStatusChange: (itemId: string, status: BacklogItemStatus) => void
  onPriorityChange: (itemId: string, priority: BacklogPriority) => void
  onStartSprint: (sprintId: string) => void
  onCompleteSprint: (sprintId: string) => void
  onCancelSprint: (sprintId: string) => void
  onSelectSprint: (sprintId: string) => void
  onCreateTask: (data: { backlogItemId: string; title: string; description?: string; type?: TaskType; priority?: TaskPriority; assigneeUserId?: string; estimatedMinutes?: number; startDate?: string; dueDate?: string }) => void
  onUpdateTask: (taskId: string, data: { title?: string; description?: string; type?: TaskType; priority?: TaskPriority; estimatedMinutes?: number; startDate?: string; dueDate?: string }) => void
  onDeleteTask: (taskId: string) => void
  onTaskStatusChange: (taskId: string, status: TaskStatus, position?: number) => void
  onAssignTask: (taskId: string, assigneeUserId: string) => void
  onOpenTask: (taskId: string) => void
  onCloseTask: () => void
  onAddDependency: (taskId: string, dependsOnTaskId: string) => void
  onRemoveDependency: (taskId: string, dependencyId: string) => void
  onUnblockTask: (taskId: string, targetStatus: TaskStatus) => void
  onCreateTaskComment: (taskId: string, content: string, parentCommentId?: string) => void
  onUpdateTaskComment: (taskId: string, commentId: string, content: string) => void
  onDeleteTaskComment: (taskId: string, commentId: string) => void
  onCreateTaskTimeLog: (taskId: string, data: { workDate: string; minutes: number; description?: string }) => void
  onUpdateTaskTimeLog: (taskId: string, timeLogId: string, data: { workDate?: string; minutes?: number; description?: string }) => void
  onDeleteTaskTimeLog: (taskId: string, timeLogId: string) => void
  onDownloadTaskTemplate: (sprintId: string) => void
  onImportTasks: (sprintId: string, file: File) => void
  onClearTaskImportResult: () => void
  onRefreshStatistics?: (sprintId: string) => void
}

function taskPriorityClass(priority: TaskPriority) {
  if (priority === 'URGENT') return 'bg-red-50 text-red-600 ring-1 ring-red-500/20'
  if (priority === 'HIGH') return 'bg-orange-50 text-orange-600 ring-1 ring-orange-500/20'
  if (priority === 'MEDIUM') return 'bg-blue-50 text-blue-600 ring-1 ring-blue-500/20'
  return 'bg-slate-50 text-slate-600 ring-1 ring-slate-500/20'
}

function taskPriorityIndicatorClass(priority: TaskPriority) {
  if (priority === 'URGENT') return 'bg-red-500'
  if (priority === 'HIGH') return 'bg-orange-500'
  if (priority === 'MEDIUM') return 'bg-blue-500'
  return 'bg-slate-400'
}

function formatMinutes(minutes?: number | null) {
  if (!minutes) return '0h'
  const hours = Math.floor(minutes / 60)
  const rest = minutes % 60
  return `${hours ? `${hours}h` : ''}${rest ? ` ${rest}m` : ''}`.trim()
}

function today() {
  return new Date().toISOString().slice(0, 10)
}

function formatDisplayDate(dateStr: string | null | undefined) {
  if (!dateStr) return '...'
  const parts = dateStr.split('-')
  if (parts.length === 3) return `${parts[2]}/${parts[1]}/${parts[0]}`
  return dateStr
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

  return <Modal open={open} onClose={onClose} title="Tạo backlog item" description="Backlog mới sẽ nằm trong Product Backlog và có thể kéo vào Sprint." showClose={false}>
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

  return <Modal open={open} onClose={onClose} title="Tạo Sprint" description="Sprint mới ở trạng thái Lên kế hoạch và có thể nhận backlog item." showClose={false}>
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


function CreateTaskModal({
  open,
  saving,
  backlogItems,
  members,
  onClose,
  onSave,
}: {
  open: boolean
  saving: boolean
  backlogItems: BacklogItem[]
  members: ProjectMember[]
  onClose: () => void
  onSave: ScrumBoardViewProps['onCreateTask']
}) {
  const [backlogItemId, setBacklogItemId] = useState('')
  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [type, setType] = useState<TaskType>('DEVELOPMENT')
  const [priority, setPriority] = useState<TaskPriority>('MEDIUM')
  const [assigneeUserId, setAssigneeUserId] = useState('')
  const [estimatedMinutes, setEstimatedMinutes] = useState(60)
  const [startDate, setStartDate] = useState('')
  const [dueDate, setDueDate] = useState('')

  const submit = (event: FormEvent) => {
    event.preventDefault()
    onSave({
      backlogItemId,
      title,
      description: description || undefined,
      type,
      priority,
      assigneeUserId: assigneeUserId || undefined,
      estimatedMinutes,
      startDate: startDate || undefined,
      dueDate: dueDate || undefined,
    })
    setTitle('')
    setDescription('')
  }

  return <Modal open={open} onClose={onClose} title="Tạo Task" description="Task sẽ nằm trong Sprint theo Backlog Item đã chọn." showClose={false}>
    <form className="space-y-4" onSubmit={submit}>
      <Select label="Backlog Item" required value={backlogItemId} onChange={event => setBacklogItemId(event.target.value)} options={[{ label: 'Chọn backlog item trong sprint', value: '' }, ...backlogItems.map(item => ({ label: item.title, value: item.id }))]} />
      <Input label="Tiêu đề Task" required value={title} onChange={event => setTitle(event.target.value)} placeholder="Xây dựng API đăng nhập" />
      <div>
        <label className="mb-2 block text-sm font-medium text-[#3f3f46]">Mô tả</label>
        <textarea className="min-h-24 w-full rounded-lg border border-line bg-white px-3.5 py-3 text-sm outline-none focus:border-brand focus:ring-2 focus:ring-brand/20" value={description} onChange={event => setDescription(event.target.value)} />
      </div>
      <div className="grid gap-4 sm:grid-cols-3">
        <Select label="Loại" value={type} onChange={event => setType(event.target.value as TaskType)} options={taskTypes.map(item => ({ label: taskTypeLabels[item], value: item }))} />
        <Select label="Ưu tiên" value={priority} onChange={event => setPriority(event.target.value as TaskPriority)} options={taskPriorities.map(item => ({ label: taskPriorityLabels[item], value: item }))} />
        <Input label="Ước tính phút" type="number" min={0} value={estimatedMinutes} onChange={event => setEstimatedMinutes(Number(event.target.value))} />
      </div>
      <div className="grid gap-4 sm:grid-cols-3">
        <Select label="Người phụ trách" value={assigneeUserId} onChange={event => setAssigneeUserId(event.target.value)} options={[{ label: 'Chưa phân công', value: '' }, ...members.map(member => ({ label: `${member.username} · ${member.projectRole}`, value: member.userId }))]} />
        <Input label="Ngày bắt đầu" type="date" value={startDate} onChange={event => setStartDate(event.target.value)} />
        <Input label="Deadline" type="date" value={dueDate} onChange={event => setDueDate(event.target.value)} />
      </div>
      <div className="flex justify-end gap-3"><Button type="button" variant="secondary" onClick={onClose}>Hủy</Button><Button type="submit" loading={saving} leadingIcon={<Plus size={17} />}>Tạo Task</Button></div>
    </form>
  </Modal>
}

function TaskDetailModal({
  task,
  comments,
  timeLogs,
  timeSummary,
  dependencies,
  risk,
  allTasks,
  members,
  loading,
  saving,
  canManage,
  onClose,
  onUpdateTask,
  onDeleteTask,
  onAssignTask,
  onAddDependency,
  onRemoveDependency,
  onUnblockTask,
  onCreateComment,
  onUpdateComment,
  onDeleteComment,
  onCreateTimeLog,
  onUpdateTimeLog,
  onDeleteTimeLog,
  onAskConfirm,
}: {
  task: Task | null
  comments: TaskCommentPage
  timeLogs: TaskTimeLogPage
  timeSummary: TaskTimeSummary | null
  dependencies: TaskDependency[]
  risk: TaskRisk | null
  allTasks: KanbanTask[]
  members: ProjectMember[]
  loading: boolean
  saving: boolean
  canManage: boolean
  onClose: () => void
  onUpdateTask: ScrumBoardViewProps['onUpdateTask']
  onDeleteTask: ScrumBoardViewProps['onDeleteTask']
  onAssignTask: ScrumBoardViewProps['onAssignTask']
  onAddDependency: ScrumBoardViewProps['onAddDependency']
  onRemoveDependency: ScrumBoardViewProps['onRemoveDependency']
  onUnblockTask: ScrumBoardViewProps['onUnblockTask']
  onCreateComment: ScrumBoardViewProps['onCreateTaskComment']
  onUpdateComment: ScrumBoardViewProps['onUpdateTaskComment']
  onDeleteComment: ScrumBoardViewProps['onDeleteTaskComment']
  onCreateTimeLog: ScrumBoardViewProps['onCreateTaskTimeLog']
  onUpdateTimeLog: ScrumBoardViewProps['onUpdateTaskTimeLog']
  onDeleteTimeLog: ScrumBoardViewProps['onDeleteTaskTimeLog']
  onAskConfirm: AskConfirm
}) {
  const [editingTask, setEditingTask] = useState(false)
  const [comment, setComment] = useState('')
  const [workDate, setWorkDate] = useState(today())
  const [minutes, setMinutes] = useState(60)
  const [logDescription, setLogDescription] = useState('')
  const [textAction, setTextAction] = useState<{ title: string; value: string; onSave: (value: string) => void } | null>(null)
  const [timeEdit, setTimeEdit] = useState<{ id: string; minutes: number; description: string } | null>(null)
  const [editTitle, setEditTitle] = useState(task?.title ?? '')
  const [editDescription, setEditDescription] = useState(task?.description ?? '')
  const [editType, setEditType] = useState<TaskType>(task?.type ?? 'DEVELOPMENT')
  const [editPriority, setEditPriority] = useState<TaskPriority>(task?.priority ?? 'MEDIUM')
  const [editEstimatedMinutes, setEditEstimatedMinutes] = useState(task?.estimatedMinutes ?? 0)
  const [editStartDate, setEditStartDate] = useState(task?.startDate ?? '')
  const [editDueDate, setEditDueDate] = useState(task?.dueDate ?? '')
  const [unblockModalOpen, setUnblockModalOpen] = useState(false)
  const [unblockStatus, setUnblockStatus] = useState<TaskStatus>('TODO')
  const [selectedDepTaskId, setSelectedDepTaskId] = useState('')
  const [mentionSearch, setMentionSearch] = useState('')
  const [showMentionDropdown, setShowMentionDropdown] = useState(false)
  const [mentionStartIndex, setMentionStartIndex] = useState(-1)
  const [activeMentionInput, setActiveMentionInput] = useState<'comment' | 'modal'>('comment')

  const filteredMembers = members.filter(m =>
    m.username.toLowerCase().includes(mentionSearch.toLowerCase())
  )

  const handleCommentInputChange = (value: string) => {
    setComment(value)
    const lastAt = value.lastIndexOf('@')
    if (lastAt !== -1 && (lastAt === 0 || value[lastAt - 1] === ' ')) {
      const searchPart = value.substring(lastAt + 1)
      if (!searchPart.includes(' ')) {
        setMentionSearch(searchPart)
        setShowMentionDropdown(true)
        setMentionStartIndex(lastAt)
        setActiveMentionInput('comment')
        return
      }
    }
    setShowMentionDropdown(false)
  }

  const handleModalInputChange = (value: string) => {
    setTextAction(current => current ? { ...current, value } : null)
    const lastAt = value.lastIndexOf('@')
    if (lastAt !== -1 && (lastAt === 0 || value[lastAt - 1] === ' ')) {
      const searchPart = value.substring(lastAt + 1)
      if (!searchPart.includes(' ')) {
        setMentionSearch(searchPart)
        setShowMentionDropdown(true)
        setMentionStartIndex(lastAt)
        setActiveMentionInput('modal')
        return
      }
    }
    setShowMentionDropdown(false)
  }

  const handleSelectMention = (username: string) => {
    if (activeMentionInput === 'comment') {
      const prefix = comment.substring(0, mentionStartIndex)
      const suffix = comment.substring(mentionStartIndex + 1 + mentionSearch.length)
      setComment(`${prefix}@${username} ${suffix}`)
    } else if (activeMentionInput === 'modal' && textAction) {
      const prefix = textAction.value.substring(0, mentionStartIndex)
      const suffix = textAction.value.substring(mentionStartIndex + 1 + mentionSearch.length)
      setTextAction({ ...textAction, value: `${prefix}@${username} ${suffix}` })
    }
    setShowMentionDropdown(false)
  }

  const renderCommentContent = (content: string, mentionedUsernames?: string[]) => {
    if (!mentionedUsernames || mentionedUsernames.length === 0) return <span>{content}</span>
    const parts = content.split(/(\s+)/)
    return (
      <>
        {parts.map((part, index) => {
          if (part.startsWith('@')) {
            const username = part.substring(1)
            if (mentionedUsernames.includes(username)) {
              return (
                <span key={index} className="font-bold text-brand hover:underline cursor-pointer">
                  {part}
                </span>
              )
            }
          }
          return <span key={index}>{part}</span>
        })}
      </>
    )
  }

  if (!task) return null

  return <Modal
    open={Boolean(task)}
    onClose={onClose}
    title={task.title}
    description={`${taskTypeLabels[task.type]} · ${taskStatusLabels[task.status]}`}
    actions={canManage ? <ActionMenu>
      {task.status !== 'DONE' && task.status !== 'CANCELLED' && (
        <ActionItem onClick={() => setEditingTask(value => !value)}><Pencil size={15} /> {editingTask ? 'Đóng sửa' : 'Sửa đầy đủ'}</ActionItem>
      )}
      {task.status !== 'CANCELLED' && (
        <ActionItem danger onClick={() => onAskConfirm('Xóa Task?', `Task "${task.title}" sẽ bị xóa mềm khỏi Sprint.`, 'Xóa Task', () => onDeleteTask(task.id))}><Trash2 size={15} /> Xóa Task</ActionItem>
      )}
    </ActionMenu> : undefined}
  >
    <div className={`space-y-5 ${loading ? 'opacity-50' : ''}`}>
      {task.status === 'CANCELLED' && (
        <div className="rounded-xl border border-red-200 bg-red-50/50 p-4 text-sm text-red-800 font-medium">
          Task đã bị hủy nên không thể chỉnh sửa, phân công hoặc cập nhật.
        </div>
      )}

      {risk && (risk.riskLevel === 'HIGH' || risk.riskLevel === 'CRITICAL') && (
        <div className="rounded-xl border border-amber-200 bg-amber-50/50 p-4 text-sm text-amber-800 font-medium flex items-start gap-2">
          <ShieldAlert size={18} className="shrink-0 text-amber-600 mt-0.5" />
          <div>
            <p className="font-bold text-amber-900">Cảnh báo rủi ro: Mức độ {taskRiskLevelLabels[risk.riskLevel]}</p>
            <ul className="list-disc pl-4 mt-1 space-y-1">
              {risk.reasons.map((reason, idx) => (
                <li key={idx}>{reason}</li>
              ))}
            </ul>
          </div>
        </div>
      )}

      {task.status === 'BLOCKED' && (
        <div className="rounded-xl border border-rose-200 bg-rose-50/50 p-4 text-sm text-rose-800 font-medium">
          <div className="flex items-start justify-between gap-3 flex-wrap sm:flex-nowrap">
            <div className="flex gap-2">
              <AlertTriangle size={18} className="shrink-0 text-rose-600 mt-0.5" />
              <div>
                <p className="font-bold text-rose-900">Task đang bị chặn (BLOCKED)</p>
                <p className="mt-1 text-xs text-rose-700">
                  Chặn bởi: <span className="font-semibold">{members.find(m => m.userId === task.blockedByUserId)?.username || 'Thành viên'}</span> 
                  {task.blockedAt && ` vào lúc ${new Date(task.blockedAt).toLocaleString('vi-VN')}`}
                </p>
                <p className="mt-2 text-sm text-rose-950 bg-white/60 p-2 rounded-lg border border-rose-100/70">
                  Lý do: {task.blockReason || 'Không có lý do chi tiết'}
                </p>
              </div>
            </div>
            {canManage && (
              <Button 
                type="button" 
                size="sm" 
                className="shrink-0 bg-white hover:bg-rose-100 text-rose-700 border border-rose-300 shadow-sm"
                onClick={() => {
                  setUnblockStatus('TODO')
                  setUnblockModalOpen(true)
                }}
              >
                Mở chặn (Unblock)
              </Button>
            )}
          </div>
        </div>
      )}

      <div className="grid gap-3 sm:grid-cols-3">
        <div className="rounded-xl bg-canvas p-3"><p className="text-xs text-muted">Ước tính</p><p className="font-bold">{formatMinutes(task.estimatedMinutes)}</p></div>
        <div className="rounded-xl bg-canvas p-3"><p className="text-xs text-muted">Đã log</p><p className="font-bold">{formatMinutes(task.spentMinutes)}</p></div>
        <div className="rounded-xl bg-canvas p-3"><p className="text-xs text-muted">Tiến độ time</p><p className="font-bold">{Math.round(timeSummary?.progressPercentage ?? 0)}%</p></div>
      </div>

      {canManage && task.status !== 'CANCELLED' && <section className="rounded-xl border border-line bg-canvas p-4">
        <div className="mb-3 flex items-center justify-between gap-3">
          <h3 className="font-bold">Nội dung Task</h3>
        </div>
        {editingTask && <div className="space-y-3">
          <Input label="Tiêu đề" value={editTitle} onChange={event => setEditTitle(event.target.value)} />
          <textarea aria-label="Mô tả task" className="min-h-24 w-full rounded-lg border border-line bg-white px-3.5 py-3 text-sm outline-none focus:border-brand focus:ring-2 focus:ring-brand/20" value={editDescription} onChange={event => setEditDescription(event.target.value)} />
          <div className="grid gap-3 sm:grid-cols-3">
            <Select label="Loại" value={editType} onChange={event => setEditType(event.target.value as TaskType)} options={taskTypes.map(item => ({ label: taskTypeLabels[item], value: item }))} />
            <Select label="Ưu tiên" value={editPriority} onChange={event => setEditPriority(event.target.value as TaskPriority)} options={taskPriorities.map(item => ({ label: taskPriorityLabels[item], value: item }))} />
            <Input label="Ước tính phút" type="number" min={0} value={editEstimatedMinutes} onChange={event => setEditEstimatedMinutes(Number(event.target.value))} />
          </div>
          <div className="grid gap-3 sm:grid-cols-2">
            <Input label="Ngày bắt đầu" type="date" value={editStartDate} onChange={event => setEditStartDate(event.target.value)} />
            <Input label="Deadline" type="date" value={editDueDate} onChange={event => setEditDueDate(event.target.value)} />
          </div>
          <div className="flex justify-end">
            <Button loading={saving} leadingIcon={<Save size={16} />} onClick={() => {
              onUpdateTask(task.id, { title: editTitle, description: editDescription, type: editType, priority: editPriority, estimatedMinutes: editEstimatedMinutes, startDate: editStartDate || undefined, dueDate: editDueDate || undefined })
              setEditingTask(false)
            }}>Lưu Task</Button>
          </div>
        </div>}
      </section>}

      {canManage && task.status !== 'CANCELLED' && <div className="grid gap-3 sm:grid-cols-[1fr_160px_160px]">
        <Select aria-label="Phân công" value={task.assigneeUserId ?? ''} onChange={event => onAssignTask(task.id, event.target.value)} options={[{ label: 'Chưa phân công', value: '' }, ...members.map(member => ({ label: `${member.username} · ${member.projectRole}`, value: member.userId }))]} />
        <Select aria-label="Ưu tiên" value={task.priority} onChange={event => onUpdateTask(task.id, { priority: event.target.value as TaskPriority })} disabled={task.status === 'DONE'} options={taskPriorities.map(item => ({ label: taskPriorityLabels[item], value: item }))} />
        <Input aria-label="Ước tính phút" type="number" min={0} value={task.estimatedMinutes ?? 0} onChange={event => onUpdateTask(task.id, { estimatedMinutes: Number(event.target.value) })} disabled={task.status === 'DONE'} />
      </div>}

      <section>
        <h3 className="mb-3 flex items-center gap-2 font-bold"><MessageSquare size={17} /> Bình luận</h3>
        <div className="relative">
          <form className="mb-3 flex gap-2" onSubmit={event => { event.preventDefault(); if (comment.trim()) { onCreateComment(task.id, comment); setComment(''); setShowMentionDropdown(false) } }}>
            <div className="flex-1 relative">
              <Input 
                aria-label="Nhập bình luận" 
                value={comment} 
                onChange={event => handleCommentInputChange(event.target.value)} 
                placeholder="Trao đổi về task (Gõ @ để nhắc tên)..." 
              />
              {showMentionDropdown && activeMentionInput === 'comment' && filteredMembers.length > 0 && (
                <div className="absolute z-50 left-0 bottom-full mb-1 w-64 max-h-48 overflow-y-auto rounded-lg border border-line bg-white shadow-lg divide-y divide-line">
                  {filteredMembers.map(member => (
                    <button
                      key={member.id}
                      type="button"
                      className="w-full text-left px-3 py-2 text-xs hover:bg-slate-50 flex items-center justify-between"
                      onClick={() => handleSelectMention(member.username)}
                    >
                      <span className="font-semibold text-slate-800">@{member.username}</span>
                      <span className="text-slate-400 text-[10px]">{member.email}</span>
                    </button>
                  ))}
                </div>
              )}
            </div>
            <Button type="submit" loading={saving}>Gửi</Button>
          </form>
        </div>
        <div className="space-y-2">
          {comments.content.map(item => <article key={item.id} className="rounded-xl border border-line p-3">
            <div className="flex items-start justify-between gap-3">
              <div>
                <p className="text-sm font-semibold flex items-center gap-1.5">
                  {item.username}
                  {item.edited && (
                    <span className="text-[10px] text-muted font-normal bg-slate-100 px-1 py-0.2 rounded" title="Đã chỉnh sửa">
                      (đã sửa)
                    </span>
                  )}
                </p>
                <p className="mt-1 text-sm text-muted">{renderCommentContent(item.content, item.mentionedUsernames)}</p>
              </div>
              <ActionMenu>
                {item.canEdit && <ActionItem onClick={() => setTextAction({ title: 'Sửa bình luận', value: item.content, onSave: value => onUpdateComment(task.id, item.id, value) })}><Pencil size={15} /> Sửa bình luận</ActionItem>}
                <ActionItem onClick={() => setTextAction({ title: 'Reply bình luận', value: '', onSave: value => onCreateComment(task.id, value, item.id) })}><MessageSquare size={15} /> Trả lời</ActionItem>
                {item.canDelete && <ActionItem danger onClick={() => onAskConfirm('Xóa bình luận?', 'Bình luận này sẽ bị xóa khỏi Task.', 'Xóa bình luận', () => onDeleteComment(task.id, item.id))}><Trash2 size={15} /> Xóa bình luận</ActionItem>}
              </ActionMenu>
            </div>
            {item.replies?.length > 0 && <div className="mt-3 space-y-2 border-l-2 border-line pl-3">
              {item.replies.map(reply => <div key={reply.id} className="rounded-lg bg-canvas p-2">
                <p className="text-xs font-semibold flex items-center gap-1.5">
                  {reply.username}
                  {reply.edited && (
                    <span className="text-[9px] text-muted font-normal bg-slate-200 px-1 py-0.2 rounded" title="Đã chỉnh sửa">
                      (đã sửa)
                    </span>
                  )}
                </p>
                <p className="mt-1 text-xs text-muted">{renderCommentContent(reply.content, reply.mentionedUsernames)}</p>
              </div>)}
            </div>}
          </article>)}
          {!comments.content.length && <p className="rounded-xl bg-canvas p-4 text-center text-sm text-muted">Chưa có bình luận.</p>}
        </div>
      </section>

      <section className="rounded-xl border border-line bg-canvas p-4">
        <h3 className="mb-3 flex items-center gap-2 font-bold text-ink">
          <FolderKanban size={17} className="text-brand" /> Task liên kết phụ thuộc (Dependencies)
        </h3>

        <div className="space-y-2 mb-4">
          {dependencies.map(dep => (
            <div key={dep.id} className="flex items-center justify-between gap-3 rounded-lg border border-line bg-white p-3 shadow-sm">
              <div className="min-w-0 flex-1">
                <p className="font-semibold text-sm text-ink truncate" title={dep.dependsOnTaskTitle}>
                  {dep.dependsOnTaskTitle}
                </p>
                <div className="mt-1 flex flex-wrap items-center gap-2 text-xs">
                  <span className={`rounded px-1.5 py-0.5 font-bold ${
                    dep.dependsOnTaskStatus === 'DONE' ? 'bg-emerald-50 text-emerald-600' : 'bg-amber-50 text-amber-600'
                  }`}>
                    {taskStatusLabels[dep.dependsOnTaskStatus]}
                  </span>
                  <span className="text-muted">
                    Ưu tiên: {taskPriorityLabels[dep.dependsOnTaskPriority]}
                  </span>
                  {dep.dependencyCompleted && (
                    <span className="rounded bg-emerald-100 text-emerald-800 px-1 py-0.5 text-[10px] font-bold">
                      Đã xong
                    </span>
                  )}
                </div>
              </div>
              {canManage && task.status !== 'CANCELLED' && (
                <button
                  type="button"
                  className="rounded-lg p-1.5 text-muted hover:bg-slate-100 hover:text-rose-600 transition"
                  onClick={() => onRemoveDependency(task.id, dep.id)}
                  title="Xóa liên kết"
                >
                  <Trash2 size={15} />
                </button>
              )}
            </div>
          ))}
          {dependencies.length === 0 && (
            <p className="text-center text-xs text-muted py-3 bg-white rounded-lg border border-line border-dashed">
              Không có task phụ thuộc nào.
            </p>
          )}
        </div>

        {canManage && task.status !== 'CANCELLED' && (
          <div className="flex gap-2 items-end">
            <div className="flex-1">
              <Select
                label="Thêm task phụ thuộc"
                value={selectedDepTaskId}
                onChange={event => setSelectedDepTaskId(event.target.value)}
                options={[
                  { label: '-- Chọn Task phụ thuộc --', value: '' },
                  ...allTasks
                    .filter(t => t.id !== task.id && !dependencies.some(d => d.dependsOnTaskId === t.id))
                    .map(t => ({
                      label: `${t.backlogItemTitle ? `[${t.backlogItemTitle}] ` : ''}${t.title}`,
                      value: t.id
                    }))
                ]}
              />
            </div>
            <Button
              type="button"
              disabled={!selectedDepTaskId || saving}
              onClick={() => {
                onAddDependency(task.id, selectedDepTaskId)
                setSelectedDepTaskId('')
              }}
            >
              Liên kết
            </Button>
          </div>
        )}
      </section>

      <section>
        <h3 className="mb-3 flex items-center gap-2 font-bold"><Clock3 size={17} /> Time log</h3>
        {task.status !== 'CANCELLED' ? (
          <form className="mb-3 grid gap-2 sm:grid-cols-[150px_120px_1fr_auto]" onSubmit={event => { event.preventDefault(); onCreateTimeLog(task.id, { workDate, minutes, description: logDescription || undefined }); setLogDescription('') }}>
            <Input aria-label="Ngày làm" type="date" value={workDate} onChange={event => setWorkDate(event.target.value)} required />
            <Input aria-label="Số phút" type="number" min={1} max={720} value={minutes} onChange={event => setMinutes(Number(event.target.value))} required />
            <Input aria-label="Mô tả time log" value={logDescription} onChange={event => setLogDescription(event.target.value)} placeholder="Đã làm gì?" />
            <Button type="submit" loading={saving}>Ghi</Button>
          </form>
        ) : (
          <p className="mb-3 text-xs text-rose-600 bg-rose-50 border border-rose-100 p-2.5 rounded-lg font-semibold flex items-center gap-1.5">
            <AlertTriangle size={14} /> Không thể ghi log thời gian cho Task đã Hủy.
          </p>
        )}
        <div className="space-y-2">
          {timeLogs.content.map(item => <article key={item.id} className="flex items-start justify-between gap-3 rounded-xl border border-line p-3">
            <div><p className="text-sm font-semibold">{item.username} · {item.workDate}</p><p className="mt-1 text-sm text-muted">{item.description || 'Không có mô tả'}</p></div>
            <div className="flex items-center gap-2">
              <span className="rounded-full bg-[#fef3e2] px-2.5 py-1 text-xs font-bold text-brand-dark">{formatMinutes(item.minutes)}</span>
              {(item.canEdit || item.canDelete) && <ActionMenu>
                {item.canEdit && <ActionItem onClick={() => setTimeEdit({ id: item.id, minutes: item.minutes, description: item.description ?? '' })}><Pencil size={15} /> Sửa time log</ActionItem>}
                {item.canDelete && <ActionItem danger onClick={() => onAskConfirm('Xóa time log', 'Bản ghi thời gian này sẽ bị xóa khỏi Task.', 'Xóa time log', () => onDeleteTimeLog(task.id, item.id))}><Trash2 size={15} /> Xóa time log</ActionItem>}
              </ActionMenu>}
            </div>
          </article>)}
          {!timeLogs.content.length && <p className="rounded-xl bg-canvas p-4 text-center text-sm text-muted">Chưa có time log.</p>}
        </div>
      </section>
      <Modal open={Boolean(textAction)} title={textAction?.title ?? ''} onClose={() => { setTextAction(null); setShowMentionDropdown(false) }} showClose={false}>
        <form className="space-y-4" onSubmit={event => {
          event.preventDefault()
          const value = textAction?.value.trim()
          if (textAction && value) textAction.onSave(value)
          setTextAction(null)
          setShowMentionDropdown(false)
        }}>
          <div className="relative">
            <textarea 
              className="min-h-28 w-full rounded-lg border border-line bg-white px-3.5 py-3 text-sm outline-none focus:border-brand focus:ring-2 focus:ring-brand/20" 
              value={textAction?.value ?? ''} 
              onChange={event => handleModalInputChange(event.target.value)} 
              autoFocus 
            />
            {showMentionDropdown && activeMentionInput === 'modal' && filteredMembers.length > 0 && (
              <div className="absolute z-50 left-0 bottom-full mb-1 w-64 max-h-48 overflow-y-auto rounded-lg border border-line bg-white shadow-lg divide-y divide-line">
                {filteredMembers.map(member => (
                  <button
                    key={member.id}
                    type="button"
                    className="w-full text-left px-3 py-2 text-xs hover:bg-slate-50 flex items-center justify-between"
                    onClick={() => handleSelectMention(member.username)}
                  >
                    <span className="font-semibold text-slate-800">@{member.username}</span>
                    <span className="text-slate-400 text-[10px]">{member.email}</span>
                  </button>
                ))}
              </div>
            )}
          </div>
          <div className="flex justify-end gap-3">
            <Button type="button" variant="secondary" onClick={() => { setTextAction(null); setShowMentionDropdown(false) }}>Hủy</Button>
            <Button type="submit" loading={saving}>Lưu</Button>
          </div>
        </form>
      </Modal>
      <Modal open={Boolean(timeEdit)} title="Sửa time log" onClose={() => setTimeEdit(null)} showClose={false}>
        <form className="space-y-4" onSubmit={event => {
          event.preventDefault()
          if (timeEdit) onUpdateTimeLog(task.id, timeEdit.id, { minutes: timeEdit.minutes, description: timeEdit.description })
          setTimeEdit(null)
        }}>
          <Input label="Số phút" type="number" min={1} max={720} value={timeEdit?.minutes ?? 0} onChange={event => setTimeEdit(current => current ? { ...current, minutes: Number(event.target.value) } : current)} />
          <Input label="Mô tả" value={timeEdit?.description ?? ''} onChange={event => setTimeEdit(current => current ? { ...current, description: event.target.value } : current)} />
          <div className="flex justify-end gap-3">
            <Button type="button" variant="secondary" onClick={() => setTimeEdit(null)}>Hủy</Button>
            <Button type="submit" loading={saving}>Lưu</Button>
          </div>
        </form>
      </Modal>
      <Modal open={unblockModalOpen} title="Mở chặn Task (Unblock)" onClose={() => setUnblockModalOpen(false)} showClose={false}>
        <form className="space-y-4" onSubmit={event => {
          event.preventDefault()
          if (unblockStatus) {
            onUnblockTask(task.id, unblockStatus)
            setUnblockModalOpen(false)
          }
        }}>
          <Select 
            label="Chọn trạng thái đích muốn quay lại" 
            required 
            value={unblockStatus} 
            onChange={event => setUnblockStatus(event.target.value as TaskStatus)} 
            options={[
              { label: 'Cần làm (TODO)', value: 'TODO' },
              { label: 'Đang làm (IN_PROGRESS)', value: 'IN_PROGRESS' }
            ]} 
          />
          <div className="flex justify-end gap-3">
            <Button type="button" variant="secondary" onClick={() => setUnblockModalOpen(false)}>Hủy</Button>
            <Button type="submit" loading={saving}>Mở chặn</Button>
          </div>
        </form>
      </Modal>
    </div>
  </Modal>
}

function TaskCard({ task, canDrag, onDragStart, onDragEnd, onOpen }: { task: KanbanTask; canDrag: boolean; onDragStart: (event: DragEvent<HTMLElement>, task: KanbanTask) => void; onDragEnd: () => void; onOpen: (taskId: string) => void }) {
  return (
    <article 
      draggable={canDrag}
      onDragStart={event => {
        // Only drag if not clicking a button/interactive element inside
        if (canDrag) onDragStart(event, task);
      }}
      onDragEnd={onDragEnd}
      onClick={() => onOpen(task.id)}
      className="group relative flex flex-col overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm transition-all duration-300 hover:border-brand/40 hover:shadow-md hover:-translate-y-0.5 cursor-pointer"
    >
      {/* Left accent bar for priority */}
      <div className={`absolute bottom-0 left-0 top-0 w-1 transition-colors ${taskPriorityIndicatorClass(task.priority)} opacity-80 group-hover:opacity-100`} />

      <div className="flex flex-col p-3.5 pl-4">
        {/* Top bar: Type & Status */}
        <div className="mb-2.5 flex items-start justify-between gap-2">
          <div className="flex flex-wrap items-center gap-1.5">
            <span className="rounded bg-slate-100 px-2 py-0.5 text-[10px] font-bold uppercase tracking-wider text-slate-600 border border-slate-200/60">
              {taskTypeLabels[task.type]}
            </span>
            {task.overdue && (
              <span className="rounded bg-red-50 px-2 py-0.5 text-[10px] font-bold uppercase tracking-wider text-red-600 border border-red-100">
                Quá hạn
              </span>
            )}
          </div>
          
          {/* Subtle drag indicator that appears on hover */}
          {canDrag && (
            <div className="text-slate-300 transition-colors group-hover:text-slate-400">
              <GripVertical size={14} />
            </div>
          )}
        </div>

        {/* Title */}
        <h4 className="mb-1.5 text-sm font-semibold leading-snug text-slate-800 transition-colors group-hover:text-brand">
          {task.title}
        </h4>

        {/* Parent Backlog Item */}
        <div className="mb-4 flex items-center gap-1.5 text-xs text-slate-500">
          <div className="h-1 w-1 shrink-0 rounded-full bg-slate-300" />
          <p className="line-clamp-1">{task.backlogItemTitle}</p>
        </div>

        {/* Bottom bar: Priority & Meta (Time / Assignee) */}
        <div className="mt-auto pt-3 border-t border-slate-100 flex items-center justify-between gap-2">
          <span className={`rounded-md px-2 py-1 text-[10px] font-bold tracking-wide ${taskPriorityClass(task.priority)}`}>
            {taskPriorityLabels[task.priority]}
          </span>

          <div className="flex items-center gap-2.5 text-[11px] font-medium text-slate-500">
            <div className="flex items-center gap-1" title="Thời gian log / Ước tính">
              <Clock3 size={12} className="text-slate-400" />
              <span>{formatMinutes(task.spentMinutes)}<span className="mx-0.5 text-slate-300">/</span>{formatMinutes(task.estimatedMinutes)}</span>
            </div>
            <div className="flex items-center gap-1 bg-slate-50 px-1.5 py-0.5 rounded border border-slate-100" title="Người phụ trách">
              <UserRound size={12} className="text-slate-400" />
              <span className="max-w-[70px] truncate">{task.assigneeUsername || 'Chưa giao'}</span>
            </div>
          </div>
        </div>
      </div>
    </article>
  )
}

const getStatusColors = (status: TaskStatus) => {
  switch (status) {
    case 'TODO': return { headerBg: 'bg-slate-50', badgeBg: 'bg-slate-500', borderColor: 'border-slate-200', activeBorder: 'border-slate-400', ringColor: 'ring-slate-400/20', bodyBg: 'bg-white' }
    case 'IN_PROGRESS': return { headerBg: 'bg-blue-50/70', badgeBg: 'bg-blue-500', borderColor: 'border-blue-200', activeBorder: 'border-blue-500', ringColor: 'ring-blue-500/20', bodyBg: 'bg-white' }
    case 'IN_REVIEW': return { headerBg: 'bg-purple-50/70', badgeBg: 'bg-purple-500', borderColor: 'border-purple-200', activeBorder: 'border-purple-500', ringColor: 'ring-purple-500/20', bodyBg: 'bg-white' }
    case 'DONE': return { headerBg: 'bg-emerald-50/70', badgeBg: 'bg-emerald-500', borderColor: 'border-emerald-200', activeBorder: 'border-emerald-500', ringColor: 'ring-emerald-500/20', bodyBg: 'bg-white' }
    case 'BLOCKED': return { headerBg: 'bg-amber-50/70', badgeBg: 'bg-amber-500', borderColor: 'border-amber-200', activeBorder: 'border-amber-500', ringColor: 'ring-amber-500/20', bodyBg: 'bg-white' }
    case 'CANCELLED': return { headerBg: 'bg-rose-50/70', badgeBg: 'bg-rose-500', borderColor: 'border-rose-200', activeBorder: 'border-rose-500', ringColor: 'ring-rose-500/20', bodyBg: 'bg-white' }
    default: return { headerBg: 'bg-white', badgeBg: 'bg-brand', borderColor: 'border-brand-line', activeBorder: 'border-brand', ringColor: 'ring-brand/20', bodyBg: 'bg-white' }
  }
}

function SprintTaskKanban({
  sprints,
  selectedSprintId,
  board,
  userStories,
  statistics: _statistics,
  burndown,
  importResult,
  canManage,
  onSelectSprint,
  onTaskStatusChange,
  onOpenTask,
  onCreateTaskClick,
  onDownloadTemplate,
  onImportTasks,
  onClearImportResult,
  onBack,
  onOpenStatistics,
  onOpenProgress,
  onOpenClosing,
}: {
  sprints: Sprint[]
  selectedSprintId: string | null
  board: KanbanBoard | undefined
  userStories: BacklogItem[]
  statistics: SprintTaskStatistics | null
  burndown: SprintBurndown | null
  importResult: TaskImportResult | null
  canManage: boolean
  onSelectSprint: (sprintId: string) => void
  onTaskStatusChange: ScrumBoardViewProps['onTaskStatusChange']
  onOpenTask: ScrumBoardViewProps['onOpenTask']
  onCreateTaskClick: () => void
  onDownloadTemplate: ScrumBoardViewProps['onDownloadTaskTemplate']
  onImportTasks: ScrumBoardViewProps['onImportTasks']
  onClearImportResult: ScrumBoardViewProps['onClearTaskImportResult']
  onBack: () => void
  onOpenStatistics: () => void
  onOpenProgress?: () => void
  onOpenClosing: () => void
}) {
  const [dragOver, setDragOver] = useState<TaskStatus | null>(null)
  const [selectedBacklogItemId, setSelectedBacklogItemId] = useState<string>('')
  const [isTemplateAnim, setIsTemplateAnim] = useState(false)
  const [isImportAnim, setIsImportAnim] = useState(false)

  useEffect(() => {
    if (userStories.length > 0) {
      if (!selectedBacklogItemId || !userStories.some(u => u.id === selectedBacklogItemId)) {
        setSelectedBacklogItemId(userStories[0].id)
      }
    } else {
      setSelectedBacklogItemId('')
    }
  }, [userStories, selectedBacklogItemId])

  const handleDownloadTemplate = () => {
    if (!selectedSprintId) return
    setIsTemplateAnim(true)
    setTimeout(() => setIsTemplateAnim(false), 300)
    onDownloadTemplate(selectedSprintId)
  }

  const handleImportClick = () => {
    setIsImportAnim(true)
    setTimeout(() => setIsImportAnim(false), 300)
  }

  const handleDragStart = (event: DragEvent<HTMLElement>, task: KanbanTask) => {
    event.dataTransfer.effectAllowed = 'move'
    event.dataTransfer.setData('application/json', JSON.stringify({ taskId: task.id, status: task.status }))
    const preview = createDragPreview(task.title)
    event.dataTransfer.setDragImage(preview, 16, 16)
    window.requestAnimationFrame(() => preview.remove())
  }

  const handleImport = (event: ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0]
    if (selectedSprintId && file) onImportTasks(selectedSprintId, file)
    event.target.value = ''
  }
  const canMoveTask = canManage && board?.sprintStatus === 'ACTIVE'
  const allTasks = board?.columns.flatMap(column => column.tasks) ?? []
  const selectedBacklogItemExists = userStories.some(item => item.id === selectedBacklogItemId)
  const activeBacklogItemId = selectedBacklogItemExists ? selectedBacklogItemId : (userStories[0]?.id ?? '')
  const columnOrder: TaskStatus[] = ['TODO', 'IN_PROGRESS', 'BLOCKED', 'IN_REVIEW', 'DONE', 'CANCELLED']
  const visibleBoard = board
    ? {
        ...board,
        columns: columnOrder
          .map(status => {
            const existingColumn = board.columns.find(c => c.status === status)
            const tasks = existingColumn 
              ? (activeBacklogItemId ? existingColumn.tasks.filter(task => task.backlogItemId === activeBacklogItemId) : [])
              : []

            return {
              status,
              title: taskStatusLabels[status] || status,
              tasks,
              taskCount: tasks.length,
            }
          })
      }
    : undefined

  const localTotalTasks = board?.columns.reduce((sum, col) => sum + col.tasks.length, 0) ?? 0
  const localCompletedTasks = board?.columns.find(col => col.status === 'DONE')?.tasks.length ?? 0
  const localCompletionRate = localTotalTasks > 0 ? (localCompletedTasks / localTotalTasks) * 100 : 0
  const localSpentMinutes = board?.columns.reduce((sum, col) => sum + col.tasks.reduce((tSum, t) => tSum + (t.spentMinutes ?? 0), 0), 0) ?? 0

  return <section className="space-y-4 rounded-2xl border border-brand-line bg-gradient-to-br from-brand-soft via-brand-cream to-white p-4 shadow-[0_18px_45px_rgba(247,148,29,0.08)]">
    <div className="flex flex-col gap-3 rounded-xl border border-brand-line/80 bg-white/90 p-3 shadow-sm lg:flex-row lg:items-center lg:justify-between">
      <div className="flex items-start gap-3">
        <Button variant="secondary" size="sm" leadingIcon={<ChevronLeft size={16} />} onClick={onBack}>Sprint Board</Button>
        <div>
          <h3 className="text-lg font-bold">Kanban Task</h3>
          <p className="mt-1 text-sm text-muted">Kéo task giữa các cột để cập nhật trạng thái trong Sprint.</p>
        </div>
      </div>
      <div className="flex flex-wrap gap-2">
        <Select aria-label="Chọn Sprint Kanban" value={selectedSprintId ?? ''} onChange={event => onSelectSprint(event.target.value)} options={sprints.map(sprint => ({ label: `${sprint.name} · ${sprintStatusLabels[sprint.status]}`, value: sprint.id }))} />
        {canManage && <Button leadingIcon={<Plus size={17} />} onClick={onCreateTaskClick}>Tạo Task</Button>}
        {selectedSprintId && <Button leadingIcon={<BarChart3 size={17} />} variant="solid-blue" onClick={onOpenStatistics}>Thống kê</Button>}
        {selectedSprintId && onOpenProgress && <Button leadingIcon={<Target size={17} />} variant="outline-amber" onClick={onOpenProgress}>Tiến độ</Button>}
        {selectedSprintId && <Button leadingIcon={<CheckCheck size={17} />} variant="outline-green" onClick={onOpenClosing}>Tổng kết</Button>}
        {selectedSprintId && <Button variant="outline-blue" leadingIcon={<Download size={17} className={`transition-transform duration-300 ${isTemplateAnim ? 'translate-y-1.5' : ''}`} />} onClick={handleDownloadTemplate}>File mẫu</Button>}
        {selectedSprintId && canManage && <Button as="label" variant="outline-green" onClick={handleImportClick} className="!h-11 cursor-pointer">
          <Import size={17} className={`transition-transform duration-300 ${isImportAnim ? 'translate-y-1.5' : ''}`} /> Import
          <input className="hidden" type="file" accept=".xlsx,.xls" onChange={handleImport} />
        </Button>}
      </div>
    </div>

    {importResult && <div className="fixed inset-0 z-[100] grid place-items-center bg-brand-black/55 p-4 animate-enter">
      <div className={`max-h-[85vh] w-[min(720px,calc(100vw-2rem))] overflow-hidden flex flex-col rounded-2xl border shadow-2xl ${importResult.failedRows > 0 ? 'border-danger/30 bg-[#fff0ed]' : 'border-success/30 bg-[#ecfdf3]'}`}>
        <div className="flex shrink-0 items-start justify-between gap-3 p-5 border-b border-black/5">
          <div>
            <p className={`text-[20px] font-bold ${importResult.failedRows > 0 ? 'text-danger' : 'text-success'}`}>
              {importResult.failedRows > 0 ? 'Import Task chưa thành công' : 'Import Task thành công'}
            </p>
            <p className="mt-1 text-sm opacity-80">
              Trạng thái: {importResult.status} · Tổng {importResult.totalRows} dòng · Thành công {importResult.successRows} · Lỗi {importResult.failedRows}
            </p>
          </div>
          <Button variant="ghost" size="sm" iconOnly leadingIcon={<X size={18} />} aria-label="Đóng kết quả import" onClick={onClearImportResult} />
        </div>
        
        {importResult.errors.length > 0 && <div className="min-h-0 flex-1 overflow-auto p-5">
          <div className="overflow-hidden rounded-xl border border-danger/20 bg-white">
            <table className="w-full min-w-[680px] text-left text-sm">
              <thead className="bg-[#fff7f5] text-xs font-bold uppercase tracking-wider text-danger">
                <tr><th className="px-4 py-3">Dòng</th><th className="px-4 py-3">Cột</th><th className="px-4 py-3">Giá trị</th><th className="px-4 py-3">Lỗi</th></tr>
              </thead>
              <tbody>
                {importResult.errors.map((error, index) => <tr key={`${error.rowNumber}-${error.fieldName ?? index}`} className="border-t border-line">
                  <td className="px-4 py-3 font-semibold">{error.rowNumber}</td>
                  <td className="px-4 py-3">{error.fieldName || '-'}</td>
                  <td className="px-4 py-3">{error.rawValue || '-'}</td>
                  <td className="px-4 py-3 text-danger">{error.message}</td>
                </tr>)}
              </tbody>
            </table>
          </div>
        </div>}
      </div>
    </div>}

    <div className="grid gap-3 md:grid-cols-4">
      <div className="rounded-xl border border-brand-line/70 bg-white/95 p-4 shadow-sm"><p className="text-xs text-muted">Tổng Task</p><p className="mt-1 text-2xl font-bold text-brand-black">{localTotalTasks}</p></div>
      <div className="rounded-xl border border-brand-line/70 bg-white/95 p-4 shadow-sm"><p className="text-xs text-muted">Hoàn thành</p><p className="mt-1 text-2xl font-bold text-success">{Math.round(localCompletionRate)}%</p></div>
      <div className="rounded-xl border border-brand-line/70 bg-white/95 p-4 shadow-sm"><p className="text-xs text-muted">Đã log</p><p className="mt-1 text-2xl font-bold text-brand-dark">{formatMinutes(localSpentMinutes)}</p></div>
      <div className="rounded-xl border border-brand-line/70 bg-white/95 p-4 shadow-sm"><p className="text-xs text-muted">Burndown điểm</p><p className="mt-1 text-2xl font-bold text-brand-black">{burndown?.points.length ?? 0}</p></div>
    </div>

    {!visibleBoard ? <p className="rounded-xl border border-dashed border-line bg-white p-8 text-center text-sm text-muted">Chưa có Kanban cho Sprint này.</p> : <div className="space-y-4">
      {/* Horizontal User Story Filter */}
      <div className="flex items-center gap-3 overflow-x-auto rounded-xl border border-brand-line/70 bg-white/95 p-4 shadow-sm scrollbar-thin">
        <div className="shrink-0 font-bold text-ink mr-2">User Story:</div>
        {userStories.length > 0 ? (
          userStories.map(item => {
            const taskCount = allTasks.filter(task => task.backlogItemId === item.id).length
            return (
              <UserStoryHorizontalCard
                key={item.id}
                item={item}
                isActive={activeBacklogItemId === item.id}
                taskCount={taskCount}
                onClick={() => setSelectedBacklogItemId(item.id)}
              />
            )
          })
        ) : (
          <p className="text-sm text-muted italic">Chưa có User Story trong Sprint này.</p>
        )}
      </div>

      {/* Kanban Columns */}
      <div className="grid min-w-0 gap-3 grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-6 items-start pb-4">
          {visibleBoard.columns.map(column => {
            const originalColumn = board?.columns.find(item => item.status === column.status)
            const colors = getStatusColors(column.status)

            return <div
              key={column.status}
              onDragOver={event => { if (canMoveTask) { event.preventDefault(); setDragOver(column.status) } }}
              onDragLeave={() => setDragOver(null)}
              onDrop={event => {
                event.preventDefault()
                setDragOver(null)
                const payload = JSON.parse(event.dataTransfer.getData('application/json')) as { taskId: string; status: TaskStatus }
                if (payload.status === 'CANCELLED') return
                if (payload.status === 'DONE' && column.status === 'TODO') return
                if (canMoveTask && payload.status !== column.status) onTaskStatusChange(payload.taskId, column.status, (originalColumn?.tasks.length ?? column.tasks.length) + 1)
              }}
              className={`flex h-[620px] min-w-0 flex-col overflow-hidden rounded-2xl border bg-white shadow-sm transition ${dragOver === column.status ? `${colors.activeBorder} ring-2 ${colors.ringColor}` : colors.borderColor}`}
            >
              <div className={`flex h-14 shrink-0 items-center justify-between gap-2 border-b px-3 py-2 ${colors.borderColor} ${colors.headerBg}`}>
                <p className="font-bold text-ink">{column.title || taskStatusLabels[column.status]}</p>
                <span className={`grid size-8 place-items-center rounded-lg text-sm font-bold text-white shadow-sm ${colors.badgeBg}`}>{column.taskCount}</span>
              </div>
              <div className={`min-h-0 flex-1 space-y-3 overflow-y-auto p-3 ${colors.bodyBg}`}>
                {column.tasks.map(task => <TaskCard key={task.id} task={task} canDrag={canMoveTask} onDragStart={handleDragStart} onDragEnd={() => setDragOver(null)} onOpen={onOpenTask} />)}
                {!column.tasks.length && <p className="rounded-xl border border-dashed border-line p-5 text-center text-sm text-muted">Thả task vào đây.</p>}
              </div>
            </div>
          })}
      </div>
    </div>}
  </section>
}

export function ScrumBoardView({
  projectId,
  backlogItems,
  sprints,
  sprintItems,
  kanbanBoards,
  selectedSprintId,
  sprintStatistics,
  sprintBurndown,
  sprintCapacity,
  sprintHealth,
  sprintRisks,
  sprintProgress,
  sprintTaskRiskSummary,
  selectedTask,
  taskComments,
  taskTimeLogs,
  taskTimeSummary,
  taskImportResult,
  taskDependencies,
  taskRisk,
  members,
  loading,
  taskDetailLoading,
  saving,
  canManage,
  onCreateBacklog,
  onCreateSprint,
  onUpdateBacklog,
  onDeleteBacklog,
  onUpdateSprint,
  onDeleteSprint,
  onMoveToSprint,
  onMoveToBacklog,
  onStatusChange,
  onPriorityChange,
  onStartSprint,
  onCompleteSprint,
  onCancelSprint,
  onSelectSprint,
  onCreateTask,
  onUpdateTask,
  onDeleteTask,
  onTaskStatusChange,
  onAssignTask,
  onOpenTask,
  onCloseTask,
  onAddDependency,
  onRemoveDependency,
  onUnblockTask,
  onCreateTaskComment,
  onUpdateTaskComment,
  onDeleteTaskComment,
  onCreateTaskTimeLog,
  onUpdateTaskTimeLog,
  onDeleteTaskTimeLog,
  onDownloadTaskTemplate,
  onImportTasks,
  onClearTaskImportResult,
  onRefreshStatistics,
}: ScrumBoardViewProps) {
  const [backlogOpen, setBacklogOpen] = useState(false)
  const [sprintOpen, setSprintOpen] = useState(false)
  const [taskOpen, setTaskOpen] = useState(false)
  const [statisticsOpen, setStatisticsOpen] = useState(false)
  const [progressOpen, setProgressOpen] = useState(false)
  const [closingOpen, setClosingOpen] = useState(false)
  const [sprintEdit, setSprintEdit] = useState<Sprint | null>(null)
  const [confirmAction, setConfirmAction] = useState<{ title: string; description: string; confirmLabel: string; onConfirm: () => void } | null>(null)
  const [dragOver, setDragOver] = useState<string | null>(null)
  const [draggingId, setDraggingId] = useState<string | null>(null)
  const [taskBoardOpen, setTaskBoardOpen] = useState(false)

  const hasActiveSprint = sprints.some(s => s.status === 'ACTIVE')
  const currentBoard = selectedSprintId ? kanbanBoards[selectedSprintId] : undefined
  const sprintTasks = currentBoard ? currentBoard.columns.flatMap(col => col.tasks) : []

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
  const selectedSprintBacklogItems = selectedSprintId ? (sprintItems[selectedSprintId] ?? []) : []
  const askConfirm = (title: string, description: string, confirmLabel: string, onConfirm: () => void) =>
    setConfirmAction({ title, description, confirmLabel, onConfirm })
  const openTaskBoard = (sprintId: string) => {
    onSelectSprint(sprintId)
    setTaskBoardOpen(true)
  }

  return <section className="space-y-4">
    {taskBoardOpen ? (
      statisticsOpen ? (
        <SprintStatisticsView
          statistics={sprintStatistics}
          burndown={sprintBurndown}
          capacity={sprintCapacity}
          health={sprintHealth}
          risks={sprintRisks}
          taskRiskSummary={sprintTaskRiskSummary}
          onBack={() => setStatisticsOpen(false)}
          onRefresh={onRefreshStatistics && selectedSprintId ? () => onRefreshStatistics(selectedSprintId) : undefined}
        />
      ) : progressOpen ? (
        <SprintProgressView
          progress={sprintProgress}
          onBack={() => setProgressOpen(false)}
          onRefresh={onRefreshStatistics && selectedSprintId ? () => onRefreshStatistics(selectedSprintId) : undefined}
        />
      ) : closingOpen ? (
        <SprintClosingView
          projectId={projectId}
          sprintId={selectedSprintId || ''}
          members={members}
          canManage={canManage}
          onBack={() => setClosingOpen(false)}
        />
      ) : (
        <SprintTaskKanban
          sprints={sprints}
      selectedSprintId={selectedSprintId}
      board={selectedSprintId ? kanbanBoards[selectedSprintId] : undefined}
      userStories={selectedSprintBacklogItems}
      statistics={sprintStatistics}
      burndown={sprintBurndown}
      importResult={taskImportResult}
      canManage={canManage}
      onSelectSprint={onSelectSprint}
      onTaskStatusChange={onTaskStatusChange}
      onOpenTask={onOpenTask}
      onCreateTaskClick={() => setTaskOpen(true)}
      onDownloadTemplate={onDownloadTaskTemplate}
      onImportTasks={onImportTasks}
         onClearImportResult={onClearTaskImportResult}
         onBack={() => setTaskBoardOpen(false)}
         onOpenStatistics={() => setStatisticsOpen(true)}
         onOpenProgress={() => setProgressOpen(true)}
         onOpenClosing={() => setClosingOpen(true)}
       />
      )
    ) : <>
    <div className="flex flex-col gap-3 rounded-xl border border-brand-line/70 bg-brand-cream p-4 sm:flex-row sm:items-center sm:justify-between">
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
        className={`min-h-[560px] w-[340px] shrink-0 rounded-2xl border bg-gradient-to-b from-white to-[#f8fafc] p-3 transition ${dragOver === 'backlog' ? 'border-brand ring-2 ring-brand/20' : 'border-line'}`}
      >
        <div className="mb-3 flex items-center justify-between">
          <div><p className="font-bold">Product Backlog</p><p className="text-xs text-muted">{backlogItems.length} item chưa vào sprint</p></div>
        </div>
        <div className="space-y-3">
          {backlogItems.map(item => <BacklogCard key={item.id} item={item} canManage={canManage} dragging={draggingId === item.id} onDragStart={handleCardDragStart} onDragEnd={handleCardDragEnd} onUpdate={onUpdateBacklog} onDelete={onDeleteBacklog} onStatusChange={onStatusChange} onPriorityChange={onPriorityChange} onAskConfirm={askConfirm} />)}
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
          className={`min-h-[560px] w-[360px] shrink-0 rounded-2xl border bg-white p-3 transition hover:border-brand hover:shadow-lg ${dragOver === sprint.id ? 'border-brand ring-2 ring-brand/20' : 'border-brand-line/80'}`}
        >
          <div className="mb-4 relative rounded-2xl bg-[#1e1e1e] p-5 text-white shadow-xl">
            <div className="relative flex items-start justify-between gap-3">
              <div>
                <p className="text-lg font-bold text-white">{sprint.name}</p>
                <p className="mt-1.5 text-sm text-white/70">{sprint.goal || 'Chưa có mục tiêu sprint'}</p>
              </div>
              <span className="shrink-0 rounded-full border border-brand/50 bg-[#ff7849]/20 px-3 py-1 text-xs font-semibold text-brand shadow-sm">{sprintStatusLabels[sprint.status]}</span>
            </div>
            <div className="relative mt-4 flex items-center gap-2 text-sm font-medium text-white/80">
              <CalendarDays size={16} className="text-brand" />
              {formatDisplayDate(sprint.startDate)} <span className="text-white/50">→</span> {formatDisplayDate(sprint.endDate)}
            </div>
            
            {sprint.taskCount !== undefined && sprint.taskCount > 0 && (
              <div className="relative mt-4 pt-3 border-t border-white/10 space-y-1.5">
                <div className="flex items-center justify-between text-xs font-bold text-white/75">
                  <span>Tasks: {sprint.completedTaskCount} / {sprint.taskCount}</span>
                  <span 
                    className="font-extrabold"
                    style={{ color: `hsl(${((sprint.completionRate || 0) / 100) * 38}, 90%, 55%)` }}
                  >
                    {Math.round(sprint.completionRate || 0)}%
                  </span>
                </div>
                <div className="w-full h-1 bg-white/15 rounded-full overflow-hidden">
                  <div 
                    className="h-full rounded-full transition-all duration-500"
                    style={{ 
                      width: `${sprint.completionRate || 0}%`,
                      backgroundColor: `hsl(${((sprint.completionRate || 0) / 100) * 38}, 85%, 45%)`
                    }}
                  />
                </div>
              </div>
            )}

            <div className="relative mt-5 flex flex-wrap items-center gap-2">
              <Button size="sm" variant="primary" leadingIcon={<FolderKanban size={15} />} onClick={() => openTaskBoard(sprint.id)}>Mở Kanban</Button>
              {canManage && <>
                {sprint.status === 'PLANNING' && (
                  <Button
                    size="sm"
                    variant="solid-green"
                    leadingIcon={<Play size={15} />}
                    disabled={hasActiveSprint || !(sprintItems[sprint.id]?.length > 0)}
                    title={hasActiveSprint ? 'Đã có Sprint đang hoạt động' : !(sprintItems[sprint.id]?.length > 0) ? 'Sprint chưa có công việc nào' : ''}
                    onClick={() => askConfirm('Bắt đầu Sprint?', `Bạn có chắc chắn muốn bắt đầu Sprint "${sprint.name}" không? Bạn chỉ có thể chạy 1 Sprint tại một thời điểm.`, 'Bắt đầu', () => onStartSprint(sprint.id))}
                  >
                    Start
                  </Button>
                )}
                {sprint.status === 'ACTIVE' && (
                  <Button
                    size="sm"
                    variant="solid-blue"
                    leadingIcon={<Trophy size={15} />}
                    onClick={() => askConfirm('Hoàn thành Sprint?', `Bạn có chắc chắn muốn hoàn thành Sprint "${sprint.name}" không? Các công việc chưa hoàn thành (chưa DONE) sẽ được tự động chuyển về Product Backlog.`, 'Hoàn thành', () => onCompleteSprint(sprint.id))}
                  >
                    Done
                  </Button>
                )}
                {sprint.status !== 'COMPLETED' && sprint.status !== 'CANCELLED' && (
                  <Button
                    size="sm"
                    variant="solid-red"
                    onClick={() => askConfirm('Hủy Sprint?', `Bạn có chắc chắn muốn hủy Sprint "${sprint.name}" không? Các công việc chưa hoàn thành sẽ được tự động chuyển về Product Backlog.`, 'Xác nhận hủy', () => onCancelSprint(sprint.id))}
                  >
                    Hủy
                  </Button>
                )}
                {sprint.status !== 'CANCELLED' && sprint.status !== 'COMPLETED' && (
                  <ActionMenu tone="dark">
                    <ActionItem onClick={() => setSprintEdit(sprint)}><Pencil size={15} /> Sửa</ActionItem>
                    <ActionItem danger onClick={() => askConfirm('Xóa Sprint?', `Sprint "${sprint.name}" sẽ bị xóa mềm.`, 'Xóa Sprint', () => onDeleteSprint(sprint.id))}><Trash2 size={15} /> Xóa</ActionItem>
                  </ActionMenu>
                )}
              </>}
            </div>
          </div>
          <div className="space-y-3">
            {(sprintItems[sprint.id] ?? []).map(item => <BacklogCard key={item.id} item={item} canManage={canManage} dragging={draggingId === item.id} onDragStart={handleCardDragStart} onDragEnd={handleCardDragEnd} onUpdate={onUpdateBacklog} onDelete={onDeleteBacklog} onStatusChange={onStatusChange} onPriorityChange={onPriorityChange} onAskConfirm={askConfirm} />)}
            {!(sprintItems[sprint.id] ?? []).length && <p className="rounded-xl border border-dashed border-line p-6 text-center text-sm text-muted">Kéo backlog item vào Sprint này.</p>}
          </div>
        </div>
      ))}
    </div>
    </>}

    <CreateBacklogModal open={backlogOpen} saving={saving} onClose={() => setBacklogOpen(false)} onSave={data => { onCreateBacklog(data); setBacklogOpen(false) }} />
    <CreateSprintModal open={sprintOpen} saving={saving} onClose={() => setSprintOpen(false)} onSave={data => { onCreateSprint(data); setSprintOpen(false) }} />
    <Modal open={Boolean(sprintEdit)} title="Sửa Sprint" description="Chỉ Sprint đang lên kế hoạch mới được chỉnh sửa." onClose={() => setSprintEdit(null)} showClose={false}>
      <form className="space-y-4" onSubmit={event => {
        event.preventDefault()
        if (sprintEdit) onUpdateSprint(sprintEdit.id, { name: sprintEdit.name, goal: sprintEdit.goal ?? '', startDate: sprintEdit.startDate ?? undefined, endDate: sprintEdit.endDate ?? undefined })
        setSprintEdit(null)
      }}>
        <Input label="Tên Sprint" value={sprintEdit?.name ?? ''} onChange={event => setSprintEdit(current => current ? { ...current, name: event.target.value } : current)} />
        <Input label="Mục tiêu" value={sprintEdit?.goal ?? ''} onChange={event => setSprintEdit(current => current ? { ...current, goal: event.target.value } : current)} />
        <div className="grid gap-4 sm:grid-cols-2">
          <Input label="Ngày bắt đầu" type="date" value={sprintEdit?.startDate ?? ''} onChange={event => setSprintEdit(current => current ? { ...current, startDate: event.target.value } : current)} />
          <Input label="Ngày kết thúc" type="date" value={sprintEdit?.endDate ?? ''} onChange={event => setSprintEdit(current => current ? { ...current, endDate: event.target.value } : current)} />
        </div>
        <div className="flex justify-end gap-3">
          <Button type="button" variant="secondary" onClick={() => setSprintEdit(null)}>Hủy</Button>
          <Button type="submit" loading={saving}>Lưu Sprint</Button>
        </div>
      </form>
    </Modal>
    <CreateTaskModal open={taskOpen} saving={saving} backlogItems={selectedSprintBacklogItems} members={members} onClose={() => setTaskOpen(false)} onSave={data => { onCreateTask(data); setTaskOpen(false) }} />
    <TaskDetailModal
      task={selectedTask}
      comments={taskComments}
      timeLogs={taskTimeLogs}
      timeSummary={taskTimeSummary}
      dependencies={taskDependencies}
      risk={taskRisk}
      allTasks={sprintTasks}
      members={members}
      loading={taskDetailLoading}
      saving={saving}
      canManage={canManage}
      onClose={onCloseTask}
      onUpdateTask={onUpdateTask}
      onDeleteTask={onDeleteTask}
      onAssignTask={onAssignTask}
      onAddDependency={onAddDependency}
      onRemoveDependency={onRemoveDependency}
      onUnblockTask={onUnblockTask}
      onCreateComment={onCreateTaskComment}
      onUpdateComment={onUpdateTaskComment}
      onDeleteComment={onDeleteTaskComment}
      onCreateTimeLog={onCreateTaskTimeLog}
      onUpdateTimeLog={onUpdateTaskTimeLog}
      onDeleteTimeLog={onDeleteTaskTimeLog}
      onAskConfirm={askConfirm}
    />
    <ConfirmDialog
      open={Boolean(confirmAction)}
      title={confirmAction?.title ?? ''}
      description={confirmAction?.description ?? ''}
      confirmLabel={confirmAction?.confirmLabel}
      loading={saving}
      onCancel={() => setConfirmAction(null)}
      onConfirm={() => {
        confirmAction?.onConfirm()
        setConfirmAction(null)
      }}
    />
  </section>
}





