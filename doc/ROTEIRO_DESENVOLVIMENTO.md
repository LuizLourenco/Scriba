# Scriba — Roteiro de Desenvolvimento por Fases

> **Guia de execução em formato Perguntas & Respostas**
> Projeto: Scriba — Sistema de Gestão de Acervo para Instituições Comunitárias
> Stack: Java 25 · Spring Boot 4.0.0 · MariaDB 11.4 LTS · Flyway · Docker Compose
> Pacote raiz: `br.dev.lourenco.scriba`
> Repositório: https://github.com/LuizLourenco/Scriba

Este roteiro é o companheiro operacional do `PROJETO_DA_ARQUITETURA.md`. Enquanto o documento arquitetural descreve **o que** o sistema é, este roteiro descreve **como** construí-lo, fase a fase, com perguntas objetivas, respostas esperadas e comandos prontos para colar no terminal.

---

## Índice

1. [Como Usar Este Roteiro](#1-como-usar-este-roteiro)
2. [Diretriz de Execução](#2-diretriz-de-execução)
3. [Convenções e Legendas](#3-convenções-e-legendas)
4. [Fluxo Padrão de uma Fase](#4-fluxo-padrão-de-uma-fase)
5. [Checklist Transversal de Encerramento de Fase](#5-checklist-transversal-de-encerramento-de-fase)
6. [Roteiro Guiado por Fase](#6-roteiro-guiado-por-fase)
   - [Fase 0 — Preparação de Projeto](#fase-0--preparação-de-projeto)
   - [Fase 1 — Baseline CORE](#fase-1--baseline-core)
   - [Fase 2 — Módulo de Usuários (Administração)](#fase-2--módulo-de-usuários-administração)
   - [Fase 3 — Módulo de Leitores e Pessoas](#fase-3--módulo-de-leitores-e-pessoas)
   - [Fase 4 — Módulo Catálogo](#fase-4--módulo-catálogo)
   - [Fase 5 — Módulo Acervo](#fase-5--módulo-acervo)
   - [Fase 6 — Módulo Circulação](#fase-6--módulo-circulação)
   - [Fase 7 — Módulo Curadoria](#fase-7--módulo-curadoria)
   - [Fase 8 — Portal do Leitor, Notificações e Relatórios](#fase-8--portal-do-leitor-notificações-e-relatórios)
   - [Fase 9 — Hardening e Pré-Piloto](#fase-9--hardening-e-pré-piloto)
7. [Backlog Prioritário Imediato](#7-backlog-prioritário-imediato)
8. [Modelo de Resposta para Condução Assistida](#8-modelo-de-resposta-para-condução-assistida)
9. [Regra Final de Avanço](#9-regra-final-de-avanço)

---

## 1. Como Usar Este Roteiro

Cada fase segue o mesmo esqueleto:

| Seção | Propósito |
|---|---|
| **Objetivo** | O que precisa existir ao final da fase. |
| **Pré-requisitos** | O que precisa estar pronto antes de começar. |
| **Perguntas & Respostas de Validação** | Perguntas objetivas com resposta esperada e comando para obter evidência. |
| **Testes & Comandos** | Instruções executáveis para reproduzir a validação. |
| **Checklist Executável (Scriba)** | Itens específicos do projeto que precisam ficar verdes. |
| **Gate de Saída** | Condição booleana para avançar à próxima fase. |

**Regra de ouro:** não execute validações fora de hora. Primeiro prepara-se o ambiente, depois implementa-se, só então testa-se. Na `Fase 0`, por exemplo, nunca tente rodar teste de integração antes de o Docker estar configurado e o MariaDB respondendo.

---

## 2. Diretriz de Execução

- Construção em fatias evolutivas sobre a base arquitetural definida.
- Cada fase encerra com critérios de aceite **objetivos** — pergunta respondida com evidência.
- Nunca avançar sem validação de segurança, tenant isolation e migrações.
- Toda evidência deve ser reproduzível por outro desenvolvedor com os comandos registrados no PR.

---

## 3. Convenções e Legendas

**Ícones usados nas respostas esperadas:**

- ✅ Resposta afirmativa / comportamento correto.
- ❌ Resposta negativa / comportamento incorreto que deve ser bloqueado.
- ⚠️ Alerta — atenção operacional necessária.
- 🔒 Item de segurança crítico — não avançar sem resolver.

**Formato dos comandos:**

- Prefixados por `$` quando executados no host.
- Prefixados por `mariadb>` quando executados no cliente MariaDB interativo.
- Blocos `Resultado esperado:` descrevem exatamente o que deve aparecer.

**Perfis Spring suportados:**

- `dev` — desenvolvimento local (porta MariaDB 3306).
- `test` — execução de testes (porta MariaDB 3307).
- `prod` — produção (MariaDB apenas em rede interna Docker).

---

## 4. Fluxo Padrão de uma Fase

Para qualquer fase, siga esta ordem:

1. **Entender o objetivo** — ler o bloco `Objetivo` da fase e anotar o que **não** será feito.
2. **Preparar ambiente** — executar checklist de pré-requisitos.
3. **Definir escopo técnico** — listar entidades, migrations, services, DTOs, mappers, controllers, testes.
4. **Implementar em ordem segura** — domínio → migration → regra → segurança → endpoint → erro → teste → documentação.
5. **Executar testes no momento certo** — nunca rodar suíte inteira antes de o módulo estar pronto.
6. **Responder o Q&A de validação** — registrar respostas com evidência.
7. **Preencher o modelo de resposta** (seção 8).
8. **Decidir se pode avançar** (seção 9).

**Ordem de implementação recomendada dentro de cada fase:**

```
1. Domínio (@Entity, enums, value objects)
2. Migration Flyway (V{n}__{descricao}.sql)
3. Repository (interfaces Spring Data)
4. Service (regras de negócio, transações)
5. Segurança (@PreAuthorize, rotas no SecurityConfig)
6. DTO + Mapper (records + MapStruct)
7. Controller (rotas, validação, HTMX)
8. Templates Thymeleaf (list.html, form.html)
9. Tratamento de erro (GlobalExceptionHandler)
10. Testes (integração > web > unitário)
11. Documentação (README do módulo, comentários Javadoc)
```

---

## 5. Checklist Transversal de Encerramento de Fase

Válido para **todas** as fases. Use este checklist ao abrir o PR.

| # | Item | Como validar | Critério de aprovação |
|---|---|---|---|
| 1 | Branch da fase aberta com escopo explícito | `$ git branch --show-current` | Nome no formato `fase-{n}-{modulo}` (ex: `fase-4-catalogo`) |
| 2 | Commits pequenos e descritivos | `$ git log --oneline origin/main..HEAD` | Cada commit descreve uma unidade de trabalho; PR não contém mudanças fora do escopo |
| 3 | Build local passa | `$ ./mvnw clean compile` | `BUILD SUCCESS` sem erros |
| 4 | Testes do módulo alterado passam | `$ ./mvnw test -Dtest="*{Modulo}*"` | Todos os testes do módulo verdes |
| 5 | Testes de RBAC e tenant isolation passam | `$ ./mvnw test -Dtest="*Tenant*,*Security*"` | Zero regressões |
| 6 | Evidências registradas no PR | Inspeção do PR | PR contém: comandos executados, saídas, cenários de teste |
| 7 | Checklist específico da fase 100% verde | Inspeção visual | Nenhum item crítico com status `não` |
| 8 | Documentação atualizada | Inspeção do PR | README do módulo ou seção no `PROJETO_DA_ARQUITETURA.md` atualizada |

---

## 6. Roteiro Guiado por Fase

---

### Fase 0 — Preparação de Projeto

#### Objetivo

Deixar o ambiente local 100% reproduzível: qualquer pessoa clona o repositório, executa 3-4 comandos e tem Scriba rodando com MariaDB.

**Entregas:**
- Padronização de branches e convenções de commit.
- Baseline de quality gate (build + testes + lint).
- Setup local com Java 25, Maven Wrapper e MariaDB via Docker Compose.

#### Pré-requisitos

- [ ] Docker e Docker Compose instalados.
- [ ] JDK 25 instalada ou gerenciada por SDKMAN.
- [ ] Acesso ao repositório.

#### Perguntas & Respostas de Validação

**P1. A versão correta da JDK está instalada?**

> **Comando:**
> ```bash
> $ java -version
> ```
> **Resultado esperado (linha contendo "25"):**
> ```
> openjdk version "25" 2025-09-16
> OpenJDK Runtime Environment (build 25+36)
> OpenJDK 64-Bit Server VM (build 25+36, mixed mode, sharing)
> ```
> ✅ Aceitar qualquer patch release de Java 25.
> ❌ Se aparecer Java 21 ou 17, instalar JDK 25 antes de prosseguir.

**P2. O Maven Wrapper está funcional?**

> **Comando:**
> ```bash
> $ ./mvnw -v
> ```
> **Resultado esperado:**
> ```
> Apache Maven 3.9.x
> Maven home: .../.m2/wrapper/dists/apache-maven-3.9.x/...
> Java version: 25, vendor: ...
> Default locale: ..., platform encoding: UTF-8
> OS name: "linux"/"mac os x"/"windows", ...
> ```
> ✅ Saída lista Maven 3.9.x e Java 25.
> ❌ Se `./mvnw` for "permission denied": `$ chmod +x mvnw`.

**P3. O MariaDB de desenvolvimento sobe via Docker Compose?**

> **Comando:**
> ```bash
> $ docker compose up -d db-dev
> $ docker compose ps
> ```
> **Resultado esperado:**
> ```
> NAME          IMAGE                COMMAND                  SERVICE   STATUS         PORTS
> scriba-db-dev mariadb:11.4         "docker-entrypoint.s…"   db-dev    Up (healthy)   0.0.0.0:3306->3306/tcp
> ```
> ✅ Status `Up (healthy)` e porta `3306` publicada.
> ❌ Se porta 3306 estiver ocupada: parar processo local com `$ sudo lsof -i :3306` e matar o PID, ou reconfigurar `docker-compose.yml`.

**P4. A aplicação conecta no banco de desenvolvimento?**

> **Comando:**
> ```bash
> $ docker compose exec db-dev mariadb -uroot -p"$DB_ROOT_PASSWORD" -e "SELECT VERSION();"
> ```
> **Resultado esperado:**
> ```
> +-----------------+
> | VERSION()       |
> +-----------------+
> | 11.4.x-MariaDB  |
> +-----------------+
> ```
> ✅ Versão 11.4.x retornada.

**P5. A build completa passa em banco limpo?**

> **Comando:**
> ```bash
> $ ./mvnw clean package -DskipTests
> ```
> **Resultado esperado (últimas linhas):**
> ```
> [INFO] BUILD SUCCESS
> [INFO] Total time:  XX.XXX s
> [INFO] Finished at: ...
> ```
> ✅ `BUILD SUCCESS`. Arquivo `target/scriba-1.0.0-SNAPSHOT.jar` gerado.

**P6. A suíte de testes inicial executa?**

> **Comando:**
> ```bash
> $ make test
> ```
> ou equivalente:
> ```bash
> $ docker compose -f docker-compose.test.yml up -d db-test
> $ ./mvnw test -Dspring.profiles.active=test
> ```
> **Resultado esperado:**
> ```
> [INFO] Tests run: N, Failures: 0, Errors: 0, Skipped: 0
> [INFO] BUILD SUCCESS
> ```
> ✅ Zero falhas, zero erros.

**P7. As migrações Flyway executam em banco limpo?**

> **Comando:**
> ```bash
> $ ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
> ```
> Procurar nas logs por linhas como:
> ```
> Successfully applied N migrations to schema `scriba_dev`
> ```
> **Validação no banco:**
> ```bash
> $ docker compose exec db-dev mariadb -uroot -p"scriba_root_dev_pass" scriba_dev \
>     -e "SELECT version, description, success FROM flyway_schema_history ORDER BY installed_rank;"
> ```
> **Resultado esperado (todas as linhas com `success = 1`):**
> ```
> +---------+----------------------+---------+
> | version | description          | success |
> +---------+----------------------+---------+
> | 1       | schema administracao | 1       |
> +---------+----------------------+---------+
> ```
>
> No estado atual do repositório existe apenas a migration `V1__schema_administracao.sql`.

#### Checklist Executável da Fase (Scriba)

| Item | Critério de aprovação |
|---|---|
| Subir banco local e iniciar a aplicação | Aplicação inicia sem erro fatal e loga `Started ScribaApplication in X.XXX seconds` |
| Executar migration inicial em banco limpo | Tabelas `instituicao`, `biblioteca`, `usuario` existem após a execução |
| Registrar passo a passo de setup no PR | Outro dev executa o roteiro sem ajuste manual e obtém o mesmo resultado |

**Validar existência das tabelas:**
```bash
$ docker compose exec db-dev mariadb -uroot -p"scriba_root_dev_pass" scriba_dev \
    -e "SHOW TABLES;"
```

Saída esperada deve conter pelo menos: `instituicao`, `biblioteca`, `usuario`, `flyway_schema_history`.

#### Gate de Saída

- [ ] Ambiente pode ser preparado do zero sem ajuda verbal.
- [ ] `./mvnw clean verify -DskipTests` passa em 100%.
- [ ] Banco local sobe via `make dev` ou `docker compose up -d db-dev`.
- [ ] `flyway_schema_history` contém a migration disponível no repositório com `success = 1`.

---

### Fase 1 — Baseline CORE

#### Objetivo

Entregar a infraestrutura transversal reutilizável por todos os módulos: autenticação, RBAC, entidades base, tratamento global de erros e estratégia inicial de tenant isolation.

**Módulos/escopo:** `core/config`, `core/domain`, `core/security`, `core/exception`.

#### Pré-requisitos

- [ ] Fase 0 concluída.
- [ ] Banco funcionando.
- [ ] Estratégia de autenticação decidida (form login + Spring Security + BCrypt strength 12).
- [ ] Migration V1 (administração) pronta para seed mínimo de `usuario`.

#### Perguntas & Respostas de Validação

**P1. A autenticação funciona com credenciais válidas?**

> **Comando:**
> ```bash
> $ curl -i -X POST http://localhost:8080/login \
>     -d "email=admin@scriba.dev&senha=admin123" \
>     -c cookies.txt
> ```
> **Resultado esperado:**
> ```
> HTTP/1.1 302 Found
> Location: http://localhost:8080/
> Set-Cookie: JSESSIONID=...; Path=/; HttpOnly
> Set-Cookie: XSRF-TOKEN=...; Path=/
> ```
> ✅ Redirect 302 para `/` e cookies `JSESSIONID` + `XSRF-TOKEN` setados.

**P2. A autenticação falha com credenciais inválidas?**

> **Comando:**
> ```bash
> $ curl -i -X POST http://localhost:8080/login \
>     -d "email=admin@scriba.dev&senha=errada"
> ```
> **Resultado esperado:**
> ```
> HTTP/1.1 302 Found
> Location: http://localhost:8080/login?error
> ```
> ✅ Redirect para `/login?error` (mensagem genérica, sem vazar qual campo errou).

**P3. O RBAC diferencia corretamente os três perfis?**

> **Teste de integração obrigatório:**
> ```java
> @SpringBootTest
> @AutoConfigureMockMvc
> @ActiveProfiles("test")
> class RbacIntegrationTest {
>
>     @Autowired MockMvc mvc;
>
>     @Test @WithMockUser(authorities = "ROLE_ADMIN")
>     void adminAcessaRotaAdministrativa() throws Exception {
>         mvc.perform(get("/admin/usuarios"))
>            .andExpect(status().isOk());
>     }
>
>     @Test @WithMockUser(authorities = "ROLE_LEITOR")
>     void leitorRecebeForbiddenEmRotaAdministrativa() throws Exception {
>         mvc.perform(get("/admin/usuarios"))
>            .andExpect(status().isForbidden());
>     }
>
>     @Test @WithMockUser(authorities = "ROLE_BIBLIOTECARIO")
>     void bibliotecarioAcessaCatalogoMasNaoAdmin() throws Exception {
>         mvc.perform(get("/catalogo/autores")).andExpect(status().isOk());
>         mvc.perform(get("/admin/usuarios")).andExpect(status().isForbidden());
>     }
> }
> ```
> **Comando:**
> ```bash
> $ ./mvnw test -Dtest="RbacIntegrationTest"
> ```
> **Resultado esperado:** `Tests run: 3, Failures: 0`.

**P4. `BaseEntity` e `TenantEntity` estão aplicadas corretamente?**

> **Checagem estática — grep no código:**
> ```bash
> $ grep -rn "extends TenantEntity" src/main/java/br/dev/lourenco/scriba/modules/
> ```
> **Resultado esperado:** toda entidade com escopo institucional (ex: `AcervoItem`, `Leitor`, `Emprestimo`, `Categoria`) aparece na listagem. Entidades globais como `Autor` e `Editora` aparecem com `extends TenantEntity` também (após V7 do refactor).
>
> **Checagem de auditoria no banco:**
> ```sql
> mariadb> DESCRIBE categoria;
> ```
> **Colunas esperadas (além das específicas da entidade):**
> ```
> id                VARCHAR(36)    NO PK
> instituicao_id    VARCHAR(36)    NO    FK
> criado_em         DATETIME(6)    NO
> atualizado_em     DATETIME(6)    NO
> ```

**P5. O tratamento global de erros padroniza a resposta?**

> **Comando:**
> ```bash
> $ curl -i http://localhost:8080/catalogo/autores/00000000-0000-0000-0000-000000000000 \
>     -b cookies.txt
> ```
> **Resultado esperado:**
> ```
> HTTP/1.1 404 Not Found
> Content-Type: application/problem+json
>
> {
>   "type": "https://scriba.dev/errors/not-found",
>   "title": "Recurso não encontrado",
>   "status": 404,
>   "detail": "Autor com id 00000000-... não encontrado",
>   "instance": "/catalogo/autores/00000000-..."
> }
> ```
> ✅ Resposta RFC 7807 (problem+json) para requisições não-HTMX.

**P6. Requisições HTMX recebem resposta apropriada no erro?**

> **Comando:**
> ```bash
> $ curl -i http://localhost:8080/catalogo/autores/00000000-0000-0000-0000-000000000000 \
>     -H "HX-Request: true" -b cookies.txt
> ```
> **Resultado esperado:**
> ```
> HTTP/1.1 404 Not Found
> Content-Type: text/html;charset=UTF-8
>
> <div class="alert alert-error" role="alert">...</div>
> ```
> ✅ Fragmento HTML `error/htmx-error` retornado (não JSON).

**P7. A estratégia de tenant isolation está ativa?**

> **Comando (checar log de startup):**
> ```bash
> $ ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev 2>&1 | grep -i "tenant"
> ```
> **Resultado esperado:**
> ```
> INFO  TenantEnforcementAspect: Pointcut ativo em br.dev.lourenco.scriba.modules.*.repository.*
> INFO  TenantEnforcementAspect: scriba.tenant.strict-mode=true (dev)
> ```

#### Testes & Comandos

```bash
# Suíte completa do core
$ ./mvnw test -Dtest="*Core*,*Security*,*Tenant*"

# Só autenticação
$ ./mvnw test -Dtest="AuthenticationIntegrationTest"

# Só isolamento de tenant
$ ./mvnw test -Dtest="TenantIsolation*Test"
```

#### Checklist Executável da Fase (Scriba)

| Item | Comando/Validação | Critério de aprovação |
|---|---|---|
| `BaseEntity` e `TenantEntity` nas entidades tenant-aware | `$ grep -rn "extends TenantEntity" src/main/java` | Entidades institucionais herdam `TenantEntity` |
| RBAC básico por perfil validado | `$ ./mvnw test -Dtest="RbacIntegrationTest"` | ADMIN acessa rota admin; LEITOR recebe 403 |
| Isolamento entre duas instituições | `$ ./mvnw test -Dtest="TenantIsolationCoreTest"` | Sessão da inst. A nunca retorna dados da inst. B |

#### Gate de Saída

- [ ] Testes de autenticação, autorização e tenant isolation **todos verdes**.
- [ ] Migrations V1–V7 executam em banco limpo.
- [ ] `/login` funcional, `/admin/**` protegido, `/portal/**` protegido.
- [ ] `GlobalExceptionHandler` retorna RFC 7807 ou fragmento HTMX conforme `HX-Request`.

---

### Fase 2 — Módulo de Usuários (Administração)

#### Objetivo

Permitir administração de instituições, bibliotecas e usuários, com as regras de empréstimo configuráveis por instituição.

**Módulo:** `modules/administracao`.

#### Pré-requisitos

- [ ] Fase 1 concluída.
- [ ] RBAC funcional.
- [ ] Entidades `Instituicao`, `Biblioteca`, `Usuario`, `RegraEmprestimo` mapeadas.
- [ ] Migration V1 aplicada.

#### Perguntas & Respostas de Validação

**P1. O CRUD de usuário está completo?**

> **Comandos de teste (logado como ADMIN):**
> ```bash
> # Criar
> $ curl -X POST http://localhost:8080/admin/usuarios \
>     -b cookies.txt \
>     -H "X-XSRF-TOKEN: $(grep XSRF cookies.txt | awk '{print $7}')" \
>     -d "nome=Maria&email=maria@biblio.org&senha=Temp@123&role=BIBLIOTECARIO"
> # Esperado: 302 redirect para /admin/usuarios com flash de sucesso
>
> # Listar
> $ curl http://localhost:8080/admin/usuarios -b cookies.txt
> # Esperado: HTML contendo <td>maria@biblio.org</td>
>
> # Editar (vira inativo)
> $ curl -X PATCH http://localhost:8080/admin/usuarios/{id}/desativar \
>     -b cookies.txt \
>     -H "X-XSRF-TOKEN: ..."
> # Esperado: 200 com fragmento HTMX atualizado
> ```

**P2. As regras de empréstimo por instituição podem ser alteradas sem redeploy?**

> **Comando (logado como ADMIN da instituição A):**
> ```bash
> $ curl -X POST http://localhost:8080/admin/regras-emprestimo \
>     -b cookies.txt \
>     -H "X-XSRF-TOKEN: ..." \
>     -d "prazoPadraoDias=21&limiteEmprestimos=7&valorMulta=1.00"
> ```
> **Validação no banco:**
> ```sql
> mariadb> SELECT prazo_padrao_dias, limite_emprestimos, valor_multa
>          FROM instituicao WHERE id = '<id-inst-A>';
> ```
> **Resultado esperado:**
> ```
> +-------------------+---------------------+-------------+
> | prazo_padrao_dias | limite_emprestimos  | valor_multa |
> +-------------------+---------------------+-------------+
> | 21                | 7                   | 1.00        |
> +-------------------+---------------------+-------------+
> ```

**P3. Um bibliotecário da instituição A consegue ver usuários da instituição B?**

> **Teste obrigatório:**
> ```java
> @Test @WithMockUser(authorities = "ROLE_BIBLIOTECARIO")
> void bibliotecarioNaoListaUsuariosDeOutraInstituicao() throws Exception {
>     // seed: inst A = usuario1, inst B = usuario2; sessão = inst A
>     mvc.perform(get("/admin/usuarios").with(tenantA()))
>        .andExpect(status().isOk())
>        .andExpect(model().attribute("usuarios",
>            not(hasItem(hasProperty("email", is("usuario2@instB.org"))))));
> }
> ```
> **Comando:**
> ```bash
> $ ./mvnw test -Dtest="UsuarioTenantIsolationTest"
> ```
> **Resultado esperado:** `Tests run: X, Failures: 0`.

**P4. A senha é armazenada com BCrypt strength 12?**

> **Validação no banco:**
> ```sql
> mariadb> SELECT email, LEFT(senha, 7) AS prefix, LENGTH(senha) AS len FROM usuario LIMIT 1;
> ```
> **Resultado esperado:**
> ```
> +-------------------+---------+-----+
> | email             | prefix  | len |
> +-------------------+---------+-----+
> | admin@scriba.dev  | $2a$12$ | 60  |
> +-------------------+---------+-----+
> ```
> ✅ Prefixo `$2a$12$` indica BCrypt com strength 12.

#### Testes & Comandos

```bash
# Suíte do módulo administração
$ ./mvnw test -Dtest="*Administracao*,*Usuario*,*Instituicao*"

# Teste específico de isolamento do módulo
$ ./mvnw test -Dtest="UsuarioTenantIsolationTest"

# Teste de RBAC por endpoint
$ ./mvnw test -Dtest="AdminRbacTest"
```

#### Checklist Executável da Fase (Scriba)

| Item | Comando/Validação | Critério de aprovação |
|---|---|---|
| Criar `BIBLIOTECARIO` para instituição A como ADMIN | `POST /admin/usuarios` autenticado | Usuário persistido com `instituicao_id` da inst. A |
| Bloquear visualização cruzada entre instituições | `$ ./mvnw test -Dtest="UsuarioTenantIsolationTest"` | Sessão da inst. A não lista usuários da inst. B |
| Alterar regra de empréstimo da inst. A | SQL `SELECT` após o POST | Nova regra persiste em A, dados de B inalterados |

#### Gate de Saída

- [ ] CRUD completo de Usuario, Instituicao, Biblioteca com RBAC.
- [ ] Teste de isolamento de tenant por instituição aprovado.
- [ ] Regras de empréstimo editáveis pelo ADMIN sem redeploy.
- [ ] BCrypt strength 12 confirmado no banco.

---

### Fase 3 — Módulo de Leitores e Pessoas

#### Objetivo

Habilitar cadastro e governança do público atendido pela biblioteca.

**Módulo:** `modules/pessoas` (`Leitor`, `TipoLeitor`, `Fornecedor`).

#### Pré-requisitos

- [ ] Fase 2 concluída.
- [ ] Operador autenticado disponível.
- [ ] Relacionamentos institucionais definidos (`Leitor` extends `TenantEntity`).
- [ ] Migration V2 e V7 aplicadas.

#### Perguntas & Respostas de Validação

**P1. É possível cadastrar um leitor com TipoLeitor válido da instituição logada?**

> **Comando (autenticado como BIBLIOTECARIO):**
> ```bash
> $ curl -X POST http://localhost:8080/pessoas/leitores \
>     -b cookies.txt \
>     -H "X-XSRF-TOKEN: ..." \
>     -d "nome=Joao+Silva&cpf=12345678900&email=joao@email.com&tipoLeitorId=<uuid>"
> ```
> **Resultado esperado:** `302 Found` para `/pessoas/leitores` com flash `Leitor criado com sucesso.`.
>
> **Validação no banco:**
> ```sql
> mariadb> SELECT id, nome, cpf, instituicao_id FROM leitor WHERE email = 'joao@email.com';
> ```
> ✅ `instituicao_id` igual ao `instituicao_id` do usuário logado.

**P2. Tentativa de cadastrar leitor apontando para `TipoLeitor` de outra instituição é bloqueada?**

> **Teste obrigatório:**
> ```java
> @Test @WithMockUser(authorities = "ROLE_BIBLIOTECARIO")
> void naoPermiteCadastrarLeitorComTipoDeOutraInstituicao() throws Exception {
>     // seed: tipoLeitor pertence a instB; sessão é de instA
>     mvc.perform(post("/pessoas/leitores").with(tenantA())
>             .param("tipoLeitorId", tipoB.getId().toString()).with(csrf()))
>        .andExpect(status().isUnprocessableEntity())  // 422 BusinessException
>        .andExpect(jsonPath("$.detail").value(containsString("não pertence à instituição")));
> }
> ```
> **Comando:**
> ```bash
> $ ./mvnw test -Dtest="LeitorValidacaoInstitucionalTest"
> ```

**P3. A listagem respeita o escopo institucional?**

> **Comando:**
> ```bash
> # Autenticado na inst A
> $ curl http://localhost:8080/pessoas/leitores -b cookies_A.txt
> # Autenticado na inst B
> $ curl http://localhost:8080/pessoas/leitores -b cookies_B.txt
> ```
> **Resultado esperado:** conjunto de leitores disjunto — leitores criados em A não aparecem em B.

**P4. O CRUD de `Fornecedor` suporta os dois tipos (PF e PJ)?**

> **Comando:**
> ```bash
> # Fornecedor PJ
> $ curl -X POST http://localhost:8080/pessoas/fornecedores \
>     -b cookies.txt -H "X-XSRF-TOKEN: ..." \
>     -d "nome=Editora+ABC&tipo=PJ&cpfCnpj=12345678000190"
>
> # Fornecedor PF
> $ curl -X POST http://localhost:8080/pessoas/fornecedores \
>     -b cookies.txt -H "X-XSRF-TOKEN: ..." \
>     -d "nome=Ana+Fornecedora&tipo=PF&cpfCnpj=12345678900"
> ```
> **Validação:**
> ```sql
> mariadb> SELECT nome, tipo, cpf_cnpj FROM fornecedor;
> ```
> ✅ Ambos persistem com `tipo` correto e `cpf_cnpj` preenchido.

#### Testes & Comandos

```bash
# Suíte do módulo pessoas
$ ./mvnw test -Dtest="*Leitor*,*Pessoas*,*Fornecedor*"

# Integração específica
$ ./mvnw test -Dtest="LeitorTenantIsolationTest"
```

#### Checklist Executável da Fase (Scriba)

| Item | Comando/Validação | Critério de aprovação |
|---|---|---|
| Cadastrar leitor com `TipoLeitor` válido | `POST /pessoas/leitores` | Cadastro persiste com `instituicao_id` = sessão |
| Bloquear leitor com vínculo divergente | `$ ./mvnw test -Dtest="LeitorValidacaoInstitucionalTest"` | Operação bloqueada por validação de domínio |
| Bibliotecário lista apenas leitores da própria inst. | `$ curl /pessoas/leitores` em cookies distintos | Conjuntos disjuntos entre instituições |

#### Gate de Saída

- [ ] CRUD funcional de `Leitor`, `TipoLeitor`, `Fornecedor`.
- [ ] Validação institucional bloqueia cadastros cross-tenant.
- [ ] Testes de isolamento aprovados.

---

### Fase 4 — Módulo Catálogo

#### Objetivo

Estruturar o vocabulário bibliográfico por instituição: autores, editoras, categorias e classificações.

**Módulo:** `modules/catalogo`.

> **Observação arquitetural (pós-V7):** `Autor` e `Editora` agora são **por instituição** (não mais globais). Cada biblioteca mantém seu próprio vocabulário controlado.

#### Pré-requisitos

- [ ] Fase 3 concluída.
- [ ] DTOs (records) e entidades planejados.
- [ ] Mappers MapStruct configurados.
- [ ] Filtros de busca definidos.
- [ ] Migrations V2, V6 e V7 aplicadas.

#### Perguntas & Respostas de Validação

**P1. O CRUD de `Autor` está completo e segue o modelo canônico?**

> **Comandos:**
> ```bash
> # CREATE
> $ curl -X POST http://localhost:8080/catalogo/autores \
>     -b cookies.txt -H "X-XSRF-TOKEN: ..." \
>     -d "nome=Machado+de+Assis&nacionalidade=Brasileira"
>
> # READ (listagem com busca)
> $ curl "http://localhost:8080/catalogo/autores?busca=machado" -b cookies.txt
>
> # UPDATE
> $ curl -X PUT http://localhost:8080/catalogo/autores/{id} \
>     -b cookies.txt -H "X-XSRF-TOKEN: ..." \
>     -d "nome=Machado+de+Assis&nacionalidade=Brasileira&biografia=..."
>
> # DELETE (soft-delete)
> $ curl -X DELETE http://localhost:8080/catalogo/autores/{id} \
>     -b cookies.txt -H "X-XSRF-TOKEN: ..."
> ```
> **Resultado esperado:** cada operação retorna status 2xx / 302 e fragmento HTMX atualizado.

**P2. O DTO mantém consistência com a entidade via Mapper?**

> **Validação em tempo de build (MapStruct detecta inconsistência como erro):**
> ```bash
> $ ./mvnw clean compile
> ```
> **Resultado esperado:** `BUILD SUCCESS`. Se algum campo do request não estiver mapeado, o build **falha** com mensagem como:
> ```
> ERROR: Unmapped target property: "xyz"
> ```
>
> **Teste de mapeamento:**
> ```java
> @Test
> void mapperAutorConverteRequestParaEntity() {
>     AutorRequest req = new AutorRequest("Clarice Lispector", "Brasileira", null);
>     Autor entity = autorMapper.toEntity(req);
>     assertThat(entity.getNome()).isEqualTo("Clarice Lispector");
>     assertThat(entity.getInstituicaoId()).isNull(); // setado pelo controller
> }
> ```

**P3. A busca aceita filtros e retorna apenas do tenant logado?**

> **Comando:**
> ```bash
> $ curl "http://localhost:8080/catalogo/autores?busca=machado&page=0&size=10" \
>     -b cookies_instA.txt
> ```
> **Resultado esperado:** HTML contém `Machado de Assis` **somente se** o autor foi cadastrado na instituição A. Não retorna registros de outras instituições.

**P4. Categorias suportam hierarquia pai/filho?**

> **Comando:**
> ```bash
> # Criar pai
> $ curl -X POST http://localhost:8080/catalogo/categorias \
>     -b cookies.txt -H "X-XSRF-TOKEN: ..." \
>     -d "nome=Literatura"
>
> # Criar filho
> $ curl -X POST http://localhost:8080/catalogo/categorias \
>     -b cookies.txt -H "X-XSRF-TOKEN: ..." \
>     -d "nome=Poesia&paiId={uuid_literatura}"
> ```
> **Validação:**
> ```sql
> mariadb> SELECT id, nome, categoria_pai_id FROM categoria ORDER BY criado_em;
> ```
> ✅ A linha de Poesia tem `categoria_pai_id` apontando para a linha de Literatura.

**P5. `Classificacao` respeita o padrão definido em `RegraEmprestimo`?**

> **Comando:**
> ```sql
> mariadb> SELECT i.id, i.classificacao_padrao, c.codigo, c.descricao
>          FROM instituicao i
>          LEFT JOIN classificacao c ON c.instituicao_id = i.id
>          WHERE i.id = '<id>';
> ```
> **Resultado esperado:** se `classificacao_padrao = CDD`, as classificações criadas seguem o formato CDD (ex: `869.3`, `808.1`).

#### Testes & Comandos

```bash
# Suíte completa do catálogo
$ ./mvnw test -Dtest="*Catalogo*,*Autor*,*Editora*,*Categoria*,*Classificacao*"

# Isolamento de tenant no catálogo
$ ./mvnw test -Dtest="CatalogoTenantIsolationTest"

# Validação de mapeamento
$ ./mvnw test -Dtest="*MapperTest"
```

#### Checklist Executável da Fase (Scriba)

| Item | Comando/Validação | Critério de aprovação |
|---|---|---|
| CRUD de Autor, Editora, Categoria, Classificacao na inst. A | `POST /catalogo/{entidade}` | Todas as entidades persistem com `instituicao_id` correto |
| Busca por nome parcial e categoria | `GET /catalogo/autores?busca=...` | Retorna apenas registros que batem com o filtro |
| Segregação por tenant com sessão da inst. B | Comparar listagens em cookies distintos | Conjuntos disjuntos por instituição |

#### Gate de Saída

- [ ] CRUD completo de `Autor`, `Editora`, `Categoria`, `Classificacao`.
- [ ] Consistência DTO/Entity validada em build (MapStruct).
- [ ] Busca com filtros respeitando escopo de tenant.
- [ ] Hierarquia de `Categoria` funcional (pai/filho).

---

### Fase 5 — Módulo Acervo

#### Objetivo

Implementar os exemplares físicos (`AcervoItem` abstrata + subtipos via JOINED) e o ciclo de vida de estado (`StatusAcervo`).

**Módulo:** `modules/acervo`.

#### Pré-requisitos

- [ ] Fase 4 concluída.
- [ ] Hierarquia `AcervoItem` desenhada (JOINED).
- [ ] Subtipos definidos: `Livro`, `Periodico`, `Carta`, `Foto`, `Midia`.
- [ ] Máquina de estados (`StatusAcervo` como padrão State) planejada.
- [ ] Migration V4 aplicada.

#### Perguntas & Respostas de Validação

**P1. A hierarquia JOINED persiste cada subtipo na sua tabela específica?**

> **Comando (criar um livro):**
> ```bash
> $ curl -X POST http://localhost:8080/acervo/itens \
>     -b cookies.txt -H "X-XSRF-TOKEN: ..." \
>     -d "tipoItem=LIVRO&titulo=Dom+Casmurro&tombo=LIV-0001&isbn=9788525406958&autorId=..."
> ```
> **Validação:**
> ```sql
> mariadb> SELECT ai.id, ai.tipo_item, ai.titulo, al.isbn, al.numero_paginas
>          FROM acervo_item ai
>          JOIN acervo_livro al ON al.id = ai.id
>          WHERE ai.tombo = 'LIV-0001';
> ```
> **Resultado esperado:** linha única com `tipo_item = LIVRO`, `isbn` preenchido. Tabela base `acervo_item` e tabela específica `acervo_livro` ambas populadas.

**P2. O `AcervoService` usa Factory com pattern matching de Java 25?**

> **Validação estática:**
> ```bash
> $ grep -A 10 "switch.*tipoItem" src/main/java/br/dev/lourenco/scriba/modules/acervo/service/AcervoService.java
> ```
> **Resultado esperado:** código como:
> ```java
> AcervoItem item = switch (request.tipoItem()) {
>     case LIVRO -> new Livro();
>     case PERIODICO -> new Periodico();
>     case CARTA -> new Carta();
>     case FOTO -> new Foto();
>     case MIDIA -> new Midia();
> };
> ```

**P3. Uma transição válida de estado é aceita?**

> **Comando:**
> ```bash
> $ curl -X PATCH http://localhost:8080/acervo/itens/{id}/status \
>     -b cookies.txt -H "X-XSRF-TOKEN: ..." \
>     -d "novoStatus=EM_MANUTENCAO&justificativa=Capa+danificada"
> ```
> **Validação:**
> ```sql
> mariadb> SELECT id, status FROM acervo_item WHERE id = '<id>';
> ```
> **Resultado esperado:** `status = 'EM_MANUTENCAO'`.

**P4. Uma transição inválida é bloqueada pelo enum State?**

> **Comando (tentar DESCARTADO → DISPONIVEL, que é inválido):**
> ```bash
> $ curl -i -X PATCH http://localhost:8080/acervo/itens/{id_descartado}/status \
>     -b cookies.txt -H "X-XSRF-TOKEN: ..." \
>     -d "novoStatus=DISPONIVEL&justificativa=Teste"
> ```
> **Resultado esperado:**
> ```
> HTTP/1.1 422 Unprocessable Entity
> Content-Type: application/problem+json
>
> {
>   "type": "https://scriba.dev/errors/business",
>   "title": "Regra de negócio violada",
>   "status": 422,
>   "detail": "Transição DESCARTADO → DISPONIVEL não permitida"
> }
> ```
> ✅ Status do item permanece `DESCARTADO` no banco.

**P5. O lock otimista (`@Version`) está aplicado em `AcervoItem`?**

> **Validação estática:**
> ```bash
> $ grep -B 1 -A 1 "@Version" src/main/java/br/dev/lourenco/scriba/modules/acervo/domain/AcervoItem.java
> ```
> **Resultado esperado:**
> ```java
>     @Version
>     private Long versao;
> ```
>
> **Validação no schema:**
> ```sql
> mariadb> DESCRIBE acervo_item;
> ```
> Coluna `versao BIGINT NOT NULL DEFAULT 0` deve existir.

**P6. O soft-delete é aplicado exclusivamente ao status `DESCARTADO`?**

> **Comando:**
> ```sql
> mariadb> SELECT COUNT(*) FROM acervo_item WHERE deleted_at IS NOT NULL AND status <> 'DESCARTADO';
> ```
> **Resultado esperado:** `0` linhas. Apenas itens `DESCARTADO` têm `deleted_at` preenchido.

#### Testes & Comandos

```bash
# Suíte do acervo
$ ./mvnw test -Dtest="*Acervo*,*StatusAcervo*"

# Teste da máquina de estados
$ ./mvnw test -Dtest="StatusAcervoTransicoesTest"

# Teste de Factory
$ ./mvnw test -Dtest="AcervoFactoryTest"

# Teste de subtipos JOINED
$ ./mvnw test -Dtest="AcervoSubtiposIntegrationTest"
```

#### Checklist Executável da Fase (Scriba)

| Item | Comando/Validação | Critério de aprovação |
|---|---|---|
| Criar `Livro` com status inicial `DISPONIVEL` | `POST /acervo/itens` com `tipoItem=LIVRO` | Item persiste em `acervo_item` + `acervo_livro`, `status=DISPONIVEL` |
| Transição válida `DISPONIVEL → EMPRESTADO` | `PATCH /acervo/itens/{id}/status` | Status atualizado, `versao` incrementada |
| Transição inválida `DESCARTADO → DISPONIVEL` | `PATCH` devolve 422 | `BusinessException` lançada, status permanece DESCARTADO |

#### Gate de Saída

- [ ] Todos os 5 subtipos criam-se corretamente via JOINED.
- [ ] Enum `StatusAcervo` valida 100% das transições.
- [ ] `@Version` presente e testado.
- [ ] Soft-delete apenas para `DESCARTADO`.

---

### Fase 6 — Módulo Circulação

#### Objetivo

Viabilizar empréstimo, devolução, renovação, reserva e multa — com controle de concorrência robusto.

**Módulo:** `modules/circulacao`.

> 🔒 **Fase crítica.** O parecer arquitetural (seção 0 e 15.1 do `PROJETO_DA_ARQUITETURA.md`) lista concorrência em circulação como um dos dois maiores riscos. Não avançar sem os testes concorrentes verdes.

#### Pré-requisitos

- [ ] Fases 4 e 5 concluídas (catálogo e acervo).
- [ ] Regras institucionais (`RegraEmprestimo`) configuráveis.
- [ ] Lock otimista (`@Version`) em `AcervoItem` aplicado.
- [ ] Migration V5 e V8 (blindagem concorrência) aplicadas.
- [ ] Constraint `uq_emprestimo_item_ativo` criada no banco (coluna gerada MariaDB).

#### Perguntas & Respostas de Validação

**P1. O fluxo de empréstimo respeita todas as verificações da seção 7.4?**

> **Comando:**
> ```bash
> $ curl -X POST http://localhost:8080/circulacao/emprestimos \
>     -b cookies.txt -H "X-XSRF-TOKEN: ..." \
>     -d "acervoItemId={uuid_item}&leitorId={uuid_leitor}"
> ```
> **Resultado esperado (sequência validada):**
> 1. Sem multa pendente (se `bloqueioComMulta=true`).
> 2. Leitor abaixo do `limiteEmprestimos`.
> 3. Item em `DISPONIVEL` ou `RESERVADO` pelo próprio leitor.
> 4. Prazo calculado: `LocalDate.now() + prazoPorTipoItem`.
> 5. Item transiciona para `EMPRESTADO`.
>
> **Validação:**
> ```sql
> mariadb> SELECT e.id, e.data_emprestimo, e.data_prevista_devolucao, e.status,
>                 ai.status AS status_item
>          FROM emprestimo e
>          JOIN acervo_item ai ON ai.id = e.acervo_item_id
>          WHERE e.id = '<id>';
> ```
> ✅ `emprestimo.status = 'ATIVO'` e `acervo_item.status = 'EMPRESTADO'`.

**P2. A devolução em atraso gera multa com valor correto?**

> **Setup (forçar data de devolução no passado):**
> ```sql
> mariadb> UPDATE emprestimo
>          SET data_prevista_devolucao = CURDATE() - INTERVAL 5 DAY
>          WHERE id = '<id>';
> ```
> **Comando:**
> ```bash
> $ curl -X POST http://localhost:8080/circulacao/emprestimos/{id}/devolver \
>     -b cookies.txt -H "X-XSRF-TOKEN: ..."
> ```
> **Validação:**
> ```sql
> mariadb> SELECT m.valor, m.dias_atraso, e.data_efetiva_devolucao
>          FROM multa m JOIN emprestimo e ON e.id = m.emprestimo_id
>          WHERE m.emprestimo_id = '<id>';
> ```
> **Resultado esperado (assumindo `valorMulta=0.50` FIXO_DIARIO):**
> ```
> +-------+-------------+-------------------------+
> | valor | dias_atraso | data_efetiva_devolucao  |
> +-------+-------------+-------------------------+
> | 2.50  | 5           | 2026-04-22              |
> +-------+-------------+-------------------------+
> ```

**P3. Dois empréstimos concorrentes para o mesmo item: apenas 1 sucede?**

> **Teste obrigatório (seção 17 do projeto arquitetural):**
> ```java
> @SpringBootTest
> @ActiveProfiles("test")
> class EmprestimoConcorrenciaTest {
>
>     @Test
>     void empréstimoConcorrenteFalhaUmaDasOperações() throws Exception {
>         UUID itemId = seedItemDisponivel();
>
>         ExecutorService executor = Executors.newFixedThreadPool(2);
>         CompletableFuture<String> f1 = CompletableFuture.supplyAsync(
>             () -> tentarEmprestar(itemId, leitor1Id), executor);
>         CompletableFuture<String> f2 = CompletableFuture.supplyAsync(
>             () -> tentarEmprestar(itemId, leitor2Id), executor);
>
>         CompletableFuture.allOf(f1, f2).join();
>
>         long sucessos = Stream.of(f1, f2).filter(f -> "OK".equals(f.join())).count();
>         long falhas   = Stream.of(f1, f2).filter(f -> "CONFLITO".equals(f.join())).count();
>
>         assertThat(sucessos).isEqualTo(1);
>         assertThat(falhas).isEqualTo(1);
>     }
> }
> ```
> **Comando:**
> ```bash
> $ ./mvnw test -Dtest="EmprestimoConcorrenciaTest"
> ```
> **Resultado esperado:**
> ```
> [INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
> ```
>
> **Validação adicional no banco (após o teste):**
> ```sql
> mariadb> SELECT COUNT(*) FROM emprestimo
>          WHERE acervo_item_id = '<item_id>' AND status = 'ATIVO';
> ```
> **Resultado esperado:** `1` linha. A constraint `uq_emprestimo_item_ativo` garante isso no banco.

**P4. A constraint do banco impede duplo empréstimo mesmo se o lock otimista falhar?**

> **Comando SQL direto (bypass do service):**
> ```sql
> mariadb> INSERT INTO emprestimo (id, acervo_item_id, leitor_id, status, instituicao_id, criado_em, atualizado_em)
>          VALUES (UUID(), '<item_com_emprestimo_ativo>', '<outro_leitor>', 'ATIVO', '<inst>', NOW(), NOW());
> ```
> **Resultado esperado:**
> ```
> ERROR 1062 (23000): Duplicate entry '<item_id>' for key 'uq_emprestimo_item_ativo'
> ```
> ✅ A coluna gerada + `UNIQUE` bloqueia a inserção duplicada.

**P5. A fila de reserva respeita FIFO?**

> **Comando:**
> ```bash
> # Leitor1 reserva
> $ curl -X POST http://localhost:8080/circulacao/reservas \
>     -b cookies_leitor1.txt -H "X-XSRF-TOKEN: ..." \
>     -d "acervoItemId=<uuid>"
>
> # Leitor2 reserva
> $ curl -X POST http://localhost:8080/circulacao/reservas \
>     -b cookies_leitor2.txt -H "X-XSRF-TOKEN: ..." \
>     -d "acervoItemId=<uuid>"
> ```
> **Validação:**
> ```sql
> mariadb> SELECT leitor_id, posicao_fila FROM reserva
>          WHERE acervo_item_id = '<uuid>' ORDER BY posicao_fila;
> ```
> **Resultado esperado:**
> ```
> +-----------+--------------+
> | leitor_id | posicao_fila |
> +-----------+--------------+
> | <leitor1> | 1            |
> | <leitor2> | 2            |
> +-----------+--------------+
> ```

**P6. A renovação respeita `maximoRenovacoes`?**

> **Comando (renovar N+1 vezes, onde N = maximoRenovacoes=3):**
> ```bash
> $ for i in 1 2 3 4; do
>     echo "Renovação $i:"
>     curl -i -X POST http://localhost:8080/circulacao/emprestimos/{id}/renovar \
>         -b cookies.txt -H "X-XSRF-TOKEN: ..."
> done
> ```
> **Resultado esperado:** tentativas 1, 2 e 3 retornam `200 OK`; tentativa 4 retorna `422 Unprocessable Entity` com `detail: "Limite de renovações atingido"`.

#### Testes & Comandos

```bash
# Suíte completa de circulação
$ ./mvnw test -Dtest="*Circulacao*,*Emprestimo*,*Reserva*,*Multa*"

# Teste crítico de concorrência
$ ./mvnw test -Dtest="EmprestimoConcorrenciaTest"

# Teste de cálculo de multa
$ ./mvnw test -Dtest="MultaCalculoTest"

# Teste de fila de reserva
$ ./mvnw test -Dtest="ReservaFilaFifoTest"
```

#### Checklist Executável da Fase (Scriba)

| Item | Comando/Validação | Critério de aprovação |
|---|---|---|
| Processar empréstimo de item disponível | `POST /circulacao/emprestimos` | 1 empréstimo ativo, item em `EMPRESTADO` |
| Processar devolução com atraso | `POST /circulacao/emprestimos/{id}/devolver` após forçar data passada | Multa gerada com `valor > 0`, devolução registrada |
| Simular concorrência para o mesmo item | `$ ./mvnw test -Dtest="EmprestimoConcorrenciaTest"` | 1 sucesso + 1 conflito, apenas 1 empréstimo ativo no banco |

#### Gate de Saída

- [ ] Fluxo de empréstimo/devolução/renovação/reserva funcional.
- [ ] 🔒 Teste de concorrência (`EmprestimoConcorrenciaTest`) **verde**.
- [ ] 🔒 Constraint `uq_emprestimo_item_ativo` ativa e testada.
- [ ] Multas calculadas corretamente (FIXO_DIARIO e PERCENTUAL).
- [ ] Fila de reserva FIFO respeitada.

---

### Fase 7 — Módulo Curadoria

#### Objetivo

Executar descarte e remanejamento com rastreabilidade auditável e dupla proteção de autorização.

**Módulo:** `modules/curadoria`.

#### Pré-requisitos

- [ ] Fase 6 concluída.
- [ ] Estados do acervo consolidados.
- [ ] Regras de bloqueio operacional definidas (não descartar itens em `EMPRESTADO` ou `RESERVADO`).
- [ ] Trilha de auditoria prevista no domínio.

#### Perguntas & Respostas de Validação

**P1. Um ADMIN consegue descartar um item elegível?**

> **Comando (logado como ADMIN):**
> ```bash
> $ curl -X POST http://localhost:8080/curadoria/desbastamentos \
>     -b cookies_admin.txt -H "X-XSRF-TOKEN: ..." \
>     -d "acervoItemId={uuid}&tipo=DESCARTE&justificativa=Item+danificado+sem+recuperacao"
> ```
> **Validação:**
> ```sql
> mariadb> SELECT ai.status, ai.deleted_at, d.tipo, d.justificativa, d.usuario_id
>          FROM acervo_item ai
>          JOIN desbastamento d ON d.acervo_item_id = ai.id
>          WHERE ai.id = '<uuid>';
> ```
> **Resultado esperado:**
> ```
> +-------------+---------------------+----------+-------------------+----------+
> | status      | deleted_at          | tipo     | justificativa     | usuario  |
> +-------------+---------------------+----------+-------------------+----------+
> | DESCARTADO  | 2026-04-22 14:22:00 | DESCARTE | Item danificado…  | <uuid>   |
> +-------------+---------------------+----------+-------------------+----------+
> ```
> ✅ Status `DESCARTADO`, `deleted_at` preenchido, registro em `desbastamento`.

**P2. Um BIBLIOTECARIO é bloqueado ao tentar descartar?**

> **Comando (logado como BIBLIOTECARIO):**
> ```bash
> $ curl -i -X POST http://localhost:8080/curadoria/desbastamentos \
>     -b cookies_biblio.txt -H "X-XSRF-TOKEN: ..." \
>     -d "acervoItemId={uuid}&tipo=DESCARTE&justificativa=Teste"
> ```
> **Resultado esperado:**
> ```
> HTTP/1.1 403 Forbidden
> ```
> ✅ `@PreAuthorize("hasRole('ADMIN')")` bloqueia no Service (primeira camada de dupla proteção).

**P3. Item em `EMPRESTADO` não pode ser descartado?**

> **Comando:**
> ```bash
> $ curl -i -X POST http://localhost:8080/curadoria/desbastamentos \
>     -b cookies_admin.txt -H "X-XSRF-TOKEN: ..." \
>     -d "acervoItemId={uuid_emprestado}&tipo=DESCARTE&justificativa=Teste"
> ```
> **Resultado esperado:**
> ```
> HTTP/1.1 422 Unprocessable Entity
> Content-Type: application/problem+json
>
> {
>   "title": "Regra de negócio violada",
>   "detail": "Item em status EMPRESTADO não pode ser descartado"
> }
> ```
> ✅ Status do item permanece inalterado.

**P4. `justificativa` em branco é rejeitada?**

> **Comando:**
> ```bash
> $ curl -i -X POST http://localhost:8080/curadoria/desbastamentos \
>     -b cookies_admin.txt -H "X-XSRF-TOKEN: ..." \
>     -d "acervoItemId={uuid}&tipo=DESCARTE&justificativa="
> ```
> **Resultado esperado:**
> ```
> HTTP/1.1 400 Bad Request
>
> { "detail": "Justificativa é obrigatória" }
> ```

**P5. O remanejamento registra o destino?**

> **Comando:**
> ```bash
> $ curl -X POST http://localhost:8080/curadoria/desbastamentos \
>     -b cookies_admin.txt -H "X-XSRF-TOKEN: ..." \
>     -d "acervoItemId={uuid}&tipo=REMANEJAMENTO&destinoBibliotecaId={uuid}&justificativa=Filial+solicitou"
> ```
> **Validação:**
> ```sql
> mariadb> SELECT ai.status, d.tipo, d.destino_biblioteca_id, d.destino_instituicao_id
>          FROM acervo_item ai JOIN desbastamento d ON d.acervo_item_id = ai.id
>          WHERE ai.id = '<uuid>';
> ```
> **Resultado esperado:** `status = REMANEJADO`, `destino_biblioteca_id` preenchido.

**P6. A trilha de auditoria contém os 5 campos obrigatórios?**

> **Validação:**
> ```sql
> mariadb> SELECT usuario_id, instituicao_id, tipo, justificativa, criado_em
>          FROM desbastamento ORDER BY criado_em DESC LIMIT 1;
> ```
> ✅ Os 5 campos (`quem`, `tenant`, `operação`, `motivo`, `quando`) estão preenchidos.

#### Testes & Comandos

```bash
# Suíte da curadoria
$ ./mvnw test -Dtest="*Curadoria*,*Desbastamento*"

# Autorização dupla (Service + programática)
$ ./mvnw test -Dtest="CuradoriaAutorizacaoTest"

# Regras de bloqueio (não descartar emprestado/reservado)
$ ./mvnw test -Dtest="CuradoriaBloqueiosTest"
```

#### Checklist Executável da Fase (Scriba)

| Item | Comando/Validação | Critério de aprovação |
|---|---|---|
| Executar descarte com ADMIN em item elegível | `POST /curadoria/desbastamentos` tipo=DESCARTE | Item passa para `DESCARTADO`, `deleted_at` preenchido |
| Tentar descartar item emprestado/reservado | Mesmo POST em item não elegível | 422 com `BusinessException`, estado inalterado |
| Validar trilha de auditoria | `SELECT` em `desbastamento` | Registro contém `usuario`, `instituicao`, `tipo`, `justificativa`, `criado_em` |

#### Gate de Saída

- [ ] Descarte e remanejamento funcionais.
- [ ] Dupla proteção (`@PreAuthorize` + checagem programática) confirmada.
- [ ] Bloqueio de itens em `EMPRESTADO` / `RESERVADO` testado.
- [ ] Trilha auditável completa em `desbastamento`.

---

### Fase 8 — Portal do Leitor, Notificações e Relatórios

#### Objetivo

Completar a experiência do usuário final: portal de consulta para `LEITOR`, notificações de reserva/atraso e relatórios operacionais.

#### Pré-requisitos

- [ ] Fase 7 concluída.
- [ ] SMTP configurado no `application.yml` (variáveis `MAIL_*`).
- [ ] Fluxos de consulta definidos.
- [ ] Relatórios prioritários escolhidos pelo ADMIN.

#### Perguntas & Respostas de Validação

**P1. O leitor consegue consultar o acervo com segurança?**

> **Comando:**
> ```bash
> $ curl "http://localhost:8080/portal/acervo?busca=dom+casmurro" \
>     -b cookies_leitor.txt
> ```
> **Resultado esperado:** HTML do portal com resultados **apenas** da instituição do leitor e **apenas** de itens em `DISPONIVEL` ou `RESERVADO`. Itens em `EM_MANUTENCAO`, `DESCARTADO`, etc., não aparecem.

**P2. Um leitor consegue acessar áreas restritas do bibliotecário?**

> **Comando:**
> ```bash
> $ curl -i http://localhost:8080/acervo/itens -b cookies_leitor.txt
> ```
> **Resultado esperado:**
> ```
> HTTP/1.1 403 Forbidden
> ```

**P3. Uma reserva dispara notificação para o leitor?**

> **Comando:**
> ```bash
> $ curl -X POST http://localhost:8080/portal/reservas \
>     -b cookies_leitor.txt -H "X-XSRF-TOKEN: ..." \
>     -d "acervoItemId={uuid}"
> ```
> **Validação (log da aplicação):**
> ```
> INFO NotificacaoService: E-mail enviado para leitor@email.com — Reserva confirmada #abc123
> ```
> **Validação no SMTP de teste (MailHog / GreenMail):**
> ```bash
> $ curl http://localhost:8025/api/v2/messages | jq '.items[0].Content.Headers.Subject'
> "Reserva confirmada - Scriba"
> ```

**P4. O job agendado de notificação de atraso executa?**

> **Comando (rodar manualmente via endpoint de admin):**
> ```bash
> $ curl -X POST http://localhost:8080/admin/jobs/notificar-atrasos \
>     -b cookies_admin.txt -H "X-XSRF-TOKEN: ..."
> ```
> **Resultado esperado:** log informa quantas notificações foram enviadas. E-mails chegam na caixa dos leitores com empréstimos atrasados.

**P5. O relatório de empréstimos em aberto bate com o banco?**

> **Comando:**
> ```bash
> $ curl "http://localhost:8080/admin/relatorios/emprestimos-em-aberto?de=2026-01-01&ate=2026-04-22" \
>     -b cookies_admin.txt
> ```
> **Validação cruzada:**
> ```sql
> mariadb> SELECT COUNT(*) FROM emprestimo
>          WHERE status = 'ATIVO'
>          AND data_emprestimo BETWEEN '2026-01-01' AND '2026-04-22';
> ```
> **Resultado esperado:** o total do relatório deve ser **idêntico** à contagem do SQL.

#### Testes & Comandos

```bash
# Suíte do portal e notificações
$ ./mvnw test -Dtest="*Portal*,*Notificacao*,*Relatorio*"

# Teste de restrição por perfil
$ ./mvnw test -Dtest="PortalLeitorSegurancaTest"
```

#### Checklist Executável da Fase (Scriba)

| Item | Comando/Validação | Critério de aprovação |
|---|---|---|
| Consultar acervo como LEITOR com filtros | `GET /portal/acervo?busca=...` | Retorna apenas dados do tenant e do perfil logado |
| Criar reserva com notificação | `POST /portal/reservas` | Reserva persiste, evento de notificação disparado/registrado |
| Gerar relatório de empréstimos em aberto | `GET /admin/relatorios/...` | Total confere com SELECT direto no banco |

#### Gate de Saída

- [ ] Portal do leitor protegido com `ROLE_LEITOR`.
- [ ] Notificações de reserva e atraso funcionais.
- [ ] Relatórios conferem com o banco.
- [ ] Itens não-consultáveis (manutenção/descartados) ocultos do portal.

---

### Fase 9 — Hardening e Pré-Piloto

#### Objetivo

Elevar a maturidade operacional para entrada em piloto real: segurança de produção, observabilidade completa, backup/restore testados, validação de carga.

> 🔒 **Gate final.** Atende ao parecer de viabilidade (seção 0 do `PROJETO_DA_ARQUITETURA.md`). Mudança de status: **"GO condicionado" → "GO para piloto"**.

#### Pré-requisitos

- [ ] Fase 8 concluída.
- [ ] Ambiente de staging disponível.
- [ ] Certificado TLS provisionado.
- [ ] Variáveis `DB_PROD_PASS`, `MAIL_*` em arquivo `.env` isolado com permissão 600.

#### Perguntas & Respostas de Validação

**P1. Os endpoints do Actuator estão protegidos?**

> **Comando (não autenticado):**
> ```bash
> $ curl -i http://localhost:8080/actuator/metrics
> ```
> **Resultado esperado:**
> ```
> HTTP/1.1 401 Unauthorized
> ```
>
> **Comando (autenticado como ADMIN):**
> ```bash
> $ curl http://localhost:8080/actuator/health -b cookies_admin.txt
> ```
> **Resultado esperado:**
> ```json
> {"status":"UP","components":{"db":{"status":"UP"},"diskSpace":{"status":"UP"}}}
> ```

**P2. O rate limiting no login está ativo no NGINX?**

> **Comando (gerar 10 logins em sequência):**
> ```bash
> $ for i in $(seq 1 10); do
>     curl -s -o /dev/null -w "%{http_code}\n" -X POST https://localhost/login \
>         -d "email=x@y.z&senha=errada"
> done
> ```
> **Resultado esperado:** as primeiras 5 tentativas retornam `302` (login falhou) e as seguintes retornam `429 Too Many Requests`.
>
> ⚠️ Configuração esperada no `nginx.conf`:
> ```nginx
> limit_req_zone $binary_remote_addr zone=login:10m rate=5r/m;
> location /login { limit_req zone=login burst=5 nodelay; }
> ```

**P3. Os logs estão em formato JSON estruturado?**

> **Comando:**
> ```bash
> $ docker logs scriba-app-prod 2>&1 | head -5
> ```
> **Resultado esperado (uma linha por log, JSON válido):**
> ```json
> {"@timestamp":"2026-04-22T14:30:00Z","level":"INFO","logger":"br.dev.lourenco.scriba.modules.circulacao.service.CirculacaoService","message":"Empréstimo criado","instituicao_id":"...","usuario_id":"...","acervo_item_id":"...","operacao":"EMPRESTIMO","duracao_ms":142,"status":"OK"}
> ```

**P4. O backup diário é criado e íntegro?**

> **Comando:**
> ```bash
> $ make prod-db-backup
> $ ls -lh /var/backups/scriba/
> ```
> **Resultado esperado:** arquivo `scriba_<timestamp>.sql.gz` com tamanho > 0.
>
> **Teste de integridade:**
> ```bash
> $ gzip -t /var/backups/scriba/scriba_*.sql.gz && echo "OK" || echo "CORROMPIDO"
> ```
> **Resultado esperado:** `OK`.

**P5. O restore em ambiente isolado funciona?**

> **Comando:**
> ```bash
> # Subir banco isolado
> $ docker run -d --name mariadb-restore-test -e MARIADB_ROOT_PASSWORD=test \
>     -p 3399:3306 mariadb:11.4
>
> # Criar database
> $ docker exec mariadb-restore-test mariadb -uroot -ptest \
>     -e "CREATE DATABASE scriba_restored;"
>
> # Restaurar
> $ gunzip -c /var/backups/scriba/scriba_latest.sql.gz | \
>     docker exec -i mariadb-restore-test mariadb -uroot -ptest scriba_restored
>
> # Validar contagens críticas
> $ docker exec mariadb-restore-test mariadb -uroot -ptest scriba_restored \
>     -e "SELECT COUNT(*) AS usuarios FROM usuario;
>         SELECT COUNT(*) AS acervo FROM acervo_item;
>         SELECT COUNT(*) AS emprestimos FROM emprestimo WHERE status='ATIVO';"
> ```
> **Resultado esperado:** contagens **idênticas** às do banco de produção no momento do backup.

**P6. A aplicação suporta carga mínima (100 empréstimos simultâneos)?**

> **Script k6 em `scripts/load/emprestimo.js`:**
> ```javascript
> import http from 'k6/http';
> import { check } from 'k6';
>
> export const options = {
>     stages: [
>         { duration: '1m', target: 100 },
>         { duration: '3m', target: 100 },
>         { duration: '1m', target: 0 },
>     ],
>     thresholds: {
>         http_req_duration: ['p(95)<500'],
>         http_req_failed: ['rate<0.01'],
>     },
> };
>
> export default function () {
>     const res = http.post('https://staging.scriba.dev/circulacao/emprestimos', {...});
>     check(res, { 'status 2xx/3xx': (r) => r.status < 400 });
> }
> ```
> **Comando:**
> ```bash
> $ k6 run scripts/load/emprestimo.js
> ```
> **Resultado esperado:**
> ```
> ✓ http_req_duration.................: avg=210ms p(95)=480ms
> ✓ http_req_failed..................: 0.2%
> ```

**P7. Teste de chaos: matar o container do banco e validar recuperação?**

> **Comando:**
> ```bash
> # Com a app rodando sob carga leve, matar o db
> $ docker compose -f docker-compose.prod.yml kill db-prod
>
> # Aguardar 10s e reiniciar
> $ sleep 10
> $ docker compose -f docker-compose.prod.yml start db-prod
>
> # Checar que a app voltou a operar
> $ curl https://staging.scriba.dev/actuator/health -u admin:...
> ```
> **Resultado esperado:** após ~15-30s, `/health` volta a retornar `{"status":"UP"}` e nenhum dado foi corrompido (validar contagens críticas).

**P8. Reexecutar os testes críticos de tenant e concorrência?**

> **Comando:**
> ```bash
> $ ./mvnw test -Dtest="*TenantIsolation*,*Concorrencia*" -Dspring.profiles.active=test
> ```
> **Resultado esperado:** 100% verde.

#### Testes & Comandos

```bash
# Suíte completa de pré-piloto
$ ./mvnw verify -Dspring.profiles.active=test

# Auditoria de dependências
$ ./mvnw dependency:tree | grep -i "SNAPSHOT"
# Resultado esperado: vazio (nenhum SNAPSHOT em prod)

# Scan de vulnerabilidades (OWASP)
$ ./mvnw org.owasp:dependency-check-maven:check

# Carga com k6
$ k6 run scripts/load/emprestimo.js

# Chaos test manual
$ bash scripts/ops/chaos-test.sh
```

#### Checklist de Go-Live (extraído da seção 16.5 do projeto arquitetural)

**Segurança**
- [ ] Senha padrão do seed trocada.
- [ ] Certificado TLS válido instalado.
- [ ] Firewall: apenas porta 443 exposta externamente.
- [ ] Variáveis sensíveis não aparecem em logs.
- [ ] Rate limit no `/login` funcional (5 req/min).

**Banco de Dados**
- [ ] MariaDB 11.4 LTS via container dedicado.
- [ ] Usuário sem `GRANT ALL PRIVILEGES` no root.
- [ ] Backup automático configurado (cron 02:00).
- [ ] Restore testado em ambiente isolado.
- [ ] `flyway.clean-disabled=true` em prod.

**Aplicação**
- [ ] `APP_PROFILE=prod` confirmado.
- [ ] `spring.jpa.show-sql=false` em prod.
- [ ] `/actuator/**` protegido com `ROLE_ADMIN`.
- [ ] Logs em JSON estruturado.
- [ ] Healthcheck `/actuator/health` UP.

**Operação**
- [ ] Runbook de inicialização testado (`scripts/ops/start-prod.sh`).
- [ ] Runbook de rollback testado (`scripts/ops/rollback.sh`).
- [ ] Contato de plantão definido.
- [ ] Alertas configurados (JVM heap > 80%, erros 5xx > 1%, backup falho).

#### Checklist Executável da Fase (Scriba)

| Item | Comando/Validação | Critério de aprovação |
|---|---|---|
| Health check e métricas protegidos | `curl /actuator/health` com e sem auth | Sem auth = 401; com ADMIN = 200 UP |
| Backup e restore em ambiente isolado | `make prod-db-backup` + restore em DB dedicado | Restore conclui, contagens críticas batem |
| Carga mínima em circulação | `k6 run scripts/load/emprestimo.js` | p95 < 500ms, zero inconsistência de empréstimo |

#### Gate Final (GO para Piloto)

- [ ] Sem falhas críticas de tenant isolation (`TenantIsolation*Test` 100% verde).
- [ ] Sem inconsistência em cenários concorrentes de circulação (`EmprestimoConcorrenciaTest` 100% verde).
- [ ] Checklist operacional homologado (seção 16.5 do arquitetural completa).
- [ ] Teste de carga dentro de thresholds.
- [ ] Teste de chaos recupera sem corrupção.
- [ ] Runbooks executáveis (`start-prod.sh`, `rollback.sh`, `backup-now.sh`, `restore.sh`) testados em dry-run.

---

## 7. Backlog Prioritário Imediato

Ordem de execução recomendada quando múltiplas frentes estão abertas:

1. **Concluir baseline CORE com testes de tenant** (Fase 1) — base para tudo.
2. **Subir módulo de Usuários com RBAC completo** (Fase 2).
3. **Implementar módulo de Leitores** (Fase 3) — habilita circulação.
4. **Consolidar Catálogo e Acervo** (Fases 4 e 5) antes das regras de empréstimo.
5. **Circulação com testes concorrentes verdes** (Fase 6) — gate crítico.
6. **Curadoria auditável** (Fase 7).
7. **Portal + notificações** (Fase 8).
8. **Hardening + carga + runbook** (Fase 9) — go-live.

---

## 8. Modelo de Resposta para Condução Assistida

Ao finalizar qualquer fase, preencha o bloco abaixo e cole no PR:

```
Fase atual: <nome>
Objetivo resumido: <uma frase>
Preparação concluída: sim | não
Respostas Q&A:
  - P1: <resposta curta com evidência>
  - P2: <resposta curta com evidência>
  - ...
Testes executados:
  - <nome do teste>: <OK | FALHOU>
  - <comando de reprodução>
Pendências: <lista curta>
Decisão: avançar | não avançar | avançar com pendências não bloqueantes
Evidências anexadas:
  - <link para log>
  - <screenshot ou saída de terminal>
```

**Exemplo preenchido (Fase 6 — Circulação):**

```
Fase atual: Fase 6 — Módulo Circulação
Objetivo resumido: validar empréstimo, devolução, reserva, renovação e multa com integridade concorrente
Preparação concluída: sim
Respostas Q&A:
  - P1: Fluxo de empréstimo respeita as 6 verificações. Evidência: SELECT em emprestimo + acervo_item mostra status consistente.
  - P2: Devolução em atraso de 5 dias gerou multa R$ 2,50 (valorMulta=0.50 × 5 dias). Evidência: SELECT em multa.
  - P3: Concorrência protegida. Evidência: EmprestimoConcorrenciaTest verde (1 OK + 1 CONFLITO).
  - P4: Constraint uq_emprestimo_item_ativo impede duplo empréstimo mesmo via SQL direto. Evidência: INSERT direto retornou ERROR 1062.
  - P5: Fila FIFO confirmada. Evidência: SELECT posicao_fila ordenada corretamente.
  - P6: Renovação bloqueia na 4ª tentativa (limite=3). Evidência: curl sequencial.
Testes executados:
  - EmprestimoConcorrenciaTest: OK
  - MultaCalculoTest: OK
  - ReservaFilaFifoTest: OK
  - $ ./mvnw test -Dtest="*Circulacao*" → Tests run: 18, Failures: 0
Pendências: validar comportamento com volume de carga maior (Fase 9)
Decisão: avançar com pendências não bloqueantes
Evidências anexadas:
  - logs/fase6-concorrencia.log
  - print do dashboard de métricas
```

---

## 9. Regra Final de Avanço

Uma fase **só pode avançar** quando **todos** os critérios abaixo estiverem satisfeitos:

1. ✅ A preparação da fase está concluída (todos os pré-requisitos marcados).
2. ✅ Todas as perguntas de validação (Q&A) foram respondidas com evidência.
3. ✅ Os testes mínimos foram executados e estão verdes.
4. ✅ O gate de saída original da fase foi atendido.
5. ✅ O checklist transversal de encerramento está 100% verde.
6. ✅ Existe evidência mínima no PR para revisão posterior.

> **Regra de ouro:** se algum item crítico está `não` ou `parcial`, **não avançar**. Criar issue, resolver, e então avançar. Débito técnico de fase anterior compromete as seguintes.

---

*Atualizado em Abril/2026 · Companheiro operacional do `PROJETO_DA_ARQUITETURA.md`*
