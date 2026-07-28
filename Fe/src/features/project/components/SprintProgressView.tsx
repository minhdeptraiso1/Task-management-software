import { Button } from '../../../components/ui'
import { ChevronLeft, RefreshCcw, Bell, AlertTriangle, AlertCircle, Clock, Lock } from 'lucide-react'
import { useState } from 'react'
import { 
  Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer, Area, ComposedChart
} from 'recharts'
import type { SprintProgress } from '../models/scrum.model'
import { remindSprintEnding, remindSprintOverdueTasks, remindSprintBlockedTasks } from '../services/scrum.service'

interface SprintProgressViewProps {
  progress: SprintProgress | null
  onBack?: () => void
  onRefresh?: () => void
}

export function SprintProgressView({
  progress,
  onBack,
  onRefresh
}: SprintProgressViewProps) {
  const [isRefreshAnim, setIsRefreshAnim] = useState(false)
  const [isReminding, setIsReminding] = useState(false)
  const [reminderMessage, setReminderMessage] = useState<{ text: string, type: 'success' | 'error' } | null>(null)

  if (!progress) return null

  const handleRefreshClick = () => {
    setIsRefreshAnim(true)
    setTimeout(() => setIsRefreshAnim(false), 500)
    if (onRefresh) onRefresh()
  }

  const handleRemind = async (
    _type: 'ending' | 'overdue' | 'blocked', 
    apiCall: (projectId: string, sprintId: string) => Promise<any>
  ) => {
    setIsReminding(true)
    setReminderMessage(null)
    try {
      const res = await apiCall(progress.projectId, progress.sprintId)
      setReminderMessage({ text: res?.message || 'Gửi nhắc nhở thành công', type: 'success' })
      setTimeout(() => setReminderMessage(null), 5000)
    } catch (err: any) {
      setReminderMessage({ text: err.message || 'Có lỗi xảy ra khi gửi nhắc nhở', type: 'error' })
    } finally {
      setIsReminding(false)
    }
  }

  // format date from YYYY-MM-DD to DD/MM
  const chartData = progress.dailyProgress.map(d => {
    const parts = d.date.split('-')
    const shortDate = parts.length === 3 ? `${parts[2]}/${parts[1]}` : d.date
    const total = progress.totalTasks || 1
    const actualProgress = (d.cumulativeCompletedTasks / total) * 100
    const expectedProgress = ((total - d.idealRemainingTasks) / total) * 100
    return {
      date: shortDate,
      'Thực tế (%)': Math.round(actualProgress * 10) / 10,
      'Kỳ vọng (%)': Math.round(expectedProgress * 10) / 10
    }
  })

  return (
    <section className="space-y-6 rounded-2xl border border-brand-line bg-gradient-to-br from-brand-soft/30 via-brand-cream/30 to-white p-5 shadow-[0_18px_45px_rgba(247,148,29,0.08)] animate-enter">
      {/* Header */}
      <div className="flex flex-col gap-3 rounded-xl border border-slate-200 bg-white p-4 shadow-sm lg:flex-row lg:items-center lg:justify-between">
        <div className="flex items-start gap-3">
          {onBack && <Button variant="secondary" size="sm" leadingIcon={<ChevronLeft size={16} />} onClick={onBack}>Kanban Task</Button>}
          <div>
            <h3 className="text-lg font-bold text-slate-800">Tiến độ Sprint: {progress.sprintName}</h3>
            <p className="mt-0.5 text-sm text-slate-500">Báo cáo tiến độ hoàn thành theo ngày so với kế hoạch.</p>
          </div>
        </div>
        <div className="flex items-center gap-2">
          {onRefresh && (
            <Button variant="outline-amber" size="sm" leadingIcon={<RefreshCcw size={16} className={`transition-transform duration-500 ${isRefreshAnim ? 'rotate-[360deg]' : ''}`} />} onClick={handleRefreshClick}>
              Làm mới
            </Button>
          )}
        </div>
      </div>

      {reminderMessage && (
        <div className={`p-4 rounded-xl border flex items-center gap-3 ${reminderMessage.type === 'success' ? 'bg-emerald-50 border-emerald-200 text-emerald-800' : 'bg-red-50 border-red-200 text-red-800'}`}>
          {reminderMessage.type === 'success' ? <Bell size={18} /> : <AlertCircle size={18} />}
          <span className="font-semibold text-sm">{reminderMessage.text}</span>
        </div>
      )}

      {/* Warning Badges */}
      <div className="flex flex-wrap gap-3">
        {progress.behindSchedule && (
          <div className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-orange-50 border border-orange-200 text-orange-700 text-xs font-bold shadow-sm">
            <AlertTriangle size={14} /> Chậm tiến độ ({progress.progressGap.toFixed(1)}%)
          </div>
        )}
        {progress.endingSoon && (
          <div className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-amber-50 border border-amber-200 text-amber-700 text-xs font-bold shadow-sm">
            <Clock size={14} /> Sắp kết thúc
          </div>
        )}
        {progress.overdueSprint && (
          <div className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-red-50 border border-red-200 text-red-700 text-xs font-bold shadow-sm">
            <AlertCircle size={14} /> Sprint quá hạn
          </div>
        )}
      </div>

      {/* Overview Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-5">
        <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-sm">
          <p className="text-sm font-semibold text-slate-500 mb-1">Thực tế / Kỳ vọng</p>
          <div className="flex items-end gap-2">
            <p className="text-3xl font-bold text-brand">{progress.actualCompletionRate.toFixed(1)}%</p>
            <p className="text-sm font-medium text-slate-400 mb-1">/ {progress.expectedProgressRate.toFixed(1)}%</p>
          </div>
        </div>

        <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-sm">
          <p className="text-sm font-semibold text-slate-500 mb-1">Công việc hoàn thành</p>
          <div className="flex items-end gap-2">
            <p className="text-3xl font-bold text-emerald-600">{progress.completedTasks}</p>
            <p className="text-sm font-medium text-slate-400 mb-1">/ {progress.totalTasks} Tasks</p>
          </div>
        </div>

        <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-sm">
          <p className="text-sm font-semibold text-slate-500 mb-1">Task quá hạn</p>
          <div className="flex items-end gap-2">
            <p className="text-3xl font-bold text-red-500">{progress.overdueTasks}</p>
            <p className="text-sm font-medium text-slate-400 mb-1">trong tổng {progress.unfinishedTasks} chưa xong</p>
          </div>
        </div>

        <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-sm">
          <p className="text-sm font-semibold text-slate-500 mb-1">Task bị chặn</p>
          <div className="flex items-end gap-2">
            <p className="text-3xl font-bold text-orange-500">{progress.blockedTasks}</p>
            <p className="text-sm font-medium text-slate-400 mb-1">cần xử lý ngay</p>
          </div>
        </div>
      </div>

      {/* Progress Chart */}
      <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm">
        <div className="mb-8 text-center">
          <h4 className="font-bold text-xl text-slate-800">Biểu đồ Tiến độ Sprint</h4>
          <p className="text-sm text-slate-500 mt-1">So sánh tiến độ thực tế (tích lũy) so với đường kỳ vọng</p>
        </div>
        
        <div className="h-[380px] w-full">
          {chartData.length > 0 ? (
            <ResponsiveContainer width="100%" height="100%">
              <ComposedChart data={chartData} margin={{ top: 5, right: 30, left: 0, bottom: 5 }}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e2e8f0" />
                <XAxis dataKey="date" fontSize={12} tickLine={false} axisLine={false} tickMargin={12} />
                <YAxis fontSize={12} tickLine={false} axisLine={false} tickMargin={12} domain={[0, 100]} />
                <Tooltip 
                  isAnimationActive={false}
                  contentStyle={{ borderRadius: '12px', border: 'none', boxShadow: '0 4px 20px rgba(0,0,0,0.08)' }}
                />
                <Legend wrapperStyle={{ paddingTop: '20px' }} />
                <Line type="monotone" name="Tiến độ Kỳ vọng (%)" dataKey="Kỳ vọng (%)" stroke="#94a3b8" strokeDasharray="5 5" strokeWidth={2} dot={false} activeDot={false} isAnimationActive={true} animationDuration={1200} animationEasing="ease-in-out" />
                <Area type="monotone" name="Tiến độ Thực tế (%)" dataKey="Thực tế (%)" fill="#f97316" stroke="#f97316" strokeWidth={3} fillOpacity={0.1} dot={{ r: 4, strokeWidth: 2, fill: '#fff' }} activeDot={{ r: 6, strokeWidth: 0, fill: '#f97316' }} isAnimationActive={true} animationDuration={1200} animationEasing="ease-in-out" />
              </ComposedChart>
            </ResponsiveContainer>
          ) : (
            <div className="flex h-full items-center justify-center text-sm font-medium text-slate-400 bg-slate-50 rounded-xl border border-dashed border-slate-200">Chưa có dữ liệu tiến độ</div>
          )}
        </div>
      </div>

      {/* Actions / Reminders */}
      <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-sm">
         <h4 className="font-bold text-md text-slate-800 mb-4 flex items-center gap-2"><Bell size={18} className="text-brand" /> Gửi nhắc nhở thủ công</h4>
         <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <Button 
              variant="outline-amber" 
              className="w-full justify-start" 
              leadingIcon={<Clock size={16} />}
              disabled={isReminding || (!progress.endingSoon && !progress.overdueSprint)}
              onClick={() => handleRemind('ending', remindSprintEnding)}
            >
              Nhắc nhở sắp hết hạn
            </Button>
            <Button 
              variant="outline-red" 
              className="w-full justify-start" 
              leadingIcon={<AlertCircle size={16} />}
              disabled={isReminding || progress.overdueTasks === 0}
              onClick={() => handleRemind('overdue', remindSprintOverdueTasks)}
            >
              Nhắc nhở Task quá hạn
            </Button>
            <Button 
              variant="outline-red" 
              className="w-full justify-start" 
              leadingIcon={<Lock size={16} />}
              disabled={isReminding || progress.blockedTasks === 0}
              onClick={() => handleRemind('blocked', remindSprintBlockedTasks)}
            >
              Nhắc nhở Task bị chặn
            </Button>
         </div>
         <p className="text-[11px] text-slate-500 mt-3 italic">* Lưu ý: Các nút nhắc nhở chỉ khả dụng khi có dữ liệu thỏa mãn (Sprint sắp kết thúc, có task quá hạn, hoặc có task bị chặn).</p>
      </div>

    </section>
  )
}
