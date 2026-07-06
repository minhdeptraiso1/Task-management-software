import { Clock, CheckCircle2, AlertCircle, LayoutDashboard, CalendarDays, CalendarClock } from 'lucide-react'
import type { User } from '../../user/models/user.model'
import type { MyDashboardResponse, MyTaskPageResponse, MyTimeSummaryResponse } from '../models/dashboard.model'
import { Button } from '../../../components/ui'
import { taskStatusLabels, taskPriorityLabels } from '../../project/models/task.model'

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

  return (
    <div className="p-6">
      <header className="mb-6">
        <h1 className="text-2xl font-bold text-ink">Chào mừng, {me.username}!</h1>
        <p className="mt-1 text-sm text-muted">Dưới đây là tổng quan công việc của bạn</p>
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
        </div>
      </div>
    </div>
  )
}
