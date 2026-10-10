# Checklist chức năng đã thực hiện của hệ thống HICAS Task Management

> Dùng làm cơ sở viết báo cáo đồ án. Các nội dung được sắp xếp theo trình tự xây dựng hệ thống, từ nền tảng đến nghiệp vụ, bảo mật, kiểm thử, triển khai và AI.

## Giai đoạn 1 - Khởi tạo và thiết kế nền tảng

| STT | Yêu cầu | Trạng thái | Hoàn thành |
|---:|---|---|:---:|
| 1 | Phân tích bài toán quản lý công việc theo mô hình Agile/Scrum | Đã thực hiện | ☑ |
| 2 | Xác định các vai trò hệ thống và vai trò trong dự án | Đã thực hiện | ☑ |
| 3 | Thiết kế kiến trúc frontend React và backend Spring Boot | Đã thực hiện | ☑ |
| 4 | Thiết kế RESTful API cho các module nghiệp vụ | Đã thực hiện | ☑ |
| 5 | Thiết kế và chuẩn hóa cơ sở dữ liệu PostgreSQL | Đã thực hiện | ☑ |
| 6 | Tích hợp Flyway để quản lý phiên bản cơ sở dữ liệu | Đã thực hiện | ☑ |
| 7 | Xây dựng cấu trúc dự án theo Controller, Service, Repository và DTO | Đã thực hiện | ☑ |
| 8 | Xây dựng cơ chế xử lý lỗi và mã lỗi thống nhất | Đã thực hiện | ☑ |
| 9 | Chuẩn hóa cấu trúc phản hồi API | Đã thực hiện | ☑ |
| 10 | Cấu hình OpenAPI và Swagger cho REST API | Đã thực hiện | ☑ |

## Giai đoạn 2 - Xác thực, tài khoản và phân quyền

| STT | Yêu cầu | Trạng thái | Hoàn thành |
|---:|---|---|:---:|
| 11 | Xây dựng chức năng đăng nhập bằng tài khoản và mật khẩu | Đã thực hiện | ☑ |
| 12 | Xây dựng xác thực bằng Spring Security và JWT | Đã thực hiện | ☑ |
| 13 | Phát hành Access Token và Refresh Token | Đã thực hiện | ☑ |
| 14 | Thiết lập thời hạn Access Token 30 phút và Refresh Token 7 ngày | Đã thực hiện | ☑ |
| 15 | Bổ sung `jti` cho Access Token và Refresh Token | Đã thực hiện | ☑ |
| 16 | Chỉ lưu hash Refresh Token, không lưu token thô | Đã thực hiện | ☑ |
| 17 | Xây dựng cơ chế Refresh Token Rotation | Đã thực hiện | ☑ |
| 18 | Thu hồi phiên hiện tại khi đăng xuất | Đã thực hiện | ☑ |
| 19 | Thu hồi toàn bộ phiên của người dùng khi đăng xuất tất cả thiết bị | Đã thực hiện | ☑ |
| 20 | Kiểm tra phiên trong cơ sở dữ liệu theo Access Token JTI | Đã thực hiện | ☑ |
| 21 | Thu hồi phiên khi tài khoản bị khóa hoặc vai trò hệ thống thay đổi | Đã thực hiện | ☑ |
| 22 | Lấy vai trò hiện tại từ cơ sở dữ liệu thay vì tin hoàn toàn vào JWT cũ | Đã thực hiện | ☑ |
| 23 | Xây dựng chức năng quản lý tài khoản người dùng | Đã thực hiện | ☑ |
| 24 | Xây dựng phân quyền hệ thống ADMIN, MANAGER và EMPLOYEE | Đã thực hiện | ☑ |
| 25 | Xây dựng phân quyền dự án Owner, Project Manager, Scrum Master, Product Owner, Developer, Tester và Viewer | Đã thực hiện | ☑ |

## Giai đoạn 3 - Quản lý dự án và thành viên

| STT | Yêu cầu | Trạng thái | Hoàn thành |
|---:|---|---|:---:|
| 26 | Tạo, cập nhật, tìm kiếm và quản lý trạng thái dự án | Đã thực hiện | ☑ |
| 27 | Xem thông tin tổng quan và chi tiết dự án | Đã thực hiện | ☑ |
| 28 | Thêm tài khoản đã tồn tại vào dự án | Đã thực hiện | ☑ |
| 29 | Thay đổi vai trò thành viên trong dự án | Đã thực hiện | ☑ |
| 30 | Xóa thành viên khỏi dự án nhưng bảo toàn lịch sử công việc | Đã thực hiện | ☑ |
| 31 | Bảo vệ Owner cuối cùng của dự án | Đã thực hiện | ☑ |
| 32 | Kiểm tra quyền quản lý thành viên theo vai trò dự án | Đã thực hiện | ☑ |
| 33 | Ghi nhận và hiển thị lịch sử hoạt động của dự án | Đã thực hiện | ☑ |
| 34 | Xây dựng nhật ký kiểm toán cho các thao tác quan trọng | Đã thực hiện | ☑ |

## Giai đoạn 4 - Product Backlog và Sprint

| STT | Yêu cầu | Trạng thái | Hoàn thành |
|---:|---|---|:---:|
| 35 | Tạo và quản lý Product Backlog | Đã thực hiện | ☑ |
| 36 | Tạo, cập nhật và xóa Backlog Item | Đã thực hiện | ☑ |
| 37 | Quản lý loại, độ ưu tiên, trạng thái và Story Point của Backlog Item | Đã thực hiện | ☑ |
| 38 | Tạo và lập kế hoạch Sprint | Đã thực hiện | ☑ |
| 39 | Đưa Backlog Item vào Sprint và đưa trở lại Product Backlog | Đã thực hiện | ☑ |
| 40 | Bắt đầu, hoàn thành và hủy Sprint | Đã thực hiện | ☑ |
| 41 | Bảo đảm mỗi dự án chỉ có một Sprint đang hoạt động | Đã thực hiện | ☑ |
| 42 | Bảo toàn Task, Comment và Time Log khi kết thúc hoặc hủy Sprint | Đã thực hiện | ☑ |
| 43 | Lưu lịch sử Task tham gia và rời Sprint | Đã thực hiện | ☑ |
| 44 | Xây dựng Sprint Capacity, Sprint Health và Sprint Risk | Đã thực hiện | ☑ |
| 45 | Xây dựng Sprint Review và Sprint Retrospective | Đã thực hiện | ☑ |

## Giai đoạn 5 - Task và bảng Kanban

| STT | Yêu cầu | Trạng thái | Hoàn thành |
|---:|---|---|:---:|
| 46 | Tạo, cập nhật, xem chi tiết và xóa Task | Đã thực hiện | ☑ |
| 47 | Quản lý loại Task, độ ưu tiên, Story Point và thời gian ước tính | Đã thực hiện | ☑ |
| 48 | Gán Task cho thành viên trong dự án | Đã thực hiện | ☑ |
| 49 | Xây dựng bảng Kanban theo trạng thái công việc | Đã thực hiện | ☑ |
| 50 | Chuyển Task giữa các cột Cần làm, Đang làm, Đang chờ, Đang review và Hoàn thành | Đã thực hiện | ☑ |
| 51 | Sắp xếp vị trí Task trong từng cột Kanban | Đã thực hiện | ☑ |
| 52 | Kiểm soát xung đột khi nhiều người đồng thời thay đổi Task hoặc vị trí Kanban | Đã thực hiện | ☑ |
| 53 | Xây dựng phụ thuộc giữa các Task | Đã thực hiện | ☑ |
| 54 | Xây dựng chức năng chặn và gỡ chặn Task | Đã thực hiện | ☑ |
| 55 | Phân tích và hiển thị rủi ro của Task | Đã thực hiện | ☑ |
| 56 | Tìm kiếm và lọc Task theo nhiều tiêu chí | Đã thực hiện | ☑ |
| 57 | Nhập danh sách Task từ file Excel theo cơ chế atomic | Đã thực hiện | ☑ |
| 58 | Cung cấp file Excel mẫu và lịch sử các lần import | Đã thực hiện | ☑ |

## Giai đoạn 6 - Bình luận và ghi nhận thời gian

| STT | Yêu cầu | Trạng thái | Hoàn thành |
|---:|---|---|:---:|
| 59 | Thêm, sửa và xóa bình luận trên Task | Đã thực hiện | ☑ |
| 60 | Phân quyền cập nhật và xóa bình luận | Đã thực hiện | ☑ |
| 61 | Ghi nhận thời gian làm việc trên từng Task | Đã thực hiện | ☑ |
| 62 | Cập nhật và xóa Time Log | Đã thực hiện | ☑ |
| 63 | Tổng hợp thời gian đã làm theo Task và thành viên | Đã thực hiện | ☑ |
| 64 | Kiểm soát đồng thời khi nhiều yêu cầu cập nhật Time Log | Đã thực hiện | ☑ |
| 65 | Xây dựng màn hình Timesheet | Đã thực hiện | ☑ |

## Giai đoạn 7 - Bug, tài liệu và tệp đính kèm

| STT | Yêu cầu | Trạng thái | Hoàn thành |
|---:|---|---|:---:|
| 66 | Tạo và quản lý Bug trong dự án | Đã thực hiện | ☑ |
| 67 | Quản lý trạng thái, mức độ nghiêm trọng và người phụ trách Bug | Đã thực hiện | ☑ |
| 68 | Trao đổi và cộng tác trong quá trình xử lý Bug | Đã thực hiện | ☑ |
| 69 | Đính kèm minh chứng và tài liệu cho Bug hoặc Task | Đã thực hiện | ☑ |
| 70 | Tải lên, tải xuống và xóa tệp theo quyền truy cập | Đã thực hiện | ☑ |
| 71 | Kiểm tra loại tệp, kích thước, tên tệp và chống path traversal | Đã thực hiện | ☑ |
| 72 | Tách vùng lưu file runtime khỏi vùng thực thi công khai | Đã thực hiện | ☑ |

## Giai đoạn 8 - Thông báo và cập nhật thời gian thực

| STT | Yêu cầu | Trạng thái | Hoàn thành |
|---:|---|---|:---:|
| 73 | Xây dựng thông báo trong hệ thống | Đã thực hiện | ☑ |
| 74 | Đánh dấu đã đọc và xóa thông báo | Đã thực hiện | ☑ |
| 75 | Gửi thông báo theo người nhận và sự kiện dự án | Đã thực hiện | ☑ |
| 76 | Chống tạo thông báo trùng bằng deduplication key | Đã thực hiện | ☑ |
| 77 | Cập nhật thông báo thời gian thực bằng WebSocket/STOMP | Đã thực hiện | ☑ |
| 78 | Đồng bộ thay đổi bảng Kanban giữa nhiều trình duyệt | Đã thực hiện | ☑ |
| 79 | Xây dựng nhắc việc và tổng hợp thông báo hằng ngày | Đã thực hiện | ☑ |
| 80 | Kiểm tra quyền khi kết nối và đăng ký WebSocket topic | Đã thực hiện | ☑ |

## Giai đoạn 9 - Dashboard, tìm kiếm và báo cáo

| STT | Yêu cầu | Trạng thái | Hoàn thành |
|---:|---|---|:---:|
| 81 | Xây dựng Dashboard cá nhân | Đã thực hiện | ☑ |
| 82 | Xây dựng Dashboard tổng quan dự án | Đã thực hiện | ☑ |
| 83 | Thống kê số lượng Task theo trạng thái và thành viên | Đã thực hiện | ☑ |
| 84 | Xây dựng biểu đồ tiến độ Sprint và Burndown | Đã thực hiện | ☑ |
| 85 | Xây dựng tìm kiếm toàn hệ thống và bộ lọc nâng cao | Đã thực hiện | ☑ |
| 86 | Xây dựng báo cáo theo Project, Sprint, thành viên và thời gian | Đã thực hiện | ☑ |
| 87 | Xuất báo cáo Excel bằng Apache POI | Đã thực hiện | ☑ |
| 88 | Xuất báo cáo PDF bằng OpenPDF | Đã thực hiện | ☑ |
| 89 | Xây dựng trang quản trị và thống kê hệ thống | Đã thực hiện | ☑ |
| 90 | Tích hợp trang kiểm tra Actuator Health và Info cho Admin | Đã thực hiện | ☑ |

## Giai đoạn 10 - Giao diện và trải nghiệm người dùng

| STT | Yêu cầu | Trạng thái | Hoàn thành |
|---:|---|---|:---:|
| 91 | Xây dựng frontend bằng React, TypeScript và Vite | Đã thực hiện | ☑ |
| 92 | Xây dựng giao diện responsive bằng Tailwind CSS | Đã thực hiện | ☑ |
| 93 | Chuẩn hóa Button, Input, Select, Modal, Tabs, Table, Toast và các component dùng chung | Đã thực hiện | ☑ |
| 94 | Xây dựng giao diện quản lý Project, Backlog, Sprint, Kanban, Bug, Timesheet và báo cáo | Đã thực hiện | ☑ |
| 95 | Xây dựng trạng thái loading, empty, error và success | Đã thực hiện | ☑ |
| 96 | Chuẩn hóa nội dung tiếng Việt và thông báo lỗi cho người dùng | Đã thực hiện | ☑ |
| 97 | Xây dựng hướng dẫn sử dụng trực quan trong hệ thống | Đã thực hiện | ☑ |
| 98 | Đồng bộ giao diện hướng dẫn với giao diện chức năng thực tế | Đã thực hiện | ☑ |

## Giai đoạn 11 - Bảo mật và kiểm soát dữ liệu

| STT | Yêu cầu | Trạng thái | Hoàn thành |
|---:|---|---|:---:|
| 99 | Kiểm tra quyền truy cập Project và tài nguyên con tại backend | Đã thực hiện | ☑ |
| 100 | Ngăn truy cập Task, Attachment và Report ngoài phạm vi dự án | Đã thực hiện | ☑ |
| 101 | Kiểm tra quyền gán Task và quản lý thành viên dự án | Đã thực hiện | ☑ |
| 102 | Bổ sung validation và giới hạn độ dài dữ liệu đầu vào | Đã thực hiện | ☑ |
| 103 | Chuẩn hóa và làm sạch từ khóa tìm kiếm | Đã thực hiện | ☑ |
| 104 | Chống upload file không hợp lệ và truy cập đường dẫn trái phép | Đã thực hiện | ☑ |
| 105 | Áp dụng rate limit cho đăng nhập và các API có nguy cơ bị lạm dụng | Đã thực hiện | ☑ |
| 106 | Kiểm tra CORS, cấu hình Security Header và phản hồi lỗi bảo mật | Đã thực hiện | ☑ |
| 107 | Không trả stack trace hoặc dữ liệu nhạy cảm về frontend | Đã thực hiện | ☑ |
| 108 | Tắt Swagger và dữ liệu demo trong môi trường production | Đã thực hiện | ☑ |

## Giai đoạn 12 - Hiệu năng và xử lý đồng thời

| STT | Yêu cầu | Trạng thái | Hoàn thành |
|---:|---|---|:---:|
| 109 | Tích hợp Redis Cache | Đã thực hiện | ☑ |
| 110 | Chuẩn hóa cache name, cache key, TTL và cơ chế invalidation | Đã thực hiện | ☑ |
| 111 | Tối ưu truy vấn và hạn chế lỗi N+1 | Đã thực hiện | ☑ |
| 112 | Sử dụng projection và batch processing tại các luồng phù hợp | Đã thực hiện | ☑ |
| 113 | Tối ưu hiệu năng import Task từ Excel | Đã thực hiện | ☑ |
| 114 | Xây dựng khóa và version để xử lý đồng thời trên Kanban | Đã thực hiện | ☑ |
| 115 | Kiểm soát đồng thời khi cập nhật Time Log | Đã thực hiện | ☑ |
| 116 | Bổ sung index và constraint phục vụ truy vấn thực tế | Đã thực hiện | ☑ |

## Giai đoạn 13 - Kiểm thử

| STT | Yêu cầu | Trạng thái | Hoàn thành |
|---:|---|---|:---:|
| 117 | Xây dựng Unit Test cho validator, service và helper | Đã thực hiện | ☑ |
| 118 | Xây dựng Repository Test với PostgreSQL Testcontainers | Đã thực hiện | ☑ |
| 119 | Xây dựng Integration Test cho các API chính | Đã thực hiện | ☑ |
| 120 | Kiểm thử luồng Agile từ Project đến Sprint và Task | Đã thực hiện | ☑ |
| 121 | Kiểm thử transaction atomic cho import Task và Sprint | Đã thực hiện | ☑ |
| 122 | Kiểm thử Security cho Authentication và Authorization | Đã thực hiện | ☑ |
| 123 | Kiểm thử quyền sở hữu Attachment và phạm vi Task/Project | Đã thực hiện | ☑ |
| 124 | Kiểm thử cache, WebSocket, rate limit và CORS | Đã thực hiện | ☑ |
| 125 | Kiểm thử đồng thời cho Kanban và Time Log | Đã thực hiện | ☑ |
| 126 | Kiểm thử migration, constraint và tính toàn vẹn cơ sở dữ liệu | Đã thực hiện | ☑ |

## Giai đoạn 14 - Docker, CI/CD và vận hành

| STT | Yêu cầu | Trạng thái | Hoàn thành |
|---:|---|---|:---:|
| 127 | Viết Dockerfile cho backend Spring Boot | Đã thực hiện | ☑ |
| 128 | Viết Dockerfile và cấu hình Nginx cho frontend React | Đã thực hiện | ☑ |
| 129 | Chạy frontend, backend, PostgreSQL và Redis bằng Docker Compose | Đã thực hiện | ☑ |
| 130 | Chuẩn hóa biến môi trường cho dev, docker và production | Đã thực hiện | ☑ |
| 131 | Cho phép truyền runtime environment vào Nginx thay vì hard-code backend | Đã thực hiện | ☑ |
| 132 | Tạo dữ liệu demo đầy đủ cho profile dev và docker | Đã thực hiện | ☑ |
| 133 | Xây dựng GitHub Actions để build và kiểm thử dự án | Đã thực hiện | ☑ |
| 134 | Tích hợp Spring Boot Actuator Health và Info | Đã thực hiện | ☑ |
| 135 | Xây dựng quy trình backup và restore PostgreSQL cùng file upload | Đã thực hiện | ☑ |
| 136 | Chuẩn bị cấu hình reverse proxy, domain và HTTPS khi triển khai | Đã thực hiện | ☑ |
| 137 | Tách cấu hình và secret khỏi mã nguồn | Đã thực hiện | ☑ |

## Giai đoạn 15 - Chuẩn hóa mã nguồn và tài liệu

| STT | Yêu cầu | Trạng thái | Hoàn thành |
|---:|---|---|:---:|
| 138 | Rà soát và làm sạch mã nguồn toàn dự án | Đã thực hiện | ☑ |
| 139 | Chuẩn hóa response và Global Exception Handler | Đã thực hiện | ☑ |
| 140 | Rà soát và chuẩn hóa hệ thống ErrorCode | Đã thực hiện | ☑ |
| 141 | Bổ sung tài liệu OpenAPI/Swagger cho API | Đã thực hiện | ☑ |
| 142 | Rà soát schema, index, constraint và migration database | Đã thực hiện | ☑ |
| 143 | Hoàn thiện README hướng dẫn chạy local và Docker | Đã thực hiện | ☑ |
| 144 | Hoàn thiện tài liệu kiến trúc, bảo mật, cache, database và API | Đã thực hiện | ☑ |
| 145 | Hoàn thiện release checklist và tài liệu vận hành | Đã thực hiện | ☑ |

## Giai đoạn 16 - AI Meeting Assistant với Google Gemini

| STT | Yêu cầu | Trạng thái | Hoàn thành |
|---:|---|---|:---:|
| 146 | Tích hợp Google Gemini AI qua cấu hình API key và model | Đã thực hiện | ☑ |
| 147 | Xây dựng lớp AI Provider để tách logic Gemini khỏi nghiệp vụ | Đã thực hiện | ☑ |
| 148 | Xây dựng Project Context từ dữ liệu Project, Sprint và Task | Đã thực hiện | ☑ |
| 149 | Xây dựng chức năng hỏi đáp dữ liệu Project bằng AI | Đã thực hiện | ☑ |
| 150 | Xây dựng chức năng gợi ý nội dung cuộc họp bằng AI | Đã thực hiện | ☑ |
| 151 | Hỗ trợ gợi ý Daily Standup, Sprint Planning, Sprint Review, Retrospective và Issue Resolution | Đã thực hiện | ☑ |
| 152 | Xây dựng chức năng tạo biên bản họp từ ghi chú thô | Đã thực hiện | ☑ |
| 153 | Trích xuất nội dung thảo luận, quyết định, vấn đề tồn đọng và rủi ro | Đã thực hiện | ☑ |
| 154 | Xây dựng chức năng đề xuất Action Item sau cuộc họp | Đã thực hiện | ☑ |
| 155 | Hiển thị Action Item ở dạng bản nháp, không tự động tạo Task | Đã thực hiện | ☑ |
| 156 | Xây dựng giao diện AI Meeting Assistant trên frontend | Đã thực hiện | ☑ |
| 157 | Cho phép sao chép kết quả và tạo lại nội dung bằng AI | Đã thực hiện | ☑ |
| 158 | Xây dựng quản lý cuộc họp và gắn link Google Meet thủ công | Đã thực hiện | ☑ |
| 159 | Ghi log loại yêu cầu AI, model, thời gian xử lý và trạng thái | Đã thực hiện | ☑ |
| 160 | Giới hạn context theo quyền Project và không gửi dữ liệu bí mật vào prompt | Đã thực hiện | ☑ |

## Tổng kết kết quả

| Nội dung | Kết quả |
|---|---|
| Tổng số hạng mục trong checklist | 160 |
| Hạng mục đã thực hiện | 160 |
| Backend | Spring Boot 3.5, Java 21, Spring Security, JPA, WebSocket |
| Frontend | React 19, TypeScript, Vite, Tailwind CSS |
| Dữ liệu | PostgreSQL 16, Flyway, Redis |
| Báo cáo | Excel bằng Apache POI, PDF bằng OpenPDF |
| AI | Google Gemini AI Meeting Assistant |
| Kiểm thử | JUnit 5, Spring Boot Test, Spring Security Test, Testcontainers |
| Triển khai | Docker Compose, Nginx, Caddy, GitHub Actions, Actuator |

## Thứ tự trình bày đề xuất trong báo cáo

1. Giới thiệu bài toán và mục tiêu hệ thống.
2. Phân tích yêu cầu và vai trò người dùng.
3. Kiến trúc tổng thể và công nghệ sử dụng.
4. Thiết kế cơ sở dữ liệu.
5. Xác thực, quản lý phiên và phân quyền.
6. Quản lý Project và thành viên.
7. Product Backlog và Sprint.
8. Task, Kanban, Comment và Time Log.
9. Bug, Attachment, Notification và realtime.
10. Dashboard, tìm kiếm và báo cáo.
11. Bảo mật và tối ưu hiệu năng.
12. Kiểm thử hệ thống.
13. Docker, CI/CD, backup và triển khai.
14. AI Meeting Assistant.
15. Kết quả đạt được, hạn chế và hướng phát triển.
