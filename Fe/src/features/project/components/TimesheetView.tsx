import { useEffect, useState, useCallback, useMemo } from 'react'
import { Clock3, FolderKanban, Users2, AlertTriangle, ChevronLeft, ChevronRight, RefreshCcw, Download, FileText, RotateCcw } from 'lucide-react'
import { Button, Input, Select } from '../../../components/ui'
import type { ProjectMember } from '../models/project.model'
import type { TimesheetPage, TimesheetSummary } from '../models/timesheet.model'
import { getMyTimesheet, getMyTimesheetSummary, getProjectTimesheet, getProjectTimesheetSummary, exportProjectTimeLogsExcel, exportMyTimeLogsExcel } from '../services/timesheet.service'

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

  // Animation states for Reset & Reload buttons
  const [isResetting, setIsResetting] = useState(false)
  const [isRefreshing, setIsRefreshing] = useState(false)

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

  const handleExportExcel = async () => {
    setError('')
    try {
      if (mode === 'project' && projectId) {
        await exportProjectTimeLogsExcel(projectId, {
          fromDate: fromDate || undefined,
          toDate: toDate || undefined,
          userId: selectedUserId || undefined,
          taskId: selectedTaskId || undefined
        })
      } else {
        await exportMyTimeLogsExcel({
          fromDate: fromDate || undefined,
          toDate: toDate || undefined
        })
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Không xuất được file Excel')
    }
  }

  const handleResetFilters = () => {
    setFromDate(firstDayOfMonth())
    setToDate(lastDayOfMonth())
    setSelectedUserId('')
    setSelectedTaskId('')
    setPage(0)
  }

  const handleResetFiltersWithAnim = () => {
    setIsResetting(true)
    handleResetFilters()
    setTimeout(() => setIsResetting(false), 600)
  }

  const handleReloadWithAnim = () => {
    setIsRefreshing(true)
    void loadTimesheet()
    setTimeout(() => setIsRefreshing(false), 600)
  }

  // Workload helper for daily grid styling
  const getWorkloadStyle = (minutes: number) => {
    if (minutes === 0) {
      return 'bg-slate-50/60 border-slate-100 text-slate-400'
    }
    if (minutes <= 480) {
      return 'bg-emerald-50/90 border-emerald-200/70 text-emerald-900 shadow-2xs hover:bg-emerald-100/60 font-semibold'
    }
    if (minutes <= 720) {
      return 'bg-amber-50/90 border-amber-200/70 text-amber-900 shadow-2xs hover:bg-amber-100/60 font-bold'
    }
    return 'bg-rose-50/90 border-rose-200/70 text-rose-900 shadow-2xs hover:bg-rose-100/60 font-bold'
  }

  // Build complete calendar grid aligned from THỨ 2 to CHỦ NHẬT with padding days
  const buildCalendarGrid = useMemo(() => {
    if (!fromDate || !toDate) return []

    const summaryMap = new Map<string, { totalMinutes: number; logCount: number }>()
    if (summaryData && summaryData.byDate) {
      summaryData.byDate.forEach(item => {
        summaryMap.set(item.workDate, { totalMinutes: item.totalMinutes, logCount: item.logCount })
      })
    }

    const start = new Date(fromDate)
    const end = new Date(toDate)

    if (isNaN(start.getTime()) || isNaN(end.getTime())) return []

    const startDayOfWeek = (start.getDay() + 6) % 7
    const gridStart = new Date(start)
    gridStart.setDate(gridStart.getDate() - startDayOfWeek)

    const endDayOfWeek = (end.getDay() + 6) % 7
    const endPaddingDays = 6 - endDayOfWeek
    const gridEnd = new Date(end)
    gridEnd.setDate(gridEnd.getDate() + endPaddingDays)

    const gridItems: {
      workDate: string
      dayNum: number
      monthNum: number
      weekdayLabel: string
      inRange: boolean
      totalMinutes: number
      logCount: number
    }[] = []

    const curr = new Date(gridStart)
    const weekdays = ['THỨ 2', 'THỨ 3', 'THỨ 4', 'THỨ 5', 'THỨ 6', 'THỨ 7', 'CN']

    while (curr <= gridEnd) {
      const yyyy = curr.getFullYear()
      const mm = String(curr.getMonth() + 1).padStart(2, '0')
      const dd = String(curr.getDate()).padStart(2, '0')
      const isoDate = `${yyyy}-${mm}-${dd}`

      const dayOfWeekIdx = (curr.getDay() + 6) % 7
      const inRange = isoDate >= fromDate && isoDate <= toDate
      const summary = summaryMap.get(isoDate) || { totalMinutes: 0, logCount: 0 }

      gridItems.push({
        workDate: isoDate,
        dayNum: curr.getDate(),
        monthNum: curr.getMonth() + 1,
        weekdayLabel: weekdays[dayOfWeekIdx],
        inRange,
        totalMinutes: summary.totalMinutes,
        logCount: summary.logCount,
      })

      curr.setDate(curr.getDate() + 1)
    }

    return gridItems
  }, [fromDate, toDate, summaryData])

  return (
    <div className="space-y-6 animate-enter">
      {/* Search / Filter Panel */}
      <section className="rounded-2xl border border-line/70 bg-white p-5 shadow-xs">
        <h3 className="mb-4 font-bold text-ink text-sm flex items-center gap-2">
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
            <Button 
              variant="outline-teal" 
              onClick={handleExportExcel}
              title={mode === 'project' ? 'Xuất Excel Time Log Dự án' : 'Xuất Excel Time Log Cá nhân'}
              leadingIcon={<Download size={16} />}
            >
              Xuất Excel
            </Button>

            {/* Nút Đặt lại (Icon Mũi tên quay ngược, Hover hiện 'Đặt lại', Click xoay 1 vòng ngược chiều) */}
            <button
              type="button"
              onClick={handleResetFiltersWithAnim}
              title="Đặt lại"
              className="relative group grid size-10 place-items-center rounded-xl border border-line/70 bg-white text-muted hover:border-brand hover:text-brand transition-all shadow-2xs"
            >
              <RotateCcw 
                size={18} 
                className="transition-transform duration-500 ease-in-out"
                style={{ transform: isResetting ? 'rotate(-360deg)' : 'rotate(0deg)' }}
              />
              <span className="absolute -top-8 left-1/2 -translate-x-1/2 hidden group-hover:block bg-slate-800 text-white text-[10px] font-bold px-2 py-0.5 rounded shadow-sm whitespace-nowrap z-20 pointer-events-none">
                Đặt lại
              </span>
            </button>

            {/* Nút Làm mới / Reload (Icon Reload, Hover hiện 'Làm mới', Click xoay 1 vòng thuận chiều) */}
            <button
              type="button"
              onClick={handleReloadWithAnim}
              title="Làm mới"
              className="relative group grid size-10 place-items-center rounded-xl border border-line/70 bg-white text-muted hover:border-brand hover:text-brand transition-all shadow-2xs"
            >
              <RefreshCcw 
                size={18} 
                className="transition-transform duration-500 ease-in-out"
                style={{ transform: isRefreshing ? 'rotate(360deg)' : 'rotate(0deg)' }}
              />
              <span className="absolute -top-8 left-1/2 -translate-x-1/2 hidden group-hover:block bg-slate-800 text-white text-[10px] font-bold px-2 py-0.5 rounded shadow-sm whitespace-nowrap z-20 pointer-events-none">
                Làm mới
              </span>
            </button>
          </div>
        </div>
      </section>

      {error && (
        <div className="rounded-2xl border border-rose-100 bg-rose-50 p-4 text-sm text-rose-600 flex items-center gap-2 animate-enter">
          <AlertTriangle size={16} />
          <span>{error}</span>
        </div>
      )}

      {/* Summary Stat Cards */}
      {summaryData && (
        <section className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
          <div className="rounded-2xl border border-line/70 bg-white p-5 shadow-xs flex items-center gap-4 transition hover:-translate-y-0.5 hover:shadow-md">
            <div className="grid size-12 place-items-center rounded-2xl bg-sky-50 text-sky-600 border border-sky-100 shrink-0">
              <Clock3 size={24} />
            </div>
            <div>
              <p className="text-xs text-muted font-medium">Tổng thời gian làm</p>
              <p className="text-xl font-bold mt-0.5 text-ink">{formatMinutes(summaryData.totalMinutes)}</p>
            </div>
          </div>

          <div className="rounded-2xl border border-line/70 bg-white p-5 shadow-xs flex items-center gap-4 transition hover:-translate-y-0.5 hover:shadow-md">
            <div className="grid size-12 place-items-center rounded-2xl bg-emerald-50 text-emerald-600 border border-emerald-100 shrink-0">
              <FileText size={24} />
            </div>
            <div>
              <p className="text-xs text-muted font-medium">Tổng số bản ghi</p>
              <p className="text-xl font-bold mt-0.5 text-ink">{summaryData.totalLogs} bản ghi</p>
            </div>
          </div>

          <div className="rounded-2xl border border-line/70 bg-white p-5 shadow-xs flex items-center gap-4 transition hover:-translate-y-0.5 hover:shadow-md">
            <div className="grid size-12 place-items-center rounded-2xl bg-amber-50 text-amber-600 border border-amber-100 shrink-0">
              <FolderKanban size={24} />
            </div>
            <div>
              <p className="text-xs text-muted font-medium">Số lượng Nhiệm vụ</p>
              <p className="text-xl font-bold mt-0.5 text-ink">{summaryData.taskCount} công việc</p>
            </div>
          </div>

          {mode === 'project' && (
            <div className="rounded-2xl border border-line/70 bg-white p-5 shadow-xs flex items-center gap-4 transition hover:-translate-y-0.5 hover:shadow-md">
              <div className="grid size-12 place-items-center rounded-2xl bg-teal-50 text-teal-600 border border-teal-100 shrink-0">
                <Users2 size={24} />
              </div>
              <div>
                <p className="text-xs text-muted font-medium">Số người log</p>
                <p className="text-xl font-bold mt-0.5 text-ink">{summaryData.userCount} thành viên</p>
              </div>
            </div>
          )}
        </section>
      )}

      {/* Tabs list */}
      <div className="border-b border-line/70">
        <div className="flex gap-4">
          <button 
            type="button" 
            className={`border-b-2 px-4 py-2.5 text-xs font-extrabold uppercase tracking-wider transition ${activeTab === 'details' ? 'border-brand text-brand' : 'border-transparent text-muted hover:text-ink'}`}
            onClick={() => setActiveTab('details')}
          >
            Chi tiết log
          </button>
          <button 
            type="button" 
            className={`border-b-2 px-4 py-2.5 text-xs font-extrabold uppercase tracking-wider transition ${activeTab === 'daily' ? 'border-brand text-brand' : 'border-transparent text-muted hover:text-ink'}`}
            onClick={() => setActiveTab('daily')}
          >
            Tổng hợp theo ngày
          </button>
          {mode === 'project' && (
            <button 
              type="button" 
              className={`border-b-2 px-4 py-2.5 text-xs font-extrabold uppercase tracking-wider transition ${activeTab === 'users' ? 'border-brand text-brand' : 'border-transparent text-muted hover:text-ink'}`}
              onClick={() => setActiveTab('users')}
            >
              Theo thành viên
            </button>
          )}
        </div>
      </div>

      {/* Active Tab Panel */}
      <section className="rounded-2xl border border-line/70 bg-white shadow-xs overflow-hidden">
        {loading ? (
          <div className="grid place-items-center py-16">
            <span className="size-8 animate-spin rounded-full border-4 border-brand border-r-transparent" />
          </div>
        ) : (
          <>
            {/* Tab 1: Chi tiết log */}
            {activeTab === 'details' && (
              <div>
                <div className="overflow-x-auto">
                  <table className="w-full text-left text-xs">
                    <thead className="bg-slate-50/80 text-muted-dark border-b border-line/60 font-bold uppercase text-[11px]">
                      <tr>
                        <th className="px-6 py-3.5 whitespace-nowrap">Ngày làm</th>
                        {mode === 'project' && <th className="px-5 py-3.5 whitespace-nowrap">Thành viên</th>}
                        <th className="px-5 py-3.5 whitespace-nowrap">Dự án</th>
                        <th className="px-5 py-3.5 whitespace-nowrap">Công việc (Task)</th>
                        <th className="px-5 py-3.5 whitespace-nowrap">Thời gian</th>
                        <th className="px-5 py-3.5 whitespace-nowrap">Mô tả</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-line/50">
                      {pageData && pageData.content.map(entry => (
                        <tr key={entry.id} className="transition-colors hover:bg-slate-50/80">
                          <td className="px-6 py-3.5 font-semibold text-ink whitespace-nowrap">
                            {new Date(entry.workDate).toLocaleDateString('vi-VN')}
                          </td>
                          {mode === 'project' && (
                            <td className="px-5 py-3.5 text-ink font-semibold whitespace-nowrap">
                              {entry.username}
                            </td>
                          )}
                          <td className="px-5 py-3.5">
                            <span className="inline-flex items-center rounded-full bg-brand-cream/80 border border-brand-line/60 text-brand-dark px-3 py-1 text-xs font-bold shadow-2xs">
                              [{entry.projectCode}] {entry.projectName}
                            </span>
                          </td>
                          <td className="px-5 py-3.5 text-ink font-medium">
                            {entry.taskTitle}
                          </td>
                          <td className="px-5 py-3.5 whitespace-nowrap">
                            <span className="inline-flex items-center rounded-md bg-slate-100 px-2.5 py-1 text-xs font-bold text-muted-dark">
                              {formatMinutes(entry.minutes)}
                            </span>
                          </td>
                          <td className="px-5 py-3.5 text-muted max-w-xs truncate" title={entry.description}>
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
                  <footer className="flex items-center justify-between border-t border-line/60 px-6 py-3 text-xs text-muted">
                    <span>Trang {page + 1} / {pageData.totalPages}</span>
                    <div className="flex gap-2">
                      <Button variant="secondary" size="sm" iconOnly disabled={page === 0} leadingIcon={<ChevronLeft size={16} />} onClick={() => setPage(page - 1)} />
                      <Button variant="secondary" size="sm" iconOnly disabled={page + 1 >= pageData.totalPages} leadingIcon={<ChevronRight size={16} />} onClick={() => setPage(page + 1)} />
                    </div>
                  </footer>
                )}
              </div>
            )}

            {/* Tab 2: Tổng hợp theo ngày (Lịch Grid View) */}
            {activeTab === 'daily' && (
              <div className="p-6 space-y-4">
                {/* Column Titles Header (THỨ 2 -> CN) */}
                <div className="grid grid-cols-7 gap-3 text-center border-b border-line/60 pb-3">
                  {['THỨ 2', 'THỨ 3', 'THỨ 4', 'THỨ 5', 'THỨ 6', 'THỨ 7', 'CN'].map(wd => (
                    <div key={wd} className="text-xs font-black uppercase text-slate-800 tracking-wider">
                      {wd}
                    </div>
                  ))}
                </div>

                <div className="grid gap-3 grid-cols-7">
                  {buildCalendarGrid.map(day => {
                    const formattedDateText = `${day.dayNum}/${day.monthNum}`
                    
                    if (!day.inRange) {
                      return (
                        <div
                          key={day.workDate}
                          className="rounded-2xl border border-slate-200/50 bg-slate-50/50 p-4 opacity-40 flex flex-col justify-between h-32 select-none"
                        >
                          <div>
                            <p className="text-base font-bold text-slate-400">
                              {formattedDateText}
                            </p>
                          </div>
                          <div className="mt-2 pt-2 border-t border-slate-200/40">
                            <p className="text-[10px] italic text-slate-400">Tháng khác</p>
                          </div>
                        </div>
                      )
                    }

                    // Days inside the filter range
                    const hasLog = day.totalMinutes > 0
                    const workloadStyle = hasLog 
                      ? getWorkloadStyle(day.totalMinutes)
                      : 'bg-white border-line hover:border-brand/40 shadow-2xs'

                    return (
                      <div
                        key={day.workDate}
                        className={`rounded-2xl border p-4 transition flex flex-col justify-between h-32 ${workloadStyle}`}
                      >
                        <div>
                          <p className={`text-base font-black tracking-tight ${hasLog ? '' : 'text-slate-900'}`}>
                            {formattedDateText}
                          </p>
                        </div>
                        <div className="mt-2 pt-2 border-t border-slate-200/60">
                          {hasLog ? (
                            <>
                              <p className="text-xs font-extrabold">{formatMinutes(day.totalMinutes)}</p>
                              <p className="text-[10px] font-black uppercase mt-0.5 opacity-80">{day.logCount} BẢN GHI</p>
                              {day.totalMinutes > 720 && (
                                <p className="text-[9px] bg-rose-100 text-rose-800 rounded px-1 mt-1 font-bold inline-block border border-rose-200">
                                  Quá tải (max 12h)
                                </p>
                              )}
                            </>
                          ) : (
                            <p className="text-[10px] font-bold text-slate-400">0h 0m</p>
                          )}
                        </div>
                      </div>
                    )
                  })}

                  {buildCalendarGrid.length === 0 && (
                    <div className="col-span-full py-10 text-center text-muted text-xs">
                      Không tìm thấy tổng hợp theo ngày.
                    </div>
                  )}
                </div>
              </div>
            )}

            {/* Tab 3: Theo thành viên */}
            {activeTab === 'users' && mode === 'project' && (
              <div className="overflow-x-auto">
                <table className="w-full text-left text-xs">
                  <thead className="bg-slate-50/80 text-muted-dark border-b border-line/60 font-bold uppercase text-[11px]">
                    <tr>
                      <th className="px-6 py-3.5">Tài khoản</th>
                      <th className="px-5 py-3.5">Email</th>
                      <th className="px-5 py-3.5">Số lượng Tasks</th>
                      <th className="px-5 py-3.5">Số lượng Logs</th>
                      <th className="px-5 py-3.5">Tổng thời gian</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-line/50">
                    {summaryData && summaryData.byUser.map(user => (
                      <tr key={user.userId} className="transition-colors hover:bg-slate-50/80">
                        <td className="px-6 py-3.5 font-bold text-ink">
                          {user.username}
                        </td>
                        <td className="px-5 py-3.5 text-muted">
                          {user.email}
                        </td>
                        <td className="px-5 py-3.5 text-ink font-semibold">
                          {user.taskCount} tasks
                        </td>
                        <td className="px-5 py-3.5 text-muted">
                          {user.logCount} logs
                        </td>
                        <td className="px-5 py-3.5">
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
