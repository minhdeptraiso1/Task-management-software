import { useState } from 'react'
import {
  FileCheck2,
  Sparkles,
  Zap,
  Copy,
  Check,
  AlertTriangle,
  ListChecks,
  Target,
  FileText,
  Clock,
  CheckCircle2,
  ShieldAlert,
  Users,
  Calendar,
  UserCheck,
  CheckSquare,
  Bot,
} from 'lucide-react'
import { generateAiMeetingMinutes } from '../services/ai.service'
import type {
  AiMeetingType,
  AiMeetingMinutesResponse,
  ActionItemDraft,
} from '../models/ai.model'

interface ProjectAiMeetingMinutesTabProps {
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

const SAMPLE_RAW_NOTES = `Team trao đổi về task dashboard bị trễ do API thống kê chưa xong. Backend cần thêm một ngày để hoàn thành API. Frontend có thể làm UI trước bằng mock data. Team thống nhất ưu tiên API dashboard trước notification.`

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

function normalizeActionItems(value: any): ActionItemDraft[] {
  if (Array.isArray(value)) {
    return value.map(item => {
      if (typeof item === 'object' && item !== null) {
        return item as ActionItemDraft
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

export function ProjectAiMeetingMinutesTab({
  projectId,
  projectName,
  projectCode,
}: ProjectAiMeetingMinutesTabProps) {
  const [meetingType, setMeetingType] = useState<AiMeetingType>('ISSUE_RESOLUTION')
  const [meetingTitle, setMeetingTitle] = useState('')
  const [meetingTime, setMeetingTime] = useState('')
  const [participants, setParticipants] = useState('')
  const [rawNotes, setRawNotes] = useState('')

  const [loading, setLoading] = useState(false)
  const [minutes, setMinutes] = useState<AiMeetingMinutesResponse | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [copied, setCopied] = useState(false)

  const charCount = rawNotes.length
  const isValidLength = charCount >= 20 && charCount <= 10000

  const handleGenerate = async () => {
    if (!isValidLength) {
      setError('Ghi chú cuộc họp phải có độ dài từ 20 đến 10.000 ký tự.')
      return
    }

    setLoading(true)
    setError(null)
    setMinutes(null)

    try {
      const res = await generateAiMeetingMinutes(projectId, {
        meetingType,
        meetingTitle: meetingTitle.trim() || undefined,
        meetingTime: meetingTime.trim() || undefined,
        participants: participants.trim() || undefined,
        rawNotes: rawNotes.trim(),
      })
      setMinutes(res)
    } catch (err: any) {
      const msg = err instanceof Error ? err.message : 'Không thể tạo biên bản họp từ AI'
      setError(msg)
    } finally {
      setLoading(false)
    }
  }

  const handleCopyFormatted = () => {
    if (!minutes) return

    const title = minutes.title || minutes.meetingTitle || meetingTitle || 'Biên bản Cuộc họp'
    const typeStr = minutes.meetingType || meetingType
    const timeStr = minutes.meetingTime || meetingTime || 'N/A'
    const partStr = minutes.participants || participants || 'N/A'
    const overviewText = minutes.overview || ''

    const topics = normalizeArray(minutes.discussedTopics || minutes.topics)
    const decisionsList = normalizeArray(minutes.decisions)
    const unresolvedList = normalizeArray(minutes.unresolvedIssues || minutes.issues)
    const risksList = normalizeArray(minutes.relatedRisks || minutes.risks)
    const actionItems = normalizeActionItems(minutes.actionItemDrafts || minutes.actionItems)
    const summaryText = minutes.summary || ''

    let formatted = `# 📑 BIÊN BẢN CUỘC HỌP: ${title}\n`
    formatted += `**Dự án:** [${projectCode}] ${projectName}\n`
    formatted += `**Loại họp:** ${typeStr} | **Thời gian:** ${timeStr}\n`
    formatted += `**Thành phần tham dự:** ${partStr}\n\n`

    if (overviewText) {
      formatted += `### 📌 Tổng quan:\n${overviewText}\n\n`
    }

    if (topics.length > 0) {
      formatted += `### 🗣️ Nội dung đã thảo luận:\n`
      topics.forEach(t => {
        formatted += `- ${t}\n`
      })
      formatted += `\n`
    }

    if (decisionsList.length > 0) {
      formatted += `### ✅ Quyết định đã thống nhất:\n`
      decisionsList.forEach(d => {
        formatted += `- ${d}\n`
      })
      formatted += `\n`
    }

    if (unresolvedList.length > 0) {
      formatted += `### ⏳ Vấn đề chưa giải quyết:\n`
      unresolvedList.forEach(u => {
        formatted += `- ${u}\n`
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

    if (actionItems.length > 0) {
      formatted += `### 📋 Bản nháp Việc cần làm (Action Items):\n`
      actionItems.forEach(item => {
        const assignee = item.assignee ? ` (@${item.assignee})` : ''
        const priority = item.priority ? ` [${item.priority}]` : ''
        const due = item.dueDate ? ` - Hạn: ${item.dueDate}` : ''
        formatted += `- **${item.title || 'Task'}**${assignee}${priority}${due}\n`
      })
      formatted += `\n`
    }

    if (summaryText) {
      formatted += `### 📝 Tóm tắt cuối:\n${summaryText}\n`
    }

    navigator.clipboard.writeText(formatted)
    setCopied(true)
    setTimeout(() => setCopied(false), 2000)
  }

  const topics = minutes ? normalizeArray(minutes.discussedTopics || minutes.topics) : []
  const decisions = minutes ? normalizeArray(minutes.decisions) : []
  const unresolved = minutes ? normalizeArray(minutes.unresolvedIssues || minutes.issues) : []
  const risks = minutes ? normalizeArray(minutes.relatedRisks || minutes.risks) : []
  const actionItems = minutes ? normalizeActionItems(minutes.actionItemDrafts || minutes.actionItems) : []
  const displayTitle = minutes?.title || minutes?.meetingTitle || meetingTitle || 'Biên bản Cuộc họp'
  const overview = minutes?.overview
  const summary = minutes?.summary

  return (
    <div className="flex flex-col gap-6 p-6 overflow-y-auto max-h-[70vh]">
      {/* Top Banner */}
      <div className="rounded-2xl border border-blue-200/80 bg-gradient-to-r from-blue-50 via-indigo-50/50 to-sky-50 p-4 shadow-2xs">
        <div className="flex items-start gap-3">
          <div className="flex size-10 items-center justify-center rounded-xl bg-gradient-to-tr from-blue-600 to-indigo-600 text-white shrink-0 shadow-md">
            <FileCheck2 size={20} />
          </div>
          <div className="flex-1">
            <h4 className="font-extrabold text-slate-900 text-sm tracking-tight">
              Tự động Tổng hợp Biên bản Cuộc họp từ Ghi chú Thô
            </h4>
            <p className="text-xs text-slate-600 mt-0.5 leading-relaxed">
              Dán ghi chú vắt tắt của buổi họp, Gemini AI sẽ tự động phân loại: <span className="font-bold text-blue-700">Nội dung thảo luận, Quyết định, Vấn đề chưa xong, Rủi ro</span> và tự sinh <span className="font-bold text-indigo-700">Bản nháp Action Items</span>.
            </p>
          </div>
        </div>
      </div>

      {/* Input Metadata Grid */}
      <div className="grid gap-4 sm:grid-cols-2">
        {/* Meeting Title */}
        <div>
          <label className="block text-xs font-extrabold text-slate-800 uppercase tracking-wider mb-1.5 flex items-center gap-1.5">
            <FileText size={14} className="text-brand" /> Tiêu đề buổi họp (Tùy chọn):
          </label>
          <input
            type="text"
            value={meetingTitle}
            onChange={e => setMeetingTitle(e.target.value)}
            placeholder="Ví dụ: Họp xử lý task bị trễ Sprint 3..."
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

        {/* Meeting Time */}
        <div>
          <label className="block text-xs font-extrabold text-slate-800 uppercase tracking-wider mb-1.5 flex items-center gap-1.5">
            <Clock size={14} className="text-brand" /> Thời gian họp (Tùy chọn):
          </label>
          <input
            type="text"
            value={meetingTime}
            onChange={e => setMeetingTime(e.target.value)}
            placeholder="Ví dụ: 14:00 12/08/2026..."
            className="w-full rounded-xl border border-slate-200 bg-white px-3.5 py-2.5 text-xs text-slate-900 placeholder:text-slate-400 focus:border-brand focus:outline-none focus:ring-2 focus:ring-brand/20 transition-all font-medium"
          />
        </div>

        {/* Participants */}
        <div>
          <label className="block text-xs font-extrabold text-slate-800 uppercase tracking-wider mb-1.5 flex items-center gap-1.5">
            <Users size={14} className="text-brand" /> Người tham gia (Tùy chọn):
          </label>
          <input
            type="text"
            value={participants}
            onChange={e => setParticipants(e.target.value)}
            placeholder="Ví dụ: Scrum Master, Backend Dev, Frontend Dev..."
            className="w-full rounded-xl border border-slate-200 bg-white px-3.5 py-2.5 text-xs text-slate-900 placeholder:text-slate-400 focus:border-brand focus:outline-none focus:ring-2 focus:ring-brand/20 transition-all font-medium"
          />
        </div>
      </div>

      {/* Raw Notes Textarea */}
      <div>
        <div className="flex items-center justify-between mb-1.5">
          <label className="text-xs font-extrabold text-slate-800 uppercase tracking-wider flex items-center gap-1.5">
            <FileText size={14} className="text-brand" /> Ghi chú cuộc họp dạng thô (Raw Notes)*:
          </label>
          <div className="flex items-center gap-2">
            <button
              type="button"
              onClick={() => {
                setMeetingTitle('Họp xử lý task bị trễ')
                setMeetingTime('14:00 12/08/2026')
                setParticipants('Scrum Master, Backend Dev, Frontend Dev')
                setRawNotes(SAMPLE_RAW_NOTES)
              }}
              className="text-[11px] font-bold text-brand hover:text-amber-700 bg-amber-50 hover:bg-amber-100 px-2 py-0.5 rounded-md transition-colors cursor-pointer border border-amber-200/60"
            >
              + Dùng ghi chú mẫu
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
              {charCount} / 10,000 ký tự (Tối thiểu 20)
            </span>
          </div>
        </div>

        <textarea
          rows={5}
          value={rawNotes}
          onChange={e => setRawNotes(e.target.value)}
          placeholder="Dán toàn bộ ghi chú nhanh của bạn vào đây (ít nhất 20 ký tự)..."
          className="w-full rounded-xl border border-slate-200 bg-white p-3.5 text-xs text-slate-900 placeholder:text-slate-400 focus:border-brand focus:outline-none focus:ring-2 focus:ring-brand/20 transition-all font-medium leading-relaxed"
        />
      </div>

      {/* Submit Button */}
      <div>
        <button
          type="button"
          onClick={handleGenerate}
          disabled={loading || !isValidLength}
          className="w-full inline-flex items-center justify-center gap-2.5 rounded-xl bg-gradient-to-r from-blue-600 via-indigo-600 to-brand py-3 px-5 text-xs font-extrabold text-white shadow-md hover:shadow-lg hover:scale-[1.005] active:scale-[0.995] transition-all disabled:opacity-50 cursor-pointer"
        >
          {loading ? (
            <>
              <Sparkles size={16} className="animate-spin text-blue-200" />
              <span>AI đang đọc ghi chú & phân tích tạo biên bản...</span>
            </>
          ) : (
            <>
              <Zap size={16} />
              <span>Tạo Biên bản Cuộc họp bằng AI</span>
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

      {/* Results Render */}
      {minutes && (
        <div className="flex flex-col gap-5 pt-2 border-t border-slate-200">
          {/* Header Card */}
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 bg-slate-900 text-white p-5 rounded-2xl shadow-md border border-slate-800">
            <div>
              <span className="inline-block px-2.5 py-0.5 rounded-full text-[10px] font-extrabold uppercase mb-2 bg-blue-500/20 text-blue-300 border border-blue-500/30">
                {minutes.meetingType || meetingType}
              </span>
              <h3 className="text-base font-extrabold tracking-tight text-white">
                {displayTitle}
              </h3>
              <div className="flex flex-wrap items-center gap-4 text-xs text-slate-300 mt-1.5 font-medium">
                {(minutes.meetingTime || meetingTime) && (
                  <span className="flex items-center gap-1.5">
                    <Clock size={13} className="text-amber-400" />
                    {minutes.meetingTime || meetingTime}
                  </span>
                )}
                {(minutes.participants || participants) && (
                  <span className="flex items-center gap-1.5">
                    <Users size={13} className="text-indigo-400" />
                    {minutes.participants || participants}
                  </span>
                )}
              </div>
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

          {/* Overview */}
          {overview && (
            <div className="rounded-2xl border border-blue-200 bg-blue-50/60 p-4 shadow-2xs">
              <h4 className="text-xs font-extrabold text-blue-950 uppercase tracking-wider mb-1 flex items-center gap-2">
                <Target size={15} className="text-blue-600" /> Tổng quan buổi họp:
              </h4>
              <p className="text-xs text-slate-800 font-medium leading-relaxed">
                {overview}
              </p>
            </div>
          )}

          <div className="grid gap-5 md:grid-cols-2">
            {/* Discussed Topics */}
            {topics.length > 0 && (
              <div className="rounded-2xl border border-slate-200 bg-white p-4.5 shadow-2xs">
                <h4 className="text-xs font-extrabold text-slate-900 uppercase tracking-wider mb-3 flex items-center gap-2 text-indigo-600">
                  <ListChecks size={16} /> Nội dung đã thảo luận:
                </h4>
                <div className="space-y-2.5">
                  {topics.map((t, idx) => (
                    <div key={idx} className="flex items-start gap-2.5 p-2.5 rounded-lg bg-slate-50 border border-slate-100">
                      <span className="size-1.5 rounded-full bg-indigo-500 shrink-0 mt-1.5" />
                      <span className="text-xs text-slate-800 font-medium leading-relaxed">
                        {t}
                      </span>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* Agreed Decisions */}
            {decisions.length > 0 && (
              <div className="rounded-2xl border border-emerald-200 bg-emerald-50/50 p-4.5 shadow-2xs">
                <h4 className="text-xs font-extrabold text-emerald-950 uppercase tracking-wider mb-3 flex items-center gap-2">
                  <CheckCircle2 size={16} className="text-emerald-600" /> Quyết định đã thống nhất:
                </h4>
                <div className="space-y-2.5">
                  {decisions.map((d, idx) => (
                    <div key={idx} className="flex items-start gap-2.5 p-2.5 rounded-xl bg-white border border-emerald-200/80">
                      <CheckCircle2 size={15} className="text-emerald-600 shrink-0 mt-0.5" />
                      <span className="text-xs text-slate-900 font-bold leading-relaxed">
                        {d}
                      </span>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>

          <div className="grid gap-5 md:grid-cols-2">
            {/* Unresolved Issues */}
            {unresolved.length > 0 && (
              <div className="rounded-2xl border border-amber-200 bg-amber-50/60 p-4.5 shadow-2xs">
                <h4 className="text-xs font-extrabold text-amber-950 uppercase tracking-wider mb-3 flex items-center gap-2">
                  <Clock size={16} className="text-amber-600" /> Vấn đề chưa giải quyết:
                </h4>
                <div className="space-y-2">
                  {unresolved.map((u, idx) => (
                    <div key={idx} className="flex items-start gap-2.5 p-2.5 rounded-xl bg-white border border-amber-200/80">
                      <span className="size-1.5 rounded-full bg-amber-500 shrink-0 mt-1.5" />
                      <span className="text-xs text-slate-800 font-semibold leading-relaxed">
                        {u}
                      </span>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* Related Risks */}
            {risks.length > 0 && (
              <div className="rounded-2xl border border-rose-200 bg-rose-50/60 p-4.5 shadow-2xs">
                <h4 className="text-xs font-extrabold text-rose-950 uppercase tracking-wider mb-3 flex items-center gap-2">
                  <AlertTriangle size={16} className="text-rose-600" /> Rủi ro liên quan:
                </h4>
                <div className="space-y-2">
                  {risks.map((r, idx) => (
                    <div key={idx} className="flex items-start gap-2.5 p-2.5 rounded-xl bg-white border border-rose-200/80">
                      <AlertTriangle size={15} className="text-rose-500 shrink-0 mt-0.5" />
                      <span className="text-xs text-slate-800 font-medium leading-relaxed">
                        {r}
                      </span>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>

          {/* Action Items Draft */}
          {actionItems.length > 0 && (
            <div className="rounded-2xl border border-indigo-200 bg-indigo-50/30 p-4.5 shadow-2xs">
              <h4 className="text-xs font-extrabold text-indigo-950 uppercase tracking-wider mb-3 flex items-center gap-2">
                <CheckSquare size={16} className="text-indigo-600" /> Bản nháp Việc cần làm (Action Items Draft):
              </h4>
              <div className="grid gap-3 sm:grid-cols-2">
                {actionItems.map((item, idx) => (
                  <div key={idx} className="p-3 rounded-xl bg-white border border-indigo-100 shadow-2xs flex flex-col justify-between">
                    <div>
                      <div className="flex items-center justify-between mb-1">
                        <span className="text-xs font-extrabold text-slate-900 leading-snug">
                          {item.title || item.content || 'Task'}
                        </span>
                        {item.priority && (
                          <span className="rounded-full bg-indigo-100 px-2 py-0.5 text-[9px] font-extrabold text-indigo-800 uppercase">
                            {item.priority}
                          </span>
                        )}
                      </div>
                      {(item.description || item.sourceNote) && (
                        <p className="text-[11px] text-slate-500 mt-1 leading-snug">
                          {item.description || item.sourceNote}
                        </p>
                      )}
                    </div>
                    <div className="flex items-center justify-between text-[11px] text-slate-400 mt-2.5 pt-2 border-t border-slate-100 font-medium">
                      {(item.assignee || item.suggestedAssignee) ? (
                        <span className="flex items-center gap-1 text-slate-700 font-bold">
                          <UserCheck size={13} className="text-indigo-500" /> @{item.assignee || item.suggestedAssignee}
                        </span>
                      ) : (
                        <span className="text-slate-400">Chưa gán</span>
                      )}
                      {item.dueDate && <span>Hạn: {item.dueDate}</span>}
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Summary Note */}
          {summary && (
            <div className="rounded-2xl border border-slate-200 bg-slate-50 p-4 shadow-2xs">
              <h4 className="text-xs font-extrabold text-slate-700 uppercase tracking-wider mb-1.5 flex items-center gap-2">
                <Bot size={15} className="text-brand" /> Tóm tắt cuối từ AI:
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
