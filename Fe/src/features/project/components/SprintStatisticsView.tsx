import { Button } from '../../../components/ui'
import { ChevronLeft, RefreshCcw, Download, Loader2 } from 'lucide-react'
import { useRef, useState } from 'react'
import { toJpeg } from 'html-to-image'
import { jsPDF } from 'jspdf'
import type { SprintTaskStatistics, SprintBurndown } from '../models/task.model'
import { taskStatusLabels } from '../models/task.model'
import {
  PieChart, Pie, Cell,
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend,
  LineChart, Line, ResponsiveContainer
} from 'recharts'
import { formatShortDate } from '../../../utils/format'

interface SprintStatisticsViewProps {
  statistics: SprintTaskStatistics | null
  burndown: SprintBurndown | null
  onBack?: () => void
  onRefresh?: () => void
}

const COLORS = ['#0088FE', '#00C49F', '#FFBB28', '#FF8042', '#a855f7', '#ef4444']
const STATUS_COLORS: Record<string, string> = {
  TODO: '#64748b',
  IN_PROGRESS: '#3b82f6',
  IN_REVIEW: '#a855f7',
  DONE: '#22c55e',
  BLOCKED: '#ef4444',
  CANCELLED: '#94a3b8'
}

export function SprintStatisticsView({ statistics, burndown, onBack, onRefresh }: SprintStatisticsViewProps) {
  if (!statistics) return null

  // Prepare Pie Chart Data
  const pieData = statistics.byStatus.map(s => ({
    name: taskStatusLabels[s.status] || s.status,
    value: s.count || 0,
    status: s.status
  })).filter(d => d.value > 0)

  // Prepare Bar Chart Data (Workload by Assignee)
  const barData = statistics.byAssignee.map(a => ({
    name: a.username || 'Chưa giao',
    Tasks: a.totalTasks || 0,
    'Log (h)': Math.round(((a.spentMinutes || 0) / 60) * 10) / 10
  }))

  // Prepare Line Chart Data (Burndown)
  const burndownData = burndown?.points.map(p => ({
    date: formatShortDate(p.date), // DD/MM
    'Thực tế': p.actualRemainingTasks,
    'Lý tưởng': p.idealRemainingTasks
  })) ?? []

  const printRef = useRef<HTMLDivElement>(null)
  const [isExporting, setIsExporting] = useState(false)
  const [isExportAnim, setIsExportAnim] = useState(false)
  const [isRefreshAnim, setIsRefreshAnim] = useState(false)

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

  const handleRefreshClick = () => {
    setIsRefreshAnim(true)
    setTimeout(() => setIsRefreshAnim(false), 500)
    if (onRefresh) onRefresh()
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
          {onRefresh && (
            <Button variant="outline-amber" size="sm" leadingIcon={<RefreshCcw size={16} className={`transition-transform duration-500 ${isRefreshAnim ? 'rotate-[360deg]' : ''}`} />} onClick={handleRefreshClick}>
              Làm mới
            </Button>
          )}
        </div>
      </div>

      <div className="pb-4">
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
                  <p className="text-[11px] font-medium text-slate-400 mt-1 uppercase tracking-wide">Bị chặn</p>
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
                  <Legend wrapperStyle={{ paddingTop: '20px' }} />
                  <Line type="monotone" name="Tasks Lý tưởng còn lại" dataKey="Lý tưởng" stroke="#94a3b8" strokeDasharray="5 5" strokeWidth={2} dot={false} activeDot={false} />
                  <Line type="monotone" name="Tasks Thực tế còn lại" dataKey="Thực tế" stroke="#f97316" strokeWidth={3} dot={{ r: 4, strokeWidth: 2, fill: '#fff' }} activeDot={{ r: 6, strokeWidth: 0, fill: '#f97316' }} />
                </LineChart>
              </ResponsiveContainer>
            ) : (
              <div className="flex h-full items-center justify-center text-sm font-medium text-slate-400 bg-slate-50 rounded-xl border border-dashed border-slate-200">Chưa có dữ liệu burndown</div>
            )}
          </div>
        </div>

        {/* BOTTOM CHARTS */}
        <div className="mt-6 grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Status Pie Chart */}
          <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm lg:col-span-1">
            <h4 className="font-bold text-lg text-slate-800 text-center mb-6">Phân bổ Trạng thái</h4>
            <div className="h-[280px]">
              {pieData.length > 0 ? (
                <ResponsiveContainer width="100%" height="100%">
                  <PieChart margin={{ top: 10, right: 40, left: 40, bottom: 10 }}>
                    <Pie
                      data={pieData}
                      cx="50%"
                      cy="50%"
                      innerRadius={60}
                      outerRadius={85}
                      paddingAngle={4}
                      dataKey="value"
                    >
                      {pieData.map((entry, index) => (
                        <Cell key={`cell-${index}`} fill={STATUS_COLORS[entry.status as string] || COLORS[index % COLORS.length]} />
                      ))}
                    </Pie>
                    <Tooltip contentStyle={{ borderRadius: '12px', border: 'none', boxShadow: '0 4px 20px rgba(0,0,0,0.08)' }} />
                    <Legend wrapperStyle={{ paddingTop: '20px' }} />
                  </PieChart>
                </ResponsiveContainer>
              ) : (
                <div className="flex h-full items-center justify-center text-sm font-medium text-slate-400 bg-slate-50 rounded-xl border border-dashed border-slate-200">Chưa có dữ liệu task</div>
              )}
            </div>
          </div>

          {/* Workload Bar Chart */}
          <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm lg:col-span-2">
            <h4 className="font-bold text-lg text-slate-800 text-center mb-6">Khối lượng công việc theo Thành viên</h4>
            <div className="h-[280px]">
              {barData.length > 0 ? (
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={barData} margin={{ top: 5, right: 0, left: -20, bottom: 5 }}>
                    <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e2e8f0" />
                    <XAxis dataKey="name" fontSize={12} tickLine={false} axisLine={false} tickMargin={12} />
                    <YAxis yAxisId="left" fontSize={12} tickLine={false} axisLine={false} tickMargin={12} />
                    <YAxis yAxisId="right" orientation="right" fontSize={12} tickLine={false} axisLine={false} tickMargin={12} />
                    <Tooltip contentStyle={{ borderRadius: '12px', border: 'none', boxShadow: '0 4px 20px rgba(0,0,0,0.08)' }} cursor={{ fill: '#f1f5f9' }} />
                    <Legend wrapperStyle={{ paddingTop: '10px' }} />
                    <Bar yAxisId="left" name="Số lượng Task" dataKey="Tasks" fill="#3b82f6" radius={[4, 4, 0, 0]} maxBarSize={40} />
                    <Bar yAxisId="right" name="Giờ đã Log (h)" dataKey="Log (h)" fill="#f59e0b" radius={[4, 4, 0, 0]} maxBarSize={40} />
                  </BarChart>
                </ResponsiveContainer>
              ) : (
                <div className="flex h-full items-center justify-center text-sm font-medium text-slate-400 bg-slate-50 rounded-xl border border-dashed border-slate-200">Chưa có dữ liệu thành viên</div>
              )}
            </div>
          </div>
        </div>
      </div>
    </section>
  )
}
