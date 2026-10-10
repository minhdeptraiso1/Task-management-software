import { useState } from 'react'
import { CheckCircle2, Clock3, FolderKanban, Plus, Search, TrendingUp, UserRound } from 'lucide-react'
import { CartesianGrid, Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import { Badge, Button, EmptyState, Input, PageHeader, Select, StatCard } from '../../../components/ui'
import { formatDate } from '../../../utils/format'
import { projectStatusLabels, type ProjectStatus } from '../../project/models/project.model'
import type { AdminProjectProgressFilters, AdminProjectProgressItem } from '../models/admin.model'
import type { User } from '../models/user.model'

interface Props {
  projects: AdminProjectProgressItem[]
  loading: boolean
  filters: AdminProjectProgressFilters
  onFiltersChange: (filters: AdminProjectProgressFilters) => void
  managerOptions: User[]
  onCreateProject: () => void
}

function statusVariant(status: ProjectStatus): 'brand' | 'success' | 'danger' | 'warning' | 'info' | 'neutral' {
  if (status === 'COMPLETED') return 'success'
  if (status === 'ACTIVE') return 'info'
  if (status === 'ON_HOLD') return 'warning'
  if (status === 'CANCELLED') return 'danger'
  if (status === 'PLANNING') return 'brand'
  return 'neutral'
}

export function AdminProjectsView({ projects, loading, filters, onFiltersChange, managerOptions, onCreateProject }: Props) {
  const [showCompletionLine, setShowCompletionLine] = useState(true)
  const [showTimelineLine, setShowTimelineLine] = useState(true)
  const completed = projects.filter(project => project.status === 'COMPLETED').length
  const active = projects.filter(project => project.status === 'ACTIVE').length
  const averageCompletion = projects.length
    ? Math.round(projects.reduce((total, project) => total + project.completionRate, 0) / projects.length)
    : 0
  const chartData = projects.map(project => ({
    name: project.code,
    completion: Math.round(project.completionRate),
    timeline: project.timelineProgress,
  }))

  return <section>
    <PageHeader
      icon={<FolderKanban size={22} />}
      title="Quản lý dự án"
      description="Theo dõi tiến độ thực tế so với thời gian đã sử dụng và giao dự án mới cho quản lý."
      actions={<Button leadingIcon={<Plus size={16} />} onClick={onCreateProject}>Tạo dự án</Button>}
    />

    <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
      <StatCard title="Tổng dự án hiển thị" value={projects.length} icon={<FolderKanban size={20} />} variant="brand" />
      <StatCard title="Đang triển khai" value={active} icon={<TrendingUp size={20} />} variant="info" />
      <StatCard title="Đã hoàn thành" value={completed} icon={<CheckCircle2 size={20} />} variant="success" />
      <StatCard title="Hoàn thành trung bình" value={`${averageCompletion}%`} icon={<Clock3 size={20} />} variant="warning" />
    </div>

    <div className="mt-6 rounded-2xl border border-line bg-white p-5 shadow-2xs">
      <div className="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
        <div>
          <h2 className="text-base font-bold text-ink">Tiến độ và thời gian dự án</h2>
          <p className="mt-1 text-sm text-muted">Đường cam là tỷ lệ task hoàn thành; đường xanh là tỷ lệ thời gian dự án đã sử dụng.</p>
        </div>
        <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-[minmax(220px,1fr)_190px_220px]">
          <Input
            aria-label="Tìm dự án"
            value={filters.keyword}
            onChange={event => onFiltersChange({ ...filters, keyword: event.target.value })}
            leadingIcon={<Search size={16} />}
            placeholder="Tìm mã hoặc tên dự án..."
          />
          <Select
            aria-label="Lọc theo trạng thái"
            value={filters.status}
            onChange={event => onFiltersChange({ ...filters, status: event.target.value as AdminProjectProgressFilters['status'] })}
            options={[
              { label: 'Mọi trạng thái', value: '' },
              ...Object.entries(projectStatusLabels).map(([value, label]) => ({ value, label })),
            ]}
          />
          <Select
            aria-label="Lọc theo quản lý"
            value={filters.managerUserId}
            onChange={event => onFiltersChange({ ...filters, managerUserId: event.target.value })}
            options={[
              { label: 'Mọi quản lý', value: '' },
              ...managerOptions.map(manager => ({ label: `${manager.username} — ${manager.email}`, value: manager.id })),
            ]}
          />
        </div>
      </div>

      <div className="mt-4 flex flex-wrap items-center justify-center gap-2" aria-label="Ẩn hiện đường biểu đồ">
        <Button
          type="button"
          variant="ghost"
          tone="neutral"
          size="sm"
          className={showCompletionLine ? '!text-brand' : '!text-muted opacity-50'}
          leadingIcon={<span className="relative block h-0.5 w-4 bg-brand"><span className="absolute left-1/2 top-1/2 size-1.5 -translate-x-1/2 -translate-y-1/2 rounded-full bg-brand" /></span>}
          aria-pressed={showCompletionLine}
          onClick={() => setShowCompletionLine(value => !value)}
        >
          Công việc hoàn thành
        </Button>
        <Button
          type="button"
          variant="ghost"
          tone="neutral"
          size="sm"
          className={showTimelineLine ? '!text-info' : '!text-muted opacity-50'}
          leadingIcon={<span className="relative block h-0.5 w-4 bg-info"><span className="absolute left-1/2 top-1/2 size-1.5 -translate-x-1/2 -translate-y-1/2 rounded-full bg-info" /></span>}
          aria-pressed={showTimelineLine}
          onClick={() => setShowTimelineLine(value => !value)}
        >
          Thời gian đã dùng
        </Button>
      </div>

      <div className="mt-5 h-80 w-full" aria-label="Biểu đồ tiến độ và thời gian dự án">
        {loading && projects.length === 0 ? <div className="h-full animate-pulse rounded-xl bg-panel" /> : chartData.length === 0 ? <EmptyState
          className="h-full"
          icon={<FolderKanban size={22} />}
          title="Không có dự án phù hợp"
          description="Thử thay đổi từ khóa hoặc bộ lọc trạng thái."
        /> : <ResponsiveContainer width="100%" height="100%">
          <LineChart data={chartData} margin={{ top: 16, right: 16, left: -12, bottom: 8 }}>
            <CartesianGrid stroke="var(--color-line)" strokeDasharray="4 4" vertical={false} />
            <XAxis dataKey="name" tick={{ fontSize: 11, fill: 'var(--color-muted)' }} />
            <YAxis domain={[0, 100]} tickFormatter={value => `${value}%`} tick={{ fontSize: 11, fill: 'var(--color-muted)' }} />
            <Tooltip formatter={(value, name) => [`${Number(value).toFixed(0)}%`, name === 'completion' ? 'Công việc hoàn thành' : 'Thời gian đã dùng']} />
            <Line hide={!showCompletionLine} type="monotone" dataKey="completion" stroke="var(--color-brand)" strokeWidth={3} dot={{ r: 4 }} activeDot={{ r: 6 }} />
            <Line hide={!showTimelineLine} type="monotone" dataKey="timeline" stroke="var(--color-info)" strokeWidth={3} strokeDasharray="6 4" dot={{ r: 4 }} activeDot={{ r: 6 }} />
          </LineChart>
        </ResponsiveContainer>}
      </div>
    </div>

    <div className="mt-6 overflow-hidden rounded-2xl border border-line bg-white shadow-2xs">
      <div className="border-b border-line px-5 py-4">
        <h2 className="text-base font-bold text-ink">Danh sách tiến độ dự án</h2>
        <p className="mt-1 text-sm text-muted">So sánh tiến độ task với thời gian thực tế của từng dự án.</p>
      </div>
      {projects.length === 0 ? <EmptyState
        bordered={false}
        icon={<FolderKanban size={22} />}
        title="Chưa có dữ liệu dự án"
        description="Tạo dự án mới hoặc thay đổi bộ lọc để xem dữ liệu."
      /> : <div className="divide-y divide-line">
        {projects.map(project => {
          const completion = Math.min(100, Math.max(0, Math.round(project.completionRate)))
          const timeline = Math.min(100, Math.max(0, project.timelineProgress))
          const behindSchedule = completion + 10 < timeline && project.status === 'ACTIVE'
          return <article key={project.id} className="p-5 transition-colors hover:bg-canvas">
            <div className="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
              <div className="min-w-0 lg:w-72">
                <div className="flex items-center gap-2">
                  <p className="truncate text-sm font-bold text-ink">{project.name}</p>
                  <Badge size="sm" variant={statusVariant(project.status)}>{projectStatusLabels[project.status]}</Badge>
                </div>
                <p className="mt-1 text-xs font-medium text-muted">{project.code} · {formatDate(project.startDate)} — {formatDate(project.endDate)}</p>
                <p className="mt-2 inline-flex items-center gap-1.5 text-xs font-semibold text-ink">
                  <UserRound size={13} className="text-info" />
                  Quản lý: {project.managerUsername ?? 'Chưa xác định'}
                  {project.managerEmail && <span className="font-normal text-muted">({project.managerEmail})</span>}
                </p>
              </div>
              <div className="grid min-w-0 flex-1 gap-3 sm:grid-cols-2">
                <div>
                  <div className="mb-1 flex justify-between text-xs font-medium text-muted">
                    <span>Hoàn thành công việc</span><span className="font-bold tabular-nums text-ink">{completion}%</span>
                  </div>
                  <div className="h-2 overflow-hidden rounded-full bg-panel"><div className="h-full rounded-full bg-brand" style={{ width: `${completion}%` }} /></div>
                </div>
                <div>
                  <div className="mb-1 flex justify-between text-xs font-medium text-muted">
                    <span>Thời gian đã sử dụng</span><span className="font-bold tabular-nums text-ink">{timeline}%</span>
                  </div>
                  <div className="h-2 overflow-hidden rounded-full bg-panel"><div className="h-full rounded-full bg-info" style={{ width: `${timeline}%` }} /></div>
                </div>
              </div>
              <div className="lg:w-32 lg:text-right">
                <p className={`text-xs font-bold ${behindSchedule ? 'text-danger' : 'text-success'}`}>{behindSchedule ? 'Chậm tiến độ' : 'Đúng tiến độ'}</p>
                <p className="mt-1 text-xs text-muted">{project.completedTasks}/{project.totalTasks} task</p>
              </div>
            </div>
          </article>
        })}
      </div>}
    </div>
  </section>
}
