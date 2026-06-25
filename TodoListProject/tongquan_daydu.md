# TỔNG QUAN HỆ THỐNG QUẢN LÝ CÔNG VIỆC THEO AGILE SCRUM

## 1. Mục tiêu hệ thống

Hệ thống được xây dựng để quản lý công việc trong văn phòng IT theo mô hình Agile Scrum.

Hệ thống hỗ trợ:

- Quản lý tài khoản người dùng.
- Tạo và quản lý dự án.
- Thêm thành viên vào dự án.
- Phân quyền thành viên theo từng dự án.
- Quản lý backlog, sprint, task, bug và comment.
- Theo dõi lịch sử thay đổi trạng thái công việc.
- Gửi thông báo đến đúng người dùng liên quan.
- Thống kê tiến độ dự án và hiệu suất làm việc.
- Lưu audit log phục vụ quản trị và đánh giá.

---

## 2. Mô hình phân quyền tổng thể

Hệ thống có hai tầng phân quyền độc lập:

1. Vai trò cấp hệ thống.
2. Vai trò của thành viên trong từng dự án.

### 2.1. Vai trò cấp hệ thống

```java
public enum UserRole {
    ADMIN,
    MANAGER,
    EMPLOYEE
}
```

#### ADMIN

ADMIN là người quản trị toàn bộ nền tảng.

ADMIN có các quyền chính:

- Tạo, cập nhật, khóa, mở khóa và xóa mềm tài khoản.
- Gán vai trò hệ thống cho tài khoản.
- Xem toàn bộ audit log của hệ thống.
- Xem thống kê tổng thể của toàn bộ hệ thống.
- Xem thống kê theo quản lý, theo dự án và theo khoảng thời gian.
- Xem danh sách tất cả dự án để giám sát.
- Xem lịch sử hoạt động của người dùng.
- Quản lý cấu hình hệ thống.
- Can thiệp dữ liệu khi có sự cố quản trị.

ADMIN không trực tiếp tạo hoặc điều hành dự án trong quy trình nghiệp vụ thông thường.

ADMIN chỉ thực hiện quản trị, giám sát và đánh giá toàn hệ thống.

#### MANAGER

MANAGER là tài khoản quản lý dự án.

MANAGER có các quyền chính:

- Tạo dự án.
- Cập nhật thông tin dự án do mình quản lý.
- Thêm hoặc xóa thành viên trong dự án.
- Phân vai trò cho thành viên trong từng dự án.
- Tạo và quản lý sprint.
- Quản lý backlog và tiến độ dự án.
- Xem thống kê của các dự án do mình quản lý.
- Theo dõi trạng thái task, bug và hiệu suất thành viên.
- Đóng, hoàn thành hoặc lưu trữ dự án.
- Xem lịch sử hoạt động trong dự án.

Khi MANAGER tạo dự án, hệ thống tự động thêm người đó vào dự án với vai trò `OWNER`.

#### EMPLOYEE

EMPLOYEE là tài khoản nhân viên thông thường.

EMPLOYEE không tự tạo dự án.

EMPLOYEE chỉ nhìn thấy các dự án mà mình đã được MANAGER thêm vào.

Trong mỗi dự án, EMPLOYEE được thao tác theo `ProjectMemberRole` đã được phân.

Các chức năng ngoài quyền của role trong dự án chỉ được phép xem hoặc bị từ chối tùy nghiệp vụ.

---

## 3. Vai trò trong từng dự án

```java
public enum ProjectMemberRole {
    OWNER,
    PROJECT_MANAGER,
    SCRUM_MASTER,
    PRODUCT_OWNER,
    DEVELOPER,
    TESTER,
    VIEWER
}
```

### OWNER

- Là chủ sở hữu dự án.
- Thường là MANAGER đã tạo dự án.
- Có toàn quyền quản lý dự án.
- Thêm, xóa và phân quyền thành viên.
- Cập nhật, đóng hoặc lưu trữ dự án.
- Quản lý sprint, backlog và thống kê dự án.
- Không được xóa OWNER cuối cùng khỏi dự án.

### PROJECT_MANAGER

- Hỗ trợ OWNER quản lý dự án.
- Quản lý thành viên nếu được phép.
- Theo dõi tiến độ, deadline và nguồn lực.
- Xem và xuất thống kê dự án.
- Điều phối công việc tổng thể.

### SCRUM_MASTER

- Quản lý quy trình Scrum.
- Tạo, bắt đầu và kết thúc sprint.
- Theo dõi sprint goal.
- Hỗ trợ xử lý các task bị block.
- Theo dõi tiến độ sprint.
- Không tự thay đổi yêu cầu sản phẩm nếu không có quyền Product Owner.

### PRODUCT_OWNER

- Quản lý product backlog.
- Tạo và ưu tiên backlog item.
- Xác nhận yêu cầu nghiệp vụ.
- Đưa backlog item vào sprint.
- Xác nhận kết quả công việc theo yêu cầu sản phẩm.

### DEVELOPER

- Xem task được phân công.
- Nhận task hoặc được giao task.
- Cập nhật trạng thái thực hiện.
- Comment tiến độ.
- Đính kèm tài liệu hoặc kết quả.
- Chuyển task sang trạng thái chờ review hoặc chờ test.
- Sửa lỗi được TESTER báo lại.
- Không quản lý thành viên hoặc cấu hình dự án.

### TESTER

- Nhận thông báo khi task chuyển sang trạng thái cần kiểm thử.
- Kiểm thử task.
- Comment kết quả kiểm thử.
- Xác nhận task đạt yêu cầu.
- Tạo bug khi phát hiện lỗi.
- Gán bug về cho developer liên quan.
- Chuyển task hoặc bug theo luồng kiểm thử.
- Không sửa nội dung phát triển nếu không được phân công.

### VIEWER

- Chỉ xem thông tin dự án.
- Xem backlog, sprint, task, comment và thống kê được cho phép.
- Không tạo, sửa, xóa hoặc chuyển trạng thái công việc.

---

## 4. Nguyên tắc kiểm tra quyền

Quyền thực hiện một chức năng được xác định theo hai bước.

### Bước 1: Kiểm tra role hệ thống

Ví dụ:

- Chỉ ADMIN được quản lý tài khoản.
- Chỉ MANAGER được tạo dự án.
- EMPLOYEE không được tạo dự án.

### Bước 2: Kiểm tra vai trò trong dự án

Ví dụ:

- OWNER và PROJECT_MANAGER được quản lý thành viên.
- SCRUM_MASTER được quản lý sprint.
- PRODUCT_OWNER được quản lý backlog.
- DEVELOPER được cập nhật task được giao.
- TESTER được kiểm thử và tạo bug.
- VIEWER chỉ được xem.

ADMIN có thể xem toàn bộ dữ liệu để quản trị và thống kê nhưng không tham gia trực tiếp vào quy trình dự án nếu không được thêm làm thành viên.

---

## 5. Luồng tạo và tham gia dự án

### 5.1. MANAGER tạo dự án

Luồng xử lý:

1. MANAGER gửi yêu cầu tạo dự án.
2. Hệ thống kiểm tra tài khoản có `UserRole.MANAGER`.
3. Hệ thống tạo bản ghi `Project`.
4. Hệ thống tự động tạo `ProjectMember` cho MANAGER.
5. Vai trò trong dự án của người tạo là `OWNER`.
6. Hệ thống ghi audit log `CREATE_PROJECT`.
7. Dự án xuất hiện trong danh sách dự án của MANAGER.

### 5.2. MANAGER thêm thành viên

Luồng xử lý:

1. OWNER hoặc PROJECT_MANAGER chọn người dùng.
2. Hệ thống kiểm tra người dùng tồn tại và đang hoạt động.
3. Kiểm tra người dùng chưa nằm trong dự án.
4. Chọn `ProjectMemberRole`.
5. Tạo bản ghi `ProjectMember`.
6. Gửi thông báo cho người dùng được thêm.
7. Ghi lịch sử hoạt động.
8. Dự án xuất hiện trong danh sách dự án của thành viên.

### 5.3. Nhân viên truy cập dự án

Luồng xử lý:

1. EMPLOYEE đăng nhập.
2. Hệ thống lấy danh sách `ProjectMember` theo `userId`.
3. Chỉ trả các dự án mà user đang là thành viên.
4. Khi thao tác, hệ thống kiểm tra role trong dự án.
5. Thao tác không đúng quyền trả về `403 Forbidden`.

---

## 6. Luồng công việc Task

### 6.1. Trạng thái task đề xuất

```java
public enum TaskStatus {
    TODO,
    IN_PROGRESS,
    READY_FOR_REVIEW,
    READY_FOR_TEST,
    TESTING,
    REOPENED,
    DONE,
    BLOCKED,
    CANCELLED
}
```

### 6.2. Luồng cơ bản

```text
TODO
→ IN_PROGRESS
→ READY_FOR_REVIEW
→ READY_FOR_TEST
→ TESTING
→ DONE
```

Nếu kiểm thử thất bại:

```text
TESTING
→ REOPENED
→ IN_PROGRESS
→ READY_FOR_TEST
→ TESTING
→ DONE
```

Nếu công việc bị chặn:

```text
IN_PROGRESS
→ BLOCKED
→ IN_PROGRESS
```

### 6.3. Quyền thay đổi trạng thái

#### DEVELOPER

Có thể:

- `TODO → IN_PROGRESS`
- `IN_PROGRESS → READY_FOR_REVIEW`
- `IN_PROGRESS → BLOCKED`
- `REOPENED → IN_PROGRESS`
- Sau khi sửa xong: `IN_PROGRESS → READY_FOR_TEST`

#### TESTER

Có thể:

- `READY_FOR_TEST → TESTING`
- `TESTING → DONE`
- `TESTING → REOPENED`
- Tạo bug khi phát hiện lỗi.

#### SCRUM_MASTER / PROJECT_MANAGER / OWNER

Có thể:

- Điều phối trạng thái khi cần.
- Mở lại task.
- Chuyển task bị block.
- Hủy task theo nghiệp vụ.
- Không nên thay developer hoặc tester thực hiện công việc chuyên môn nếu không cần thiết.

---

## 7. Lịch sử trạng thái Task

Mỗi lần task đổi trạng thái, hệ thống phải tạo bản ghi lịch sử.

Bảng đề xuất:

```text
task_status_histories
```

Các trường chính:

- `id`
- `task_id`
- `from_status`
- `to_status`
- `changed_by_user_id`
- `changed_at`
- `comment`
- `reason`

Lịch sử này không được ghi đè hoặc xóa thông thường.

Mục đích:

- Theo dõi ai đã thay đổi trạng thái.
- Biết task bị trả lại bao nhiêu lần.
- Đánh giá thời gian xử lý của từng bước.
- Phục vụ thống kê hiệu suất.
- Phục vụ audit và bảo vệ đồ án.

---

## 8. Luồng kiểm thử và Bug

### 8.1. Task chuyển sang kiểm thử

Khi developer chuyển task sang `READY_FOR_TEST`:

1. Hệ thống lưu lịch sử trạng thái.
2. Tìm các thành viên có `ProjectMemberRole.TESTER`.
3. Gửi thông báo cho tester liên quan.
4. Task xuất hiện trong danh sách chờ test.

### 8.2. Tester kiểm thử thành công

1. Tester chuyển task sang `TESTING`.
2. Tester comment kết quả.
3. Tester chuyển task sang `DONE`.
4. Hệ thống ghi lịch sử.
5. Gửi thông báo cho developer, product owner và manager liên quan.

### 8.3. Tester phát hiện lỗi

1. Tester comment kết quả kiểm thử.
2. Tạo bug liên kết với task gốc.
3. Bug được gán cho developer phù hợp.
4. Task hoặc bug chuyển sang `REOPENED`.
5. Gửi thông báo cho developer.
6. Developer sửa lỗi.
7. Developer chuyển lại sang `READY_FOR_TEST`.
8. Tester kiểm thử lại.
9. Khi đạt yêu cầu, task và bug được đóng theo nghiệp vụ.

---

## 9. Mô hình Bug

Bug có thể được thiết kế là một loại Task:

```java
public enum TaskType {
    STORY,
    TASK,
    BUG,
    SUB_TASK
}
```

Bug cần lưu:

- Task gốc liên quan.
- Người phát hiện.
- Người được giao sửa.
- Mức độ nghiêm trọng.
- Mức độ ưu tiên.
- Môi trường phát hiện.
- Các bước tái hiện.
- Kết quả mong đợi.
- Kết quả thực tế.
- File đính kèm.
- Trạng thái xử lý.

Mức độ nghiêm trọng đề xuất:

```java
public enum BugSeverity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}
```

---

## 10. Comment và trao đổi

Comment được gắn với task hoặc bug.

Người dùng có thể:

- Comment tiến độ.
- Comment kết quả review.
- Comment kết quả kiểm thử.
- Tag thành viên liên quan.
- Trả lời comment.
- Đính kèm file.

Mỗi comment cần lưu:

- Người tạo.
- Nội dung.
- Thời gian tạo.
- Thời gian cập nhật.
- Comment cha nếu là reply.
- Danh sách người được nhắc đến.

Comment quan trọng có thể sinh thông báo.

---

## 11. Hệ thống thông báo

Thông báo phải hướng đến đúng người dùng liên quan theo sự kiện và role.

### Các sự kiện cần thông báo

- Người dùng được thêm vào dự án.
- Vai trò trong dự án bị thay đổi.
- Người dùng bị xóa khỏi dự án.
- Task được giao.
- Deadline task thay đổi.
- Task sắp đến hạn hoặc quá hạn.
- Task chuyển sang `READY_FOR_TEST`.
- Tester tạo bug.
- Bug được gán cho developer.
- Task hoặc bug bị reopen.
- Comment có nhắc tên user.
- Sprint bắt đầu.
- Sprint sắp kết thúc.
- Project bị đóng hoặc lưu trữ.

### Người nhận theo ví dụ

#### Task chuyển `READY_FOR_TEST`

Nhận thông báo:

- TESTER trong dự án.
- Developer được giao task.
- Có thể thêm SCRUM_MASTER tùy cấu hình.

#### Tester tạo bug

Nhận thông báo:

- Developer được giao sửa.
- PROJECT_MANAGER.
- SCRUM_MASTER.
- Người tạo hoặc người phụ trách task gốc.

#### Task chuyển `DONE`

Nhận thông báo:

- Developer.
- Tester.
- PRODUCT_OWNER.
- PROJECT_MANAGER hoặc OWNER.

---

## 12. Audit log và Activity log

Cần phân biệt hai loại lịch sử.

### Audit log hệ thống

Dành cho ADMIN.

Ví dụ:

- Đăng nhập.
- Đăng xuất.
- Tạo tài khoản.
- Khóa tài khoản.
- Xóa tài khoản.
- Thay đổi role hệ thống.
- Tạo hoặc xóa dự án.
- Các thao tác quản trị quan trọng.

### Activity log dự án

Dành cho thành viên dự án và MANAGER.

Ví dụ:

- Thêm thành viên.
- Thay đổi role thành viên.
- Tạo sprint.
- Tạo task.
- Giao task.
- Đổi status.
- Tạo bug.
- Comment.
- Thay đổi deadline.

Activity log phải chứa:

- `project_id`
- `entity_type`
- `entity_id`
- `action`
- `performed_by_user_id`
- `old_value`
- `new_value`
- `created_at`

---

## 13. Thống kê dành cho ADMIN

ADMIN có thể xem thống kê toàn hệ thống.

### Thống kê tổng thể

- Tổng số tài khoản.
- Số tài khoản đang hoạt động.
- Số MANAGER.
- Số EMPLOYEE.
- Tổng số dự án.
- Số dự án đang hoạt động.
- Số dự án hoàn thành.
- Số task theo trạng thái.
- Số bug theo mức độ nghiêm trọng.
- Tỷ lệ task hoàn thành đúng hạn.
- Số lần task bị reopen.
- Tần suất hoạt động của người dùng.

### Thống kê theo MANAGER

- Số dự án do quản lý tạo.
- Tỷ lệ hoàn thành dự án.
- Số task hoàn thành.
- Số task trễ hạn.
- Số bug phát sinh.
- Hiệu suất theo khoảng thời gian.
- Tiến độ các dự án của quản lý.
- Khả năng phân bổ công việc.

### Thống kê theo khoảng thời gian

Hỗ trợ lọc theo:

- Ngày.
- Tuần.
- Tháng.
- Quý.
- Năm.
- Khoảng thời gian tùy chọn.

ADMIN chủ yếu xem và đánh giá, không thao tác trực tiếp vào nghiệp vụ dự án.

---

## 14. Thống kê dành cho MANAGER

MANAGER chỉ xem thống kê của các dự án mình sở hữu hoặc quản lý.

Các thống kê chính:

- Tổng số thành viên.
- Task theo trạng thái.
- Task theo người thực hiện.
- Task đúng hạn và trễ hạn.
- Tiến độ sprint.
- Burndown chart.
- Velocity của team.
- Số bug theo severity.
- Tỷ lệ test pass/fail.
- Số task bị reopen.
- Khối lượng công việc theo thành viên.
- Thời gian trung bình hoàn thành task.
- Hiệu suất theo tuần, tháng hoặc sprint.

---

## 15. Thống kê dành cho EMPLOYEE

EMPLOYEE được xem thống kê cá nhân và dữ liệu dự án được phép.

Ví dụ:

- Task được giao.
- Task đã hoàn thành.
- Task trễ hạn.
- Bug đang xử lý.
- Thời gian xử lý trung bình.
- Hoạt động gần đây.
- Thống kê theo sprint.
- Lịch sử task cá nhân.

Không được xem thống kê nhạy cảm của người khác nếu role trong dự án không cho phép.

---

## 16. Danh sách module dự kiến

### Phase 1 — Nền tảng

- Auth.
- JWT.
- Refresh token.
- Logout.
- Change password.
- User.
- UserRole.
- Soft delete.
- Cache.
- Audit log.
- Error handling.
- Swagger.

### Phase 2 — Project

- Project.
- ProjectMember.
- ProjectMemberRole.
- Project permission.
- Danh sách dự án của user.
- Quản lý thành viên.
- Activity log dự án.

### Phase 3 — Backlog và Sprint

- ProductBacklog.
- BacklogItem.
- Sprint.
- SprintGoal.
- SprintPlanning.
- Sprint status.
- Thêm backlog item vào sprint.

### Phase 4 — Task và Bug

- Task.
- TaskType.
- TaskStatus.
- TaskPriority.
- Task assignment.
- TaskStatusHistory.
- BugSeverity.
- Bug liên kết task gốc.

### Phase 5 — Trao đổi

- Comment.
- Reply.
- Mention.
- Attachment.
- Activity timeline.

### Phase 6 — Notification

- Notification database.
- WebSocket real-time.
- Notification theo role.
- Đánh dấu đã đọc.
- Cấu hình nhận thông báo.

### Phase 7 — Dashboard và báo cáo

- Admin dashboard.
- Manager dashboard.
- Employee dashboard.
- Project statistics.
- Sprint statistics.
- Member performance.
- Bug statistics.
- Báo cáo theo thời gian.

### Phase 8 — Hoàn thiện

- Unit test.
- Integration test.
- Security test.
- Docker.
- Seed dữ liệu demo.
- Tài liệu API.
- Tài liệu nghiệp vụ.
- Chuẩn bị bảo vệ đồ án.

---

## 17. Quy tắc nghiệp vụ cốt lõi

1. ADMIN không tạo dự án trong flow nghiệp vụ thông thường.
2. Chỉ MANAGER được tạo dự án.
3. Người tạo dự án tự động trở thành OWNER.
4. EMPLOYEE chỉ xem các dự án được thêm vào.
5. Mọi thao tác trong dự án phải kiểm tra ProjectMemberRole.
6. Role không có quyền chỉnh sửa thì chỉ được xem.
7. Không được thêm một user hai lần vào cùng dự án.
8. Không được xóa OWNER cuối cùng.
9. User bị khóa hoặc xóa mềm không thể đăng nhập.
10. Task đổi trạng thái phải lưu lịch sử.
11. Task chuyển sang kiểm thử phải thông báo TESTER.
12. Tester phát hiện lỗi phải có thể tạo BUG và gán lại developer.
13. Comment, bug, đổi status và phân công phải ghi activity log.
14. ADMIN xem được audit log và thống kê toàn hệ thống.
15. MANAGER chỉ quản lý và thống kê dự án thuộc phạm vi của mình.
16. EMPLOYEE chỉ thao tác trong phạm vi dự án và role được cấp.
17. Các API không đủ quyền phải trả `403 Forbidden`.
18. API không có xác thực phải trả `401 Unauthorized`.

---

## 18. Mô hình role chính thức

### Role hệ thống

```java
public enum UserRole {
    ADMIN,
    MANAGER,
    EMPLOYEE
}
```

### Role trong dự án

```java
public enum ProjectMemberRole {
    OWNER,
    PROJECT_MANAGER,
    SCRUM_MASTER,
    PRODUCT_OWNER,
    DEVELOPER,
    TESTER,
    VIEWER
}
```

Đây là thiết kế thống nhất được sử dụng cho toàn bộ hệ thống trong các phase tiếp theo.

---

## 19. Quy tắc gán vai trò giữa hệ thống và dự án

### 19.1. Nguyên tắc chung

`UserRole` xác định phạm vi hoạt động trên toàn hệ thống.

`ProjectMemberRole` xác định quyền trong một dự án cụ thể.

Hai loại role không thay thế cho nhau.

Ví dụ:

```text
UserRole = MANAGER
Project A = OWNER
Project B = PROJECT_MANAGER
Project C = VIEWER
```

Hoặc:

```text
UserRole = EMPLOYEE
Project A = DEVELOPER
Project B = TESTER
Project C = VIEWER
```

### 19.2. Quy tắc gán role dự án

| UserRole hệ thống | ProjectMemberRole được phép |
|---|---|
| `ADMIN` | Không tham gia flow dự án thông thường |
| `MANAGER` | `OWNER`, `PROJECT_MANAGER`, `SCRUM_MASTER`, `PRODUCT_OWNER`, `DEVELOPER`, `TESTER`, `VIEWER` |
| `EMPLOYEE` | `SCRUM_MASTER`, `PRODUCT_OWNER`, `DEVELOPER`, `TESTER`, `VIEWER` |

Quy tắc chính thức:

1. `OWNER` chỉ được gán cho tài khoản có `UserRole.MANAGER`.
2. `PROJECT_MANAGER` chỉ được gán cho tài khoản có `UserRole.MANAGER`.
3. `EMPLOYEE` không được tạo dự án.
4. `EMPLOYEE` không được trở thành `OWNER`.
5. `EMPLOYEE` không được trở thành `PROJECT_MANAGER`.
6. `MANAGER` có thể tham gia dự án khác với role nghiệp vụ thấp hơn.
7. `ADMIN` không tự động là thành viên của mọi dự án.
8. `ADMIN` được xem dữ liệu phục vụ quản trị, audit và thống kê.
9. `ADMIN` không sửa nghiệp vụ dự án trong flow thông thường.
10. Khi cần xử lý sự cố đặc biệt, thao tác can thiệp của ADMIN phải được ghi audit log riêng.

---

## 20. Ma trận quyền tổng quát

Ký hiệu:

- `C`: được tạo.
- `R`: được xem.
- `U`: được cập nhật.
- `D`: được xóa hoặc xóa mềm.
- `A`: được thực hiện hành động nghiệp vụ.
- `—`: không có quyền.

### 20.1. Quyền cấp hệ thống

| Chức năng | ADMIN | MANAGER | EMPLOYEE |
|---|---:|---:|---:|
| Đăng nhập, đăng xuất | A | A | A |
| Xem hồ sơ cá nhân | R | R | R |
| Đổi mật khẩu | U | U | U |
| Tạo tài khoản | C | — | — |
| Cập nhật tài khoản | U | — | — |
| Khóa, mở khóa tài khoản | A | — | — |
| Xóa mềm tài khoản | D | — | — |
| Xem danh sách tài khoản | R | Có giới hạn | Có giới hạn |
| Xem audit log hệ thống | R | — | — |
| Xem thống kê toàn hệ thống | R | — | — |
| Xem toàn bộ dự án | R | Chỉ phạm vi quản lý/tham gia | Chỉ dự án tham gia |
| Tạo dự án | — | C | — |
| Cấu hình hệ thống | U | — | — |

MANAGER và EMPLOYEE chỉ được tìm người dùng trong phạm vi cần thiết để thêm thành viên hoặc gán công việc. Không trả dữ liệu nhạy cảm như mật khẩu, refresh token, audit nội bộ hoặc thông tin bảo mật.

### 20.2. Quyền trong dự án

| Chức năng | OWNER | PROJECT_MANAGER | SCRUM_MASTER | PRODUCT_OWNER | DEVELOPER | TESTER | VIEWER |
|---|---:|---:|---:|---:|---:|---:|---:|
| Xem dự án | R | R | R | R | R | R | R |
| Cập nhật thông tin dự án | U | U | — | — | — | — | — |
| Đóng/lưu trữ dự án | A | A | — | — | — | — | — |
| Thêm thành viên | C | C | — | — | — | — | — |
| Xóa thành viên | D | D | — | — | — | — | — |
| Thay đổi role thành viên | U | U | — | — | — | — | — |
| Xem thống kê dự án | R | R | R | R | R giới hạn | R giới hạn | R giới hạn |
| Tạo sprint | C | C | C | — | — | — | — |
| Bắt đầu/kết thúc sprint | A | A | A | — | — | — | — |
| Tạo backlog item | C | C | Có giới hạn | C | — | — | — |
| Ưu tiên backlog | U | U | — | U | — | — | — |
| Tạo task | C | C | C | C | Có giới hạn | Có giới hạn | — |
| Giao task | U | U | U | Có giới hạn | — | — | — |
| Cập nhật task được giao | A | A | A | A giới hạn | A | A kiểm thử | — |
| Chuyển task sang test | A | A | A | A | A | — | — |
| Kiểm thử task | — | — | Theo dõi | — | — | A | — |
| Tạo bug | C | C | C | C | C | C | — |
| Comment | C | C | C | C | C | C | R |
| Xem activity log | R | R | R | R | R | R | R |
| Xóa project | D | Theo chính sách | — | — | — | — | — |

### 20.3. Quy tắc “không có quyền sửa thì chỉ xem”

Người dùng đã là thành viên dự án nhưng role không có quyền chỉnh sửa một chức năng vẫn có thể xem dữ liệu, trừ các dữ liệu bị giới hạn riêng.

Ví dụ:

- `DEVELOPER` xem sprint nhưng không kết thúc sprint.
- `TESTER` xem backlog nhưng không thay đổi độ ưu tiên.
- `VIEWER` chỉ xem, không comment, không đổi trạng thái.
- User không thuộc dự án không được xem dữ liệu dự án.

---

## 21. Mô hình dữ liệu cốt lõi

### 21.1. Danh sách entity chính

```text
User
TokenSession
AuditLog
Project
ProjectMember
ProjectActivityLog
Sprint
BacklogItem
Task
TaskAssignee
TaskStatusHistory
TaskDependency
Comment
CommentMention
Attachment
Notification
NotificationRecipient
```

### 21.2. Quan hệ tổng quát

```text
User 1 --- N ProjectMember
Project 1 --- N ProjectMember

User 1 --- N Project
Project.createdByUserId -> User.id

Project 1 --- N Sprint
Project 1 --- N BacklogItem
Project 1 --- N Task
Project 1 --- N ProjectActivityLog

Sprint 1 --- N Task
BacklogItem 1 --- N Task

Task N --- N User thông qua TaskAssignee
Task 1 --- N TaskStatusHistory
Task 1 --- N Comment
Task 1 --- N Attachment
Task 1 --- N TaskDependency

Task có thể liên kết Task cha
Task có thể liên kết Bug gốc hoặc Task gốc
Comment 1 --- N Comment reply
Comment N --- N User thông qua CommentMention

User 1 --- N NotificationRecipient
Notification 1 --- N NotificationRecipient
```

### 21.3. Nguyên tắc thiết kế

1. Dữ liệu nghiệp vụ dùng UUID.
2. Enum lưu bằng `EnumType.STRING`.
3. Không dùng `EnumType.ORDINAL`.
4. Dữ liệu quan trọng dùng soft delete.
5. Lịch sử trạng thái và audit log không chỉnh sửa sau khi tạo.
6. Các bảng quan trọng phải có index theo khóa ngoại và trạng thái.
7. Các trường ngày giờ hệ thống dùng `Instant`.
8. Ngày nghiệp vụ không có múi giờ dùng `LocalDate`.
9. API trả ngày giờ theo ISO-8601 UTC.
10. Không cache trực tiếp `PageImpl`; dùng DTO phân trang ổn định.

---

## 22. Quy tắc dữ liệu Project

### 22.1. ProjectStatus

```java
public enum ProjectStatus {
    PLANNING,
    ACTIVE,
    ON_HOLD,
    COMPLETED,
    CANCELLED,
    ARCHIVED
}
```

### 22.2. Chuyển trạng thái hợp lệ

| Trạng thái hiện tại | Trạng thái tiếp theo hợp lệ |
|---|---|
| `PLANNING` | `ACTIVE`, `CANCELLED` |
| `ACTIVE` | `ON_HOLD`, `COMPLETED`, `CANCELLED` |
| `ON_HOLD` | `ACTIVE`, `CANCELLED` |
| `COMPLETED` | `ARCHIVED` |
| `CANCELLED` | `ARCHIVED` |
| `ARCHIVED` | Không chuyển tiếp trong flow thông thường |

### 22.3. Quy tắc Project

1. Mã dự án không được trùng, không phân biệt hoa thường.
2. Ngày kết thúc không được trước ngày bắt đầu.
3. Khi tạo project, MANAGER tự động thành `OWNER`.
4. Mỗi project phải luôn có ít nhất một `OWNER`.
5. Không xóa `OWNER` cuối cùng.
6. Một user chỉ có một `ProjectMember` đang hoạt động trong một project.
7. User bị khóa hoặc xóa mềm không được thêm vào project.
8. Project `ARCHIVED` chỉ cho xem.
9. Project `COMPLETED` không tạo sprint hoặc task mới.
10. Project `CANCELLED` không tiếp tục nghiệp vụ.
11. MANAGER chỉ quản lý project mình tạo hoặc được gán role quản lý.
12. EMPLOYEE chỉ thấy project mà mình đang là thành viên.

---

## 23. Quy tắc dữ liệu ProjectMember

### 23.1. Trạng thái thành viên

Có thể dùng soft delete thay vì enum trạng thái.

Thành viên hoạt động khi:

```text
deleted_at IS NULL
```

### 23.2. Quy tắc thêm thành viên

1. Chỉ `OWNER` hoặc `PROJECT_MANAGER` được thêm thành viên.
2. User được thêm phải tồn tại và đang enabled.
3. Không thêm trùng user trong cùng project.
4. Nếu membership cũ đã soft delete, có thể khôi phục record cũ.
5. Role được gán phải phù hợp với `UserRole`.
6. Khi thêm thành viên phải tạo notification.
7. Khi thêm thành viên phải tạo project activity log.

### 23.3. Quy tắc xóa thành viên

1. Không xóa `OWNER` cuối cùng.
2. Không tự xóa chính mình nếu đang là `OWNER` cuối cùng.
3. Thành viên còn task chưa hoàn thành phải được chuyển giao trước khi xóa.
4. Xóa thành viên là soft delete.
5. Sau khi bị xóa, user không còn thấy project.
6. Không xóa lịch sử task, comment hoặc activity cũ của thành viên.
7. Khi xóa thành viên phải gửi notification.
8. Khi xóa thành viên phải ghi activity log.

---

## 24. Quy tắc Sprint

### 24.1. SprintStatus

```java
public enum SprintStatus {
    PLANNING,
    ACTIVE,
    COMPLETED,
    CANCELLED
}
```

### 24.2. Chuyển trạng thái Sprint

| Từ | Sang | Role hợp lệ |
|---|---|---|
| `PLANNING` | `ACTIVE` | OWNER, PROJECT_MANAGER, SCRUM_MASTER |
| `PLANNING` | `CANCELLED` | OWNER, PROJECT_MANAGER, SCRUM_MASTER |
| `ACTIVE` | `COMPLETED` | OWNER, PROJECT_MANAGER, SCRUM_MASTER |
| `ACTIVE` | `CANCELLED` | OWNER, PROJECT_MANAGER |
| `COMPLETED` | Không chuyển | — |
| `CANCELLED` | Không chuyển | — |

### 24.3. Quy tắc Sprint

1. Một project chỉ có tối đa một sprint `ACTIVE`.
2. Sprint phải thuộc project đang `ACTIVE`.
3. Ngày kết thúc sprint không trước ngày bắt đầu.
4. Sprint `COMPLETED` không thêm hoặc xóa task.
5. Không bắt đầu sprint rỗng nếu nghiệp vụ yêu cầu có backlog item.
6. Task chưa hoàn thành khi kết thúc sprint phải được chuyển sang backlog hoặc sprint sau.
7. Mọi lần bắt đầu, hoàn thành hoặc hủy sprint phải ghi activity log.
8. Khi sprint bắt đầu hoặc sắp kết thúc phải gửi notification phù hợp.

---

## 25. Quy tắc Backlog

### 25.1. BacklogItemStatus

```java
public enum BacklogItemStatus {
    DRAFT,
    READY,
    IN_SPRINT,
    DONE,
    CANCELLED
}
```

### 25.2. Quy tắc Backlog

1. Backlog item thuộc đúng một project.
2. `PRODUCT_OWNER`, `OWNER`, `PROJECT_MANAGER` được tạo và ưu tiên backlog.
3. Backlog item chỉ được đưa vào sprint khi ở `READY`.
4. Không đưa backlog item vào sprint của project khác.
5. Backlog item `DONE` không được sửa nội dung nghiệp vụ quan trọng.
6. Mọi thay đổi độ ưu tiên phải ghi activity log.
7. Product backlog có thứ tự ưu tiên rõ ràng, không trùng vị trí sau khi sắp xếp.

---

## 26. Quy tắc Task

### 26.1. TaskType

```java
public enum TaskType {
    STORY,
    TASK,
    BUG,
    SUB_TASK
}
```

### 26.2. TaskPriority

```java
public enum TaskPriority {
    LOW,
    MEDIUM,
    HIGH,
    URGENT
}
```

### 26.3. TaskStatus

```java
public enum TaskStatus {
    TODO,
    IN_PROGRESS,
    READY_FOR_REVIEW,
    READY_FOR_TEST,
    TESTING,
    REOPENED,
    DONE,
    BLOCKED,
    CANCELLED
}
```

### 26.4. Ma trận chuyển trạng thái Task

| Từ | Sang | Vai trò/hành động hợp lệ |
|---|---|---|
| `TODO` | `IN_PROGRESS` | Người được giao, OWNER, PROJECT_MANAGER, SCRUM_MASTER |
| `TODO` | `CANCELLED` | OWNER, PROJECT_MANAGER |
| `IN_PROGRESS` | `READY_FOR_REVIEW` | Developer được giao |
| `IN_PROGRESS` | `READY_FOR_TEST` | Developer được giao nếu bỏ qua review |
| `IN_PROGRESS` | `BLOCKED` | Người được giao, SCRUM_MASTER |
| `READY_FOR_REVIEW` | `IN_PROGRESS` | Reviewer trả lại |
| `READY_FOR_REVIEW` | `READY_FOR_TEST` | Người review hợp lệ |
| `READY_FOR_TEST` | `TESTING` | Tester được giao hoặc tester trong project |
| `TESTING` | `DONE` | Tester |
| `TESTING` | `REOPENED` | Tester |
| `REOPENED` | `IN_PROGRESS` | Developer được giao |
| `BLOCKED` | `IN_PROGRESS` | Người được giao, SCRUM_MASTER |
| Bất kỳ trạng thái chưa đóng | `CANCELLED` | OWNER, PROJECT_MANAGER |
| `DONE` | `REOPENED` | OWNER, PROJECT_MANAGER, TESTER có lý do |
| `CANCELLED` | Không chuyển trong flow thường | — |

### 26.5. Quy tắc Task

1. Task phải thuộc project.
2. Task có thể thuộc sprint hoặc backlog.
3. Người được giao task phải là thành viên đang hoạt động trong project.
4. Không giao task cho user disabled hoặc đã bị xóa khỏi project.
5. Developer chỉ cập nhật task được giao hoặc task được phép nhận.
6. Tester chỉ kiểm thử task thuộc project mình tham gia.
7. Task chuyển trạng thái phải tạo `TaskStatusHistory`.
8. Lịch sử trạng thái không bị sửa hoặc xóa.
9. Task `DONE` chỉ được mở lại khi có lý do.
10. Task `CANCELLED` không tiếp tục xử lý.
11. Task quá hạn được xác định khi chưa hoàn thành và `dueDate` nhỏ hơn thời điểm hiện tại.
12. Thay đổi assignee, deadline, priority hoặc status phải ghi activity log.
13. Project `ARCHIVED`, `COMPLETED`, `CANCELLED` không cho tạo task mới.
14. Sprint `COMPLETED` không cho thêm task mới.

---

## 27. Quy tắc Review, Test và Bug

### 27.1. Luồng Developer hoàn thành công việc

```text
TODO
→ IN_PROGRESS
→ READY_FOR_REVIEW
→ READY_FOR_TEST
```

Nếu không áp dụng code review:

```text
TODO
→ IN_PROGRESS
→ READY_FOR_TEST
```

### 27.2. Luồng Tester kiểm thử thành công

```text
READY_FOR_TEST
→ TESTING
→ DONE
```

### 27.3. Luồng Tester phát hiện lỗi

```text
READY_FOR_TEST
→ TESTING
→ REOPENED
→ IN_PROGRESS
→ READY_FOR_TEST
→ TESTING
→ DONE
```

Khi phát hiện lỗi:

1. Tester nhập comment kết quả.
2. Tester tạo task type `BUG`.
3. Bug liên kết task gốc.
4. Bug lưu người phát hiện.
5. Bug gán cho developer xử lý.
6. Task hoặc bug chuyển `REOPENED`.
7. Developer nhận notification.
8. Developer sửa lỗi và chuyển lại `READY_FOR_TEST`.
9. Tester kiểm thử lại.
10. Khi đạt yêu cầu, tester đóng bug và task theo nghiệp vụ.

### 27.4. BugSeverity

```java
public enum BugSeverity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}
```

### 27.5. Dữ liệu riêng của Bug

- `sourceTaskId`
- `reportedByUserId`
- `assignedToUserId`
- `severity`
- `environment`
- `stepsToReproduce`
- `expectedResult`
- `actualResult`
- `detectedAt`

---

## 28. TaskStatusHistory

Mỗi lần đổi trạng thái phải lưu:

```text
id
task_id
from_status
to_status
changed_by_user_id
changed_at
reason
comment
```

Quy tắc:

1. Không cập nhật bản ghi lịch sử sau khi tạo.
2. Không hard delete lịch sử.
3. Status đầu tiên có thể có `from_status = null`.
4. Chuyển sang `REOPENED`, `BLOCKED` hoặc `CANCELLED` bắt buộc có lý do.
5. Lịch sử dùng để tính lead time, cycle time, thời gian test và số lần reopen.
6. API timeline phải sắp xếp theo thời gian tăng dần hoặc giảm dần nhất quán.

---

## 29. Comment, Mention và Attachment

### 29.1. Comment

Comment có thể gắn với task hoặc bug.

Quy tắc:

1. Chỉ thành viên dự án được comment.
2. `VIEWER` chỉ xem, không comment.
3. Người tạo được sửa comment của mình trong thời gian cho phép.
4. OWNER hoặc PROJECT_MANAGER có thể ẩn comment vi phạm nhưng không xóa dấu vết.
5. Comment reply lưu `parentCommentId`.
6. Comment được chỉnh sửa phải có `updatedAt`.

### 29.2. Mention

Khi nội dung có mention user:

1. User được mention phải là thành viên của project.
2. Tạo `CommentMention`.
3. Tạo notification cho user được mention.
4. Không gửi lặp notification cho cùng user trong cùng comment.

### 29.3. Attachment

1. Attachment lưu metadata, không nhúng trực tiếp file lớn vào database.
2. Chỉ cho phép loại file hợp lệ.
3. Có giới hạn dung lượng.
4. Attachment kế thừa quyền truy cập của entity cha.
5. Không cung cấp URL công khai không kiểm soát.

---

## 30. Hệ thống Notification

### 30.1. NotificationType

```java
public enum NotificationType {
    PROJECT_MEMBER_ADDED,
    PROJECT_MEMBER_REMOVED,
    PROJECT_MEMBER_ROLE_CHANGED,
    TASK_ASSIGNED,
    TASK_UNASSIGNED,
    TASK_STATUS_CHANGED,
    TASK_READY_FOR_TEST,
    TASK_REOPENED,
    TASK_OVERDUE,
    BUG_CREATED,
    BUG_ASSIGNED,
    COMMENT_MENTIONED,
    SPRINT_STARTED,
    SPRINT_ENDING,
    SPRINT_COMPLETED,
    PROJECT_STATUS_CHANGED
}
```

### 30.2. Dữ liệu Notification

```text
id
type
title
content
actor_user_id
project_id
entity_type
entity_id
created_at
```

Bảng người nhận:

```text
notification_recipients
id
notification_id
user_id
read_at
delivered_at
```

### 30.3. Quy tắc người nhận

| Sự kiện | Người nhận |
|---|---|
| Thành viên được thêm | User được thêm |
| Role thành viên thay đổi | User bị đổi role |
| Task được giao | Assignee mới |
| Task chuyển READY_FOR_TEST | Tester liên quan, assignee, có thể Scrum Master |
| Tester tạo bug | Developer được giao, Scrum Master, Project Manager |
| Task bị reopen | Assignee, người tạo task, quản lý liên quan |
| Task hoàn thành | Assignee, tester, Product Owner, quản lý liên quan |
| User được mention | User được mention |
| Sprint bắt đầu | Thành viên sprint/project |
| Sprint sắp kết thúc | Thành viên có task chưa hoàn thành, Scrum Master, quản lý |
| Task quá hạn | Assignee, Scrum Master, Project Manager |

### 30.4. Quy tắc Notification

1. Notification được lưu database trước khi gửi WebSocket.
2. WebSocket chỉ là kênh real-time, database là nguồn dữ liệu chính.
3. User chỉ đọc notification của chính mình.
4. Hỗ trợ đánh dấu một hoặc tất cả là đã đọc.
5. Tránh tạo notification trùng cho cùng sự kiện và người nhận.
6. Notification không làm rollback giao dịch chính nếu WebSocket gửi thất bại.
7. Các job quá hạn được chạy theo lịch.

---

## 31. Audit log và Project Activity Log

### 31.1. AuditLog

Dùng cho quản trị toàn hệ thống.

Các action tối thiểu:

```text
LOGIN
LOGIN_FAILED
LOGOUT
LOGOUT_ALL
CHANGE_PASSWORD
CREATE_USER
UPDATE_USER
ENABLE_USER
DISABLE_USER
DELETE_USER
CHANGE_SYSTEM_ROLE
CREATE_PROJECT
DELETE_PROJECT
ADMIN_VIEW_AUDIT
ADMIN_EXPORT_REPORT
```

### 31.2. ProjectActivityLog

Các action tối thiểu:

```text
PROJECT_CREATED
PROJECT_UPDATED
PROJECT_STATUS_CHANGED
MEMBER_ADDED
MEMBER_REMOVED
MEMBER_ROLE_CHANGED
SPRINT_CREATED
SPRINT_STARTED
SPRINT_COMPLETED
BACKLOG_CREATED
BACKLOG_PRIORITY_CHANGED
TASK_CREATED
TASK_UPDATED
TASK_ASSIGNED
TASK_STATUS_CHANGED
BUG_CREATED
COMMENT_CREATED
DEADLINE_CHANGED
```

### 31.3. Dữ liệu ActivityLog

```text
id
project_id
entity_type
entity_id
action
performed_by_user_id
old_value_json
new_value_json
created_at
```

Quy tắc:

1. Không lưu mật khẩu, JWT hoặc refresh token vào log.
2. Dữ liệu nhạy cảm phải được mask.
3. Log không bị cập nhật hoặc hard delete.
4. ADMIN xem audit toàn hệ thống.
5. Thành viên dự án chỉ xem activity trong dự án theo quyền.
6. MANAGER chỉ xem log trong phạm vi project mình quản lý.

---

## 32. Thống kê và chỉ số

### 32.1. Bộ lọc thời gian chung

Các dashboard hỗ trợ:

- Hôm nay.
- Tuần này.
- Tháng này.
- Quý này.
- Năm này.
- Khoảng ngày tùy chọn.

Thời gian bắt đầu không được sau thời gian kết thúc.

### 32.2. Thống kê ADMIN

- Tổng số tài khoản theo role.
- Tài khoản active, disabled, deleted.
- Tổng số project theo status.
- Project theo MANAGER.
- Tỷ lệ project hoàn thành.
- Task theo trạng thái toàn hệ thống.
- Task đúng hạn, trễ hạn.
- Bug theo severity.
- Tỷ lệ test pass/fail.
- Số lần task reopen.
- Số thao tác theo người dùng.
- Mức độ hoạt động theo thời gian.
- Thống kê riêng từng MANAGER.
- So sánh các project theo tiến độ.

### 32.3. Thống kê MANAGER

- Project do mình tạo hoặc quản lý.
- Thành viên theo role.
- Task theo trạng thái.
- Task theo assignee.
- Task đúng hạn, trễ hạn.
- Sprint burndown.
- Team velocity.
- Bug theo severity.
- Tỷ lệ test pass/fail.
- Số lần reopen.
- Cycle time trung bình.
- Khối lượng công việc theo thành viên.
- Tiến độ theo sprint và khoảng thời gian.

### 32.4. Thống kê EMPLOYEE

- Task được giao.
- Task đang làm.
- Task hoàn thành.
- Task trễ hạn.
- Bug đang xử lý.
- Số lần task bị reopen.
- Cycle time cá nhân.
- Hoạt động gần đây.
- Kết quả theo sprint.

### 32.5. Nguyên tắc đánh giá

1. Thống kê chỉ dùng dữ liệu trong phạm vi quyền.
2. Không dùng một chỉ số duy nhất để kết luận hiệu suất.
3. Tách số lượng task và độ phức tạp công việc.
4. Số bug phải đặt trong bối cảnh khối lượng và độ khó.
5. Dữ liệu thống kê phải truy vết được về activity/history.
6. Có thể cache kết quả thống kê theo bộ lọc thời gian.

---

## 33. Quy ước API

### 33.1. Prefix

API thống nhất dùng:

```text
/api/v1
```

Ví dụ:

```text
POST   /api/v1/auth/login
GET    /api/v1/users/me
GET    /api/v1/projects
POST   /api/v1/projects
```

Nếu project hiện tại chưa dùng prefix, cần chọn một chuẩn và áp dụng thống nhất trước khi mở rộng Phase 2.

### 33.2. URL resource

```text
GET    /api/v1/projects
POST   /api/v1/projects
GET    /api/v1/projects/{projectId}
PUT    /api/v1/projects/{projectId}
DELETE /api/v1/projects/{projectId}

GET    /api/v1/projects/{projectId}/members
POST   /api/v1/projects/{projectId}/members
PUT    /api/v1/projects/{projectId}/members/{memberId}
DELETE /api/v1/projects/{projectId}/members/{memberId}

GET    /api/v1/projects/{projectId}/sprints
POST   /api/v1/projects/{projectId}/sprints

GET    /api/v1/projects/{projectId}/tasks
POST   /api/v1/projects/{projectId}/tasks
PATCH  /api/v1/projects/{projectId}/tasks/{taskId}/status

GET    /api/v1/notifications
PATCH  /api/v1/notifications/{id}/read
PATCH  /api/v1/notifications/read-all
```

### 33.3. HTTP status

| Trường hợp | HTTP status |
|---|---:|
| Thành công đọc/cập nhật | `200` |
| Tạo mới thành công | `201` |
| Xóa thành công không trả body | `204` |
| Request không hợp lệ | `400` |
| Chưa xác thực | `401` |
| Không đủ quyền | `403` |
| Không tìm thấy | `404` |
| Dữ liệu xung đột | `409` |
| Lỗi nghiệp vụ không xử lý được | `422` nếu áp dụng |
| Lỗi hệ thống | `500` |
| Dịch vụ tạm không sẵn sàng | `503` |

### 33.4. Pagination

Request:

```text
?page=0&size=20&sort=createdAt,desc
```

Response dùng DTO riêng:

```text
content
totalElements
totalPages
number
size
numberOfElements
first
last
empty
```

### 33.5. Date và time

- `LocalDate`: `yyyy-MM-dd`
- `Instant`: ISO-8601 UTC, ví dụ `2026-06-23T10:30:00Z`
- Backend lưu UTC.
- Frontend chuyển về múi giờ người dùng.

---

## 34. Quy ước mã lỗi theo module

Đề xuất nhóm mã:

| Nhóm | Khoảng mã |
|---|---|
| Validation | `400xxx` |
| Authentication | `401xxx` |
| Authorization | `403xxx` |
| User | `4041xx`, `4091xx` |
| Project | `4042xx`, `4092xx` |
| Project Member | `4043xx`, `4093xx` |
| Sprint | `4044xx`, `4094xx` |
| Task | `4045xx`, `4095xx` |
| Comment | `4046xx` |
| Notification | `4047xx` |
| Database/System | `500xxx`, `503xxx` |

Các lỗi nghiệp vụ tối thiểu cần có:

```text
PROJECT_NOT_FOUND
PROJECT_CODE_ALREADY_EXISTS
PROJECT_ACCESS_DENIED
PROJECT_NOT_EDITABLE
PROJECT_ARCHIVED
PROJECT_DATE_INVALID

PROJECT_MEMBER_NOT_FOUND
PROJECT_MEMBER_ALREADY_EXISTS
PROJECT_LAST_OWNER_CANNOT_BE_REMOVED
PROJECT_MEMBER_ROLE_NOT_ALLOWED
PROJECT_MEMBER_HAS_OPEN_TASKS

SPRINT_NOT_FOUND
SPRINT_ALREADY_ACTIVE
SPRINT_NOT_EDITABLE
SPRINT_DATE_INVALID

TASK_NOT_FOUND
TASK_ASSIGNEE_NOT_IN_PROJECT
TASK_STATUS_TRANSITION_INVALID
TASK_STATUS_REASON_REQUIRED
TASK_NOT_EDITABLE
TASK_ALREADY_COMPLETED

NOTIFICATION_NOT_FOUND
NOTIFICATION_ACCESS_DENIED
```

---

## 35. Quy tắc bảo mật

1. Mọi API ngoài public endpoint phải yêu cầu access token.
2. Refresh token không được dùng gọi API nghiệp vụ.
3. JWT phải phân biệt `ACCESS` và `REFRESH`.
4. Không tin role hoặc userId từ request body.
5. User hiện tại lấy từ SecurityContext/JWT đã xác thực.
6. Mọi project API phải kiểm tra membership và role.
7. Chỉ `ADMIN` được quản lý tài khoản hệ thống.
8. Chỉ `MANAGER` được tạo project.
9. Query phải lọc dữ liệu theo phạm vi user.
10. Không trả password, token hoặc dữ liệu bảo mật.
11. File upload phải kiểm tra loại và kích thước.
12. Chống IDOR bằng kiểm tra quyền trên từng `projectId`, `taskId`.
13. Các thao tác quan trọng phải ghi audit/activity.
14. Rate limit login và các API nhạy cảm.
15. Error response không làm lộ stack trace hoặc chi tiết database.

---

## 36. Quy tắc cache

### 36.1. Dữ liệu có thể cache

- Current user.
- User detail.
- User search.
- Project detail.
- Project list theo user.
- Project member list.
- Danh sách enum.
- Dashboard đã tổng hợp theo bộ lọc.

### 36.2. Dữ liệu không nên cache lâu

- Task đang thay đổi liên tục.
- Notification chưa đọc.
- Permission kiểm tra theo project.
- Task status history mới nhất.

### 36.3. Evict cache

Cần xóa cache khi:

- Tạo/cập nhật/xóa user.
- Thay role hệ thống.
- Tạo/cập nhật/xóa project.
- Thêm/xóa/đổi role project member.
- Thay đổi trạng thái project.
- Cập nhật dữ liệu dashboard liên quan.

Cache không được là nguồn dữ liệu duy nhất.

---

## 37. Definition of Done

Một chức năng chỉ được xem là hoàn thành khi đáp ứng:

1. Nghiệp vụ đã được mô tả và thống nhất.
2. Entity/migration đã hoàn thiện.
3. Request DTO có validation.
4. Response DTO không lộ dữ liệu nội bộ.
5. Service có transaction phù hợp.
6. Authorization được kiểm tra.
7. Audit/activity được ghi nếu cần.
8. Notification được tạo nếu cần.
9. Cache được cập nhật hoặc evict đúng.
10. Error code đúng HTTP status.
11. Swagger mô tả đầy đủ.
12. Có unit test cho business rule quan trọng.
13. Có integration test cho API chính.
14. Test 401, 403, 404, 409 và validation.
15. Build thành công.
16. Flyway chạy thành công trên database sạch.
17. Không còn lỗi 500 sai loại.
18. Code tuân theo convention của dự án.

---

## 38. Lộ trình triển khai chính thức

### Phase 1 — Nền tảng

Trạng thái: gần hoàn tất.

Bao gồm:

- Authentication.
- JWT access/refresh.
- Token session.
- Logout/logout all.
- Change password.
- User CRUD.
- System role.
- Soft delete.
- Audit log cơ bản.
- Redis cache.
- Error handling.
- Swagger.
- Security 401/403.

### Phase 2 — Project và ProjectMember

Thứ tự:

1. Chốt `UserRole`: `ADMIN`, `MANAGER`, `EMPLOYEE`.
2. Tạo `ProjectStatus`.
3. Tạo `ProjectMemberRole`.
4. Tạo migration `projects`.
5. Tạo migration `project_members`.
6. Tạo entity và repository.
7. Tạo DTO và mapper.
8. API MANAGER tạo project.
9. Tự động thêm MANAGER thành OWNER.
10. API danh sách project theo user.
11. API chi tiết project.
12. API cập nhật trạng thái project.
13. API thêm/xóa/đổi role thành viên.
14. Permission helper theo project.
15. Project activity log.
16. Notification thành viên.
17. Cache.
18. Test.

### Phase 3 — Backlog và Sprint

1. Product backlog.
2. Backlog item.
3. Ưu tiên backlog.
4. Sprint.
5. Một sprint active/project.
6. Sprint planning.
7. Start/complete/cancel sprint.
8. Sprint activity và notification.
9. Test.

### Phase 4 — Task, History và Bug

1. Task.
2. Assignee.
3. Task status transition.
4. Task status history.
5. Review flow.
6. Test flow.
7. Bug flow.
8. Dependency/sub-task.
9. Deadline.
10. Test.

### Phase 5 — Comment và Attachment

1. Comment.
2. Reply.
3. Mention.
4. Attachment.
5. Activity timeline.
6. Notification.
7. Test.

### Phase 6 — Notification real-time

1. Notification database.
2. Notification recipient.
3. Unread count.
4. Mark read/read all.
5. WebSocket.
6. Scheduled overdue notification.
7. Test.

### Phase 7 — Dashboard và thống kê

1. Admin dashboard.
2. Manager dashboard.
3. Employee dashboard.
4. Project statistics.
5. Sprint burndown.
6. Velocity.
7. Bug statistics.
8. Thống kê theo thời gian.
9. Export báo cáo nếu cần.
10. Test.

### Phase 8 — Hoàn thiện

1. Unit test.
2. Integration test.
3. Security test.
4. Docker.
5. Seed demo.
6. API documentation.
7. Business documentation.
8. Deployment.
9. Demo data.
10. Chuẩn bị bảo vệ đồ án.

---

## 39. Các quyết định kiến trúc đã chốt

1. Role hệ thống gồm `ADMIN`, `MANAGER`, `EMPLOYEE`.
2. Role dự án dùng `ProjectMemberRole`.
3. ADMIN không tạo project trong flow thông thường.
4. Chỉ MANAGER tạo project.
5. Người tạo project tự động là OWNER.
6. EMPLOYEE chỉ thấy project được thêm vào.
7. OWNER và PROJECT_MANAGER chỉ gán cho tài khoản MANAGER.
8. Quyền trong project kiểm tra bằng membership và role.
9. Task đổi status phải lưu history.
10. Task sẵn sàng test phải thông báo tester.
11. Tester được tạo bug và trả lại developer.
12. Database notification là nguồn chính; WebSocket là kênh real-time.
13. Audit log và project activity log là hai loại riêng.
14. API phân trang dùng response DTO riêng.
15. Dữ liệu nghiệp vụ dùng soft delete khi phù hợp.
16. Enum lưu dạng string.
17. Hệ thống dùng UTC cho timestamp.
18. Thống kê luôn giới hạn theo phạm vi quyền.

---

## 40. Phạm vi MVP bắt buộc

Để đồ án chạy được end-to-end, MVP tối thiểu gồm:

1. ADMIN quản lý tài khoản.
2. MANAGER tạo project.
3. MANAGER thêm thành viên và phân role.
4. EMPLOYEE xem project được thêm.
5. Tạo sprint.
6. Tạo backlog/task.
7. Giao task.
8. Developer cập nhật tiến độ.
9. Task chuyển sang test.
10. Tester kiểm thử.
11. Tester tạo bug và trả developer.
12. Lưu task status history.
13. Comment.
14. Notification database và WebSocket cơ bản.
15. Project activity log.
16. Admin dashboard cơ bản.
17. Manager project dashboard cơ bản.
18. Phân quyền 401/403 đầy đủ.
19. Swagger.
20. Seed dữ liệu demo.

Các chức năng nâng cao như export Excel/PDF, email notification, biểu đồ nâng cao, tùy chỉnh workflow và nhiều assignee có thể thực hiện sau MVP.

