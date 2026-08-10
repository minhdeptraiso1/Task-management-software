-- Dữ liệu trình diễn chỉ được nạp bởi profile dev/docker.
-- Tất cả khóa chính cố định và INSERT idempotent để an toàn khi Flyway chạy lại.

-- Mật khẩu dùng chung cho tài khoản demo: 123456
INSERT INTO users (id, username, email, password, role, enabled, created_at, updated_at, created_by, updated_by)
VALUES
('10000000-0000-0000-0000-000000000001', 'manager.demo', 'manager@hicas.demo', '$2a$10$SLFWIgDoFq49uquyFJDoReLBUcVmElHLISAfXp/TgY3EwN07VCRsC', 'MANAGER', TRUE, now() - interval '120 days', now(), 'demo', 'demo'),
('10000000-0000-0000-0000-000000000002', 'scrum.demo', 'scrum@hicas.demo', '$2a$10$SLFWIgDoFq49uquyFJDoReLBUcVmElHLISAfXp/TgY3EwN07VCRsC', 'EMPLOYEE', TRUE, now() - interval '110 days', now(), 'demo', 'demo'),
('10000000-0000-0000-0000-000000000003', 'po.demo', 'po@hicas.demo', '$2a$10$SLFWIgDoFq49uquyFJDoReLBUcVmElHLISAfXp/TgY3EwN07VCRsC', 'EMPLOYEE', TRUE, now() - interval '100 days', now(), 'demo', 'demo'),
('10000000-0000-0000-0000-000000000004', 'dev.minh', 'dev.minh@hicas.demo', '$2a$10$SLFWIgDoFq49uquyFJDoReLBUcVmElHLISAfXp/TgY3EwN07VCRsC', 'EMPLOYEE', TRUE, now() - interval '90 days', now(), 'demo', 'demo'),
('10000000-0000-0000-0000-000000000005', 'dev.lan', 'dev.lan@hicas.demo', '$2a$10$SLFWIgDoFq49uquyFJDoReLBUcVmElHLISAfXp/TgY3EwN07VCRsC', 'EMPLOYEE', TRUE, now() - interval '80 days', now(), 'demo', 'demo'),
('10000000-0000-0000-0000-000000000006', 'qa.linh', 'qa.linh@hicas.demo', '$2a$10$SLFWIgDoFq49uquyFJDoReLBUcVmElHLISAfXp/TgY3EwN07VCRsC', 'EMPLOYEE', TRUE, now() - interval '70 days', now(), 'demo', 'demo'),
('10000000-0000-0000-0000-000000000007', 'viewer.demo', 'viewer@hicas.demo', '$2a$10$SLFWIgDoFq49uquyFJDoReLBUcVmElHLISAfXp/TgY3EwN07VCRsC', 'EMPLOYEE', TRUE, now() - interval '60 days', now(), 'demo', 'demo'),
('10000000-0000-0000-0000-000000000008', 'locked.demo', 'locked@hicas.demo', '$2a$10$SLFWIgDoFq49uquyFJDoReLBUcVmElHLISAfXp/TgY3EwN07VCRsC', 'EMPLOYEE', FALSE, now() - interval '50 days', now(), 'demo', 'demo'),
('10000000-0000-0000-0000-000000000009', 'pm.demo', 'pm@hicas.demo', '$2a$10$SLFWIgDoFq49uquyFJDoReLBUcVmElHLISAfXp/TgY3EwN07VCRsC', 'MANAGER', TRUE, now() - interval '95 days', now(), 'demo', 'demo')
ON CONFLICT DO NOTHING;

INSERT INTO projects (id, code, name, description, status, start_date, end_date, created_by_user_id, created_at, updated_at, created_by, updated_by)
VALUES
('20000000-0000-0000-0000-000000000001', 'HICAS-DEMO', 'Hệ thống quản lý công việc Agile', 'Dự án demo đầy đủ Sprint, Kanban, báo cáo, Bug và cộng tác.', 'ACTIVE', current_date - 90, current_date + 90, '10000000-0000-0000-0000-000000000001', now() - interval '90 days', now(), 'manager.demo', 'manager.demo'),
('20000000-0000-0000-0000-000000000002', 'ERP-FIN', 'Hệ thống quản lý tài chính', 'Dự án đang ở giai đoạn lập kế hoạch.', 'PLANNING', current_date + 7, current_date + 180, '10000000-0000-0000-0000-000000000001', now() - interval '20 days', now(), 'manager.demo', 'manager.demo'),
('20000000-0000-0000-0000-000000000003', 'CRM-2026', 'Nâng cấp hệ thống CRM', 'Dự án tạm dừng để chờ xác nhận phạm vi.', 'ON_HOLD', current_date - 60, current_date + 30, '10000000-0000-0000-0000-000000000001', now() - interval '60 days', now(), 'manager.demo', 'manager.demo'),
('20000000-0000-0000-0000-000000000004', 'PORTAL-V2', 'Cổng thông tin khách hàng V2', 'Dự án đã hoàn thành và bàn giao.', 'COMPLETED', current_date - 180, current_date - 30, '10000000-0000-0000-0000-000000000001', now() - interval '180 days', now() - interval '30 days', 'manager.demo', 'manager.demo'),
('20000000-0000-0000-0000-000000000005', 'MOBILE-POC', 'Ứng dụng di động thử nghiệm', 'POC đã dừng sau giai đoạn đánh giá.', 'CANCELLED', current_date - 120, current_date - 75, '10000000-0000-0000-0000-000000000001', now() - interval '120 days', now() - interval '75 days', 'manager.demo', 'manager.demo'),
('20000000-0000-0000-0000-000000000006', 'LEGACY-ARC', 'Hệ thống nội bộ cũ', 'Dữ liệu dự án đã lưu trữ, chỉ dùng để tra cứu.', 'ARCHIVED', current_date - 500, current_date - 200, '10000000-0000-0000-0000-000000000001', now() - interval '500 days', now() - interval '200 days', 'manager.demo', 'manager.demo')
ON CONFLICT DO NOTHING;

INSERT INTO project_members (id, project_id, user_id, role, joined_at, created_at, updated_at, created_by, updated_by)
VALUES
('21000000-0000-0000-0000-000000000001','20000000-0000-0000-0000-000000000001','10000000-0000-0000-0000-000000000001','OWNER',now()-interval '90 days',now()-interval '90 days',now(),'demo','demo'),
('21000000-0000-0000-0000-000000000002','20000000-0000-0000-0000-000000000001','10000000-0000-0000-0000-000000000002','SCRUM_MASTER',now()-interval '85 days',now()-interval '85 days',now(),'demo','demo'),
('21000000-0000-0000-0000-000000000003','20000000-0000-0000-0000-000000000001','10000000-0000-0000-0000-000000000003','PRODUCT_OWNER',now()-interval '85 days',now()-interval '85 days',now(),'demo','demo'),
('21000000-0000-0000-0000-000000000004','20000000-0000-0000-0000-000000000001','10000000-0000-0000-0000-000000000004','DEVELOPER',now()-interval '80 days',now()-interval '80 days',now(),'demo','demo'),
('21000000-0000-0000-0000-000000000005','20000000-0000-0000-0000-000000000001','10000000-0000-0000-0000-000000000005','DEVELOPER',now()-interval '80 days',now()-interval '80 days',now(),'demo','demo'),
('21000000-0000-0000-0000-000000000006','20000000-0000-0000-0000-000000000001','10000000-0000-0000-0000-000000000006','TESTER',now()-interval '75 days',now()-interval '75 days',now(),'demo','demo'),
('21000000-0000-0000-0000-000000000007','20000000-0000-0000-0000-000000000001','10000000-0000-0000-0000-000000000007','VIEWER',now()-interval '60 days',now()-interval '60 days',now(),'demo','demo'),
('21000000-0000-0000-0000-000000000008','20000000-0000-0000-0000-000000000002','10000000-0000-0000-0000-000000000001','OWNER',now()-interval '20 days',now()-interval '20 days',now(),'demo','demo'),
('21000000-0000-0000-0000-000000000009','20000000-0000-0000-0000-000000000002','10000000-0000-0000-0000-000000000004','DEVELOPER',now()-interval '15 days',now()-interval '15 days',now(),'demo','demo'),
('21000000-0000-0000-0000-000000000010','20000000-0000-0000-0000-000000000003','10000000-0000-0000-0000-000000000001','OWNER',now()-interval '60 days',now()-interval '60 days',now(),'demo','demo'),
('21000000-0000-0000-0000-000000000011','20000000-0000-0000-0000-000000000001','10000000-0000-0000-0000-000000000009','PROJECT_MANAGER',now()-interval '88 days',now()-interval '88 days',now(),'demo','demo')
ON CONFLICT DO NOTHING;

INSERT INTO sprints (id, project_id, name, goal, status, start_date, end_date, started_at, completed_at, created_by_user_id, created_at, updated_at, created_by, updated_by)
VALUES
('30000000-0000-0000-0000-000000000001','20000000-0000-0000-0000-000000000001','Sprint 01 - Nền tảng','Hoàn thiện đăng nhập và nền tảng dự án','COMPLETED',current_date-27,current_date-21,now()-interval '27 days',now()-interval '21 days','10000000-0000-0000-0000-000000000002',now()-interval '30 days',now()-interval '21 days','scrum.demo','scrum.demo'),
('30000000-0000-0000-0000-000000000002','20000000-0000-0000-0000-000000000001','Sprint 02 - Kanban Realtime','Hoàn thiện Kanban, thông báo và báo cáo','ACTIVE',current_date-6,current_date+1,now()-interval '6 days',NULL,'10000000-0000-0000-0000-000000000002',now()-interval '10 days',now(),'scrum.demo','scrum.demo'),
('30000000-0000-0000-0000-000000000003','20000000-0000-0000-0000-000000000001','Sprint 03 - Báo cáo','Chuẩn bị báo cáo nâng cao và tối ưu hiệu năng','PLANNING',current_date+3,current_date+10,NULL,NULL,'10000000-0000-0000-0000-000000000002',now()-interval '2 days',now(),'scrum.demo','scrum.demo'),
('30000000-0000-0000-0000-000000000004','20000000-0000-0000-0000-000000000001','Sprint thử nghiệm đã hủy','Minh họa Sprint bị hủy','CANCELLED',current_date-40,current_date-34,now()-interval '40 days',now()-interval '38 days','10000000-0000-0000-0000-000000000002',now()-interval '42 days',now()-interval '38 days','scrum.demo','scrum.demo')
ON CONFLICT DO NOTHING;

INSERT INTO backlog_items (id, project_id, sprint_id, title, description, type, status, priority, story_points, position, created_by_user_id, created_at, updated_at, created_by, updated_by)
VALUES
('40000000-0000-0000-0000-000000000001','20000000-0000-0000-0000-000000000001','30000000-0000-0000-0000-000000000002','Đăng nhập và quản lý phiên','Người dùng đăng nhập an toàn và refresh token tự động.','USER_STORY','IN_SPRINT','URGENT',8,1,'10000000-0000-0000-0000-000000000003',now()-interval '20 days',now(),'po.demo','po.demo'),
('40000000-0000-0000-0000-000000000002','20000000-0000-0000-0000-000000000001','30000000-0000-0000-0000-000000000002','Bảng Kanban thời gian thực','Các thành viên theo dõi thay đổi mà không cần F5.','FEATURE','IN_SPRINT','HIGH',13,2,'10000000-0000-0000-0000-000000000003',now()-interval '18 days',now(),'po.demo','po.demo'),
('40000000-0000-0000-0000-000000000003','20000000-0000-0000-0000-000000000001','30000000-0000-0000-0000-000000000002','Thông báo trong dự án','Gửi thông báo đúng người và đúng sự kiện.','USER_STORY','IN_SPRINT','HIGH',8,3,'10000000-0000-0000-0000-000000000003',now()-interval '16 days',now(),'po.demo','po.demo'),
('40000000-0000-0000-0000-000000000004','20000000-0000-0000-0000-000000000001','30000000-0000-0000-0000-000000000002','Báo cáo Sprint','Xuất Excel, PDF và biểu đồ tiến độ.','EPIC','IN_SPRINT','MEDIUM',13,4,'10000000-0000-0000-0000-000000000003',now()-interval '15 days',now(),'po.demo','po.demo'),
('40000000-0000-0000-0000-000000000005','20000000-0000-0000-0000-000000000001',NULL,'Tìm kiếm toàn hệ thống','Tìm kiếm dự án, Sprint, Task và Bug.','FEATURE','READY','MEDIUM',5,5,'10000000-0000-0000-0000-000000000003',now()-interval '8 days',now(),'po.demo','po.demo'),
('40000000-0000-0000-0000-000000000006','20000000-0000-0000-0000-000000000001',NULL,'Tối ưu giao diện mobile','Cải thiện khả năng sử dụng trên màn hình nhỏ.','TECHNICAL','DRAFT','LOW',3,6,'10000000-0000-0000-0000-000000000003',now()-interval '6 days',now(),'po.demo','po.demo'),
('40000000-0000-0000-0000-000000000007','20000000-0000-0000-0000-000000000001','30000000-0000-0000-0000-000000000001','Khởi tạo hệ thống phân quyền','Thiết lập role và quyền theo dự án.','USER_STORY','DONE','HIGH',8,7,'10000000-0000-0000-0000-000000000003',now()-interval '35 days',now()-interval '21 days','po.demo','po.demo'),
('40000000-0000-0000-0000-000000000008','20000000-0000-0000-0000-000000000001','30000000-0000-0000-0000-000000000004','POC giao diện cũ','Backlog item trong Sprint đã hủy.','TECHNICAL','CANCELLED','LOW',2,8,'10000000-0000-0000-0000-000000000003',now()-interval '45 days',now()-interval '38 days','po.demo','po.demo')
ON CONFLICT DO NOTHING;

INSERT INTO tasks (id, project_id, backlog_item_id, current_sprint_id, origin_sprint_id, title, description, type, status, priority, assignee_user_id, reporter_user_id, estimated_minutes, start_date, due_date, completed_at, position, block_reason, blocked_at, blocked_by_user_id, created_at, updated_at, created_by, updated_by, version)
VALUES
('50000000-0000-0000-0000-000000000001','20000000-0000-0000-0000-000000000001','40000000-0000-0000-0000-000000000001','30000000-0000-0000-0000-000000000002','30000000-0000-0000-0000-000000000002','Xây dựng API đăng nhập bằng email','Đăng nhập, trả access token và refresh token.','DEVELOPMENT','DONE','URGENT','10000000-0000-0000-0000-000000000004','10000000-0000-0000-0000-000000000003',480,current_date-6,current_date-4,now()-interval '3 days',1,NULL,NULL,NULL,now()-interval '12 days',now()-interval '3 days','po.demo','dev.minh',3),
('50000000-0000-0000-0000-000000000002','20000000-0000-0000-0000-000000000001','40000000-0000-0000-0000-000000000001','30000000-0000-0000-0000-000000000002','30000000-0000-0000-0000-000000000002','Refresh token tự động','Rotation token và retry request khi access token hết hạn.','DEVELOPMENT','IN_REVIEW','HIGH','10000000-0000-0000-0000-000000000004','10000000-0000-0000-0000-000000000003',360,current_date-5,current_date,NULL,1,NULL,NULL,NULL,now()-interval '10 days',now()-interval '1 day','po.demo','dev.minh',2),
('50000000-0000-0000-0000-000000000003','20000000-0000-0000-0000-000000000001','40000000-0000-0000-0000-000000000002','30000000-0000-0000-0000-000000000002','30000000-0000-0000-0000-000000000002','Phát sự kiện Kanban qua WebSocket','Đồng bộ sự kiện đổi trạng thái cho thành viên dự án.','DEVELOPMENT','IN_PROGRESS','HIGH','10000000-0000-0000-0000-000000000005','10000000-0000-0000-0000-000000000002',420,current_date-5,current_date+1,NULL,1,NULL,NULL,NULL,now()-interval '9 days',now()-interval '2 days','scrum.demo','dev.lan',2),
('50000000-0000-0000-0000-000000000004','20000000-0000-0000-0000-000000000001','40000000-0000-0000-0000-000000000002','30000000-0000-0000-0000-000000000002','30000000-0000-0000-0000-000000000002','Xử lý xung đột vị trí Kanban','Khóa vị trí và trả lỗi trực quan khi dữ liệu đã thay đổi.','DEVELOPMENT','BLOCKED','HIGH','10000000-0000-0000-0000-000000000005','10000000-0000-0000-0000-000000000002',300,current_date-4,current_date-1,NULL,1,'Đang chờ xác nhận quy tắc ưu tiên khi hai người kéo đồng thời.',now()-interval '1 day','10000000-0000-0000-0000-000000000005',now()-interval '8 days',now()-interval '1 day','scrum.demo','dev.lan',2),
('50000000-0000-0000-0000-000000000005','20000000-0000-0000-0000-000000000001','40000000-0000-0000-0000-000000000002','30000000-0000-0000-0000-000000000002','30000000-0000-0000-0000-000000000002','Kiểm thử kéo thả đa trình duyệt','Kiểm tra cập nhật realtime và trạng thái loading.','TESTING','TODO','MEDIUM','10000000-0000-0000-0000-000000000006','10000000-0000-0000-0000-000000000002',240,current_date,current_date+1,NULL,1,NULL,NULL,NULL,now()-interval '7 days',now()-interval '7 days','scrum.demo','scrum.demo',0),
('50000000-0000-0000-0000-000000000006','20000000-0000-0000-0000-000000000001','40000000-0000-0000-0000-000000000003','30000000-0000-0000-0000-000000000002','30000000-0000-0000-0000-000000000002','Thiết kế popup thông báo','Danh sách thông báo, trạng thái đã đọc và đường dẫn đích.','DESIGN','DONE','MEDIUM','10000000-0000-0000-0000-000000000005','10000000-0000-0000-0000-000000000003',300,current_date-6,current_date-3,now()-interval '2 days',2,NULL,NULL,NULL,now()-interval '11 days',now()-interval '2 days','po.demo','dev.lan',2),
('50000000-0000-0000-0000-000000000007','20000000-0000-0000-0000-000000000001','40000000-0000-0000-0000-000000000003','30000000-0000-0000-0000-000000000002','30000000-0000-0000-0000-000000000002','Scheduler nhắc việc quá hạn','Gửi nhắc việc đúng một lần theo dedup key.','DEVELOPMENT','IN_PROGRESS','HIGH','10000000-0000-0000-0000-000000000004','10000000-0000-0000-0000-000000000002',360,current_date-4,current_date+1,NULL,2,NULL,NULL,NULL,now()-interval '8 days',now()-interval '1 day','scrum.demo','dev.minh',1),
('50000000-0000-0000-0000-000000000008','20000000-0000-0000-0000-000000000001','40000000-0000-0000-0000-000000000003','30000000-0000-0000-0000-000000000002','30000000-0000-0000-0000-000000000002','Daily digest cho thành viên','Tổng hợp công việc cần chú ý mỗi ngày.','DEVELOPMENT','TODO','LOW',NULL,'10000000-0000-0000-0000-000000000002',180,current_date,current_date+2,NULL,2,NULL,NULL,NULL,now()-interval '6 days',now()-interval '6 days','scrum.demo','scrum.demo',0),
('50000000-0000-0000-0000-000000000009','20000000-0000-0000-0000-000000000001','40000000-0000-0000-0000-000000000004','30000000-0000-0000-0000-000000000002','30000000-0000-0000-0000-000000000002','Xuất báo cáo Excel Sprint','Xuất summary, task, time log và biểu đồ.','DEVELOPMENT','DONE','MEDIUM','10000000-0000-0000-0000-000000000004','10000000-0000-0000-0000-000000000003',420,current_date-6,current_date-2,now()-interval '1 day',3,NULL,NULL,NULL,now()-interval '14 days',now()-interval '1 day','po.demo','dev.minh',4),
('50000000-0000-0000-0000-000000000010','20000000-0000-0000-0000-000000000001','40000000-0000-0000-0000-000000000004','30000000-0000-0000-0000-000000000002','30000000-0000-0000-0000-000000000002','Xuất báo cáo PDF Sprint','PDF tiếng Việt, bảng thống kê và biểu đồ.','DEVELOPMENT','IN_REVIEW','MEDIUM','10000000-0000-0000-0000-000000000005','10000000-0000-0000-0000-000000000003',360,current_date-5,current_date+1,NULL,2,NULL,NULL,NULL,now()-interval '13 days',now()-interval '1 day','po.demo','dev.lan',2),
('50000000-0000-0000-0000-000000000011','20000000-0000-0000-0000-000000000001','40000000-0000-0000-0000-000000000004','30000000-0000-0000-0000-000000000002','30000000-0000-0000-0000-000000000002','Kiểm thử số liệu cumulative flow','Đối chiếu lịch sử trạng thái theo từng ngày.','TESTING','TODO','HIGH','10000000-0000-0000-0000-000000000006','10000000-0000-0000-0000-000000000003',240,current_date,current_date+1,NULL,3,NULL,NULL,NULL,now()-interval '5 days',now()-interval '5 days','po.demo','po.demo',0),
('50000000-0000-0000-0000-000000000012','20000000-0000-0000-0000-000000000001','40000000-0000-0000-0000-000000000004','30000000-0000-0000-0000-000000000002','30000000-0000-0000-0000-000000000002','Tài liệu hướng dẫn báo cáo','Mô tả mục đích và cách đọc từng chỉ số.','DOCUMENTATION','CANCELLED','LOW','10000000-0000-0000-0000-000000000005','10000000-0000-0000-0000-000000000003',120,current_date-3,current_date,NULL,1,NULL,NULL,NULL,now()-interval '5 days',now()-interval '2 days','po.demo','po.demo',1),
('50000000-0000-0000-0000-000000000013','20000000-0000-0000-0000-000000000001','40000000-0000-0000-0000-000000000002','30000000-0000-0000-0000-000000000002','30000000-0000-0000-0000-000000000002','Tối ưu truy vấn bảng Kanban','Giảm số truy vấn và giữ thời gian phản hồi ổn định.','RESEARCH','TODO','MEDIUM','10000000-0000-0000-0000-000000000004','10000000-0000-0000-0000-000000000002',300,current_date,current_date+3,NULL,4,NULL,NULL,NULL,now()-interval '4 days',now()-interval '4 days','scrum.demo','scrum.demo',0),
('50000000-0000-0000-0000-000000000014','20000000-0000-0000-0000-000000000001','40000000-0000-0000-0000-000000000003','30000000-0000-0000-0000-000000000002','30000000-0000-0000-0000-000000000002','Kiểm tra quyền xem thông báo','Đảm bảo thành viên chỉ nhận thông báo thuộc phạm vi.','TESTING','DONE','HIGH','10000000-0000-0000-0000-000000000006','10000000-0000-0000-0000-000000000002',180,current_date-5,current_date-3,now()-interval '2 days',4,NULL,NULL,NULL,now()-interval '8 days',now()-interval '2 days','scrum.demo','qa.linh',1),
('50000000-0000-0000-0000-000000000015','20000000-0000-0000-0000-000000000001','40000000-0000-0000-0000-000000000007',NULL,'30000000-0000-0000-0000-000000000001','Phân quyền theo role dự án','Hoàn thiện quyền Owner, PO, Scrum Master, Developer và Tester.','DEVELOPMENT','DONE','HIGH','10000000-0000-0000-0000-000000000004','10000000-0000-0000-0000-000000000003',480,current_date-27,current_date-22,now()-interval '22 days',1,NULL,NULL,NULL,now()-interval '32 days',now()-interval '22 days','po.demo','dev.minh',3)
ON CONFLICT DO NOTHING;

INSERT INTO task_comments (id, task_id, user_id, parent_comment_id, content, edited_at, created_at, updated_at, created_by, updated_by)
VALUES
('60000000-0000-0000-0000-000000000001','50000000-0000-0000-0000-000000000002','10000000-0000-0000-0000-000000000006',NULL,'Đã kiểm tra rotation token, cần bổ sung trường hợp đăng xuất toàn bộ thiết bị.',NULL,now()-interval '2 days',now()-interval '2 days','qa.linh','qa.linh'),
('60000000-0000-0000-0000-000000000002','50000000-0000-0000-0000-000000000002','10000000-0000-0000-0000-000000000004','60000000-0000-0000-0000-000000000001','Mình đã bổ sung kiểm tra accessTokenJti và cập nhật test.',NULL,now()-interval '1 day',now()-interval '1 day','dev.minh','dev.minh'),
('60000000-0000-0000-0000-000000000003','50000000-0000-0000-0000-000000000004','10000000-0000-0000-0000-000000000005',NULL,'Đang chờ PO xác nhận quy tắc sắp xếp khi có xung đột.',NULL,now()-interval '1 day',now()-interval '1 day','dev.lan','dev.lan'),
('60000000-0000-0000-0000-000000000004','50000000-0000-0000-0000-000000000010','10000000-0000-0000-0000-000000000006',NULL,'PDF hiển thị tiếng Việt tốt, còn cần kiểm tra tên file tải xuống.',NULL,now()-interval '6 hours',now()-interval '6 hours','qa.linh','qa.linh')
ON CONFLICT DO NOTHING;

INSERT INTO task_time_logs (id, task_id, user_id, work_date, minutes, description, created_at, updated_at, created_by, updated_by)
VALUES
('61000000-0000-0000-0000-000000000001','50000000-0000-0000-0000-000000000001','10000000-0000-0000-0000-000000000004',current_date-6,240,'Xây dựng API và validation',now()-interval '6 days',now()-interval '6 days','dev.minh','dev.minh'),
('61000000-0000-0000-0000-000000000002','50000000-0000-0000-0000-000000000001','10000000-0000-0000-0000-000000000004',current_date-5,270,'Hoàn thiện test đăng nhập',now()-interval '5 days',now()-interval '5 days','dev.minh','dev.minh'),
('61000000-0000-0000-0000-000000000003','50000000-0000-0000-0000-000000000002','10000000-0000-0000-0000-000000000004',current_date-3,210,'Refresh rotation và retry request',now()-interval '3 days',now()-interval '3 days','dev.minh','dev.minh'),
('61000000-0000-0000-0000-000000000004','50000000-0000-0000-0000-000000000003','10000000-0000-0000-0000-000000000005',current_date-4,300,'WebSocket event và subscription',now()-interval '4 days',now()-interval '4 days','dev.lan','dev.lan'),
('61000000-0000-0000-0000-000000000005','50000000-0000-0000-0000-000000000004','10000000-0000-0000-0000-000000000005',current_date-2,180,'Điều tra concurrent move',now()-interval '2 days',now()-interval '2 days','dev.lan','dev.lan'),
('61000000-0000-0000-0000-000000000006','50000000-0000-0000-0000-000000000006','10000000-0000-0000-0000-000000000005',current_date-3,300,'Thiết kế và hoàn thiện popup',now()-interval '3 days',now()-interval '3 days','dev.lan','dev.lan'),
('61000000-0000-0000-0000-000000000007','50000000-0000-0000-0000-000000000009','10000000-0000-0000-0000-000000000004',current_date-2,420,'Xuất workbook và kiểm tra số liệu',now()-interval '2 days',now()-interval '2 days','dev.minh','dev.minh'),
('61000000-0000-0000-0000-000000000008','50000000-0000-0000-0000-000000000010','10000000-0000-0000-0000-000000000005',current_date-1,240,'Định dạng PDF và font tiếng Việt',now()-interval '1 day',now()-interval '1 day','dev.lan','dev.lan'),
('61000000-0000-0000-0000-000000000009','50000000-0000-0000-0000-000000000014','10000000-0000-0000-0000-000000000006',current_date-2,150,'Kiểm thử ma trận quyền',now()-interval '2 days',now()-interval '2 days','qa.linh','qa.linh')
ON CONFLICT DO NOTHING;

INSERT INTO task_import_batches (id, project_id, sprint_id, imported_by_user_id, original_file_name, status, total_rows, success_rows, failed_rows, started_at, completed_at, error_message, created_at, updated_at, created_by, updated_by)
VALUES
('62000000-0000-0000-0000-000000000001','20000000-0000-0000-0000-000000000001','30000000-0000-0000-0000-000000000002','10000000-0000-0000-0000-000000000002','task-demo-thanh-cong.xlsx','COMPLETED',12,12,0,now()-interval '5 days',now()-interval '5 days'+interval '8 seconds',NULL,now()-interval '5 days',now()-interval '5 days','scrum.demo','scrum.demo'),
('62000000-0000-0000-0000-000000000002','20000000-0000-0000-0000-000000000001','30000000-0000-0000-0000-000000000002','10000000-0000-0000-0000-000000000002','task-demo-loi.xlsx','VALIDATION_FAILED',5,0,3,now()-interval '4 days',now()-interval '4 days'+interval '3 seconds','Dữ liệu không hợp lệ',now()-interval '4 days',now()-interval '4 days','scrum.demo','scrum.demo')
ON CONFLICT DO NOTHING;

INSERT INTO task_import_errors (id, import_batch_id, row_number, field_name, raw_value, error_message, created_at)
VALUES
('63000000-0000-0000-0000-000000000001','62000000-0000-0000-0000-000000000002',3,'BACKLOG_ITEM_TITLE','Tên không tồn tại','Không tìm thấy Backlog Item theo tên đã nhập.',now()-interval '4 days'),
('63000000-0000-0000-0000-000000000002','62000000-0000-0000-0000-000000000002',4,'PRIORITY','RẤT CAO','Giá trị Priority không thuộc danh sách cho phép.',now()-interval '4 days'),
('63000000-0000-0000-0000-000000000003','62000000-0000-0000-0000-000000000002',5,'TASK_TITLE',NULL,'TASK_TITLE không được để trống.',now()-interval '4 days')
ON CONFLICT DO NOTHING;

INSERT INTO sprint_reviews (id, sprint_id, project_id, goal_achieved, demo_summary, stakeholder_feedback, accepted_item_summary, rejected_item_summary, note, created_at, updated_at, created_by, updated_by)
VALUES ('70000000-0000-0000-0000-000000000001','30000000-0000-0000-0000-000000000001','20000000-0000-0000-0000-000000000001',TRUE,'Demo đăng nhập, phân quyền và quản lý thành viên.','Nghiệp vụ rõ ràng, cần cải thiện thông báo lỗi.','Đăng nhập; Phân quyền; Quản lý thành viên','Thông báo lỗi import cần trực quan hơn','Đã thống nhất nghiệm thu Sprint 01.',now()-interval '21 days',now()-interval '21 days','scrum.demo','scrum.demo')
ON CONFLICT DO NOTHING;

INSERT INTO sprint_retrospectives (id, sprint_id, project_id, went_well, went_wrong, improvement, action_items_json, note, created_at, updated_at, created_by, updated_by)
VALUES ('70000000-0000-0000-0000-000000000002','30000000-0000-0000-0000-000000000001','20000000-0000-0000-0000-000000000001','Team phối hợp tốt; test sớm; review code đều.','Một số task ước lượng thiếu thời gian kiểm thử.','Tách task nhỏ hơn và thống nhất Definition of Done.','[{"title":"Bổ sung checklist Done","assigneeUserId":"10000000-0000-0000-0000-000000000002","completed":true},{"title":"Review estimate trước Sprint","assigneeUserId":"10000000-0000-0000-0000-000000000003","completed":false}]','Dùng làm mẫu cho màn hình Retrospective.',now()-interval '21 days',now()-interval '20 days','scrum.demo','scrum.demo')
ON CONFLICT DO NOTHING;

INSERT INTO task_dependencies (id, task_id, depends_on_task_id, created_at, created_by)
VALUES
('71000000-0000-0000-0000-000000000001','50000000-0000-0000-0000-000000000002','50000000-0000-0000-0000-000000000001',now()-interval '5 days','dev.minh'),
('71000000-0000-0000-0000-000000000002','50000000-0000-0000-0000-000000000004','50000000-0000-0000-0000-000000000003',now()-interval '3 days','dev.lan')
ON CONFLICT DO NOTHING;

INSERT INTO bugs (id, project_id, backlog_item_id, task_id, sprint_id, title, description, severity, priority, status, assignee_user_id, reporter_user_id, reproduction_steps, expected_result, actual_result, due_date, reopened_count, resolved_at, closed_at, created_at, updated_at, created_by, updated_by)
VALUES
('80000000-0000-0000-0000-000000000001','20000000-0000-0000-0000-000000000001','40000000-0000-0000-0000-000000000001','50000000-0000-0000-0000-000000000002','30000000-0000-0000-0000-000000000002','Refresh token không chạy khi mở hai tab','Tab thứ hai nhận 401 sau khi tab đầu refresh.','CRITICAL','URGENT','IN_PROGRESS','10000000-0000-0000-0000-000000000004','10000000-0000-0000-0000-000000000006','Đăng nhập; mở hai tab; chờ token hết hạn; thao tác đồng thời.','Chỉ refresh một lần và cả hai request được retry.','Một request thành công, request còn lại trả 401.',current_date+1,1,NULL,NULL,now()-interval '3 days',now()-interval '1 day','qa.linh','dev.minh'),
('80000000-0000-0000-0000-000000000002','20000000-0000-0000-0000-000000000001','40000000-0000-0000-0000-000000000002','50000000-0000-0000-0000-000000000003','30000000-0000-0000-0000-000000000002','Thẻ Kanban bị mờ sau khi kéo','Overlay kéo thả chưa được reset.','HIGH','HIGH','RESOLVED','10000000-0000-0000-0000-000000000005','10000000-0000-0000-0000-000000000006','Kéo task liên tục giữa hai cột.','Thẻ rõ nét ngay sau khi thả.','Thẻ giữ opacity thấp đến khi reload.',current_date,0,now()-interval '10 hours',NULL,now()-interval '4 days',now()-interval '10 hours','qa.linh','dev.lan'),
('80000000-0000-0000-0000-000000000003','20000000-0000-0000-0000-000000000001','40000000-0000-0000-0000-000000000004','50000000-0000-0000-0000-000000000010','30000000-0000-0000-0000-000000000002','Tên file PDF tải xuống bị mã hóa','Content-Disposition trả filename không đúng chuẩn.','MEDIUM','MEDIUM','VERIFIED','10000000-0000-0000-0000-000000000005','10000000-0000-0000-0000-000000000006','Xuất PDF có tên tiếng Việt rồi tải xuống.','Tên file UTF-8 đọc được.','Tên file hiển thị chuỗi encoded-word.',current_date,0,now()-interval '2 days',NULL,now()-interval '6 days',now()-interval '1 day','qa.linh','qa.linh'),
('80000000-0000-0000-0000-000000000004','20000000-0000-0000-0000-000000000001','40000000-0000-0000-0000-000000000003',NULL,'30000000-0000-0000-0000-000000000002','Thông báo hiển thị trùng','Hai notification giống nhau xuất hiện sau scheduler.','LOW','LOW','CLOSED','10000000-0000-0000-0000-000000000004','10000000-0000-0000-0000-000000000006','Chạy scheduler nhắc hạn hai lần.','Dedup key ngăn bản ghi trùng.','Có hai thông báo cùng nội dung.',current_date-1,0,now()-interval '3 days',now()-interval '2 days',now()-interval '7 days',now()-interval '2 days','qa.linh','qa.linh'),
('80000000-0000-0000-0000-000000000005','20000000-0000-0000-0000-000000000001',NULL,NULL,'30000000-0000-0000-0000-000000000002','Sai màu badge mức ưu tiên','Màu badge chưa đồng bộ UI/UX.','LOW','LOW','OPEN',NULL,'10000000-0000-0000-0000-000000000006','Mở danh sách Bug và so sánh các priority.','Mỗi priority có màu nhất quán.','LOW và MEDIUM gần như cùng màu.',current_date+3,0,NULL,NULL,now()-interval '1 day',now()-interval '1 day','qa.linh','qa.linh'),
('80000000-0000-0000-0000-000000000006','20000000-0000-0000-0000-000000000001',NULL,NULL,'30000000-0000-0000-0000-000000000002','Bug không còn phù hợp sau đổi yêu cầu','Yêu cầu nghiệp vụ đã loại bỏ luồng liên quan.','MEDIUM','LOW','CANCELLED',NULL,'10000000-0000-0000-0000-000000000006','Không áp dụng.','Không áp dụng.','Không áp dụng.',current_date+5,0,NULL,NULL,now()-interval '2 days',now()-interval '1 day','qa.linh','po.demo')
ON CONFLICT DO NOTHING;

INSERT INTO bug_comments (id, project_id, bug_id, parent_id, author_user_id, content, created_at, updated_at, created_by, updated_by)
VALUES
('81000000-0000-0000-0000-000000000001','20000000-0000-0000-0000-000000000001','80000000-0000-0000-0000-000000000001',NULL,'10000000-0000-0000-0000-000000000006','Lỗi tái hiện ổn định trên Chrome và Edge.',now()-interval '2 days',now()-interval '2 days','qa.linh','qa.linh'),
('81000000-0000-0000-0000-000000000002','20000000-0000-0000-0000-000000000001','80000000-0000-0000-0000-000000000001','81000000-0000-0000-0000-000000000001','10000000-0000-0000-0000-000000000004','Đã xác định race condition ở hàng đợi refresh.',now()-interval '1 day',now()-interval '1 day','dev.minh','dev.minh')
ON CONFLICT DO NOTHING;

INSERT INTO bug_evidences (id, project_id, bug_id, created_by_user_id, title, steps_to_reproduce, expected_result, actual_result, environment, note, created_at, updated_at, created_by, updated_by)
VALUES ('82000000-0000-0000-0000-000000000001','20000000-0000-0000-0000-000000000001','80000000-0000-0000-0000-000000000001','10000000-0000-0000-0000-000000000006','Bằng chứng lỗi refresh đồng thời','Mở hai tab và gửi request cùng lúc khi token hết hạn.','Một refresh request; các request còn lại chờ và retry.','Hai refresh request dùng cùng refresh token.','Chrome 128, Windows 11, Docker local','Không gắn file vật lý để tránh link tải xuống giả.',now()-interval '2 days',now()-interval '2 days','qa.linh','qa.linh')
ON CONFLICT DO NOTHING;

-- Lịch sử trạng thái thật để biểu đồ CFD thay đổi qua từng ngày.
INSERT INTO project_activity_logs (id, project_id, entity_type, entity_id, action, performed_by_user_id, old_value_json, new_value_json, created_at)
VALUES
('90000000-0000-0000-0000-000000000001','20000000-0000-0000-0000-000000000001','TASK','50000000-0000-0000-0000-000000000001','TASK_CREATED','10000000-0000-0000-0000-000000000003',NULL,'{"status":"TODO"}',now()-interval '6 days'),
('90000000-0000-0000-0000-000000000002','20000000-0000-0000-0000-000000000001','TASK','50000000-0000-0000-0000-000000000001','TASK_STATUS_CHANGED','10000000-0000-0000-0000-000000000004','{"status":"TODO"}','{"status":"IN_PROGRESS"}',now()-interval '5 days'),
('90000000-0000-0000-0000-000000000003','20000000-0000-0000-0000-000000000001','TASK','50000000-0000-0000-0000-000000000001','TASK_STATUS_CHANGED','10000000-0000-0000-0000-000000000004','{"status":"IN_PROGRESS"}','{"status":"IN_REVIEW"}',now()-interval '4 days'),
('90000000-0000-0000-0000-000000000004','20000000-0000-0000-0000-000000000001','TASK','50000000-0000-0000-0000-000000000001','TASK_STATUS_CHANGED','10000000-0000-0000-0000-000000000006','{"status":"IN_REVIEW"}','{"status":"DONE"}',now()-interval '3 days'),
('90000000-0000-0000-0000-000000000005','20000000-0000-0000-0000-000000000001','TASK','50000000-0000-0000-0000-000000000003','TASK_CREATED','10000000-0000-0000-0000-000000000002',NULL,'{"status":"TODO"}',now()-interval '6 days'),
('90000000-0000-0000-0000-000000000006','20000000-0000-0000-0000-000000000001','TASK','50000000-0000-0000-0000-000000000003','TASK_STATUS_CHANGED','10000000-0000-0000-0000-000000000005','{"status":"TODO"}','{"status":"IN_PROGRESS"}',now()-interval '2 days'),
('90000000-0000-0000-0000-000000000007','20000000-0000-0000-0000-000000000001','TASK','50000000-0000-0000-0000-000000000004','TASK_CREATED','10000000-0000-0000-0000-000000000002',NULL,'{"status":"TODO"}',now()-interval '6 days'),
('90000000-0000-0000-0000-000000000008','20000000-0000-0000-0000-000000000001','TASK','50000000-0000-0000-0000-000000000004','TASK_STATUS_CHANGED','10000000-0000-0000-0000-000000000005','{"status":"TODO"}','{"status":"IN_PROGRESS"}',now()-interval '3 days'),
('90000000-0000-0000-0000-000000000009','20000000-0000-0000-0000-000000000001','TASK','50000000-0000-0000-0000-000000000004','TASK_BLOCKED','10000000-0000-0000-0000-000000000005','{"status":"IN_PROGRESS"}','{"status":"BLOCKED"}',now()-interval '1 day'),
('90000000-0000-0000-0000-000000000010','20000000-0000-0000-0000-000000000001','TASK','50000000-0000-0000-0000-000000000006','TASK_CREATED','10000000-0000-0000-0000-000000000003',NULL,'{"status":"TODO"}',now()-interval '6 days'),
('90000000-0000-0000-0000-000000000011','20000000-0000-0000-0000-000000000001','TASK','50000000-0000-0000-0000-000000000006','TASK_STATUS_CHANGED','10000000-0000-0000-0000-000000000005','{"status":"TODO"}','{"status":"IN_PROGRESS"}',now()-interval '5 days'),
('90000000-0000-0000-0000-000000000012','20000000-0000-0000-0000-000000000001','TASK','50000000-0000-0000-0000-000000000006','TASK_STATUS_CHANGED','10000000-0000-0000-0000-000000000006','{"status":"IN_PROGRESS"}','{"status":"DONE"}',now()-interval '2 days'),
('90000000-0000-0000-0000-000000000013','20000000-0000-0000-0000-000000000001','TASK','50000000-0000-0000-0000-000000000010','TASK_CREATED','10000000-0000-0000-0000-000000000003',NULL,'{"status":"TODO"}',now()-interval '6 days'),
('90000000-0000-0000-0000-000000000014','20000000-0000-0000-0000-000000000001','TASK','50000000-0000-0000-0000-000000000010','TASK_STATUS_CHANGED','10000000-0000-0000-0000-000000000005','{"status":"TODO"}','{"status":"IN_PROGRESS"}',now()-interval '4 days'),
('90000000-0000-0000-0000-000000000015','20000000-0000-0000-0000-000000000001','TASK','50000000-0000-0000-0000-000000000010','TASK_STATUS_CHANGED','10000000-0000-0000-0000-000000000005','{"status":"IN_PROGRESS"}','{"status":"IN_REVIEW"}',now()-interval '1 day'),
('90000000-0000-0000-0000-000000000016','20000000-0000-0000-0000-000000000001','BUG','80000000-0000-0000-0000-000000000001','BUG_CREATED','10000000-0000-0000-0000-000000000006',NULL,'{"status":"OPEN","severity":"CRITICAL"}',now()-interval '3 days'),
('90000000-0000-0000-0000-000000000017','20000000-0000-0000-0000-000000000001','TIME_LOG','61000000-0000-0000-0000-000000000008','TIME_LOG_CREATED','10000000-0000-0000-0000-000000000005',NULL,'{"minutes":240,"workDate":"today"}',now()-interval '1 day')
ON CONFLICT DO NOTHING;

INSERT INTO notifications (id, type, title, content, actor_user_id, project_id, entity_type, entity_id, created_at, dedup_key, target_url)
VALUES
('a0000000-0000-0000-0000-000000000001','TASK_ASSIGNED','Bạn được giao một Task','Task Refresh token tự động đã được giao cho bạn.','10000000-0000-0000-0000-000000000002','20000000-0000-0000-0000-000000000001','TASK','50000000-0000-0000-0000-000000000002',now()-interval '2 days','demo-task-assigned-2','/projects/20000000-0000-0000-0000-000000000001/tasks/50000000-0000-0000-0000-000000000002'),
('a0000000-0000-0000-0000-000000000002','TASK_BLOCKED','Task đang bị chặn','Task Xử lý xung đột vị trí Kanban đang chờ xác nhận.','10000000-0000-0000-0000-000000000005','20000000-0000-0000-0000-000000000001','TASK','50000000-0000-0000-0000-000000000004',now()-interval '1 day','demo-task-blocked-4','/projects/20000000-0000-0000-0000-000000000001/tasks/50000000-0000-0000-0000-000000000004'),
('a0000000-0000-0000-0000-000000000003','BUG_CRITICAL','Có Bug nghiêm trọng','Bug refresh token hai tab cần được ưu tiên xử lý.','10000000-0000-0000-0000-000000000006','20000000-0000-0000-0000-000000000001','BUG','80000000-0000-0000-0000-000000000001',now()-interval '20 hours','demo-bug-critical-1','/projects/20000000-0000-0000-0000-000000000001/bugs/80000000-0000-0000-0000-000000000001'),
('a0000000-0000-0000-0000-000000000004','SPRINT_ENDING_SOON','Sprint sắp kết thúc','Sprint 02 còn một ngày và vẫn có Task chưa hoàn thành.','10000000-0000-0000-0000-000000000002','20000000-0000-0000-0000-000000000001','SPRINT','30000000-0000-0000-0000-000000000002',now()-interval '4 hours','demo-sprint-ending-2','/projects/20000000-0000-0000-0000-000000000001/sprints/30000000-0000-0000-0000-000000000002')
ON CONFLICT DO NOTHING;

INSERT INTO notification_recipients (id, notification_id, user_id, delivered_at, read_at)
VALUES
('b0000000-0000-0000-0000-000000000001','a0000000-0000-0000-0000-000000000001','10000000-0000-0000-0000-000000000004',now()-interval '2 days',now()-interval '1 day'),
('b0000000-0000-0000-0000-000000000002','a0000000-0000-0000-0000-000000000002','10000000-0000-0000-0000-000000000002',now()-interval '1 day',NULL),
('b0000000-0000-0000-0000-000000000003','a0000000-0000-0000-0000-000000000002','10000000-0000-0000-0000-000000000003',now()-interval '1 day',NULL),
('b0000000-0000-0000-0000-000000000004','a0000000-0000-0000-0000-000000000003','10000000-0000-0000-0000-000000000004',now()-interval '20 hours',NULL),
('b0000000-0000-0000-0000-000000000005','a0000000-0000-0000-0000-000000000003','10000000-0000-0000-0000-000000000006',now()-interval '20 hours',now()-interval '18 hours'),
('b0000000-0000-0000-0000-000000000006','a0000000-0000-0000-0000-000000000004','10000000-0000-0000-0000-000000000002',now()-interval '4 hours',NULL),
('b0000000-0000-0000-0000-000000000007','a0000000-0000-0000-0000-000000000004','10000000-0000-0000-0000-000000000003',now()-interval '4 hours',NULL)
ON CONFLICT DO NOTHING;

INSERT INTO system_audit_logs (id, actor_user_id, action, resource_type, resource_id, ip_address, user_agent, old_value_json, new_value_json, success, error_message, created_at)
VALUES
('c0000000-0000-0000-0000-000000000001','10000000-0000-0000-0000-000000000001','LOGIN_SUCCESS','AUTH',NULL,'127.0.0.1','HICAS Demo Browser',NULL,'{"email":"manager@hicas.demo"}',TRUE,NULL,now()-interval '2 days'),
('c0000000-0000-0000-0000-000000000002',NULL,'LOGIN_FAILED','AUTH',NULL,'127.0.0.1','HICAS Demo Browser',NULL,'{"email":"unknown@hicas.demo"}',FALSE,'Sai thông tin đăng nhập',now()-interval '1 day'),
('c0000000-0000-0000-0000-000000000003','10000000-0000-0000-0000-000000000002','IMPORT_COMPLETED','TASK_IMPORT_BATCH','62000000-0000-0000-0000-000000000001','127.0.0.1','HICAS Demo Browser',NULL,'{"successRows":12,"failedRows":0}',TRUE,NULL,now()-interval '5 days'),
('c0000000-0000-0000-0000-000000000004','10000000-0000-0000-0000-000000000002','IMPORT_VALIDATION_FAILED','TASK_IMPORT_BATCH','62000000-0000-0000-0000-000000000002','127.0.0.1','HICAS Demo Browser',NULL,'{"successRows":0,"failedRows":3}',FALSE,'File import có dữ liệu không hợp lệ',now()-interval '4 days'),
('c0000000-0000-0000-0000-000000000005','10000000-0000-0000-0000-000000000001','ADMIN_VIEWED_DASHBOARD','SYSTEM',NULL,'127.0.0.1','HICAS Demo Browser',NULL,NULL,TRUE,NULL,now()-interval '3 hours')
ON CONFLICT DO NOTHING;

INSERT INTO audit_logs (id, user_id, action, created_at)
VALUES
('d0000000-0000-0000-0000-000000000001','10000000-0000-0000-0000-000000000001','LOGIN',now()-interval '2 days'),
('d0000000-0000-0000-0000-000000000002','10000000-0000-0000-0000-000000000002','CREATE_PROJECT',now()-interval '90 days')
ON CONFLICT DO NOTHING;
