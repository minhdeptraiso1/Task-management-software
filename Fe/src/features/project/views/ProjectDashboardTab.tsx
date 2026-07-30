import { useEffect, useState, useCallback } from 'react'
import type { ProjectDashboardResponse } from '../../dashboard/models/dashboard.model'
import { getProjectDashboard } from '../../dashboard/services/dashboard.service'
import { getMemberReport, getTimeReport, getSprintReport, exportProjectExcelReport, exportSprintExcelReport, exportProjectPdfReport, exportSprintPdfReport } from '../services/report.service'
import { getProjectAnalyticsSummary, getProjectVelocity, getProjectBurnup, getSprintCumulativeFlow } from '../services/analytics.service'
import type { ProjectMemberReportResponse, ProjectTimeReportResponse, SprintReportResponse } from '../models/report.model'
import type { ProjectAnalyticsSummaryResponse, VelocityChartResponse, BurnupChartResponse, CumulativeFlowResponse } from '../models/analytics.model'
import { AlertCircle, Clock, Users, BarChart3, ShieldAlert, RefreshCcw, CheckCircle2, AlertTriangle, ListTodo, Bell, FileSpreadsheet, Download, FileText, TrendingUp, Layers, Activity as ActivityIcon } from 'lucide-react'
import { Select, Button, Modal, Input, toast } from '../../../components/ui'
import { SprintStatisticsView } from '../components/SprintStatisticsView'
import type { Sprint } from '../models/scrum.model'
import type { SprintTaskStatistics, SprintBurndown, TaskRiskScan } from '../models/task.model'
import { taskStatusLabels, taskRiskLevelLabels, taskRiskReasonLabels } from '../models/task.model'
import { scanProjectRisks } from '../services/task.service'
import { formatShortDate } from '../../../utils/format'
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Cell, LabelList, LineChart, Line
} from 'recharts'

export function ProjectDashboardTab({ projectId, sprints, onOpenTask }: { projectId: string; sprints: Sprint[]; onOpenTask?: (taskId: string) => void }) {
  const [dashboard, setDashboard] = useState<ProjectDashboardResponse | null>(null)
  const [memberReport, setMemberReport] = useState<ProjectMemberReportResponse | null>(null)
  const [timeReport, setTimeReport] = useState<ProjectTimeReportResponse | null>(null)
  const [sprintReport, setSprintReport] = useState<SprintReportResponse | null>(null)

  const [analyticsSummary, setAnalyticsSummary] = useState<ProjectAnalyticsSummaryResponse | null>(null)
  const [velocityData, setVelocityData] = useState<VelocityChartResponse | null>(null)
  const [burnupData, setBurnupData] = useState<BurnupChartResponse | null>(null)
  const [cumulativeFlowData, setCumulativeFlowData] = useState<CumulativeFlowResponse | null>(null)
  
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [selectedSprintId, setSelectedSprintId] = useState<string>('')
  const [scanResult, setScanResult] = useState<TaskRiskScan | null>(null)
  const [scanning, setScanning] = useState(false)
  const [hiddenRiskKeys, setHiddenRiskKeys] = useState<string[]>([])
  const [hiddenVelocityBars, setHiddenVelocityBars] = useState<string[]>([])
  const [hiddenBurnupLines, setHiddenBurnupLines] = useState<string[]>([])
  const [hiddenCfdAreas, setHiddenCfdAreas] = useState<string[]>([])

  const toggleVelocityBar = (key: string) => {
    setHiddenVelocityBars(prev =>
      prev.includes(key) ? prev.filter((itemKey: string) => itemKey !== key) : [...prev, key]
    )
  }

  const toggleBurnupLine = (key: string) => {
    setHiddenBurnupLines(prev =>
      prev.includes(key) ? prev.filter((itemKey: string) => itemKey !== key) : [...prev, key]
    )
  }

  const toggleCfdArea = (key: string) => {
    setHiddenCfdAreas(prev =>
      prev.includes(key) ? prev.filter((itemKey: string) => itemKey !== key) : [...prev, key]
    )
  }
  
  const [exportModalOpen, setExportModalOpen] = useState(false)
  const [exportFilters, setExportFilters] = useState<{
    fromDate: string
    toDate: string
    sprintId: string
    userId: string
  }>({
    fromDate: '',
    toDate: '',
    sprintId: '',
    userId: '',
  })
  const [exportingProject, setExportingProject] = useState(false)
  const [exportingProjectPdf, setExportingProjectPdf] = useState(false)
  const [exportingSprint, setExportingSprint] = useState(false)
  const [exportingSprintPdf, setExportingSprintPdf] = useState(false)

  const toggleRiskVisibility = (key: string) => {
    setHiddenRiskKeys(prev => 
      prev.includes(key) ? prev.filter(k => k !== key) : [...prev, key]
    )
  }

  const loadData = useCallback(async (showLoading = true) => {
    if (showLoading) setLoading(true)
    setError('')
    try {
      const [dash, members, time, analytics, vel, burnup] = await Promise.all([
        getProjectDashboard(projectId),
        getMemberReport(projectId),
        getTimeReport(projectId),
        getProjectAnalyticsSummary(projectId).catch(() => null),
        getProjectVelocity(projectId).catch(() => null),
        getProjectBurnup(projectId).catch(() => null)
      ])
      setDashboard(dash)
      setMemberReport(members)
      setTimeReport(time)
      setAnalyticsSummary(analytics)
      setVelocityData(vel)
      setBurnupData(burnup)
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
      toast.error(err instanceof Error ? err.message : 'Lỗi khi quét rủi ro')
    } finally {
      setScanning(false)
    }
  }

  const handleExportProjectExcel = async () => {
    setExportingProject(true)
    try {
      await exportProjectExcelReport(projectId, exportFilters)
      toast.success('Đã xuất báo cáo Excel dự án thành công!')
      setExportModalOpen(false)
    } catch (err) {
      toast.error(err instanceof Error ? err.message : 'Lỗi khi xuất báo cáo Excel dự án')
    } finally {
      setExportingProject(false)
    }
  }

  const handleExportSprintExcel = async () => {
    if (!selectedSprintId) return
    setExportingSprint(true)
    try {
      await exportSprintExcelReport(projectId, selectedSprintId)
      toast.success('Đã xuất báo cáo Excel Sprint thành công!')
    } catch (err) {
      toast.error(err instanceof Error ? err.message : 'Lỗi khi xuất báo cáo Excel Sprint')
    } finally {
      setExportingSprint(false)
    }
  }

  const handleExportProjectPdf = async () => {
    setExportingProjectPdf(true)
    try {
      await exportProjectPdfReport(projectId, exportFilters)
      toast.success('Đã xuất báo cáo PDF dự án thành công!')
      setExportModalOpen(false)
    } catch (err) {
      toast.error(err instanceof Error ? err.message : 'Lỗi khi xuất báo cáo PDF dự án')
    } finally {
      setExportingProjectPdf(false)
    }
  }

  const handleExportSprintPdf = async () => {
    if (!selectedSprintId) return
    setExportingSprintPdf(true)
    try {
      await exportSprintPdfReport(projectId, selectedSprintId)
      toast.success('Đã xuất báo cáo PDF Sprint thành công!')
    } catch (err) {
      toast.error(err instanceof Error ? err.message : 'Lỗi khi xuất báo cáo PDF Sprint')
    } finally {
      setExportingSprintPdf(false)
    }
  }

  useEffect(() => {
    if (selectedSprintId) {
      Promise.all([
        getSprintReport(projectId, selectedSprintId),
        getSprintCumulativeFlow(projectId, selectedSprintId).catch(() => null)
      ])
        .then(([sRep, cfd]) => {
          setSprintReport(sRep)
          setCumulativeFlowData(cfd)
        })
        .catch(console.error)
    } else {
      setSprintReport(null)
      setCumulativeFlowData(null)
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
      {/* Header Action Bar: Xuất Báo Cáo Excel */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 bg-white p-4 rounded-xl border border-line shadow-xs">
        <div>
          <h3 className="font-extrabold text-base text-ink flex items-center gap-2">
            Báo cáo & Export Dự án
          </h3>
          <p className="text-xs text-muted font-medium mt-0.5">Xuất dữ liệu Excel (.xlsx) / PDF (.pdf) báo cáo đa chiều.</p>
        </div>
        <div className="flex items-center gap-2.5 shrink-0">
          <Button
            variant="secondary"
            size="sm"
            onClick={() => setExportModalOpen(true)}
            leadingIcon={<FileSpreadsheet size={16} className="text-emerald-600" />}
            className="!px-3.5 font-bold text-xs"
          >
            Xuất Báo Cáo (Excel / PDF)
          </Button>
        </div>
      </div>

      {/* 1. Tổng quan cơ bản & Analytics Summary */}
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <div className="rounded-xl border border-line bg-white p-5 shadow-sm">
          <p className="text-sm font-medium text-muted">Tổng số Task</p>
          <p className="mt-1 text-2xl font-bold">{analyticsSummary?.totalTasks ?? taskSummary.totalTasks}</p>
        </div>
        <div className="rounded-xl border border-line bg-white p-5 shadow-sm">
          <p className="text-sm font-medium text-muted">Task hoàn thành</p>
          <p className="mt-1 text-2xl font-bold text-emerald-600">{analyticsSummary?.completedTasks ?? taskSummary.completedTasks}</p>
        </div>
        <div className="rounded-xl border border-line bg-white p-5 shadow-sm">
          <p className="text-sm font-medium text-muted">Task trễ hạn / Blocked</p>
          <div className="flex items-baseline gap-2 mt-1">
            <span className="text-2xl font-bold text-rose-600">{analyticsSummary?.overdueTasks ?? taskSummary.overdueTasks}</span>
            <span className="text-xs text-muted font-semibold">({analyticsSummary?.blockedTasks ?? 0} nghẽn)</span>
          </div>
        </div>
        <div className="rounded-xl border border-line bg-white p-5 shadow-sm">
          <p className="text-sm font-medium text-muted">Hoàn thành (%)</p>
          <p className="mt-1 text-2xl font-bold text-blue-600">{(analyticsSummary?.taskCompletionRate ?? taskSummary.completionRate).toFixed(1)}%</p>
        </div>
      </div>

      {/* Analytics Charts Grid: Velocity & Burnup */}
      <div className="grid gap-6 lg:grid-cols-2">
        {/* Velocity Chart */}
        <section className="rounded-xl border border-line bg-white p-5 shadow-sm">
          <div className="flex items-center justify-between border-b border-line pb-3 mb-4">
            <h3 className="font-bold text-base text-ink flex items-center gap-2">
              <TrendingUp size={18} className="text-brand" /> Biểu đồ Velocity
            </h3>
            {velocityData && (
              <span className="text-xs font-semibold text-slate-500">
                TB Hoàn thành: <span className="font-extrabold text-brand">{velocityData.averageCompletedStoryPoints} pt</span> ({velocityData.averageCompletedItems} mục)
              </span>
            )}
          </div>
          <div className="h-64 w-full">
            {velocityData && velocityData.points.length > 0 ? (
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={velocityData.points} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                  <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e2e8f0" />
                  <XAxis dataKey="sprintName" tick={{ fontSize: 11, fontWeight: 700 }} axisLine={false} tickLine={false} />
                  <YAxis tick={{ fontSize: 11 }} axisLine={false} tickLine={false} />
                  <Tooltip
                    contentStyle={{ borderRadius: '12px', border: '1px solid #e2e8f0', boxShadow: '0 4px 12px rgba(0,0,0,0.08)' }}
                    formatter={(value: any, name: any) => [
                      `${value} pts`,
                      name === 'committedStoryPoints' ? 'Cam kết' : 'Hoàn thành'
                    ]}
                  />
                  {!hiddenVelocityBars.includes('committedStoryPoints') && (
                    <Bar dataKey="committedStoryPoints" fill="#cbd5e1" radius={[4, 4, 0, 0]} name="committedStoryPoints" />
                  )}
                  {!hiddenVelocityBars.includes('completedStoryPoints') && (
                    <Bar dataKey="completedStoryPoints" fill="#f7941d" radius={[4, 4, 0, 0]} name="completedStoryPoints" />
                  )}
                </BarChart>
              </ResponsiveContainer>
            ) : (
              <div className="flex h-full items-center justify-center text-xs font-medium text-slate-400 bg-slate-50 rounded-xl border border-dashed">Chưa có dữ liệu Velocity Sprint</div>
            )}
          </div>
          {velocityData && velocityData.points.length > 0 && (
            <div className="flex items-center justify-center gap-3 pt-3 border-t border-line mt-3">
              {[
                { key: 'committedStoryPoints', name: 'Cam kết', color: '#cbd5e1' },
                { key: 'completedStoryPoints', name: 'Hoàn thành', color: '#f7941d' }
              ].map(item => {
                const isHidden = hiddenVelocityBars.includes(item.key)
                return (
                  <button
                    key={item.key}
                    type="button"
                    onClick={() => toggleVelocityBar(item.key)}
                    className={`flex items-center gap-2 px-3 py-1 rounded-full text-xs font-bold transition-all border cursor-pointer ${
                      isHidden 
                        ? 'bg-slate-100 text-slate-400 border-slate-200 line-through opacity-50' 
                        : 'bg-white text-slate-700 border-slate-200 shadow-2xs hover:scale-105 hover:shadow-xs'
                    }`}
                    title={isHidden ? 'Bấm để hiển thị' : 'Bấm để ẩn'}
                  >
                    <span className="size-2.5 rounded-full shrink-0 transition-transform duration-200" style={{ backgroundColor: isHidden ? '#cbd5e1' : item.color }} />
                    <span>{item.name}</span>
                  </button>
                )
              })}
            </div>
          )}
        </section>

        {/* Burnup Chart */}
        <section className="rounded-xl border border-line bg-white p-5 shadow-sm">
          <div className="flex items-center justify-between border-b border-line pb-3 mb-4">
            <h3 className="font-bold text-base text-ink flex items-center gap-2">
              <ActivityIcon size={18} className="text-indigo-600" /> Biểu đồ Burnup
            </h3>
          </div>
          <div className="h-64 w-full">
            {burnupData && burnupData.points.length > 0 ? (
              <ResponsiveContainer width="100%" height="100%">
                <LineChart data={burnupData.points} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                  <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e2e8f0" />
                  <XAxis dataKey="date" tick={{ fontSize: 10 }} tickFormatter={val => formatShortDate(val)} axisLine={false} tickLine={false} />
                  <YAxis tick={{ fontSize: 11 }} axisLine={false} tickLine={false} />
                  <Tooltip
                    contentStyle={{ borderRadius: '12px', border: '1px solid #e2e8f0', boxShadow: '0 4px 12px rgba(0,0,0,0.08)' }}
                    labelFormatter={val => `Ngày: ${formatShortDate(val)}`}
                    formatter={(value: any, name: any) => [
                      `${value} tasks`,
                      name === 'totalScope' ? 'Tổng phạm vi' : 'Đã hoàn thành'
                    ]}
                  />
                  {!hiddenBurnupLines.includes('totalScope') && (
                    <Line type="monotone" dataKey="totalScope" stroke="#94a3b8" strokeWidth={2} dot={false} name="totalScope" />
                  )}
                  {!hiddenBurnupLines.includes('completedScope') && (
                    <Line type="monotone" dataKey="completedScope" stroke="#10b981" strokeWidth={2.5} dot={false} name="completedScope" />
                  )}
                </LineChart>
              </ResponsiveContainer>
            ) : (
              <div className="flex h-full items-center justify-center text-xs font-medium text-slate-400 bg-slate-50 rounded-xl border border-dashed">Chưa có dữ liệu Burnup Chart</div>
            )}
          </div>
          {burnupData && burnupData.points.length > 0 && (
            <div className="flex items-center justify-center gap-3 pt-3 border-t border-line mt-3">
              {[
                { key: 'totalScope', name: 'Tổng phạm vi', color: '#94a3b8' },
                { key: 'completedScope', name: 'Đã hoàn thành', color: '#10b981' }
              ].map(item => {
                const isHidden = hiddenBurnupLines.includes(item.key)
                return (
                  <button
                    key={item.key}
                    type="button"
                    onClick={() => toggleBurnupLine(item.key)}
                    className={`flex items-center gap-2 px-3 py-1 rounded-full text-xs font-bold transition-all border cursor-pointer ${
                      isHidden 
                        ? 'bg-slate-100 text-slate-400 border-slate-200 line-through opacity-50' 
                        : 'bg-white text-slate-700 border-slate-200 shadow-2xs hover:scale-105 hover:shadow-xs'
                    }`}
                    title={isHidden ? 'Bấm để hiển thị' : 'Bấm để ẩn'}
                  >
                    <span className="size-2.5 rounded-full shrink-0 transition-transform duration-200" style={{ backgroundColor: isHidden ? '#cbd5e1' : item.color }} />
                    <span>{item.name}</span>
                  </button>
                )
              })}
            </div>
          )}
        </section>
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

      {/* 4. Đánh giá Rủi ro Công việc (Task Risk Assessment) - Hình 1 và Hình 2 gộp chung trong 1 khung */}
      {(() => {
        const riskChartData = dashboard.riskSummary ? [
          { key: 'CRITICAL', name: 'NGUY CẤP', value: dashboard.riskSummary.criticalRiskTasks, fill: '#ef4444', border: 'border-red-100', bg: 'bg-red-50/40', text: 'text-red-700' },
          { key: 'HIGH', name: 'RỦI RO CAO', value: dashboard.riskSummary.highRiskTasks, fill: '#f97316', border: 'border-orange-100', bg: 'bg-orange-50/40', text: 'text-orange-700' },
          { key: 'MEDIUM', name: 'RỦI RO VỪA', value: dashboard.riskSummary.mediumRiskTasks, fill: '#f59e0b', border: 'border-amber-100', bg: 'bg-amber-50/40', text: 'text-amber-700' },
          { key: 'LOW', name: 'RỦI RO THẤP', value: dashboard.riskSummary.lowRiskTasks, fill: '#3b82f6', border: 'border-blue-100', bg: 'bg-blue-50/40', text: 'text-blue-700' },
          { key: 'OVERDUE', name: 'QUÁ HẠN', value: dashboard.riskSummary.overdueTasks, fill: '#f43f5e', border: 'border-rose-100', bg: 'bg-rose-50/40', text: 'text-rose-700' },
          { key: 'DUE_SOON', name: 'SẮP ĐẾN HẠN', value: dashboard.riskSummary.dueSoonTasks, fill: '#eab308', border: 'border-yellow-100', bg: 'bg-yellow-50/40', text: 'text-yellow-700' },
          { key: 'BLOCKED', name: 'BỊ CHẶN', value: dashboard.riskSummary.blockedTasks, fill: '#ec4899', border: 'border-pink-100', bg: 'bg-pink-50/40', text: 'text-pink-700' },
          { key: 'DEPENDENCY', name: 'PHỤ THUỘC', value: dashboard.riskSummary.dependencyRiskTasks, fill: '#a855f7', border: 'border-purple-100', bg: 'bg-purple-50/40', text: 'text-purple-700' },
        ] : []

        const riskBarChartData = riskChartData.map(item => ({
          ...item,
          'Số task': hiddenRiskKeys.includes(item.key) ? 0 : item.value,
          displayValue: hiddenRiskKeys.includes(item.key) || item.value === 0 ? '' : item.value
        }))

        return (
          <section className="rounded-xl border border-line bg-white shadow-sm space-y-4">
            <header className="border-b border-line px-5 py-4 font-bold text-ink flex items-center justify-between gap-2">
              <div className="flex items-center gap-2">
                <ShieldAlert size={18} className="text-rose-600 shrink-0" />
                <span>Đánh giá Rủi ro Công việc (Task Risk Assessment)</span>
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

            {/* Container gộp chung: 8 Thẻ Thống Kê (Hình 1) + Biểu Đồ Cột Rủi Ro */}
            <div className="px-5 space-y-5">
              {/* 8 Ô thẻ chỉ số rủi ro (Hình 1) - Nhấp để Ẩn/Hiện cột trên biểu đồ */}
              <div className="grid gap-3 grid-cols-2 sm:grid-cols-4 lg:grid-cols-8">
                {riskChartData.map(item => {
                  const isHidden = hiddenRiskKeys.includes(item.key)
                  return (
                    <button
                      key={item.key}
                      type="button"
                      onClick={() => toggleRiskVisibility(item.key)}
                      className={`rounded-xl border p-3 text-center transition-all cursor-pointer ${
                        isHidden 
                          ? 'bg-slate-100/70 border-slate-200 opacity-50 line-through' 
                          : `${item.bg} ${item.border} hover:scale-105 hover:shadow-xs`
                      }`}
                      title={isHidden ? 'Bấm để hiển thị cột' : 'Bấm để ẩn cột'}
                    >
                      <p className={`text-[11px] font-bold uppercase tracking-wider ${isHidden ? 'text-slate-400' : item.text}`}>{item.name}</p>
                      <p className={`mt-1.5 text-xl font-extrabold ${isHidden ? 'text-slate-400' : ''}`} style={{ color: isHidden ? undefined : item.fill }}>
                        {item.value}
                      </p>
                    </button>
                  )
                })}
              </div>

              {/* Biểu đồ Cột Rủi ro Công việc (Task Risk Assessment Bar Chart) */}
              <div className="bg-slate-50/50 p-4 rounded-2xl border border-slate-100">
                <div className="flex items-center justify-between mb-3 px-1">
                  <h4 className="text-xs font-bold text-slate-700 uppercase tracking-wider">Biểu đồ Phân bổ Rủi ro Công việc</h4>
                  <span className="text-xs font-semibold text-slate-500">
                    Tổng số rủi ro: <span className="font-extrabold text-brand">{dashboard.riskSummary?.totalRiskTasks ?? 0}</span>
                  </span>
                </div>

                <div className="h-56 w-full">
                  {riskBarChartData.length > 0 ? (
                    <ResponsiveContainer width="100%" height="100%" debounce={50}>
                      <BarChart data={riskBarChartData} margin={{ top: 20, right: 15, left: -20, bottom: 5 }}>
                        <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e2e8f0" />
                        <XAxis dataKey="name" tick={{ fontSize: 10, fontWeight: 700, fill: '#475569' }} axisLine={false} tickLine={false} />
                        <YAxis allowDecimals={false} tick={{ fontSize: 11, fill: '#64748b' }} axisLine={false} tickLine={false} />
                        <Tooltip 
                          isAnimationActive={false}
                          cursor={{ fill: 'rgba(241, 245, 249, 0.6)' }}
                          formatter={(val: any) => [`${val ?? 0} task`, 'Số lượng']}
                          contentStyle={{ borderRadius: '12px', border: '1px solid #e2e8f0', boxShadow: '0 4px 12px rgba(0, 0, 0, 0.08)' }}
                        />
                        <Bar dataKey="Số task" radius={[8, 8, 0, 0]} isAnimationActive={false}>
                          <LabelList dataKey="displayValue" position="top" style={{ fontSize: 11, fontWeight: 800, fill: '#334155' }} />
                          {riskBarChartData.map((entry, index) => (
                            <Cell key={`risk-bar-${index}`} fill={entry.fill} />
                          ))}
                        </Bar>
                      </BarChart>
                    </ResponsiveContainer>
                  ) : (
                    <div className="flex h-full items-center justify-center text-xs font-medium text-slate-400 bg-white rounded-xl border border-dashed border-slate-200">Chưa có dữ liệu rủi ro</div>
                  )}
                </div>
              </div>
            </div>

            {/* Bảng Danh sách Task Rủi ro (Hình 2) */}
            <div className="p-0 overflow-x-auto border-t border-line">
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
        )
      })()}

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
            {selectedSprintId && (
              <div className="flex items-center gap-2">
                <Button
                  variant="secondary"
                  size="sm"
                  onClick={handleExportSprintExcel}
                  loading={exportingSprint}
                  leadingIcon={<Download size={15} className="text-emerald-600" />}
                  className="!px-3 font-bold text-xs shrink-0"
                >
                  Xuất Excel Sprint
                </Button>
                <Button
                  variant="secondary"
                  size="sm"
                  onClick={handleExportSprintPdf}
                  loading={exportingSprintPdf}
                  leadingIcon={<FileText size={15} className="text-rose-600" />}
                  className="!px-3 font-bold text-xs shrink-0 !border-rose-200 !bg-rose-50/70 hover:!bg-rose-100/80 !text-rose-800"
                >
                  Xuất PDF Sprint
                </Button>
              </div>
            )}
          </div>
        </header>

        {sprintReport ? (
          <div className="mt-4 space-y-6">
            {/* Cumulative Flow Diagram (CFD) for Sprint */}
            {cumulativeFlowData && cumulativeFlowData.points.length > 0 && (
              <div className="p-4 rounded-xl border border-line bg-slate-50/50">
                <h4 className="text-xs font-extrabold uppercase tracking-wider text-slate-700 mb-3 flex items-center gap-2">
                  <Layers size={16} className="text-blue-600" /> Cumulative Flow Diagram (CFD Sprint)
                </h4>
                <div className="h-56 w-full">
                  <ResponsiveContainer width="100%" height="100%">
                    <LineChart data={cumulativeFlowData.points} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                      <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e2e8f0" />
                      <XAxis dataKey="date" tick={{ fontSize: 10 }} tickFormatter={val => formatShortDate(val)} axisLine={false} tickLine={false} />
                      <YAxis tick={{ fontSize: 11 }} axisLine={false} tickLine={false} />
                      <Tooltip 
                        contentStyle={{ borderRadius: '12px', border: '1px solid #e2e8f0', boxShadow: '0 4px 12px rgba(0,0,0,0.08)' }} 
                        labelFormatter={val => `Ngày: ${formatShortDate(val)}`}
                      />
                      {!hiddenCfdAreas.includes('todo') && (
                        <Line type="monotone" dataKey="todo" stroke="#64748b" strokeWidth={2} dot={{ r: 4, fill: '#64748b' }} activeDot={{ r: 6 }} name="Cần làm" />
                      )}
                      {!hiddenCfdAreas.includes('inProgress') && (
                        <Line type="monotone" dataKey="inProgress" stroke="#3b82f6" strokeWidth={2} dot={{ r: 4, fill: '#3b82f6' }} activeDot={{ r: 6 }} name="Đang làm" />
                      )}
                      {!hiddenCfdAreas.includes('blocked') && (
                        <Line type="monotone" dataKey="blocked" stroke="#f59e0b" strokeWidth={2} dot={{ r: 4, fill: '#f59e0b' }} activeDot={{ r: 6 }} name="Đang chờ" />
                      )}
                      {!hiddenCfdAreas.includes('inReview') && (
                        <Line type="monotone" dataKey="inReview" stroke="#8b5cf6" strokeWidth={2} dot={{ r: 4, fill: '#8b5cf6' }} activeDot={{ r: 6 }} name="Đang review" />
                      )}
                      {!hiddenCfdAreas.includes('done') && (
                        <Line type="monotone" dataKey="done" stroke="#10b981" strokeWidth={2} dot={{ r: 4, fill: '#10b981' }} activeDot={{ r: 6 }} name="Hoàn thành" />
                      )}
                      {!hiddenCfdAreas.includes('cancelled') && (
                        <Line type="monotone" dataKey="cancelled" stroke="#ef4444" strokeWidth={2} dot={{ r: 4, fill: '#ef4444' }} activeDot={{ r: 6 }} name="Đã hủy" />
                      )}
                    </LineChart>
                  </ResponsiveContainer>
                </div>
                <div className="flex items-center justify-center gap-3 pt-3 border-t border-line/60 mt-3 flex-wrap">
                  {[
                    { key: 'todo', name: 'Cần làm', color: '#64748b' },
                    { key: 'inProgress', name: 'Đang làm', color: '#3b82f6' },
                    { key: 'blocked', name: 'Đang chờ', color: '#f59e0b' },
                    { key: 'inReview', name: 'Đang review', color: '#8b5cf6' },
                    { key: 'done', name: 'Hoàn thành', color: '#10b981' },
                    { key: 'cancelled', name: 'Đã hủy', color: '#ef4444' }
                  ].map(item => {
                    const isHidden = hiddenCfdAreas.includes(item.key)
                    return (
                      <button
                        key={item.key}
                        type="button"
                        onClick={() => toggleCfdArea(item.key)}
                        className={`flex items-center gap-2 px-3 py-1 rounded-full text-xs font-bold transition-all border cursor-pointer ${
                          isHidden 
                            ? 'bg-slate-100 text-slate-400 border-slate-200 line-through opacity-50' 
                            : 'bg-white text-slate-700 border-slate-200 shadow-2xs hover:scale-105 hover:shadow-xs'
                        }`}
                        title={isHidden ? 'Bấm để hiển thị' : 'Bấm để ẩn'}
                      >
                        <span className="size-2.5 rounded-full shrink-0 transition-transform duration-200" style={{ backgroundColor: isHidden ? '#cbd5e1' : item.color }} />
                        <span>{item.name}</span>
                      </button>
                    )
                  })}
                </div>
              </div>
            )}

            <SprintStatisticsView 
              statistics={sprintReport.statistics as unknown as SprintTaskStatistics}
              burndown={sprintReport.burndown as unknown as SprintBurndown}
            />
          </div>
        ) : (
          <div className="py-8 text-center text-muted">Vui lòng chọn Sprint để xem báo cáo chi tiết</div>
        )}
      </section>

      <Modal open={Boolean(scanResult)} title="Kết quả quét rủi ro" onClose={() => setScanResult(null)}>
        {scanResult && (
          <div className="space-y-5 py-1">
            <p className="text-xs font-semibold text-muted">
              Hệ thống đã hoàn tất quét và phân tích rủi ro tự động cho toàn bộ dự án:
            </p>

            {/* Quick Metrics Grid */}
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-3.5">
              <div className="flex flex-col items-center justify-center p-3.5 sm:p-4 rounded-2xl border border-slate-200/80 bg-slate-50/80 text-center shadow-2xs">
                <div className="size-10 rounded-2xl bg-slate-200/70 text-slate-700 flex items-center justify-center mb-2 shrink-0">
                  <ListTodo size={18} />
                </div>
                <span className="text-2xl font-black text-slate-800">{scanResult.scannedTasks}</span>
                <span className="text-[10px] sm:text-[11px] font-bold text-slate-400 uppercase tracking-tight whitespace-nowrap mt-1">Đã quét</span>
              </div>

              <div className={`flex flex-col items-center justify-center p-3.5 sm:p-4 rounded-2xl border text-center shadow-2xs ${
                scanResult.riskTasks > 0 ? 'border-amber-200 bg-amber-50/60' : 'border-slate-200/80 bg-slate-50/80'
              }`}>
                <div className={`size-10 rounded-2xl flex items-center justify-center mb-2 shrink-0 ${
                  scanResult.riskTasks > 0 ? 'bg-amber-100 text-amber-700' : 'bg-slate-200/70 text-slate-700'
                }`}>
                  <AlertTriangle size={18} />
                </div>
                <span className={`text-2xl font-black ${scanResult.riskTasks > 0 ? 'text-amber-700' : 'text-slate-800'}`}>
                  {scanResult.riskTasks}
                </span>
                <span className="text-[10px] sm:text-[11px] font-bold text-slate-400 uppercase tracking-tight whitespace-nowrap mt-1">Có rủi ro</span>
              </div>

              <div className={`flex flex-col items-center justify-center p-3.5 sm:p-4 rounded-2xl border text-center shadow-2xs ${
                (scanResult.highRiskTasks > 0 || scanResult.criticalRiskTasks > 0) ? 'border-rose-200 bg-rose-50/60' : 'border-slate-200/80 bg-slate-50/80'
              }`}>
                <div className={`size-10 rounded-2xl flex items-center justify-center mb-2 shrink-0 ${
                  (scanResult.highRiskTasks > 0 || scanResult.criticalRiskTasks > 0) ? 'bg-rose-100 text-rose-700' : 'bg-slate-200/70 text-slate-700'
                }`}>
                  <ShieldAlert size={18} />
                </div>
                <div className="flex items-baseline gap-1">
                  <span className={`text-2xl font-black ${scanResult.criticalRiskTasks > 0 ? 'text-rose-600' : 'text-slate-800'}`}>
                    {scanResult.criticalRiskTasks}
                  </span>
                  <span className="text-sm font-bold text-slate-300">/</span>
                  <span className={`text-base font-bold ${scanResult.highRiskTasks > 0 ? 'text-orange-600' : 'text-slate-600'}`}>
                    {scanResult.highRiskTasks}
                  </span>
                </div>
                <span className="text-[10px] sm:text-[10.5px] font-bold text-slate-400 uppercase tracking-tight whitespace-nowrap mt-1">Nguy cấp / Cao</span>
              </div>

              <div className="flex flex-col items-center justify-center p-3.5 sm:p-4 rounded-2xl border border-indigo-200/80 bg-indigo-50/60 text-center shadow-2xs">
                <div className="size-10 rounded-2xl bg-indigo-100 text-indigo-700 flex items-center justify-center mb-2 shrink-0">
                  <Bell size={18} />
                </div>
                <span className="text-2xl font-black text-indigo-700">{scanResult.notificationsCreated}</span>
                <span className="text-[10px] sm:text-[11px] font-bold text-indigo-400 uppercase tracking-tight whitespace-nowrap mt-1">Thông báo</span>
              </div>
            </div>

            {/* Status Summary Banner */}
            {scanResult.riskTasks === 0 ? (
              <div className="p-4 rounded-2xl border border-emerald-200 bg-emerald-50/70 flex items-center gap-3.5 text-emerald-800 shadow-2xs">
                <div className="size-9 rounded-xl bg-emerald-500 text-white flex items-center justify-center shrink-0 shadow-xs">
                  <CheckCircle2 size={20} />
                </div>
                <div>
                  <p className="text-xs font-extrabold">Dự án hoạt động rất an toàn!</p>
                  <p className="text-[11px] font-medium text-emerald-700/80 mt-0.5">Không phát hiện rủi ro đáng kể nào trong đợt quét lần này.</p>
                </div>
              </div>
            ) : (
              <div className="p-4 rounded-2xl border border-amber-200 bg-amber-50/70 flex items-center gap-3.5 text-amber-900 shadow-2xs">
                <div className="size-9 rounded-xl bg-amber-500 text-white flex items-center justify-center shrink-0 shadow-xs">
                  <AlertTriangle size={20} />
                </div>
                <div>
                  <p className="text-xs font-extrabold">Phát hiện {scanResult.riskTasks} task cần chú ý!</p>
                  <p className="text-[11px] font-medium text-amber-800/80 mt-0.5">Hệ thống đã tự động tạo thông báo gửi đến các thành viên phụ trách.</p>
                </div>
              </div>
            )}

            {/* Footer with Timestamp & Close button */}
            <div className="flex items-center justify-between pt-3 border-t border-line/60">
              <div className="flex items-center gap-1.5 text-xs text-muted">
                <Clock size={13} className="text-slate-400" />
                <span>Thời gian quét: <span className="font-semibold text-slate-700">{new Date(scanResult.scannedAt).toLocaleString('vi-VN')}</span></span>
              </div>
              <Button variant="primary" size="sm" onClick={() => setScanResult(null)} className="!px-5 font-bold shadow-sm">
                Đóng
              </Button>
            </div>
          </div>
        )}
      </Modal>

      {/* Modal Xuất Báo Cáo Dự Án Excel / PDF */}
      <Modal open={exportModalOpen} title="Xuất Báo cáo Dự án (Excel / PDF)" onClose={() => setExportModalOpen(false)}>
        <div className="space-y-4 py-2">
          <p className="text-xs text-muted font-medium">
            Báo cáo bao gồm thông tin chi tiết SPRINT_SUMMARY, PROJECT_SUMMARY, SPRINTS, TASKS, BACKLOG_ITEMS, TIME_LOGS, MEMBER_PERFORMANCE, BUG_SUMMARY & BUG_LIST.
          </p>

          <div className="grid gap-3 sm:grid-cols-2">
            <div>
              <label className="block text-xs font-bold text-ink mb-1">Từ ngày (From Date)</label>
              <Input
                type="date"
                value={exportFilters.fromDate}
                onChange={e => setExportFilters({ ...exportFilters, fromDate: e.target.value })}
              />
            </div>
            <div>
              <label className="block text-xs font-bold text-ink mb-1">Đến ngày (To Date)</label>
              <Input
                type="date"
                value={exportFilters.toDate}
                onChange={e => setExportFilters({ ...exportFilters, toDate: e.target.value })}
              />
            </div>
          </div>

          <div className="grid gap-3 sm:grid-cols-2">
            <div>
              <label className="block text-xs font-bold text-ink mb-1">Lọc theo Sprint</label>
              <Select
                value={exportFilters.sprintId}
                onChange={e => setExportFilters({ ...exportFilters, sprintId: e.target.value })}
                options={[
                  { label: '-- Tất cả Sprint --', value: '' },
                  ...sprints.map(s => ({ label: s.name, value: s.id }))
                ]}
              />
            </div>
            <div>
              <label className="block text-xs font-bold text-ink mb-1">Lọc theo Thành viên</label>
              <Select
                value={exportFilters.userId}
                onChange={e => setExportFilters({ ...exportFilters, userId: e.target.value })}
                options={[
                  { label: '-- Tất cả Thành viên --', value: '' },
                  ...(memberReport?.members.map(m => ({ label: `${m.username} (${m.email})`, value: m.userId })) ?? [])
                ]}
              />
            </div>
          </div>

          <div className="flex items-center justify-end gap-2 pt-3 border-t border-line">
            <Button variant="secondary" size="sm" onClick={() => setExportModalOpen(false)}>Hủy</Button>
            <Button
              variant="secondary"
              size="sm"
              loading={exportingProjectPdf}
              onClick={handleExportProjectPdf}
              leadingIcon={<FileText size={16} className="text-rose-600" />}
              className="!border-rose-200 !bg-rose-50/70 hover:!bg-rose-100/80 !text-rose-800 font-bold"
            >
              Tải PDF (.pdf)
            </Button>
            <Button
              variant="primary"
              size="sm"
              loading={exportingProject}
              onClick={handleExportProjectExcel}
              leadingIcon={<FileSpreadsheet size={16} />}
              className="!bg-emerald-600 hover:!bg-emerald-700 !text-white font-bold"
            >
              Tải Excel (.xlsx)
            </Button>
          </div>
        </div>
      </Modal>

    </div>
  )
}
