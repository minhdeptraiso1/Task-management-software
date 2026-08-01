import { useEffect, useState, useCallback } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { FileSpreadsheet, X, CheckCircle2, AlertTriangle, Clock, User, ChevronLeft, ChevronRight, RefreshCcw, Layers } from 'lucide-react'
import { Button } from '../../../components/ui'
import type { TaskImportBatchResponse, TaskImportErrorPageResponse } from '../models/import-history.model'
import { getImportErrors } from '../services/import-history.service'

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

export function TaskImportBatchDetailModal({
  projectId,
  batch,
  onClose
}: {
  projectId: string
  batch: TaskImportBatchResponse
  onClose: () => void
}) {
  const [errorPageData, setErrorPageData] = useState<TaskImportErrorPageResponse | null>(null)
  const [page, setPage] = useState(0)
  const [loading, setLoading] = useState(false)
  const [errorMessage, setErrorMessage] = useState('')

  const fetchErrors = useCallback(async () => {
    if (!batch.failedRows || batch.failedRows === 0) return
    setLoading(true)
    setErrorMessage('')
    try {
      const res = await getImportErrors(projectId, batch.id, page, 20)
      setErrorPageData(res)
    } catch (err) {
      setErrorMessage(err instanceof Error ? err.message : 'Không thể tải danh sách lỗi import')
    } finally {
      setLoading(false)
    }
  }, [projectId, batch.id, batch.failedRows, page])

  useEffect(() => {
    void Promise.resolve().then(fetchErrors)
  }, [fetchErrors])

  const total = batch.totalRows || 0
  const success = batch.successRows || 0
  const failed = batch.failedRows || 0
  const successPercent = total > 0 ? Math.round((success / total) * 100) : 0

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'COMPLETED':
        return { label: 'Hoàn thành', className: 'bg-emerald-50 text-emerald-700 border-emerald-200' }
      case 'VALIDATION_FAILED':
        return { label: 'Lỗi Validation Excel', className: 'bg-amber-50 text-amber-700 border-amber-200' }
      case 'FAILED':
        return { label: 'Thất bại', className: 'bg-rose-50 text-rose-700 border-rose-200' }
      default:
        return { label: 'Đang xử lý', className: 'bg-blue-50 text-blue-700 border-blue-200' }
    }
  }

  const statusBadge = getStatusBadge(batch.status)

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
          {/* Header */}
          <div className="flex items-center justify-between border-b border-slate-100 bg-slate-50/80 px-6 py-4">
            <div className="flex items-center gap-3">
              <div className="flex size-10 items-center justify-center rounded-xl bg-emerald-50 text-emerald-600">
                <FileSpreadsheet size={20} />
              </div>
              <div>
                <div className="flex items-center gap-2">
                  <h3 className="text-sm font-bold text-slate-900">{batch.fileName}</h3>
                  <span className={`inline-block rounded-full px-2.5 py-0.5 text-[10px] font-extrabold border ${statusBadge.className}`}>
                    {statusBadge.label}
                  </span>
                </div>
                <p className="text-xs text-slate-500 font-mono">Batch ID: {batch.id}</p>
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

          {/* Body */}
          <div className="max-h-[70vh] overflow-y-auto p-6 space-y-6 text-xs">
            {/* Batch Info Grid */}
            <div className="grid grid-cols-2 gap-4 rounded-xl border border-slate-100 bg-slate-50/50 p-4 sm:grid-cols-4">
              <div>
                <span className="text-[10px] font-bold text-slate-400 uppercase">Sprint áp dụng</span>
                <p className="mt-0.5 font-bold text-slate-800 flex items-center gap-1">
                  <Layers size={13} className="text-brand" />
                  {batch.sprintName || 'Toàn bộ Project'}
                </p>
              </div>
              <div>
                <span className="text-[10px] font-bold text-slate-400 uppercase">Người thực thi</span>
                <p className="mt-0.5 font-bold text-slate-800 flex items-center gap-1">
                  <User size={13} className="text-slate-500" />
                  {batch.importedByUsername}
                </p>
              </div>
              <div>
                <span className="text-[10px] font-bold text-slate-400 uppercase">Thời gian thực hiện</span>
                <p className="mt-0.5 font-medium text-slate-600 flex items-center gap-1">
                  <Clock size={13} className="text-slate-400" />
                  {new Date(batch.createdAt).toLocaleString('vi-VN')}
                </p>
              </div>
              <div>
                <span className="text-[10px] font-bold text-slate-400 uppercase">Tỷ lệ thành công</span>
                <p className="mt-0.5 font-black text-emerald-600 text-sm">{successPercent}%</p>
              </div>
            </div>

            {/* Progress Bar & Row Counts */}
            <div className="space-y-2">
              <div className="flex items-center justify-between font-bold text-slate-700">
                <span>Kết quả xử lý dòng (Rows)</span>
                <span>{success} thành công / {total} tổng dòng</span>
              </div>
              <div className="h-3 w-full overflow-hidden rounded-full bg-slate-100 flex">
                <div
                  className="h-full bg-emerald-500 transition-all duration-500"
                  style={{ width: `${total > 0 ? (success / total) * 100 : 0}%` }}
                  title={`${success} thành công`}
                />
                <div
                  className="h-full bg-rose-500 transition-all duration-500"
                  style={{ width: `${total > 0 ? (failed / total) * 100 : 0}%` }}
                  title={`${failed} thất bại`}
                />
              </div>
              <div className="flex justify-between text-[11px] font-medium text-slate-500">
                <span className="flex items-center gap-1 text-emerald-700 font-bold">
                  <CheckCircle2 size={12} /> {success} dòng thêm mới thành công
                </span>
                {failed > 0 && (
                  <span className="flex items-center gap-1 text-rose-700 font-bold">
                    <AlertTriangle size={12} /> {failed} dòng bị lỗi Validation/Import
                  </span>
                )}
              </div>
            </div>

            {/* Errors List Section */}
            {failed > 0 && (
              <div className="space-y-3 pt-3 border-t border-slate-100">
                <div className="flex items-center justify-between">
                  <h4 className="font-bold text-slate-900 text-xs flex items-center gap-1.5 text-rose-700">
                    <AlertTriangle size={15} />
                    Danh sách lỗi dòng Excel ({failed} lỗi)
                  </h4>
                  <Button variant="ghost" size="sm" leadingIcon={<RefreshCcw size={13} className={loading ? 'animate-spin' : ''} />} onClick={fetchErrors}>
                    Làm mới
                  </Button>
                </div>

                {errorMessage && (
                  <div className="rounded-xl bg-rose-50 p-3 text-rose-700 font-bold text-xs">{errorMessage}</div>
                )}

                {loading && !errorPageData && (
                  <div className="p-8 text-center">
                    <span className="size-6 animate-spin rounded-full border-2 border-brand border-r-transparent inline-block" />
                  </div>
                )}

                {errorPageData && (
                  <div className="overflow-x-auto rounded-xl border border-slate-200">
                    <table className="w-full text-left text-xs">
                      <thead className="bg-slate-50 text-[10px] font-bold uppercase text-slate-500 border-b border-slate-200">
                        <tr>
                          <th className="px-4 py-2.5">DÒNG (ROW)</th>
                          <th className="px-4 py-2.5">CỘT (COLUMN)</th>
                          <th className="px-4 py-2.5">GIÁ TRỊ THÔ</th>
                          <th className="px-4 py-2.5">NỘI DUNG LỖI</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-slate-100">
                        {errorPageData.content.map(err => (
                          <tr key={err.id} className="hover:bg-slate-50/80">
                            <td className="px-4 py-2.5 font-black text-rose-600">Dòng {err.rowNumber}</td>
                            <td className="px-4 py-2.5 font-bold text-slate-700">{err.columnName || 'Toàn dòng'}</td>
                            <td className="px-4 py-2.5 font-mono text-[11px] text-slate-500 bg-slate-50/50 rounded">{err.rawValue || '(Trống)'}</td>
                            <td className="px-4 py-2.5 font-medium text-rose-700">{err.errorMessage}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}

                {/* Errors Pagination */}
                {errorPageData && errorPageData.totalPages > 1 && (
                  <div className="flex items-center justify-between text-xs text-slate-500 pt-2">
                    <span>Trang <strong>{page + 1}</strong> / {errorPageData.totalPages}</span>
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
                        disabled={page + 1 >= errorPageData.totalPages || loading}
                        trailingIcon={<ChevronRight size={14} />}
                        onClick={() => setPage(p => p + 1)}
                      >
                        Sau
                      </Button>
                    </div>
                  </div>
                )}
              </div>
            )}
          </div>
        </motion.div>
      </div>
    </AnimatePresence>
  )
}
