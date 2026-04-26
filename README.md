# Scriba
Sistema de gestão de acervo para bibliotecas comunitárias e instituições filantrópicas.

## Visão geral
O Scriba é um sistema web para gestão de acervo físico, circulação de itens, curadoria e atendimento de leitores.

A arquitetura adotada é de **monolito modular**, com foco em:
- simplicidade operacional para equipes enxutas;
- isolamento por instituição (`instituicao_id`);
- evolução incremental por módulos.

## Estado atual
Projeto em construção incremental, com base arquitetural definida em `doc/ARQUITETURA_v4.md` e roteiro de execução por fases.

## Documentação
- Arquitetura detalhada de referência: `doc/ARQUITETURA_v4.md`
- Arquitetura alvo consolidada: `doc/ARQUITETURA_ALVO.md`
- Baseline técnico do CORE: `doc/ARQUITETURA_BASELINE_CORE.md`
- Roteiro de desenvolvimento por fases: `doc/ROTEIRO_DESENVOLVIMENTO.md`

## Roadmap resumido
1. **CORE baseline** (infra transversal, segurança, tenant, erros e observabilidade);
2. **Módulo de Usuários** (administração de instituição, bibliotecas e perfis);
3. **Módulo de Leitores** (cadastro de leitores, tipos e fornecedores);
4. **Catálogo** (autor, editora, categoria, classificação);
5. **Acervo** (itens físicos e máquina de estados);
6. **Circulação** (empréstimo, devolução, reserva, multa);
7. **Curadoria** (descarte e remanejamento auditáveis);
8. **Portal, notificações e relatórios**;
9. **Hardening e pré-piloto**.

## Stack principal
- Java 25
- Spring Boot 4
- Spring Security
- Spring Data JPA + Flyway
- Thymeleaf + HTMX
- MariaDB
- Docker Compose

## Como executar localmente (baseline)
Pré-requisitos:
- Java 25
- Docker + Docker Compose

Subir banco local:
```bash
docker compose -f compose.yaml up -d
```

Executar aplicação:
```bash
./mvnw spring-boot:run
```

Executar testes:
```bash
./mvnw test
```

## Convenções arquiteturais obrigatórias
- `Controller` trabalha com DTO e não acessa `Repository` diretamente.
- `Service` implementa regra de negócio com entidades e não depende de DTO.
- Toda consulta tenant-aware deve aplicar filtro por `instituicao_id`.
