import { useCallback, useEffect, useState } from 'react'
import { ConfirmDialog, toast } from '../../../components/ui'
import type { AuditLogPage } from '../models/audit-log.model'
import { createUser, deleteUser, getUserRoles, searchUsers, updateUser } from '../services/user.service'
import { subscribeAuditLogs } from '../services/audit-log.websocket'
import type { CreateUserData, User, UserFilters, UserPage, UserRole } from '../models/user.model'
import { DashboardView } from '../views/DashboardView'
import { UserFormModal } from '../views/UserFormModal'

import { getAdminDashboard, getAdminSystemStatus, enableUser, disableUser, updateUserRole, searchSystemAuditLogs, getAuditSummary } from '../services/admin.service'
import type { AdminDashboardResponse, AdminAuditSummaryResponse, AdminSystemStatusResponse } from '../models/admin.model'
import { AdminUserActivityModal } from '../views/AdminUserActivityModal'
import { createProject, searchProjects } from '../../project/services/project.service'
import { ProjectCreateModal, type ProjectCreateFormData } from '../../project/components/ProjectCreateModal'
import { getProjectDashboard } from '../../dashboard/services/dashboard.service'
import type { AdminProjectProgressFilters, AdminProjectProgressItem } from '../models/admin.model'

const emptyPage: UserPage = { content: [], totalElements: 0, totalPages: 0, number: 0, size: 8 }
const emptyAuditPage: AuditLogPage = { content: [], totalElements: 0, totalPages: 0, number: 0, size: 12, numberOfElements: 0, first: true, last: true, empty: true }
const initialFilters: UserFilters = { keyword: '', role: '', enabled: '' }
const fallbackRoles: UserRole[] = ['ADMIN', 'MANAGER', 'EMPLOYEE']
export type AdminSection = 'overview' | 'projects' | 'members' | 'audit' | 'files'

function calculateTimelineProgress(startDate: string | null, endDate: string | null) {
  if (!startDate || !endDate) return 0
  const start = new Date(`${startDate}T00:00:00`).getTime()
  const end = new Date(`${endDate}T23:59:59`).getTime()
  const now = Date.now()
  if (!Number.isFinite(start) || !Number.isFinite(end) || end <= start) return 0
  if (now <= start) return 0
  if (now >= end) return 100
  return Math.round(((now - start) / (end - start)) * 100)
}

export function DashboardController({ me, onLogout, onOpenSettings }: { me: User; onLogout: () => void; onOpenSettings: () => void }) {
  const [users, setUsers] = useState(emptyPage)
  const [activeSection, setActiveSection] = useState<AdminSection>('overview')
  const [roles, setRoles] = useState<UserRole[]>(fallbackRoles)
  const [filters, setFilters] = useState(initialFilters)
  const [appliedFilters, setAppliedFilters] = useState(initialFilters)
  const [page, setPage] = useState(0)
  const [auditLogs, setAuditLogs] = useState(emptyAuditPage)
  const [auditPage, setAuditPage] = useState(0)
  const [loading, setLoading] = useState(true)
  const [auditLoading, setAuditLoading] = useState(false)
  const [auditRealtimeStatus, setAuditRealtimeStatus] = useState<'connected' | 'disconnected' | 'error'>('disconnected')
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [auditError, setAuditError] = useState('')
  const [modalOpen, setModalOpen] = useState(false)
  const [selected, setSelected] = useState<User | null>(null)
  const [pendingDelete, setPendingDelete] = useState<User | null>(null)
  const [projectModalOpen, setProjectModalOpen] = useState(false)
  const [projectSaving, setProjectSaving] = useState(false)
  const [managerCandidates, setManagerCandidates] = useState(emptyPage)
  const [managerCandidateLoading, setManagerCandidateLoading] = useState(false)
  const [projectProgressItems, setProjectProgressItems] = useState<AdminProjectProgressItem[]>([])
  const [projectProgressLoading, setProjectProgressLoading] = useState(false)
  const [projectProgressFilters, setProjectProgressFilters] = useState<AdminProjectProgressFilters>({ keyword: '', status: '', managerUserId: '' })

  const [adminDashboard, setAdminDashboard] = useState<AdminDashboardResponse | null>(null)
  const [adminDashboardLoading, setAdminDashboardLoading] = useState(false)
  const [adminDashboardError, setAdminDashboardError] = useState('')
  const [adminSystemStatus, setAdminSystemStatus] = useState<AdminSystemStatusResponse | null>(null)
  const [adminSystemStatusLoading, setAdminSystemStatusLoading] = useState(false)
  const [adminSystemStatusError, setAdminSystemStatusError] = useState('')

  const loadAdminDashboard = useCallback(async () => {
    setAdminDashboardLoading(true)
    setAdminDashboardError('')
    try {
      setAdminDashboard(await getAdminDashboard())
    } catch (err) {
      setAdminDashboardError(err instanceof Error ? err.message : 'Không thể tải Admin Dashboard')
    } finally {
      setAdminDashboardLoading(false)
    }
  }, [])

  const loadAdminSystemStatus = useCallback(async () => {
    setAdminSystemStatusLoading(true)
    setAdminSystemStatusError('')
    try {
      setAdminSystemStatus(await getAdminSystemStatus())
    } catch (err) {
      setAdminSystemStatusError(err instanceof Error ? err.message : 'Không thể kiểm tra trạng thái hệ thống')
    } finally {
      setAdminSystemStatusLoading(false)
    }
  }, [])

  const loadProjectProgress = useCallback(async () => {
    setProjectProgressLoading(true)
    try {
      const projectPage = await searchProjects({
        keyword: '',
        status: '',
        managerUserId: projectProgressFilters.managerUserId || undefined,
      }, 0, 12)
      const progressItems = await Promise.all(projectPage.content.map(async project => {
        try {
          const projectDashboard = await getProjectDashboard(project.id)
          const manager = projectDashboard.workload.find(member => member.projectRole === 'OWNER')
            ?? projectDashboard.workload.find(member => member.projectRole === 'PROJECT_MANAGER')
          return {
            id: project.id,
            code: project.code,
            name: project.name,
            status: project.status,
            startDate: project.startDate,
            endDate: project.endDate,
            totalTasks: projectDashboard.taskSummary.totalTasks,
            completedTasks: projectDashboard.taskSummary.completedTasks,
            completionRate: project.status === 'COMPLETED' ? 100 : projectDashboard.taskSummary.completionRate,
            timelineProgress: calculateTimelineProgress(project.startDate, project.endDate),
            managerUserId: manager?.userId ?? null,
            managerUsername: manager?.username ?? null,
            managerEmail: manager?.email ?? null,
          } satisfies AdminProjectProgressItem
        } catch {
          return {
            id: project.id,
            code: project.code,
            name: project.name,
            status: project.status,
            startDate: project.startDate,
            endDate: project.endDate,
            totalTasks: 0,
            completedTasks: 0,
            completionRate: project.status === 'COMPLETED' ? 100 : 0,
            timelineProgress: calculateTimelineProgress(project.startDate, project.endDate),
            managerUserId: null,
            managerUsername: null,
            managerEmail: null,
          } satisfies AdminProjectProgressItem
        }
      }))
      setProjectProgressItems(progressItems)
    } catch (error) {
      toast.error(error instanceof Error ? error.message : 'Không tải được tiến độ dự án')
    } finally {
      setProjectProgressLoading(false)
    }
  }, [projectProgressFilters.managerUserId])

  const refreshAdminOverview = useCallback(async () => {
    await Promise.all([loadAdminDashboard(), loadAdminSystemStatus(), loadProjectProgress()])
  }, [loadAdminDashboard, loadAdminSystemStatus, loadProjectProgress])

  useEffect(() => {
    void Promise.resolve().then(loadAdminDashboard)
  }, [loadAdminDashboard])

  useEffect(() => {
    void Promise.resolve().then(loadAdminSystemStatus)
  }, [loadAdminSystemStatus])

  useEffect(() => {
    void Promise.resolve().then(loadProjectProgress)
  }, [loadProjectProgress])

  useEffect(() => {
    getUserRoles()
      .then(value => setRoles(value.length ? value : fallbackRoles))
      .catch(() => setRoles(fallbackRoles))
  }, [])

  const loadUsers = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      setUsers(await searchUsers(appliedFilters, page))
    } catch (error) {
      setError(error instanceof Error ? error.message : 'Không tải được thành viên')
    } finally {
      setLoading(false)
    }
  }, [appliedFilters, page])

  useEffect(() => {
    void Promise.resolve().then(loadUsers)
  }, [loadUsers])

  const [auditActionFilter, setAuditActionFilter] = useState('')
  const [auditKeywordFilter, setAuditKeywordFilter] = useState('')
  const [auditSummary, setAuditSummary] = useState<AdminAuditSummaryResponse | null>(null)

  const loadAuditLogs = useCallback(async () => {
    setAuditLoading(true)
    setAuditError('')
    try {
      const [res, summaryRes] = await Promise.all([
        searchSystemAuditLogs({
          page: auditPage,
          size: 12,
          action: auditActionFilter || undefined,
          keyword: auditKeywordFilter || undefined,
        }),
        getAuditSummary({
          keyword: auditKeywordFilter || undefined,
        }).catch(() => null)
      ])
      setAuditLogs(res as any)
      if (summaryRes) setAuditSummary(summaryRes)
    } catch (error) {
      setAuditError(error instanceof Error ? error.message : 'Không tải được lịch sử hoạt động')
    } finally {
      setAuditLoading(false)
    }
  }, [auditPage, auditActionFilter, auditKeywordFilter])

  useEffect(() => {
    if (activeSection === 'audit') void Promise.resolve().then(loadAuditLogs)
  }, [activeSection, loadAuditLogs])

  useEffect(() => {
    if (activeSection !== 'audit') return undefined

    return subscribeAuditLogs({
      onStatusChange: setAuditRealtimeStatus,
      onLog: log => {
        setAuditLogs((current: any) => {
          if (!current || auditPage !== 0 || current.content?.some((item: any) => item.id === log.id)) return current

          return {
            ...current,
            content: [log, ...current.content].slice(0, current.size),
            totalElements: (current.totalElements || 0) + 1,
            numberOfElements: Math.min((current.numberOfElements || 0) + 1, current.size),
            empty: false,
          }
        })
      },
    })
  }, [activeSection, auditPage])

  const [activityUser, setActivityUser] = useState<User | null>(null)

  const handleToggleEnable = async (targetUser: User) => {
    setSaving(true)
    setError('')
    try {
      if (targetUser.enabled) {
        await disableUser(targetUser.id)
        toast.success(`Đã vô hiệu hóa tài khoản ${targetUser.username}`)
      } else {
        await enableUser(targetUser.id)
        toast.success(`Đã kích hoạt tài khoản ${targetUser.username}`)
      }
      await loadUsers()
      await loadAdminDashboard()
      await loadProjectProgress()
    } catch (err) {
      const msg = err instanceof Error ? err.message : 'Không thể thay đổi trạng thái tài khoản'
      setError(msg)
      toast.error(msg)
    } finally {
      setSaving(false)
    }
  }

  const handleUpdateRole = async (targetUser: User, newRole: UserRole) => {
    setSaving(true)
    setError('')
    try {
      await updateUserRole(targetUser.id, newRole)
      toast.success(`Đã cập nhật quyền thành viên ${targetUser.username} thành ${newRole}`)
      await loadUsers()
      await loadAdminDashboard()
    } catch (err) {
      const msg = err instanceof Error ? err.message : 'Không thể cập nhật quyền người dùng'
      setError(msg)
      toast.error(msg)
    } finally {
      setSaving(false)
    }
  }

  const save = async (data: CreateUserData | { role: UserRole; enabled: boolean; password?: string }) => {
    setSaving(true)
    try {
      if (selected) {
        await updateUser(selected.id, data)
        toast.success(`Đã cập nhật tài khoản ${selected.username}`)
      } else {
        await createUser(data as CreateUserData)
        toast.success(`Đã tạo tài khoản thành công`)
      }
      setModalOpen(false)
      await loadUsers()
      await loadAdminDashboard()
    } catch (error) {
      const msg = error instanceof Error ? error.message : 'Không thể lưu thành viên'
      setError(msg)
      toast.error(msg)
    } finally {
      setSaving(false)
    }
  }

  const remove = async () => {
    if (!pendingDelete) return
    setSaving(true)
    try {
      if (pendingDelete.enabled) {
        await disableUser(pendingDelete.id)
        toast.success(`Đã vô hiệu hóa tài khoản ${pendingDelete.username}`)
      } else {
        await deleteUser(pendingDelete.id)
        toast.success(`Đã xóa tài khoản ${pendingDelete.username}`)
      }
      setPendingDelete(null)
      await loadUsers()
      await loadAdminDashboard()
    } catch (error) {
      const msg = error instanceof Error ? error.message : 'Không thể cập nhật tài khoản'
      setError(msg)
      toast.error(msg)
    } finally {
      setSaving(false)
    }
  }

  const handleManagerSearch = async (keyword: string) => {
    setManagerCandidateLoading(true)
    try {
      setManagerCandidates(await searchUsers({ keyword, role: 'MANAGER', enabled: 'true' }, 0, 20))
    } catch (error) {
      toast.error(error instanceof Error ? error.message : 'Không tìm được tài khoản quản lý phù hợp')
    } finally {
      setManagerCandidateLoading(false)
    }
  }

  const openProjectModal = () => {
    setProjectModalOpen(true)
    void handleManagerSearch('')
  }

  const handleCreateProject = async (data: ProjectCreateFormData) => {
    setProjectSaving(true)
    try {
      const project = await createProject({
        code: data.code,
        name: data.name,
        description: data.description || undefined,
        startDate: data.startDate || undefined,
        endDate: data.endDate || undefined,
        ownerUserId: data.ownerUserId,
      })
      setProjectModalOpen(false)
      toast.success(`Đã tạo dự án ${project.code} và giao cho quản lý được chọn`)
      await Promise.all([loadAdminDashboard(), loadProjectProgress()])
    } catch (error) {
      toast.error(error instanceof Error ? error.message : 'Không thể tạo dự án')
    } finally {
      setProjectSaving(false)
    }
  }

  const visibleProjectProgressItems = projectProgressItems.filter(project => {
    const keyword = projectProgressFilters.keyword.trim().toLocaleLowerCase('vi-VN')
    const matchesKeyword = !keyword
      || project.code.toLocaleLowerCase('vi-VN').includes(keyword)
      || project.name.toLocaleLowerCase('vi-VN').includes(keyword)
    const matchesStatus = !projectProgressFilters.status || project.status === projectProgressFilters.status
    return matchesKeyword && matchesStatus
  })

  return <>
    <DashboardView
      me={me}
      activeSection={activeSection}
      adminDashboard={adminDashboard}
      adminDashboardLoading={adminDashboardLoading}
      adminDashboardError={adminDashboardError}
      adminSystemStatus={adminSystemStatus}
      adminSystemStatusLoading={adminSystemStatusLoading}
      adminSystemStatusError={adminSystemStatusError}
      visibleProjectProgressItems={visibleProjectProgressItems}
      projectProgressLoading={projectProgressLoading}
      projectProgressFilters={projectProgressFilters}
      onProjectProgressFiltersChange={setProjectProgressFilters}
      managerFilterOptions={managerCandidates.content}
      onRefreshAdminDashboard={refreshAdminOverview}
      onCreateProject={openProjectModal}
      users={users}
      auditLogs={auditLogs}
      auditSummary={auditSummary}
      roles={roles}
      filters={filters}
      loading={loading}
      auditLoading={auditLoading}
      auditRealtimeStatus={auditRealtimeStatus}
      error={error}
      auditError={auditError}
      page={page}
      auditPage={auditPage}
      onSectionChange={section => {
        setActiveSection(section)
        if (section === 'projects' && managerCandidates.content.length === 0) {
          void handleManagerSearch('')
        }
      }}
      onFiltersChange={newFilters => {
        setFilters(newFilters)
        setAppliedFilters(newFilters)
        setPage(0)
      }}
      onSearch={() => {
        setPage(0)
        setAppliedFilters(filters)
      }}
      onPageChange={setPage}
      onAuditPageChange={setAuditPage}
      onAuditRefresh={loadAuditLogs}
      onAuditFilterChange={({ action, keyword }) => {
        setAuditActionFilter(action ?? '')
        setAuditKeywordFilter(keyword ?? '')
        setAuditPage(0)
      }}
      onCreate={() => {
        setSelected(null)
        setModalOpen(true)
      }}
      onEdit={user => {
        setSelected(user)
        setModalOpen(true)
      }}
      onDelete={setPendingDelete}
      onToggleEnable={handleToggleEnable}
      onUpdateRole={handleUpdateRole}
      onViewActivities={setActivityUser}
      onLogout={onLogout}
      onOpenSettings={onOpenSettings}
    />
    {modalOpen && (
      <UserFormModal
        key={selected?.id ?? 'create-user'}
        open
        user={selected}
        roles={roles}
        loading={saving}
        onClose={() => setModalOpen(false)}
        onSave={save}
      />
    )}
    {activityUser && (
      <AdminUserActivityModal
        user={activityUser}
        onClose={() => setActivityUser(null)}
      />
    )}
    <ProjectCreateModal
      open={projectModalOpen}
      saving={projectSaving}
      isAdmin
      managerCandidates={managerCandidates.content}
      managerCandidateLoading={managerCandidateLoading}
      onManagerSearch={handleManagerSearch}
      onClose={() => setProjectModalOpen(false)}
      onSave={handleCreateProject}
    />
    <ConfirmDialog
      open={Boolean(pendingDelete)}
      title={`${pendingDelete?.enabled ? 'Vô hiệu hóa' : 'Xóa'} tài khoản?`}
      description={`Bạn đang thao tác với tài khoản “${pendingDelete?.username ?? ''}”.`}
      confirmLabel={pendingDelete?.enabled ? 'Vô hiệu hóa' : 'Xóa tài khoản'}
      loading={saving}
      onCancel={() => setPendingDelete(null)}
      onConfirm={remove}
    />
  </>
}
