#!/usr/bin/env bash
set -euo pipefail

POSTGRES_CONTAINER=${POSTGRES_CONTAINER:-task_management_postgres}
POSTGRES_DB=${POSTGRES_DB:-${DB_NAME:-}}
POSTGRES_USER=${POSTGRES_USER:-${DB_USERNAME:-}}
BACKUP_FILE=${1:-}

if [ -z "$POSTGRES_DB" ]; then
  POSTGRES_DB=$(docker exec "$POSTGRES_CONTAINER" printenv POSTGRES_DB 2>/dev/null || true)
fi
if [ -z "$POSTGRES_USER" ]; then
  POSTGRES_USER=$(docker exec "$POSTGRES_CONTAINER" printenv POSTGRES_USER 2>/dev/null || true)
fi
POSTGRES_DB=${POSTGRES_DB:-task_management}
POSTGRES_USER=${POSTGRES_USER:-task_user}

if [ -z "$BACKUP_FILE" ]; then
  echo "Usage: $0 <backup-file.sql.gz>" >&2
  exit 1
fi

if [ ! -f "$BACKUP_FILE" ]; then
  echo "[RESTORE-DB] Backup file not found: $BACKUP_FILE" >&2
  exit 1
fi

gzip -t "$BACKUP_FILE"

echo "[RESTORE-DB] Restoring '$BACKUP_FILE' into database '$POSTGRES_DB'"
gunzip -c "$BACKUP_FILE" | docker exec -i "$POSTGRES_CONTAINER" \
  psql \
  -v ON_ERROR_STOP=1 \
  -U "$POSTGRES_USER" \
  -d "$POSTGRES_DB"

echo "[RESTORE-DB] Restore completed"
