import { useEffect, useRef, useState } from 'react'
import { Bell, CheckCheck, Inbox, Trash2 } from 'lucide-react'
import { Badge, Button, EmptyState } from '../../../components/ui'
import { formatDate } from '../../../utils/format'
import type { NotificationPage } from '../models/notification.model'

interface Props {
  notifications: NotificationPage
  unreadCount: number
  onRead: (id: string) => void
  onDelete: (id: string) => void
  onReadAll: () => void
}

function normalizeDateText(value: string) {
  return value.replace(/\b(\d{4})-(\d{2})-(\d{2})\b/g, '$3/$2/$1')
}

export function NotificationDropdown({ notifications, unreadCount, onRead, onDelete, onReadAll }: Props) {
  const [open, setOpen] = useState(false)
  const containerRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    if (!open) return

    const closeOutside = (event: MouseEvent) => {
      if (!containerRef.current?.contains(event.target as Node)) setOpen(false)
    }
    const closeWithEscape = (event: KeyboardEvent) => {
      if (event.key === 'Escape') setOpen(false)
    }

    document.addEventListener('mousedown', closeOutside)
    document.addEventListener('keydown', closeWithEscape)
    return () => {
      document.removeEventListener('mousedown', closeOutside)
      document.removeEventListener('keydown', closeWithEscape)
    }
  }, [open])

  return <div ref={containerRef} className="relative">
    <div className="relative">
      <Button
        type="button"
        variant="ghost"
        tone="dark"
        size="sm"
        iconOnly
        leadingIcon={<Bell size={18} />}
        aria-label={`Thông báo${unreadCount ? `, ${unreadCount} chưa đọc` : ''}`}
        aria-expanded={open}
        title="Thông báo"
        onClick={() => setOpen(value => !value)}
      />
      {unreadCount > 0 && <span className="pointer-events-none absolute -right-1 -top-1 grid min-w-5 place-items-center rounded-full border-2 border-brand-black bg-danger px-1 text-[10px] font-bold leading-4 text-white tabular-nums">
        {unreadCount > 99 ? '99+' : unreadCount}
      </span>}
    </div>

    {open && <section className="absolute right-0 top-12 z-50 w-[min(420px,calc(100vw-24px))] overflow-hidden rounded-2xl border border-line bg-white text-ink shadow-xl">
      <header className="flex items-center justify-between gap-3 border-b border-line bg-canvas px-4 py-3">
        <div className="min-w-0">
          <div className="flex items-center gap-2">
            <h2 className="text-sm font-bold">Thông báo</h2>
            {unreadCount > 0 && <Badge variant="brand" size="sm">{unreadCount} chưa đọc</Badge>}
          </div>
          <p className="mt-1 text-xs text-muted">Thông báo chung từ các dự án của bạn</p>
        </div>
        {notifications.content.length > 0 && <Button
          type="button"
          variant="ghost"
          tone="success"
          size="sm"
          leadingIcon={<CheckCheck size={15} />}
          onClick={onReadAll}
        >
          Đọc tất cả
        </Button>}
      </header>

      {notifications.content.length === 0 ? <EmptyState
        bordered={false}
        className="!py-10"
        icon={<Inbox size={22} />}
        title="Chưa có thông báo"
        description="Thông báo mới từ dự án và công việc sẽ xuất hiện tại đây."
      /> : <div className="max-h-[440px] divide-y divide-line overflow-y-auto">
        {notifications.content.map(item => <article key={item.id} className={`flex items-start gap-2 p-2 ${item.read ? 'bg-white' : 'bg-brand/5'}`}>
          <Button
            type="button"
            variant="ghost"
            tone="neutral"
            className="!h-auto min-w-0 flex-1 !justify-start !whitespace-normal !rounded-xl !px-3 !py-2 text-left"
            onClick={() => !item.read && onRead(item.id)}
          >
            <span className="min-w-0 flex-1">
              <span className="flex items-start gap-2">
                {!item.read && <span className="mt-1.5 size-2 shrink-0 rounded-full bg-brand" />}
                <span className="block min-w-0">
                  <span className="block text-sm font-bold leading-5 text-ink">{normalizeDateText(item.title || '')}</span>
                  <span className="mt-1 block text-xs font-medium leading-5 text-muted">{normalizeDateText(item.content || '')}</span>
                  <span className="mt-1 block text-[11px] text-muted">{formatDate(item.createdAt)}</span>
                </span>
              </span>
            </span>
          </Button>
          <Button
            type="button"
            variant="ghost"
            tone="danger"
            size="sm"
            iconOnly
            leadingIcon={<Trash2 size={15} />}
            aria-label={`Xóa thông báo ${item.title}`}
            title="Xóa thông báo"
            onClick={() => onDelete(item.id)}
          />
        </article>)}
      </div>}

      {notifications.totalElements > notifications.content.length && <footer className="border-t border-line bg-canvas px-4 py-2 text-center text-xs font-medium text-muted">
        Hiển thị {notifications.content.length}/{notifications.totalElements} thông báo gần nhất
      </footer>}
    </section>}
  </div>
}
