# Scriba — Baseline de Construção do CORE
Este documento define a linha de base técnica da fase CORE, que deve ser concluída antes da implementação dos módulos de negócio.

## 1. Objetivo do Baseline
Entregar a infraestrutura mínima e segura para suportar evolução modular do sistema sem retrabalho estrutural:
- base de segurança e autenticação;
- base de persistência e migração;
- isolamento de tenant;
- padrão de erros e observabilidade.

## 2. Escopo da Fase CORE
### 2.1 Estrutura de projeto
- Estrutura inicial por feature em `core/` e `modules/`.
- Convenções de nomenclatura de pacotes, DTOs, mappers e serviços.
- Padrão de versionamento de migrações com Flyway.

### 2.2 Persistência base
- `BaseEntity` com `UUID`, `criadoEm` e `atualizadoEm`.
- `TenantEntity` com `instituicaoId` para entidades multi-tenant.
- Migração inicial para `instituicao`, `biblioteca` e `usuario` (fundação do domínio).

### 2.3 Segurança base
- Configuração RBAC com perfis `ADMIN`, `BIBLIOTECARIO`, `LEITOR`.
- Fluxo de login com senha em BCrypt.
- CSRF habilitado e compatível com requisições HTMX.

### 2.4 Erros e validações
- `BusinessException` e `ResourceNotFoundException`.
- `GlobalExceptionHandler` padronizado para respostas HTML/HTMX e ProblemDetail.
- Mensagens de validação centralizadas.

### 2.5 Multi-tenancy
- `TenantContext` vinculado ao usuário autenticado.
- Enforced tenant filter para consultas tenant-aware.
- Testes de fronteira para impedir vazamento entre instituições.

### 2.6 Observabilidade mínima
- Endpoint de health para operação.
- Logs estruturados com contexto de tenant e usuário nas operações críticas.
- Padrão de correlação para troubleshooting (request id).

## 3. Entregáveis Técnicos
1. **CORE funcional** com segurança, exceptions, entidades-base e contexto tenant.
2. **Migração V1 aplicada** em ambiente local com MariaDB.
3. **Template base de Controller/Service/Repository** para novos módulos.
4. **Checklist de DoD de módulo** para garantir consistência arquitetural.

## 4. Definition of Done da Fase CORE
A fase CORE só é considerada concluída quando todos os itens abaixo estiverem validados:
- build local executa sem erro;
- migrações sobem em banco limpo;
- autenticação e autorização básica funcionando;
- testes de isolamento tenant passando;
- padrão de exceção e resposta de erro validado;
- documentação de referência atualizada (`ARQUITETURA_ALVO` e roteiro).

## 5. Fora de Escopo do CORE
- Regras completas de catálogo, acervo, circulação e curadoria.
- Interface completa de portal do leitor.
- Relatórios avançados e notificações.

## 6. Riscos da Fase e Mitigação
- **Risco:** acessar dados sem filtro de tenant.  
  **Mitigação:** contratos de repository tenant-aware + testes automáticos de fronteira.
- **Risco:** travar evolução por acoplamento entre camadas.  
  **Mitigação:** enforcement da regra Controller/DTO e Service/Entity.
- **Risco:** hardening tardio.  
  **Mitigação:** incluir segurança, health e logs desde o baseline.
