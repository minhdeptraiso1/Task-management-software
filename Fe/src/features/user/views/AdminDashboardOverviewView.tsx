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
  Sparkles
} from 'lucide-react'
import { Button, toast } from '../../../components/ui'
import type { AdminDashboardResponse } from '../models/admin.model'
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
  onRefresh,
  onNavigateSection
}: {
  dashboard: AdminDashboardResponse | null
  loading: boolean
  error: string
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

  return (
    <motion.div
      variants={containerVariants}
      initial="initial"
      animate="animate"
      className="space-y-6 p-6"
    >
      {/* System Status Banner */}
      <motion.div variants={itemVariants} className="flex flex-wrap items-center justify-between gap-4 rounded-2xl border border-slate-200 bg-white p-4 shadow-2xs">
        <div className="flex items-center gap-3">
          <div className="flex size-10 items-center justify-center rounded-xl bg-brand/10 text-brand">
            <Activity size={20} />
          </div>
          <div>
            <h2 className="text-sm font-bold text-slate-900">Tổng quan Hệ thống HICAS ONE</h2>
            <p className="text-xs text-slate-500">
              Cập nhật lúc: {new Date(systemSummary.generatedAt).toLocaleString('vi-VN')} ({systemSummary.timezone})
            </p>
          </div>
        </div>
        <div className="flex flex-wrap items-center gap-3">
          <span className="inline-flex items-center gap-1.5 rounded-full bg-emerald-50 px-3 py-1 text-xs font-bold text-emerald-700 border border-emerald-200/80">
            <span className="size-2 rounded-full bg-emerald-500 animate-pulse" />
            Hệ thống ổn định
          </span>
          <Button
            variant="secondary"
            size="sm"
            className="!border-indigo-300 !bg-indigo-50 !text-indigo-800 hover:!bg-indigo-100"
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
            className="!border-amber-300 !bg-amber-50 !text-amber-800 hover:!bg-amber-100"
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

      {/* Top 6 KPI Summary Cards */}
      <motion.div variants={containerVariants} className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-6">
        {/* User Card */}
        <motion.div
          variants={itemVariants}
          className="group relative overflow-hidden rounded-2xl border border-slate-200 bg-white p-4 shadow-2xs hover:shadow-md transition-all cursor-pointer"
          onClick={() => onNavigateSection?.('members')}
        >
          <div className="flex items-center justify-between">
            <span className="text-[11px] font-extrabold uppercase tracking-wider text-slate-400">Người dùng</span>
            <div className="flex size-8 items-center justify-center rounded-lg bg-blue-50 text-blue-600 group-hover:scale-110 transition-transform">
              <Users size={16} />
            </div>
          </div>
          <p className="mt-3 text-2xl font-black text-slate-900">{userSummary.totalUsers}</p>
          <div className="mt-2 flex items-center justify-between text-xs text-slate-500">
            <span className="flex items-center gap-1 text-emerald-600 font-bold">
              <UserCheck size={12} /> {userSummary.activeUsers} hoạt động
            </span>
            <span className="text-slate-400">{userSummary.disabledUsers} khóa</span>
          </div>
        </motion.div>

        {/* Project Card */}
        <motion.div variants={itemVariants} className="group relative overflow-hidden rounded-2xl border border-slate-200 bg-white p-4 shadow-2xs hover:shadow-md transition-all">
          <div className="flex items-center justify-between">
            <span className="text-[11px] font-extrabold uppercase tracking-wider text-slate-400">Dự án</span>
            <div className="flex size-8 items-center justify-center rounded-lg bg-indigo-50 text-indigo-600 group-hover:scale-110 transition-transform">
              <FolderKanban size={16} />
            </div>
          </div>
          <p className="mt-3 text-2xl font-black text-slate-900">{projectSummary.totalProjects}</p>
          <div className="mt-2 flex items-center justify-between text-xs text-slate-500">
            <span className="font-bold text-indigo-600">{projectSummary.activeProjects} đang chạy</span>
            <span className="text-slate-400">{projectSummary.completedProjects} hoàn thành</span>
          </div>
        </motion.div>

        {/* Sprint Card */}
        <motion.div variants={itemVariants} className="group relative overflow-hidden rounded-2xl border border-slate-200 bg-white p-4 shadow-2xs hover:shadow-md transition-all">
          <div className="flex items-center justify-between">
            <span className="text-[11px] font-extrabold uppercase tracking-wider text-slate-400">Sprint</span>
            <div className="flex size-8 items-center justify-center rounded-lg bg-amber-50 text-amber-600 group-hover:scale-110 transition-transform">
              <Zap size={16} />
            </div>
          </div>
          <p className="mt-3 text-2xl font-black text-slate-900">{sprintSummary.totalSprints}</p>
          <div className="mt-2 flex items-center justify-between text-xs text-slate-500">
            <span className="font-bold text-amber-600">{sprintSummary.activeSprints} đang mở</span>
            <span className="text-slate-400">{sprintSummary.planningSprints} kế hoạch</span>
          </div>
        </motion.div>

        {/* Task Card */}
        <motion.div variants={itemVariants} className="group relative overflow-hidden rounded-2xl border border-slate-200 bg-white p-4 shadow-2xs hover:shadow-md transition-all">
          <div className="flex items-center justify-between">
            <span className="text-[11px] font-extrabold uppercase tracking-wider text-slate-400">Công việc</span>
            <div className="flex size-8 items-center justify-center rounded-lg bg-emerald-50 text-emerald-600 group-hover:scale-110 transition-transform">
              <CheckSquare size={16} />
            </div>
          </div>
          <p className="mt-3 text-2xl font-black text-slate-900">{taskSummary.totalTasks}</p>
          <div className="mt-2 flex items-center justify-between text-xs text-slate-500">
            <span className="font-bold text-emerald-600">{taskSummary.doneTasks} hoàn thành</span>
            {taskSummary.overdueTasks > 0 && (
              <span className="font-bold text-rose-600 flex items-center gap-0.5">
                <AlertTriangle size={11} /> {taskSummary.overdueTasks} quá hạn
              </span>
            )}
          </div>
        </motion.div>

        {/* Bug Card */}
        <motion.div variants={itemVariants} className="group relative overflow-hidden rounded-2xl border border-slate-200 bg-white p-4 shadow-2xs hover:shadow-md transition-all">
          <div className="flex items-center justify-between">
            <span className="text-[11px] font-extrabold uppercase tracking-wider text-slate-400">Lỗi (Bugs)</span>
            <div className="flex size-8 items-center justify-center rounded-lg bg-rose-50 text-rose-600 group-hover:scale-110 transition-transform">
              <Bug size={16} />
            </div>
          </div>
          <p className="mt-3 text-2xl font-black text-slate-900">{bugSummary.totalBugs}</p>
          <div className="mt-2 flex items-center justify-between text-xs text-slate-500">
            <span className="font-bold text-rose-600">{bugSummary.openBugs + bugSummary.inProgressBugs} cần xử lý</span>
            {bugSummary.criticalBugs > 0 && (
              <span className="font-bold text-rose-700 bg-rose-100 px-1 rounded text-[10px]">
                {bugSummary.criticalBugs} nghiêm trọng
              </span>
            )}
          </div>
        </motion.div>

        {/* Storage Card */}
        <motion.div
          variants={itemVariants}
          className="group relative overflow-hidden rounded-2xl border border-slate-200 bg-white p-4 shadow-2xs hover:shadow-md transition-all cursor-pointer"
          onClick={() => onNavigateSection?.('files')}
        >
          <div className="flex items-center justify-between">
            <span className="text-[11px] font-extrabold uppercase tracking-wider text-slate-400">Dung lượng File</span>
            <div className="flex size-8 items-center justify-center rounded-lg bg-purple-50 text-purple-600 group-hover:scale-110 transition-transform">
              <HardDrive size={16} />
            </div>
          </div>
          <p className="mt-3 text-xl font-black text-slate-900 truncate">{formatBytes(attachmentSummary.totalSizeBytes)}</p>
          <div className="mt-2 flex items-center justify-between text-xs text-slate-500">
            <span className="font-bold text-purple-600">{attachmentSummary.totalAttachments} đính kèm</span>
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
