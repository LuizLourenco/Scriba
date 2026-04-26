# Scriba — Documentação Arquitetural

> **Sistema de Gestão de Acervo para Instituições Comunitárias e Filantrópicas**  
> Versão `1.0.0-SNAPSHOT` · Java 25 · Spring Boot 4.0.0 · MariaDB 11.4  
> Repositório: [https://github.com/LuizLourenco/Scriba](https://github.com/LuizLourenco/Scriba)

---

## Índice

0. [Parecer de Viabilidade](#0-parecer-de-viabilidade)
1. [Visão do Sistema](#1-visão-do-sistema)
2. [Decisões Arquiteturais](#2-decisões-arquiteturais)
3. [Estrutura de Pacotes](#3-estrutura-de-pacotes)
4. [Camadas e Fluxo de Dados](#4-camadas-e-fluxo-de-dados)
5. [Modelo de Domínio](#5-modelo-de-domínio)
6. [Modelo de Dados (Schema)](#6-modelo-de-dados-schema)
7. [Módulos — Responsabilidades e Regras](#7-módulos--responsabilidades-e-regras)
8. [Segurança](#8-segurança)
9. [Configuração de Ambientes](#9-configuração-de-ambientes)
10. [Migrações Flyway](#10-migrações-flyway)
10.5 [Docker e Infraestrutura](#105-docker-e-infraestrutura)
11. [Stack Tecnológica](#11-stack-tecnológica)
12. [Padrões de Código](#12-padrões-de-código)
13. [Testes](#13-testes)
14. [Plano de Sprints](#14-plano-de-sprints)
15. [Riscos Detalhados e Mitigações](#15-riscos-detalhados-e-mitigações)
16. [Estratégia Operacional](#16-estratégia-operacional)
17. [Plano de Correção Pré-Piloto](#17-plano-de-correção-pré-piloto-2-sprints)
18. [Glossário](#18-glossário)

---

## 0. Parecer de Viabilidade

> **Revisão arquitetural externa** · Abril/2025

### Veredito

> **GO com ressalvas — Viabilidade Condicionada**

A proposta é tecnicamente viável e bem estruturada para o contexto de bibliotecas comunitárias. A arquitetura está alinhada ao porte do domínio e as decisões de modelagem são maduras. Contudo, o projeto **não deve ir a produção** sem implementar as 6 mitigações prioritárias listadas na seção 15.

O maior risco ativo é a combinação de **isolamento de tenant apenas em aplicação** com **ausência de controle de concorrência em circulação** — os dois pontos de maior impacto no caso de incidente.

---

### O que está sólido

| Aspecto | Avaliação |
|---|---|
| Arquitetura monolito modular | ✅ Adequada ao porte e ao contexto operacional |
| Separação Controller / Service / Mapper / Repository | ✅ Clara, consistente e verificável por lint |
| Decisão de domínio (catálogo vs acervo, state machine, curadoria auditável) | ✅ Madura e alinhada ao negócio |
| Multi-tenancy por `instituicao_id` | ✅ Simplifica operação e custo |
| Segurança base (RBAC + CSRF + BCrypt + Flyway versionado) | ✅ Cobre o essencial do MVP |

---

### Riscos Priorizados por Severidade

#### 🔴 Críticos — bloqueiam a ida a produção

| # | Risco | Impacto |
|---|---|---|
| R1 | **Isolamento de tenant apenas na aplicação** — uma query sem filtro vaza dados entre instituições | Segurança / Compliance |
| R2 | **Condição de corrida em circulação** — duas operações simultâneas no mesmo item geram inconsistência | Integridade de negócio |

#### 🟠 Altos — devem ser resolvidos antes do primeiro uso real

| # | Risco | Impacto |
|---|---|---|
| R3 | **Stack em versão de fronteira** — Spring Boot 4.0.0 snapshot pode gerar instabilidade e custo de upgrade | Prazo e manutenção |
| R4 | ✅ RESOLVIDO — MariaDB 11.4 em todos os ambientes (dev, test, prod) via Docker Compose, eliminando gap de banco | — |
| R5 | **Hardening adiado para Sprint 6** — problemas de segurança e performance descobertos tarde encarecem a correção | Cronograma |

#### 🟡 Médios — gerenciáveis com monitoramento

| # | Risco | Impacto |
|---|---|---|
| R6 | **Herança JOINED no acervo** — degradação de consultas polimórficas com crescimento da base | Performance |
| R7 | **Entidades globais (Autor, Editora) em multi-tenant** — governança de qualidade de cadastro sem dono definido | Qualidade de dados |
| R8 | **Ausência de estratégia operacional explícita** — backup/restore, observabilidade e SLA não documentados | Operação real |

---

### 6 Mitigações Mínimas Antes de Produção

As ações abaixo são **pré-requisito de go-live**, não melhorias opcionais:

| Prioridade | Ação |
|---|---|
| **1** | ✅ Resolvido (V7) — MariaDB real em todos os ambientes via Docker. Não é mais necessário Testcontainers. |
| **2** | Blindar tenant com enforcement central auditável + testes de fronteira por módulo — elimina R1 |
| **3** | Adicionar controle de concorrência (lock otimista `@Version` ou pessimista `SELECT FOR UPDATE`) + constraints únicos no banco — elimina R2 |
| **4** | Congelar stack em versões estáveis e remover dependências snapshot do `pom.xml` — reduz R3 |
| **5** | Antecipar hardening para Sprint 3/4: segurança, carga mínima, resiliência básica — reduz R5 |
| **6** | Formalizar operação: backup testado, restore ensaiado, logs estruturados (JSON), métricas e alertas — elimina R8 |

---

### Resumo Executivo

A proposta é **forte no desenho de domínio e modularização**. Os riscos relevantes estão **menos na modelagem** e **mais em**:
- segurança de dados entre tenants,
- controle de concorrência nas operações de circulação,
- maturidade da stack escolhida, e
- prontidão operacional para produção real.

Seções 15 e 16 deste documento detalham as ações de mitigação e a estratégia operacional recomendada.

---
## 1. Visão do Sistema

### 1.1 Propósito

O **Scriba** é um sistema web para gestão de acervo físico voltado a bibliotecas comunitárias e instituições filantrópicas. Centraliza o controle de itens do acervo (livros, periódicos, cartas, fotografias e mídias), as operações de circulação (empréstimo, devolução, reserva) e as atividades de curadoria (descarte e remanejamento).

### 1.2 Requisitos Não-Funcionais Centrais

| Atributo | Decisão |
|---|---|
| **Operabilidade** | Deploy como Fat JAR executável único: `java -jar scriba.jar` |
| **Multi-tenancy** | Múltiplas instituições no mesmo banco, isoladas por `instituicao_id` |
| **Configurabilidade** | Regras de empréstimo configuráveis por instituição sem redeployment |
| **Rastreabilidade** | Todas as operações de curadoria são registros permanentes e auditáveis |
| **Segurança** | RBAC com três perfis; CSRF protegido para compatibilidade com HTMX |

### 1.3 Perfis de Acesso

| Role | Capacidades |
|---|---|
| `ADMIN` | Acesso total. Configura regras, gerencia usuários, aprova descartes. |
| `BIBLIOTECARIO` | Operações do dia a dia: catálogo, acervo, circulação, pessoas. |
| `LEITOR` | Portal de consulta: pesquisa acervo e solicita reservas online. |

---

## 2. Decisões Arquiteturais

### 2.1 Monolito Modular

**Decisão:** Monolito modular com fronteiras internas por domínio de negócio.

**Contexto:** Bibliotecas comunitárias têm equipes enxutas, infraestrutura simples e sem necessidade de escala independente por serviço.

**Consequências:**
- ✅ Deploy trivial — um único JAR com Tomcat embutido.
- ✅ Transações distribuídas desnecessárias — circulação é atômica num único banco.
- ✅ Módulos já isolados — extração futura para microsserviços é cirúrgica.
- ⚠️ Escalabilidade horizontal limitada ao JAR inteiro (aceitável para o contexto).

### 2.2 Package by Feature

**Decisão:** Organização de pacotes por funcionalidade de negócio, não por camada técnica.

```
# ✅ Package by Feature (adotado)
modules/catalogo/domain/
modules/catalogo/service/
modules/catalogo/controller/

# ❌ Package by Layer (rejeitado)
domain/Autor.java
service/AutorService.java
controller/AutorController.java
```

**Motivo:** Coesão por domínio — todos os artefatos de um contexto ficam juntos, facilitando localização e evolução independente do módulo.

### 2.3 Separação Estrita de Camadas

**Decisão:** Isolamento rígido entre `Controller` (DTOs) e `Service` (Entities), com `Mapper` como ponte exclusiva.

```
Controller  → recebe/retorna  DTOs    (Java records)
Mapper      → converte        DTO ↔ Entity
Service     → recebe/retorna  @Entity
```

**Regra inviolável:** `Service` nunca importa classes de `dto`. `Controller` nunca importa `@Repository`.

### 2.4 Herança JPA — Estratégia JOINED

**Decisão:** `InheritanceType.JOINED` para a hierarquia de `AcervoItem`.

| Estratégia | Avaliação |
|---|---|
| `SINGLE_TABLE` | ❌ Gera ~50 colunas nullable. Viola integridade. |
| `TABLE_PER_CLASS` | ❌ `UNION ALL` em consultas polimórficas. Sem FK na base. |
| **`JOINED`** ✅ | Uma tabela base + uma por subtipo. Integridade preservada. |

### 2.5 Padrão State via Enum (StatusAcervo)

**Decisão:** O enum `StatusAcervo` encapsula as transições válidas de estado, implementando o padrão State.

**Motivo:** Elimina condicionais espalhados nos Services. Cada estado sabe para onde pode ir — uma transição inválida lança `BusinessException` antes de qualquer persistência.

### 2.6 MariaDB em Todos os Ambientes

**Decisão:** MariaDB 11.4 LTS via Docker em todos os ambientes (dev, test, prod).

**Contexto:** A solução anterior usava H2 em dev/test, criando um gap de comportamento que poderia ocultar bugs de produção (ausência de partial indexes, diferenças de UUID, `COMMENT ON COLUMN` incompatível).

**Consequências:**
- ✅ Paridade total dev/test/prod — o que funciona no container dev funciona em produção.
- ✅ Sem surpresas com migrations — Flyway executa no mesmo engine em todos os ambientes.
- ✅ Docker Compose isola cada ambiente sem conflito de portas (dev=3306, test=3307).
- ⚠️ Requer Docker instalado na máquina de desenvolvimento.

### 2.7 Multi-tenancy por Discriminador de Coluna

**Decisão:** Todas as entidades de domínio carregam `instituicao_id`. O isolamento é feito em aplicação (não em banco).

**Motivo:** Simplicidade operacional. Schema-per-tenant exigiria Flyway por tenant e complicaria queries cross-institution (relatórios futuros). O volume de dados de uma biblioteca comunitária não justifica esse custo.

---

## 3. Estrutura de Pacotes

```
br.dev.lourenco.scriba/
│
├── ScribaApplication.java                       ← @SpringBootApplication + @ConfigurationPropertiesScan
│
├── core/                                        ── infraestrutura transversal ──
│   ├── config/
│   │   ├── JpaConfig.java                       ← @EnableJpaAuditing, AuditorAware
│   │   └── SecurityConfig.java                  ← Spring Security, CSRF, rotas
│   ├── domain/
│   │   ├── BaseEntity.java                      ← id UUID + criadoEm + atualizadoEm
│   │   └── TenantEntity.java                    ← BaseEntity + instituicaoId
│   ├── exception/
│   │   ├── BusinessException.java               ← HTTP 422 Unprocessable Entity
│   │   ├── ResourceNotFoundException.java       ← HTTP 404 Not Found
│   │   └── GlobalExceptionHandler.java          ← @ControllerAdvice, RFC 7807, detecção HX-Request
│   └── security/
│       ├── UserDetailsImpl.java                 ← wrapper de Usuario p/ Spring Security
│       ├── UserDetailsServiceImpl.java          ← carrega usuário por e-mail
│       └── annotation/
│           └── CurrentUser.java                 ← @AuthenticationPrincipal tipado
│
└── modules/                                     ── domínios de negócio ──
    │
    ├── administracao/
    │   ├── domain/
    │   │   ├── Instituicao.java                 ← raiz de multi-tenancy
    │   │   ├── RegraEmprestimo.java             ← @Embeddable, 16 parâmetros configuráveis
    │   │   ├── Biblioteca.java                  ← filial física (1..N por instituição)
    │   │   ├── Usuario.java                     ← funcionários do sistema
    │   │   ├── Role.java                        ← enum: ADMIN | BIBLIOTECARIO | LEITOR
    │   │   ├── TipoMulta.java                   ← enum: FIXO_DIARIO | PERCENTUAL
    │   │   └── PadraoClassificacao.java         ← enum: CDD | CDU | LIVRE
    │   └── repository/
    │       ├── InstituicaoRepository.java
    │       └── UsuarioRepository.java
    │
    ├── catalogo/
    │   ├── domain/
    │   │   ├── Autor.java                       ← entidade global (sem instituicaoId)
    │   │   ├── Editora.java                     ← entidade global (sem instituicaoId)
    │   │   ├── Categoria.java                   ← por instituição, hierarquia pai/filho
    │   │   ├── Classificacao.java               ← por instituição (CDD/CDU/LIVRE)
    │   │   └── RoleAutor.java                   ← enum: AUTOR | CO_AUTOR | AUTOR_ESPIRITUAL
    │   ├── dto/
    │   │   └── CatalogoDtos.java                ← records: AutorRequest/Response, Editora, Categoria, Classificacao
    │   ├── mapper/
    │   │   └── CatalogoMappers.java             ← AutorMapper, EditoraMapper, CategoriaMapper, ClassificacaoMapper
    │   ├── repository/
    │   │   └── CatalogoRepositories.java        ← AutorRepository, EditoraRepository, CategoriaRepository, ClassificacaoRepository
    │   ├── service/
    │   │   ├── AutorService.java
    │   │   └── CatalogoServices.java            ← Editora + Categoria + Classificacao
    │   └── controller/
    │       ├── AutorController.java             ← modelo canônico do projeto
    │       └── CatalogoControllers.java         ← Editora + Categoria + Classificacao
    │
    ├── acervo/
    │   ├── domain/
    │   │   ├── AcervoItem.java                  ← @Entity abstrata, @Inheritance(JOINED)
    │   │   ├── AcervoSubtipos.java              ← Livro, Periodico, Carta, Foto, Midia
    │   │   ├── AcervoItemAutor.java             ← associação genérica Item-Autor (Livro/Carta/Foto/Mídia)
    │   │   ├── AcervoItemAutorId.java           ← @EmbeddedId (acervoItemId + autorId)
    │   │   ├── StatusAcervo.java                ← enum State: 8 estados + transições
    │   │   ├── TipoPeriodico.java               ← enum: JORNAL | REVISTA
    │   │   └── TipoMidia.java                   ← enum: DISCO | VIDEO | DVD | CD
    │   ├── dto/
    │   │   └── AcervoDtos.java                  ← AcervoItemRequest (unificado), ResumoResponse, DetalheResponse, MudarStatusRequest
    │   ├── repository/
    │   │   └── AcervoItemRepository.java        ← busca polimórfica, soft-delete, contadores
    │   └── service/
    │       └── AcervoService.java               ← Factory interno + State + CRUD polimórfico
    │
    ├── circulacao/
    │   ├── domain/
    │   │   └── CirculacaoDomain.java            ← Emprestimo, Reserva, Multa + enums de status
    │   ├── repository/
    │   │   └── CirculacaoRepositories.java      ← EmprestimoRepository, ReservaRepository, MultaRepository
    │   └── service/
    │       └── CirculacaoService.java           ← empréstimo, devolução, renovação, reserva, multa
    │
    ├── curadoria/
    │   ├── domain/
    │   │   └── Desbastamento.java               ← registro auditável (DESCARTE | REMANEJAMENTO)
    │   ├── repository/
    │   │   └── DesbastamentoRepository.java
    │   └── service/
    │       └── CuradoriaService.java            ← @PreAuthorize("hasRole('ADMIN')")
    │
    └── pessoas/
        └── domain/                              ← estrutura criada, implementação Sprint 4
            ├── Leitor.java
            ├── TipoLeitor.java
            └── Fornecedor.java
```

**Recursos (`src/main/resources/`):**

```
application.yml                    ← configuração base (todos os ambientes)
application-dev.yml                ← MariaDB dev, DevTools, SQL log
application-test.yml               ← MariaDB test, porta 3307
application-prod.yml               ← MariaDB prod, HikariCP, log JSON
db/migration/
  V1__schema_administracao.sql
messages/messages.properties       ← validações Bean Validation em PT-BR
static/manifest.json               ← PWA manifest
templates/
  layout/base.html                 ← layout mestre (Thymeleaf + HTMX + Tailwind)
  auth/login.html
  catalogo/autores/{list,form}.html
  error/{htmx-error,page-error}.html
```

---

## 4. Camadas e Fluxo de Dados

### 4.1 Fluxo de uma Requisição Típica

```
Browser
  │  HTTP GET/POST (Thymeleaf form ou hx-post HTMX)
  ▼
┌─────────────────────────────────────────────────────┐
│  Controller  (@Controller)                          │
│  • Extrai tenant de @CurrentUser                    │
│  • Converte Request DTO → Entity via Mapper         │
│  • Chama Service com Entity                         │
│  • Converte Entity → Response DTO via Mapper        │
│  • Retorna view name (Thymeleaf) ou fragmento HTMX  │
└───────────────────┬─────────────────────────────────┘
                    │ @Entity
                    ▼
┌─────────────────────────────────────────────────────┐
│  Service  (@Service, @Transactional)                │
│  • Aplica regras de negócio                         │
│  • Lê RegraEmprestimo da Instituicao                │
│  • Delega transições ao enum StatusAcervo           │
│  • Lança BusinessException / ResourceNotFound       │
└───────────────────┬─────────────────────────────────┘
                    │ Spring Data JPA
                    ▼
┌─────────────────────────────────────────────────────┐
│  Repository  (JpaRepository)                        │
│  • Queries derivadas do nome do método              │
│  • JPQL para buscas polimórficas                    │
│  • Sempre filtrado por instituicaoId                │
└───────────────────┬─────────────────────────────────┘
                    │ JDBC / HikariCP
                    ▼
         MariaDB 11.4 (Docker)
```

### 4.2 Tratamento de Erros

```
Exceção lançada no Service
  │
  ▼
GlobalExceptionHandler (@ControllerAdvice)
  │
  ├── Detecta HX-Request: true?
  │     ├── SIM → retorna fragmento HTML (error/htmx-error.html)
  │     └── NÃO → retorna ProblemDetail RFC 7807 (error/page-error.html)
  │
  └── Mapeia por tipo:
        BusinessException          → HTTP 422
        ResourceNotFoundException  → HTTP 404
        MethodArgumentNotValid     → HTTP 400 (com mapa de erros por campo)
        Exception                  → HTTP 500
```

### 4.3 Fluxo HTMX (Sem Reload de Página)

```
Usuario digita na busca
  │ hx-get="/catalogo/autores" hx-trigger="input delay:400ms"
  ▼
Controller retorna apenas o fragmento "tabela-autores"
  │ th:fragment="tabela-autores"
  ▼
HTMX substitui #tabela-autores-wrapper no DOM
  │ hx-target="#tabela-autores-wrapper" hx-swap="innerHTML"
  ▼
Página não recarrega — apenas a tabela é atualizada
```

---

## 5. Modelo de Domínio

### 5.1 Hierarquia do Acervo (JOINED)

```
AcervoItem  (tabela: acervo_item)  ← campos comuns
  │  id, titulo, status, codigoBarras, tombamento,
  │  localizacao, dataAquisicao, instituicao_id, biblioteca_id
  │
  ├── Livro        (acervo_livro)      isbn, paginas, edicao, volume
  │     └── acervo_livro_categoria     livro_id, categoria_id
  │
  │  [autores, mediador e role_autor herdados de AcervoItem → acervo_item_autor]
  │
  ├── Periodico    (acervo_periodico)  tipo(JORNAL|REVISTA), issn, volume, numero
  ├── Carta        (acervo_carta)      remetente, destinatario, dataEnvio
  ├── Foto         (acervo_foto)       assunto, fotografo, formato, resolucao
  └── Midia        (acervo_midia)      tipo(DISCO|VIDEO|DVD|CD), duracao, produtora
```

### 5.2 Máquina de Estados — StatusAcervo

```
                    ┌──────────────────────────────┐
                    │         DISPONIVEL           │◄──────────────────┐
                    └──┬───────┬──────┬───────┬────┘                   │
                       │       │      │       │                         │
               reserva()│ empr()│ manu()│ restr()│              devolucao()│
                       │       │      │       │                         │
                    ┌──▼──┐ ┌──▼───┐ ┌▼─────┐ ┌▼────────┐   ┌─────────┴──────┐
                    │RESER│ │EMPR  │ │MANUT │ │USO      │   │    EMPRESTADO  │
                    │VADO │ │ADO   │ │ENCAO │ │INTERNO  │   │                │
                    └──┬──┘ └──┬───┘ └──┬───┘ └────┬────┘   └──────┬─────────┘
                       │       │        │           │               │
               retirada()│       │  recupera()│      │         devolv()│ extravio()
                       │       │        │           │               │         │
                    ┌──▼──────▼──┐  ┌──▼──────┐ ┌──▼────────┐     │   ┌─────▼──────┐
                    │  EMPRESTADO │  │DISPONIV.│ │DISPONIVEL │     │   │ EXTRAVIADO │
                    └─────────────┘  └─────────┘ └───────────┘     │   └────┬───────┘
                                                                    │        │
                    ┌───────────────┐  ┌──────────────┐            │  encontrado()
                    │  REMANEJADO   │◄─┤ EM_MANUTENCAO│            │        │
                    │  (terminal)   │  └──────┬───────┘            │   ┌────▼──────┐
                    └───────────────┘         │                    │   │DISPONIVEL │
                    ┌───────────────┐  descarte()                  │   └───────────┘
                    │  DESCARTADO   │◄─────────┘                   │
                    │  (soft-delete)│                               │
                    └───────────────┘                               │
                                                            ◄───────┘
```

### 5.3 Papel de Autoria (RoleAutor)

A relação `AcervoItem ↔ Autor` usa `AcervoItemAutor` (genérica) para registrar o papel em qualquer subtipo do acervo:

| Enum | Significado |
|---|---|
| `AUTOR` | Autoria principal e intelectual. Valor padrão. |
| `CO_AUTOR` | Coautoria — participação equivalente na criação. |
| `AUTOR_ESPIRITUAL` | Obra atribuída a entidade espiritual (psicografia). O receptor é registrado no campo `mediador` da entidade `Livro`. |

```java
// Exemplo de uso no AcervoService
livro.adicionarAutor(entidadeEspiritual, RoleAutor.AUTOR_ESPIRITUAL);
livro.adicionarAutor(outroContribuidor,  RoleAutor.CO_AUTOR);
livro.setMediador("Francisco Cândido Xavier");
```

### 5.4 Regras de Empréstimo (RegraEmprestimo)

`@Embeddable` armazenado diretamente em `Instituicao`. Lido em tempo de execução pela `CirculacaoService`.

| Campo | Padrão | Descrição |
|---|---|---|
| `prazoPadraoDias` | 14 | Prazo padrão de devolução |
| `prazoLivroDias` | 14 | Prazo específico para livros |
| `prazoPeriodicoDias` | 7 | Prazo para periódicos |
| `prazoMidiaDias` | 7 | Prazo para mídias |
| `limiteEmprestimos` | 5 | Máximo de itens simultâneos por leitor |
| `limiteReservas` | 3 | Máximo de reservas ativas por leitor |
| `maximoRenovacoes` | 3 | Renovações permitidas por empréstimo |
| `diasExpiracaoReserva` | 7 | Prazo para retirada de reserva |
| `multaAtivada` | `true` | Liga/desliga cobrança de multas |
| `tipoMulta` | `FIXO_DIARIO` | Modalidade: valor fixo ou percentual |
| `valorMulta` | R$ 0,50 | Valor base por dia de atraso |
| `tetoMaximoMulta` | `null` | Teto da multa (`null` = sem limite) |
| `bloqueioComMulta` | `true` | Bloqueia novos empréstimos com multa pendente |
| `isencaoPermitida` | `true` | ADMIN pode isentar multas |
| `classificacaoPadrao` | `CDD` | Padrão bibliográfico da instituição |

---

## 6. Modelo de Dados (Schema)

### 6.1 Diagrama de Tabelas

```
┌─────────────────┐     1     N ┌──────────────┐
│   instituicao   ├─────────────┤  biblioteca  │
│  (+ regras emb) │             └──────┬───────┘
└────────┬────────┘                    │ N
         │ 1                           │
         │ N            ┌──────────────▼───────────┐
    ┌────▼────┐          │         acervo_item      │
    │ usuario │          │  (base JOINED)           │
    └─────────┘          │  status, titulo, etc.    │
                         └──────┬───────────────────┘
                                │ (herança JOINED)
              ┌─────────────────┼──────────────────┬──────────────────┐
              │                 │                  │                  │
         ┌────▼─────┐  ┌────────▼────┐  ┌─────────▼───┐  ┌──────────▼──┐
         │acervo    │  │acervo_livro │  │acervo_carta │  │acervo_foto  │
         │_periodico│  └──────┬──────┘  └─────────────┘  └─────────────┘
         └──────────┘         │ 1                         ┌─────────────┐
                              │ N                         │acervo_midia │
                       ┌──────▼────────┐                  └─────────────┘
                       │acervo_livro   │
                       │_autor         │  (role_autor: AUTOR|CO_AUTOR|ESPIRITUAL)
                       └──────┬────────┘
                              │ N
                         ┌────▼────┐
                         │  autor  │  (global)
                         └─────────┘

┌────────────┐  N  ┌────────────┐  N  ┌──────────┐
│ emprestimo ├─────┤   multa    │     │  reserva │
└────────────┘     └────────────┘     └──────────┘

┌───────────────┐
│ desbastamento │  (DESCARTE | REMANEJAMENTO)
└───────────────┘
```

### 6.2 Tabelas por Módulo

| Tabela | Módulo | Escopos |
|---|---|---|
| `instituicao` | administracao | global |
| `biblioteca` | administracao | por instituição |
| `usuario` | administracao | por instituição |
| `autor` | catalogo | por instituição |
| `editora` | catalogo | por instituição |
| `categoria` | catalogo | por instituição |
| `classificacao` | catalogo | por instituição |
| `tipo_leitor` | pessoas | por instituição |
| `leitor` | pessoas | por instituição |
| `fornecedor` | pessoas | por instituição |
| `acervo_item` | acervo | por instituição |
| `acervo_livro` | acervo | por instituição |
| `acervo_item_autor` | acervo | associação genérica: `role_autor` em Livro/Carta/Foto/Mídia |
| `acervo_livro_categoria` | acervo | associação |
| `acervo_periodico` | acervo | por instituição |
| `acervo_carta` | acervo | por instituição |
| `acervo_foto` | acervo | por instituição |
| `acervo_midia` | acervo | por instituição |
| `emprestimo` | circulacao | por instituição |
| `reserva` | circulacao | por instituição |
| `multa` | circulacao | por instituição |
| `desbastamento` | curadoria | por instituição |

---

## 7. Módulos — Responsabilidades e Regras

### 7.1 `administracao`

**Entidades:** `Instituicao`, `RegraEmprestimo` (embedded), `Biblioteca`, `Usuario`

**Regras:**
- Cada `Instituicao` é a âncora de todos os dados — sua PK aparece como FK em toda entidade tenant-aware.
- `RegraEmprestimo` é embeddable: alterações feitas pelo ADMIN entram em vigor imediatamente, sem redeployment.
- `Biblioteca` representa filiais físicas. O remanejamento de acervo ocorre entre filiais (`destino_biblioteca_id`) ou entre instituições (`destino_instituicao_id`).

---

### 7.2 `catalogo`

**Entidades:** `Autor`, `Editora` (globais), `Categoria`, `Classificacao` (por instituição), `RoleAutor` (enum)

**Regras:**
- `Autor` e `Editora` são **por instituição** — cada biblioteca mantém seu próprio vocabulário controlado de autores e editoras, evitando conflitos de qualidade de dados em ambiente multi-tenant.
- `Categoria` suporta hierarquia pai/filho (profundidade máxima recomendada: 2 níveis).
- `Classificacao` segue o padrão definido em `RegraEmprestimo.classificacaoPadrao`.
- **O catálogo representa a obra intelectual; o acervo representa o exemplar físico.** Um registro de `Livro` em catálogo pode corresponder a N cópias no acervo.
- `RoleAutor` agora se aplica a **todos os subtipos** do acervo via `AcervoItemAutor`: Livro (autor literário), Carta (remetente-autor de valor histórico), Foto (fotógrafo) e Mídia (compositor, diretor).

---

### 7.3 `acervo`

**Entidades:** `AcervoItem` (abstrata, JOINED), `Livro`, `Periodico`, `Carta`, `Foto`, `Midia`, `AcervoItemAutor`, `AcervoItemAutorId`, enums

**Regras:**
- O `AcervoService` implementa um **Factory interno** que instancia o subtipo correto com base em `tipoItem` do DTO, usando `switch` com pattern matching do Java 25.
- Transições de estado são delegadas ao enum `StatusAcervo.transicionar()` — o Service não decide a validade, ele apenas invoca.
- `Soft-delete` exclusivo ao status `DESCARTADO` — somente itens descartados têm `deleted_at` preenchido.
- A relação `AcervoItem ↔ Autor` usa `AcervoItemAutor` (genérica) para comportar o campo `role_autor` em Livro, Carta, Foto e Mídia.

---

### 7.4 `circulacao`

**Entidades:** `Emprestimo`, `Reserva`, `Multa`

**Fluxo de Empréstimo:**
1. Verificar multa pendente se `bloqueioComMulta = true`.
2. Verificar limite de empréstimos simultâneos do leitor.
3. Verificar disponibilidade: item deve estar em `DISPONIVEL` ou `RESERVADO` pelo próprio leitor.
4. Calcular prazo: `LocalDate.now() + prazo_por_tipo_item` (lido de `RegraEmprestimo`).
5. Transicionar item para `EMPRESTADO`.
6. Cancelar reserva ativa do leitor para este item.

**Fluxo de Devolução:**
1. Se `estaAtrasado()` → gerar `Multa` (`valor = valorMulta × diasAtraso`, com teto opcional).
2. Registrar `dataEfetivaDevolucao`.
3. Verificar fila: se há reserva `AGUARDANDO` → item vai para `RESERVADO`; senão vai para `DISPONIVEL`.

**Fila de Reserva:**
- Política **FIFO** por `posicaoFila`.
- Reserva expira após `diasExpiracaoReserva` dias sem retirada (job agendado — Sprint 5).

---

### 7.5 `curadoria`

**Entidades:** `Desbastamento`, `TipoDesbastamento` (enum)

**Regras:**
- **Dupla proteção:** `@PreAuthorize("hasRole('ADMIN')")` no Service + validação programática do role.
- **Descarte:** item → `DESCARTADO` (soft-delete, `deletedAt` preenchido). Irreversível.
- **Remanejamento:** item → `REMANEJADO` (terminal nesta instituição). Registra destino.
- Itens em `EMPRESTADO` ou `RESERVADO` não podem ser descartados nem remanejados.
- `justificativa` é obrigatória em ambas as operações.

---

## 8. Segurança

### 8.1 Controle de Acesso por Rota

```yaml
/login, /static/**, /manifest.json    → público
/portal/**                            → ROLE_LEITOR
/admin/**                             → ROLE_ADMIN
/catalogo/**, /acervo/**,
/circulacao/**, /pessoas/**           → ROLE_ADMIN ou ROLE_BIBLIOTECARIO
/curadoria/** (descarte/remanejamento)→ ROLE_ADMIN (@PreAuthorize)
```

### 8.2 Proteção CSRF com HTMX

O Spring Security usa `CookieCsrfTokenRepository` com `httpOnly=false` para que o JavaScript do HTMX leia o cookie `XSRF-TOKEN` e o envie no header `X-XSRF-TOKEN` em toda requisição mutante:

```javascript
// layout/base.html — configurado globalmente
document.body.addEventListener('htmx:configRequest', (e) => {
    e.detail.headers[csrfHeader] = csrfToken;
});
```

### 8.3 Senhas e Credenciais

- Senhas armazenadas com **BCrypt strength 12**.
- Credenciais sensíveis injetadas via variáveis de ambiente — nunca em código ou em arquivos commitados.
- Arquivo `.env` bloqueado no `.gitignore`.

### 8.4 Headers de Segurança

| Header | Valor | Propósito |
|---|---|---|
| `X-Frame-Options` | `SAMEORIGIN` | Previne clickjacking |
| `Strict-Transport-Security` | `max-age=31536000; includeSubDomains` | Força HTTPS em produção |

---

## 9. Configuração de Ambientes

### 9.1 Perfis

| Perfil | Banco | Container | Porta |
|---|---|---|---|
| `dev` | MariaDB 11.4 — `scriba_dev` | `db-dev` (docker-compose.yml) | 3306 |
| `test` | MariaDB 11.4 — `scriba_test` | `db-test` (docker-compose.test.yml) | 3307 |
| `prod` | MariaDB 11.4 — `scriba_prod` | `db-prod` (docker-compose.prod.yml) | interno |

### 9.2 Variáveis de Ambiente (`.env`)

```bash
APP_PROFILE=dev

# Banco de dados (obrigatório apenas em prod)
DB_HOST=localhost
DB_PORT=5432
DB_NAME=scriba
DB_USERNAME=scriba_user
DB_PASSWORD=senha_forte

# E-mail SMTP
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=noreply@suabiblioteca.org.br
MAIL_PASSWORD=senha_app_gmail

# Segurança — gerar com: openssl rand -base64 64
JWT_SECRET=SEGREDO_FORTE_64_CHARS_MINIMO

# Aplicação
APP_URL=http://localhost:8080
APP_NAME=Scriba
```

### 9.3 HikariCP (Produção)

```yaml
hikari:
  pool-name: ScribaPool
  maximum-pool-size: 20
  minimum-idle: 5
  connection-timeout: 30000      # 30s
  idle-timeout: 600000           # 10min
  max-lifetime: 1800000          # 30min
  keepalive-time: 60000          # 1min
```

---

## 10. Migrações Flyway

| Versão | Arquivo | Conteúdo |
|---|---|---|
| V1 | `V1__schema_administracao.sql` | `instituicao`, `biblioteca`, `usuario` |

No estado atual do repositório, apenas a migration `V1__schema_administracao.sql` está presente em `src/main/resources/db/migration/`.

> **Regra:** `flyway.clean-disabled=true` no perfil `prod`. Nunca limpar o banco de produção automaticamente.

### 10.5 Docker e Infraestrutura

#### Arquivos de Infraestrutura

```
scriba/
├── Dockerfile                      ← multi-stage: build (JDK 25) + runtime (JRE 25)
├── docker-compose.yml              ← ambiente dev
├── docker-compose.test.yml         ← ambiente test (MariaDB na porta 3307)
├── docker-compose.prod.yml         ← ambiente prod (banco interno, sem porta exposta)
├── Makefile                        ← atalhos: make dev, make test, make prod-up
├── .dockerignore
├── nginx/
│   ├── nginx.dev.conf              ← proxy simples HTTP (dev)
│   ├── nginx.conf                  ← proxy HTTPS + SSL + rate limiting (prod)
│   └── ssl/                        ← certificados TLS (não versionados)
└── docker/
    └── mariadb/
        ├── init-dev.sql            ← charset UTF8MB4 no primeiro start
        └── my.prod.cnf             ← tuning: buffer pool 512MB, slow query log
```

#### Bancos por Ambiente

| Ambiente | Host Docker | Porta | Banco | Exposto externamente |
|---|---|---|---|---|
| dev | `db-dev` | 3306 | `scriba_dev` | ✅ porta 3306 do host |
| test | `db-test` | 3307 | `scriba_test` | ✅ porta 3307 do host |
| prod | `db-prod` | 3306 | `scriba_prod` | ❌ apenas rede interna |

#### Comandos Rápidos (Makefile)

```bash
make dev          # Sobe MariaDB dev + app + NGINX em segundo plano
make test         # Sobe MariaDB test, executa mvn test, derruba tudo
make build        # Constrói imagem scriba:latest
make prod-up      # Sobe prod (MariaDB prod + app + NGINX com SSL)
make prod-db-backup  # Dump do banco de produção com timestamp
make dev-db       # Abre client MariaDB no container dev
```

#### Arquitetura Docker (Produção)

```
Internet
    │ HTTPS :443
    ▼
┌─────────────────────────────────────────┐
│  NGINX (nginx:1.27-alpine)              │
│  • SSL termination                      │
│  • Rate limit: /login = 5 req/min       │
│  • Headers de segurança (HSTS, XFRAME)  │
│  • Cache de estáticos                   │
└──────────────────┬──────────────────────┘
                   │ HTTP :8080 (rede interna)
                   ▼
┌─────────────────────────────────────────┐
│  Scriba App (eclipse-temurin:25-jre)    │
│  • Usuário não-root                     │
│  • JVM: -XX:+UseContainerSupport       │
│  • Healthcheck: /actuator/health        │
└──────────────────┬──────────────────────┘
                   │ JDBC :3306 (rede interna)
                   ▼
┌─────────────────────────────────────────┐
│  MariaDB 11.4 LTS                       │
│  • Não exposto externamente em prod     │
│  • Volume persistente Docker            │
│  • UTF8MB4 / utf8mb4_unicode_ci         │
└─────────────────────────────────────────┘
```

#### Dockerfile — Multi-stage

```dockerfile
# Estágio 1: Build com Maven (cache de dependências separado)
FROM eclipse-temurin:25-jdk-alpine AS builder
WORKDIR /build
COPY pom.xml .mvn/ mvnw ./
RUN ./mvnw dependency:go-offline -q    # camada de cache
COPY src/ src/
RUN ./mvnw package -DskipTests -q

# Estágio 2: Runtime enxuto (sem JDK, sem Maven, sem sources)
FROM eclipse-temurin:25-jre-alpine AS runtime
RUN addgroup -S scriba && adduser -S scriba -G scriba
COPY --from=builder /build/target/scriba-*.jar scriba.jar
USER scriba
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar scriba.jar"]
```


---

## 11. Stack Tecnológica

| Tecnologia | Versão | Justificativa |
|---|---|---|
| Java | 25 | Records, sealed classes, pattern matching — menos boilerplate |
| Spring Boot | 4.0.0 | Auto-configuration, ecossistema maduro |
| Spring Security | 7.x | RBAC declarativo, integração Thymeleaf nativa |
| Spring Data JPA | 4.x | Repositories sem SQL boilerplate |
| Hibernate | 7.x | Herança JOINED, auditoria `@EntityListeners` |
| Flyway | 10.x | Migrações versionadas e imutáveis |
| MapStruct | 1.6.x | Mapeamento em compile-time, erro em build se incompleto |
| Lombok | 1.18.x | Reduz ~60% de boilerplate nas entidades |
| Thymeleaf | 3.1.x | Templating server-side, dialeto `sec:authorize` |
| HTMX | 2.0.x | Interatividade sem JavaScript, swap de fragmentos |
| Tailwind CSS | Play CDN | Estilização utilitária, PWA-ready |
| MariaDB | 11.4 LTS (todos os ambientes) | ACID, UTF8MB4, compatível com MySQL, LTS com suporte longo |
| Docker Compose | 3.x | Ambiente isolado por perfil (dev/test/prod). MariaDB sempre real. |
| NGINX | 1.27 | Proxy reverso, SSL termination, rate limiting no login |
| Maven | 3.9.x | Build reproduzível, BOM gerencia versões |

---

## 12. Padrões de Código

### 12.1 Entidade

```java
@Entity
@Table(name = "minha_tabela")
@Getter @Setter
public class MinhaEntidade extends TenantEntity {

    @NotBlank @Size(max = 200)
    @Column(nullable = false, length = 200)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MeuEnum tipo = MeuEnum.PADRAO;

    // Associações: SEMPRE FetchType.LAZY
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "outra_id")
    private OutraEntidade outra;
}
```

### 12.2 DTO (Java Record)

```java
// Request — validação Bean Validation
public record MinhaEntidadeRequest(
    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 200)
    String nome,

    @NotNull MeuEnum tipo
) {}

// Response — campos selecionados
public record MinhaEntidadeResponse(
    UUID id,
    String nome,
    MeuEnum tipo,
    LocalDateTime criadoEm
) {}
```

### 12.3 Mapper (MapStruct)

```java
@Mapper  // registrado automaticamente como @Component Spring
interface MinhaEntidadeMapper {

    @Mapping(target = "instituicaoId", ignore = true)
    MinhaEntidade toEntity(MinhaEntidadeRequest request);

    MinhaEntidadeResponse toResponse(MinhaEntidade entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromRequest(MinhaEntidadeRequest req, @MappingTarget MinhaEntidade entity);
}
```

### 12.4 Service

```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)  // padrão somente leitura
public class MinhaEntidadeService {

    private final MinhaEntidadeRepository repository;

    public MinhaEntidade buscarPorId(UUID id, UUID instituicaoId) {
        return repository.findByIdAndInstituicaoId(id, instituicaoId)
            .orElseThrow(() -> new ResourceNotFoundException("MinhaEntidade", id));
    }

    @Transactional  // sobrescreve readOnly para escrita
    public MinhaEntidade criar(MinhaEntidade entidade) {
        // validações de negócio aqui — nunca DTOs
        return repository.save(entidade);
    }
}
```

### 12.5 Controller

```java
@Controller
@RequestMapping("/modulo/entidades")
@RequiredArgsConstructor
public class MinhaEntidadeController {

    private final MinhaEntidadeService service;
    private final MinhaEntidadeMapper  mapper;

    @GetMapping
    public String listar(@CurrentUser UserDetailsImpl usuario, Model model) {
        model.addAttribute("itens",
            service.listar(usuario.getInstituicaoId(), ...)
                   .map(mapper::toResponse));
        return "modulo/entidades/list";
    }

    @PostMapping
    public String criar(@Valid @ModelAttribute("item") MinhaEntidadeRequest req,
                        BindingResult binding,
                        @CurrentUser UserDetailsImpl usuario,
                        RedirectAttributes flash) {
        if (binding.hasErrors()) return "modulo/entidades/form";

        MinhaEntidade e = mapper.toEntity(req);
        e.setInstituicaoId(usuario.getInstituicaoId());  // tenant do usuário logado
        service.criar(e);

        flash.addFlashAttribute("sucesso", "Item criado com sucesso.");
        return "redirect:/modulo/entidades";
    }
}
```

### 12.6 Template Thymeleaf + HTMX

```html
<!-- Fragmento reutilizável — retornado tanto para página completa quanto para swap HTMX -->
<div th:fragment="tabela-itens" id="tabela-itens-wrapper">

  <!-- Busca em tempo real — sem JavaScript explícito -->
  <input hx-get="/modulo/entidades"
         hx-trigger="input changed delay:400ms"
         hx-target="#tabela-itens-wrapper"
         hx-swap="innerHTML"
         name="busca" />

  <table>
    <tr th:each="item : ${itens}" th:id="'item-' + ${item.id}">
      <td th:text="${item.nome}"></td>
      <td>
        <!-- DELETE sem reload — HTMX envia o token CSRF automaticamente -->
        <button hx-delete="@{/{id}(id=${item.id})}"
                hx-target="#tabela-itens-wrapper"
                hx-confirm="Confirma exclusão?">Excluir</button>
      </td>
    </tr>
  </table>
</div>
```

---

## 13. Testes

### 13.1 Estratégia

| Tipo | Framework | Escopo |
|---|---|---|
| Integração (Service) | `@SpringBootTest` + `@Transactional` | Regras de negócio, persistência, Flyway |
| Web (Controller) | `@WebMvcTest` + `@WithMockUser` | Rotas, autorização, serialização |
| Unitário | JUnit 5 + Mockito | Lógica de enums e classes de domínio |

### 13.2 Convenções

- Nome de método: `deve[Acao]Quando[Condicao]` (ex: `deveLancarExcecaoNomeDuplicado`).
- Perfil `test` ativado via `@ActiveProfiles("test")`.
- Cada teste de integração roda em `@Transactional` — rollback automático ao final.
- AssertJ para asserções fluentes.

### 13.3 Comandos

```bash
# Todos os testes
mvn test

# Apenas um teste específico
mvn test -Dtest="AutorServiceIntegrationTest"

# Com relatório de cobertura (JaCoCo — a configurar na Sprint 6)
mvn verify
```

---

## 14. Plano de Sprints

| Sprint | Objetivo | Status |
|---|---|---|
| **1** | Fundação: setup, core, catálogo completo (Autor, Editora, Categoria, Classificacao) | ✅ Concluída |
| **2** | Acervo: hierarquia JOINED, todos os subtipos, máquina de estados | ✅ Concluída |
| **3** | Circulação básica: empréstimo, devolução, renovação, reserva, multa | 🔄 Em andamento |
| **4** | Curadoria + Portal do Leitor: descarte, remanejamento, entidade Leitor | ⏳ Planejado |
| **5** | Notificações (e-mail) + relatórios + scheduler de reservas | ⏳ Planejado |
| **6** | Hardening: cobertura ≥ 70%, Tailwind CLI, PWA, deploy em produção | ⏳ Planejado |

---

## 15. Riscos Detalhados e Mitigações

### 15.1 Riscos Críticos (R1–R2) — bloqueiam produção

#### R1 · Isolamento de tenant apenas na aplicação

**Probabilidade:** Média · **Impacto:** Crítico

O isolamento de dados entre instituições é feito exclusivamente em código (filtro por `instituicao_id` nos Services). Uma query mal escrita, uma chamada direta a um Repository sem o filtro, ou um bug de autenticação pode expor dados de uma instituição a outra.

**Mitigação — implementar antes da Sprint 3:**

```java
// Opção A: interceptor que audita toda query (Spring AOP)
@Aspect @Component
public class TenantQueryAuditor {
    @Before("execution(* br.dev.lourenco.scriba.modules.*.repository.*.*(..))")
    public void auditarFiltroTenant(JoinPoint jp) {
        // verificar que os args contêm o instituicaoId do SecurityContext
    }
}

// Opção B: Spring Data Specification obrigatória em todos os repositories tenant-aware
// Definir interface TenantRepository<T> que força o uso de Specification com filtro
```

**Testes obrigatórios:** para cada módulo, um teste de integração que chama o endpoint como `institucaoA` e verifica que dados da `institucaoB` não aparecem na resposta.

---

#### R2 · Condição de corrida em circulação

**Probabilidade:** Média-Alta · **Impacto:** Alto

Dois bibliotecários realizando empréstimo do mesmo item ao mesmo tempo podem ultrapassar as verificações de disponibilidade e criar dois `Emprestimo` para o mesmo exemplar.

**Mitigação — implementar antes da Sprint 3:**

```java
// Opção A: lock pessimista no momento da operação crítica
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT a FROM AcervoItem a WHERE a.id = :id AND a.instituicaoId = :instId")
Optional<AcervoItem> findByIdWithLock(@Param("id") UUID id, @Param("instId") UUID instId);

// Opção B: lock otimista na entidade AcervoItem
@Entity
public abstract class AcervoItem extends TenantEntity {
    @Version
    private Long versao;   // Hibernate lança OptimisticLockException na colisão
    // ...
}
```

**Constraint de banco (defesa em profundidade):**
```sql
-- MariaDB NÃO suporta partial unique indexes como o PostgreSQL (cláusula WHERE).
-- Usamos uma coluna gerada (generated column) como alternativa canônica:

ALTER TABLE emprestimo
    ADD COLUMN acervo_item_id_ativo VARCHAR(36) AS (
        CASE WHEN status = 'ATIVO' THEN acervo_item_id ELSE NULL END
    ) VIRTUAL,
    ADD CONSTRAINT uq_emprestimo_item_ativo UNIQUE (acervo_item_id_ativo);

-- A coluna gerada é NULL quando o empréstimo não está ATIVO.
-- MariaDB permite múltiplos NULLs em UNIQUE, garantindo que apenas
-- empréstimos ATIVO participem da restrição de unicidade.
```

---

### 15.2 Riscos Altos (R3–R5)

#### R3 · Stack em versão de fronteira

**Probabilidade:** Média · **Impacto:** Alto

Spring Boot 4.0.0 e Java 25 são versões de fronteira no momento desta análise. Dependências do ecossistema podem não ter suporte completo, gerando custo de upgrade e instabilidade.

**Mitigação:**
- Fixar versões explícitas de todas as dependências no `pom.xml` (sem `SNAPSHOT`).
- Criar um job de CI que roda `mvn dependency:tree` e alerta sobre dependências desatualizadas.
- Definir uma janela de upgrade trimestral antes que se acumule débito técnico.

---

#### R4 · Gap de banco em testes — RESOLVIDO ✅

**Probabilidade:** N/A · **Impacto:** N/A (mitigado)

Este risco foi eliminado com a mudança para MariaDB em todos os ambientes (dev, test, prod). Cada ambiente tem seu próprio container MariaDB via Docker Compose, garantindo paridade total.

**Mitigação:**

```bash
# Subir banco de teste MariaDB antes de rodar os testes
docker compose -f docker-compose.test.yml up -d db-test

# Executar testes apontando para o MariaDB de teste
mvn test -Dspring.profiles.active=test

# Ou via Makefile:
make test
```

O MariaDB de teste roda na porta 3307 para não conflitar com o de dev (3306).

---

#### R5 · Hardening tardio (Sprint 6)

**Probabilidade:** Alta · **Impacto:** Alto

Postergar segurança e performance para a última sprint é uma das causas mais comuns de atraso em projetos. Problemas descobertos tarde têm custo de correção 5–10x maior.

**Mitigação — incorporar ao critério de conclusão da Sprint 3:**
- Configurar rate limiting na autenticação (evitar brute force).
- Adicionar `spring-boot-actuator` com endpoints de health e métricas (protegidos).
- Definir cota máxima de registros por página (evitar OOM em listagens grandes).
- Testar o fluxo de empréstimo com 100 requisições simultâneas (JMeter ou k6).

---

### 15.3 Riscos Médios (R6–R8)

#### R6 · Herança JOINED e consultas polimórficas

**Probabilidade:** Baixa · **Impacto:** Médio

Consultas que buscam qualquer `AcervoItem` (circulação, portal do leitor) geram `JOIN` em até 5 tabelas. Com acervo crescendo para dezenas de milhares de exemplares, isso pode impactar a performance.

**Mitigação:**
- Índice composto em `(instituicao_id, status, tipo_item)` na tabela `acervo_item`.
- Habilitar o cache de segundo nível do Hibernate para entidades lidas frequentemente.
- Monitorar queries lentas com `performance_schema` do MariaDB (`events_statements_summary_by_digest`) e o slow query log (já configurado em `docker/mariadb/my.prod.cnf`).
- Avaliar views materializadas para listagem do portal do leitor (leitura intensiva).

---

#### R7 · Governança de Autor e Editora por instituição

**Probabilidade:** Baixa (mitigado) · **Impacto:** Baixo

`Autor` e `Editora` foram movidos para escopo por instituição (V7). Cada biblioteca gerencia seu próprio vocabulário controlado. Isso elimina conflitos de qualidade cross-tenant. O risco restante é que instituições parceiras que compartilham obras precisem duplicar o cadastro de autor/editora em cada instituição. ("Machado de Assis" vs "Machado de Assis "), dados inconsistentes e disputas sobre quem pode editar ou excluir.

**Mitigação:**
- Definir que apenas `ADMIN` de nível sistêmico (super-admin) pode criar/editar entidades globais.
- Criar endpoint de `merge` de autores duplicados.
- Implementar busca por similaridade com `FULLTEXT INDEX` do MariaDB + `MATCH ... AGAINST` (ou SOUNDEX para sugerir autor existente antes de criar novo).

---

#### R8 · Ausência de estratégia operacional

**Probabilidade:** Alta · **Impacto:** Médio-Alto

O documento de arquitetura descreve o sistema mas não define como ele será operado em produção: como recuperar de falha, como detectar problemas, qual o RTO/RPO aceitável.

**Mitigação — ver seção 16 (Estratégia Operacional) para o plano completo.**

---

## 16. Estratégia Operacional

### 16.1 Backup e Recuperação

| Item | Definição |
|---|---|
| **Frequência de backup** | Diária completa + WAL arquivamento contínuo (produção) |
| **Retenção** | 30 dias de backups diários; 7 dias de WAL |
| **RPO alvo** | ≤ 1 hora de perda de dados |
| **RTO alvo** | ≤ 4 horas para restaurar serviço |
| **Comando de backup** | `mariadb-dump -u root -p scriba_prod --single-transaction --routines --triggers > scriba_$(date +%Y%m%d).sql` |
| **Teste de restore** | Mensal obrigatório em ambiente isolado |

**Script mínimo de backup automatizado:**

```bash
#!/bin/bash
# /etc/cron.d/scriba-backup — executa às 02:00 diariamente
# Requer as variáveis DB_PROD_NAME e MARIADB_ROOT_PASS_PROD no environment
BACKUP_DIR=/var/backups/scriba
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="$BACKUP_DIR/scriba_$TIMESTAMP.sql.gz"

# Dump via container do MariaDB de produção (uso do Makefile)
cd /opt/scriba && \
  docker compose -f docker-compose.prod.yml exec -T db-prod \
    mariadb-dump -u root -p"$MARIADB_ROOT_PASS_PROD" \
      --single-transaction \
      --routines --triggers \
      --default-character-set=utf8mb4 \
      "$DB_PROD_NAME" | gzip > "$BACKUP_FILE"

# Manter apenas os últimos 30 dias
find "$BACKUP_DIR" -name "*.sql.gz" -mtime +30 -delete

# Verificar integridade do arquivo gerado
if gzip -t "$BACKUP_FILE" 2>/dev/null; then
    echo "Backup OK: $TIMESTAMP ($(du -h $BACKUP_FILE | cut -f1))"
else
    echo "FALHA no backup: $TIMESTAMP" | \
        mail -s "ALERTA: Backup Scriba" ops@suabiblioteca.org.br
fi
```

---

### 16.2 Observabilidade

#### Logs Estruturados (JSON)

Adicionar ao `application-prod.yml`:

```yaml
logging:
  structured:
    format:
      console: json         # Spring Boot 4 suporta JSON nativo
  level:
    br.dev.lourenco.scriba: INFO
    org.springframework.security: WARN
    org.hibernate: WARN
```

Campos obrigatórios em cada log de operação de circulação:
- `instituicao_id`, `usuario_id`, `acervo_item_id`, `operacao` (EMPRESTIMO/DEVOLUCAO/RESERVA)
- `timestamp`, `duracao_ms`, `status` (OK/ERRO)

#### Métricas com Spring Actuator

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

```yaml
# application-prod.yml
management:
  endpoints:
    web:
      exposure:
        include: health, metrics, info
      base-path: /actuator
  endpoint:
    health:
      show-details: when-authorized
  metrics:
    tags:
      application: scriba
```

> ⚠️ Proteger `/actuator/**` com `ROLE_ADMIN` na `SecurityConfig`.

#### Métricas de negócio a instrumentar

| Métrica | Como medir |
|---|---|
| Empréstimos realizados / hora | `Counter` no `CirculacaoService.realizarEmprestimo()` |
| Itens atualmente emprestados | `Gauge` via `AcervoItemRepository.countByStatus(EMPRESTADO)` |
| Multas geradas / dia | `Counter` no `CirculacaoService.gerarMulta()` |
| Taxa de renovação | `Counter` no `CirculacaoService.renovar()` |
| Reservas expiradas / dia | `Counter` no scheduler de expiração |

---

### 16.3 Alertas Mínimos Recomendados

| Alerta | Threshold | Canal |
|---|---|---|
| JVM heap > 80% | 3 ocorrências em 5min | E-mail / Slack |
| Erro HTTP 5xx > 1% das requisições | janela de 1min | E-mail |
| Conexões HikariCP ativas = pool máximo | 2min contínuos | E-mail |
| Falha em backup diário | qualquer falha | E-mail |
| Disk usage > 80% | qualquer medição | E-mail |
| CPU > 90% por mais de 5min | — | E-mail |

---

### 16.4 Runbook — Procedimentos Operacionais

#### Inicialização em Produção

```bash
# 1. Verificar pré-requisitos
java -version          # deve ser Java 25+
docker compose -f docker-compose.prod.yml exec db-prod \
    mariadb -u "$DB_PROD_USER" -p"$DB_PROD_PASS" \
    -e "SELECT 1" "$DB_PROD_NAME"  # banco acessível

# 2. Copiar o JAR e o .env
scp target/scriba-1.0.0.jar prod:/opt/scriba/
scp .env prod:/opt/scriba/

# 3. Iniciar como serviço systemd
sudo systemctl start scriba
sudo journalctl -u scriba -f

# 4. Verificar saúde
curl http://localhost:8080/actuator/health
```

**Arquivo `/etc/systemd/system/scriba.service`:**

```ini
[Unit]
Description=Scriba — Sistema de Gestão de Acervo
After=network.target docker.service
Requires=docker.service

[Service]
Type=simple
User=scriba
WorkingDirectory=/opt/scriba
EnvironmentFile=/opt/scriba/.env
ExecStart=/usr/bin/java -jar /opt/scriba/scriba-1.0.0.jar
Restart=on-failure
RestartSec=10
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
```

#### Rollback de Versão

```bash
# 1. Parar o serviço
sudo systemctl stop scriba

# 2. Reverter JAR
cp /opt/scriba/backups/scriba-anterior.jar /opt/scriba/scriba.jar

# 3. Reverter migrations Flyway (APENAS se a nova versão criou migrations)
# ATENÇÃO: Flyway não faz rollback automático. Criar script de undo manual.
docker compose -f docker-compose.prod.yml exec db-prod \
    mariadb -u "$DB_PROD_USER" -p"$DB_PROD_PASS" "$DB_PROD_NAME" \
    -e "DELETE FROM flyway_schema_history WHERE version = '7';"
# e executar o SQL de undo manual (o Flyway não reverte automaticamente)

# 4. Reiniciar
sudo systemctl start scriba
```

#### Troca de Senha do ADMIN seed

```bash
# Gerar hash BCrypt (strength 12) para a nova senha
htpasswd -bnBC 12 "" 'NovaSenhaForte@2025' | tr -d ':'

# Atualizar no banco
docker compose -f docker-compose.prod.yml exec db-prod \
    mariadb -u "$DB_PROD_USER" -p"$DB_PROD_PASS" "$DB_PROD_NAME" \
    -e "UPDATE usuario SET senha = 'hash_gerado_acima' WHERE email = 'admin@scriba.dev';"
```

---

### 16.5 Checklist de Go-Live

Todos os itens abaixo devem estar **verificados e documentados** antes da primeira execução em produção:

```
Segurança
[ ] Senha padrão do seed trocada
[ ] JWT_SECRET com mínimo 64 chars aleatórios
[ ] HTTPS configurado (certificado TLS válido)
[ ] Firewall: apenas porta 443 exposta externamente
[ ] Variáveis sensíveis não expostas em logs

Banco de Dados
[ ] MariaDB 11.4 LTS via container Docker dedicado
[ ] Usuário MariaDB com privilégios apenas no banco do sistema (sem GRANT ALL no root)
[ ] Backup automático configurado e testado
[ ] Restore testado em ambiente isolado
[ ] `flyway.clean-disabled=true` confirmado

Aplicação
[ ] `APP_PROFILE=prod` definido
[ ] `spring.jpa.show-sql=false` confirmado
[ ] `/actuator` protegido com ROLE_ADMIN
[ ] Logs estruturados em JSON habilitados
[ ] Monitoramento básico (uptime/health check) configurado

Operação
[ ] Runbook de inicialização testado
[ ] Runbook de rollback testado
[ ] Contato de plantão definido para incidentes
[ ] Política de atualização de dependências documentada
```

---

## 17. Plano de Correção Pré-Piloto (2 Sprints)

> **Origem:** parecer técnico externo de Abril/2025.  
> **Objetivo:** resolver os 3 blocos críticos (tenant isolation, concorrência, coerência MariaDB) antes da primeira implantação em piloto.  
> **Status após aplicação parcial:** o código já contempla as correções de MariaDB, lock otimista, AOP de tenant e remoção de JWT_SECRET. As sprints abaixo formalizam os testes e verificações restantes.

---

### Sprint A — Blindagem Multi-tenant e Concorrência (2 semanas)

#### Item A.1 — Enforcement de tenant em Repositories

**Implementação (já aplicada ao código):**
- `TenantContext` expõe o `instituicaoId` do usuário autenticado via `SecurityContextHolder`.
- `TenantEnforcementAspect` audita cada chamada a `*.repository.*` e registra warn/exception quando a chamada não carrega o `instituicaoId` esperado.
- Propriedade `scriba.tenant.strict-mode`: `true` em dev/test (falha build), `false` em prod (apenas log).

**Critérios de aceite:**

| Critério | Como verificar |
|---|---|
| Toda chamada a Repository passa pelo aspecto | Log `INFO` do aspecto em startup mostra o pointcut ativo |
| Chamada a `findAll()` sem contexto lança `TenantIsolationViolationException` em dev | Teste unitário com `@WithMockUser` |
| Teste de fronteira existe para cada módulo | 5 testes (administracao, catalogo, acervo, circulacao, curadoria, pessoas) |
| Zero warnings de vazamento no log da suite de testes | `mvn test` e inspecionar `target/surefire-reports` |

**Testes obrigatórios:**

```java
@SpringBootTest
@ActiveProfiles("test")
class TenantIsolationAcervoTest {

    @Autowired MockMvc mvc;
    @Autowired AcervoItemRepository repo;

    /** Dado dois tenants A e B com 1 item cada, B não pode ver item de A. */
    @Test @WithMockUser(username = "bibA", authorities = "ROLE_BIBLIOTECARIO")
    void bibliotecarioNaoAcessaAcervoDeOutraInstituicao() throws Exception {
        // seed: item1 em instA, item2 em instB — via helper que popula o contexto
        mvc.perform(get("/acervo")
                .with(tenantA()))                  // autenticação no tenant A
           .andExpect(status().isOk())
           .andExpect(model().attribute("itens",
                hasItem(hasProperty("id", is(item1.getId())))))
           .andExpect(model().attribute("itens",
                not(hasItem(hasProperty("id", is(item2.getId()))))));
    }
}
```

---

#### Item A.2 — Controle de concorrência em circulação

**Implementação (já aplicada ao código):**
- Campo `versao` (`@Version`) em `AcervoItem` — lock otimista JPA.
- Migration `V8__blindagem_concorrencia.sql`:
  - `acervo_item.versao BIGINT NOT NULL DEFAULT 0`
  - Coluna gerada `acervo_item_id_ativo` em `emprestimo` + `UNIQUE (acervo_item_id_ativo)` — garante no máximo 1 empréstimo ATIVO por item (alternativa MariaDB ao partial index do PostgreSQL).
- Índices compostos `(instituicao_id, status)` em `acervo_item`, `emprestimo` e `reserva`.

**Critérios de aceite:**

| Critério | Como verificar |
|---|---|
| `@Version` presente em `AcervoItem` | Inspeção de código |
| Duas transações paralelas tentando emprestar o mesmo item: apenas 1 sucesso | Teste de integração com `CompletableFuture` ou `Executors` |
| Segunda transação recebe `OptimisticLockException` (ou nossa `BusinessException` wrappada) | Teste de integração |
| Constraint `uq_emprestimo_item_ativo` impede dupla alocação mesmo se o lock otimista falhar | Teste SQL direto após bypass do service |

**Teste obrigatório (pseudocódigo):**

```java
@Test
void empréstimoConcorrenteFalhaUmaDasOperações() throws Exception {
    UUID itemId = seedItemDisponivel();

    ExecutorService executor = Executors.newFixedThreadPool(2);
    CompletableFuture<String> f1 = CompletableFuture.supplyAsync(
        () -> tentarEmprestar(itemId, leitor1Id), executor);
    CompletableFuture<String> f2 = CompletableFuture.supplyAsync(
        () -> tentarEmprestar(itemId, leitor2Id), executor);

    CompletableFuture.allOf(f1, f2).join();

    long sucessos = Stream.of(f1, f2).filter(f -> "OK".equals(f.join())).count();
    long falhas   = Stream.of(f1, f2).filter(f -> "CONFLITO".equals(f.join())).count();

    assertThat(sucessos).isEqualTo(1);
    assertThat(falhas).isEqualTo(1);
}
```

---

### Sprint B — Coerência Operacional e Hardening (2 semanas)

#### Item B.1 — Auditoria estruturada de ações sensíveis

**A implementar:**

Criar `core/audit/AuditLog` (entidade) e `AuditService` com interceptor que registra:
- Quem (usuário + `instituicao_id`)
- Onde (IP + rota)
- Quando (timestamp UTC)
- Antes/depois (diff do `@Entity` via Hibernate Envers — avaliar alternativa mais leve para o MVP)

Eventos auditados mandatoriamente:
- Descarte (`CuradoriaService.descartar`)
- Remanejamento (`CuradoriaService.remanejar`)
- Isenção de multa (`CirculacaoService.isentarMulta`)
- Alteração de `RegraEmprestimo`
- Login falho (rate limiting no NGINX já está em `nginx.conf`)

**Critério de aceite:** todo evento listado acima gera um registro em `audit_log` com os 5 campos; teste verifica presença do registro após cada operação.

---

#### Item B.2 — Gestão de segredos

**A implementar:**

1. Definir diretório `/opt/scriba/secrets/` com permissões 600, montado no container como read-only.
2. Sobrescrever variáveis do `.env` por arquivos (`_FILE` convention): `DB_PROD_PASS_FILE=/opt/scriba/secrets/db_prod_pass`.
3. Documentar rotação trimestral de senhas no runbook (seção 16.4).
4. Avaliar integração com Vault/SOPS na Sprint seguinte — não bloqueante para piloto.

---

#### Item B.3 — Carga e resiliência

**A implementar:**

1. Script `k6` em `scripts/load/` simulando 100 empréstimos simultâneos por 5 minutos.
2. Script de chaos: matar container `db-prod` durante operação, verificar que a app se recupera sem corrupção.
3. Métricas Actuator capturadas em arquivo durante a carga; threshold: p95 < 500ms.

---

#### Item B.4 — Runbook único e executável

**A implementar:**

Converter os snippets espalhados na seção 16 em **scripts** versionados em `scripts/ops/`:
- `start-prod.sh` — sobe o stack de produção com verificações prévias.
- `rollback.sh` — rollback de versão + restauração de migration Flyway.
- `backup-now.sh` — executa backup manual com verificação de integridade.
- `restore.sh` — restaura banco a partir de um dump, com confirmação interativa.

**Critério de aceite:** cada script termina com exit code 0 em dry-run; documentação aponta para `scripts/ops/README.md` com a matriz de uso.

---

### Veredito após as 2 sprints

Se todos os 6 itens estiverem verdes nos testes automatizados e o runbook executado em ambiente de staging, o status do projeto muda de **"GO condicionado"** para **"GO para piloto"**.

---

## 18. Glossário


| Termo | Definição |
|---|---|
| **Acervo** | Conjunto de exemplares físicos pertencentes a uma biblioteca. |
| **Catálogo** | Base de referência das obras (autores, editoras, categorias). Descreve o que existe, não o exemplar físico. |
| **Circulação** | Operações de movimentação do acervo: empréstimo, devolução, renovação e reserva. |
| **Curadoria** | Processo de avaliação sobre itens que devem ser retirados do acervo ativo. |
| **Desbastamento** | Operação de curadoria: Descarte ou Remanejamento. |
| **Discriminador JPA** | Coluna `tipo_item` que identifica o subtipo concreto de um `acervo_item`. |
| **Fat JAR** | JAR executável que inclui todas as dependências e o servidor Tomcat embutido. |
| **HTMX** | Biblioteca que adiciona comportamento AJAX via atributos HTML (`hx-get`, `hx-post`, etc.). |
| **LivroAutor** | Entidade de associação N:N entre `Livro` e `Autor` com campo `role_autor`. |
| **Multi-tenancy** | Atender múltiplas instituições em uma única instância, com dados isolados por `instituicao_id`. |
| **Padrão State** | Cada estado do enum `StatusAcervo` encapsula as transições válidas a partir de si mesmo. |
| **RoleAutor** | Enum que distingue o papel de um autor: `AUTOR`, `CO_AUTOR` ou `AUTOR_ESPIRITUAL`. |
| **Soft-delete** | Exclusão lógica: registro mantido no banco com `deleted_at` preenchido. |
| **TenantEntity** | Superclasse de todas as entidades com escopo institucional (`+ instituicaoId`). |
| **Mediador** | Nome do médium ou receptor registrado em obras com `AUTOR_ESPIRITUAL`. |

---

*Atualizado em Abril/2025 · MariaDB · Docker · NGINX · Escopo de Domínio Revisado · Pacote: `br.dev.lourenco.scriba`*
