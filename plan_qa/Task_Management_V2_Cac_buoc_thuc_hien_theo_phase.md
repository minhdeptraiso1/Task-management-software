# Task Management V2 - Các bước thực hiện theo từng phase

> Nguồn: `Task_Management_V2_Ke_hoach_trien_khai_chi_tiet.docx`  
> Mục đích: chuyển kế hoạch sản phẩm thành checklist thực thi theo đúng thứ tự S0 đến S7.  
> Nguyên tắc: chỉ chuyển phase khi cổng nghiệm thu của phase hiện tại đã đạt và có bằng chứng kiểm tra.

## 1. Phạm vi và nguyên tắc chung

### 1.1. Phạm vi V2

- Nâng cấp từ hệ thống Agile/Scrum nội bộ hiện có thành nền tảng SaaS nhiều workspace.
- Bảo toàn dữ liệu, lịch sử và nghiệp vụ V1 đang hoạt động.
- Bổ sung tenant isolation, public authentication, gói dịch vụ, quota, thanh toán, quản trị SaaS và vận hành production.
- Giữ Google Meet ở dạng nhập link thủ công trong V2.0.
- AI chỉ tạo nội dung và Action Item dạng đề xuất; chỉ tạo Task thật sau khi người dùng xác nhận và backend kiểm tra lại quyền.

### 1.2. Thứ tự triển khai bắt buộc

```text
S0 Baseline
  -> S1 Tenant và Workspace
  -> S2 Public Authentication
  -> S3 Plan, Entitlement và Usage
  -> S4 Billing và Payment
  -> S5 Platform Administration
  -> S6 Product Packaging và AI Action Items
  -> S7 Production Readiness và Release
```

Không làm đảo thứ tự các nền tảng quan trọng:

- Không mở đăng ký công khai trước khi tenant isolation của S1 đạt.
- Không bán gói trước khi entitlement và quota của S3 hoạt động chính xác.
- Không cấp quyền sử dụng từ redirect thanh toán; chỉ cấp sau khi backend xác minh payment.
- Không đưa production nếu migration, restore, cross-tenant test và payment idempotency chưa đạt.

### 1.3. Quy tắc thực hiện mỗi ticket

1. Tìm class, method, repository, query, component và test tương tự đang có.
2. Tái sử dụng helper và cấu trúc hiện tại; không tạo thêm lớp hoặc API trùng chức năng.
3. Viết migration mới, không sửa migration đã chạy.
4. Làm theo thứ tự: migration -> entity/DTO/mapper/repository -> service/validator/access -> controller -> integration test -> frontend.
5. Dùng transaction cho mutation nhiều entity; không giữ transaction DB trong lúc gọi payment, email hoặc AI provider.
6. Ghi outbox trong transaction nghiệp vụ và gửi sau commit.
7. Kiểm tra tenant, role, trạng thái workspace, entitlement và quota ở backend service.
8. Bổ sung index theo query thật; kiểm tra N+1 và query plan khi có rủi ro hiệu năng.
9. Cache phải có tenant namespace, TTL/version và invalidation phù hợp.
10. Cập nhật OpenAPI, test, tài liệu và bằng chứng nghiệm thu cùng ticket.

---

## 2. Phase S0 - Kiểm chứng baseline và chuẩn bị

### 2.1. Mục tiêu

Biết chính xác hệ thống hiện có gì, chạy được đến đâu và có thể khôi phục hay không trước khi thêm tenant.

### 2.2. Phụ thuộc

- Không có phase kỹ thuật trước đó.
- Cần quyền đọc mã nguồn, chạy FE/BE, truy cập môi trường phát triển, DB và thư mục upload.

### 2.3. Các bước thực hiện

- [ ] **S0.1 - Chốt baseline kỹ thuật**
  - [ ] Build backend và frontend từ môi trường sạch.
  - [ ] Ghi phiên bản Java/Spring, Node, framework FE, PostgreSQL, Redis và các dependency quan trọng.
  - [ ] Kiểm tra migration hiện có và phiên bản schema cuối.
  - [ ] Kiểm tra runtime config, profile, Docker, OpenAPI và CI thực tế.
  - [ ] Tạo commit hoặc tag baseline có thể quay lại.

- [ ] **S0.2 - Lập inventory toàn hệ thống**
  - [ ] Liệt kê module, API, bảng, repository, service và màn hình FE.
  - [ ] Liệt kê cache key, WebSocket topic, scheduler, worker, file storage và AI retrieval.
  - [ ] Liệt kê unit test, integration test, security test và E2E hiện có.
  - [ ] Đánh dấu từng hạng mục: `DONE_VERIFIED`, `DONE_UNVERIFIED`, `PARTIAL` hoặc `NOT_STARTED`.

- [ ] **S0.3 - Chạy smoke test luồng V1**
  - [ ] Đăng nhập và quản lý phiên.
  - [ ] Tạo Project và thêm thành viên nội bộ.
  - [ ] Tạo Backlog, Sprint và Task.
  - [ ] Import Task từ Excel.
  - [ ] Kéo Task trên Kanban và kiểm tra đồng thời.
  - [ ] Comment, Time Log, Attachment và Bug.
  - [ ] Xuất báo cáo Excel/PDF và kiểm tra Analytics.
  - [ ] Đóng/hủy Sprint và kiểm tra lịch sử dữ liệu.
  - [ ] Kiểm tra AI Meeting Assistant và link Google Meet thủ công nếu module đang bật.

- [ ] **S0.4 - Rà quyền và đường truy cập dữ liệu**
  - [ ] Rà toàn bộ permission helper và nguồn lấy role.
  - [ ] Tìm hành vi system `ADMIN`/`MANAGER` đang được dùng như quyền doanh nghiệp toàn cục.
  - [ ] Rà native query, export, search, attachment download và soft delete.
  - [ ] Rà request interceptor, route guard và component ẩn/hiện quyền ở FE.
  - [ ] Ghi thành backlog các vị trí phải tenant-scope trong S1.

- [ ] **S0.5 - Chốt lỗi ảnh hưởng tính toàn vẹn dữ liệu**
  - [ ] Xác minh import có atomic hay tạo dữ liệu dở dang.
  - [ ] Xác minh lịch sử Task vào/rời Sprint.
  - [ ] Xác minh concurrent Kanban không sai position.
  - [ ] Xác minh concurrent Time Log không mất cập nhật hoặc vượt giới hạn.
  - [ ] Viết test có khả năng tái hiện các lỗi thật đã tìm thấy.

- [ ] **S0.6 - Sao lưu và diễn tập khôi phục**
  - [ ] Backup database và file upload theo cùng một mốc.
  - [ ] Restore vào staging cô lập.
  - [ ] Đối chiếu row count, quan hệ dữ liệu và số file.
  - [ ] Chuẩn bị seed/demo đã loại thông tin nhạy cảm.
  - [ ] Chuẩn bị ít nhất hai nhóm dữ liệu A/B để dùng cho test cross-tenant ở S1.

### 2.4. Đầu ra bắt buộc

- [ ] `baseline-inventory` thể hiện trạng thái thật của từng module.
- [ ] Danh sách chênh lệch giữa tài liệu và mã nguồn.
- [ ] Schema hiện hành và row count trước migration.
- [ ] Bộ smoke test V1.
- [ ] Backup/restore report.
- [ ] Baseline commit hoặc release tag.

### 2.5. Cổng nghiệm thu S0

- [ ] FE/BE build tái hiện được.
- [ ] Luồng chính không còn lỗi làm mất dữ liệu.
- [ ] Test quyền nền V1 chạy được.
- [ ] Restore DB và file thành công, có số liệu đối chiếu.
- [ ] Mọi module được công bố là đã có đều đã được kiểm chứng thực tế.

---

## 3. Phase S1 - Workspace, tenant isolation và thành viên

### 3.1. Mục tiêu

Mọi dữ liệu và mọi đường truy cập của module đang bật phải thuộc đúng workspace. User ở nhiều workspace không được nhìn thấy hoặc tác động nhầm dữ liệu giữa các tenant.

### 3.2. Phụ thuộc

- S0 đã đạt.
- Có backup, restore rehearsal, inventory và dữ liệu A/B.

### 3.3. Bước 1 - Thiết kế workspace và membership

- [ ] **S1.1 - Tạo schema nền tenant**
  - [ ] Thêm `workspaces` với name, slug, status, timezone, owner, version và timestamp.
  - [ ] Thêm `workspace_members` với role `ADMIN`, `MEMBER`, `VIEWER` và status.
  - [ ] Bảo đảm mỗi workspace đang hoạt động có đúng một owner ACTIVE.
  - [ ] Tạo workspace và owner membership trong cùng transaction.
  - [ ] Viết `WorkspaceAccessService` và DTO quyền hiệu lực.

- [ ] **S1.2 - Validate việc tạo workspace**
  - [ ] Giới hạn số workspace được tạo theo tài khoản để chống lạm dụng.
  - [ ] Validate tên từ 2 đến 100 ký tự.
  - [ ] Chuẩn hóa và kiểm tra unique slug.
  - [ ] Validate timezone IANA.
  - [ ] Trả error code rõ khi slug hoặc trạng thái xung đột.

### 3.4. Bước 2 - Chuyển dữ liệu V1 sang tenant

- [ ] **S1.3 - Expand schema**
  - [ ] Thêm `workspace_id` nullable vào Project và các bảng nghiệp vụ cần scope.
  - [ ] Tạo index phục vụ truy vấn theo workspace.
  - [ ] Chưa bật `NOT NULL` cho đến khi backfill và đối soát xong.

- [ ] **S1.4 - Backfill dữ liệu**
  - [ ] Tạo legacy workspace và xác định owner từ dữ liệu thật.
  - [ ] Backfill Project trước, sau đó Sprint, Backlog, Task, Comment, Time Log, Bug, Attachment và Meeting theo quan hệ cha.
  - [ ] Backfill Notification, Activity và Job từ resource cha.
  - [ ] Liệt kê dữ liệu mồ côi để xử lý; không gán workspace tùy ý.
  - [ ] Bảo toàn ID, project role và lịch sử.

- [ ] **S1.5 - Contract schema**
  - [ ] Đối chiếu row count, FK, owner và project role.
  - [ ] Thêm `NOT NULL` và composite FK sau khi dữ liệu sạch.
  - [ ] Tắt đường truy cập legacy không kiểm tra tenant.

### 3.5. Bước 3 - Tenant-scope toàn bộ backend

- [ ] Sửa `ProjectAccessService` để xác minh cả workspace, project membership và project role.
- [ ] Bỏ hành vi ADMIN doanh nghiệp có thể xem toàn bộ Project.
- [ ] Scope repository/native query ngay từ truy vấn, không lấy toàn bộ rồi lọc ở FE.
- [ ] Kiểm tra ID con thuộc đúng cha và cùng workspace.
- [ ] Namespace Redis theo `workspaceId`, resource và phạm vi quyền.
- [ ] Namespace file key theo workspace; download luôn kiểm tra quyền hoặc dùng URL ký ngắn hạn.
- [ ] Scope Search, Dashboard, Report, Export và Analytics trước pagination.
- [ ] Scope WebSocket ở CONNECT/SUBSCRIBE và ngắt quyền sau khi membership bị thu hồi.
- [ ] Gắn `workspaceId` và `actorId` vào worker/outbox; kiểm tra lại quyền khi job chạy.
- [ ] Scope scheduler và AI retrieval theo workspace/project.
- [ ] Context tenant chỉ tồn tại trong request/transaction và luôn được clear sau xử lý.

### 3.6. Bước 4 - Workspace lifecycle

- [ ] `ACTIVE`: dùng theo quyền và gói.
- [ ] `ARCHIVED`: chỉ đọc; không tự động dừng billing.
- [ ] `SUSPENDED`: chặn nghiệp vụ, job, AI và realtime; owner chỉ vào vùng khắc phục/billing được phép.
- [ ] `PENDING_DELETION`: owner yêu cầu với xác thực lại; có thời gian chờ và có thể hủy.
- [ ] `DELETED`: không truy cập; purge theo policy và retention.
- [ ] Transfer owner phải lock workspace, target là member ACTIVE và cập nhật trong một transaction.
- [ ] Không cho owner cuối tự leave, bị remove hoặc disable trước khi xử lý quyền sở hữu.

### 3.7. Bước 5 - Invitation và member management

- [ ] Chỉ Owner/Admin được mời; Admin chỉ mời `MEMBER` hoặc `VIEWER`.
- [ ] Chuẩn hóa email; kiểm tra member hiện có, invitation pending và quota.
- [ ] Sinh token đủ mạnh, chỉ lưu hash; mặc định hết hạn sau 7 ngày nếu policy được chốt.
- [ ] Resend tạo token mới và revoke token cũ.
- [ ] Gửi email bằng outbox; lỗi email không làm rollback nghiệp vụ đã commit.
- [ ] Invitation pending chưa chiếm seat; lúc accept phải reserve/lock quota nguyên tử.
- [ ] Accept yêu cầu user đã đăng nhập, verified và email khớp.
- [ ] Accept tạo/reactivate membership, ghi activity và đánh dấu invitation `ACCEPTED` trong cùng transaction.
- [ ] Accept lặp trả membership cũ; không tăng usage lần nữa.
- [ ] Token hết hạn, revoke, replay bởi người khác hoặc sai email không được cấp quyền.
- [ ] Reject/withdraw đổi trạng thái, không xóa lịch sử audit cần thiết.
- [ ] Remove member thu hồi project membership, realtime, cache và job access ngay.
- [ ] Giữ nguyên tác giả, Comment, Time Log và lịch sử Task.
- [ ] Task đang gán cho member bị xóa phải unassign hoặc reassign theo lựa chọn có kiểm tra quyền.

### 3.8. Frontend S1

- [ ] Tạo trang danh sách và tạo workspace.
- [ ] Tạo `WorkspaceSelector`.
- [ ] Đưa `workspaceId` vào route và query key.
- [ ] Khi đổi workspace: hủy request cũ, clear dữ liệu nhạy cảm, unsubscribe realtime và tải lại permission.
- [ ] Tạo Workspace Settings và hiển thị đúng trạng thái lifecycle.
- [ ] Tạo màn hình Members, Invitations, Resend, Revoke, Remove và Transfer Ownership.
- [ ] Hiển thị loading, empty, error, version conflict và quota conflict.
- [ ] Không dùng FE guard thay cho kiểm tra quyền backend.

### 3.9. API và lỗi S1

- [ ] Ưu tiên prefix `/api/v2` hoặc giữ API cũ có compatibility rõ ràng.
- [ ] Trả `400` cho dữ liệu sai, `401` chưa xác thực, `403` thiếu quyền, `404` không tồn tại/không được biết, `409` cho state/version/quota conflict và `429` cho rate limit.
- [ ] Bổ sung `errorCode` và `correlationId` vào envelope hiện có.
- [ ] Không tiết lộ tên, count hoặc metadata của workspace khác.

### 3.10. Cổng nghiệm thu S1

- [ ] User thuộc A/B mở hai tab không trộn API, cache, realtime hoặc AI context.
- [ ] Không đọc/sửa/tải tài nguyên B qua URL, export, file hoặc query của A.
- [ ] Hai request accept tranh seat cuối chỉ một request thành công.
- [ ] Xóa member A không làm mất quyền của user tại B.
- [ ] Quyền bị thu hồi có hiệu lực ngay nhưng lịch sử Task vẫn còn.
- [ ] Migration bảo toàn row count, quan hệ cha-con, owner và project role.

---

## 4. Phase S2 - Public Authentication và email lifecycle

### 4.1. Mục tiêu

Cho phép user tự đăng ký, xác minh email, đăng nhập, khôi phục mật khẩu và nhận lời mời workspace một cách an toàn.

### 4.2. Phụ thuộc

- Tenant foundation S1 đã đạt.
- Workspace invitation và membership rules đã ổn định.

### 4.3. Backend Authentication

- [ ] **S2.1 - Register**
  - [ ] Nhận email, password và hồ sơ tối thiểu.
  - [ ] Chuẩn hóa email và validate password.
  - [ ] Không nhận platform role hoặc workspace role từ request public.

- [ ] **S2.2 - Tạo tài khoản chờ xác minh**
  - [ ] Tạo user `PENDING_VERIFICATION`.
  - [ ] Tạo verification token nhưng DB chỉ lưu hash.
  - [ ] Ghi outbox email trong cùng transaction.
  - [ ] Trả phản hồi chung để hạn chế dò email.

- [ ] **S2.3 - Verify và resend**
  - [ ] Token single-use, có expiry và revoked state.
  - [ ] Mặc định expiry 24 giờ nếu policy được duyệt.
  - [ ] Resend có rate limit và revoke token cũ.
  - [ ] Sau verify cập nhật `email_verified_at`.

- [ ] **S2.4 - Onboarding sau xác minh**
  - [ ] User chưa có workspace được chọn tạo mới hoặc nhận lời mời.
  - [ ] User có nhiều workspace chỉ thấy danh sách backend cho phép.
  - [ ] User chưa verified không tạo workspace, accept invitation hoặc dùng nghiệp vụ.

### 4.4. Mật khẩu và phiên

- [ ] Forgot password luôn trả thông báo chung.
- [ ] Reset token chỉ lưu hash, single-use và mặc định hết hạn 30 phút nếu policy được duyệt.
- [ ] Reset password phải revoke mọi refresh session.
- [ ] Giữ refresh rotation và reuse detection đã có.
- [ ] Kiểm tra `auth_version` và user status để token cũ bị vô hiệu sau reset/disable.
- [ ] Logout revoke phiên hiện tại; logout-all revoke toàn bộ phiên.
- [ ] Đổi password trong ứng dụng yêu cầu password cũ hoặc re-authentication.
- [ ] Profile update không được nhận role.
- [ ] Nếu refresh token dùng cookie: `HttpOnly`, `Secure`, chính sách `SameSite`, CSRF và CORS phải khớp FE.
- [ ] Không ghi JWT, reset token hoặc link bí mật vào log.

### 4.5. Email lifecycle

- [ ] Tạo `EmailService` interface và adapter theo provider cấu hình.
- [ ] Template email hỗ trợ locale và dùng public base URL allowlist.
- [ ] Không nhận redirect URL tùy ý từ client.
- [ ] Outbox có attempts, `next_retry_at`, provider message ID và trạng thái gửi.
- [ ] Retry theo backoff; có hàng lỗi để kiểm tra và chạy lại an toàn.
- [ ] Có dedup key theo loại thông báo và sự kiện nghiệp vụ.
- [ ] Không lưu token/link thô trong audit.
- [ ] Nếu payload worker cần token thô để dựng link thì phải mã hóa, hạn chế quyền và xóa sau khi gửi/hết hạn.

### 4.6. Frontend S2

- [ ] Register.
- [ ] Login.
- [ ] Verify Email và Resend Verification.
- [ ] Forgot Password và Reset Password.
- [ ] Profile và danh sách Sessions.
- [ ] Accept Invitation cho cả user mới và user đã có tài khoản.
- [ ] Hiển thị loading/error/success và hướng dẫn khi email đến chậm.
- [ ] Xử lý link hết hạn, link cũ, token replay, sai email, user disabled và session bị revoke.

### 4.7. Cổng nghiệm thu S2

- [ ] Register -> email -> verify -> login -> onboarding chạy E2E.
- [ ] Forgot/reset password chạy E2E và token cũ không dùng lại được.
- [ ] Email retry không tạo thêm user hoặc member.
- [ ] Link đúng môi trường và domain gửi được cấu hình đúng.
- [ ] Không có user enumeration hoặc secret trong log.

---

## 5. Phase S3 - Plan, entitlement, trial và quota

### 5.1. Mục tiêu

Mỗi workspace có gói và quyền sử dụng được backend quyết định; mọi quota ghi dữ liệu phải được kiểm tra nguyên tử.

### 5.2. Phụ thuộc

- S1 tenant isolation đạt.
- S2 onboarding và email lifecycle đạt.
- Giá, quota và policy chưa được phê duyệt phải nằm trong config/sandbox, không hard-code như cam kết thương mại.

### 5.3. Plan và entitlement

- [ ] **S3.1 - Tạo dữ liệu plan có version**
  - [ ] Tạo `feature_definitions`, `plan_versions`, `plan_prices` và quota.
  - [ ] Dùng amount minor integer, không dùng floating point cho tiền.
  - [ ] Không cho checkout live nếu giá, tiền tệ hoặc provider chưa chốt.
  - [ ] Chuẩn hóa feature code: `REPORT`, `EXPORT_EXCEL`, `EXPORT_PDF`, `ADVANCED_ANALYTICS`, `FILE_ATTACHMENT`, `TIME_TRACKING`, `AI_ASSISTANT`.

- [ ] **S3.2 - EntitlementService**
  - [ ] Tính quyền hiệu lực từ subscription status và plan version.
  - [ ] Kiểm tra entitlement tại service nghiệp vụ và job entry.
  - [ ] FE chỉ đọc capabilities backend trả về, không suy luận theo tên gói.
  - [ ] Cache entitlement có version/invalidation khi đổi gói hoặc trạng thái.

- [ ] **S3.3 - Free và trial**
  - [ ] Tạo Free subscription `ACTIVE` khi tạo workspace.
  - [ ] Backfill Free/legacy subscription idempotent cho workspace hiện có.
  - [ ] Owner chủ động bắt đầu Pro trial.
  - [ ] Ghi `trial_used_at` một lần và `trial_end` sau 14 ngày nếu policy được duyệt.
  - [ ] Chống lạm dụng trial qua việc tạo/xóa workspace hoặc đổi owner.

- [ ] **S3.4 - API entitlement/usage**
  - [ ] `GET /workspaces/{w}/entitlements` trả feature/capability hiệu lực.
  - [ ] `GET /workspaces/{w}/usage` trả limit, used, reserved và resetAt.
  - [ ] Billing detail chỉ trả cho Owner.

### 5.4. Định nghĩa usage

- [ ] Member tính membership `ACTIVE`, gồm Owner và Viewer; invitation pending chưa tính seat.
- [ ] Project/Task chưa soft delete đều tính quota; trạng thái Done/Cancelled/Archived vẫn tính theo bảng gói.
- [ ] Storage tính byte vật lý còn lưu, gồm file soft delete chưa purge.
- [ ] Chỉ giảm storage usage sau khi xóa file vật lý thành công.
- [ ] Export tính khi job hoàn tất tạo artifact; retry cùng job không tính thêm.
- [ ] AI ghi request cùng token/chi phí thực tế; lỗi trước provider không tính, lỗi đã tiêu thụ provider tính phần đã dùng.
- [ ] Quota tháng của export/AI reset theo tháng UTC và API trả rõ `resetAt`.

### 5.5. Reserve, commit và release quota

- [ ] **S3.6 - Thao tác đồng bộ**
  - [ ] Lock hoặc conditional update theo `used + reserved + delta <= limit`.
  - [ ] Ghi resource và counter trong cùng transaction.
  - [ ] Rollback resource phải rollback counter.

- [ ] **S3.7 - Thao tác bất đồng bộ**
  - [ ] Upload/import/export/AI tạo reservation có TTL và business key duy nhất.
  - [ ] Commit khi có kết quả.
  - [ ] Release phần chưa dùng khi hủy/lỗi.

- [ ] **S3.8 - Import và upload**
  - [ ] Reserve toàn bộ số Task hợp lệ trước khi import.
  - [ ] Một dòng lỗi hoặc thiếu quota phải rollback toàn bộ batch.
  - [ ] Upload kiểm tra cả dung lượng dự kiến và dung lượng thực.

- [ ] **S3.9 - Idempotency**
  - [ ] Unique request/job key chống cộng usage hai lần.
  - [ ] Reservation timeout phải kiểm tra trạng thái job/provider trước khi release.

- [ ] **S3.10 - Reconciliation**
  - [ ] Job hằng ngày tính lại usage từ dữ liệu gốc.
  - [ ] Ghi chênh lệch và sửa có audit.
  - [ ] Không dùng Redis làm nguồn quyết định quota nếu DB/counter nguyên tử là nguồn thật.

### 5.6. Xử lý downgrade vượt quota

- [ ] Không xóa dữ liệu cũ.
- [ ] Owner chọn số seat được giữ, luôn gồm Owner.
- [ ] Seat vượt giới hạn chuyển `OVER_LIMIT` chỉ đọc nếu chưa được chọn.
- [ ] Chặn tạo Project/Task/upload mới khi metric tương ứng vượt quota.
- [ ] Cho đọc, dọn dữ liệu và sửa/hoàn tất Task hiện có theo quyền.
- [ ] Chặn job paid-only mới; artifact cũ còn tải trong retention và phạm vi quyền.
- [ ] FE hiển thị metric đang vượt, ảnh hưởng và cách khắc phục.

### 5.7. Cổng nghiệm thu S3

- [ ] Hai request tranh một chỗ chỉ một thành công.
- [ ] Import rollback không tăng usage.
- [ ] Upload lỗi không giữ reservation vô hạn.
- [ ] Đổi plan/trial có hiệu lực và cache không giữ quyền cũ.
- [ ] FE và BE hiển thị cùng một capability/limit.
- [ ] Không endpoint hoặc job nào chỉ dựa vào kiểm tra phía FE.

---

## 6. Phase S4 - Subscription, checkout, payment và billing

### 6.1. Mục tiêu

Xử lý vòng đời thuê bao và thanh toán có thể retry, chống xử lý trùng và không cấp sai kỳ sử dụng.

### 6.2. Phụ thuộc

- Entitlement, plan version và quota S3 đã đạt.
- Giá, currency, billing interval, provider account và webhook secret đã được cấu hình thật trước khi mở live.

### 6.3. Subscription lifecycle

- [ ] Free -> Pro bằng giao dịch được backend xác nhận.
- [ ] MVP chỉ mở một gói trả phí self-service; Business hoặc thay chu kỳ để cuối kỳ hay liên hệ riêng.
- [ ] Downgrade lưu `pending_plan` và `effective_at`; hiển thị ảnh hưởng quota.
- [ ] Không xóa Task, file hoặc member khi hạ gói.
- [ ] Manual renewal tạo order có kỳ dự kiến; trước hạn thì nối từ `period_end`, sau hạn thì áp policy đã chốt.
- [ ] Chỉ bật auto-renew nếu provider có recurring subscription thật và consent/cancel được đồng bộ.
- [ ] Job trial/grace/period phải idempotent, dùng UTC và lock subscription.
- [ ] API đọc entitlement tự kiểm tra thời hạn, không phụ thuộc scheduler chạy đúng phút.

### 6.4. Checkout

- [ ] **S4.1 - Tiếp nhận yêu cầu checkout**
  - [ ] Chỉ Owner được checkout.
  - [ ] Request dùng `planPriceId` và `Idempotency-Key`.
  - [ ] Backend kiểm tra workspace, giá đang bán, currency và subscription hiện tại.
  - [ ] Không nhận amount hoặc kỳ được cấp từ client.

- [ ] **S4.2 - Tạo billing order**
  - [ ] Tạo `billing_order` trạng thái `PENDING`.
  - [ ] Lưu snapshot plan, price, amount, currency, interval và intended period.
  - [ ] Cùng idempotency key khác payload trả `409`.
  - [ ] Gọi provider ngoài transaction DB dài và truyền provider idempotency key.

- [ ] **S4.3 - Xử lý redirect**
  - [ ] Lưu provider session/reference hoặc trạng thái cần reconcile khi timeout.
  - [ ] Chỉ redirect tới URL allowlist.
  - [ ] Trang quay lại hiển thị `đang xác nhận` và polling order; không tự cấp quyền hoặc báo thành công.

### 6.5. Webhook và reconciliation

- [ ] **S4.4 - Xác minh webhook**
  - [ ] Verify chữ ký trên raw body.
  - [ ] Kiểm tra timestamp, replay window, secret, environment và provider account.
  - [ ] Payload sai không được ghi payment hợp lệ.

- [ ] **S4.5 - Lưu event bền vững**
  - [ ] Unique theo provider, environment, account và event ID.
  - [ ] Chỉ trả 2xx sau khi event hợp lệ đã được lưu hoặc xác nhận đã tiếp nhận trước đó.
  - [ ] DB lỗi trả response để provider retry theo hợp đồng của provider.

- [ ] **S4.6 - Worker xử lý payment**
  - [ ] Lock event và order.
  - [ ] Xác minh transaction, amount, currency, order và workspace mapping.
  - [ ] Event không mapping chắc chắn đưa vào hàng đối soát, không cấp quyền.

- [ ] **S4.7 - Apply kết quả nguyên tử**
  - [ ] Cập nhật payment, subscription, subscription history, event và outbox trong một transaction.
  - [ ] Dùng unique business key để hai event khác ID cho cùng payment/kỳ không cấp hai lần.

- [ ] **S4.8 - Event sai thứ tự**
  - [ ] Đối chiếu provider object/version.
  - [ ] Không để event `FAILED` cũ ghi đè `SUCCEEDED` mới.
  - [ ] Refund/chargeback là state transition riêng.

- [ ] **S4.9 - Retry và reconcile**
  - [ ] Worker retry theo backoff, giới hạn attempts và chuyển hàng lỗi.
  - [ ] Reconciliation job gọi server API provider đã xác thực.
  - [ ] Mọi kết quả đi qua cùng hàm apply idempotent.

### 6.6. Frontend Billing

- [ ] Billing Overview: plan, status, trial/grace/period, usage và cảnh báo quota.
- [ ] Plan Comparison: feature và giá lấy từ backend.
- [ ] Checkout Result: pending, confirmed, failed và expired.
- [ ] Polling có timeout/backoff; khi chưa rõ hiển thị order ID để kiểm tra sau.
- [ ] Cancel Renewal và Undo Cancel nếu policy/provider cho phép.
- [ ] Downgrade hiển thị ngày hiệu lực và ảnh hưởng dữ liệu/quota.
- [ ] Lịch sử payment và billing document.
- [ ] Phân biệt rõ hủy gia hạn với xóa workspace.

### 6.7. Refund và tranh chấp

- [ ] Refund yêu cầu `BILLING_ADMIN`, re-authentication, reason và audit.
- [ ] Không hoàn vượt số tiền còn có thể hoàn.
- [ ] Timeout phải reconcile trước khi retry.
- [ ] Trạng thái gồm `REQUESTED`, `PROCESSING`, `SUCCEEDED`, `FAILED` và hỗ trợ partial/full theo policy.
- [ ] Chốt entitlement sau refund/chargeback trước khi mở live.
- [ ] Không tự xóa dữ liệu công việc sau refund.

### 6.8. Cổng nghiệm thu S4

- [ ] Webhook trùng không cấp gói/kỳ hai lần.
- [ ] Event sai thứ tự không làm lùi trạng thái đúng.
- [ ] Sai signature, amount, currency hoặc account không kích hoạt gói.
- [ ] Provider timeout và DB lỗi có thể reconcile an toàn.
- [ ] Hai checkout cùng kỳ không cấp thừa thời gian.
- [ ] Trial, cancel, grace và renewal có đúng ngày hiệu lực.
- [ ] FE không báo thành công chỉ dựa vào redirect URL.

---

## 7. Phase S5 - Platform Administration và hỗ trợ vận hành

### 7.1. Mục tiêu

Cung cấp công cụ tối thiểu để vận hành tenant, subscription, payment và hàng lỗi trước pilot trả phí mà không cho nhân sự hỗ trợ mặc nhiên đọc nội dung Project.

### 7.2. Phụ thuộc

- S1 đến S4 đã đạt.
- Đã có audit, outbox, payment event và subscription state đủ dữ liệu vận hành.

### 7.3. Các bước thực hiện

- [ ] **S5.1 - Platform roles**
  - [ ] Tách `SUPER_ADMIN`, `SUPPORT`, `BILLING_ADMIN` khỏi workspace/project role.
  - [ ] Bảo vệ route `/platform` bằng server policy.
  - [ ] Không cấp platform role qua public register.
  - [ ] Bật MFA hoặc xác thực mạnh cho tài khoản admin theo khả năng hệ thống.

- [ ] **S5.2 - Dashboard vận hành**
  - [ ] Tổng user và workspace.
  - [ ] Subscription theo trạng thái.
  - [ ] Trial sắp hết.
  - [ ] Payment cần đối soát.
  - [ ] Webhook/outbox/job lỗi.
  - [ ] Workspace gần quota.
  - [ ] Filter theo thời gian, plan, status và phân trang.

- [ ] **S5.3 - Workspace Detail**
  - [ ] Chỉ hiển thị metadata, owner, member count, plan, usage, status và audit.
  - [ ] Không đọc Task, file hoặc prompt theo mặc định.
  - [ ] Suspend/restore yêu cầu reason, expectedVersion và re-authentication.

- [ ] **S5.4 - Plan version management**
  - [ ] Chỉ tạo version mới, không sửa ngược lịch sử giá/order.
  - [ ] Validate quota, currency, interval và phạm vi áp dụng.
  - [ ] Preview workspace bị ảnh hưởng trước khi kích hoạt.

- [ ] **S5.5 - Hàng lỗi và replay**
  - [ ] Trang payment, webhook, outbox lỗi hiển thị payload đã lọc, attempts và correlation ID.
  - [ ] Replay qua processor idempotent hiện có.
  - [ ] Không tạo nút chỉnh payment thành success khi không có bằng chứng provider.

- [ ] **S5.6 - User support**
  - [ ] Xem verified/disabled status.
  - [ ] Revoke session.
  - [ ] Resend verification/reset qua luồng chuẩn.
  - [ ] Không xem password, token hoặc nội dung tenant.
  - [ ] Ghi audit actor, reason, time và correlation ID.

### 7.4. Frontend S5

- [ ] Platform Dashboard.
- [ ] Users, Workspaces, Subscriptions và Payments.
- [ ] Audit Logs.
- [ ] Webhook/Outbox Retry Queue.
- [ ] Plan Version Editor và impact preview.
- [ ] Confirm dialog + reason cho thao tác nhạy cảm.
- [ ] Permission gate theo đúng platform role.

### 7.5. Cổng nghiệm thu S5

- [ ] Support không đọc nội dung Project.
- [ ] Billing Admin không gán platform role.
- [ ] Suspend workspace A không khóa workspace B của cùng user.
- [ ] Replay payment không gia hạn hai lần.
- [ ] Mọi mutation nhạy cảm có actor, reason, time, correlation ID và dữ liệu audit đã lọc.

---

## 8. Phase S6 - Đóng gói module V1 và AI Action Items

### 8.1. Mục tiêu

Hoàn thiện các module V1 theo tenant, role và plan; bổ sung luồng xác nhận Action Item từ AI thành Task thật.

### 8.2. Phụ thuộc

- S1 đã tenant-scope mọi module đang bật.
- S3 entitlement/quota đã ổn định.
- Inventory và regression baseline S0 còn sử dụng được.

### 8.3. Đóng gói các module V1

- [ ] **S6.1 - Tạo ticket theo inventory**
  - [ ] Mỗi module có ticket riêng cho tenant, permission, entitlement, data integrity và FE state.
  - [ ] Không viết lại CRUD đang ổn chỉ để đổi cấu trúc.

- [ ] **S6.2 - Feature gating**
  - [ ] Bổ sung entitlement tại service và job entry.
  - [ ] Permission vẫn kiểm tra độc lập với plan.
  - [ ] Chỉ gợi ý nâng gói khi lỗi thật sự do entitlement/quota; không biến mọi `403` thành upsell.

- [ ] **S6.3 - Lịch sử Sprint và báo cáo**
  - [ ] Tạo hoặc hoàn thiện `task_sprint_history` với attach/detach time và reason.
  - [ ] Ghi snapshot/sự kiện cần thiết để báo cáo quá khứ không đổi theo trạng thái hiện tại.
  - [ ] Không suy diễn lịch sử không tồn tại thành số liệu chính xác.

- [ ] **S6.4 - Regression toàn sản phẩm**
  - [ ] Chạy E2E trên Free và Pro.
  - [ ] Chạy user có nhiều workspace.
  - [ ] So sánh số liệu nghiệp vụ trước/sau migration bằng cùng dataset.
  - [ ] Kiểm tra Project, Sprint, Backlog, Task, Bug, Kanban, Comment, Time Log, Notification, Activity, Attachment, Import, Search, Report, Export và Analytics.

### 8.4. AI Meeting Assistant và Action Items

- [ ] **S6.A1 - Quyền gọi AI**
  - [ ] Kiểm tra workspace/project access, AI entitlement và quota trước retrieval/provider call.
  - [ ] Chỉ lấy dữ liệu user có quyền xem.

- [ ] **S6.A2 - Sinh bản nháp**
  - [ ] AI trả title, description, backlog đề xuất, assignee, estimate và due date.
  - [ ] Đánh dấu trường AI suy đoán.
  - [ ] Backend validate schema và không tin project ID/role trong output AI.

- [ ] **S6.A3 - Lưu preview**
  - [ ] Lưu workspace, project, source meeting, creator, revision và expiry.
  - [ ] UI cho chọn, bỏ chọn và sửa từng item.
  - [ ] Chỉ tạo Backlog Item mới khi user có quyền và chủ động chọn.

- [ ] **S6.A4 - Confirm**
  - [ ] Request gửi preview ID, expected revision, selected items và `Idempotency-Key`.
  - [ ] Kiểm tra lại membership và quyền tạo Task tại thời điểm confirm.
  - [ ] Kiểm tra Backlog đúng Project, assignee còn hợp lệ, Sprint chưa đóng và quota còn đủ.

- [ ] **S6.A5 - Tạo Task nguyên tử**
  - [ ] Tạo toàn bộ Task được chọn trong một transaction hoặc từ chối toàn bộ.
  - [ ] Trả lỗi cụ thể theo item.
  - [ ] Giữ invariant Task thuộc Backlog và không gán Sprint đã đóng.

- [ ] **S6.A6 - Idempotency**
  - [ ] Lưu mapping preview item -> task ID duy nhất và request hash.
  - [ ] Confirm lặp trả cùng Task.
  - [ ] Cùng key khác payload trả `409`.
  - [ ] Quyền bị thu hồi sau preview phải bị chặn ở confirm.

### 8.5. An toàn dữ liệu và chi phí AI

- [ ] Cache AI tách theo tenant và phạm vi quyền.
- [ ] Không log prompt/audio thô mặc định.
- [ ] Ghi model, request ID, token và ước tính chi phí đã lọc.
- [ ] Giới hạn input/output, timeout, concurrency và ngân sách workspace.
- [ ] Chống prompt injection yêu cầu truy cập workspace khác hoặc bỏ qua quyền.
- [ ] Provider lỗi không tạo Task ngầm và phải có trạng thái retry rõ.
- [ ] Google Meet vẫn là link nhập thủ công, không tuyên bố đã tạo Calendar/Meet tự động.

### 8.6. Frontend S6

- [ ] AI Preview hiển thị nguồn, revision, trường suy đoán và thời hạn.
- [ ] Cho sửa/chọn từng Action Item trước confirm.
- [ ] Hiển thị validation theo item, quota error, version conflict và assignee invalid.
- [ ] Hiển thị rõ Action Item là đề xuất, chưa phải Task thật.
- [ ] Sau confirm trả danh sách Task đã tạo và deep link hợp lệ.

### 8.7. Cổng nghiệm thu S6

- [ ] Tất cả chức năng V1 được công bố hoạt động đúng tenant, role và plan.
- [ ] Downgrade không phá khả năng đọc dữ liệu cũ.
- [ ] Không endpoint/job nào bỏ qua feature gating.
- [ ] AI output chứa ID sai hoặc prompt injection không vượt được kiểm tra backend.
- [ ] Assignee bị xóa, quota hết, confirm đồng thời và idempotency đều được kiểm thử.
- [ ] AI không tạo Task nếu người dùng chưa confirm.

---

## 9. Phase S7 - Security, production readiness và release

### 9.1. Mục tiêu

Đóng toàn bộ security, migration, vận hành, hiệu năng và release gate trước pilot/production.

### 9.2. Phụ thuộc

- S0 đến S6 có bằng chứng nghiệm thu.
- Cấu hình email, payment, storage, domain và secrets đúng môi trường.

### 9.3. Security gate

- [ ] **S7.1 - Authorization review toàn hệ thống**
  - [ ] Rà API, native query, worker và scheduler.
  - [ ] Kiểm tra tenant, project role, resource ownership, entitlement và workspace status.
  - [ ] Test cross-tenant cho export, file, cache, WebSocket, Search, Report và AI.

- [ ] **S7.2 - Abuse protection**
  - [ ] Rate limit auth theo IP và account.
  - [ ] Chống lạm dụng email, invitation và workspace creation.
  - [ ] Rate limit/quota upload, search, export và AI theo tenant.
  - [ ] Backend không tin role cũ trong token.

- [ ] **S7.3 - Secret và surface công khai**
  - [ ] Tất cả secret qua runtime config hoặc secret manager.
  - [ ] Tách JWT, email, payment và storage key theo dev/staging/prod.
  - [ ] Không commit secret.
  - [ ] Không expose actuator/OpenAPI nhạy cảm ra public.

- [ ] **S7.4 - File security**
  - [ ] Validate MIME, nội dung, size, filename và path.
  - [ ] Lưu ngoài executable/public directory.
  - [ ] File đáng ngờ được cách ly hoặc scan trước download.
  - [ ] Xóa vật lý qua job có checkpoint và audit.

- [ ] **S7.5 - Session và thao tác nhạy cảm**
  - [ ] Session/cookie/CORS/CSRF khớp kiến trúc FE.
  - [ ] Admin dùng xác thực mạnh.
  - [ ] Owner transfer, workspace deletion và refund yêu cầu re-authentication.

### 9.4. Vận hành và deploy

- [ ] **S7.6 - CI/CD**
  - [ ] Pipeline: build -> unit/integration/security test -> migration check -> staging -> smoke -> release.
  - [ ] Health/readiness phản ánh dependency cần thiết nhưng không lộ secret.
  - [ ] Docker và profile chạy bằng runtime env, không hard-code môi trường deploy.

- [ ] **S7.7 - Observability**
  - [ ] Structured log có correlation ID và workspace ID phù hợp.
  - [ ] Lọc token, password và payment payload nhạy cảm.
  - [ ] Theo dõi latency, error rate, DB pool, disk/storage, worker lag, email/webhook backlog.
  - [ ] Có dashboard và cảnh báo kèm người chịu trách nhiệm.

- [ ] **S7.8 - Backup và restore**
  - [ ] Backup DB và file theo cùng mốc.
  - [ ] Restore rehearsal bằng dữ liệu và môi trường ghi rõ.
  - [ ] Mục tiêu pilot đề xuất: RPO không quá 24 giờ và RTO không quá 4 giờ.
  - [ ] Chỉ công bố RPO/RTO sau khi diễn tập chứng minh được.

- [ ] **S7.9 - Worker và scheduler**
  - [ ] Retry/backoff, dead-letter queue, lease timeout và idempotency.
  - [ ] Multi-instance scheduler dùng lock/dedup theo event/kỳ.
  - [ ] Không gửi reminder hoặc áp expiration hai lần.

### 9.5. Hiệu năng và chất lượng

- [ ] API đọc thông thường đạt mục tiêu pilot p95 <= 800 ms trên dataset đã ghi rõ.
- [ ] API ghi thông thường đạt mục tiêu pilot p95 <= 1.200 ms.
- [ ] HTTP 5xx dưới 1% trong bài tải 30 phút, không tính validation/429 chủ đích.
- [ ] Webhook ingestion p95 <= 1 giây cho bước verify và lưu event.
- [ ] Load test gồm workspace lớn, nhỏ và tenant gửi nhiều request.
- [ ] Không bỏ tenant/permission check để đạt tốc độ; tối ưu bằng index, projection và batch fetch.
- [ ] Export lớn chạy job và giới hạn kích thước.
- [ ] AI có timeout và concurrency riêng.
- [ ] FE có loading skeleton, empty/error state, keyboard/focus và giữ dữ liệu form khi lỗi có thể phục hồi.

### 9.6. Pilot và release

- [ ] Pilot với 2 đến 3 nhóm được mời và dữ liệu được phép sử dụng.
- [ ] Theo dõi ít nhất một vòng trial/billing sandbox và một lần đổi/hủy gói.
- [ ] Chốt giá, quota, retention, refund và provider trước mở rộng.
- [ ] Chạy rehearsal deploy, rollback và restore.
- [ ] Triage toàn bộ defect; không còn P0/P1 về rò dữ liệu, mất dữ liệu, auth bypass hoặc cấp gói sai.
- [ ] P2 chỉ được giữ khi có owner, workaround và không ảnh hưởng tenant/tiền/dữ liệu.
- [ ] Báo cáo release ghi commit, môi trường, người kiểm tra và giới hạn còn lại.

### 9.7. Cổng nghiệm thu S7

- [ ] Cross-tenant security suite đạt.
- [ ] Email verification/reset/invite chạy E2E đúng môi trường.
- [ ] Payment/trial/grace/cancel chạy E2E và idempotent.
- [ ] Migration, rollback và restore rehearsal đạt.
- [ ] Alert, dashboard và người trực vận hành đã sẵn sàng.
- [ ] Không còn P0/P1 chưa xử lý.
- [ ] Chỉ đánh dấu release khi có bằng chứng thực tế, không dựa vào mô tả trong tài liệu.

---

## 10. Checklist migration dùng xuyên suốt S1 đến S7

### 10.1. Trước migration

- [ ] Ghi baseline commit và migration version cuối.
- [ ] Lưu schema, row count và danh sách orphan record.
- [ ] Backup DB và file, sau đó restore thử.
- [ ] Xác định legacy workspace owner từ dữ liệu/người phụ trách thật.
- [ ] Chốt maintenance window hoặc thiết kế dual-write riêng nếu thực sự cần zero downtime.
- [ ] Rà mapping system role cũ sang workspace/project role; không biến ADMIN nội bộ thành SUPER_ADMIN SaaS.

### 10.2. Expand và backfill

- [ ] Tạo bảng tenant/billing nền và cột nullable.
- [ ] Backfill theo thứ tự cha trước, con sau.
- [ ] Bảo toàn ID và history.
- [ ] Đối chiếu từng bảng, FK và phạm vi quyền.
- [ ] File migration theo quy trình copy -> checksum -> cập nhật metadata -> dọn sau.
- [ ] Xóa cache namespace cũ và rotate/revalidate session nếu mapping role thay đổi.

### 10.3. Contract

- [ ] Chỉ bật `NOT NULL`/composite FK khi không còn dữ liệu sai.
- [ ] Tắt route/query legacy không tenant-safe.
- [ ] Chỉ drop cột cũ sau một kỳ ổn định và có migration riêng.
- [ ] Không trộn cleanup phá hủy vào cutover đầu.

### 10.4. Rollback

- [ ] Nếu chưa mở ghi V2: dừng deploy, rollback app tương thích schema mở rộng hoặc restore checkpoint.
- [ ] Nếu đã phát sinh ghi V2: dừng ghi/worker cần thiết và ưu tiên sửa tiến; không restore backup cũ gây mất dữ liệu mới.
- [ ] Không chạy V1 không tenant-safe trên DB đã chứa nhiều khách hàng.
- [ ] Rollback feature/route vẫn phải giữ tenant boundary.

---

## 11. Ma trận kiểm thử tối thiểu

- [ ] Đổi UUID từ tài nguyên A sang Task/file/report B không đọc, sửa, tải hoặc lộ metadata.
- [ ] User mở A/B ở hai tab không trộn query cache, realtime hoặc AI context.
- [ ] Workspace/Project Viewer gọi API ghi bị từ chối.
- [ ] Token verify/reset/invite hết hạn hoặc replay không cấp quyền.
- [ ] Hai invitation accept tranh seat cuối chỉ một thành công.
- [ ] Import có một dòng lỗi hoặc thiếu quota tạo 0 Task và usage không tăng.
- [ ] Webhook trùng/sai thứ tự không cấp một kỳ hai lần.
- [ ] Webhook sai signature/amount/currency/account không kích hoạt gói.
- [ ] DB/worker lỗi trước hoặc sau payment commit có thể retry không cấp thiếu/thừa.
- [ ] Scheduler chạy trùng không áp cancel/trial/grace hai lần.
- [ ] Member bị xóa sau khi tạo export/AI preview không còn download/confirm được.
- [ ] Kanban/Time Log concurrent request không ghi đè âm thầm hoặc sai dữ liệu.
- [ ] Task chuyển/hủy Sprint vẫn giữ Comment, Time Log và history đúng scope.
- [ ] Restore DB + file chạy smoke và row/file count khớp.

---

## 12. Roadmap đề xuất

| Phase | Khung tuần đề xuất | Đầu ra chính |
|---|---:|---|
| S0 - Baseline | W1 | Build, inventory, restore, smoke V1 và chốt phạm vi thực có |
| S1 - Tenant | W2-W4 | Workspace/member/role, migration rehearsal và mọi module tenant-safe |
| S2 - Public Auth | W5-W6 | Register, verify, reset, invite qua email và onboarding E2E |
| S3 - Plan/Usage | W7-W8 | Free/Pro/trial, quota nguyên tử, entitlement và usage UI |
| S4 - Billing | W9-W11 | Payment sandbox, webhook/reconcile, subscription lifecycle và billing UI |
| S5 - Platform Admin | W12 | Support metadata, suspend, audit, hàng lỗi và replay |
| S6 - Product completion | W13-W14 | Regression V1, report/history và AI Action Item confirm |
| S7 - Pilot/Release | W15-W16 | Security/load/restore gate, pilot, release và monitoring |
| Dự phòng | W17-W19 nếu cần | Provider integration, migration phức tạp và sửa lỗi pilot |

Thời lượng trên chỉ là ước lượng ban đầu cho hai developer có kinh nghiệm FE/BE và QA bán thời gian. Sau S0 phải cập nhật lại lịch theo baseline thực tế.

---

## 13. Definition of Done cho mỗi phase

Một phase chỉ được đánh dấu hoàn thành khi:

- [ ] Toàn bộ checklist bắt buộc đã có kết quả thực tế.
- [ ] Migration và rollback tương ứng đã được kiểm tra.
- [ ] Unit/integration/E2E/security test cần thiết đã chạy.
- [ ] OpenAPI, schema, error code và tài liệu FE/BE được cập nhật.
- [ ] Không còn lỗi P0/P1 trong phạm vi phase.
- [ ] Có commit/tag, môi trường, seed, test report và người kiểm tra.
- [ ] Không có secret, token hoặc dữ liệu nhạy cảm trong log/tài liệu.
- [ ] Cổng nghiệm thu phase đạt trước khi bắt đầu phase phụ thuộc tiếp theo.

## 14. Trạng thái theo dõi đề xuất

Dùng bảng sau khi bắt đầu triển khai thực tế:

| Phase | Trạng thái | Người phụ trách | Ngày bắt đầu | Ngày nghiệm thu | Bằng chứng | Blocker |
|---|---|---|---|---|---|---|
| S0 | Chưa bắt đầu |  |  |  |  |  |
| S1 | Chưa bắt đầu |  |  |  |  |  |
| S2 | Chưa bắt đầu |  |  |  |  |  |
| S3 | Chưa bắt đầu |  |  |  |  |  |
| S4 | Chưa bắt đầu |  |  |  |  |  |
| S5 | Chưa bắt đầu |  |  |  |  |  |
| S6 | Chưa bắt đầu |  |  |  |  |  |
| S7 | Chưa bắt đầu |  |  |  |  |  |

