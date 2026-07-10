import { useState } from 'react'
import { Clock, CheckCircle2, AlertCircle, LayoutDashboard, CalendarDays, CalendarClock, ShieldAlert } from 'lucide-react'
import type { User } from '../../user/models/user.model'
import type { MyDashboardResponse, MyTaskPageResponse, MyTimeSummaryResponse } from '../models/dashboard.model'
import { Button } from '../../../components/ui'
import { taskStatusLabels, taskPriorityLabels, taskRiskLevelLabels, taskRiskReasonLabels } from '../../project/models/task.model'
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
      <div className="grid h-full place-items-center p-8">
        <span className="size-8 animate-spin rounded-full border-4 border-brand border-r-transparent" />
      </div>
    )
  }

  if (error) {
    return (
      <div className="p-8 text-center text-rose-600">
        <AlertCircle className="mx-auto mb-2 size-8" />
        <p>{error}</p>
      </div>
    )
  }

  if (isTimesheet) {
    return (
      <div className="p-6">
        <header className="mb-6 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
          <div>
            <h1 className="text-2xl font-bold text-ink">Timesheet cá nhân</h1>
            <p className="mt-1 text-sm text-muted">Quản lý và tổng hợp thời gian làm việc của {me.username}</p>
          </div>
          <div className="flex gap-2">
            <Button 
              variant="secondary" 
              className={isOverview ? '!bg-indigo-500 !text-white !border-transparent' : ''} 
              size="sm" 
              leadingIcon={<LayoutDashboard size={16} />}
              onClick={() => setActiveSubTab('overview')}
            >
              Tổng quan
            </Button>
            <Button 
              variant="secondary" 
              className={isTimesheet ? '!bg-brand !text-white !border-transparent' : ''} 
              size="sm" 
              leadingIcon={<Clock size={16} />}
              onClick={() => setActiveSubTab('timesheet')}
            >
              Timesheet cá nhân
            </Button>
          </div>
        </header>
        <TimesheetView mode="personal" />
      </div>
    )
  }

  return (
    <div className="p-6">
      <header className="mb-6 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-ink">Chào mừng, {me.username}!</h1>
          <p className="mt-1 text-sm text-muted">Dưới đây là tổng quan công việc của bạn</p>
        </div>
        <div className="flex gap-2">
          <Button 
            variant="secondary" 
            className={isOverview ? '!bg-indigo-500 !text-white !border-transparent' : ''} 
            size="sm" 
            leadingIcon={<LayoutDashboard size={16} />}
            onClick={() => setActiveSubTab('overview')}
          >
            Tổng quan
          </Button>
          <Button 
            variant="secondary" 
            className={isTimesheet ? '!bg-brand !text-white !border-transparent' : ''} 
            size="sm" 
            leadingIcon={<Clock size={16} />}
            onClick={() => setActiveSubTab('timesheet')}
          >
            Timesheet cá nhân
          </Button>
        </div>
      </header>

      {dashboard && (
        <div className="mb-8 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
          <div className="flex items-center gap-4 rounded-xl border border-line bg-white p-5 shadow-sm">
            <div className="grid size-12 place-items-center rounded-full bg-blue-100 text-blue-600"><LayoutDashboard size={24} /></div>
            <div><p className="text-sm font-medium text-muted">Dự án tham gia</p><p className="text-2xl font-bold">{dashboard.projectCount}</p></div>
          </div>
          <div className="flex items-center gap-4 rounded-xl border border-line bg-white p-5 shadow-sm">
            <div className="grid size-12 place-items-center rounded-full bg-orange-100 text-orange-600"><AlertCircle size={24} /></div>
            <div><p className="text-sm font-medium text-muted">Thông báo mới</p><p className="text-2xl font-bold">{dashboard.unreadNotifications}</p></div>
          </div>
          <div className="flex items-center gap-4 rounded-xl border border-line bg-white p-5 shadow-sm">
            <div className="grid size-12 place-items-center rounded-full bg-emerald-100 text-emerald-600"><CheckCircle2 size={24} /></div>
            <div><p className="text-sm font-medium text-muted">Task hoàn thành</p><p className="text-2xl font-bold">{dashboard.taskSummary.doneTasks}</p></div>
          </div>
          <div className="flex items-center gap-4 rounded-xl border border-line bg-white p-5 shadow-sm">
            <div className="grid size-12 place-items-center rounded-full bg-rose-100 text-rose-600"><Clock size={24} /></div>
            <div><p className="text-sm font-medium text-muted">Task trễ hạn</p><p className="text-2xl font-bold">{dashboard.taskSummary.overdueTasks}</p></div>
          </div>
        </div>
      )}

      <div className="grid gap-6 lg:grid-cols-3">
        <div className="lg:col-span-2">
          <section className="rounded-xl border border-line bg-white shadow-sm">
            <header className="flex items-center justify-between border-b border-line px-5 py-4">
              <h2 className="font-bold text-ink">Công việc của tôi</h2>
            </header>
            <div className="p-0">
              {tasks && tasks.content.length > 0 ? (
                <table className="w-full text-left text-sm">
                  <thead className="bg-canvas text-xs uppercase text-muted">
                    <tr>
                      <th className="px-5 py-3 font-semibold">Tên công việc</th>
                      <th className="px-5 py-3 font-semibold">Dự án</th>
                      <th className="px-5 py-3 font-semibold">Trạng thái</th>
                      <th className="px-5 py-3 font-semibold">Độ ưu tiên</th>
                      <th className="px-5 py-3 font-semibold">Hạn chót</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-line">
                    {tasks.content.map(task => (
                      <tr key={task.taskId} className="transition-colors hover:bg-canvas">
                        <td className="px-5 py-3 font-medium text-ink">{task.title}</td>
                        <td className="px-5 py-3 text-muted">{task.projectCode}</td>
                        <td className="px-5 py-3">{taskStatusLabels[task.status]}</td>
                        <td className="px-5 py-3">{taskPriorityLabels[task.priority]}</td>
                        <td className="px-5 py-3">
                          {task.dueDate ? (
                            <span className={task.overdue ? 'font-semibold text-rose-500' : task.dueSoon ? 'text-amber-500' : 'text-muted'}>
                              {new Date(task.dueDate).toLocaleDateString('vi-VN')}
                            </span>
                          ) : '-'}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              ) : (
                <div className="p-8 text-center text-muted">Chưa có công việc nào.</div>
              )}
            </div>
            {tasks && tasks.totalPages > 1 && (
              <footer className="flex items-center justify-between border-t border-line px-5 py-3">
                <span className="text-sm text-muted">Trang {taskPage + 1} / {tasks.totalPages}</span>
                <div className="flex gap-2">
                  <Button variant="secondary" size="sm" disabled={tasks.first} onClick={() => onTaskPageChange(taskPage - 1)}>Trước</Button>
                  <Button variant="secondary" size="sm" disabled={tasks.last} onClick={() => onTaskPageChange(taskPage + 1)}>Sau</Button>
                </div>
              </footer>
            )}
          </section>
        </div>

        <div>
          {timeSummary && (
            <section className="rounded-xl border border-line bg-white shadow-sm mb-6">
              <header className="border-b border-line px-5 py-4">
                <h2 className="font-bold text-ink">Thời gian làm việc</h2>
              </header>
              <div className="p-5">
                <div className="space-y-4">
                  <div className="flex items-center justify-between border-b border-line pb-3">
                    <div className="flex items-center gap-2 text-muted"><CalendarClock size={18} /> Hôm nay</div>
                    <div className="font-bold">{Math.floor(timeSummary.todayMinutes / 60)}h {timeSummary.todayMinutes % 60}m</div>
                  </div>
                  <div className="flex items-center justify-between border-b border-line pb-3">
                    <div className="flex items-center gap-2 text-muted"><CalendarDays size={18} /> Tuần này</div>
                    <div className="font-bold">{Math.floor(timeSummary.weekMinutes / 60)}h {timeSummary.weekMinutes % 60}m</div>
                  </div>
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-2 text-muted"><CalendarDays size={18} /> Tháng này</div>
                    <div className="font-bold">{Math.floor(timeSummary.monthMinutes / 60)}h {timeSummary.monthMinutes % 60}m</div>
                  </div>
                </div>
              </div>
            </section>
          )}

          {dashboard?.riskSummary && (
            <section className="rounded-xl border border-line bg-white shadow-sm mt-6">
              <header className="border-b border-line px-5 py-4 flex items-center justify-between">
                <h2 className="font-bold text-ink flex items-center gap-2">
                  <ShieldAlert size={18} className="text-rose-600" /> Rủi ro công việc cá nhân
                </h2>
                <span className="rounded bg-rose-100 text-rose-800 px-2 py-0.5 text-xs font-bold">
                  {dashboard.riskSummary.totalRiskTasks} rủi ro
                </span>
              </header>
              <div className="p-5 space-y-4">
                {/* Stats list */}
                <div className="grid grid-cols-3 gap-2 text-center text-xs">
                  <div className="bg-rose-50/50 border border-rose-100 rounded-lg p-2">
                    <p className="text-rose-700 font-bold">{dashboard.riskSummary.criticalRiskTasks}</p>
                    <p className="text-muted mt-0.5 scale-90">Critical</p>
                  </div>
                  <div className="bg-orange-50/50 border border-orange-100 rounded-lg p-2">
                    <p className="text-orange-700 font-bold">{dashboard.riskSummary.highRiskTasks}</p>
                    <p className="text-muted mt-0.5 scale-90">High</p>
                  </div>
                  <div className="bg-amber-50/50 border border-amber-100 rounded-lg p-2">
                    <p className="text-amber-700 font-bold">{dashboard.riskSummary.mediumRiskTasks}</p>
                    <p className="text-muted mt-0.5 scale-90">Medium</p>
                  </div>
                </div>

                {/* Top risks lists */}
                <div className="space-y-2 pt-2 border-t border-line">
                  <p className="text-xs font-semibold text-ink">Các Task rủi ro nhất:</p>
                  {dashboard.riskSummary.topRisks.map(risk => (
                    <div key={risk.taskId} className="bg-canvas border border-line rounded-lg p-2 text-xs">
                      <div className="flex justify-between items-center gap-2">
                        <span className="font-semibold text-ink truncate flex-1" title={risk.title}>
                          {risk.title}
                        </span>
                        <span className={`rounded-full px-1.5 py-0.2 font-bold ${
                          risk.riskLevel === 'CRITICAL' ? 'bg-red-100 text-red-700' :
                          risk.riskLevel === 'HIGH' ? 'bg-orange-100 text-orange-700' : 'bg-amber-100 text-amber-700'
                        }`}>
                          {taskRiskLevelLabels[risk.riskLevel]}
                        </span>
                      </div>
                      <p className="text-[10px] text-rose-700 mt-1">
                        {risk.reasons.map(r => taskRiskReasonLabels[r] || r).join(', ')}
                      </p>
                    </div>
                  ))}
                  {dashboard.riskSummary.topRisks.length === 0 && (
                    <p className="text-center text-xs text-muted py-2">Tuyệt vời! Không có rủi ro nào được tìm thấy.</p>
                  )}
                </div>
              </div>
            </section>
          )}
        </div>
      </div>
    </div>
  )
}
