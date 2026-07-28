import { useEffect, useState, useCallback, useMemo } from 'react'
import { 
  Bug as BugIcon, 
  ShieldAlert, 
  Pencil, 
  Trash2, 
  Plus, 
  Search, 
  RefreshCcw, 
  RotateCcw,
  CheckCircle2,
  Download,
  FileText,
  CheckSquare,
  MessageSquare,
  Paperclip,
  UserPlus,
  Clock
} from 'lucide-react'
import AttachmentSection from './AttachmentSection'
import {
  PieChart, Pie, Cell,
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, LabelList
} from 'recharts'
import { 
  ActionMenu, 
  ActionItem, 
  Button, 
  ConfirmDialog, 
  Input, 
  Modal, 
  Select,
  toast
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
  getBugReport,
  getSprintBugReport,
  getQaMetrics,
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
      return 'bg-amber-50 text-amber-700 border border-amber-200'
    case 'RESOLVED':
      return 'bg-emerald-50 text-emerald-700 border border-emerald-200'
    case 'VERIFIED':
      return 'bg-teal-50 text-teal-700 border border-teal-200'
    case 'REOPENED':
      return 'bg-purple-50 text-purple-700 border border-purple-200'
    case 'CLOSED':
      return 'bg-slate-100 text-slate-700 border border-slate-300'
    case 'CANCELLED':
    default:
      return 'bg-rose-50 text-rose-700 border border-rose-200'
  }
}

export const getPriorityBadgeClass = (priority: TaskPriority | string) => {
  switch (priority) {
    case 'URGENT':
    case 'CRITICAL':
      return 'bg-rose-100 text-rose-800 border border-rose-200'
    case 'HIGH':
      return 'bg-amber-100 text-amber-800 border border-amber-200'
    case 'MEDIUM':
      return 'bg-sky-100 text-sky-800 border border-sky-200'
    case 'LOW':
    default:
      return 'bg-slate-100 text-slate-700 border border-slate-200'
  }
}

export const priorityLabels: Record<string, string> = {
  LOW: 'Thấp (LOW)',
  MEDIUM: 'Vừa (MEDIUM)',
  HIGH: 'Cao (HIGH)',
  URGENT: 'Khẩn cấp (URGENT)'
}

export const getStatusCardStyle = (status: BugStatus, isCurrent: boolean) => {
  if (isCurrent) {
    switch (status) {
      case 'OPEN':
        return 'border-blue-500 bg-blue-100/90 text-blue-900 ring-2 ring-blue-500/60 shadow-sm font-extrabold'
      case 'ASSIGNED':
        return 'border-indigo-500 bg-indigo-100/90 text-indigo-900 ring-2 ring-indigo-500/60 shadow-sm font-extrabold'
      case 'IN_PROGRESS':
        return 'border-amber-500 bg-amber-100/90 text-amber-900 ring-2 ring-amber-500/60 shadow-sm font-extrabold'
      case 'RESOLVED':
        return 'border-emerald-500 bg-emerald-100/90 text-emerald-900 ring-2 ring-emerald-500/60 shadow-sm font-extrabold'
      case 'VERIFIED':
        return 'border-teal-500 bg-teal-100/90 text-teal-900 ring-2 ring-teal-500/60 shadow-sm font-extrabold'
      case 'REOPENED':
        return 'border-purple-500 bg-purple-100/90 text-purple-900 ring-2 ring-purple-500/60 shadow-sm font-extrabold'
      case 'CLOSED':
        return 'border-slate-500 bg-slate-200 text-slate-900 ring-2 ring-slate-500/60 shadow-sm font-extrabold'
      case 'CANCELLED':
      default:
        return 'border-rose-500 bg-rose-100/90 text-rose-900 ring-2 ring-rose-500/60 shadow-sm font-extrabold'
    }
  }

  switch (status) {
    case 'OPEN':
      return 'border-blue-200 bg-blue-50/50 text-blue-800 hover:bg-blue-100/70 hover:border-blue-400'
    case 'ASSIGNED':
      return 'border-indigo-200 bg-indigo-50/50 text-indigo-800 hover:bg-indigo-100/70 hover:border-indigo-400'
    case 'IN_PROGRESS':
      return 'border-amber-200 bg-amber-50/50 text-amber-800 hover:bg-amber-100/70 hover:border-amber-400'
    case 'RESOLVED':
      return 'border-emerald-200 bg-emerald-50/50 text-emerald-800 hover:bg-emerald-100/70 hover:border-emerald-400'
    case 'VERIFIED':
      return 'border-teal-200 bg-teal-50/50 text-teal-800 hover:bg-teal-100/70 hover:border-teal-400'
    case 'REOPENED':
      return 'border-purple-200 bg-purple-50/50 text-purple-800 hover:bg-purple-100/70 hover:border-purple-400'
    case 'CLOSED':
      return 'border-slate-200 bg-slate-50 text-slate-700 hover:bg-slate-100 hover:border-slate-400'
    case 'CANCELLED':
    default:
      return 'border-rose-200 bg-rose-50/50 text-rose-800 hover:bg-rose-100/70 hover:border-rose-400'
  }
}

export const getSeverityCardStyle = (sev: BugSeverity, isCurrent: boolean) => {
  if (isCurrent) {
    switch (sev) {
      case 'CRITICAL':
        return 'border-rose-500 bg-rose-100/90 text-rose-900 ring-2 ring-rose-500/60 shadow-sm font-extrabold'
      case 'HIGH':
        return 'border-amber-500 bg-amber-100/90 text-amber-900 ring-2 ring-amber-500/60 shadow-sm font-extrabold'
      case 'MEDIUM':
        return 'border-emerald-500 bg-emerald-100/90 text-emerald-900 ring-2 ring-emerald-500/60 shadow-sm font-extrabold'
      case 'LOW':
      default:
        return 'border-slate-500 bg-slate-200 text-slate-900 ring-2 ring-slate-500/60 shadow-sm font-extrabold'
    }
  }

  switch (sev) {
    case 'CRITICAL':
      return 'border-rose-200 bg-rose-50/50 text-rose-800 hover:bg-rose-100/70 hover:border-rose-400'
    case 'HIGH':
      return 'border-amber-200 bg-amber-50/50 text-amber-800 hover:bg-amber-100/70 hover:border-amber-400'
    case 'MEDIUM':
      return 'border-emerald-200 bg-emerald-50/50 text-emerald-800 hover:bg-emerald-100/70 hover:border-emerald-400'
    case 'LOW':
    default:
      return 'border-slate-200 bg-slate-50 text-slate-700 hover:bg-slate-100 hover:border-slate-400'
  }
}

export const getPriorityCardStyle = (pri: TaskPriority | string, isCurrent: boolean) => {
  if (isCurrent) {
    switch (pri) {
      case 'URGENT':
      case 'CRITICAL':
        return 'border-rose-500 bg-rose-100/90 text-rose-900 ring-2 ring-rose-500/60 shadow-sm font-extrabold'
      case 'HIGH':
        return 'border-amber-500 bg-amber-100/90 text-amber-900 ring-2 ring-amber-500/60 shadow-sm font-extrabold'
      case 'MEDIUM':
        return 'border-sky-500 bg-sky-100/90 text-sky-900 ring-2 ring-sky-500/60 shadow-sm font-extrabold'
      case 'LOW':
      default:
        return 'border-slate-500 bg-slate-200 text-slate-900 ring-2 ring-slate-500/60 shadow-sm font-extrabold'
    }
  }

  switch (pri) {
    case 'URGENT':
    case 'CRITICAL':
      return 'border-rose-200 bg-rose-50/50 text-rose-800 hover:bg-rose-100/70 hover:border-rose-400'
    case 'HIGH':
      return 'border-amber-200 bg-amber-50/50 text-amber-800 hover:bg-amber-100/70 hover:border-amber-400'
    case 'MEDIUM':
      return 'border-sky-200 bg-sky-50/50 text-sky-800 hover:bg-sky-100/70 hover:border-sky-400'
    case 'LOW':
    default:
      return 'border-slate-200 bg-slate-50 text-slate-700 hover:bg-slate-100 hover:border-slate-400'
  }
}

export const getStatusDotColor = (status: BugStatus) => {
  switch (status) {
    case 'OPEN': return 'bg-blue-600'
    case 'ASSIGNED': return 'bg-indigo-600'
    case 'IN_PROGRESS': return 'bg-amber-500'
    case 'RESOLVED': return 'bg-emerald-600'
    case 'VERIFIED': return 'bg-teal-600'
    case 'REOPENED': return 'bg-purple-600'
    case 'CLOSED': return 'bg-slate-600'
    case 'CANCELLED': default: return 'bg-rose-600'
  }
}

export const getSeverityDotColor = (sev: BugSeverity) => {
  switch (sev) {
    case 'CRITICAL': return 'bg-rose-600'
    case 'HIGH': return 'bg-amber-600'
    case 'MEDIUM': return 'bg-emerald-600'
    case 'LOW': default: return 'bg-slate-500'
  }
}

export const getPriorityDotColor = (pri: TaskPriority | string) => {
  switch (pri) {
    case 'URGENT':
    case 'CRITICAL': return 'bg-rose-600'
    case 'HIGH': return 'bg-amber-600'
    case 'MEDIUM': return 'bg-sky-600'
    case 'LOW': default: return 'bg-slate-500'
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
  const [qaMetrics, setQaMetrics] = useState<QaMetrics | null>(null)
  const [reportData, setReportData] = useState<BugReport | null>(null)
  const [reportLoading, setReportLoading] = useState(false)
  const [hiddenStatuses, setHiddenStatuses] = useState<string[]>([])
  const [hiddenSeverities, setHiddenSeverities] = useState<string[]>([])
  const [hiddenPriorities, setHiddenPriorities] = useState<string[]>([])

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

  // Modals state
  const [createOpen, setCreateOpen] = useState(false)
  const [selectedBugDetails, setSelectedBugDetails] = useState<Bug | null>(null)
  const [editBug, setEditBug] = useState<Bug | null>(null)
  const [deleteBugId, setDeleteBugId] = useState<string | null>(null)
  const [statusModalBug, setStatusModalBug] = useState<Bug | null>(null)
  const [assigneeModalBug, setAssigneeModalBug] = useState<Bug | null>(null)

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
          {/* Summary Section - 5 Cards Strip (Unified Design matching Dashboard & QA Metrics) */}
          {summary && (
            <div className="grid gap-4 grid-cols-2 md:grid-cols-5">
              {/* Card 1: TỔNG SỐ LỖI */}
              <div className="rounded-2xl border border-line/70 bg-white p-5 shadow-xs transition hover:shadow-md hover:-translate-y-0.5 flex flex-col justify-between space-y-2">
                <div className="flex justify-between items-center">
                  <div className="size-10 rounded-xl bg-indigo-100/70 text-indigo-700 flex items-center justify-center shrink-0">
                    <BugIcon size={20} />
                  </div>
                  <span className="text-[10px] font-extrabold text-indigo-700 bg-indigo-50 border border-indigo-200 px-2.5 py-0.5 rounded-full uppercase tracking-wider">
                    TỔNG QUAN
                  </span>
                </div>
                <div>
                  <p className="text-[11px] font-bold text-muted uppercase tracking-wider">TỔNG SỐ LỖI</p>
                  <p className="text-3xl font-black text-ink mt-1">{summary.totalBugs}</p>
                  <div className="w-full bg-slate-100 h-1.5 rounded-full overflow-hidden mt-3">
                    <div className="bg-indigo-600 h-full rounded-full" style={{ width: '100%' }} />
                  </div>
                </div>
              </div>

              {/* Card 2: CHỜ XỬ LÝ */}
              <div className="rounded-2xl border border-line/70 bg-white p-5 shadow-xs transition hover:shadow-md hover:-translate-y-0.5 flex flex-col justify-between space-y-2">
                <div className="flex justify-between items-center">
                  <div className="size-10 rounded-xl bg-sky-100/70 text-sky-700 flex items-center justify-center shrink-0">
                    <Clock size={20} />
                  </div>
                  <span className="text-[10px] font-extrabold text-sky-700 bg-sky-50 border border-sky-200 px-2.5 py-0.5 rounded-full uppercase tracking-wider">
                    CHỜ XỬ LÝ
                  </span>
                </div>
                <div>
                  <p className="text-[11px] font-bold text-muted uppercase tracking-wider">CHỜ XỬ LÝ</p>
                  <p className="text-3xl font-black text-ink mt-1">{summary.openBugs + summary.reopenedBugs}</p>
                  <div className="w-full bg-slate-100 h-1.5 rounded-full overflow-hidden mt-3">
                    <div className="bg-sky-500 h-full rounded-full" style={{ width: `${summary.totalBugs > 0 ? ((summary.openBugs + summary.reopenedBugs) / summary.totalBugs) * 100 : 0}%` }} />
                  </div>
                </div>
              </div>

              {/* Card 3: ĐANG XỬ LÝ */}
              <div className="rounded-2xl border border-line/70 bg-white p-5 shadow-xs transition hover:shadow-md hover:-translate-y-0.5 flex flex-col justify-between space-y-2">
                <div className="flex justify-between items-center">
                  <div className="size-10 rounded-xl bg-amber-100/70 text-amber-700 flex items-center justify-center shrink-0">
                    <RefreshCcw size={20} />
                  </div>
                  <span className="text-[10px] font-extrabold text-amber-700 bg-amber-50 border border-amber-200 px-2.5 py-0.5 rounded-full uppercase tracking-wider">
                    ĐANG XỬ LÝ
                  </span>
                </div>
                <div>
                  <p className="text-[11px] font-bold text-muted uppercase tracking-wider">ĐANG XỬ LÝ</p>
                  <p className="text-3xl font-black text-ink mt-1">{summary.inProgressBugs}</p>
                  <div className="w-full bg-slate-100 h-1.5 rounded-full overflow-hidden mt-3">
                    <div className="bg-amber-500 h-full rounded-full" style={{ width: `${summary.totalBugs > 0 ? (summary.inProgressBugs / summary.totalBugs) * 100 : 0}%` }} />
                  </div>
                </div>
              </div>

              {/* Card 4: LỖI KHẨN CẤP */}
              <div className="rounded-2xl border border-line/70 bg-white p-5 shadow-xs transition hover:shadow-md hover:-translate-y-0.5 flex flex-col justify-between space-y-2">
                <div className="flex justify-between items-center">
                  <div className="size-10 rounded-xl bg-rose-100/70 text-rose-600 flex items-center justify-center shrink-0">
                    <ShieldAlert size={20} />
                  </div>
                  <span className="text-[10px] font-extrabold text-rose-600 bg-rose-50 border border-rose-200 px-2.5 py-0.5 rounded-full uppercase tracking-wider">
                    CẦN CHÚ Ý
                  </span>
                </div>
                <div>
                  <p className="text-[11px] font-bold text-muted uppercase tracking-wider">LỖI KHẨN CẤP</p>
                  <p className="text-3xl font-black text-rose-600 mt-1">{summary.criticalBugs}</p>
                  <div className="w-full bg-slate-100 h-1.5 rounded-full overflow-hidden mt-3">
                    <div className="bg-rose-600 h-full rounded-full" style={{ width: `${summary.totalBugs > 0 ? (summary.criticalBugs / summary.totalBugs) * 100 : 0}%` }} />
                  </div>
                </div>
              </div>

              {/* Card 5: ĐÃ GIẢI QUYẾT */}
              <div className="rounded-2xl border border-line/70 bg-white p-5 shadow-xs transition hover:shadow-md hover:-translate-y-0.5 flex flex-col justify-between space-y-2">
                <div className="flex justify-between items-center">
                  <div className="size-10 rounded-xl bg-emerald-100/70 text-emerald-700 flex items-center justify-center shrink-0">
                    <CheckCircle2 size={20} />
                  </div>
                  <span className="text-[10px] font-extrabold text-emerald-700 bg-emerald-50 border border-emerald-200 px-2.5 py-0.5 rounded-full uppercase tracking-wider">
                    HOÀN THÀNH
                  </span>
                </div>
                <div>
                  <p className="text-[11px] font-bold text-muted uppercase tracking-wider">ĐÃ GIẢI QUYẾT</p>
                  <p className="text-3xl font-black text-emerald-600 mt-1">{summary.resolvedBugs}</p>
                  <div className="w-full bg-slate-100 h-1.5 rounded-full overflow-hidden mt-3">
                    <div className="bg-emerald-500 h-full rounded-full" style={{ width: `${summary.totalBugs > 0 ? (summary.resolvedBugs / summary.totalBugs) * 100 : 0}%` }} />
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* Title Header & Actions Bar matching Image 3 */}
          <div className="rounded-2xl border border-line/70 bg-white p-5 shadow-xs space-y-4">
            <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 border-b border-line/50 pb-4">
              <div>
                <h3 className="text-base font-extrabold text-ink">Danh sách lỗi (Bảng chi tiết)</h3>
                <p className="text-xs font-medium text-muted mt-0.5">Theo dõi và quản lý các vấn đề kỹ thuật trong dự án.</p>
              </div>
              <div className="flex items-center gap-2">
                <Button 
                  variant="outline-teal" 
                  leadingIcon={<Download size={15} />}
                  onClick={() => toast.success('Xuất báo cáo thành công!')}
                  className="!px-3 text-xs"
                >
                  Xuất Excel
                </Button>
                <Button 
                  leadingIcon={<Plus size={16} />} 
                  onClick={handleOpenCreate}
                  className="!px-4 text-xs font-bold"
                >
                  Báo lỗi mới
                </Button>
              </div>
            </div>

            {/* Filter controls */}
            <div className="flex flex-col md:flex-row gap-4 justify-between items-start md:items-center">
              <div className="flex-1 min-w-0 w-full relative">
                <Search className="absolute left-3.5 top-1/2 -translate-y-1/2 text-muted" size={17} />
                <Input 
                  aria-label="Tìm kiếm bug"
                  value={filters.keyword} 
                  onChange={event => setFilters(prev => ({ ...prev, keyword: event.target.value }))}
                  placeholder="Tìm kiếm theo ID, tiêu đề hoặc nội dung..." 
                  className="pl-10 text-xs"
                />
              </div>
              
              <div className="flex flex-wrap items-center gap-2.5 w-full md:w-auto">
                <div className="w-44">
                  <Select 
                    aria-label="Độ nghiêm trọng"
                    value={filters.severity} 
                    onChange={event => setFilters(prev => ({ ...prev, severity: event.target.value as BugSeverity | '' }))}
                    options={[
                      { label: 'Độ nghiêm trọng', value: '' },
                      ...Object.entries(bugSeverityLabels).map(([k, v]) => ({ label: v, value: k }))
                    ]}
                  />
                </div>
                <div className="w-36">
                  <Select 
                    aria-label="Độ ưu tiên"
                    value={filters.priority} 
                    onChange={event => setFilters(prev => ({ ...prev, priority: event.target.value as any }))}
                    options={[
                      { label: 'Độ ưu tiên', value: '' },
                      { label: 'Thấp', value: 'LOW' },
                      { label: 'Vừa', value: 'MEDIUM' },
                      { label: 'Cao', value: 'HIGH' },
                      { label: 'Khẩn cấp', value: 'URGENT' }
                    ]}
                  />
                </div>
                <div className="w-40">
                  <Select 
                    aria-label="Người xử lý"
                    value={filters.assigneeUserId} 
                    onChange={event => setFilters(prev => ({ ...prev, assigneeUserId: event.target.value }))}
                    options={[
                      { label: 'Người xử lý', value: '' },
                      ...members.map(m => ({ label: m.username, value: m.userId }))
                    ]}
                  />
                </div>
                <Button 
                  variant="outline-amber" 
                  iconOnly
                  leadingIcon={<RotateCcw size={16} />}
                  onClick={handleClearFilters}
                  title="Đặt lại bộ lọc"
                  aria-label="Đặt lại bộ lọc"
                  className="!h-10 !w-10 shrink-0"
                />
              </div>
            </div>
          </div>

          {/* Bugs List Grid (Modern Card Rows matching Image 3) */}
          <div className="space-y-3">
            {bugs.content.map((bug, index) => {
              const borderLeftClass = bug.severity === 'CRITICAL' ? 'border-l-4 border-l-rose-500' :
                                      bug.severity === 'HIGH' ? 'border-l-4 border-l-amber-500' :
                                      bug.severity === 'MEDIUM' ? 'border-l-4 border-l-emerald-500' : 'border-l-4 border-l-slate-300'
              return (
                <article 
                  key={bug.id} 
                  style={{ zIndex: bugs.content.length - index }}
                  className={`rounded-2xl border border-line/70 bg-white p-5 shadow-xs transition hover:shadow-md hover:-translate-y-0.5 relative hover:z-50 focus-within:z-50 ${borderLeftClass}`}
                >
                  <div className="flex flex-col md:flex-row gap-5 justify-between items-start md:items-center">
                    
                    {/* Left ID & Severity tag */}
                    <div className="w-32 shrink-0 space-y-1.5">
                      <span className="text-[11px] font-extrabold text-muted-dark bg-slate-100 px-2.5 py-1 rounded-md border border-slate-200 block w-fit">
                        #BUG-{bug.id.substring(0, 5).toUpperCase()}
                      </span>
                      <span className={`text-[11px] font-bold block ${
                        bug.severity === 'CRITICAL' ? 'text-rose-600' :
                        bug.severity === 'HIGH' ? 'text-amber-700' :
                        bug.severity === 'MEDIUM' ? 'text-emerald-700' : 'text-slate-600'
                      }`}>
                        {bug.severity === 'CRITICAL' && '! KHẨN CẤP'}
                        {bug.severity === 'HIGH' && '→ CAO'}
                        {bug.severity === 'MEDIUM' && '✔ TRUNG BÌNH'}
                        {bug.severity === 'LOW' && '↓ THẤP'}
                      </span>
                    </div>

                    {/* Main Title & Subtags */}
                    <button 
                      type="button" 
                      className="space-y-1.5 text-left flex-1 min-w-0" 
                      onClick={() => setSelectedBugDetails(bug)}
                    >
                      <h3 className="text-base font-extrabold text-ink hover:text-brand transition leading-snug">{bug.title}</h3>
                      <div className="flex flex-wrap items-center gap-2 text-xs text-muted">
                        <span className="bg-slate-50 border border-slate-200/80 px-2 py-0.5 rounded text-[11px] font-semibold text-slate-600">Báo cáo: {bug.reporterUsername || 'Hệ thống'}</span>
                        {bug.backlogItemId && (
                          <span className="bg-indigo-50 border border-indigo-100 px-2 py-0.5 rounded text-[11px] font-semibold text-indigo-700">Story: {getBacklogTitle(bug.backlogItemId)}</span>
                        )}
                        {bug.taskId && (
                          <span className="bg-sky-50 border border-sky-100 px-2 py-0.5 rounded text-[11px] font-semibold text-sky-700">Task: {getTaskTitle(bug.taskId)}</span>
                        )}
                      </div>
                    </button>

                    {/* Status Column - Clickable Status Badge Pill to open Status Selection Popup Modal */}
                    <div className="min-w-[140px] text-center shrink-0 flex flex-col items-center justify-center">
                      <p className="text-[10px] font-extrabold text-muted uppercase tracking-wider mb-1">TRẠNG THÁI</p>
                      <button
                        type="button"
                        onClick={() => setStatusModalBug(bug)}
                        className={`rounded-full px-3.5 py-1 text-xs font-bold inline-flex items-center gap-1.5 whitespace-nowrap transition hover:scale-105 hover:shadow-xs cursor-pointer ${getStatusBadgeClass(bug.status)}`}
                        title="Bấm để đổi trạng thái"
                      >
                        <div className={`size-2 rounded-full shrink-0 ${getStatusDotColor(bug.status)}`} />
                        <span>{bugStatusLabels[bug.status]}</span>
                        <Pencil size={11} className="opacity-70 shrink-0" />
                      </button>
                    </div>

                    {/* Assignee Column - Clickable Badge Pill just like Status */}
                    <div className="min-w-[150px] text-center shrink-0 flex flex-col items-center justify-center">
                      <p className="text-[10px] font-extrabold text-muted uppercase tracking-wider mb-1">NGƯỜI XỬ LÝ</p>
                      <button
                        type="button"
                        onClick={() => setAssigneeModalBug(bug)}
                        className="rounded-full px-3.5 py-1 text-xs font-bold inline-flex items-center gap-1.5 whitespace-nowrap transition hover:scale-105 hover:shadow-xs cursor-pointer bg-slate-100 text-slate-700 border border-slate-200"
                        title="Bấm để phân công người xử lý"
                      >
                        <div className="size-4 rounded-full bg-slate-800 text-white font-bold text-[9px] flex items-center justify-center shrink-0">
                          {(bug.assigneeUsername || 'C').substring(0, 1).toUpperCase()}
                        </div>
                        <span className="truncate max-w-[90px]">{bug.assigneeUsername || 'Chưa gán'}</span>
                        <Pencil size={11} className="opacity-70 shrink-0" />
                      </button>
                    </div>

                    {/* Actions Column */}
                    <div className="flex items-center gap-2 shrink-0 self-end md:self-center">
                      <ActionMenu>
                        <ActionItem onClick={() => setStatusModalBug(bug)}>
                          <RefreshCcw size={15} /> Đổi trạng thái
                        </ActionItem>
                        <ActionItem onClick={() => setAssigneeModalBug(bug)}>
                          <UserPlus size={15} /> Phân công người xử lý
                        </ActionItem>
                        <ActionItem onClick={() => handleOpenEdit(bug)}>
                          <Pencil size={15} /> Sửa chi tiết
                        </ActionItem>
                        {bug.status !== 'CLOSED' && bug.status !== 'CANCELLED' && (
                          <ActionItem danger onClick={() => setDeleteBugId(bug.id)}>
                            <Trash2 size={15} /> Xóa Bug
                          </ActionItem>
                        )}
                      </ActionMenu>
                    </div>
                  </div>
                </article>
              )
            })}

            {/* Empty state */}
            {bugs.content.length === 0 && !loading && (
              <div className="rounded-2xl border border-dashed border-line bg-white p-12 text-center">
                <BugIcon size={36} className="mx-auto mb-2 text-muted" />
                <p className="text-sm font-semibold text-muted">Chưa có lỗi nào phù hợp với bộ lọc.</p>
              </div>
            )}
          </div>
        </div>
      ) : (
        /* Dashboard Tab */
        <div className="space-y-6">
          <div className="rounded-2xl border border-line bg-white p-5 shadow-sm space-y-4">
            <div className="flex flex-wrap items-center justify-between gap-4">
              <div>
                <h3 className="text-base font-bold text-ink flex items-center gap-2">
                  <BugIcon className="text-brand" size={20} />
                  Báo cáo & QA Metrics Dự án
                </h3>
                <p className="text-xs text-muted">Dự án: {projectId}</p>
              </div>
              <div className="flex flex-wrap items-center gap-2">
                <Button 
                  variant="outline-teal" 
                  leadingIcon={<Download size={16} />}
                  onClick={() => toast.success('Xuất báo cáo Excel thành công!')}
                >
                  Xuất Excel
                </Button>
              </div>
            </div>

            {/* Filters */}
            <div className="grid gap-3 grid-cols-1 sm:grid-cols-2 md:grid-cols-5 pt-2">
              <div className="space-y-1">
                <label className="text-[10px] font-bold text-muted uppercase">Từ ngày</label>
                <Input 
                  type="date" 
                  value={reportFilters.fromDate || ''} 
                  onChange={e => setReportFilters(prev => ({ ...prev, fromDate: e.target.value }))}
                />
              </div>
              <div className="space-y-1">
                <label className="text-[10px] font-bold text-muted uppercase">Đến ngày</label>
                <Input 
                  type="date" 
                  value={reportFilters.toDate || ''} 
                  onChange={e => setReportFilters(prev => ({ ...prev, toDate: e.target.value }))}
                />
              </div>
              <div className="space-y-1">
                <label className="text-[10px] font-bold text-muted uppercase">Sprint</label>
                <Select 
                  aria-label="Chọn Sprint"
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
                  variant="outline-amber" 
                  iconOnly
                  leadingIcon={<RotateCcw size={16} />}
                  title="Đặt lại bộ lọc"
                  aria-label="Đặt lại bộ lọc"
                  className="!h-10 !w-10 shrink-0"
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
                />
              </div>
            </div>
          </div>

          {reportLoading ? (
            <div className="rounded-2xl border border-line bg-white p-12 text-center text-muted animate-pulse">
              Đang tải dữ liệu báo cáo QA Metrics...
            </div>
          ) : (
            <div className="space-y-6">
              {/* QA Metrics Panel - Top 4 Stat Cards matching Image 1 */}
              {qaMetrics && (() => {
                const formatRate = (rate: number) => {
                  if (rate == null || isNaN(rate)) return '0.0%'
                  const percentage = rate <= 1 && rate > 0 ? rate * 100 : rate
                  return `${Math.min(Math.max(percentage, 0), 100).toFixed(1)}%`
                }

                return (
                  <div className="grid gap-4 grid-cols-2 lg:grid-cols-4">
                    {/* Card 1: TỶ LỆ XỬ LÝ (RESOLVE RATE) */}
                    <div className="rounded-2xl border border-line/70 bg-white p-5 shadow-xs space-y-2 transition hover:-translate-y-0.5 hover:shadow-md flex flex-col justify-between">
                      <div className="flex justify-between items-center">
                        <div className="size-10 rounded-xl bg-amber-100/70 text-amber-700 flex items-center justify-center shrink-0">
                          <CheckCircle2 size={20} />
                        </div>
                        <span className="text-xs font-bold text-emerald-600 bg-emerald-50 border border-emerald-200 px-2 py-0.5 rounded-full flex items-center gap-0.5">
                          ↑ {formatRate(qaMetrics.resolveRate)}
                        </span>
                      </div>
                      <div>
                        <p className="text-[11px] font-bold text-muted uppercase tracking-wider">TỶ LỆ XỬ LÝ</p>
                        <p className="text-3xl font-black text-ink mt-1">{formatRate(qaMetrics.resolveRate)}</p>
                        <div className="w-full bg-slate-100 h-1.5 rounded-full overflow-hidden mt-3">
                          <div className="bg-amber-700 h-full rounded-full" style={{ width: formatRate(qaMetrics.resolveRate) }} />
                        </div>
                      </div>
                    </div>

                    {/* Card 2: TỶ LỆ MỞ LẠI (REOPEN RATE) */}
                    <div className="rounded-2xl border border-line/70 bg-white p-5 shadow-xs space-y-2 transition hover:-translate-y-0.5 hover:shadow-md flex flex-col justify-between">
                      <div className="flex justify-between items-center">
                        <div className="size-10 rounded-xl bg-rose-100/70 text-rose-600 flex items-center justify-center shrink-0">
                          <RefreshCcw size={20} />
                        </div>
                        <span className="text-xs font-bold text-rose-600 bg-rose-50 border border-rose-200 px-2 py-0.5 rounded-full flex items-center gap-0.5">
                          ↑ {formatRate(qaMetrics.reopenRate)}
                        </span>
                      </div>
                      <div>
                        <p className="text-[11px] font-bold text-muted uppercase tracking-wider">TỶ LỆ MỞ LẠI</p>
                        <p className="text-3xl font-black text-ink mt-1">{formatRate(qaMetrics.reopenRate)}</p>
                        <div className="w-full bg-slate-100 h-1.5 rounded-full overflow-hidden mt-3">
                          <div className="bg-rose-600 h-full rounded-full" style={{ width: formatRate(qaMetrics.reopenRate) }} />
                        </div>
                      </div>
                    </div>

                    {/* Card 3: LỖI KHẨN CẤP */}
                    <div className="rounded-2xl border border-line/70 bg-white p-5 shadow-xs space-y-2 transition hover:-translate-y-0.5 hover:shadow-md flex flex-col justify-between">
                      <div className="flex justify-between items-center">
                        <div className="size-10 rounded-xl bg-orange-100/70 text-orange-600 flex items-center justify-center shrink-0">
                          <ShieldAlert size={20} />
                        </div>
                        <span className="text-xs font-bold text-rose-600 bg-rose-50 border border-rose-200 px-2 py-0.5 rounded-full">
                          ! {qaMetrics.totalCriticalBugs}
                        </span>
                      </div>
                      <div>
                        <p className="text-[11px] font-bold text-muted uppercase tracking-wider">LỖI KHẨN CẤP</p>
                        <p className="text-3xl font-black text-ink mt-1">{qaMetrics.totalCriticalBugs}</p>
                        <p className="text-[11px] font-semibold text-muted mt-2">Yêu cầu xử lý ngay lập tức</p>
                      </div>
                    </div>

                    {/* Card 4: LỖI QUÁ HẠN */}
                    <div className="rounded-2xl border border-line/70 bg-white p-5 shadow-xs space-y-2 transition hover:-translate-y-0.5 hover:shadow-md flex flex-col justify-between">
                      <div className="flex justify-between items-center">
                        <div className="size-10 rounded-xl bg-slate-100 text-slate-600 flex items-center justify-center shrink-0">
                          <BugIcon size={20} />
                        </div>
                        <span className="text-xs font-bold text-slate-600 bg-slate-100 border border-slate-200 px-2 py-0.5 rounded-full">
                          ! {qaMetrics.totalOverdueBugs}
                        </span>
                      </div>
                      <div>
                        <p className="text-[11px] font-bold text-muted uppercase tracking-wider">LỖI QUÁ HẠN</p>
                        <p className="text-3xl font-black text-ink mt-1">{qaMetrics.totalOverdueBugs}</p>
                        <p className="text-[11px] font-semibold text-muted mt-2">Số lỗi trễ hạn cần chú ý</p>
                      </div>
                    </div>
                  </div>
                )
              })()}

              {/* Breakdown distributions with Recharts (Donut Ring Chart with Center Text & Bar Charts) */}
              {reportData && (() => {
                const STATUS_COLORS: Record<string, string> = {
                  OPEN: '#3b82f6',
                  ASSIGNED: '#6366f1',
                  IN_PROGRESS: '#f59e0b',
                  RESOLVED: '#10b981',
                  VERIFIED: '#14b8a6',
                  REOPENED: '#a855f7',
                  CLOSED: '#64748b',
                  CANCELLED: '#f43f5e'
                }

                const toggleStatusVisibility = (statusKey: string) => {
                  setHiddenStatuses(prev => 
                    prev.includes(statusKey) ? prev.filter(s => s !== statusKey) : [...prev, statusKey]
                  )
                }

                const toggleSeverityVisibility = (sevKey: string) => {
                  setHiddenSeverities(prev => 
                    prev.includes(sevKey) ? prev.filter(s => s !== sevKey) : [...prev, sevKey]
                  )
                }

                const togglePriorityVisibility = (priKey: string) => {
                  setHiddenPriorities(prev => 
                    prev.includes(priKey) ? prev.filter(p => p !== priKey) : [...prev, priKey]
                  )
                }

                const renderCustomizedPieLabel = ({ cx, cy, midAngle, outerRadius, percent }: any) => {
                  if (!percent || percent <= 0) return null
                  const RADIAN = Math.PI / 180
                  const radius = outerRadius + 18
                  const x = cx + radius * Math.cos(-midAngle * RADIAN)
                  const y = cy + radius * Math.sin(-midAngle * RADIAN)
                  const percentStr = `${(percent * 100).toFixed(0)}%`

                  return (
                    <text
                      x={x}
                      y={y}
                      fill="#475569"
                      textAnchor={x > cx ? 'start' : 'end'}
                      dominantBaseline="central"
                      style={{ fontSize: '11px', fontWeight: 800 }}
                    >
                      {percentStr}
                    </text>
                  )
                }

                const statusPieData = reportData.byStatus
                  .filter(s => s.total > 0)
                  .map(s => ({
                    statusKey: s.status,
                    name: bugStatusLabels[s.status] || s.status,
                    value: hiddenStatuses.includes(s.status) ? 0 : s.total,
                    color: STATUS_COLORS[s.status] || '#94a3b8'
                  }))

                const severityBarData = reportData.bySeverity.map(s => {
                  const isHidden = hiddenSeverities.includes(s.severity)
                  const val = isHidden ? 0 : s.total
                  const pct = reportData.totalBugs > 0 ? (val / reportData.totalBugs) * 100 : 0
                  return {
                    sevKey: s.severity,
                    name: bugSeverityLabels[s.severity] || s.severity,
                    'Số lỗi': val,
                    percentLabel: val > 0 ? `${pct.toFixed(1)}%` : '',
                    fill: s.severity === 'CRITICAL' ? '#e11d48' :
                          s.severity === 'HIGH' ? '#f97316' :
                          s.severity === 'MEDIUM' ? '#f59e0b' : '#3b82f6'
                  }
                })

                const priorityBarData = reportData.byPriority.map(s => {
                  const isHidden = hiddenPriorities.includes(s.priority)
                  const val = isHidden ? 0 : s.total
                  const pct = reportData.totalBugs > 0 ? (val / reportData.totalBugs) * 100 : 0
                  return {
                    priKey: s.priority,
                    name: s.priority === 'URGENT' ? 'Khẩn cấp' :
                          s.priority === 'HIGH' ? 'Cao' :
                          s.priority === 'MEDIUM' ? 'Vừa' : 'Thấp',
                    'Số lỗi': val,
                    percentLabel: val > 0 ? `${pct.toFixed(1)}%` : '',
                    fill: s.priority === 'URGENT' ? '#ef4444' :
                          s.priority === 'HIGH' ? '#f97316' :
                          s.priority === 'MEDIUM' ? '#f59e0b' : '#64748b'
                  }
                })

                const hasActivePieSlices = statusPieData.some(d => d.value > 0)

                return (
                  <div className="grid gap-6 md:grid-cols-2">
                    
                    {/* 1. Status Breakdown - Full Solid Pie Chart (Chart Tròn Đặc) */}
                    <div className="rounded-2xl border border-line/70 bg-white p-5 shadow-xs space-y-3 flex flex-col justify-between">
                      <div className="flex items-center justify-between border-b border-line/50 pb-3">
                        <h4 className="text-sm font-bold text-ink">Thống kê theo Trạng thái</h4>
                        <span className="text-xs font-semibold text-muted">{reportData.totalBugs} lỗi</span>
                      </div>
                      
                      {reportData.byStatus.some(s => s.total > 0) ? (
                        <div className="space-y-2 flex-1 flex flex-col justify-between">
                          <div className="h-60 w-full flex items-center justify-center relative">
                            {statusPieData.length > 0 ? (
                              <ResponsiveContainer width="100%" height="100%">
                                <PieChart margin={{ top: 10, right: 30, left: 30, bottom: 10 }}>
                                  <Pie
                                    data={statusPieData}
                                    cx="50%"
                                    cy="50%"
                                    innerRadius={0}
                                    outerRadius={85}
                                    paddingAngle={0}
                                    stroke="none"
                                    dataKey="value"
                                    isAnimationActive={true}
                                    animationDuration={800}
                                    animationEasing="ease-in-out"
                                    animationBegin={0}
                                    labelLine={true}
                                    label={renderCustomizedPieLabel}
                                    onClick={(data: any) => data && data.payload && data.payload.statusKey && toggleStatusVisibility(data.payload.statusKey)}
                                    cursor="pointer"
                                  >
                                    {statusPieData.map((entry, index) => (
                                      <Cell 
                                        key={`cell-${index}`} 
                                        fill={entry.color} 
                                        stroke="none"
                                        className="transition-all duration-300 hover:opacity-85 cursor-pointer"
                                      />
                                    ))}
                                  </Pie>
                                  <Tooltip 
                                    formatter={(value: any) => [`${value ?? 0} lỗi`, 'Số lượng']}
                                    contentStyle={{ borderRadius: '12px', border: '1px solid #e2e8f0', boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1)' }}
                                  />
                                </PieChart>
                              </ResponsiveContainer>
                            ) : null}
                            {!hasActivePieSlices && (
                              <div className="absolute inset-0 flex items-center justify-center text-center text-xs text-muted bg-white/80 rounded-xl">Tất cả trạng thái đã ẩn. Bấm các nút phía dưới để hiển thị lại.</div>
                            )}
                          </div>

                          {/* Compact Interactive Legend Buttons */}
                          <div className="flex flex-wrap items-center justify-center gap-2 pt-1">
                            {reportData.byStatus
                              .filter(s => s.total > 0)
                              .map(s => {
                                const isHidden = hiddenStatuses.includes(s.status)
                                const color = STATUS_COLORS[s.status] || '#94a3b8'
                                return (
                                  <button
                                    key={s.status}
                                    type="button"
                                    onClick={() => toggleStatusVisibility(s.status)}
                                    className={`flex items-center gap-2 px-3 py-1 rounded-full text-xs font-bold transition-all border cursor-pointer ${
                                      isHidden 
                                        ? 'bg-slate-100 text-slate-400 border-slate-200 line-through opacity-50' 
                                        : 'bg-white text-slate-700 border-slate-200 shadow-2xs hover:scale-105 hover:shadow-xs'
                                    }`}
                                    title={isHidden ? 'Bấm để hiển thị' : 'Bấm để ẩn'}
                                  >
                                    <span 
                                      className="size-3 rounded-full shrink-0 transition-transform duration-200" 
                                      style={{ backgroundColor: isHidden ? '#cbd5e1' : color }} 
                                    />
                                    <span>{bugStatusLabels[s.status] || s.status}</span>
                                  </button>
                                )
                              })}
                          </div>
                        </div>
                      ) : (
                        <div className="py-12 text-center text-xs text-muted">Chưa có dữ liệu trạng thái.</div>
                      )}
                    </div>

                    {/* 2. Severity Breakdown - Bar Chart (Chart Cột kèm Toggle Bật/Tắt) */}
                    <div className="rounded-2xl border border-line/70 bg-white p-5 shadow-xs space-y-4 flex flex-col justify-between">
                      <div className="flex items-center justify-between border-b border-line/50 pb-3">
                        <h4 className="text-sm font-bold text-ink">Thống kê theo Độ nghiêm trọng</h4>
                        <span className="text-xs font-semibold text-muted">Phân loại Độ nghiêm trọng</span>
                      </div>

                      <div className="h-60 w-full pt-2">
                        {severityBarData.length > 0 ? (
                          <ResponsiveContainer width="100%" height="100%">
                            <BarChart data={severityBarData} margin={{ top: 20, right: 10, left: -20, bottom: 0 }}>
                              <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f1f5f9" />
                              <XAxis dataKey="name" tick={{ fontSize: 11, fontWeight: 600, fill: '#64748b' }} axisLine={false} tickLine={false} />
                              <YAxis allowDecimals={false} tick={{ fontSize: 11, fill: '#64748b' }} axisLine={false} tickLine={false} />
                              <Tooltip 
                                formatter={(value: any) => [`${value ?? 0} lỗi`, 'Số lượng']}
                                contentStyle={{ borderRadius: '12px', border: '1px solid #e2e8f0', boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1)' }}
                              />
                              <Bar dataKey="Số lỗi" radius={[8, 8, 0, 0]} isAnimationActive={true} animationDuration={800} animationEasing="ease-out">
                                <LabelList dataKey="percentLabel" position="top" style={{ fontSize: 11, fontWeight: 700, fill: '#475569' }} />
                                {severityBarData.map((entry, index) => (
                                  <Cell key={`bar-${index}`} fill={entry.fill} />
                                ))}
                              </Bar>
                            </BarChart>
                          </ResponsiveContainer>
                        ) : (
                          <div className="py-12 text-center text-xs text-muted">Tất cả độ nghiêm trọng đã ẩn. Bấm nút phía dưới để hiển thị lại.</div>
                        )}
                      </div>

                      {/* Interactive Legend Buttons for Severity */}
                      <div className="flex flex-wrap items-center justify-center gap-2 pt-1 border-t border-line/40">
                        {(['CRITICAL', 'HIGH', 'MEDIUM', 'LOW'] as BugSeverity[]).map(sev => {
                          const isHidden = hiddenSeverities.includes(sev)
                          const color = sev === 'CRITICAL' ? '#e11d48' :
                                        sev === 'HIGH' ? '#f97316' :
                                        sev === 'MEDIUM' ? '#f59e0b' : '#3b82f6'
                          return (
                            <button
                              key={sev}
                              type="button"
                              onClick={() => toggleSeverityVisibility(sev)}
                              className={`flex items-center gap-2 px-3 py-1 rounded-full text-xs font-bold transition-all border cursor-pointer ${
                                isHidden 
                                  ? 'bg-slate-100 text-slate-400 border-slate-200 line-through opacity-50' 
                                  : 'bg-white text-slate-700 border-slate-200 shadow-2xs hover:scale-105 hover:shadow-xs'
                              }`}
                              title={isHidden ? 'Bấm để hiển thị' : 'Bấm để ẩn'}
                            >
                              <span 
                                className="size-3 rounded-full shrink-0 transition-transform duration-200" 
                                style={{ backgroundColor: isHidden ? '#cbd5e1' : color }} 
                              />
                              <span>{bugSeverityLabels[sev] || sev}</span>
                            </button>
                          )
                        })}
                      </div>
                    </div>

                    {/* 3. Priority Breakdown - Bar Chart (Chart Cột kèm Toggle Bật/Tắt) */}
                    <div className="rounded-2xl border border-line/70 bg-white p-5 shadow-xs space-y-4 flex flex-col justify-between">
                      <div className="flex items-center justify-between border-b border-line/50 pb-3">
                        <h4 className="text-sm font-bold text-ink">Thống kê theo Độ ưu tiên</h4>
                        <span className="text-xs font-semibold text-muted">Phân loại Độ ưu tiên</span>
                      </div>

                      <div className="h-60 w-full pt-2">
                        {priorityBarData.length > 0 ? (
                          <ResponsiveContainer width="100%" height="100%">
                            <BarChart data={priorityBarData} margin={{ top: 20, right: 10, left: -20, bottom: 0 }}>
                              <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f1f5f9" />
                              <XAxis dataKey="name" tick={{ fontSize: 11, fontWeight: 600, fill: '#64748b' }} axisLine={false} tickLine={false} />
                              <YAxis allowDecimals={false} tick={{ fontSize: 11, fill: '#64748b' }} axisLine={false} tickLine={false} />
                              <Tooltip 
                                formatter={(value: any) => [`${value ?? 0} lỗi`, 'Số lượng']}
                                contentStyle={{ borderRadius: '12px', border: '1px solid #e2e8f0', boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1)' }}
                              />
                              <Bar dataKey="Số lỗi" radius={[8, 8, 0, 0]} isAnimationActive={true} animationDuration={800} animationEasing="ease-out">
                                <LabelList dataKey="percentLabel" position="top" style={{ fontSize: 11, fontWeight: 700, fill: '#475569' }} />
                                {priorityBarData.map((entry, index) => (
                                  <Cell key={`bar-p-${index}`} fill={entry.fill} />
                                ))}
                              </Bar>
                            </BarChart>
                          </ResponsiveContainer>
                        ) : (
                          <div className="py-12 text-center text-xs text-muted">Tất cả mức ưu tiên đã ẩn. Bấm nút phía dưới để hiển thị lại.</div>
                        )}
                      </div>

                      {/* Interactive Legend Buttons for Priority */}
                      <div className="flex flex-wrap items-center justify-center gap-2 pt-1 border-t border-line/40">
                        {(['URGENT', 'HIGH', 'MEDIUM', 'LOW'] as TaskPriority[]).map(pri => {
                          const isHidden = hiddenPriorities.includes(pri)
                          const color = pri === 'URGENT' ? '#ef4444' :
                                        pri === 'HIGH' ? '#f97316' :
                                        pri === 'MEDIUM' ? '#f59e0b' : '#64748b'
                          return (
                            <button
                              key={pri}
                              type="button"
                              onClick={() => togglePriorityVisibility(pri)}
                              className={`flex items-center gap-2 px-3 py-1 rounded-full text-xs font-bold transition-all border cursor-pointer ${
                                isHidden 
                                  ? 'bg-slate-100 text-slate-400 border-slate-200 line-through opacity-50' 
                                  : 'bg-white text-slate-700 border-slate-200 shadow-2xs hover:scale-105 hover:shadow-xs'
                              }`}
                              title={isHidden ? 'Bấm để hiển thị' : 'Bấm để ẩn'}
                            >
                              <span 
                                className="size-3 rounded-full shrink-0 transition-transform duration-200" 
                                style={{ backgroundColor: isHidden ? '#cbd5e1' : color }} 
                              />
                              <span>{priorityLabels[pri] || pri}</span>
                            </button>
                          )
                        })}
                      </div>
                    </div>

                    {/* 4. Assignees breakdown / Leaderboard */}
                    <div className="rounded-2xl border border-line/70 bg-white p-5 shadow-xs space-y-4 font-semibold">
                      <div className="flex items-center justify-between border-b border-line/50 pb-3">
                        <h4 className="text-sm font-bold text-ink">Phân công lỗi theo Thành viên</h4>
                        <span className="text-xs font-semibold text-muted">{reportData.byAssignee.length} thành viên</span>
                      </div>
                      <div className="overflow-x-auto">
                        <table className="w-full text-xs text-left border-collapse">
                          <thead>
                            <tr className="border-b border-line/60 text-muted uppercase font-bold text-[10px] bg-slate-50/80">
                              <th className="py-2.5 px-3">Thành viên</th>
                              <th className="py-2.5 px-2 text-center">Tổng số lỗi</th>
                              <th className="py-2.5 px-2 text-center">Đang mở</th>
                              <th className="py-2.5 px-2 text-center">Đã sửa</th>
                              <th className="py-2.5 px-2 text-center text-rose-600">Khẩn cấp</th>
                            </tr>
                          </thead>
                          <tbody className="divide-y divide-line/40">
                            {reportData.byAssignee.map(u => (
                              <tr key={u.assigneeUserId} className="transition-colors hover:bg-slate-50/80">
                                <td className="py-2.5 px-3 font-semibold text-ink">{u.username || 'Chưa phân công'}</td>
                                <td className="py-2.5 px-2 text-center font-bold text-ink">{u.totalBugs}</td>
                                <td className="py-2.5 px-2 text-center text-sky-600 font-semibold">{u.openBugs}</td>
                                <td className="py-2.5 px-2 text-center text-emerald-600 font-semibold">{u.resolvedBugs}</td>
                                <td className="py-2.5 px-2 text-center text-rose-600 font-bold">{u.criticalBugs}</td>
                              </tr>
                            ))}
                            {reportData.byAssignee.length === 0 && (
                              <tr>
                                <td colSpan={5} className="py-8 text-center text-muted">Chưa có gán lỗi nào phát sinh.</td>
                              </tr>
                            )}
                          </tbody>
                        </table>
                      </div>
                    </div>

                  </div>
                )
              })()}
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
            <Button type="button" variant="danger" onClick={() => setCreateOpen(false)}>Hủy</Button>
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
            <Button type="button" variant="danger" onClick={() => setEditBug(null)}>Hủy</Button>
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
      {/* Status Selection Popup Modal */}
      {statusModalBug && (
        <Modal
          open={Boolean(statusModalBug)}
          onClose={() => setStatusModalBug(null)}
          title={`Cập nhật Trạng thái Bug (#BUG-${statusModalBug.id.substring(0, 5).toUpperCase()})`}
        >
          <div className="space-y-4">
            <p className="text-xs font-semibold text-muted">
              Lỗi: <span className="font-bold text-ink">{statusModalBug.title}</span>
            </p>

            <div className="space-y-2 pt-2">
              <label className="text-[11px] font-extrabold text-muted uppercase tracking-wider block">Chọn trạng thái mới</label>
              <div className="grid grid-cols-2 gap-2.5">
                {(['OPEN', 'ASSIGNED', 'IN_PROGRESS', 'RESOLVED', 'VERIFIED', 'REOPENED', 'CLOSED', 'CANCELLED'] as BugStatus[]).map(st => {
                  const isCurrent = statusModalBug.status === st
                  return (
                    <button
                      key={st}
                      type="button"
                      onClick={async () => {
                        await handleStatusChange(statusModalBug.id, st)
                        setStatusModalBug(null)
                      }}
                      className={`p-3.5 rounded-xl border text-xs font-bold text-left transition flex items-center justify-between cursor-pointer ${getStatusCardStyle(st, isCurrent)}`}
                    >
                      <div className="flex items-center gap-2">
                        <div className={`size-2.5 rounded-full shrink-0 ${getStatusDotColor(st)}`} />
                        <span className="font-extrabold text-xs tracking-wide">{bugStatusLabels[st]}</span>
                      </div>
                      {isCurrent && <CheckCircle2 size={18} className="shrink-0 text-current" />}
                    </button>
                  )
                })}
              </div>
            </div>

            <div className="flex justify-end pt-4 border-t border-line/60">
              <Button variant="danger" onClick={() => setStatusModalBug(null)}>
                Hủy bỏ
              </Button>
            </div>
          </div>
        </Modal>
      )}

      {/* Assignee Selection Popup Modal */}
      {assigneeModalBug && (
        <Modal
          open={Boolean(assigneeModalBug)}
          onClose={() => setAssigneeModalBug(null)}
          title={`Phân công Người xử lý Bug (#BUG-${assigneeModalBug.id.substring(0, 5).toUpperCase()})`}
        >
          <div className="space-y-4">
            <p className="text-xs font-semibold text-muted">
              Lỗi: <span className="font-bold text-ink">{assigneeModalBug.title}</span>
            </p>

            <div className="space-y-2 pt-2">
              <label className="text-[11px] font-extrabold text-muted uppercase tracking-wider block">Chọn thành viên xử lý</label>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5 max-h-60 overflow-y-auto pr-1">
                {/* Option: Unassigned */}
                <button
                  type="button"
                  onClick={async () => {
                    await handleAssignChange(assigneeModalBug.id, null)
                    setAssigneeModalBug(null)
                  }}
                  className={`p-3 rounded-xl border text-xs font-bold text-left transition flex items-center justify-between cursor-pointer ${
                    !assigneeModalBug.assigneeUserId 
                      ? 'border-slate-500 bg-slate-100 text-slate-800 ring-2 ring-slate-400/40 shadow-xs' 
                      : 'border-line bg-white hover:bg-slate-50 text-slate-700 hover:border-slate-300'
                  }`}
                >
                  <div className="flex items-center gap-2">
                    <div className="size-7 rounded-full bg-slate-200 text-slate-600 font-bold text-xs flex items-center justify-center">
                      ?
                    </div>
                    <span className="font-bold">Chưa gán</span>
                  </div>
                  {!assigneeModalBug.assigneeUserId && <CheckCircle2 size={17} className="shrink-0 text-current" />}
                </button>

                {/* Members list */}
                {members.map(m => {
                  const isCurrent = assigneeModalBug.assigneeUserId === m.userId
                  return (
                    <button
                      key={m.userId}
                      type="button"
                      onClick={async () => {
                        await handleAssignChange(assigneeModalBug.id, m.userId)
                        setAssigneeModalBug(null)
                      }}
                      className={`p-3 rounded-xl border text-xs font-bold text-left transition flex items-center justify-between cursor-pointer ${
                        isCurrent 
                          ? 'border-indigo-500 bg-indigo-50/90 text-indigo-800 ring-2 ring-indigo-400/40 shadow-xs' 
                          : 'border-line bg-white hover:bg-slate-50 text-slate-700 hover:border-slate-300'
                      }`}
                    >
                      <div className="flex items-center gap-2 min-w-0">
                        <div className="size-7 rounded-full bg-slate-800 text-white font-bold text-xs flex items-center justify-center shrink-0">
                          {m.username.substring(0, 1).toUpperCase()}
                        </div>
                        <div className="truncate">
                          <p className="font-bold text-ink truncate">{m.username}</p>
                          <p className="text-[10px] font-normal text-muted truncate">{m.email || 'Thành viên'}</p>
                        </div>
                      </div>
                      {isCurrent && <CheckCircle2 size={17} className="shrink-0 text-indigo-600" />}
                    </button>
                  )
                })}
              </div>
            </div>

            <div className="flex justify-end pt-4 border-t border-line/60">
              <Button variant="danger" onClick={() => setAssigneeModalBug(null)}>
                Hủy bỏ
              </Button>
            </div>
          </div>
        </Modal>
      )}

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

  // Quick metadata selection popup states
  const [statusModalOpen, setStatusModalOpen] = useState(false)
  const [severityModalOpen, setSeverityModalOpen] = useState(false)
  const [priorityModalOpen, setPriorityModalOpen] = useState(false)
  const [assigneeModalOpen, setAssigneeModalOpen] = useState(false)

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
        
        {/* Info badges header (Modern Redesigned Interactive Metadata Grid) */}
        <div className="p-4 sm:p-5 rounded-2xl border border-line/80 bg-slate-50/70 shadow-2xs space-y-4">
          <div className="flex flex-wrap items-center gap-x-6 gap-y-3">
            {/* Mã lỗi */}
            <div className="flex flex-col gap-1">
              <span className="text-[10px] font-extrabold text-muted uppercase tracking-wider">MÃ LỖI</span>
              <span className="bg-slate-900 text-white font-extrabold text-xs px-3 py-1.5 rounded-xl border border-slate-800 shadow-2xs block w-fit">
                #BUG-{bug.id.substring(0, 5).toUpperCase()}
              </span>
            </div>
            
            {/* Độ nghiêm trọng */}
            <div className="flex flex-col gap-1">
              <span className="text-[10px] font-extrabold text-muted uppercase tracking-wider">ĐỘ NGHIÊM TRỌNG</span>
              <button
                type="button"
                onClick={() => setSeverityModalOpen(true)}
                disabled={bug.status === 'CLOSED' || bug.status === 'CANCELLED'}
                className={`rounded-xl px-3.5 py-1.5 text-xs font-bold inline-flex items-center gap-1.5 transition hover:scale-105 hover:shadow-xs cursor-pointer ${getSeverityBadgeClass(bug.severity)}`}
                title="Bấm để đổi độ nghiêm trọng"
              >
                <span>{bugSeverityLabels[bug.severity]}</span>
                <Pencil size={11} className="opacity-70 shrink-0" />
              </button>
            </div>

            {/* Độ ưu tiên */}
            <div className="flex flex-col gap-1">
              <span className="text-[10px] font-extrabold text-muted uppercase tracking-wider">ĐỘ ƯU TIÊN</span>
              <button
                type="button"
                onClick={() => setPriorityModalOpen(true)}
                disabled={bug.status === 'CLOSED' || bug.status === 'CANCELLED'}
                className={`rounded-xl px-3.5 py-1.5 text-xs font-bold inline-flex items-center gap-1.5 transition hover:scale-105 hover:shadow-xs cursor-pointer ${getPriorityBadgeClass(bug.priority)}`}
                title="Bấm để đổi độ ưu tiên"
              >
                <span>{priorityLabels[bug.priority] || bug.priority}</span>
                <Pencil size={11} className="opacity-70 shrink-0" />
              </button>
            </div>

            {/* Người xử lý */}
            <div className="flex flex-col gap-1">
              <span className="text-[10px] font-extrabold text-muted uppercase tracking-wider">NGƯỜI XỬ LÝ</span>
              <button
                type="button"
                onClick={() => setAssigneeModalOpen(true)}
                disabled={bug.status === 'CLOSED' || bug.status === 'CANCELLED'}
                className="rounded-xl px-3.5 py-1.5 text-xs font-bold inline-flex items-center gap-2 transition hover:scale-105 hover:shadow-xs cursor-pointer bg-white text-slate-700 border border-slate-200"
                title="Bấm để phân công người xử lý"
              >
                <div className="size-4 rounded-full bg-slate-800 text-white font-bold text-[9px] flex items-center justify-center shrink-0">
                  {(bug.assigneeUsername || 'C').substring(0, 1).toUpperCase()}
                </div>
                <span className="font-bold">{bug.assigneeUsername || 'Chưa gán'}</span>
                <Pencil size={11} className="opacity-70 shrink-0" />
              </button>
            </div>

            {/* Trạng thái */}
            <div className="flex flex-col gap-1">
              <span className="text-[10px] font-extrabold text-muted uppercase tracking-wider">TRẠNG THÁI</span>
              <button
                type="button"
                onClick={() => setStatusModalOpen(true)}
                className={`rounded-xl px-3.5 py-1.5 text-xs font-bold inline-flex items-center gap-1.5 transition hover:scale-105 hover:shadow-xs cursor-pointer ${getStatusBadgeClass(bug.status)}`}
                title="Bấm để đổi trạng thái"
              >
                <div className={`size-2 rounded-full shrink-0 ${getStatusDotColor(bug.status)}`} />
                <span>{bugStatusLabels[bug.status]}</span>
                <Pencil size={11} className="opacity-70 shrink-0" />
              </button>
            </div>
          </div>
          
          {/* Quick status transitions bar */}
          {transitions.length > 0 && (
            <div className="flex flex-wrap items-center gap-2 pt-3 border-t border-line/60">
              <span className="text-xs font-bold text-muted uppercase tracking-wider mr-1">Chuyển nhanh:</span>
              {transitions.map(status => (
                <button 
                  key={status} 
                  type="button" 
                  onClick={() => onStatusChange(bug.id, status)}
                  className={`px-3 py-1 text-xs font-bold rounded-xl transition hover:scale-105 hover:shadow-xs cursor-pointer ${getStatusBadgeClass(status)}`}
                >
                  {bugStatusLabels[status]}
                </button>
              ))}
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

      {/* Quick Metadata Selection Popup Modals */}
      {statusModalOpen && (
        <Modal
          open={statusModalOpen}
          onClose={() => setStatusModalOpen(false)}
          title="Cập nhật Trạng thái Bug"
        >
          <div className="space-y-4">
            <p className="text-xs font-semibold text-muted">
              Lỗi: <span className="font-bold text-ink">{bug.title}</span>
            </p>

            <div className="space-y-2 pt-2">
              <label className="text-[11px] font-extrabold text-muted uppercase tracking-wider block">Chọn trạng thái mới</label>
              <div className="grid grid-cols-2 gap-2.5">
                {(['OPEN', 'ASSIGNED', 'IN_PROGRESS', 'RESOLVED', 'VERIFIED', 'REOPENED', 'CLOSED', 'CANCELLED'] as BugStatus[]).map(st => {
                  const isCurrent = bug.status === st
                  return (
                    <button
                      key={st}
                      type="button"
                      onClick={() => {
                        onStatusChange(bug.id, st)
                        setStatusModalOpen(false)
                      }}
                      className={`p-3.5 rounded-xl border text-xs font-bold text-left transition flex items-center justify-between cursor-pointer ${getStatusCardStyle(st, isCurrent)}`}
                    >
                      <div className="flex items-center gap-2">
                        <div className={`size-2.5 rounded-full shrink-0 ${getStatusDotColor(st)}`} />
                        <span className="font-extrabold text-xs tracking-wide">{bugStatusLabels[st]}</span>
                      </div>
                      {isCurrent && <CheckCircle2 size={18} className="shrink-0 text-current" />}
                    </button>
                  )
                })}
              </div>
            </div>

            <div className="flex justify-end pt-4 border-t border-line/60">
              <Button variant="danger" onClick={() => setStatusModalOpen(false)}>
                Hủy bỏ
              </Button>
            </div>
          </div>
        </Modal>
      )}

      {severityModalOpen && (
        <Modal
          open={severityModalOpen}
          onClose={() => setSeverityModalOpen(false)}
          title="Cập nhật Độ nghiêm trọng (Severity)"
        >
          <div className="space-y-4">
            <p className="text-xs font-semibold text-muted">
              Lỗi: <span className="font-bold text-ink">{bug.title}</span>
            </p>

            <div className="space-y-2 pt-2">
              <label className="text-[11px] font-extrabold text-muted uppercase tracking-wider block">Chọn mức độ nghiêm trọng</label>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5">
                {(['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'] as BugSeverity[]).map(sev => {
                  const isCurrent = bug.severity === sev
                  return (
                    <button
                      key={sev}
                      type="button"
                      onClick={() => {
                        onSeverityChange(bug.id, sev)
                        setSeverityModalOpen(false)
                      }}
                      className={`p-3.5 rounded-xl border text-xs font-bold text-left transition flex items-center justify-between cursor-pointer ${getSeverityCardStyle(sev, isCurrent)}`}
                    >
                      <div className="flex items-center gap-2">
                        <div className={`size-2.5 rounded-full shrink-0 ${getSeverityDotColor(sev)}`} />
                        <span className="font-extrabold text-xs tracking-wide">{bugSeverityLabels[sev]}</span>
                      </div>
                      {isCurrent && <CheckCircle2 size={18} className="shrink-0 text-current" />}
                    </button>
                  )
                })}
              </div>
            </div>

            <div className="flex justify-end pt-4 border-t border-line/60">
              <Button variant="danger" onClick={() => setSeverityModalOpen(false)}>
                Hủy bỏ
              </Button>
            </div>
          </div>
        </Modal>
      )}

      {priorityModalOpen && (
        <Modal
          open={priorityModalOpen}
          onClose={() => setPriorityModalOpen(false)}
          title="Cập nhật Mức độ ưu tiên (Priority)"
        >
          <div className="space-y-4">
            <p className="text-xs font-semibold text-muted">
              Lỗi: <span className="font-bold text-ink">{bug.title}</span>
            </p>

            <div className="space-y-2 pt-2">
              <label className="text-[11px] font-extrabold text-muted uppercase tracking-wider block">Chọn mức độ ưu tiên</label>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5">
                {(['LOW', 'MEDIUM', 'HIGH', 'URGENT'] as TaskPriority[]).map(pri => {
                  const isCurrent = bug.priority === pri
                  return (
                    <button
                      key={pri}
                      type="button"
                      onClick={() => {
                        onPriorityChange(bug.id, pri)
                        setPriorityModalOpen(false)
                      }}
                      className={`p-3.5 rounded-xl border text-xs font-bold text-left transition flex items-center justify-between cursor-pointer ${getPriorityCardStyle(pri, isCurrent)}`}
                    >
                      <div className="flex items-center gap-2">
                        <div className={`size-2.5 rounded-full shrink-0 ${getPriorityDotColor(pri)}`} />
                        <span className="font-extrabold text-xs tracking-wide">{priorityLabels[pri] || pri}</span>
                      </div>
                      {isCurrent && <CheckCircle2 size={18} className="shrink-0 text-current" />}
                    </button>
                  )
                })}
              </div>
            </div>

            <div className="flex justify-end pt-4 border-t border-line/60">
              <Button variant="danger" onClick={() => setPriorityModalOpen(false)}>
                Hủy bỏ
              </Button>
            </div>
          </div>
        </Modal>
      )}

      {assigneeModalOpen && (
        <Modal
          open={assigneeModalOpen}
          onClose={() => setAssigneeModalOpen(false)}
          title="Phân công Người xử lý Bug"
        >
          <div className="space-y-4">
            <p className="text-xs font-semibold text-muted">
              Lỗi: <span className="font-bold text-ink">{bug.title}</span>
            </p>

            <div className="space-y-2 pt-2">
              <label className="text-[11px] font-extrabold text-muted uppercase tracking-wider block">Chọn thành viên xử lý</label>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5 max-h-60 overflow-y-auto pr-1">
                {/* Option: Unassigned */}
                <button
                  type="button"
                  onClick={() => {
                    onAssigneeChange(bug.id, null)
                    setAssigneeModalOpen(false)
                  }}
                  className={`p-3 rounded-xl border text-xs font-bold text-left transition flex items-center justify-between cursor-pointer ${
                    !bug.assigneeUserId 
                      ? 'border-slate-500 bg-slate-100 text-slate-800 ring-2 ring-slate-400/40 shadow-xs' 
                      : 'border-line bg-white hover:bg-slate-50 text-slate-700 hover:border-slate-300'
                  }`}
                >
                  <div className="flex items-center gap-2">
                    <div className="size-7 rounded-full bg-slate-200 text-slate-600 font-bold text-xs flex items-center justify-center">
                      ?
                    </div>
                    <span className="font-bold">Chưa gán</span>
                  </div>
                  {!bug.assigneeUserId && <CheckCircle2 size={17} className="shrink-0 text-current" />}
                </button>

                {/* Members list */}
                {members.map(m => {
                  const isCurrent = bug.assigneeUserId === m.userId
                  return (
                    <button
                      key={m.userId}
                      type="button"
                      onClick={() => {
                        onAssigneeChange(bug.id, m.userId)
                        setAssigneeModalOpen(false)
                      }}
                      className={`p-3 rounded-xl border text-xs font-bold text-left transition flex items-center justify-between cursor-pointer ${
                        isCurrent 
                          ? 'border-indigo-500 bg-indigo-50/90 text-indigo-800 ring-2 ring-indigo-400/40 shadow-xs' 
                          : 'border-line bg-white hover:bg-slate-50 text-slate-700 hover:border-slate-300'
                      }`}
                    >
                      <div className="flex items-center gap-2 min-w-0">
                        <div className="size-7 rounded-full bg-slate-800 text-white font-bold text-xs flex items-center justify-center shrink-0">
                          {m.username.substring(0, 1).toUpperCase()}
                        </div>
                        <div className="truncate">
                          <p className="font-bold text-ink truncate">{m.username}</p>
                          <p className="text-[10px] font-normal text-muted truncate">{m.email || 'Thành viên'}</p>
                        </div>
                      </div>
                      {isCurrent && <CheckCircle2 size={17} className="shrink-0 text-indigo-600" />}
                    </button>
                  )
                })}
              </div>
            </div>

            <div className="flex justify-end pt-4 border-t border-line/60">
              <Button variant="danger" onClick={() => setAssigneeModalOpen(false)}>
                Hủy bỏ
              </Button>
            </div>
          </div>
        </Modal>
      )}
    </Modal>
  )
}
