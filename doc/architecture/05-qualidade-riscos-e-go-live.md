# Qualidade, Riscos e Go-Live

## Riscos Críticos

### 1. Vazamento de Tenant

* Falta de filtro por `instituicao_id`

### 2. Concorrência

* Empréstimos simultâneos inconsistentes

---

## Mitigações

* Filtro obrigatório de tenant
* Lock otimista ou pessimista
* Constraints no banco

---

## Antes de Produção

* Testes automatizados
* Logs estruturados
* Backup validado
* Monitoramento ativo

---

## Checklist

* Segurança configurada
* Banco validado
* Deploy testado
* Observabilidade ativa
