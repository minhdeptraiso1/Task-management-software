import { useState } from 'react'
import {
  ListTodo,
  Sparkles,
  Zap,
  Copy,
  Check,
  AlertTriangle,
  UserCheck,
  Calendar,
  FileText,
  Clock,
  ShieldAlert,
  Bot,
  HelpCircle,
  Link2,
} from 'lucide-react'
import { getAiActionItems } from '../services/ai.service'
import type {
  AiMeetingType,
  AiActionItemCandidate,
  AiActionItemsResponse,
} from '../models/ai.model'

interface ProjectAiActionItemsTabProps {
  projectId: string
  projectName: string
  projectCode: string
}

const MEETING_TYPE_OPTIONS: Array<{ type: AiMeetingType; label: string }> = [
  { type: 'DAILY', label: 'Daily Standup' },
  { type: 'SPRINT_PLANNING', label: 'Sprint Planning' },
  { type: 'SPRINT_REVIEW', label: 'Sprint Review' },
  { type: 'RETROSPECTIVE', label: 'Retrospective' },
  { type: 'ISSUE_RESOLUTION', label: 'Giải quyết Vấn đề (Issue Resolution)' },
]

const SAMPLE_MEETING_CONTENT = `Backend cần hoàn thành API thống kê trong một ngày. Frontend làm UI bằng mock data. Team ưu tiên API dashboard trước notification.`

function normalizeActionItemCandidates(response: AiActionItemsResponse): AiActionItemCandidate[] {
  if (Array.isArray(response)) {
    return response
  }
  if (response && typeof response === 'object') {
    if (Array.isArray(response.actionItems)) return response.actionItems
    if (Array.isArray(response.items)) return response.items
    if (Array.isArray(response.suggestions)) return response.suggestions
  }
  return []
}

function normalizeRelatedTasks(value: any): string[] {
  if (Array.isArray(value)) {
    return value.map(v => (typeof v === 'string' ? v : v?.code || v?.title || JSON.stringify(v)))
  }
  if (typeof value === 'string' && value.trim()) {
    return value.split(',').map(s => s.trim()).filter(Boolean)
  }
  return []
}

export function ProjectAiActionItemsTab({
  projectId,
  projectName,
  projectCode,
}: ProjectAiActionItemsTabProps) {
  const [meetingType, setMeetingType] = useState<AiMeetingType>('ISSUE_RESOLUTION')
  const [meetingTitle, setMeetingTitle] = useState('')
  const [meetingContent, setMeetingContent] = useState('')
  const [additionalNote, setAdditionalNote] = useState('')

  const [loading, setLoading] = useState(false)
  const [actionItems, setActionItems] = useState<AiActionItemCandidate[]>([])
  const [hasSearched, setHasSearched] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [copied, setCopied] = useState(false)

  const charCount = meetingContent.length
  const isValidLength = charCount >= 20 && charCount <= 12000

  const handleGenerate = async () => {
    if (!isValidLength) {
      setError('Nội dung cuộc họp phải có độ dài từ 20 đến 12.000 ký tự.')
      return
    }

    setLoading(true)
    setError(null)
    setHasSearched(true)

    try {
      const res = await getAiActionItems(projectId, {
        meetingType,
        meetingTitle: meetingTitle.trim() || undefined,
        meetingContent: meetingContent.trim(),
        additionalNote: additionalNote.trim() || undefined,
      })
      const items = normalizeActionItemCandidates(res)
      setActionItems(items)
    } catch (err: any) {
      const msg = err instanceof Error ? err.message : 'Không thể lấy đề xuất Action Items từ AI'
      setError(msg)
    } finally {
      setLoading(false)
    }
  }

  const handleCopyMarkdown = () => {
    if (actionItems.length === 0) return

    let formatted = `# 📋 ĐỀ XUẤT ACTION ITEMS TỪ AI\n`
    formatted += `**Dự án:** [${projectCode}] ${projectName}\n`
    if (meetingTitle) formatted += `**Cuộc họp:** ${meetingTitle}\n`
    formatted += `**Loại họp:** ${meetingType}\n\n`

    actionItems.forEach((item, index) => {
      const title = item.title || item.name || `Action Item #${index + 1}`
      const assignee = item.proposedAssignee || item.assignee || 'Chưa phân công'
      const dueDate = item.proposedDueDate || item.dueDate || 'N/A'
      const priority = item.priority || 'NORMAL'
      const desc = item.description || 'Không có mô tả'
      const reasoning = item.reasoning || item.reason
      const warning = item.warningMessage || item.warning
      const tasks = normalizeRelatedTasks(item.relatedTasks)

      formatted += `### ${index + 1}. ${title} [${priority}]\n`
      formatted += `- **Người phụ trách đề xuất:** @${assignee}\n`
      formatted += `- **Hạn hoàn thành đề xuất:** ${dueDate}\n`
      formatted += `- **Mô tả:** ${desc}\n`
      if (tasks.length > 0) formatted += `- **Task liên quan:** ${tasks.join(', ')}\n`
      if (reasoning) formatted += `- **Lý do đề xuất:** ${reasoning}\n`
      if (warning) formatted += `- **Cảnh báo:** ⚠️ ${warning}\n`
      formatted += `\n`
    })

    navigator.clipboard.writeText(formatted)
    setCopied(true)
    setTimeout(() => setCopied(false), 2000)
  }

  return (
    <div className="flex flex-col gap-6 p-6 overflow-y-auto max-h-[70vh]">
      {/* Top Banner */}
      <div className="rounded-2xl border border-emerald-200/80 bg-gradient-to-r from-emerald-50 via-teal-50/50 to-sky-50 p-4 shadow-2xs">
        <div className="flex items-start gap-3">
          <div className="flex size-10 items-center justify-center rounded-xl bg-gradient-to-tr from-emerald-600 to-teal-600 text-white shrink-0 shadow-md">
            <ListTodo size={20} />
          </div>
          <div className="flex-1">
            <h4 className="font-extrabold text-slate-900 text-sm tracking-tight">
              Đề xuất Action Items Tự động cho Dự án
            </h4>
            <p className="text-xs text-slate-600 mt-0.5 leading-relaxed">
              Gemini AI sẽ trích xuất <span className="font-bold text-emerald-700">tối đa 10 Action Items</span> rõ ràng từ nội dung cuộc họp kèm theo <span className="font-bold text-teal-700">Người phụ trách, Hạn hoàn thành, Mức độ ưu tiên, Task liên quan & Độ tin cậy</span>.
            </p>
          </div>
        </div>
      </div>

      {/* Input Metadata Grid */}
      <div className="grid gap-4 sm:grid-cols-2">
        {/* Meeting Title */}
        <div>
          <label className="block text-xs font-extrabold text-slate-800 uppercase tracking-wider mb-1.5 flex items-center gap-1.5">
            <FileText size={14} className="text-brand" /> Tiêu đề cuộc họp (Tùy chọn):
          </label>
          <input
            type="text"
            value={meetingTitle}
            onChange={e => setMeetingTitle(e.target.value)}
            placeholder="Ví dụ: Họp xử lý task dashboard bị trễ..."
            className="w-full rounded-xl border border-slate-200 bg-white px-3.5 py-2.5 text-xs text-slate-900 placeholder:text-slate-400 focus:border-brand focus:outline-none focus:ring-2 focus:ring-brand/20 transition-all font-medium"
          />
        </div>

        {/* Meeting Type Selector */}
        <div>
          <label className="block text-xs font-extrabold text-slate-800 uppercase tracking-wider mb-1.5 flex items-center gap-1.5">
            <Calendar size={14} className="text-brand" /> Loại cuộc họp:
          </label>
          <select
            value={meetingType}
            onChange={e => setMeetingType(e.target.value as AiMeetingType)}
            className="w-full rounded-xl border border-slate-200 bg-white px-3.5 py-2.5 text-xs text-slate-900 focus:border-brand focus:outline-none focus:ring-2 focus:ring-brand/20 transition-all font-semibold"
          >
            {MEETING_TYPE_OPTIONS.map(opt => (
              <option key={opt.type} value={opt.type}>
                {opt.label}
              </option>
            ))}
          </select>
        </div>
      </div>

      {/* Meeting Content Textarea */}
      <div>
        <div className="flex items-center justify-between mb-1.5">
          <label className="text-xs font-extrabold text-slate-800 uppercase tracking-wider flex items-center gap-1.5">
            <FileText size={14} className="text-brand" /> Nội dung cuộc họp (Meeting Content)*:
          </label>
          <div className="flex items-center gap-2">
            <button
              type="button"
              onClick={() => {
                setMeetingTitle('Họp xử lý task dashboard bị trễ')
                setMeetingContent(SAMPLE_MEETING_CONTENT)
                setAdditionalNote('Chỉ lấy các việc cần theo dõi sau cuộc họp.')
              }}
              className="text-[11px] font-bold text-brand hover:text-amber-700 bg-amber-50 hover:bg-amber-100 px-2 py-0.5 rounded-md transition-colors cursor-pointer border border-amber-200/60"
            >
              + Dùng nội dung mẫu
            </button>
            <span
              className={`text-[11px] font-mono font-bold ${
                charCount === 0
                  ? 'text-slate-400'
                  : isValidLength
                  ? 'text-emerald-600'
                  : 'text-rose-600'
              }`}
            >
              {charCount} / 12,000 ký tự (Tối thiểu 20)
            </span>
          </div>
        </div>

        <textarea
          rows={4}
          value={meetingContent}
          onChange={e => setMeetingContent(e.target.value)}
          placeholder="Dán diễn biến, ghi chú hoặc bản ghi nội dung họp tại đây (tối thiểu 20 ký tự)..."
          className="w-full rounded-xl border border-slate-200 bg-white p-3.5 text-xs text-slate-900 placeholder:text-slate-400 focus:border-brand focus:outline-none focus:ring-2 focus:ring-brand/20 transition-all font-medium leading-relaxed"
        />
      </div>

      {/* Additional Note Input */}
      <div>
        <label className="block text-xs font-extrabold text-slate-800 uppercase tracking-wider mb-1.5 flex items-center gap-1.5">
          <HelpCircle size={14} className="text-brand" /> Yêu cầu lọc bổ sung (Tùy chọn):
        </label>
        <input
          type="text"
          value={additionalNote}
          onChange={e => setAdditionalNote(e.target.value)}
          placeholder="Ví dụ: Chỉ lấy các việc cần hoàn thành trong tuần này..."
          className="w-full rounded-xl border border-slate-200 bg-white px-3.5 py-2.5 text-xs text-slate-900 placeholder:text-slate-400 focus:border-brand focus:outline-none focus:ring-2 focus:ring-brand/20 transition-all font-medium"
        />
      </div>

      {/* Submit Button */}
      <div>
        <button
          type="button"
          onClick={handleGenerate}
          disabled={loading || !isValidLength}
          className="w-full inline-flex items-center justify-center gap-2.5 rounded-xl bg-gradient-to-r from-emerald-600 via-teal-600 to-brand py-3 px-5 text-xs font-extrabold text-white shadow-md hover:shadow-lg hover:scale-[1.005] active:scale-[0.995] transition-all disabled:opacity-50 cursor-pointer"
        >
          {loading ? (
            <>
              <Sparkles size={16} className="animate-spin text-emerald-200" />
              <span>AI đang phân tích & trích xuất Action Items...</span>
            </>
          ) : (
            <>
              <Zap size={16} />
              <span>Đề xuất Action Items bằng AI</span>
            </>
          )}
        </button>
      </div>

      {/* Error Alert */}
      {error && (
        <div className="rounded-xl border border-rose-200 bg-rose-50 p-4 text-xs text-rose-800 flex items-start gap-3">
          <ShieldAlert size={18} className="text-rose-600 shrink-0 mt-0.5" />
          <div>
            <h5 className="font-extrabold text-rose-900">Lỗi thực hiện</h5>
            <p className="mt-0.5">{error}</p>
          </div>
        </div>
      )}

      {/* Action Items Result Cards List */}
      {hasSearched && !loading && !error && (
        <div className="flex flex-col gap-4 pt-2 border-t border-slate-200">
          <div className="flex items-center justify-between">
            <h4 className="text-xs font-extrabold text-slate-900 uppercase tracking-wider flex items-center gap-2">
              <ListTodo size={16} className="text-emerald-600" /> Kết quả Đề xuất ({actionItems.length} Action Items):
            </h4>

            {actionItems.length > 0 && (
              <button
                type="button"
                onClick={handleCopyMarkdown}
                className="inline-flex items-center gap-1.5 text-xs font-bold text-slate-700 bg-slate-100 hover:bg-slate-200 px-3 py-1.5 rounded-xl transition-all cursor-pointer border border-slate-200"
              >
                {copied ? (
                  <>
                    <Check size={14} className="text-emerald-600" />
                    <span className="text-emerald-700">Đã sao chép!</span>
                  </>
                ) : (
                  <>
                    <Copy size={14} />
                    <span>Sao chép Markdown</span>
                  </>
                )}
              </button>
            )}
          </div>

          {actionItems.length === 0 ? (
            <div className="text-center p-8 bg-slate-50 rounded-2xl border border-slate-200 text-slate-500 text-xs font-medium">
              Không tìm thấy Action Item nào thỏa mãn từ nội dung họp đã cung cấp.
            </div>
          ) : (
            <div className="space-y-4">
              {actionItems.map((item, index) => {
                const title = item.title || item.name || `Action Item #${index + 1}`
                const assignee = item.proposedAssignee || item.suggestedAssigneeName || item.assignee
                const dueDate = item.proposedDueDate || item.suggestedDueDate || item.dueDate
                const priority = (item.priority || 'MEDIUM').toUpperCase()
                const desc = item.description
                const reasoning = item.reasoning || item.reason
                const warning = item.warningMessage || item.warning
                const confidence = item.confidence
                const relatedTasks = normalizeRelatedTasks(item.relatedTasks)
                if (item.relatedTaskTitle && !relatedTasks.includes(item.relatedTaskTitle)) relatedTasks.push(item.relatedTaskTitle)

                const isHighPriority = priority === 'HIGH' || priority === 'URGENT'

                return (
                  <div
                    key={item.temporaryId || index}
                    className="rounded-2xl border border-slate-200 bg-white p-4.5 shadow-2xs hover:shadow-md transition-all space-y-3"
                  >
                    {/* Header line */}
                    <div className="flex flex-wrap items-center justify-between gap-2 border-b border-slate-100 pb-3">
                      <div className="flex items-center gap-2.5 flex-1 min-w-[200px]">
                        <span className="size-6 rounded-lg bg-emerald-100 text-emerald-800 font-extrabold text-[11px] flex items-center justify-center shrink-0">
                          #{index + 1}
                        </span>
                        <h5 className="font-extrabold text-slate-900 text-xs sm:text-sm tracking-tight leading-snug">
                          {title}
                        </h5>
                      </div>

                      <div className="flex items-center gap-2">
                        {/* Priority Badge */}
                        <span
                          className={`rounded-full px-2.5 py-0.5 text-[10px] font-extrabold uppercase border ${
                            isHighPriority
                              ? 'bg-rose-50 text-rose-700 border-rose-200'
                              : 'bg-emerald-50 text-emerald-700 border-emerald-200'
                          }`}
                        >
                          {priority}
                        </span>

                        {/* Confidence score */}
                        {typeof confidence === 'number' && (
                          <span className="rounded-full bg-slate-100 text-slate-700 px-2 py-0.5 text-[10px] font-bold border border-slate-200" title="Độ tin cậy từ AI">
                            🎯 Math: {Math.round(confidence * 100)}%
                          </span>
                        )}
                      </div>
                    </div>

                    {/* Description */}
                    {desc && (
                      <p className="text-xs text-slate-700 leading-relaxed font-medium">
                        {desc}
                      </p>
                    )}

                    {/* Metadata strip: Assignee & DueDate */}
                    <div className="flex flex-wrap items-center gap-4 text-xs font-semibold text-slate-600 bg-slate-50/80 p-2.5 rounded-xl border border-slate-100">
                      <div className="flex items-center gap-1.5">
                        <UserCheck size={14} className="text-emerald-600" />
                        <span>Người phụ trách đề xuất:</span>
                        <span className={assignee ? 'font-bold text-slate-900' : 'text-slate-400 italic'}>
                          {assignee ? `@${assignee}` : 'Chưa chỉ định'}
                        </span>
                      </div>

                      <div className="flex items-center gap-1.5">
                        <Clock size={14} className="text-teal-600" />
                        <span>Hạn đề xuất:</span>
                        <span className={dueDate ? 'font-bold text-slate-900' : 'text-slate-400 italic'}>
                          {dueDate || 'Chưa xác định'}
                        </span>
                      </div>
                    </div>

                    {/* Related Tasks */}
                    {relatedTasks.length > 0 && (
                      <div className="flex items-center gap-2 text-xs">
                        <span className="font-extrabold text-slate-500 uppercase text-[10px] tracking-wider flex items-center gap-1">
                          <Link2 size={12} className="text-blue-600" /> Task liên quan:
                        </span>
                        <div className="flex flex-wrap gap-1.5">
                          {relatedTasks.map((t, idx) => (
                            <span key={idx} className="rounded-md bg-blue-50 text-blue-700 font-bold px-2 py-0.5 text-[11px] border border-blue-200/60">
                              {t}
                            </span>
                          ))}
                        </div>
                      </div>
                    )}

                    {/* Reasoning */}
                    {reasoning && (
                      <div className="text-[11px] text-slate-600 bg-amber-50/40 p-2.5 rounded-xl border border-amber-100 leading-relaxed">
                        <span className="font-extrabold text-amber-900 flex items-center gap-1 mb-0.5">
                          <Bot size={12} className="text-amber-600" /> Lý do đề xuất từ AI:
                        </span>
                        {reasoning}
                      </div>
                    )}

                    {/* Warning Message */}
                    {warning && (
                      <div className="flex items-start gap-2 text-[11px] text-rose-800 bg-rose-50 p-2.5 rounded-xl border border-rose-200">
                        <AlertTriangle size={14} className="text-rose-600 shrink-0 mt-0.5" />
                        <span>
                          <strong className="font-extrabold">Cảnh báo dữ liệu:</strong> {warning}
                        </span>
                      </div>
                    )}
                  </div>
                )
              })}
            </div>
          )}
        </div>
      )}
    </div>
  )
}
