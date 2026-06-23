import { CalendarDays, CheckCircle2, CircleUserRound, LayoutDashboard, LogOut, Mail, ShieldCheck, Sparkles, type LucideIcon } from 'lucide-react'
import { Button } from '../../../components/ui'
import { roleDescriptions, roleLabels, type User } from '../models/user.model'

interface UserHomeViewProps {
  user: User
  onLogout: () => void
}

const overview: { label: string; icon: LucideIcon }[] = [
  { label: 'Công việc đang làm', icon: CircleUserRound },
  { label: 'Đã hoàn thành', icon: CheckCircle2 },
  { label: 'Lịch sắp tới', icon: CalendarDays },
]

export function UserHomeView({ user, onLogout }: UserHomeViewProps) {
  return <div className="min-h-screen bg-canvas">
    <header className="flex h-14 items-center justify-between bg-brand-black px-5 text-white shadow-sm md:px-6">
      <div className="flex items-center gap-3 font-bold">
        <span className="text-2xl tracking-[-.08em]">HI<span className="text-brand">CAS</span></span>
        <span className="h-6 w-px bg-white/20" />
        <span className="text-sm tracking-normal text-white/55">ONE</span>
      </div>
      <div className="flex items-center gap-3">
        <div className="hidden text-right sm:block">
          <p className="text-sm font-semibold">{user.username}</p>
          <p className="text-xs text-white/45">{roleLabels[user.role]}</p>
        </div>
        <span className="grid size-10 place-items-center rounded-full bg-panel font-bold text-brand">{user.username.slice(0, 2).toUpperCase()}</span>
        <Button className="!text-white" variant="ghost" size="sm" onClick={onLogout} leadingIcon={<LogOut size={18} />}>Đăng xuất</Button>
      </div>
    </header>

    <main className="mx-auto max-w-6xl p-5 md:p-8">
      <section className="relative overflow-hidden rounded-xl bg-brand-black px-6 py-8 text-white md:px-10">
        <div className="absolute -right-12 -top-20 size-64 rounded-full border-[50px] border-brand/10" />
        <div className="relative max-w-2xl">
          <span className="inline-flex items-center gap-2 rounded-full bg-brand/10 px-3 py-1.5 text-xs font-semibold uppercase tracking-widest text-accent">
            <Sparkles size={14} />
            Không gian {roleLabels[user.role]}
          </span>
          <h1 className="mt-5 text-[28px] font-bold leading-9">Xin chào, {user.username}</h1>
          <p className="mt-3 max-w-xl leading-7 text-white/70">
            Chào mừng bạn đến với HiCAS One. Giao diện này chỉ tải dữ liệu thuộc quyền của vai trò hiện tại, không gọi API quản trị.
          </p>
        </div>
      </section>

      <section className="mt-6 grid gap-5 lg:grid-cols-[1.4fr_.6fr]">
        <article className="rounded-2xl border border-line bg-white p-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-semibold text-brand">Hôm nay</p>
              <h2 className="mt-1 text-xl font-bold">Tổng quan công việc</h2>
            </div>
            <span className="grid size-11 place-items-center rounded-xl bg-panel text-brand">
              <LayoutDashboard size={21} />
            </span>
          </div>
          <div className="mt-8 grid gap-4 sm:grid-cols-3">
            {overview.map(({ label, icon: Icon }) => (
              <div key={label} className="rounded-xl border border-line bg-canvas p-4">
                <Icon className="text-brand" size={20} />
                <p className="mt-5 text-2xl font-bold">00</p>
                <p className="mt-1 text-xs text-muted">{label}</p>
              </div>
            ))}
          </div>
          <div className="mt-5 rounded-xl border border-dashed border-line p-8 text-center">
            <p className="font-semibold">Chưa có công việc được giao</p>
            <p className="mt-1 text-sm text-muted">Dữ liệu sẽ hiển thị khi module dự án được kích hoạt cho vai trò này.</p>
          </div>
        </article>

        <aside className="rounded-2xl border border-line bg-white p-6">
          <div className="flex items-center gap-3">
            <span className="grid size-11 place-items-center rounded-xl bg-[#fff1e5] text-brand">
              <ShieldCheck size={22} />
            </span>
            <div>
              <h2 className="font-bold">Tài khoản</h2>
              <p className="text-xs text-muted">Thông tin phiên hiện tại</p>
            </div>
          </div>
          <dl className="mt-6 space-y-5 text-sm">
            <div>
              <dt className="text-xs text-muted">Tên đăng nhập</dt>
              <dd className="mt-1 flex items-center gap-2 font-semibold"><CircleUserRound size={16} className="text-brand" />{user.username}</dd>
            </div>
            <div>
              <dt className="text-xs text-muted">Email</dt>
              <dd className="mt-1 flex items-center gap-2 font-semibold"><Mail size={16} className="text-brand" />{user.email}</dd>
            </div>
            <div>
              <dt className="text-xs text-muted">Vai trò</dt>
              <dd className="mt-1 font-semibold text-ink">{roleLabels[user.role]}</dd>
              <dd className="mt-1 text-xs leading-5 text-muted">{roleDescriptions[user.role]}</dd>
            </div>
            <div>
              <dt className="text-xs text-muted">Trạng thái</dt>
              <dd className="mt-1 inline-flex items-center gap-2 font-semibold text-success"><i className="size-2 rounded-full bg-success" />Đang hoạt động</dd>
            </div>
          </dl>
        </aside>
      </section>
    </main>
  </div>
}
