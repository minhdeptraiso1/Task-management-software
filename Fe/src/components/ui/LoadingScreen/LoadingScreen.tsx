import { useEffect, useState, useRef } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { Cpu, Activity } from 'lucide-react'

export interface LoadingScreenProps {
  onLoadingComplete?: () => void
  brandName?: string
  subtitle?: string
  tagline?: string
  minDuration?: number
  autoExit?: boolean
}

// Stage progress milestones & status messages adapted for HiCAS One Project Management
const LOADING_STEPS = [
  { minProgress: 0, maxProgress: 24, status: 'ĐANG KHỞI TẠO HỆ THỐNG...', detail: 'Thiết lập phiên làm việc & mã hóa bảo mật' },
  { minProgress: 25, maxProgress: 49, status: 'ĐANG TẢI DỮ LIỆU DỰ ÁN...', detail: 'Đồng bộ danh sách công việc, backlog & sprint' },
  { minProgress: 50, maxProgress: 74, status: 'TÍNH TOÁN HIỆU SUẤT ĐỘI NGŨ...', detail: 'Phân tích tiến độ công việc & báo cáo lỗi QA' },
  { minProgress: 75, maxProgress: 98, status: 'ĐỒNG BỘ BẢNG KANBAN TIẾN ĐỘ...', detail: 'Kết nối máy chủ thời gian thực' },
  { minProgress: 99, maxProgress: 100, status: 'HỆ THỐNG ĐÃ SẴN SÀNG', detail: 'Đang mở không gian làm việc HiCAS One' },
]

export function LoadingScreen({
  onLoadingComplete,
  brandName = 'HiCAS ONE',
  subtitle = 'NỀN TẢNG QUẢN TRỊ DỰ ÁN & TIẾN ĐỘ DOANH NGHIỆP',
  tagline = 'TỐI ƯU HÓA QUY TRÌNH LÀM VIỆC DỰ ÁN',
  minDuration = 3600,
  autoExit = true,
}: LoadingScreenProps) {
  const [progress, setProgress] = useState(0)
  const [stage, setStage] = useState<1 | 2 | 3 | 4 | 5>(1)
  const [currentStepIndex, setCurrentStepIndex] = useState(0)
  const [isExiting, setIsExiting] = useState(false)

  // Use refs to avoid re-triggering effect loop when inline callbacks or parent state changes
  const onCompleteRef = useRef(onLoadingComplete)
  onCompleteRef.current = onLoadingComplete

  const autoExitRef = useRef(autoExit)
  autoExitRef.current = autoExit

  const minDurationRef = useRef(minDuration)
  minDurationRef.current = minDuration

  const hasFinishedRef = useRef(false)

  useEffect(() => {
    // Stage 1: Logo reveal (0ms)
    const t1 = setTimeout(() => setStage(2), 400)   // Stage 2: Title slide up
    const t2 = setTimeout(() => setStage(3), 800)   // Stage 3: Subtitle slogan
    const t3 = setTimeout(() => setStage(4), 1200)  // Stage 4: Progress counting starts

    const startTime = performance.now() + 1200
    const duration = Math.max(1500, minDurationRef.current - 1200)

    let animationFrameId: number

    const updateProgress = (now: number) => {
      if (hasFinishedRef.current) return

      const elapsed = Math.max(0, now - startTime)
      const rawProgress = Math.min(100, Math.floor((elapsed / duration) * 100))

      // Enforce strictly monotonic progression (never decrease or reset)
      setProgress(prev => {
        const nextVal = Math.max(prev, rawProgress)

        // Find step index based on new nextVal
        const stepIdx = LOADING_STEPS.findIndex(
          step => nextVal >= step.minProgress && nextVal <= step.maxProgress
        )
        if (stepIdx !== -1) {
          setCurrentStepIndex(stepIdx)
        }

        return nextVal
      })

      if (rawProgress < 100) {
        animationFrameId = requestAnimationFrame(updateProgress)
      } else {
        // Reached 100%: set finished & trigger exit transition
        hasFinishedRef.current = true
        setStage(5)
        if (autoExitRef.current) {
          setTimeout(() => {
            setIsExiting(true)
            setTimeout(() => {
              onCompleteRef.current?.()
            }, 600) // matches fade-out/scale transition duration
          }, 350)
        }
      }
    }

    // Start progress loop
    animationFrameId = requestAnimationFrame(updateProgress)

    return () => {
      clearTimeout(t1)
      clearTimeout(t2)
      clearTimeout(t3)
      cancelAnimationFrame(animationFrameId)
    }
  }, []) // Empty dependency array ensures timer runs ONCE from 0 to 100% without interruptions!

  if (isExiting && autoExit) return null

  return (
    <AnimatePresence>
      <motion.div
        initial={{ opacity: 1, scale: 1 }}
        animate={stage === 5 && isExiting ? { opacity: 0, scale: 1.08 } : { opacity: 1, scale: 1 }}
        transition={{ duration: 0.8, ease: [0.16, 1, 0.3, 1] }}
        className="fixed inset-0 z-[99999] flex flex-col items-center justify-between overflow-hidden bg-[#08090c] p-6 sm:p-12 text-white select-none"
      >
        {/* Background Ambient Glow & Light Beams */}
        <div className="absolute inset-0 pointer-events-none overflow-hidden">
          {/* Main Radial Light behind logo */}
          <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 size-[600px] sm:size-[800px] rounded-full bg-gradient-to-tr from-[#f7941d]/20 via-[#ffc20e]/10 to-transparent blur-[120px] animate-ambient-pulse" />
          
          {/* Subtle Ambient Beams */}
          <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 size-[900px] rounded-full bg-[radial-gradient(circle_at_center,rgba(247,148,29,0.08)_0%,transparent_70%)] animate-light-beam" />
          
          {/* Cinematic Background Grid Lines */}
          <div 
            className="absolute inset-0 opacity-[0.03]"
            style={{
              backgroundImage: `linear-gradient(to right, #ffffff 1px, transparent 1px), linear-gradient(to bottom, #ffffff 1px, transparent 1px)`,
              backgroundSize: '80px 80px'
            }}
          />

          {/* Vignette dark edge gradient */}
          <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_center,transparent_0%,rgba(8,9,12,0.85)_80%)]" />
        </div>

        {/* Top Header Tag: System Status indicator */}
        <motion.div
          initial={{ opacity: 0, y: -20 }}
          animate={{ opacity: stage >= 1 ? 0.7 : 0, y: stage >= 1 ? 0 : -20 }}
          transition={{ duration: 0.8, delay: 0.2 }}
          className="relative z-10 flex items-center gap-2 text-[10px] sm:text-xs font-mono tracking-[0.3em] uppercase text-white/50"
        >
          
          <span>{tagline}</span>
        </motion.div>

        {/* Center Section: Logo, Brand Title & Slogan */}
        <div className="relative z-10 my-auto flex flex-col items-center text-center max-w-2xl px-4">
          
          {/* GIAI ĐOẠN 1: Luxury Geometric Shield/Crown Emblem with Glowing Aura */}
          <motion.div
            initial={{ opacity: 0, scale: 0.75, filter: 'blur(10px)' }}
            animate={{ 
              opacity: stage >= 1 ? 1 : 0, 
              scale: stage >= 1 ? 1 : 0.75,
              filter: stage >= 1 ? 'blur(0px)' : 'blur(10px)'
            }}
            transition={{ duration: 1, ease: [0.16, 1, 0.3, 1] }}
            className="relative mb-8 sm:mb-12 group cursor-default"
          >
            {/* Outer Rotating Glowing Ring */}
            <div className="absolute -inset-4 rounded-3xl bg-gradient-to-tr from-[#f7941d]/30 via-[#ffc20e]/20 to-transparent blur-md opacity-75 group-hover:opacity-100 transition-opacity" />
            
            {/* Emblem Container */}
            <div className="relative flex items-center justify-center size-24 sm:size-28 rounded-2xl border border-white/15 bg-white/[0.03] backdrop-blur-xl shadow-[0_0_50px_rgba(247,148,29,0.25)] p-5">
              <svg viewBox="0 0 100 100" className="size-full text-[#f7941d]">
                <defs>
                  <linearGradient id="goldGradient" x1="0%" y1="0%" x2="100%" y2="100%">
                    <stop offset="0%" stopColor="#ffffff" />
                    <stop offset="40%" stopColor="#f7941d" />
                    <stop offset="100%" stopColor="#ffc20e" />
                  </linearGradient>
                </defs>

                {/* Outer Luxury Shield Frame */}
                <polygon 
                  points="50,5 90,25 90,75 50,95 10,75 10,25" 
                  fill="none" 
                  stroke="url(#goldGradient)" 
                  strokeWidth="3"
                  className="opacity-90"
                />

                {/* Inner Hexagon Layer */}
                <polygon 
                  points="50,15 82,31 82,69 50,85 18,69 18,31" 
                  fill="none" 
                  stroke="url(#goldGradient)" 
                  strokeWidth="1.5"
                  strokeDasharray="4 2"
                  className="opacity-60"
                />

                {/* Center Stylized 'H' Emblem */}
                <path 
                  d="M 35,32 L 35,68 M 65,32 L 65,68 M 35,50 L 65,50" 
                  stroke="url(#goldGradient)" 
                  strokeWidth="5" 
                  strokeLinecap="round" 
                />

                {/* Sparkling Core Diamonds */}
                <polygon points="50,22 54,26 50,30 46,26" fill="#ffffff" />
                <polygon points="50,70 54,74 50,78 46,74" fill="#ffc20e" />
              </svg>
            </div>
          </motion.div>

          {/* GIAI ĐOẠN 2: Tên thương hiệu lớn (Cinzel / Luxury Serif) + Fade & Slide Up */}
          <motion.div
            initial={{ opacity: 0, y: 35 }}
            animate={{ 
              opacity: stage >= 2 ? 1 : 0, 
              y: stage >= 2 ? 0 : 35 
            }}
            transition={{ duration: 0.9, ease: [0.16, 1, 0.3, 1] }}
            className="space-y-2"
          >
            <h1 className="font-cinzel text-4xl sm:text-6xl md:text-7xl font-extrabold tracking-[-0.03em] uppercase leading-none">
              {brandName === 'HiCAS ONE' ? (
                <>
                  <span className="text-white">HI</span>
                  <span className="text-gold-shimmer ml-1 sm:ml-2">CAS</span>
                  <span className="text-white/40 ml-3 font-light tracking-[0.15em] text-3xl sm:text-5xl md:text-6xl">ONE</span>
                </>
              ) : (
                <span className="text-gold-shimmer">{brandName}</span>
              )}
            </h1>
          </motion.div>

          {/* GIAI ĐOẠN 3: Subtitle Slogan bên dưới với Cormorant Garamond / Luxury Letter Spacing */}
          <motion.p
            initial={{ opacity: 0, y: 20 }}
            animate={{ 
              opacity: stage >= 3 ? 1 : 0, 
              y: stage >= 3 ? 0 : 20 
            }}
            transition={{ duration: 0.8, delay: 0.1, ease: [0.16, 1, 0.3, 1] }}
            className="mt-4 sm:mt-5 font-cormorant text-base sm:text-xl md:text-2xl font-medium tracking-[0.25em] text-white/70 uppercase max-w-xl"
          >
            {subtitle}
          </motion.p>
        </div>

        {/* GIAI ĐOẠN 4: Progress Bar & Monospace Initialization Status */}
        <motion.div
          initial={{ opacity: 0, y: 30 }}
          animate={{ 
            opacity: stage >= 4 ? 1 : 0, 
            y: stage >= 4 ? 0 : 30 
          }}
          transition={{ duration: 0.8, ease: [0.16, 1, 0.3, 1] }}
          className="relative z-10 w-full max-w-md space-y-4"
        >
          {/* Status Label & Counter */}
          <div className="flex items-center justify-between text-xs font-space-mono text-white/70">
            <div className="flex items-center gap-2">
              <Activity className="size-3.5 text-[#f7941d] animate-pulse" />
              <span className="tracking-wider uppercase text-white/90">
                {LOADING_STEPS[currentStepIndex]?.status || 'ĐANG KHỞI TẠO...'}
              </span>
            </div>
            <span className="font-bold text-[#ffc20e] tracking-widest text-sm">
              [{progress}%]
            </span>
          </div>

          {/* Progress Bar Container */}
          <div className="relative h-2.5 w-full rounded-full bg-[#121318] p-0.5 border border-[#f7941d]/30 shadow-[inset_0_1px_4px_rgba(0,0,0,0.9)] overflow-hidden">
            {/* Animated Glow Fill */}
            <div
              className="h-full rounded-full bg-gradient-to-r from-[#f7941d] via-[#ffc20e] to-[#ffffff] shadow-[0_0_20px_rgba(247,148,29,0.95)] transition-all duration-100 ease-out relative"
              style={{ width: `${progress}%` }}
            >
              {/* Glowing Leading Head Dot */}
              {progress > 0 && (
                <div className="absolute right-0 top-1/2 -translate-y-1/2 size-2 rounded-full bg-white shadow-[0_0_8px_#ffffff]" />
              )}
            </div>
          </div>

          {/* Micro Status Detail */}
          <div className="flex items-center justify-between text-[11px] font-mono text-white/40 pt-1">
            <span className="truncate">
              {LOADING_STEPS[currentStepIndex]?.detail || 'Đang tải dữ liệu hệ thống'}
            </span>
            <span className="flex items-center gap-1 text-[#f7941d]/80 text-[10px]">
              <Cpu size={12} /> HỆ THỐNG HICAS V2.6
            </span>
          </div>
        </motion.div>
      </motion.div>
    </AnimatePresence>
  )
}
