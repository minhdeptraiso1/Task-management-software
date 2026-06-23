# Quy chuẩn UI/UX — Hệ thống ERP/Quản lý dự án nội bộ HiCAS

**Phiên bản:** 1.0  
**Phạm vi:** HiCAS One — React + TypeScript  
**Đối tượng:** Designer, Frontend Developer, QA, Product Owner

Tài liệu này và `Luat.md` là nguồn chuẩn bắt buộc trước khi tạo hoặc sửa giao diện.

## 1. Mục tiêu và nguyên tắc

HiCAS One là công cụ làm việc hằng ngày cho kỹ thuật, quản lý dự án, kế toán và hành chính.

- Rõ ràng hơn hoa mỹ: dễ đọc, dễ quét, giảm tải nhận thức.
- Nhất quán: mọi màn hình dùng chung pattern và component; không sáng tạo riêng lẻ.
- Hiệu quả thao tác: ít click, hỗ trợ bàn phím, tối ưu người dùng thành thạo.
- Phản hồi tức thì: lưu, xóa, gửi luôn có loading, success hoặc error rõ ràng.
- Chịu lỗi tốt: validate sớm; hành động phá hủy phải xác nhận và có đường quay lại.
- Đúng thương hiệu: đen HiCAS và cam–vàng dùng tiết chế như một công cụ làm việc, không phải landing page.

## 2. Hệ thống màu

Token được khai báo tại `src/index.css` và sử dụng qua Tailwind.

### 2.1. Màu thương hiệu

| Token | Giá trị | Vai trò |
|---|---|---|
| `brand-black` | `#1A1A1A` | Header/sidebar tối, logo, tiêu đề mạnh |
| `brand` / `primary` | `#F7941D` | CTA chính, link quan trọng, focus |
| `brand-dark` | `#E07F0E` | Hover CTA |
| `primary-active` | `#C76C09` | Active/pressed |
| `accent` / `warning` | `#FFC20E` | Điểm cuối gradient, cảnh báo |
| `brand-gradient` | `135deg, #F7941D → #FFC20E` | Logo, badge nổi, tiến trình nhỏ |

Gradient không dùng cho nền chữ dài hoặc nhiều button lặp lại. Button chính dùng cam đặc `#F7941D`.

### 2.2. Màu ngữ nghĩa và trung tính

| Token | Giá trị | Vai trò |
|---|---|---|
| `success` | `#1F9D55` | Thành công, hoàn thành, đã duyệt |
| `danger` | `#D92D20` | Lỗi, xóa, quá hạn |
| `info` | `#2E6FE0` | Thông tin, đang xử lý, link phụ |
| `ink` / gray-900 | `#1A1A1A` | Text chính |
| gray-700 | `#3F3F46` | Text phụ, label |
| `muted` / gray-500 | `#71717A` | Placeholder, disabled |
| `line` / gray-300 | `#D4D4D8` | Border, divider |
| `panel` / gray-100 | `#F4F4F5` | Section, hover |
| `canvas` / gray-50 | `#FAFAFA` | Nền trang |
| white | `#FFFFFF` | Card, input |

Quy tắc: không quá hai màu nhấn trên một màn hình; màu ngữ nghĩa chỉ biểu đạt trạng thái; text đạt WCAG AA 4.5:1.

## 3. Typography

- Font: `Inter`; dự phòng `-apple-system`, `Segoe UI`, Roboto, Helvetica, Arial, sans-serif.
- Dữ liệu bảng/báo cáo bật tabular numbers (`font-variant-numeric: tabular-nums`).
- `h1`: 28/36, 700. `h2`: 22/30, 600. `h3`: 18/26, 600.
- Body mặc định: 14/22, 400. Label: 14/22, 500. Small: 13/18. Caption: 12/16.
- ERP dùng base 14px để hiển thị dữ liệu hiệu quả; không tự ý phóng body lên 16px.

## 4. Spacing và app shell

Chỉ dùng thang `4, 8, 12, 16, 20, 24, 32, 40, 48px`; không dùng giá trị tùy ý.

- Header: cao `56px`, nền `brand-black`; gồm logo, breadcrumb/global search, thông báo và avatar.
- Sidebar: rộng `240px`, nền `brand-black`; có thể thu còn `72px` ở 1024–1279px.
- Item active: vạch trái màu `primary` và nền `rgba(247,148,29,.12)`.
- Main: nền gray-50, padding mặc định `24px`, không giới hạn cứng chiều rộng trên desktop.
- Desktop-first: `≥1280px` chuẩn; `1024–1279px` thu sidebar; `<1024px` vẫn usable và có thể cảnh báo trải nghiệm tốt nhất trên màn hình lớn.

## 5. Component

Mọi control nền tảng phải import từ `src/components/ui` theo `Luat.md`.

### 5.1. Button

- Primary: nền cam, chữ trắng; một khu vực chỉ có một primary.
- Secondary: nền trắng, border gray-300, text gray-900.
- Danger: nền đỏ, chữ trắng; hành động không thể hoàn tác phải mở confirm dialog.
- Ghost: không nền/viền, chữ primary; dùng cho action phụ trong bảng.
- Icon truyền qua `leadingIcon`/`trailingIcon`. Icon-only dùng `iconOnly`, bắt buộc `aria-label` và tooltip (`title`).
- Text không xuống dòng. Loading vô hiệu hóa button, có spinner và nhãn xử lý rõ nghĩa.

### 5.2. Form

- Label luôn ở trên; placeholder không thay label.
- Trường `required` có dấu `*` màu danger sau label.
- Validate format khi blur, bắt buộc khi submit; lỗi nằm ngay dưới input kèm icon.
- Disabled: nền gray-100, text gray-500, không có focus border.

### 5.3. Data table

- Header sticky, nền gray-100, text gray-700, weight 600.
- Row hover gray-50; row selected `rgba(247,148,29,.08)`.
- Số căn phải; text căn trái; status/action căn giữa.
- Luôn có tìm kiếm, filter, chọn số dòng/trang và phân trang.
- Status dùng badge nền nhạt + chữ đậm cùng tone.
- Action ở cột cuối dùng icon button có tooltip.

### 5.4. Modal và confirm dialog

- Small `400px`, Medium `600px`, Large `900px`.
- Có nút X, đóng bằng `Esc`; form nhanh hoặc xác nhận không cần chuyển trang.
- Confirm phá hủy có tiêu đề cụ thể, mô tả hậu quả, Cancel bên trái và Danger bên phải.

### 5.5. Trạng thái hệ thống

- Loading bảng/card: skeleton; spinner toàn trang chỉ dùng lần tải app đầu tiên.
- Empty: icon line-art + mô tả ngắn + CTA nếu có.
- Error: banner/toast danger và nút “Thử lại” nếu tải dữ liệu lỗi.
- Success: toast success tự ẩn sau 3 giây, không chặn thao tác.
- Toast ở góc phải trên, tối đa ba toast; lỗi nghiêm trọng không tự ẩn.

## 6. Icon và hình ảnh

- Chỉ dùng Lucide Icons, stroke 1.5–2px.
- Kích thước: 16px inline/button nhỏ, 20px toolbar/menu, 24px dashboard.
- Icon trạng thái dùng màu ngữ nghĩa tương ứng.
- Empty illustration là line-art đơn giản, đơn sắc hoặc điểm cam; không dùng ảnh trang trí rối.

## 7. Tương tác và accessibility

- `Ctrl/Cmd + S` lưu form, `Esc` đóng modal, `Enter` submit form đơn giản.
- Rời trang khi có thay đổi chưa lưu phải cảnh báo.
- Search/filter debounce tối thiểu 300ms trước khi gọi API tự động.
- Tải dữ liệu lớn dùng progress/background job và thông báo khi hoàn tất.
- Mọi action dùng được bằng chuột và bàn phím; focus ring primary rõ ràng 2px.
- Icon-only có accessible name; màu không là tín hiệu duy nhất; thứ bậc heading đúng.

## 8. Phân quyền giao diện

Sau login chỉ gọi `GET /users/me`, sau đó controller định tuyến theo role.

| Role | Trang mặc định | API được phép gọi |
|---|---|---|
| `ADMIN` | Quản trị thành viên | `/users/me`, `/users/search`, tạo/sửa/xóa user |
| `USER` | Không gian cá nhân | `/users/me`; không gọi API quản trị |

View không gọi API. Không render menu/action ngoài quyền và không dùng response `403` làm cơ chế điều hướng.

## 9. Checklist bàn giao

- Đúng MVC, phân quyền và component UI chung.
- Đúng token đen–cam, type scale 14px và spacing 4px.
- Có loading/empty/error/success/disabled.
- Kiểm tra keyboard, focus, contrast và confirm destructive action.
- Test cả ADMIN/USER và Network không có request sai quyền.
- Test desktop `1024/1280/1920px`; kiểm tra tối thiểu ở `360/768px` nếu màn hình hỗ trợ mobile.
- `npm run lint` và `npm run build` thành công.
