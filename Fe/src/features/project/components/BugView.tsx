import { useEffect, useState, useCallback, useMemo } from 'react'
import { 
  Bug as BugIcon, 
  ShieldAlert, 
  Pencil, 
  Trash2, 
  Plus, 
  Search, 
  X, 
  ChevronLeft, 
  ChevronRight, 
  RefreshCcw, 
  CheckCircle2,
  Play,
  MessageSquare,
  FileText,
  Paperclip,
  CheckSquare,
  Download,
  Filter
} from 'lucide-react'
import AttachmentSection from './AttachmentSection'
import { 
  ActionMenu, 
  ActionItem, 
  Button, 
  ConfirmDialog, 
  Input, 
  Modal, 
  Select 
} from '../../../components/ui'
import type { ProjectMember } from '../models/project.model'
import type { Task, KanbanTask, TaskPriority } from '../models/task.model'
import type { BacklogItem, Sprint } from '../models/scrum.model'
import type { 
  Bug, 
  BugPage, 
  BugSummary, 
  BugSearchFilters, 
  BugStatus, 
  BugSeverity,
  BugComment,
  BugEvidence,
  BugDashboard,
  BugReport,
  QaMetrics,
  BugReportFilters
} from '../models/bug.model'
import { 
  bugSeverityLabels, 
  bugStatusLabels 
} from '../models/bug.model'
import { 
  createBug, 
  searchBugs, 
  updateBug, 
  assignBug, 
  updateBugStatus, 
  deleteBug, 
  getBugSummary,
  getBugById,
  getBugComments,
  createBugComment,
  updateBugComment,
  deleteBugComment,
  getBugEvidences,
  createBugEvidence,
  updateBugEvidence,
  deleteBugEvidence,
  getBugDashboard,
  getBugReport,
  getSprintBugReport,
  getQaMetrics,
  exportBugReportExcel,
  unassignBug,
  updateBugSeverity,
  updateBugPriority
} from '../services/bug.service'

interface BugViewProps {
  projectId: string
  members: ProjectMember[]
  backlogItems: BacklogItem[]
  tasks: (Task | KanbanTask)[]
  sprints: Sprint[]
  openBugId?: string
  onCloseBug?: () => void
}

const emptyBugPage: BugPage = {
  content: [],
  totalElements: 0,
  totalPages: 0,
  number: 0,
  size: 10,
  numberOfElements: 0,
  first: true,
  last: true,
  empty: true
}

export const getValidTransitions = (status: BugStatus): BugStatus[] => {
  switch (status) {
    case 'OPEN':
      return ['ASSIGNED', 'IN_PROGRESS', 'CANCELLED']
    case 'ASSIGNED':
      return ['IN_PROGRESS', 'OPEN', 'CANCELLED']
    case 'IN_PROGRESS':
      return ['RESOLVED', 'OPEN', 'CANCELLED']
    case 'RESOLVED':
      return ['VERIFIED', 'REOPENED', 'IN_PROGRESS']
    case 'VERIFIED':
      return ['CLOSED', 'REOPENED']
    case 'REOPENED':
      return ['ASSIGNED', 'IN_PROGRESS', 'CANCELLED']
    case 'CLOSED':
    case 'CANCELLED':
    default:
      return []
  }
}

export const getSeverityBadgeClass = (sev: BugSeverity) => {
  switch (sev) {
    case 'CRITICAL':
      return 'bg-rose-100 text-rose-800 border border-rose-200'
    case 'HIGH':
      return 'bg-red-100 text-red-800 border border-red-200'
    case 'MEDIUM':
      return 'bg-amber-100 text-amber-800 border border-amber-200'
    case 'LOW':
    default:
      return 'bg-slate-100 text-slate-800 border border-slate-200'
  }
}

export const getStatusBadgeClass = (status: BugStatus) => {
  switch (status) {
    case 'OPEN':
      return 'bg-blue-50 text-blue-700 border border-blue-200'
    case 'ASSIGNED':
      return 'bg-indigo-50 text-indigo-700 border border-indigo-200'
    case 'IN_PROGRESS':
      return 'bg-orange-50 text-orange-700 border border-orange-200'
    case 'RESOLVED':
      return 'bg-emerald-50 text-emerald-700 border border-emerald-200'
    case 'VERIFIED':
      return 'bg-teal-50 text-teal-700 border border-teal-200'
    case 'REOPENED':
      return 'bg-purple-50 text-purple-700 border border-purple-200'
    case 'CLOSED':
      return 'bg-slate-50 text-slate-600 border border-slate-200'
    case 'CANCELLED':
    default:
      return 'bg-gray-100 text-gray-500 border border-gray-200'
  }
}

export function BugView({ projectId, members, backlogItems, tasks, sprints, openBugId, onCloseBug }: BugViewProps) {
  const [bugs, setBugs] = useState<BugPage>(emptyBugPage)
  const [summary, setSummary] = useState<BugSummary | null>(null)
  const [loading, setLoading] = useState(false)
  const [saving, setSaving] = useState(false)
  const [page, setPage] = useState(0)

  // Sub-tab state
  const [activeSubTab, setActiveSubTab] = useState<'list' | 'dashboard'>('list')

  // Dashboard & QA Metrics state
  const [dashboardData, setDashboardData] = useState<BugDashboard | null>(null)
  const [qaMetrics, setQaMetrics] = useState<QaMetrics | null>(null)
  const [reportData, setReportData] = useState<BugReport | null>(null)
  const [reportLoading, setReportLoading] = useState(false)

  // Report filters state
  const [reportFilters, setReportFilters] = useState<BugReportFilters>({
    fromDate: new Date(Date.now() - 30 * 24 * 3600 * 1000).toISOString().split('T')[0],
    toDate: new Date().toISOString().split('T')[0],
    sprintId: '',
    assigneeUserId: '',
    status: '',
    severity: '',
    priority: '',
    overdueOnly: false
  })
  
  // Filters state
  const [filters, setFilters] = useState<BugSearchFilters>({
    keyword: '',
    status: '',
    severity: '',
    priority: '',
    assigneeUserId: '',
    reporterUserId: '',
    taskId: '',
    backlogItemId: ''
  })
  const [showBugAdvancedFilters, setShowBugAdvancedFilters] = useState(false)

  // Modals state
  const [createOpen, setCreateOpen] = useState(false)
  const [selectedBugDetails, setSelectedBugDetails] = useState<Bug | null>(null)
  const [editBug, setEditBug] = useState<Bug | null>(null)
  const [deleteBugId, setDeleteBugId] = useState<string | null>(null)

  // Bug form state
  const [formTitle, setFormTitle] = useState('')
  const [formDesc, setFormDesc] = useState('')
  const [formSeverity, setFormSeverity] = useState<BugSeverity>('MEDIUM')
  const [formPriority, setFormPriority] = useState<'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT'>('MEDIUM')
  const [formAssigneeId, setFormAssigneeId] = useState('')
  const [formBacklogId, setFormBacklogId] = useState('')
  const [formTaskId, setFormTaskId] = useState('')
  const [formSteps, setFormSteps] = useState('')
  const [formExpected, setFormExpected] = useState('')
  const [formActual, setFormActual] = useState('')

  const loadBugs = useCallback(async () => {
    setLoading(true)
    try {
      const pageData = await searchBugs(projectId, filters, page, 10)
      setBugs(pageData)
      const sumData = await getBugSummary(projectId)
      setSummary(sumData)
    } catch (err) {
      console.error(err)
    } finally {
      setLoading(false)
    }
  }, [projectId, filters, page])

  useEffect(() => {
    loadBugs()
  }, [loadBugs])

  const loadDashboard = useCallback(async () => {
    setReportLoading(true)
    try {
      const db = await getBugDashboard(projectId)
      setDashboardData(db)
      const qa = await getQaMetrics(projectId, reportFilters)
      setQaMetrics(qa)
      const rep = reportFilters.sprintId 
        ? await getSprintBugReport(projectId, reportFilters.sprintId, reportFilters)
        : await getBugReport(projectId, reportFilters)
      setReportData(rep)
    } catch (err) {
      console.error(err)
    } finally {
      setReportLoading(false)
    }
  }, [projectId, reportFilters])

  useEffect(() => {
    if (activeSubTab === 'dashboard') {
      loadDashboard()
    }
  }, [activeSubTab, loadDashboard])

  // Load bug details when openBugId is passed from outside
  useEffect(() => {
    if (openBugId) {
      setLoading(true)
      getBugById(projectId, openBugId)
        .then(bug => {
          setSelectedBugDetails(bug)
        })
        .catch(err => {
          console.error(err)
        })
        .finally(() => {
          setLoading(false)
        })
    }
  }, [projectId, openBugId])

  const handleClearFilters = () => {
    setFilters({
      keyword: '',
      status: '',
      severity: '',
      priority: '',
      assigneeUserId: '',
      reporterUserId: '',
      taskId: '',
      backlogItemId: '',
      sprintId: '',
      linkedTaskId: '',
      reopenedOnly: false,
      overdueOnly: false,
      dueDateFrom: '',
      dueDateTo: '',
      createdFrom: '',
      createdTo: ''
    })
    setPage(0)
  }

  const handleOpenCreate = () => {
    setFormTitle('')
    setFormDesc('')
    setFormSeverity('MEDIUM')
    setFormPriority('MEDIUM')
    setFormAssigneeId('')
    setFormBacklogId('')
    setFormTaskId('')
    setFormSteps('')
    setFormExpected('')
    setFormActual('')
    setCreateOpen(true)
  }

  const handleOpenEdit = (bug: Bug) => {
    setEditBug(bug)
    setFormTitle(bug.title)
    setFormDesc(bug.description || '')
    setFormSeverity(bug.severity)
    setFormPriority(bug.priority)
    setFormAssigneeId(bug.assigneeUserId || '')
    setFormBacklogId(bug.backlogItemId || '')
    setFormTaskId(bug.taskId || '')
    setFormSteps(bug.reproductionSteps || '')
    setFormExpected(bug.expectedResult || '')
    setFormActual(bug.actualResult || '')
  }

  const handleSaveCreate = async (event: React.FormEvent) => {
    event.preventDefault()
    if (!formTitle.trim()) return
    setSaving(true)
    try {
      await createBug(projectId, {
        title: formTitle,
        description: formDesc || null,
        severity: formSeverity,
        priority: formPriority,
        assigneeUserId: formAssigneeId || null,
        backlogItemId: formBacklogId || null,
        taskId: formTaskId || null,
        reproductionSteps: formSteps || null,
        expectedResult: formExpected || null,
        actualResult: formActual || null
      })
      setCreateOpen(false)
      loadBugs()
    } catch (err) {
      console.error(err)
    } finally {
      setSaving(false)
    }
  }

  const handleSaveEdit = async (event: React.FormEvent) => {
    event.preventDefault()
    if (!editBug || !formTitle.trim()) return
    setSaving(true)
    try {
      await updateBug(projectId, editBug.id, {
        title: formTitle,
        description: formDesc || null,
        severity: formSeverity,
        priority: formPriority,
        backlogItemId: formBacklogId || null,
        taskId: formTaskId || null,
        reproductionSteps: formSteps || null,
        expectedResult: formExpected || null,
        actualResult: formActual || null
      })
      if (formAssigneeId !== (editBug.assigneeUserId || '')) {
        await assignBug(projectId, editBug.id, formAssigneeId || null)
      }
      setEditBug(null)
      loadBugs()
    } catch (err) {
      console.error(err)
    } finally {
      setSaving(false)
    }
  }

  const handleStatusChange = async (bugId: string, nextStatus: BugStatus) => {
    setSaving(true)
    try {
      await updateBugStatus(projectId, bugId, nextStatus)
      loadBugs()
      if (selectedBugDetails?.id === bugId) {
        setSelectedBugDetails(prev => prev ? { ...prev, status: nextStatus } : null)
      }
    } catch (err) {
      console.error(err)
    } finally {
      setSaving(false)
    }
  }

  const handleAssignChange = async (bugId: string, assigneeId: string | null) => {
    setSaving(true)
    try {
      if (assigneeId) {
        await assignBug(projectId, bugId, assigneeId)
      } else {
        await unassignBug(projectId, bugId)
      }
      loadBugs()
      if (selectedBugDetails?.id === bugId) {
        const user = assigneeId ? members.find(m => m.userId === assigneeId) : null
        setSelectedBugDetails(prev => prev ? { 
          ...prev, 
          assigneeUserId: assigneeId || null,
          assigneeUsername: user ? user.username : null,
          assigneeEmail: user ? user.email : null,
          status: assigneeId 
            ? (prev.status === 'OPEN' ? 'ASSIGNED' : prev.status) 
            : (prev.status === 'ASSIGNED' ? 'OPEN' : prev.status)
        } : null)
      }
    } catch (err) {
      console.error(err)
    } finally {
      setSaving(false)
    }
  }

  const handleSeverityChange = async (bugId: string, severity: BugSeverity) => {
    setSaving(true)
    try {
      await updateBugSeverity(projectId, bugId, severity)
      loadBugs()
      if (selectedBugDetails?.id === bugId) {
        setSelectedBugDetails(prev => prev ? { ...prev, severity } : null)
      }
    } catch (err) {
      console.error(err)
    } finally {
      setSaving(false)
    }
  }

  const handlePriorityChange = async (bugId: string, priority: TaskPriority) => {
    setSaving(true)
    try {
      await updateBugPriority(projectId, bugId, priority)
      loadBugs()
      if (selectedBugDetails?.id === bugId) {
        setSelectedBugDetails(prev => prev ? { ...prev, priority } : null)
      }
    } catch (err) {
      console.error(err)
    } finally {
      setSaving(false)
    }
  }

  const handleDelete = async () => {
    if (!deleteBugId) return
    setSaving(true)
    try {
      await deleteBug(projectId, deleteBugId)
      setDeleteBugId(null)
      loadBugs()
    } catch (err) {
      console.error(err)
    } finally {
      setSaving(false)
    }
  }



  // Lookup helpers
  const getTaskTitle = (taskId: string | null) => {
    if (!taskId) return null
    const found = tasks.find(t => t.id === taskId)
    return found ? found.title : 'Task không rõ'
  }

  const getBacklogTitle = (backlogId: string | null) => {
    if (!backlogId) return null
    const found = backlogItems.find(b => b.id === backlogId)
    return found ? found.title : 'User Story không rõ'
  }

  return (
    <div className="space-y-6">
      {/* Sub-tab switcher */}
      <div className="flex border-b border-line gap-4">
        <button
          type="button"
          onClick={() => setActiveSubTab('list')}
          className={`pb-3 text-sm font-bold border-b-2 transition ${
            activeSubTab === 'list' ? 'border-b-brand text-brand border-brand' : 'border-transparent text-muted hover:text-ink'
          }`}
        >
          Danh sách lỗi
        </button>
        <button
          type="button"
          onClick={() => setActiveSubTab('dashboard')}
          className={`pb-3 text-sm font-bold border-b-2 transition ${
            activeSubTab === 'dashboard' ? 'border-b-brand text-brand border-brand' : 'border-transparent text-muted hover:text-ink'
          }`}
        >
          Báo cáo & Thống kê (Dashboard)
        </button>
      </div>

      {activeSubTab === 'list' ? (
        <div className="space-y-6">
          {/* Summary Section */}
          {summary && (
            <div className="grid gap-4 grid-cols-2 md:grid-cols-5">
              <div className="rounded-2xl border border-line bg-white p-4 shadow-sm">
                <p className="text-xs font-semibold text-muted uppercase tracking-wider">Tổng số lỗi</p>
                <p className="mt-2 text-2xl font-bold text-ink flex items-center gap-2">
                  <BugIcon className="text-indigo-500" size={22} />
                  {summary.totalBugs}
                </p>
              </div>
              <div className="rounded-2xl border border-line bg-white p-4 shadow-sm">
                <p className="text-xs font-semibold text-muted uppercase tracking-wider">Chờ xử lý / Reopened</p>
                <p className="mt-2 text-2xl font-bold text-ink flex items-center gap-2">
                  <Play className="text-blue-500" size={22} />
                  {summary.openBugs + summary.reopenedBugs}
                </p>
              </div>
              <div className="rounded-2xl border border-line bg-white p-4 shadow-sm">
                <p className="text-xs font-semibold text-muted uppercase tracking-wider">Đang sửa</p>
                <p className="mt-2 text-2xl font-bold text-ink flex items-center gap-2">
                  <RefreshCcw className="text-orange-500" size={22} />
                  {summary.inProgressBugs}
                </p>
              </div>
              <div className="rounded-2xl border border-line bg-white p-4 shadow-sm relative overflow-hidden">
                <p className="text-xs font-semibold text-muted uppercase tracking-wider">Khẩn cấp (Critical)</p>
                <p className="mt-2 text-2xl font-bold text-rose-600 flex items-center gap-2">
                  <ShieldAlert className="text-rose-500 animate-pulse" size={22} />
                  {summary.criticalBugs}
                </p>
              </div>
              <div className="rounded-2xl border border-line bg-white p-4 shadow-sm">
                <p className="text-xs font-semibold text-muted uppercase tracking-wider">Đã giải quyết</p>
                <p className="mt-2 text-2xl font-bold text-emerald-600 flex items-center gap-2">
                  <CheckCircle2 className="text-emerald-500" size={22} />
                  {summary.resolvedBugs}
                </p>
              </div>
            </div>
          )}

          {/* Filter & Actions Bar */}
          <div className="rounded-2xl border border-line bg-white p-5 shadow-sm space-y-4">
            <div className="flex flex-col md:flex-row gap-4 justify-between items-start md:items-center">
              <div className="flex-1 min-w-0 w-full relative">
                <Search className="absolute left-3.5 top-1/2 -translate-y-1/2 text-muted" size={17} />
                <Input 
                  aria-label="Tìm kiếm bug"
                  value={filters.keyword} 
                  onChange={event => setFilters(prev => ({ ...prev, keyword: event.target.value }))}
                  placeholder="Tìm theo tiêu đề, mô tả hoặc người xử lý..." 
                  className="pl-10"
                />
              </div>
              <Button 
                leadingIcon={<Plus size={16} />} 
                onClick={handleOpenCreate}
                className="w-full md:w-auto"
              >
                Báo cáo Bug
              </Button>
            </div>

            <div className="grid gap-3 grid-cols-2 sm:grid-cols-4 lg:grid-cols-7">
              <Select 
                aria-label="Trạng thái"
                value={filters.status} 
                onChange={event => setFilters(prev => ({ ...prev, status: event.target.value as BugStatus | '' }))}
                options={[
                  { label: 'Tất cả trạng thái', value: '' },
                  ...Object.entries(bugStatusLabels).map(([k, v]) => ({ label: v, value: k }))
                ]}
              />
              <Select 
                aria-label="Độ nghiêm trọng"
                value={filters.severity} 
                onChange={event => setFilters(prev => ({ ...prev, severity: event.target.value as BugSeverity | '' }))}
                options={[
                  { label: 'Tất cả độ nghiêm trọng', value: '' },
                  ...Object.entries(bugSeverityLabels).map(([k, v]) => ({ label: v, value: k }))
                ]}
              />
              <Select 
                aria-label="Độ ưu tiên"
                value={filters.priority} 
                onChange={event => setFilters(prev => ({ ...prev, priority: event.target.value as any }))}
                options={[
                  { label: 'Tất cả độ ưu tiên', value: '' },
                  { label: 'Thấp (LOW)', value: 'LOW' },
                  { label: 'Trung bình (MEDIUM)', value: 'MEDIUM' },
                  { label: 'Cao (HIGH)', value: 'HIGH' },
                  { label: 'Khẩn cấp (URGENT)', value: 'URGENT' }
                ]}
              />
              <Select 
                aria-label="Người xử lý"
                value={filters.assigneeUserId} 
                onChange={event => setFilters(prev => ({ ...prev, assigneeUserId: event.target.value }))}
                options={[
                  { label: 'Tất cả người xử lý', value: '' },
                  ...members.map(m => ({ label: m.username, value: m.userId }))
                ]}
              />
              <Select 
                aria-label="Người báo cáo"
                value={filters.reporterUserId} 
                onChange={event => setFilters(prev => ({ ...prev, reporterUserId: event.target.value }))}
                options={[
                  { label: 'Tất cả người báo cáo', value: '' },
                  ...members.map(m => ({ label: m.username, value: m.userId }))
                ]}
              />
              <Button 
                variant="secondary"
                onClick={() => setShowBugAdvancedFilters(!showBugAdvancedFilters)}
                className={`w-full !px-3 ${showBugAdvancedFilters ? '!bg-brand/10 !text-brand border-brand/20' : ''}`}
                leadingIcon={<Filter size={15} />}
              >
                Lọc nâng cao
              </Button>
              <Button 
                variant="secondary" 
                leadingIcon={<X size={15} />} 
                onClick={handleClearFilters}
                className="w-full"
              >
                Xóa lọc
              </Button>
            </div>

            {showBugAdvancedFilters && (
              <div className="p-4 bg-slate-50 border border-line rounded-xl space-y-4 text-xs animate-enter">
                <div className="grid gap-3 sm:grid-cols-2 md:grid-cols-4">
                  <div>
                    <label className="block text-[11px] font-bold text-muted-dark mb-1">Sprint</label>
                    <Select
                      aria-label="Chọn Sprint"
                      value={filters.sprintId || ''}
                      onChange={e => setFilters(prev => ({ ...prev, sprintId: e.target.value || undefined }))}
                      options={[
                        { label: 'Tất cả Sprint', value: '' },
                        ...sprints.map(s => ({ label: s.name, value: s.id }))
                      ]}
                    />
                  </div>
                  <div>
                    <label className="block text-[11px] font-bold text-muted-dark mb-1">Yêu cầu (Backlog Item)</label>
                    <Select
                      aria-label="Chọn Yêu cầu"
                      value={filters.backlogItemId || ''}
                      onChange={e => setFilters(prev => ({ ...prev, backlogItemId: e.target.value || undefined }))}
                      options={[
                        { label: 'Tất cả Yêu cầu', value: '' },
                        ...backlogItems.map(item => ({ label: item.title, value: item.id }))
                      ]}
                    />
                  </div>
                  <div>
                    <label className="block text-[11px] font-bold text-muted-dark mb-1">Nhiệm vụ (Task)</label>
                    <Select
                      aria-label="Chọn Nhiệm vụ"
                      value={filters.taskId || ''}
                      onChange={e => setFilters(prev => ({ ...prev, taskId: e.target.value || undefined }))}
                      options={[
                        { label: 'Tất cả Nhiệm vụ', value: '' },
                        ...tasks.map(t => ({ label: t.title, value: t.id }))
                      ]}
                    />
                  </div>
                  <div>
                    <label className="block text-[11px] font-bold text-muted-dark mb-1">Nhiệm vụ liên kết (Linked Task)</label>
                    <Select
                      aria-label="Chọn Linked Task"
                      value={filters.linkedTaskId || ''}
                      onChange={e => setFilters(prev => ({ ...prev, linkedTaskId: e.target.value || undefined }))}
                      options={[
                        { label: 'Tất cả Linked Task', value: '' },
                        ...tasks.map(t => ({ label: t.title, value: t.id }))
                      ]}
                    />
                  </div>
                </div>

                <div className="grid gap-3 sm:grid-cols-2 md:grid-cols-4">
                  <div>
                    <label className="block text-[11px] font-bold text-muted-dark mb-1">Hạn chót từ ngày</label>
                    <Input type="date" value={filters.dueDateFrom || ''} onChange={e => setFilters(prev => ({ ...prev, dueDateFrom: e.target.value || undefined }))} />
                  </div>
                  <div>
                    <label className="block text-[11px] font-bold text-muted-dark mb-1">Đến ngày</label>
                    <Input type="date" value={filters.dueDateTo || ''} onChange={e => setFilters(prev => ({ ...prev, dueDateTo: e.target.value || undefined }))} />
                  </div>
                  <div>
                    <label className="block text-[11px] font-bold text-muted-dark mb-1">Ngày tạo từ ngày</label>
                    <Input type="date" value={filters.createdFrom || ''} onChange={e => setFilters(prev => ({ ...prev, createdFrom: e.target.value ? new Date(e.target.value).toISOString() : undefined }))} />
                  </div>
                  <div>
                    <label className="block text-[11px] font-bold text-muted-dark mb-1">Đến ngày</label>
                    <Input type="date" value={filters.createdTo || ''} onChange={e => setFilters(prev => ({ ...prev, createdTo: e.target.value ? new Date(new Date(e.target.value).setHours(23, 59, 59, 999)).toISOString() : undefined }))} />
                  </div>
                </div>

                <div className="flex flex-wrap items-center justify-between gap-3 pt-2.5 border-t border-line/60">
                  <div className="flex gap-4">
                    <label className="flex items-center gap-1.5 font-bold text-muted-dark select-none cursor-pointer">
                      <input
                        type="checkbox"
                        checked={filters.reopenedOnly || false}
                        onChange={e => setFilters(prev => ({ ...prev, reopenedOnly: e.target.checked }))}
                        className="rounded border-line text-brand focus:ring-brand"
                      />
                      Đã mở lại (Reopened)
                    </label>
                    <label className="flex items-center gap-1.5 font-bold text-muted-dark select-none cursor-pointer">
                      <input
                        type="checkbox"
                        checked={filters.overdueOnly || false}
                        onChange={e => setFilters(prev => ({ ...prev, overdueOnly: e.target.checked }))}
                        className="rounded border-line text-brand focus:ring-brand"
                      />
                      Quá hạn (Overdue)
                    </label>
                  </div>
                  <button
                    type="button"
                    onClick={handleClearFilters}
                    className="text-brand hover:text-brand-dark font-extrabold"
                  >
                    Đặt lại bộ lọc
                  </button>
                </div>
              </div>
            )}
          </div>

          {/* Bugs List Grid */}
          <div className="space-y-3">
            {bugs.content.map(bug => {
              const transitions = getValidTransitions(bug.status)
              return (
                <article 
                  key={bug.id} 
                  className="rounded-2xl border border-line bg-white p-5 shadow-sm transition hover:shadow-md relative"
                >
                  {/* Critical indicator bar */}
                  {bug.severity === 'CRITICAL' && <div className="absolute left-0 top-0 bottom-0 w-1 bg-rose-600 animate-pulse rounded-l-2xl" />}

                  <div className="flex flex-col sm:flex-row gap-4 justify-between items-start sm:items-center">
                    <button 
                      type="button" 
                      className="space-y-1 text-left flex-1" 
                      onClick={() => setSelectedBugDetails(bug)}
                    >
                      <div className="flex flex-wrap items-center gap-2">
                        <span className="text-[11px] font-bold text-muted bg-slate-100 px-2 py-0.5 rounded">BUG-{bug.id.substring(0, 5).toUpperCase()}</span>
                        <span className={`rounded-full px-2.5 py-0.5 text-xs font-semibold ${getSeverityBadgeClass(bug.severity)}`}>
                          {bugSeverityLabels[bug.severity]}
                        </span>
                        <span className={`rounded-full px-2.5 py-0.5 text-xs font-semibold ${getStatusBadgeClass(bug.status)}`}>
                          {bugStatusLabels[bug.status]}
                        </span>
                      </div>
                      <h3 className="text-base font-bold text-ink hover:text-brand transition">{bug.title}</h3>
                      <div className="flex flex-wrap gap-x-4 gap-y-1 text-xs text-muted">
                        <p>Báo cáo bởi: <span className="font-semibold text-ink">{bug.reporterUsername || 'Hệ thống'}</span></p>
                        <p>Xử lý: <span className="font-semibold text-ink">{bug.assigneeUsername || 'Chưa gán'}</span></p>
                        {bug.resolvedAt && <p>Giải quyết lúc: <span className="font-semibold text-emerald-600">{new Date(bug.resolvedAt).toLocaleDateString()}</span></p>}
                      </div>
                    </button>

                    <div className="flex items-center gap-2 self-end sm:self-center">
                      {/* Assignee Quick Select */}
                      <div className="w-36">
                        <Select
                          aria-label="Người xử lý"
                          value={bug.assigneeUserId || ''}
                          onChange={e => handleAssignChange(bug.id, e.target.value || null)}
                          disabled={saving || bug.status === 'CLOSED' || bug.status === 'CANCELLED'}
                          options={[
                            { label: 'Chưa gán', value: '' },
                            ...members.map(m => ({ label: m.username, value: m.userId }))
                          ]}
                        />
                      </div>

                      {bug.status !== 'CLOSED' && bug.status !== 'CANCELLED' && (
                        <>
                          {/* Status update menu */}
                          {transitions.length > 0 && (
                            <ActionMenu label="Đổi trạng thái">
                              {transitions.map(next => (
                                <ActionItem key={next} onClick={() => handleStatusChange(bug.id, next)}>
                                  Chuyển sang: {bugStatusLabels[next]}
                                </ActionItem>
                              ))}
                            </ActionMenu>
                          )}

                          {/* Actions menu */}
                          <ActionMenu>
                            <ActionItem onClick={() => handleOpenEdit(bug)}>
                              <Pencil size={15} /> Sửa chi tiết
                            </ActionItem>
                            <ActionItem danger onClick={() => setDeleteBugId(bug.id)}>
                              <Trash2 size={15} /> Xóa Bug
                            </ActionItem>
                          </ActionMenu>
                        </>
                      )}
                    </div>
                  </div>

                  {/* Linked task or backlog item info */}
                  {(bug.taskId || bug.backlogItemId) && (
                    <div className="mt-3 pt-3 border-t border-line flex flex-wrap gap-3 text-xs">
                      {bug.backlogItemId && (
                        <div className="flex items-center gap-1.5 text-indigo-600 bg-indigo-50 border border-indigo-100 px-2 py-1 rounded-md">
                          <span className="font-bold">User Story:</span>
                          <span>{getBacklogTitle(bug.backlogItemId)}</span>
                        </div>
                      )}
                      {bug.taskId && (
                        <div className="flex items-center gap-1.5 text-sky-600 bg-sky-50 border border-sky-100 px-2 py-1 rounded-md">
                          <span className="font-bold">Task liên kết:</span>
                          <span>{getTaskTitle(bug.taskId)}</span>
                        </div>
                      )}
                    </div>
                  )}
                </article>
              )
            })}

            {/* Empty state */}
            {bugs.content.length === 0 && !loading && (
              <div className="rounded-2xl border border-dashed border-line bg-white p-12 text-center">
                <BugIcon size={36} className="mx-auto mb-2 text-muted" />
                <p className="text-sm text-muted">Không tìm thấy bản ghi lỗi nào.</p>
              </div>
            )}

            {/* Pagination */}
            {bugs.totalPages > 1 && (
              <footer className="mt-6 flex items-center justify-between">
                <p className="text-sm text-muted">Trang {bugs.number + 1} / {bugs.totalPages}</p>
                <div className="flex gap-2">
                  <Button 
                    variant="secondary" 
                    size="sm" 
                    leadingIcon={<ChevronLeft size={16} />} 
                    disabled={bugs.first} 
                    onClick={() => setPage(p => p - 1)}
                  >
                    Trước
                  </Button>
                  <Button 
                    variant="secondary" 
                    size="sm" 
                    leadingIcon={<ChevronRight size={16} />} 
                    disabled={bugs.last} 
                    onClick={() => setPage(p => p + 1)}
                  >
                    Sau
                  </Button>
                </div>
              </footer>
            )}
          </div>
        </div>
      ) : (
        <div className="space-y-6">
          {/* Date / Filter bar */}
          <div className="rounded-2xl border border-line bg-white p-5 shadow-sm space-y-4">
            <div className="flex flex-col sm:flex-row gap-4 justify-between items-start sm:items-center">
              <div>
                <h3 className="text-base font-bold text-ink flex items-center gap-2">
                  <BugIcon className="text-rose-500" size={20} />
                  Báo cáo & QA Metrics Dự án
                </h3>
                {dashboardData && (
                  <p className="text-xs text-muted mt-1">
                    Dự án: <span className="font-semibold text-ink">{dashboardData.projectName} ({dashboardData.projectCode})</span>
                  </p>
                )}
              </div>
              
              {/* Quick Presets & Export */}
              <div className="flex items-center gap-2 w-full sm:w-auto">
                <Button 
                  variant="secondary" 
                  size="sm" 
                  className="flex-1 sm:flex-none"
                  onClick={() => setReportFilters(prev => ({
                    ...prev,
                    fromDate: new Date(Date.now() - 30 * 24 * 3600 * 1000).toISOString().split('T')[0],
                    toDate: new Date().toISOString().split('T')[0]
                  }))}
                >
                  30 Ngày qua
                </Button>
                <Button 
                  variant="secondary" 
                  size="sm" 
                  className="flex-1 sm:flex-none"
                  onClick={() => setReportFilters(prev => ({
                    ...prev,
                    fromDate: new Date(Date.now() - 90 * 24 * 3600 * 1000).toISOString().split('T')[0],
                    toDate: new Date().toISOString().split('T')[0]
                  }))}
                >
                  90 Ngày qua
                </Button>
                <Button 
                  variant="outline-teal" 
                  size="sm" 
                  leadingIcon={<Download size={15} />}
                  className="flex-1 sm:flex-none"
                  onClick={async () => {
                    try {
                      await exportBugReportExcel(projectId, reportFilters)
                    } catch (err) {
                      console.error(err)
                    }
                  }}
                >
                  Xuất Excel
                </Button>
              </div>
            </div>

            <div className="grid gap-3 grid-cols-1 sm:grid-cols-2 md:grid-cols-4 lg:grid-cols-5">
              <div className="space-y-1">
                <label className="text-[10px] font-bold text-muted uppercase">Từ ngày</label>
                <Input 
                  aria-label="Từ ngày"
                  type="date" 
                  value={reportFilters.fromDate} 
                  onChange={e => setReportFilters(prev => ({ ...prev, fromDate: e.target.value }))}
                />
              </div>
              <div className="space-y-1">
                <label className="text-[10px] font-bold text-muted uppercase">Đến ngày</label>
                <Input 
                  aria-label="Đến ngày"
                  type="date" 
                  value={reportFilters.toDate} 
                  onChange={e => setReportFilters(prev => ({ ...prev, toDate: e.target.value }))}
                />
              </div>
              <div className="space-y-1">
                <label className="text-[10px] font-bold text-muted uppercase">Sprint</label>
                <Select 
                  aria-label="Sprint"
                  value={reportFilters.sprintId || ''} 
                  onChange={e => setReportFilters(prev => ({ ...prev, sprintId: e.target.value }))}
                  options={[
                    { label: 'Tất cả Sprint', value: '' },
                    ...sprints.map(s => ({ label: s.name, value: s.id }))
                  ]}
                />
              </div>
              <div className="space-y-1">
                <label className="text-[10px] font-bold text-muted uppercase">Người xử lý</label>
                <Select 
                  aria-label="Người xử lý"
                  value={reportFilters.assigneeUserId || ''} 
                  onChange={e => setReportFilters(prev => ({ ...prev, assigneeUserId: e.target.value }))}
                  options={[
                    { label: 'Tất cả người xử lý', value: '' },
                    ...members.map(m => ({ label: m.username, value: m.userId }))
                  ]}
                />
              </div>
              <div className="space-y-1 flex items-end">
                <Button 
                  variant="secondary" 
                  className="w-full h-11"
                  onClick={() => setReportFilters({
                    fromDate: new Date(Date.now() - 30 * 24 * 3600 * 1000).toISOString().split('T')[0],
                    toDate: new Date().toISOString().split('T')[0],
                    sprintId: '',
                    assigneeUserId: '',
                    status: '',
                    severity: '',
                    priority: '',
                    overdueOnly: false
                  })}
                >
                  Đặt lại bộ lọc
                </Button>
              </div>
            </div>
          </div>

          {reportLoading ? (
            <div className="rounded-2xl border border-line bg-white p-12 text-center text-muted animate-pulse">
              Đang tải dữ liệu báo cáo QA Metrics...
            </div>
          ) : (
            <div className="space-y-6">
              {/* QA Metrics Panel */}
              {qaMetrics && (
                <div className="grid gap-4 grid-cols-2 lg:grid-cols-4">
                  <div className="rounded-2xl border border-line bg-white p-5 shadow-sm text-center space-y-1">
                    <p className="text-xs font-semibold text-muted uppercase tracking-wider">Tỷ lệ giải quyết (Resolve Rate)</p>
                    <p className="text-3xl font-extrabold text-emerald-600">{(qaMetrics.resolveRate * 100).toFixed(1)}%</p>
                    <p className="text-[11px] text-muted">Đã sửa: {qaMetrics.totalBugsResolved} / {qaMetrics.totalBugsReported}</p>
                  </div>
                  <div className="rounded-2xl border border-line bg-white p-5 shadow-sm text-center space-y-1">
                    <p className="text-xs font-semibold text-muted uppercase tracking-wider">Tỷ lệ Reopen (Reopen Rate)</p>
                    <p className={`text-3xl font-extrabold ${qaMetrics.reopenRate > 0.2 ? 'text-rose-600' : 'text-amber-600'}`}>
                      {(qaMetrics.reopenRate * 100).toFixed(1)}%
                    </p>
                    <p className="text-[11px] text-muted">Bị Reopen: {qaMetrics.totalBugsReopened} lỗi</p>
                  </div>
                  <div className="rounded-2xl border border-line bg-white p-5 shadow-sm text-center space-y-1">
                    <p className="text-xs font-semibold text-muted uppercase tracking-wider">Tỷ lệ lỗi Critical</p>
                    <p className="text-3xl font-extrabold text-rose-600">{(qaMetrics.criticalRate * 100).toFixed(1)}%</p>
                    <p className="text-[11px] text-muted">Số lỗi nghiêm trọng: {qaMetrics.totalCriticalBugs}</p>
                  </div>
                  <div className="rounded-2xl border border-line bg-white p-5 shadow-sm text-center space-y-1">
                    <p className="text-xs font-semibold text-muted uppercase tracking-wider">Tỷ lệ quá hạn (Overdue Rate)</p>
                    <p className="text-3xl font-extrabold text-slate-700">{(qaMetrics.overdueRate * 100).toFixed(1)}%</p>
                    <p className="text-[11px] text-muted">Số lỗi trễ hạn: {qaMetrics.totalOverdueBugs}</p>
                  </div>
                </div>
              )}

              {/* Breakdown distributions */}
              {reportData && (
                <div className="grid gap-6 md:grid-cols-2">
                  
                  {/* Status Breakdown */}
                  <div className="rounded-2xl border border-line bg-white p-5 shadow-sm space-y-4">
                    <h4 className="text-sm font-bold text-ink">Thống kê theo Trạng thái (Bug Status)</h4>
                    <div className="space-y-3">
                      {reportData.byStatus.map(s => {
                        const percent = reportData.totalBugs > 0 ? (s.total / reportData.totalBugs) * 100 : 0
                        return (
                          <div key={s.status} className="space-y-1">
                            <div className="flex justify-between text-xs font-semibold">
                              <span className="text-ink">{bugStatusLabels[s.status]}</span>
                              <span className="text-muted">{s.total} ({percent.toFixed(1)}%)</span>
                            </div>
                            <div className="w-full bg-slate-100 h-2.5 rounded-full overflow-hidden">
                              <div 
                                className={`h-full rounded-full ${
                                  s.status === 'OPEN' ? 'bg-blue-500' :
                                  s.status === 'IN_PROGRESS' ? 'bg-orange-500' :
                                  s.status === 'RESOLVED' ? 'bg-emerald-500' :
                                  s.status === 'REOPENED' ? 'bg-purple-500' :
                                  s.status === 'CLOSED' ? 'bg-slate-500' :
                                  'bg-gray-400'
                                }`}
                                style={{ width: `${percent}%` }}
                              />
                            </div>
                          </div>
                        )
                      })}
                    </div>
                  </div>

                  {/* Severity Breakdown */}
                  <div className="rounded-2xl border border-line bg-white p-5 shadow-sm space-y-4">
                    <h4 className="text-sm font-bold text-ink">Thống kê theo Độ nghiêm trọng (Severity)</h4>
                    <div className="space-y-3">
                      {reportData.bySeverity.map(s => {
                        const percent = reportData.totalBugs > 0 ? (s.total / reportData.totalBugs) * 100 : 0
                        return (
                          <div key={s.severity} className="space-y-1">
                            <div className="flex justify-between text-xs font-semibold">
                              <span className="text-ink">{bugSeverityLabels[s.severity]}</span>
                              <span className="text-muted">{s.total} ({percent.toFixed(1)}%)</span>
                            </div>
                            <div className="w-full bg-slate-100 h-2.5 rounded-full overflow-hidden">
                              <div 
                                className={`h-full rounded-full ${
                                  s.severity === 'CRITICAL' ? 'bg-rose-600 animate-pulse' :
                                  s.severity === 'HIGH' ? 'bg-red-500' :
                                  s.severity === 'MEDIUM' ? 'bg-amber-500' :
                                  'bg-slate-400'
                                }`}
                                style={{ width: `${percent}%` }}
                              />
                            </div>
                          </div>
                        )
                      })}
                    </div>
                  </div>

                  {/* Priority Breakdown */}
                  <div className="rounded-2xl border border-line bg-white p-5 shadow-sm space-y-4">
                    <h4 className="text-sm font-bold text-ink">Thống kê theo Độ ưu tiên (Priority)</h4>
                    <div className="space-y-3">
                      {reportData.byPriority.map(s => {
                        const percent = reportData.totalBugs > 0 ? (s.total / reportData.totalBugs) * 100 : 0
                        return (
                          <div key={s.priority} className="space-y-1">
                            <div className="flex justify-between text-xs font-semibold">
                              <span className="text-ink">{s.priority}</span>
                              <span className="text-muted">{s.total} ({percent.toFixed(1)}%)</span>
                            </div>
                            <div className="w-full bg-slate-100 h-2.5 rounded-full overflow-hidden">
                              <div 
                                className={`h-full rounded-full ${
                                  s.priority === 'URGENT' ? 'bg-rose-500' :
                                  s.priority === 'HIGH' ? 'bg-red-400' :
                                  s.priority === 'MEDIUM' ? 'bg-amber-400' :
                                  'bg-slate-300'
                                }`}
                                style={{ width: `${percent}%` }}
                              />
                            </div>
                          </div>
                        )
                      })}
                    </div>
                  </div>

                  {/* Assignees breakdown / leaderboard */}
                  <div className="rounded-2xl border border-line bg-white p-5 shadow-sm space-y-4 font-semibold">
                    <h4 className="text-sm font-bold text-ink">Phân công lỗi theo Thành viên</h4>
                    <div className="overflow-x-auto">
                      <table className="w-full text-xs text-left border-collapse">
                        <thead>
                          <tr className="border-b border-line text-muted uppercase font-bold text-[10px]">
                            <th className="py-2">Thành viên</th>
                            <th className="py-2 text-center">Tổng số lỗi</th>
                            <th className="py-2 text-center">Đang mở</th>
                            <th className="py-2 text-center">Đã sửa</th>
                            <th className="py-2 text-center text-rose-600">Critical</th>
                          </tr>
                        </thead>
                        <tbody>
                          {reportData.byAssignee.map(u => (
                            <tr key={u.assigneeUserId} className="border-b border-line last:border-0 hover:bg-canvas">
                              <td className="py-2.5 font-semibold text-ink">{u.username || 'Chưa phân công'}</td>
                              <td className="py-2.5 text-center font-bold">{u.totalBugs}</td>
                              <td className="py-2.5 text-center text-blue-600">{u.openBugs}</td>
                              <td className="py-2.5 text-center text-emerald-600">{u.resolvedBugs}</td>
                              <td className="py-2.5 text-center text-rose-600">{u.criticalBugs}</td>
                            </tr>
                          ))}
                          {reportData.byAssignee.length === 0 && (
                            <tr>
                              <td colSpan={5} className="py-4 text-center text-muted">Chưa có gán lỗi nào phát sinh.</td>
                            </tr>
                          )}
                        </tbody>
                      </table>
                    </div>
                  </div>

                </div>
              )}
            </div>
          )}

        </div>
      )}

      {/* Modal Báo cáo Bug mới */}
      <Modal open={createOpen} title="Báo cáo Bug mới (QA)" onClose={() => setCreateOpen(false)} showClose={false}>
        <form onSubmit={handleSaveCreate} className="space-y-4">
          <Input 
            label="Tiêu đề lỗi" 
            required 
            placeholder="Tóm tắt ngắn gọn lỗi phát sinh..."
            value={formTitle}
            onChange={e => setFormTitle(e.target.value)}
          />

          <div className="grid gap-4 sm:grid-cols-2">
            <Select 
              label="Độ nghiêm trọng"
              value={formSeverity}
              onChange={e => setFormSeverity(e.target.value as BugSeverity)}
              options={Object.entries(bugSeverityLabels).map(([k, v]) => ({ label: v, value: k }))}
            />
            <Select 
              label="Độ ưu tiên"
              value={formPriority}
              onChange={e => setFormPriority(e.target.value as any)}
              options={[
                { label: 'Thấp (LOW)', value: 'LOW' },
                { label: 'Trung bình (MEDIUM)', value: 'MEDIUM' },
                { label: 'Cao (HIGH)', value: 'HIGH' },
                { label: 'Khẩn cấp (URGENT)', value: 'URGENT' }
              ]}
            />
          </div>

          <div className="grid gap-4 sm:grid-cols-3">
            <Select 
              label="Người xử lý"
              value={formAssigneeId}
              onChange={e => setFormAssigneeId(e.target.value)}
              options={[
                { label: '-- Chọn thành viên --', value: '' },
                ...members.map(m => ({ label: m.username, value: m.userId }))
              ]}
            />
            <Select 
              label="Liên kết User Story"
              value={formBacklogId}
              onChange={e => setFormBacklogId(e.target.value)}
              options={[
                { label: '-- Không liên kết --', value: '' },
                ...backlogItems.map(b => ({ label: b.title, value: b.id }))
              ]}
            />
            <Select 
              label="Liên kết Task"
              value={formTaskId}
              onChange={e => setFormTaskId(e.target.value)}
              options={[
                { label: '-- Không liên kết --', value: '' },
                ...tasks
                  .filter(t => !formBacklogId || t.backlogItemId === formBacklogId)
                  .map(t => ({ label: t.title, value: t.id }))
              ]}
            />
          </div>

          <div className="space-y-1">
            <label className="text-xs font-semibold text-muted uppercase">Mô tả lỗi</label>
            <textarea 
              className="min-h-20 w-full rounded-lg border border-line bg-white px-3.5 py-3 text-sm outline-none focus:border-brand focus:ring-2 focus:ring-brand/20"
              placeholder="Chi tiết về môi trường, hệ điều hành..."
              value={formDesc}
              onChange={e => setFormDesc(e.target.value)}
            />
          </div>

          <div className="space-y-1">
            <label className="text-xs font-semibold text-muted uppercase">Các bước tái hiện (Reproduction Steps)</label>
            <textarea 
              className="min-h-24 w-full rounded-lg border border-line bg-white px-3.5 py-3 text-sm outline-none focus:border-brand focus:ring-2 focus:ring-brand/20"
              placeholder="1. Vào trang đăng nhập&#10;2. Bỏ trống mật khẩu&#10;3. Click Đăng nhập..."
              value={formSteps}
              onChange={e => setFormSteps(e.target.value)}
            />
          </div>

          <div className="grid gap-4 sm:grid-cols-2">
            <div className="space-y-1">
              <label className="text-xs font-semibold text-muted uppercase">Kết quả mong muốn</label>
              <textarea 
                className="min-h-20 w-full rounded-lg border border-line bg-white px-3.5 py-3 text-sm outline-none focus:border-brand focus:ring-2 focus:ring-brand/20"
                placeholder="Nên báo lỗi đỏ..."
                value={formExpected}
                onChange={e => setFormExpected(e.target.value)}
              />
            </div>
            <div className="space-y-1">
              <label className="text-xs font-semibold text-muted uppercase">Kết quả thực tế</label>
              <textarea 
                className="min-h-20 w-full rounded-lg border border-line bg-white px-3.5 py-3 text-sm outline-none focus:border-brand focus:ring-2 focus:ring-brand/20"
                placeholder="Trang web bị crash trắng màn hình..."
                value={formActual}
                onChange={e => setFormActual(e.target.value)}
              />
            </div>
          </div>

          <div className="flex justify-end gap-3 pt-2">
            <Button type="button" variant="secondary" onClick={() => setCreateOpen(false)}>Hủy</Button>
            <Button type="submit" loading={saving}>Báo cáo Bug</Button>
          </div>
        </form>
      </Modal>

      {/* Modal Chỉnh sửa Bug */}
      <Modal open={Boolean(editBug)} title="Chỉnh sửa thông tin Bug (QA)" onClose={() => setEditBug(null)} showClose={false}>
        <form onSubmit={handleSaveEdit} className="space-y-4">
          <Input 
            label="Tiêu đề lỗi" 
            required 
            placeholder="Tóm tắt ngắn gọn lỗi phát sinh..."
            value={formTitle}
            onChange={e => setFormTitle(e.target.value)}
          />

          <div className="grid gap-4 sm:grid-cols-2">
            <Select 
              label="Độ nghiêm trọng"
              value={formSeverity}
              onChange={e => setFormSeverity(e.target.value as BugSeverity)}
              options={Object.entries(bugSeverityLabels).map(([k, v]) => ({ label: v, value: k }))}
            />
            <Select 
              label="Độ ưu tiên"
              value={formPriority}
              onChange={e => setFormPriority(e.target.value as any)}
              options={[
                { label: 'Thấp (LOW)', value: 'LOW' },
                { label: 'Trung bình (MEDIUM)', value: 'MEDIUM' },
                { label: 'Cao (HIGH)', value: 'HIGH' },
                { label: 'Khẩn cấp (URGENT)', value: 'URGENT' }
              ]}
            />
          </div>

          <div className="grid gap-4 sm:grid-cols-3">
            <Select 
              label="Người xử lý"
              value={formAssigneeId}
              onChange={e => setFormAssigneeId(e.target.value)}
              options={[
                { label: '-- Chọn thành viên --', value: '' },
                ...members.map(m => ({ label: m.username, value: m.userId }))
              ]}
            />
            <Select 
              label="Liên kết User Story"
              value={formBacklogId}
              onChange={e => setFormBacklogId(e.target.value)}
              options={[
                { label: '-- Không liên kết --', value: '' },
                ...backlogItems.map(b => ({ label: b.title, value: b.id }))
              ]}
            />
            <Select 
              label="Liên kết Task"
              value={formTaskId}
              onChange={e => setFormTaskId(e.target.value)}
              options={[
                { label: '-- Không liên kết --', value: '' },
                ...tasks
                  .filter(t => !formBacklogId || t.backlogItemId === formBacklogId)
                  .map(t => ({ label: t.title, value: t.id }))
              ]}
            />
          </div>

          <div className="space-y-1">
            <label className="text-xs font-semibold text-muted uppercase">Mô tả lỗi</label>
            <textarea 
              className="min-h-20 w-full rounded-lg border border-line bg-white px-3.5 py-3 text-sm outline-none focus:border-brand focus:ring-2 focus:ring-brand/20"
              placeholder="Chi tiết về môi trường, hệ điều hành..."
              value={formDesc}
              onChange={e => setFormDesc(e.target.value)}
            />
          </div>

          <div className="space-y-1">
            <label className="text-xs font-semibold text-muted uppercase">Các bước tái hiện (Reproduction Steps)</label>
            <textarea 
              className="min-h-24 w-full rounded-lg border border-line bg-white px-3.5 py-3 text-sm outline-none focus:border-brand focus:ring-2 focus:ring-brand/20"
              placeholder="1. Vào trang đăng nhập&#10;2. Bỏ trống mật khẩu&#10;3. Click Đăng nhập..."
              value={formSteps}
              onChange={e => setFormSteps(e.target.value)}
            />
          </div>

          <div className="grid gap-4 sm:grid-cols-2">
            <div className="space-y-1">
              <label className="text-xs font-semibold text-muted uppercase">Kết quả mong muốn</label>
              <textarea 
                className="min-h-20 w-full rounded-lg border border-line bg-white px-3.5 py-3 text-sm outline-none focus:border-brand focus:ring-2 focus:ring-brand/20"
                placeholder="Nên báo lỗi đỏ..."
                value={formExpected}
                onChange={e => setFormExpected(e.target.value)}
              />
            </div>
            <div className="space-y-1">
              <label className="text-xs font-semibold text-muted uppercase">Kết quả thực tế</label>
              <textarea 
                className="min-h-20 w-full rounded-lg border border-line bg-white px-3.5 py-3 text-sm outline-none focus:border-brand focus:ring-2 focus:ring-brand/20"
                placeholder="Trang web bị crash trắng màn hình..."
                value={formActual}
                onChange={e => setFormActual(e.target.value)}
              />
            </div>
          </div>

          <div className="flex justify-end gap-3 pt-2">
            <Button type="button" variant="secondary" onClick={() => setEditBug(null)}>Hủy</Button>
            <Button type="submit" loading={saving}>Lưu thay đổi</Button>
          </div>
        </form>
      </Modal>

      {/* Detail Modal for Bug (QA Collaboration) */}
      {selectedBugDetails && (
        <BugDetailModal 
          bug={selectedBugDetails}
          projectId={projectId}
          members={members}
          backlogItems={backlogItems}
          tasks={tasks}
          onClose={() => {
            setSelectedBugDetails(null)
            onCloseBug?.()
          }}
          onStatusChange={handleStatusChange}
          onSeverityChange={handleSeverityChange}
          onPriorityChange={handlePriorityChange}
          onAssigneeChange={handleAssignChange}
        />
      )}

      {/* Confirm delete bug dialog */}
      <ConfirmDialog 
        open={Boolean(deleteBugId)}
        title="Xóa báo cáo Bug?"
        description="Báo cáo lỗi này sẽ bị xóa vĩnh viễn khỏi Project QA. Hành động này không thể hoàn tác."
        confirmLabel="Xóa Bug"
        loading={saving}
        onCancel={() => setDeleteBugId(null)}
        onConfirm={handleDelete}
      />
    </div>
  )
}

/* ==========================================
   BugDetailModal (Comments, Evidences, Specs)
   ========================================== */
interface BugDetailModalProps {
  bug: Bug
  projectId: string
  members: ProjectMember[]
  backlogItems: BacklogItem[]
  tasks: (Task | KanbanTask)[]
  onClose: () => void
  onStatusChange: (bugId: string, nextStatus: BugStatus) => void
  onSeverityChange: (bugId: string, nextSeverity: BugSeverity) => void
  onPriorityChange: (bugId: string, nextPriority: TaskPriority) => void
  onAssigneeChange: (bugId: string, nextAssigneeId: string | null) => void
}

function BugDetailModal({ 
  bug, 
  projectId, 
  members,
  backlogItems, 
  tasks,
  onClose,
  onStatusChange,
  onSeverityChange,
  onPriorityChange,
  onAssigneeChange
}: BugDetailModalProps) {
  const [activeSubTab, setActiveSubTab] = useState<'info' | 'comments' | 'evidences' | 'attachments'>('info')
  const [comments, setComments] = useState<BugComment[]>([])
  const [evidences, setEvidences] = useState<BugEvidence[]>([])
  const [commentsLoading, setCommentsLoading] = useState(false)
  const [evidencesLoading, setEvidencesLoading] = useState(false)

  // Comments state
  const [commentInput, setCommentInput] = useState('')
  const [replyToId, setReplyToId] = useState<string | null>(null)
  const [replyInput, setReplyInput] = useState('')
  const [editingCommentId, setEditingCommentId] = useState<string | null>(null)
  const [editCommentInput, setEditCommentInput] = useState('')

  // Evidence forms state
  const [evidenceAddOpen, setEvidenceAddOpen] = useState(false)
  const [editingEvidence, setEditingEvidence] = useState<BugEvidence | null>(null)
  const [evidenceTitle, setEvidenceTitle] = useState('')
  const [evidenceSteps, setEvidenceSteps] = useState('')
  const [evidenceExpected, setEvidenceExpected] = useState('')
  const [evidenceActual, setEvidenceActual] = useState('')
  const [evidenceEnv, setEvidenceEnv] = useState('')
  const [evidenceNote, setEvidenceNote] = useState('')

  // Confirm delete comments/evidence states
  const [confirmDeleteCommentId, setConfirmDeleteCommentId] = useState<string | null>(null)
  const [confirmDeleteEvidenceId, setConfirmDeleteEvidenceId] = useState<string | null>(null)

  const handleLoadComments = useCallback(async () => {
    setCommentsLoading(true)
    try {
      const data = await getBugComments(projectId, bug.id)
      setComments(data)
    } catch (err) {
      console.error(err)
    } finally {
      setCommentsLoading(false)
    }
  }, [projectId, bug.id])

  const handleLoadEvidences = useCallback(async () => {
    setEvidencesLoading(true)
    try {
      const data = await getBugEvidences(projectId, bug.id)
      setEvidences(data)
    } catch (err) {
      console.error(err)
    } finally {
      setEvidencesLoading(false)
    }
  }, [projectId, bug.id])

  useEffect(() => {
    handleLoadComments()
    handleLoadEvidences()
  }, [handleLoadComments, handleLoadEvidences])

  // Nesting replies helper (1 level of nesting maximum)
  const nestedComments = useMemo(() => {
    const roots = comments.filter(c => !c.parentId)
    return roots.map(root => ({
      ...root,
      replies: comments.filter(c => c.parentId === root.id)
    }))
  }, [comments])

  // Comment Actions
  const handleAddCommentSubmit = async (event: React.FormEvent) => {
    event.preventDefault()
    if (!commentInput.trim()) return
    try {
      await createBugComment(projectId, bug.id, { content: commentInput })
      setCommentInput('')
      handleLoadComments()
    } catch (err) {
      console.error(err)
    }
  }

  const handleAddReplySubmit = async (parentId: string) => {
    if (!replyInput.trim()) return
    try {
      await createBugComment(projectId, bug.id, { content: replyInput, parentId })
      setReplyToId(null)
      setReplyInput('')
      handleLoadComments()
    } catch (err) {
      console.error(err)
    }
  }

  const handleSaveCommentEdit = async (commentId: string) => {
    if (!editCommentInput.trim()) return
    try {
      await updateBugComment(projectId, bug.id, commentId, editCommentInput)
      setEditingCommentId(null)
      setEditCommentInput('')
      handleLoadComments()
    } catch (err) {
      console.error(err)
    }
  }

  const handleDeleteComment = async () => {
    if (!confirmDeleteCommentId) return
    try {
      await deleteBugComment(projectId, bug.id, confirmDeleteCommentId)
      setConfirmDeleteCommentId(null)
      handleLoadComments()
    } catch (err) {
      console.error(err)
    }
  }

  // Evidence Actions
  const handleOpenAddEvidence = () => {
    setEvidenceTitle('')
    setEvidenceSteps('')
    setEvidenceExpected('')
    setEvidenceActual('')
    setEvidenceEnv('')
    setEvidenceNote('')
    setEvidenceAddOpen(true)
  }

  const handleAddEvidenceSubmit = async (event: React.FormEvent) => {
    event.preventDefault()
    if (!evidenceTitle.trim()) return
    try {
      await createBugEvidence(projectId, bug.id, {
        title: evidenceTitle,
        stepsToReproduce: evidenceSteps || null,
        expectedResult: evidenceExpected || null,
        actualResult: evidenceActual || null,
        environment: evidenceEnv || null,
        note: evidenceNote || null
      })
      setEvidenceAddOpen(false)
      handleLoadEvidences()
    } catch (err) {
      console.error(err)
    }
  }

  const handleOpenEditEvidence = (ev: BugEvidence) => {
    setEditingEvidence(ev)
    setEvidenceTitle(ev.title)
    setEvidenceSteps(ev.stepsToReproduce || '')
    setEvidenceExpected(ev.expectedResult || '')
    setEvidenceActual(ev.actualResult || '')
    setEvidenceEnv(ev.environment || '')
    setEvidenceNote(ev.note || '')
  }

  const handleSaveEvidenceEdit = async (event: React.FormEvent) => {
    event.preventDefault()
    if (!editingEvidence || !evidenceTitle.trim()) return
    try {
      await updateBugEvidence(projectId, bug.id, editingEvidence.id, {
        title: evidenceTitle,
        stepsToReproduce: evidenceSteps || null,
        expectedResult: evidenceExpected || null,
        actualResult: evidenceActual || null,
        environment: evidenceEnv || null,
        note: evidenceNote || null
      })
      setEditingEvidence(null)
      handleLoadEvidences()
    } catch (err) {
      console.error(err)
    }
  }

  const handleDeleteEvidence = async () => {
    if (!confirmDeleteEvidenceId) return
    try {
      await deleteBugEvidence(projectId, bug.id, confirmDeleteEvidenceId)
      setConfirmDeleteEvidenceId(null)
      handleLoadEvidences()
    } catch (err) {
      console.error(err)
    }
  }

  // Helpers
  const getTaskTitle = (taskId: string | null) => {
    if (!taskId) return null
    const found = tasks.find(t => t.id === taskId)
    return found ? found.title : 'Task không rõ'
  }

  const getBacklogTitle = (backlogId: string | null) => {
    if (!backlogId) return null
    const found = backlogItems.find(b => b.id === backlogId)
    return found ? found.title : 'User Story không rõ'
  }

  const transitions = getValidTransitions(bug.status)

  return (
    <Modal open={true} title={`Chi tiết Bug: ${bug.title}`} onClose={onClose} showClose={true}>
      <div className="space-y-5">
        
        {/* Info badges header */}
        <div className="flex flex-wrap items-center gap-3 p-4 rounded-xl border border-line bg-canvas">
          <div className="flex items-center gap-1.5 text-xs text-muted">
            <span className="font-bold">Mã lỗi:</span>
            <span className="bg-white border px-2 py-0.5 rounded font-bold text-ink">BUG-{bug.id.substring(0, 5).toUpperCase()}</span>
          </div>
          
          <div className="flex items-center gap-1.5 text-xs text-muted">
            <span className="font-bold">Nghiêm trọng:</span>
            <Select
              aria-label="Độ nghiêm trọng"
              value={bug.severity}
              onChange={e => onSeverityChange(bug.id, e.target.value as BugSeverity)}
              disabled={bug.status === 'CLOSED' || bug.status === 'CANCELLED'}
              options={[
                { label: 'Thấp (LOW)', value: 'LOW' },
                { label: 'Trung bình (MEDIUM)', value: 'MEDIUM' },
                { label: 'Cao (HIGH)', value: 'HIGH' },
                { label: 'Khẩn cấp (CRITICAL)', value: 'CRITICAL' }
              ]}
              className="!h-8 !py-1 text-xs !w-28"
            />
          </div>

          <div className="flex items-center gap-1.5 text-xs text-muted">
            <span className="font-bold">Độ ưu tiên:</span>
            <Select
              aria-label="Độ ưu tiên"
              value={bug.priority}
              onChange={e => onPriorityChange(bug.id, e.target.value as TaskPriority)}
              disabled={bug.status === 'CLOSED' || bug.status === 'CANCELLED'}
              options={[
                { label: 'Thấp (LOW)', value: 'LOW' },
                { label: 'Trung bình (MEDIUM)', value: 'MEDIUM' },
                { label: 'Cao (HIGH)', value: 'HIGH' },
                { label: 'Khẩn cấp (URGENT)', value: 'URGENT' }
              ]}
              className="!h-8 !py-1 text-xs !w-28"
            />
          </div>

          <div className="flex items-center gap-1.5 text-xs text-muted">
            <span className="font-bold">Người xử lý:</span>
            <Select
              aria-label="Người xử lý"
              value={bug.assigneeUserId || ''}
              onChange={e => onAssigneeChange(bug.id, e.target.value || null)}
              disabled={bug.status === 'CLOSED' || bug.status === 'CANCELLED'}
              options={[
                { label: 'Chưa gán', value: '' },
                ...members.map(m => ({ label: m.username, value: m.userId }))
              ]}
              className="!h-8 !py-1 text-xs !w-32"
            />
          </div>

          <div className="flex items-center gap-1.5 text-xs text-muted">
            <span className="font-bold">Trạng thái:</span>
            <span className={`px-2.5 py-0.5 rounded-full font-semibold border ${getStatusBadgeClass(bug.status)}`}>
              {bugStatusLabels[bug.status]}
            </span>
          </div>
          
          <div className="flex-1" />

          {/* Quick status transitions */}
          {transitions.length > 0 && (
            <div className="flex items-center gap-2">
              <span className="text-xs text-muted">Chuyển sang:</span>
              <div className="flex gap-1.5">
                {transitions.map(status => (
                  <Button 
                    key={status} 
                    variant="outline-teal" 
                    size="sm" 
                    onClick={() => onStatusChange(bug.id, status)}
                  >
                    {bugStatusLabels[status]}
                  </Button>
                ))}
              </div>
            </div>
          )}
        </div>

        {/* Detail Tabs */}
        <div className="border-b border-line flex gap-2">
          <button 
            type="button"
            onClick={() => setActiveSubTab('info')}
            className={`px-4 py-2.5 border-b-2 text-sm font-bold flex items-center gap-2 transition ${
              activeSubTab === 'info' ? 'border-brand text-brand' : 'border-transparent text-muted hover:text-ink'
            }`}
          >
            <FileText size={16} /> Mô tả & Tái hiện
          </button>
          <button 
            type="button"
            onClick={() => setActiveSubTab('evidences')}
            className={`px-4 py-2.5 border-b-2 text-sm font-bold flex items-center gap-2 transition ${
              activeSubTab === 'evidences' ? 'border-brand text-brand' : 'border-transparent text-muted hover:text-ink'
            }`}
          >
            <CheckSquare size={16} /> Bằng chứng QA ({evidences.length})
          </button>
          <button 
            type="button"
            onClick={() => setActiveSubTab('comments')}
            className={`px-4 py-2.5 border-b-2 text-sm font-bold flex items-center gap-2 transition ${
              activeSubTab === 'comments' ? 'border-brand text-brand' : 'border-transparent text-muted hover:text-ink'
            }`}
          >
            <MessageSquare size={16} /> Bình luận ({comments.length})
          </button>
          <button 
            type="button"
            onClick={() => setActiveSubTab('attachments')}
            className={`px-4 py-2.5 border-b-2 text-sm font-bold flex items-center gap-2 transition ${
              activeSubTab === 'attachments' ? 'border-brand text-brand' : 'border-transparent text-muted hover:text-ink'
            }`}
          >
            <Paperclip size={16} /> Tệp đính kèm
          </button>
        </div>

        {/* Tab Content */}
        <div className="min-h-72">
          
          {/* Tab 1: Info & Specs */}
          {activeSubTab === 'info' && (
            <div className="space-y-4">
              <div className="rounded-xl border border-line bg-white p-4 space-y-3">
                <h4 className="text-xs font-bold text-muted uppercase tracking-wider">Mô tả lỗi (Description)</h4>
                <p className="text-sm text-ink whitespace-pre-wrap">{bug.description || 'Không có mô tả chi tiết.'}</p>
              </div>

              <div className="rounded-xl border border-line bg-white p-4 space-y-3">
                <h4 className="text-xs font-bold text-muted uppercase tracking-wider">Các bước tái hiện (Reproduction Steps)</h4>
                <p className="text-sm text-ink bg-slate-50 p-3 rounded-lg font-mono text-xs whitespace-pre-wrap">
                  {bug.reproductionSteps || 'Chưa cung cấp các bước tái hiện.'}
                </p>
              </div>

              <div className="grid gap-4 sm:grid-cols-2">
                <div className="rounded-xl border border-line bg-white p-4 space-y-2">
                  <h4 className="text-xs font-bold text-emerald-700 uppercase tracking-wider">Kết quả mong muốn (Expected Result)</h4>
                  <p className="text-sm text-ink whitespace-pre-wrap">{bug.expectedResult || 'Chưa định nghĩa kết quả mong muốn.'}</p>
                </div>
                <div className="rounded-xl border border-line bg-white p-4 space-y-2">
                  <h4 className="text-xs font-bold text-rose-700 uppercase tracking-wider">Kết quả thực tế (Actual Result)</h4>
                  <p className="text-sm text-ink whitespace-pre-wrap">{bug.actualResult || 'Chưa cung cấp kết quả thực tế xảy ra.'}</p>
                </div>
              </div>

              {/* Links metadata */}
              {(bug.backlogItemId || bug.taskId) && (
                <div className="rounded-xl border border-line bg-white p-4 space-y-3">
                  <h4 className="text-xs font-bold text-muted uppercase tracking-wider font-bold">Liên kết nghiệp vụ</h4>
                  <div className="flex flex-wrap gap-3">
                    {bug.backlogItemId && (
                      <div className="flex items-center gap-1.5 text-xs text-indigo-700 bg-indigo-50 px-3 py-1.5 rounded-lg border border-indigo-100">
                        <span className="font-bold">User Story:</span>
                        <span>{getBacklogTitle(bug.backlogItemId)}</span>
                      </div>
                    )}
                    {bug.taskId && (
                      <div className="flex items-center gap-1.5 text-xs text-sky-700 bg-sky-50 px-3 py-1.5 rounded-lg border border-sky-100">
                        <span className="font-bold">Task:</span>
                        <span>{getTaskTitle(bug.taskId)}</span>
                      </div>
                    )}
                  </div>
                </div>
              )}
            </div>
          )}

          {/* Tab 2: Evidences QA */}
          {activeSubTab === 'evidences' && (
            <div className="space-y-4">
              <div className="flex justify-between items-center">
                <h4 className="text-sm font-bold text-ink">Bằng chứng tái hiện lỗi ({evidences.length})</h4>
                <Button size="sm" leadingIcon={<Plus size={15} />} onClick={handleOpenAddEvidence}>
                  Thêm bằng chứng
                </Button>
              </div>

              {/* Evidence Form Open */}
              {evidenceAddOpen && (
                <form onSubmit={handleAddEvidenceSubmit} className="border border-brand/20 bg-brand/5 p-4 rounded-xl space-y-3">
                  <h5 className="text-xs font-bold text-brand uppercase">Khai báo bằng chứng mới</h5>
                  <Input 
                    label="Tiêu đề bằng chứng" 
                    required 
                    value={evidenceTitle} 
                    onChange={e => setEvidenceTitle(e.target.value)} 
                    placeholder="Lỗi xảy ra trên môi trường Production khi..."
                  />
                  <div className="grid gap-3 sm:grid-cols-2">
                    <Input label="Môi trường (Environment)" value={evidenceEnv} onChange={e => setEvidenceEnv(e.target.value)} placeholder="Chrome v120 / iOS 17" />
                    <Input label="Ghi chú thêm" value={evidenceNote} onChange={e => setEvidenceNote(e.target.value)} placeholder="Ghi chú tùy chọn..." />
                  </div>
                  <div className="space-y-1">
                    <label className="text-xs font-semibold text-muted uppercase">Các bước thực hiện</label>
                    <textarea className="min-h-16 w-full rounded-lg border border-line bg-white px-3.5 py-2 text-sm outline-none focus:border-brand" value={evidenceSteps} onChange={e => setEvidenceSteps(e.target.value)} />
                  </div>
                  <div className="grid gap-3 sm:grid-cols-2">
                    <div className="space-y-1">
                      <label className="text-xs font-semibold text-muted uppercase">Kết quả mong muốn</label>
                      <textarea className="min-h-16 w-full rounded-lg border border-line bg-white px-3.5 py-2 text-sm outline-none focus:border-brand" value={evidenceExpected} onChange={e => setEvidenceExpected(e.target.value)} />
                    </div>
                    <div className="space-y-1">
                      <label className="text-xs font-semibold text-muted uppercase">Kết quả thực tế</label>
                      <textarea className="min-h-16 w-full rounded-lg border border-line bg-white px-3.5 py-2 text-sm outline-none focus:border-brand" value={evidenceActual} onChange={e => setEvidenceActual(e.target.value)} />
                    </div>
                  </div>
                  <div className="flex justify-end gap-2">
                    <Button size="sm" type="button" variant="secondary" onClick={() => setEvidenceAddOpen(false)}>Hủy</Button>
                    <Button size="sm" type="submit">Lưu bằng chứng</Button>
                  </div>
                </form>
              )}

              {/* Editing Evidence Form */}
              {editingEvidence && (
                <form onSubmit={handleSaveEvidenceEdit} className="border border-amber-200 bg-amber-50/50 p-4 rounded-xl space-y-3">
                  <h5 className="text-xs font-bold text-amber-700 uppercase">Cập nhật bằng chứng</h5>
                  <Input label="Tiêu đề" required value={evidenceTitle} onChange={e => setEvidenceTitle(e.target.value)} />
                  <div className="grid gap-3 sm:grid-cols-2">
                    <Input label="Môi trường" value={evidenceEnv} onChange={e => setEvidenceEnv(e.target.value)} />
                    <Input label="Ghi chú" value={evidenceNote} onChange={e => setEvidenceNote(e.target.value)} />
                  </div>
                  <div className="space-y-1">
                    <label className="text-xs font-semibold text-muted uppercase">Các bước thực hiện</label>
                    <textarea className="min-h-16 w-full rounded-lg border border-line bg-white px-3.5 py-2 text-sm outline-none" value={evidenceSteps} onChange={e => setEvidenceSteps(e.target.value)} />
                  </div>
                  <div className="grid gap-3 sm:grid-cols-2">
                    <div className="space-y-1">
                      <label className="text-xs font-semibold text-muted uppercase">Kết quả mong muốn</label>
                      <textarea className="min-h-16 w-full rounded-lg border border-line bg-white px-3.5 py-2 text-sm outline-none" value={evidenceExpected} onChange={e => setEvidenceExpected(e.target.value)} />
                    </div>
                    <div className="space-y-1">
                      <label className="text-xs font-semibold text-muted uppercase">Kết quả thực tế</label>
                      <textarea className="min-h-16 w-full rounded-lg border border-line bg-white px-3.5 py-2 text-sm outline-none" value={evidenceActual} onChange={e => setEvidenceActual(e.target.value)} />
                    </div>
                  </div>
                  <div className="flex justify-end gap-2">
                    <Button size="sm" type="button" variant="secondary" onClick={() => setEditingEvidence(null)}>Hủy</Button>
                    <Button size="sm" type="submit" variant="outline-teal">Lưu thay đổi</Button>
                  </div>
                </form>
              )}

              {/* Evidence List */}
              <div className="space-y-3">
                {evidences.map(ev => (
                  <div key={ev.id} className="rounded-xl border border-line bg-white p-4 space-y-3 relative hover:border-brand/40 transition">
                    <div className="flex justify-between items-start">
                      <div>
                        <h5 className="font-bold text-ink text-sm">{ev.title}</h5>
                        <p className="text-[11px] text-muted">Khai báo bởi: <span className="font-semibold">{ev.createdByUsername}</span> · {new Date(ev.createdAt).toLocaleString()}</p>
                      </div>
                      
                      {(ev.canEdit || ev.canDelete) && (
                        <ActionMenu>
                          {ev.canEdit && (
                            <ActionItem onClick={() => handleOpenEditEvidence(ev)}>
                              <Pencil size={14} /> Sửa bằng chứng
                            </ActionItem>
                          )}
                          {ev.canDelete && (
                            <ActionItem danger onClick={() => setConfirmDeleteEvidenceId(ev.id)}>
                              <Trash2 size={14} /> Xóa bằng chứng
                            </ActionItem>
                          )}
                        </ActionMenu>
                      )}
                    </div>

                    {ev.environment && (
                      <div className="text-xs bg-slate-100 px-2.5 py-1 rounded inline-block text-muted font-mono">
                        Môi trường: {ev.environment}
                      </div>
                    )}

                    <div className="grid gap-3 sm:grid-cols-2 text-xs">
                      {ev.stepsToReproduce && (
                        <div className="bg-canvas p-2.5 rounded-lg border border-line">
                          <span className="font-bold text-muted uppercase text-[9px]">Các bước</span>
                          <p className="mt-1 whitespace-pre-wrap">{ev.stepsToReproduce}</p>
                        </div>
                      )}
                      <div className="space-y-2">
                        {ev.expectedResult && (
                          <div className="bg-emerald-50/50 p-2.5 rounded-lg border border-emerald-100">
                            <span className="font-bold text-emerald-700 uppercase text-[9px]">Mong muốn</span>
                            <p className="mt-1 whitespace-pre-wrap">{ev.expectedResult}</p>
                          </div>
                        )}
                        {ev.actualResult && (
                          <div className="bg-rose-50/50 p-2.5 rounded-lg border border-rose-100">
                            <span className="font-bold text-rose-700 uppercase text-[9px]">Thực tế xảy ra</span>
                            <p className="mt-1 whitespace-pre-wrap">{ev.actualResult}</p>
                          </div>
                        )}
                      </div>
                    </div>

                    {ev.note && (
                      <p className="text-xs text-muted italic border-l-2 border-line pl-2 mt-2">Ghi chú: {ev.note}</p>
                    )}
                  </div>
                ))}

                {evidences.length === 0 && !evidencesLoading && (
                  <p className="text-center text-sm text-muted py-8">Chưa có bằng chứng QA nào được khai báo cho lỗi này.</p>
                )}
              </div>
            </div>
          )}

          {/* Tab 3: Bug Comments */}
          {activeSubTab === 'comments' && (
            <div className="space-y-4">
              <h4 className="text-sm font-bold text-ink">Thảo luận & Bình luận lỗi</h4>

              {/* Add main comment form */}
              <form onSubmit={handleAddCommentSubmit} className="flex gap-2">
                <Input 
                  aria-label="Nhập bình luận"
                  value={commentInput}
                  onChange={e => setCommentInput(e.target.value)}
                  placeholder="Nhập ý kiến đóng góp của bạn về Bug này..."
                  className="flex-1"
                />
                <Button type="submit">Gửi</Button>
              </form>

              {/* Comment Thread List */}
              <div className="space-y-3">
                {nestedComments.map(comment => (
                  <div key={comment.id} className="rounded-xl border border-line p-3 space-y-2 bg-white">
                    <div className="flex justify-between items-start">
                      <div>
                        <p className="text-xs font-bold text-ink">{comment.authorUsername}</p>
                        {editingCommentId === comment.id ? (
                          <div className="flex gap-2 mt-1.5">
                            <Input value={editCommentInput} onChange={e => setEditCommentInput(e.target.value)} />
                            <Button size="sm" onClick={() => handleSaveCommentEdit(comment.id)}>Lưu</Button>
                            <Button size="sm" variant="secondary" onClick={() => setEditingCommentId(null)}>Hủy</Button>
                          </div>
                        ) : (
                          <p className="text-sm text-muted mt-0.5">{comment.content}</p>
                        )}
                        <span className="text-[10px] text-muted">{new Date(comment.createdAt).toLocaleString()}</span>
                      </div>
                      
                      <div className="flex items-center gap-1.5 font-semibold text-brand">
                        <Button size="sm" variant="ghost" onClick={() => { setReplyToId(comment.id); setReplyInput('') }}>
                          Trả lời
                        </Button>
                        
                        {(comment.canEdit || comment.canDelete) && (
                          <ActionMenu>
                            {comment.canEdit && (
                              <ActionItem onClick={() => { setEditingCommentId(comment.id); setEditCommentInput(comment.content) }}>
                                Sửa bình luận
                              </ActionItem>
                            )}
                            {comment.canDelete && (
                              <ActionItem danger onClick={() => setConfirmDeleteCommentId(comment.id)}>
                                Xóa bình luận
                              </ActionItem>
                            )}
                          </ActionMenu>
                        )}
                      </div>
                    </div>

                    {/* Reply Form */}
                    {replyToId === comment.id && (
                      <div className="ml-6 flex gap-2 border-l-2 border-line pl-3 py-1">
                        <Input 
                          aria-label="Nhập câu trả lời"
                          value={replyInput} 
                          onChange={e => setReplyInput(e.target.value)} 
                          placeholder="Viết câu trả lời..." 
                          className="flex-1 !h-9 !py-1 text-xs" 
                        />
                        <Button size="sm" onClick={() => handleAddReplySubmit(comment.id)}>Gửi</Button>
                        <Button size="sm" variant="secondary" onClick={() => setReplyToId(null)}>Hủy</Button>
                      </div>
                    )}

                    {/* Nested Replies */}
                    {comment.replies && comment.replies.length > 0 && (
                      <div className="ml-6 space-y-2 border-l-2 border-line pl-3">
                        {comment.replies.map(reply => (
                          <div key={reply.id} className="bg-canvas p-2.5 rounded-lg flex justify-between items-start">
                            <div>
                              <p className="text-xs font-bold text-ink">{reply.authorUsername}</p>
                              {editingCommentId === reply.id ? (
                                <div className="flex gap-2 mt-1">
                                  <Input value={editCommentInput} onChange={e => setEditCommentInput(e.target.value)} />
                                  <Button size="sm" onClick={() => handleSaveCommentEdit(reply.id)}>Lưu</Button>
                                  <Button size="sm" variant="secondary" onClick={() => setEditingCommentId(null)}>Hủy</Button>
                                </div>
                              ) : (
                                <p className="text-xs text-muted mt-0.5">{reply.content}</p>
                              )}
                              <span className="text-[9px] text-muted">{new Date(reply.createdAt).toLocaleString()}</span>
                            </div>
                            
                            {(reply.canEdit || reply.canDelete) && (
                              <ActionMenu>
                                {reply.canEdit && (
                                  <ActionItem onClick={() => { setEditingCommentId(reply.id); setEditCommentInput(reply.content) }}>
                                    Sửa câu trả lời
                                  </ActionItem>
                                )}
                                {reply.canDelete && (
                                  <ActionItem danger onClick={() => setConfirmDeleteCommentId(reply.id)}>
                                    Xóa câu trả lời
                                  </ActionItem>
                                )}
                              </ActionMenu>
                            )}
                          </div>
                        ))}
                      </div>
                    )}
                  </div>
                ))}

                {comments.length === 0 && !commentsLoading && (
                  <p className="text-center text-sm text-muted py-8">Chưa có bình luận nào. Hãy bắt đầu cuộc thảo luận!</p>
                )}
              </div>
            </div>
          )}

          {/* Tab 4: Attachments */}
          {activeSubTab === 'attachments' && (
            <AttachmentSection
              projectId={projectId}
              entityType="BUG"
              entityId={bug.id}
              isEditable={bug.status !== 'CLOSED' && bug.status !== 'CANCELLED'}
            />
          )}

        </div>
      </div>

      {/* Nested Comment Delete Confirm */}
      <ConfirmDialog
        open={Boolean(confirmDeleteCommentId)}
        title="Xóa bình luận?"
        description="Bình luận này sẽ bị xóa khỏi luồng thảo luận lỗi."
        confirmLabel="Xóa bình luận"
        onCancel={() => setConfirmDeleteCommentId(null)}
        onConfirm={handleDeleteComment}
      />

      {/* Nested Evidence Delete Confirm */}
      <ConfirmDialog
        open={Boolean(confirmDeleteEvidenceId)}
        title="Xóa bằng chứng QA?"
        description="Bản ghi bằng chứng này sẽ bị xóa vĩnh viễn khỏi danh sách QA Bug."
        confirmLabel="Xóa bằng chứng"
        onCancel={() => setConfirmDeleteEvidenceId(null)}
        onConfirm={handleDeleteEvidence}
      />
    </Modal>
  )
}
