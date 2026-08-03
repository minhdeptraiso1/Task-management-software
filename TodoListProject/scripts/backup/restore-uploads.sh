#!/usr/bin/env bash
set -euo pipefail

UPLOAD_BACKUP_MODE=${UPLOAD_BACKUP_MODE:-docker}
UPLOAD_PATH=${UPLOAD_PATH:-uploads}
UPLOAD_CONTAINER_PATH=${UPLOAD_CONTAINER_PATH:-/app/uploads}
BACKEND_CONTAINER=${BACKEND_CONTAINER:-task_management_backend}
BACKUP_FILE=${1:-}

if [ -z "$BACKUP_FILE" ]; then
  echo "Usage: $0 <uploads-backup.tar.gz>" >&2
  exit 1
fi

if [ ! -f "$BACKUP_FILE" ]; then
  echo "[RESTORE-UPLOADS] Backup file not found: $BACKUP_FILE" >&2
  exit 1
fi

tar -tzf "$BACKUP_FILE" > /dev/null
if tar -tzf "$BACKUP_FILE" | grep -Eq '(^/|(^|/)\.\.(/|$))'; then
  echo "[RESTORE-UPLOADS] Unsafe path detected in archive" >&2
  exit 1
fi

case "$UPLOAD_BACKUP_MODE" in
  docker)
    if [ -z "$UPLOAD_CONTAINER_PATH" ] || [ "$UPLOAD_CONTAINER_PATH" = "/" ]; then
      echo "[RESTORE-UPLOADS] Unsafe UPLOAD_CONTAINER_PATH" >&2
      exit 1
    fi
    echo "[RESTORE-UPLOADS] Restoring into '$BACKEND_CONTAINER:$UPLOAD_CONTAINER_PATH'"
    docker exec "$BACKEND_CONTAINER" sh -c 'mkdir -p "$1" && find "$1" -mindepth 1 -maxdepth 1 -exec rm -rf -- {} +' sh "$UPLOAD_CONTAINER_PATH"
    docker exec -i "$BACKEND_CONTAINER" tar -C "$UPLOAD_CONTAINER_PATH" -xzf - < "$BACKUP_FILE"
    ;;
  local)
    if [ -z "$UPLOAD_PATH" ] || [ "$UPLOAD_PATH" = "/" ] || [ "$UPLOAD_PATH" = "." ]; then
      echo "[RESTORE-UPLOADS] Unsafe UPLOAD_PATH" >&2
      exit 1
    fi
    echo "[RESTORE-UPLOADS] Restoring into local folder '$UPLOAD_PATH'"
    mkdir -p "$UPLOAD_PATH"
    find "$UPLOAD_PATH" -mindepth 1 -maxdepth 1 -exec rm -rf -- {} +
    tar -C "$UPLOAD_PATH" -xzf "$BACKUP_FILE"
    ;;
  *)
    echo "[RESTORE-UPLOADS] UPLOAD_BACKUP_MODE must be 'docker' or 'local'" >&2
    exit 1
    ;;
esac

echo "[RESTORE-UPLOADS] Restore completed"
