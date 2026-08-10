FROM node:22-alpine AS build

WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY . .

ARG VITE_API_URL=/api
ENV VITE_API_URL=$VITE_API_URL

RUN npm run build

FROM nginxinc/nginx-unprivileged:1.27-alpine

ENV PORT=8080 \
    NGINX_SERVER_NAME=_ \
    NGINX_BACKEND_URL=http://backend:8080 \
    NGINX_BACKEND_HOST=backend \
    NGINX_CLIENT_MAX_BODY_SIZE=10m

COPY nginx.conf.template /etc/nginx/templates/default.conf.template
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=5 \
  CMD wget --quiet --spider http://127.0.0.1:${PORT}/healthz || exit 1
