import { useState, useEffect } from 'react'
import {
  ChevronLeft,
  RefreshCcw,
  Save,
  Trophy,
  Target,
  MessageSquare,
  Plus,
  Trash2,
  CheckCircle2,
  AlertCircle,
  Calendar,
  ListTodo,
  Lightbulb,
  AlertTriangle,
  FileText,
  Activity,
  UserRound,
  Pencil,
  Clock
} from 'lucide-react'
import { Button, Input, Modal } from '../../../components/ui'
import type { ProjectMember } from '../models/project.model'
import type {
  SprintClosingReportResponse,
  SprintRetroActionItemRequest
} from '../models/scrum.model'
import {
  getSprintClosingReport,
  updateSprintReview,
  updateSprintRetrospective
} from '../services/scrum.service'
import {
  ComposedChart, Area, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer
} from 'recharts'
import { formatShortDate } from '../../../utils/format'

interface SprintClosingViewProps {
  projectId: string
  sprintId: string
  members: ProjectMember[]
  canManage: boolean
  onBack: () => void
}

export function SprintClosingView({
  projectId,
  sprintId,
  members,
  canManage,
  onBack
}: SprintClosingViewProps) {
  const [activeTab, setActiveTab] = useState<'report' | 'review' | 'retrospective'>('report')
  const [report, setReport] = useState<SprintClosingReportResponse | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [saving, setSaving] = useState(false)
  const [saveSuccess, setSaveSuccess] = useState('')

  const [hiddenBurndownSeries, setHiddenBurndownSeries] = useState<Record<string, boolean>>({})
  const [chartViewMode, setChartViewMode] = useState<'increasing' | 'remaining'>('increasing')
  const [assigneeModalIdx, setAssigneeModalIdx] = useState<number | null>(null)
  const toggleBurndownSeries = (seriesName: string) => setHiddenBurndownSeries(prev => ({ ...prev, [seriesName]: !prev[seriesName] }))

  // Review Form State
  const [goalAchieved, setGoalAchieved] = useState(false)
  const [demoSummary, setDemoSummary] = useState('')
  const [stakeholderFeedback, setStakeholderFeedback] = useState('')
  const [acceptedItemSummary, setAcceptedItemSummary] = useState('')
  const [rejectedItemSummary, setRejectedItemSummary] = useState('')
  const [reviewNote, setReviewNote] = useState('')

  // Retrospective Form State
  const [wentWell, setWentWell] = useState('')
  const [wentWrong, setWentWrong] = useState('')
  const [improvement, setImprovement] = useState('')
  const [retroNote, setRetroNote] = useState('')
  const [actionItems, setActionItems] = useState<SprintRetroActionItemRequest[]>([])

  const loadClosingReport = async () => {
    setLoading(true)
    setError('')
    try {
      const data = await getSprintClosingReport(projectId, sprintId)
      setReport(data)
      
      // Initialize Review Form
      if (data.review) {
        setGoalAchieved(data.review.goalAchieved)
        setDemoSummary(data.review.demoSummary || '')
        setStakeholderFeedback(data.review.stakeholderFeedback || '')
        setAcceptedItemSummary(data.review.acceptedItemSummary || '')
        setRejectedItemSummary(data.review.rejectedItemSummary || '')
        setReviewNote(data.review.note || '')
      } else {
        setGoalAchieved(false)
        setDemoSummary('')
        setStakeholderFeedback('')
        setAcceptedItemSummary('')
        setRejectedItemSummary('')
        setReviewNote('')
      }

      // Initialize Retrospective Form
      if (data.retrospective) {
        setWentWell(data.retrospective.wentWell || '')
        setWentWrong(data.retrospective.wentWrong || '')
        setImprovement(data.retrospective.improvement || '')
        setRetroNote(data.retrospective.note || '')
        setActionItems(
          data.retrospective.actionItems.map(item => ({
            content: item.content,
            assigneeUserId: item.assigneeUserId,
            dueDate: item.dueDate,
            done: item.done
          }))
        )
      } else {
        setWentWell('')
        setWentWrong('')
        setImprovement('')
        setRetroNote('')
        setActionItems([])
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Lỗi khi tải báo cáo đóng Sprint')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    void loadClosingReport()
  }, [projectId, sprintId])

  const handleSaveReview = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!canManage) return
    setSaving(true)
    setSaveSuccess('')
    setError('')
    try {
      await updateSprintReview(projectId, sprintId, {
        goalAchieved,
        demoSummary: demoSummary || undefined,
        stakeholderFeedback: stakeholderFeedback || undefined,
        acceptedItemSummary: acceptedItemSummary || undefined,
        rejectedItemSummary: rejectedItemSummary || undefined,
        note: reviewNote || undefined
      })
      setSaveSuccess('Lưu Sprint Review thành công!')
      setTimeout(() => setSaveSuccess(''), 4000)
      const updatedReport = await getSprintClosingReport(projectId, sprintId)
      setReport(updatedReport)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Lỗi khi cập nhật Sprint Review')
    } finally {
      setSaving(false)
    }
  }

  const handleSaveRetrospective = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!canManage) return
    
    // Validate action items
    for (const item of actionItems) {
      if (!item.content.trim()) {
        setError('Nội dung hành động cải tiến không được để trống.')
        return
      }
      if (!item.assigneeUserId) {
        setError('Vui lòng chọn người thực hiện cho tất cả các hành động.')
        return
      }
    }

    setSaving(true)
    setSaveSuccess('')
    setError('')
    try {
      await updateSprintRetrospective(projectId, sprintId, {
        wentWell: wentWell || undefined,
        wentWrong: wentWrong || undefined,
        improvement: improvement || undefined,
        actionItems,
        note: retroNote || undefined
      })
      setSaveSuccess('Lưu Sprint Retrospective thành công!')
      setTimeout(() => setSaveSuccess(''), 4000)
      const updatedReport = await getSprintClosingReport(projectId, sprintId)
      setReport(updatedReport)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Lỗi khi cập nhật Sprint Retrospective')
    } finally {
      setSaving(false)
    }
  }

  const addActionItem = () => {
    setActionItems(curr => [
      ...curr,
      {
        content: '',
        assigneeUserId: members[0]?.userId || '',
        dueDate: null,
        done: false
      }
    ])
  }

  const removeActionItem = (index: number) => {
    setActionItems(curr => curr.filter((_, i) => i !== index))
  }

  const updateActionItem = (index: number, field: keyof SprintRetroActionItemRequest, value: any) => {
    setActionItems(curr =>
      curr.map((item, i) => (i === index ? { ...item, [field]: value } : item))
    )
  }

  const getMemberInitials = (userId: string) => {
    const member = members.find(m => m.userId === userId)
    if (!member) return '?'
    return member.username.slice(0, 2).toUpperCase()
  }

  const getMemberColorClass = (userId: string) => {
    const colors = [
      'bg-[#eff6ff] text-[#1d4ed8] border-[#bfdbfe]',
      'bg-[#f5f3ff] text-[#6d28d9] border-[#ddd6fe]',
      'bg-[#ecfdf5] text-[#047857] border-[#a7f3d0]',
      'bg-[#f0f9ff] text-[#0369a1] border-[#bae6fd]',
      'bg-[#fff1f2] text-[#be123c] border-[#fecdd3]',
      'bg-[#fff7ed] text-[#c2410c] border-[#ffedd5]'
    ]
    const index = userId.charCodeAt(0) % colors.length
    return colors[index]
  }

  if (loading && !report) {
    return (
      <div className="grid h-64 place-items-center bg-white rounded-2xl border border-brand-line/50 p-8 shadow-sm">
        <div className="text-center space-y-2">
          <span className="inline-block size-8 animate-spin rounded-full border-4 border-brand border-r-transparent" />
          <p className="text-sm font-medium text-slate-400">Đang tải báo cáo tổng kết...</p>
        </div>
      </div>
    )
  }

  if (error && !report) {
    return (
      <div className="p-6 text-center bg-white rounded-2xl border border-slate-200 space-y-3 shadow-sm max-w-md mx-auto my-6">
        <AlertCircle className="mx-auto size-8 text-rose-500" />
        <p className="text-sm font-semibold text-slate-700">{error}</p>
        <Button variant="secondary" size="sm" onClick={() => void loadClosingReport()}>Thử lại</Button>
      </div>
    )
  }

  if (!report) return null

  // Prepare Burndown & Increasing Progress data (Strict Whole Integers)
  const totalTasksCount = report.taskStatistics?.totalTasks || report.backlogItemCount || 1

  const burndownData = report.burndown?.points.map((p: any) => {
    const rawIdealRem = p.idealRemainingTasks ?? 0
    const rawActualRem = p.actualRemainingTasks ?? 0
    const idealRemInt = Math.max(0, Math.round(rawIdealRem))
    const actualRemInt = Math.max(0, Math.round(rawActualRem))

    // Calculate increasing cumulative progress
    const idealIncInt = Math.min(totalTasksCount, Math.max(0, Math.round(totalTasksCount - rawIdealRem)))
    const actualIncInt = Math.min(totalTasksCount, Math.max(0, Math.round(p.cumulativeCompletedTasks ?? (totalTasksCount - rawActualRem))))

    return {
      date: formatShortDate(p.date),
      'Kỳ vọng (Tăng dần)': idealIncInt,
      'Thực tế (Tăng dần)': actualIncInt,
      'Kỳ vọng': idealRemInt,
      'Thực tế': actualRemInt
    }
  }) ?? []

  const backlogCompletionRate = report.backlogItemCount > 0 
    ? (report.completedBacklogItemCount / report.backlogItemCount) * 100 
    : 0

  return (
    <section className="space-y-6 rounded-2xl border border-brand-line bg-gradient-to-br from-brand-soft/30 via-brand-cream/30 to-white p-5 shadow-[0_18px_45px_rgba(247,148,29,0.08)] animate-enter">
      {/* Header */}
      <div className="flex flex-col gap-3 rounded-xl border border-slate-200 bg-white p-4 shadow-sm lg:flex-row lg:items-center lg:justify-between">
        <div className="flex items-start gap-3">
          <Button variant="secondary" size="sm" leadingIcon={<ChevronLeft size={16} />} onClick={onBack}>Kanban Task</Button>
          <div>
            <h3 className="text-lg font-bold text-slate-800">Tổng kết Sprint: {report.sprintName}</h3>
            <p className="mt-0.5 text-sm text-slate-500">Xem báo cáo tổng kết, điền họp nghiệm thu (Review) và họp cải tiến (Retrospective).</p>
          </div>
        </div>
        <div className="flex items-center gap-2">
          {canManage && (
            <span className="text-xs font-bold text-brand bg-brand-soft px-3 py-1 rounded-full border border-brand/20">
              Quyền quản trị viên
            </span>
          )}
          <Button
            variant="outline-amber"
            size="sm"
            leadingIcon={<RefreshCcw size={16} />}
            onClick={() => void loadClosingReport()}
          >
            Làm mới
          </Button>
        </div>
      </div>

      {/* Notifications */}
      {saveSuccess && (
        <div className="p-4 rounded-xl border border-emerald-200 bg-emerald-50 text-emerald-800 flex items-center gap-3 animate-enter shadow-sm">
          <CheckCircle2 size={18} className="text-emerald-600 shrink-0" />
          <span className="text-sm font-semibold">{saveSuccess}</span>
        </div>
      )}
      {error && (
        <div className="p-4 rounded-xl border border-rose-200 bg-rose-50 text-rose-800 flex items-center gap-3 animate-enter shadow-sm">
          <AlertCircle size={18} className="text-rose-600 shrink-0" />
          <span className="text-sm font-semibold">{error}</span>
        </div>
      )}

      {/* Tabs */}
      <div className="flex border-b border-slate-200 bg-white/40 backdrop-blur-sm p-1 rounded-xl gap-2">
        <button
          type="button"
          className={`px-5 py-2.5 text-sm font-bold border-b-2 transition-all ${
            activeTab === 'report'
              ? 'border-brand text-brand'
              : 'border-transparent text-slate-500 hover:text-slate-700'
          }`}
          onClick={() => setActiveTab('report')}
        >
          Báo cáo đóng
        </button>
        <button
          type="button"
          className={`px-5 py-2.5 text-sm font-bold border-b-2 transition-all ${
            activeTab === 'review'
              ? 'border-brand text-brand'
              : 'border-transparent text-slate-500 hover:text-slate-700'
          }`}
          onClick={() => setActiveTab('review')}
        >
          Họp Review
        </button>
        <button
          type="button"
          className={`px-5 py-2.5 text-sm font-bold border-b-2 transition-all ${
            activeTab === 'retrospective'
              ? 'border-brand text-brand'
              : 'border-transparent text-slate-500 hover:text-slate-700'
          }`}
          onClick={() => setActiveTab('retrospective')}
        >
          Họp Retrospective
        </button>
      </div>

      {/* Tab Contents */}
      {activeTab === 'report' && (
        <div className="space-y-6">
          {/* Key Metrics Cards */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-5">
            {/* Goal Achieved Card */}
            {(() => {
              const isAchieved = report.review?.goalAchieved
              const isEvaluated = !!report.review
              const goalBadgeText = !isEvaluated ? 'CHƯA ĐÁNH GIÁ' : isAchieved ? 'ĐẠT MỤC TIÊU' : 'CHƯA ĐẠT'
              const goalBadgeClass = !isEvaluated 
                ? 'text-amber-700 bg-amber-50 border-amber-200'
                : isAchieved
                ? 'text-emerald-700 bg-emerald-50 border-emerald-200'
                : 'text-rose-600 bg-rose-50 border-rose-200'
              const goalText = !isEvaluated ? 'Chưa đánh giá' : isAchieved ? 'Đã hoàn thành' : 'Chưa đạt mục tiêu'
              const iconBgClass = !isEvaluated ? 'bg-amber-100/70 text-amber-700' : isAchieved ? 'bg-emerald-100/70 text-emerald-700' : 'bg-rose-100/70 text-rose-600'
              const barBgClass = !isEvaluated ? 'bg-amber-500' : isAchieved ? 'bg-emerald-500' : 'bg-rose-500'

              return (
                <div className="rounded-2xl border border-line/70 bg-white p-5 shadow-xs transition hover:shadow-md hover:-translate-y-0.5 flex flex-col justify-between space-y-2">
                  <div className="flex justify-between items-center">
                    <div className={`size-10 rounded-xl flex items-center justify-center shrink-0 ${iconBgClass}`}>
                      <Trophy size={20} />
                    </div>
                    <span className={`text-[10px] font-extrabold border px-2.5 py-0.5 rounded-full uppercase tracking-wider ${goalBadgeClass}`}>
                      {goalBadgeText}
                    </span>
                  </div>
                  <div>
                    <p className="text-[11px] font-bold text-muted uppercase tracking-wider">MỤC TIÊU SPRINT</p>
                    <p className="text-xl font-black text-ink mt-1 truncate">{goalText}</p>
                    <div className="w-full bg-slate-100 h-1.5 rounded-full overflow-hidden mt-3">
                      <div className={`h-full rounded-full ${barBgClass}`} style={{ width: !isEvaluated ? '50%' : isAchieved ? '100%' : '30%' }} />
                    </div>
                  </div>
                </div>
              )
            })()}

            {/* Backlog Items Completion */}
            <div className="rounded-2xl border border-line/70 bg-white p-5 shadow-xs transition hover:shadow-md hover:-translate-y-0.5 flex flex-col justify-between space-y-2">
              <div className="flex justify-between items-center">
                <div className="size-10 rounded-xl bg-emerald-100/70 text-emerald-700 flex items-center justify-center shrink-0">
                  <CheckCircle2 size={20} />
                </div>
                <span className="text-[10px] font-extrabold text-emerald-700 bg-emerald-50 border border-emerald-200 px-2.5 py-0.5 rounded-full uppercase tracking-wider">
                  HOÀN THÀNH
                </span>
              </div>
              <div>
                <p className="text-[11px] font-bold text-muted uppercase tracking-wider">NGHIỆM THU BACKLOG</p>
                <div className="flex items-baseline gap-1.5 mt-1">
                  <span className="text-3xl font-black text-ink">{report.completedBacklogItemCount}</span>
                  <span className="text-xs font-bold text-muted">/ {report.backlogItemCount} items</span>
                </div>
                <div className="w-full bg-slate-100 h-1.5 rounded-full overflow-hidden mt-3">
                  <div className="bg-emerald-500 h-full rounded-full transition-all duration-500" style={{ width: `${backlogCompletionRate}%` }} />
                </div>
              </div>
            </div>

            {/* Total Story Points */}
            <div className="rounded-2xl border border-line/70 bg-white p-5 shadow-xs transition hover:shadow-md hover:-translate-y-0.5 flex flex-col justify-between space-y-2">
              <div className="flex justify-between items-center">
                <div className="size-10 rounded-xl bg-amber-100/70 text-amber-700 flex items-center justify-center shrink-0">
                  <Target size={20} />
                </div>
                <span className="text-[10px] font-extrabold text-amber-700 bg-amber-50 border border-amber-200 px-2.5 py-0.5 rounded-full uppercase tracking-wider">
                  TỔNG ĐIỂM
                </span>
              </div>
              <div>
                <p className="text-[11px] font-bold text-muted uppercase tracking-wider">QUY MÔ STORY POINT</p>
                <p className="text-3xl font-black text-ink mt-1">{report.totalStoryPoints} pt</p>
                <div className="w-full bg-slate-100 h-1.5 rounded-full overflow-hidden mt-3">
                  <div className="bg-amber-500 h-full rounded-full" style={{ width: '100%' }} />
                </div>
              </div>
            </div>

            {/* Action Items count */}
            <div className="rounded-2xl border border-line/70 bg-white p-5 shadow-xs transition hover:shadow-md hover:-translate-y-0.5 flex flex-col justify-between space-y-2">
              <div className="flex justify-between items-center">
                <div className="size-10 rounded-xl bg-indigo-100/70 text-indigo-700 flex items-center justify-center shrink-0">
                  <ListTodo size={20} />
                </div>
                <span className="text-[10px] font-extrabold text-indigo-700 bg-indigo-50 border border-indigo-200 px-2.5 py-0.5 rounded-full uppercase tracking-wider">
                  RETROSPECTIVE
                </span>
              </div>
              <div>
                <p className="text-[11px] font-bold text-muted uppercase tracking-wider">HÀNH ĐỘNG CẢI TIẾN</p>
                <p className="text-3xl font-black text-ink mt-1">
                  {report.retrospective?.actionItems.length || 0} việc
                </p>
                <div className="w-full bg-slate-100 h-1.5 rounded-full overflow-hidden mt-3">
                  <div className="bg-indigo-500 h-full rounded-full" style={{ width: report.retrospective?.actionItems.length ? '100%' : '15%' }} />
                </div>
              </div>
            </div>
          </div>

          {/* Details & Burndown chart */}
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            <div className="lg:col-span-2 bg-white p-5 rounded-2xl border border-line/70 shadow-xs">
              <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3 mb-4">
                <h4 className="font-extrabold text-sm text-ink flex items-center gap-2">
                  <Activity size={16} className="text-brand" /> Biểu đồ Tiến độ Sprint
                </h4>
                <div className="flex items-center gap-1 bg-slate-100/80 p-1 rounded-xl border border-line/50 text-xs">
                  <button
                    type="button"
                    onClick={() => setChartViewMode('increasing')}
                    className={`px-3 py-1 font-bold rounded-lg transition-all cursor-pointer ${
                      chartViewMode === 'increasing'
                        ? 'bg-white text-brand shadow-2xs font-extrabold'
                        : 'text-muted hover:text-ink'
                    }`}
                  >
                    Tiến độ Tăng dần
                  </button>
                  <button
                    type="button"
                    onClick={() => setChartViewMode('remaining')}
                    className={`px-3 py-1 font-bold rounded-lg transition-all cursor-pointer ${
                      chartViewMode === 'remaining'
                        ? 'bg-white text-brand shadow-2xs font-extrabold'
                        : 'text-muted hover:text-ink'
                    }`}
                  >
                    Task Còn lại
                  </button>
                </div>
              </div>

              <div className="h-[270px] w-full">
                {burndownData.length > 0 ? (
                  <ResponsiveContainer width="100%" height="100%" debounce={50}>
                    <ComposedChart data={burndownData} margin={{ top: 5, right: 10, left: -25, bottom: 5 }}>
                      <defs>
                        <linearGradient id="glow" x1="0" y1="0" x2="0" y2="1">
                          <stop offset="5%" stopColor="#f97316" stopOpacity={0.15}/>
                          <stop offset="95%" stopColor="#f97316" stopOpacity={0}/>
                        </linearGradient>
                      </defs>
                      <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f1f5f9" />
                      <XAxis dataKey="date" fontSize={11} tickLine={false} axisLine={false} stroke="#64748b" />
                      <YAxis allowDecimals={false} fontSize={11} tickLine={false} axisLine={false} stroke="#64748b" />
                      <Tooltip 
                        isAnimationActive={false}
                        formatter={(val: any) => [`${Math.round(Number(val ?? 0))} task`, '']}
                        contentStyle={{ borderRadius: '12px', border: 'none', boxShadow: '0 4px 20px rgba(0,0,0,0.08)' }} 
                      />
                      <Line 
                        type="monotone" 
                        name={chartViewMode === 'increasing' ? 'Tasks Kỳ vọng (Tăng dần)' : 'Tasks Kỳ vọng còn lại'} 
                        dataKey={chartViewMode === 'increasing' ? 'Kỳ vọng (Tăng dần)' : 'Kỳ vọng'} 
                        stroke="#94a3b8" 
                        strokeDasharray="5 5" 
                        strokeWidth={2} 
                        dot={false} 
                        activeDot={false}
                        hide={!!hiddenBurndownSeries['Kỳ vọng']} 
                        isAnimationActive={true}
                        animationDuration={1200}
                        animationEasing="ease-in-out"
                      />
                      <Area 
                        type="monotone" 
                        name={chartViewMode === 'increasing' ? 'Tasks Thực tế (Tăng dần)' : 'Tasks Thực tế còn lại'} 
                        dataKey={chartViewMode === 'increasing' ? 'Thực tế (Tăng dần)' : 'Thực tế'} 
                        fill="url(#glow)" 
                        stroke="#f97316" 
                        strokeWidth={3} 
                        fillOpacity={0.4} 
                        dot={{ r: 4, strokeWidth: 2, fill: '#fff' }} 
                        activeDot={{ r: 6, strokeWidth: 0, fill: '#f97316' }}
                        hide={!!hiddenBurndownSeries['Thực tế']} 
                        isAnimationActive={true}
                        animationDuration={1200}
                        animationEasing="ease-in-out"
                      />
                    </ComposedChart>
                  </ResponsiveContainer>
                ) : (
                  <div className="flex h-full items-center justify-center text-xs text-slate-400 bg-slate-50 rounded-xl border border-dashed border-slate-200">
                    Không có dữ liệu biểu đồ
                  </div>
                )}
              </div>

              {/* Custom Legend chú thích tương tác (Click bật/tắt line) bên dưới biểu đồ */}
              <div className="flex flex-wrap items-center justify-center gap-3 pt-3 border-t border-slate-100 select-none">
                <button
                  type="button"
                  onClick={() => toggleBurndownSeries('Kỳ vọng')}
                  title="Bấm để ẩn/hiện đường Kỳ vọng"
                  className={`flex items-center gap-2 px-4 py-1.5 rounded-full border bg-white shadow-2xs text-xs font-bold transition-all cursor-pointer ${
                    hiddenBurndownSeries['Kỳ vọng']
                      ? 'border-slate-200 text-slate-400 opacity-50 line-through bg-slate-50'
                      : 'border-slate-300 text-slate-700 hover:border-slate-400 hover:shadow-xs'
                  }`}
                >
                  <span className={`size-2.5 rounded-full ${hiddenBurndownSeries['Kỳ vọng'] ? 'bg-slate-300' : 'bg-slate-400'}`} />
                  <span>{chartViewMode === 'increasing' ? 'Tasks Kỳ vọng (Tăng dần)' : 'Tasks Kỳ vọng còn lại'}</span>
                </button>

                <button
                  type="button"
                  onClick={() => toggleBurndownSeries('Thực tế')}
                  title="Bấm để ẩn/hiện đường Thực tế"
                  className={`flex items-center gap-2 px-4 py-1.5 rounded-full border bg-white shadow-2xs text-xs font-bold transition-all cursor-pointer ${
                    hiddenBurndownSeries['Thực tế']
                      ? 'border-slate-200 text-slate-400 opacity-50 line-through bg-slate-50'
                      : 'border-orange-300 text-slate-700 hover:border-orange-400 hover:shadow-xs'
                  }`}
                >
                  <span className={`size-2.5 rounded-full ${hiddenBurndownSeries['Thực tế'] ? 'bg-slate-300' : 'bg-orange-500'}`} />
                  <span>{chartViewMode === 'increasing' ? 'Tasks Thực tế (Tăng dần)' : 'Tasks Thực tế còn lại'}</span>
                </button>
              </div>
            </div>

            {/* Task Stats Card */}
            <div className="bg-white p-5 rounded-2xl border border-line/70 shadow-xs flex flex-col justify-between">
              <div>
                <h4 className="font-extrabold text-sm text-ink mb-4 flex items-center gap-2">
                  <FileText size={16} className="text-brand" /> Thống kê Nhiệm vụ (Task)
                </h4>
                {report.taskStatistics ? (
                  <div className="space-y-3 text-xs font-medium text-slate-600">
                    <div className="flex justify-between items-center py-2 border-b border-slate-100">
                      <span>Tổng số Task:</span>
                      <span className="font-bold text-slate-800">{report.taskStatistics.totalTasks}</span>
                    </div>
                    <div className="flex justify-between items-center py-2 border-b border-slate-100">
                      <span>Đã hoàn thành:</span>
                      <span className="font-bold text-emerald-600">{report.taskStatistics.completedTasks} ({Math.round(report.taskStatistics.completionRate)}%)</span>
                    </div>
                    <div className="flex justify-between items-center py-2 border-b border-slate-100">
                      <span>Chưa xong:</span>
                      <span className="font-bold text-slate-800">{report.taskStatistics.unfinishedTasks}</span>
                    </div>
                    <div className="flex justify-between items-center py-2 border-b border-slate-100">
                      <span>Nhiệm vụ quá hạn:</span>
                      <span className={`font-bold ${report.taskStatistics.overdueTasks > 0 ? 'text-rose-500' : 'text-slate-800'}`}>
                        {report.taskStatistics.overdueTasks}
                      </span>
                    </div>
                    <div className="flex justify-between items-center py-2 border-b border-slate-100">
                      <span>Nhiệm vụ bị chặn:</span>
                      <span className={`font-bold ${report.taskStatistics.blockedTasks > 0 ? 'text-orange-500' : 'text-slate-800'}`}>
                        {report.taskStatistics.blockedTasks}
                      </span>
                    </div>
                  </div>
                ) : (
                  <p className="text-xs text-slate-400 italic">Không có thống kê nhiệm vụ</p>
                )}
              </div>

              <div className="text-[10px] text-slate-400 italic mt-4 pt-3 border-t border-slate-100">
                * Thống kê dựa trên dữ liệu Task hoạt động thời gian thực tại Kanban.
              </div>
            </div>
          </div>

          {/* Action Items list in Report tab */}
          <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm">
            <h4 className="font-bold text-sm text-slate-800 mb-4 flex items-center gap-1.5">
              <ListTodo size={16} className="text-brand" /> Danh sách Hành động cải tiến đã đề xuất
            </h4>
            
            {report.retrospective && report.retrospective.actionItems.length > 0 ? (
              <div className="overflow-x-auto rounded-lg border border-slate-200/80">
                <table className="w-full min-w-[500px] text-left text-xs border-collapse">
                  <thead>
                    <tr className="bg-slate-50 text-slate-400 font-bold border-b border-slate-200/80">
                      <th className="px-4 py-2.5 w-8">#</th>
                      <th className="px-4 py-2.5">Hành động</th>
                      <th className="px-4 py-2.5">Người chịu trách nhiệm</th>
                      <th className="px-4 py-2.5">Hạn chót</th>
                      <th className="px-4 py-2.5 text-right">Trạng thái</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-100">
                    {report.retrospective.actionItems.map((item, idx) => (
                      <tr key={idx} className="hover:bg-slate-50/50">
                        <td className="px-4 py-3 text-slate-400 font-bold">{idx + 1}</td>
                        <td className="px-4 py-3 font-semibold text-slate-700">{item.content}</td>
                        <td className="px-4 py-3">
                          <div className="flex items-center gap-2">
                            <span className={`size-5 rounded-full text-[9px] font-bold flex items-center justify-center border shrink-0 ${getMemberColorClass(item.assigneeUserId)}`}>
                              {getMemberInitials(item.assigneeUserId)}
                            </span>
                            <span className="font-semibold text-slate-700">{item.assigneeUsername || 'Không rõ'}</span>
                          </div>
                        </td>
                        <td className="px-4 py-3">
                          {item.dueDate ? (
                            <span className="inline-flex items-center gap-1 bg-slate-50 border border-slate-200 rounded px-2 py-0.5 text-slate-500 font-bold">
                              <Calendar size={12} /> {item.dueDate}
                            </span>
                          ) : (
                            <span className="text-slate-400 italic">Chưa cấu hình</span>
                          )}
                        </td>
                        <td className="px-4 py-3 text-right">
                          <span className={`inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold border ${
                            item.done 
                              ? 'bg-emerald-50 border-emerald-100 text-emerald-700' 
                              : 'bg-orange-50 border-orange-100 text-orange-700'
                          }`}>
                            {item.done ? 'Hoàn thành' : 'Đang xử lý'}
                          </span>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            ) : (
              <p className="text-xs text-slate-400 italic">Chưa có hành động cải tiến nào cho Sprint này.</p>
            )}
          </div>
        </div>
      )}

      {activeTab === 'review' && (
        <form onSubmit={handleSaveReview} className="space-y-6 bg-white p-6 rounded-2xl border border-line/70 shadow-xs animate-enter">
          {/* Header */}
          <div className="border-b border-line/50 pb-4 flex items-center justify-between">
            <div>
              <h4 className="font-extrabold text-base text-ink">Sprint Review (Nghiệm thu Sprint)</h4>
              <p className="text-xs font-medium text-muted mt-0.5">Lưu lại kết quả demo nghiệm thu sản phẩm và thống nhất bàn giao công việc.</p>
            </div>
            <div className="size-10 rounded-xl bg-amber-100/70 text-amber-700 flex items-center justify-center shrink-0">
              <Trophy size={20} />
            </div>
          </div>

          {/* Goal Achieved Status Control Banner */}
          <div className={`p-5 rounded-2xl border flex flex-col lg:flex-row items-start lg:items-center justify-between gap-4 transition-all duration-300 shadow-2xs ${
            goalAchieved 
              ? 'bg-gradient-to-r from-emerald-500/15 via-emerald-500/5 to-white border-emerald-500/35 ring-1 ring-emerald-500/20'
              : 'bg-gradient-to-r from-amber-500/15 via-amber-500/5 to-white border-amber-500/35 ring-1 ring-amber-500/20'
          }`}>
            <div className="flex items-center gap-4">
              <div className={`size-11 rounded-2xl flex items-center justify-center shrink-0 shadow-2xs transition-transform duration-300 ${
                goalAchieved 
                  ? 'bg-emerald-500 text-white shadow-emerald-500/20 scale-105' 
                  : 'bg-amber-500 text-white shadow-amber-500/20'
              }`}>
                {goalAchieved ? <Trophy size={22} /> : <Target size={22} />}
              </div>
              <div>
                <div className="flex items-center gap-2">
                  <span className={`text-[10px] font-extrabold uppercase px-2.5 py-0.5 rounded-full border tracking-wider ${
                    goalAchieved 
                      ? 'bg-emerald-100/80 text-emerald-800 border-emerald-200' 
                      : 'bg-amber-100/80 text-amber-800 border-amber-200'
                  }`}>
                    {goalAchieved ? 'ĐÁNH GIÁ: ĐẠT MỤC TIÊU' : 'ĐÁNH GIÁ: CHƯA ĐẠT'}
                  </span>
                </div>
                <p className={`text-sm font-extrabold mt-1 ${goalAchieved ? 'text-emerald-900' : 'text-amber-900'}`}>
                  {goalAchieved ? 'Sprint đã hoàn thành trọn vẹn mục tiêu đề ra!' : 'Mục tiêu Sprint chưa đạt'}
                </p>
                <p className="text-xs font-medium text-slate-500 mt-0.5">
                  {goalAchieved 
                    ? 'Sản phẩm đã được nghiệm thu và xác nhận bàn giao bởi Product Owner & Stakeholders.' 
                    : 'Chọn trạng thái bên phải để xác nhận đánh giá kết quả nghiệm thu Sprint chính thức.'}
                </p>
              </div>
            </div>

            {/* Compact Pill Switch with Animated Sliding Pill Background matching Image 2 */}
            {canManage && (
              <div className="relative inline-grid grid-cols-2 bg-[#eef2f6] p-0.5 rounded-full border border-slate-200/80 shrink-0 self-start sm:self-auto shadow-inner w-56">
                {/* Sliding pill indicator */}
                <div 
                  className={`absolute top-0.5 bottom-0.5 rounded-full shadow-xs transition-all duration-300 ease-out ${
                    goalAchieved ? 'bg-emerald-600 shadow-emerald-500/20' : 'bg-amber-500 shadow-amber-500/20'
                  }`}
                  style={{
                    left: goalAchieved ? '2px' : 'calc(50% + 1px)',
                    width: 'calc(50% - 3px)'
                  }}
                />

                <button
                  type="button"
                  onClick={() => setGoalAchieved(true)}
                  disabled={!canManage || saving}
                  className={`relative z-10 inline-flex items-center justify-center gap-1 rounded-full px-2.5 py-1 text-[11px] font-bold whitespace-nowrap transition-colors duration-300 cursor-pointer ${
                    goalAchieved ? 'text-white' : 'text-slate-600 hover:text-slate-900'
                  }`}
                >
                  <CheckCircle2 size={13} className={goalAchieved ? 'text-white' : 'text-emerald-600'} />
                  <span>Đạt mục tiêu</span>
                </button>

                <button
                  type="button"
                  onClick={() => setGoalAchieved(false)}
                  disabled={!canManage || saving}
                  className={`relative z-10 inline-flex items-center justify-center gap-1 rounded-full px-2.5 py-1 text-[11px] font-bold whitespace-nowrap transition-colors duration-300 cursor-pointer ${
                    !goalAchieved ? 'text-white' : 'text-slate-600 hover:text-slate-900'
                  }`}
                >
                  <Target size={13} className={!goalAchieved ? 'text-white' : 'text-amber-600'} />
                  <span>Chưa đạt</span>
                </button>
              </div>
            )}
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
            {/* Demo Summary */}
            <div className="space-y-2">
              <label className="text-[11px] font-extrabold text-muted uppercase tracking-wider flex items-center gap-2">
                <span className="size-6 rounded-lg bg-amber-100/70 text-amber-700 flex items-center justify-center">
                  <Target size={13} />
                </span>
                Tóm tắt phần Demo trình bày sản phẩm
              </label>
              <textarea
                value={demoSummary}
                onChange={e => setDemoSummary(e.target.value)}
                disabled={!canManage || saving}
                placeholder="Nội dung demo, phản hồi kỹ thuật lúc chạy thử sản phẩm..."
                className="w-full text-xs p-3.5 rounded-xl border border-line/80 focus:border-brand focus:ring-2 focus:ring-brand/20 bg-slate-50/50 focus:bg-white transition-all text-ink font-medium h-28 resize-none shadow-2xs placeholder:text-muted-dark/50"
              />
            </div>

            {/* Stakeholder Feedback */}
            <div className="space-y-2">
              <label className="text-[11px] font-extrabold text-muted uppercase tracking-wider flex items-center gap-2">
                <span className="size-6 rounded-lg bg-indigo-100/70 text-indigo-700 flex items-center justify-center">
                  <UserRound size={13} />
                </span>
                Ý kiến phản hồi từ các bên liên quan (Stakeholders)
              </label>
              <textarea
                value={stakeholderFeedback}
                onChange={e => setStakeholderFeedback(e.target.value)}
                disabled={!canManage || saving}
                placeholder="Đóng góp, đánh giá từ khách hàng, Product Owner, Ban giám đốc..."
                className="w-full text-xs p-3.5 rounded-xl border border-line/80 focus:border-brand focus:ring-2 focus:ring-brand/20 bg-slate-50/50 focus:bg-white transition-all text-ink font-medium h-28 resize-none shadow-2xs placeholder:text-muted-dark/50"
              />
            </div>

            {/* Accepted Items */}
            <div className="space-y-2">
              <label className="text-[11px] font-extrabold text-muted uppercase tracking-wider flex items-center gap-2">
                <span className="size-6 rounded-lg bg-emerald-100/70 text-emerald-700 flex items-center justify-center">
                  <CheckCircle2 size={13} />
                </span>
                Các công việc được nghiệm thu đóng lại (Accepted)
              </label>
              <textarea
                value={acceptedItemSummary}
                onChange={e => setAcceptedItemSummary(e.target.value)}
                disabled={!canManage || saving}
                placeholder="Danh sách User Stories hoặc nhiệm vụ được chấp nhận hoàn thành..."
                className="w-full text-xs p-3.5 rounded-xl border border-line/80 focus:border-brand focus:ring-2 focus:ring-brand/20 bg-slate-50/50 focus:bg-white transition-all text-ink font-medium h-28 resize-none shadow-2xs placeholder:text-muted-dark/50"
              />
            </div>

            {/* Rejected Items */}
            <div className="space-y-2">
              <label className="text-[11px] font-extrabold text-muted uppercase tracking-wider flex items-center gap-2">
                <span className="size-6 rounded-lg bg-rose-100/70 text-rose-600 flex items-center justify-center">
                  <AlertTriangle size={13} />
                </span>
                Công việc bị từ chối / cần chỉnh sửa (Rejected)
              </label>
              <textarea
                value={rejectedItemSummary}
                onChange={e => setRejectedItemSummary(e.target.value)}
                disabled={!canManage || saving}
                placeholder="Nhiệm vụ lỗi, chưa đạt chuẩn yêu cầu, cần chuyển sang Sprint sau..."
                className="w-full text-xs p-3.5 rounded-xl border border-line/80 focus:border-brand focus:ring-2 focus:ring-brand/20 bg-slate-50/50 focus:bg-white transition-all text-ink font-medium h-28 resize-none shadow-2xs placeholder:text-muted-dark/50"
              />
            </div>
          </div>

          <div className="space-y-2">
            <label className="text-[11px] font-extrabold text-muted uppercase tracking-wider">Ghi chú & Thống nhất thêm</label>
            <textarea
              value={reviewNote}
              onChange={e => setReviewNote(e.target.value)}
              disabled={!canManage || saving}
              placeholder="Các lưu ý hoặc ghi chú bổ sung trong buổi họp nghiệm thu..."
              className="w-full text-xs p-3.5 rounded-xl border border-line/80 focus:border-brand focus:ring-2 focus:ring-brand/20 bg-slate-50/50 focus:bg-white transition-all text-ink font-medium h-24 resize-none shadow-2xs placeholder:text-muted-dark/50"
            />
          </div>

          {canManage && (
            <div className="flex justify-end pt-4 border-t border-line/50">
              <Button
                type="submit"
                variant="primary"
                size="md"
                leadingIcon={<Save size={16} />}
                disabled={saving}
                className="!px-6 font-bold shadow-sm"
              >
                {saving ? 'Đang lưu...' : 'Lưu đánh giá Sprint Review'}
              </Button>
            </div>
          )}
        </form>
      )}

      {activeTab === 'retrospective' && (
        <form onSubmit={handleSaveRetrospective} className="space-y-6 bg-white p-6 rounded-2xl border border-line/70 shadow-xs animate-enter">
          {/* Header */}
          <div className="border-b border-line/50 pb-4 flex items-center justify-between">
            <div>
              <h4 className="font-extrabold text-base text-ink">Sprint Retrospective (Họp cải tiến)</h4>
              <p className="text-xs font-medium text-muted mt-0.5">Nhìn nhận lại quá trình phối hợp của đội ngũ để làm tốt hơn trong Sprint tiếp theo.</p>
            </div>
            <div className="size-10 rounded-xl bg-indigo-100/70 text-indigo-700 flex items-center justify-center shrink-0">
              <MessageSquare size={20} />
            </div>
          </div>

          {/* Three pillars cards with color headers and badges */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
            {/* Went Well */}
            <div className="rounded-2xl border border-emerald-200/80 bg-white shadow-2xs overflow-hidden transition-all hover:shadow-xs">
              <div className="bg-emerald-50/80 px-4 py-3 border-b border-emerald-100 flex items-center justify-between">
                <div className="flex items-center gap-2 text-xs font-extrabold text-emerald-800">
                  <CheckCircle2 size={16} className="text-emerald-600" /> Điểm làm tốt (Went Well)
                </div>
                <span className="text-[10px] font-extrabold text-emerald-700 bg-white border border-emerald-200 px-2 py-0.5 rounded-full uppercase tracking-wider">
                  KHUYẾN KHÍCH
                </span>
              </div>
              <textarea
                value={wentWell}
                onChange={e => setWentWell(e.target.value)}
                disabled={!canManage || saving}
                placeholder="Những việc, quy trình đã hoạt động hiệu quả và cần duy trì..."
                className="w-full text-xs p-3.5 border-0 focus:ring-0 focus:outline-none bg-transparent text-ink font-medium h-32 resize-none placeholder:text-emerald-700/40"
              />
            </div>

            {/* Went Wrong */}
            <div className="rounded-2xl border border-rose-200/80 bg-white shadow-2xs overflow-hidden transition-all hover:shadow-xs">
              <div className="bg-rose-50/80 px-4 py-3 border-b border-rose-100 flex items-center justify-between">
                <div className="flex items-center gap-2 text-xs font-extrabold text-rose-800">
                  <AlertTriangle size={16} className="text-rose-600" /> Điểm chưa tốt (Went Wrong)
                </div>
                <span className="text-[10px] font-extrabold text-rose-700 bg-white border border-rose-200 px-2 py-0.5 rounded-full uppercase tracking-wider">
                  CẦN KHẮC PHỤC
                </span>
              </div>
              <textarea
                value={wentWrong}
                onChange={e => setWentWrong(e.target.value)}
                disabled={!canManage || saving}
                placeholder="Khó khăn, xung đột, ước lượng sai thời gian hoặc sự cố kỹ thuật..."
                className="w-full text-xs p-3.5 border-0 focus:ring-0 focus:outline-none bg-transparent text-ink font-medium h-32 resize-none placeholder:text-rose-700/40"
              />
            </div>

            {/* Improvement */}
            <div className="rounded-2xl border border-sky-200/80 bg-white shadow-2xs overflow-hidden transition-all hover:shadow-xs">
              <div className="bg-sky-50/80 px-4 py-3 border-b border-sky-100 flex items-center justify-between">
                <div className="flex items-center gap-2 text-xs font-extrabold text-sky-800">
                  <Lightbulb size={16} className="text-sky-600" /> Ý kiến cải tiến (Improvement)
                </div>
                <span className="text-[10px] font-extrabold text-sky-700 bg-white border border-sky-200 px-2 py-0.5 rounded-full uppercase tracking-wider">
                  HÀNH ĐỘNG
                </span>
              </div>
              <textarea
                value={improvement}
                onChange={e => setImprovement(e.target.value)}
                disabled={!canManage || saving}
                placeholder="Giải pháp cụ thể, hành động khắc phục lỗi ở các Sprint sau..."
                className="w-full text-xs p-3.5 border-0 focus:ring-0 focus:outline-none bg-transparent text-ink font-medium h-32 resize-none placeholder:text-sky-700/40"
              />
            </div>
          </div>

          {/* Action Items Editor */}
          <div className="space-y-4 pt-2">
            <div className="flex items-center justify-between border-b border-line/50 pb-3">
              <div className="flex items-center gap-2.5">
                <div className="size-8 rounded-lg bg-indigo-100/70 text-indigo-700 flex items-center justify-center shrink-0">
                  <ListTodo size={16} />
                </div>
                <div>
                  <h5 className="text-xs font-extrabold text-ink">
                    Hành động cụ thể cho Sprint tiếp theo (Action Items)
                  </h5>
                  <p className="text-[11px] font-medium text-muted">Phân công chi tiết người phụ trách và hạn chót hoàn thành.</p>
                </div>
              </div>
              {canManage && (
                <Button
                  type="button"
                  variant="outline-amber"
                  size="sm"
                  leadingIcon={<Plus size={14} />}
                  onClick={addActionItem}
                  disabled={saving}
                  className="font-bold"
                >
                  Thêm hành động
                </Button>
              )}
            </div>

            <div className="space-y-3">
              {actionItems.map((item, idx) => {
                const assignedMember = members.find(m => m.userId === item.assigneeUserId)
                return (
                  <div 
                    key={idx} 
                    className="flex flex-col md:flex-row items-stretch md:items-center justify-between gap-3.5 p-4 rounded-2xl border border-line/70 bg-white hover:border-brand/40 transition-all shadow-2xs animate-enter"
                  >
                    <div className="flex-1 min-w-0">
                      <Input
                        aria-label="Nội dung công việc cải tiến"
                        value={item.content}
                        onChange={e => updateActionItem(idx, 'content', e.target.value)}
                        disabled={!canManage || saving}
                        placeholder="Nội dung hành động cải tiến cụ thể..."
                        className="w-full bg-slate-50/50 hover:bg-white text-xs font-medium"
                      />
                    </div>
                    
                    {/* Assignee Pill Button (Fixed width w-44 for 100% column alignment) */}
                    <button
                      type="button"
                      onClick={() => setAssigneeModalIdx(idx)}
                      disabled={!canManage || saving}
                      title="Bấm để phân công người thực hiện"
                      className="w-44 rounded-xl px-3 py-1.5 text-xs font-bold inline-flex items-center justify-between gap-1.5 transition hover:scale-[1.02] active:scale-95 hover:shadow-xs cursor-pointer bg-slate-100/90 text-slate-700 border border-slate-200 shrink-0 self-start md:self-auto"
                    >
                      <div className="flex items-center gap-1.5 min-w-0">
                        <div className="size-4.5 rounded-full bg-slate-800 text-white font-extrabold text-[9px] flex items-center justify-center shrink-0">
                          {getMemberInitials(item.assigneeUserId)}
                        </div>
                        <span className="font-extrabold text-xs truncate">{assignedMember?.username || 'Chưa gán'}</span>
                      </div>
                      <Pencil size={11} className="opacity-70 shrink-0 text-slate-400" />
                    </button>

                    <div className="w-full md:w-36 shrink-0">
                      <input
                        type="date"
                        value={item.dueDate || ''}
                        onChange={e => updateActionItem(idx, 'dueDate', e.target.value || null)}
                        disabled={!canManage || saving}
                        className="w-full rounded-xl border border-line/80 px-3 py-1.5 text-xs focus:border-brand focus:ring-2 focus:ring-brand/20 bg-slate-50/50 hover:bg-white text-ink font-medium shadow-2xs"
                      />
                    </div>
                    <div className="flex items-center gap-2.5 shrink-0 self-end md:self-auto">
                      {/* Status Button with FIXED WIDTH w-[130px] to prevent layout shift */}
                      <button
                        type="button"
                        onClick={() => updateActionItem(idx, 'done', !item.done)}
                        disabled={!canManage || saving}
                        title="Bấm để đổi trạng thái"
                        className={`w-[130px] inline-flex items-center justify-center gap-1.5 py-1.5 rounded-xl border text-xs font-extrabold transition-all cursor-pointer shadow-2xs active:scale-95 shrink-0 ${
                          item.done 
                            ? 'bg-emerald-50 border-emerald-200 text-emerald-700 hover:bg-emerald-100' 
                            : 'bg-amber-50 border-amber-200 text-amber-700 hover:bg-amber-100'
                        }`}
                      >
                        {item.done ? <CheckCircle2 size={14} className="text-emerald-600 shrink-0" /> : <Clock size={14} className="text-amber-600 shrink-0" />}
                        <span>{item.done ? 'Hoàn thành' : 'Đang xử lý'}</span>
                      </button>
                      {canManage && (
                        <Button
                          type="button"
                          variant="ghost"
                          size="sm"
                          iconOnly
                          title="Xóa hành động"
                          leadingIcon={<Trash2 size={15} className="text-rose-500 hover:text-rose-700" />}
                          onClick={() => removeActionItem(idx)}
                          disabled={saving}
                        />
                      )}
                    </div>
                  </div>
                )
              })}
              {actionItems.length === 0 && (
                <div className="text-xs text-muted italic py-6 text-center rounded-2xl border border-dashed border-line/80 bg-slate-50/50">
                  Chưa cấu hình hành động cải tiến nào cho Sprint này.
                </div>
              )}
            </div>
          </div>

          <div className="space-y-2">
            <label className="text-[11px] font-extrabold text-muted uppercase tracking-wider">Ghi chú chung</label>
            <textarea
              value={retroNote}
              onChange={e => setRetroNote(e.target.value)}
              disabled={!canManage || saving}
              placeholder="Nhập các chú thích thảo luận chung trong buổi họp cải tiến..."
              className="w-full text-xs p-3.5 rounded-xl border border-line/80 focus:border-brand focus:ring-2 focus:ring-brand/20 h-24 bg-slate-50/50 focus:bg-white transition-all text-ink font-medium resize-none shadow-2xs placeholder:text-muted-dark/50"
            />
          </div>

          {canManage && (
            <div className="flex justify-end pt-4 border-t border-line/50">
              <Button
                type="submit"
                variant="primary"
                size="md"
                leadingIcon={<Save size={16} />}
                disabled={saving}
                className="!px-6 font-bold shadow-sm"
              >
                {saving ? 'Đang lưu...' : 'Lưu họp Retrospective'}
              </Button>
            </div>
          )}
        </form>
      )}

      {/* Modal Phân công Người xử lý Hành động Cải tiến (Matching Screenshot 2) */}
      {assigneeModalIdx !== null && actionItems[assigneeModalIdx] && (
        <Modal
          open={assigneeModalIdx !== null}
          onClose={() => setAssigneeModalIdx(null)}
          title={`Phân công Người xử lý (${actionItems[assigneeModalIdx].content ? `"${actionItems[assigneeModalIdx].content.slice(0, 22)}..."` : `#${assigneeModalIdx + 1}`})`}
        >
          <div className="space-y-4">
            <p className="text-xs font-semibold text-muted">
              Hành động: <span className="font-bold text-ink">{actionItems[assigneeModalIdx].content || `Hành động #${assigneeModalIdx + 1}`}</span>
            </p>

            <div className="space-y-2 pt-2">
              <label className="text-[11px] font-extrabold text-muted uppercase tracking-wider block">CHỌN THÀNH VIÊN XỬ LÝ</label>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5 max-h-60 overflow-y-auto pr-1">
                {/* Option: Unassigned */}
                <button
                  type="button"
                  onClick={() => {
                    updateActionItem(assigneeModalIdx, 'assigneeUserId', '')
                    setAssigneeModalIdx(null)
                  }}
                  className={`p-3 rounded-xl border text-xs font-bold text-left transition flex items-center justify-between cursor-pointer ${
                    !actionItems[assigneeModalIdx].assigneeUserId 
                      ? 'border-slate-500 bg-slate-100 text-slate-800 ring-2 ring-slate-400/40 shadow-xs' 
                      : 'border-line bg-white hover:bg-slate-50 text-slate-700 hover:border-slate-300'
                  }`}
                >
                  <div className="flex items-center gap-2">
                    <div className="size-7 rounded-full bg-slate-200 text-slate-600 font-bold text-xs flex items-center justify-center">
                      ?
                    </div>
                    <span className="font-bold">Chưa gán</span>
                  </div>
                  {!actionItems[assigneeModalIdx].assigneeUserId && <CheckCircle2 size={17} className="shrink-0 text-current" />}
                </button>

                {/* Members list */}
                {members.map(m => {
                  const isCurrent = actionItems[assigneeModalIdx].assigneeUserId === m.userId
                  return (
                    <button
                      key={m.userId}
                      type="button"
                      onClick={() => {
                        updateActionItem(assigneeModalIdx, 'assigneeUserId', m.userId)
                        setAssigneeModalIdx(null)
                      }}
                      className={`p-3 rounded-xl border text-xs font-bold text-left transition flex items-center justify-between cursor-pointer ${
                        isCurrent 
                          ? 'border-indigo-500 bg-indigo-50/90 text-indigo-800 ring-2 ring-indigo-400/40 shadow-xs' 
                          : 'border-line bg-white hover:bg-slate-50 text-slate-700 hover:border-slate-300'
                      }`}
                    >
                      <div className="flex items-center gap-2 min-w-0">
                        <div className="size-7 rounded-full bg-slate-800 text-white font-bold text-xs flex items-center justify-center shrink-0">
                          {m.username.substring(0, 1).toUpperCase()}
                        </div>
                        <div className="truncate">
                          <p className="font-bold text-ink truncate">{m.username}</p>
                          <p className="text-[10px] font-normal text-muted truncate">{m.email || 'Thành viên'}</p>
                        </div>
                      </div>
                      {isCurrent && <CheckCircle2 size={17} className="shrink-0 text-indigo-600" />}
                    </button>
                  )
                })}
              </div>
            </div>

            <div className="flex justify-end pt-4 border-t border-line/60">
              <Button variant="danger" onClick={() => setAssigneeModalIdx(null)}>
                Hủy bỏ
              </Button>
            </div>
          </div>
        </Modal>
      )}
    </section>
  )
}
