import { useState } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import {
  LayoutDashboard,
  FolderKanban,
  Bell,
  Clock,
  Search,
  ShieldCheck,
  CheckCircle2,
  AlertCircle,
  LogOut,
  ChevronRight
} from 'lucide-react'
import { Button } from '../../../components/ui'

export interface MainDashboardViewProps {
  username?: string
  roleName?: string
  projectCount?: number
  unreadNotifications?: number
  doneTasks?: number
  overdueTasks?: number
  tasksList?: Array<{
    taskId: string
    title: string
    projectCode: string
    status: string
    priority: string
    dueDate?: string
    overdue?: boolean
  }>
  onLogout?: () => void
}

// Custom cubic-bezier easing matching GSAP expo.out
const EXPO_OUT_EASE = [0.16, 1, 0.3, 1] as const

// ----------------------------------------------------------------------
// PHẦN 1: KHUNG GIAO DIỆN (LAYOUT SKELETON - CHẠY 1 LẦN DUY NHẤT)
// ----------------------------------------------------------------------

// 1.1 Sidebar (Menu trái): translateX -100%, opacity 0 -> translateX 0, opacity 1 in 1.0s, delay 0s
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

// 1.2 Header Topbar: translateY -30px (ngắn 30px), opacity 0 -> translateY 0, opacity 1 in 0.8s, delay 0.2s
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

// ----------------------------------------------------------------------
// PHẦN 2: NỘI DUNG CHÍNH (MAIN CONTENT STAGGERED FADE-UP - DELAY 0.4s)
// ----------------------------------------------------------------------

const mainContentContainerVariants = {
  initial: {},
  animate: {
    transition: {
      delayChildren: 0.4, // Kích hoạt sau khi Header trượt xuống
      staggerChildren: 0.1,
    },
  },
}

// 2.1 Lớp 1: Banner Welcome / Header Title (y: 20px, opacity 0 -> y: 0, opacity 1 in 0.8s)
const bannerLayerVariants = {
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

// 2.2 Lớp 2: Metric Cards Row Container with 0.1s stagger
const cardsRowContainerVariants = {
  initial: {},
  animate: {
    transition: {
      staggerChildren: 0.1,
    },
  },
}

// Mỗi Card: y: 20px, opacity 0 -> y: 0, opacity 1 in 0.8s
const cardItemVariants = {
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

// 2.3 Lớp 3: Bảng Dữ Liệu / Danh Sách Task (staggerChildren 0.05s cho từng hàng)
const tableContainerVariants = {
  initial: { y: 20, opacity: 0 },
  animate: {
    y: 0,
    opacity: 1,
    transition: {
      duration: 0.8,
      ease: EXPO_OUT_EASE,
      staggerChildren: 0.05,
      delayChildren: 0.1,
    },
  },
}

// Mỗi hàng/item: y: 10px, opacity 0 -> y: 0, opacity 1 in 0.6s
const tableRowItemVariants = {
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

// Tab Switch Transition: Fade in/out 0.3s khi chuyển giữa các tab (Sidebar & Header giữ CỐ ĐỊNH 100%)
const tabSwitchVariants = {
  initial: { opacity: 0 },
  animate: {
    opacity: 1,
    transition: {
      duration: 0.3,
      ease: EXPO_OUT_EASE,
    },
  },
  exit: {
    opacity: 0,
    transition: {
      duration: 0.2,
      ease: EXPO_OUT_EASE,
    },
  },
}

export function MainDashboardView({
  username = 'Tuấn Anh',
  roleName = 'Quản trị viên',
  projectCount = 3,
  unreadNotifications = 5,
  doneTasks = 12,
  overdueTasks = 2,
  tasksList = [
    { taskId: '1', title: 'Thiết kế giao diện Dashboard 60fps', projectCode: 'TASK-MGT', status: 'Đang triển khai', priority: 'Khẩn cấp', dueDate: '29/07/2026' },
    { taskId: '2', title: 'Tối ưu hiệu ứng Framer Motion', projectCode: 'HICAS-EXP', status: 'Đang triển khai', priority: 'Cao', dueDate: '30/07/2026' },
    { taskId: '3', title: 'Kiểm thử Realtime Audit Logs', projectCode: 'TASK-MGT', status: 'Hoàn thành', priority: 'Bình thường', dueDate: '28/07/2026' },
  ],
  onLogout
}: MainDashboardViewProps) {
  const [activeTab, setActiveTab] = useState<'overview' | 'timesheet'>('overview')

  return (
    <div className="min-h-screen bg-canvas text-ink overflow-x-hidden">
      {/* ------------------------------------------------------------------ */}
      {/* PHẦN 1.2: HEADER TOPBAR (Trượt xuống 30px, Opacity 0 -> 1 trong 0.8s, Delay 0.2s) */}
      {/* ------------------------------------------------------------------ */}
      <motion.header
        variants={headerVariants}
        initial="initial"
        animate="animate"
        style={{ willChange: 'transform, opacity', transform: 'translateZ(0)' }}
        className="sticky top-0 z-30 flex h-14 w-full items-center justify-between bg-brand-black px-5 text-white shadow-md"
      >
        <div className="flex items-center gap-3 font-bold">
          <span className="text-xl tracking-tighter text-white">
            HI<span className="text-accent">CAS</span>
          </span>
          <span className="h-4 w-px bg-white/20" />
          <span className="text-xs tracking-widest text-white/70">WORKSPACE</span>
        </div>

        <div className="flex items-center gap-3">
          <div className="hidden sm:flex items-center gap-2 rounded-lg bg-white/10 px-3 py-1.5 text-xs text-white/80">
            <Search size={14} />
            <span>Tìm kiếm dự án, task... (Ctrl + K)</span>
          </div>

          <div className="flex items-center gap-2">
            <div className="grid size-8 place-items-center rounded-full bg-brand text-xs font-bold text-white uppercase">
              {username.charAt(0)}
            </div>
            <span className="hidden text-xs font-semibold md:block">{username}</span>
            <span className="hidden text-[10px] bg-white/15 px-2 py-0.5 rounded-full text-white/80 md:block">{roleName}</span>
          </div>

          <div className="h-5 w-px bg-white/20 mx-1" />

          {onLogout && (
            <Button
              variant="ghost"
              size="sm"
              className="!text-white/80 hover:!text-white hover:bg-white/10"
              leadingIcon={<LogOut size={16} />}
              onClick={onLogout}
            >
              Đăng xuất
            </Button>
          )}
        </div>
      </motion.header>

      {/* Main Grid Layout */}
      <div className="grid gap-5 p-5 grid-cols-1 lg:grid-cols-[320px_minmax(0,1fr)] xl:grid-cols-[360px_minmax(0,1fr)] items-start">
        {/* ------------------------------------------------------------------ */}
        {/* PHẦN 1.1: SIDEBAR (Trượt từ trái x: -100% -> 0%, Opacity 0 -> 1 trong 1.0s, Delay 0s) */}
        {/* ------------------------------------------------------------------ */}
        <motion.aside
          variants={sidebarVariants}
          initial="initial"
          animate="animate"
          style={{ willChange: 'transform, opacity', transform: 'translateZ(0)' }}
          className="sticky top-20 flex flex-col rounded-xl border border-line bg-white p-5 shadow-xs"
        >
          <div className="flex items-center justify-between border-b border-line/60 pb-4 mb-4">
            <h2 className="font-bold text-base text-ink">Dự án của tôi</h2>
            <span className="text-xs text-muted font-semibold">HiCAS Workspace</span>
          </div>

          <div className="space-y-2">
            <button
              type="button"
              className="w-full flex items-center justify-between rounded-xl bg-brand/10 border border-brand/20 p-3 text-left font-bold text-brand-dark transition hover:bg-brand/15"
            >
              <div className="flex items-center gap-2.5">
                <LayoutDashboard size={18} className="text-brand" />
                <span className="text-sm">Trang tổng quan</span>
              </div>
              <ChevronRight size={16} className="text-brand opacity-80" />
            </button>

            <div className="pt-2 text-xs font-bold uppercase tracking-wider text-muted px-1">Dự án đang triển khai</div>

            <div className="rounded-xl border border-line p-3 transition hover:border-slate-300 bg-white">
              <div className="flex items-center justify-between text-xs font-bold text-slate-800">
                <span>HICAS_EXP</span>
                <span className="rounded-full bg-slate-100 px-2 py-0.5 text-[10px] text-muted font-semibold">Lưu trữ</span>
              </div>
              <p className="text-xs font-semibold text-ink mt-1 truncate">Hệ thống quản lý tài chính</p>
            </div>

            <div className="rounded-xl border border-brand/30 bg-amber-50/40 p-3 transition hover:border-brand shadow-2xs">
              <div className="flex items-center justify-between text-xs font-bold text-amber-800">
                <span>TASK-MANAGEMENT</span>
                <span className="rounded-full bg-emerald-100 text-emerald-800 px-2 py-0.5 text-[10px] font-extrabold">Đang chạy</span>
              </div>
              <p className="text-xs font-bold text-ink mt-1 truncate">Hệ thống quản lý công việc Agile Scrum</p>
            </div>
          </div>
        </motion.aside>

        {/* ------------------------------------------------------------------ */}
        {/* PHẦN 2: NỘI DUNG CHÍNH (MAIN CONTENT - STAGGERED FADE-UP DELAY 0.4s) */}
        {/* ------------------------------------------------------------------ */}
        <motion.main
          variants={mainContentContainerVariants}
          initial="initial"
          animate="animate"
          className="min-w-0 flex-1 rounded-xl border border-line bg-white p-6 shadow-xs"
        >
          {/* Top Main Navigation Header (Giữ CỐ ĐỊNH 100% khi chuyển tab) */}
          <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 pb-6 border-b border-line/60 mb-6">
            <div>
              <h1 className="text-2xl font-bold text-ink">Chào mừng, {username}!</h1>
              <p className="mt-1 text-xs text-muted">Dưới đây là tổng quan công việc của bạn hôm nay.</p>
            </div>

            {/* Sub-Tab Toggle Pills */}
            <div className="inline-flex rounded-full bg-slate-100 p-1 border border-line/60">
              <Button
                type="button"
                variant="secondary"
                size="sm"
                leadingIcon={<LayoutDashboard size={14} />}
                className={activeTab === 'overview' ? '!rounded-full !bg-brand !text-white !border-transparent' : '!rounded-full !bg-transparent !border-transparent !text-muted-dark hover:!text-ink'}
                onClick={() => setActiveTab('overview')}
              >
                Tổng quan
              </Button>
              <Button
                type="button"
                variant="secondary"
                size="sm"
                leadingIcon={<Clock size={14} />}
                className={activeTab === 'timesheet' ? '!rounded-full !bg-brand !text-white !border-transparent' : '!rounded-full !bg-transparent !border-transparent !text-muted-dark hover:!text-ink'}
                onClick={() => setActiveTab('timesheet')}
              >
                Timesheet cá nhân
              </Button>
            </div>
          </div>

          {/* Tab Content wrapped in AnimatePresence for silky smooth tab switching without layout jank */}
          <AnimatePresence mode="wait">
            {activeTab === 'overview' ? (
              <motion.div
                key="overview-tab"
                variants={tabSwitchVariants}
                initial="initial"
                animate="animate"
                exit="exit"
                className="space-y-6"
              >
                {/* 2.1 Lớp 1: Banner Welcome / Title Layer */}
                <motion.div
                  variants={bannerLayerVariants}
                  style={{ willChange: 'transform, opacity' }}
                  className="rounded-2xl bg-gradient-to-r from-slate-900 via-sky-950 to-blue-950 p-6 text-white shadow-sm relative overflow-hidden"
                >
                  <div className="relative z-10 max-w-xl">
                    <span className="inline-flex items-center gap-2 rounded-full bg-white/10 px-3 py-1 text-xs font-extrabold uppercase tracking-wider text-cyan-300">
                      <ShieldCheck size={14} /> HiCAS Workstation 2026
                    </span>
                    <h2 className="text-2xl font-bold mt-3 leading-snug">Vận hành dự án & công việc hiệu quả</h2>
                    <p className="text-xs text-white/70 mt-2 leading-relaxed">
                      Theo dõi tiến độ Sprint, hoàn thành task đúng thời hạn và tổng hợp thời gian làm việc chính xác.
                    </p>
                  </div>
                </motion.div>

                {/* 2.2 Lớp 2: Metric Cards Row Container (StaggerChildren 0.1s cho tất cả các thẻ Card) */}
                <motion.div
                  variants={cardsRowContainerVariants}
                  initial="initial"
                  animate="animate"
                  className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4"
                >
                  {/* Card 1 */}
                  <motion.div
                    variants={cardItemVariants}
                    style={{ willChange: 'transform, opacity' }}
                    className="rounded-2xl border border-line/70 bg-white p-5 shadow-xs transition-all hover:-translate-y-0.5 hover:shadow-md flex flex-col justify-between"
                  >
                    <div className="flex items-center justify-between">
                      <div className="grid size-11 place-items-center rounded-xl bg-brand/10 text-brand border border-brand/20">
                        <FolderKanban size={22} />
                      </div>
                      <span className="text-[10px] font-extrabold uppercase tracking-wider text-muted bg-panel px-2 py-0.5 rounded-full">
                        ĐANG HOẠT ĐỘNG
                      </span>
                    </div>
                    <div className="mt-4">
                      <p className="text-xs font-medium text-muted">Dự án tham gia</p>
                      <p className="text-2xl font-bold text-ink mt-0.5">{projectCount}</p>
                    </div>
                  </motion.div>

                  {/* Card 2 */}
                  <motion.div
                    variants={cardItemVariants}
                    style={{ willChange: 'transform, opacity' }}
                    className="rounded-2xl border border-line/70 bg-white p-5 shadow-xs transition-all hover:-translate-y-0.5 hover:shadow-md flex flex-col justify-between"
                  >
                    <div className="flex items-center justify-between">
                      <div className="grid size-11 place-items-center rounded-xl bg-info/10 text-info border border-info/20">
                        <Bell size={22} />
                      </div>
                      <span className="text-[10px] font-extrabold uppercase tracking-wider text-muted bg-panel px-2 py-0.5 rounded-full">
                        THÔNG BÁO
                      </span>
                    </div>
                    <div className="mt-4">
                      <p className="text-xs font-medium text-muted">Thông báo mới</p>
                      <p className="text-2xl font-bold text-ink mt-0.5">{unreadNotifications}</p>
                    </div>
                  </motion.div>

                  {/* Card 3 */}
                  <motion.div
                    variants={cardItemVariants}
                    style={{ willChange: 'transform, opacity' }}
                    className="rounded-2xl border border-line/70 bg-white p-5 shadow-xs transition-all hover:-translate-y-0.5 hover:shadow-md flex flex-col justify-between"
                  >
                    <div className="flex items-center justify-between">
                      <div className="grid size-11 place-items-center rounded-xl bg-success/10 text-success border border-success/20">
                        <CheckCircle2 size={22} />
                      </div>
                      <span className="text-[10px] font-extrabold uppercase tracking-wider text-success bg-success/10 px-2 py-0.5 rounded-full">
                        HOÀN THÀNH
                      </span>
                    </div>
                    <div className="mt-4">
                      <p className="text-xs font-medium text-muted">Task hoàn thành</p>
                      <p className="text-2xl font-bold text-success mt-0.5">{doneTasks}</p>
                    </div>
                  </motion.div>

                  {/* Card 4 */}
                  <motion.div
                    variants={cardItemVariants}
                    style={{ willChange: 'transform, opacity' }}
                    className="rounded-2xl border border-line/70 bg-white p-5 shadow-xs transition-all hover:-translate-y-0.5 hover:shadow-md flex flex-col justify-between"
                  >
                    <div className="flex items-center justify-between">
                      <div className="grid size-11 place-items-center rounded-xl bg-danger/10 text-danger border border-danger/20">
                        <AlertCircle size={22} />
                      </div>
                      <span className="text-[10px] font-extrabold uppercase tracking-wider text-danger bg-danger/10 px-2 py-0.5 rounded-full">
                        CẦN CHÚ Ý
                      </span>
                    </div>
                    <div className="mt-4">
                      <p className="text-xs font-medium text-muted">Task trễ hạn</p>
                      <p className="text-2xl font-bold text-danger mt-0.5">{overdueTasks}</p>
                    </div>
                  </motion.div>
                </motion.div>

                {/* 2.3 Lớp 3: Bảng Dữ Liệu Task (StaggerChildren 0.05s cho mỗi hàng) */}
                <motion.section
                  variants={tableContainerVariants}
                  initial="initial"
                  animate="animate"
                  style={{ willChange: 'transform, opacity' }}
                  className="rounded-2xl border border-line/70 bg-white shadow-xs overflow-hidden"
                >
                  <header className="flex items-center justify-between border-b border-line/60 px-6 py-4">
                    <h3 className="font-bold text-ink text-sm">Công việc của tôi</h3>
                    <span className="text-xs text-muted">Tổng cộng {tasksList.length} công việc</span>
                  </header>

                  <div className="overflow-x-auto">
                    <table className="w-full text-left text-xs">
                      <thead className="bg-slate-50/80 text-muted-dark border-b border-line/60 font-bold uppercase text-[11px]">
                        <tr>
                          <th className="px-6 py-3.5">Tên công việc</th>
                          <th className="px-5 py-3.5">Dự án</th>
                          <th className="px-5 py-3.5">Trạng thái</th>
                          <th className="px-5 py-3.5">Độ ưu tiên</th>
                          <th className="px-5 py-3.5">Hạn chót</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-line/50">
                        {tasksList.map(task => (
                          <motion.tr
                            key={task.taskId}
                            variants={tableRowItemVariants}
                            style={{ willChange: 'transform, opacity' }}
                            className="transition-colors hover:bg-slate-50/80"
                          >
                            <td className="px-6 py-3.5 font-semibold text-ink">
                              <div className="flex items-center gap-2">
                                <span className="size-1.5 rounded-full bg-brand shrink-0" />
                                <span className="truncate max-w-xs">{task.title}</span>
                              </div>
                            </td>
                            <td className="px-5 py-3.5">
                              <span className="inline-flex items-center rounded-full bg-brand-cream/80 border border-brand-line/60 text-brand-dark px-2.5 py-0.5 text-[11px] font-bold">
                                {task.projectCode}
                              </span>
                            </td>
                            <td className="px-5 py-3.5">
                              <span className="inline-flex items-center rounded-md bg-slate-100 px-2 py-0.5 text-[11px] font-medium text-muted-dark">
                                {task.status}
                              </span>
                            </td>
                            <td className="px-5 py-3.5 text-muted-dark font-medium">
                              {task.priority}
                            </td>
                            <td className="px-5 py-3.5 text-muted font-semibold">
                              {task.dueDate}
                            </td>
                          </motion.tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                </motion.section>
              </motion.div>
            ) : (
              <motion.div
                key="timesheet-tab"
                variants={tabSwitchVariants}
                initial="initial"
                animate="animate"
                exit="exit"
                className="p-4 text-sm text-muted rounded-xl border border-line bg-slate-50/50"
              >
                <div className="flex items-center gap-2 font-bold text-ink text-base mb-2">
                  <Clock size={20} className="text-brand" /> Bảng Timesheet cá nhân
                </div>
                <p>Tổng hợp chi tiết giờ làm việc và nhật ký cá nhân của {username}.</p>
              </motion.div>
            )}
          </AnimatePresence>
        </motion.main>
      </div>
    </div>
  )
}
