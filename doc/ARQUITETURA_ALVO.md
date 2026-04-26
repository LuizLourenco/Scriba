# Scriba — Arquitetura Alvo
Documento de referência arquitetural consolidado a partir de `doc/ARQUITETURA_v4.md`, orientado para execução incremental.

## 1. Objetivo
Definir a arquitetura de implementação do Scriba para:
- manter um monolito modular com fronteiras claras por domínio;
- permitir evolução por fases (CORE → Usuários → Leitores → demais módulos);
- reduzir riscos de multi-tenancy, concorrência e operação antes do piloto.

## 2. Princípios Arquiteturais
1. **Monolito modular por domínio**: módulos funcionais independentes dentro do mesmo deploy.
2. **Package by feature**: cada módulo contém `domain`, `service`, `repository`, `controller`, `dto`, `mapper`.
3. **Camadas estritas**:
   - `Controller` trabalha com DTO;
   - `Service` trabalha com entidades e regras de negócio;
   - `Repository` só acessa dados.
4. **Tenant-first**: qualquer acesso a dados tenant-aware exige `instituicao_id`.
5. **Evolução segura**: cada fase só avança com critérios mínimos de teste, segurança e migração.

## 3. Visão de Módulos
### 3.1 CORE (infraestrutura transversal)
- Configuração de segurança, JPA, tratamento de erros e contexto de tenant.
- Contratos-base: `BaseEntity`, `TenantEntity`, exceções de negócio.
- Mecanismos transversais: auditoria de acesso tenant, observabilidade e políticas operacionais.

### 3.2 Módulos de Negócio
- **administracao**: instituição, biblioteca, usuários e perfis de acesso.
- **pessoas**: leitores, tipos de leitor e fornecedores.
- **catalogo**: autor, editora, categoria, classificação.
- **acervo**: itens físicos com herança JOINED e máquina de estados.
- **circulacao**: empréstimo, devolução, reserva e multa.
- **curadoria**: descarte e remanejamento com rastreabilidade.

## 4. Dependências Entre Módulos
Ordem de dependência recomendada:
1. `core`
2. `administracao`
3. `pessoas`
4. `catalogo`
5. `acervo`
6. `circulacao`
7. `curadoria`

Dependências funcionais:
- `administracao` depende de `core`.
- `pessoas` depende de `core` e `administracao`.
- `catalogo` depende de `core` e `administracao`.
- `acervo` depende de `core`, `administracao` e `catalogo`.
- `circulacao` depende de `core`, `administracao`, `pessoas` e `acervo`.
- `curadoria` depende de `core`, `administracao` e `acervo`.

## 5. Decisões Estruturantes
1. **Banco único com discriminador de tenant (`instituicao_id`)**.
2. **Flyway como trilha oficial de evolução de schema**.
3. **MariaDB em todos os ambientes (dev/test/prod)** para evitar gap de comportamento.
4. **Controle de concorrência em circulação** com lock otimista e proteção por constraint.
5. **Segurança RBAC + CSRF** para fluxos tradicionais e HTMX.

## 6. Restrições e Regras Obrigatórias
- Nenhum `Repository` tenant-aware deve expor consulta sem filtro institucional.
- Nenhum `Service` deve importar DTO.
- Nenhum `Controller` deve acessar `Repository` diretamente.
- Regras de transição de item do acervo devem ficar centralizadas em `StatusAcervo`.
- Operações críticas (circulação e curadoria) devem produzir trilha auditável.

## 7. Qualidade Arquitetural
Critérios mínimos por fase:
- migrações versionadas e reproduzíveis;
- testes de isolamento de tenant por módulo;
- testes de concorrência nas operações de empréstimo;
- observabilidade mínima (health, logs estruturados, métricas);
- documentação da operação (backup, restore e rollback).

## 8. Resultado Esperado
Ao final do ciclo de construção, o Scriba deve operar como um monolito modular robusto, com governança de dados por tenant, segurança de circulação contra corrida de concorrência e base operacional pronta para piloto controlado.
