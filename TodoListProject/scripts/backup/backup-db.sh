#!/usr/bin/env bash
set -euo pipefail

POSTGRES_CONTAINER=${POSTGRES_CONTAINER:-task_management_postgres}
POSTGRES_DB=${POSTGRES_DB:-${DB_NAME:-}}
POSTGRES_USER=${POSTGRES_USER:-${DB_USERNAME:-}}
BACKUP_DIR=${BACKUP_DIR:-backups}
BACKUP_TIMESTAMP=${BACKUP_TIMESTAMP:-$(date +"%Y%m%d_%H%M%S")}

if [ -z "$POSTGRES_DB" ]; then
  POSTGRES_DB=$(docker exec "$POSTGRES_CONTAINER" printenv POSTGRES_DB 2>/dev/null || true)
fi
if [ -z "$POSTGRES_USER" ]; then
  POSTGRES_USER=$(docker exec "$POSTGRES_CONTAINER" printenv POSTGRES_USER 2>/dev/null || true)
fi
POSTGRES_DB=${POSTGRES_DB:-task_management}
POSTGRES_USER=${POSTGRES_USER:-task_user}

OUTPUT_DIR="$BACKUP_DIR/db"
OUTPUT_FILE="$OUTPUT_DIR/${POSTGRES_DB}_${BACKUP_TIMESTAMP}.sql.gz"

mkdir -p "$OUTPUT_DIR"

echo "[BACKUP-DB] Backing up database '$POSTGRES_DB' from '$POSTGRES_CONTAINER'"
docker exec "$POSTGRES_CONTAINER" pg_dump \
  -U "$POSTGRES_USER" \
  -d "$POSTGRES_DB" \
  --clean \
  --if-exists \
  --no-owner \
  --no-privileges \
  | gzip > "$OUTPUT_FILE"

if [ ! -s "$OUTPUT_FILE" ]; then
  echo "[BACKUP-DB] Backup file is empty: $OUTPUT_FILE" >&2
  exit 1
fi

gzip -t "$OUTPUT_FILE"
echo "[BACKUP-DB] Created: $OUTPUT_FILE"
