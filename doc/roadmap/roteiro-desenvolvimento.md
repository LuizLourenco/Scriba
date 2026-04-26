# Roteiro de Desenvolvimento

## Fase 0 — CORE (Fundação)

* BaseEntity
* TenantEntity
* Segurança (Spring Security + RBAC)
* Configuração JPA
* Exception Handler global
* Auditoria

---

## Fase 1 — Administração

* Instituição
* Usuários
* Roles
* Regras de empréstimo

---

## Fase 2 — Catálogo

* Autor
* Editora
* Categoria
* Classificação

---

## Fase 3 — Pessoas

* Leitor
* TipoLeitor
* Fornecedor

---

## Fase 4 — Acervo

* Hierarquia JOINED
* CRUD polimórfico
* StatusAcervo (State)

---

## Fase 5 — Circulação

* Empréstimo
* Devolução
* Reserva
* Multa
* Controle de concorrência

---

## Fase 6 — Curadoria

* Descarte
* Remanejamento

---

## Fase 7 — Hardening

* Testes
* Observabilidade
* Segurança
* Deploy
