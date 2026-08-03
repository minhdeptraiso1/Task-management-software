import { useState } from 'react'

interface UserGuidePageProps {
  onBack: () => void
}

export function UserGuidePage({ onBack }: UserGuidePageProps) {
  const [activeSection, setActiveSection] = useState<'steps' | 'kanban' | 'review' | 'forms' | 'roles'>('kanban')

  // State Giả lập Kanban (Match 100% UI Image 2)
  const [simTasks, setSimTasks] = useState([
    { id: 'TASK-101', title: 'Xây dựng api đăng nhập', project: 'qwe', status: 'TODO', priority: 'Vừa', tag: 'DEV', isOverdue: true, logged: '0h/1h', assignee: 'Chưa giao' },
    { id: 'TASK-103', title: 'Xây dựng product backlog', project: 'qwe', status: 'TODO', priority: 'Vừa', tag: 'DEV', isOverdue: false, logged: '0h/1h', assignee: 'Chưa giao' },
    { id: 'TASK-108', title: 'Tích hợp refresh token tự động', project: 'qwe', status: 'IN_PROGRESS', priority: 'Cao', tag: 'BE', isOverdue: false, logged: '2h/4h', assignee: 'Dev_Minh' },
    { id: 'TASK-105', title: 'Xây dựng api đăng nhập OAuth', project: 'qwe', status: 'WAITING', priority: 'Vừa', tag: 'DEV', isOverdue: true, logged: '1h 30m/1h', assignee: 'Tuấn Anh' },
    { id: 'TASK-106', title: 'Kiểm thử giao diện Scrum Board', project: 'qwe', status: 'IN_REVIEW', priority: 'Cao', tag: 'QA', isOverdue: false, logged: '2h/3h', assignee: 'Minh' },
    { id: 'TASK-109', title: 'Rà soát popup import Excel', project: 'qwe', status: 'IN_REVIEW', priority: 'Vừa', tag: 'QA', isOverdue: false, logged: '1h/2h', assignee: 'QA_Linh' },
    { id: 'TASK-107', title: 'Tạo cơ sở dữ liệu dự án', project: 'qwe', status: 'DONE', priority: 'Vừa', tag: 'DEV', isOverdue: false, logged: '2h/2h', assignee: 'Hoàng' },
    { id: 'TASK-110', title: 'Hoàn thiện audit log realtime', project: 'qwe', status: 'DONE', priority: 'Thấp', tag: 'BE', isOverdue: false, logged: '3h/3h', assignee: 'Dev_Tuấn' },
    { id: 'TASK-111', title: 'Bỏ hiệu ứng loading cũ', project: 'qwe', status: 'CANCELLED', priority: 'Thấp', tag: 'UI', isOverdue: false, logged: '0h/1h', assignee: 'PM' },
  ])

  // State Giả lập Sprint Review (Match 100% UI Image 3)
  const [reviewTab, setReviewTab] = useState<'report' | 'review' | 'retro'>('report')

  const moveTask = (id: string, targetStatus: string) => {
    setSimTasks(prev => prev.map(t => t.id === id ? { ...t, status: targetStatus } : t))
  }

  const resetSimulator = () => {
    setSimTasks([
      { id: 'TASK-101', title: 'Xây dựng api đăng nhập', project: 'qwe', status: 'TODO', priority: 'Vừa', tag: 'DEV', isOverdue: true, logged: '0h/1h', assignee: 'Chưa giao' },
      { id: 'TASK-103', title: 'Xây dựng product backlog', project: 'qwe', status: 'TODO', priority: 'Vừa', tag: 'DEV', isOverdue: false, logged: '0h/1h', assignee: 'Chưa giao' },
      { id: 'TASK-108', title: 'Tích hợp refresh token tự động', project: 'qwe', status: 'IN_PROGRESS', priority: 'Cao', tag: 'BE', isOverdue: false, logged: '2h/4h', assignee: 'Dev_Minh' },
      { id: 'TASK-105', title: 'Xây dựng api đăng nhập OAuth', project: 'qwe', status: 'WAITING', priority: 'Vừa', tag: 'DEV', isOverdue: true, logged: '1h 30m/1h', assignee: 'Tuấn Anh' },
      { id: 'TASK-106', title: 'Kiểm thử giao diện Scrum Board', project: 'qwe', status: 'IN_REVIEW', priority: 'Cao', tag: 'QA', isOverdue: false, logged: '2h/3h', assignee: 'Minh' },
      { id: 'TASK-109', title: 'Rà soát popup import Excel', project: 'qwe', status: 'IN_REVIEW', priority: 'Vừa', tag: 'QA', isOverdue: false, logged: '1h/2h', assignee: 'QA_Linh' },
      { id: 'TASK-107', title: 'Tạo cơ sở dữ liệu dự án', project: 'qwe', status: 'DONE', priority: 'Vừa', tag: 'DEV', isOverdue: false, logged: '2h/2h', assignee: 'Hoàng' },
      { id: 'TASK-110', title: 'Hoàn thiện audit log realtime', project: 'qwe', status: 'DONE', priority: 'Thấp', tag: 'BE', isOverdue: false, logged: '3h/3h', assignee: 'Dev_Tuấn' },
      { id: 'TASK-111', title: 'Bỏ hiệu ứng loading cũ', project: 'qwe', status: 'CANCELLED', priority: 'Thấp', tag: 'UI', isOverdue: false, logged: '0h/1h', assignee: 'PM' },
    ])
  }

  const totalCount = simTasks.length
  const todoCount = simTasks.filter(t => t.status === 'TODO').length
  const inProgressCount = simTasks.filter(t => t.status === 'IN_PROGRESS').length
  const waitingCount = simTasks.filter(t => t.status === 'WAITING').length
  const inReviewCount = simTasks.filter(t => t.status === 'IN_REVIEW').length
  const doneCount = simTasks.filter(t => t.status === 'DONE').length
  const cancelledCount = simTasks.filter(t => t.status === 'CANCELLED').length
  const sprintProgress = [
    { date: '29/07', expected: 1, actual: 1 },
    { date: '30/07', expected: 2, actual: 2 },
    { date: '31/07', expected: 3, actual: 2 },
    { date: '01/08', expected: 5, actual: 3 },
    { date: '02/08', expected: 6, actual: 4 },
    { date: '03/08', expected: 7, actual: 5 },
    { date: '04/08', expected: 8, actual: 7 },
  ]
  const expectedLinePoints = sprintProgress
    .map((item, index) => `${40 + index * 115},${180 - item.expected * 18}`)
    .join(' ')
  const actualLinePoints = sprintProgress
    .map((item, index) => `${40 + index * 115},${180 - item.actual * 18}`)
    .join(' ')

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
      <div className="mx-auto flex w-full max-w-[1680px] flex-1 gap-8 p-6 md:p-8">
        
        {/* Sidebar Table of Contents */}
        <aside className="hidden w-64 shrink-0 lg:block">
          <div className="sticky top-20 rounded-2xl border border-slate-200 bg-white p-4 shadow-xs space-y-2">
            <p className="px-3 text-sm font-bold uppercase tracking-wider text-slate-400">Mục lục Hướng dẫn</p>
            <nav className="space-y-1 text-sm font-bold">
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
                3. Quy trình 7 bước
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
                5. Phân quyền vai trò
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
                  <h2 className="text-xl font-extrabold text-slate-900">Giả lập Giao diện Bảng Kanban Thực Tế</h2>
                  <p className="text-sm text-slate-500 mt-0.5">Bấm vào các nút di chuyển dưới mỗi thẻ Task để trải nghiệm di chuyển trạng thái</p>
                </div>
                <button
                  onClick={resetSimulator}
                  className="rounded-lg border border-slate-300 bg-white px-3 py-1.5 text-sm font-bold text-slate-700 hover:bg-slate-100 transition"
                >
                  Đặt lại dữ liệu
                </button>
              </div>

              {/* REAL KANBAN UI (MATCHING IMAGE 2 EXACTLY) */}
              <div className="rounded-2xl border border-slate-200 bg-slate-50/60 p-4 overflow-x-auto shadow-xs">
                <div className="grid grid-cols-6 gap-4 min-w-[1280px]">

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
                      {simTasks.filter(t => t.status === 'CANCELLED').length === 0 ? (
                        <div className="rounded-xl border border-dashed border-rose-200 bg-white/70 p-6 text-center text-xs text-slate-400 font-medium">
                          Thả task vào đây.
                        </div>
                      ) : (
                        simTasks.filter(t => t.status === 'CANCELLED').map(t => (
                          <div key={t.id} className="rounded-xl border border-rose-300 bg-white p-3 space-y-2 shadow-xs">
                            <div className="flex items-center gap-1.5 text-[10px]">
                              <span className="rounded bg-rose-50 px-1.5 py-0.5 font-bold text-rose-600 border border-rose-200">ĐÃ HỦY</span>
                              <span className="rounded bg-slate-100 px-1.5 py-0.5 font-bold text-slate-700">{t.tag}</span>
                            </div>
                            <p className="text-xs font-bold text-slate-500 line-through leading-snug">{t.title}</p>
                            <p className="text-[10px] text-slate-400 font-mono">- {t.project}</p>
                            <button
                              onClick={() => moveTask(t.id, 'TODO')}
                              className="w-full rounded-lg bg-slate-700 py-1 text-[10px] font-bold text-white hover:bg-slate-800"
                            >
                              Khôi phục về Cần làm
                            </button>
                          </div>
                        ))
                      )}
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
                <h2 className="text-xl font-extrabold text-slate-900">Mô phỏng Màn hình Tổng kết & Họp Review</h2>
                <p className="text-sm text-slate-500 mt-0.5">Giao diện tổng kết khi bấm nút Đóng Sprint để nghiệm thu dự án</p>
              </div>

              {/* Sub Navigation Tabs (Match Image 3) */}
              <div className="flex border-b border-slate-200 gap-6 text-sm font-bold">
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

              {reviewTab === 'report' && (
                <div className="rounded-2xl border border-amber-100 bg-amber-50/70 p-4 text-sm leading-relaxed text-amber-900">
                  <p className="font-extrabold">Báo cáo đóng dùng để tổng kết Sprint trước khi nghiệm thu.</p>
                  <p className="mt-1">
                    Một Sprint được xem là ổn khi mục tiêu Sprint đã được đánh giá, backlog quan trọng đã nghiệm thu, số task quá hạn thấp và biểu đồ tiến độ giảm đều theo ngày.
                    Chưa ổn khi task còn dồn nhiều ở cuối tuần, còn nhiều việc quá hạn, hoặc mục tiêu Sprint chưa được PO/Manager xác nhận.
                  </p>
                </div>
              )}

              {reviewTab === 'review' && (
                <div className="rounded-2xl border border-blue-100 bg-blue-50/70 p-4 text-sm leading-relaxed text-blue-900">
                  <p className="font-extrabold">Họp Review dùng để nghiệm thu kết quả Sprint trước khi bàn giao.</p>
                  <p className="mt-1">
                    Đạt khi phần demo chạy đúng tiêu chí chấp nhận, có phản hồi từ stakeholder, các hạng mục accepted/rejected rõ ràng và có thống nhất cuối buổi.
                    Chưa đạt khi mục tiêu Sprint chưa được xác nhận, còn lỗi nghiêm trọng, hoặc còn việc cần chỉnh sửa nhưng chưa ghi nhận vào danh sách rejected.
                  </p>
                </div>
              )}

              {reviewTab === 'retro' && (
                <div className="rounded-2xl border border-purple-100 bg-purple-50/70 p-4 text-sm leading-relaxed text-purple-900">
                  <p className="font-extrabold">Retrospective dùng để cải thiện cách team phối hợp ở Sprint tiếp theo.</p>
                  <p className="mt-1">
                    Buổi retro tốt cần tách rõ điểm làm tốt, điểm chưa tốt, ý kiến cải tiến và action item có người phụ trách/hạn hoàn thành.
                    Chưa tốt nếu chỉ ghi nhận xét chung chung mà không biến thành hành động theo dõi được.
                  </p>
                </div>
              )}

              {reviewTab === 'review' && (
                <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-xs">
                  <div className="flex items-start justify-between gap-4 border-b border-slate-100 pb-4">
                    <div>
                      <h3 className="text-base font-extrabold text-slate-900">Sprint Review (Nghiệm thu Sprint)</h3>
                      <p className="mt-1 text-sm text-slate-500">Lưu lại kết quả demo nghiệm thu sản phẩm và thống nhất bàn giao công việc.</p>
                    </div>
                    <span className="rounded-xl border border-amber-100 bg-amber-50 px-3 py-2 text-xs font-extrabold text-amber-700">
                      Review
                    </span>
                  </div>

                  <div className="mt-5 flex flex-col gap-4 rounded-2xl border border-amber-200 bg-gradient-to-r from-amber-50 to-white p-4 sm:flex-row sm:items-center sm:justify-between">
                    <div>
                      <span className="inline-flex rounded-full border border-amber-200 bg-white px-3 py-1 text-xs font-extrabold text-amber-700">
                        ĐÁNH GIÁ: CHƯA ĐẠT
                      </span>
                      <p className="mt-2 font-extrabold text-amber-950">Mục tiêu Sprint chưa đạt</p>
                      <p className="mt-1 text-sm text-slate-600">Chọn trạng thái bên phải để xác nhận đánh giá kết quả nghiệm thu Sprint chính thức.</p>
                    </div>
                    <div className="flex w-fit rounded-full bg-slate-100 p-1 text-sm font-bold">
                      <span className="rounded-full px-3 py-1 text-slate-500">Đạt mục tiêu</span>
                      <span className="rounded-full bg-brand px-3 py-1 text-white">Chưa đạt</span>
                    </div>
                  </div>

                  <div className="mt-6 grid gap-5 lg:grid-cols-2">
                    {[
                      ['TÓM TẮT PHẦN DEMO TRÌNH BÀY SẢN PHẨM', 'Nội dung demo, phản hồi kỹ thuật lúc chạy thử sản phẩm...'],
                      ['Ý KIẾN PHẢN HỒI TỪ CÁC BÊN LIÊN QUAN', 'Đóng góp, đánh giá từ khách hàng, Product Owner, Ban giám đốc...'],
                      ['CÁC CÔNG VIỆC ĐƯỢC NGHIỆM THU ĐÓNG LẠI', 'Danh sách User Stories hoặc nhiệm vụ được chấp nhận hoàn thành...'],
                      ['CÔNG VIỆC BỊ TỪ CHỐI / CẦN CHỈNH SỬA', 'Nhiệm vụ lỗi, chưa đạt chuẩn yêu cầu, cần chuyển sang Sprint sau...'],
                    ].map(([label, placeholder]) => (
                      <label key={label} className="block space-y-2">
                        <span className="text-xs font-extrabold uppercase tracking-wide text-slate-500">{label}</span>
                        <div className="min-h-28 rounded-xl border border-slate-200 bg-slate-50 p-4 text-sm text-slate-400">
                          {placeholder}
                        </div>
                      </label>
                    ))}
                  </div>

                  <label className="mt-5 block space-y-2">
                    <span className="text-xs font-extrabold uppercase tracking-wide text-slate-500">GHI CHÚ & THỐNG NHẤT THÊM</span>
                    <div className="min-h-24 rounded-xl border border-slate-200 bg-slate-50 p-4 text-sm text-slate-400">
                      Các lưu ý hoặc ghi chú bổ sung trong buổi họp nghiệm thu...
                    </div>
                  </label>
                </div>
              )}

              {reviewTab === 'retro' && (
                <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-xs">
                  <div className="flex items-start justify-between gap-4 border-b border-slate-100 pb-4">
                    <div>
                      <h3 className="text-base font-extrabold text-slate-900">Sprint Retrospective (Họp cải tiến)</h3>
                      <p className="mt-1 text-sm text-slate-500">Nhìn nhận lại quá trình phối hợp của đội ngũ để làm tốt hơn trong Sprint tiếp theo.</p>
                    </div>
                    <span className="rounded-xl border border-purple-100 bg-purple-50 px-3 py-2 text-xs font-extrabold text-purple-700">
                      Retro
                    </span>
                  </div>

                  <div className="mt-6 grid gap-5 xl:grid-cols-3">
                    <div className="overflow-hidden rounded-2xl border border-emerald-200 bg-emerald-50/40">
                      <div className="flex items-center justify-between border-b border-emerald-100 bg-emerald-50 px-4 py-3">
                        <h4 className="text-sm font-extrabold text-emerald-900">Điểm làm tốt (Went Well)</h4>
                        <span className="rounded-full border border-emerald-200 bg-white px-2.5 py-1 text-xs font-extrabold text-emerald-700">KHUYẾN KHÍCH</span>
                      </div>
                      <div className="min-h-32 p-4 text-sm text-emerald-600/70">
                        Những việc, quy trình đã hoạt động hiệu quả và cần duy trì...
                      </div>
                    </div>

                    <div className="overflow-hidden rounded-2xl border border-rose-200 bg-rose-50/40">
                      <div className="flex items-center justify-between border-b border-rose-100 bg-rose-50 px-4 py-3">
                        <h4 className="text-sm font-extrabold text-rose-900">Điểm chưa tốt (Went Wrong)</h4>
                        <span className="rounded-full border border-rose-200 bg-white px-2.5 py-1 text-xs font-extrabold text-rose-700">CẦN KHẮC PHỤC</span>
                      </div>
                      <div className="min-h-32 p-4 text-sm text-rose-600/70">
                        Khó khăn, xung đột, ước lượng sai thời gian hoặc sự cố kỹ thuật...
                      </div>
                    </div>

                    <div className="overflow-hidden rounded-2xl border border-sky-200 bg-sky-50/40">
                      <div className="flex items-center justify-between border-b border-sky-100 bg-sky-50 px-4 py-3">
                        <h4 className="text-sm font-extrabold text-sky-900">Ý kiến cải tiến (Improvement)</h4>
                        <span className="rounded-full border border-sky-200 bg-white px-2.5 py-1 text-xs font-extrabold text-sky-700">HÀNH ĐỘNG</span>
                      </div>
                      <div className="min-h-32 p-4 text-sm text-sky-600/70">
                        Giải pháp cụ thể, hành động khắc phục lỗi ở các Sprint sau...
                      </div>
                    </div>
                  </div>

                  <div className="mt-6 border-t border-slate-100 pt-5">
                    <div className="mb-4 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                      <div>
                        <h4 className="text-sm font-extrabold text-slate-900">Hành động cụ thể cho Sprint tiếp theo (Action Items)</h4>
                        <p className="mt-1 text-sm text-slate-500">Phân công chi tiết người phụ trách và hạn chót hoàn thành.</p>
                      </div>
                      <button className="w-fit rounded-xl border border-brand px-4 py-2 text-sm font-extrabold text-brand hover:bg-brand-soft">
                        + Thêm hành động
                      </button>
                    </div>

                    <div className="space-y-3">
                      {[
                        ['Chuẩn hóa popup lỗi import/upload', 'QA_Linh', '14/07/2026', 'Hoàn thành'],
                        ['Tạo reminder trước hạn 24h', 'Dev_Minh', '15/07/2026', 'Đang theo dõi'],
                      ].map(item => (
                        <div key={item[0]} className="grid gap-3 rounded-2xl border border-slate-200 bg-white p-3 text-sm shadow-xs lg:grid-cols-[1fr_160px_140px_140px] lg:items-center">
                          <div className="rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 font-medium text-slate-800">{item[0]}</div>
                          <div className="rounded-xl bg-slate-100 px-3 py-2 font-bold text-slate-700">{item[1]}</div>
                          <div className="rounded-xl border border-slate-200 px-3 py-2 text-slate-700">{item[2]}</div>
                          <span className={`rounded-xl px-3 py-2 text-center font-bold ${
                            item[3] === 'Hoàn thành'
                              ? 'border border-emerald-200 bg-emerald-50 text-emerald-700'
                              : 'border border-amber-200 bg-amber-50 text-amber-700'
                          }`}>
                            {item[3]}
                          </span>
                        </div>
                      ))}
                    </div>

                    <label className="mt-5 block space-y-2">
                      <span className="text-xs font-extrabold uppercase tracking-wide text-slate-500">GHI CHÚ CHUNG</span>
                      <div className="min-h-24 rounded-xl border border-slate-200 bg-slate-50 p-4 text-sm text-slate-400">
                        Ghi chú tổng hợp sau buổi retro...
                      </div>
                    </label>
                  </div>
                </div>
              )}

              {reviewTab === 'report' && (
                <>
              {/* STAT CARDS (MATCH IMAGE 3 EXACTLY) */}
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
                
                {/* Card 1 */}
                <div className="rounded-2xl border border-slate-200 bg-white p-5 space-y-3 shadow-xs">
                  <div className="flex items-center justify-between">
                    <span className="text-xs font-bold uppercase tracking-wider text-slate-400">MỤC TIÊU SPRINT</span>
                    <span className="rounded-full bg-amber-50 px-2 py-0.5 text-xs font-extrabold text-amber-700 border border-amber-200">
                      ĐÃ ĐÁNH GIÁ
                    </span>
                  </div>
                  <h3 className="text-xl font-extrabold text-slate-900">Đạt 80%</h3>
                  <div className="w-full bg-slate-100 h-1 rounded-full overflow-hidden">
                    <div className="bg-amber-500 h-full w-[80%]"></div>
                  </div>
                </div>

                {/* Card 2 */}
                <div className="rounded-2xl border border-slate-200 bg-white p-5 space-y-3 shadow-xs">
                  <div className="flex items-center justify-between">
                    <span className="text-xs font-bold uppercase tracking-wider text-slate-400">NGHIỆM THU BACKLOG</span>
                    <span className="rounded-full bg-emerald-50 px-2 py-0.5 text-xs font-extrabold text-emerald-700 border border-emerald-200">
                      HOÀN THÀNH
                    </span>
                  </div>
                  <h3 className="text-xl font-extrabold text-slate-900">3 <span className="text-sm font-normal text-slate-400">/ 4 items</span></h3>
                </div>

                {/* Card 3 */}
                <div className="rounded-2xl border border-slate-200 bg-white p-5 space-y-3 shadow-xs">
                  <div className="flex items-center justify-between">
                    <span className="text-xs font-bold uppercase tracking-wider text-slate-400">QUY MÔ STORY POINT</span>
                    <span className="rounded-full bg-amber-50 px-2 py-0.5 text-xs font-extrabold text-amber-700 border border-amber-200">
                      TỔNG ĐIỂM
                    </span>
                  </div>
                  <h3 className="text-xl font-extrabold text-slate-900">13 pt</h3>
                  <div className="w-full bg-slate-100 h-1 rounded-full overflow-hidden">
                    <div className="bg-amber-500 h-full w-[100%]"></div>
                  </div>
                </div>

                {/* Card 4 */}
                <div className="rounded-2xl border border-slate-200 bg-white p-5 space-y-3 shadow-xs">
                  <div className="flex items-center justify-between">
                    <span className="text-xs font-bold uppercase tracking-wider text-slate-400">HÀNH ĐỘNG CẢI TIẾN</span>
                    <span className="rounded-full bg-purple-50 px-2 py-0.5 text-xs font-extrabold text-purple-700 border border-purple-200">
                      RETROSPECTIVE
                    </span>
                  </div>
                  <h3 className="text-xl font-extrabold text-slate-900">3 việc</h3>
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
                    <h3 className="font-extrabold text-base text-slate-900">Biểu đồ Tiến độ Sprint</h3>
                    <div className="flex items-center gap-1 bg-slate-100 p-0.5 rounded-lg text-xs font-bold">
                      <span className="bg-white px-2 py-1 rounded text-slate-800 shadow-2xs">Tiến độ Tăng dần</span>
                      <span className="px-2 py-1 text-slate-500">Task Còn lại</span>
                    </div>
                  </div>

                  {/* Chart Graphic Mockup */}
                  <div className="w-full border-b border-slate-200 pb-4">
                    <svg viewBox="0 0 760 230" className="h-64 w-full">
                      {[0, 1, 2, 3].map(index => (
                        <line
                          key={index}
                          x1="28"
                          x2="735"
                          y1={36 + index * 46}
                          y2={36 + index * 46}
                          stroke="#e2e8f0"
                          strokeDasharray="4 4"
                        />
                      ))}
                      {[0, 3, 6, 9].map((value, index) => (
                        <text key={value} x="5" y={184 - index * 46} className="fill-slate-500 text-[11px]">
                          {value}
                        </text>
                      ))}
                      <polyline
                        points={expectedLinePoints}
                        fill="none"
                        stroke="#94a3b8"
                        strokeWidth="2"
                        strokeDasharray="6 5"
                      />
                      <polyline
                        points={actualLinePoints}
                        fill="none"
                        stroke="#f97316"
                        strokeWidth="3"
                      />
                      {sprintProgress.map((item, index) => (
                        <g key={item.date}>
                          <circle
                            cx={40 + index * 115}
                            cy={180 - item.expected * 18}
                            r="4"
                            fill="white"
                            stroke="#94a3b8"
                            strokeWidth="2"
                          />
                          <circle
                            cx={40 + index * 115}
                            cy={180 - item.actual * 18}
                            r="5"
                            fill="#f97316"
                            stroke="white"
                            strokeWidth="2"
                          />
                          <text x={24 + index * 115} y="212" className="fill-slate-500 text-[11px]">
                            {item.date}
                          </text>
                        </g>
                      ))}
                    </svg>

                    <div className="flex items-center justify-center gap-4 text-xs font-bold">
                      <span className="inline-flex items-center gap-2 rounded-full border border-slate-200 bg-white px-3 py-1.5 text-slate-600">
                        <i className="size-2 rounded-full bg-slate-400" /> Tasks kỳ vọng tăng dần
                      </span>
                      <span className="inline-flex items-center gap-2 rounded-full border border-orange-200 bg-orange-50 px-3 py-1.5 text-orange-700">
                        <i className="size-2 rounded-full bg-orange-500" /> Tasks thực tế tăng dần
                      </span>
                    </div>
                  </div>
                </div>

                {/* Right 1 Col: Thống kê Nhiệm vụ */}
                <div className="rounded-2xl border border-slate-200 bg-white p-6 space-y-4 shadow-xs">
                  <h3 className="font-extrabold text-base text-slate-900 border-b border-slate-100 pb-3">Thống kê Nhiệm vụ</h3>
                  <div className="space-y-3 text-sm">
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
                </>
              )}
            </section>
          )}

          {/* SECTION 3: 7 BƯỚC THAO TÁC */}
          {activeSection === 'steps' && (
            <section className="space-y-6">
              <div className="border-b border-slate-200 pb-3">
                <h2 className="text-xl font-extrabold text-slate-900">Quy trình 7 bước thao tác dự án từ A đến Z</h2>
                <p className="text-sm text-slate-500 mt-0.5">Hướng dẫn từng bước cơ bản cho người mới bắt đầu</p>
              </div>

              <div className="rounded-2xl border border-teal-100 bg-teal-50/70 p-4 text-sm font-semibold text-teal-800 leading-relaxed">
                Dưới đây là cẩm nang 7 bước dành cho một thành viên mới bắt đầu sử dụng hệ thống. Bạn chỉ cần đi theo đúng thứ tự để chọn dự án, tạo việc, lập Sprint, kéo Kanban, log time và đóng Sprint.
              </div>

              <div className="space-y-5">
                {[
                  {
                    title: 'Bước 1: Đăng nhập & chọn dự án',
                    color: 'text-brand',
                    position: 'Góc trên bên trái màn hình',
                    items: [
                      'Sau khi đăng nhập, hệ thống hiển thị màn hình làm việc chính.',
                      'Bấm vào menu chọn dự án để chọn dự án bạn đang làm việc.',
                      'Nếu bạn là Manager, bạn sẽ thấy nút “Tạo dự án mới” để khởi tạo dự án cho nhóm.',
                    ],
                  },
                  {
                    title: 'Bước 2: Tạo task mới hoặc tạo backlog item',
                    color: 'text-indigo-600',
                    position: 'Bảng Scrum → nút Tạo Task',
                    items: [
                      'Bấm nút “Tạo Task” hoặc “Tạo Backlog” ở góc phải bảng Scrum.',
                      'Điền tiêu đề, loại task, người thực hiện, mức ưu tiên và số giờ dự kiến.',
                      'Bấm “Lưu Task” để hoàn tất. Task sẽ xuất hiện ngay trên giao diện.',
                    ],
                  },
                  {
                    title: 'Bước 3: Nhập hàng loạt task từ file Excel',
                    color: 'text-teal-600',
                    position: 'Nút Import Excel',
                    items: [
                      'Bấm “Tải tệp mẫu Excel” để nhận file định dạng sẵn.',
                      'Nhập tiêu đề task, loại task và độ ưu tiên theo đúng cột hướng dẫn.',
                      'Bấm “Nhập Task từ Excel”, chọn file đã lưu để hệ thống tạo task hàng loạt.',
                    ],
                  },
                  {
                    title: 'Bước 4: Lập kế hoạch & bắt đầu Sprint',
                    color: 'text-purple-600',
                    position: 'Khối Sprint Planning',
                    items: [
                      'Bấm “Tạo Sprint”, nhập tên Sprint và mục tiêu Sprint.',
                      'Kéo User Story từ Product Backlog vào Sprint.',
                      'Khi đã chuẩn bị xong, bấm “Bắt đầu Sprint” để mở bảng Kanban.',
                    ],
                  },
                  {
                    title: 'Bước 5: Kéo thả trạng thái task trên Kanban',
                    color: 'text-emerald-600',
                    position: 'Bảng Kanban 6 cột',
                    items: [
                      'Các cột chính gồm Cần làm, Đang làm, Đang chờ, Đang review, Hoàn thành và Đã hủy.',
                      'Khi bắt đầu xử lý, kéo task từ Cần làm sang Đang làm.',
                      'Khi xong, kéo sang Đang review hoặc Hoàn thành. Hệ thống cập nhật realtime cho cả nhóm.',
                    ],
                  },
                  {
                    title: 'Bước 6: Ghi log giờ làm việc & bình luận',
                    color: 'text-amber-600',
                    position: 'Click trực tiếp vào thẻ Task',
                    items: [
                      'Click vào thẻ Task bất kỳ để mở popup chi tiết.',
                      'Ở phần Time Log, nhập số giờ thực tế đã làm và nội dung công việc.',
                      'Ở phần Bình luận, trao đổi với đồng nghiệp và đính kèm file sản phẩm nếu cần.',
                    ],
                  },
                  {
                    title: 'Bước 7: Nghiệm thu, đóng Sprint & xuất báo cáo',
                    color: 'text-rose-600',
                    position: 'Nút Đóng Sprint',
                    items: [
                      'Khi hết thời hạn Sprint, bấm “Đóng Sprint”.',
                      'Hệ thống tổng kết tỉ lệ hoàn thành, task còn tồn và điểm cần cải thiện.',
                      'Xuất báo cáo PDF hoặc Excel để lưu hồ sơ nghiệm thu dự án.',
                    ],
                  },
                ].map(step => (
                  <div key={step.title} className="rounded-2xl border border-slate-200 bg-white p-5 space-y-3 shadow-xs">
                    <div className="flex items-center justify-between gap-4 border-b border-slate-100 pb-2">
                      <span className={`font-extrabold ${step.color} text-sm uppercase`}>{step.title}</span>
                      <span className="shrink-0 text-xs font-mono text-slate-400">Vị trí: {step.position}</span>
                    </div>
                    <ul className="text-sm text-slate-700 space-y-2 list-disc pl-5 font-medium leading-relaxed">
                      {step.items.map(item => (
                        <li key={item}>{item}</li>
                      ))}
                    </ul>
                  </div>
                ))}
              </div>
            </section>
          )}

          {/* SECTION 4: FORM & THUẬT NGỮ */}
          {activeSection === 'forms' && (
            <section className="space-y-6">
              <div className="border-b border-slate-200 pb-3">
                <h2 className="text-xl font-extrabold text-slate-900">Chi tiết Form & Thuật ngữ Scrum</h2>
                <p className="text-sm text-slate-500 mt-0.5">Giải thích ý nghĩa các ô thông tin trong giao diện làm việc.</p>
              </div>

              <div className="space-y-5 text-sm">
                <div className="rounded-2xl border border-slate-200 bg-white p-5 space-y-4 shadow-xs">
                  <div className="border-b border-slate-100 pb-2">
                    <h3 className="font-extrabold text-slate-900 text-base">1. Tiêu đề công việc</h3>
                    <p className="text-xs font-semibold text-slate-400">Mô tả: Tên việc cần làm.</p>
                  </div>
                  <p className="text-sm text-slate-700 leading-relaxed">
                    <b>Yêu cầu:</b> Cần đặt tên ngắn gọn, rõ ràng và thể hiện được kết quả cần đạt được.
                  </p>
                  <div className="grid gap-3 md:grid-cols-2">
                    <div className="rounded-xl border border-emerald-100 bg-emerald-50/50 p-3">
                      <p className="font-extrabold text-emerald-700">Ví dụ tốt</p>
                      <p className="mt-1 text-slate-700">“Xây dựng API đăng nhập bằng email” — rõ hành động và kết quả.</p>
                    </div>
                    <div className="rounded-xl border border-rose-100 bg-rose-50/50 p-3">
                      <p className="font-extrabold text-rose-700">Ví dụ chưa tốt</p>
                      <p className="mt-1 text-slate-700">“Làm login” — quá chung chung, khó nghiệm thu, không rõ phạm vi.</p>
                    </div>
                  </div>
                </div>

                <div className="rounded-2xl border border-slate-200 bg-white p-5 space-y-4 shadow-xs">
                  <div className="border-b border-slate-100 pb-2">
                    <h3 className="font-extrabold text-slate-900 text-base">2. Loại công việc</h3>
                    <p className="text-xs font-semibold text-slate-400">Mô tả: Phân loại công việc Story, Bug hoặc Task.</p>
                  </div>
                  <div className="grid gap-3 md:grid-cols-3">
                    <div className="rounded-xl border border-blue-100 bg-blue-50/50 p-3">
                      <p className="font-extrabold text-blue-700">Story</p>
                      <p className="mt-1 text-slate-700">Yêu cầu nghiệp vụ mang lại giá trị trực tiếp cho người dùng.</p>
                    </div>
                    <div className="rounded-xl border border-rose-100 bg-rose-50/50 p-3">
                      <p className="font-extrabold text-rose-700">Bug</p>
                      <p className="mt-1 text-slate-700">Lỗi hệ thống cần được sửa chữa.</p>
                    </div>
                    <div className="rounded-xl border border-slate-200 bg-slate-50 p-3">
                      <p className="font-extrabold text-slate-700">Task</p>
                      <p className="mt-1 text-slate-700">Công việc kỹ thuật hoặc hỗ trợ, thường không tạo giá trị nghiệp vụ trực tiếp.</p>
                    </div>
                  </div>
                  <p className="text-sm text-slate-700 leading-relaxed">
                    <b>Lợi ích:</b> Việc chọn đúng loại công việc giúp hệ thống báo cáo và lọc công việc hoạt động chính xác hơn.
                  </p>
                </div>

                <div className="rounded-2xl border border-slate-200 bg-white p-5 space-y-4 shadow-xs">
                  <div className="border-b border-slate-100 pb-2">
                    <h3 className="font-extrabold text-slate-900 text-base">3. Story Points & Ước lượng bằng dãy Fibonacci</h3>
                    <p className="text-xs font-semibold text-amber-600">Mô tả: Đánh giá độ khó tương đối, không dùng để tính số giờ tuyệt đối.</p>
                  </div>
                  <p className="text-sm text-slate-700 leading-relaxed">
                    Sử dụng dãy Fibonacci 1, 2, 3, 5, 8, 13,... để đánh giá độ khó. Team nên lấy một công việc đã biết rõ làm mốc so sánh.
                    Ví dụ: lấy task “API đăng nhập email” làm mốc 3 điểm, rồi so sánh task mới khó hơn hay dễ hơn để chọn điểm phù hợp.
                  </p>
                  <div className="grid gap-3 md:grid-cols-3">
                    <div className="rounded-xl border border-emerald-100 bg-emerald-50/50 p-3">
                      <p className="font-extrabold text-emerald-700">Đánh giá tốt</p>
                      <p className="mt-1 text-slate-700">Có sự so sánh với task mốc, nêu rõ rủi ro, độ phức tạp và phạm vi công việc.</p>
                    </div>
                    <div className="rounded-xl border border-amber-100 bg-amber-50/50 p-3">
                      <p className="font-extrabold text-amber-700">Cần thảo luận</p>
                      <p className="mt-1 text-slate-700">Các thành viên vote điểm quá chênh lệch, ví dụ người chọn 3 điểm, người chọn 8 điểm.</p>
                    </div>
                    <div className="rounded-xl border border-rose-100 bg-rose-50/50 p-3">
                      <p className="font-extrabold text-rose-700">Chưa tốt</p>
                      <p className="mt-1 text-slate-700">Chọn điểm hoàn toàn dựa trên cảm tính, không giải thích được lý do task khó hay dễ.</p>
                    </div>
                  </div>
                  <p className="text-sm text-slate-700 leading-relaxed">
                    <b>Quy trình vote:</b> Mỗi thành viên đưa ra điểm số và giải thích lý do. Nếu điểm lệch nhiều, team cần hỏi lại yêu cầu, chia nhỏ task hoặc thống nhất thêm tiêu chí nghiệm thu để tìm tiếng nói chung.
                  </p>
                </div>

                <div className="rounded-2xl border border-slate-200 bg-white p-5 space-y-4 shadow-xs">
                  <div className="border-b border-slate-100 pb-2">
                    <h3 className="font-extrabold text-slate-900 text-base">4. Estimated Hours & Logged Hours</h3>
                    <p className="text-xs font-semibold text-slate-400">Mô tả: Số giờ ước tính và số giờ làm thực tế.</p>
                  </div>
                  <p className="text-sm text-slate-700 leading-relaxed">
                    <b>Estimated Hours:</b> Số giờ dự kiến cần thiết để hoàn thành công việc.
                    <br />
                    <b>Logged Hours:</b> Số giờ thực tế đã bỏ ra để thực hiện công việc đó.
                    <br />
                    <b>Lưu ý:</b> Nếu Logged Hours vượt quá Estimated Hours nhiều lần, team cần kiểm tra lại phạm vi task hoặc xem xét lại năng lực ước lượng của các thành viên.
                  </p>
                </div>

                <div className="rounded-2xl border border-slate-200 bg-white p-5 space-y-4 shadow-xs">
                  <div className="border-b border-slate-100 pb-2">
                    <h3 className="font-extrabold text-slate-900 text-base">5. Trạng thái Kanban</h3>
                    <p className="text-xs font-semibold text-slate-400">Mô tả: Cách đọc tiến độ thực hiện của một task.</p>
                  </div>
                  <div className="grid gap-3 md:grid-cols-2 xl:grid-cols-3">
                    {[
                      ['Cần làm', 'Công việc chưa được bắt đầu.', 'border-slate-300 bg-slate-100/70 text-slate-800'],
                      ['Đang làm', 'Công việc đang được xử lý.', 'border-blue-200 bg-blue-50/60 text-blue-900'],
                      ['Đang chờ', 'Công việc bị phụ thuộc vào bên ngoài hoặc đang chờ phản hồi.', 'border-amber-200 bg-amber-50/60 text-amber-900'],
                      ['Đang review', 'Công việc đang chờ kiểm tra chất lượng.', 'border-purple-200 bg-purple-50/60 text-purple-900'],
                      ['Hoàn thành', 'Công việc đã đạt đầy đủ các tiêu chí nghiệm thu.', 'border-emerald-200 bg-emerald-50/60 text-emerald-900'],
                      ['Đã hủy', 'Đã quyết định không thực hiện công việc này nữa.', 'border-rose-200 bg-rose-50/60 text-rose-900'],
                    ].map(([title, body, tone]) => (
                      <div key={title} className={`rounded-xl border p-3 ${tone}`}>
                        <p className="font-extrabold">{title}</p>
                        <p className="mt-1 text-slate-700">{body}</p>
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            </section>
          )}

          {/* SECTION 5: PHÂN QUYỀN VAI TRÒ */}
          {activeSection === 'roles' && (
            <section className="space-y-6">
              <div className="border-b border-slate-200 pb-3">
                <h2 className="text-xl font-extrabold text-slate-900">Bảng phân quyền vai trò</h2>
                <p className="text-sm text-slate-500 mt-0.5">Chi tiết chức năng của từng tài khoản</p>
              </div>

              <div className="grid gap-4 sm:grid-cols-2">
                <div className="rounded-2xl border border-amber-200 bg-amber-50/40 p-5 space-y-3 shadow-xs">
                  <div>
                    <h3 className="font-extrabold text-slate-900">ADMIN</h3>
                    <p className="text-sm text-amber-700 font-medium">Quyền cao nhất toàn bộ hệ thống</p>
                  </div>
                  <ul className="text-sm text-slate-700 space-y-2 list-disc pl-4 font-medium">
                    <li>Quản lý danh sách người dùng: tạo, sửa role, mở hoặc khóa tài khoản.</li>
                    <li>Xem nhật ký hệ thống để truy vết các thao tác quan trọng.</li>
                    <li>Cấu hình hoặc kích hoạt các job nhắc hẹn, digest, audit.</li>
                    <li>Xem thống kê tổng quan toàn hệ thống.</li>
                  </ul>
                </div>
                <div className="rounded-2xl border border-blue-200 bg-blue-50/40 p-5 space-y-3 shadow-xs">
                  <div>
                    <h3 className="font-extrabold text-slate-900">MANAGER / PROJECT MANAGER</h3>
                    <p className="text-sm text-blue-700 font-medium">Quản lý dự án và thành viên</p>
                  </div>
                  <ul className="text-sm text-slate-700 space-y-2 list-disc pl-4 font-medium">
                    <li>Khởi tạo dự án mới và chỉnh sửa thông tin dự án.</li>
                    <li>Thêm, xóa thành viên và gán vai trò trong dự án.</li>
                    <li>Quản lý Backlog, Sprint, Task và trạng thái triển khai.</li>
                    <li>Xuất báo cáo Excel/PDF để tổng kết Sprint hoặc dự án.</li>
                  </ul>
                </div>
                <div className="rounded-2xl border border-purple-200 bg-purple-50/40 p-5 space-y-3 shadow-xs">
                  <div>
                    <h3 className="font-extrabold text-slate-900">SCRUM MASTER / PRODUCT OWNER</h3>
                    <p className="text-sm text-purple-700 font-medium">Điều phối quy trình và nghiệm thu yêu cầu</p>
                  </div>
                  <ul className="text-sm text-slate-700 space-y-2 list-disc pl-4 font-medium">
                    <li>Quản lý Product Backlog, Story Point và mức ưu tiên.</li>
                    <li>Lập kế hoạch Sprint, bắt đầu Sprint và đóng Sprint.</li>
                    <li>Theo dõi tiến độ, Burndown, rủi ro và task bị chặn.</li>
                    <li>Nghiệm thu kết quả bàn giao cùng team.</li>
                  </ul>
                </div>
                <div className="rounded-2xl border border-emerald-200 bg-emerald-50/40 p-5 space-y-3 shadow-xs">
                  <div>
                    <h3 className="font-extrabold text-slate-900">DEVELOPER / QA / MEMBER</h3>
                    <p className="text-sm text-emerald-700 font-medium">Thành viên thực thi dự án</p>
                  </div>
                  <ul className="text-sm text-slate-700 space-y-2 list-disc pl-4 font-medium">
                    <li>Tạo task, cập nhật task và kéo thả trạng thái trên Kanban.</li>
                    <li>Nhập task hàng loạt bằng file Excel mẫu.</li>
                    <li>Ghi log giờ làm việc, bình luận và đính kèm file.</li>
                    <li>Phối hợp review, sửa lỗi và hoàn thành task theo Sprint.</li>
                  </ul>
                </div>
              </div>
            </section>
          )}

        </main>

      </div>
    </div>
  )
}
