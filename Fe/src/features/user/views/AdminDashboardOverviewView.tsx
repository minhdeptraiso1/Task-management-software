import { useState } from 'react'
import { motion } from 'framer-motion'
import {
  Users,
  FolderKanban,
  Zap,
  CheckSquare,
  Bug,
  HardDrive,
  AlertTriangle,
  RefreshCcw,
  Activity,
  UserCheck,
  Clock,
  Sparkles,
  Database,
  Server,
  Layers,
  FolderArchive
} from 'lucide-react'
import { Button, toast } from '../../../components/ui'
import type { AdminDashboardResponse, AdminSystemStatusResponse } from '../models/admin.model'
import { runTaskDueReminders, runDailyDigest } from '../services/admin.service'

const EXPO_OUT_EASE = [0.16, 1, 0.3, 1] as const

const containerVariants = {
  initial: {},
  animate: {
    transition: {
      staggerChildren: 0.08,
      delayChildren: 0.1
    }
  }
}

const itemVariants = {
  initial: { y: 20, opacity: 0 },
  animate: {
    y: 0,
    opacity: 1,
    transition: {
      duration: 0.6,
      ease: EXPO_OUT_EASE
    }
  }
}

function formatBytes(bytes: number, decimals = 2): string {
  if (!bytes || bytes === 0) return '0 B'
  const k = 1024
  const dm = decimals < 0 ? 0 : decimals
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return `${parseFloat((bytes / Math.pow(k, i)).toFixed(dm))} ${sizes[i]}`
}

export function AdminDashboardOverviewView({
  dashboard,
  loading,
  error,
  systemStatus,
  systemStatusLoading,
  systemStatusError,
  onRefresh,
  onNavigateSection
}: {
  dashboard: AdminDashboardResponse | null
  loading: boolean
  error: string
  systemStatus: AdminSystemStatusResponse | null
  systemStatusLoading: boolean
  systemStatusError: string
  onRefresh: () => void
  onNavigateSection?: (section: 'members' | 'audit' | 'files') => void
}) {
  const [reminderRunning, setReminderRunning] = useState(false)
  const [digestRunning, setDigestRunning] = useState(false)

  const handleRunReminders = async () => {
    setReminderRunning(true)
    try {
      await runTaskDueReminders()
      toast.success('Đã kích hoạt quét & gửi thông báo nhắc hẹn Task (Sắp hết hạn / Quá hạn) thành công!')
    } catch (err) {
      toast.error(err instanceof Error ? err.message : 'Chạy nhắc hẹn Task thất bại')
    } finally {
      setReminderRunning(false)
    }
  }

  const handleRunDailyDigest = async () => {
    setDigestRunning(true)
    try {
      const res = await runDailyDigest()
      toast.success(`Đã phát Daily Digest thành công cho ${res.sentDigests}/${res.scannedUsers} người dùng (Bỏ qua ${res.skippedUsers})!`)
    } catch (err) {
      toast.error(err instanceof Error ? err.message : 'Gửi Daily Digest thất bại')
    } finally {
      setDigestRunning(false)
    }
  }
  if (loading && !dashboard) {
    return (
      <div className="flex h-96 w-full items-center justify-center p-8">
        <div className="text-center">
          <span className="mx-auto block size-10 animate-spin rounded-full border-4 border-brand border-r-transparent" />
          <p className="mt-4 text-sm font-medium text-slate-500">Đang tải Admin Dashboard...</p>
        </div>
      </div>
    )
  }

  if (error && !dashboard) {
    return (
      <div className="p-8 text-center">
        <div className="mx-auto flex size-14 items-center justify-center rounded-2xl bg-rose-50 text-rose-600">
          <AlertTriangle size={28} />
        </div>
        <h3 className="mt-4 text-base font-bold text-slate-900">Không thể tải Dashboard</h3>
        <p className="mt-1 text-sm text-slate-500">{error}</p>
        <Button className="mt-6" leadingIcon={<RefreshCcw size={16} />} onClick={onRefresh}>
          Thử lại
        </Button>
      </div>
    )
  }

  if (!dashboard) return null

  const { userSummary, projectSummary, sprintSummary, taskSummary, bugSummary, attachmentSummary, systemSummary } = dashboard

  // Calculate percentages safely
  const activeUserPercent = userSummary.totalUsers > 0 ? Math.round((userSummary.activeUsers / userSummary.totalUsers) * 100) : 0
  const activeProjectPercent = projectSummary.totalProjects > 0 ? Math.round((projectSummary.activeProjects / projectSummary.totalProjects) * 100) : 0
  const taskDonePercent = taskSummary.totalTasks > 0 ? Math.round((taskSummary.doneTasks / taskSummary.totalTasks) * 100) : 0
  const healthUp = systemStatus?.health.status === 'UP'
  const healthComponents = systemStatus?.health.components
  const statusItems = [
    ['Cơ sở dữ liệu', healthComponents?.db?.status],
    ['Redis', healthComponents?.redis?.status],
    ['Dung lượng đĩa', healthComponents?.diskSpace?.status],
    ['Kho lưu trữ file', healthComponents?.fileStorage?.status],
  ] as const

  return (
    <motion.div
      variants={containerVariants}
      initial="initial"
      animate="animate"
      className="space-y-6 p-6"
    >
      {/* System Status Header Banner */}
      <motion.div variants={itemVariants} className="flex flex-wrap items-center justify-between gap-4 rounded-2xl border border-line/70 bg-white p-5 shadow-xs transition-all duration-200 hover:shadow-md">
        <div className="flex items-center gap-3.5">
          <div className="flex size-11 items-center justify-center rounded-xl bg-brand/10 text-brand border border-brand/20 shrink-0">
            <Server size={22} />
          </div>
          <div className="min-w-0">
            <div className="flex flex-wrap items-center gap-2">
              <h2 className="text-sm font-extrabold text-ink">
                {systemStatus?.info.app?.name ?? 'Task Management Agile Scrum Backend'}
              </h2>
              {systemStatus?.info.app?.version && (
                <span className="rounded-full bg-brand/10 border border-brand/20 px-2.5 py-0.5 text-[10px] font-extrabold text-brand-dark">
                  v{systemStatus.info.app.version}
                </span>
              )}
            </div>
            <p className="truncate text-xs font-medium text-muted mt-0.5">
              {systemStatusError
                ? systemStatusError
                : `${systemStatus?.info.app?.description ?? 'Backend Spring Boot cho hệ thống quản lý công việc IT theo Agile/Scrum'} • Cập nhật lúc ${new Date(systemSummary.generatedAt).toLocaleString('vi-VN')}`}
            </p>
          </div>
        </div>
        <div className="flex flex-wrap items-center gap-2.5">
          <Button
            variant="secondary"
            size="sm"
            className="!border-indigo-200 !bg-indigo-50/80 !text-indigo-700 hover:!bg-indigo-100 font-bold"
            leadingIcon={<Sparkles size={14} className={digestRunning ? 'animate-spin' : ''} />}
            loading={digestRunning}
            onClick={handleRunDailyDigest}
            title="Gửi báo cáo tổng hợp công việc hằng ngày (Daily Digest) cho người dùng"
          >
            Gửi Daily Digest
          </Button>
          <Button
            variant="secondary"
            size="sm"
            className="!border-amber-200 !bg-amber-50/80 !text-amber-800 hover:!bg-amber-100 font-bold"
            leadingIcon={<Clock size={14} className={reminderRunning ? 'animate-spin' : ''} />}
            loading={reminderRunning}
            onClick={handleRunReminders}
            title="Kích hoạt quét Task Sắp đến hạn / Quá hạn và gửi thông báo Realtime"
          >
            Quét nhắc hẹn Task
          </Button>
          <Button variant="secondary" size="sm" leadingIcon={<RefreshCcw size={14} className={loading ? 'animate-spin' : ''} />} onClick={onRefresh}>
            Làm mới
          </Button>
        </div>
      </motion.div>

      {/* Backend API & Infrastructure Health Monitoring Panel */}
      <motion.div variants={itemVariants} className="rounded-2xl border border-line/70 bg-canvas/60 p-5 shadow-xs space-y-3.5">
        <div className="flex items-center justify-between border-b border-line/50 pb-2.5">
          <div className="flex items-center gap-2">
            <Activity size={16} className="text-brand" />
            <h3 className="text-xs font-extrabold text-ink uppercase tracking-wider">Trạng Thái API & Hạ Tầng Backend (Health Monitoring)</h3>
          </div>
          {systemStatus?.checkedAt && (
            <span className="text-[11px] font-medium text-muted">
              Kiểm tra gần nhất: {new Date(systemStatus.checkedAt).toLocaleString('vi-VN')}
            </span>
          )}
        </div>

        <div className="grid gap-3 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-5">
          {/* Card 1: API Backend Status (Hệ thống ổn định) */}
          <div className="flex items-center justify-between rounded-xl border border-line/70 bg-white p-3.5 shadow-2xs hover:border-brand/30 hover:shadow-xs transition-all duration-200">
            <div className="flex items-center gap-2.5">
              <div className={`size-8 rounded-lg flex items-center justify-center shrink-0 ${healthUp ? 'bg-success/10 text-success border border-success/20' : 'bg-danger/10 text-danger border border-danger/20'}`}>
                <Activity size={16} />
              </div>
              <span className="text-xs font-bold text-ink">API Backend</span>
            </div>
            <span className={`inline-flex items-center gap-1.5 rounded-full px-2.5 py-0.5 text-[11px] font-extrabold border ${
              systemStatusLoading
                ? 'border-amber-200 bg-amber-50 text-amber-700'
                : healthUp
                  ? 'border-success/30 bg-success/10 text-success'
                  : 'border-danger/30 bg-danger/10 text-danger'
            }`}>
              <span className={`size-2 rounded-full ${systemStatusLoading ? 'animate-pulse bg-amber-500' : healthUp ? 'animate-pulse bg-success' : 'bg-danger'}`} />
              {systemStatusLoading ? 'Đang kiểm tra' : healthUp ? 'Hệ thống ổn định' : 'Cần kiểm tra'}
            </span>
          </div>

          {/* Infrastructure Component Cards */}
          {statusItems.map(([label, status]) => {
            const isUp = status === 'UP'
            const getIcon = () => {
              if (label.includes('sở dữ liệu')) return <Database size={16} />
              if (label.includes('Redis')) return <Layers size={16} />
              if (label.includes('đĩa')) return <HardDrive size={16} />
              return <FolderArchive size={16} />
            }

            return (
              <div key={label} className="flex items-center justify-between rounded-xl border border-line/70 bg-white p-3.5 shadow-2xs hover:border-brand/30 hover:shadow-xs transition-all duration-200">
                <div className="flex items-center gap-2.5">
                  <div className={`size-8 rounded-lg flex items-center justify-center shrink-0 ${isUp ? 'bg-success/10 text-success border border-success/20' : 'bg-danger/10 text-danger border border-danger/20'}`}>
                    {getIcon()}
                  </div>
                  <span className="text-xs font-bold text-ink">{label}</span>
                </div>
                <span className={`inline-flex items-center gap-1.5 rounded-full px-2.5 py-0.5 text-[11px] font-extrabold border ${
                  isUp
                    ? 'border-success/30 bg-success/10 text-success'
                    : status
                      ? 'border-danger/30 bg-danger/10 text-danger'
                      : 'border-line bg-panel text-muted'
                }`}>
                  <span className={`size-2 rounded-full ${isUp ? 'animate-pulse bg-success' : status ? 'bg-danger' : 'bg-muted'}`} />
                  {status ?? (systemStatus ? 'Đã ẩn' : 'N/A')}
                </span>
              </div>
            )
          })}
        </div>
      </motion.div>

      {/* Top 6 KPI Summary Cards */}
      <motion.div variants={containerVariants} className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-6">
        {/* User Card */}
        <motion.div
          variants={itemVariants}
          className="group relative overflow-hidden rounded-2xl border border-line/70 bg-white p-5 shadow-xs hover:shadow-md hover:-translate-y-0.5 transition-all cursor-pointer flex flex-col justify-between"
          onClick={() => onNavigateSection?.('members')}
        >
          <div className="flex items-center justify-between">
            <div className="grid size-11 place-items-center rounded-xl bg-info/10 text-info border border-info/20">
              <Users size={22} />
            </div>
            <span className="text-[10px] font-extrabold uppercase tracking-wider text-muted bg-panel px-2.5 py-0.5 rounded-full border border-line">NGƯỜI DÙNG</span>
          </div>
          <div className="mt-4">
            <p className="text-xs font-medium text-muted">Tổng số người dùng</p>
            <p className="text-2xl font-bold text-ink mt-0.5">{userSummary.totalUsers}</p>
          </div>
          <div className="mt-3 flex items-center justify-between text-xs text-muted font-medium pt-2 border-t border-line/50">
            <span className="flex items-center gap-1 text-success font-bold">
              <UserCheck size={12} /> {userSummary.activeUsers} hoạt động
            </span>
            <span>{userSummary.disabledUsers} khóa</span>
          </div>
        </motion.div>

        {/* Project Card */}
        <motion.div variants={itemVariants} className="group relative overflow-hidden rounded-2xl border border-line/70 bg-white p-5 shadow-xs hover:shadow-md hover:-translate-y-0.5 transition-all flex flex-col justify-between">
          <div className="flex items-center justify-between">
            <div className="grid size-11 place-items-center rounded-xl bg-brand/10 text-brand border border-brand/20">
              <FolderKanban size={22} />
            </div>
            <span className="text-[10px] font-extrabold uppercase tracking-wider text-brand-dark bg-brand/10 px-2.5 py-0.5 rounded-full border border-brand/20">DỰ ÁN</span>
          </div>
          <div className="mt-4">
            <p className="text-xs font-medium text-muted">Tổng số dự án</p>
            <p className="text-2xl font-bold text-ink mt-0.5">{projectSummary.totalProjects}</p>
          </div>
          <div className="mt-3 flex items-center justify-between text-xs text-muted font-medium pt-2 border-t border-line/50">
            <span className="font-bold text-brand-dark">{projectSummary.activeProjects} đang chạy</span>
            <span>{projectSummary.completedProjects} hoàn thành</span>
          </div>
        </motion.div>

        {/* Sprint Card */}
        <motion.div variants={itemVariants} className="group relative overflow-hidden rounded-2xl border border-line/70 bg-white p-5 shadow-xs hover:shadow-md hover:-translate-y-0.5 transition-all flex flex-col justify-between">
          <div className="flex items-center justify-between">
            <div className="grid size-11 place-items-center rounded-xl bg-info/10 text-info border border-info/20">
              <Zap size={22} />
            </div>
            <span className="text-[10px] font-extrabold uppercase tracking-wider text-info bg-info/10 px-2.5 py-0.5 rounded-full border border-info/20">SPRINT</span>
          </div>
          <div className="mt-4">
            <p className="text-xs font-medium text-muted">Tổng số Sprint</p>
            <p className="text-2xl font-bold text-ink mt-0.5">{sprintSummary.totalSprints}</p>
          </div>
          <div className="mt-3 flex items-center justify-between text-xs text-muted font-medium pt-2 border-t border-line/50">
            <span className="font-bold text-info">{sprintSummary.activeSprints} đang mở</span>
            <span>{sprintSummary.planningSprints} kế hoạch</span>
          </div>
        </motion.div>

        {/* Task Card */}
        <motion.div variants={itemVariants} className="group relative overflow-hidden rounded-2xl border border-line/70 bg-white p-5 shadow-xs hover:shadow-md hover:-translate-y-0.5 transition-all flex flex-col justify-between">
          <div className="flex items-center justify-between">
            <div className="grid size-11 place-items-center rounded-xl bg-success/10 text-success border border-success/20">
              <CheckSquare size={22} />
            </div>
            <span className="text-[10px] font-extrabold uppercase tracking-wider text-success bg-success/10 px-2.5 py-0.5 rounded-full border border-success/20">CÔNG VIỆC</span>
          </div>
          <div className="mt-4">
            <p className="text-xs font-medium text-muted">Tổng số công việc</p>
            <p className="text-2xl font-bold text-ink mt-0.5">{taskSummary.totalTasks}</p>
          </div>
          <div className="mt-3 flex items-center justify-between text-xs text-muted font-medium pt-2 border-t border-line/50">
            <span className="font-bold text-success">{taskSummary.doneTasks} hoàn thành</span>
            {taskSummary.overdueTasks > 0 && (
              <span className="font-bold text-danger flex items-center gap-0.5">
                <AlertTriangle size={11} /> {taskSummary.overdueTasks} quá hạn
              </span>
            )}
          </div>
        </motion.div>

        {/* Bug Card */}
        <motion.div variants={itemVariants} className="group relative overflow-hidden rounded-2xl border border-line/70 bg-white p-5 shadow-xs hover:shadow-md hover:-translate-y-0.5 transition-all flex flex-col justify-between">
          <div className="flex items-center justify-between">
            <div className="grid size-11 place-items-center rounded-xl bg-danger/10 text-danger border border-danger/20">
              <Bug size={22} />
            </div>
            <span className="text-[10px] font-extrabold uppercase tracking-wider text-danger bg-danger/10 px-2.5 py-0.5 rounded-full border border-danger/20">LỖI (BUGS)</span>
          </div>
          <div className="mt-4">
            <p className="text-xs font-medium text-muted">Tổng số lỗi</p>
            <p className="text-2xl font-bold text-ink mt-0.5">{bugSummary.totalBugs}</p>
          </div>
          <div className="mt-3 flex items-center justify-between text-xs text-muted font-medium pt-2 border-t border-line/50">
            <span className="font-bold text-danger">{bugSummary.openBugs + bugSummary.inProgressBugs} cần xử lý</span>
            {bugSummary.criticalBugs > 0 && (
              <span className="font-bold text-danger bg-danger/10 px-1.5 py-0.5 rounded text-[10px] border border-danger/20">
                {bugSummary.criticalBugs} nghiêm trọng
              </span>
            )}
          </div>
        </motion.div>

        {/* Storage Card */}
        <motion.div
          variants={itemVariants}
          className="group relative overflow-hidden rounded-2xl border border-line/70 bg-white p-5 shadow-xs hover:shadow-md hover:-translate-y-0.5 transition-all cursor-pointer flex flex-col justify-between"
          onClick={() => onNavigateSection?.('files')}
        >
          <div className="flex items-center justify-between">
            <div className="grid size-11 place-items-center rounded-xl bg-info/10 text-info border border-info/20">
              <HardDrive size={22} />
            </div>
            <span className="text-[10px] font-extrabold uppercase tracking-wider text-info bg-info/10 px-2.5 py-0.5 rounded-full border border-info/20">LƯU TRỮ</span>
          </div>
          <div className="mt-4">
            <p className="text-xs font-medium text-muted">Dung lượng File</p>
            <p className="text-2xl font-bold text-ink mt-0.5 truncate">{formatBytes(attachmentSummary.totalSizeBytes)}</p>
          </div>
          <div className="mt-3 flex items-center justify-between text-xs text-muted font-medium pt-2 border-t border-line/50">
            <span className="font-bold text-info">{attachmentSummary.totalAttachments} file đính kèm</span>
          </div>
        </motion.div>
      </motion.div>

      {/* Detailed Breakdown Grid */}
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        {/* User Roles & Status Breakdown */}
        <motion.div variants={itemVariants} className="rounded-2xl border border-slate-200 bg-white p-5 shadow-2xs">
          <div className="flex items-center justify-between border-b border-slate-100 pb-4">
            <div className="flex items-center gap-2">
              <Users className="text-blue-600" size={18} />
              <h3 className="text-sm font-bold text-slate-900">Phân bổ Tài khoản & Quyền hạn</h3>
            </div>
            <span className="text-xs font-bold text-blue-600 bg-blue-50 px-2.5 py-1 rounded-full">
              {activeUserPercent}% Hoạt động
            </span>
          </div>
          <div className="mt-5 space-y-4">
            <div>
              <div className="flex justify-between text-xs font-bold text-slate-700 mb-1.5">
                <span>Quản trị viên (ADMIN)</span>
                <span>{userSummary.adminUsers} người</span>
              </div>
              <div className="h-2 w-full overflow-hidden rounded-full bg-slate-100">
                <div
                  className="h-full rounded-full bg-blue-600 transition-all duration-500"
                  style={{ width: `${userSummary.totalUsers > 0 ? (userSummary.adminUsers / userSummary.totalUsers) * 100 : 0}%` }}
                />
              </div>
            </div>
            <div>
              <div className="flex justify-between text-xs font-bold text-slate-700 mb-1.5">
                <span>Quản lý dự án (MANAGER)</span>
                <span>{userSummary.managerUsers} người</span>
              </div>
              <div className="h-2 w-full overflow-hidden rounded-full bg-slate-100">
                <div
                  className="h-full rounded-full bg-indigo-500 transition-all duration-500"
                  style={{ width: `${userSummary.totalUsers > 0 ? (userSummary.managerUsers / userSummary.totalUsers) * 100 : 0}%` }}
                />
              </div>
            </div>
            <div>
              <div className="flex justify-between text-xs font-bold text-slate-700 mb-1.5">
                <span>Nhân viên (EMPLOYEE)</span>
                <span>{userSummary.employeeUsers} người</span>
              </div>
              <div className="h-2 w-full overflow-hidden rounded-full bg-slate-100">
                <div
                  className="h-full rounded-full bg-emerald-500 transition-all duration-500"
                  style={{ width: `${userSummary.totalUsers > 0 ? (userSummary.employeeUsers / userSummary.totalUsers) * 100 : 0}%` }}
                />
              </div>
            </div>

            <div className="pt-2 flex items-center justify-around border-t border-slate-100 text-center">
              <div>
                <p className="text-xs text-slate-400">Tài khoản hoạt động</p>
                <p className="text-base font-black text-emerald-600">{userSummary.activeUsers}</p>
              </div>
              <div className="h-8 w-px bg-slate-100" />
              <div>
                <p className="text-xs text-slate-400">Tài khoản bị khóa</p>
                <p className="text-base font-black text-rose-600">{userSummary.disabledUsers}</p>
              </div>
            </div>
          </div>
        </motion.div>

        {/* Project Status Breakdown */}
        <motion.div variants={itemVariants} className="rounded-2xl border border-slate-200 bg-white p-5 shadow-2xs">
          <div className="flex items-center justify-between border-b border-slate-100 pb-4">
            <div className="flex items-center gap-2">
              <FolderKanban className="text-indigo-600" size={18} />
              <h3 className="text-sm font-bold text-slate-900">Trạng thái Dự án & Sprint</h3>
            </div>
            <span className="text-xs font-bold text-indigo-600 bg-indigo-50 px-2.5 py-1 rounded-full">
              {activeProjectPercent}% Đang thực thi
            </span>
          </div>
          <div className="mt-5 grid grid-cols-2 gap-3 text-xs font-semibold">
            <div className="rounded-xl border border-slate-100 bg-slate-50/50 p-3">
              <p className="text-slate-400">Đang lập kế hoạch</p>
              <p className="text-lg font-black text-slate-800 mt-1">{projectSummary.planningProjects}</p>
            </div>
            <div className="rounded-xl border border-indigo-100 bg-indigo-50/40 p-3">
              <p className="text-indigo-600">Đang hoạt động</p>
              <p className="text-lg font-black text-indigo-700 mt-1">{projectSummary.activeProjects}</p>
            </div>
            <div className="rounded-xl border border-amber-100 bg-amber-50/40 p-3">
              <p className="text-amber-600">Tạm dừng (On Hold)</p>
              <p className="text-lg font-black text-amber-700 mt-1">{projectSummary.onHoldProjects}</p>
            </div>
            <div className="rounded-xl border border-emerald-100 bg-emerald-50/40 p-3">
              <p className="text-emerald-600">Đã hoàn thành</p>
              <p className="text-lg font-black text-emerald-700 mt-1">{projectSummary.completedProjects}</p>
            </div>
          </div>
          <div className="mt-4 flex items-center justify-between pt-3 border-t border-slate-100 text-xs text-slate-500">
            <span>Sprint đang hoạt động: <strong className="text-slate-800">{sprintSummary.activeSprints}</strong></span>
            <span>Tổng Sprint: <strong className="text-slate-800">{sprintSummary.totalSprints}</strong></span>
          </div>
        </motion.div>

        {/* Task Progress & Overdue Analysis */}
        <motion.div variants={itemVariants} className="rounded-2xl border border-slate-200 bg-white p-5 shadow-2xs">
          <div className="flex items-center justify-between border-b border-slate-100 pb-4">
            <div className="flex items-center gap-2">
              <CheckSquare className="text-emerald-600" size={18} />
              <h3 className="text-sm font-bold text-slate-900">Tiến độ Công việc (Tasks)</h3>
            </div>
            <span className="text-xs font-bold text-emerald-600 bg-emerald-50 px-2.5 py-1 rounded-full">
              {taskDonePercent}% Tỷ lệ Hoàn thành
            </span>
          </div>
          <div className="mt-5 space-y-3">
            <div className="grid grid-cols-4 gap-2 text-center text-xs">
              <div className="rounded-xl bg-slate-50 p-2.5">
                <p className="text-slate-400">Cần làm</p>
                <p className="text-base font-black text-slate-700 mt-0.5">{taskSummary.todoTasks}</p>
              </div>
              <div className="rounded-xl bg-blue-50 p-2.5">
                <p className="text-blue-600">Đang làm</p>
                <p className="text-base font-black text-blue-700 mt-0.5">{taskSummary.inProgressTasks}</p>
              </div>
              <div className="rounded-xl bg-purple-50 p-2.5">
                <p className="text-purple-600">Review</p>
                <p className="text-base font-black text-purple-700 mt-0.5">{taskSummary.inReviewTasks}</p>
              </div>
              <div className="rounded-xl bg-amber-50 p-2.5">
                <p className="text-amber-600">Bị nghẽn</p>
                <p className="text-base font-black text-amber-700 mt-0.5">{taskSummary.blockedTasks}</p>
              </div>
            </div>

            {taskSummary.overdueTasks > 0 && (
              <div className="mt-3 flex items-center justify-between rounded-xl border border-rose-200 bg-rose-50/70 p-3 text-xs text-rose-800">
                <div className="flex items-center gap-2 font-bold">
                  <AlertTriangle size={16} className="text-rose-600 shrink-0" />
                  <span>Có {taskSummary.overdueTasks} công việc đang bị quá hạn (Overdue)</span>
                </div>
              </div>
            )}
          </div>
        </motion.div>

        {/* Bug Quality & File Storage Analysis */}
        <motion.div variants={itemVariants} className="rounded-2xl border border-slate-200 bg-white p-5 shadow-2xs">
          <div className="flex items-center justify-between border-b border-slate-100 pb-4">
            <div className="flex items-center gap-2">
              <Bug className="text-rose-600" size={18} />
              <h3 className="text-sm font-bold text-slate-900">Chất lượng Lỗi (Bugs) & Lưu trữ</h3>
            </div>
            <span className="text-xs font-bold text-purple-600 bg-purple-50 px-2.5 py-1 rounded-full">
              {formatBytes(attachmentSummary.totalSizeBytes)}
            </span>
          </div>
          <div className="mt-5 space-y-4">
            <div className="grid grid-cols-4 gap-2 text-center text-xs">
              <div className="rounded-xl bg-rose-50 p-2.5">
                <p className="text-rose-600">Mới tạo</p>
                <p className="text-base font-black text-rose-700 mt-0.5">{bugSummary.openBugs}</p>
              </div>
              <div className="rounded-xl bg-amber-50 p-2.5">
                <p className="text-amber-600">Đang sửa</p>
                <p className="text-base font-black text-amber-700 mt-0.5">{bugSummary.inProgressBugs}</p>
              </div>
              <div className="rounded-xl bg-blue-50 p-2.5">
                <p className="text-blue-600">Đã giải quyết</p>
                <p className="text-base font-black text-blue-700 mt-0.5">{bugSummary.resolvedBugs}</p>
              </div>
              <div className="rounded-xl bg-emerald-50 p-2.5">
                <p className="text-emerald-600">Đóng (Closed)</p>
                <p className="text-base font-black text-emerald-700 mt-0.5">{bugSummary.closedBugs}</p>
              </div>
            </div>

            <div className="flex items-center justify-between pt-2 border-t border-slate-100 text-xs">
              <div className="flex items-center gap-2 text-slate-600">
                <HardDrive size={14} className="text-purple-600" />
                <span>Tổng số File đính kèm: <strong>{attachmentSummary.totalAttachments} files</strong></span>
              </div>
              <Button
                variant="ghost"
                size="sm"
                className="!text-xs !text-purple-600 hover:!bg-purple-50"
                onClick={() => onNavigateSection?.('files')}
              >
                Quản lý & Dọn dẹp File &rarr;
              </Button>
            </div>
          </div>
        </motion.div>
      </div>
    </motion.div>
  )
}
