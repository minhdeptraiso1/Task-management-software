import { useState } from 'react'
import { Clock, CheckCircle2, AlertCircle, LayoutDashboard, CalendarDays, CalendarClock, ShieldAlert, FolderKanban, Bell, ChevronLeft, ChevronRight, Target } from 'lucide-react'
import type { User } from '../../user/models/user.model'
import type { MyDashboardResponse, MyTaskPageResponse, MyTimeSummaryResponse } from '../models/dashboard.model'
import { Button } from '../../../components/ui'
import { taskStatusLabels, taskPriorityLabels, taskRiskReasonLabels } from '../../project/models/task.model'
import { TimesheetView } from '../../project/components/TimesheetView'

interface PersonalDashboardViewProps {
  me: User
  dashboard: MyDashboardResponse | null
  tasks: MyTaskPageResponse | null
  timeSummary: MyTimeSummaryResponse | null
  loading: boolean
  error: string
  taskPage: number
  onTaskPageChange: (page: number) => void
}

export function PersonalDashboardView({
  me,
  dashboard,
  tasks,
  timeSummary,
  loading,
  error,
  taskPage,
  onTaskPageChange
}: PersonalDashboardViewProps) {
  const [activeSubTab, setActiveSubTab] = useState<'overview' | 'timesheet'>('overview')
  const isOverview = activeSubTab === 'overview'
  const isTimesheet = activeSubTab === 'timesheet'

  if (loading && !dashboard) {
    return (
      <div className="grid h-64 place-items-center p-8">
        <span className="size-8 animate-spin rounded-full border-4 border-brand border-r-transparent" />
      </div>
    )
  }

  if (error) {
    return (
      <div className="p-8 text-center text-rose-600 bg-rose-50/50 rounded-2xl border border-rose-100 m-6">
        <AlertCircle className="mx-auto mb-2 size-8 text-rose-500" />
        <p className="font-semibold text-sm">{error}</p>
      </div>
    )
  }

  // Compact tab segment with animated sliding pill background
  const renderSlidingTabs = () => (
    <div className="relative inline-grid grid-cols-2 rounded-full bg-slate-200/70 p-1 border border-line/40 shadow-2xs shrink-0">
      {/* Animated sliding background pill */}
      <div 
        className="absolute top-1 bottom-1 rounded-full bg-brand shadow-xs transition-all duration-300 ease-out"
        style={{
          left: isOverview ? '4px' : 'calc(50% + 2px)',
          width: 'calc(50% - 6px)'
        }}
      />

      <button
        type="button"
        onClick={() => setActiveSubTab('overview')}
        className={`relative z-10 flex items-center justify-center gap-2 rounded-full px-4 py-1.5 text-xs font-bold whitespace-nowrap transition-colors duration-200 ${
          isOverview ? 'text-white' : 'text-slate-600 hover:text-slate-900'
        }`}
      >
        <LayoutDashboard size={15} />
        <span>Tổng quan</span>
      </button>

      <button
        type="button"
        onClick={() => setActiveSubTab('timesheet')}
        className={`relative z-10 flex items-center justify-center gap-2 rounded-full px-4 py-1.5 text-xs font-bold whitespace-nowrap transition-colors duration-200 ${
          isTimesheet ? 'text-white' : 'text-slate-600 hover:text-slate-900'
        }`}
      >
        <Clock size={15} />
        <span>Timesheet cá nhân</span>
      </button>
    </div>
  )

  if (isTimesheet) {
    return (
      <div className="p-6 space-y-6 animate-enter">
        <header className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
          <div>
            <h1 className="text-2xl font-bold text-ink">Timesheet cá nhân</h1>
            <p className="mt-1 text-xs text-muted">Quản lý và tổng hợp thời gian làm việc của {me.username}</p>
          </div>
          {renderSlidingTabs()}
        </header>
        <TimesheetView mode="personal" />
      </div>
    )
  }

  // Calculate work hours target progress (monthly target e.g. 160h = 9600m)
  const monthlyTargetMinutes = 9600
  const monthMinutes = timeSummary?.monthMinutes || 0
  const progressTargetPercent = Math.min(Math.round((monthMinutes / monthlyTargetMinutes) * 100), 100)

  return (
    <div className="p-6 space-y-6 animate-enter">
      <header className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-ink">Chào mừng, {me.username}!</h1>
          <p className="mt-1 text-xs text-muted">Dưới đây là tổng quan công việc của bạn hôm nay.</p>
        </div>
        {renderSlidingTabs()}
      </header>

      {/* Top 4 Stat Cards */}
      {dashboard && (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
          {/* Card 1: Dự án tham gia */}
          <div className="rounded-2xl border border-line/70 bg-white p-5 shadow-xs transition-all duration-200 hover:-translate-y-0.5 hover:shadow-md relative overflow-hidden flex flex-col justify-between">
            <div className="flex items-center justify-between">
              <div className="grid size-11 place-items-center rounded-xl bg-amber-50 text-amber-600 border border-amber-100">
                <FolderKanban size={22} />
              </div>
              <span className="text-[10px] font-extrabold uppercase tracking-wider text-muted-dark bg-slate-100 px-2 py-0.5 rounded-full">
                ĐANG HOẠT ĐỘNG
              </span>
            </div>
            <div className="mt-4">
              <p className="text-xs font-medium text-muted">Dự án tham gia</p>
              <p className="text-2xl font-bold text-ink mt-0.5">{dashboard.projectCount}</p>
            </div>
          </div>

          {/* Card 2: Thông báo mới */}
          <div className="rounded-2xl border border-line/70 bg-white p-5 shadow-xs transition-all duration-200 hover:-translate-y-0.5 hover:shadow-md relative overflow-hidden flex flex-col justify-between">
            <div className="flex items-center justify-between">
              <div className="grid size-11 place-items-center rounded-xl bg-sky-50 text-sky-600 border border-sky-100">
                <Bell size={22} />
              </div>
              <span className="text-[10px] font-extrabold uppercase tracking-wider text-muted-dark bg-slate-100 px-2 py-0.5 rounded-full">
                THÔNG BÁO
              </span>
            </div>
            <div className="mt-4">
              <p className="text-xs font-medium text-muted">Thông báo mới</p>
              <p className="text-2xl font-bold text-ink mt-0.5">{dashboard.unreadNotifications}</p>
            </div>
          </div>

          {/* Card 3: Task hoàn thành */}
          <div className="rounded-2xl border border-line/70 bg-white p-5 shadow-xs transition-all duration-200 hover:-translate-y-0.5 hover:shadow-md relative overflow-hidden flex flex-col justify-between">
            <div className="flex items-center justify-between">
              <div className="grid size-11 place-items-center rounded-xl bg-emerald-50 text-emerald-600 border border-emerald-100">
                <CheckCircle2 size={22} />
              </div>
              <span className="text-[10px] font-extrabold uppercase tracking-wider text-emerald-700 bg-emerald-50 px-2 py-0.5 rounded-full">
                HOÀN THÀNH
              </span>
            </div>
            <div className="mt-4">
              <p className="text-xs font-medium text-muted">Task hoàn thành</p>
              <p className="text-2xl font-bold text-emerald-600 mt-0.5">{dashboard.taskSummary.doneTasks}</p>
            </div>
          </div>

          {/* Card 4: Task trễ hạn */}
          <div className="rounded-2xl border border-line/70 bg-white p-5 shadow-xs transition-all duration-200 hover:-translate-y-0.5 hover:shadow-md relative overflow-hidden flex flex-col justify-between">
            <div className="flex items-center justify-between">
              <div className="grid size-11 place-items-center rounded-xl bg-rose-50 text-rose-600 border border-rose-100">
                <AlertCircle size={22} />
              </div>
              <span className="text-[10px] font-extrabold uppercase tracking-wider text-rose-700 bg-rose-50 px-2 py-0.5 rounded-full">
                CẦN CHÚ Ý
              </span>
            </div>
            <div className="mt-4">
              <p className="text-xs font-medium text-muted">Task trễ hạn</p>
              <p className="text-2xl font-bold text-rose-600 mt-0.5">{dashboard.taskSummary.overdueTasks}</p>
            </div>
          </div>
        </div>
      )}

      {/* Main Grid Content */}
      <div className="grid gap-6 lg:grid-cols-3 items-start">
        {/* Left Column: My Tasks */}
        <div className="lg:col-span-2 space-y-6">
          <section className="rounded-2xl border border-line/70 bg-white shadow-xs overflow-hidden">
            <header className="flex items-center justify-between border-b border-line/60 px-6 py-4">
              <h2 className="font-bold text-ink text-sm">Công việc của tôi</h2>
              {tasks && tasks.totalElements > 0 && (
                <span className="text-xs text-muted">Tổng cộng {tasks.totalElements} công việc</span>
              )}
            </header>

            <div className="overflow-x-auto">
              {tasks && tasks.content.length > 0 ? (
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
                    {tasks.content.map(task => (
                      <tr key={task.taskId} className="transition-colors hover:bg-slate-50/80">
                        <td className="px-6 py-3.5 font-semibold text-ink">
                          <div className="flex items-center gap-2">
                            <span className="size-1.5 rounded-full bg-brand shrink-0" />
                            <span className="truncate max-w-xs" title={task.title}>{task.title}</span>
                          </div>
                        </td>
                        <td className="px-5 py-3.5">
                          <span className="inline-flex items-center rounded-full bg-brand-cream/80 border border-brand-line/60 text-brand-dark px-2.5 py-0.5 text-[11px] font-bold">
                            {task.projectCode}
                          </span>
                        </td>
                        <td className="px-5 py-3.5">
                          <span className="inline-flex items-center rounded-md bg-slate-100 px-2 py-0.5 text-[11px] font-medium text-muted-dark">
                            {taskStatusLabels[task.status]}
                          </span>
                        </td>
                        <td className="px-5 py-3.5 text-muted-dark font-medium">
                          {taskPriorityLabels[task.priority]}
                        </td>
                        <td className="px-5 py-3.5">
                          {task.dueDate ? (
                            <span className={`font-semibold ${task.overdue ? 'text-rose-600 bg-rose-50 px-2 py-0.5 rounded' : task.dueSoon ? 'text-amber-600' : 'text-muted'}`}>
                              {new Date(task.dueDate).toLocaleDateString('vi-VN')}
                            </span>
                          ) : '-'}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              ) : (
                <div className="p-12 text-center text-muted text-xs">
                  <CheckCircle2 className="mx-auto text-muted/30 mb-2" size={32} />
                  Chưa có công việc nào được phân công.
                </div>
              )}
            </div>

            {tasks && tasks.totalPages > 1 && (
              <footer className="flex items-center justify-between border-t border-line/60 px-6 py-3 text-xs text-muted">
                <span>Trang {taskPage + 1} / {tasks.totalPages}</span>
                <div className="flex gap-2">
                  <Button variant="secondary" size="sm" disabled={tasks.first} leadingIcon={<ChevronLeft size={14} />} onClick={() => onTaskPageChange(taskPage - 1)}>Trước</Button>
                  <Button variant="secondary" size="sm" disabled={tasks.last} trailingIcon={<ChevronRight size={14} />} onClick={() => onTaskPageChange(taskPage + 1)}>Sau</Button>
                </div>
              </footer>
            )}
          </section>

          {/* Personal Task Risks Section */}
          {dashboard?.riskSummary && (
            <section className="rounded-2xl border border-rose-100 bg-white shadow-xs overflow-hidden">
              <header className="border-b border-rose-100 bg-rose-50/40 px-6 py-4 flex items-center justify-between">
                <h2 className="font-bold text-ink text-sm flex items-center gap-2">
                  <ShieldAlert size={18} className="text-rose-600" />
                  Rủi ro công việc cá nhân
                </h2>
                <span className="rounded-full bg-rose-100 text-rose-800 px-2.5 py-0.5 text-xs font-extrabold">
                  {dashboard.riskSummary.totalRiskTasks} RỦI RO
                </span>
              </header>

              <div className="p-6 space-y-5">
                {/* Risk Counter Tiles */}
                <div className="grid grid-cols-3 gap-3 text-center text-xs">
                  <div className="bg-rose-50/80 border border-rose-200/60 rounded-xl p-3">
                    <p className="text-rose-700 text-lg font-extrabold">{dashboard.riskSummary.criticalRiskTasks}</p>
                    <p className="text-rose-800/70 font-semibold text-[11px] uppercase tracking-wider mt-0.5">NGUY KỊCH</p>
                  </div>
                  <div className="bg-orange-50/80 border border-orange-200/60 rounded-xl p-3">
                    <p className="text-orange-700 text-lg font-extrabold">{dashboard.riskSummary.highRiskTasks}</p>
                    <p className="text-orange-800/70 font-semibold text-[11px] uppercase tracking-wider mt-0.5">CAO</p>
                  </div>
                  <div className="bg-amber-50/80 border border-amber-200/60 rounded-xl p-3">
                    <p className="text-amber-700 text-lg font-extrabold">{dashboard.riskSummary.mediumRiskTasks}</p>
                    <p className="text-amber-800/70 font-semibold text-[11px] uppercase tracking-wider mt-0.5">TRUNG BÌNH</p>
                  </div>
                </div>

                {/* Top Risk Tasks List */}
                <div className="space-y-2.5 pt-2 border-t border-line/60">
                  <p className="text-xs font-bold text-ink uppercase tracking-wider">Các Task rủi ro nhất:</p>
                  {dashboard.riskSummary.topRisks.map(risk => (
                    <div key={risk.taskId} className="bg-rose-50/30 border border-rose-100 rounded-xl p-3 text-xs space-y-1.5 transition hover:bg-rose-50/60">
                      <div className="flex justify-between items-center gap-2">
                        <span className="font-bold text-ink truncate flex-1" title={risk.title}>
                          {risk.title}
                        </span>
                        <span className="rounded-md bg-rose-600 text-white px-2 py-0.5 text-[10px] font-extrabold uppercase">
                          NGUY KỊCH
                        </span>
                      </div>
                      <p className="text-[11px] text-rose-700 leading-relaxed">
                        {risk.reasons.map(r => taskRiskReasonLabels[r] || r).join(', ')}
                      </p>
                    </div>
                  ))}
                  {dashboard.riskSummary.topRisks.length === 0 && (
                    <div className="text-center text-xs text-muted py-4 rounded-xl bg-emerald-50/50 border border-emerald-100 text-emerald-700">
                      <CheckCircle2 size={20} className="mx-auto mb-1 text-emerald-500" />
                      Tuyệt vời! Không phát hiện rủi ro công việc nào.
                    </div>
                  )}
                </div>
              </div>
            </section>
          )}
        </div>

        {/* Right Column: Time Summary Card */}
        <div className="space-y-6">
          {timeSummary && (
            <section className="rounded-2xl border border-line/70 bg-white shadow-xs p-6 space-y-6">
              <header className="flex items-center gap-2 border-b border-line/60 pb-4">
                <div className="rounded-lg bg-amber-50 p-2 text-amber-600 border border-amber-100">
                  <Clock size={18} />
                </div>
                <div>
                  <h2 className="font-bold text-ink text-sm">Thời gian làm việc</h2>
                  <p className="text-[11px] text-muted">Tổng hợp giờ làm cá nhân</p>
                </div>
              </header>

              <div className="space-y-3.5 text-xs">
                <div className="flex items-center justify-between rounded-xl bg-slate-50 p-3 border border-line/40">
                  <div className="flex items-center gap-2 text-muted-dark font-medium">
                    <CalendarClock size={16} className="text-muted" />
                    <span>Hôm nay</span>
                  </div>
                  <span className="font-extrabold text-sm text-ink">
                    {Math.floor(timeSummary.todayMinutes / 60)}h {timeSummary.todayMinutes % 60}m
                  </span>
                </div>

                <div className="flex items-center justify-between rounded-xl bg-slate-50 p-3 border border-line/40">
                  <div className="flex items-center gap-2 text-muted-dark font-medium">
                    <CalendarDays size={16} className="text-muted" />
                    <span>Tuần này</span>
                  </div>
                  <span className="font-extrabold text-sm text-ink">
                    {Math.floor(timeSummary.weekMinutes / 60)}h {timeSummary.weekMinutes % 60}m
                  </span>
                </div>

                <div className="flex items-center justify-between rounded-xl bg-amber-50/50 p-3 border border-amber-100">
                  <div className="flex items-center gap-2 text-amber-900 font-medium">
                    <CalendarDays size={16} className="text-amber-600" />
                    <span>Tháng này</span>
                  </div>
                  <span className="font-extrabold text-sm text-amber-700">
                    {Math.floor(timeSummary.monthMinutes / 60)}h {timeSummary.monthMinutes % 60}m
                  </span>
                </div>
              </div>

              {/* Progress Target Bar */}
              <div className="space-y-2 border-t border-line/60 pt-4">
                <div className="flex items-center justify-between text-[11px] font-bold">
                  <span className="text-muted-dark uppercase tracking-wider flex items-center gap-1">
                    <Target size={13} className="text-brand" /> TIẾN ĐỘ
                  </span>
                  <span className="text-brand-dark">{progressTargetPercent}% MỤC TIÊU</span>
                </div>
                <div className="h-2.5 w-full rounded-full bg-slate-100 overflow-hidden">
                  <div
                    className="h-full rounded-full bg-brand transition-all duration-500"
                    style={{ width: `${progressTargetPercent}%` }}
                  />
                </div>
              </div>
            </section>
          )}
        </div>
      </div>
    </div>
  )
}
