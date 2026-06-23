# Quy ước kiến trúc và phát triển Frontend

Tài liệu này là quy chuẩn chung của dự án. Mục tiêu là giúp thành viên mới hiểu nhanh cấu trúc hệ thống, giữ mã nguồn dễ bảo trì và bảo đảm giao diện đồng bộ.

> **Nguyên tắc bắt buộc:** Không tạo Button, Label, Input, Select, Checkbox, Radio, Textarea, Modal hoặc các thành phần giao diện cơ bản riêng trong từng màn hình. Mọi màn hình phải sử dụng component dùng chung trong `src/components/ui`.

## 1. Kiến trúc MVC

Dự án áp dụng MVC theo từng chức năng (feature-based MVC):

- **Model:** Định nghĩa kiểu dữ liệu, trạng thái, quy tắc nghiệp vụ và cách chuyển đổi dữ liệu.
- **View:** Chỉ hiển thị giao diện và phát ra sự kiện từ người dùng.
- **Controller:** Điều phối View, Model, gọi service, xử lý sự kiện và trạng thái của màn hình.
- **Service:** Giao tiếp API hoặc nguồn dữ liệu bên ngoài. Service không chứa mã giao diện.

Luồng dữ liệu chuẩn:

```text
Người dùng -> View -> Controller -> Service -> API
                  <- Model/Data <-
```

### Trách nhiệm từng lớp

#### Model

- Chứa `type`, `interface`, schema kiểm tra dữ liệu và giá trị mặc định.
- Chứa hàm chuyển đổi DTO khi cần.
- Không gọi API và không render giao diện.

#### View

- Nhận dữ liệu và callback qua props.
- Không gọi API trực tiếp.
- Không chứa nghiệp vụ phức tạp.
- Chỉ dùng component UI chung từ `components/ui`.

#### Controller

- Quản lý trạng thái và vòng đời của màn hình.
- Gọi service, xử lý loading, lỗi và thông báo.
- Chuyển dữ liệu/callback xuống View.
- Không chứa JSX giao diện lớn; phần hiển thị phải tách sang View.

#### Service

- Mỗi hàm thực hiện một mục đích API rõ ràng.
- Chuẩn hóa request, response và lỗi.
- Không hiển thị toast, modal hoặc điều hướng trang.

## 2. Cấu trúc thư mục đề xuất

```text
src/
├── app/                         # Khởi tạo ứng dụng
│   ├── routes/                  # Cấu hình route và bảo vệ route
│   ├── providers/               # Theme, auth, query, i18n...
│   └── App.tsx
├── assets/                      # Ảnh, font và tài nguyên tĩnh
├── components/
│   ├── ui/                      # Component giao diện nền tảng dùng chung
│   │   ├── Button/
│   │   │   ├── Button.tsx
│   │   │   ├── Button.types.ts
│   │   │   ├── Button.test.tsx
│   │   │   └── index.ts
│   │   ├── Input/
│   │   ├── FormField/           # Label + control + lỗi + mô tả
│   │   ├── Select/
│   │   ├── Checkbox/
│   │   ├── Radio/
│   │   ├── Textarea/
│   │   ├── Modal/
│   │   └── index.ts
│   └── common/                  # Component ghép dùng ở nhiều chức năng
│       ├── DataTable/
│       ├── EmptyState/
│       ├── ErrorState/
│       └── PageHeader/
├── features/                    # Nghiệp vụ, chia theo chức năng
│   └── user/
│       ├── models/
│       │   └── user.model.ts
│       ├── views/
│       │   ├── UserListView.tsx
│       │   └── UserFormView.tsx
│       ├── controllers/
│       │   ├── UserListController.tsx
│       │   └── UserFormController.tsx
│       ├── services/
│       │   └── user.service.ts
│       ├── components/          # Chỉ dùng nội bộ feature user
│       ├── hooks/               # Hook chỉ dùng nội bộ feature
│       ├── constants/
│       ├── utils/
│       └── index.ts             # Public API của feature
├── layouts/                     # Bố cục trang
├── services/                    # API client và service toàn hệ thống
│   ├── apiClient.ts
│   └── endpoints.ts