import {
  Activity,
  ChevronLeft,
  ChevronRight,
  CircleUserRound,
  LayoutDashboard,
  LogOut,
  Pencil,
  Plus,
  Search,
  Settings,
  ShieldCheck,
  Trash2,
  UserX,
  UsersRound,
  type LucideIcon,
} from 'lucide-react'
import { Button, Input, Select, ToolbarActions } from '../../../components/ui'
import type { AuditLogPage } from '../models/audit-log.model'
import { roleLabels, type User, type UserFilters, type UserPage, type UserRole } from '../models/user.model'
import { AuditLogView } from './AuditLogView'

type AdminSection = 'members' | 'audit'

interface Props {
  me: User
  activeSection: AdminSection
  users: UserPage
  auditLogs: AuditLogPage
  roles: UserRole[]
  filters: UserFilters
  loading: boolean
  auditLoading: boolean
  auditRealtimeStatus: 'connected' | 'disconnected' | 'error'
  error: string
  auditError: string
  page: number
  auditPage: number
  onSectionChange: (section: AdminSection) => void
  onFiltersChange: (filters: UserFilters) => void
  onSearch: () => void
  onPageChange: (page: number) => void
  onAuditPageChange: (page: number) => void
  onAuditRefresh: () => void
  onCreate: () => void
  onEdit: (user: User) => void
  onDelete: (user: User) => void
  onLogout: () => void
}

export function DashboardView({
  me,
  activeSection,
  users,
  auditLogs,
  roles,
  filters,
  loading,
  auditLoading,
  auditRealtimeStatus,
  error,
  auditError,
  page,
  auditPage,
  onSectionChange,
  onFiltersChange,
  onSearch,
  onPageChange,
  onAuditPageChange,
  onAuditRefresh,
  onCreate,
  onEdit,
  onDelete,
  onLogout,
}: Props) {
  const active = users.content.filter(user => user.enabled).length
  const adminCount = users.content.filter(user => user.role === 'ADMIN').length
  const roleOptions = [
    { label: 'Mọi vai trò', value: '' },
    ...roles.map(role => ({ label: roleLabels[role] ?? role, value: role })),
  ]
  const stats: { label: string; value: number; icon: LucideIcon }[] = [
    { label: 'Tổng thành viên', value: users.totalElements, icon: UsersRound },
    { label: 'Đang hoạt động', value: active, icon: CircleUserRound },
    { label: 'Quản trị viên', value: adminCount, icon: ShieldCheck },
  ]

  return <div className="min-h-screen bg-canvas lg:flex">
    <aside className="hidden w-60 shrink-0 bg-brand-black p-4 text-white lg:flex lg:flex-col">
      <div className="flex h-10 items-center gap-3 px-3 font-bold">
        <span className="text-2xl tracking-[-.08em]">HI<span className="text-brand">CAS</span></span>
        <span className="h-6 w-px bg-white/20" />
        <span className="text-xs tracking-[.16em] text-white/55">ONE</span>
      </div>
      <nav className="mt-8 space-y-1 text-sm">
        <span className="mb-2 block px-3 text-[11px] font-bold uppercase tracking-[.14em] text-white/35">Quản trị</span>
        <Button variant="ghost" className="w-full !justify-start !text-white/65 hover:!bg-white/5 hover:!text-white" leadingIcon={<LayoutDashboard size={18} />}>Tổng quan</Button>
        <Button
          variant="ghost"
          className={`relative w-full !justify-start ${activeSection === 'members' ? '!bg-brand/10 !text-brand before:absolute before:bottom-1 before:left-0 before:top-1 before:w-0.5 before:bg-brand' : '!text-white/65 hover:!bg-white/5 hover:!text-white'}`}
          leadingIcon={<UsersRound size={18} />}
          onClick={() => onSectionChange('members')}
        >
          Thành viên
        </Button>
        <Button
          variant="ghost"
          className={`relative w-full !justify-start ${activeSection === 'audit' ? '!bg-brand/10 !text-brand before:absolute before:bottom-1 before:left-0 before:top-1 before:w-0.5 before:bg-brand' : '!text-white/65 hover:!bg-white/5 hover:!text-white'}`}
          leadingIcon={<Activity size={18} />}
          onClick={() => onSectionChange('audit')}
        >
          Hoạt động
        </Button>
        <Button variant="ghost" className="w-full !justify-start !text-white/65 hover:!bg-white/5 hover:!text-white" leadingIcon={<Settings size={18} />}>Cài đặt</Button>
      </nav>
      <div className="mt-auto rounded-xl border border-white/10 bg-white/5 p-4 text-white">
        <ShieldCheck className="text-brand" size={24} />
        <p className="mt-3 text-sm font-semibold">Bảo mật hệ thống</p>
        <p className="mt-1 text-xs leading-5 text-white/45">Phiên đăng nhập được bảo vệ bằng JWT.</p>
      </div>
      <Button variant="ghost" className="mt-3 w-full !justify-start !text-white/65 hover:!bg-white/5 hover:!text-white" leadingIcon={<LogOut size={18} />} onClick={onLogout}>Đăng xuất</Button>
    </aside>

    <main className="min-w-0 flex-1">
      <header className="flex h-14 items-center justify-between bg-brand-black px-5 text-white shadow-sm md:px-6">
        <div>
          <p className="text-xs text-white/45">Quản trị / {activeSection === 'members' ? 'Thành viên' : 'Hoạt động'}</p>
          <h1 className="text-base font-semibold">{activeSection === 'members' ? 'Quản lý thành viên' : 'Quản lý hoạt động'}</h1>
        </div>
        <div className="flex items-center gap-3">
          <div className="hidden text-right sm:block">
            <p className="text-sm font-semibold">{me.username}</p>
            <p className="text-xs text-white/45">{roleLabels[me.role]}</p>
          </div>
          <span className="grid size-9 place-items-center rounded-full bg-brand font-bold text-white">{me.username.slice(0, 2).toUpperCase()}</span>
          <Button className="lg:hidden !text-white" size="sm" variant="ghost" iconOnly leadingIcon={<LogOut size={18} />} aria-label="Đăng xuất" title="Đăng xuất" onClick={onLogout} />
        </div>
      </header>

      <div className="p-5 md:p-6">
        <section className="grid gap-4 sm:grid-cols-3">
          {stats.map(({ label, value, icon: Icon }, index) => (
            <article key={label} className="rounded-xl border border-line bg-white p-5 shadow-sm">
              <div className="flex items-center justify-between">
                <p className="text-sm font-medium text-[#3f3f46]">{label}</p>
                <span className={`grid size-9 place-items-center rounded-lg ${index === 1 ? 'bg-[#ecfdf3] text-success' : 'bg-[#fef3e2] text-brand'}`}>
                  <Icon size={18} />
                </span>
              </div>
              <p className="mt-3 text-[28px] font-bold leading-9 text-ink">{String(value).padStart(2, '0')}</p>
            </article>
          ))}
        </section>

        {activeSection === 'members' ? <section className="mt-6 overflow-hidden rounded-xl border border-line bg-white">
          <div className="flex flex-col gap-4 border-b border-line p-5 xl:flex-row xl:items-end xl:justify-between">
            <div>
              <h2 className="font-bold">Danh sách thành viên</h2>
              <p className="mt-1 text-sm text-muted">Quản lý tài khoản và quyền truy cập theo vai trò dự án.</p>
            </div>
            <div className="grid gap-3 sm:grid-cols-[minmax(220px,1fr)_190px_auto_auto] xl:flex">
              <Input
                className="xl:w-64"
                aria-label="Tìm kiếm"
                placeholder="Tên hoặc email..."
                leadingIcon={<Search size={17} />}
                value={filters.keyword}
                onChange={e => onFiltersChange({ ...filters, keyword: e.target.value })}
                onKeyDown={e => e.key === 'Enter' && onSearch()}
              />
              <Select
                aria-label="Vai trò"
                className="xl:w-48"
                value={filters.role}
                onChange={e => onFiltersChange({ ...filters, role: e.target.value as UserFilters['role'] })}
                options={roleOptions}
              />
              <ToolbarActions loading={loading} createLabel="Thêm thành viên" createIcon={<Plus size={18} />} onFilter={onSearch} onCreate={onCreate} />
            </div>
          </div>

          {error && <p className="m-5 rounded-xl bg-[#fff0ed] p-3 text-sm text-[#ad3e2f]">{error}</p>}

          <div className="overflow-x-auto">
            <table className="w-full min-w-[880px] text-left text-sm">
              <thead className="sticky top-0 z-10 bg-panel text-xs font-semibold uppercase tracking-wider text-[#3f3f46]">
                <tr>
                  <th className="px-5 py-3.5">Thành viên</th>
                  <th className="px-5 py-3.5">Vai trò</th>
                  <th className="px-5 py-3.5">Trạng thái</th>
                  <th className="px-5 py-3.5 text-center">Thao tác</th>
                </tr>
              </thead>
              <tbody className={loading ? 'opacity-45' : ''}>
                {users.content.map(user => (
                  <tr key={user.id} className="border-t border-line hover:bg-canvas">
                    <td className="px-5 py-4">
                      <div className="flex items-center gap-3">
                        <span className="grid size-10 place-items-center rounded-full bg-[#fef3e2] font-bold text-brand">{user.username.slice(0, 2).toUpperCase()}</span>
                        <div>
                          <p className="font-semibold">{user.username}</p>
                          <p className="mt-0.5 text-xs text-muted">{user.email}</p>
                        </div>
                      </div>
                    </td>
                    <td className="px-5 py-4">
                      <span className="rounded-full bg-[#fef3e2] px-2.5 py-1 text-xs font-semibold text-brand-dark">{roleLabels[user.role] ?? user.role}</span>
                    </td>
                    <td className="px-5 py-4">
                      <span className={`inline-flex items-center gap-2 rounded-full px-2.5 py-1 text-xs font-semibold ${user.enabled ? 'bg-[#ecfdf3] text-success' : 'bg-panel text-muted'}`}>
                        <i className={`size-2 rounded-full ${user.enabled ? 'bg-success' : 'bg-muted'}`} />
                        {user.enabled ? 'Hoạt động' : 'Vô hiệu hóa'}
                      </span>
                    </td>
                    <td className="px-5 py-4">
                      <div className="flex justify-center gap-1">
                        <Button variant="ghost" size="sm" iconOnly leadingIcon={<Pencil size={16} />} aria-label={`Sửa ${user.username}`} title="Sửa" onClick={() => onEdit(user)} />
                        <Button variant="ghost" size="sm" iconOnly className="!text-danger hover:!bg-[#fef3f2]" leadingIcon={user.enabled ? <UserX size={16} /> : <Trash2 size={16} />} aria-label={`${user.enabled ? 'Vô hiệu hóa' : 'Xóa'} ${user.username}`} title={user.enabled ? 'Vô hiệu hóa' : 'Xóa'} onClick={() => onDelete(user)} />
                      </div>
                    </td>
                  </tr>
                ))}
                {!users.content.length && !loading && (
                  <tr>
                    <td colSpan={4} className="px-5 py-16 text-center text-muted">Không tìm thấy thành viên phù hợp.</td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>

          <footer className="flex items-center justify-between border-t border-line px-5 py-4 text-sm text-muted">
            <p>Trang {page + 1} / {Math.max(users.totalPages, 1)}</p>
            <div className="flex gap-2">
              <Button variant="secondary" size="sm" disabled={page === 0} leadingIcon={<ChevronLeft size={16} />} onClick={() => onPageChange(page - 1)}>Trước</Button>
              <Button variant="secondary" size="sm" disabled={page + 1 >= users.totalPages} trailingIcon={<ChevronRight size={16} />} onClick={() => onPageChange(page + 1)}>Sau</Button>
            </div>
          </footer>
        </section> : (
          <div className="mt-6">
            <AuditLogView
              logs={auditLogs}
              loading={auditLoading}
              realtimeStatus={auditRealtimeStatus}
              error={auditError}
              page={auditPage}
              onPageChange={onAuditPageChange}
              onRefresh={onAuditRefresh}
            />
          </div>
        )}
      </div>
    </main>
  </div>
}
