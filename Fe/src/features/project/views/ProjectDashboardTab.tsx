import { useEffect, useState, useCallback } from 'react'
import type { ProjectDashboardResponse } from '../../dashboard/models/dashboard.model'
import { getProjectDashboard } from '../../dashboard/services/dashboard.service'
import { getMemberReport, getTimeReport, getSprintReport } from '../services/report.service'
import type { ProjectMemberReportResponse, ProjectTimeReportResponse, SprintReportResponse } from '../models/report.model'
import { AlertCircle, Clock, Users, BarChart3, ShieldAlert, RefreshCcw } from 'lucide-react'
import { Select, Button, Modal } from '../../../components/ui'
import { SprintStatisticsView } from '../components/SprintStatisticsView'
import type { Sprint } from '../models/scrum.model'
import type { SprintTaskStatistics, SprintBurndown, TaskRiskScan } from '../models/task.model'
import { taskStatusLabels, taskRiskLevelLabels, taskRiskReasonLabels } from '../models/task.model'
import { scanProjectRisks } from '../services/task.service'

export function ProjectDashboardTab({ projectId, sprints, onOpenTask }: { projectId: string; sprints: Sprint[]; onOpenTask?: (taskId: string) => void }) {
  const [dashboard, setDashboard] = useState<ProjectDashboardResponse | null>(null)
  const [memberReport, setMemberReport] = useState<ProjectMemberReportResponse | null>(null)
  const [timeReport, setTimeReport] = useState<ProjectTimeReportResponse | null>(null)
  const [sprintReport, setSprintReport] = useState<SprintReportResponse | null>(null)
  
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [selectedSprintId, setSelectedSprintId] = useState<string>('')
  const [scanResult, setScanResult] = useState<TaskRiskScan | null>(null)
  const [scanning, setScanning] = useState(false)

  const loadData = useCallback(async (showLoading = true) => {
    if (showLoading) setLoading(true)
    setError('')
    try {
      const [dash, members, time] = await Promise.all([
        getProjectDashboard(projectId),
        getMemberReport(projectId),
        getTimeReport(projectId)
      ])
      setDashboard(dash)
      setMemberReport(members)
      setTimeReport(time)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Lỗi tải dữ liệu báo cáo')
    } finally {
      if (showLoading) setLoading(false)
    }
  }, [projectId])

  useEffect(() => {
    void loadData(true)
  }, [loadData])

  const handleScanRisks = async () => {
    setScanning(true)
    try {
      const result = await scanProjectRisks(projectId)
      setScanResult(result)
      await loadData(false)
    } catch (err) {
      alert(err instanceof Error ? err.message : 'Lỗi khi quét rủi ro')
    } finally {
      setScanning(false)
    }
  }

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

      {/* Risk Summary Cards */}
      {dashboard.riskSummary && (
        <div className="grid gap-3 grid-cols-2 sm:grid-cols-4 lg:grid-cols-8">
          <div className="rounded-xl border border-red-100 bg-red-50/20 p-3 shadow-sm text-center">
            <p className="text-[11px] font-semibold text-red-800 uppercase tracking-wider">Nguy cấp</p>
            <p className="mt-1.5 text-xl font-extrabold text-red-600">{dashboard.riskSummary.criticalRiskTasks}</p>
          </div>
          <div className="rounded-xl border border-orange-100 bg-orange-50/20 p-3 shadow-sm text-center">
            <p className="text-[11px] font-semibold text-orange-800 uppercase tracking-wider">Rủi ro Cao</p>
            <p className="mt-1.5 text-xl font-extrabold text-orange-600">{dashboard.riskSummary.highRiskTasks}</p>
          </div>
          <div className="rounded-xl border border-amber-100 bg-amber-50/20 p-3 shadow-sm text-center">
            <p className="text-[11px] font-semibold text-amber-800 uppercase tracking-wider">Rủi ro Vừa</p>
            <p className="mt-1.5 text-xl font-extrabold text-amber-600">{dashboard.riskSummary.mediumRiskTasks}</p>
          </div>
          <div className="rounded-xl border border-blue-100 bg-blue-50/20 p-3 shadow-sm text-center">
            <p className="text-[11px] font-semibold text-blue-800 uppercase tracking-wider">Rủi ro Thấp</p>
            <p className="mt-1.5 text-xl font-extrabold text-blue-600">{dashboard.riskSummary.lowRiskTasks}</p>
          </div>
          <div className="rounded-xl border border-rose-100 bg-rose-50/20 p-3 shadow-sm text-center">
            <p className="text-[11px] font-semibold text-rose-800 uppercase tracking-wider">Quá hạn</p>
            <p className="mt-1.5 text-xl font-extrabold text-rose-600">{dashboard.riskSummary.overdueTasks}</p>
          </div>
          <div className="rounded-xl border border-yellow-100 bg-yellow-50/20 p-3 shadow-sm text-center">
            <p className="text-[11px] font-semibold text-yellow-800 uppercase tracking-wider">Sắp đến hạn</p>
            <p className="mt-1.5 text-xl font-extrabold text-yellow-600">{dashboard.riskSummary.dueSoonTasks}</p>
          </div>
          <div className="rounded-xl border border-pink-100 bg-pink-50/20 p-3 shadow-sm text-center">
            <p className="text-[11px] font-semibold text-pink-800 uppercase tracking-wider">Bị chặn</p>
            <p className="mt-1.5 text-xl font-extrabold text-pink-600">{dashboard.riskSummary.blockedTasks}</p>
          </div>
          <div className="rounded-xl border border-purple-100 bg-purple-50/20 p-3 shadow-sm text-center">
            <p className="text-[11px] font-semibold text-purple-800 uppercase tracking-wider">Phụ thuộc</p>
            <p className="mt-1.5 text-xl font-extrabold text-purple-600">{dashboard.riskSummary.dependencyRiskTasks}</p>
          </div>
        </div>
      )}

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

      {/* 4. Đánh giá Rủi ro Công việc */}
      <section className="rounded-xl border border-line bg-white shadow-sm">
        <header className="border-b border-line px-5 py-4 font-bold text-ink flex items-center justify-between gap-2">
          <div className="flex items-center gap-2">
            <ShieldAlert size={18} className="text-rose-600" /> Đánh giá Rủi ro Công việc (Task Risk Assessment)
          </div>
          <Button
            size="sm"
            variant="outline-amber"
            className="border-amber-500 text-amber-600 hover:bg-amber-50"
            onClick={handleScanRisks}
            loading={scanning}
            leadingIcon={<RefreshCcw size={14} />}
          >
            Quét rủi ro (Scan)
          </Button>
        </header>
        <div className="p-0 overflow-x-auto">
          <table className="w-full text-center text-sm whitespace-nowrap">
            <thead className="bg-canvas text-xs uppercase text-muted">
              <tr>
                <th className="px-5 py-3 text-left">Tên Task</th>
                <th className="px-5 py-3">Trạng thái</th>
                <th className="px-5 py-3">Mức độ rủi ro</th>
                <th className="px-5 py-3">Hạn chót (Deadline)</th>
                <th className="px-5 py-3 text-left">Lý do rủi ro</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-line">
              {dashboard.riskSummary?.topRisks?.map(t => (
                <tr key={t.taskId} className="hover:bg-slate-50/50 transition">
                  <td className="px-5 py-3 text-left font-medium max-w-[250px] truncate" title={t.title}>
                    {onOpenTask ? (
                      <button
                        type="button"
                        className="text-left font-medium text-brand hover:underline truncate max-w-[250px]"
                        onClick={() => onOpenTask(t.taskId)}
                      >
                        {t.title}
                      </button>
                    ) : (
                      <span>{t.title}</span>
                    )}
                  </td>
                  <td className="px-5 py-3">
                    <span className={`inline-block rounded-full px-2.5 py-0.5 text-xs font-semibold ${
                      t.status === 'DONE' ? 'bg-emerald-50 text-emerald-600' :
                      t.status === 'BLOCKED' ? 'bg-rose-50 text-rose-600' : 'bg-slate-100 text-slate-600'
                    }`}>
                      {taskStatusLabels[t.status] || t.status}
                    </span>
                  </td>
                  <td className="px-5 py-3">
                    <span className={`inline-block rounded-md px-2 py-0.5 text-xs font-bold ${
                      t.riskLevel === 'CRITICAL' ? 'bg-red-100 text-red-700 ring-1 ring-red-500/20' :
                      t.riskLevel === 'HIGH' ? 'bg-orange-100 text-orange-700 ring-1 ring-orange-500/20' :
                      t.riskLevel === 'MEDIUM' ? 'bg-amber-100 text-amber-700 ring-1 ring-amber-500/20' :
                      'bg-slate-100 text-slate-700'
                    }`}>
                      {taskRiskLevelLabels[t.riskLevel] || t.riskLevel}
                    </span>
                  </td>
                  <td className="px-5 py-3 text-muted">
                    {t.dueDate ? new Date(t.dueDate).toLocaleDateString('vi-VN') : '---'}
                  </td>
                  <td className="px-5 py-3 text-left max-w-[400px] truncate text-xs text-rose-700 font-medium" title={t.reasons.map(r => taskRiskReasonLabels[r] || r).join(', ')}>
                    {t.reasons.map(r => taskRiskReasonLabels[r] || r).join(', ') || '---'}
                  </td>
                </tr>
              ))}
              {(!dashboard.riskSummary || !dashboard.riskSummary.topRisks || dashboard.riskSummary.topRisks.length === 0) && (
                <tr>
                  <td colSpan={5} className="py-8 text-center text-muted">Không có rủi ro đáng kể nào được ghi nhận.</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </section>

      {/* 5. Báo cáo Sprint (Dưới cùng) */}
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

      <Modal open={Boolean(scanResult)} title="Kết quả quét rủi ro" onClose={() => setScanResult(null)} showClose={false}>
        <div className="space-y-4">
          <p className="text-sm text-muted">Hệ thống đã hoàn tất quét và phân tích rủi ro thủ công cho dự án:</p>
          {scanResult && (
            <div className="space-y-2 rounded-lg border border-line bg-canvas p-4 text-sm">
              <div className="flex justify-between border-b border-line pb-2">
                <span className="text-muted">Tổng số Task đã quét:</span>
                <span className="font-bold text-ink">{scanResult.scannedTasks}</span>
              </div>
              <div className="flex justify-between border-b border-line py-2">
                <span className="text-muted">Task phát hiện rủi ro:</span>
                <span className="font-bold text-rose-600">{scanResult.riskTasks}</span>
              </div>
              <div className="flex justify-between border-b border-line py-2">
                <span className="text-muted">Rủi ro High:</span>
                <span className="font-bold text-orange-600">{scanResult.highRiskTasks}</span>
              </div>
              <div className="flex justify-between border-b border-line py-2">
                <span className="text-muted">Rủi ro Critical:</span>
                <span className="font-bold text-red-600">{scanResult.criticalRiskTasks}</span>
              </div>
              <div className="flex justify-between border-b border-line py-2">
                <span className="text-muted">Thông báo rủi ro đã tạo:</span>
                <span className="font-bold text-indigo-600">{scanResult.notificationsCreated}</span>
              </div>
              <div className="flex justify-between pt-2 text-xs text-muted">
                <span>Thời gian quét:</span>
                <span>{new Date(scanResult.scannedAt).toLocaleString('vi-VN')}</span>
              </div>
            </div>
          )}
          <div className="flex justify-end">
            <Button onClick={() => setScanResult(null)}>Đóng</Button>
          </div>
        </div>
      </Modal>

    </div>
  )
}
