import { useEffect, useRef, useState, useCallback } from 'react'
import { Search, X, FolderKanban, CalendarDays, Layers, CheckSquare, Bug, MessageSquare, CornerDownLeft, Loader2, Paperclip } from 'lucide-react'
import { searchGlobal, searchInProject } from '../services/search.service'
import type { SearchResultItem, SearchEntityType } from '../models/search.model'

interface GlobalSearchModalProps {
  open: boolean
  onClose: () => void
  currentProjectId?: string
  currentProjectCode?: string
  onSelectResult: (result: SearchResultItem) => void
}

const ENTITY_TYPES: { label: string; value: SearchEntityType | 'ALL' }[] = [
  { label: 'Tất cả', value: 'ALL' },
  { label: 'Dự án', value: 'PROJECT' },
  { label: 'Sprint', value: 'SPRINT' },
  { label: 'Yêu cầu', value: 'BACKLOG_ITEM' },
  { label: 'Nhiệm vụ', value: 'TASK' },
  { label: 'Lỗi', value: 'BUG' },
  { label: 'Bình luận', value: 'COMMENT' },
  { label: 'Tài liệu', value: 'ATTACHMENT' }
]

export default function GlobalSearchModal({
  open,
  onClose,
  currentProjectId,
  currentProjectCode,
  onSelectResult
}: GlobalSearchModalProps) {
  const [query, setQuery] = useState('')
  const [selectedType, setSelectedType] = useState<SearchEntityType | 'ALL'>('ALL')
  const [searchProjectOnly, setSearchProjectOnly] = useState(!!currentProjectId)
  const [results, setResults] = useState<SearchResultItem[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [focusedIndex, setFocusedIndex] = useState(0)

  const inputRef = useRef<HTMLInputElement>(null)
  const resultsContainerRef = useRef<HTMLDivElement>(null)

  // Focus input on open
  useEffect(() => {
    if (open) {
      setTimeout(() => inputRef.current?.focus(), 100)
      setQuery('')
      setResults([])
      setSelectedType('ALL')
      setFocusedIndex(0)
      setError(null)
    }
  }, [open])

  // Perform search
  const performSearch = useCallback(async (searchQuery: string, type: SearchEntityType | 'ALL', projectOnly: boolean) => {
    if (!searchQuery.trim()) {
      setResults([])
      return
    }

    setLoading(true)
    setError(null)
    try {
      const entityTypeParam = type === 'ALL' ? undefined : type
      let response
      if (projectOnly && currentProjectId) {
        response = await searchInProject(currentProjectId, searchQuery, entityTypeParam)
      } else {
        response = await searchGlobal(searchQuery, entityTypeParam, currentProjectId || undefined)
      }
      setResults(response.results || [])
      setFocusedIndex(0)
    } catch (err: any) {
      console.error(err)
      setError(err?.message || 'Lỗi khi tìm kiếm dữ liệu')
    } finally {
      setLoading(false)
    }
  }, [currentProjectId])

  // Debounce search input
  useEffect(() => {
    const timer = setTimeout(() => {
      if (query.trim()) {
        performSearch(query, selectedType, searchProjectOnly)
      } else {
        setResults([])
      }
    }, 300)

    return () => clearTimeout(timer)
  }, [query, selectedType, searchProjectOnly, performSearch])

  // Scroll active item into view
  useEffect(() => {
    if (resultsContainerRef.current) {
      const activeEl = resultsContainerRef.current.querySelector('[data-active="true"]') as HTMLElement
      if (activeEl) {
        const container = resultsContainerRef.current
        const activeTop = activeEl.offsetTop
        const activeHeight = activeEl.offsetHeight
        const containerHeight = container.clientHeight
        const containerScrollTop = container.scrollTop

        if (activeTop < containerScrollTop) {
          container.scrollTop = activeTop
        } else if (activeTop + activeHeight > containerScrollTop + containerHeight) {
          container.scrollTop = activeTop + activeHeight - containerHeight
        }
      }
    }
  }, [focusedIndex])

  // Hotkey navigation
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (!open) return

      if (e.key === 'Escape') {
        e.preventDefault()
        onClose()
      } else if (e.key === 'ArrowDown') {
        e.preventDefault()
        setFocusedIndex(prev => (results.length > 0 ? (prev + 1) % results.length : 0))
      } else if (e.key === 'ArrowUp') {
        e.preventDefault()
        setFocusedIndex(prev => (results.length > 0 ? (prev - 1 + results.length) % results.length : 0))
      } else if (e.key === 'Enter') {
        e.preventDefault()
        if (results[focusedIndex]) {
          onSelectResult(results[focusedIndex])
          onClose()
        }
      }
    }

    window.addEventListener('keydown', handleKeyDown)
    return () => window.removeEventListener('keydown', handleKeyDown)
  }, [open, results, focusedIndex, onClose, onSelectResult])

  if (!open) return null

  const getEntityIcon = (type: SearchEntityType) => {
    switch (type) {
      case 'PROJECT':
        return <FolderKanban size={22} className="text-indigo-500" />
      case 'SPRINT':
        return <CalendarDays size={22} className="text-emerald-500" />
      case 'BACKLOG_ITEM':
        return <Layers size={22} className="text-amber-500" />
      case 'TASK':
        return <CheckSquare size={22} className="text-sky-500" />
      case 'BUG':
        return <Bug size={22} className="text-rose-500" />
      case 'COMMENT':
        return <MessageSquare size={22} className="text-purple-500" />
      case 'ATTACHMENT':
        return <Paperclip size={22} className="text-teal-500" />
    }
  }

  const getEntityTypeLabel = (type: SearchEntityType) => {
    switch (type) {
      case 'PROJECT': return 'Dự án'
      case 'SPRINT': return 'Sprint'
      case 'BACKLOG_ITEM': return 'Yêu cầu'
      case 'TASK': return 'Nhiệm vụ'
      case 'BUG': return 'Lỗi QA'
      case 'COMMENT': return 'Bình luận'
      case 'ATTACHMENT': return 'Tài liệu'
    }
  }

  const highlightKeyword = (text: string | null, keyword: string) => {
    if (!text) return ''
    if (!keyword.trim()) return <span>{text}</span>

    const regex = new RegExp(`(${keyword.replace(/[-\/\\^$*+?.()|[\]{}]/g, '\\$&')})`, 'gi')
    const parts = text.split(regex)

    return (
      <span>
        {parts.map((part, index) =>
          regex.test(part) ? (
            <mark key={index} className="bg-amber-100 text-amber-950 font-bold rounded px-1">
              {part}
            </mark>
          ) : (
            part
          )
        )}
      </span>
    )
  }

  return (
    <div className="fixed inset-0 z-[100] flex items-center justify-center p-4 sm:p-6 md:p-10">
      {/* Backdrop */}
      <div 
        className="fixed inset-0 bg-brand-black/50 backdrop-blur-md transition-opacity" 
        onClick={onClose} 
      />

      {/* Spotlight Box (Enlarged x3, Centered) */}
      <div className="relative w-full max-w-5xl transform overflow-hidden rounded-3xl border border-slate-200 bg-white shadow-2xl transition-all flex flex-col max-h-[85vh] h-[720px] animate-enter">
        {/* Header Search Field */}
        <div className="flex items-center border-b border-line px-6 py-5 bg-white">
          <Search size={24} className="text-slate-400 mr-4 shrink-0" />
          <input
            ref={inputRef}
            type="text"
            className="flex-1 text-lg sm:text-xl font-medium bg-transparent outline-none text-ink placeholder-slate-400"
            placeholder={
              currentProjectId 
                ? `Tìm kiếm dự án, task, bug... (${searchProjectOnly ? `trong ${currentProjectCode}` : 'toàn hệ thống'})` 
                : 'Tìm kiếm dự án, task, bug, comment...'
            }
            value={query}
            onChange={e => setQuery(e.target.value)}
          />
          {loading ? (
            <Loader2 className="animate-spin text-brand mr-2" size={20} />
          ) : query ? (
            <button 
              type="button" 
              onClick={() => { setQuery(''); setResults([]) }} 
              className="text-slate-400 hover:text-slate-700 transition p-1.5 rounded-full hover:bg-slate-100"
            >
              <X size={20} />
            </button>
          ) : null}
        </div>

        {/* Project Scope Toggle */}
        {currentProjectId && (
          <div className="flex items-center justify-between bg-canvas px-6 py-3 border-b border-line text-sm">
            <span className="text-slate-500 font-semibold">Phạm vi tìm kiếm:</span>
            <label className="flex items-center gap-2.5 cursor-pointer select-none">
              <input
                type="checkbox"
                className="rounded border-line text-brand focus:ring-brand size-4 cursor-pointer"
                checked={searchProjectOnly}
                onChange={e => setSearchProjectOnly(e.target.checked)}
              />
              <span className="font-bold text-slate-800">Chỉ trong dự án hiện tại ({currentProjectCode})</span>
            </label>
          </div>
        )}

        {/* Entity Type Filter Tabs */}
        <div className="flex gap-2 overflow-x-auto p-4 border-b border-line scrollbar-none bg-canvas">
          {ENTITY_TYPES.map(tab => (
            <button
              key={tab.value}
              type="button"
              onClick={() => setSelectedType(tab.value)}
              className={`rounded-xl px-4 py-2 text-xs sm:text-sm font-bold transition whitespace-nowrap ${selectedType === tab.value ? 'bg-brand text-white shadow-md' : 'bg-white border border-line text-slate-600 hover:bg-slate-50'}`}
            >
              {tab.label}
            </button>
          ))}
        </div>

        {/* Results list */}
        <div 
          ref={resultsContainerRef}
          className="flex-1 overflow-y-auto min-h-0 divide-y divide-line/60"
        >
          {error && (
            <div className="p-6 text-center text-sm text-rose-600 bg-rose-50 font-semibold">
              {error}
            </div>
          )}

          {!query.trim() ? (
            <div className="py-20 sm:py-24 text-center text-slate-400 flex flex-col items-center justify-center">
              <div className="size-16 rounded-3xl bg-slate-100 flex items-center justify-center text-slate-400 mb-4">
                <Search size={36} />
              </div>
              <p className="text-base font-bold text-slate-700">Nhập từ khóa để bắt đầu tìm kiếm</p>
              <p className="text-xs text-slate-400 mt-1.5 max-w-md">Hỗ trợ tìm kiếm nhanh chóng cho Dự án, Sprint, Backlog, Task, Bug, Bình luận và Tài liệu đính kèm.</p>
            </div>
          ) : loading && results.length === 0 ? (
            <div className="py-20 text-center text-slate-400 animate-pulse text-sm font-semibold">
              Đang phân tích và tìm kiếm dữ liệu...
            </div>
          ) : results.length === 0 ? (
            <div className="py-20 text-center text-slate-400 flex flex-col items-center justify-center">
              <div className="size-16 rounded-3xl bg-slate-100 flex items-center justify-center text-slate-300 mb-4">
                <Search size={32} />
              </div>
              <p className="text-base font-bold text-slate-700">Không tìm thấy kết quả phù hợp</p>
              <p className="text-xs text-slate-400 mt-1.5">Thử từ khóa khác hoặc thay đổi bộ lọc phân loại.</p>
            </div>
          ) : (
            <div className="p-3 sm:p-4 space-y-1.5">
              {results.map((item, index) => (
                <button
                  key={`${item.entityType}-${item.entityId}`}
                  type="button"
                  data-active={index === focusedIndex}
                  onClick={() => {
                    onSelectResult(item)
                    onClose()
                  }}
                  className={`w-full text-left rounded-2xl p-4 flex items-start gap-4 transition-all ${index === focusedIndex ? 'bg-brand-soft border border-brand/30 shadow-md scale-[1.005]' : 'border border-transparent hover:bg-slate-50'}`}
                >
                  <div className={`p-3 rounded-2xl bg-white border border-slate-200 flex-shrink-0 ${index === focusedIndex ? 'shadow-sm' : ''}`}>
                    {getEntityIcon(item.entityType)}
                  </div>

                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2.5 flex-wrap">
                      <span className="text-xs font-extrabold uppercase tracking-wider text-slate-500">
                        {getEntityTypeLabel(item.entityType)}
                      </span>
                      {item.projectCode && (
                        <span className="text-xs font-bold bg-white border border-slate-200 text-brand-dark px-2 py-0.5 rounded-lg shadow-2xs">
                          {item.projectCode}
                        </span>
                      )}
                      <span className="text-xs text-slate-400 ml-auto">
                        Cập nhật: {new Date(item.updatedAt).toLocaleDateString('vi-VN')}
                      </span>
                    </div>

                    <h4 className="font-bold text-slate-800 text-sm sm:text-base mt-1.5 truncate">
                      {highlightKeyword(item.title, query)}
                    </h4>

                    {item.matchedText && (
                      <p className="text-xs sm:text-sm text-slate-600 mt-1.5 leading-relaxed line-clamp-2 italic bg-white border border-slate-200/80 rounded-xl p-2.5">
                        ...{highlightKeyword(item.matchedText, query)}...
                      </p>
                    )}
                  </div>

                  {index === focusedIndex && (
                    <div className="self-center flex-shrink-0 text-brand p-1">
                      <CornerDownLeft size={20} />
                    </div>
                  )}
                </button>
              ))}
            </div>
          )}
        </div>

        {/* Footer shortcuts */}
        <div className="bg-canvas border-t border-line px-6 py-3.5 flex items-center justify-between text-xs text-slate-500">
          <div className="flex gap-5">
            <span><kbd className="bg-white border border-slate-200 rounded-lg px-2 py-1 font-bold shadow-xs text-slate-700">↑↓</kbd> di chuyển</span>
            <span><kbd className="bg-white border border-slate-200 rounded-lg px-2 py-1 font-bold shadow-xs text-slate-700">Enter</kbd> chọn</span>
            <span><kbd className="bg-white border border-slate-200 rounded-lg px-2 py-1 font-bold shadow-xs text-slate-700">Esc</kbd> đóng</span>
          </div>
          {currentProjectCode && (
            <span className="font-bold text-brand text-xs">Dự án hiện tại: {currentProjectCode}</span>
          )}
        </div>
      </div>
    </div>
  )
}
