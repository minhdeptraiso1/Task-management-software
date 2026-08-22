import { Button, RefreshButton } from '../../../components/ui'
import { ChevronLeft, Download, Loader2, ShieldAlert, AlertTriangle } from 'lucide-react'
import { useRef, useState } from 'react'
import { toJpeg } from 'html-to-image'
import { jsPDF } from 'jspdf'
import type { SprintTaskStatistics, SprintBurndown, TaskRiskSummary } from '../models/task.model'
import type { SprintCapacityResponse, SprintHealthResponse, SprintRiskResponse } from '../models/scrum.model'
import { sprintRiskTypeLabels } from '../models/scrum.model'
import { taskStatusLabels, taskRiskLevelLabels, taskRiskReasonLabels } from '../models/task.model'
import {
  PieChart, Pie, Cell,
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip,
  LineChart, Line, ResponsiveContainer
} from 'recharts'
import { formatShortDate } from '../../../utils/format'

interface SprintStatisticsViewProps {
  statistics: SprintTaskStatistics | null
  burndown: SprintBurndown | null
  capacity?: SprintCapacityResponse | null
  health?: SprintHealthResponse | null
  risks?: SprintRiskResponse[] | null
  taskRiskSummary?: TaskRiskSummary | null
  onBack?: () => void
  onRefresh?: () => void
}

const COLORS = ['#0088FE', '#00C49F', '#FFBB28', '#FF8042', '#a855f7', '#ef4444']
const STATUS_COLORS: Record<string, string> = {
  TODO: '#3b82f6',
  IN_PROGRESS: '#f59e0b',
  IN_REVIEW: '#8b5cf6',
  DONE: '#10b981',
  BLOCKED: '#ef4444',
  OPEN: '#3b82f6',
  ASSIGNED: '#6366f1',
  RESOLVED: '#10b981',
  VERIFIED: '#14b8a6',
  REOPENED: '#a855f7',
  CLOSED: '#64748b',
  CANCELLED: '#f43f5e'
}

export function SprintStatisticsView({
  statistics,
  burndown,
  capacity,
  health,
  risks,
  taskRiskSummary,
  onBack,
  onRefresh
}: SprintStatisticsViewProps) {
  if (!statistics) return null

  const getHealthStatusStyles = (status?: string) => {
    switch (status) {
      case 'GOOD':
        return { text: 'Tốt', color: 'text-emerald-700 bg-emerald-50 border-emerald-200' }
      case 'WARNING':
        return { text: 'Cảnh báo', color: 'text-amber-700 bg-amber-50 border-amber-200' }
      case 'CRITICAL':
        return { text: 'Nguy kịch', color: 'text-red-700 bg-red-50 border-red-200' }
      default:
        return { text: 'Chưa rõ', color: 'text-slate-600 bg-slate-50 border-slate-200' }
    }
  }

  const getSeverityBadgeClass = (severity: string) => {
    switch (severity) {
      case 'CRITICAL':
        return 'bg-red-100 text-red-800 border-red-200'
      case 'HIGH':
        return 'bg-orange-100 text-orange-800 border-orange-200'
      case 'MEDIUM':
        return 'bg-amber-100 text-amber-800 border-amber-200'
      default:
        return 'bg-blue-100 text-blue-800 border-blue-200'
    }
  }

  const formatMinsToHours = (mins: number) => {
    return `${(mins / 60).toFixed(1)}h`
  }

  const [hiddenStatuses, setHiddenStatuses] = useState<string[]>([])
  const [hiddenBarSeries, setHiddenBarSeries] = useState<string[]>([])
  const [hiddenBurndownLines, setHiddenBurndownLines] = useState<string[]>([])

  // Prepare Pie Chart Data
  const pieData = statistics.byStatus
    .filter(s => (s.count || 0) > 0)
    .map(s => ({
      name: taskStatusLabels[s.status] || s.status,
      value: hiddenStatuses.includes(s.status) ? 0 : (s.count || 0),
      status: s.status
    }))

  // Prepare Bar Chart Data (Workload by Assignee)
  const barData = statistics.byAssignee.map(a => ({
    name: a.username || 'Chưa giao',
    Tasks: hiddenBarSeries.includes('Tasks') ? 0 : (a.totalTasks || 0),
    'Log (h)': hiddenBarSeries.includes('Log (h)') ? 0 : (Math.round(((a.spentMinutes || 0) / 60) * 10) / 10)
  }))

  // Prepare Line Chart Data (Burndown)
  const burndownData = burndown?.points.map(p => ({
    date: formatShortDate(p.date), // DD/MM
    'Thực tế': p.actualRemainingTasks,
    'Lý tưởng': p.idealRemainingTasks
  })) ?? []

  const toggleStatusVisibility = (statusKey: string) => {
    setHiddenStatuses(prev => 
      prev.includes(statusKey) ? prev.filter(s => s !== statusKey) : [...prev, statusKey]
    )
  }

  const toggleBarSeriesVisibility = (key: string) => {
    setHiddenBarSeries(prev => 
      prev.includes(key) ? prev.filter(k => k !== key) : [...prev, key]
    )
  }

  const toggleBurndownLineVisibility = (key: string) => {
    setHiddenBurndownLines(prev => 
      prev.includes(key) ? prev.filter(k => k !== key) : [...prev, key]
    )
  }

  const RADIAN = Math.PI / 180

  const renderCustomizedPieLabelLine = (props: any) => {
    const { cx, cy, midAngle, outerRadius, value, stroke } = props
    if (!value || value <= 0) return <path d="" />

    const sx = cx + outerRadius * Math.cos(-midAngle * RADIAN)
    const sy = cy + outerRadius * Math.sin(-midAngle * RADIAN)

    const mx = cx + (outerRadius + 14) * Math.cos(-midAngle * RADIAN)
    const my = cy + (outerRadius + 14) * Math.sin(-midAngle * RADIAN)

    const isRight = Math.cos(-midAngle * RADIAN) >= 0
    const ex = mx + (isRight ? 18 : -18)
    const ey = my

    return (
      <path
        d={`M${sx},${sy}L${mx},${my}L${ex},${ey}`}
        stroke={stroke || props.fill || '#94a3b8'}
        strokeWidth={1.5}
        fill="none"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    )
  }

  const renderCustomizedPieLabel = ({ cx, cy, midAngle, outerRadius, percent, value }: any) => {
    if (!value || value <= 0 || !percent) return null

    const mx = cx + (outerRadius + 14) * Math.cos(-midAngle * RADIAN)
    const my = cy + (outerRadius + 14) * Math.sin(-midAngle * RADIAN)

    const isRight = Math.cos(-midAngle * RADIAN) >= 0
    const ex = mx + (isRight ? 22 : -22)
    const ey = my

    const percentStr = `${(percent * 100).toFixed(0)}%`

    return (
      <text
        x={ex}
        y={ey}
        fill="#1e293b"
        textAnchor={isRight ? 'start' : 'end'}
        dominantBaseline="central"
        style={{ fontSize: '12px', fontWeight: 800 }}
      >
        {percentStr}
      </text>
    )
  }

  const printRef = useRef<HTMLDivElement>(null)
  const [isExporting, setIsExporting] = useState(false)
  const [isExportAnim, setIsExportAnim] = useState(false)

  const handleExportPDF = async () => {
    if (!printRef.current || !statistics) return
    try {
      setIsExporting(true)
      const dataUrl = await toJpeg(printRef.current, {
        backgroundColor: '#ffffff',
        pixelRatio: 2,
        quality: 0.95
      })
      const imgWidth = printRef.current.offsetWidth
      const imgHeight = printRef.current.offsetHeight
      
      const pdfWidth = 297 // Chiều rộng chuẩn của giấy A4 ngang (landscape)
      const pdfHeight = (imgHeight * pdfWidth) / imgWidth
      
      // Khởi tạo file PDF với kích thước tùy chỉnh để chứa toàn bộ chiều cao của thẻ section
      const pdf = new jsPDF({
        orientation: pdfWidth > pdfHeight ? 'l' : 'p',
        unit: 'mm',
        format: [pdfWidth, pdfHeight]
      })
      
      pdf.addImage(dataUrl, 'JPEG', 0, 0, pdfWidth, pdfHeight)
      pdf.save(`Thong-ke-Sprint-${statistics.sprintName.replace(/\s+/g, '-')}.pdf`)
    } catch (err) {
      console.error('Lỗi khi xuất PDF:', err)
    } finally {
      setIsExporting(false)
    }
  }

  const handleExportPDFClick = async () => {
    setIsExportAnim(true)
    setTimeout(() => setIsExportAnim(false), 300)
    await handleExportPDF()
  }

  return (
    <section ref={printRef} className="space-y-6 rounded-2xl border border-brand-line bg-gradient-to-br from-brand-soft/30 via-brand-cream/30 to-white p-5 shadow-[0_18px_45px_rgba(247,148,29,0.08)] animate-enter">
      {/* Header */}
      <div className="flex flex-col gap-3 rounded-xl border border-slate-200 bg-white p-4 shadow-sm lg:flex-row lg:items-center lg:justify-between">
        <div className="flex items-start gap-3">
          {onBack && <Button variant="secondary" size="sm" leadingIcon={<ChevronLeft size={16} />} onClick={onBack}>Kanban Task</Button>}
          <div>
            <h3 className="text-lg font-bold text-slate-800">Thống kê Sprint: {statistics.sprintName}</h3>
            <p className="mt-0.5 text-sm text-slate-500">Báo cáo tổng quan tiến độ và khối lượng công việc trong Sprint.</p>
          </div>
        </div>
        <div className="flex items-center gap-2">
          <Button 
            variant="outline-red" 
            size="sm" 
            leadingIcon={isExporting ? <Loader2 size={16} className="animate-spin" /> : <Download size={16} className={`transition-transform duration-300 ${isExportAnim ? 'translate-y-1.5' : ''}`} />} 
            onClick={handleExportPDFClick}
            disabled={isExporting}
          >
            Xuất PDF
          </Button>
          {onRefresh && <RefreshButton onRefresh={onRefresh} />}
        </div>
      </div>

      {/* Health Status summary cards */}
      {health && (
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-sm flex flex-col justify-between">
            <div>
              <p className="text-xs font-semibold text-slate-500 mb-2">Tình trạng Sức khỏe Sprint</p>
              <div className="flex items-center gap-2">
                <span className={`rounded-full px-2.5 py-0.5 text-xs font-bold border ${getHealthStatusStyles(health.healthStatus).color}`}>
                  {getHealthStatusStyles(health.healthStatus).text}
                </span>
                <p className="text-[11px] text-slate-400">Đánh giá chung</p>
              </div>
            </div>
            <div className="mt-3 pt-3 border-t border-slate-100 flex items-center justify-between text-xs text-slate-500">
              <span>Tổng số ngày:</span>
              <span className="font-bold text-slate-700">{health.totalDays} ngày (Còn {health.remainingDays} ngày)</span>
            </div>
          </div>

          <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-sm flex flex-col justify-between">
            <div>
              <p className="text-xs font-semibold text-slate-500 mb-2">Tiến độ (Thực tế vs Dự kiến)</p>
              <div className="grid grid-cols-2 gap-2">
                <div>
                  <p className="text-xl font-bold text-brand">{Math.round(health.progressRate)}%</p>
                  <p className="text-[10px] text-slate-400">Thực tế</p>
                </div>
                <div>
                  <p className="text-xl font-bold text-indigo-600">{Math.round(health.expectedProgressRate)}%</p>
                  <p className="text-[10px] text-slate-400">Dự kiến</p>
                </div>
              </div>
            </div>
            <div className="w-full h-1.5 bg-slate-100 rounded-full mt-3 overflow-hidden relative">
              <div className="absolute top-0 left-0 h-full bg-indigo-200" style={{ width: `${Math.min(100, health.expectedProgressRate)}%` }} />
              <div className="absolute top-0 left-0 h-full bg-brand" style={{ width: `${Math.min(100, health.progressRate)}%` }} />
            </div>
          </div>

          <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-sm flex flex-col justify-between">
            <div>
              <p className="text-xs font-semibold text-slate-500 mb-2">Cảnh báo Công việc</p>
              <div className="grid grid-cols-2 gap-2">
                <div>
                  <p className="text-xl font-bold text-amber-500">{health.noAssigneeTasks}</p>
                  <p className="text-[10px] text-slate-400">Chưa giao</p>
                </div>
                <div>
                  <p className="text-xl font-bold text-blue-500">{health.noEstimateTasks}</p>
                  <p className="text-[10px] text-slate-400">Chưa ước lượng</p>
                </div>
              </div>
            </div>
            <div className="mt-3 pt-3 border-t border-slate-100 flex items-center justify-between text-xs text-slate-500">
              <span>Số lượng rủi ro:</span>
              <span className="font-bold text-red-500">{health.riskCount} (Nguy cấp: {health.criticalRiskCount})</span>
            </div>
          </div>
        </div>
      )}

      <div className="pb-4 space-y-6">
        {/* Overview cards */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-5">
          {/* Card 1: Tiến độ */}
          <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-sm flex flex-col justify-center transition-shadow hover:shadow-md">
            <p className="text-sm font-semibold text-slate-500 mb-1">Tiến độ Sprint</p>
            <div className="flex items-end gap-2">
              <p className="text-3xl font-bold text-brand">{Math.round(statistics.completionRate)}%</p>
              <p className="text-sm font-medium text-slate-400 mb-1">/ {statistics.totalTasks} Tasks</p>
            </div>
            <div className="w-full h-1.5 bg-slate-100 rounded-full mt-3 overflow-hidden">
               <div className="h-full bg-brand transition-all duration-1000" style={{ width: `${statistics.completionRate}%` }} />
            </div>
          </div>

          {/* Card 2: Trạng thái Tasks */}
          <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-sm transition-shadow hover:shadow-md">
            <p className="text-sm font-semibold text-slate-500 mb-3">Cảnh báo Trạng thái</p>
            <div className="flex justify-between px-2">
               <div className="text-center">
                  <p className="text-2xl font-bold text-red-500">{statistics.blockedTasks}</p>
                  <p className="text-[11px] font-medium text-slate-400 mt-1 uppercase tracking-wide">Đang chờ</p>
               </div>
               <div className="text-center">
                  <p className="text-2xl font-bold text-amber-500">{statistics.overdueTasks}</p>
                  <p className="text-[11px] font-medium text-slate-400 mt-1 uppercase tracking-wide">Quá hạn</p>
               </div>
               <div className="text-center">
                  <p className="text-2xl font-bold text-slate-400">{statistics.unfinishedTasks}</p>
                  <p className="text-[11px] font-medium text-slate-400 mt-1 uppercase tracking-wide">Chưa xong</p>
               </div>
            </div>
          </div>

          {/* Card 3: Thời gian Log / Ước tính */}
          <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-sm transition-shadow hover:shadow-md">
            <p className="text-sm font-semibold text-slate-500 mb-2">Thời gian (Log / Ước tính)</p>
            <div className="flex items-end gap-1.5">
              <p className="text-2xl font-bold text-slate-700">{Math.round(statistics.spentMinutes / 60)}h</p>
              <p className="text-lg font-medium text-slate-400 mb-0.5">/ {Math.round(statistics.estimatedMinutes / 60)}h</p>
            </div>
            <p className="text-xs font-medium text-slate-400 mt-3">
              Thời gian ước tính còn lại: <span className="font-bold text-slate-600">{Math.round(statistics.remainingEstimatedMinutes / 60)}h</span>
            </p>
          </div>

          {/* Card 4: Hiệu suất Thời gian */}
          <div className={`p-4 rounded-xl border shadow-sm transition-shadow hover:shadow-md ${statistics.overEstimated ? 'bg-red-50/50 border-red-200' : 'bg-emerald-50/50 border-emerald-200'}`}>
            <p className={`text-sm font-semibold mb-2 ${statistics.overEstimated ? 'text-red-700/70' : 'text-emerald-700/70'}`}>Sử dụng Quỹ thời gian</p>
            <p className={`text-3xl font-bold ${statistics.overEstimated ? 'text-red-600' : 'text-emerald-600'}`}>
              {statistics.timeUsageRate.toFixed(1)}%
            </p>
          </div>
        </div>

        {/* MAIN BURNDOWN CHART - CENTRAL AND LARGE */}
        <div className="mt-6 bg-white p-6 rounded-2xl border border-slate-200 shadow-sm">
          <div className="mb-8 text-center">
            <h4 className="font-bold text-xl text-slate-800">Biểu đồ Burndown</h4>
            <p className="text-sm text-slate-500 mt-1">Theo dõi tiến độ hoàn thành task qua từng ngày trong Sprint</p>
          </div>
          
          <div className="h-[380px] w-full">
            {burndownData.length > 0 ? (
              <ResponsiveContainer width="100%" height="100%">
                <LineChart data={burndownData} margin={{ top: 5, right: 30, left: 0, bottom: 5 }}>
                  <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e2e8f0" />
                  <XAxis dataKey="date" fontSize={12} tickLine={false} axisLine={false} tickMargin={12} />
                  <YAxis fontSize={12} tickLine={false} axisLine={false} tickMargin={12} />
                  <Tooltip 
                    contentStyle={{ borderRadius: '12px', border: 'none', boxShadow: '0 4px 20px rgba(0,0,0,0.08)' }}
                    cursor={{ stroke: '#f97316', strokeWidth: 1, strokeDasharray: '5 5' }} 
                  />
                  {!hiddenBurndownLines.includes('Lý tưởng') && (
                    <Line type="monotone" name="Tasks Lý tưởng còn lại" dataKey="Lý tưởng" stroke="#94a3b8" strokeDasharray="5 5" strokeWidth={2} dot={false} activeDot={false} isAnimationActive={true} animationDuration={800} animationEasing="ease-out" />
                  )}
                  {!hiddenBurndownLines.includes('Thực tế') && (
                    <Line type="monotone" name="Tasks Thực tế còn lại" dataKey="Thực tế" stroke="#f97316" strokeWidth={3} dot={{ r: 4, strokeWidth: 2, fill: '#fff' }} activeDot={{ r: 6, strokeWidth: 0, fill: '#f97316' }} isAnimationActive={true} animationDuration={800} animationEasing="ease-out" />
                  )}
                </LineChart>
              </ResponsiveContainer>
            ) : (
              <div className="flex h-full items-center justify-center text-sm font-medium text-slate-400 bg-slate-50 rounded-xl border border-dashed border-slate-200">Chưa có dữ liệu burndown</div>
            )}
          </div>

          {/* Burndown Interactive Legend */}
          <div className="flex items-center justify-center gap-4 pt-4 border-t border-slate-100 mt-4">
            {[
              { key: 'Lý tưởng', name: 'Tasks Lý tưởng còn lại', color: '#94a3b8' },
              { key: 'Thực tế', name: 'Tasks Thực tế còn lại', color: '#f97316' }
            ].map(item => {
              const isHidden = hiddenBurndownLines.includes(item.key)
              return (
                <button
                  key={item.key}
                  type="button"
                  onClick={() => toggleBurndownLineVisibility(item.key)}
                  className={`flex items-center gap-2 px-3.5 py-1.5 rounded-full text-xs font-bold transition-all border cursor-pointer ${
                    isHidden 
                      ? 'bg-slate-100 text-slate-400 border-slate-200 line-through opacity-50' 
                      : 'bg-white text-slate-700 border-slate-200 shadow-2xs hover:scale-105 hover:shadow-xs'
                  }`}
                  title={isHidden ? 'Bấm để hiển thị' : 'Bấm để ẩn'}
                >
                  <span className="size-3 rounded-full shrink-0 transition-transform duration-200" style={{ backgroundColor: isHidden ? '#cbd5e1' : item.color }} />
                  <span>{item.name}</span>
                </button>
              )
            })}
          </div>
        </div>

        {/* BOTTOM CHARTS */}
        <div className="mt-6 grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Status Pie Chart - Full Solid Pie Chart (Chart Tròn Đặc) */}
          <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm lg:col-span-1 flex flex-col justify-between">
            <h4 className="font-bold text-lg text-slate-800 text-center mb-4">Phân bổ Trạng thái</h4>
            <div className="h-[250px]">
              {pieData.length > 0 ? (
                <ResponsiveContainer width="100%" height="100%">
                  <PieChart margin={{ top: 10, right: 30, left: 30, bottom: 10 }}>
                    <Pie
                      data={pieData}
                      cx="50%"
                      cy="50%"
                      innerRadius={0}
                      outerRadius={80}
                      paddingAngle={0}
                      stroke="none"
                      dataKey="value"
                      isAnimationActive={true}
                      animationDuration={800}
                      animationEasing="ease-in-out"
                      animationBegin={0}
                      labelLine={renderCustomizedPieLabelLine}
                      label={renderCustomizedPieLabel}
                      onClick={(data: any) => data && data.payload && data.payload.status && toggleStatusVisibility(data.payload.status)}
                      cursor="pointer"
                    >
                      {pieData.map((entry, index) => (
                        <Cell key={`cell-${index}`} fill={STATUS_COLORS[entry.status as string] || COLORS[index % COLORS.length]} stroke="none" className="cursor-pointer hover:opacity-85" />
                      ))}
                    </Pie>
                    <Tooltip contentStyle={{ borderRadius: '12px', border: 'none', boxShadow: '0 4px 20px rgba(0,0,0,0.08)' }} />
                  </PieChart>
                </ResponsiveContainer>
              ) : (
                <div className="flex h-full items-center justify-center text-xs font-medium text-slate-400 bg-slate-50 rounded-xl border border-dashed border-slate-200">Chưa có dữ liệu task</div>
              )}
            </div>

            {/* Interactive Pie Legend */}
            <div className="flex flex-wrap items-center justify-center gap-2 pt-3 border-t border-slate-100">
              {pieData.map(d => {
                const isHidden = hiddenStatuses.includes(d.status)
                const color = STATUS_COLORS[d.status] || '#94a3b8'
                return (
                  <button
                    key={d.status}
                    type="button"
                    onClick={() => toggleStatusVisibility(d.status)}
                    className={`flex items-center gap-2 px-3 py-1 rounded-full text-xs font-bold transition-all border cursor-pointer ${
                      isHidden 
                        ? 'bg-slate-100 text-slate-400 border-slate-200 line-through opacity-50' 
                        : 'bg-white text-slate-700 border-slate-200 shadow-2xs hover:scale-105 hover:shadow-xs'
                    }`}
                    title={isHidden ? 'Bấm để hiển thị' : 'Bấm để ẩn'}
                  >
                    <span className="size-3 rounded-full shrink-0 transition-transform duration-200" style={{ backgroundColor: isHidden ? '#cbd5e1' : color }} />
                    <span>{d.name}</span>
                  </button>
                )
              })}
            </div>
          </div>

          {/* Workload Bar Chart (kèm Toggle Bật/Tắt) */}
          <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm lg:col-span-2 flex flex-col justify-between">
            <h4 className="font-bold text-lg text-slate-800 text-center mb-4">Khối lượng công việc theo Thành viên</h4>
            <div className="h-[250px]">
              {barData.length > 0 ? (
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={barData} margin={{ top: 5, right: 0, left: -20, bottom: 5 }}>
                    <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e2e8f0" />
                    <XAxis dataKey="name" fontSize={12} tickLine={false} axisLine={false} tickMargin={12} />
                    <YAxis yAxisId="left" fontSize={12} tickLine={false} axisLine={false} tickMargin={12} />
                    <YAxis yAxisId="right" orientation="right" fontSize={12} tickLine={false} axisLine={false} tickMargin={12} />
                    <Tooltip contentStyle={{ borderRadius: '12px', border: 'none', boxShadow: '0 4px 20px rgba(0,0,0,0.08)' }} cursor={{ fill: '#f1f5f9' }} />
                    <Bar yAxisId="left" name="Số lượng Task" dataKey="Tasks" fill="#3b82f6" radius={[4, 4, 0, 0]} maxBarSize={40} isAnimationActive={true} animationDuration={800} animationEasing="ease-out" />
                    <Bar yAxisId="right" name="Giờ đã Log (h)" dataKey="Log (h)" fill="#f59e0b" radius={[4, 4, 0, 0]} maxBarSize={40} isAnimationActive={true} animationDuration={800} animationEasing="ease-out" />
                  </BarChart>
                </ResponsiveContainer>
              ) : (
                <div className="flex h-full items-center justify-center text-sm font-medium text-slate-400 bg-slate-50 rounded-xl border border-dashed border-slate-200">Chưa có dữ liệu thành viên</div>
              )}
            </div>

            {/* Interactive Bar Legend */}
            <div className="flex items-center justify-center gap-4 pt-3 border-t border-slate-100">
              {[
                { key: 'Tasks', name: 'Số lượng Task', color: '#3b82f6' },
                { key: 'Log (h)', name: 'Giờ đã Log (h)', color: '#f59e0b' }
              ].map(item => {
                const isHidden = hiddenBarSeries.includes(item.key)
                return (
                  <button
                    key={item.key}
                    type="button"
                    onClick={() => toggleBarSeriesVisibility(item.key)}
                    className={`flex items-center gap-2 px-3.5 py-1 rounded-full text-xs font-bold transition-all border cursor-pointer ${
                      isHidden 
                        ? 'bg-slate-100 text-slate-400 border-slate-200 line-through opacity-50' 
                        : 'bg-white text-slate-700 border-slate-200 shadow-2xs hover:scale-105 hover:shadow-xs'
                    }`}
                    title={isHidden ? 'Bấm để hiển thị' : 'Bấm để ẩn'}
                  >
                    <span className="size-3 rounded-full shrink-0 transition-transform duration-200" style={{ backgroundColor: isHidden ? '#cbd5e1' : item.color }} />
                    <span>{item.name}</span>
                  </button>
                )
              })}
            </div>
          </div>
        </div>

        {/* Member Capacity & Load Details Section */}
        {capacity && (
          <div className="mt-6 bg-white p-5 rounded-2xl border border-slate-200 shadow-sm">
            <div className="mb-4 flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
              <div>
                <h4 className="font-bold text-md text-slate-800">Quản lý tải & Công suất thành viên</h4>
                <p className="text-xs text-slate-500 mt-0.5">
                  Tải dự kiến: <span className="font-bold">{formatMinsToHours(capacity.totalEstimatedMinutes)}</span> / Quỹ công suất tối đa: <span className="font-bold">{formatMinsToHours(capacity.totalCapacityMinutes)}</span> ({capacity.utilizationRate.toFixed(1)}% hiệu suất sử dụng)
                </p>
              </div>
              {capacity.overCapacity && (
                <span className="inline-flex items-center gap-1 rounded-full bg-red-50 border border-red-200 px-2.5 py-0.5 text-xs font-bold text-red-600 shadow-sm">
                  <AlertTriangle size={13} className="text-red-500" /> Quá tải Sprint (+{formatMinsToHours(capacity.overCapacityMinutes)})
                </span>
              )}
            </div>

            <div className="overflow-x-auto">
              <table className="w-full text-left border-collapse text-xs">
                <thead>
                  <tr className="border-b border-slate-200 text-slate-400 font-bold">
                    <th className="py-2.5 px-3">Thành viên</th>
                    <th className="py-2.5 px-3">Vai trò</th>
                    <th className="py-2.5 px-3">Quỹ Công suất</th>
                    <th className="py-2.5 px-3">Đã ước lượng (Tasks)</th>
                    <th className="py-2.5 px-3">Đã Log</th>
                    <th className="py-2.5 px-3">Tải trọng (%)</th>
                    <th className="py-2.5 px-3">Phân bổ task</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {capacity.members.map((member) => (
                    <tr key={member.userId} className="hover:bg-slate-50/50 transition-colors">
                      <td className="py-2.5 px-3 font-semibold text-slate-700">
                        <div className="flex flex-col">
                          <span>{member.username}</span>
                          <span className="text-[10px] font-normal text-slate-400">{member.email}</span>
                        </div>
                      </td>
                      <td className="py-2.5 px-3 text-slate-500">{member.projectRole}</td>
                      <td className="py-2.5 px-3 text-slate-600 font-medium">{formatMinsToHours(member.capacityMinutes)}</td>
                      <td className="py-2.5 px-3 font-medium">
                        <span className={member.overCapacity ? 'text-red-600 font-bold' : 'text-slate-600'}>
                          {formatMinsToHours(member.assignedEstimatedMinutes)}
                        </span>
                        {member.overCapacity && (
                          <span className="ml-1 text-[10px] text-red-500 font-bold" title={`Quá tải ${formatMinsToHours(member.overCapacityMinutes)}`}>
                            (+{formatMinsToHours(member.overCapacityMinutes)})
                          </span>
                        )}
                      </td>
                      <td className="py-2.5 px-3 text-slate-600">{formatMinsToHours(member.spentMinutes)}</td>
                      <td className="py-2.5 px-3">
                        <div className="flex items-center gap-1.5">
                          <span className={`font-bold ${member.utilizationRate > 100 ? 'text-red-600' : member.utilizationRate > 80 ? 'text-amber-600' : 'text-emerald-600'}`}>
                            {member.utilizationRate.toFixed(1)}%
                          </span>
                          <div className="w-12 h-1 bg-slate-100 rounded-full overflow-hidden">
                            <div 
                              className={`h-full ${member.utilizationRate > 100 ? 'bg-red-500' : member.utilizationRate > 80 ? 'bg-amber-500' : 'bg-emerald-500'}`} 
                              style={{ width: `${Math.min(100, member.utilizationRate)}%` }} 
                            />
                          </div>
                        </div>
                      </td>
                      <td className="py-2.5 px-3">
                        {(() => {
                          const segments = [
                            { count: member.todoTasks, color: '#64748b', title: 'Todo' },
                            { count: member.inProgressTasks, color: '#3b82f6', title: 'In Progress' },
                            { count: member.inReviewTasks, color: '#a855f7', title: 'In Review' },
                            { count: member.doneTasks, color: '#22c55e', title: 'Done' },
                            { count: member.blockedTasks, color: '#ef4444', title: 'Blocked' }
                          ]
                          
                          const segmentsFiltered = segments.filter(s => s.count > 0)
                          const total = segments.reduce((sum, s) => sum + s.count, 0)
                          
                          if (total === 0) {
                            return (
                              <div className="flex items-center gap-2">
                                <svg className="size-6 text-slate-200" viewBox="0 0 36 36">
                                  <circle cx="18" cy="18" r="15" fill="none" stroke="currentColor" strokeWidth="6" />
                                </svg>
                                <span className="text-[10px] text-slate-400 italic">Không có task</span>
                              </div>
                            )
                          }
                          
                          const radius = 15
                          const circumference = 2 * Math.PI * radius
                          let accumulatedPercent = 0
                          
                          return (
                            <div className="relative group inline-block">
                              <div className="relative size-7 shrink-0 cursor-pointer">
                                <svg className="size-full -rotate-90" viewBox="0 0 36 36">
                                  <circle cx="18" cy="18" r={radius} fill="none" stroke="#f1f5f9" strokeWidth="6" />
                                  {segmentsFiltered.map((seg, idx) => {
                                    const percent = (seg.count / total) * 100
                                    const strokeDasharray = `${(percent / 100) * circumference} ${circumference}`
                                    const strokeDashoffset = -((accumulatedPercent / 100) * circumference)
                                    accumulatedPercent += percent
                                    return (
                                      <circle
                                        key={idx}
                                        cx="18"
                                        cy="18"
                                        r={radius}
                                        fill="none"
                                        stroke={seg.color}
                                        strokeWidth="6"
                                        strokeDasharray={strokeDasharray}
                                        strokeDashoffset={strokeDashoffset}
                                      />
                                    )
                                  })}
                                </svg>
                                <div className="absolute inset-0 flex items-center justify-center text-[9px] font-extrabold text-slate-500">
                                  {total}
                                </div>
                              </div>
                              
                              {/* Hover legend details popover (single-line horizontal to prevent vertical clipping) */}
                              <div className="invisible group-hover:visible absolute right-full top-1/2 -translate-y-1/2 mr-3 px-2.5 py-1.5 bg-[#1e1e1e] text-white rounded-lg shadow-xl text-[10px] z-50 flex items-center gap-3.5 whitespace-nowrap after:content-[''] after:absolute after:top-1/2 after:-translate-y-1/2 after:left-full after:border-4 after:border-transparent after:border-l-[#1e1e1e]">
                                {segments.map((seg, idx) => (
                                  <div key={idx} className="flex items-center gap-1.5">
                                    <span className="size-1.5 rounded-full shrink-0" style={{ backgroundColor: seg.color }} />
                                    <span className="text-white/60 font-semibold">{seg.title}</span>
                                    <span className="font-bold text-white">{seg.count}</span>
                                  </div>
                                ))}
                              </div>
                            </div>
                          )
                        })()}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {/* Sprint Task Risk Summary Section */}
        {taskRiskSummary && (
          <div className="mt-6 rounded-xl border border-slate-200 bg-white p-5 shadow-sm space-y-4">
            <div className="flex items-center justify-between border-b border-slate-100 pb-3">
              <div className="flex items-center gap-2 text-slate-800 font-bold">
                <ShieldAlert size={18} className="text-rose-600" />
                <span>Phân tích Rủi ro Công việc trong Sprint ({taskRiskSummary.totalRiskTasks} rủi ro)</span>
              </div>
            </div>

            {/* Quick Metrics Grid */}
            <div className="grid grid-cols-2 md:grid-cols-4 gap-3 text-center text-xs">
              <div className="bg-red-50/40 border border-red-100 rounded-lg p-3">
                <p className="text-red-700 text-lg font-bold">{taskRiskSummary.criticalRiskTasks}</p>
                <p className="text-slate-500 mt-0.5">Nguy cấp</p>
              </div>
              <div className="bg-orange-50/40 border border-orange-100 rounded-lg p-3">
                <p className="text-orange-700 text-lg font-bold">{taskRiskSummary.highRiskTasks}</p>
                <p className="text-slate-500 mt-0.5">Rủi ro Cao</p>
              </div>
              <div className="bg-amber-50/40 border border-amber-100 rounded-lg p-3">
                <p className="text-amber-700 text-lg font-bold">{taskRiskSummary.mediumRiskTasks}</p>
                <p className="text-slate-500 mt-0.5">Rủi ro Vừa</p>
              </div>
              <div className="bg-rose-50/40 border border-rose-100 rounded-lg p-3">
                <p className="text-rose-700 text-lg font-bold">{taskRiskSummary.overdueTasks}</p>
                <p className="text-slate-500 mt-0.5">Task Quá hạn</p>
              </div>
            </div>

            {/* Task risk list */}
            <div className="pt-2">
              <p className="text-xs font-semibold text-slate-700 mb-2.5">Danh sách các Task rủi ro trong Sprint:</p>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                {taskRiskSummary.topRisks.map(risk => (
                  <div key={risk.taskId} className="bg-slate-50 border border-slate-200 rounded-xl p-3 text-xs flex flex-col justify-between gap-1">
                    <div className="flex justify-between items-start gap-2">
                      <span className="font-semibold text-slate-800 line-clamp-2 flex-1" title={risk.title}>
                        {risk.title}
                      </span>
                      <span className={`shrink-0 rounded px-1.5 py-0.5 text-[10px] font-bold ${
                        risk.riskLevel === 'CRITICAL' ? 'bg-red-100 text-red-700 border border-red-200' :
                        risk.riskLevel === 'HIGH' ? 'bg-orange-100 text-orange-700 border border-orange-200' :
                        risk.riskLevel === 'MEDIUM' ? 'bg-amber-100 text-amber-700 border border-amber-200' :
                        'bg-blue-100 text-blue-700 border border-blue-200'
                      }`}>
                        {taskRiskLevelLabels[risk.riskLevel] || risk.riskLevel}
                      </span>
                    </div>
                    {risk.assigneeUsername && (
                      <p className="text-[10px] text-slate-500 mt-0.5">
                        Chịu trách nhiệm: <span className="font-medium text-slate-700">{risk.assigneeUsername}</span>
                      </p>
                    )}
                    <p className="text-[10px] text-rose-700 mt-1 font-medium bg-rose-50/50 p-2 rounded-lg border border-rose-100/50">
                      Nguyên nhân: {risk.reasons.map(r => taskRiskReasonLabels[r] || r).join(', ')}
                    </p>
                  </div>
                ))}
                {taskRiskSummary.topRisks.length === 0 && (
                  <div className="col-span-2 text-center text-slate-500 py-6 bg-slate-50 rounded-xl border border-dashed border-slate-200">
                    Sprint này hiện tại không phát hiện rủi ro công việc nào.
                  </div>
                )}
              </div>
            </div>
          </div>
        )}

        {/* Risks warning section */}
        {risks && risks.length > 0 && (
          <div className="mt-6 rounded-xl border border-red-200 bg-red-50/20 p-4">
            <div className="flex items-center gap-2 text-red-800 font-bold mb-3">
              <ShieldAlert size={18} className="text-red-600" />
              <span className="text-sm">Cảnh báo rủi ro Sprint ({risks.length})</span>
            </div>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {risks.map((risk, index) => (
                <div key={index} className="flex flex-col gap-1.5 rounded-lg border border-red-100 bg-white p-3 shadow-sm">
                  <div className="flex items-center justify-between gap-2">
                    <span className={`rounded px-1.5 py-0.5 text-[10px] font-bold border ${getSeverityBadgeClass(risk.severity)}`}>
                      {risk.severity === 'CRITICAL' ? 'Khẩn cấp' : risk.severity === 'HIGH' ? 'Cao' : risk.severity === 'MEDIUM' ? 'Vừa' : 'Thấp'}
                    </span>
                    <span className="text-[11px] font-semibold text-slate-500">
                      {sprintRiskTypeLabels[risk.type] || risk.type}
                    </span>
                  </div>
                  <p className="text-xs font-semibold text-slate-700">{risk.message}</p>
                  {risk.taskTitle && (
                    <div className="text-[10px] text-slate-500 bg-slate-50 p-1.5 rounded">
                      <span className="font-bold">Task:</span> {risk.taskTitle}
                    </div>
                  )}
                  {risk.username && (
                    <div className="text-[10px] text-slate-500">
                      <span className="font-bold">Người chịu trách nhiệm:</span> {risk.username}
                    </div>
                  )}
                  {risk.suggestedAction && (
                    <div className="mt-0.5 text-[10px] text-emerald-700 bg-emerald-50/50 border border-emerald-100 p-1.5 rounded">
                      <span className="font-bold">Gợi ý:</span> {risk.suggestedAction}
                    </div>
                  )}
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    </section>
  )
}
