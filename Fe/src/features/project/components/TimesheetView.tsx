import { useEffect, useState, useCallback } from 'react'
import { Clock3, FolderKanban, Users2, AlertTriangle, FileSpreadsheet, ChevronLeft, ChevronRight, RefreshCcw } from 'lucide-react'
import { Button, Input, Select } from '../../../components/ui'
import type { ProjectMember } from '../models/project.model'
import type { TimesheetPage, TimesheetSummary } from '../models/timesheet.model'
import { getMyTimesheet, getMyTimesheetSummary, getProjectTimesheet, getProjectTimesheetSummary } from '../services/timesheet.service'

interface TimesheetViewProps {
  mode: 'personal' | 'project'
  projectId?: string
  members?: ProjectMember[]
}

const firstDayOfMonth = () => {
  const d = new Date()
  return new Date(d.getFullYear(), d.getMonth(), 1).toISOString().split('T')[0]
}

const lastDayOfMonth = () => {
  const d = new Date()
  return new Date(d.getFullYear(), d.getMonth() + 1, 0).toISOString().split('T')[0]
}

const formatMinutes = (minutes: number) => {
  const h = Math.floor(minutes / 60)
  const m = minutes % 60
  return h > 0 ? `${h}h ${m}m` : `${m}m`
}

export function TimesheetView({ mode, projectId, members = [] }: TimesheetViewProps) {
  const [fromDate, setFromDate] = useState(firstDayOfMonth())
  const [toDate, setToDate] = useState(lastDayOfMonth())
  const [selectedUserId, setSelectedUserId] = useState('')
  const [selectedTaskId, setSelectedTaskId] = useState('')
  
  const [pageData, setPageData] = useState<TimesheetPage | null>(null)
  const [summaryData, setSummaryData] = useState<TimesheetSummary | null>(null)
  const [page, setPage] = useState(0)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [activeTab, setActiveTab] = useState<'details' | 'daily' | 'users'>('details')

  const loadTimesheet = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      const searchParams = {
        fromDate: fromDate || undefined,
        toDate: toDate || undefined,
        userId: selectedUserId || undefined,
        taskId: selectedTaskId || undefined,
        page,
        size: 15
      }

      if (mode === 'personal') {
        const [pageResp, summaryResp] = await Promise.all([
          getMyTimesheet(searchParams),
          getMyTimesheetSummary(searchParams)
        ])
        setPageData(pageResp)
        setSummaryData(summaryResp)
      } else if (mode === 'project' && projectId) {
        const [pageResp, summaryResp] = await Promise.all([
          getProjectTimesheet(projectId, searchParams),
          getProjectTimesheetSummary(projectId, searchParams)
        ])
        setPageData(pageResp)
        setSummaryData(summaryResp)
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Không tải được dữ liệu Timesheet')
    } finally {
      setLoading(false)
    }
  }, [mode, projectId, fromDate, toDate, selectedUserId, selectedTaskId, page])

  useEffect(() => {
    void loadTimesheet()
  }, [loadTimesheet])

  const handleResetFilters = () => {
    setFromDate(firstDayOfMonth())
    setToDate(lastDayOfMonth())
    setSelectedUserId('')
    setSelectedTaskId('')
    setPage(0)
  }

  // Workload helper for daily grid styling
  const getWorkloadStyle = (minutes: number) => {
    if (minutes === 0) {
      return 'bg-slate-50 border-slate-200 text-slate-400'
    }
    if (minutes <= 480) {
      return 'bg-emerald-50 border-emerald-100 text-emerald-700 hover:bg-emerald-100/50'
    }
    if (minutes <= 720) {
      return 'bg-amber-50 border-amber-100 text-amber-700 hover:bg-amber-100/50 font-semibold'
    }
    return 'bg-rose-50 border-rose-100 text-rose-700 hover:bg-rose-100/50 font-bold'
  }

  return (
    <div className="space-y-6">
      {/* Search / Filter Panel */}
      <section className="rounded-xl border border-line bg-white p-5 shadow-sm">
        <h3 className="mb-4 font-bold text-slate-800 flex items-center gap-2">
          <Clock3 size={18} className="text-brand" /> Bộ lọc Timesheet
        </h3>
        <div className="grid gap-4 sm:grid-cols-2 md:grid-cols-4 items-end">
          <Input label="Từ ngày" type="date" value={fromDate} onChange={event => { setFromDate(event.target.value); setPage(0) }} />
          <Input label="Đến ngày" type="date" value={toDate} onChange={event => { setToDate(event.target.value); setPage(0) }} />
          
          {mode === 'project' && (
            <Select 
              label="Thành viên" 
              value={selectedUserId} 
              onChange={event => { setSelectedUserId(event.target.value); setPage(0) }}
              options={[{ label: '-- Tất cả thành viên --', value: '' }, ...members.map(m => ({ label: m.username, value: m.userId }))]}
            />
          )}
          
          <div className="flex gap-2">
            <Button variant="secondary" className="flex-1" onClick={handleResetFilters}>
              Đặt lại
            </Button>
            <Button variant="secondary" className="px-3" onClick={() => void loadTimesheet()} title="Tải lại">
              <RefreshCcw size={16} />
            </Button>
          </div>
        </div>
      </section>

      {error && (
        <div className="rounded-xl border border-rose-100 bg-rose-50 p-4 text-sm text-rose-600 flex items-center gap-2">
          <AlertTriangle size={16} />
          <span>{error}</span>
        </div>
      )}

      {/* Summary Cards */}
      {summaryData && (
        <section className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
          <div className="rounded-xl border border-line bg-white p-5 shadow-sm flex items-center gap-4">
            <div className="grid size-12 place-items-center rounded-full bg-indigo-50 text-indigo-600">
              <Clock3 size={24} />
            </div>
            <div>
              <p className="text-xs text-muted font-medium">Tổng thời gian làm</p>
              <p className="text-xl font-bold mt-1 text-slate-800">{formatMinutes(summaryData.totalMinutes)}</p>
            </div>
          </div>

          <div className="rounded-xl border border-line bg-white p-5 shadow-sm flex items-center gap-4">
            <div className="grid size-12 place-items-center rounded-full bg-emerald-50 text-emerald-600">
              <FileSpreadsheet size={24} />
            </div>
            <div>
              <p className="text-xs text-muted font-medium">Tổng số bản ghi</p>
              <p className="text-xl font-bold mt-1 text-slate-800">{summaryData.totalLogs} logs</p>
            </div>
          </div>

          <div className="rounded-xl border border-line bg-white p-5 shadow-sm flex items-center gap-4">
            <div className="grid size-12 place-items-center rounded-full bg-amber-50 text-amber-600">
              <FolderKanban size={24} />
            </div>
            <div>
              <p className="text-xs text-muted font-medium">Số lượng Tasks</p>
              <p className="text-xl font-bold mt-1 text-slate-800">{summaryData.taskCount} tasks</p>
            </div>
          </div>

          {mode === 'project' && (
            <div className="rounded-xl border border-line bg-white p-5 shadow-sm flex items-center gap-4">
              <div className="grid size-12 place-items-center rounded-full bg-teal-50 text-teal-600">
                <Users2 size={24} />
              </div>
              <div>
                <p className="text-xs text-muted font-medium">Số người log</p>
                <p className="text-xl font-bold mt-1 text-slate-800">{summaryData.userCount} thành viên</p>
              </div>
            </div>
          )}
        </section>
      )}

      {/* Tabs list */}
      <div className="border-b border-line">
        <div className="flex gap-4">
          <button 
            type="button" 
            className={`border-b-2 px-4 py-2.5 text-sm font-semibold transition ${activeTab === 'details' ? 'border-brand text-brand' : 'border-transparent text-muted hover:text-slate-600'}`}
            onClick={() => setActiveTab('details')}
          >
            Chi tiết log
          </button>
          <button 
            type="button" 
            className={`border-b-2 px-4 py-2.5 text-sm font-semibold transition ${activeTab === 'daily' ? 'border-brand text-brand' : 'border-transparent text-muted hover:text-slate-600'}`}
            onClick={() => setActiveTab('daily')}
          >
            Tổng hợp theo ngày
          </button>
          {mode === 'project' && (
            <button 
              type="button" 
              className={`border-b-2 px-4 py-2.5 text-sm font-semibold transition ${activeTab === 'users' ? 'border-brand text-brand' : 'border-transparent text-muted hover:text-slate-600'}`}
              onClick={() => setActiveTab('users')}
            >
              Theo thành viên
            </button>
          )}
        </div>
      </div>

      {/* Active Tab Panel */}
      <section className="rounded-xl border border-line bg-white shadow-sm overflow-hidden">
        {loading ? (
          <div className="grid place-items-center py-16">
            <span className="size-8 animate-spin rounded-full border-4 border-brand border-r-transparent" />
          </div>
        ) : (
          <>
            {activeTab === 'details' && (
              <div>
                <div className="overflow-x-auto">
                  <table className="w-full text-left text-sm">
                    <thead className="bg-canvas text-xs uppercase text-muted border-b border-line">
                      <tr>
                        <th className="px-5 py-3 font-semibold">Ngày làm</th>
                        {mode === 'project' && <th className="px-5 py-3 font-semibold">Thành viên</th>}
                        <th className="px-5 py-3 font-semibold">Dự án</th>
                        <th className="px-5 py-3 font-semibold">Công việc (Task)</th>
                        <th className="px-5 py-3 font-semibold">Thời gian</th>
                        <th className="px-5 py-3 font-semibold">Mô tả</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-line">
                      {pageData && pageData.content.map(entry => (
                        <tr key={entry.id} className="transition-colors hover:bg-canvas">
                          <td className="px-5 py-3 font-medium text-slate-800 whitespace-nowrap">
                            {new Date(entry.workDate).toLocaleDateString('vi-VN')}
                          </td>
                          {mode === 'project' && (
                            <td className="px-5 py-3 text-slate-600 font-semibold">
                              {entry.username}
                            </td>
                          )}
                          <td className="px-5 py-3 text-slate-500 font-bold">
                            [{entry.projectCode}] {entry.projectName}
                          </td>
                          <td className="px-5 py-3 text-slate-700">
                            {entry.taskTitle}
                          </td>
                          <td className="px-5 py-3 whitespace-nowrap">
                            <span className="inline-flex rounded-full bg-indigo-50 text-indigo-700 px-2.5 py-0.5 text-xs font-bold">
                              {formatMinutes(entry.minutes)}
                            </span>
                          </td>
                          <td className="px-5 py-3 text-slate-500 max-w-xs truncate" title={entry.description}>
                            {entry.description || '-'}
                          </td>
                        </tr>
                      ))}
                      {(!pageData || pageData.content.length === 0) && (
                        <tr>
                          <td colSpan={mode === 'project' ? 6 : 5} className="py-12 text-center text-muted">
                            Chưa có dữ liệu log thời gian.
                          </td>
                        </tr>
                      )}
                    </tbody>
                  </table>
                </div>

                {pageData && pageData.totalPages > 1 && (
                  <footer className="flex items-center justify-between border-t border-line px-5 py-4 text-sm text-muted">
                    <span>Trang {page + 1} / {pageData.totalPages}</span>
                    <div className="flex gap-2">
                      <Button variant="secondary" size="sm" iconOnly disabled={page === 0} leadingIcon={<ChevronLeft size={16} />} onClick={() => setPage(page - 1)} />
                      <Button variant="secondary" size="sm" iconOnly disabled={page + 1 >= pageData.totalPages} leadingIcon={<ChevronRight size={16} />} onClick={() => setPage(page + 1)} />
                    </div>
                  </footer>
                )}
              </div>
            )}

            {activeTab === 'daily' && (
              <div className="p-6">
                <div className="grid gap-3 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-7">
                  {summaryData && summaryData.byDate.map(day => (
                    <div 
                      key={day.workDate} 
                      className={`rounded-xl border p-3.5 transition flex flex-col justify-between h-28 shadow-sm ${getWorkloadStyle(day.totalMinutes)}`}
                    >
                      <div>
                        <p className="text-[10px] uppercase tracking-wide opacity-75">
                          {new Date(day.workDate).toLocaleDateString('vi-VN', { weekday: 'short' })}
                        </p>
                        <p className="text-sm font-bold mt-0.5">
                          {new Date(day.workDate).toLocaleDateString('vi-VN', { day: 'numeric', month: 'numeric' })}
                        </p>
                      </div>
                      <div className="mt-2 pt-2 border-t border-slate-200/40">
                        {day.totalMinutes > 0 ? (
                          <>
                            <p className="text-xs font-bold">{formatMinutes(day.totalMinutes)}</p>
                            <p className="text-[9px] opacity-75 mt-0.5">{day.logCount} logs</p>
                            {day.totalMinutes > 720 && (
                              <p className="text-[8px] bg-red-100 text-red-800 rounded px-1 mt-1 font-bold inline-block border border-red-200 animate-pulse">
                                Quá tải (max 12h)
                              </p>
                            )}
                          </>
                        ) : (
                          <p className="text-[10px] italic opacity-60">0h 0m</p>
                        )}
                      </div>
                    </div>
                  ))}
                  {(!summaryData || summaryData.byDate.length === 0) && (
                    <div className="col-span-full py-10 text-center text-muted text-sm">
                      Không tìm thấy tổng hợp theo ngày.
                    </div>
                  )}
                </div>
              </div>
            )}

            {activeTab === 'users' && mode === 'project' && (
              <div className="overflow-x-auto">
                <table className="w-full text-left text-sm">
                  <thead className="bg-canvas text-xs uppercase text-muted border-b border-line">
                    <tr>
                      <th className="px-5 py-3 font-semibold">Tài khoản</th>
                      <th className="px-5 py-3 font-semibold">Email</th>
                      <th className="px-5 py-3 font-semibold">Số lượng Tasks</th>
                      <th className="px-5 py-3 font-semibold">Số lượng Logs</th>
                      <th className="px-5 py-3 font-semibold">Tổng thời gian</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-line">
                    {summaryData && summaryData.byUser.map(user => (
                      <tr key={user.userId} className="transition-colors hover:bg-canvas">
                        <td className="px-5 py-3 font-semibold text-slate-800">
                          {user.username}
                        </td>
                        <td className="px-5 py-3 text-slate-500">
                          {user.email}
                        </td>
                        <td className="px-5 py-3 text-slate-700 font-bold">
                          {user.taskCount} tasks
                        </td>
                        <td className="px-5 py-3 text-slate-500">
                          {user.logCount} logs
                        </td>
                        <td className="px-5 py-3">
                          <span className="inline-flex rounded-full bg-emerald-50 text-emerald-700 px-3 py-1 text-xs font-bold border border-emerald-100">
                            {formatMinutes(user.totalMinutes)}
                          </span>
                        </td>
                      </tr>
                    ))}
                    {(!summaryData || summaryData.byUser.length === 0) && (
                      <tr>
                        <td colSpan={5} className="py-12 text-center text-muted">
                          Chưa có dữ liệu thành viên.
                        </td>
                      </tr>
                    )}
                  </tbody>
                </table>
              </div>
            )}
          </>
        )}
      </section>
    </div>
  )
}
