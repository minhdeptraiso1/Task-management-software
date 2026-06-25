import { useCallback, useEffect, useState } from 'react'
import { ConfirmDialog } from '../../../components/ui'
import type { AuditLogPage } from '../models/audit-log.model'
import { createUser, deleteUser, getUserRoles, searchUsers, updateUser } from '../services/user.service'
import { getAuditLogs } from '../services/audit-log.service'
import { subscribeAuditLogs } from '../services/audit-log.websocket'
import type { CreateUserData, User, UserFilters, UserPage, UserRole } from '../models/user.model'
import { DashboardView } from '../views/DashboardView'
import { UserFormModal } from '../views/UserFormModal'

const emptyPage: UserPage = { content: [], totalElements: 0, totalPages: 0, number: 0, size: 8 }
const emptyAuditPage: AuditLogPage = { content: [], totalElements: 0, totalPages: 0, number: 0, size: 12, numberOfElements: 0, first: true, last: true, empty: true }
const initialFilters: UserFilters = { keyword: '', role: '', enabled: '' }
const fallbackRoles: UserRole[] = ['ADMIN', 'MANAGER', 'EMPLOYEE']
type AdminSection = 'members' | 'audit'

export function DashboardController({ me, onLogout }: { me: User; onLogout: () => void }) {
  const [users, setUsers] = useState(emptyPage)
  const [activeSection, setActiveSection] = useState<AdminSection>('members')
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

  const loadAuditLogs = useCallback(async () => {
    setAuditLoading(true)
    setAuditError('')
    try {
      setAuditLogs(await getAuditLogs(auditPage))
    } catch (error) {
      setAuditError(error instanceof Error ? error.message : 'Không tải được lịch sử hoạt động')
    } finally {
      setAuditLoading(false)
    }
  }, [auditPage])

  useEffect(() => {
    if (activeSection === 'audit') void Promise.resolve().then(loadAuditLogs)
  }, [activeSection, loadAuditLogs])

  useEffect(() => {
    if (activeSection !== 'audit') return undefined

    return subscribeAuditLogs({
      onStatusChange: setAuditRealtimeStatus,
      onLog: log => {
        setAuditLogs(current => {
          if (auditPage !== 0 || current.content.some(item => item.id === log.id)) return current

          return {
            ...current,
            content: [log, ...current.content].slice(0, current.size),
            totalElements: current.totalElements + 1,
            numberOfElements: Math.min(current.numberOfElements + 1, current.size),
            empty: false,
          }
        })
      },
    })
  }, [activeSection, auditPage])

  const save = async (data: CreateUserData | { role: UserRole; enabled: boolean; password?: string }) => {
    setSaving(true)
    try {
      if (selected) await updateUser(selected.id, data)
      else await createUser(data as CreateUserData)
      setModalOpen(false)
      await loadUsers()
    } catch (error) {
      setError(error instanceof Error ? error.message : 'Không thể lưu thành viên')
    } finally {
      setSaving(false)
    }
  }

  const remove = async () => {
    if (!pendingDelete) return
    setSaving(true)
    try {
      if (pendingDelete.enabled) await updateUser(pendingDelete.id, { enabled: false })
      else await deleteUser(pendingDelete.id)
      setPendingDelete(null)
      await loadUsers()
    } catch (error) {
      setError(error instanceof Error ? error.message : 'Không thể cập nhật tài khoản')
    } finally {
      setSaving(false)
    }
  }

  return <>
    <DashboardView
      me={me}
      activeSection={activeSection}
      users={users}
      auditLogs={auditLogs}
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
      onFiltersChange={setFilters}
      onSearch={() => {
        setPage(0)
        setAppliedFilters(filters)
      }}
      onPageChange={setPage}
      onAuditPageChange={setAuditPage}
      onAuditRefresh={loadAuditLogs}
      onCreate={() => {
        setSelected(null)
        setModalOpen(true)
      }}
      onEdit={user => {
        setSelected(user)
        setModalOpen(true)
      }}
      onDelete={setPendingDelete}
      onLogout={onLogout}
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
