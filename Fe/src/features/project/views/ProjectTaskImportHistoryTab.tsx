import { useState, useEffect, useCallback } from 'react'
import { motion } from 'framer-motion'
import {
  FileSpreadsheet,
  Filter,
  RefreshCw,
  Eye,
  Clock,
  ChevronLeft,
  ChevronRight,
  Layers,
  FileText
} from 'lucide-react'
import { Button, Select } from '../../../components/ui'
import type { TaskImportBatchResponse, TaskImportBatchPageResponse, TaskImportStatus } from '../models/import-history.model'
import { getImportHistory } from '../services/import-history.service'
import { TaskImportBatchDetailModal } from './TaskImportBatchDetailModal'

const EXPO_OUT_EASE = [0.16, 1, 0.3, 1] as const

const containerVariants = {
  initial: {},
  animate: {
    transition: {
      staggerChildren: 0.05,
      delayChildren: 0.1
    }
  }
}

const itemVariants = {
  initial: { y: 15, opacity: 0 },
  animate: {
    y: 0,
    opacity: 1,
    transition: { duration: 0.4, ease: EXPO_OUT_EASE }
  }
}

export function ProjectTaskImportHistoryTab({ projectId }: { projectId: string }) {
  const [data, setData] = useState<TaskImportBatchPageResponse | null>(null)
  const [page, setPage] = useState(0)
  const [statusFilter, setStatusFilter] = useState<TaskImportStatus | ''>('')
  const [fromDate, setFromDate] = useState('')
  const [toDate, setToDate] = useState('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const [selectedBatch, setSelectedBatch] = useState<TaskImportBatchResponse | null>(null)

  const fetchHistory = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      const res = await getImportHistory(projectId, {
        page,
        size: 12,
        status: statusFilter || undefined,
        fromDate: fromDate || undefined,
        toDate: toDate || undefined
      })
      setData(res)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Không thể tải lịch sử import task')
    } finally {
      setLoading(false)
    }
  }, [projectId, page, statusFilter, fromDate, toDate])

  useEffect(() => {
    void Promise.resolve().then(fetchHistory)
  }, [fetchHistory])

  const statusOptions = [
    { label: 'Tất cả trạng thái', value: '' },
    { label: 'Hoàn thành', value: 'COMPLETED' },
    { label: 'Lỗi Validation Excel', value: 'VALIDATION_FAILED' },
    { label: 'Thất bại', value: 'FAILED' },
    { label: 'Đang xử lý', value: 'EXECUTED' },
  ]

  const getStatusBadge = (status: TaskImportStatus) => {
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

  const items = data?.content || []

  return (
    <motion.div
      variants={containerVariants}
      initial="initial"
      animate="animate"
      className="p-5 md:p-6 space-y-6"
    >
      {/* Header Banner */}
      <motion.div variants={itemVariants} className="flex flex-col gap-4 rounded-2xl border border-slate-200 bg-white p-5 shadow-xs sm:flex-row sm:items-center sm:justify-between">
        <div className="flex items-center gap-3">
          <div className="flex size-12 items-center justify-center rounded-2xl bg-emerald-50 text-emerald-600 border border-emerald-100">
            <FileSpreadsheet size={24} />
          </div>
          <div>
            <h2 className="text-base font-extrabold text-slate-900">Lịch sử Import Task Excel</h2>
            <p className="text-xs text-slate-500 mt-0.5">
              Theo dõi chi tiết các lượt import công việc từ Excel, kết quả dòng thành công / thất bại và danh sách lỗi.
            </p>
          </div>
        </div>
        <Button
          variant="secondary"
          size="sm"
          leadingIcon={<RefreshCw size={15} className={loading ? 'animate-spin' : ''} />}
          onClick={fetchHistory}
        >
          Làm mới
        </Button>
      </motion.div>

      {/* Filter Row */}
      <motion.div variants={itemVariants} className="flex flex-wrap items-center gap-3 rounded-2xl border border-slate-200 bg-slate-50/60 p-4">
        <div className="flex items-center gap-1.5 text-xs font-bold text-slate-700">
          <Filter size={15} className="text-slate-400" />
          Lọc kết quả:
        </div>

        <Select
          className="w-48"
          value={statusFilter}
          onChange={e => {
            setStatusFilter(e.target.value as TaskImportStatus | '')
            setPage(0)
          }}
          options={statusOptions}
        />

        <div className="flex items-center gap-2 text-xs">
          <span className="text-slate-500 font-medium">Từ ngày:</span>
          <input
            type="date"
            className="rounded-lg border border-slate-200 bg-white px-2.5 py-1.5 text-xs text-slate-700 outline-none focus:ring-2 focus:ring-brand/30"
            value={fromDate}
            onChange={e => {
              setFromDate(e.target.value)
              setPage(0)
            }}
          />
        </div>

        <div className="flex items-center gap-2 text-xs">
          <span className="text-slate-500 font-medium">Đến ngày:</span>
          <input
            type="date"
            className="rounded-lg border border-slate-200 bg-white px-2.5 py-1.5 text-xs text-slate-700 outline-none focus:ring-2 focus:ring-brand/30"
            value={toDate}
            onChange={e => {
              setToDate(e.target.value)
              setPage(0)
            }}
          />
        </div>
      </motion.div>

      {/* Error Message */}
      {error && (
        <div className="rounded-xl border border-rose-200 bg-rose-50 p-4 text-xs font-bold text-rose-700">
          {error}
        </div>
      )}

      {/* Batches Table */}
      <motion.div variants={itemVariants} className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-xs">
        <div className="overflow-x-auto">
          <table className="w-full min-w-[880px] text-left text-xs">
            <thead className="bg-slate-50 text-[11px] font-extrabold uppercase tracking-wider text-slate-500 border-b border-slate-200">
              <tr>
                <th className="px-5 py-4">TÊN FILE EXCEL</th>
                <th className="px-5 py-4">TRẠNG THÁI</th>
                <th className="px-5 py-4">SPRINT ÁP DỤNG</th>
                <th className="px-5 py-4 text-center">DÒNG THÀNH CÔNG / TỔNG</th>
                <th className="px-5 py-4">NGƯỜI THỰC HIỆN</th>
                <th className="px-5 py-4">THỜI GIAN</th>
                <th className="px-5 py-4 text-center">CHI TIẾT</th>
              </tr>
            </thead>
            <tbody className={loading ? 'opacity-40 transition-opacity' : ''}>
              {items.map(batch => {
                const statusBadge = getStatusBadge(batch.status)
                const total = batch.totalRows || 0
                const success = batch.successRows || 0
                const failed = batch.failedRows || 0

                return (
                  <tr key={batch.id} className="border-t border-slate-100 hover:bg-slate-50/70 transition-colors">
                    <td className="px-5 py-4">
                      <div className="flex items-center gap-3">
                        <span className="flex size-9 items-center justify-center rounded-xl bg-emerald-50 text-emerald-600 font-bold border border-emerald-100 shrink-0">
                          <FileText size={18} />
                        </span>
                        <div>
                          <p className="font-bold text-slate-900 leading-snug">{batch.fileName}</p>
                          <p className="mt-0.5 text-[10px] font-mono text-slate-400">Batch: {batch.id.slice(0, 8)}</p>
                        </div>
                      </div>
                    </td>
                    <td className="px-5 py-4">
                      <span className={`inline-block rounded-full px-3 py-1 text-xs font-extrabold border ${statusBadge.className}`}>
                        {statusBadge.label}
                      </span>
                    </td>
                    <td className="px-5 py-4">
                      <span className="inline-flex items-center gap-1 font-bold text-slate-700">
                        <Layers size={13} className="text-brand" />
                        {batch.sprintName || 'Toàn dự án'}
                      </span>
                    </td>
                    <td className="px-5 py-4 text-center">
                      <div className="space-y-1">
                        <span className="font-black text-slate-900 text-sm">{success} / {total}</span>
                        {failed > 0 && (
                          <span className="block text-[10px] font-bold text-rose-600">
                            ({failed} dòng lỗi)
                          </span>
                        )}
                      </div>
                    </td>
                    <td className="px-5 py-4">
                      <div className="flex items-center gap-2">
                        <span className="flex size-7 items-center justify-center rounded-full bg-slate-100 font-extrabold text-[10px] text-slate-600">
                          {batch.importedByUsername.slice(0, 2).toUpperCase()}
                        </span>
                        <div>
                          <p className="font-bold text-slate-800">{batch.importedByUsername}</p>
                          {batch.importedByEmail && <p className="text-[10px] text-slate-400">{batch.importedByEmail}</p>}
                        </div>
                      </div>
                    </td>
                    <td className="px-5 py-4 font-medium text-slate-500">
                      <span className="inline-flex items-center gap-1">
                        <Clock size={13} className="text-slate-400" />
                        {new Date(batch.createdAt).toLocaleString('vi-VN')}
                      </span>
                    </td>
                    <td className="px-5 py-4 text-center">
                      <Button
                        variant="ghost"
                        size="sm"
                        iconOnly
                        className="!size-8 !rounded-lg border border-slate-200 bg-white text-slate-600 hover:bg-slate-100 hover:text-slate-900 shadow-2xs"
                        leadingIcon={<Eye size={15} />}
                        title="Xem chi tiết Batch Import"
                        onClick={() => setSelectedBatch(batch)}
                      />
                    </td>
                  </tr>
                )
              })}

              {!items.length && !loading && (
                <tr>
                  <td colSpan={7} className="px-5 py-16 text-center text-slate-400 font-medium">
                    Chưa có lượt import task nào được ghi nhận cho dự án này.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>

        {/* Footer / Pagination */}
        <footer className="flex items-center justify-between border-t border-slate-100 px-5 py-3.5 text-xs text-slate-500 font-medium">
          <p>Trang <span className="font-bold text-slate-800">{page + 1}</span> / {Math.max(data?.totalPages || 1, 1)}</p>
          <div className="flex gap-2">
            <Button
              variant="secondary"
              size="sm"
              disabled={page === 0 || loading}
              leadingIcon={<ChevronLeft size={15} />}
              onClick={() => setPage(p => p - 1)}
            >
              Trước
            </Button>
            <Button
              variant="secondary"
              size="sm"
              disabled={page + 1 >= (data?.totalPages || 1) || loading}
              trailingIcon={<ChevronRight size={15} />}
              onClick={() => setPage(p => p + 1)}
            >
              Sau
            </Button>
          </div>
        </footer>
      </motion.div>

      {/* Batch Detail Modal */}
      {selectedBatch && (
        <TaskImportBatchDetailModal
          projectId={projectId}
          batch={selectedBatch}
          onClose={() => setSelectedBatch(null)}
        />
      )}
    </motion.div>
  )
}
