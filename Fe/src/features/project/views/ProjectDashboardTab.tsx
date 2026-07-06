import { useEffect, useState } from 'react'
import type { ProjectDashboardResponse } from '../../dashboard/models/dashboard.model'
import { getProjectDashboard } from '../../dashboard/services/dashboard.service'
import { getMemberReport, getTimeReport, getSprintReport } from '../services/report.service'
import type { ProjectMemberReportResponse, ProjectTimeReportResponse, SprintReportResponse } from '../models/report.model'
import { AlertCircle, Clock, Users, BarChart3 } from 'lucide-react'
import { Select } from '../../../components/ui'
import { SprintStatisticsView } from '../components/SprintStatisticsView'
import type { Sprint } from '../models/scrum.model'
import type { SprintTaskStatistics, SprintBurndown } from '../models/task.model'

export function ProjectDashboardTab({ projectId, sprints }: { projectId: string; sprints: Sprint[] }) {
  const [dashboard, setDashboard] = useState<ProjectDashboardResponse | null>(null)
  const [memberReport, setMemberReport] = useState<ProjectMemberReportResponse | null>(null)
  const [timeReport, setTimeReport] = useState<ProjectTimeReportResponse | null>(null)
  const [sprintReport, setSprintReport] = useState<SprintReportResponse | null>(null)
  
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [selectedSprintId, setSelectedSprintId] = useState<string>('')

  useEffect(() => {
    setLoading(true)
    setError('')
    Promise.all([
      getProjectDashboard(projectId),
      getMemberReport(projectId),
      getTimeReport(projectId)
    ])
      .then(([dash, members, time]) => {
        setDashboard(dash)
        setMemberReport(members)
        setTimeReport(time)
      })
      .catch(err => setError(err instanceof Error ? err.message : 'Lỗi tải dữ liệu báo cáo'))
      .finally(() => setLoading(false))
  }, [projectId])

  useEffect(() => {
    if (selectedSprintId) {
      getSprintReport(projectId, selectedSprintId)
        .then(setSprintReport)
        .catch(console.error)
    } else {
      setSprintReport(null)
    }
  }, [projectId, selectedSprintId])

  if (loading) {
    return <div className="grid h-64 place-items-center"><span className="size-8 animate-spin rounded-full border-4 border-brand border-r-transparent" /></div>
  }

  if (error || !dashboard || !memberReport || !timeReport) {
    return <div className="p-8 text-center text-rose-600"><AlertCircle className="mx-auto mb-2 size-8" /><p>{error}</p></div>
  }

  const { taskSummary, backlogSummary } = dashboard

  return (
    <div className="space-y-6">
      {/* 1. Tổng quan cơ bản */}
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <div className="rounded-xl border border-line bg-white p-5 shadow-sm">
          <p className="text-sm font-medium text-muted">Tổng số Task</p>
          <p className="mt-1 text-2xl font-bold">{taskSummary.totalTasks}</p>
        </div>
        <div className="rounded-xl border border-line bg-white p-5 shadow-sm">
          <p className="text-sm font-medium text-muted">Task hoàn thành</p>
          <p className="mt-1 text-2xl font-bold text-emerald-600">{taskSummary.completedTasks}</p>
        </div>
        <div className="rounded-xl border border-line bg-white p-5 shadow-sm">
          <p className="text-sm font-medium text-muted">Task trễ hạn</p>
          <p className="mt-1 text-2xl font-bold text-rose-600">{taskSummary.overdueTasks}</p>
        </div>
        <div className="rounded-xl border border-line bg-white p-5 shadow-sm">
          <p className="text-sm font-medium text-muted">Hoàn thành (%)</p>
          <p className="mt-1 text-2xl font-bold text-blue-600">{taskSummary.completionRate.toFixed(1)}%</p>
        </div>
      </div>

      <div className="grid gap-6 lg:grid-cols-2">
        <section className="rounded-xl border border-line bg-white shadow-sm">
          <header className="border-b border-line px-5 py-4 font-bold text-ink">Tiến độ Backlog</header>
          <div className="p-5">
            <div className="flex justify-between border-b border-line py-2"><span className="text-muted">Tổng số Story Points:</span><span className="font-semibold">{backlogSummary.totalStoryPoints}</span></div>
            <div className="flex justify-between border-b border-line py-2"><span className="text-muted">Item trong Sprint:</span><span className="font-semibold">{backlogSummary.inSprintItems}</span></div>
            <div className="flex justify-between border-b border-line py-2"><span className="text-muted">Item hoàn thành:</span><span className="font-semibold text-emerald-600">{backlogSummary.doneItems}</span></div>
          </div>
        </section>
        <section className="rounded-xl border border-line bg-white shadow-sm">
          <header className="border-b border-line px-5 py-4 font-bold text-ink flex justify-between items-center">
            <span>Tổng quan Thời gian (Phút)</span>
            <Clock size={18} className="text-muted" />
          </header>
          <div className="p-5 grid grid-cols-2 gap-4 text-center">
            <div className="rounded-lg bg-canvas p-4">
              <p className="text-sm text-muted">Thời gian ước tính</p>
              <p className="text-xl font-bold mt-1">{timeReport.totalEstimatedMinutes}m</p>
            </div>
            <div className="rounded-lg bg-canvas p-4">
              <p className="text-sm text-muted">Thời gian đã tiêu tốn</p>
              <p className="text-xl font-bold mt-1 text-blue-600">{timeReport.totalMinutes}m</p>
            </div>
            <div className="rounded-lg bg-canvas p-4">
              <p className="text-sm text-muted">Số lượt log</p>
              <p className="text-xl font-bold mt-1">{timeReport.totalLogs}</p>
            </div>
            <div className="rounded-lg bg-canvas p-4">
              <p className="text-sm text-muted">Tỉ lệ sử dụng</p>
              <p className="text-xl font-bold mt-1 text-purple-600">{timeReport.timeUsageRate.toFixed(1)}%</p>
            </div>
          </div>
        </section>
      </div>

      {/* 2. Báo cáo Nhân sự Chi tiết */}
      <section className="rounded-xl border border-line bg-white shadow-sm">
        <header className="border-b border-line px-5 py-4 font-bold text-ink flex items-center gap-2">
          <Users size={18} className="text-brand" /> Báo cáo Thành viên
        </header>
        <div className="p-0 overflow-x-auto">
          <table className="w-full text-center text-sm whitespace-nowrap">
            <thead className="bg-canvas text-xs uppercase text-muted">
              <tr>
                <th className="px-5 py-3">Thành viên</th>
                <th className="px-5 py-3">Vai trò</th>
                <th className="px-5 py-3">Tổng Task</th>
                <th className="px-5 py-3">Hoàn thành</th>
                <th className="px-5 py-3">Đang làm</th>
                <th className="px-5 py-3">Trễ hạn</th>
                <th className="px-5 py-3">Hoàn thành (%)</th>
                <th className="px-5 py-3">Ước tính / Đã làm</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-line">
              {memberReport.members.map(m => (
                <tr key={m.userId}>
                  <td className="px-5 py-3 font-medium">{m.username}</td>
                  <td className="px-5 py-3">{m.projectRole}</td>
                  <td className="px-5 py-3">{m.totalTasks}</td>
                  <td className="px-5 py-3 text-emerald-600">{m.doneTasks}</td>
                  <td className="px-5 py-3 text-blue-600">{m.activeTasks}</td>
                  <td className="px-5 py-3 text-rose-600">{m.overdueTasks}</td>
                  <td className="px-5 py-3 font-semibold text-emerald-600">{m.completionRate.toFixed(1)}%</td>
                  <td className="px-5 py-3">{m.estimatedMinutes}m / {m.spentMinutes}m</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>

      {/* 3. Báo cáo Thời gian theo ngày/thành viên/task */}
      <div className="space-y-6">
        <div className="grid gap-6 lg:grid-cols-2">
          <section className="rounded-xl border border-line bg-white shadow-sm">
            <header className="border-b border-line px-5 py-4 font-bold text-ink">Thời gian log theo Ngày</header>
            <div className="p-0 overflow-x-auto max-h-[300px] overflow-y-auto">
              <table className="w-full text-center text-sm">
                <thead className="bg-canvas text-xs uppercase text-muted sticky top-0">
                  <tr><th className="px-5 py-3">Ngày</th><th className="px-5 py-3">Thời gian (Phút)</th><th className="px-5 py-3">Số lượt log</th></tr>
                </thead>
                <tbody className="divide-y divide-line">
                  {timeReport.byDate.map(d => (
                    <tr key={d.workDate}>
                      <td className="px-5 py-3">{d.workDate}</td>
                      <td className="px-5 py-3 font-medium text-blue-600">{d.spentMinutes}m</td>
                      <td className="px-5 py-3">{d.logCount}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </section>

          <section className="rounded-xl border border-line bg-white shadow-sm">
            <header className="border-b border-line px-5 py-4 font-bold text-ink">Thời gian log theo Thành viên</header>
            <div className="p-0 overflow-x-auto max-h-[300px] overflow-y-auto">
              <table className="w-full text-center text-sm">
                <thead className="bg-canvas text-xs uppercase text-muted sticky top-0">
                  <tr><th className="px-5 py-3">Thành viên</th><th className="px-5 py-3">Thời gian (Phút)</th><th className="px-5 py-3">% Tỉ trọng</th></tr>
                </thead>
                <tbody className="divide-y divide-line">
                  {timeReport.byMember.map(m => (
                    <tr key={m.userId}>
                      <td className="px-5 py-3 font-medium">{m.username}</td>
                      <td className="px-5 py-3 font-medium text-blue-600">{m.spentMinutes}m</td>
                      <td className="px-5 py-3">
                        <div className="flex items-center justify-center gap-2">
                          <span className="w-10">{m.percentage.toFixed(1)}%</span>
                          <div className="h-1.5 w-16 bg-slate-100 rounded-full overflow-hidden">
                            <div className="h-full bg-brand" style={{ width: `${m.percentage}%` }} />
                          </div>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </section>
        </div>

        {/* Thời gian theo Task */}
        <section className="rounded-xl border border-line bg-white shadow-sm">
          <header className="border-b border-line px-5 py-4 font-bold text-ink">Thời gian log theo Task</header>
          <div className="p-0 overflow-x-auto max-h-[400px] overflow-y-auto">
            <table className="w-full text-center text-sm whitespace-nowrap">
              <thead className="bg-canvas text-xs uppercase text-muted sticky top-0">
                <tr>
                  <th className="px-5 py-3">Tên Task</th>
                  <th className="px-5 py-3">Trạng thái</th>
                  <th className="px-5 py-3">Lượt log</th>
                  <th className="px-5 py-3">Thành viên tham gia</th>
                  <th className="px-5 py-3">Ước tính / Đã làm</th>
                  <th className="px-5 py-3">Tỉ lệ sử dụng</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-line">
                {timeReport.byTask.map(t => (
                  <tr key={t.taskId}>
                    <td className="px-5 py-3 font-medium truncate max-w-[250px]" title={t.taskTitle}>{t.taskTitle}</td>
                    <td className="px-5 py-3">{t.taskStatus}</td>
                    <td className="px-5 py-3">{t.logCount}</td>
                    <td className="px-5 py-3">{t.contributorCount}</td>
                    <td className="px-5 py-3">{t.estimatedMinutes}m / {t.spentMinutes}m</td>
                    <td className={`px-5 py-3 font-semibold ${t.overEstimated ? 'text-rose-600' : 'text-emerald-600'}`}>
                      {t.timeUsageRate.toFixed(1)}%
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>
      </div>

      {/* 4. Báo cáo Sprint (Dưới cùng) */}
      <section className="rounded-xl border border-line bg-white shadow-sm p-5">
        <header className="mb-4 flex flex-col gap-4 sm:flex-row sm:items-center justify-between border-b border-line pb-4">
          <div className="flex items-center gap-2 text-lg font-bold text-ink">
            <BarChart3 size={20} className="text-brand" /> Báo cáo Sprint
          </div>
          <div className="flex items-center gap-3">
            <span className="text-sm font-medium text-muted whitespace-nowrap">Chọn Sprint</span>
            <Select 
              value={selectedSprintId} 
              onChange={e => setSelectedSprintId(e.target.value)}
              className="w-48 bg-white"
              options={[
                { label: '-- Chọn Sprint --', value: '' },
                ...sprints.map(s => ({ label: s.name, value: s.id }))
              ]}
            />
          </div>
        </header>

        {sprintReport ? (
          <div className="mt-4">
            <SprintStatisticsView 
              statistics={sprintReport.statistics as unknown as SprintTaskStatistics}
              burndown={sprintReport.burndown as unknown as SprintBurndown}
            />
          </div>
        ) : (
          <div className="py-8 text-center text-muted">Vui lòng chọn Sprint để xem báo cáo chi tiết</div>
        )}
      </section>

    </div>
  )
}
