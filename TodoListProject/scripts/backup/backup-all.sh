#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" > /dev/null 2>&1 && pwd)
export BACKUP_TIMESTAMP=${BACKUP_TIMESTAMP:-$(date +"%Y%m%d_%H%M%S")}

echo "[BACKUP-ALL] Starting backup set: $BACKUP_TIMESTAMP"
bash "$SCRIPT_DIR/backup-db.sh"
bash "$SCRIPT_DIR/backup-uploads.sh"
bash "$SCRIPT_DIR/cleanup-old-backups.sh"
echo "[BACKUP-ALL] Backup set completed"
