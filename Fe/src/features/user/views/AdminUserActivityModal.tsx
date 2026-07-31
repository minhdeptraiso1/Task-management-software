import { useEffect, useState, useCallback } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { Activity, X, ChevronLeft, ChevronRight, Clock, AlertTriangle, RefreshCcw } from 'lucide-react'
import { Button } from '../../../components/ui'
import type { User } from '../models/user.model'
import type { AdminUserActivityPageResponse, AdminUserActivityResponse } from '../models/admin.model'
import { getUserActivities } from '../services/admin.service'

const EXPO_OUT_EASE = [0.16, 1, 0.3, 1] as const

const backdropVariants = {
  initial: { opacity: 0 },
  animate: { opacity: 1 },
  exit: { opacity: 0 }
}

const modalVariants = {
  initial: { scale: 0.95, opacity: 0, y: 15 },
  animate: {
    scale: 1,
    opacity: 1,
    y: 0,
    transition: { duration: 0.4, ease: EXPO_OUT_EASE }
  },
  exit: {
    scale: 0.95,
    opacity: 0,
    y: 15,
    transition: { duration: 0.2 }
  }
}

export function AdminUserActivityModal({
  user,
  onClose
}: {
  user: User
  onClose: () => void
}) {
  const [data, setData] = useState<AdminUserActivityPageResponse | null>(null)
  const [page, setPage] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const fetchActivities = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      const res = await getUserActivities(user.id, page, 15)
      setData(res)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Không thể tải nhật ký hoạt động của người dùng')
    } finally {
      setLoading(false)
    }
  }, [user.id, page])

  useEffect(() => {
    void Promise.resolve().then(fetchActivities)
  }, [fetchActivities])

  return (
    <AnimatePresence>
      <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
        <motion.div
          variants={backdropVariants}
          initial="initial"
          animate="animate"
          exit="exit"
          className="fixed inset-0 bg-slate-900/60 backdrop-blur-xs"
          onClick={onClose}
        />

        <motion.div
          variants={modalVariants}
          initial="initial"
          animate="animate"
          exit="exit"
          className="relative z-10 w-full max-w-3xl overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-xl"
        >
          {/* Modal Header */}
          <div className="flex items-center justify-between border-b border-slate-100 bg-slate-50/80 px-6 py-4">
            <div className="flex items-center gap-3">
              <div className="flex size-10 items-center justify-center rounded-xl bg-brand/10 text-brand">
                <Activity size={20} />
              </div>
              <div>
                <h3 className="text-sm font-bold text-slate-900">
                  Nhật ký Hoạt động của <span className="text-brand">{user.username}</span>
                </h3>
                <p className="text-xs text-slate-500">{user.email} &bull; {user.role}</p>
              </div>
            </div>
            <button
              type="button"
              className="flex size-8 items-center justify-center rounded-full text-slate-400 hover:bg-slate-200/60 hover:text-slate-700 transition"
              onClick={onClose}
            >
              <X size={18} />
            </button>
          </div>

          {/* Modal Content */}
          <div className="max-h-[65vh] overflow-y-auto p-6">
            {loading && !data && (
              <div className="flex h-48 w-full items-center justify-center">
                <span className="size-8 animate-spin rounded-full border-3 border-brand border-r-transparent" />
              </div>
            )}

            {error && (
              <div className="rounded-xl border border-rose-200 bg-rose-50 p-4 text-center text-xs font-bold text-rose-700">
                <AlertTriangle size={20} className="mx-auto mb-1" />
                {error}
                <div className="mt-3">
                  <Button variant="secondary" size="sm" leadingIcon={<RefreshCcw size={14} />} onClick={fetchActivities}>
                    Thử lại
                  </Button>
                </div>
              </div>
            )}

            {data && (
              <div className="space-y-3">
                {data.content.length === 0 ? (
                  <div className="p-12 text-center text-xs font-medium text-slate-400">
                    Người dùng này chưa thực hiện thao tác nào trong nhật ký hoạt động.
                  </div>
                ) : (
                  <div className="divide-y divide-slate-100">
                    {data.content.map((act: AdminUserActivityResponse) => (
                      <div key={act.id} className="flex items-start justify-between py-3 text-xs">
                        <div className="space-y-1">
                          <div className="flex items-center gap-2">
                            <span className="rounded-md bg-slate-100 px-2 py-0.5 font-extrabold uppercase text-slate-600 text-[10px]">
                              {act.entityType}
                            </span>
                            <span className="font-bold text-slate-900">{act.action}</span>
                          </div>
                          {(act.oldValueJson || act.newValueJson) && (
                            <div className="text-[11px] text-slate-500 font-mono bg-slate-50 p-1.5 rounded border border-slate-100">
                              {act.oldValueJson && <span className="text-rose-600 line-through mr-2">Cũ: {act.oldValueJson}</span>}
                              {act.newValueJson && <span className="text-emerald-600">Mới: {act.newValueJson}</span>}
                            </div>
                          )}
                        </div>
                        <span className="flex items-center gap-1 text-[11px] text-slate-400 shrink-0 ml-4">
                          <Clock size={12} />
                          {new Date(act.createdAt).toLocaleString('vi-VN')}
                        </span>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            )}
          </div>

          {/* Modal Footer / Pagination */}
          {data && data.totalPages > 1 && (
            <div className="flex items-center justify-between border-t border-slate-100 bg-slate-50/50 px-6 py-3 text-xs text-slate-500">
              <span>Trang <strong>{page + 1}</strong> / {data.totalPages}</span>
              <div className="flex gap-2">
                <Button
                  variant="secondary"
                  size="sm"
                  disabled={page === 0 || loading}
                  leadingIcon={<ChevronLeft size={14} />}
                  onClick={() => setPage(p => p - 1)}
                >
                  Trước
                </Button>
                <Button
                  variant="secondary"
                  size="sm"
                  disabled={page + 1 >= data.totalPages || loading}
                  trailingIcon={<ChevronRight size={14} />}
                  onClick={() => setPage(p => p + 1)}
                >
                  Sau
                </Button>
              </div>
            </div>
          )}
        </motion.div>
      </div>
    </AnimatePresence>
  )
}
