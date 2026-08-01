import { useState } from 'react'
import { X } from 'lucide-react'
import { Button } from '../../../components/ui'

interface UserGuideModalProps {
  open: boolean
  onClose: () => void
}

/* =========================================================================
   SIMULATOR COMPONENTS (BỘ GIẢ LẬP MÔ PHỎNG THAO TÁC THỰC TẾ CHO NGƯỜI MỚI)
   ========================================================================= */

// 1. Interactive Kanban Simulator (Match 100% UI Image 4)
function KanbanInteractiveSimulator() {
  const [simTasks, setSimTasks] = useState([
    { id: 'TASK-101', title: 'Xây dựng api đăng nhập', project: 'qwe', status: 'TODO', priority: 'Vừa', tag: 'DEV', isOverdue: true, logged: '0h/1h', assignee: 'Chưa giao' },
    { id: 'TASK-102', title: 'Đi mua sting', project: 'qwe', status: 'TODO', priority: 'Vừa', tag: 'DEV', isOverdue: false, logged: '0h/0h', assignee: 'Chưa giao' },
    { id: 'TASK-103', title: 'Xây dựng product backlog', project: 'qwe', status: 'TODO', priority: 'Vừa', tag: 'DEV', isOverdue: false, logged: '0h/1h', assignee: 'Chưa giao' },
    { id: 'TASK-104', title: 'Mua cơm gà', project: 'qwe', status: 'IN_PROGRESS', priority: 'Vừa', tag: 'DEV', isOverdue: false, logged: '0h/0h', assignee: 'Chưa giao' },
    { id: 'TASK-105', title: 'Xây dựng api đăng nhập OAuth', project: 'qwe', status: 'WAITING', priority: 'Vừa', tag: 'DEV', isOverdue: true, logged: '1h 30m/1h', assignee: 'Tuấn Anh' },
    { id: 'TASK-106', title: 'Kiểm thử giao diện Scrum Board', project: 'qwe', status: 'IN_REVIEW', priority: 'Cao', tag: 'QA', isOverdue: false, logged: '2h/3h', assignee: 'Minh' },
    { id: 'TASK-107', title: 'Tạo cơ sở dữ liệu dự án', project: 'qwe', status: 'DONE', priority: 'Vừa', tag: 'DEV', isOverdue: false, logged: '2h/2h', assignee: 'Hoàng' },
  ])

  const moveTask = (id: string, targetStatus: string) => {
    setSimTasks(prev => prev.map(t => t.id === id ? { ...t, status: targetStatus } : t))
  }

  const todoCount = simTasks.filter(t => t.status === 'TODO').length
  const inProgressCount = simTasks.filter(t => t.status === 'IN_PROGRESS').length
  const waitingCount = simTasks.filter(t => t.status === 'WAITING').length
  const inReviewCount = simTasks.filter(t => t.status === 'IN_REVIEW').length
  const doneCount = simTasks.filter(t => t.status === 'DONE').length
  const cancelledCount = simTasks.filter(t => t.status === 'CANCELLED').length

  return (
    <div className="rounded-2xl border border-slate-200 bg-slate-50/60 p-4 space-y-3 shadow-xs overflow-x-auto">
      <div className="flex items-center justify-between border-b border-slate-200 pb-2 text-xs font-bold text-slate-700">
        <span>GIAO DIỆN BẢNG KANBAN THỰC TẾ (Bấm nút thử để trải nghiệm)</span>
        <span className="text-emerald-600 font-mono text-[11px]">Realtime Active</span>
      </div>

      <div className="grid grid-cols-6 gap-2.5 min-w-[900px]">
        {/* Cần làm */}
        <div className="rounded-xl border border-slate-300 bg-slate-100/70 p-2.5 space-y-2">
          <div className="flex items-center justify-between text-xs font-extrabold text-slate-800">
            <span>Cần làm</span>
            <span className="rounded-full bg-slate-600 px-2 py-0.5 text-[10px] text-white">{todoCount}</span>
          </div>
          {simTasks.filter(t => t.status === 'TODO').map(t => (
            <div key={t.id} className="rounded-xl border border-blue-200 bg-white p-2.5 space-y-1.5 shadow-xs text-xs">
              <div className="flex items-center gap-1 text-[9px]">
                <span className="rounded bg-slate-100 px-1 py-0.5 font-bold text-slate-700">{t.tag}</span>
                {t.isOverdue && <span className="rounded bg-rose-50 px-1 py-0.5 font-bold text-rose-600 border border-rose-200">QUÁ HẠN</span>}
              </div>
              <p className="font-bold text-slate-900 leading-tight">{t.title}</p>
              <button
                onClick={() => moveTask(t.id, 'IN_PROGRESS')}
                className="w-full rounded bg-blue-600 py-1 text-[10px] font-bold text-white hover:bg-blue-700"
              >
                Đang làm →
              </button>
            </div>
          ))}
        </div>

        {/* Đang làm */}
        <div className="rounded-xl border border-blue-200 bg-blue-50/50 p-2.5 space-y-2">
          <div className="flex items-center justify-between text-xs font-extrabold text-blue-900">
            <span>Đang làm</span>
            <span className="rounded-full bg-blue-600 px-2 py-0.5 text-[10px] text-white">{inProgressCount}</span>
          </div>
          {simTasks.filter(t => t.status === 'IN_PROGRESS').map(t => (
            <div key={t.id} className="rounded-xl border border-blue-300 bg-white p-2.5 space-y-1.5 shadow-xs text-xs">
              <span className="rounded bg-slate-100 px-1 py-0.5 font-bold text-slate-700 text-[9px]">{t.tag}</span>
              <p className="font-bold text-slate-900 leading-tight">{t.title}</p>
              <button
                onClick={() => moveTask(t.id, 'WAITING')}
                className="w-full rounded bg-amber-500 py-1 text-[10px] font-bold text-white hover:bg-amber-600"
              >
                Đang chờ →
              </button>
            </div>
          ))}
        </div>

        {/* Đang chờ */}
        <div className="rounded-xl border border-amber-200 bg-amber-50/50 p-2.5 space-y-2">
          <div className="flex items-center justify-between text-xs font-extrabold text-amber-900">
            <span>Đang chờ</span>
            <span className="rounded-full bg-amber-500 px-2 py-0.5 text-[10px] text-white">{waitingCount}</span>
          </div>
          {simTasks.filter(t => t.status === 'WAITING').map(t => (
            <div key={t.id} className="rounded-xl border border-amber-300 bg-white p-2.5 space-y-1.5 shadow-xs text-xs">
              <span className="rounded bg-slate-100 px-1 py-0.5 font-bold text-slate-700 text-[9px]">{t.tag}</span>
              <p className="font-bold text-slate-900 leading-tight">{t.title}</p>
              <button
                onClick={() => moveTask(t.id, 'IN_REVIEW')}
                className="w-full rounded bg-purple-600 py-1 text-[10px] font-bold text-white hover:bg-purple-700"
              >
                Gửi Review →
              </button>
            </div>
          ))}
        </div>

        {/* Đang review */}
        <div className="rounded-xl border border-purple-200 bg-purple-50/50 p-2.5 space-y-2">
          <div className="flex items-center justify-between text-xs font-extrabold text-purple-900">
            <span>Đang review</span>
            <span className="rounded-full bg-purple-600 px-2 py-0.5 text-[10px] text-white">{inReviewCount}</span>
          </div>
          {simTasks.filter(t => t.status === 'IN_REVIEW').map(t => (
            <div key={t.id} className="rounded-xl border border-purple-300 bg-white p-2.5 space-y-1.5 shadow-xs text-xs">
              <p className="font-bold text-slate-900 leading-tight">{t.title}</p>
              <button
                onClick={() => moveTask(t.id, 'DONE')}
                className="w-full rounded bg-emerald-600 py-1 text-[10px] font-bold text-white hover:bg-emerald-700"
              >
                Duyệt ✓ Thả vào Hoàn thành
              </button>
            </div>
          ))}
        </div>

        {/* Hoàn thành */}
        <div className="rounded-xl border border-emerald-200 bg-emerald-50/50 p-2.5 space-y-2">
          <div className="flex items-center justify-between text-xs font-extrabold text-emerald-900">
            <span>Hoàn thành</span>
            <span className="rounded-full bg-emerald-600 px-2 py-0.5 text-[10px] text-white">{doneCount}</span>
          </div>
          {simTasks.filter(t => t.status === 'DONE').map(t => (
            <div key={t.id} className="rounded-xl border border-emerald-300 bg-white p-2.5 space-y-1.5 shadow-xs text-xs">
              <p className="font-bold text-slate-400 line-through leading-tight">{t.title}</p>
              <button
                onClick={() => moveTask(t.id, 'IN_PROGRESS')}
                className="w-full rounded bg-amber-600 py-1 text-[10px] font-bold text-white hover:bg-amber-700"
              >
                Mở lại làm
              </button>
            </div>
          ))}
        </div>

        {/* Đã hủy */}
        <div className="rounded-xl border border-rose-200 bg-rose-50/50 p-2.5 space-y-2">
          <div className="flex items-center justify-between text-xs font-extrabold text-rose-900">
            <span>Đã hủy</span>
            <span className="rounded-full bg-rose-600 px-2 py-0.5 text-[10px] text-white">{cancelledCount}</span>
          </div>
          <div className="rounded-xl border border-dashed border-rose-200 bg-white/70 p-4 text-center text-[11px] text-slate-400">
            Thả task vào đây.
          </div>
        </div>
      </div>
    </div>
  )
}

// 2. Form Field Guide Component
function FormFieldGuide() {
  return (
    <div className="rounded-2xl border border-slate-200 bg-white p-5 space-y-4 shadow-xs">
      <h4 className="font-bold text-slate-900 text-sm border-b border-slate-100 pb-2">
        Mô Phỏng Giao Diện Form Tạo/Sửa Task Thực Tế & Ý Nghĩa Các Ô Nhập Liệu
      </h4>
      
      <div className="grid gap-3 sm:grid-cols-2">
        <div className="rounded-xl border border-slate-200 bg-slate-50 p-3 space-y-1.5">
          <div className="flex items-center justify-between">
            <span className="font-bold text-xs text-slate-800">1. Tiêu đề Task (Title)</span>
            <span className="text-[10px] font-bold text-rose-600">Bắt buộc</span>
          </div>
          <p className="text-xs text-slate-600">Mô tả ngắn gọn công việc cần thực hiện. Vd: <i>"Thiết kế màn hình Đăng nhập"</i>.</p>
        </div>

        <div className="rounded-xl border border-slate-200 bg-slate-50 p-3 space-y-1.5">
          <div className="flex items-center justify-between">
            <span className="font-bold text-xs text-slate-800">2. Loại công việc (Task Type)</span>
            <span className="text-[10px] font-bold text-blue-600">Phân loại</span>
          </div>
          <p className="text-xs text-slate-600">Gồm: <b>STORY</b> (Yêu cầu tính năng), <b>BUG</b> (Lỗi cần sửa), <b>TASK</b> (Công việc kỹ thuật).</p>
        </div>

        <div className="rounded-xl border border-slate-200 bg-slate-50 p-3 space-y-1.5">
          <div className="flex items-center justify-between">
            <span className="font-bold text-xs text-slate-800">3. Người thực hiện (Assignee)</span>
            <span className="text-[10px] font-bold text-emerald-600">Phân công</span>
          </div>
          <p className="text-xs text-slate-600">Chọn thành viên trong dự án phụ trách chính công việc này.</p>
        </div>

        <div className="rounded-xl border border-slate-200 bg-slate-50 p-3 space-y-1.5">
          <div className="flex items-center justify-between">
            <span className="font-bold text-xs text-slate-800">4. Mức độ ưu tiên (Priority)</span>
            <span className="text-[10px] font-bold text-amber-600">Ưu tiên</span>
          </div>
          <p className="text-xs text-slate-600">Gồm: <b>LOW</b>, <b>MEDIUM</b>, <b>HIGH</b>, <b>URGENT</b> (Cực kỳ khẩn cấp).</p>
        </div>

        <div className="rounded-xl border border-slate-200 bg-slate-50 p-3 space-y-1.5">
          <div className="flex items-center justify-between">
            <span className="font-bold text-xs text-slate-800">5. Điểm Story Points (SP)</span>
            <span className="text-[10px] font-bold text-purple-600">Độ phức tạp</span>
          </div>
          <p className="text-xs text-slate-600">Điểm độ khó theo tiêu chuẩn Agile (1, 2, 3, 5, 8, 13 điểm).</p>
        </div>

        <div className="rounded-xl border border-slate-200 bg-slate-50 p-3 space-y-1.5">
          <div className="flex items-center justify-between">
            <span className="font-bold text-xs text-slate-800">6. Số giờ dự tính (Estimated Hours)</span>
            <span className="text-[10px] font-bold text-teal-600">Thời gian</span>
          </div>
          <p className="text-xs text-slate-600">Số giờ dự kiến hoàn thành. Vd: <i>8h</i> (tương đương 1 ngày làm việc).</p>
        </div>
      </div>
    </div>
  )
}

/* =========================================================================
   MAIN USER GUIDE MODAL COMPONENT
   ========================================================================= */

export function UserGuideModal({ open, onClose }: UserGuideModalProps) {
  const [activeTab, setActiveTab] = useState<'step_by_step' | 'simulator' | 'roles' | 'forms'>('step_by_step')

  if (!open) return null

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 p-4 backdrop-blur-xs animate-fade-in overflow-y-auto">
      <div className="relative flex max-h-[92vh] w-full max-w-5xl flex-col overflow-hidden rounded-3xl bg-white shadow-2xl border border-slate-100">
        
        {/* Header */}
        <div className="flex shrink-0 items-center justify-between border-b border-slate-100 bg-slate-900 px-6 py-5 text-white">
          <div>
            <h2 className="text-xl font-black tracking-tight flex items-center gap-2">
              Cẩm Nang Hướng Dẫn Sử Dụng Dự Án (Từng Bước Cho Người Mới)
              <span className="rounded-full bg-teal-500/20 px-2.5 py-0.5 text-xs font-extrabold text-teal-300 border border-teal-500/30">
                Full Beginner Guide
              </span>
            </h2>
            <p className="text-xs text-slate-300 mt-1">Đọc xong là thành thạo 100% mọi thao tác: Tạo task, Kéo thả Kanban, Import Excel, Time Log</p>
          </div>
          <button
            onClick={onClose}
            className="grid size-9 place-items-center rounded-full bg-white/10 text-slate-300 transition hover:bg-white/20 hover:text-white"
          >
            <X size={20} />
          </button>
        </div>

        {/* Navigation Tabs */}
        <div className="flex border-b border-slate-200 bg-slate-50/80 px-6 pt-3 gap-2 overflow-x-auto shrink-0">
          <button
            onClick={() => setActiveTab('step_by_step')}
            className={`border-b-2 px-4 py-3 text-sm font-bold transition whitespace-nowrap ${
              activeTab === 'step_by_step'
                ? 'border-brand text-brand bg-white rounded-t-xl shadow-xs'
                : 'border-transparent text-slate-500 hover:text-slate-800'
            }`}
          >
            Quy Trình Thao Tác Chi Tiết (7 Bước)
          </button>
          <button
            onClick={() => setActiveTab('simulator')}
            className={`border-b-2 px-4 py-3 text-sm font-bold transition whitespace-nowrap ${
              activeTab === 'simulator'
                ? 'border-brand text-brand bg-white rounded-t-xl shadow-xs'
                : 'border-transparent text-slate-500 hover:text-slate-800'
            }`}
          >
            Giả Lập Thao Tác Kéo Thả (Demo Live)
          </button>
          <button
            onClick={() => setActiveTab('forms')}
            className={`border-b-2 px-4 py-3 text-sm font-bold transition whitespace-nowrap ${
              activeTab === 'forms'
                ? 'border-brand text-brand bg-white rounded-t-xl shadow-xs'
                : 'border-transparent text-slate-500 hover:text-slate-800'
            }`}
          >
            Chi Tiết Form & Ý Nghĩa Các Ô
          </button>
          <button
            onClick={() => setActiveTab('roles')}
            className={`border-b-2 px-4 py-3 text-sm font-bold transition whitespace-nowrap ${
              activeTab === 'roles'
                ? 'border-brand text-brand bg-white rounded-t-xl shadow-xs'
                : 'border-transparent text-slate-500 hover:text-slate-800'
            }`}
          >
            Quyền Hạn Từng Vai Trò (Roles)
          </button>
        </div>

        {/* Body Content */}
        <div className="flex-1 overflow-y-auto p-6 md:p-8 space-y-8 bg-slate-50/30">
          
          {/* TAB 1: STEP BY STEP GUIDE (7 BƯỚC THỰC HÀNH TỪ A - Z) */}
          {activeTab === 'step_by_step' && (
            <div className="space-y-6 animate-fade-in">
              <div className="rounded-2xl border border-teal-100 bg-teal-50/70 p-4 text-xs font-semibold text-teal-800 leading-relaxed">
                Dưới đây là cẩm nang chi tiết 7 bước dành cho một thành viên mới bắt đầu sử dụng hệ thống. Bạn chỉ cần thực hiện theo đúng các bước bên dưới để tạo, quản lý và hoàn thành dự án.
              </div>

              <div className="space-y-5">
                
                {/* Bước 1 */}
                <div className="rounded-2xl border border-slate-200 bg-white p-5 space-y-3 shadow-xs">
                  <div className="flex items-center justify-between border-b border-slate-100 pb-2">
                    <span className="font-extrabold text-brand text-sm">BƯỚC 1: ĐĂNG NHẬP & CHỌN DỰ ÁN</span>
                    <span className="text-xs font-mono text-slate-400">Vị trí: Góc trên bên trái màn hình</span>
                  </div>
                  <ul className="text-xs text-slate-700 space-y-2 list-disc pl-5 font-medium leading-relaxed">
                    <li>Sau khi đăng nhập, hệ thống hiển thị màn hình làm việc chính.</li>
                    <li>Ở góc trên bên trái, bấm vào <b>Dropdown chọn Dự án</b> để chọn dự án mà bạn đang làm việc.</li>
                    <li>Nếu bạn là Manager, bạn sẽ thấy nút <b>"+ Tạo dự án mới"</b> để khởi tạo dự án cho nhóm.</li>
                  </ul>
                </div>

                {/* Bước 2 */}
                <div className="rounded-2xl border border-slate-200 bg-white p-5 space-y-3 shadow-xs">
                  <div className="flex items-center justify-between border-b border-slate-100 pb-2">
                    <span className="font-extrabold text-indigo-600 text-sm">BƯỚC 2: TẠO TASK MỚI HOẶC TẠO BACKLOG ITEM</span>
                    <span className="text-xs font-mono text-slate-400">Vị trí: Bảng Scrum → Nút "+ Tạo Task"</span>
                  </div>
                  <ul className="text-xs text-slate-700 space-y-2 list-disc pl-5 font-medium leading-relaxed">
                    <li>Bấm vào nút <b>"+ Tạo Task"</b> hoặc <b>"+ Tạo Backlog"</b> ở góc phải bảng Scrum.</li>
                    <li>Màn hình hiển thị Form nhập liệu. Bạn điền: <b>Tiêu đề Task</b>, <b>Loại Task (STORY, BUG, TASK)</b>, <b>Người thực hiện (Assignee)</b>, <b>Mức ưu tiên (Priority)</b> và <b>Số giờ dự kiến (Estimated Hours)</b>.</li>
                    <li>Bấm nút <b>"Lưu Task"</b> để hoàn tất. Task sẽ tự động xuất hiện trên giao diện.</li>
                  </ul>
                </div>

                {/* Bước 3 */}
                <div className="rounded-2xl border border-slate-200 bg-white p-5 space-y-3 shadow-xs">
                  <div className="flex items-center justify-between border-b border-slate-100 pb-2">
                    <span className="font-extrabold text-teal-600 text-sm">BƯỚC 3: MẸO NHẬP HÀNG CHỤC TASK TỪ FILE EXCEL (IMPORT EXCEL)</span>
                    <span className="text-xs font-mono text-slate-400">Vị trí: Nút "Import Excel"</span>
                  </div>
                  <ul className="text-xs text-slate-700 space-y-2 list-disc pl-5 font-medium leading-relaxed">
                    <li>Khi có danh sách nhiều task sẵn có, bấm nút <b>"Tải tệp mẫu Excel"</b> để nhận file định dạng sẵn (.xlsx).</li>
                    <li>Mở file Excel vừa tải, nhập tiêu đề task, loại task, độ ưu tiên theo đúng cột hướng dẫn trong tệp mẫu.</li>
                    <li>Quay lại màn hình Bảng Scrum, bấm nút <b>"Nhập Task từ Excel"</b>, chọn file vừa lưu. Hệ thống tự động tạo toàn bộ Task chỉ trong vài giây!</li>
                  </ul>
                </div>

                {/* Bước 4 */}
                <div className="rounded-2xl border border-slate-200 bg-white p-5 space-y-3 shadow-xs">
                  <div className="flex items-center justify-between border-b border-slate-100 pb-2">
                    <span className="font-extrabold text-purple-600 text-sm">BƯỚC 4: LẬP KẾ HOẠCH & BẮT ĐẦU SPRINT (SPRINT PLANNING)</span>
                    <span className="text-xs font-mono text-slate-400">Vị trí: Khối Sprint Planning</span>
                  </div>
                  <ul className="text-xs text-slate-700 space-y-2 list-disc pl-5 font-medium leading-relaxed">
                    <li>Bấm nút <b>"+ Tạo Sprint"</b>, nhập tên Sprint (vd: Sprint 1) và mục tiêu (Goal).</li>
                    <li>Dùng chuột <b>kéo thả (Drag & Drop)</b> các User Story từ cột Product Backlog bên trái thả vào vùng chứa của Sprint 1.</li>
                    <li>Khi đã chuẩn bị xong công việc cho chu kỳ làm việc, bấm nút <b>"Bắt đầu Sprint"</b> để mở Bảng Kanban.</li>
                  </ul>
                </div>

                {/* Bước 5 */}
                <div className="rounded-2xl border border-slate-200 bg-white p-5 space-y-3 shadow-xs">
                  <div className="flex items-center justify-between border-b border-slate-100 pb-2">
                    <span className="font-extrabold text-emerald-600 text-sm">BƯỚC 5: KÉO THẢ DI CHUYỂN TRẠNG THÁI TASK TRÊN KANBAN REALTIME</span>
                    <span className="text-xs font-mono text-slate-400">Vị trí: Bảng Kanban 6 cột</span>
                  </div>
                  <ul className="text-xs text-slate-700 space-y-2 list-disc pl-5 font-medium leading-relaxed">
                    <li>Màn hình hiển thị các cột: <b>TODO (Cần làm)</b> → <b>IN_PROGRESS (Đang làm)</b> → <b>BLOCKED (Tạm dừng/Vướng mắc)</b> → <b>IN_REVIEW (Chờ duyệt)</b> → <b>DONE (Hoàn thành)</b>.</li>
                    <li>Khi bắt đầu làm việc, bạn giữ chuột vào thẻ Task và <b>kéo thả</b> từ cột TODO sang cột IN_PROGRESS.</li>
                    <li>Khi làm xong, bạn kéo thả sang IN_REVIEW hoặc DONE. Hệ thống tự động cập nhật ngay lập tức cho toàn nhóm qua WebSocket mà <b>không cần nhấn F5</b>.</li>
                  </ul>
                </div>

                {/* Bước 6 */}
                <div className="rounded-2xl border border-slate-200 bg-white p-5 space-y-3 shadow-xs">
                  <div className="flex items-center justify-between border-b border-slate-100 pb-2">
                    <span className="font-extrabold text-amber-600 text-sm">BƯỚC 6: GHI LOG GIỜ LÀM VIỆC (TIME LOG) & ĐĂNG BÌNH LUẬN</span>
                    <span className="text-xs font-mono text-slate-400">Vị trí: Click trực tiếp vào thẻ Task</span>
                  </div>
                  <ul className="text-xs text-slate-700 space-y-2 list-disc pl-5 font-medium leading-relaxed">
                    <li>Click vào thẻ Task bất kỳ để mở <b>Popup Chi Tiết Task</b>.</li>
                    <li>Chuyển sang tab <b>Time Log</b>: Nhập số giờ thực tế bạn đã làm trong ngày (vd: 3.5h) và nội dung công việc. Bấm <b>"Lưu giờ làm"</b>.</li>
                    <li>Chuyển sang tab <b>Bình luận</b>: Đăng trao đổi công việc với đồng nghiệp và đính kèm file sản phẩm (ảnh, tài liệu).</li>
                  </ul>
                </div>

                {/* Bước 7 */}
                <div className="rounded-2xl border border-slate-200 bg-white p-5 space-y-3 shadow-xs">
                  <div className="flex items-center justify-between border-b border-slate-100 pb-2">
                    <span className="font-extrabold text-rose-600 text-sm">BƯỚC 7: NGHỆM THU, ĐÓNG SPRINT & XUẤT BÁO CÁO EXCEL/PDF</span>
                    <span className="text-xs font-mono text-slate-400">Vị trí: Nút "Đóng Sprint"</span>
                  </div>
                  <ul className="text-xs text-slate-700 space-y-2 list-disc pl-5 font-medium leading-relaxed">
                    <li>Khi hết thời hạn Sprint, bấm nút <b>"Đóng Sprint"</b>.</li>
                    <li>Hệ thống tổng kết kết quả: Bao nhiêu % Task đã hoàn thành. Các Task chưa hoàn thành sẽ được hỏi để chuyển về Backlog hoặc Sprint tiếp theo.</li>
                    <li>Bấm nút <b>"Xuất báo cáo PDF / Excel"</b> để lưu trữ hồ sơ nghiệm thu dự án.</li>
                  </ul>
                </div>

              </div>
            </div>
          )}

          {/* TAB 2: SIMULATOR (BỘ GIẢ LẬP KÉO THẢ MÔ PHỎNG) */}
          {activeTab === 'simulator' && (
            <div className="space-y-6 animate-fade-in">
              <div className="rounded-2xl border border-blue-100 bg-blue-50/70 p-4 text-xs font-semibold text-blue-900 leading-relaxed">
                Bạn hãy thử bấm các nút di chuyển dưới đây để trải nghiệm cách Bảng Kanban hoạt động thực tế trong ứng dụng!
              </div>

              <KanbanInteractiveSimulator />
            </div>
          )}

          {/* TAB 3: FORM GUIDE */}
          {activeTab === 'forms' && (
            <div className="space-y-6 animate-fade-in">
              <FormFieldGuide />
            </div>
          )}

          {/* TAB 4: ROLES */}
          {activeTab === 'roles' && (
            <div className="space-y-6 animate-fade-in">
              <div className="grid gap-4 sm:grid-cols-2">
                
                {/* ADMIN */}
                <div className="rounded-2xl border border-amber-200 bg-amber-50/40 p-5 space-y-3">
                  <div>
                    <h3 className="font-extrabold text-slate-900">ADMIN (Quản trị hệ thống)</h3>
                    <p className="text-xs text-amber-700 font-medium">Quyền cao nhất toàn bộ hệ thống</p>
                  </div>
                  <ul className="text-xs text-slate-700 space-y-2 list-disc pl-4 font-medium">
                    <li>Quản lý toàn bộ danh sách Người dùng (Tạo, sửa role, mở/khóa tài khoản).</li>
                    <li>Xem <b>Nhật ký Hệ thống (Audit Log)</b> ghi vết tất cả hành vi trong ứng dụng.</li>
                    <li>Kích hoạt quét thủ công Nhắc hẹn Task & Gửi Daily Digest Notification.</li>
                    <li>Xem tất cả thống kê tổng quan KPI dự án.</li>
                  </ul>
                </div>

                {/* MANAGER & PM */}
                <div className="rounded-2xl border border-blue-200 bg-blue-50/40 p-5 space-y-3">
                  <div>
                    <h3 className="font-extrabold text-slate-900">MANAGER / PROJECT MANAGER</h3>
                    <p className="text-xs text-blue-700 font-medium">Quản lý Dự án & Quản trị Thành viên</p>
                  </div>
                  <ul className="text-xs text-slate-700 space-y-2 list-disc pl-4 font-medium">
                    <li>Khởi tạo Dự án mới và chỉnh sửa thông tin dự án.</li>
                    <li>Thêm/xóa thành viên vào dự án và gán vai trò (Owner, PM, Dev, QA).</li>
                    <li>Tạo, sửa, xóa, kéo thả Task, Backlog và Sprint.</li>
                    <li>Xuất báo cáo Excel & PDF kết quả Sprint.</li>
                  </ul>
                </div>

                {/* SCRUM MASTER & PO */}
                <div className="rounded-2xl border border-purple-200 bg-purple-50/40 p-5 space-y-3">
                  <div>
                    <h3 className="font-extrabold text-slate-900">SCRUM MASTER / PRODUCT OWNER</h3>
                    <p className="text-xs text-purple-700 font-medium">Điều phối Quy trình & Nghiệm thu Requirement</p>
                  </div>
                  <ul className="text-xs text-slate-700 space-y-2 list-disc pl-4 font-medium">
                    <li>Quản lý Product Backlog, nhập điểm Story Points và độ ưu tiên.</li>
                    <li>Lập kế hoạch Sprint, kích hoạt Bắt đầu Sprint và Đóng Sprint.</li>
                    <li>Kéo thả Task, phân công người làm và theo dõi biểu đồ Burndown.</li>
                    <li>Nghiệm thu kết quả bàn giao công việc của Team.</li>
                  </ul>
                </div>

                {/* DEVELOPER & QA & MEMBER */}
                <div className="rounded-2xl border border-emerald-200 bg-emerald-50/40 p-5 space-y-3">
                  <div>
                    <h3 className="font-extrabold text-slate-900">DEVELOPER / QA / MEMBER</h3>
                    <p className="text-xs text-emerald-700 font-medium">Thành viên thực thi dự án</p>
                  </div>
                  <ul className="text-xs text-slate-700 space-y-2 list-disc pl-4 font-medium">
                    <li>Tạo Task mới, sửa Task và <b>kéo thả di chuyển trạng thái Task</b> trên Kanban.</li>
                    <li>Nhập Task hàng loạt bằng <b>Tải tệp mẫu Excel & Import</b>.</li>
                    <li>Tự nhận việc hoặc phân công Task cho đồng nghiệp.</li>
                    <li>Ghi log giờ làm việc (Time Log), bình luận và đính kèm file sản phẩm.</li>
                  </ul>
                </div>

              </div>
            </div>
          )}

        </div>

        {/* Footer */}
        <div className="flex shrink-0 items-center justify-end border-t border-slate-100 bg-slate-50 px-6 py-4">
          <Button variant="primary" onClick={onClose} className="px-6 font-bold">
            Đã hiểu & Bắt đầu làm việc
          </Button>
        </div>

      </div>
    </div>
  )
}
