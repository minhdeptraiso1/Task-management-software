#!/usr/bin/env bash
set -euo pipefail

UPLOAD_BACKUP_MODE=${UPLOAD_BACKUP_MODE:-docker}
UPLOAD_PATH=${UPLOAD_PATH:-uploads}
UPLOAD_CONTAINER_PATH=${UPLOAD_CONTAINER_PATH:-/app/uploads}
BACKEND_CONTAINER=${BACKEND_CONTAINER:-task_management_backend}
BACKUP_DIR=${BACKUP_DIR:-backups}
BACKUP_TIMESTAMP=${BACKUP_TIMESTAMP:-$(date +"%Y%m%d_%H%M%S")}

OUTPUT_DIR="$BACKUP_DIR/uploads"
OUTPUT_FILE="$OUTPUT_DIR/uploads_${BACKUP_TIMESTAMP}.tar.gz"
mkdir -p "$OUTPUT_DIR"

case "$UPLOAD_BACKUP_MODE" in
  docker)
    echo "[BACKUP-UPLOADS] Backing up '$BACKEND_CONTAINER:$UPLOAD_CONTAINER_PATH'"
    docker exec "$BACKEND_CONTAINER" mkdir -p "$UPLOAD_CONTAINER_PATH"
    docker exec "$BACKEND_CONTAINER" tar -C "$UPLOAD_CONTAINER_PATH" -czf - . > "$OUTPUT_FILE"
    ;;
  local)
    mkdir -p "$UPLOAD_PATH"
    echo "[BACKUP-UPLOADS] Backing up local folder '$UPLOAD_PATH'"
    tar -C "$UPLOAD_PATH" -czf "$OUTPUT_FILE" .
    ;;
  *)
    echo "[BACKUP-UPLOADS] UPLOAD_BACKUP_MODE must be 'docker' or 'local'" >&2
    exit 1
    ;;
esac

if [ ! -s "$OUTPUT_FILE" ]; then
  echo "[BACKUP-UPLOADS] Backup file is empty: $OUTPUT_FILE" >&2
  exit 1
fi

tar -tzf "$OUTPUT_FILE" > /dev/null
echo "[BACKUP-UPLOADS] Created: $OUTPUT_FILE"
