import { useCallback, useEffect, useState } from 'react'
import { ConfirmDialog } from '../../../components/ui'
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
import type { BacklogItem, BacklogItemPage, BacklogItemStatus, BacklogPriority, Sprint, SprintPage } from '../models/scrum.model'
import {
  addProjectMember,
  createProject,
  getProject,
  getProjectActivities,
  getProjectMembers,
  removeProjectMember,
  searchProjects,
  updateProjectMemberRole,
  updateProjectStatus,
} from '../services/project.service'
import {
  addBacklogItemToSprint,
  cancelSprint,
  completeSprint,
  createBacklogItem,
  createSprint,
  getBacklogItems,
  getSprints,
  removeBacklogItemFromSprint,
  startSprint,
  updateBacklogItemPriority,
  updateBacklogItemStatus,
} from '../services/scrum.service'
import { getNotifications, getUnreadCount, markAllNotificationsRead, markNotificationRead } from '../services/notification.service'
import { ProjectWorkspaceView } from '../views/ProjectWorkspaceView'
import { searchProjectCandidateUsers } from '../../user/services/user.service'
import type { UserPage } from '../../user/models/user.model'

const emptyProjectPage: ProjectPage = { content: [], totalElements: 0, totalPages: 0, number: 0, size: 9, numberOfElements: 0, first: true, last: true, empty: true }
const emptyActivityPage: ProjectActivityPage = { content: [], totalElements: 0, totalPages: 0, number: 0, size: 10, numberOfElements: 0, first: true, last: true, empty: true }
const emptyNotificationPage: NotificationPage = { content: [], totalElements: 0, totalPages: 0, number: 0, size: 6, numberOfElements: 0, first: true, last: true, empty: true }
const emptyCandidatePage: UserPage = { content: [], totalElements: 0, totalPages: 0, number: 0, size: 8 }
const emptyBacklogPage: BacklogItemPage = { content: [], totalElements: 0, totalPages: 0, number: 0, size: 200, numberOfElements: 0, first: true, last: true, empty: true }
const emptySprintPage: SprintPage = { content: [], totalElements: 0, totalPages: 0, number: 0, size: 50, numberOfElements: 0, first: true, last: true, empty: true }
const initialFilters: ProjectFilters = { keyword: '', status: '' }

export function ProjectWorkspaceController({ user, onLogout }: { user: User; onLogout: () => void }) {
  const [projects, setProjects] = useState(emptyProjectPage)
  const [filters, setFilters] = useState(initialFilters)
  const [appliedFilters, setAppliedFilters] = useState(initialFilters)
  const [page, setPage] = useState(0)
  const [selectedProject, setSelectedProject] = useState<Project | null>(null)
  const [members, setMembers] = useState<ProjectMember[]>([])
  const [activities, setActivities] = useState(emptyActivityPage)
  const [activityPage, setActivityPage] = useState(0)
  const [notifications, setNotifications] = useState(emptyNotificationPage)
  const [candidateUsers, setCandidateUsers] = useState(emptyCandidatePage)
  const [backlogItems, setBacklogItems] = useState(emptyBacklogPage)
  const [sprints, setSprints] = useState(emptySprintPage)
  const [sprintItems, setSprintItems] = useState<Record<string, BacklogItem[]>>({})
  const [unreadCount, setUnreadCount] = useState(0)
  const [activeTab, setActiveTab] = useState<'board' | 'members' | 'activities' | 'notifications'>('board')
  const [loading, setLoading] = useState(false)
  const [detailLoading, setDetailLoading] = useState(false)
  const [candidateLoading, setCandidateLoading] = useState(false)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [confirmRemoveMember, setConfirmRemoveMember] = useState<ProjectMember | null>(null)

  const loadProjects = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      const result = await searchProjects(appliedFilters, page)
      setProjects(result)
      setSelectedProject(current => current ?? result.content[0] ?? null)
    } catch (error) {
      setError(error instanceof Error ? error.message : 'Không tải được danh sách dự án')
    } finally {
      setLoading(false)
    }
  }, [appliedFilters, page])

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
        getProjectActivities(projectId, activityPage),
      ])
      setSelectedProject(project)
      setMembers(projectMembers)
      setActivities(projectActivities)
    } catch (error) {
      setError(error instanceof Error ? error.message : 'Không tải được chi tiết dự án')
    } finally {
      setDetailLoading(false)
    }
  }, [activityPage])

  useEffect(() => {
    if (selectedProject?.id) void Promise.resolve().then(() => loadProjectDetail(selectedProject.id))
  }, [selectedProject?.id, loadProjectDetail])

  const loadScrumBoard = useCallback(async (projectId: string, options: { silent?: boolean } = {}) => {
    if (!options.silent) setDetailLoading(true)
    setError('')
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
      setBacklogItems(unscheduled)
      setSprints(sprintPage)
      setSprintItems(Object.fromEntries(itemEntries))
    } catch (error) {
      setError(error instanceof Error ? error.message : 'Không tải được Sprint Board')
    } finally {
      if (!options.silent) setDetailLoading(false)
    }
  }, [])

  useEffect(() => {
    if (selectedProject?.id) void Promise.resolve().then(() => loadScrumBoard(selectedProject.id))
  }, [selectedProject?.id, loadScrumBoard])

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

  const reloadBoard = async () => {
    if (selectedProject) await loadScrumBoard(selectedProject.id, { silent: true })
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
    await updateBacklogItemStatus(selectedProject.id, itemId, status)
    await reloadBoard()
  }

  const handleBacklogPriority = async (itemId: string, priority: BacklogPriority) => {
    if (!selectedProject) return
    await updateBacklogItemPriority(selectedProject.id, itemId, priority)
    await reloadBoard()
  }

  const handleSprintAction = async (action: (projectId: string, sprintId: string) => Promise<Sprint>, sprintId: string) => {
    if (!selectedProject) return
    setSaving(true)
    try {
      await action(selectedProject.id, sprintId)
      await reloadBoard()
    } catch (error) {
      setError(error instanceof Error ? error.message : 'Không cập nhật được sprint')
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
      candidateUsers={candidateUsers.content}
      unreadCount={unreadCount}
      filters={filters}
      activeTab={activeTab}
      page={page}
      activityPage={activityPage}
      loading={loading}
      detailLoading={detailLoading}
      candidateLoading={candidateLoading}
      saving={saving}
      error={error}
      onLogout={onLogout}
      onFiltersChange={setFilters}
      onSearch={() => {
        setPage(0)
        setAppliedFilters(filters)
      }}
      onPageChange={setPage}
      onSelectProject={project => {
        setSelectedProject(project)
        setActivityPage(0)
      }}
      onCreateProject={handleCreateProject}
      onStatusChange={handleStatusChange}
      onTabChange={setActiveTab}
      onActivityPageChange={setActivityPage}
      onAddMember={handleAddMember}
      onCandidateSearch={handleCandidateSearch}
      onRoleChange={handleRoleChange}
      onRemoveMember={setConfirmRemoveMember}
      onReadNotification={handleReadNotification}
      onReadAllNotifications={handleReadAll}
      onCreateBacklog={handleCreateBacklog}
      onCreateSprint={handleCreateSprint}
      onMoveToSprint={handleMoveToSprint}
      onMoveToBacklog={handleMoveToBacklog}
      onBacklogStatusChange={handleBacklogStatus}
      onBacklogPriorityChange={handleBacklogPriority}
      onStartSprint={sprintId => handleSprintAction(startSprint, sprintId)}
      onCompleteSprint={sprintId => handleSprintAction(completeSprint, sprintId)}
      onCancelSprint={sprintId => handleSprintAction(cancelSprint, sprintId)}
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
  </>
}
