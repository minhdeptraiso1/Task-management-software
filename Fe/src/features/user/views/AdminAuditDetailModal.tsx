import { motion, AnimatePresence } from 'framer-motion'
import { ShieldCheck, X, Clock, Globe, Laptop, CheckCircle2, XCircle, FileCode } from 'lucide-react'
import type { SystemAuditLogResponse } from '../models/admin.model'
import { systemAuditActionLabels } from '../models/admin.model'

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

function formatJson(jsonStr: string | null): string {
  if (!jsonStr) return 'N/A'
  try {
    const parsed = JSON.parse(jsonStr)
    return JSON.stringify(parsed, null, 2)
  } catch {
    return jsonStr
  }
}

export function AdminAuditDetailModal({
  log,
  onClose
}: {
  log: SystemAuditLogResponse
  onClose: () => void
}) {
  const isSuccess = log.success !== false
  const actionLabel = systemAuditActionLabels[log.action] ?? log.action

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
          className="relative z-10 w-full max-w-2xl overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-xl"
        >
          {/* Header */}
          <div className="flex items-center justify-between border-b border-slate-100 bg-slate-50/80 px-6 py-4">
            <div className="flex items-center gap-3">
              <div className="flex size-10 items-center justify-center rounded-xl bg-brand/10 text-brand">
                <ShieldCheck size={20} />
              </div>
              <div>
                <h3 className="text-sm font-bold text-slate-900">Chi tiết System Audit Log</h3>
                <p className="text-xs text-slate-500 font-mono">ID: {log.id}</p>
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
          <div className="max-h-[70vh] overflow-y-auto p-6 space-y-5 text-xs">
            {/* Action & Status Row */}
            <div className="grid grid-cols-2 gap-4 rounded-xl border border-slate-100 bg-slate-50/50 p-4">
              <div>
                <span className="text-[11px] font-bold text-slate-400 uppercase">Hành động</span>
                <div className="mt-1 flex items-center gap-2">
                  <span className="font-extrabold text-slate-900">{actionLabel}</span>
                  <span className="rounded bg-slate-200/70 px-1.5 py-0.5 text-[10px] font-mono text-slate-600">
                    {log.action}
                  </span>
                </div>
              </div>
              <div>
                <span className="text-[11px] font-bold text-slate-400 uppercase">Trạng thái</span>
                <div className="mt-1 flex items-center gap-1.5 font-bold">
                  {isSuccess ? (
                    <span className="inline-flex items-center gap-1 text-emerald-600">
                      <CheckCircle2 size={16} /> Thành công
                    </span>
                  ) : (
                    <span className="inline-flex items-center gap-1 text-rose-600">
                      <XCircle size={16} /> Thất bại
                    </span>
                  )}
                </div>
              </div>
            </div>

            {/* Error Message if failed */}
            {log.errorMessage && (
              <div className="rounded-xl border border-rose-200 bg-rose-50 p-3 text-rose-800 font-medium">
                <strong className="block text-[11px] font-bold uppercase text-rose-600 mb-0.5">Thông báo lỗi:</strong>
                {log.errorMessage}
              </div>
            )}

            {/* Actor & Metadata */}
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <span className="text-[11px] font-bold text-slate-400 uppercase">Người thực hiện</span>
                <p className="font-bold text-slate-900">{log.actorUsername || 'Hệ thống (System)'}</p>
                {log.actorEmail && <p className="text-slate-500">{log.actorEmail}</p>}
                {log.actorUserId && <p className="text-[10px] font-mono text-slate-400">User ID: {log.actorUserId}</p>}
              </div>

              <div className="space-y-2">
                <span className="text-[11px] font-bold text-slate-400 uppercase">Thông tin kết nối</span>
                <p className="flex items-center gap-1.5 text-slate-700">
                  <Globe size={14} className="text-slate-400" />
                  IP: <strong className="font-mono">{log.ipAddress || 'Không có'}</strong>
                </p>
                <p className="flex items-center gap-1.5 text-slate-700 truncate" title={log.userAgent || ''}>
                  <Laptop size={14} className="text-slate-400 shrink-0" />
                  UA: <span className="truncate">{log.userAgent || 'Không có'}</span>
                </p>
                <p className="flex items-center gap-1.5 text-slate-500">
                  <Clock size={14} className="text-slate-400" />
                  {new Date(log.createdAt).toLocaleString('vi-VN')}
                </p>
              </div>
            </div>

            {/* Target Resource */}
            <div className="rounded-xl border border-slate-100 bg-slate-50/50 p-3">
              <span className="text-[11px] font-bold text-slate-400 uppercase">Đối tượng tài nguyên</span>
              <p className="mt-1 font-bold text-slate-800">
                Loại: <span className="text-brand">{log.resourceType}</span>
                {log.resourceId && <span className="ml-3 font-mono text-slate-500">ID: {log.resourceId}</span>}
              </p>
            </div>

            {/* JSON Values (Old vs New) */}
            <div className="space-y-3 pt-2">
              <span className="flex items-center gap-1.5 font-bold text-slate-800 text-xs">
                <FileCode size={16} className="text-brand" />
                Chi tiết dữ liệu (Masked JSON Diff)
              </span>
              <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
                <div>
                  <span className="text-[10px] font-bold uppercase text-slate-400">Giá trị Cũ (Old Value)</span>
                  <pre className="mt-1 max-h-48 overflow-auto rounded-xl border border-slate-200 bg-slate-900 p-3 font-mono text-[11px] text-amber-300">
                    {formatJson(log.oldValueJson)}
                  </pre>
                </div>
                <div>
                  <span className="text-[10px] font-bold uppercase text-slate-400">Giá trị Mới (New Value)</span>
                  <pre className="mt-1 max-h-48 overflow-auto rounded-xl border border-slate-200 bg-slate-900 p-3 font-mono text-[11px] text-emerald-300">
                    {formatJson(log.newValueJson)}
                  </pre>
                </div>
              </div>
            </div>
          </div>
        </motion.div>
      </div>
    </AnimatePresence>
  )
}
