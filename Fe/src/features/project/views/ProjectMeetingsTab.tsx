import { useState, useEffect, useMemo } from 'react'
import {
  Video,
  Plus,
  Search,
  Calendar,
  Clock,
  Link2,
  Pencil,
  ExternalLink,
  Sparkles,
  ShieldAlert,
  User,
  Check,
  Copy,
} from 'lucide-react'
import { Button, Input, Modal, Textarea } from '../../../components/ui'
import type { ProjectMeeting, ProjectMeetingType, CreateMeetingRequest } from '../models/meeting.model'
import {
  getProjectMeetings,
  createProjectMeeting,
  updateGoogleMeetLink,
} from '../services/meeting.service'

interface ProjectMeetingsTabProps {
  projectId: string
  projectName: string
  projectCode: string
  userRole?: string
  onOpenAiAssistant?: (tab?: 'chat' | 'meeting' | 'minutes' | 'action-items') => void
}

const MEETING_TYPE_MAP: Record<
  ProjectMeetingType,
  { label: string; bg: string; text: string; border: string }
> = {
  DAILY: {
    label: 'Daily Standup',
    bg: 'bg-warning/15 font-bold',
    text: 'text-warning-dark',
    border: 'border-warning/30',
  },
  SPRINT_PLANNING: {
    label: 'Sprint Planning',
    bg: 'bg-info/10 font-bold',
    text: 'text-info',
    border: 'border-info/20',
  },
  SPRINT_REVIEW: {
    label: 'Sprint Review',
    bg: 'bg-cat-indigo/10 font-bold',
    text: 'text-cat-indigo',
    border: 'border-cat-indigo/20',
  },
  RETROSPECTIVE: {
    label: 'Retrospective',
    bg: 'bg-cat-purple/10 font-bold',
    text: 'text-cat-purple',
    border: 'border-cat-purple/20',
  },
  ISSUE_RESOLUTION: {
    label: 'Giải quyết rủi ro/Sự cố',
    bg: 'bg-danger/10 font-bold',
    text: 'text-danger',
    border: 'border-danger/20',
  },
  OTHER: {
    label: 'Cuộc họp khác',
    bg: 'bg-panel font-bold',
    text: 'text-muted',
    border: 'border-line',
  },
}

function getMeetingStatus(startTime: string, endTime: string): 'LIVE' | 'UPCOMING' | 'FINISHED' {
  const now = new Date().getTime()
  const start = new Date(startTime).getTime()
  const end = new Date(endTime).getTime()

  if (now >= start && now <= end) return 'LIVE'
  if (now < start) return 'UPCOMING'
  return 'FINISHED'
}

function formatDateTimeDisplay(dateStr: string) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  if (isNaN(d.getTime())) return dateStr
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const year = d.getFullYear()
  return `${hours}:${minutes}, ${day}/${month}/${year}`
}

function formatTimeOnlyDisplay(dateStr: string) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  if (isNaN(d.getTime())) return ''
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  return `${hours}:${minutes}`
}

function validateGoogleMeetUrl(url: string): boolean {
  if (!url || !url.trim()) return true
  const trimmed = url.trim()
  return (
    trimmed.startsWith('https://meet.google.com/') ||
    trimmed.startsWith('http://meet.google.com/')
  )
}

export function ProjectMeetingsTab({
  projectId,
  projectName,
  projectCode,
  userRole,
  onOpenAiAssistant,
}: ProjectMeetingsTabProps) {
  const [meetings, setMeetings] = useState<ProjectMeeting[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  // Filters
  const [search, setSearch] = useState('')
  const [selectedType, setSelectedType] = useState<string>('ALL')
  const [selectedStatusFilter, setSelectedStatusFilter] = useState<string>('ALL')

  // Modals
  const [createModalOpen, setCreateModalOpen] = useState(false)
  const [linkModalOpen, setLinkModalOpen] = useState(false)
  const [selectedMeetingForLink, setSelectedMeetingForLink] = useState<ProjectMeeting | null>(null)

  // Create Form state
  const [createForm, setCreateForm] = useState<CreateMeetingRequest>({
    title: '',
    meetingType: 'DAILY',
    description: '',
    startTime: '',
    endTime: '',
    googleMeetLink: '',
  })
  const [createError, setCreateError] = useState<string | null>(null)
  const [createLoading, setCreateLoading] = useState(false)

  // Link Form state
  const [meetLinkInput, setMeetLinkInput] = useState('')
  const [linkError, setLinkError] = useState<string | null>(null)
  const [linkLoading, setLinkLoading] = useState(false)

  // Copied state
  const [copiedId, setCopiedId] = useState<string | null>(null)

  const canManageMeetings =
    userRole === 'OWNER' || userRole === 'PROJECT_MANAGER' || userRole === 'SCRUM_MASTER' || userRole === 'ADMIN'

  const fetchMeetings = async () => {
    try {
      setLoading(true)
      setError(null)
      const res = await getProjectMeetings(projectId)
      setMeetings(res || [])
    } catch (err: any) {
      setError(err?.message || 'Không thể tải danh sách cuộc họp dự án')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchMeetings()
  }, [projectId])

  const filteredMeetings = useMemo(() => {
    return meetings.filter(m => {
      // Type filter
      if (selectedType !== 'ALL' && m.meetingType !== selectedType) return false

      // Status filter
      if (selectedStatusFilter !== 'ALL') {
        const st = getMeetingStatus(m.startTime, m.endTime)
        if (selectedStatusFilter === 'LIVE' && st !== 'LIVE') return false
        if (selectedStatusFilter === 'UPCOMING' && st !== 'UPCOMING') return false
        if (selectedStatusFilter === 'FINISHED' && st !== 'FINISHED') return false
      }

      // Search keyword
      if (search.trim()) {
        const kw = search.trim().toLowerCase()
        const titleMatch = m.title?.toLowerCase().includes(kw)
        const descMatch = m.description?.toLowerCase().includes(kw)
        const creatorMatch = m.createdByName?.toLowerCase().includes(kw)
        if (!titleMatch && !descMatch && !creatorMatch) return false
      }

      return true
    })
  }, [meetings, selectedType, selectedStatusFilter, search])

  const handleOpenCreateModal = () => {
    const now = new Date()
    const nowIso = new Date(now.getTime() - now.getTimezoneOffset() * 60000)
      .toISOString()
      .slice(0, 16)
    const endIso = new Date(now.getTime() + 60 * 60000 - now.getTimezoneOffset() * 60000)
      .toISOString()
      .slice(0, 16)

    setCreateForm({
      title: '',
      meetingType: 'DAILY',
      description: '',
      startTime: nowIso,
      endTime: endIso,
      googleMeetLink: '',
    })
    setCreateError(null)
    setCreateModalOpen(true)
  }

  const handleCreateMeetingSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!createForm.title.trim()) {
      setCreateError('Vui lòng nhập tiêu đề cuộc họp.')
      return
    }
    if (!createForm.startTime || !createForm.endTime) {
      setCreateError('Vui lòng chọn đầy đủ thời gian bắt đầu và kết thúc.')
      return
    }

    const start = new Date(createForm.startTime).getTime()
    const end = new Date(createForm.endTime).getTime()
    if (end <= start) {
      setCreateError('Thời gian kết thúc phải diễn ra sau thời gian bắt đầu.')
      return
    }

    if (createForm.googleMeetLink && !validateGoogleMeetUrl(createForm.googleMeetLink)) {
      setCreateError('Link Google Meet phải đúng dạng https://meet.google.com/abc-defg-hij')
      return
    }

    try {
      setCreateLoading(true)
      setCreateError(null)
      await createProjectMeeting(projectId, {
        ...createForm,
        title: createForm.title.trim(),
        description: createForm.description?.trim() || undefined,
        googleMeetLink: createForm.googleMeetLink?.trim() || undefined,
      })
      setCreateModalOpen(false)
      fetchMeetings()
    } catch (err: any) {
      setCreateError(err?.message || 'Không thể tạo cuộc họp mới.')
    } finally {
      setCreateLoading(false)
    }
  }

  const handleOpenLinkModal = (meeting: ProjectMeeting) => {
    setSelectedMeetingForLink(meeting)
    setMeetLinkInput(meeting.googleMeetLink || '')
    setLinkError(null)
    setLinkModalOpen(true)
  }

  const handleUpdateLinkSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!selectedMeetingForLink) return

    if (!meetLinkInput.trim()) {
      setLinkError('Vui lòng nhập đường dẫn Google Meet.')
      return
    }

    if (!validateGoogleMeetUrl(meetLinkInput)) {
      setLinkError('Link Google Meet phải đúng định dạng https://meet.google.com/abc-defg-hij')
      return
    }

    try {
      setLinkLoading(true)
      setLinkError(null)
      await updateGoogleMeetLink(projectId, selectedMeetingForLink.id, meetLinkInput.trim())
      setLinkModalOpen(false)
      fetchMeetings()
    } catch (err: any) {
      setLinkError(err?.message || 'Không thể cập nhật link Google Meet.')
    } finally {
      setLinkLoading(false)
    }
  }

  const handleCopyLink = (meetingId: string, link: string) => {
    navigator.clipboard.writeText(link)
    setCopiedId(meetingId)
    setTimeout(() => setCopiedId(null), 2000)
  }

  return (
    <div className="p-6 space-y-6 max-w-7xl mx-auto">
      {/* Header Banner */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 bg-white p-6 rounded-2xl border border-line/70 shadow-xs">
        <div>
          <div className="flex items-center gap-2 mb-1">
            <span className="px-2.5 py-0.5 rounded-md bg-amber-100 text-amber-800 text-[11px] font-extrabold tracking-wider uppercase border border-amber-200">
              {projectCode}
            </span>
            <span className="text-xs text-muted font-medium">· Quản lý cuộc họp & Google Meet dự án {projectName}</span>
          </div>
          <h2 className="text-xl font-bold text-ink flex items-center gap-2">
            <Video className="text-brand size-6" /> Lịch Cuộc Họp Dự Án
          </h2>
          <p className="text-xs text-muted mt-1 leading-relaxed max-w-2xl">
            Tổ chức các buổi Daily Standup, Sprint Planning, Review & Retrospective cho {projectName}. Gắn link Google Meet thủ công để các thành viên tham gia dễ dàng.
          </p>
        </div>

        <div className="flex items-center gap-3 shrink-0">
          {onOpenAiAssistant && (
            <Button
              variant="secondary"
              size="sm"
              onClick={() => onOpenAiAssistant('meeting')}
              leadingIcon={<Sparkles size={18} className="text-ink shrink-0" />}
              className="hover:border-amber-400 hover:bg-amber-50/50 font-bold"
            >
              Gợi ý cuộc họp AI
            </Button>
          )}

          {canManageMeetings && (
            <Button
              variant="primary"
              size="sm"
              onClick={handleOpenCreateModal}
              leadingIcon={<Plus size={16} />}
              className="bg-gradient-to-r from-brand to-amber-500 text-white font-bold shadow-md hover:shadow-lg"
            >
              Tạo cuộc họp mới
            </Button>
          )}
        </div>
      </div>

      {/* Filter Toolbar */}
      <div className="bg-white p-4 rounded-2xl border border-line/70 shadow-2xs flex flex-wrap items-center justify-between gap-4">
        <div className="flex flex-wrap items-center gap-3 flex-1 min-w-[280px]">
          {/* Search box */}
          <div className="relative flex-1 min-w-[200px] max-w-md">
            <Search className="absolute left-3 top-2.5 size-4 text-muted shrink-0" />
            <input
              type="text"
              value={search}
              onChange={e => setSearch(e.target.value)}
              placeholder="Tìm theo tiêu đề, mô tả hoặc người tạo..."
              className="w-full bg-canvas pl-9 pr-3 py-2 text-xs rounded-xl border border-line focus:outline-none focus:border-brand focus:ring-1 focus:ring-brand/20 transition-all font-medium text-ink"
            />
            {search && (
              <button
                type="button"
                onClick={() => setSearch('')}
                className="absolute right-3 top-2.5 text-muted hover:text-ink text-xs font-bold"
              >
                ✕
              </button>
            )}
          </div>

          {/* Type filter dropdown */}
          <select
            value={selectedType}
            onChange={e => setSelectedType(e.target.value)}
            className="bg-canvas px-3 py-2 text-xs rounded-xl border border-line font-semibold text-ink focus:outline-none focus:border-brand cursor-pointer"
          >
            <option value="ALL">Tất cả loại cuộc họp</option>
            <option value="DAILY">Daily Standup</option>
            <option value="SPRINT_PLANNING">Sprint Planning</option>
            <option value="SPRINT_REVIEW">Sprint Review</option>
            <option value="RETROSPECTIVE">Retrospective</option>
            <option value="ISSUE_RESOLUTION">Giải quyết rủi ro/Sự cố</option>
            <option value="OTHER">Cuộc họp khác</option>
          </select>

          {/* Status filter dropdown */}
          <select
            value={selectedStatusFilter}
            onChange={e => setSelectedStatusFilter(e.target.value)}
            className="bg-canvas px-3 py-2 text-xs rounded-xl border border-line font-semibold text-ink focus:outline-none focus:border-brand cursor-pointer"
          >
            <option value="ALL">Tất cả trạng thái</option>
            <option value="LIVE">🔴 Đang diễn ra</option>
            <option value="UPCOMING">🔵 Sắp diễn ra</option>
            <option value="FINISHED">⚪ Đã kết thúc</option>
          </select>
        </div>

        <div className="text-xs text-muted font-medium">
          Hiển thị <strong className="text-ink font-bold">{filteredMeetings.length}</strong> / {meetings.length} cuộc họp
        </div>
      </div>

      {/* Main Content List / Cards */}
      {loading ? (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {[1, 2, 3].map(i => (
            <div key={i} className="h-64 rounded-2xl bg-slate-100 animate-pulse border border-slate-200" />
          ))}
        </div>
      ) : error ? (
        <div className="p-8 rounded-2xl bg-rose-50 border border-rose-200 text-center space-y-3">
          <ShieldAlert className="size-10 text-rose-500 mx-auto" />
          <h4 className="font-extrabold text-rose-900 text-sm">{error}</h4>
          <Button variant="secondary" size="sm" onClick={fetchMeetings}>
            Thử lại
          </Button>
        </div>
      ) : filteredMeetings.length === 0 ? (
        <div className="p-12 text-center bg-white rounded-2xl border border-line/70 shadow-xs space-y-4">
          <div className="size-16 rounded-2xl bg-amber-50 border border-amber-100 flex items-center justify-center mx-auto text-amber-600">
            <Video size={32} />
          </div>
          <div>
            <h4 className="font-bold text-ink text-base">Chưa tìm thấy cuộc họp nào</h4>
            <p className="text-xs text-muted max-w-md mx-auto mt-1">
              {search || selectedType !== 'ALL' || selectedStatusFilter !== 'ALL'
                ? 'Không có cuộc họp phù hợp với bộ lọc hiện tại. Hãy thử thay đổi từ khóa hoặc bộ lọc.'
                : 'Dự án này chưa có cuộc họp nào được lên lịch. Hãy bấm nút tạo cuộc họp để bắt đầu!'}
            </p>
          </div>
          {canManageMeetings && (
            <Button
              variant="primary"
              size="sm"
              onClick={handleOpenCreateModal}
              leadingIcon={<Plus size={16} />}
              className="bg-brand text-white font-bold"
            >
              Tạo cuộc họp mới
            </Button>
          )}
        </div>
      ) : (
        <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
          {filteredMeetings.map(meeting => {
            const status = getMeetingStatus(meeting.startTime, meeting.endTime)
            const typeConfig = MEETING_TYPE_MAP[meeting.meetingType] || MEETING_TYPE_MAP.OTHER

            return (
              <div
                key={meeting.id}
                className="bg-white rounded-2xl border border-line/80 shadow-xs hover:shadow-md transition-all duration-200 p-5 flex flex-col justify-between space-y-4 hover:-translate-y-0.5"
              >
                {/* Card Top Header */}
                <div className="space-y-3">
                  <div className="flex items-center justify-between gap-2">
                    <span
                      className={`px-2.5 py-0.5 rounded-full text-[10px] uppercase font-extrabold border ${typeConfig.bg} ${typeConfig.text} ${typeConfig.border}`}
                    >
                      {typeConfig.label}
                    </span>

                    {/* Status capsule */}
                    {status === 'LIVE' ? (
                      <span className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full bg-success/10 text-success text-[10px] font-extrabold border border-success/20 animate-pulse">
                        <span className="size-2 rounded-full bg-success" />
                        ĐANG DIỄN RA
                      </span>
                    ) : status === 'UPCOMING' ? (
                      <span className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full bg-info/10 text-info text-[10px] font-extrabold border border-info/20">
                        <Clock size={11} />
                        SẮP DIỄN RA
                      </span>
                    ) : (
                      <span className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full bg-panel text-muted text-[10px] font-extrabold border border-line">
                        ĐÃ KẾT THÚC
                      </span>
                    )}
                  </div>

                  {/* Title */}
                  <h3 className="font-bold text-ink text-base line-clamp-2 leading-snug">
                    {meeting.title}
                  </h3>

                  {/* Description */}
                  {meeting.description && (
                    <p className="text-xs text-muted line-clamp-2 leading-relaxed font-normal">
                      {meeting.description}
                    </p>
                  )}

                  {/* Time info */}
                  <div className="space-y-1.5 pt-1 text-xs text-slate-700 font-medium">
                    <div className="flex items-center gap-2 text-slate-600">
                      <Calendar size={14} className="text-brand shrink-0" />
                      <span>{formatDateTimeDisplay(meeting.startTime)}</span>
                    </div>
                    <div className="flex items-center gap-2 text-slate-500 text-[11px] pl-5">
                      <span>Thời lượng: đến {formatTimeOnlyDisplay(meeting.endTime)}</span>
                    </div>
                  </div>

                  {/* Creator */}
                  {meeting.createdByName && (
                    <div className="flex items-center gap-2 text-[11px] text-muted pt-1">
                      <User size={13} className="shrink-0 text-slate-400" />
                      <span>Tạo bởi: <strong className="text-ink font-semibold">{meeting.createdByName}</strong></span>
                    </div>
                  )}
                </div>

                {/* Google Meet Block & Actions */}
                <div className="pt-3 border-t border-line/60 space-y-3">
                  {meeting.hasGoogleMeetLink || meeting.googleMeetLink ? (
                    <div className="bg-emerald-50/70 border border-emerald-200 rounded-xl p-3 space-y-2">
                      <div className="flex items-center justify-between gap-2 text-xs">
                        <span className="font-extrabold text-emerald-900 flex items-center gap-1.5">
                          <Video size={14} className="text-emerald-600" /> Link Google Meet:
                        </span>
                        <div className="flex items-center gap-1">
                          <button
                            type="button"
                            onClick={() => handleCopyLink(meeting.id, meeting.googleMeetLink!)}
                            className="p-1 text-emerald-700 hover:bg-emerald-100 rounded-lg transition-colors cursor-pointer"
                            title="Sao chép link"
                          >
                            {copiedId === meeting.id ? (
                              <Check size={13} className="text-emerald-700 font-bold" />
                            ) : (
                              <Copy size={13} />
                            )}
                          </button>

                          {canManageMeetings && (
                            <button
                              type="button"
                              onClick={() => handleOpenLinkModal(meeting)}
                              className="p-1 text-emerald-700 hover:bg-emerald-100 rounded-lg transition-colors cursor-pointer"
                              title="Sửa link Google Meet"
                            >
                              <Pencil size={13} />
                            </button>
                          )}
                        </div>
                      </div>

                      <div className="text-[11px] text-emerald-800 font-mono truncate bg-white/80 px-2 py-1 rounded-md border border-emerald-200/60">
                        {meeting.googleMeetLink}
                      </div>

                      {status !== 'FINISHED' && (
                        <a
                          href={meeting.googleMeetLink}
                          target="_blank"
                          rel="noopener noreferrer"
                          className="inline-flex w-full items-center justify-center gap-2 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white font-extrabold text-xs py-2 px-3 shadow-sm hover:shadow transition-all cursor-pointer"
                        >
                          <Video size={15} /> Tham gia cuộc họp ngay
                          <ExternalLink size={13} className="opacity-80" />
                        </a>
                      )}
                    </div>
                  ) : (
                    <div className="bg-amber-50/70 border border-amber-200 rounded-xl p-3 text-center space-y-2">
                      <p className="text-xs text-amber-900 font-semibold flex items-center justify-center gap-1.5">
                        <ShieldAlert size={14} className="text-amber-600" /> Chưa gắn link Google Meet
                      </p>

                      {canManageMeetings ? (
                        <button
                          type="button"
                          onClick={() => handleOpenLinkModal(meeting)}
                          className="inline-flex items-center gap-1.5 text-xs font-bold text-amber-800 bg-amber-100 hover:bg-amber-200 border border-amber-300 px-3 py-1.5 rounded-xl transition-all cursor-pointer"
                        >
                          <Link2 size={13} /> + Gắn link Google Meet
                        </button>
                      ) : (
                        <span className="text-[11px] text-amber-700 italic">Vui lòng báo PM hoặc Scrum Master cập nhật link</span>
                      )}
                    </div>
                  )}

                  {/* AI Quick Actions */}
                  {onOpenAiAssistant && (
                    <button
                      type="button"
                      onClick={() => onOpenAiAssistant('minutes')}
                      className="w-full text-left text-xs font-semibold text-slate-700 bg-slate-50 hover:bg-amber-50 hover:text-brand border border-slate-200 p-2.5 rounded-xl transition-all cursor-pointer flex items-center justify-between group"
                    >
                      <span className="flex items-center gap-2">
                        <Sparkles size={14} className="text-amber-500 group-hover:text-brand transition-colors" />
                        <span>Tạo biên bản họp với AI</span>
                      </span>
                      <span className="text-[10px] font-bold text-amber-600 bg-amber-100/80 px-2 py-0.5 rounded-full">
                        AI Assistant
                      </span>
                    </button>
                  )}
                </div>
              </div>
            )
          })}
        </div>
      )}

      {/* CREATE MEETING MODAL */}
      <Modal
        open={createModalOpen}
        onClose={() => setCreateModalOpen(false)}
        title="Lên lịch cuộc họp mới"
        maxWidthClass="max-w-xl"
      >
        <form onSubmit={handleCreateMeetingSubmit} className="space-y-4 p-1">
          {createError && (
            <div className="p-3 rounded-xl bg-rose-50 border border-rose-200 text-xs text-rose-800 font-medium flex items-start gap-2">
              <ShieldAlert size={16} className="text-rose-600 shrink-0 mt-0.5" />
              <span>{createError}</span>
            </div>
          )}

          <div>
            <label className="block text-xs font-extrabold text-ink mb-1.5">
              Tiêu đề cuộc họp <span className="text-rose-500">*</span>
            </label>
            <Input
              type="text"
              value={createForm.title}
              onChange={e => setCreateForm({ ...createForm, title: e.target.value })}
              placeholder="VD: Daily Standup Sprint 3, Review tính năng Payment..."
              required
            />
          </div>

          <div className="grid gap-4 sm:grid-cols-2">
            <div>
              <label className="block text-xs font-extrabold text-ink mb-1.5">
                Loại cuộc họp <span className="text-rose-500">*</span>
              </label>
              <select
                value={createForm.meetingType}
                onChange={e =>
                  setCreateForm({ ...createForm, meetingType: e.target.value as ProjectMeetingType })
                }
                className="w-full bg-white px-3 py-2 text-xs rounded-xl border border-line font-medium text-ink focus:outline-none focus:border-brand"
              >
                <option value="DAILY">Daily Standup</option>
                <option value="SPRINT_PLANNING">Sprint Planning</option>
                <option value="SPRINT_REVIEW">Sprint Review</option>
                <option value="RETROSPECTIVE">Retrospective</option>
                <option value="ISSUE_RESOLUTION">Giải quyết rủi ro/Sự cố</option>
                <option value="OTHER">Cuộc họp khác</option>
              </select>
            </div>

            <div>
              <label className="block text-xs font-extrabold text-ink mb-1.5">
                Link Google Meet (Tùy chọn)
              </label>
              <Input
                type="url"
                value={createForm.googleMeetLink || ''}
                onChange={e => setCreateForm({ ...createForm, googleMeetLink: e.target.value })}
                placeholder="https://meet.google.com/abc-defg-hij"
              />
            </div>
          </div>

          <div className="grid gap-4 sm:grid-cols-2">
            <Input
              type="datetime-local"
              label="Thời gian bắt đầu"
              value={createForm.startTime}
              onChange={e => setCreateForm({ ...createForm, startTime: e.target.value })}
              required
            />

            <Input
              type="datetime-local"
              label="Thời gian kết thúc"
              value={createForm.endTime}
              onChange={e => setCreateForm({ ...createForm, endTime: e.target.value })}
              required
            />
          </div>

          <Textarea
            label="Mô tả / Nội dung dự kiến"
            value={createForm.description || ''}
            onChange={e => setCreateForm({ ...createForm, description: e.target.value })}
            rows={3}
            placeholder="Nhập nội dung chính, mục tiêu hoặc ghi chú cho các thành viên..."
          />

          <div className="flex justify-end gap-3 pt-3 border-t border-line">
            <Button
              type="button"
              variant="secondary"
              size="sm"
              onClick={() => setCreateModalOpen(false)}
              disabled={createLoading}
            >
              Hủy
            </Button>
            <Button
              type="submit"
              variant="primary"
              size="sm"
              disabled={createLoading}
              className="bg-gradient-to-r from-brand to-amber-500 text-white font-bold"
            >
              {createLoading ? 'Đang lưu...' : 'Xác nhận tạo cuộc họp'}
            </Button>
          </div>
        </form>
      </Modal>

      {/* UPDATE GOOGLE MEET LINK MODAL */}
      <Modal
        open={linkModalOpen}
        onClose={() => setLinkModalOpen(false)}
        title="Gắn / Cập nhật link Google Meet"
        maxWidthClass="max-w-md"
      >
        <form onSubmit={handleUpdateLinkSubmit} className="space-y-4 p-1">
          {selectedMeetingForLink && (
            <div className="bg-slate-50 p-3 rounded-xl border border-slate-200 text-xs">
              <span className="font-extrabold text-slate-900 block">{selectedMeetingForLink.title}</span>
              <span className="text-slate-500 font-medium">
                {formatDateTimeDisplay(selectedMeetingForLink.startTime)}
              </span>
            </div>
          )}

          {linkError && (
            <div className="p-3 rounded-xl bg-rose-50 border border-rose-200 text-xs text-rose-800 font-medium flex items-start gap-2">
              <ShieldAlert size={16} className="text-rose-600 shrink-0 mt-0.5" />
              <span>{linkError}</span>
            </div>
          )}

          <div>
            <label className="block text-xs font-extrabold text-ink mb-1.5">
              Đường dẫn Google Meet <span className="text-rose-500">*</span>
            </label>
            <Input
              type="url"
              value={meetLinkInput}
              onChange={e => setMeetLinkInput(e.target.value)}
              placeholder="https://meet.google.com/abc-defg-hij"
              required
            />
            <p className="text-[11px] text-muted mt-1">
              Ví dụ: <code className="bg-slate-100 px-1 py-0.5 rounded">https://meet.google.com/abc-defg-hij</code>
            </p>
          </div>

          <div className="flex justify-end gap-3 pt-3 border-t border-line">
            <Button
              type="button"
              variant="secondary"
              size="sm"
              onClick={() => setLinkModalOpen(false)}
              disabled={linkLoading}
            >
              Hủy
            </Button>
            <Button
              type="submit"
              variant="primary"
              size="sm"
              disabled={linkLoading}
              className="bg-emerald-600 hover:bg-emerald-700 text-white font-bold"
            >
              {linkLoading ? 'Đang lưu...' : 'Lưu link Google Meet'}
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  )
}
