import { useState, type FormEvent } from 'react'
import { motion } from 'framer-motion'
import { ArrowRight, CheckCircle2, Eye, EyeOff, LockKeyhole, Mail, ShieldCheck } from 'lucide-react'
import { Button, Input } from '../../../components/ui'

export interface LoginPageProps {
  loading: boolean
  error: string
  onSubmit: (email: string, password: string) => void
}

function Brand() {
  return (
    <div className="flex items-center gap-3 font-bold">
      <span className="text-3xl tracking-[-.09em] text-white">
        HI<span className="text-accent">CAS</span>
      </span>
      <span className="h-7 w-px bg-white/25" />
      <span className="text-sm tracking-[.18em] text-white/70">ONE</span>
    </div>
  )
}

// Cubic bezier matching GSAP expo.out: cubic-bezier(0.16, 1, 0.3, 1)
const EXPO_OUT_EASE = [0.16, 1, 0.3, 1] as const

// 1. Màn hình chia đôi (Split-Screen Reveal) Variants
const leftPanelVariants = {
  initial: { x: '-100%' },
  animate: {
    x: '0%',
    transition: {
      duration: 1.4,
      ease: EXPO_OUT_EASE,
    },
  },
}

const rightPanelVariants = {
  initial: { x: '100%' },
  animate: {
    x: '0%',
    transition: {
      duration: 1.4,
      ease: EXPO_OUT_EASE,
    },
  },
}

// 2. Hiệu ứng Nội dung Form (Staggered Fade-Down Reveal) Variants
const contentContainerVariants = {
  initial: {},
  animate: {
    transition: {
      delayChildren: 0.6, // Trigger after ~60% of split-screen slide
      staggerChildren: 0.1, // 0.1s delay between consecutive elements
    },
  },
}

const fadeDownItemVariants = {
  initial: {
    opacity: 0,
    y: -40,
  },
  animate: {
    opacity: 1,
    y: 0,
    transition: {
      duration: 0.8,
      ease: EXPO_OUT_EASE,
    },
  },
}

export function LoginPage({ loading, error, onSubmit }: LoginPageProps) {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)

  const submit = (event: FormEvent) => {
    event.preventDefault()
    onSubmit(email.trim(), password)
  }

  return (
    <main className="grid min-h-screen bg-canvas overflow-hidden lg:grid-cols-2">
      {/* Nửa Trái: Banner Section */}
      <motion.section
        variants={leftPanelVariants}
        initial="initial"
        animate="animate"
        style={{ willChange: 'transform', transform: 'translateZ(0)' }}
        className="relative hidden overflow-hidden bg-brand-black p-12 text-white lg:flex lg:flex-col lg:justify-between xl:p-16"
      >
        <div className="absolute -right-24 top-[-6rem] size-96 rounded-full border-[70px] border-white/5 pointer-events-none" />
        <div className="absolute -bottom-40 -left-24 size-[30rem] rounded-full border-[80px] border-brand/10 pointer-events-none" />

        <motion.div
          variants={contentContainerVariants}
          initial="initial"
          animate="animate"
          className="relative flex flex-col justify-between h-full"
        >
          <motion.div variants={fadeDownItemVariants} style={{ willChange: 'transform, opacity' }}>
            <Brand />
          </motion.div>

          <div className="relative max-w-xl my-auto">
            <motion.span
              variants={fadeDownItemVariants}
              style={{ willChange: 'transform, opacity' }}
              className="mb-5 inline-flex items-center gap-2 rounded-full border border-brand/30 bg-brand/10 px-3 py-1.5 text-xs font-semibold uppercase tracking-[.16em] text-accent"
            >
              <ShieldCheck size={15} /> Nền tảng quản trị nội bộ
            </motion.span>

            <motion.h1
              variants={fadeDownItemVariants}
              style={{ willChange: 'transform, opacity' }}
              className="text-5xl font-bold leading-[1.08] tracking-[-.045em]"
            >
              Kết nối đội ngũ.
              <br />
              Vận hành hiệu quả.
            </motion.h1>

            <motion.p
              variants={fadeDownItemVariants}
              style={{ willChange: 'transform, opacity' }}
              className="mt-6 max-w-lg text-lg leading-8 text-white/65"
            >
              Một không gian thống nhất để HiCAS quản lý thành viên, dự án và tiến độ công việc.
            </motion.p>

            <motion.div
              variants={fadeDownItemVariants}
              style={{ willChange: 'transform, opacity' }}
              className="mt-10 grid grid-cols-2 gap-4"
            >
              {['Phân quyền rõ ràng', 'Bảo mật phiên đăng nhập'].map(text => (
                <div key={text} className="flex items-center gap-2 text-sm text-white/80">
                  <CheckCircle2 className="text-accent" size={18} />
                  {text}
                </div>
              ))}
            </motion.div>
          </div>

          <motion.p
            variants={fadeDownItemVariants}
            style={{ willChange: 'transform, opacity' }}
            className="relative text-xs text-white/35"
          >
            © 2026 HiCAS · HiCAS One Workspace
          </motion.p>
        </motion.div>
      </motion.section>

      {/* Nửa Phải: Form Login Section */}
      <motion.section
        variants={rightPanelVariants}
        initial="initial"
        animate="animate"
        style={{ willChange: 'transform', transform: 'translateZ(0)' }}
        className="flex items-center justify-center p-6 sm:p-10"
      >
        <motion.div
          variants={contentContainerVariants}
          initial="initial"
          animate="animate"
          className="w-full max-w-md rounded-2xl border border-line bg-white p-6 shadow-[0_18px_60px_rgba(7,21,125,.08)] sm:p-9 lg:border-0 lg:bg-transparent lg:p-0 lg:shadow-none"
        >
          <motion.div
            variants={fadeDownItemVariants}
            style={{ willChange: 'transform, opacity' }}
            className="mb-10 lg:hidden"
          >
            <div className="inline-flex rounded-xl bg-brand px-4 py-2">
              <Brand />
            </div>
          </motion.div>

          <motion.p
            variants={fadeDownItemVariants}
            style={{ willChange: 'transform, opacity' }}
            className="text-sm font-semibold text-brand uppercase tracking-wider"
          >
            CHÀO MỪNG TRỞ LẠI
          </motion.p>

          <motion.h2
            variants={fadeDownItemVariants}
            style={{ willChange: 'transform, opacity' }}
            className="mt-2 text-[28px] font-bold leading-9 tracking-tight text-ink"
          >
            Đăng nhập HiCAS One
          </motion.h2>

          <motion.p
            variants={fadeDownItemVariants}
            style={{ willChange: 'transform, opacity' }}
            className="mt-3 text-sm leading-6 text-muted"
          >
            Sử dụng tài khoản do quản trị viên HiCAS cấp.
          </motion.p>

          <form className="mt-8 space-y-5" onSubmit={submit}>
            <motion.div variants={fadeDownItemVariants} style={{ willChange: 'transform, opacity' }}>
              <Input
                name="email"
                label="Email"
                type="email"
                autoComplete="email"
                placeholder="admin@hicas.vn"
                leadingIcon={<Mail size={18} />}
                value={email}
                onChange={event => setEmail(event.target.value)}
                required
              />
            </motion.div>

            <motion.div variants={fadeDownItemVariants} style={{ willChange: 'transform, opacity' }}>
              <Input
                name="password"
                label="Mật khẩu"
                type={showPassword ? 'text' : 'password'}
                autoComplete="current-password"
                placeholder="Tối thiểu 6 ký tự"
                leadingIcon={<LockKeyhole size={18} />}
                value={password}
                onChange={event => setPassword(event.target.value)}
                minLength={6}
                required
                trailing={
                  <Button
                    type="button"
                    variant="ghost"
                    size="sm"
                    iconOnly
                    className="!size-8"
                    leadingIcon={showPassword ? <EyeOff size={17} /> : <Eye size={17} />}
                    aria-label={showPassword ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'}
                    onClick={() => setShowPassword(value => !value)}
                  />
                }
              />
            </motion.div>

            {error && (
              <motion.p
                variants={fadeDownItemVariants}
                style={{ willChange: 'transform, opacity' }}
                role="alert"
                className="rounded-xl bg-[#fff0f0] px-4 py-3 text-sm text-danger"
              >
                {error}
              </motion.p>
            )}

            <motion.div variants={fadeDownItemVariants} style={{ willChange: 'transform, opacity' }}>
              <Button
                className="w-full"
                loading={loading}
                type="submit"
                trailingIcon={!loading && <ArrowRight size={18} />}
              >
                Đăng nhập
              </Button>
            </motion.div>
          </form>

          <motion.p
            variants={fadeDownItemVariants}
            style={{ willChange: 'transform, opacity' }}
            className="mt-8 text-center text-xs text-muted"
          >
            Cần hỗ trợ? Liên hệ quản trị viên hệ thống.
          </motion.p>
        </motion.div>
      </motion.section>
    </main>
  )
}
