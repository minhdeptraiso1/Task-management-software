import { useEffect, useRef, useState, type FormEvent } from 'react'
import {
  Activity,
  Bell,
  CalendarDays,
  CheckCircle2,
  CheckCheck,
  ChevronLeft,
  ChevronRight,
  FolderKanban,
  LogOut,
  Pencil,
  Plus,
  RefreshCw,
  Search,
  Settings,
  Trash2,
  UserPlus,
  UsersRound,
} from 'lucide-react'
import { ActionMenu, ActionItem, Button, ConfirmDialog, Input, Modal, Select, ToolbarActions } from '../../../components/ui'
import type { User } from '../../user/models/user.model'
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
import type { BacklogItem, BacklogItemStatus, BacklogPriority, Sprint } from '../models/scrum.model'
import type { KanbanBoard, SprintBurndown, SprintTaskStatistics, Task, TaskCommentPage, TaskImportResult, TaskPriority, TaskStatus, TaskTimeLogPage, TaskTimeSummary, TaskType } from '../models/task.model'
import { ScrumBoardView } from './ScrumBoardView'

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
  selectedTask: Task | null
  taskComments: TaskCommentPage
  taskTimeLogs: TaskTimeLogPage
  taskTimeSummary: TaskTimeSummary | null
  taskImportResult: TaskImportResult | null
  candidateUsers: User[]
  unreadCount: number
  filters: ProjectFilters
  activeTab: 'board' | 'members' | 'activities' | 'notifications'
  page: number
  activityPage: number
  loading: boolean
  detailLoading: boolean
  taskDetailLoading: boolean
  candidateLoading: boolean
  saving: boolean
  error: string
  onLogout: () => void
  onFiltersChange: (filters: ProjectFilters) => void
  onSearch: () => void
  onPageChange: (page: number) => void
  onSelectProject: (project: Project) => void
  onCreateProject: (data: { code: string; name: string; description: string; startDate: string; endDate: string }) => void
  onUpdateProject: (data: { name?: string; description?: string; startDate?: string; endDate?: string }) => void
  onDeleteProject: () => void
  onStatusChange: (status: ProjectStatus) => void
  onTabChange: (tab: 'board' | 'members' | 'activities' | 'notifications') => void
  onActivityPageChange: (page: number) => void
  onAddMember: (userId: string, role: ProjectMemberRole) => void
  onCandidateSearch: (keyword: string) => void
  onRoleChange: (memberId: string, role: ProjectMemberRole) => void
  onRemoveMember: (member: ProjectMember) => void
  onReadNotification: (id: string) => void
  onReadAllNotifications: () => void
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
}

function statusClass(status: ProjectStatus) {
  if (status === 'ACTIVE') return 'bg-[#ecfdf3] text-success'
  if (status === 'CANCELLED') return 'bg-[#fff0ed] text-danger'
  if (status === 'COMPLETED') return 'bg-[#eff6ff] text-info'
  if (status === 'ARCHIVED') return 'bg-panel text-muted'
  return 'bg-[#fef3e2] text-brand-dark'
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
        <label className="mb-2 block text-sm font-medium text-[#3f3f46]">Mô tả</label>
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
        <label className="mb-2 block text-sm font-medium text-[#3f3f46]">Mô tả</label>
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
  selectedTask,
  taskComments,
  taskTimeLogs,
  taskTimeSummary,
  taskImportResult,
  candidateUsers,
  unreadCount,
  filters,
  activeTab,
  page,
  activityPage,
  loading,
  detailLoading,
  taskDetailLoading,
  candidateLoading,
  saving,
  errors,
  onDismissError,
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
  onReadAllNotifications,
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
}: Props) {
  const [createOpen, setCreateOpen] = useState(false)
  const [projectEditOpen, setProjectEditOpen] = useState(false)
  const [deleteProjectOpen, setDeleteProjectOpen] = useState(false)
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false)
  const canCreateProject = user.role === 'MANAGER'
  const canManageMembers = selectedProject?.currentUserRole === 'OWNER' || selectedProject?.currentUserRole === 'PROJECT_MANAGER'

  return <div className="min-h-screen bg-canvas">
    {errors.length > 0 && <div className="fixed right-5 top-5 z-50 flex max-w-md flex-col gap-2">
      {errors.map(err => (
        <div key={err.id} onClick={() => onDismissError(err.id)} className="animate-enter cursor-pointer rounded-2xl border border-danger/20 bg-[#fff0ed] p-4 text-danger shadow-2xl transition-all hover:opacity-80">
          <p className="font-bold">Có lỗi xảy ra</p>
          <p className="mt-1 text-sm leading-5">{err.message}</p>
        </div>
      ))}
    </div>}
    <header className="flex h-14 items-center justify-between bg-brand-black px-5 text-white shadow-sm md:px-6">
      <div className="flex items-center gap-3 font-bold">
        <span className="text-2xl tracking-[-.08em]">HI<span className="text-brand">CAS</span></span>
        <span className="h-6 w-px bg-white/20" />
        <span className="text-sm text-white/55">PROJECT</span>
      </div>
      <div className="flex items-center gap-3">
        <div className="hidden text-right sm:block">
          <p className="text-sm font-semibold">{user.username}</p>
          <p className="text-xs text-white/45">{user.role}</p>
        </div>
        <span className="grid size-9 place-items-center rounded-full bg-brand font-bold">{user.username.slice(0, 2).toUpperCase()}</span>
        <Button className="!text-white" variant="ghost" size="sm" leadingIcon={<LogOut size={18} />} onClick={onLogout}>Đăng xuất</Button>
      </div>
    </header>

    <main className={`grid gap-5 p-5 transition-[grid-template-columns] duration-300 ${sidebarCollapsed ? 'xl:grid-cols-[76px_1fr]' : 'xl:grid-cols-[380px_1fr]'}`}>
      <aside className={`rounded-xl border border-line bg-white transition-all duration-300 ${sidebarCollapsed ? 'overflow-hidden' : ''}`}>
        <div className="border-b border-line p-5">
          <div className="flex items-center justify-between gap-3">
            {!sidebarCollapsed && <div>
              <p className="text-sm font-semibold text-brand">Phase 2</p>
              <h1 className="mt-1 text-xl font-bold">Dự án của tôi</h1>
            </div>}
            <Button
              variant="secondary"
              size="sm"
              iconOnly
              leadingIcon={sidebarCollapsed ? <ChevronRight size={16} /> : <ChevronLeft size={16} />}
              aria-label={sidebarCollapsed ? 'Mở rộng danh sách dự án' : 'Thu gọn danh sách dự án'}
              onClick={() => setSidebarCollapsed(value => !value)}
            />
          </div>
          {!sidebarCollapsed && <div className="mt-4 grid gap-3">
            <Input aria-label="Tìm dự án" placeholder="Tìm mã hoặc tên..." leadingIcon={<Search size={17} />} value={filters.keyword} onChange={event => onFiltersChange({ ...filters, keyword: event.target.value })} onKeyDown={event => event.key === 'Enter' && onSearch()} />
            <div className="grid gap-3 sm:grid-cols-[1fr_auto]">
              <Select aria-label="Trạng thái" value={filters.status} onChange={event => onFiltersChange({ ...filters, status: event.target.value as ProjectFilters['status'] })} options={[{ label: 'Mọi trạng thái', value: '' }, ...statuses.map(status => ({ label: projectStatusLabels[status], value: status }))]} />
              <ToolbarActions loading={loading} canCreate={canCreateProject} createLabel="Tạo dự án" createIcon={<Plus size={18} />} onFilter={onSearch} onCreate={() => setCreateOpen(true)} />
            </div>
          </div>}
        </div>

        {sidebarCollapsed ? <div className={`max-h-[calc(100vh-160px)] space-y-2 overflow-y-auto p-2 ${loading ? 'opacity-50' : ''}`}>
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
        <div className={`max-h-[calc(100vh-300px)] overflow-y-auto p-3 ${loading ? 'opacity-50' : ''}`}>
          {projects.content.map(project => (
            <button key={project.id} type="button" className={`mb-3 w-full rounded-xl border p-4 text-left transition hover:border-brand ${selectedProject?.id === project.id ? 'border-brand bg-[#fff7ed]' : 'border-line bg-white'}`} onClick={() => onSelectProject(project)}>
              <div className="flex items-start justify-between gap-3">
                <div>
                  <p className="text-xs font-bold uppercase tracking-wider text-brand">{project.code}</p>
                  <p className="mt-1 font-semibold text-ink">{project.name}</p>
                </div>
                <span className={`shrink-0 rounded-full px-2.5 py-1 text-xs font-semibold ${statusClass(project.status)}`}>{projectStatusLabels[project.status]}</span>
              </div>
              <div className="mt-3 flex items-center gap-3 text-xs text-muted">
                <span className="inline-flex items-center gap-1"><CalendarDays size={14} />{formatDate(project.startDate)}</span>
                {project.currentUserRole && <span>{projectMemberRoleLabels[project.currentUserRole]}</span>}
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
      </aside>

      <section className="min-w-0 rounded-xl border border-line bg-white">
        {!selectedProject ? <div className="grid min-h-[520px] place-items-center p-8 text-center text-muted">
          <div><FolderKanban className="mx-auto text-brand" size={42} /><p className="mt-1 font-semibold text-ink">Chọn một dự án để xem chi tiết</p></div>
        </div> : <>
          <div className="border-b border-line p-5">
            <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
              <div>
                <div className="flex flex-wrap items-center gap-2">
                  <span className="rounded-full bg-[#fef3e2] px-2.5 py-1 text-xs font-bold text-brand-dark">{selectedProject.code}</span>
                  <span className={`rounded-full px-2.5 py-1 text-xs font-semibold ${statusClass(selectedProject.status)}`}>{projectStatusLabels[selectedProject.status]}</span>
                  {selectedProject.currentUserRole && <span className="rounded-full bg-panel px-2.5 py-1 text-xs font-semibold text-muted">{projectMemberRoleLabels[selectedProject.currentUserRole]}</span>}
                </div>
                <h2 className="mt-3 text-2xl font-bold">{selectedProject.name}</h2>
                <p className="mt-2 max-w-3xl text-sm leading-6 text-muted">{selectedProject.description || 'Chưa có mô tả dự án.'}</p>
              </div>
              <div className="flex flex-wrap gap-2">
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
                <Select aria-label="Đổi trạng thái" value={selectedProject.status} onChange={event => onStatusChange(event.target.value as ProjectStatus)} disabled={saving || detailLoading} options={statuses.map(status => ({ label: projectStatusLabels[status], value: status }))} />
              </div>
            </div>
            <div className="mt-5 grid gap-3 sm:grid-cols-3">
              <div className="rounded-xl bg-canvas p-4"><p className="text-xs text-muted">Bắt đầu</p><p className="mt-1 font-semibold">{formatDate(selectedProject.startDate)}</p></div>
              <div className="rounded-xl bg-canvas p-4"><p className="text-xs text-muted">Kết thúc</p><p className="mt-1 font-semibold">{formatDate(selectedProject.endDate)}</p></div>
              <div className="rounded-xl bg-canvas p-4"><p className="text-xs text-muted">Thành viên</p><p className="mt-1 font-semibold">{members.length}</p></div>
            </div>
          </div>

          <div className="border-b border-line px-5 py-4">
            <div className="flex flex-wrap justify-center gap-2">
              <Button variant="secondary" className={activeTab === 'board' ? '!bg-brand !text-white !border-transparent' : ''} size="sm" leadingIcon={<FolderKanban size={16} />} onClick={() => onTabChange('board')}>Sprint Board</Button>
              <Button variant="secondary" className={activeTab === 'members' ? '!bg-teal-500 !text-white !border-transparent' : ''} size="sm" leadingIcon={<UsersRound size={16} />} onClick={() => onTabChange('members')}>Thành viên</Button>
              <Button variant="secondary" className={activeTab === 'activities' ? '!bg-blue-500 !text-white !border-transparent' : ''} size="sm" leadingIcon={<Activity size={16} />} onClick={() => onTabChange('activities')}>Hoạt động</Button>
              <Button variant="secondary" className={activeTab === 'notifications' ? '!bg-rose-500 !text-white !border-transparent' : ''} size="sm" leadingIcon={<Bell size={16} />} onClick={() => onTabChange('notifications')}>Thông báo {unreadCount ? `(${unreadCount})` : ''}</Button>
            </div>
          </div>

          <div className={`p-5 ${detailLoading ? 'opacity-50' : ''}`}>
            {activeTab === 'board' && <ScrumBoardView
              backlogItems={backlogItems}
              sprints={sprints}
              sprintItems={sprintItems}
              kanbanBoards={kanbanBoards}
              selectedSprintId={selectedSprintId}
              sprintStatistics={sprintStatistics}
              sprintBurndown={sprintBurndown}
              selectedTask={selectedTask}
              taskComments={taskComments}
              taskTimeLogs={taskTimeLogs}
              taskTimeSummary={taskTimeSummary}
              taskImportResult={taskImportResult}
              members={members}
              loading={detailLoading}
              taskDetailLoading={taskDetailLoading}
              saving={saving}
              canManage={canManageMembers}
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
            />}

            {activeTab === 'members' && <>
              {canManageMembers && <AddMemberForm candidates={candidateUsers} saving={saving} loading={candidateLoading} onSearch={onCandidateSearch} onAdd={onAddMember} />}
              <div className="mt-4 overflow-x-auto rounded-xl border border-line">
                <table className="w-full min-w-[760px] text-left text-sm">
                  <thead className="bg-panel text-xs font-semibold uppercase tracking-wider text-[#3f3f46]"><tr><th className="px-4 py-3">Thành viên</th><th className="px-4 py-3">Role hệ thống</th><th className="px-4 py-3">Role dự án</th><th className="px-4 py-3 text-center">Thao tác</th></tr></thead>
                  <tbody>
                    {members.map(member => <tr key={member.id} className="border-t border-line">
                      <td className="px-4 py-3"><p className="font-semibold">{member.username}</p><p className="text-xs text-muted">{member.email}</p></td>
                      <td className="px-4 py-3">{member.systemRole}</td>
                      <td className="px-4 py-3">{canManageMembers ? <Select aria-label="Role dự án" value={member.projectRole} onChange={event => onRoleChange(member.id, event.target.value as ProjectMemberRole)} options={memberRoles.map(role => ({ label: projectMemberRoleLabels[role], value: role }))} /> : projectMemberRoleLabels[member.projectRole]}</td>
                      <td className="px-4 py-3 text-center">{canManageMembers && <Button variant="ghost" size="sm" iconOnly className="!text-danger" leadingIcon={<Trash2 size={16} />} aria-label={`Xóa ${member.username}`} onClick={() => onRemoveMember(member)} />}</td>
                    </tr>)}
                  </tbody>
                </table>
              </div>
            </>}

            {activeTab === 'activities' && <div className="space-y-3">
              {activities.content.map(activity => <article key={activity.id} className="rounded-xl border border-line p-4">
                <div className="flex items-start justify-between gap-3">
                  <div><p className="font-semibold">{projectActivityLabels[activity.action] ?? activity.action}</p><p className="mt-1 text-xs text-muted">{entityTypeLabels[activity.entityType] ?? activity.entityType} · {formatDate(activity.createdAt)}</p></div>
                  <CheckCircle2 className="text-success" size={18} />
                </div>
              </article>)}
              {!activities.content.length && <p className="py-10 text-center text-sm text-muted">Chưa có hoạt động.</p>}
              <footer className="flex justify-end gap-2">
                <Button variant="secondary" size="sm" disabled={activityPage === 0} leadingIcon={<ChevronLeft size={16} />} onClick={() => onActivityPageChange(activityPage - 1)}>Trước</Button>
                <Button variant="secondary" size="sm" disabled={activityPage + 1 >= activities.totalPages} trailingIcon={<ChevronRight size={16} />} onClick={() => onActivityPageChange(activityPage + 1)}>Sau</Button>
              </footer>
            </div>}

            {activeTab === 'notifications' && <div className="space-y-3">
              <div className="flex justify-end"><Button variant="outline-teal" size="sm" leadingIcon={<CheckCheck size={16} />} onClick={onReadAllNotifications}>Đánh dấu tất cả đã đọc</Button></div>
              {notifications.content.map(item => <button key={item.id} type="button" className={`w-full rounded-xl border p-4 text-left ${item.read ? 'border-line bg-white' : 'border-brand bg-[#fff7ed]'}`} onClick={() => onReadNotification(item.id)}>
                <p className="font-semibold">{item.title}</p>
                <p className="mt-1 text-sm text-muted">{item.content}</p>
                <p className="mt-2 text-xs text-muted">{formatDate(item.createdAt)}</p>
              </button>)}
              {!notifications.content.length && <p className="py-10 text-center text-sm text-muted">Chưa có thông báo.</p>}
            </div>}
          </div>
        </>}
      </section>
    </main>

    <ProjectCreateModal open={createOpen} saving={saving} onClose={() => setCreateOpen(false)} onSave={data => { onCreateProject(data); setCreateOpen(false) }} />
    <ProjectEditModal open={projectEditOpen} project={selectedProject} saving={saving} onClose={() => setProjectEditOpen(false)} onSave={data => { onUpdateProject(data); setProjectEditOpen(false) }} />
    <ConfirmDialog open={deleteProjectOpen} title="Xóa dự án?" description="Dự án sẽ bị xóa mềm khỏi hệ thống. Hành động này có thể ảnh hưởng dữ liệu Sprint, Task và thành viên." confirmLabel="Xóa dự án" loading={saving} onCancel={() => setDeleteProjectOpen(false)} onConfirm={() => { onDeleteProject(); setDeleteProjectOpen(false) }} />
  </div>
}




