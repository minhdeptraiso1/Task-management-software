#!/usr/bin/env bash
set -euo pipefail

BACKUP_DIR=${BACKUP_DIR:-backups}
BACKUP_RETENTION_DAYS=${BACKUP_RETENTION_DAYS:-14}

if ! [[ "$BACKUP_RETENTION_DAYS" =~ ^[0-9]+$ ]]; then
  echo "[BACKUP-CLEANUP] BACKUP_RETENTION_DAYS must be a non-negative integer" >&2
  exit 1
fi

echo "[BACKUP-CLEANUP] Removing backup files older than $BACKUP_RETENTION_DAYS days"
if [ -d "$BACKUP_DIR" ]; then
  find "$BACKUP_DIR" \
    -type f \
    \( -name "*.sql.gz" -o -name "*.tar.gz" \) \
    -mtime +"$BACKUP_RETENTION_DAYS" \
    -print \
    -delete
fi

echo "[BACKUP-CLEANUP] Cleanup completed"
