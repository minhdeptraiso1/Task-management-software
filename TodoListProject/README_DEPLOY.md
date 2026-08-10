# Triển khai HICAS Task Management

## 1. Yêu cầu

- Docker Desktop hoặc Docker Engine có Docker Compose V2.
- Tối thiểu 4 GB RAM trống cho frontend, backend, PostgreSQL và Redis.
- Không cần cài Java hoặc Node.js khi chạy toàn bộ bằng Docker.

## 2. Cấu hình môi trường

Tại thư mục `TodoListProject`, sao chép `.env.example` thành `.env`:

```bash
cp .env.example .env
```

Bắt buộc đổi các giá trị sau trước khi deploy:

```env
DB_PASSWORD=mat_khau_database_manh
JWT_SECRET=chuoi_ngau_nhien_toi_thieu_32_ky_tu
```

Không commit `.env`, database dump hoặc thư mục upload lên Git.

## 3. Chạy full stack

```bash
docker compose up -d --build
docker compose ps
```

Địa chỉ mặc định:

- Frontend: `http://localhost:5173`
- Backend trực tiếp: `http://localhost:8080/api`
- Swagger demo: `http://localhost:8080/api/swagger-ui/index.html`
- Health qua frontend proxy: `http://localhost:5173/api/actuator/health`
- Health backend trực tiếp: `http://localhost:8080/api/actuator/health`

Frontend Nginx proxy `/api` và `/api/ws` vào backend trong Docker network, vì vậy trình duyệt sử dụng cùng origin và WebSocket không cần địa chỉ backend riêng.

Nginx được sinh từ `Fe/nginx.conf.template` mỗi lần container khởi động. Muốn đổi backend không cần sửa hoặc build lại frontend image; cập nhật `.env` rồi recreate container frontend:

```env
# Backend cùng Docker Compose
NGINX_BACKEND_URL=http://backend:8080
NGINX_BACKEND_HOST=backend

# Hoặc backend HTTPS bên ngoài
# NGINX_BACKEND_URL=https://task-management-software-q6r5.onrender.com
# NGINX_BACKEND_HOST=task-management-software-q6r5.onrender.com
```

```bash
docker compose up -d --no-deps --force-recreate frontend
```

`VITE_API_URL` vẫn là biến build-time của Vite và mặc định là `/api`. Với Docker deployment nên giữ `/api`; biến runtime của Nginx phía trên sẽ chọn backend thật.

Template cũng đọc biến `PORT` khi container khởi động. Full stack gán `PORT` từ `FRONTEND_CONTAINER_PORT` và mặc định là `8080`. Khi dùng Caddy hoặc Cloudflare Tunnel, giữ cổng nội bộ `8080` để route `frontend:8080` tiếp tục hoạt động; khi deploy frontend độc lập trên Render/Railway có thể dùng `PORT` do nền tảng cấp.

### Mẫu env khi deploy frontend và backend riêng

- Backend: copy `TodoListProject/.env.backend.example` và khai báo các biến tương ứng trên dịch vụ backend.
- Frontend Docker: tham khảo `Fe/.env.example`. `VITE_API_URL=/api` là build-time; các biến `NGINX_*` và `PORT` là runtime.
- Full stack Docker/VPS: copy `TodoListProject/.env.example` thành `.env`.
- Production có Caddy: copy `TodoListProject/.env.prod.example` thành `.env.prod`.

Các secret và kết nối production như `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `REDIS_HOST`, `JWT_SECRET`, `FILE_STORAGE_ROOT_PATH` và `CORS_ALLOWED_ORIGINS` không có fallback trong profile `prod`, vì vậy backend sẽ dừng sớm nếu cấu hình thiếu. Các giá trị vận hành không nhạy cảm như cache TTL, giới hạn file và batch size vẫn có default an toàn trong `application.yaml`, nhưng đã được liệt kê đầy đủ trong env mẫu để có thể điều chỉnh mà không sửa mã nguồn.

## 4. Tài khoản demo

Profile `docker` nạp dữ liệu demo. Mật khẩu dùng chung cho các tài khoản dưới đây là `123456`:

- Manager: `manager@hicas.demo`
- Developer: `dev.minh@hicas.demo`
- QA: `qa.linh@hicas.demo`

Không bật dữ liệu demo trong profile production.

## 5. Kiểm tra sau khi chạy

```bash
curl http://localhost:5173/healthz
curl http://localhost:5173/api/actuator/health
curl http://localhost:5173/api/actuator/info
docker compose ps
```

Checklist chức năng:

1. Đăng nhập và refresh token hoạt động.
2. Mở danh sách Project, Sprint và Kanban.
3. Tạo hoặc cập nhật Task và kiểm tra realtime ở trình duyệt thứ hai.
4. Upload rồi tải lại attachment.
5. Xuất thử báo cáo Excel/PDF.
6. Trang Admin đọc được health/info; endpoint Actuator nhạy cảm vẫn yêu cầu xác thực.

## 6. Log và restart

```bash
docker compose logs -f backend
docker compose logs -f frontend
docker compose restart backend frontend
```

Restart không xóa named volume PostgreSQL, Redis hoặc upload.

## 7. Backup trước deploy

```bash
bash scripts/backup/backup-all.sh
```

Xem quy trình đầy đủ tại `docs/backup-restore.md`.

## 8. Dừng hệ thống

```bash
docker compose down
```

Không dùng `docker compose down -v` nếu cần giữ dữ liệu. Tùy chọn `-v` sẽ xóa named volume database, Redis và upload.

## 9. Production

- Dùng profile `prod`, secret từ secret manager hoặc biến môi trường của máy chủ.
- Không bật Swagger, demo migration hoặc health details.
- Đặt reverse proxy TLS phía trước frontend.
- Chỉ expose frontend ra Internet; giới hạn port PostgreSQL, Redis và backend bằng firewall hoặc bỏ host port khi không cần vận hành trực tiếp.

## 10. Gắn domain và HTTPS production

Cấu hình có sẵn cho domain `hoangitk.io.vn` nằm trong `docker-compose.prod.yaml` và `Caddyfile`. Caddy là reverse proxy ngoài cùng, tự xin và gia hạn chứng chỉ HTTPS. Các service PostgreSQL, Redis, backend và frontend không public cổng trực tiếp ra Internet.

### 10.1. Trỏ DNS

Trong trang quản lý DNS của tên miền, tạo:

| Loại | Tên | Giá trị |
| --- | --- | --- |
| `A` | `@` | IP public IPv4 của VPS |
| `CNAME` | `www` | `hoangitk.io.vn` |

Chỉ tạo bản ghi `AAAA` khi VPS đã có IPv6. Xóa bản ghi `A`/`AAAA` cũ bị trùng nếu chúng trỏ sang máy chủ khác.

### 10.2. Chuẩn bị VPS

Mở inbound TCP `80` và `443` trên firewall/security group. Nếu dùng UFW:

```bash
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
```

Không mở `5432`, `6379`, `8080` hoặc `5173` ra Internet.

### 10.3. Tạo biến môi trường production

```bash
cp .env.prod.example .env.prod
```

Sửa `ACME_EMAIL`, `DB_PASSWORD` và `JWT_SECRET` thành giá trị thật. Không commit `.env.prod`.

### 10.4. Khởi động

```bash
docker compose --env-file .env.prod -f docker-compose.prod.yaml up -d --build
docker compose --env-file .env.prod -f docker-compose.prod.yaml ps
```

Sau khi DNS đã cập nhật, truy cập:

```text
https://hoangitk.io.vn
https://hoangitk.io.vn/api/actuator/health
```

Kiểm tra log cấp chứng chỉ nếu HTTPS chưa hoạt động:

```bash
docker compose --env-file .env.prod -f docker-compose.prod.yaml logs -f caddy
```

DNS phải trỏ đúng IP VPS và cổng 80/443 phải truy cập được từ Internet thì Let's Encrypt mới cấp chứng chỉ.

## 11. Dùng máy local làm máy chủ thử nghiệm

Khuyến nghị dùng Cloudflare Tunnel thay vì mở port modem. Tunnel tạo kết nối outbound từ máy local đến Cloudflare nên không yêu cầu IP tĩnh, port forwarding hoặc public IP. Theo tài liệu Cloudflare, tunnel token là thông tin bí mật: người có token có thể chạy connector cho tunnel đó.

### 11.1. Đưa domain vào Cloudflare

1. Tạo tài khoản Cloudflare và thêm zone `hoangitk.io.vn`.
2. Cloudflare cung cấp hai nameserver.
3. Vào nơi mua domain và thay nameserver hiện tại bằng hai nameserver Cloudflare cung cấp.
4. Chờ trạng thái zone chuyển thành `Active`.

Không cần tạo bản ghi `A` trỏ về IP mạng nhà khi dùng Tunnel.

### 11.2. Tạo tunnel

Trong Cloudflare Dashboard:

1. Mở `Networking` > `Tunnels`.
2. Chọn tạo Cloudflare Tunnel, đặt tên `hicas-local`.
3. Chọn môi trường Docker và sao chép chuỗi token bắt đầu bằng `eyJ...`.
4. Mở tunnel, thêm Public Hostname:

| Hostname | Service |
| --- | --- |
| `hoangitk.io.vn` | `http://frontend:8080` |
| `www.hoangitk.io.vn` | `http://frontend:8080` |

`frontend` là tên service trong Docker network, không dùng `localhost:5173` ở ô Service.

### 11.3. Chạy tunnel cùng hệ thống

Thêm token vào file `.env` đang dùng ở máy local:

```env
CLOUDFLARE_TUNNEL_TOKEN=eyJ...token_that...
```

Không gửi hoặc commit token. Khởi động:

```bash
docker compose -f docker-compose.yaml -f docker-compose.tunnel.yaml up -d --build
docker compose -f docker-compose.yaml -f docker-compose.tunnel.yaml ps
docker compose -f docker-compose.yaml -f docker-compose.tunnel.yaml logs -f cloudflared
```

Khi tunnel báo `Connected`, kiểm tra:

```text
https://hoangitk.io.vn
https://hoangitk.io.vn/api/actuator/health
```

Máy local phải bật Docker và có Internet thì website mới hoạt động. Khi chuyển sang VPS, dừng tunnel và dùng lại `docker-compose.prod.yaml` ở mục 10.
