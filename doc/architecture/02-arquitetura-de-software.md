# Arquitetura de Software

## Camadas

### Controller

* Recebe requisições
* Trabalha com DTOs

### Service

* Regras de negócio
* Transações

### Repository

* Persistência
* JPA

---

## Regras Importantes

* Service NÃO usa DTO
* Controller NÃO usa Repository
* Mapper é obrigatório

---

## Multi-tenancy

* Todas entidades possuem `instituicao_id`
* Filtro obrigatório em todas consultas

---

## Banco de Dados

* MariaDB único
* Flyway para versionamento
