import React from 'react'
import { motion } from 'framer-motion'

export interface DashboardLayoutProps {
  header: React.ReactNode
  sidebar: React.ReactNode
  children: React.ReactNode
  sidebarCollapsed?: boolean
}

// Cubic bezier matching GSAP expo.out: cubic-bezier(0.16, 1, 0.3, 1)
const EXPO_OUT_EASE = [0.16, 1, 0.3, 1] as const

// 1. Sidebar Entry Animation: translateX -100% -> 0 in 1.0s, no delay
const sidebarVariants = {
  initial: { x: '-100%' },
  animate: {
    x: '0%',
    transition: {
      duration: 1.0,
      ease: EXPO_OUT_EASE,
    },
  },
}

// 2. Header Topbar Entry Animation: translateY -100%, opacity 0 -> translateY 0, opacity 1 in 0.8s, delay 0.2s
const headerVariants = {
  initial: { y: '-100%', opacity: 0 },
  animate: {
    y: '0%',
    opacity: 1,
    transition: {
      duration: 0.8,
      delay: 0.2,
      ease: EXPO_OUT_EASE,
    },
  },
}

// 3. Main Content Container Variant (Delay 0.4s total after header & sidebar shape up)
const mainContentVariants = {
  initial: {},
  animate: {
    transition: {
      delayChildren: 0.4,
      staggerChildren: 0.12,
    },
  },
}

export function DashboardLayout({
  header,
  sidebar,
  children,
  sidebarCollapsed = false,
}: DashboardLayoutProps) {
  return (
    <div className="min-h-screen bg-canvas text-ink overflow-x-hidden">
      {/* Header Topbar Animation (Runs ONCE on layout mount) */}
      <motion.header
        variants={headerVariants}
        initial="initial"
        animate="animate"
        style={{ willChange: 'transform, opacity', transform: 'translateZ(0)' }}
        className="sticky top-0 z-30 w-full"
      >
        {header}
      </motion.header>

      {/* Main Container */}
      <div
        className={`grid gap-5 p-5 transition-[grid-template-columns] duration-300 items-start grid-cols-1 ${
          sidebarCollapsed
            ? 'lg:grid-cols-[76px_minmax(0,1fr)]'
            : 'lg:grid-cols-[320px_minmax(0,1fr)] xl:grid-cols-[380px_minmax(0,1fr)]'
        }`}
      >
        {/* Sidebar Animation (Runs ONCE on layout mount) */}
        <motion.aside
          variants={sidebarVariants}
          initial="initial"
          animate="animate"
          style={{ willChange: 'transform', transform: 'translateZ(0)' }}
          className={`sticky top-20 flex h-auto lg:h-[calc(100vh-96px)] flex-col rounded-xl border border-line bg-white transition-all duration-300 ${
            sidebarCollapsed ? 'overflow-hidden' : ''
          }`}
        >
          {sidebar}
        </motion.aside>

        {/* Main Content Animation (Staggered Fade-Up Reveal) */}
        <motion.main
          variants={mainContentVariants}
          initial="initial"
          animate="animate"
          className="min-w-0 flex-1"
        >
          {children}
        </motion.main>
      </div>
    </div>
  )
}

// ----------------------------------------------------------------------
// REUSABLE STAGGER LAYER COMPONENTS FOR MAIN CONTENT
// ----------------------------------------------------------------------

// Standard Fade-Up Item Variant (opacity 0, y 30 -> opacity 1, y 0 in 0.8s)
const fadeUpItemVariants = {
  initial: { opacity: 0, y: 30 },
  animate: {
    opacity: 1,
    y: 0,
    transition: {
      duration: 0.8,
      ease: EXPO_OUT_EASE,
    },
  },
}

// Lớp 1: Tiêu đề / Banner Hero Section
export function DashboardBannerLayer({ children, className = '' }: { children: React.ReactNode; className?: string }) {
  return (
    <motion.div
      variants={fadeUpItemVariants}
      style={{ willChange: 'transform, opacity' }}
      className={className}
    >
      {children}
    </motion.div>
  )
}

// Lớp 2: Metric Cards Row Container with 0.1s Stagger
const cardsContainerVariants = {
  initial: {},
  animate: {
    transition: {
      staggerChildren: 0.1,
    },
  },
}

export function DashboardCardsRowLayer({ children, className = '' }: { children: React.ReactNode; className?: string }) {
  return (
    <motion.div
      variants={cardsContainerVariants}
      className={className}
    >
      {children}
    </motion.div>
  )
}

export function DashboardCardItem({ children, className = '' }: { children: React.ReactNode; className?: string }) {
  return (
    <motion.div
      variants={fadeUpItemVariants}
      style={{ willChange: 'transform, opacity' }}
      className={className}
    >
      {children}
    </motion.div>
  )
}

// Lớp 3: Data Table / List Layer with Row Stagger (0.05s per row)
const tableContainerVariants = {
  initial: { opacity: 0, y: 30 },
  animate: {
    opacity: 1,
    y: 0,
    transition: {
      duration: 0.8,
      ease: EXPO_OUT_EASE,
      staggerChildren: 0.05,
      delayChildren: 0.1,
    },
  },
}

export function DashboardTableLayer({ children, className = '' }: { children: React.ReactNode; className?: string }) {
  return (
    <motion.div
      variants={tableContainerVariants}
      style={{ willChange: 'transform, opacity' }}
      className={className}
    >
      {children}
    </motion.div>
  )
}

const tableRowVariants = {
  initial: { opacity: 0, y: 20 },
  animate: {
    opacity: 1,
    y: 0,
    transition: {
      duration: 0.6,
      ease: EXPO_OUT_EASE,
    },
  },
}

export function DashboardTableRow({ children, className = '', ...props }: React.ComponentPropsWithoutRef<typeof motion.tr>) {
  return (
    <motion.tr
      variants={tableRowVariants}
      style={{ willChange: 'transform, opacity' }}
      className={className}
      {...props}
    >
      {children}
    </motion.tr>
  )
}
