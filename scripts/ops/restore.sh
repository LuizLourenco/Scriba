#!/usr/bin/env bash
set -euo pipefail

BACKUP_FILE="${1:?usage: scripts/ops/restore.sh backups/scriba_YYYYMMDDTHHMMSSZ.sql.gz}"

gzip -t "$BACKUP_FILE"
gunzip -c "$BACKUP_FILE" | docker compose -f docker-compose.prod.yml exec -T db-prod \
    mariadb -u"${DB_USERNAME:?DB_USERNAME is required}" -p"${DB_PASSWORD:?DB_PASSWORD is required}" "${DB_NAME:?DB_NAME is required}"
