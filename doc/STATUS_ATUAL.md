# Status Atual do Scriba

Data da análise: 2026-04-26

## Sincronização Git

- Branch local: `main`
- Remoto: `origin/main`
- Divergência após `git fetch --prune origin`: `0` commits locais e `0` commits remotos pendentes.
- Há alterações locais não commitadas que representam o trabalho em andamento.

## Validação Técnica

- `./mvnw test`: 44 testes, 0 falhas, 0 erros.
- `./mvnw -DskipTests package`: `BUILD SUCCESS`.
- `docker compose -f docker-compose.prod.yml --env-file .env.prod.example config`: configuração resolvida com perfil `prod`.
- `docker build -t scriba:local .`: imagem construída com sucesso.
- `docker compose -f docker-compose.prod.yml --env-file .env.prod.example up -d`: stack iniciou com MariaDB, aplicação e NGINX.
- `curl -i http://localhost:8080/actuator/health`: retorna `401` sem autenticação.
- Smoke test encerrado com `docker compose -f docker-compose.prod.yml --env-file .env.prod.example down`.

## Fase Atual do Roteiro

As fases 1 a 8 estão implementadas em nível funcional e cobertas pela suíte atual:

- CORE, RBAC e tenant isolation.
- Administração, pessoas, catálogo, acervo, circulação e curadoria.
- Portal do leitor, notificações e relatório operacional.

O projeto está entrando na Fase 9: hardening e pré-piloto.

## Itens Implementados da Fase 9

- `Dockerfile` para empacotamento da aplicação.
- `docker-compose.prod.yml` com app, MariaDB e NGINX.
- NGINX com rate limit básico no `/login`.
- Configuração MariaDB de produção em `docker/mariadb/my.prod.cnf`.
- `application-prod.yaml` endurecido com:
  - `scriba.tenant.strict-mode=true`;
  - `spring.jpa.hibernate.ddl-auto=validate`;
  - `spring.flyway.clean-disabled=true`;
  - Thymeleaf cacheado;
  - logs estruturados no console.
- `/actuator/**` protegido por `ROLE_ADMIN`, com resposta `401` para acesso não autenticado.
- Health check de e-mail desabilitado para o `/actuator/health` refletir saúde de app/db/disk sem depender de SMTP externo.
- Scripts operacionais:
  - `scripts/ops/start-prod.sh`;
  - `scripts/ops/rollback.sh`;
  - `scripts/ops/backup-now.sh`;
  - `scripts/ops/restore.sh`;
  - `scripts/ops/chaos-test.sh`.
- `Makefile` com comandos de teste, produção, backup, restore e chaos.
- Script base de carga em `scripts/load/emprestimo.js`.

## Pendências Para Conclusão

- Validar ambiente real de staging com TLS.
- Trocar todas as senhas de exemplo antes de qualquer piloto.
- Executar backup e restore com banco real e comparar contagens críticas.
- Executar teste de carga com k6 usando fluxo autenticado real de empréstimo.
- Executar chaos test com aplicação sob carga leve.
- Homologar runbooks de start, rollback, backup e restore em dry-run.
- Configurar alertas externos para heap, erros 5xx e falha de backup.

## Próximo Gate

O próximo gate é a homologação operacional da Fase 9. O código está verde, mas o projeto só deve ser considerado pronto para piloto depois das validações de staging, carga, backup/restore e chaos.
