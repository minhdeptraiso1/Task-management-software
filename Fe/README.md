# HICAS Task Management Frontend

Frontend React 19 + TypeScript + Vite + Tailwind CSS cho HICAS Task Management.

## Chạy local

```bash
npm ci
npm run dev
```

Mặc định Vite chạy tại `http://localhost:5173`. Sao chép `.env.example` thành `.env` khi cần thay đổi cấu hình API.

## Build production

```bash
npm run build
```

Artifact được tạo trong `dist`. Docker image dùng Nginx unprivileged, sinh cấu hình proxy từ `nginx.conf.template` khi container khởi động.

Các biến chính:

- `VITE_API_URL`: build-time, Docker full stack nên giữ `/api`.
- `PORT`: cổng Nginx runtime, mặc định `8080`.
- `NGINX_BACKEND_URL`: URL backend runtime.
- `NGINX_BACKEND_HOST`: host dùng cho DNS resolver của Nginx.
- `NGINX_CLIENT_MAX_BODY_SIZE`: giới hạn request upload.

## Docker

Frontend được chạy cùng backend từ `TodoListProject/docker-compose.yaml`:

```bash
cd ../TodoListProject
docker compose up -d --build
```

Xem hướng dẫn tổng thể tại [README dự án](../README.md) và [README triển khai](../TodoListProject/README_DEPLOY.md).
