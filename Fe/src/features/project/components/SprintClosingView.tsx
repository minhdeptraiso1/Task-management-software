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
  UserRound
} from 'lucide-react'
import { Button, Input, Select } from '../../../components/ui'
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
  ComposedChart, Area, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer
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

  // Prepare Burndown data
  const burndownData = report.burndown?.points.map((p: any) => ({
    date: formatShortDate(p.date),
    'Thực tế': p.actualRemainingTasks,
    'Kỳ vọng': p.idealRemainingTasks
  })) ?? []

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
            <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-sm flex flex-col justify-between transition-shadow hover:shadow-md">
              <p className="text-xs font-semibold text-slate-500 mb-1">Mục tiêu Sprint</p>
              <div className="flex items-center justify-between mt-2">
                <p className="text-md font-bold text-slate-800">
                  {report.review ? (report.review.goalAchieved ? 'Đã hoàn thành' : 'Chưa đạt mục tiêu') : 'Chưa đánh giá'}
                </p>
                <div className={`p-1.5 rounded-lg border ${
                  report.review?.goalAchieved 
                    ? 'bg-emerald-50 border-emerald-200 text-emerald-600' 
                    : 'bg-rose-50 border-rose-200 text-rose-500'
                }`}>
                  <Trophy size={18} />
                </div>
              </div>
            </div>

            {/* Backlog Items Completion */}
            <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-sm flex flex-col justify-between transition-shadow hover:shadow-md">
              <p className="text-xs font-semibold text-slate-500 mb-1">Nghiệm thu Backlog</p>
              <div className="flex items-end justify-between mt-2">
                <div>
                  <div className="flex items-end gap-1.5">
                    <span className="text-2xl font-bold text-slate-800">{report.completedBacklogItemCount}</span>
                    <span className="text-xs font-medium text-slate-400 mb-0.5">/ {report.backlogItemCount} items</span>
                  </div>
                  <div className="w-24 h-1.5 bg-slate-100 rounded-full mt-2 overflow-hidden">
                    <div className="h-full bg-emerald-500 rounded-full" style={{ width: `${backlogCompletionRate}%` }} />
                  </div>
                </div>
                <div className="p-1.5 rounded-lg bg-emerald-50 border border-emerald-200 text-emerald-600">
                  <CheckCircle2 size={18} />
                </div>
              </div>
            </div>

            {/* Total Story Points */}
            <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-sm flex flex-col justify-between transition-shadow hover:shadow-md">
              <p className="text-xs font-semibold text-slate-500 mb-1">Quy mô Story Point</p>
              <div className="flex items-center justify-between mt-2">
                <p className="text-2xl font-bold text-slate-800">{report.totalStoryPoints} pt</p>
                <div className="p-1.5 rounded-lg bg-orange-50 border border-orange-200 text-brand">
                  <Target size={18} />
                </div>
              </div>
            </div>

            {/* Action Items count */}
            <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-sm flex flex-col justify-between transition-shadow hover:shadow-md">
              <p className="text-xs font-semibold text-slate-500 mb-1">Hành động cải tiến</p>
              <div className="flex items-center justify-between mt-2">
                <p className="text-2xl font-bold text-slate-800">
                  {report.retrospective?.actionItems.length || 0} việc
                </p>
                <div className="p-1.5 rounded-lg bg-indigo-50 border border-indigo-200 text-indigo-600">
                  <ListTodo size={18} />
                </div>
              </div>
            </div>
          </div>

          {/* Details & Burndown chart */}
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            <div className="lg:col-span-2 bg-white p-5 rounded-xl border border-slate-200 shadow-sm">
              <h4 className="font-bold text-sm text-slate-800 mb-4 flex items-center gap-1.5">
                <Activity size={16} className="text-brand" /> Biểu đồ Burndown của Sprint
              </h4>
              <div className="h-[270px] w-full">
                {burndownData.length > 0 ? (
                  <ResponsiveContainer width="100%" height="100%">
                    <ComposedChart data={burndownData} margin={{ top: 5, right: 10, left: -25, bottom: 5 }}>
                      <defs>
                        <linearGradient id="glow" x1="0" y1="0" x2="0" y2="1">
                          <stop offset="5%" stopColor="#f97316" stopOpacity={0.1}/>
                          <stop offset="95%" stopColor="#f97316" stopOpacity={0}/>
                        </linearGradient>
                      </defs>
                      <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f1f5f9" />
                      <XAxis dataKey="date" fontSize={11} tickLine={false} axisLine={false} stroke="#64748b" />
                      <YAxis fontSize={11} tickLine={false} axisLine={false} stroke="#64748b" />
                      <Tooltip contentStyle={{ borderRadius: '12px', border: '1px solid #e2e8f0', boxShadow: '0 4px 20px rgba(0,0,0,0.06)' }} />
                      <Legend wrapperStyle={{ fontSize: '11px', paddingTop: '10px' }} />
                      <Line type="monotone" name="Tasks Lý tưởng còn lại" dataKey="Kỳ vọng" stroke="#cbd5e1" strokeDasharray="4 4" strokeWidth={1.5} dot={false} />
                      <Area type="monotone" name="Tasks Thực tế còn lại" dataKey="Thực tế" fill="url(#glow)" stroke="#f97316" strokeWidth={2} dot={{ r: 3 }} />
                    </ComposedChart>
                  </ResponsiveContainer>
                ) : (
                  <div className="flex h-full items-center justify-center text-xs text-slate-400 bg-slate-50 rounded-xl border border-dashed border-slate-200">
                    Không có dữ liệu biểu đồ
                  </div>
                )}
              </div>
            </div>

            {/* Task Stats Card */}
            <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm flex flex-col justify-between">
              <div>
                <h4 className="font-bold text-sm text-slate-800 mb-4 flex items-center gap-1.5">
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
        <form onSubmit={handleSaveReview} className="space-y-5 bg-white p-5 rounded-xl border border-slate-200 shadow-sm animate-enter">
          <div className="border-b border-slate-100 pb-3 flex items-center justify-between">
            <div>
              <h4 className="font-bold text-md text-slate-800">Sprint Review (Nghiệm thu Sprint)</h4>
              <p className="text-xs text-slate-400 mt-0.5">Lưu lại kết quả demo nghiệm thu sản phẩm và thống nhất bàn giao công việc.</p>
            </div>
            <div className="size-8 rounded-lg bg-orange-50 text-brand flex items-center justify-center border border-brand/20">
              <Trophy size={16} />
            </div>
          </div>

          {/* Goal Achieved Switch Toggle (Aligned style) */}
          <div className="flex items-center gap-3 p-3 bg-brand-soft/20 rounded-xl border border-brand/10">
            <input
              type="checkbox"
              id="goalAchievedCheckbox"
              checked={goalAchieved}
              onChange={e => setGoalAchieved(e.target.checked)}
              disabled={!canManage || saving}
              className="size-4 rounded border-slate-300 text-brand focus:ring-brand cursor-pointer"
            />
            <label htmlFor="goalAchievedCheckbox" className="text-xs font-bold text-slate-700 cursor-pointer select-none">
              Hoàn thành mục tiêu của Sprint này
            </label>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="space-y-1.5">
              <label className="text-xs font-bold text-slate-600 flex items-center gap-1">
                <Target size={14} className="text-slate-400" /> Tóm tắt phần Demo trình bày sản phẩm
              </label>
              <textarea
                value={demoSummary}
                onChange={e => setDemoSummary(e.target.value)}
                disabled={!canManage || saving}
                placeholder="Nội dung demo, phản hồi kỹ thuật lúc chạy thử..."
                className="w-full text-xs p-3 rounded-lg border border-slate-300 focus:border-brand focus:ring-1 focus:ring-brand bg-white h-24 resize-none"
              />
            </div>

            <div className="space-y-1.5">
              <label className="text-xs font-bold text-slate-600 flex items-center gap-1">
                <UserRound size={14} className="text-slate-400" /> Ý kiến phản hồi từ các bên liên quan (Stakeholders)
              </label>
              <textarea
                value={stakeholderFeedback}
                onChange={e => setStakeholderFeedback(e.target.value)}
                disabled={!canManage || saving}
                placeholder="Đóng góp, đánh giá từ khách hàng, Product Owner..."
                className="w-full text-xs p-3 rounded-lg border border-slate-300 focus:border-brand focus:ring-1 focus:ring-brand bg-white h-24 resize-none"
              />
            </div>

            <div className="space-y-1.5">
              <label className="text-xs font-bold text-slate-600 flex items-center gap-1">
                <CheckCircle2 size={14} className="text-slate-400" /> Các công việc được nghiệm thu đóng lại (Accepted)
              </label>
              <textarea
                value={acceptedItemSummary}
                onChange={e => setAcceptedItemSummary(e.target.value)}
                disabled={!canManage || saving}
                placeholder="User stories hoặc nhiệm vụ được chấp nhận và hoàn thành..."
                className="w-full text-xs p-3 rounded-lg border border-slate-300 focus:border-brand focus:ring-1 focus:ring-brand bg-white h-24 resize-none"
              />
            </div>

            <div className="space-y-1.5">
              <label className="text-xs font-bold text-slate-600 flex items-center gap-1">
                <AlertTriangle size={14} className="text-slate-400" /> Công việc bị từ chối / cần chỉnh sửa (Rejected)
              </label>
              <textarea
                value={rejectedItemSummary}
                onChange={e => setRejectedItemSummary(e.target.value)}
                disabled={!canManage || saving}
                placeholder="Nhiệm vụ lỗi, chưa đạt chuẩn yêu cầu, cần cải thiện..."
                className="w-full text-xs p-3 rounded-lg border border-slate-300 focus:border-brand focus:ring-1 focus:ring-brand bg-white h-24 resize-none"
              />
            </div>
          </div>

          <div className="space-y-1.5">
            <label className="text-xs font-bold text-slate-600">Ghi chú & Thống nhất thêm</label>
            <textarea
              value={reviewNote}
              onChange={e => setReviewNote(e.target.value)}
              disabled={!canManage || saving}
              placeholder="Các lưu ý hoặc ghi chú khác..."
              className="w-full text-xs p-3 rounded-lg border border-slate-300 focus:border-brand focus:ring-1 focus:ring-brand bg-white h-20 resize-none"
            />
          </div>

          {canManage && (
            <div className="flex justify-end pt-3 border-t border-slate-100">
              <Button
                type="submit"
                variant="primary"
                leadingIcon={<Save size={16} />}
                disabled={saving}
              >
                {saving ? 'Đang lưu...' : 'Lưu đánh giá'}
              </Button>
            </div>
          )}
        </form>
      )}

      {activeTab === 'retrospective' && (
        <form onSubmit={handleSaveRetrospective} className="space-y-5 bg-white p-5 rounded-xl border border-slate-200 shadow-sm animate-enter">
          <div className="border-b border-slate-100 pb-3 flex items-center justify-between">
            <div>
              <h4 className="font-bold text-md text-slate-800">Sprint Retrospective (Họp cải tiến)</h4>
              <p className="text-xs text-slate-400 mt-0.5">Nhìn nhận lại quá trình phối hợp của đội ngũ để làm tốt hơn trong Sprint tiếp theo.</p>
            </div>
            <div className="size-8 rounded-lg bg-indigo-50 text-indigo-600 flex items-center justify-center border border-indigo-200">
              <MessageSquare size={16} />
            </div>
          </div>

          {/* Three pillars cards (Aligned style: White background, slate borders, color indicators) */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            {/* Went Well */}
            <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-sm space-y-2">
              <div className="flex items-center gap-1.5 text-emerald-600 font-bold text-xs">
                <Trophy size={14} /> Điểm làm tốt (Went Well)
              </div>
              <textarea
                value={wentWell}
                onChange={e => setWentWell(e.target.value)}
                disabled={!canManage || saving}
                placeholder="Những việc, quy trình đã hoạt động hiệu quả..."
                className="w-full text-xs p-3 rounded-lg border border-slate-300 focus:border-brand focus:ring-1 focus:ring-brand bg-white h-28 resize-none"
              />
            </div>

            {/* Went Wrong */}
            <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-sm space-y-2">
              <div className="flex items-center gap-1.5 text-rose-500 font-bold text-xs">
                <AlertTriangle size={14} /> Điểm chưa tốt (Went Wrong)
              </div>
              <textarea
                value={wentWrong}
                onChange={e => setWentWrong(e.target.value)}
                disabled={!canManage || saving}
                placeholder="Khó khăn, xung đột, ước lượng sai thời gian..."
                className="w-full text-xs p-3 rounded-lg border border-slate-300 focus:border-brand focus:ring-1 focus:ring-brand bg-white h-28 resize-none"
              />
            </div>

            {/* Improvement */}
            <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-sm space-y-2">
              <div className="flex items-center gap-1.5 text-indigo-600 font-bold text-xs">
                <Lightbulb size={14} /> Ý kiến cải tiến (Improvement)
              </div>
              <textarea
                value={improvement}
                onChange={e => setImprovement(e.target.value)}
                disabled={!canManage || saving}
                placeholder="Giải pháp, hành động khắc phục lỗi ở Sprint sau..."
                className="w-full text-xs p-3 rounded-lg border border-slate-300 focus:border-brand focus:ring-1 focus:ring-brand bg-white h-28 resize-none"
              />
            </div>
          </div>

          {/* Action Items Editor (Aligned style) */}
          <div className="space-y-3 pt-2">
            <div className="flex items-center justify-between border-b border-slate-100 pb-2">
              <div className="flex items-center gap-2">
                <div className="p-1 rounded-md bg-indigo-50 text-indigo-600 border border-indigo-200">
                  <ListTodo size={14} />
                </div>
                <label className="text-xs font-bold text-slate-800">
                  Hành động cụ thể cho Sprint tiếp theo (Action Items)
                </label>
              </div>
              {canManage && (
                <Button
                  type="button"
                  variant="outline-amber"
                  size="sm"
                  leadingIcon={<Plus size={14} />}
                  onClick={addActionItem}
                  disabled={saving}
                >
                  Thêm hành động
                </Button>
              )}
            </div>

            <div className="space-y-2">
              {actionItems.map((item, idx) => (
                <div 
                  key={idx} 
                  className="flex flex-col gap-2 p-3 rounded-xl border border-slate-200 bg-slate-50/50 md:flex-row md:items-center hover:bg-slate-50 transition-colors animate-enter"
                >
                  <div className="flex-1">
                    <Input
                      aria-label="Nội dung công việc cải tiến"
                      value={item.content}
                      onChange={e => updateActionItem(idx, 'content', e.target.value)}
                      disabled={!canManage || saving}
                      placeholder="Nội dung hành động cải tiến cụ thể..."
                      className="w-full bg-white text-xs"
                    />
                  </div>
                  <div className="w-full md:w-48 shrink-0">
                    <Select
                      aria-label="Người thực hiện"
                      value={item.assigneeUserId}
                      onChange={e => updateActionItem(idx, 'assigneeUserId', e.target.value)}
                      disabled={!canManage || saving}
                      className="w-full bg-white text-xs"
                      options={members.map(m => ({ label: m.username, value: m.userId }))}
                    />
                  </div>
                  <div className="w-full md:w-36 shrink-0">
                    <input
                      type="date"
                      value={item.dueDate || ''}
                      onChange={e => updateActionItem(idx, 'dueDate', e.target.value || null)}
                      disabled={!canManage || saving}
                      className="w-full rounded-lg border border-slate-300 px-3 py-2 text-xs focus:border-brand focus:ring-1 focus:ring-brand bg-white"
                    />
                  </div>
                  <div className="flex items-center gap-3 shrink-0 self-end md:self-auto">
                    <div className="flex items-center gap-1.5">
                      <input
                        type="checkbox"
                        id={`retro-done-${idx}`}
                        checked={item.done}
                        onChange={e => updateActionItem(idx, 'done', e.target.checked)}
                        disabled={!canManage || saving}
                        className="size-4 rounded border-slate-300 text-brand focus:ring-brand cursor-pointer"
                      />
                      <label htmlFor={`retro-done-${idx}`} className="text-[10px] font-bold text-slate-500 cursor-pointer select-none">
                        Đóng
                      </label>
                    </div>
                    {canManage && (
                      <Button
                        type="button"
                        variant="ghost"
                        size="sm"
                        iconOnly
                        leadingIcon={<Trash2 size={15} className="text-rose-500" />}
                        onClick={() => removeActionItem(idx)}
                        disabled={saving}
                      />
                    )}
                  </div>
                </div>
              ))}
              {actionItems.length === 0 && (
                <p className="text-xs text-slate-400 italic py-3 text-center">Chưa cấu hình hành động cải tiến nào.</p>
              )}
            </div>
          </div>

          <div className="space-y-1.5">
            <label className="text-xs font-bold text-slate-600">Ghi chú chung</label>
            <textarea
              value={retroNote}
              onChange={e => setRetroNote(e.target.value)}
              disabled={!canManage || saving}
              placeholder="Nhập các chú thích thảo luận..."
              className="w-full text-xs p-3.5 rounded-xl border border-slate-300 focus:border-brand focus:ring-2 focus:ring-brand/10 h-20 bg-white resize-none"
            />
          </div>

          {canManage && (
            <div className="flex justify-end pt-3 border-t border-slate-100">
              <Button
                type="submit"
                variant="primary"
                leadingIcon={<Save size={16} />}
                disabled={saving}
              >
                {saving ? 'Đang lưu...' : 'Lưu cải tiến'}
              </Button>
            </div>
          )}
        </form>
      )}
    </section>
  )
}
