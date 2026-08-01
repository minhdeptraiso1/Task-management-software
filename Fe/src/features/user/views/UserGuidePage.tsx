import { useState } from 'react'

interface UserGuidePageProps {
  onBack: () => void
}

export function UserGuidePage({ onBack }: UserGuidePageProps) {
  const [activeSection, setActiveSection] = useState<'steps' | 'kanban' | 'review' | 'forms' | 'roles'>('kanban')

  // State Giả lập Kanban (Match 100% UI Image 2)
  const [simTasks, setSimTasks] = useState([
    { id: 'TASK-101', title: 'Xây dựng api đăng nhập', project: 'qwe', status: 'TODO', priority: 'Vừa', tag: 'DEV', isOverdue: true, logged: '0h/1h', assignee: 'Chưa giao' },
    { id: 'TASK-102', title: 'Đi mua sting', project: 'qwe', status: 'TODO', priority: 'Vừa', tag: 'DEV', isOverdue: false, logged: '0h/0h', assignee: 'Chưa giao' },
    { id: 'TASK-103', title: 'Xây dựng product backlog', project: 'qwe', status: 'TODO', priority: 'Vừa', tag: 'DEV', isOverdue: false, logged: '0h/1h', assignee: 'Chưa giao' },
    { id: 'TASK-104', title: 'Mua cơm gà', project: 'qwe', status: 'IN_PROGRESS', priority: 'Vừa', tag: 'DEV', isOverdue: false, logged: '0h/0h', assignee: 'Chưa giao' },
    { id: 'TASK-105', title: 'Xây dựng api đăng nhập OAuth', project: 'qwe', status: 'WAITING', priority: 'Vừa', tag: 'DEV', isOverdue: true, logged: '1h 30m/1h', assignee: 'Tuấn Anh' },
    { id: 'TASK-106', title: 'Kiểm thử giao diện Scrum Board', project: 'qwe', status: 'IN_REVIEW', priority: 'Cao', tag: 'QA', isOverdue: false, logged: '2h/3h', assignee: 'Minh' },
    { id: 'TASK-107', title: 'Tạo cơ sở dữ liệu dự án', project: 'qwe', status: 'DONE', priority: 'Vừa', tag: 'DEV', isOverdue: false, logged: '2h/2h', assignee: 'Hoàng' },
  ])

  // State Giả lập Sprint Review (Match 100% UI Image 3)
  const [reviewTab, setReviewTab] = useState<'report' | 'review' | 'retro'>('report')

  const moveTask = (id: string, targetStatus: string) => {
    setSimTasks(prev => prev.map(t => t.id === id ? { ...t, status: targetStatus } : t))
  }

  const resetSimulator = () => {
    setSimTasks([
      { id: 'TASK-101', title: 'Xây dựng api đăng nhập', project: 'qwe', status: 'TODO', priority: 'Vừa', tag: 'DEV', isOverdue: true, logged: '0h/1h', assignee: 'Chưa giao' },
      { id: 'TASK-102', title: 'Đi mua sting', project: 'qwe', status: 'TODO', priority: 'Vừa', tag: 'DEV', isOverdue: false, logged: '0h/0h', assignee: 'Chưa giao' },
      { id: 'TASK-103', title: 'Xây dựng product backlog', project: 'qwe', status: 'TODO', priority: 'Vừa', tag: 'DEV', isOverdue: false, logged: '0h/1h', assignee: 'Chưa giao' },
      { id: 'TASK-104', title: 'Mua cơm gà', project: 'qwe', status: 'IN_PROGRESS', priority: 'Vừa', tag: 'DEV', isOverdue: false, logged: '0h/0h', assignee: 'Chưa giao' },
      { id: 'TASK-105', title: 'Xây dựng api đăng nhập OAuth', project: 'qwe', status: 'WAITING', priority: 'Vừa', tag: 'DEV', isOverdue: true, logged: '1h 30m/1h', assignee: 'Tuấn Anh' },
      { id: 'TASK-106', title: 'Kiểm thử giao diện Scrum Board', project: 'qwe', status: 'IN_REVIEW', priority: 'Cao', tag: 'QA', isOverdue: false, logged: '2h/3h', assignee: 'Minh' },
      { id: 'TASK-107', title: 'Tạo cơ sở dữ liệu dự án', project: 'qwe', status: 'DONE', priority: 'Vừa', tag: 'DEV', isOverdue: false, logged: '2h/2h', assignee: 'Hoàng' },
    ])
  }

  const totalCount = simTasks.length
  const todoCount = simTasks.filter(t => t.status === 'TODO').length
  const inProgressCount = simTasks.filter(t => t.status === 'IN_PROGRESS').length
  const waitingCount = simTasks.filter(t => t.status === 'WAITING').length
  const inReviewCount = simTasks.filter(t => t.status === 'IN_REVIEW').length
  const doneCount = simTasks.filter(t => t.status === 'DONE').length
  const cancelledCount = simTasks.filter(t => t.status === 'CANCELLED').length

  return (
    <div className="min-h-screen bg-slate-50 text-slate-900 flex flex-col font-sans">
      
      {/* Top Navigation Bar */}
      <header className="sticky top-0 z-40 flex h-14 items-center justify-between border-b border-slate-200 bg-slate-900 px-6 text-white shadow-sm">
        <div className="flex items-center gap-4">
          <button
            onClick={onBack}
            className="flex items-center gap-2 rounded-lg bg-slate-800 px-3 py-1.5 text-xs font-bold text-slate-200 hover:bg-slate-700 hover:text-white transition"
          >
            ← Quay lại
          </button>
          <div>
            <h1 className="text-sm font-extrabold tracking-wide">Hướng dẫn sử dụng dự án & Giả lập UI thực tế</h1>
          </div>
        </div>

        <button
          onClick={onBack}
          className="rounded-lg bg-brand px-4 py-1.5 text-xs font-bold text-white hover:bg-brand-dark transition"
        >
          Về màn hình làm việc
        </button>
      </header>

      {/* Main Container */}
      <div className="mx-auto flex w-full max-w-7xl flex-1 gap-8 p-6 md:p-8">
        
        {/* Sidebar Table of Contents */}
        <aside className="hidden w-64 shrink-0 lg:block">
          <div className="sticky top-20 rounded-2xl border border-slate-200 bg-white p-4 shadow-xs space-y-2">
            <p className="px-3 text-xs font-bold uppercase tracking-wider text-slate-400">Mục lục Hướng dẫn</p>
            <nav className="space-y-1 text-xs font-bold">
              <button
                onClick={() => setActiveSection('kanban')}
                className={`w-full rounded-xl px-3 py-2.5 text-left transition ${
                  activeSection === 'kanban' ? 'bg-brand text-white' : 'text-slate-600 hover:bg-slate-100'
                }`}
              >
                1. Giả lập Bảng Kanban UI
              </button>
              <button
                onClick={() => setActiveSection('review')}
                className={`w-full rounded-xl px-3 py-2.5 text-left transition ${
                  activeSection === 'review' ? 'bg-brand text-white' : 'text-slate-600 hover:bg-slate-100'
                }`}
              >
                2. Giả lập Sprint Review & Báo cáo
              </button>
              <button
                onClick={() => setActiveSection('steps')}
                className={`w-full rounded-xl px-3 py-2.5 text-left transition ${
                  activeSection === 'steps' ? 'bg-brand text-white' : 'text-slate-600 hover:bg-slate-100'
                }`}
              >
                3. Quy trình 7 bước (A đến Z)
              </button>
              <button
                onClick={() => setActiveSection('forms')}
                className={`w-full rounded-xl px-3 py-2.5 text-left transition ${
                  activeSection === 'forms' ? 'bg-brand text-white' : 'text-slate-600 hover:bg-slate-100'
                }`}
              >
                4. Chi tiết Form & Thuật ngữ
              </button>
              <button
                onClick={() => setActiveSection('roles')}
                className={`w-full rounded-xl px-3 py-2.5 text-left transition ${
                  activeSection === 'roles' ? 'bg-brand text-white' : 'text-slate-600 hover:bg-slate-100'
                }`}
              >
                5. Phân quyền vai trò (Roles)
              </button>
            </nav>
          </div>
        </aside>

        {/* Content Body */}
        <main className="flex-1 space-y-8 min-w-0">

          {/* Mobile Navigation Tabs */}
          <div className="flex border-b border-slate-200 bg-white rounded-xl p-1 gap-1 lg:hidden overflow-x-auto shadow-xs">
            <button
              onClick={() => setActiveSection('kanban')}
              className={`px-3 py-2 text-xs font-bold rounded-lg whitespace-nowrap ${
                activeSection === 'kanban' ? 'bg-brand text-white' : 'text-slate-600'
              }`}
            >
              1. Giả lập Kanban
            </button>
            <button
              onClick={() => setActiveSection('review')}
              className={`px-3 py-2 text-xs font-bold rounded-lg whitespace-nowrap ${
                activeSection === 'review' ? 'bg-brand text-white' : 'text-slate-600'
              }`}
            >
              2. Sprint Review
            </button>
            <button
              onClick={() => setActiveSection('steps')}
              className={`px-3 py-2 text-xs font-bold rounded-lg whitespace-nowrap ${
                activeSection === 'steps' ? 'bg-brand text-white' : 'text-slate-600'
              }`}
            >
              3. Quy trình 7 bước
            </button>
            <button
              onClick={() => setActiveSection('forms')}
              className={`px-3 py-2 text-xs font-bold rounded-lg whitespace-nowrap ${
                activeSection === 'forms' ? 'bg-brand text-white' : 'text-slate-600'
              }`}
            >
              4. Form & Thuật ngữ
            </button>
          </div>

          {/* SECTION 1: KANBAN SIMULATOR (100% MATCH UI IMAGE 2) */}
          {activeSection === 'kanban' && (
            <section className="space-y-6">
              <div className="flex items-center justify-between flex-wrap gap-2 border-b border-slate-200 pb-3">
                <div>
                  <h2 className="text-lg font-extrabold text-slate-900">Giả lập Giao diện Bảng Kanban Thực Tế (100% khớp UI)</h2>
                  <p className="text-xs text-slate-500 mt-0.5">Bấm vào các nút di chuyển dưới mỗi thẻ Task để trải nghiệm di chuyển trạng thái</p>
                </div>
                <button
                  onClick={resetSimulator}
                  className="rounded-lg border border-slate-300 bg-white px-3 py-1.5 text-xs font-bold text-slate-700 hover:bg-slate-100 transition"
                >
                  Đặt lại dữ liệu
                </button>
              </div>

              {/* REAL KANBAN UI (MATCHING IMAGE 2 EXACTLY) */}
              <div className="rounded-2xl border border-slate-200 bg-slate-50/60 p-4 overflow-x-auto shadow-xs">
                <div className="grid grid-cols-6 gap-3 min-w-[1000px]">

                  {/* Column 1: Cần làm */}
                  <div className="rounded-xl border border-slate-300 bg-slate-100/70 p-3 space-y-3">
                    <div className="flex items-center justify-between text-xs font-extrabold text-slate-800">
                      <span>Cần làm</span>
                      <span className="rounded-full bg-slate-600 px-2 py-0.5 text-[10px] text-white">{todoCount}</span>
                    </div>

                    <div className="space-y-2.5">
                      {simTasks.filter(t => t.status === 'TODO').map(t => (
                        <div key={t.id} className="rounded-xl border border-blue-200 bg-white p-3 space-y-2 shadow-xs">
                          <div className="flex items-center gap-1.5 text-[10px]">
                            <span className="rounded bg-slate-100 px-1.5 py-0.5 font-bold text-slate-700">{t.tag}</span>
                            {t.isOverdue && <span className="rounded bg-rose-50 px-1.5 py-0.5 font-bold text-rose-600 border border-rose-200">QUÁ HẠN</span>}
                          </div>
                          <p className="text-xs font-bold text-slate-900 leading-snug">{t.title}</p>
                          <p className="text-[10px] text-slate-400 font-mono">- {t.project}</p>
                          <div className="flex items-center justify-between text-[10px] text-slate-500 pt-1 border-t border-slate-100">
                            <span className="rounded bg-blue-50 px-1.5 py-0.5 font-bold text-blue-600">{t.priority}</span>
                            <span>{t.logged}</span>
                            <span>{t.assignee}</span>
                          </div>
                          <button
                            onClick={() => moveTask(t.id, 'IN_PROGRESS')}
                            className="w-full rounded-lg bg-blue-600 py-1 text-[10px] font-bold text-white hover:bg-blue-700 transition"
                          >
                            Đang làm →
                          </button>
                        </div>
                      ))}
                    </div>
                  </div>

                  {/* Column 2: Đang làm */}
                  <div className="rounded-xl border border-blue-200 bg-blue-50/50 p-3 space-y-3">
                    <div className="flex items-center justify-between text-xs font-extrabold text-blue-900">
                      <span>Đang làm</span>
                      <span className="rounded-full bg-blue-600 px-2 py-0.5 text-[10px] text-white">{inProgressCount}</span>
                    </div>

                    <div className="space-y-2.5">
                      {simTasks.filter(t => t.status === 'IN_PROGRESS').map(t => (
                        <div key={t.id} className="rounded-xl border border-blue-300 bg-white p-3 space-y-2 shadow-xs ring-1 ring-blue-200">
                          <div className="flex items-center gap-1.5 text-[10px]">
                            <span className="rounded bg-slate-100 px-1.5 py-0.5 font-bold text-slate-700">{t.tag}</span>
                          </div>
                          <p className="text-xs font-bold text-slate-900 leading-snug">{t.title}</p>
                          <p className="text-[10px] text-slate-400 font-mono">- {t.project}</p>
                          <div className="flex items-center justify-between text-[10px] text-slate-500 pt-1 border-t border-slate-100">
                            <span className="rounded bg-blue-50 px-1.5 py-0.5 font-bold text-blue-600">{t.priority}</span>
                            <span>{t.logged}</span>
                            <span>{t.assignee}</span>
                          </div>
                          <div className="flex gap-1 pt-1">
                            <button
                              onClick={() => moveTask(t.id, 'TODO')}
                              className="flex-1 rounded-lg bg-slate-200 py-1 text-[9px] font-bold text-slate-700 hover:bg-slate-300"
                            >
                              ← Về TODO
                            </button>
                            <button
                              onClick={() => moveTask(t.id, 'WAITING')}
                              className="flex-1 rounded-lg bg-amber-500 py-1 text-[9px] font-bold text-white hover:bg-amber-600"
                            >
                              Đang chờ →
                            </button>
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>

                  {/* Column 3: Đang chờ */}
                  <div className="rounded-xl border border-amber-200 bg-amber-50/50 p-3 space-y-3">
                    <div className="flex items-center justify-between text-xs font-extrabold text-amber-900">
                      <span>Đang chờ</span>
                      <span className="rounded-full bg-amber-500 px-2 py-0.5 text-[10px] text-white">{waitingCount}</span>
                    </div>

                    <div className="space-y-2.5">
                      {simTasks.filter(t => t.status === 'WAITING').map(t => (
                        <div key={t.id} className="rounded-xl border border-amber-300 bg-white p-3 space-y-2 shadow-xs">
                          <div className="flex items-center gap-1.5 text-[10px]">
                            <span className="rounded bg-slate-100 px-1.5 py-0.5 font-bold text-slate-700">{t.tag}</span>
                            {t.isOverdue && <span className="rounded bg-rose-50 px-1.5 py-0.5 font-bold text-rose-600 border border-rose-200">QUÁ HẠN</span>}
                          </div>
                          <p className="text-xs font-bold text-slate-900 leading-snug">{t.title}</p>
                          <p className="text-[10px] text-slate-400 font-mono">- {t.project}</p>
                          <div className="flex items-center justify-between text-[10px] text-slate-500 pt-1 border-t border-slate-100">
                            <span className="rounded bg-blue-50 px-1.5 py-0.5 font-bold text-blue-600">{t.priority}</span>
                            <span>{t.logged}</span>
                            <span>{t.assignee}</span>
                          </div>
                          <button
                            onClick={() => moveTask(t.id, 'IN_REVIEW')}
                            className="w-full rounded-lg bg-purple-600 py-1 text-[10px] font-bold text-white hover:bg-purple-700"
                          >
                            Gửi Review →
                          </button>
                        </div>
                      ))}
                    </div>
                  </div>

                  {/* Column 4: Đang review */}
                  <div className="rounded-xl border border-purple-200 bg-purple-50/50 p-3 space-y-3">
                    <div className="flex items-center justify-between text-xs font-extrabold text-purple-900">
                      <span>Đang review</span>
                      <span className="rounded-full bg-purple-600 px-2 py-0.5 text-[10px] text-white">{inReviewCount}</span>
                    </div>

                    <div className="space-y-2.5">
                      {simTasks.filter(t => t.status === 'IN_REVIEW').length === 0 ? (
                        <div className="rounded-xl border border-dashed border-purple-200 bg-white/70 p-6 text-center text-xs text-slate-400 font-medium">
                          Thả task vào đây.
                        </div>
                      ) : (
                        simTasks.filter(t => t.status === 'IN_REVIEW').map(t => (
                          <div key={t.id} className="rounded-xl border border-purple-300 bg-white p-3 space-y-2 shadow-xs">
                            <div className="flex items-center gap-1.5 text-[10px]">
                              <span className="rounded bg-slate-100 px-1.5 py-0.5 font-bold text-slate-700">{t.tag}</span>
                            </div>
                            <p className="text-xs font-bold text-slate-900 leading-snug">{t.title}</p>
                            <button
                              onClick={() => moveTask(t.id, 'DONE')}
                              className="w-full rounded-lg bg-emerald-600 py-1 text-[10px] font-bold text-white hover:bg-emerald-700"
                            >
                              Duyệt ✓ Thả vào Hoàn thành
                            </button>
                          </div>
                        ))
                      )}
                    </div>
                  </div>

                  {/* Column 5: Hoàn thành */}
                  <div className="rounded-xl border border-emerald-200 bg-emerald-50/50 p-3 space-y-3">
                    <div className="flex items-center justify-between text-xs font-extrabold text-emerald-900">
                      <span>Hoàn thành</span>
                      <span className="rounded-full bg-emerald-600 px-2 py-0.5 text-[10px] text-white">{doneCount}</span>
                    </div>

                    <div className="space-y-2.5">
                      {simTasks.filter(t => t.status === 'DONE').length === 0 ? (
                        <div className="rounded-xl border border-dashed border-emerald-200 bg-white/70 p-6 text-center text-xs text-slate-400 font-medium">
                          Thả task vào đây.
                        </div>
                      ) : (
                        simTasks.filter(t => t.status === 'DONE').map(t => (
                          <div key={t.id} className="rounded-xl border border-emerald-300 bg-white p-3 space-y-2 shadow-xs">
                            <div className="flex items-center gap-1.5 text-[10px]">
                              <span className="rounded bg-emerald-50 px-1.5 py-0.5 font-bold text-emerald-700">HOÀN THÀNH</span>
                            </div>
                            <p className="text-xs font-bold text-slate-900 line-through text-slate-400 leading-snug">{t.title}</p>
                            <button
                              onClick={() => moveTask(t.id, 'IN_PROGRESS')}
                              className="w-full rounded-lg bg-amber-600 py-1 text-[10px] font-bold text-white hover:bg-amber-700"
                            >
                              Mở lại task
                            </button>
                          </div>
                        ))
                      )}
                    </div>
                  </div>

                  {/* Column 6: Đã hủy */}
                  <div className="rounded-xl border border-rose-200 bg-rose-50/50 p-3 space-y-3">
                    <div className="flex items-center justify-between text-xs font-extrabold text-rose-900">
                      <span>Đã hủy</span>
                      <span className="rounded-full bg-rose-600 px-2 py-0.5 text-[10px] text-white">{cancelledCount}</span>
                    </div>

                    <div className="space-y-2.5">
                      <div className="rounded-xl border border-dashed border-rose-200 bg-white/70 p-6 text-center text-xs text-slate-400 font-medium">
                        Thả task vào đây.
                      </div>
                    </div>
                  </div>

                </div>
              </div>
            </section>
          )}

          {/* SECTION 2: SPRINT REVIEW SIMULATOR (100% MATCH UI IMAGE 3) */}
          {activeSection === 'review' && (
            <section className="space-y-6">
              <div className="border-b border-slate-200 pb-3">
                <h2 className="text-lg font-extrabold text-slate-900">Mô phỏng Màn hình Tổng kết & Họp Review (100% khớp UI Image 3)</h2>
                <p className="text-xs text-slate-500 mt-0.5">Giao diện tổng kết khi bấm nút Đóng Sprint để nghiệm thu dự án</p>
              </div>

              {/* Sub Navigation Tabs (Match Image 3) */}
              <div className="flex border-b border-slate-200 gap-6 text-xs font-bold">
                <button
                  onClick={() => setReviewTab('report')}
                  className={`pb-2.5 transition border-b-2 ${
                    reviewTab === 'report' ? 'border-brand text-brand font-black' : 'border-transparent text-slate-400 hover:text-slate-700'
                  }`}
                >
                  Báo cáo đóng
                </button>
                <button
                  onClick={() => setReviewTab('review')}
                  className={`pb-2.5 transition border-b-2 ${
                    reviewTab === 'review' ? 'border-brand text-brand font-black' : 'border-transparent text-slate-400 hover:text-slate-700'
                  }`}
                >
                  Họp Review
                </button>
                <button
                  onClick={() => setReviewTab('retro')}
                  className={`pb-2.5 transition border-b-2 ${
                    reviewTab === 'retro' ? 'border-brand text-brand font-black' : 'border-transparent text-slate-400 hover:text-slate-700'
                  }`}
                >
                  Họp Retrospective
                </button>
              </div>

              {/* STAT CARDS (MATCH IMAGE 3 EXACTLY) */}
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
                
                {/* Card 1 */}
                <div className="rounded-2xl border border-slate-200 bg-white p-5 space-y-3 shadow-xs">
                  <div className="flex items-center justify-between">
                    <span className="text-[10px] font-bold uppercase tracking-wider text-slate-400">MỤC TIÊU SPRINT</span>
                    <span className="rounded-full bg-amber-50 px-2 py-0.5 text-[10px] font-extrabold text-amber-700 border border-amber-200">
                      CHƯA ĐÁNH GIÁ
                    </span>
                  </div>
                  <h3 className="text-lg font-extrabold text-slate-900">Chưa đánh giá</h3>
                  <div className="w-full bg-slate-100 h-1 rounded-full overflow-hidden">
                    <div className="bg-amber-500 h-full w-[30%]"></div>
                  </div>
                </div>

                {/* Card 2 */}
                <div className="rounded-2xl border border-slate-200 bg-white p-5 space-y-3 shadow-xs">
                  <div className="flex items-center justify-between">
                    <span className="text-[10px] font-bold uppercase tracking-wider text-slate-400">NGHIỆM THU BACKLOG</span>
                    <span className="rounded-full bg-emerald-50 px-2 py-0.5 text-[10px] font-extrabold text-emerald-700 border border-emerald-200">
                      HOÀN THÀNH
                    </span>
                  </div>
                  <h3 className="text-lg font-extrabold text-slate-900">0 <span className="text-xs font-normal text-slate-400">/ 1 items</span></h3>
                </div>

                {/* Card 3 */}
                <div className="rounded-2xl border border-slate-200 bg-white p-5 space-y-3 shadow-xs">
                  <div className="flex items-center justify-between">
                    <span className="text-[10px] font-bold uppercase tracking-wider text-slate-400">QUY MÔ STORY POINT</span>
                    <span className="rounded-full bg-amber-50 px-2 py-0.5 text-[10px] font-extrabold text-amber-700 border border-amber-200">
                      TỔNG ĐIỂM
                    </span>
                  </div>
                  <h3 className="text-lg font-extrabold text-slate-900">2 pt</h3>
                  <div className="w-full bg-slate-100 h-1 rounded-full overflow-hidden">
                    <div className="bg-amber-500 h-full w-[100%]"></div>
                  </div>
                </div>

                {/* Card 4 */}
                <div className="rounded-2xl border border-slate-200 bg-white p-5 space-y-3 shadow-xs">
                  <div className="flex items-center justify-between">
                    <span className="text-[10px] font-bold uppercase tracking-wider text-slate-400">HÀNH ĐỘNG CẢI TIẾN</span>
                    <span className="rounded-full bg-purple-50 px-2 py-0.5 text-[10px] font-extrabold text-purple-700 border border-purple-200">
                      RETROSPECTIVE
                    </span>
                  </div>
                  <h3 className="text-lg font-extrabold text-slate-900">0 việc</h3>
                  <div className="w-full bg-slate-100 h-1 rounded-full overflow-hidden">
                    <div className="bg-purple-600 h-full w-[100%]"></div>
                  </div>
                </div>

              </div>

              {/* LOWER PANEL: BURNDOWN CHART & TASK STATS (MATCH IMAGE 3) */}
              <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                
                {/* Left 2 Cols: Biểu đồ Tiến độ Sprint */}
                <div className="lg:col-span-2 rounded-2xl border border-slate-200 bg-white p-6 space-y-4 shadow-xs">
                  <div className="flex items-center justify-between border-b border-slate-100 pb-3">
                    <h3 className="font-extrabold text-sm text-slate-900">Biểu đồ Tiến độ Sprint</h3>
                    <div className="flex items-center gap-1 bg-slate-100 p-0.5 rounded-lg text-[10px] font-bold">
                      <span className="bg-white px-2 py-1 rounded text-slate-800 shadow-2xs">Tiến độ Tăng dần</span>
                      <span className="px-2 py-1 text-slate-500">Task Còn lại</span>
                    </div>
                  </div>

                  {/* Chart Graphic Mockup */}
                  <div className="h-48 w-full flex items-end justify-between px-4 pb-2 border-b border-slate-200 relative">
                    {/* Dashed Goal Line */}
                    <div className="absolute inset-x-4 top-12 border-b border-dashed border-slate-300"></div>
                    <div className="absolute inset-x-4 top-24 border-b border-dashed border-slate-300"></div>

                    {/* Timeline Points */}
                    {['28/06', '30/06', '02/07', '04/07', '06/07', '08/07', '10/07', '12/07', '14/07', '16/07', '18/07', '20/07', '22/07', '24/07', '26/07', '28/07', '30/07', '01/08'].map((date, idx) => (
                      <div key={date} className="flex flex-col items-center gap-1">
                        <div className={`size-2 rounded-full border-2 border-amber-500 bg-white z-10 ${idx > 10 ? 'bg-amber-500' : ''}`}></div>
                        <span className="text-[9px] text-slate-400 font-mono hidden sm:inline">{date}</span>
                      </div>
                    ))}
                  </div>
                </div>

                {/* Right 1 Col: Thống kê Nhiệm vụ (Task) */}
                <div className="rounded-2xl border border-slate-200 bg-white p-6 space-y-4 shadow-xs">
                  <h3 className="font-extrabold text-sm text-slate-900 border-b border-slate-100 pb-3">Thống kê Nhiệm vụ (Task)</h3>
                  <div className="space-y-3 text-xs">
                    <div className="flex justify-between py-1 border-b border-slate-100">
                      <span className="text-slate-600">Tổng số Task:</span>
                      <span className="font-bold text-slate-900">{totalCount}</span>
                    </div>
                    <div className="flex justify-between py-1 border-b border-slate-100">
                      <span className="text-slate-600">Đã hoàn thành:</span>
                      <span className="font-bold text-emerald-600">{doneCount}</span>
                    </div>
                    <div className="flex justify-between py-1 border-b border-slate-100">
                      <span className="text-slate-600">Chưa xong:</span>
                      <span className="font-bold text-rose-600">{totalCount - doneCount}</span>
                    </div>
                    <div className="flex justify-between py-1 border-b border-slate-100">
                      <span className="text-slate-600">Nhiệm vụ quá hạn:</span>
                      <span className="font-bold text-amber-600">2</span>
                    </div>
                    <div className="flex justify-between py-1">
                      <span className="text-slate-600">Nhiệm vụ bị chặn:</span>
                      <span className="font-bold text-slate-400">0</span>
                    </div>
                  </div>
                </div>

              </div>
            </section>
          )}

          {/* SECTION 3: 7 BƯỚC THAO TÁC */}
          {activeSection === 'steps' && (
            <section className="space-y-6">
              <div className="border-b border-slate-200 pb-3">
                <h2 className="text-lg font-extrabold text-slate-900">Quy trình 7 bước thao tác dự án từ A đến Z</h2>
                <p className="text-xs text-slate-500 mt-0.5">Hướng dẫn từng bước cơ bản cho người mới bắt đầu</p>
              </div>

              <div className="space-y-4">
                <div className="rounded-2xl border border-slate-200 bg-white p-5 space-y-2 shadow-xs">
                  <span className="font-extrabold text-brand text-xs">Bước 1: Chọn dự án làm việc</span>
                  <p className="text-xs text-slate-700 leading-relaxed">
                    Sau khi đăng nhập, tại góc trên bên trái màn hình, nhấn vào menu chọn dự án để xem danh sách dự án bạn tham gia. Manager có thể bấm "Tạo dự án mới".
                  </p>
                </div>

                <div className="rounded-2xl border border-slate-200 bg-white p-5 space-y-2 shadow-xs">
                  <span className="font-extrabold text-indigo-600 text-xs">Bước 2: Tạo công việc hoặc danh mục yêu cầu</span>
                  <p className="text-xs text-slate-700 leading-relaxed">
                    Bấm vào nút "Tạo Task" hoặc "Tạo Backlog". Điền tiêu đề, loại task (Story, Bug, Task), gán người làm và mức ưu tiên.
                  </p>
                </div>

                <div className="rounded-2xl border border-slate-200 bg-white p-5 space-y-2 shadow-xs">
                  <span className="font-extrabold text-teal-600 text-xs">Bước 3: Nhập danh sách công việc hàng loạt từ Excel</span>
                  <p className="text-xs text-slate-700 leading-relaxed">
                    Bấm "Tải tệp mẫu Excel" để lấy file định dạng sẵn. Nhập công việc rồi bấm "Nhập Task từ Excel" để đưa công việc vào hệ thống.
                  </p>
                </div>

                <div className="rounded-2xl border border-slate-200 bg-white p-5 space-y-2 shadow-xs">
                  <span className="font-extrabold text-purple-600 text-xs">Bước 4: Lập kế hoạch Sprint & Bắt đầu</span>
                  <p className="text-xs text-slate-700 leading-relaxed">
                    Tạo Sprint mới, kéo thả các User Story từ Backlog vào Sprint. Sau đó bấm "Bắt đầu Sprint" để kích hoạt bảng Kanban.
                  </p>
                </div>

                <div className="rounded-2xl border border-slate-200 bg-white p-5 space-y-2 shadow-xs">
                  <span className="font-extrabold text-emerald-600 text-xs">Bước 5: Kéo thả di chuyển trạng thái trên bảng Kanban</span>
                  <p className="text-xs text-slate-700 leading-relaxed">
                    Trên bảng Kanban, kéo thả thẻ công việc qua lại giữa các cột: Cần làm → Đang làm → Đang chờ → Đang review → Hoàn thành.
                  </p>
                </div>

                <div className="rounded-2xl border border-slate-200 bg-white p-5 space-y-2 shadow-xs">
                  <span className="font-extrabold text-amber-600 text-xs">Bước 6: Ghi nhận giờ làm việc (Time Log) và trao đổi</span>
                  <p className="text-xs text-slate-700 leading-relaxed">
                    Nhấn vào thẻ công việc để mở popup chi tiết, chuyển sang tab Time Log để nhập số giờ thực tế đã làm và trao đổi ở tab Bình luận.
                  </p>
                </div>

                <div className="rounded-2xl border border-slate-200 bg-white p-5 space-y-2 shadow-xs">
                  <span className="font-extrabold text-rose-600 text-xs">Bước 7: Đóng Sprint & Nghiệm thu</span>
                  <p className="text-xs text-slate-700 leading-relaxed">
                    Khi hết thời gian Sprint, bấm "Đóng Sprint". Hệ thống tự động tổng kết tỉ lệ hoàn thành và kết xuất báo cáo Excel/PDF.
                  </p>
                </div>
              </div>
            </section>
          )}

          {/* SECTION 4: FORM & THUẬT NGỮ */}
          {activeSection === 'forms' && (
            <section className="space-y-6">
              <div className="border-b border-slate-200 pb-3">
                <h2 className="text-lg font-extrabold text-slate-900">Chi tiết Form & Thuật ngữ Scrum</h2>
                <p className="text-xs text-slate-500 mt-0.5">Giải thích ý nghĩa các ô thông tin trong giao diện</p>
              </div>

              <div className="grid gap-4 sm:grid-cols-2">
                <div className="rounded-2xl border border-slate-200 bg-white p-5 space-y-2 shadow-xs">
                  <h3 className="font-extrabold text-slate-900 text-xs">Tiêu đề công việc (Title)</h3>
                  <p className="text-xs text-slate-600 leading-relaxed">Tên ngắn gọn của công việc cần làm.</p>
                </div>
                <div className="rounded-2xl border border-slate-200 bg-white p-5 space-y-2 shadow-xs">
                  <h3 className="font-extrabold text-slate-900 text-xs">Loại công việc (Task Type)</h3>
                  <p className="text-xs text-slate-600 leading-relaxed">STORY (Yêu cầu), BUG (Sửa lỗi), TASK (Kỹ thuật).</p>
                </div>
                <div className="rounded-2xl border border-slate-200 bg-white p-5 space-y-2 shadow-xs">
                  <h3 className="font-extrabold text-slate-900 text-xs">Story Points (Điểm độ phức tạp)</h3>
                  <p className="text-xs text-slate-600 leading-relaxed">Điểm đánh giá độ khó (1, 2, 3, 5, 8, 13).</p>
                </div>
                <div className="rounded-2xl border border-slate-200 bg-white p-5 space-y-2 shadow-xs">
                  <h3 className="font-extrabold text-slate-900 text-xs">Estimated Hours & Logged Hours</h3>
                  <p className="text-xs text-slate-600 leading-relaxed">Số giờ ước tính làm task vs Số giờ thực tế đã làm.</p>
                </div>
              </div>
            </section>
          )}

          {/* SECTION 5: PHÂN QUYỀN VAI TRÒ */}
          {activeSection === 'roles' && (
            <section className="space-y-6">
              <div className="border-b border-slate-200 pb-3">
                <h2 className="text-lg font-extrabold text-slate-900">Bảng phân quyền vai trò (Roles)</h2>
                <p className="text-xs text-slate-500 mt-0.5">Chi tiết chức năng của từng tài khoản</p>
              </div>

              <div className="grid gap-4 sm:grid-cols-2">
                <div className="rounded-2xl border border-slate-200 bg-white p-5 space-y-2 shadow-xs">
                  <h3 className="font-extrabold text-slate-900 text-xs">ADMIN</h3>
                  <p className="text-xs text-slate-600 leading-relaxed">Quản lý người dùng, xem Audit Log, cấu hình hệ thống.</p>
                </div>
                <div className="rounded-2xl border border-slate-200 bg-white p-5 space-y-2 shadow-xs">
                  <h3 className="font-extrabold text-slate-900 text-xs">MANAGER / PROJECT MANAGER</h3>
                  <p className="text-xs text-slate-600 leading-relaxed">Khởi tạo dự án, quản lý thành viên, xuất báo cáo tổng kết.</p>
                </div>
                <div className="rounded-2xl border border-slate-200 bg-white p-5 space-y-2 shadow-xs">
                  <h3 className="font-extrabold text-slate-900 text-xs">SCRUM MASTER / PRODUCT OWNER</h3>
                  <p className="text-xs text-slate-600 leading-relaxed">Quản lý Product Backlog, lập kế hoạch Sprint, nghiệm thu.</p>
                </div>
                <div className="rounded-2xl border border-slate-200 bg-white p-5 space-y-2 shadow-xs">
                  <h3 className="font-extrabold text-slate-900 text-xs">DEVELOPER / QA / MEMBER</h3>
                  <p className="text-xs text-slate-600 leading-relaxed">Tạo task, kéo thả di chuyển trạng thái Kanban, import Excel, time log.</p>
                </div>
              </div>
            </section>
          )}

        </main>

      </div>
    </div>
  )
}
