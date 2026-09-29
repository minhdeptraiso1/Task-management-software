---
name: uiuxpromax
description: >-
  Chuyên gia thiết kế UI/UX Pro Max và chuẩn hóa component cho Frontend dự án (React, Tailwind CSS, Lucide Icons, MVC architecture). Kích hoạt skill này khi tạo mới, sửa đổi, tái cấu trúc hoặc audit giao diện người dùng, bảng điều khiển (dashboard), form nhập liệu, modal, bảng dữ liệu, và bắt buộc dùng toàn bộ component chuẩn từ `src/components/ui/` thay vì HTML thô.
---

# UI/UX Pro Max — Frontend Design Intelligence & Component System

Skill này hướng dẫn toàn bộ quy chuẩn thiết kế, bảng màu Design Tokens, cấu trúc bố cục hiện đại (Bento Grid, Clean SaaS) và cách sử dụng **toàn bộ 20+ component dùng chung** tại `Fe/src/components/ui/`.

---

## 1. Nguyên Tắc Cốt Lõi (Core Rules)

> ⚠️ **LUẬT BẤT THÀNH VĂN (TUYỆT ĐỐI TUÂN THỦ):**
> 1. **KHÔNG DÙNG RAW HTML**: Nghiêm cấm viết trực tiếp `<button>`, `<input>`, `<select>`, `<textarea>`, pagination tùy biến hoặc header/empty state tự chế trong các View.
> 2. **100% IMPORT TỪ `@/components/ui`**: Mọi control phải được import từ `src/components/ui`.
> 3. **DESIGN TOKENS**: Dùng đúng tên token màu (`brand`, `brand-dark`, `ink`, `muted`, `line`, `panel`, `canvas`, `success`, `danger`, `info`). Tuyệt đối không hardcode mã màu hex (`#f7941d`) hay màu mặc định không chuẩn (`bg-orange-500`, `text-gray-900`).
> 4. **SPACING 4/8pt SCALE**: Chỉ dùng spacing chuẩn: `4, 8, 12, 16, 20, 24, 32, 40, 48px` (`gap-1`, `gap-2`, `gap-3`, `gap-4`, `gap-5`, `gap-6`, `gap-8`, `gap-10`, `gap-12`). Không dùng `p-3.5`, `px-7`.
> 5. **TYPOGRAPHY**: Base 14px cho dữ liệu bảng và form enterprise. Số liệu thống kê/tiền tệ/thời gian luôn dùng `tabular-nums`.
> 6. **MVC PATTERN**: View chỉ hiển thị dữ liệu và phát sự kiện qua props/callbacks; không gọi API trực tiếp, không chứa business logic phức tạp.

---

## 2. Hệ Thống Design Tokens

### 2.1. Màu Thương Hiệu (Brand)
| Token Tailwind | Giá trị Hex | Ứng dụng |
| :--- | :--- | :--- |
| `bg-brand` / `text-brand` | `#F7941D` | Nút CTA chính (Primary Button), icon active, viền focus |
| `bg-brand-dark` | `#E07F0E` | Hover của nút primary, link active |
| `bg-brand-active` | `#C76C09` | Trạng thái active/pressed |
| `bg-brand-black` | `#1A1A1A` | Header và Sidebar nền tối, logo |
| `bg-accent` | `#FFC20E` | Điểm kết thúc gradient, cảnh báo nhẹ |

### 2.2. Màu Ngữ Nghĩa & Trung Tính (Semantic & Neutral)
| Token Tailwind | Giá trị Hex | Ứng dụng |
| :--- | :--- | :--- |
| `text-ink` | `#1A1A1A` | Chữ chính, tiêu đề |
| `text-muted` | `#71717A` | Chữ phụ, gợi ý, placeholder, icon mờ |
| `border-line` | `#D4D4D8` | Đường viền card, bảng, input |
| `bg-panel` | `#F4F4F5` | Nền thanh công cụ, nền section, badge neutral |
| `bg-canvas` | `#FAFAFA` | Nền tổng thể trang |
| `text-success` / `bg-success` | `#1F9D55` | Hoàn thành, thành công, hợp lệ |
| `text-danger` / `bg-danger` | `#D92D20` | Lỗi, nút xóa nguy hiểm, quá hạn |
| `text-info` / `bg-info` | `#2E6FE0` | Thông tin, trạng thái đang xử lý |

### 2.3. Màu Phân Loại Bản Chất (Category Tokens — Non-semantic)
*Chỉ dùng phân loại loại công việc/cuộc họp, KHÔNG dùng làm trạng thái hoàn thành:*
- `cat-indigo` (`#4F46E5`): `SPRINT_REVIEW`
- `cat-purple` (`#7C3AED`): `RETROSPECTIVE`, Backlog `FEATURE`
- `cat-cyan` (`#0284C7`): Backlog `EPIC`, Docs
- `cat-teal` (`#0D9488`): Backlog `USER_STORY`, Subtask
- `cat-pink` (`#DB2777`): Backlog `TECHNICAL`, Refactor

---

## 3. Danh Mục Chi Tiết & Mẫu Code Toàn Bộ UI Component

Import tập trung từ `components/ui`:
```tsx
import {
  Button,
  Input,
  Select,
  Textarea,
  Checkbox,
  Radio,
  Badge,
  StatCard,
  Modal,
  ConfirmDialog,
  DatePicker,
  PageHeader,
  Pagination,
  EmptyState,
  Tabs,
  Toast,
  CollapsiblePanel,
  ActionMenu,
  LoadingScreen,
  ToolbarActions,
} from '@/components/ui'
```

---

### 3.1. `<Button>` — Nút bấm chuẩn UI/UX Pro Max

**Các Variant:** `primary` | `secondary` | `ghost` | `outline` | `danger` | `solid-blue` | `solid-green` | `solid-red`  
**Các Tone:** `brand` | `danger` | `success` | `warning` | `info` | `neutral` | `dark`  
**Size:** `sm` (h-8, text-xs) | `md` (h-10, text-sm)

```tsx
// 1. Primary Action (Duy nhất 1 nút chính trong một khu vực)
<Button
  variant="primary"
  leadingIcon={<Plus size={16} />}
  onClick={handleCreate}
>
  Tạo công việc mới
</Button>

// 2. Secondary / Cancel Action
<Button variant="secondary" onClick={onCancel}>
  Hủy bỏ
</Button>

// 3. Danger Action (Xóa, thao tác hủy)
<Button
  variant="danger"
  leadingIcon={<Trash2 size={16} />}
  onClick={handleDelete}
>
  Xóa bản ghi
</Button>

// 4. Ghost Action (Nút trong bảng, icon toolbar)
<Button
  variant="ghost"
  size="sm"
  leadingIcon={<RefreshCw size={14} />}
  onClick={refreshData}
>
  Làm mới
</Button>

// 5. Icon-Only Action (BẮT BUỘC có aria-label và title)
<Button
  variant="ghost"
  size="sm"
  iconOnly
  leadingIcon={<Edit size={16} />}
  aria-label="Chỉnh sửa công việc"
  title="Chỉnh sửa công việc"
  onClick={() => handleEdit(task)}
/>

// 6. Loading state (Tự động disable và hiện spinner)
<Button variant="primary" loading={isSubmitting}>
  Lưu thay đổi
</Button>
```

---

### 3.2. `<Input>` & Input Ngày (`type="date"`)

Tích hợp sẵn label, error message, hint, leading/trailing icon. Khi `type="date"`, tự động hiển thị DatePicker chuẩn tiếng Việt!

```tsx
// 1. Text input kèm label và leading icon
<Input
  label="Tiêu đề công việc"
  required
  placeholder="Nhập tiêu đề tóm tắt..."
  value={title}
  onChange={e => setTitle(e.target.value)}
  leadingIcon={<FileText size={16} />}
  error={errors.title}
/>

// 2. Input tìm kiếm có trailing clear
<Input
  placeholder="Tìm kiếm theo mã hoặc tên..."
  value={searchQuery}
  onChange={e => setSearchQuery(e.target.value)}
  leadingIcon={<Search size={16} />}
  trailing={searchQuery && (
    <Button
      variant="ghost"
      size="sm"
      iconOnly
      leadingIcon={<X size={14} />}
      onClick={() => setSearchQuery('')}
      aria-label="Xóa tìm kiếm"
    />
  )}
/>

// 3. Date Input (Tự động tích hợp DatePicker popup chuẩn giao diện)
<Input
  type="date"
  label="Hạn chót (Due date)"
  value={dueDate}
  onChange={e => setDueDate(e.target.value)}
  hint="Định dạng ngày/tháng/năm"
/>
```

---

### 3.3. `<Select>` — Hộp chọn chuẩn

```tsx
<Select
  label="Mức độ ưu tiên"
  required
  value={priority}
  onChange={e => setPriority(e.target.value)}
  options={[
    { label: 'Thấp (Low)', value: 'LOW' },
    { label: 'Trung bình (Medium)', value: 'MEDIUM' },
    { label: 'Cao (High)', value: 'HIGH' },
    { label: 'Khẩn cấp (Urgent)', value: 'URGENT' },
  ]}
/>
```

---

### 3.4. `<Textarea>` — Nhập văn bản nhiều dòng

```tsx
<Textarea
  label="Mô tả chi tiết"
  rows={4}
  value={description}
  onChange={e => setDescription(e.target.value)}
  placeholder="Mô tả các bước thực hiện, tiêu chuẩn nghiệm thu (DoD)..."
  hint="Tối đa 2,000 ký tự"
  error={errors.description}
/>
```

---

### 3.5. `<Checkbox>` & `<Radio>` — Lựa chọn Boolean / Tùy chọn

```tsx
// Checkbox kèm mô tả phụ
<Checkbox
  label="Tạo thêm công việc khác sau khi lưu"
  description="Giữ nguyên form để tiếp tục nhập nhanh"
  checked={createAnother}
  onChange={e => setCreateAnother(e.target.checked)}
/>

// Checkbox trong bộ lọc danh sách
<Checkbox
  label="Chỉ xem task quá hạn"
  checked={filterOverdue}
  onChange={e => setFilterOverdue(e.target.checked)}
/>

// Radio option
<Radio
  name="visibility"
  value="PUBLIC"
  checked={visibility === 'PUBLIC'}
  onChange={() => setVisibility('PUBLIC')}
  label="Công khai"
  description="Mọi thành viên trong dự án đều xem được"
/>
```

---

### 3.6. `<PageHeader>` — Tiêu đề trang chuẩn mực

```tsx
<PageHeader
  title="Quản lý Công việc & Sprint"
  description="Theo dõi tiến độ backlog, phân công nhiệm vụ và rủi ro sprint hiện tại."
  icon={<FolderKanban size={22} />}
  badge={<Badge variant="brand">Sprint 4 Active</Badge>}
  actions={
    <div className="flex items-center gap-2">
      <Button variant="secondary" leadingIcon={<Download size={16} />}>
        Xuất Excel
      </Button>
      <Button variant="primary" leadingIcon={<Plus size={16} />} onClick={openCreateModal}>
        Tạo Task mới
      </Button>
    </div>
  }
/>
```

---

### 3.7. `<Pagination>` — Phân trang chuẩn phân cách

Hỗ trợ cả chế độ chuẩn (đầy đủ) và `compact` (dùng cho sidebar hoặc bảng phụ hẹp):

```tsx
// 1. Phân trang đầy đủ ở cuối bảng dữ liệu
<Pagination
  page={currentPage} // 0-indexed
  totalPages={totalPages}
  totalElements={totalRecords}
  onPageChange={newPage => setCurrentPage(newPage)}
  disabled={isLoading}
/>

// 2. Chế độ compact cho Sidebar / Danh sách hẹp
<Pagination
  page={page}
  totalPages={totalPages}
  onPageChange={setPage}
  compact
/>
```

---

### 3.8. `<EmptyState>` — Trạng thái danh sách rỗng

Tuân thủ quy chuẩn UX: Icon minh họa + Tiêu đề ngắn + Mô tả hướng dẫn + Nút hành động kêu gọi (CTA).

```tsx
<EmptyState
  icon={<Inbox size={36} className="text-muted/60" />}
  title="Chưa có công việc nào"
  description="Dự án hiện tại chưa có task nào được tạo. Hãy bắt đầu bằng cách tạo task đầu tiên."
  action={
    <Button variant="primary" size="sm" leadingIcon={<Plus size={14} />} onClick={openCreateModal}>
      Tạo task ngay
    </Button>
  }
  bordered={true}
  className="my-6"
/>
```

---

### 3.9. `<Tabs>` — Điều hướng phân đoạn (Line / Pill / Enclosed)

```tsx
// 1. Line variant (Chuẩn cho đầu màn hình hoặc tab lớn)
<Tabs
  variant="line"
  activeTab={activeTab}
  onChange={id => setActiveTab(id)}
  tabs={[
    { id: 'board', label: 'Kanban Board', icon: <LayoutKanban size={16} />, badge: tasks.length },
    { id: 'list', label: 'Danh sách Backlog', icon: <ListTodo size={16} /> },
    { id: 'timeline', label: 'Timeline & Gantt', icon: <Calendar size={16} /> },
  ]}
/>

// 2. Pill variant (Chuẩn cho tab lọc nhỏ, view switch)
<Tabs
  variant="pill"
  size="sm"
  activeTab={filterStatus}
  onChange={setFilterStatus}
  tabs={[
    { id: 'ALL', label: 'Tất cả' },
    { id: 'IN_PROGRESS', label: 'Đang làm', badge: 12 },
    { id: 'DONE', label: 'Đã xong' },
  ]}
/>
```

---

### 3.10. `<StatCard>` — Thẻ chỉ số phong cách Bento Grid

```tsx
<div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
  <StatCard
    title="Tổng số Task"
    value="128"
    icon={<CheckSquare size={20} />}
    variant="brand"
    subtext="12 task mới tạo hôm nay"
    trend={{ value: "+8%", isPositive: true }}
  />
  <StatCard
    title="Đã Hoàn Thành"
    value="84"
    icon={<CheckCircle2 size={20} />}
    variant="success"
    subtext="Đạt 65.6% kế hoạch"
    trend={{ value: "+12%", isPositive: true }}
  />
  <StatCard
    title="Task Quá Hạn"
    value="5"
    icon={<AlertTriangle size={20} />}
    variant="danger"
    subtext="Cần xử lý gấp trong Sprint"
    trend={{ value: "+2", isPositive: false }}
  />
  <StatCard
    title="Giờ Làm Thực Tế"
    value="340h"
    icon={<Clock size={20} />}
    variant="info"
    subtext="Kế hoạch: 320h"
  />
</div>
```

---

### 3.11. `<Modal>` & `<ConfirmDialog>` — Hộp thoại chuyên nghiệp

```tsx
// 1. Form Modal hoàn chỉnh
<Modal
  open={isOpen}
  onClose={() => setIsOpen(false)}
  title="Tạo Công Việc Mới"
  description="Điền các thông tin chi tiết bên dưới để tạo task trong sprint."
  maxWidthClass="max-w-[700px]"
  actions={
    <div className="flex items-center gap-2">
      <Button variant="secondary" onClick={() => setIsOpen(false)}>Hủy</Button>
      <Button variant="primary" loading={isSaving} onClick={handleSave}>Lưu Task</Button>
    </div>
  }
>
  <form className="space-y-4" onSubmit={handleSubmit}>
    <Input label="Tiêu đề task" required ... />
    <div className="grid grid-cols-2 gap-4">
      <Select label="Ưu tiên" ... />
      <Input type="date" label="Hạn chót" ... />
    </div>
    <Textarea label="Mô tả" rows={3} ... />
  </form>
</Modal>

// 2. Confirm Dialog trước thao tác hủy / xóa
<ConfirmDialog
  open={showDeleteConfirm}
  title="Xác nhận xóa công việc"
  description={`Bạn có chắc chắn muốn xóa "${taskToDelete?.title}"? Dữ liệu đính kèm và lịch sử log work sẽ bị xóa hoàn toàn.`}
  confirmLabel="Xóa vĩnh viễn"
  loading={isDeleting}
  onCancel={() => setShowDeleteConfirm(false)}
  onConfirm={executeDelete}
/>
```

---

### 3.12. `<Badge>` — Nhãn trạng thái & phân loại

```tsx
<Badge variant="success">Hoàn thành</Badge>
<Badge variant="danger">Quá hạn</Badge>
<Badge variant="warning">Đang review</Badge>
<Badge variant="info">Đang thực hiện</Badge>
<Badge variant="brand">Sprint 4</Badge>
<Badge variant="neutral">Chưa bắt đầu</Badge>
```

---

## 4. Mẫu View Thực Chiến (Standard View Template)

Mẫu template chuẩn cho mọi màn hình quản lý / bảng dữ liệu theo kiến trúc MVC:

```tsx
import { useState } from 'react'
import { Plus, Search, Filter, RefreshCw, FolderKanban, Edit, Trash2 } from 'lucide-react'
import {
  PageHeader,
  Button,
  Input,
  Select,
  Pagination,
  EmptyState,
  Badge,
  Modal,
  ConfirmDialog,
} from '@/components/ui'

export function ExampleManagementView({
  items,
  totalItems,
  page,
  totalPages,
  isLoading,
  onPageChange,
  onSearch,
  onCreate,
  onDelete,
}: ExampleViewProps) {
  const [search, setSearch] = useState('')
  const [filterStatus, setFilterStatus] = useState('ALL')
  const [selectedItem, setSelectedItem] = useState<Item | null>(null)
  const [showDeleteModal, setShowDeleteModal] = useState(false)

  return (
    <div className="p-6 space-y-6 bg-canvas min-h-screen">
      {/* 1. Header Trang */}
      <PageHeader
        title="Danh sách Hạng mục"
        description="Quản lý và giám sát toàn bộ hạng mục trong dự án."
        icon={<FolderKanban size={24} />}
        actions={
          <Button variant="primary" leadingIcon={<Plus size={16} />} onClick={onCreate}>
            Tạo mới
          </Button>
        }
      />

      {/* 2. Thanh Công Cụ / Bộ Lọc (Toolbar) */}
      <div className="flex flex-col sm:flex-row items-center justify-between gap-3 bg-white p-4 rounded-2xl border border-line shadow-2xs">
        <div className="w-full sm:w-80">
          <Input
            placeholder="Tìm kiếm theo tên hoặc mã..."
            leadingIcon={<Search size={16} />}
            value={search}
            onChange={e => {
              setSearch(e.target.value)
              onSearch(e.target.value)
            }}
          />
        </div>
        <div className="flex items-center gap-2 w-full sm:w-auto">
          <Select
            value={filterStatus}
            onChange={e => setFilterStatus(e.target.value)}
            options={[
              { label: 'Tất cả trạng thái', value: 'ALL' },
              { label: 'Đang hoạt động', value: 'ACTIVE' },
              { label: 'Đã hoàn thành', value: 'DONE' },
            ]}
          />
        </div>
      </div>

      {/* 3. Bảng Dữ Liệu hoặc Empty State */}
      <div className="bg-white rounded-2xl border border-line shadow-2xs overflow-hidden">
        {items.length === 0 ? (
          <div className="p-8">
            <EmptyState
              title="Không có dữ liệu phù hợp"
              description="Thử thay đổi bộ lọc tìm kiếm hoặc tạo một mục mới."
              action={
                <Button variant="primary" size="sm" onClick={onCreate}>
                  Tạo mục mới
                </Button>
              }
            />
          </div>
        ) : (
          <>
            <div className="overflow-x-auto">
              <table className="w-full text-left text-sm">
                <thead className="bg-panel border-b border-line text-xs font-semibold text-muted uppercase">
                  <tr>
                    <th className="px-5 py-3.5">Mã</th>
                    <th className="px-5 py-3.5">Tiêu đề</th>
                    <th className="px-5 py-3.5">Trạng thái</th>
                    <th className="px-5 py-3.5 text-right">Thao tác</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-line/60">
                  {items.map(item => (
                    <tr key={item.id} className="hover:bg-slate-50/80 transition-colors">
                      <td className="px-5 py-3.5 font-bold text-ink">{item.code}</td>
                      <td className="px-5 py-3.5 font-medium text-ink">{item.title}</td>
                      <td className="px-5 py-3.5">
                        <Badge variant={item.status === 'DONE' ? 'success' : 'info'}>
                          {item.status}
                        </Badge>
                      </td>
                      <td className="px-5 py-3.5 text-right">
                        <div className="flex items-center justify-end gap-1">
                          <Button
                            variant="ghost"
                            size="sm"
                            iconOnly
                            leadingIcon={<Trash2 size={16} className="text-danger" />}
                            aria-label="Xóa"
                            onClick={() => {
                              setSelectedItem(item)
                              setShowDeleteModal(true)
                            }}
                          />
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {/* 4. Phân trang chuẩn */}
            <Pagination
              page={page}
              totalPages={totalPages}
              totalElements={totalItems}
              onPageChange={onPageChange}
            />
          </>
        )}
      </div>

      {/* 5. Dialog xác nhận xóa */}
      <ConfirmDialog
        open={showDeleteModal}
        title="Xóa hạng mục"
        description={`Bạn có chắc muốn xóa "${selectedItem?.title}"?`}
        confirmLabel="Xác nhận xóa"
        onCancel={() => setShowDeleteModal(false)}
        onConfirm={() => {
          if (selectedItem) onDelete(selectedItem.id)
          setShowDeleteModal(false)
        }}
      />
    </div>
  )
}
```

---

## 5. Bảng Kiểm Tra Hoàn Thành (Audit Checklist)

Trước khi xác nhận hoàn thành bất kỳ task UI nào trên Frontend:
- [ ] **Zero Raw Tags**: Kiểm tra không có thẻ `<button>`, `<input>`, `<select>`, `<textarea>` thô nào chưa được chuyển sang component từ `@/components/ui`.
- [ ] **Pagination**: Không dùng cụm footer `<Button>Trước</Button> <Button>Sau</Button>` tự viết; luôn dùng `<Pagination page={...} totalPages={...} onPageChange={...} />`.
- [ ] **Page Header**: Tiêu đề trang dùng `<PageHeader>`.
- [ ] **Empty State**: Bảng/danh sách rỗng dùng `<EmptyState>`.
- [ ] **Icon-Only Buttons**: Có thuộc tính `aria-label` và `title`.
- [ ] **Brand Consistency**: Đảm bảo thương hiệu là **BICAS** (không dùng HICAS cũ).
- [ ] **Build Check**: Chạy `npm run build` không phát sinh lỗi TypeScript (`0 errors`) và không có biến/import thừa.
