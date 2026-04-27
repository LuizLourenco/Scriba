#!/usr/bin/env bash
set -euo pipefail

docker compose -f docker-compose.prod.yml kill db-prod
sleep 10
docker compose -f docker-compose.prod.yml start db-prod
sleep 20
docker compose -f docker-compose.prod.yml ps
