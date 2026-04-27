COMPOSE_PROD=docker compose -f docker-compose.prod.yml
BACKUP_DIR?=./backups

.PHONY: test verify prod-up prod-down prod-logs prod-db-backup prod-db-restore prod-chaos

test:
	./mvnw test

verify:
	./mvnw verify -Dspring.profiles.active=test

prod-up:
	$(COMPOSE_PROD) up -d --build

prod-down:
	$(COMPOSE_PROD) down

prod-logs:
	$(COMPOSE_PROD) logs -f app

prod-db-backup:
	BACKUP_DIR=$(BACKUP_DIR) scripts/ops/backup-now.sh

prod-db-restore:
	scripts/ops/restore.sh $(BACKUP_FILE)

prod-chaos:
	scripts/ops/chaos-test.sh
