#!/usr/bin/env bash
set -euo pipefail

BACKUP_DIR="${BACKUP_DIR:-./backups}"
TIMESTAMP="$(date -u +%Y%m%dT%H%M%SZ)"
BACKUP_FILE="${BACKUP_DIR}/scriba_${TIMESTAMP}.sql.gz"

mkdir -p "$BACKUP_DIR"

docker compose -f docker-compose.prod.yml exec -T db-prod \
    mariadb-dump -u"${DB_USERNAME:?DB_USERNAME is required}" -p"${DB_PASSWORD:?DB_PASSWORD is required}" "${DB_NAME:?DB_NAME is required}" \
    | gzip > "$BACKUP_FILE"

gzip -t "$BACKUP_FILE"
ln -sfn "$(basename "$BACKUP_FILE")" "${BACKUP_DIR}/scriba_latest.sql.gz"
echo "$BACKUP_FILE"
