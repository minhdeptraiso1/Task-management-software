import { useCallback, useEffect, useState } from 'react'
import { ConfirmDialog, Modal, Button } from '../../../components/ui'
import type { User } from '../../user/models/user.model'
import type {
  Project,
  ProjectActivityPage,
  ProjectFilters,
  ProjectMember,
  ProjectMemberRole,
  ProjectPage,
  ProjectStatus,
} from '../models/project.model'
import type { NotificationPage } from '../models/notification.model'
import type { BacklogItem, BacklogItemPage, BacklogItemStatus, BacklogPriority, Sprint, SprintPage, SprintCapacityResponse, SprintHealthResponse, SprintRiskResponse, SprintProgress } from '../models/scrum.model'
import type { KanbanBoard, SprintBurndown, SprintTaskStatistics, Task, TaskCommentPage, TaskImportResult, TaskPriority, TaskStatus, TaskTimeLogPage, TaskTimeSummary, TaskType, TaskDependency, TaskRisk, TaskRiskSummary } from '../models/task.model'
import {
  addProjectMember,
  createProject,
  deleteProject,
  getProject,
  getProjectActivities,
  getProjectMembers,
  removeProjectMember,
  searchProjects,
  updateProject,
  updateProjectMemberRole,
  updateProjectStatus,
} from '../services/project.service'
import {
  addBacklogItemToSprint,
  cancelSprint,
  completeSprint,
  createBacklogItem,
  createSprint,
  deleteBacklogItem,
  deleteSprint,
  getBacklogItems,
  getSprints,
  removeBacklogItemFromSprint,
  startSprint,
  updateBacklogItem,
  updateBacklogItemPriority,
  updateBacklogItemStatus,
  updateSprint,
  getSprintCapacity,
  getSprintHealth,
  getSprintRisks,
  getSprintProgress,
} from '../services/scrum.service'
import {
  assignTask,
  createTask,
  createTaskComment,
  createTaskCommentReply,
  createTaskTimeLog,
  deleteTask,
  deleteTaskComment,
  deleteTaskTimeLog,
  downloadTaskExcelTemplate,
  getSprintBurndown,
  getSprintKanban,
  getSprintTaskStatistics,
  getTask,
  getTaskComments,
  getTaskTimeLogs,
  getTaskTimeSummary,
  importTasksFromExcel,
  unassignTask,
  updateTask,
  updateTaskComment,
  updateTaskStatus,
  updateTaskTimeLog,
  blockTask,
  reopenTask,
  getTaskDependencies,
  addTaskDependency,
  removeTaskDependency,
  unblockTask,
  getTaskRisk,
  getSprintRiskSummary,
} from '../services/task.service'
import {
  deleteNotification,
  getNotifications,
  getUnreadCount,
  markAllNotificationsRead,
  markNotificationRead,
} from '../services/notification.service'
import { ProjectWorkspaceView } from '../views/ProjectWorkspaceView'
import { searchProjectCandidateUsers } from '../../user/services/user.service'
import type { ProjectActivityFilters } from '../services/project.service'
import type { UserPage } from '../../user/models/user.model'

const emptyProjectPage: ProjectPage = { content: [], totalElements: 0, totalPages: 0, number: 0, size: 9, numberOfElements: 0, first: true, last: true, empty: true }
const emptyActivityPage: ProjectActivityPage = { content: [], totalElements: 0, totalPages: 0, number: 0, size: 10, numberOfElements: 0, first: true, last: true, empty: true }
const emptyNotificationPage: NotificationPage = { content: [], totalElements: 0, totalPages: 0, number: 0, size: 6, numberOfElements: 0, first: true, last: true, empty: true }
const emptyCandidatePage: UserPage = { content: [], totalElements: 0, totalPages: 0, number: 0, size: 8 }
const emptyBacklogPage: BacklogItemPage = { content: [], totalElements: 0, totalPages: 0, number: 0, size: 200, numberOfElements: 0, first: true, last: true, empty: true }
const emptySprintPage: SprintPage = { content: [], totalElements: 0, totalPages: 0, number: 0, size: 50, numberOfElements: 0, first: true, last: true, empty: true }
const emptyCommentPage: TaskCommentPage = { content: [], totalElements: 0, totalPages: 0, number: 0, size: 20, numberOfElements: 0, first: true, last: true, empty: true }
const emptyTimeLogPage: TaskTimeLogPage = { content: [], totalElements: 0, totalPages: 0, number: 0, size: 20, numberOfElements: 0, first: true, last: true, empty: true }
const initialFilters: ProjectFilters = { keyword: '', status: '' }

export function ProjectWorkspaceController({ user, onLogout, onOpenSettings }: { user: User; onLogout: () => void; onOpenSettings: () => void }) {
  const [projects, setProjects] = useState(emptyProjectPage)
  const [filters, setFilters] = useState(initialFilters)
  const [appliedFilters, setAppliedFilters] = useState(initialFilters)
  const [page, setPage] = useState(0)
  const [selectedProject, setSelectedProject] = useState<Project | null>(null)
  const [members, setMembers] = useState<ProjectMember[]>([])
  const [activities, setActivities] = useState(emptyActivityPage)
  const [activityPage, setActivityPage] = useState(0)
  const [activityFilters, setActivityFilters] = useState<ProjectActivityFilters>({})
  const [appliedActivityFilters, setAppliedActivityFilters] = useState<ProjectActivityFilters>({})
  const [notifications, setNotifications] = useState(emptyNotificationPage)
  const [candidateUsers, setCandidateUsers] = useState(emptyCandidatePage)
  const [backlogItems, setBacklogItems] = useState(emptyBacklogPage)
  const [sprints, setSprints] = useState(emptySprintPage)
  const [sprintItems, setSprintItems] = useState<Record<string, BacklogItem[]>>({})
  const [kanbanBoards, setKanbanBoards] = useState<Record<string, KanbanBoard>>({})
  const [selectedSprintId, setSelectedSprintId] = useState<string | null>(null)
  const [sprintStatistics, setSprintStatistics] = useState<SprintTaskStatistics | null>(null)
  const [sprintBurndown, setSprintBurndown] = useState<SprintBurndown | null>(null)
  const [sprintCapacity, setSprintCapacity] = useState<SprintCapacityResponse | null>(null)
  const [sprintHealth, setSprintHealth] = useState<SprintHealthResponse | null>(null)
  const [sprintRisks, setSprintRisks] = useState<SprintRiskResponse[] | null>(null)
  const [sprintProgress, setSprintProgress] = useState<SprintProgress | null>(null)
  const [sprintTaskRiskSummary, setSprintTaskRiskSummary] = useState<TaskRiskSummary | null>(null)
  const [selectedTask, setSelectedTask] = useState<Task | null>(null)
  const [taskComments, setTaskComments] = useState(emptyCommentPage)
  const [taskTimeLogs, setTaskTimeLogs] = useState(emptyTimeLogPage)
  const [taskTimeSummary, setTaskTimeSummary] = useState<TaskTimeSummary | null>(null)
  const [taskImportResult, setTaskImportResult] = useState<TaskImportResult | null>(null)
  const [taskDetailLoading, setTaskDetailLoading] = useState(false)
  const [unreadCount, setUnreadCount] = useState(0)
  const [activeTab, setActiveTab] = useState<'board' | 'members' | 'activities' | 'notifications' | 'dashboard' | 'reports' | 'timesheet'>('dashboard')
  const [loading, setLoading] = useState(false)
  const [detailLoading, setDetailLoading] = useState(false)
  const [candidateLoading, setCandidateLoading] = useState(false)
  const [saving, setSaving] = useState(false)
  const [errors, setErrors] = useState<{id: string, message: string}[]>([])
  const [taskDependencies, setTaskDependencies] = useState<TaskDependency[]>([])
  const [taskRisk, setTaskRisk] = useState<TaskRisk | null>(null)

  const [blockTaskState, setBlockTaskState] = useState<{
    taskId: string
    status: TaskStatus
    position?: number
  } | null>(null)
  const [reopenTaskState, setReopenTaskState] = useState<{
    taskId: string
    targetStatus: TaskStatus
    position?: number
  } | null>(null)
  const [blockReason, setBlockReason] = useState('')
  const [reopenReason, setReopenReason] = useState('')

  const setError = useCallback((message: string) => {
    if (!message) return
    const id = String(Date.now() + Math.random())
    setErrors(prev => [...prev, { id, message }])
    setTimeout(() => {
      setErrors(prev => prev.filter(err => err.id !== id))
    }, 5000)
  }, [])
  
  const dismissError = useCallback((id: string) => {
    setErrors(prev => prev.filter(err => err.id !== id))
  }, [])

  const [confirmRemoveMember, setConfirmRemoveMember] = useState<ProjectMember | null>(null)

  const loadProjects = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      const result = await searchProjects(appliedFilters, page)
      setProjects(result)
      // Do not auto-select first project, let null indicate Personal Dashboard
    } catch (error) {
      setError(error instanceof Error ? error.message : 'Không tải được danh sách dự án')
    } finally {
      setLoading(false)
    }
  }, [appliedFilters, page, setError])

  useEffect(() => {
    void Promise.resolve().then(loadProjects)
  }, [loadProjects])

  const loadProjectDetail = useCallback(async (projectId: string) => {
    setDetailLoading(true)
    setError('')
    try {
      const [project, projectMembers, projectActivities] = await Promise.all([
        getProject(projectId),
        getProjectMembers(projectId),
        getProjectActivities(projectId, activityPage, 10, appliedActivityFilters),
      ])
      setSelectedProject(project)
      setMembers(projectMembers)
      setActivities(projectActivities)
    } catch (error) {
      setError(error instanceof Error ? error.message : 'Không tải được chi tiết dự án')
    } finally {
      setDetailLoading(false)
    }
  }, [activityPage, appliedActivityFilters, setError])

  useEffect(() => {
    if (selectedProject?.id) void Promise.resolve().then(() => loadProjectDetail(selectedProject.id))
  }, [selectedProject?.id, loadProjectDetail])

  const loadScrumBoard = useCallback(async (projectId: string, options: { silent?: boolean } = {}) => {
    if (!options.silent) setDetailLoading(true)
    if (!options.silent) setError('')
    try {
      const [unscheduled, sprintPage] = await Promise.all([
        getBacklogItems(projectId, { unscheduledOnly: true }),
        getSprints(projectId),
      ])
      const itemEntries = await Promise.all(
        sprintPage.content.map(async sprint => {
          const items = await getBacklogItems(projectId, { sprintId: sprint.id })
          return [sprint.id, items.content] as const
        }),
      )
      const kanbanEntries = await Promise.all(
        sprintPage.content.map(async sprint => {
          const board = await getSprintKanban(projectId, sprint.id).catch(() => null)
          return board ? ([sprint.id, board] as const) : null
        }),
      )
      setBacklogItems(unscheduled)
      setSprints(sprintPage)
      setSprintItems(Object.fromEntries(itemEntries))
      setKanbanBoards(Object.fromEntries(kanbanEntries.filter(entry => entry !== null)))
      setSelectedSprintId(current => current ?? sprintPage.content.find(sprint => sprint.status === 'ACTIVE')?.id ?? sprintPage.content[0]?.id ?? null)
    } catch (error) {
      setError(error instanceof Error ? error.message : 'Không tải được Sprint Board')
    } finally {
      if (!options.silent) setDetailLoading(false)
    }
  }, [setError])

  useEffect(() => {
    if (selectedProject?.id) void Promise.resolve().then(() => loadScrumBoard(selectedProject.id))
  }, [selectedProject?.id, loadScrumBoard])

  const loadSprintStatistics = useCallback(async () => {
    if (!selectedProject?.id || !selectedSprintId) {
      setSprintCapacity(null)
      setSprintHealth(null)
      setSprintRisks(null)
      setSprintProgress(null)
      setSprintTaskRiskSummary(null)
      return
    }
    try {
      const [statistics, burndown, capacity, health, risks, progress, riskSummary] = await Promise.all([
        getSprintTaskStatistics(selectedProject.id, selectedSprintId),
        getSprintBurndown(selectedProject.id, selectedSprintId),
        getSprintCapacity(selectedProject.id, selectedSprintId),
        getSprintHealth(selectedProject.id, selectedSprintId),
        getSprintRisks(selectedProject.id, selectedSprintId),
        getSprintProgress(selectedProject.id, selectedSprintId),
        getSprintRiskSummary(selectedProject.id, selectedSprintId).catch(() => null),
      ])
      setSprintStatistics(statistics)
      setSprintBurndown(burndown)
      setSprintCapacity(capacity)
      setSprintHealth(health)
      setSprintRisks(risks)
      setSprintProgress(progress)
      setSprintTaskRiskSummary(riskSummary)
    } catch {
      setSprintStatistics(null)
      setSprintBurndown(null)
      setSprintCapacity(null)
      setSprintHealth(null)
      setSprintRisks(null)
      setSprintProgress(null)
      setSprintTaskRiskSummary(null)
    }
  }, [selectedProject?.id, selectedSprintId])

  useEffect(() => {
    void Promise.resolve().then(loadSprintStatistics)
  }, [loadSprintStatistics])

  const loadNotifications = useCallback(async () => {
    try {
      const [items, count] = await Promise.all([getNotifications(), getUnreadCount()])
      setNotifications(items)
      setUnreadCount(count.unreadCount)
    } catch {
      setNotifications(emptyNotificationPage)
    }
  }, [])

  useEffect(() => {
    void Promise.resolve().then(loadNotifications)
  }, [loadNotifications])

  const handleCreateProject = async (data: { code: string; name: string; description: string; startDate: string; endDate: string }) => {
    setSaving(true)
    setError('')
    try {
      const project = await createProject({
        code: data.code,
        name: data.name,
        description: data.description || undefined,
        startDate: data.startDate || undefined,
        endDate: data.endDate || undefined,
      })
      setSelectedProject(project)
      setPage(0)
      await loadProjects()
    } catch (error) {
      setError(error instanceof Error ? error.message : 'Không tạo được dự án')
    } finally {
      setSaving(false)
    }
  }

  const handleStatusChange = async (status: ProjectStatus) => {
    if (!selectedProject) return
    setSaving(true)
    setError('')
    try {
      const project = await updateProjectStatus(selectedProject.id, status)
      setSelectedProject(project)
      await loadProjects()
    } catch (error) {
      setError(error instanceof Error ? error.message : 'Không đổi được trạng thái')
    } finally {
      setSaving(false)
    }
  }

  const handleAddMember = async (userId: string, role: ProjectMemberRole) => {
    if (!selectedProject) return
    setSaving(true)
    setError('')
    try {
      await addProjectMember(selectedProject.id, { userId, role })
      await loadProjectDetail(selectedProject.id)
    } catch (error) {
      setError(error instanceof Error ? error.message : 'Không thêm được thành viên')
    } finally {
      setSaving(false)
    }
  }

  const handleCandidateSearch = async (keyword: string) => {
    setCandidateLoading(true)
    setError('')
    try {
      setCandidateUsers(await searchProjectCandidateUsers(keyword))
    } catch (error) {
      setError(error instanceof Error ? error.message : 'Không tìm được thành viên phù hợp')
    } finally {
      setCandidateLoading(false)
    }
  }

  const handleRoleChange = async (memberId: string, role: ProjectMemberRole) => {
    if (!selectedProject) return
    setSaving(true)
    setError('')
    try {
      await updateProjectMemberRole(selectedProject.id, memberId, role)
      await loadProjectDetail(selectedProject.id)
    } catch (error) {
      setError(error instanceof Error ? error.message : 'Không đổi được vai trò thành viên')
    } finally {
      setSaving(false)
    }
  }

  const handleRemoveMember = async () => {
    if (!selectedProject || !confirmRemoveMember) return
    setSaving(true)
    try {
      await removeProjectMember(selectedProject.id, confirmRemoveMember.id)
      setConfirmRemoveMember(null)
      await loadProjectDetail(selectedProject.id)
    } catch (error) {
      setError(error instanceof Error ? error.message : 'Không xóa được thành viên')
    } finally {
      setSaving(false)
    }
  }

  const handleReadNotification = async (id: string) => {
    await markNotificationRead(id)
    await loadNotifications()
  }

  const handleReadAll = async () => {
    await markAllNotificationsRead()
    await loadNotifications()
  }

  const handleDeleteNotification = async (id: string) => {
    await deleteNotification(id)
    await loadNotifications()
  }

  const reloadBoard = async () => {
    if (selectedProject) await loadScrumBoard(selectedProject.id, { silent: true })
  }

  const loadSelectedTask = async (taskId: string) => {
    if (!selectedProject) return
    setTaskDetailLoading(true)
    setError('')
    try {
      const [task, comments, timeLogs, timeSummary, dependencies, risk] = await Promise.all([
        getTask(selectedProject.id, taskId),
        getTaskComments(selectedProject.id, taskId),
        getTaskTimeLogs(selectedProject.id, taskId),
        getTaskTimeSummary(selectedProject.id, taskId),
        getTaskDependencies(selectedProject.id, taskId),
        getTaskRisk(selectedProject.id, taskId).catch(() => null),
      ])
      setSelectedTask(task)
      setTaskComments(comments)
      setTaskTimeLogs(timeLogs)
      setTaskTimeSummary(timeSummary)
      setTaskDependencies(dependencies || [])
      setTaskRisk(risk)
    } catch (error) {
      setError(error instanceof Error ? error.message : 'Không tải được chi tiết Task')
    } finally {
      setTaskDetailLoading(false)
    }
  }

  const handleUpdateProject = async (data: { name?: string; description?: string; startDate?: string; endDate?: string }) => {
    if (!selectedProject) return
    setSaving(true)
    setError('')
    try {
      const project = await updateProject(selectedProject.id, data)
      setSelectedProject(project)
      await loadProjects()
    } catch (error) {
      setError(error instanceof Error ? error.message : 'Không cập nhật được dự án')
    } finally {
      setSaving(false)
    }
  }

  const handleDeleteProject = async () => {
    if (!selectedProject) return
    setSaving(true)
    setError('')
    try {
      await deleteProject(selectedProject.id)
      setSelectedProject(null)
      await loadProjects()
    } catch (error) {
      setError(error instanceof Error ? error.message : 'Không xóa được dự án')
    } finally {
      setSaving(false)
    }
  }

  const refreshSelectedTask = async () => {
    if (selectedTask) await loadSelectedTask(selectedTask.id)
  }

  const moveItemInBoard = (itemId: string, targetSprintId: string | null) => {
    const movingItem =
      backlogItems.content.find(item => item.id === itemId)
      ?? Object.values(sprintItems).flat().find(item => item.id === itemId)

    if (!movingItem) return

    setBacklogItems(current => ({
      ...current,
      content: current.content.filter(item => item.id !== itemId),
    }))

    setSprintItems(current => {
      const next: Record<string, BacklogItem[]> = {}

      for (const [sprintId, items] of Object.entries(current)) {
        next[sprintId] = items.filter(item => item.id !== itemId)
      }

      if (targetSprintId) {
        next[targetSprintId] = [
          { ...movingItem, sprintId: targetSprintId, status: 'IN_SPRINT' },
          ...(next[targetSprintId] ?? []),
        ]
      }

      return next
    })

    if (!targetSprintId) {
      setBacklogItems(current => ({
        ...current,
        content: [{ ...movingItem, sprintId: null, status: 'READY' }, ...current.content],
      }))
    }
  }

  const handleCreateBacklog = async (data: Parameters<typeof createBacklogItem>[1]) => {
    if (!selectedProject) return
    setSaving(true)
    try {
      await createBacklogItem(selectedProject.id, data)
      await reloadBoard()
    } catch (error) {
      setError(error instanceof Error ? error.message : 'Không tạo được backlog item')
    } finally {
      setSaving(false)
    }
  }

  const handleCreateSprint = async (data: Parameters<typeof createSprint>[1]) => {
    if (!selectedProject) return
    setSaving(true)
    try {
      await createSprint(selectedProject.id, {
        name: data.name,
        goal: data.goal || undefined,
        startDate: data.startDate || undefined,
        endDate: data.endDate || undefined,
      })
      await reloadBoard()
    } catch (error) {
      setError(error instanceof Error ? error.message : 'Không tạo được sprint')
    } finally {
      setSaving(false)
    }
  }

  const handleMoveToSprint = async (itemId: string, sprintId: string, sourceSprintId?: string | null) => {
    if (!selectedProject) return
    moveItemInBoard(itemId, sprintId)
    setSaving(true)
    try {
      if (sourceSprintId) await removeBacklogItemFromSprint(selectedProject.id, sourceSprintId, itemId)
      await addBacklogItemToSprint(selectedProject.id, sprintId, itemId)
      await reloadBoard()
    } catch (error) {
      setError(error instanceof Error ? error.message : 'Không kéo item vào sprint được')
      await reloadBoard()
    } finally {
      setSaving(false)
    }
  }

  const handleMoveToBacklog = async (itemId: string, sprintId: string) => {
    if (!selectedProject) return
    moveItemInBoard(itemId, null)
    setSaving(true)
    try {
      await removeBacklogItemFromSprint(selectedProject.id, sprintId, itemId)
      await reloadBoard()
    } catch (error) {
      setError(error instanceof Error ? error.message : 'Không đưa item về backlog được')
      await reloadBoard()
    } finally {
      setSaving(false)
    }
  }

  const handleBacklogStatus = async (itemId: string, status: BacklogItemStatus) => {
    if (!selectedProject) return
    setSaving(true)
    try {
      await updateBacklogItemStatus(selectedProject.id, itemId, status)
      await reloadBoard()
    } catch (error) {
      setError(error instanceof Error ? error.message : 'Không cập nhật được trạng thái backlog item')
      await reloadBoard()
    } finally {
      setSaving(false)
    }
  }

  const handleBacklogPriority = async (itemId: string, priority: BacklogPriority) => {
    if (!selectedProject) return
    setSaving(true)
    try {
      await updateBacklogItemPriority(selectedProject.id, itemId, priority)
      await reloadBoard()
    } catch (error) {
      setError(error instanceof Error ? error.message : 'Không cập nhật được độ ưu tiên backlog item')
      await reloadBoard()
    } finally {
      setSaving(false)
    }
  }

  const handleSprintAction = async (action: (projectId: string, sprintId: string) => Promise<Sprint>, sprintId: string) => {
    if (!selectedProject) return
    setSaving(true)
    setError('')
    try {
      await action(selectedProject.id, sprintId)
      await reloadBoard()
    } catch (error) {
      setError(error instanceof Error ? error.message : 'Không cập nhật được sprint')
    } finally {
      setSaving(false)
    }
  }

  const handleUpdateBacklog = async (itemId: string, data: Parameters<typeof updateBacklogItem>[2]) => {
    if (!selectedProject) return
    setSaving(true)
    setError('')
    try {
      await updateBacklogItem(selectedProject.id, itemId, data)
      await reloadBoard()
    } catch (error) {
      setError(error instanceof Error ? error.message : 'Không cập nhật được backlog item')
    } finally {
      setSaving(false)
    }
  }

  const handleDeleteBacklog = async (itemId: string) => {
    if (!selectedProject) return
    setSaving(true)
    setError('')
    try {
      await deleteBacklogItem(selectedProject.id, itemId)
      await reloadBoard()
    } catch (error) {
      setError(error instanceof Error ? error.message : 'Không xóa được backlog item')
    } finally {
      setSaving(false)
    }
  }

  const handleUpdateSprint = async (sprintId: string, data: Parameters<typeof updateSprint>[2]) => {
    if (!selectedProject) return
    setSaving(true)
    setError('')
    try {
      await updateSprint(selectedProject.id, sprintId, data)
      await reloadBoard()
    } catch (error) {
      setError(error instanceof Error ? error.message : 'Không cập nhật được sprint')
    } finally {
      setSaving(false)
    }
  }

  const handleDeleteSprint = async (sprintId: string) => {
    if (!selectedProject) return
    setSaving(true)
    setError('')
    try {
      await deleteSprint(selectedProject.id, sprintId)
      setSelectedSprintId(current => current === sprintId ? null : current)
      await reloadBoard()
    } catch (error) {
      setError(error instanceof Error ? error.message : 'Không xóa được sprint')
    } finally {
      setSaving(false)
    }
  }

  const handleCreateTask = async (data: {
    backlogItemId: string
    title: string
    description?: string
    type?: TaskType
    priority?: TaskPriority
    assigneeUserId?: string
    estimatedMinutes?: number
    startDate?: string
    dueDate?: string
  }) => {
    if (!selectedProject) return
    setSaving(true)
    try {
      await createTask(selectedProject.id, data)
      await reloadBoard()
    } catch (error) {
      setError(error instanceof Error ? error.message : 'Không tạo được Task')
    } finally {
      setSaving(false)
    }
  }

  const handleUpdateTask = async (taskId: string, data: {
    title?: string
    description?: string
    type?: TaskType
    priority?: TaskPriority
    estimatedMinutes?: number
    startDate?: string
    dueDate?: string
  }) => {
    if (!selectedProject) return
    setSaving(true)
    try {
      await updateTask(selectedProject.id, taskId, data)
      await Promise.all([reloadBoard(), loadSelectedTask(taskId)])
    } catch (error) {
      setError(error instanceof Error ? error.message : 'Không cập nhật được Task')
    } finally {
      setSaving(false)
    }
  }

  const handleTaskStatusChange = async (taskId: string, status: TaskStatus, position?: number) => {
    if (!selectedProject) return

    const board = selectedSprintId ? kanbanBoards[selectedSprintId] : undefined
    const task = board?.columns.flatMap(c => c.tasks).find(t => t.id === taskId)
    const oldStatus = task?.status

    if (status === 'BLOCKED') {
      setBlockTaskState({ taskId, status, position })
      return
    }

    if (oldStatus === 'DONE' && status !== 'DONE') {
      setReopenTaskState({ taskId, targetStatus: status, position })
      return
    }

    setSaving(true)
    try {
      await updateTaskStatus(selectedProject.id, taskId, status, position)
      await reloadBoard()
      if (selectedTask?.id === taskId) await loadSelectedTask(taskId)
    } catch (error) {
      setError(error instanceof Error ? error.message : 'Không đổi được trạng thái Task')
      await reloadBoard()
    } finally {
      setSaving(false)
    }
  }

  const handleAssignTask = async (taskId: string, assigneeUserId: string) => {
    if (!selectedProject) return
    setSaving(true)
    try {
      if (assigneeUserId) await assignTask(selectedProject.id, taskId, assigneeUserId)
      else await unassignTask(selectedProject.id, taskId)
      await Promise.all([reloadBoard(), loadSelectedTask(taskId)])
    } catch (error) {
      setError(error instanceof Error ? error.message : 'Không phân công được Task')
    } finally {
      setSaving(false)
    }
  }

  const handleAddDependency = async (taskId: string, dependsOnTaskId: string) => {
    if (!selectedProject) return
    setSaving(true)
    try {
      await addTaskDependency(selectedProject.id, taskId, dependsOnTaskId)
      const [dependencies, risk] = await Promise.all([
        getTaskDependencies(selectedProject.id, taskId),
        getTaskRisk(selectedProject.id, taskId).catch(() => null),
      ])
      setTaskDependencies(dependencies || [])
      setTaskRisk(risk)
      await reloadBoard()
    } catch (error) {
      setError(error instanceof Error ? error.message : 'Không thêm được liên kết phụ thuộc')
    } finally {
      setSaving(false)
    }
  }

  const handleRemoveDependency = async (taskId: string, dependencyId: string) => {
    if (!selectedProject) return
    setSaving(true)
    try {
      await removeTaskDependency(selectedProject.id, taskId, dependencyId)
      const [dependencies, risk] = await Promise.all([
        getTaskDependencies(selectedProject.id, taskId),
        getTaskRisk(selectedProject.id, taskId).catch(() => null),
      ])
      setTaskDependencies(dependencies || [])
      setTaskRisk(risk)
      await reloadBoard()
    } catch (error) {
      setError(error instanceof Error ? error.message : 'Không xóa được liên kết phụ thuộc')
    } finally {
      setSaving(false)
    }
  }

  const handleUnblockTask = async (taskId: string, targetStatus: TaskStatus) => {
    if (!selectedProject) return
    setSaving(true)
    try {
      await unblockTask(selectedProject.id, taskId, targetStatus)
      await reloadBoard()
      await loadSelectedTask(taskId)
    } catch (error) {
      setError(error instanceof Error ? error.message : 'Không mở chặn được Task')
    } finally {
      setSaving(false)
    }
  }

  const handleCreateTaskComment = async (taskId: string, content: string, parentCommentId?: string) => {
    if (!selectedProject) return
    setSaving(true)
    try {
      if (parentCommentId) {
        await createTaskCommentReply(selectedProject.id, taskId, parentCommentId, content)
      } else {
        await createTaskComment(selectedProject.id, taskId, { content })
      }
      await refreshSelectedTask()
    } catch (error) {
      setError(error instanceof Error ? error.message : 'Không gửi được bình luận')
    } finally {
      setSaving(false)
    }
  }

  const handleUpdateTaskComment = async (taskId: string, commentId: string, content: string) => {
    if (!selectedProject) return
    setSaving(true)
    try {
      await updateTaskComment(selectedProject.id, taskId, commentId, content)
      await refreshSelectedTask()
    } catch (error) {
      setError(error instanceof Error ? error.message : 'Không sửa được bình luận')
    } finally {
      setSaving(false)
    }
  }

  const handleDeleteTaskComment = async (taskId: string, commentId: string) => {
    if (!selectedProject) return
    setSaving(true)
    try {
      await deleteTaskComment(selectedProject.id, taskId, commentId)
      await refreshSelectedTask()
    } catch (error) {
      setError(error instanceof Error ? error.message : 'Không xóa được bình luận')
    } finally {
      setSaving(false)
    }
  }

  const handleCreateTaskTimeLog = async (taskId: string, data: { workDate: string; minutes: number; description?: string }) => {
    if (!selectedProject) return
    setSaving(true)
    try {
      await createTaskTimeLog(selectedProject.id, taskId, data)
      await Promise.all([refreshSelectedTask(), reloadBoard()])
    } catch (error) {
      setError(error instanceof Error ? error.message : 'Không ghi được thời gian làm việc')
    } finally {
      setSaving(false)
    }
  }

  const handleUpdateTaskTimeLog = async (taskId: string, timeLogId: string, data: { workDate?: string; minutes?: number; description?: string }) => {
    if (!selectedProject) return
    setSaving(true)
    try {
      await updateTaskTimeLog(selectedProject.id, taskId, timeLogId, data)
      await Promise.all([refreshSelectedTask(), reloadBoard()])
    } catch (error) {
      setError(error instanceof Error ? error.message : 'Không sửa được time log')
    } finally {
      setSaving(false)
    }
  }

  const handleDeleteTaskTimeLog = async (taskId: string, timeLogId: string) => {
    if (!selectedProject) return
    setSaving(true)
    try {
      await deleteTaskTimeLog(selectedProject.id, taskId, timeLogId)
      await Promise.all([refreshSelectedTask(), reloadBoard()])
    } catch (error) {
      setError(error instanceof Error ? error.message : 'Không xóa được time log')
    } finally {
      setSaving(false)
    }
  }

  const handleDeleteTask = async (taskId: string) => {
    if (!selectedProject) return
    setSaving(true)
    try {
      await deleteTask(selectedProject.id, taskId)
      setSelectedTask(null)
      await reloadBoard()
    } catch (error) {
      setError(error instanceof Error ? error.message : 'Không xóa được Task')
    } finally {
      setSaving(false)
    }
  }

  const handleDownloadTaskTemplate = async (sprintId: string) => {
    if (!selectedProject) return
    try {
      const { blob, fileName } = await downloadTaskExcelTemplate(selectedProject.id, sprintId)
      const url = URL.createObjectURL(blob)
      const anchor = document.createElement('a')
      anchor.href = url
      anchor.download = fileName
      anchor.click()
      URL.revokeObjectURL(url)
    } catch (error) {
      setError(error instanceof Error ? error.message : 'Không tải được file mẫu Task')
    }
  }

  const handleImportTasks = async (sprintId: string, file: File) => {
    if (!selectedProject) return
    setSaving(true)
    try {
      const result = await importTasksFromExcel(selectedProject.id, sprintId, file)
      setTaskImportResult(result)
      setSelectedSprintId(sprintId)
      await reloadBoard()
      if (result.failedRows > 0) {
        const firstError = result.errors[0]
        const detail = firstError ? ` Dòng ${firstError.rowNumber}: ${firstError.message}` : ''
        setError(`Import thất bại ${result.failedRows}/${result.totalRows} dòng.${detail}`)
      }
    } catch (error) {
      setTaskImportResult(null)
      setError(error instanceof Error ? error.message : 'Không import được Task')
    } finally {
      setSaving(false)
    }
  }

  return <>
    <ProjectWorkspaceView
      user={user}
      projects={projects}
      selectedProject={selectedProject}
      members={members}
      activities={activities}
      notifications={notifications}
      backlogItems={backlogItems.content}
      sprints={sprints.content}
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
      candidateUsers={candidateUsers.content}
      unreadCount={unreadCount}
      filters={filters}
      activityFilters={activityFilters}
      onActivityFiltersChange={setActivityFilters}
      onActivitySearch={(f) => {
        setAppliedActivityFilters(f)
        setActivityPage(0)
      }}
      onActivityPageChange={setActivityPage}
      activeTab={activeTab}
      page={page}
      activityPage={activityPage}
      loading={loading}
      detailLoading={detailLoading}
      taskDetailLoading={taskDetailLoading}
      candidateLoading={candidateLoading}
      saving={saving}
      errors={errors}
      onDismissError={dismissError}
      onLogout={onLogout}
      onFiltersChange={setFilters}
      onSearch={(forceFilters) => {
        setPage(0)
        setAppliedFilters(forceFilters || filters)
      }}
      onPageChange={setPage}
      onSelectProject={project => {
        setSelectedProject(project)
        setActivityPage(0)
        setActivityFilters({})
        setAppliedActivityFilters({})
        setActiveTab('dashboard')
        setSelectedSprintId(null)
        setSprintStatistics(null)
        setSprintBurndown(null)
        setSprintCapacity(null)
        setSprintHealth(null)
        setSprintRisks(null)
      }}
      onCreateProject={handleCreateProject}
      onUpdateProject={handleUpdateProject}
      onDeleteProject={handleDeleteProject}
      onStatusChange={handleStatusChange}
      onTabChange={setActiveTab}
      onAddMember={handleAddMember}
      onCandidateSearch={handleCandidateSearch}
      onRoleChange={handleRoleChange}
      onRemoveMember={setConfirmRemoveMember}
      onReadNotification={handleReadNotification}
      onDeleteNotification={handleDeleteNotification}
      onReadAllNotifications={handleReadAll}
      onOpenSettings={onOpenSettings}
      onCreateBacklog={handleCreateBacklog}
      onCreateSprint={handleCreateSprint}
      onUpdateBacklog={handleUpdateBacklog}
      onDeleteBacklog={handleDeleteBacklog}
      onUpdateSprint={handleUpdateSprint}
      onDeleteSprint={handleDeleteSprint}
      onMoveToSprint={handleMoveToSprint}
      onMoveToBacklog={handleMoveToBacklog}
      onBacklogStatusChange={handleBacklogStatus}
      onBacklogPriorityChange={handleBacklogPriority}
      onStartSprint={sprintId => handleSprintAction(startSprint, sprintId)}
      onCompleteSprint={sprintId => handleSprintAction(completeSprint, sprintId)}
      onCancelSprint={sprintId => handleSprintAction(cancelSprint, sprintId)}
      onSelectSprint={setSelectedSprintId}
      onCreateTask={handleCreateTask}
      onUpdateTask={handleUpdateTask}
      onDeleteTask={handleDeleteTask}
      onTaskStatusChange={handleTaskStatusChange}
      onAssignTask={handleAssignTask}
      onOpenTask={taskId => void loadSelectedTask(taskId)}
      onCloseTask={() => {
        setSelectedTask(null)
        setTaskComments(emptyCommentPage)
        setTaskTimeLogs(emptyTimeLogPage)
        setTaskTimeSummary(null)
        setTaskDependencies([])
        setTaskRisk(null)
      }}
      onAddDependency={handleAddDependency}
      onRemoveDependency={handleRemoveDependency}
      onUnblockTask={handleUnblockTask}
      onCreateTaskComment={handleCreateTaskComment}
      onUpdateTaskComment={handleUpdateTaskComment}
      onDeleteTaskComment={handleDeleteTaskComment}
      onCreateTaskTimeLog={handleCreateTaskTimeLog}
      onUpdateTaskTimeLog={handleUpdateTaskTimeLog}
      onDeleteTaskTimeLog={handleDeleteTaskTimeLog}
      onDownloadTaskTemplate={handleDownloadTaskTemplate}
      onImportTasks={handleImportTasks}
      onClearTaskImportResult={() => setTaskImportResult(null)}
      onRefreshSprintStats={loadSprintStatistics}
    />
    <ConfirmDialog
      open={Boolean(confirmRemoveMember)}
      title="Xóa thành viên khỏi dự án?"
      description={`Thành viên “${confirmRemoveMember?.username ?? ''}” sẽ không còn quyền trong dự án này.`}
      confirmLabel="Xóa thành viên"
      loading={saving}
      onCancel={() => setConfirmRemoveMember(null)}
      onConfirm={handleRemoveMember}
    />

    <Modal
      open={Boolean(blockTaskState)}
      title="Chặn Task (Block Task)"
      description="Vui lòng cung cấp lý do chi tiết cho việc chặn Task này."
      onClose={() => {
        setBlockTaskState(null)
        setBlockReason('')
      }}
    >
      <div className="space-y-4">
        <textarea
          className="min-h-24 w-full rounded-lg border border-line bg-white px-3.5 py-3 text-sm outline-none focus:border-brand focus:ring-2 focus:ring-brand/20"
          placeholder="Lý do chặn task..."
          value={blockReason}
          onChange={event => setBlockReason(event.target.value)}
        />
        <div className="flex justify-end gap-3">
          <Button
            variant="secondary"
            onClick={() => {
              setBlockTaskState(null)
              setBlockReason('')
            }}
          >
            Hủy
          </Button>
          <Button
            loading={saving}
            disabled={!blockReason.trim()}
            onClick={async () => {
              if (!selectedProject || !blockTaskState) return
              setSaving(true)
              try {
                await blockTask(selectedProject.id, blockTaskState.taskId, blockReason)
                setBlockTaskState(null)
                setBlockReason('')
                await reloadBoard()
                if (selectedTask?.id === blockTaskState.taskId) await loadSelectedTask(blockTaskState.taskId)
              } catch (error) {
                setError(error instanceof Error ? error.message : 'Không block được Task')
                await reloadBoard()
              } finally {
                setSaving(false)
              }
            }}
          >
            Xác nhận Chặn
          </Button>
        </div>
      </div>
    </Modal>

    <Modal
      open={Boolean(reopenTaskState)}
      title="Mở lại Task (Reopen Task)"
      description="Bạn có chắc chắn muốn mở lại Task này không?"
      onClose={() => {
        setReopenTaskState(null)
        setReopenReason('')
      }}
    >
      <div className="space-y-4">
        <textarea
          className="min-h-24 w-full rounded-lg border border-line bg-white px-3.5 py-3 text-sm outline-none focus:border-brand focus:ring-2 focus:ring-brand/20"
          placeholder="Lý do mở lại (tùy chọn)..."
          value={reopenReason}
          onChange={event => setReopenReason(event.target.value)}
        />
        <div className="flex justify-end gap-3">
          <Button
            variant="secondary"
            onClick={() => {
              setReopenTaskState(null)
              setReopenReason('')
            }}
          >
            Hủy
          </Button>
          <Button
            loading={saving}
            onClick={async () => {
              if (!selectedProject || !reopenTaskState) return
              setSaving(true)
              try {
                await reopenTask(
                  selectedProject.id,
                  reopenTaskState.taskId,
                  reopenTaskState.targetStatus,
                  reopenReason || undefined
                )
                setReopenTaskState(null)
                setReopenReason('')
                await reloadBoard()
                if (selectedTask?.id === reopenTaskState.taskId) await loadSelectedTask(reopenTaskState.taskId)
              } catch (error) {
                setError(error instanceof Error ? error.message : 'Không mở lại được Task')
                await reloadBoard()
              } finally {
                setSaving(false)
              }
            }}
          >
            Xác nhận Mở lại
          </Button>
        </div>
      </div>
    </Modal>
  </>
}
