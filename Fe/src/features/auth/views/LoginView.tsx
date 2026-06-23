import { useState, type FormEvent } from 'react'
import { ArrowRight, CheckCircle2, Eye, EyeOff, LockKeyhole, Mail, ShieldCheck } from 'lucide-react'
import { Button, Input } from '../../../components/ui'

interface LoginViewProps { loading: boolean; error: string; onSubmit: (email: string, password: string) => void }

function Brand() {
  return <div className="flex items-center gap-3 font-bold"><span className="text-3xl tracking-[-.09em] text-white">HI<span className="text-accent">CAS</span></span><span className="h-7 w-px bg-white/25" /><span className="text-sm tracking-[.18em] text-white/70">ONE</span></div>
}

export function LoginView({ loading, error, onSubmit }: LoginViewProps) {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const submit = (event: FormEvent) => { event.preventDefault(); onSubmit(email.trim(), password) }

  return <main className="grid min-h-screen bg-canvas lg:grid-cols-[1.02fr_.98fr]">
    <section className="relative hidden overflow-hidden bg-brand-black p-12 text-white lg:flex lg:flex-col lg:justify-between xl:p-16">
      <div className="absolute -right-24 top-[-6rem] size-96 rounded-full border-[70px] border-white/5" />
      <div className="absolute -bottom-40 -left-24 size-[30rem] rounded-full border-[80px] border-brand/10" />
      <div className="relative"><Brand /></div>
      <div className="relative max-w-xl">
        <span className="mb-5 inline-flex items-center gap-2 rounded-full border border-brand/30 bg-brand/10 px-3 py-1.5 text-xs font-semibold uppercase tracking-[.16em] text-accent"><ShieldCheck size={15} />Nền tảng quản trị nội bộ</span>
        <h1 className="text-5xl font-bold leading-[1.08] tracking-[-.045em]">Kết nối đội ngũ.<br />Vận hành hiệu quả.</h1>
        <p className="mt-6 max-w-lg text-lg leading-8 text-white/65">Một không gian thống nhất để HiCAS quản lý thành viên, dự án và tiến độ công việc.</p>
        <div className="mt-10 grid grid-cols-2 gap-4">{['Phân quyền rõ ràng', 'Bảo mật phiên đăng nhập'].map(text => <div key={text} className="flex items-center gap-2 text-sm text-white/80"><CheckCircle2 className="text-accent" size={18} />{text}</div>)}</div>
      </div>
      <p className="relative text-xs text-white/35">© 2026 HiCAS · HiCAS One Workspace</p>
    </section>
    <section className="flex items-center justify-center p-6 sm:p-10">
      <div className="animate-enter w-full max-w-md rounded-2xl border border-line bg-white p-6 shadow-[0_18px_60px_rgba(7,21,125,.08)] sm:p-9 lg:border-0 lg:bg-transparent lg:p-0 lg:shadow-none">
        <div className="mb-10 lg:hidden"><div className="inline-flex rounded-xl bg-brand px-4 py-2"><Brand /></div></div>
        <p className="text-sm font-semibold text-brand">CHÀO MỪNG TRỞ LẠI</p>
        <h2 className="mt-2 text-[28px] font-bold leading-9 tracking-tight text-ink">Đăng nhập HiCAS One</h2>
        <p className="mt-3 text-sm leading-6 text-muted">Sử dụng tài khoản do quản trị viên HiCAS cấp.</p>
        <form className="mt-8 space-y-5" onSubmit={submit}>
          <Input name="email" label="Email" type="email" autoComplete="email" placeholder="admin@hicas.vn" leadingIcon={<Mail size={18} />} value={email} onChange={event => setEmail(event.target.value)} required />
          <Input name="password" label="Mật khẩu" type={showPassword ? 'text' : 'password'} autoComplete="current-password" placeholder="Tối thiểu 6 ký tự" leadingIcon={<LockKeyhole size={18} />} value={password} onChange={event => setPassword(event.target.value)} minLength={6} required trailing={<Button type="button" variant="ghost" size="sm" iconOnly className="!size-8" leadingIcon={showPassword ? <EyeOff size={17} /> : <Eye size={17} />} aria-label={showPassword ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'} onClick={() => setShowPassword(value => !value)} />} />
          {error && <p role="alert" className="rounded-xl bg-[#fff0f0] px-4 py-3 text-sm text-danger">{error}</p>}
          <Button className="w-full" loading={loading} type="submit" trailingIcon={!loading && <ArrowRight size={18} />}>Đăng nhập</Button>
        </form>
        <p className="mt-8 text-center text-xs text-muted">Cần hỗ trợ? Liên hệ quản trị viên hệ thống.</p>
      </div>
    </section>
  </main>
}
