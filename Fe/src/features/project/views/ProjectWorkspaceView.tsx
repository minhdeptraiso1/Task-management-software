import { useEffect, useRef, useState, type FormEvent } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import {
  Activity,
  Bell,
  Bug,
  CalendarDays,
  CheckCheck,
  CheckCircle2,
  ChevronDown,
  ChevronLeft,
  ChevronRight,
  Clock,
  Filter,
  FolderKanban,
  LayoutDashboard,
  LogOut,
  Pencil,
  Plus,
  Search,
  Trash2,
  User as UserIcon,
  UserPlus,
  UsersRound,
  FileSpreadsheet,
  RotateCcw,
  Paperclip,
} from 'lucide-react'
import { ActionMenu, ActionItem, Button, ConfirmDialog, Input, Modal, Select, CollapsiblePanel } from '../../../components/ui'
import type { User } from '../../user/models/user.model'
import { UserGuideModal } from '../../user/views/UserGuideModal'
import {
  entityTypeLabels,
  projectActivityLabels,
  projectMemberRoleLabels,
  projectStatusLabels,
  type Project,
  type ProjectActivityPage,
  type ProjectFilters,
  type ProjectMember,
  type ProjectMemberRole,
  type ProjectPage,
  type ProjectStatus,
} from '../models/project.model'
import { formatDate } from '../../../utils/format'
import type { NotificationPage } from '../models/notification.model'
import type { BacklogItem, BacklogItemStatus, BacklogPriority, Sprint, SprintCapacityResponse, SprintHealthResponse, SprintRiskResponse, SprintProgress } from '../models/scrum.model'

const EXPO_OUT_EASE = [0.16, 1, 0.3, 1] as const

const sidebarVariants = {
  initial: { x: '-100%', opacity: 0 },
  animate: {
    x: '0%',
    opacity: 1,
    transition: {
      duration: 1.0,
      ease: EXPO_OUT_EASE,
    },
  },
}

const headerVariants = {
  initial: { y: -30, opacity: 0 },
  animate: {
    y: 0,
    opacity: 1,
    transition: {
      duration: 0.8,
      delay: 0.2,
      ease: EXPO_OUT_EASE,
    },
  },
}

const EASE_IN_CUBIC = [0.32, 0, 0.67, 0] as const

const contentTransitionVariants = {
  initial: { opacity: 0, x: 20 },
  animate: {
    opacity: 1,
    x: 0,
    transition: {
      duration: 0.3,
      ease: EXPO_OUT_EASE,
      staggerChildren: 0.05,
    },
  },
  exit: {
    opacity: 0,
    x: -20,
    transition: {
      duration: 0.2,
      ease: EASE_IN_CUBIC,
    },
  },
}
import type { KanbanBoard, SprintBurndown, SprintTaskStatistics, Task, TaskCommentPage, TaskImportResult, TaskPriority, TaskStatus, TaskTimeLogPage, TaskTimeSummary, TaskType, TaskDependency, TaskRisk, TaskRiskSummary } from '../models/task.model'
import type { ProjectActivityFilters } from '../services/project.service'
import { ScrumBoardView } from './ScrumBoardView'
import { PersonalDashboardController } from '../../dashboard/controllers/PersonalDashboardController'
import { ProjectDashboardTab } from './ProjectDashboardTab'
import { ProjectAttachmentsTab } from './ProjectAttachmentsTab'
import { ProjectTaskImportHistoryTab } from './ProjectTaskImportHistoryTab'
import { TimesheetView } from '../components/TimesheetView'
import { BugView } from '../components/BugView'
import { ProjectActivityDetailModal } from './ProjectActivityDetailModal'
import { searchProjects } from '../services/project.service'
import GlobalSearchModal from '../components/GlobalSearchModal'
import type { SearchResultItem } from '../models/search.model'

const statuses: ProjectStatus[] = ['PLANNING', 'ACTIVE', 'ON_HOLD', 'COMPLETED', 'CANCELLED', 'ARCHIVED']
const memberRoles: ProjectMemberRole[] = ['OWNER', 'PROJECT_MANAGER', 'SCRUM_MASTER', 'PRODUCT_OWNER', 'DEVELOPER', 'TESTER', 'VIEWER']

interface Props {
  user: User
  projects: ProjectPage
  selectedProject: Project | null
  members: ProjectMember[]
  activities: ProjectActivityPage
  notifications: NotificationPage
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
  onAddDependency: (taskId: string, dependsOnTaskId: string) => void
  onRemoveDependency: (taskId: string, dependencyId: string) => void
  onUnblockTask: (taskId: string, targetStatus: TaskStatus) => void
  candidateUsers: User[]
  unreadCount: number
  filters: ProjectFilters
  activeTab: 'board' | 'members' | 'activities' | 'notifications' | 'dashboard' | 'reports' | 'timesheet' | 'bugs' | 'attachments' | 'imports'
  page: number
  activityPage: number
  activityFilters: ProjectActivityFilters
  onActivityFiltersChange: (filters: ProjectActivityFilters) => void
  onActivitySearch: (filters: ProjectActivityFilters) => void
  loading: boolean
  detailLoading: boolean
  taskDetailLoading: boolean
  candidateLoading: boolean
  saving: boolean
  errors?: { id: string; message: string }[]
  onDismissError?: (id: string) => void
  onLogout: () => void
  onFiltersChange: (filters: ProjectFilters) => void
  onSearch: (forceFilters?: ProjectFilters) => void
  onPageChange: (page: number) => void
  onSelectProject: (project: Project) => void
  onCreateProject: (data: { code: string; name: string; description: string; startDate: string; endDate: string }) => void
  onUpdateProject: (data: { name?: string; description?: string; startDate?: string; endDate?: string }) => void
  onDeleteProject: () => void
  onStatusChange: (status: ProjectStatus) => void
  onTabChange: (tab: 'board' | 'members' | 'activities' | 'notifications' | 'dashboard' | 'reports' | 'timesheet' | 'bugs' | 'attachments' | 'imports') => void
  onActivityPageChange: (page: number) => void
  onAddMember: (userId: string, role: ProjectMemberRole) => void
  onCandidateSearch: (keyword: string) => void
  onRoleChange: (memberId: string, role: ProjectMemberRole) => void
  onRemoveMember: (member: ProjectMember) => void
  onReadNotification: (id: string) => void
  onDeleteNotification: (id: string) => void
  onReadAllNotifications: () => void
  onOpenSettings: () => void
  onCreateBacklog: Parameters<typeof ScrumBoardView>[0]['onCreateBacklog']
  onCreateSprint: Parameters<typeof ScrumBoardView>[0]['onCreateSprint']
  onUpdateBacklog: Parameters<typeof ScrumBoardView>[0]['onUpdateBacklog']
  onDeleteBacklog: Parameters<typeof ScrumBoardView>[0]['onDeleteBacklog']
  onUpdateSprint: Parameters<typeof ScrumBoardView>[0]['onUpdateSprint']
  onDeleteSprint: Parameters<typeof ScrumBoardView>[0]['onDeleteSprint']
  onMoveToSprint: (itemId: string, sprintId: string) => void
  onMoveToBacklog: (itemId: string, sprintId: string) => void
  onBacklogStatusChange: (itemId: string, status: BacklogItemStatus) => void
  onBacklogPriorityChange: (itemId: string, priority: BacklogPriority) => void
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
  onCreateTaskComment: (taskId: string, content: string, parentCommentId?: string) => void
  onUpdateTaskComment: (taskId: string, commentId: string, content: string) => void
  onDeleteTaskComment: (taskId: string, commentId: string) => void
  onCreateTaskTimeLog: (taskId: string, data: { workDate: string; minutes: number; description?: string }) => void
  onUpdateTaskTimeLog: (taskId: string, timeLogId: string, data: { workDate?: string; minutes?: number; description?: string }) => void
  onDeleteTaskTimeLog: (taskId: string, timeLogId: string) => void
  onDownloadTaskTemplate: (sprintId: string) => void
  onImportTasks: (sprintId: string, file: File) => void
  onClearTaskImportResult: () => void
  onRefreshSprintStats?: (sprintId: string) => void
  onExportSprintTasks: (sprintId: string) => void
  openBugId?: string | null
  onCloseBug?: () => void
  onSelectSearchResult: (result: SearchResultItem) => void
}

function statusClass(status: ProjectStatus) {
  if (status === 'ACTIVE') return 'bg-success/10 text-success'
  if (status === 'CANCELLED') return 'bg-danger/10 text-danger'
  if (status === 'COMPLETED') return 'bg-info/10 text-info'
  if (status === 'ARCHIVED') return 'bg-panel text-muted'
}

function getProjectStatusBadgeStyle(status: ProjectStatus) {
  switch (status) {
    case 'ACTIVE':
      return 'bg-success/10 text-success border-success/20 hover:bg-success/20 shadow-2xs'
    case 'COMPLETED':
      return 'bg-info/10 text-info border-info/20 hover:bg-info/20 shadow-2xs'
    case 'CANCELLED':
      return 'bg-danger/10 text-danger border-danger/20 hover:bg-danger/20 shadow-2xs'
    case 'ARCHIVED':
    default:
      return 'bg-panel text-muted border-line hover:bg-panel/80 shadow-2xs'
  }
}

function getProjectStatusDotColor(status: ProjectStatus) {
  switch (status) {
    case 'ACTIVE': return 'bg-success'
    case 'COMPLETED': return 'bg-info'
    case 'CANCELLED': return 'bg-danger'
    case 'ARCHIVED': default: return 'bg-muted'
  }
}

function getProjectStatusCardStyle(st: ProjectStatus, isCurrent: boolean) {
  if (isCurrent) {
    switch (st) {
      case 'ACTIVE': return 'bg-success/10 border-success/40 ring-2 ring-success/20 shadow-xs'
      case 'COMPLETED': return 'bg-info/10 border-info/40 ring-2 ring-info/20 shadow-xs'
      case 'CANCELLED': return 'bg-danger/10 border-danger/40 ring-2 ring-danger/20 shadow-xs'
      case 'ARCHIVED': default: return 'bg-panel border-line ring-2 ring-muted/20 shadow-xs'
    }
  }
  switch (st) {
    case 'ACTIVE': return 'bg-success/5 border-success/20 hover:border-success/40 hover:bg-success/10'
    case 'COMPLETED': return 'bg-info/5 border-info/20 hover:border-info/40 hover:bg-info/10'
    case 'CANCELLED': return 'bg-danger/5 border-danger/20 hover:border-danger/40 hover:bg-danger/10'
    case 'ARCHIVED': default: return 'bg-canvas border-line hover:border-line/80 hover:bg-panel/70'
  }
}

function ProjectCreateModal({ open, saving, onClose, onSave }: { open: boolean; saving: boolean; onClose: () => void; onSave: Props['onCreateProject'] }) {
  const [code, setCode] = useState('')
  const [name, setName] = useState('')
  const [description, setDescription] = useState('')
  const [startDate, setStartDate] = useState('')
  const [endDate, setEndDate] = useState('')

  const submit = (event: FormEvent) => {
    event.preventDefault()
    onSave({ code, name, description, startDate, endDate })
  }

  return <Modal open={open} onClose={onClose} title="Tạo dự án mới" description="Người tạo dự án sẽ tự động là OWNER." showClose={false}>
    <form className="space-y-4" onSubmit={submit}>
      <div className="grid gap-4 sm:grid-cols-2">
        <Input label="Mã dự án" value={code} onChange={event => setCode(event.target.value)} required placeholder="HICAS-ERP" />
        <Input label="Tên dự án" value={name} onChange={event => setName(event.target.value)} required placeholder="Hệ thống ERP nội bộ" />
      </div>
      <div>
        <label className="mb-2 block text-sm font-medium text-ink">Mô tả</label>
        <textarea className="min-h-28 w-full rounded-lg border border-line bg-white px-3.5 py-3 text-sm outline-none focus:border-brand focus:ring-2 focus:ring-brand/20" value={description} onChange={event => setDescription(event.target.value)} placeholder="Mục tiêu, phạm vi, ghi chú..." />
      </div>
      <div className="grid gap-4 sm:grid-cols-2">
        <Input label="Ngày bắt đầu" type="date" value={startDate} onChange={event => setStartDate(event.target.value)} />
        <Input label="Ngày kết thúc" type="date" value={endDate} onChange={event => setEndDate(event.target.value)} />
      </div>
      <div className="flex justify-end gap-3 pt-2">
        <Button type="button" variant="secondary" onClick={onClose}>Hủy</Button>
        <Button type="submit" loading={saving} leadingIcon={<Plus size={17} />}>Tạo dự án</Button>
      </div>
    </form>
  </Modal>
}
function ProjectEditModal({ open, project, saving, onClose, onSave }: { open: boolean; project: Project | null; saving: boolean; onClose: () => void; onSave: Props['onUpdateProject'] }) {
  if (!project) return null

  const submit = (event: FormEvent) => {
    event.preventDefault()
    const form = new FormData(event.currentTarget as HTMLFormElement)
    const name = String(form.get('name') ?? '')
    const description = String(form.get('description') ?? '')
    const startDate = String(form.get('startDate') ?? '')
    const endDate = String(form.get('endDate') ?? '')
    onSave({ name, description, startDate: startDate || undefined, endDate: endDate || undefined })
  }

  return <Modal open={open} onClose={onClose} title="Sửa dự án" description="Cập nhật thông tin tổng quan của dự án." showClose={false}>
    <form className="space-y-4" onSubmit={submit}>
      <Input name="name" label="Tên dự án" defaultValue={project.name ?? ''} required />
      <div>
        <label className="mb-2 block text-sm font-medium text-ink">Mô tả</label>
        <textarea name="description" className="min-h-28 w-full rounded-lg border border-line bg-white px-3.5 py-3 text-sm outline-none focus:border-brand focus:ring-2 focus:ring-brand/20" defaultValue={project.description ?? ''} />
      </div>
      <div className="grid gap-4 sm:grid-cols-2">
        <Input name="startDate" label="Ngày bắt đầu" type="date" defaultValue={project.startDate ?? ''} />
        <Input name="endDate" label="Ngày kết thúc" type="date" defaultValue={project.endDate ?? ''} />
      </div>
      <div className="flex justify-end gap-3">
        <Button type="button" variant="secondary" onClick={onClose}>Hủy</Button>
        <Button type="submit" loading={saving}>Lưu dự án</Button>
      </div>
    </form>
  </Modal>
}
function AddMemberForm({
  candidates,
  saving,
  loading,
  onSearch,
  onAdd,
}: {
  candidates: User[]
  saving: boolean
  loading: boolean
  onSearch: (keyword: string) => void
  onAdd: Props['onAddMember']
}) {
  const [keyword, setKeyword] = useState('')
  const [userId, setUserId] = useState('')
  const [role, setRole] = useState<ProjectMemberRole>('DEVELOPER')

  const submit = (event: FormEvent) => {
    event.preventDefault()
    onAdd(userId, role)
    setUserId('')
    setKeyword('')
  }

  return <form className="space-y-3 rounded-xl border border-line bg-canvas p-4" onSubmit={submit}>
    <div className="grid gap-3 md:grid-cols-[1fr_auto]">
      <Input
        aria-label="Tìm tài khoản"
        value={keyword}
        onChange={event => setKeyword(event.target.value)}
        onKeyDown={event => {
          if (event.key === 'Enter') {
            event.preventDefault()
            onSearch(keyword)
          }
        }}
        placeholder="Tìm username hoặc email để thêm vào dự án"
      />
      <Button type="button" variant="secondary" loading={loading} leadingIcon={<Search size={17} />} onClick={() => onSearch(keyword)}>Tìm</Button>
    </div>
    <div className="grid gap-3 md:grid-cols-[1fr_220px_auto]">
      <Select
        aria-label="Tài khoản"
        value={userId}
        onChange={event => setUserId(event.target.value)}
        required
        options={[
          { label: candidates.length ? 'Chọn tài khoản' : 'Chưa có kết quả tìm kiếm', value: '' },
          ...candidates.map(candidate => ({ label: `${candidate.username} · ${candidate.email} · ${candidate.role}`, value: candidate.id })),
        ]}
      />
      <Select
        aria-label="Vai trò dự án"
        value={role}
        onChange={event => setRole(event.target.value as ProjectMemberRole)}
        options={memberRoles.filter(item => item !== 'OWNER').map(item => ({ label: projectMemberRoleLabels[item], value: item }))}
      />
      <Button type="submit" loading={saving} disabled={!userId} leadingIcon={<UserPlus size={17} />}>Thêm</Button>
    </div>
  </form>
}

export function ProjectWorkspaceView({
  user,
  projects,
  selectedProject,
  members,
  activities,
  notifications,
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
  candidateUsers,
  unreadCount,
  filters,
  activeTab,
  page,
  activityPage,
  activityFilters,
  onActivityFiltersChange,
  onActivitySearch,
  loading,
  detailLoading,
  taskDetailLoading,
  candidateLoading,
  saving,
  errors: _errors,
  onDismissError: _onDismissError,
  onLogout,
  onFiltersChange,
  onSearch,
  onPageChange,
  onSelectProject,
  onCreateProject,
  onUpdateProject,
  onDeleteProject,
  onStatusChange,
  onTabChange,
  onActivityPageChange,
  onAddMember,
  onCandidateSearch,
  onRoleChange,
  onRemoveMember,
  onReadNotification,
  onDeleteNotification,
  onReadAllNotifications,
  onOpenSettings,
  onCreateBacklog,
  onCreateSprint,
  onUpdateBacklog,
  onDeleteBacklog,
  onUpdateSprint,
  onDeleteSprint,
  onMoveToSprint,
  onMoveToBacklog,
  onBacklogStatusChange,
  onBacklogPriorityChange,
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
  onRefreshSprintStats,
  onExportSprintTasks,
  openBugId,
  onCloseBug,
  onSelectSearchResult,
}: Props) {
  const [globalSearchOpen, setGlobalSearchOpen] = useState(false)
  const [showProjectAdvancedFilters, setShowProjectAdvancedFilters] = useState(false)
  const [createOpen, setCreateOpen] = useState(false)
  const [projectEditOpen, setProjectEditOpen] = useState(false)
  const [deleteProjectOpen, setDeleteProjectOpen] = useState(false)
  const [projectStatusModalOpen, setProjectStatusModalOpen] = useState(false)
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false)
  const [selectedActivityId, setSelectedActivityId] = useState<string | null>(null)
  const [isActivityFilterVisible, setIsActivityFilterVisible] = useState(false)
  
  const [showSuggestions, setShowSuggestions] = useState(false)
  const [recentSearches, setRecentSearches] = useState<string[]>([])
  const [suggestionProjects, setSuggestionProjects] = useState<Project[]>([])
  const [focusedIndex, setFocusedIndex] = useState(-1)
  const searchContainerRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    setFocusedIndex(-1)
  }, [filters.keyword, showSuggestions])

  useEffect(() => {
    setIsActivityFilterVisible(false)
  }, [selectedProject?.id])

  useEffect(() => {
    const saved = localStorage.getItem('recentProjectSearches')
    if (saved) {
      try {
        setRecentSearches(JSON.parse(saved))
      } catch (e) {
        // ignore
      }
    }
  }, [])

  // Listen for Ctrl+K / Cmd+K to open global search
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
        e.preventDefault()
        setGlobalSearchOpen(prev => !prev)
      }
    }
    window.addEventListener('keydown', handleKeyDown)
    return () => window.removeEventListener('keydown', handleKeyDown)
  }, [])

  useEffect(() => {
    const keyword = filters.keyword?.trim() || ''
    if (!keyword) {
      setSuggestionProjects([])
      return
    }
    
    const timeoutId = setTimeout(() => {
      searchProjects({ keyword, status: '' }, 0, 5)
        .then(res => setSuggestionProjects(res.content))
        .catch(() => setSuggestionProjects([]))
    }, 300)
    
    return () => clearTimeout(timeoutId)
  }, [filters.keyword])

  const handleSearch = (overrideKeyword?: string | React.MouseEvent | React.KeyboardEvent | React.FormEvent) => {
    const keywordStr = typeof overrideKeyword === 'string' ? overrideKeyword : (filters.keyword?.trim() || '')
    if (keywordStr) {
      const newRecent = [keywordStr, ...recentSearches.filter(s => s !== keywordStr)].slice(0, 5)
      setRecentSearches(newRecent)
      localStorage.setItem('recentProjectSearches', JSON.stringify(newRecent))
    }
    setShowSuggestions(false)
    if (typeof overrideKeyword === 'string') {
      onFiltersChange({ ...filters, keyword: overrideKeyword })
      onSearch({ ...filters, keyword: overrideKeyword })
    } else {
      onSearch()
    }
  }

  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (searchContainerRef.current && !searchContainerRef.current.contains(e.target as Node)) {
        setShowSuggestions(false)
      }
    }
    document.addEventListener('mousedown', handleClickOutside)
    return () => document.removeEventListener('mousedown', handleClickOutside)
  }, [])

  const canCreateProject = user.role === 'MANAGER'
  const canManageMembers = selectedProject?.currentUserRole === 'OWNER' || selectedProject?.currentUserRole === 'PROJECT_MANAGER'
  const canManageTasks = !!selectedProject?.currentUserRole
  const [guideModalOpen, setGuideModalOpen] = useState(false)

  const keywordStr = filters.keyword?.trim() || ''
  const dropdownOptions = keywordStr
    ? suggestionProjects.map(p => ({ type: 'project' as const, label: p.name, value: p.name, code: p.code, id: p.id }))
    : recentSearches.map(term => ({ type: 'recent' as const, label: term, value: term, code: '', id: term }))

  return <div className="min-h-screen bg-canvas">
    <motion.header
      variants={headerVariants}
      initial="initial"
      animate="animate"
      style={{ willChange: 'transform, opacity', transform: 'translateZ(0)' }}
      className="flex h-14 items-center justify-between bg-brand-black px-5 text-white shadow-sm md:px-6 sticky top-0 z-30"
    >
      <div className="flex items-center gap-3 font-bold">
        <span className="text-2xl tracking-[-.08em]">HI<span className="text-brand">CAS</span></span>
        <span className="h-6 w-px bg-white/20" />
        <span className="text-sm text-white/55">PROJECT</span>
      </div>
      
      <div className="flex-1 max-w-md mx-6 hidden sm:block">
        <button
          type="button"
          onClick={() => setGlobalSearchOpen(true)}
          className="w-full flex items-center gap-2 rounded-lg border border-white/20 bg-white/10 px-3.5 py-2 text-left text-xs text-white/50 hover:bg-white/15 hover:text-white/80 transition-all cursor-pointer"
        >
          <Search size={14} className="shrink-0 text-white/50" />
          <span>Tìm kiếm dự án, task, bug... (Ctrl + K)</span>
          <kbd className="ml-auto rounded bg-white/10 px-1.5 py-0.5 text-[9px] font-bold text-white/40 border border-white/5">Ctrl K</kbd>
        </button>
      </div>

      <div className="flex items-center gap-3">
        <button 
          type="button" 
          onClick={() => setGlobalSearchOpen(true)}
          className="sm:hidden grid size-8 place-items-center rounded-full hover:bg-white/10 text-white/80 transition mr-1 cursor-pointer"
          title="Tìm kiếm"
        >
          <Search size={18} />
        </button>
        <button type="button" className="flex items-center gap-2 rounded-full p-1 hover:bg-white/10 transition" onClick={onOpenSettings}>
          <div className="flex h-8 w-8 items-center justify-center rounded-full bg-brand font-bold text-white uppercase text-sm">
            {user.username.charAt(0)}
          </div>
          <span className="hidden text-sm font-medium md:block pl-1 pr-2">{user.username}</span>
        </button>
        <div className="h-6 w-px bg-white/20 mx-1" />
        <Button className="!text-white/80 hover:!text-white hover:bg-white/10" variant="ghost" size="sm" leadingIcon={<LogOut size={17} />} onClick={onLogout}>Đăng xuất</Button>
      </div>
    </motion.header>

    <main className={`grid gap-5 p-5 transition-[grid-template-columns] duration-300 items-start grid-cols-1 ${sidebarCollapsed ? 'lg:grid-cols-[76px_minmax(0,1fr)]' : 'lg:grid-cols-[320px_minmax(0,1fr)] xl:grid-cols-[380px_minmax(0,1fr)]'}`}>
      <motion.aside
        variants={sidebarVariants}
        initial="initial"
        animate="animate"
        style={{ willChange: 'transform, opacity', transform: 'translateZ(0)' }}
        className={`sticky top-5 flex h-auto lg:h-[calc(100vh-96px)] flex-col rounded-xl border border-line bg-white transition-all duration-300 overflow-x-hidden ${sidebarCollapsed ? 'overflow-hidden' : ''}`}
      >
        <div className="shrink-0 border-b border-line p-5">
          <div className="flex items-center justify-between gap-3">
            {!sidebarCollapsed && <div>
              <h1 className="text-xl font-bold">Dự án của tôi</h1>
            </div>}
            <div className="flex items-center gap-1.5">
              {!sidebarCollapsed && (
                <Button
                  variant="secondary"
                  size="sm"
                  iconOnly
                  onClick={() => setShowProjectAdvancedFilters(!showProjectAdvancedFilters)}
                  className={`${showProjectAdvancedFilters ? '!bg-brand/10 !text-brand border-brand/20' : ''}`}
                  title="Bộ lọc nâng cao"
                  aria-label="Bộ lọc nâng cao"
                  leadingIcon={<Filter size={16} />}
                />
              )}
              <Button
                variant="secondary"
                size="sm"
                iconOnly
                leadingIcon={sidebarCollapsed ? <ChevronRight size={16} /> : <ChevronLeft size={16} />}
                aria-label={sidebarCollapsed ? 'Mở rộng danh sách dự án' : 'Thu gọn danh sách dự án'}
                onClick={() => setSidebarCollapsed(value => !value)}
              />
            </div>
          </div>
          {!sidebarCollapsed && <div className="mt-4 grid gap-3">
            <div className="relative" ref={searchContainerRef}>
              <Input 
                aria-label="Tìm dự án" 
                placeholder="Tìm mã hoặc tên..." 
                leadingIcon={<Search size={17} />} 
                value={filters.keyword} 
                onChange={event => {
                  onFiltersChange({ ...filters, keyword: event.target.value })
                  setShowSuggestions(true)
                }} 
                onFocus={() => setShowSuggestions(true)}
                onKeyDown={event => {
                  if (event.key === 'ArrowDown') {
                    event.preventDefault()
                    if (showSuggestions) {
                      setFocusedIndex(prev => (prev < dropdownOptions.length - 1 ? prev + 1 : prev))
                    } else {
                      setShowSuggestions(true)
                    }
                  } else if (event.key === 'ArrowUp') {
                    event.preventDefault()
                    if (showSuggestions) {
                      setFocusedIndex(prev => (prev > 0 ? prev - 1 : -1))
                    }
                  } else if (event.key === 'Enter') {
                    event.preventDefault()
                    if (showSuggestions && focusedIndex >= 0 && focusedIndex < dropdownOptions.length) {
                      handleSearch(dropdownOptions[focusedIndex].value)
                    } else {
                      handleSearch()
                    }
                  }
                }} 
              />
              
              {showSuggestions && dropdownOptions.length > 0 && (
                <div className="absolute top-full left-0 mt-1 w-full max-h-72 overflow-y-auto rounded-lg border border-line bg-white py-2 shadow-lg z-50">
                  {keywordStr ? (
                    <>
                      {dropdownOptions.map((option, idx) => (
                          <button 
                            key={option.id}
                            type="button"
                            className={`w-full px-4 py-2 text-left text-sm flex flex-col gap-0.5 ${focusedIndex === idx ? 'bg-slate-100' : 'hover:bg-slate-50'}`}
                            onClick={() => handleSearch(option.value)}
                            onMouseEnter={() => setFocusedIndex(idx)}
                          >
                            <div className="flex items-center gap-2 font-medium text-ink">
                              <Search size={13} className="text-muted flex-shrink-0" />
                              <span className="truncate">{option.label}</span>
                            </div>
                            <div className="pl-5 text-xs text-muted truncate">{option.code}</div>
                          </button>
                        ))}
                    </>
                  ) : (
                    <>
                      <div className="px-4 py-1 text-xs font-semibold text-muted uppercase">Tìm kiếm gần đây</div>
                      {dropdownOptions.map((option, idx) => (
                        <button 
                          key={option.id}
                          type="button"
                          className={`w-full px-4 py-2 text-left text-sm flex items-center gap-2 ${focusedIndex === idx ? 'bg-slate-100' : 'hover:bg-slate-50'}`}
                          onClick={() => handleSearch(option.value)}
                          onMouseEnter={() => setFocusedIndex(idx)}
                        >
                          <Clock size={14} className="text-muted" />
                          {option.label}
                        </button>
                      ))}
                    </>
                  )}
                </div>
              )}
            </div>
            <div className="grid gap-3 sm:grid-cols-[1fr_auto]">
              <Select aria-label="Trạng thái" value={filters.status} onChange={event => onFiltersChange({ ...filters, status: event.target.value as ProjectFilters['status'] })} options={[{ label: 'Mọi trạng thái', value: '' }, ...statuses.map(status => ({ label: projectStatusLabels[status], value: status }))]} />
              <div className="flex gap-2 justify-end">
                <Button type="button" variant="secondary" loading={loading} leadingIcon={<Search size={17} />} onClick={() => handleSearch()}>Tìm</Button>
                {canCreateProject && <Button type="button" leadingIcon={<Plus size={17} />} onClick={() => setCreateOpen(true)}>Tạo</Button>}
              </div>
            </div>

            <CollapsiblePanel
              open={showProjectAdvancedFilters}
              innerClassName="mt-3 p-3.5 bg-canvas border border-line rounded-xl space-y-3.5 text-xs"
            >
              <div className="grid gap-3 sm:grid-cols-2">
                <div>
                  <label className="block text-[11px] font-extrabold text-muted-dark mb-1">Bắt đầu từ ngày</label>
                  <Input type="date" value={filters.startDateFrom || ''} onChange={e => onFiltersChange({ ...filters, startDateFrom: e.target.value || undefined })} />
                </div>
                <div>
                  <label className="block text-[11px] font-extrabold text-muted-dark mb-1">Đến ngày</label>
                  <Input type="date" value={filters.startDateTo || ''} onChange={e => onFiltersChange({ ...filters, startDateTo: e.target.value || undefined })} />
                </div>
              </div>

              <div className="grid gap-3 sm:grid-cols-2">
                <div>
                  <label className="block text-[11px] font-extrabold text-muted-dark mb-1">Kết thúc từ ngày</label>
                  <Input type="date" value={filters.endDateFrom || ''} onChange={e => onFiltersChange({ ...filters, endDateFrom: e.target.value || undefined })} />
                </div>
                <div>
                  <label className="block text-[11px] font-extrabold text-muted-dark mb-1">Đến ngày</label>
                  <Input type="date" value={filters.endDateTo || ''} onChange={e => onFiltersChange({ ...filters, endDateTo: e.target.value || undefined })} />
                </div>
              </div>

              <div className="grid gap-3 sm:grid-cols-2">
                <div>
                  <label className="block text-[11px] font-extrabold text-muted-dark mb-1">Ngày tạo từ ngày</label>
                  <Input type="date" value={filters.createdFrom || ''} onChange={e => onFiltersChange({ ...filters, createdFrom: e.target.value ? new Date(e.target.value).toISOString() : undefined })} />
                </div>
                <div>
                  <label className="block text-[11px] font-extrabold text-muted-dark mb-1">Đến ngày</label>
                  <Input type="date" value={filters.createdTo || ''} onChange={e => onFiltersChange({ ...filters, createdTo: e.target.value ? new Date(new Date(e.target.value).setHours(23, 59, 59, 999)).toISOString() : undefined })} />
                </div>
              </div>

              <div className="flex justify-end gap-3 pt-2.5 border-t border-line/60">
                <button
                  type="button"
                  onClick={() => {
                    onFiltersChange({
                      keyword: filters.keyword,
                      status: filters.status
                    })
                    setTimeout(() => handleSearch(), 50)
                  }}
                  className="text-muted hover:text-ink font-semibold"
                >
                  Đặt lại
                </button>
                <button
                  type="button"
                  onClick={() => handleSearch()}
                  className="text-brand hover:text-brand-dark font-bold"
                >
                  Áp dụng
                </button>
              </div>
            </CollapsiblePanel>
          </div>}
        </div>

        {sidebarCollapsed ? <div className={`flex-1 space-y-2 overflow-y-auto p-2 ${loading ? 'opacity-50' : ''}`}>
          <button
            type="button"
            title="Trang chủ"
            className={`grid size-12 place-items-center rounded-xl border transition hover:border-brand ${!selectedProject ? 'border-brand bg-[#fff7ed] text-brand' : 'border-line bg-white text-muted'}`}
            onClick={() => onSelectProject(null as any)}
          >
            <LayoutDashboard size={20} />
          </button>
          {projects.content.map(project => (
            <button
              key={project.id}
              type="button"
              title={project.name}
              className={`grid size-12 place-items-center rounded-xl border text-xs font-bold uppercase transition hover:border-brand ${selectedProject?.id === project.id ? 'border-brand bg-[#fff7ed] text-brand' : 'border-line bg-white text-muted'}`}
              onClick={() => onSelectProject(project)}
            >
              {project.code.slice(0, 2)}
            </button>
          ))}
        </div> : <>
        <div className={`flex-1 overflow-y-auto p-3 ${loading ? 'opacity-50' : ''}`}>
          <button 
            type="button" 
            className={`mb-3 w-full rounded-2xl border p-4 text-left transition-all duration-200 hover:-translate-y-0.5 hover:shadow-md ${!selectedProject ? 'border-brand bg-brand-cream/60 text-brand-dark shadow-xs font-bold' : 'border-line/70 bg-white hover:border-brand/40'}`} 
            onClick={() => onSelectProject(null as any)}
          >
            <div className="flex items-center gap-3 font-bold text-sm">
              <div className="grid size-9 place-items-center rounded-xl bg-amber-50 text-amber-600 border border-amber-100">
                <LayoutDashboard size={18} />
              </div>
              <span>Trang tổng quan</span>
            </div>
          </button>

          {projects.content.map(project => (
            <button 
              key={project.id} 
              type="button" 
              className={`mb-3 w-full rounded-2xl border p-4 text-left transition-all duration-200 hover:-translate-y-0.5 hover:shadow-md ${selectedProject?.id === project.id ? 'border-brand bg-brand-cream/40 shadow-xs' : 'border-line/70 bg-white hover:border-brand/40'}`} 
              onClick={() => onSelectProject(project)}
            >
              <div className="flex items-start justify-between gap-3">
                <div className="min-w-0 flex-1">
                  <span className="text-[11px] font-extrabold uppercase tracking-wider text-brand">
                    {project.code}
                  </span>
                  <h4 className="mt-1 font-bold text-ink text-sm leading-snug line-clamp-2" title={project.name}>
                    {project.name}
                  </h4>
                </div>
                <span className={`shrink-0 rounded-full px-2.5 py-0.5 text-[10px] font-extrabold shadow-2xs ${statusClass(project.status)}`}>
                  {projectStatusLabels[project.status]}
                </span>
              </div>
              <div className="mt-3 flex items-center justify-between border-t border-line/50 pt-2.5 text-xs text-muted">
                <span className="inline-flex items-center gap-1 font-medium"><CalendarDays size={13} className="text-muted" />{formatDate(project.startDate)}</span>
                {project.currentUserRole && (
                  <span className="font-semibold text-muted-dark text-[11px]">
                    {projectMemberRoleLabels[project.currentUserRole]}
                  </span>
                )}
              </div>
            </button>
          ))}
          {!projects.content.length && !loading && <p className="py-10 text-center text-sm text-muted">Chưa có dự án phù hợp.</p>}
        </div>
        <footer className="flex items-center justify-between border-t border-line px-4 py-3 text-sm text-muted">
          <span>Trang {page + 1} / {Math.max(projects.totalPages, 1)}</span>
          <div className="flex gap-2">
            <Button variant="secondary" size="sm" iconOnly disabled={page === 0} leadingIcon={<ChevronLeft size={16} />} aria-label="Trang trước" onClick={() => onPageChange(page - 1)} />
            <Button variant="secondary" size="sm" iconOnly disabled={page + 1 >= projects.totalPages} leadingIcon={<ChevronRight size={16} />} aria-label="Trang sau" onClick={() => onPageChange(page + 1)} />
          </div>
        </footer>
        </>}
      </motion.aside>

      <section className="min-w-0 flex-1 rounded-xl border border-line bg-white h-auto lg:h-[calc(100vh-96px)] overflow-y-auto overflow-x-hidden">
        <AnimatePresence mode="wait">
          {!selectedProject ? (
            <motion.div
              key="personal-dashboard"
              variants={contentTransitionVariants}
              initial="initial"
              animate="animate"
              exit="exit"
              style={{ willChange: 'transform, opacity', transform: 'translateZ(0)' }}
              className="min-h-full"
            >
              <PersonalDashboardController me={user} />
            </motion.div>
          ) : (
            <motion.div
              key={`${selectedProject.id}-${activeTab}`}
              variants={contentTransitionVariants}
              initial="initial"
              animate="animate"
              exit="exit"
              style={{ willChange: 'transform, opacity', transform: 'translateZ(0)' }}
              className="min-h-full"
            >
          {/* Bộ Menu Điều Hướng Tab Đẩy Lên Vị Trí Cao Nhất */}
          <div className="sticky top-0 z-20 border-b border-line bg-white/95 backdrop-blur-sm px-5 py-3 shadow-2xs">
            <div className="flex flex-wrap items-center justify-start gap-2">
              <motion.div whileHover={{ scale: 1.03 }} whileTap={{ scale: 0.96 }}>
                <Button variant="secondary" className={activeTab === 'dashboard' ? '!bg-brand !text-white !border-transparent shadow-xs' : ''} size="sm" leadingIcon={<LayoutDashboard size={16} />} onClick={() => onTabChange('dashboard')}>Tổng quan</Button>
              </motion.div>
              <motion.div whileHover={{ scale: 1.03 }} whileTap={{ scale: 0.96 }}>
                <Button variant="secondary" className={activeTab === 'board' ? '!bg-brand !text-white !border-transparent shadow-xs' : ''} size="sm" leadingIcon={<FolderKanban size={16} />} onClick={() => onTabChange('board')}>Sprint Board</Button>
              </motion.div>
              <motion.div whileHover={{ scale: 1.03 }} whileTap={{ scale: 0.96 }}>
                <Button variant="secondary" className={activeTab === 'members' ? '!bg-brand !text-white !border-transparent shadow-xs' : ''} size="sm" leadingIcon={<UsersRound size={16} />} onClick={() => onTabChange('members')}>Thành viên</Button>
              </motion.div>
              <motion.div whileHover={{ scale: 1.03 }} whileTap={{ scale: 0.96 }}>
                <Button variant="secondary" className={activeTab === 'activities' ? '!bg-brand !text-white !border-transparent shadow-xs' : ''} size="sm" leadingIcon={<Activity size={16} />} onClick={() => onTabChange('activities')}>Hoạt động</Button>
              </motion.div>
              <motion.div whileHover={{ scale: 1.03 }} whileTap={{ scale: 0.96 }}>
                <Button variant="secondary" className={activeTab === 'notifications' ? '!bg-brand !text-white !border-transparent shadow-xs' : ''} size="sm" leadingIcon={<Bell size={16} />} onClick={() => onTabChange('notifications')}>Thông báo {unreadCount ? `(${unreadCount})` : ''}</Button>
              </motion.div>
              <motion.div whileHover={{ scale: 1.03 }} whileTap={{ scale: 0.96 }}>
                <Button variant="secondary" className={activeTab === 'timesheet' ? '!bg-brand !text-white !border-transparent shadow-xs' : ''} size="sm" leadingIcon={<Clock size={16} />} onClick={() => onTabChange('timesheet')}>Timesheet</Button>
              </motion.div>
              <motion.div whileHover={{ scale: 1.03 }} whileTap={{ scale: 0.96 }}>
                <Button variant="secondary" className={activeTab === 'bugs' ? '!bg-brand !text-white !border-transparent shadow-xs' : ''} size="sm" leadingIcon={<Bug size={16} />} onClick={() => onTabChange('bugs')}>Quản lý Bug (QA)</Button>
              </motion.div>
              <motion.div whileHover={{ scale: 1.03 }} whileTap={{ scale: 0.96 }}>
                <Button variant="secondary" className={activeTab === 'attachments' ? '!bg-brand !text-white !border-transparent shadow-xs' : ''} size="sm" leadingIcon={<Paperclip size={16} />} onClick={() => onTabChange('attachments')}>Tài liệu & Bảo mật</Button>
              </motion.div>
              <motion.div whileHover={{ scale: 1.03 }} whileTap={{ scale: 0.96 }}>
                <Button variant="secondary" className={activeTab === 'imports' ? '!bg-brand !text-white !border-transparent shadow-xs' : ''} size="sm" leadingIcon={<FileSpreadsheet size={16} />} onClick={() => onTabChange('imports')}>Lịch sử Import</Button>
              </motion.div>
            </div>
          </div>

          {activeTab === 'dashboard' && (
            <div className="border-b border-line bg-white overflow-hidden rounded-t-xl">
              {/* 1. Banner Hình Nền Dự Án IT Cyber Tech & Neon Glow Grid (Tăng Kích Thước 160% Hoành Tráng) */}
              <div className="relative h-52 sm:h-64 md:h-72 w-full overflow-hidden bg-gradient-to-r from-slate-950 via-sky-950 to-blue-950">
                {/* IT Tech Wallpaper Background */}
                <div 
                  className="absolute inset-0 bg-cover bg-center opacity-85 mix-blend-screen"
                  style={{ backgroundImage: `url('https://images.unsplash.com/photo-1451187580459-43490279c0fa?auto=format&fit=crop&w=1600&q=80')` }}
                />
                
                {/* Neon Cyan Circuit Overlay matching user reference images */}
                <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_bottom_right,_var(--tw-gradient-stops))] from-cyan-500/30 via-blue-600/20 to-transparent" />
                
                {/* Cyber Grid Lines & Square Frame Overlay */}
                <div className="absolute inset-0 bg-[linear-gradient(to_right,#0284c720_1px,transparent_1px),linear-gradient(to_bottom,#0284c720_1px,transparent_1px)] bg-[size:32px_32px] [mask-image:radial-gradient(ellipse_75%_65%_at_50%_50%,#000_85%,transparent_100%)]" />

                {/* Laser Light Ray Effect */}
                <div className="absolute top-1/3 right-0 h-0.5 w-3/4 bg-gradient-to-l from-cyan-300 via-sky-400/70 to-transparent shadow-[0_0_28px_#38bdf8]" />

                <div className="absolute inset-0 bg-gradient-to-t from-slate-950/90 via-transparent to-black/30" />
                <div className="absolute -right-20 -top-20 size-96 rounded-full bg-cyan-400/25 blur-3xl" />
                <div className="absolute left-1/4 -bottom-24 size-80 rounded-full bg-blue-500/25 blur-3xl" />

                {/* Top Right Controls (Nút Tùy Chọn & Trạng thái Dự án) */}
                <div className="absolute top-4 right-4 z-10 flex flex-wrap items-center gap-2">
                  {canManageMembers && (
                    <ActionMenu label="Tùy chọn">
                      <ActionItem onClick={() => setProjectEditOpen(true)}>
                        <Pencil size={15} /> Sửa dự án
                      </ActionItem>
                      {selectedProject.currentUserRole === 'OWNER' && (
                        <ActionItem danger onClick={() => setDeleteProjectOpen(true)}>
                          <Trash2 size={15} /> Xóa dự án
                        </ActionItem>
                      )}
                    </ActionMenu>
                  )}
                  <button
                    type="button"
                    disabled={saving || detailLoading}
                    onClick={() => setProjectStatusModalOpen(true)}
                    className={`inline-flex items-center gap-2 rounded-full border px-4 py-2 text-xs font-extrabold shadow-sm transition-all cursor-pointer backdrop-blur-md ${getProjectStatusBadgeStyle(selectedProject.status)}`}
                    title="Bấm để thay đổi trạng thái dự án"
                  >
                    <span className={`size-2.5 rounded-full ${getProjectStatusDotColor(selectedProject.status)}`} />
                    <span>{projectStatusLabels[selectedProject.status]}</span>
                    <ChevronDown size={14} className="opacity-60 shrink-0" />
                  </button>
                </div>
              </div>

              {/* 2. Thẻ Thông Tin Dự Án (Tiêu Đề & Mô Tả Đặt Tỷ Lệ Phù Hợp) */}
              <div className="px-6 pb-6 pt-5">
                <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-3">
                  <div>
                    <div className="flex flex-wrap items-center gap-2 mb-2">
                      <span className="rounded-full bg-brand/10 border border-brand/20 px-3 py-0.5 text-xs font-extrabold text-brand-dark">
                        {selectedProject.code}
                      </span>
                      {selectedProject.currentUserRole && (
                        <span className="rounded-full bg-slate-100 border border-slate-200 px-3 py-0.5 text-xs font-bold text-slate-700">
                          {projectMemberRoleLabels[selectedProject.currentUserRole]}
                        </span>
                      )}
                    </div>
                    <h2 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight leading-snug">
                      {selectedProject.name}
                    </h2>
                  </div>
                </div>

                <p className="max-w-4xl text-sm sm:text-base leading-relaxed text-slate-600 font-medium mb-5">
                  {selectedProject.description || 'Chưa có mô tả chi tiết cho dự án này.'}
                </p>

                {/* 3. 3 Thẻ Metrics Thu Nhỏ 30% (Ngày Bắt Đầu, Ngày Kết Thúc, Số Thành Viên) */}
                <div className="grid gap-3 sm:grid-cols-3">
                  {/* Thẻ 1: Ngày Bắt Đầu */}
                  <div className="flex items-center gap-3 rounded-xl border border-emerald-100 bg-emerald-50/40 py-2.5 px-3.5 shadow-2xs transition-all hover:bg-emerald-50/70">
                    <div className="grid size-9 shrink-0 place-items-center rounded-lg bg-emerald-500 text-white shadow-xs">
                      <CalendarDays size={18} />
                    </div>
                    <div>
                      <p className="text-[10px] font-extrabold uppercase tracking-wider text-emerald-800">Ngày Bắt Đầu</p>
                      <p className="mt-0.5 text-xs sm:text-sm font-extrabold text-slate-800">
                        {formatDate(selectedProject.startDate)}
                      </p>
                    </div>
                  </div>

                  {/* Thẻ 2: Ngày Kết Thúc */}
                  <div className="flex items-center gap-3 rounded-xl border border-blue-100 bg-blue-50/40 py-2.5 px-3.5 shadow-2xs transition-all hover:bg-blue-50/70">
                    <div className="grid size-9 shrink-0 place-items-center rounded-lg bg-blue-500 text-white shadow-xs">
                      <Clock size={18} />
                    </div>
                    <div>
                      <p className="text-[10px] font-extrabold uppercase tracking-wider text-blue-800">Ngày Kết Thúc</p>
                      <p className="mt-0.5 text-xs sm:text-sm font-extrabold text-slate-800">
                        {formatDate(selectedProject.endDate)}
                      </p>
                    </div>
                  </div>

                  {/* Thẻ 3: Thành viên */}
                  <div className="flex items-center gap-3 rounded-xl border border-indigo-100 bg-indigo-50/40 py-2.5 px-3.5 shadow-2xs transition-all hover:bg-indigo-50/70">
                    <div className="grid size-9 shrink-0 place-items-center rounded-lg bg-indigo-600 text-white shadow-xs">
                      <UsersRound size={18} />
                    </div>
                    <div>
                      <p className="text-[10px] font-extrabold uppercase tracking-wider text-indigo-800">Tổng Thành Viên</p>
                      <p className="mt-0.5 text-xs sm:text-sm font-extrabold text-slate-800">
                        {members.length} thành viên
                      </p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          )}

          <div className={`p-5 ${detailLoading ? 'opacity-50' : ''}`}>
            {activeTab === 'dashboard' && <ProjectDashboardTab projectId={selectedProject.id} sprints={sprints} onOpenTask={onOpenTask} />}
            {activeTab === 'board' && <ScrumBoardView
              projectId={selectedProject.id}
              backlogItems={backlogItems}
              sprints={sprints}
              sprintItems={sprintItems}
              kanbanBoards={kanbanBoards}
              selectedSprintId={selectedSprintId}
              sprintStatistics={sprintStatistics}
              sprintBurndown={sprintBurndown}
              sprintCapacity={sprintCapacity}
              sprintHealth={sprintHealth}
              sprintRisks={sprintRisks}
              sprintProgress={sprintProgress}
              sprintTaskRiskSummary={sprintTaskRiskSummary}
              selectedTask={selectedTask}
              taskComments={taskComments}
              taskTimeLogs={taskTimeLogs}
              taskTimeSummary={taskTimeSummary}
              taskImportResult={taskImportResult}
              taskDependencies={taskDependencies}
              taskRisk={taskRisk}
              members={members}
              loading={detailLoading}
              taskDetailLoading={taskDetailLoading}
              saving={saving}
              canManage={canManageTasks}
              currentUserId={user.id}
              onCreateBacklog={onCreateBacklog}
              onCreateSprint={onCreateSprint}
              onUpdateBacklog={onUpdateBacklog}
              onDeleteBacklog={onDeleteBacklog}
              onUpdateSprint={onUpdateSprint}
              onDeleteSprint={onDeleteSprint}
              onMoveToSprint={onMoveToSprint}
              onMoveToBacklog={onMoveToBacklog}
              onStatusChange={onBacklogStatusChange}
              onPriorityChange={onBacklogPriorityChange}
              onStartSprint={onStartSprint}
              onCompleteSprint={onCompleteSprint}
              onCancelSprint={onCancelSprint}
              onSelectSprint={onSelectSprint}
              onCreateTask={onCreateTask}
              onUpdateTask={onUpdateTask}
              onDeleteTask={onDeleteTask}
              onTaskStatusChange={onTaskStatusChange}
              onAssignTask={onAssignTask}
              onOpenTask={onOpenTask}
              onCloseTask={onCloseTask}
              onAddDependency={onAddDependency}
              onRemoveDependency={onRemoveDependency}
              onUnblockTask={onUnblockTask}
              onCreateTaskComment={onCreateTaskComment}
              onUpdateTaskComment={onUpdateTaskComment}
              onDeleteTaskComment={onDeleteTaskComment}
              onCreateTaskTimeLog={onCreateTaskTimeLog}
              onUpdateTaskTimeLog={onUpdateTaskTimeLog}
              onDeleteTaskTimeLog={onDeleteTaskTimeLog}
              onDownloadTaskTemplate={onDownloadTaskTemplate}
              onImportTasks={onImportTasks}
              onClearTaskImportResult={onClearTaskImportResult}
              onRefreshStatistics={onRefreshSprintStats}
              onExportSprintTasks={onExportSprintTasks}
            />}

            {activeTab === 'members' && <div className="p-5">
              {canManageMembers && <AddMemberForm candidates={candidateUsers} saving={saving} loading={candidateLoading} onSearch={onCandidateSearch} onAdd={onAddMember} />}
              <div className="mt-4 overflow-x-auto rounded-xl border border-line">
                <table className="w-full min-w-[760px] text-left text-sm">
                  <thead className="bg-panel text-xs font-semibold uppercase tracking-wider text-[#3f3f46]">
                    <tr>
                      <th className="px-4 py-3">Thành viên</th>
                      <th className="px-4 py-3">Role hệ thống</th>
                      <th className="px-4 py-3">Role dự án</th>
                      {canManageMembers && <th className="px-4 py-3 text-center">Thao tác</th>}
                    </tr>
                  </thead>
                  <tbody>
                    {members.map(member => <tr key={member.id} className="border-t border-line">
                      <td className="px-4 py-3"><p className="font-semibold">{member.username}</p><p className="text-xs text-muted">{member.email}</p></td>
                      <td className="px-4 py-3"><span className="text-xs font-semibold text-slate-700">{member.systemRole}</span></td>
                      <td className="px-4 py-3">
                        {canManageMembers ? (
                          <Select aria-label="Role dự án" value={member.projectRole} onChange={event => onRoleChange(member.id, event.target.value as ProjectMemberRole)} options={memberRoles.map(role => ({ label: projectMemberRoleLabels[role], value: role }))} />
                        ) : (
                          <span className="inline-flex items-center px-2.5 py-1 rounded-md text-xs font-semibold bg-slate-100 text-slate-800 border border-slate-200">
                            {projectMemberRoleLabels[member.projectRole]}
                          </span>
                        )}
                      </td>
                      {canManageMembers && (
                        <td className="px-4 py-3 text-center">
                          <Button variant="ghost" size="sm" iconOnly className="!text-danger" leadingIcon={<Trash2 size={16} />} aria-label={`Xóa ${member.username}`} onClick={() => onRemoveMember(member)} />
                        </td>
                      )}
                    </tr>)}
                  </tbody>
                </table>
              </div>
            </div>}
            {activeTab === 'activities' && <div className="space-y-3">
              <div className="flex justify-between items-center mb-4">
                <h3 className="font-semibold text-lg">Lịch sử hoạt động</h3>
                <Button 
                  variant="secondary"
                  size="sm"
                  iconOnly
                  onClick={() => setIsActivityFilterVisible(!isActivityFilterVisible)}
                  title="Lọc dữ liệu"
                  aria-label="Lọc dữ liệu"
                  className={`!px-3 ${isActivityFilterVisible ? '!bg-brand/10 !text-brand border-brand/20' : ''}`}
                  leadingIcon={<Filter size={16} />}
                />
              </div>

              <CollapsiblePanel open={isActivityFilterVisible}>
                <div className="mb-4 rounded-2xl border border-slate-200/90 bg-white p-4 sm:p-5 shadow-sm space-y-4 animate-enter">
                  <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-3.5">
                    <div>
                      <label className="block text-[11px] font-bold text-muted-dark mb-1">Thành viên</label>
                      <Select
                        aria-label="Thành viên"
                        value={activityFilters.performedByUserId ?? ''}
                        onChange={e => onActivityFiltersChange({ ...activityFilters, performedByUserId: e.target.value })}
                        options={[
                          { label: 'Tất cả', value: '' },
                          ...members.map(m => ({ label: m.username, value: m.userId }))
                        ]}
                        className="w-full"
                      />
                    </div>
                    <div>
                      <label className="block text-[11px] font-bold text-muted-dark mb-1">Đối tượng</label>
                      <Select
                        aria-label="Đối tượng"
                        value={activityFilters.entityType ?? ''}
                        onChange={e => onActivityFiltersChange({ ...activityFilters, entityType: e.target.value })}
                        options={[
                          { label: 'Tất cả', value: '' },
                          ...Object.entries(entityTypeLabels).map(([key, label]) => ({ label, value: key }))
                        ]}
                        className="w-full"
                      />
                    </div>
                    <div>
                      <label className="block text-[11px] font-bold text-muted-dark mb-1">Hành động</label>
                      <Select
                        aria-label="Hành động"
                        value={activityFilters.action ?? ''}
                        onChange={e => onActivityFiltersChange({ ...activityFilters, action: e.target.value })}
                        options={[
                          { label: 'Tất cả', value: '' },
                          ...Object.entries(projectActivityLabels).map(([key, label]) => ({ label, value: key }))
                        ]}
                        className="w-full"
                      />
                    </div>
                    <div>
                      <label className="block text-[11px] font-bold text-muted-dark mb-1">Từ ngày</label>
                      <Input
                        type="date"
                        aria-label="Từ ngày"
                        value={activityFilters.fromDate ? activityFilters.fromDate.split('T')[0] : ''}
                        onChange={e => onActivityFiltersChange({ ...activityFilters, fromDate: e.target.value ? new Date(e.target.value).toISOString() : '' })}
                        className="w-full"
                      />
                    </div>
                    <div>
                      <label className="block text-[11px] font-bold text-muted-dark mb-1">Đến ngày</label>
                      <Input
                        type="date"
                        aria-label="Đến ngày"
                        value={activityFilters.toDate ? activityFilters.toDate.split('T')[0] : ''}
                        onChange={e => onActivityFiltersChange({ ...activityFilters, toDate: e.target.value ? new Date(new Date(e.target.value).setHours(23, 59, 59, 999)).toISOString() : '' })}
                        className="w-full"
                      />
                    </div>
                    <div>
                      <label className="block text-[11px] font-bold text-muted-dark mb-1">Từ khóa</label>
                      <Input 
                        placeholder="Tìm tiêu đề, nội dung..." 
                        leadingIcon={<Search size={16} />} 
                        value={activityFilters.keyword ?? ''} 
                        onChange={e => onActivityFiltersChange({ ...activityFilters, keyword: e.target.value })} 
                        onKeyDown={e => {
                          if (e.key === 'Enter') {
                            e.preventDefault()
                            onActivitySearch(activityFilters)
                          }
                        }} 
                        className="w-full"
                      />
                    </div>
                  </div>

                  <div className="flex items-center justify-end gap-2 pt-3 border-t border-line/60">
                    <Button
                      variant="secondary"
                      size="sm"
                      iconOnly
                      title="Đặt lại bộ lọc"
                      aria-label="Đặt lại bộ lọc"
                      onClick={() => {
                        const resetObj = { performedByUserId: '', entityType: '', action: '', fromDate: '', toDate: '', keyword: '' }
                        onActivityFiltersChange(resetObj)
                        onActivitySearch(resetObj)
                      }}
                      className="!px-3"
                      leadingIcon={<RotateCcw size={16} />}
                    />
                    <Button variant="primary" size="sm" loading={loading} leadingIcon={<Filter size={15} />} onClick={() => onActivitySearch(activityFilters)} className="!px-5 font-bold shadow-xs">
                      Áp dụng Lọc
                    </Button>
                  </div>
                </div>
              </CollapsiblePanel>
              
              {activities.content.map(activity => (
                <button
                  key={activity.id}
                  type="button"
                  className="w-full rounded-xl border border-line bg-white p-4 text-left transition hover:border-brand"
                  onClick={() => setSelectedActivityId(activity.id)}
                >
                  <div className="flex items-center gap-2">
                    <div className="grid size-8 shrink-0 place-items-center rounded-full bg-blue-100 text-blue-600">
                      <Activity size={16} />
                    </div>
                    <div>
                      <p className="font-medium text-ink">{projectActivityLabels[activity.action] ?? activity.action}</p>
                      <div className="mt-1 flex items-center gap-3 text-xs text-muted">
                        <span className="flex items-center gap-1"><UserIcon size={12} /> {activity.performedByUsername}</span>
                        <span>{formatDate(activity.createdAt)}</span>
                      </div>
                    </div>
                  </div>
                </button>
              ))}
              {!activities.content.length && <p className="py-10 text-center text-sm text-muted">Chưa có hoạt động nào.</p>}
              {activities.totalPages > 1 && (
                <footer className="flex items-center justify-between border-t border-line pt-4 text-sm text-muted">
                  <span>Trang {activityPage + 1} / {activities.totalPages}</span>
                  <div className="flex gap-2">
                    <Button variant="secondary" size="sm" disabled={activityPage === 0} onClick={() => onActivityPageChange(activityPage - 1)}>Trước</Button>
                    <Button variant="secondary" size="sm" disabled={activityPage + 1 >= activities.totalPages} onClick={() => onActivityPageChange(activityPage + 1)}>Sau</Button>
                  </div>
                </footer>
              )}
            </div>}

            {activeTab === 'notifications' && (
              <div className="space-y-4">
                <div className="flex items-center justify-between">
                  <h3 className="font-semibold text-lg">Thông báo hệ thống ({notifications.content.length})</h3>
                  {notifications.content.length > 0 && (
                    <Button variant="outline-teal" size="sm" leadingIcon={<CheckCheck size={16} />} onClick={onReadAllNotifications}>
                      Đánh dấu tất cả đã đọc
                    </Button>
                  )}
                </div>

                <div className="max-h-[580px] overflow-y-auto pr-1.5 space-y-3">
                  {notifications.content.map(item => (
                    <div
                      key={item.id}
                      className={`flex items-start gap-4 rounded-xl border p-4 transition ${
                        item.read ? 'border-line bg-white hover:border-slate-300' : 'border-brand bg-[#fff7ed] shadow-2xs'
                      }`}
                    >
                      <button type="button" className="flex-1 text-left" onClick={() => onReadNotification(item.id)}>
                        <div className="flex items-center gap-2 font-semibold">
                          <span>{item.title ? item.title.replace(/\b(\d{4})-(\d{2})-(\d{2})\b/g, '$3/$2/$1') : ''}</span>
                          {!item.read && <span className="size-2 rounded-full bg-brand" />}
                        </div>
                        <p className="mt-1 text-sm text-muted">{item.content ? item.content.replace(/\b(\d{4})-(\d{2})-(\d{2})\b/g, '$3/$2/$1') : ''}</p>
                        <p className="mt-2 text-xs text-muted font-medium">{formatDate(item.createdAt)}</p>
                      </button>
                      <Button
                        variant="secondary"
                        size="sm"
                        iconOnly
                        aria-label="Xóa thông báo"
                        leadingIcon={<Trash2 size={16} className="text-rose-500" />}
                        onClick={() => onDeleteNotification(item.id)}
                      />
                    </div>
                  ))}

                  {!notifications.content.length && (
                    <div className="py-16 text-center text-sm text-muted bg-white rounded-2xl border border-line">
                      Chưa có thông báo nào.
                    </div>
                  )}
                </div>
              </div>
            )}

            {activeTab === 'timesheet' && (
              <TimesheetView mode="project" projectId={selectedProject.id} members={members} />
            )}

            {activeTab === 'bugs' && (
              <BugView 
                projectId={selectedProject.id} 
                projectName={selectedProject.name}
                members={members} 
                backlogItems={backlogItems} 
                tasks={Object.values(kanbanBoards).flatMap(board => board?.columns.flatMap(col => col.tasks) ?? [])} 
                sprints={sprints}
                openBugId={openBugId || undefined}
                onCloseBug={onCloseBug}
              />
            )}

            {activeTab === 'attachments' && (
              <ProjectAttachmentsTab projectId={selectedProject.id} />
            )}

            {activeTab === 'imports' && (
              <ProjectTaskImportHistoryTab projectId={selectedProject.id} />
            )}
          </div>
          </motion.div>
          )}
        </AnimatePresence>
      </section>
    </main>

    <ProjectCreateModal open={createOpen} saving={saving} onClose={() => setCreateOpen(false)} onSave={data => { onCreateProject(data); setCreateOpen(false) }} />
    <ProjectEditModal open={projectEditOpen} project={selectedProject} saving={saving} onClose={() => setProjectEditOpen(false)} onSave={data => { onUpdateProject(data); setProjectEditOpen(false) }} />
    <ConfirmDialog open={deleteProjectOpen} title="Xóa dự án?" description="Dự án sẽ bị xóa mềm khỏi hệ thống. Hành động này có thể ảnh hưởng dữ liệu Sprint, Task và thành viên." confirmLabel="Xóa dự án" loading={saving} onCancel={() => setDeleteProjectOpen(false)} onConfirm={() => { onDeleteProject(); setDeleteProjectOpen(false) }} />

    {selectedProject && (
      <ProjectActivityDetailModal
        open={!!selectedActivityId}
        projectId={selectedProject.id}
        activityId={selectedActivityId}
        onClose={() => setSelectedActivityId(null)}
      />
    )}

    <GlobalSearchModal
      open={globalSearchOpen}
      onClose={() => setGlobalSearchOpen(false)}
      currentProjectId={selectedProject?.id}
      currentProjectCode={selectedProject?.code}
      onSelectResult={onSelectSearchResult}
    />

    {selectedProject && (
      <Modal 
        open={projectStatusModalOpen} 
        onClose={() => setProjectStatusModalOpen(false)} 
        title="Cập nhật trạng thái dự án"
        description="Chọn trạng thái hoạt động hiện tại cho dự án này."
      >
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 pt-2">
          {statuses.map(st => {
            const isCurrent = selectedProject.status === st
            return (
              <button
                key={st}
                type="button"
                onClick={() => {
                  onStatusChange(st)
                  setProjectStatusModalOpen(false)
                }}
                className={`flex items-center justify-between p-3.5 rounded-2xl border text-left transition-all cursor-pointer ${getProjectStatusCardStyle(st, isCurrent)}`}
              >
                <div className="flex items-center gap-3">
                  <div className={`size-3 rounded-full shrink-0 ${getProjectStatusDotColor(st)}`} />
                  <div>
                    <p className="text-xs font-extrabold text-slate-800">{projectStatusLabels[st]}</p>
                    <p className="text-[11px] text-slate-500 font-medium mt-0.5">
                      {st === 'ACTIVE' && 'Dự án đang trong giai đoạn triển khai'}
                      {st === 'COMPLETED' && 'Dự án đã hoàn thành các công việc'}
                      {st === 'CANCELLED' && 'Dự án tạm dừng hoặc đã hủy bỏ'}
                      {st === 'ARCHIVED' && 'Dự án đã đóng và đưa vào lưu trữ'}
                    </p>
                  </div>
                </div>
                {isCurrent && <CheckCircle2 size={18} className="text-brand shrink-0 ml-2" />}
              </button>
            )
          })}
        </div>
      </Modal>
    )}

    <UserGuideModal
      open={guideModalOpen}
      onClose={() => setGuideModalOpen(false)}
    />
  </div>
}




