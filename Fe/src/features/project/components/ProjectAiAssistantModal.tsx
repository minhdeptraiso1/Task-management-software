import { useState, useRef, useEffect } from 'react'
import {
  Sparkles,
  Send,
  User,
  RotateCcw,
  Copy,
  Check,
  Zap,
  X,
  ShieldAlert,
  MessageSquarePlus,
  Bot,
  Target,
  AlertTriangle,
  Link2,
  HelpCircle,
  CheckCircle2,
  SlidersHorizontal,
} from 'lucide-react'
import { Button, Modal } from '../../../components/ui'
import { askProjectAi } from '../services/ai.service'
import type { ChatMessage, ProjectAiAskResponse } from '../models/ai.model'
import type { Project } from '../models/project.model'
import { ProjectAiMeetingSuggestionsTab } from './ProjectAiMeetingSuggestionsTab'
import { ProjectAiMeetingMinutesTab } from './ProjectAiMeetingMinutesTab'
import { ProjectAiActionItemsTab } from './ProjectAiActionItemsTab'
import { ProjectMeetingsTab } from '../views/ProjectMeetingsTab'

interface ProjectAiAssistantModalProps {
  open: boolean
  onClose: () => void
  project: Project | null
  initialTab?: 'chat' | 'meeting' | 'minutes' | 'action-items' | 'meetings'
}

const QUICK_PROMPTS = [
  'Tóm tắt tình hình hiện tại của dự án và chỉ ra các vấn đề cần ưu tiên.',
  'Sprint đang chạy có bao nhiêu task bị blocked, quá hạn và chưa hoàn thành?',
  'Dựa trên số lượng task theo từng trạng thái, tiến độ sprint hiện tại như thế nào?',
  'Đề xuất 3 việc cần làm tiếp theo để cải thiện tiến độ dự án.',
  'Nếu sprint đang có nhiều task IN_PROGRESS nhưng ít task DONE thì rủi ro là gì?',
  'Backlog và số lượng thành viên hiện tại có dấu hiệu mất cân bằng không?',
]

function formatAiContentLine(line: string) {
  // Replace status tokens with inline styled badges
  const parts = line.split(/(\b(?:TODO|IN_PROGRESS|IN_REVIEW|DONE|BLOCKED|CANCELLED|ARCHIVED|ACTIVE|COMPLETED)\b)/g)

  return parts.map((part, i) => {
    if (part === 'DONE' || part === 'COMPLETED' || part === 'ACTIVE') {
      return (
        <span key={i} className="mx-1 inline-flex items-center rounded-full bg-emerald-100 px-2 py-0.5 text-[10px] font-extrabold text-emerald-800 border border-emerald-300 shadow-2xs">
          {part}
        </span>
      )
    }
    if (part === 'IN_PROGRESS' || part === 'IN_REVIEW') {
      return (
        <span key={i} className="mx-1 inline-flex items-center rounded-full bg-sky-100 px-2 py-0.5 text-[10px] font-extrabold text-sky-800 border border-sky-300 shadow-2xs">
          {part}
        </span>
      )
    }
    if (part === 'BLOCKED' || part === 'CANCELLED') {
      return (
        <span key={i} className="mx-1 inline-flex items-center rounded-full bg-rose-100 px-2 py-0.5 text-[10px] font-extrabold text-rose-800 border border-rose-300 shadow-2xs">
          {part}
        </span>
      )
    }
    if (part === 'TODO' || part === 'ARCHIVED') {
      return (
        <span key={i} className="mx-1 inline-flex items-center rounded-full bg-slate-200 px-2 py-0.5 text-[10px] font-extrabold text-slate-700 border border-slate-300 shadow-2xs">
          {part}
        </span>
      )
    }

    // Parse **bold text** inside part
    const boldParts = part.split(/(\*\*.*?\*\*)/g)
    return boldParts.map((bPart, bIdx) => {
      if (bPart.startsWith('**') && bPart.endsWith('**')) {
        return (
          <strong key={`${i}-${bIdx}`} className="font-extrabold text-slate-900">
            {bPart.slice(2, -2)}
          </strong>
        )
      }
      return bPart
    })
  })
}

function renderAiMessageBody(text: string) {
  const lines = text.split('\n')
  return (
    <div className="space-y-2 text-xs leading-relaxed text-slate-700 font-normal">
      {lines.map((line, idx) => {
        const trimmed = line.trim()
        if (!trimmed) return <div key={idx} className="h-1" />

        // Check if line is bullet list item
        if (trimmed.startsWith('* ') || trimmed.startsWith('- ') || trimmed.startsWith('• ')) {
          const content = trimmed.substring(2)
          return (
            <div key={idx} className="flex items-start gap-2.5 pl-1 py-0.5">
              <span className="size-1.5 rounded-full bg-brand shrink-0 mt-1.5 shadow-2xs" />
              <div className="flex-1">{formatAiContentLine(content)}</div>
            </div>
          )
        }

        // Check if line is numbered list item
        const matchNumber = trimmed.match(/^(\d+)\.\s+(.*)/)
        if (matchNumber) {
          return (
            <div key={idx} className="flex items-start gap-2.5 pl-1 py-0.5">
              <span className="flex size-4 items-center justify-center rounded-full bg-amber-100 text-[10px] font-extrabold text-amber-800 shrink-0 mt-0.5 border border-amber-200">
                {matchNumber[1]}
              </span>
              <div className="flex-1">{formatAiContentLine(matchNumber[2])}</div>
            </div>
          )
        }

        return <p key={idx}>{formatAiContentLine(line)}</p>
      })}
    </div>
  )
}

function AiThinkingIndicator() {
  const [progress, setProgress] = useState(0)

  useEffect(() => {
    const timer = setInterval(() => {
      setProgress(prev => {
        if (prev >= 98) return 98
        const step = Math.max(1, Math.floor((99 - prev) / 8))
        return prev + step
      })
    }, 120)

    return () => clearInterval(timer)
  }, [])

  return (
    <div className="flex flex-col gap-2.5 p-1 min-w-[280px] sm:min-w-[340px]">
      <div className="flex items-center gap-3">
        <div className="relative flex size-9 items-center justify-center rounded-xl bg-gradient-to-tr from-amber-500/20 to-brand/20 border border-amber-400/40 text-amber-600 shrink-0 shadow-inner">
          <Bot size={18} className="animate-bounce text-brand" />
          <span className="absolute size-full rounded-xl bg-amber-400/20 animate-ping" />
        </div>
        <div className="flex-1">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <span className="text-xs font-extrabold text-slate-900 tracking-tight">AI đang suy nghĩ...</span>
              <div className="flex items-center gap-1">
                <span className="size-1.5 rounded-full bg-amber-500 animate-bounce [animation-delay:-0.3s]" />
                <span className="size-1.5 rounded-full bg-amber-500 animate-bounce [animation-delay:-0.15s]" />
                <span className="size-1.5 rounded-full bg-amber-500 animate-bounce" />
              </div>
            </div>
            <span className="text-[11px] font-extrabold text-brand font-mono tabular-nums">
              {progress}%
            </span>
          </div>
          <p className="text-[11px] text-slate-500 font-medium mt-0.5 animate-pulse">
            Đang đọc dữ liệu Sprint, Backlog & tổng hợp phân tích...
          </p>
        </div>
      </div>

      {/* Smoothly animated progress bar from 0% to 99% */}
      <div className="relative h-2 w-full overflow-hidden rounded-full bg-slate-100/90 border border-slate-200/60 p-0.5 shadow-inner">
        <div
          className="h-full rounded-full bg-gradient-to-r from-amber-400 via-brand to-emerald-400 transition-all duration-200 ease-out shadow-2xs"
          style={{ width: `${progress}%` }}
        />
      </div>
    </div>
  )
}

function normalizeArrayString(val: any): string[] {
  if (Array.isArray(val)) {
    return val.map(item => (typeof item === 'string' ? item : item?.code || item?.title || JSON.stringify(item)))
  }
  if (typeof val === 'string' && val.trim()) {
    return [val.trim()]
  }
  return []
}

function renderStructuredAiCards(data?: ProjectAiAskResponse) {
  if (!data) return null

  const keyPoints = normalizeArrayString(data.keyPoints)
  const relatedTasks = normalizeArrayString(data.relatedTasks)
  const risks = normalizeArrayString(data.risks)
  const suggestions = normalizeArrayString(data.suggestions)
  const limitation = data.limitation

  const hasExtra =
    keyPoints.length > 0 ||
    relatedTasks.length > 0 ||
    risks.length > 0 ||
    suggestions.length > 0 ||
    Boolean(limitation)

  if (!hasExtra) return null

  return (
    <div className="mt-3 pt-3 border-t border-slate-100 space-y-3">
      {/* Key Points */}
      {keyPoints.length > 0 && (
        <div className="rounded-xl border border-indigo-100 bg-indigo-50/50 p-3 text-xs">
          <h5 className="font-extrabold text-indigo-900 flex items-center gap-1.5 mb-1.5 uppercase text-[10px] tracking-wider">
            <Target size={14} className="text-indigo-600" /> Điểm phân tích chính:
          </h5>
          <ul className="space-y-1 pl-1">
            {keyPoints.map((kp, idx) => (
              <li key={idx} className="flex items-start gap-2 text-slate-700 font-medium">
                <span className="size-1 rounded-full bg-indigo-500 shrink-0 mt-1.5" />
                <span>{kp}</span>
              </li>
            ))}
          </ul>
        </div>
      )}

      {/* Related Tasks */}
      {relatedTasks.length > 0 && (
        <div className="rounded-xl border border-blue-100 bg-blue-50/50 p-3 text-xs">
          <h5 className="font-extrabold text-blue-900 flex items-center gap-1.5 mb-1.5 uppercase text-[10px] tracking-wider">
            <Link2 size={14} className="text-blue-600" /> Task liên quan:
          </h5>
          <div className="flex flex-wrap gap-1.5">
            {relatedTasks.map((t, idx) => (
              <span key={idx} className="rounded-md bg-white border border-blue-200 text-blue-800 font-bold px-2 py-0.5 text-[11px] shadow-2xs">
                {t}
              </span>
            ))}
          </div>
        </div>
      )}

      {/* Risks */}
      {risks.length > 0 && (
        <div className="rounded-xl border border-amber-200 bg-amber-50/60 p-3 text-xs">
          <h5 className="font-extrabold text-amber-900 flex items-center gap-1.5 mb-1.5 uppercase text-[10px] tracking-wider">
            <AlertTriangle size={14} className="text-amber-600" /> Rủi ro tiềm ẩn:
          </h5>
          <ul className="space-y-1 pl-1">
            {risks.map((r, idx) => (
              <li key={idx} className="flex items-start gap-2 text-amber-950 font-medium">
                <span className="size-1.5 rounded-full bg-amber-500 shrink-0 mt-1.5" />
                <span>{r}</span>
              </li>
            ))}
          </ul>
        </div>
      )}

      {/* Suggestions */}
      {suggestions.length > 0 && (
        <div className="rounded-xl border border-emerald-200 bg-emerald-50/60 p-3 text-xs">
          <h5 className="font-extrabold text-emerald-900 flex items-center gap-1.5 mb-1.5 uppercase text-[10px] tracking-wider">
            <Sparkles size={14} className="text-emerald-600" /> Đề xuất hành động:
          </h5>
          <ul className="space-y-1 pl-1">
            {suggestions.map((s, idx) => (
              <li key={idx} className="flex items-start gap-2 text-emerald-950 font-medium">
                <CheckCircle2 size={13} className="text-emerald-600 shrink-0 mt-0.5" />
                <span>{s}</span>
              </li>
            ))}
          </ul>
        </div>
      )}

      {/* Limitation */}
      {limitation && (
        <div className="rounded-xl border border-slate-200 bg-slate-50 p-2.5 text-[11px] text-slate-600 flex items-start gap-2">
          <HelpCircle size={14} className="text-slate-400 shrink-0 mt-0.5" />
          <div>
            <strong className="font-extrabold text-slate-700">Giới hạn thông tin:</strong> {limitation}
          </div>
        </div>
      )}
    </div>
  )
}

export function ProjectAiAssistantModal({ open, onClose, project, initialTab = 'chat' }: ProjectAiAssistantModalProps) {
  const [activeTab, setActiveTab] = useState<'chat' | 'meeting' | 'minutes' | 'action-items' | 'meetings'>(initialTab)
  const [messages, setMessages] = useState<ChatMessage[]>([])
  const [input, setInput] = useState('')
  const [additionalContext, setAdditionalContext] = useState('')
  const [showContextInput, setShowContextInput] = useState(false)

  const [loading, setLoading] = useState(false)
  const [copiedId, setCopiedId] = useState<string | null>(null)
  const messagesEndRef = useRef<HTMLDivElement>(null)

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }

  useEffect(() => {
    if (open) {
      scrollToBottom()
    }
  }, [messages, open])

  // Clear messages when project changes
  useEffect(() => {
    setMessages([])
  }, [project?.id])

  useEffect(() => {
    if (open) setActiveTab(initialTab)
  }, [open, initialTab])

  if (!project) return null

  const handleSend = async (textToSend?: string) => {
    const questionText = (textToSend || input).trim()
    if (!questionText || loading) return

    if (questionText.length < 5 || questionText.length > 1000) {
      alert('Câu hỏi phải có độ dài từ 5 đến 1.000 ký tự.')
      return
    }

    const userMessageId = `user-${Date.now()}`
    const aiMessageId = `ai-${Date.now()}`

    const userMsg: ChatMessage = {
      id: userMessageId,
      sender: 'user',
      text: questionText,
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    }

    const initialAiMsg: ChatMessage = {
      id: aiMessageId,
      sender: 'ai',
      text: '',
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      loading: true,
    }

    setMessages(prev => [...prev, userMsg, initialAiMsg])
    if (!textToSend) setInput('')
    setLoading(true)

    try {
      const res = await askProjectAi(project.id, {
        question: questionText,
        additionalContext: additionalContext.trim() || undefined,
      })
      setMessages(prev =>
        prev.map(msg =>
          msg.id === aiMessageId
            ? {
                ...msg,
                text: res.answer,
                provider: res.provider,
                model: res.model,
                structuredData: res,
                loading: false,
              }
            : msg
        )
      )
    } catch (err: any) {
      const errorMsg = err instanceof Error ? err.message : 'Không thể kết nối tới dịch vụ AI'
      setMessages(prev =>
        prev.map(msg =>
          msg.id === aiMessageId
            ? {
                ...msg,
                text: `Lỗi: ${errorMsg}`,
                error: errorMsg,
                loading: false,
              }
            : msg
        )
      )
    } finally {
      setLoading(false)
    }
  }

  const handleCopy = (id: string, text: string) => {
    navigator.clipboard.writeText(text)
    setCopiedId(id)
    setTimeout(() => setCopiedId(null), 2000)
  }

  const handleClearHistory = () => {
    setMessages([])
  }

  return (
    <Modal
      open={open}
      onClose={onClose}
      hideHeader={true}
      maxWidthClass="max-w-6xl"
      bodyClassName="p-0"
    >
      <div className="flex flex-col h-[88vh] max-h-[860px] bg-slate-50/50 rounded-2xl overflow-hidden">
        {/* Sleek Dark Header */}
        <div className="flex items-center justify-between border-b border-slate-800 bg-gradient-to-r from-slate-900 via-slate-950 to-slate-900 px-6 py-4 text-white shrink-0 shadow-md">
          <div className="flex items-center gap-3.5">
            <div className="relative size-11 rounded-2xl overflow-hidden border-2 border-amber-400 shadow-md shadow-amber-500/20 shrink-0 flex items-center justify-center bg-gradient-to-br from-amber-500 via-orange-500 to-brand">
              <img src="/ai-avatar.jpg" alt="AI Mascot" className="size-full object-cover" onError={(e) => { (e.target as HTMLElement).style.display = 'none' }} />
            </div>
            <div>
              <div className="flex items-center gap-2.5">
                <h3 className="font-extrabold text-white text-base tracking-tight">Trợ lý AI HiCAS</h3>
                <span className="inline-flex items-center gap-1.5 rounded-full bg-emerald-500/20 px-2.5 py-0.5 text-[10px] font-extrabold text-emerald-300 border border-emerald-500/30 backdrop-blur-xs">
                  <span className="size-1.5 rounded-full bg-emerald-400 animate-ping" />
                  Gemini 2.5 Flash
                </span>
              </div>
              <p className="text-xs text-slate-400 mt-0.5">
                Dự án: <span className="font-bold text-amber-400">{project.code}</span> - {project.name}
              </p>
            </div>
          </div>

          <div className="flex items-center gap-2">
            {messages.length > 0 && (
              <Button
                type="button"
                variant="ghost"
                tone="dark"
                size="sm"
                leadingIcon={<RotateCcw size={14} />}
                onClick={handleClearHistory}
                title="Xóa hội thoại"
              >
                <span className="hidden sm:inline">Xóa hội thoại</span>
              </Button>
            )}
            <Button
              type="button"
              variant="ghost"
              tone="dark"
              size="sm"
              iconOnly
              aria-label="Đóng"
              title="Đóng"
              onClick={onClose}
              leadingIcon={<X size={18} />}
            />
          </div>
        </div>

        {/* Navigation Tabs Bar */}
        <div className="flex items-center gap-2 border-b border-slate-200 bg-white px-6 py-2 shrink-0 overflow-x-auto scrollbar-none">
          <Button
            type="button"
            variant="secondary"
            active={activeTab === 'chat'}
            size="sm"
            onClick={() => setActiveTab('chat')}
          >
            Hỏi đáp Project AI
          </Button>

          <Button
            type="button"
            variant="secondary"
            active={activeTab === 'meeting'}
            size="sm"
            onClick={() => setActiveTab('meeting')}
          >
            Gợi ý cuộc họp
          </Button>

          <Button
            type="button"
            variant="secondary"
            active={activeTab === 'minutes'}
            size="sm"
            onClick={() => setActiveTab('minutes')}
          >
            Biên bản họp AI
          </Button>

          <Button
            type="button"
            variant="secondary"
            active={activeTab === 'action-items'}
            size="sm"
            onClick={() => setActiveTab('action-items')}
          >
            Đề xuất Action Items
          </Button>

          <Button
            type="button"
            variant="secondary"
            active={activeTab === 'meetings'}
            size="sm"
            onClick={() => setActiveTab('meetings')}
          >
            Lịch cuộc họp
          </Button>
        </div>

        {activeTab === 'meetings' ? (
          <div className="flex-1 min-h-0 overflow-y-auto bg-slate-50">
            <ProjectMeetingsTab
              projectId={project.id}
              projectName={project.name}
              projectCode={project.code}
              userRole={project.currentUserRole || undefined}
              onOpenAiAssistant={tab => setActiveTab(tab === 'meeting' ? 'meeting' : tab === 'minutes' ? 'minutes' : tab === 'action-items' ? 'action-items' : 'chat')}
            />
          </div>
        ) : activeTab === 'meeting' ? (
          <ProjectAiMeetingSuggestionsTab
            projectId={project.id}
            projectName={project.name}
            projectCode={project.code}
          />
        ) : activeTab === 'minutes' ? (
          <ProjectAiMeetingMinutesTab
            projectId={project.id}
            projectName={project.name}
            projectCode={project.code}
          />
        ) : activeTab === 'action-items' ? (
          <ProjectAiActionItemsTab
            projectId={project.id}
            projectName={project.name}
            projectCode={project.code}
          />
        ) : (
          <>
            {/* Chat Conversation Stream */}
            <div className="flex-1 overflow-y-auto p-6 space-y-6 scrollbar-thin">
              {messages.length === 0 ? (
                <div className="h-full flex flex-col items-center justify-center text-center p-8 bg-white/80 backdrop-blur-md rounded-2xl border border-slate-200/80 shadow-xs my-auto">
                  <div className="relative size-20 rounded-3xl overflow-hidden border-3 border-amber-400 shadow-xl shadow-amber-500/20 mb-4 shrink-0">
                    <img src="/ai-avatar.jpg" alt="AI Mascot" className="size-full object-cover" />
                  </div>
                  <h4 className="font-extrabold text-slate-900 text-base">Hỏi đáp AI theo ngữ cảnh thực tế dự án</h4>
                  <p className="text-xs text-slate-500 max-w-lg mt-1.5 mb-6 leading-relaxed">
                    Gemini AI sẽ tự động đọc dữ liệu tổng hợp về Sprint, Backlog, Task quá hạn và các thành viên để cung cấp câu trả lời chính xác nhất.
                  </p>

                  <div className="w-full max-w-xl">
                    <p className="text-[11px] font-extrabold text-slate-400 uppercase tracking-wider text-left mb-3 flex items-center gap-1.5">
                      <Zap size={13} className="text-amber-500" /> Gợi ý câu hỏi phổ biến:
                    </p>
                    <div className="grid gap-2.5 sm:grid-cols-2">
                      {QUICK_PROMPTS.map((prompt, idx) => (
                        <button
                          key={idx}
                          type="button"
                          onClick={() => handleSend(prompt)}
                          className="text-left text-xs font-semibold text-slate-700 bg-white border border-slate-200 p-3.5 rounded-xl hover:border-brand hover:bg-amber-50/50 hover:text-brand hover:-translate-y-0.5 transition-all shadow-2xs cursor-pointer flex items-start gap-2.5 group"
                        >
                          <MessageSquarePlus size={16} className="text-amber-500 group-hover:text-brand shrink-0 mt-0.5 transition-colors" />
                          <span className="leading-snug">{prompt}</span>
                        </button>
                      ))}
                    </div>
                  </div>
                </div>
              ) : (
                messages.map(msg => (
                  <div
                    key={msg.id}
                    className={`flex gap-3.5 ${msg.sender === 'user' ? 'justify-end' : 'justify-start'}`}
                  >
                    {msg.sender === 'ai' && (
                      <div className="size-9 rounded-2xl overflow-hidden border-2 border-amber-400/80 shadow-md shrink-0 mt-1">
                        <img src="/ai-avatar.jpg" alt="AI Mascot" className="size-full object-cover" />
                      </div>
                    )}

                    <div
                      className={`max-w-[88%] sm:max-w-[80%] rounded-2xl p-4 transition-all ${
                        msg.sender === 'user'
                          ? 'bg-gradient-to-r from-brand to-amber-500 text-white font-medium rounded-tr-xs shadow-md shadow-brand/10 text-xs leading-relaxed'
                          : msg.error
                          ? 'bg-rose-50 border border-rose-200 text-rose-800 rounded-tl-xs shadow-2xs'
                          : 'bg-white border border-slate-200/80 text-slate-800 rounded-tl-xs shadow-sm space-y-2'
                      }`}
                    >
                      {msg.loading ? (
                        <AiThinkingIndicator />
                      ) : msg.error ? (
                        <div className="flex items-start gap-2.5 text-xs">
                          <ShieldAlert size={18} className="text-rose-600 shrink-0 mt-0.5" />
                          <div>
                            <p className="font-extrabold text-rose-900">Không thể thực hiện câu hỏi</p>
                            <p className="mt-1 text-rose-700">{msg.text}</p>
                          </div>
                        </div>
                      ) : (
                        <div>
                          {msg.sender === 'user' ? (
                            <div className="whitespace-pre-wrap break-words font-sans">{msg.text}</div>
                          ) : (
                            <div>
                              {renderAiMessageBody(msg.text)}
                              {renderStructuredAiCards(msg.structuredData)}
                              <div className="mt-3 pt-2.5 border-t border-slate-100 flex items-center justify-between text-[11px] text-slate-400">
                                <span className="font-semibold text-slate-400">
                                  {msg.model || 'Gemini 2.5 Flash'} · {msg.timestamp}
                                </span>
                                <button
                                  type="button"
                                  onClick={() => handleCopy(msg.id, msg.text)}
                                  className="inline-flex items-center gap-1.5 px-2 py-1 rounded-lg hover:bg-slate-100 text-slate-600 hover:text-slate-900 transition-colors cursor-pointer font-medium"
                                >
                                  {copiedId === msg.id ? (
                                    <>
                                      <Check size={13} className="text-emerald-600" />
                                      <span className="text-emerald-600 font-bold">Đã sao chép</span>
                                    </>
                                  ) : (
                                    <>
                                      <Copy size={13} />
                                      <span>Sao chép</span>
                                    </>
                                  )}
                                </button>
                              </div>
                            </div>
                          )}
                        </div>
                      )}
                    </div>

                    {msg.sender === 'user' && (
                      <div className="size-9 rounded-2xl bg-slate-900 text-white flex items-center justify-center shrink-0 shadow-md mt-1">
                        <User size={18} />
                      </div>
                    )}
                  </div>
                ))
              )}
              <div ref={messagesEndRef} />
            </div>

            {/* Input Bar Footer */}
            <div className="border-t border-slate-200 bg-white p-4 shrink-0 shadow-lg space-y-2">
              {showContextInput && (
                <div className="flex items-center gap-2 bg-amber-50/60 rounded-xl border border-amber-200/80 px-3 py-1.5 transition-all">
                  <SlidersHorizontal size={14} className="text-amber-600 shrink-0" />
                  <input
                    type="text"
                    value={additionalContext}
                    onChange={e => setAdditionalContext(e.target.value)}
                    placeholder="Ngữ cảnh bổ sung (VD: Tập trung vào task quá hạn và task bị block)..."
                    className="flex-1 bg-transparent text-xs text-slate-900 placeholder:text-slate-400 focus:outline-none font-medium"
                  />
                  <button
                    type="button"
                    onClick={() => {
                      setAdditionalContext('')
                      setShowContextInput(false)
                    }}
                    className="text-[10px] text-slate-400 hover:text-rose-600 p-0.5 cursor-pointer font-bold"
                  >
                    <X size={12} />
                  </button>
                </div>
              )}

              <form
                onSubmit={e => {
                  e.preventDefault()
                  handleSend()
                }}
                className="flex items-center gap-2 bg-slate-50 rounded-2xl border border-slate-200 p-1.5 focus-within:border-brand focus-within:ring-2 focus-within:ring-brand/20 transition-all"
              >
                <input
                  type="text"
                  value={input}
                  onChange={e => setInput(e.target.value)}
                  placeholder="Đặt câu hỏi về dự án (5–1,000 ký tự)..."
                  disabled={loading}
                  className="flex-1 bg-transparent px-3 py-2 text-xs text-slate-900 placeholder:text-slate-400 focus:outline-none disabled:opacity-50 font-medium"
                />

                <button
                  type="button"
                  onClick={() => setShowContextInput(prev => !prev)}
                  title="Thêm ngữ cảnh bổ sung"
                  className={`p-2 rounded-xl transition-all cursor-pointer ${
                    showContextInput || additionalContext
                      ? 'bg-amber-100 text-amber-800 border border-amber-300'
                      : 'text-slate-400 hover:text-slate-700 hover:bg-slate-200/60'
                  }`}
                >
                  <SlidersHorizontal size={15} />
                </button>

                <button
                  type="submit"
                  disabled={!input.trim() || loading || input.trim().length < 5 || input.trim().length > 1000}
                  className="inline-flex items-center gap-2 rounded-xl bg-gradient-to-r from-brand to-amber-500 px-4 py-2 text-xs font-bold text-white shadow-md hover:shadow-lg hover:scale-102 active:scale-98 transition-all disabled:opacity-40 disabled:hover:scale-100 disabled:shadow-none cursor-pointer"
                >
                  {loading ? (
                    <>
                      <Sparkles size={16} className="animate-spin text-amber-200" />
                      <span className="hidden sm:inline">AI đang suy nghĩ...</span>
                    </>
                  ) : (
                    <>
                      <Send size={16} />
                      <span className="hidden sm:inline">Gửi câu hỏi</span>
                    </>
                  )}
                </button>
              </form>
            </div>
          </>
        )}
      </div>
    </Modal>
  )
}
