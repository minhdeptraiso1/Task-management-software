# Backup và khôi phục dữ liệu

Hệ thống phải sao lưu đồng thời PostgreSQL và file upload. Metadata attachment nằm trong database, còn nội dung file nằm trong Docker named volume `/app/uploads`.

## Cấu hình

Sao chép `.env.example` thành `.env` và cấu hình ít nhất:

```env
DB_NAME=task_management
DB_USERNAME=task_user
POSTGRES_CONTAINER=task_management_postgres
BACKEND_CONTAINER=task_management_backend
BACKUP_DIR=backups
BACKUP_RETENTION_DAYS=14
UPLOAD_BACKUP_MODE=docker
UPLOAD_CONTAINER_PATH=/app/uploads
```

Script không đọc hoặc hard-code mật khẩu database. Nếu lệnh gọi không truyền `POSTGRES_DB`/`POSTGRES_USER`, script tự đọc hai giá trị này từ PostgreSQL container. `pg_dump` và `psql` chạy bên trong container bằng tài khoản đã được Docker Compose cấu hình.

## Sao lưu trước khi deploy

```bash
bash scripts/backup/backup-all.sh
```

Kiểm tra trước khi tiếp tục deploy:

- Có cả file `backups/db/*.sql.gz` và `backups/uploads/*.tar.gz` cùng timestamp.
- Hai file có dung lượng lớn hơn 0 và kiểm tra archive không báo lỗi.
- Sao chép bộ backup ra ngoài máy chủ nếu có thể.
- Ghi lại timestamp của bộ backup được chọn để rollback.
- Chỉ pull, build và deploy sau khi các bước trên đạt.

Backup cũ hơn `BACKUP_RETENTION_DAYS` được dọn sau mỗi lần chạy `backup-all.sh`.

## Khôi phục khi deploy lỗi

Khôi phục sẽ ghi đè database và nội dung upload hiện tại. Luôn chọn hai file có cùng timestamp.

```bash
docker compose stop backend
bash scripts/backup/restore-db.sh backups/db/task_management_YYYYMMDD_HHMMSS.sql.gz
docker compose up -d backend
bash scripts/backup/restore-uploads.sh backups/uploads/uploads_YYYYMMDD_HHMMSS.tar.gz
```

Checklist sau restore:

1. `GET http://localhost:8080/api/actuator/health` trả `UP`.
2. Đăng nhập được bằng tài khoản hợp lệ.
3. API Project, Sprint và Task trả dữ liệu đúng.
4. Tải thử ít nhất một attachment có trong bộ backup.
5. Đối chiếu file có metadata nhưng thiếu vật lý và file vật lý không còn metadata.

Không xóa file orphan tự động trong script restore. Việc cleanup vật lý phải chạy bằng job riêng sau khi hết thời gian rollback.

## Chạy ngoài Docker

Để backup/restore thư mục local thay cho named volume:

```bash
UPLOAD_BACKUP_MODE=local UPLOAD_PATH=uploads bash scripts/backup/backup-uploads.sh
UPLOAD_BACKUP_MODE=local UPLOAD_PATH=uploads bash scripts/backup/restore-uploads.sh <archive.tar.gz>
```
