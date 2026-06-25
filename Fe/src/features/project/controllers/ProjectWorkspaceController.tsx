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
import { getNotifications, getUnreadCount, markAllNotificationsRead, markNotificationRead } from '../services/notification.service'
import { ProjectWorkspaceView } from '../views/ProjectWorkspaceView'
import { searchProjectCandidateUsers } from '../../user/services/user.service'
import type { UserPage } from '../../user/models/user.model'

const emptyProjectPage: ProjectPage = { content: [], totalElements: 0, totalPages: 0, number: 0, size: 9, numberOfElements: 0, first: true, last: true, empty: true }
const emptyActivityPage: ProjectActivityPage = { content: [], totalElements: 0, totalPages: 0, number: 0, size: 10, numberOfElements: 0, first: true, last: true, empty: true }
const emptyNotificationPage: NotificationPage = { content: [], totalElements: 0, totalPages: 0, number: 0, size: 6, numberOfElements: 0, first: true, last: true, empty: true }
const emptyCandidatePage: UserPage = { content: [], totalElements: 0, totalPages: 0, number: 0, size: 8 }
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
  const [unreadCount, setUnreadCount] = useState(0)
  const [activeTab, setActiveTab] = useState<'members' | 'activities' | 'notifications'>('members')
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

  return <>
    <ProjectWorkspaceView
      user={user}
      projects={projects}
      selectedProject={selectedProject}
      members={members}
      activities={activities}
      notifications={notifications}
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
