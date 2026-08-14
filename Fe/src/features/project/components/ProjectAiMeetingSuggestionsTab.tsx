import { useState } from 'react'
import {
  Calendar,
  Sparkles,
  Copy,
  Check,
  Target,
  Clock,
  Flame,
  CheckCircle2,
  ShieldAlert,
  HelpCircle,
} from 'lucide-react'
import { getAiMeetingSuggestions } from '../services/ai.service'
import type {
  AiMeetingType,
  AiMeetingSuggestionResponse,
  RelatedTaskInfo,
} from '../models/ai.model'

interface ProjectAiMeetingSuggestionsTabProps {
  projectId: string
  projectName: string
  projectCode: string
}

interface MeetingTypeOption {
  type: AiMeetingType
  title: string
  subtitle: string
  icon: any
  badgeColor: string
  activeBorder: string
}

const MEETING_TYPES: MeetingTypeOption[] = [
  {
    type: 'DAILY',
    title: 'Daily Standup',
    subtitle: 'Cập nhật tiến độ ngắn, vướng mắc & mục tiêu trong ngày',
    icon: Clock,
    badgeColor: 'bg-amber-100 text-amber-800 border-amber-300',
    activeBorder: 'border-amber-500 bg-amber-50/40 text-amber-950',
  },
  {
    type: 'SPRINT_PLANNING',
    title: 'Sprint Planning',
    subtitle: 'Lập kế hoạch Sprint, phân chia khối lượng & cam kết mục tiêu',
    icon: Target,
    badgeColor: 'bg-blue-100 text-blue-800 border-blue-300',
    activeBorder: 'border-blue-500 bg-blue-50/40 text-blue-950',
  },
  {
    type: 'SPRINT_REVIEW',
    title: 'Sprint Review',
    subtitle: 'Đánh giá kết quả Sprint, demo tính năng & thu thập phản hồi',
    icon: CheckCircle2,
    badgeColor: 'bg-emerald-100 text-emerald-800 border-emerald-300',
    activeBorder: 'border-emerald-500 bg-emerald-50/40 text-emerald-950',
  },
  {
    type: 'RETROSPECTIVE',
    title: 'Retrospective',
    subtitle: 'Nhìn lại Sprint: Điều đã làm tốt, việc cần cải thiện & action items',
    icon: Calendar,
    badgeColor: 'bg-purple-100 text-purple-800 border-purple-300',
    activeBorder: 'border-purple-500 bg-purple-50/40 text-purple-950',
  },
  {
    type: 'ISSUE_RESOLUTION',
    title: 'Giải quyết Vấn đề (Issue Resolution)',
    subtitle: 'Tập trung tháo gỡ các task bị Blocked, quá hạn & rủi ro cao',
    icon: Flame,
    badgeColor: 'bg-rose-100 text-rose-800 border-rose-300',
    activeBorder: 'border-rose-500 bg-rose-50/40 text-rose-950',
  },
]

const PRESET_NOTES = [
  'Tập trung vào các task quá hạn và task đang bị block',
  'Đánh giá nguy cơ chậm tiến độ Release cuối tuần',
  'Cân đối lại khối lượng công việc giữa các thành viên',
  'Rà soát kỹ các Bug quan trọng ưu tiên cao',
]

function normalizeArray(value: any): string[] {
  if (Array.isArray(value)) {
    return value.map(item => (typeof item === 'string' ? item : JSON.stringify(item)))
  }
  if (typeof value === 'string' && value.trim()) {
    return value
      .split('\n')
      .map(s => s.replace(/^[-*•\d.\s]+/, '').trim())
      .filter(Boolean)
  }
  return []
}

function normalizeTasks(value: any): RelatedTaskInfo[] {
  if (Array.isArray(value)) {
    return value.map(item => {
      if (typeof item === 'object' && item !== null) {
        return item as RelatedTaskInfo
      }
      return { title: String(item) }
    })
  }
  if (typeof value === 'string' && value.trim()) {
    return value
      .split('\n')
      .map(line => line.trim())
      .filter(Boolean)
      .map(title => ({ title }))
  }
  return []
}

export function ProjectAiMeetingSuggestionsTab({
  projectId,
  projectName,
  projectCode,
}: ProjectAiMeetingSuggestionsTabProps) {
  const [selectedType, setSelectedType] = useState<AiMeetingType>('DAILY')
  const [additionalNote, setAdditionalNote] = useState('')
  const [loading, setLoading] = useState(false)
  const [suggestion, setSuggestion] = useState<AiMeetingSuggestionResponse | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [copied, setCopied] = useState(false)

  const handleGenerate = async () => {
    setLoading(true)
    setError(null)
    setSuggestion(null)

    try {
      const res = await getAiMeetingSuggestions(projectId, {
        meetingType: selectedType,
        additionalNote: additionalNote.trim() || undefined,
      })
      setSuggestion(res)
    } catch (err: any) {
      const msg = err instanceof Error ? err.message : 'Không thể lấy gợi ý cuộc họp từ AI'
      setError(msg)
    } finally {
      setLoading(false)
    }
  }

  const handleCopyFormatted = () => {
    if (!suggestion) return

    const title = suggestion.title || suggestion.meetingTitle || 'Gợi ý Cuộc họp AI'
    const goal = suggestion.objective || suggestion.goal || 'N/A'
    const agendaList = normalizeArray(suggestion.agenda)
    const questionsList = normalizeArray(suggestion.discussionQuestions || suggestion.questions)
    const risksList = normalizeArray(suggestion.relatedRisks || suggestion.risks)
    const tasksList = normalizeTasks(suggestion.relatedTasks || suggestion.tasks)
    const summaryText = suggestion.summary || suggestion.overview || ''

    let formatted = `# 📋 ${title}\n`
    formatted += `**Dự án:** [${projectCode}] ${projectName}\n`
    formatted += `**Mục tiêu:** ${goal}\n\n`

    if (agendaList.length > 0) {
      formatted += `### ⏱️ Chương trình họp (Agenda):\n`
      agendaList.forEach((item, idx) => {
        formatted += `${idx + 1}. ${item}\n`
      })
      formatted += `\n`
    }

    if (questionsList.length > 0) {
      formatted += `### ❓ Câu hỏi thảo luận:\n`
      questionsList.forEach(q => {
        formatted += `- ${q}\n`
      })
      formatted += `\n`
    }

    if (risksList.length > 0) {
      formatted += `### ⚠️ Rủi ro liên quan:\n`
      risksList.forEach(r => {
        formatted += `- ${r}\n`
      })
      formatted += `\n`
    }

    if (tasksList.length > 0) {
      formatted += `### 📌 Task liên quan:\n`
      tasksList.forEach(t => {
        const codeStr = t.code ? `[${t.code}] ` : ''
        const statusStr = t.status ? ` (${t.status})` : ''
        formatted += `- ${codeStr}${t.title || 'Task'}${statusStr}\n`
      })
      formatted += `\n`
    }

    if (summaryText) {
      formatted += `### 📝 Tóm tắt:\n${summaryText}\n`
    }

    navigator.clipboard.writeText(formatted)
    setCopied(true)
    setTimeout(() => setCopied(false), 2000)
  }

  const selectedTypeInfo = MEETING_TYPES.find(m => m.type === selectedType) || MEETING_TYPES[0]

  const agenda = suggestion ? normalizeArray(suggestion.agenda) : []
  const questions = suggestion ? normalizeArray(suggestion.discussionQuestions || suggestion.questions) : []
  const risks = suggestion ? normalizeArray(suggestion.relatedRisks || suggestion.risks) : []
  const tasks = suggestion ? normalizeTasks(suggestion.relatedTasks || suggestion.tasks) : []
  const title = suggestion?.title || suggestion?.meetingTitle
  const objective = suggestion?.objective || suggestion?.goal
  const summary = suggestion?.summary || suggestion?.overview

  return (
    <div className="flex flex-col gap-6 p-6 overflow-y-auto max-h-[70vh]">
      {/* Top Banner */}
      <div className="rounded-2xl border border-amber-200/80 bg-gradient-to-r from-amber-50 via-orange-50/50 to-amber-50 p-4 shadow-2xs">
        <div className="flex items-start gap-3">
          <div className="flex size-10 items-center justify-center rounded-xl bg-gradient-to-tr from-brand to-amber-500 text-white shrink-0 shadow-md">
            <Sparkles size={20} />
          </div>
          <div className="flex-1">
            <h4 className="font-extrabold text-slate-900 text-sm tracking-tight">
              Tạo Gợi ý Cuộc họp Thông minh với AI
            </h4>
            <p className="text-xs text-slate-600 mt-0.5 leading-relaxed">
              Gemini AI sẽ tự động phân tích dữ liệu thực tế của dự án <span className="font-bold text-amber-700">[{projectCode}]</span> (Backlog, Sprint, Task Blocked & Quá hạn) để lập Tiêu đề, Agenda, Rủi ro & Task cần đưa vào thảo luận.
            </p>
          </div>
        </div>
      </div>

      {/* Meeting Type Selection */}
      <div>
        <label className="block text-xs font-extrabold text-slate-800 uppercase tracking-wider mb-2.5 flex items-center gap-1.5">
          <Calendar size={14} className="text-brand" /> Chọn loại cuộc họp:
        </label>
        <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
          {MEETING_TYPES.map(item => {
            const isSelected = selectedType === item.type
            return (
              <button
                key={item.type}
                type="button"
                onClick={() => setSelectedType(item.type)}
                className={`text-left p-3.5 rounded-xl border transition-all duration-200 cursor-pointer flex flex-col justify-between ${
                  isSelected
                    ? `${item.activeBorder} shadow-md ring-2 ring-brand/30 -translate-y-0.5`
                    : 'border-slate-200/80 bg-white hover:border-slate-300 hover:bg-slate-50/50 text-slate-700'
                }`}
              >
                <div>
                  <div className="flex items-center justify-between mb-1.5">
                    <span className="font-extrabold text-xs">
                      {item.title}
                    </span>
                    {isSelected && (
                      <span className="size-2 rounded-full bg-brand animate-pulse" />
                    )}
                  </div>
                  <p className="text-[11px] text-slate-500 leading-snug line-clamp-2">
                    {item.subtitle}
                  </p>
                </div>
              </button>
            )
          })}
        </div>
      </div>

      {/* Additional Notes */}
      <div>
        <label className="block text-xs font-extrabold text-slate-800 uppercase tracking-wider mb-2 flex items-center gap-1.5">
          <HelpCircle size={14} className="text-brand" /> Ghi chú bổ sung (tùy chọn):
        </label>
        <textarea
          rows={2}
          value={additionalNote}
          onChange={e => setAdditionalNote(e.target.value)}
          placeholder="Ví dụ: Tập trung vào các task quá hạn và task đang bị block trong Sprint 3..."
          className="w-full rounded-xl border border-slate-200 bg-white p-3 text-xs text-slate-900 placeholder:text-slate-400 focus:border-brand focus:outline-none focus:ring-2 focus:ring-brand/20 transition-all font-medium"
        />

        {/* Preset Suggestions */}
        <div className="flex flex-wrap items-center gap-2 mt-2">
          <span className="text-[10px] font-bold text-slate-400 uppercase">Gợi ý nhanh:</span>
          {PRESET_NOTES.map((preset, idx) => (
            <button
              key={idx}
              type="button"
              onClick={() => setAdditionalNote(preset)}
              className="text-[11px] font-medium text-slate-600 bg-slate-100 hover:bg-amber-100 hover:text-amber-900 px-2.5 py-1 rounded-lg transition-colors cursor-pointer border border-slate-200/60"
            >
              + {preset}
            </button>
          ))}
        </div>
      </div>

      {/* Submit Button */}
      <div>
        <button
          type="button"
          onClick={handleGenerate}
          disabled={loading}
          className="w-full inline-flex items-center justify-center gap-2.5 rounded-xl bg-gradient-to-r from-brand via-amber-500 to-orange-500 py-3 px-5 text-xs font-extrabold text-white shadow-md hover:shadow-lg hover:scale-[1.005] active:scale-[0.995] transition-all disabled:opacity-50 cursor-pointer"
        >
          {loading ? (
            <>
              <Sparkles size={16} className="animate-spin text-amber-200" />
              <span>AI đang phân tích dữ liệu & tạo gợi ý cuộc họp...</span>
            </>
          ) : (
            <span>Tạo gợi ý cuộc họp ({selectedTypeInfo.title})</span>
          )}
        </button>
      </div>

      {/* Error Alert */}
      {error && (
        <div className="rounded-xl border border-rose-200 bg-rose-50 p-4 text-xs text-rose-800 flex items-start gap-3">
          <ShieldAlert size={18} className="text-rose-600 shrink-0 mt-0.5" />
          <div>
            <h5 className="font-extrabold text-rose-900">Lỗi khi tạo gợi ý</h5>
            <p className="mt-0.5">{error}</p>
          </div>
        </div>
      )}

      {/* Results Render */}
      {suggestion && (
        <div className="flex flex-col gap-5 pt-2 border-t border-slate-200">
          {/* Header Card & Copy Bar */}
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 bg-slate-900 text-white p-5 rounded-2xl shadow-md border border-slate-800">
            <div>
              <span className={`inline-block px-2.5 py-0.5 rounded-full text-[10px] font-extrabold uppercase mb-2 border ${selectedTypeInfo.badgeColor}`}>
                {selectedTypeInfo.title}
              </span>
              <h3 className="text-base font-extrabold tracking-tight text-white">
                {title || `Cuộc họp ${selectedTypeInfo.title}`}
              </h3>
              {objective && (
                <p className="text-xs text-amber-300 mt-1 font-medium">
                  <span>Mục tiêu: {objective}</span>
                </p>
              )}
            </div>

            <button
              type="button"
              onClick={handleCopyFormatted}
              className="inline-flex items-center gap-2 rounded-xl bg-white/10 hover:bg-white/20 border border-white/20 px-3.5 py-2 text-xs font-bold text-white transition-all cursor-pointer shrink-0 self-start sm:self-auto"
            >
              {copied ? (
                <>
                  <Check size={15} className="text-emerald-400" />
                  <span className="text-emerald-300">Đã sao chép!</span>
                </>
              ) : (
                <>
                  <Copy size={15} />
                  <span>Sao chép Markdown</span>
                </>
              )}
            </button>
          </div>

          <div className="grid gap-5 md:grid-cols-2">
            {/* Agenda List */}
            {agenda.length > 0 && (
              <div className="rounded-2xl border border-slate-200 bg-white p-4.5 shadow-2xs">
                <h4 className="text-xs font-extrabold text-slate-900 uppercase tracking-wider mb-3 text-brand">
                  Chương trình họp (Agenda):
                </h4>
                <div className="space-y-2.5">
                  {agenda.map((item, idx) => (
                    <div key={idx} className="flex items-start gap-2.5 p-2 rounded-lg bg-slate-50 border border-slate-100">
                      <span className="flex size-5 items-center justify-center rounded-full bg-brand/10 text-brand text-[11px] font-extrabold shrink-0 mt-0.5">
                        {idx + 1}
                      </span>
                      <span className="text-xs text-slate-800 font-medium leading-relaxed">
                        {item}
                      </span>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* Discussion Questions */}
            {questions.length > 0 && (
              <div className="rounded-2xl border border-slate-200 bg-white p-4.5 shadow-2xs">
                <h4 className="text-xs font-extrabold text-slate-900 uppercase tracking-wider mb-3 text-blue-600">
                  Câu hỏi thảo luận:
                </h4>
                <div className="space-y-2.5">
                  {questions.map((q, idx) => (
                    <div key={idx} className="flex items-start gap-2.5 p-2.5 rounded-lg bg-blue-50/50 border border-blue-100">
                      <span className="text-xs text-slate-800 font-semibold leading-relaxed">
                        • {q}
                      </span>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>

          {/* Related Risks */}
          {risks.length > 0 && (
            <div className="rounded-2xl border border-amber-200 bg-amber-50/60 p-4.5 shadow-2xs">
              <h4 className="text-xs font-extrabold text-amber-900 uppercase tracking-wider mb-3">
                Rủi ro liên quan cần chú ý:
              </h4>
              <div className="grid gap-2 sm:grid-cols-2">
                {risks.map((r, idx) => (
                  <div key={idx} className="flex items-start gap-2.5 p-2.5 rounded-xl bg-white border border-amber-200/80">
                    <span className="size-1.5 rounded-full bg-amber-500 shrink-0 mt-1.5" />
                    <span className="text-xs text-slate-800 font-medium leading-relaxed">
                      {r}
                    </span>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Related Tasks */}
          {tasks.length > 0 && (
            <div className="rounded-2xl border border-slate-200 bg-white p-4.5 shadow-2xs">
              <h4 className="text-xs font-extrabold text-slate-900 uppercase tracking-wider mb-3 text-indigo-600">
                Task liên quan đến buổi họp:
              </h4>
              <div className="flex flex-wrap gap-2">
                {tasks.map((task, idx) => (
                  <div
                    key={idx}
                    className="inline-flex items-center gap-2 rounded-xl bg-slate-100 border border-slate-200 px-3 py-1.5 text-xs font-semibold text-slate-800"
                  >
                    {task.code && (
                      <span className="rounded bg-brand/10 px-1.5 py-0.5 text-[10px] font-extrabold text-brand">
                        {task.code}
                      </span>
                    )}
                    <span>{task.title || 'Task'}</span>
                    {task.status && (
                      <span className="text-[10px] font-extrabold text-slate-500 uppercase">
                        ({task.status})
                      </span>
                    )}
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Summary Overview */}
          {summary && (
            <div className="rounded-2xl border border-slate-200 bg-slate-50 p-4 shadow-2xs">
              <h4 className="text-xs font-extrabold text-slate-700 uppercase tracking-wider mb-1.5">
                Tóm tắt đánh giá từ AI:
              </h4>
              <p className="text-xs text-slate-600 leading-relaxed font-medium">
                {summary}
              </p>
            </div>
          )}
        </div>
      )}
    </div>
  )
}
