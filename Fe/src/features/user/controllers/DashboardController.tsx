import { useCallback, useEffect, useState } from 'react'
import { ConfirmDialog, toast } from '../../../components/ui'
import type { AuditLogPage } from '../models/audit-log.model'
import { createUser, deleteUser, getUserRoles, searchUsers, updateUser } from '../services/user.service'
import { subscribeAuditLogs } from '../services/audit-log.websocket'
import type { CreateUserData, User, UserFilters, UserPage, UserRole } from '../models/user.model'
import { DashboardView } from '../views/DashboardView'
import { UserFormModal } from '../views/UserFormModal'

import { getAdminDashboard, enableUser, disableUser, updateUserRole, searchSystemAuditLogs, getAuditSummary } from '../services/admin.service'
import type { AdminDashboardResponse, AdminAuditSummaryResponse } from '../models/admin.model'
import { AdminUserActivityModal } from '../views/AdminUserActivityModal'

const emptyPage: UserPage = { content: [], totalElements: 0, totalPages: 0, number: 0, size: 8 }
const emptyAuditPage: AuditLogPage = { content: [], totalElements: 0, totalPages: 0, number: 0, size: 12, numberOfElements: 0, first: true, last: true, empty: true }
const initialFilters: UserFilters = { keyword: '', role: '', enabled: '' }
const fallbackRoles: UserRole[] = ['ADMIN', 'MANAGER', 'EMPLOYEE']
export type AdminSection = 'overview' | 'members' | 'audit' | 'files'

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

  const [adminDashboard, setAdminDashboard] = useState<AdminDashboardResponse | null>(null)
  const [adminDashboardLoading, setAdminDashboardLoading] = useState(false)
  const [adminDashboardError, setAdminDashboardError] = useState('')

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

  useEffect(() => {
    void Promise.resolve().then(loadAdminDashboard)
  }, [loadAdminDashboard])

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

  return <>
    <DashboardView
      me={me}
      activeSection={activeSection}
      adminDashboard={adminDashboard}
      adminDashboardLoading={adminDashboardLoading}
      adminDashboardError={adminDashboardError}
      onRefreshAdminDashboard={loadAdminDashboard}
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
      onSectionChange={setActiveSection}
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
