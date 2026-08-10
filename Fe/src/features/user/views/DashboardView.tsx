import { useState } from 'react'
import { motion } from 'framer-motion'
import {
  Activity,
  ChevronDown,
  ChevronLeft,
  ChevronRight,
  CircleUserRound,
  LogOut,
  Pencil,
  Plus,
  Search,
  Settings,
  ShieldCheck,
  UserX,
  UsersRound,
  HardDrive,
  LayoutDashboard,
} from 'lucide-react'
import { Button, Input, Select } from '../../../components/ui'
import type { AuditLogPage } from '../models/audit-log.model'
import { roleLabels, type User, type UserFilters, type UserPage, type UserRole } from '../models/user.model'
import { UserGuideModal } from './UserGuideModal'
import { AuditLogView } from './AuditLogView'
import { AdminFileCleanupView } from './AdminFileCleanupView'
import { AdminDashboardOverviewView } from './AdminDashboardOverviewView'
import type { AdminDashboardResponse, AdminSystemStatusResponse } from '../models/admin.model'

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

const mainContentVariants = {
  initial: {},
  animate: {
    transition: {
      delayChildren: 0.4,
      staggerChildren: 0.1,
    },
  },
}

const titleVariants = {
  initial: { y: -15, opacity: 0 },
  animate: {
    y: 0,
    opacity: 1,
    transition: {
      duration: 0.5,
      ease: EXPO_OUT_EASE,
    },
  },
}

const statsContainerVariants = {
  initial: {},
  animate: {
    transition: {
      staggerChildren: 0.1,
    },
  },
}

const statCardVariants = {
  initial: { y: 20, opacity: 0 },
  animate: {
    y: 0,
    opacity: 1,
    transition: {
      duration: 0.8,
      ease: EXPO_OUT_EASE,
    },
  },
}

const filterRowVariants = {
  initial: { y: -15, opacity: 0 },
  animate: {
    y: 0,
    opacity: 1,
    transition: {
      duration: 0.5,
      ease: EXPO_OUT_EASE,
    },
  },
}

const tableContainerVariants = {
  initial: { opacity: 0 },
  animate: {
    opacity: 1,
    transition: {
      duration: 0.5,
      ease: EXPO_OUT_EASE,
      staggerChildren: 0.04,
      delayChildren: 0.1,
    },
  },
}

const tableRowVariants = {
  initial: { y: 10, opacity: 0 },
  animate: {
    y: 0,
    opacity: 1,
    transition: {
      duration: 0.6,
      ease: EXPO_OUT_EASE,
    },
  },
}

type AdminSection = 'overview' | 'members' | 'audit' | 'files'

interface Props {
  me: User
  activeSection: AdminSection
  adminDashboard: AdminDashboardResponse | null
  adminDashboardLoading: boolean
  adminDashboardError: string
  adminSystemStatus: AdminSystemStatusResponse | null
  adminSystemStatusLoading: boolean
  adminSystemStatusError: string
  onRefreshAdminDashboard: () => void
  users: UserPage
  auditLogs: AuditLogPage
  roles: UserRole[]
  filters: UserFilters
  auditSummary?: any | null
  loading: boolean
  auditLoading: boolean
  auditRealtimeStatus: 'connected' | 'disconnected' | 'error'
  error?: string
  auditError: string
  page: number
  auditPage: number
  onSectionChange: (section: AdminSection) => void
  onFiltersChange: (filters: UserFilters) => void
  onSearch: () => void
  onPageChange: (page: number) => void
  onAuditPageChange: (page: number) => void
  onAuditRefresh: () => void
  onAuditFilterChange?: (filters: { action?: string; keyword?: string }) => void
  onCreate: () => void
  onEdit: (user: User) => void
  onDelete: (user: User) => void
  onToggleEnable?: (user: User) => void
  onUpdateRole?: (user: User, role: UserRole) => void
  onViewActivities?: (user: User) => void
  onLogout: () => void
  onOpenSettings: () => void
}

export function DashboardView({
  me,
  activeSection,
  adminDashboard,
  adminDashboardLoading,
  adminDashboardError,
  adminSystemStatus,
  adminSystemStatusLoading,
  adminSystemStatusError,
  onRefreshAdminDashboard,
  users,
  auditLogs,
  auditSummary,
  roles,
  filters,
  loading,
  auditLoading,
  auditRealtimeStatus,
  auditError,
  page,
  auditPage,
  onSectionChange,
  onFiltersChange,
  onSearch,
  onPageChange,
  onAuditPageChange,
  onAuditRefresh,
  onAuditFilterChange,
  onCreate,
  onEdit,
  onDelete,
  onToggleEnable,
  onUpdateRole,
  onViewActivities,
  onLogout,
  onOpenSettings,
}: Props) {
  const [guideModalOpen, setGuideModalOpen] = useState(false)
  const active = users.content.filter(user => user.enabled).length
  const adminCount = users.content.filter(user => user.role === 'ADMIN').length
  const roleOptions = [
    { label: 'Mọi vai trò', value: '' },
    ...roles.map(role => ({ label: roleLabels[role] ?? role, value: role })),
  ]
  const stats = [
    { 
      label: 'Tổng thành viên', 
      value: users.totalElements, 
      icon: UsersRound,
      badgeText: 'TỔNG THÀNH VIÊN',
      badgeClass: 'bg-amber-50 text-amber-700 border-amber-200/80',
      iconBoxClass: 'bg-amber-100/70 text-amber-700'
    },
    { 
      label: 'Đang hoạt động', 
      value: active, 
      icon: CircleUserRound,
      badgeText: 'ĐANG HOẠT ĐỘNG',
      badgeClass: 'bg-emerald-50 text-emerald-700 border-emerald-200/80',
      iconBoxClass: 'bg-emerald-100/70 text-emerald-700'
    },
    { 
      label: 'Quản trị viên', 
      value: adminCount, 
      icon: ShieldCheck,
      badgeText: 'QUẢN TRỊ VIÊN',
      badgeClass: 'bg-blue-50 text-blue-700 border-blue-200/80',
      iconBoxClass: 'bg-blue-100/70 text-blue-700'
    },
  ]

  return <div className="min-h-screen bg-canvas lg:flex">
    <motion.aside
      variants={sidebarVariants}
      initial="initial"
      animate="animate"
      style={{ willChange: 'transform, opacity', transform: 'translateZ(0)' }}
      className="hidden w-64 border-r border-line bg-brand-black p-5 text-white lg:flex lg:flex-col lg:justify-between shrink-0"
    >
      <div className="flex h-10 items-center gap-3 px-3 font-bold">
        <span className="text-2xl tracking-[-.08em]">HI<span className="text-brand">CAS</span></span>
        <span className="h-6 w-px bg-white/20" />
        <span className="text-xs font-bold tracking-[.16em] text-white/55">ONE</span>
      </div>
      <nav className="mt-8 space-y-1 text-sm">
        <span className="mb-2 block px-3 text-[11px] font-bold uppercase tracking-[.14em] text-white/35">Quản trị</span>
        <Button
          variant="ghost"
          className={`relative w-full !justify-start ${activeSection === 'overview' ? '!bg-brand/10 !text-brand before:absolute before:bottom-1 before:left-0 before:top-1 before:w-0.5 before:bg-brand font-bold' : '!text-white/65 hover:!bg-white/5 hover:!text-white'}`}
          leadingIcon={<LayoutDashboard size={18} />}
          onClick={() => onSectionChange('overview')}
        >
          Tổng quan
        </Button>
        <Button
          variant="ghost"
          className={`relative w-full !justify-start ${activeSection === 'members' ? '!bg-brand/10 !text-brand before:absolute before:bottom-1 before:left-0 before:top-1 before:w-0.5 before:bg-brand font-bold' : '!text-white/65 hover:!bg-white/5 hover:!text-white'}`}
          leadingIcon={<UsersRound size={18} />}
          onClick={() => onSectionChange('members')}
        >
          Thành viên
        </Button>
        <Button
          variant="ghost"
          className={`relative w-full !justify-start ${activeSection === 'audit' ? '!bg-brand/10 !text-brand before:absolute before:bottom-1 before:left-0 before:top-1 before:w-0.5 before:bg-brand font-bold' : '!text-white/65 hover:!bg-white/5 hover:!text-white'}`}
          leadingIcon={<Activity size={18} />}
          onClick={() => onSectionChange('audit')}
        >
          Hoạt động
        </Button>
        <Button
          variant="ghost"
          className={`relative w-full !justify-start ${activeSection === 'files' ? '!bg-brand/10 !text-brand before:absolute before:bottom-1 before:left-0 before:top-1 before:w-0.5 before:bg-brand font-bold' : '!text-white/65 hover:!bg-white/5 hover:!text-white'}`}
          leadingIcon={<HardDrive size={18} />}
          onClick={() => onSectionChange('files')}
        >
          Quản lý File
        </Button>
        <Button variant="ghost" className="w-full !justify-start !text-white/65 hover:!bg-white/5 hover:!text-white" leadingIcon={<Settings size={18} />}>Cài đặt</Button>
      </nav>
      <div className="mt-auto rounded-2xl border border-white/10 bg-white/5 p-4 text-white">
        <ShieldCheck className="text-brand" size={24} />
        <p className="mt-3 text-sm font-bold">Bảo mật hệ thống</p>
        <p className="mt-1 text-xs leading-5 text-white/45 font-medium">Phiên đăng nhập được bảo vệ bằng JWT.</p>
      </div>
    </motion.aside>

    <main className="min-w-0 flex-1">
      <motion.header
        variants={headerVariants}
        initial="initial"
        animate="animate"
        style={{ willChange: 'transform, opacity', transform: 'translateZ(0)' }}
        className="flex h-14 items-center justify-between bg-brand-black px-5 text-white shadow-sm md:px-6 sticky top-0 z-30"
      >
        <div className="flex items-center gap-3 font-bold">
          <span className="text-2xl tracking-[-.08em] lg:hidden">HI<span className="text-brand">CAS</span></span>
          <span className="h-6 w-px bg-white/20 lg:hidden" />
          <motion.div variants={titleVariants}>
            <p className="text-[11px] text-white/45 font-medium">Quản trị / {activeSection === 'overview' ? 'Tổng quan' : activeSection === 'members' ? 'Thành viên' : activeSection === 'audit' ? 'Hoạt động' : 'Quản lý File'}</p>
            <h1 className="text-sm font-bold">{activeSection === 'overview' ? 'Admin Dashboard Tổng quan' : activeSection === 'members' ? 'Quản lý thành viên' : activeSection === 'audit' ? 'Quản lý hoạt động' : 'Quản lý File & Dọn dẹp'}</h1>
          </motion.div>
        </div>

        <div className="flex items-center gap-3">
          <button type="button" className="flex items-center gap-2 rounded-full p-1 hover:bg-white/10 transition cursor-pointer" onClick={onOpenSettings}>
            <div className="flex h-8 w-8 items-center justify-center rounded-full bg-brand font-extrabold text-white uppercase text-xs">
              {me.username.charAt(0)}
            </div>
            <div className="hidden text-left sm:block pr-1">
              <p className="text-xs font-bold text-white leading-tight">{me.username}</p>
              <p className="text-[10px] text-white/45 leading-tight">{roleLabels[me.role]}</p>
            </div>
          </button>
          <div className="h-6 w-px bg-white/20 mx-1 hidden lg:block" />
          <Button className="!text-white/80 hover:!text-white hover:bg-white/10" variant="ghost" size="sm" leadingIcon={<LogOut size={17} />} onClick={onLogout}>
            Đăng xuất
          </Button>
        </div>
      </motion.header>

      <motion.div
        variants={mainContentVariants}
        initial="initial"
        animate="animate"
        className="p-5 md:p-6 space-y-6"
      >
        {activeSection === 'overview' && (
          <AdminDashboardOverviewView
            dashboard={adminDashboard}
            loading={adminDashboardLoading}
            error={adminDashboardError}
            systemStatus={adminSystemStatus}
            systemStatusLoading={adminSystemStatusLoading}
            systemStatusError={adminSystemStatusError}
            onRefresh={onRefreshAdminDashboard}
            onNavigateSection={section => onSectionChange(section)}
          />
        )}

        {activeSection !== 'overview' && (
          <motion.section variants={statsContainerVariants} className="grid gap-5 sm:grid-cols-3">
            {stats.map(({ value, icon: Icon, badgeText, iconBoxClass }) => (
              <motion.article key={badgeText} variants={statCardVariants} style={{ willChange: 'transform, opacity' }} className="rounded-2xl border border-line/70 bg-white p-5 shadow-xs transition hover:shadow-md hover:-translate-y-0.5 flex items-center gap-4">
                <span className={`grid size-14 place-items-center rounded-2xl shrink-0 ${iconBoxClass}`}>
                  <Icon size={24} />
                </span>
                <div>
                  <p className="text-[11px] font-bold text-muted uppercase tracking-wider">{badgeText}</p>
                  <p className="text-3xl font-black text-ink mt-0.5">{String(value).padStart(2, '0')}</p>
                </div>
              </motion.article>
            ))}
          </motion.section>
        )}

        <div>
          {activeSection === 'members' && (
            <motion.section variants={tableContainerVariants} initial="initial" animate="animate" className="overflow-hidden rounded-2xl border border-line/70 bg-white shadow-xs">
            <motion.div variants={filterRowVariants} className="flex flex-col gap-4 border-b border-line/60 p-5 xl:flex-row xl:items-center xl:justify-between">
              <div>
                <h2 className="font-extrabold text-base text-ink">Danh sách thành viên</h2>
                <p className="mt-0.5 text-xs text-muted font-medium">Quản lý tài khoản và phân quyền quản trị hệ thống.</p>
              </div>
              <div className="flex flex-col sm:flex-row items-stretch sm:items-center gap-3">
                <Input
                  className="sm:w-64"
                  aria-label="Tìm kiếm"
                  placeholder="Tên hoặc email..."
                  leadingIcon={<Search size={17} />}
                  value={filters.keyword}
                  onChange={e => onFiltersChange({ ...filters, keyword: e.target.value })}
                  onKeyDown={e => e.key === 'Enter' && onSearch()}
                />
                <Select
                  aria-label="Vai trò"
                  className="sm:w-44"
                  value={filters.role}
                  onChange={e => {
                    const newRole = e.target.value as UserFilters['role']
                    onFiltersChange({ ...filters, role: newRole })
                  }}
                  options={roleOptions}
                />
                <Button
                  onClick={onCreate}
                  leadingIcon={<Plus size={18} />}
                  className="!px-4 text-xs font-extrabold !bg-brand hover:!bg-brand-dark !text-white shrink-0"
                >
                  Thêm thành viên
                </Button>
              </div>
            </motion.div>

            <div className="overflow-x-auto">
              <table className="w-full min-w-[880px] text-left text-sm">
                <thead className="sticky top-0 z-10 bg-panel text-xs font-extrabold uppercase tracking-wider text-muted-dark border-b border-line/60">
                  <tr>
                    <th className="px-5 py-4">THÀNH VIÊN</th>
                    <th className="px-5 py-4">VAI TRÒ</th>
                    <th className="px-5 py-4">TRẠNG THÁI</th>
                    <th className="px-5 py-4 text-center">THAO TÁC</th>
                  </tr>
                </thead>
                <tbody className={loading ? 'opacity-40 transition-opacity' : ''}>
                  {users.content.map(user => (
                    <motion.tr key={user.id} variants={tableRowVariants} style={{ willChange: 'transform, opacity' }} className="relative border-t border-line/50 hover:bg-canvas/80 transition-colors hover:z-20">
                      <td className="px-5 py-4">
                        <div className="flex items-center gap-3.5">
                          <span className="grid size-11 place-items-center rounded-full bg-brand-soft text-brand-dark font-black text-sm border border-brand-line/50 shrink-0 shadow-2xs">
                            {user.username.slice(0, 2).toUpperCase()}
                          </span>
                          <div>
                            <p className="font-bold text-ink text-sm leading-snug">{user.username}</p>
                            <p className="mt-0.5 text-xs text-muted font-medium">{user.email}</p>
                          </div>
                        </div>
                      </td>
                      <td className="px-5 py-4">
                        <div className="relative inline-flex items-center">
                          <select
                            className={`appearance-none rounded-full px-3.5 py-1 pr-7 text-xs font-extrabold cursor-pointer border outline-none transition-all shadow-2xs ${
                              user.role === 'ADMIN'
                                ? 'bg-brand/10 text-brand-dark border-brand/30 hover:bg-brand/20 focus:ring-2 focus:ring-brand/30'
                                : user.role === 'MANAGER'
                                  ? 'bg-info/10 text-info border-info/30 hover:bg-info/20 focus:ring-2 focus:ring-info/30'
                                  : 'bg-success/10 text-success border-success/30 hover:bg-success/20 focus:ring-2 focus:ring-success/30'
                            }`}
                            value={user.role}
                            onChange={e => onUpdateRole?.(user, e.target.value as UserRole)}
                          >
                            {roles.map(r => (
                              <option key={r} value={r} className="bg-white text-ink font-bold py-1">
                                {roleLabels[r] ?? r}
                              </option>
                            ))}
                          </select>
                          <ChevronDown size={13} className="pointer-events-none absolute right-2.5 text-current opacity-70" />
                        </div>
                      </td>
                      <td className="px-5 py-4">
                        <span className={`inline-flex items-center gap-2 rounded-full px-3 py-1 text-xs font-extrabold border ${
                          user.enabled ? 'bg-success/10 text-success border-success/20' : 'bg-panel text-muted border-line'
                        }`}>
                          <i className={`size-2 rounded-full ${user.enabled ? 'bg-success' : 'bg-muted'}`} />
                          {user.enabled ? 'Hoạt động' : 'Vô hiệu hóa'}
                        </span>
                      </td>
                      <td className="px-5 py-4">
                        <div className="flex justify-center items-center gap-2">
                          <Button
                            variant="ghost"
                            size="sm"
                            iconOnly
                            className="!size-9 !rounded-xl border border-info/30 bg-info/10 !text-info hover:!bg-info/20 hover:!border-info/40 active:scale-95 transition-all shadow-2xs"
                            leadingIcon={<Activity size={16} />}
                            aria-label={`Xem nhật ký ${user.username}`}
                            title="Xem nhật ký hoạt động"
                            onClick={() => onViewActivities?.(user)}
                          />
                          <Button
                            variant="ghost"
                            size="sm"
                            iconOnly
                            className="!size-9 !rounded-xl border border-brand/30 bg-brand/10 !text-brand-dark hover:!bg-brand/20 hover:!border-brand/40 active:scale-95 transition-all shadow-2xs"
                            leadingIcon={<Pencil size={16} />}
                            aria-label={`Sửa ${user.username}`}
                            title="Sửa thông tin / vai trò"
                            onClick={() => onEdit(user)}
                          />
                          <Button
                            variant="ghost"
                            size="sm"
                            iconOnly
                            className={`!size-9 !rounded-xl border active:scale-95 transition-all shadow-2xs ${
                              user.enabled
                                ? 'border-danger/30 bg-danger/10 !text-danger hover:!bg-danger/20 hover:!border-danger/40'
                                : 'border-success/30 bg-success/10 !text-success hover:!bg-success/20 hover:!border-success/40'
                            }`}
                            leadingIcon={user.enabled ? <UserX size={16} /> : <ShieldCheck size={16} />}
                            aria-label={`${user.enabled ? 'Vô hiệu hóa' : 'Kích hoạt'} ${user.username}`}
                            title={user.enabled ? 'Vô hiệu hóa tài khoản' : 'Kích hoạt tài khoản'}
                            onClick={() => onToggleEnable ? onToggleEnable(user) : onDelete(user)}
                          />
                        </div>
                      </td>
                    </motion.tr>
                  ))}
                  {!users.content.length && !loading && (
                    <tr>
                      <td colSpan={4} className="px-5 py-16 text-center text-muted font-medium">Không tìm thấy thành viên phù hợp.</td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>

            <footer className="flex items-center justify-between border-t border-line/60 px-5 py-3.5 text-xs text-muted font-medium">
              <p>Trang <span className="font-bold text-ink">{page + 1}</span> / {Math.max(users.totalPages, 1)}</p>
              <div className="flex gap-2">
                <Button variant="secondary" size="sm" disabled={page === 0} leadingIcon={<ChevronLeft size={15} />} onClick={() => onPageChange(page - 1)}>Trước</Button>
                <Button variant="secondary" size="sm" disabled={page + 1 >= users.totalPages} trailingIcon={<ChevronRight size={15} />} onClick={() => onPageChange(page + 1)}>Sau</Button>
              </div>
            </footer>
            </motion.section>
          )}

        {activeSection === 'audit' && (
          <div className="mt-6">
            <AuditLogView
              logs={auditLogs}
              summary={auditSummary}
              loading={auditLoading}
              realtimeStatus={auditRealtimeStatus}
              error={auditError}
              page={auditPage}
              usersMap={users.content.reduce<Record<string, { username: string; email?: string }>>((acc, u) => {
                acc[u.id] = { username: u.username, email: u.email }
                return acc
              }, {})}
              onPageChange={onAuditPageChange}
              onRefresh={onAuditRefresh}
              onFilterChange={onAuditFilterChange}
            />
          </div>
        )}

        {activeSection === 'files' && (
          <div className="mt-6">
            <AdminFileCleanupView />
          </div>
        )}
        </div>
      </motion.div>
    </main>

    <UserGuideModal
      open={guideModalOpen}
      onClose={() => setGuideModalOpen(false)}
    />
  </div>
}
