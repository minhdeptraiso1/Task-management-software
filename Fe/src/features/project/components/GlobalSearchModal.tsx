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
        return <FolderKanban size={18} className="text-indigo-500" />
      case 'SPRINT':
        return <CalendarDays size={18} className="text-emerald-500" />
      case 'BACKLOG_ITEM':
        return <Layers size={18} className="text-amber-500" />
      case 'TASK':
        return <CheckSquare size={18} className="text-sky-500" />
      case 'BUG':
        return <Bug size={18} className="text-rose-500" />
      case 'COMMENT':
        return <MessageSquare size={18} className="text-purple-500" />
      case 'ATTACHMENT':
        return <Paperclip size={18} className="text-teal-500" />
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
            <mark key={index} className="bg-amber-100 text-amber-950 font-semibold rounded px-0.5">
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
    <div className="fixed inset-0 z-[100] flex items-start justify-center pt-[10vh] px-4">
      {/* Backdrop */}
      <div 
        className="fixed inset-0 bg-brand-black/40 backdrop-blur-sm transition-opacity" 
        onClick={onClose} 
      />

      {/* Spotlight Box */}
      <div className="relative w-full max-w-2xl transform overflow-hidden rounded-2xl border border-line bg-white shadow-2xl transition-all flex flex-col max-h-[75vh] animate-enter">
        {/* Header Search Field */}
        <div className="flex items-center border-b border-line px-4 py-3.5">
          <Search size={20} className="text-muted mr-3 shrink-0" />
          <input
            ref={inputRef}
            type="text"
            className="flex-1 text-sm bg-transparent outline-none text-ink placeholder-muted/80"
            placeholder={
              currentProjectId 
                ? `Tìm kiếm dự án, task, bug... (${searchProjectOnly ? `trong ${currentProjectCode}` : 'toàn hệ thống'})` 
                : 'Tìm kiếm dự án, task, bug, comment...'
            }
            value={query}
            onChange={e => setQuery(e.target.value)}
          />
          {loading ? (
            <Loader2 className="animate-spin text-brand mr-2" size={16} />
          ) : query ? (
            <button 
              type="button" 
              onClick={() => { setQuery(''); setResults([]) }} 
              className="text-muted hover:text-ink transition p-1"
            >
              <X size={16} />
            </button>
          ) : null}
        </div>

        {/* Project Scope Toggle */}
        {currentProjectId && (
          <div className="flex items-center justify-between bg-canvas px-4 py-2 border-b border-line text-xs">
            <span className="text-muted font-medium">Tìm kiếm trong dự án:</span>
            <label className="flex items-center gap-2 cursor-pointer select-none">
              <input
                type="checkbox"
                className="rounded border-line text-brand focus:ring-brand size-3.5"
                checked={searchProjectOnly}
                onChange={e => setSearchProjectOnly(e.target.checked)}
              />
              <span className="font-semibold text-ink">Chỉ dự án hiện tại ({currentProjectCode})</span>
            </label>
          </div>
        )}

        {/* Entity Type Filter Tabs */}
        <div className="flex gap-1 overflow-x-auto p-3 border-b border-line scrollbar-none bg-canvas">
          {ENTITY_TYPES.map(tab => (
            <button
              key={tab.value}
              type="button"
              onClick={() => setSelectedType(tab.value)}
              className={`rounded-lg px-3 py-1.5 text-xs font-bold transition whitespace-nowrap ${selectedType === tab.value ? 'bg-brand text-white shadow-sm' : 'bg-white border border-line text-muted hover:bg-slate-50'}`}
            >
              {tab.label}
            </button>
          ))}
        </div>

        {/* Results list */}
        <div 
          ref={resultsContainerRef}
          className="flex-1 overflow-y-auto min-h-0 divide-y divide-line"
        >
          {error && (
            <div className="p-4 text-center text-xs text-rose-600 bg-rose-50 font-medium">
              {error}
            </div>
          )}

          {!query.trim() ? (
            <div className="py-12 text-center text-muted">
              <Search className="mx-auto text-muted/50 mb-3" size={36} />
              <p className="text-xs font-medium">Nhập từ khóa để bắt đầu tìm kiếm</p>
              <p className="text-[11px] text-muted/70 mt-1">Hỗ trợ tìm nhanh Dự án, Sprint, Backlog, Task, Bug, Bình luận.</p>
            </div>
          ) : loading && results.length === 0 ? (
            <div className="py-12 text-center text-muted animate-pulse text-xs">
              Đang tải danh sách kết quả...
            </div>
          ) : results.length === 0 ? (
            <div className="py-12 text-center text-muted">
              <Search className="mx-auto text-muted/30 mb-3" size={32} />
              <p className="text-xs font-bold text-ink">Không tìm thấy kết quả</p>
              <p className="text-[11px] text-muted mt-1">Không tìm thấy thực thể phù hợp với "{query}".</p>
            </div>
          ) : (
            <div className="p-2 space-y-0.5">
              {results.map((item, index) => (
                <button
                  key={`${item.entityType}-${item.entityId}`}
                  type="button"
                  data-active={index === focusedIndex}
                  onClick={() => {
                    onSelectResult(item)
                    onClose()
                  }}
                  className={`w-full text-left rounded-xl p-3 flex items-start gap-3 transition-all ${index === focusedIndex ? 'bg-brand-soft border border-brand/20 shadow-sm' : 'border border-transparent hover:bg-slate-50'}`}
                >
                  <div className={`p-2 rounded-lg bg-white border border-line flex-shrink-0 ${index === focusedIndex ? 'shadow-sm' : ''}`}>
                    {getEntityIcon(item.entityType)}
                  </div>

                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2 flex-wrap">
                      <span className="text-[10px] font-extrabold uppercase tracking-wider text-muted-dark">
                        {getEntityTypeLabel(item.entityType)}
                      </span>
                      {item.projectCode && (
                        <span className="text-[10px] font-semibold bg-white border border-line text-brand-dark px-1.5 py-0.5 rounded">
                          {item.projectCode}
                        </span>
                      )}
                      <span className="text-[10px] text-muted ml-auto">
                        Cập nhật: {new Date(item.updatedAt).toLocaleDateString('vi-VN')}
                      </span>
                    </div>

                    <h4 className="font-bold text-ink text-xs mt-1 truncate">
                      {highlightKeyword(item.title, query)}
                    </h4>

                    {item.matchedText && (
                      <p className="text-[11px] text-muted mt-1 leading-relaxed line-clamp-2 italic bg-white border border-line/60 rounded px-2 py-1">
                        ...{highlightKeyword(item.matchedText, query)}...
                      </p>
                    )}
                  </div>

                  {index === focusedIndex && (
                    <div className="self-center flex-shrink-0 text-brand">
                      <CornerDownLeft size={16} />
                    </div>
                  )}
                </button>
              ))}
            </div>
          )}
        </div>

        {/* Footer shortcuts */}
        <div className="bg-canvas border-t border-line px-4 py-2 flex items-center justify-between text-[11px] text-muted">
          <div className="flex gap-4">
            <span><kbd className="bg-white border border-line rounded px-1.5 py-0.5 font-bold shadow-sm">↑↓</kbd> để di chuyển</span>
            <span><kbd className="bg-white border border-line rounded px-1.5 py-0.5 font-bold shadow-sm">Enter</kbd> để chọn</span>
            <span><kbd className="bg-white border border-line rounded px-1.5 py-0.5 font-bold shadow-sm">Esc</kbd> để đóng</span>
          </div>
          {currentProjectCode && (
            <span className="font-medium text-brand">Dự án: {currentProjectCode}</span>
          )}
        </div>
      </div>
    </div>
  )
}
