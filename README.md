<div align="center">

# 📚 Scriba

### Sistema Livre de Gestão de Bibliotecas

**Plataforma open source para gestão de acervo, empréstimos e membros**  
de bibliotecas filantrópicas e instituições espíritas do Brasil.

[![Licença: AGPL-3.0](https://img.shields.io/badge/Licença-AGPL--3.0-blue.svg)](https://www.gnu.org/licenses/agpl-3.0)
[![GitHub](https://img.shields.io/badge/GitHub-LuizLourenco%2FScriba-black?logo=github)](https://github.com/LuizLourenco/Scriba)
[![Java](https://img.shields.io/badge/Java-25-orange.svg)](https://adoptium.net)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![SQLite](https://img.shields.io/badge/Banco-SQLite-lightgrey.svg)](https://www.sqlite.org)
[![PRs Welcome](https://img.shields.io/badge/PRs-bem--vindos-brightgreen.svg)](CONTRIBUTING.md)

[Funcionalidades](#-funcionalidades) · [Início Rápido](#-início-rápido) · [Arquitetura](#-arquitetura) · [Contribuindo](#-contribuindo) · [Licença](#-licença)

</div>

---

## 💡 Por que o Scriba existe?

Muitas bibliotecas de centros espíritas e instituições filantrópicas no Brasil operam sem nenhum sistema de gestão — o controle de acervo e empréstimos é feito em cadernos ou planilhas. Sistemas comerciais existem, mas cobram licenças mensais que estão fora do alcance de entidades com orçamento zero.

O **Scriba** é a resposta a isso: uma plataforma gratuita, de código aberto, que qualquer voluntário com um computador básico consegue instalar e colocar em funcionamento em minutos — sem servidor dedicado, sem banco de dados externo, sem mensalidade.

---

## ✨ Funcionalidades

### v1.0 — MVP
- 📖 **Acervo** — cadastro de livros, revistas e exemplares; busca por título, autor ou ISBN; preenchimento automático de metadados via [Open Library API](https://openlibrary.org/developers/api)
- 👤 **Membros** — cadastro de leitores, carteira do membro, histórico completo de empréstimos
- 🔄 **Empréstimos** — retirada, devolução e renovação; prazos configuráveis; cálculo automático de multa
- 📊 **Dashboard** — livros mais emprestados, membros ativos, taxa de atraso
- 📧 **Notificações por e-mail** — lembrete 3 dias antes do vencimento e aviso de atraso
- 📱 **PWA** — instalável no celular como app, sem precisar de loja de aplicativos
- 💾 **Backup com 1 clique** — exporta o banco de dados para a pasta de Downloads

### v1.1 — Planejado
- 💬 Notificações via WhatsApp (Evolution API)
- 📄 Exportação de relatórios em PDF e CSV
- 🌐 Catálogo público de consulta (sem login)
- ⏰ Backup agendado automático

### v2.0 — Futuro
- 🏢 Multi-tenancy (várias bibliotecas no mesmo servidor)
- 📥 Importação de acervo via planilha Excel/CSV
- 🔌 API REST pública documentada (OpenAPI)

---

## 🚀 Início Rápido

### Pré-requisitos

Apenas o **Java 25** instalado na máquina. Nada mais.

```bash
# Verificar se Java está instalado
java -version

# Instalar via SDKMAN (recomendado)
curl -s "https://get.sdkman.io" | bash
sdk install java 25-tem
```

### Instalação

```bash
# 1. Baixar a última versão
# Acesse: https://github.com/LuizLourenco/Scriba/releases
# Baixe o arquivo: scriba-1.0.0.jar

# 2. Iniciar o sistema
java -jar scriba-1.0.0.jar

# 3. Acessar no navegador
# http://localhost:8080
```

Ou use os scripts prontos:

```bash
# Linux / macOS
./start.sh

# Windows
start.bat
```

O sistema cria o banco de dados automaticamente em `~/scriba/biblioteca.db` na primeira execução.

> **Credenciais padrão do primeiro acesso**  
> Usuário: `admin` · Senha: `scriba@2025`  
> **Troque a senha imediatamente após o primeiro login.**

---

## ⚙️ Configuração

Todas as configurações ficam no arquivo `application.yml`, criado automaticamente ao lado do JAR:

```yaml
# application.yml — sem nenhuma credencial aqui
# Todos os valores sensíveis vêm do arquivo .env (nunca versionado)

scriba:
  biblioteca:
    nome: ${SCRIBA_NOME:Biblioteca Espírita}
    prazo-emprestimo-dias: 14
    max-livros-por-membro: 3
    multa-por-dia: 0.50

spring:
  mail:
    enabled: ${MAIL_ENABLED:false}
    host: ${MAIL_HOST:smtp.gmail.com}
    port: ${MAIL_PORT:587}
    username: ${MAIL_USER}
    password: ${MAIL_APP_PASSWORD}
```

### Variáveis de Ambiente

O Scriba nunca armazena credenciais em arquivos versionados. Toda configuração sensível usa variáveis de ambiente carregadas de um arquivo `.env` local:

```bash
# 1. Criar o .env a partir do exemplo
cp .env.example .env

# 2. Preencher os valores (nunca commitar este arquivo)
nano .env   # ou qualquer editor

# 3. Iniciar — as variáveis são carregadas automaticamente
./start.sh
```

O arquivo `.env.example` documenta todas as variáveis disponíveis com descrição.  
O arquivo `.env` já está no `.gitignore` — **nunca será enviado ao repositório**.

| Arquivo | Versionado | Conteúdo |
|---|---|---|
| `.env.example` | ✅ Sim | Chaves com valores vazios — template público |
| `.env` | ❌ Não | Valores reais — apenas na máquina local |
| `application.yml` | ✅ Sim | Referências `${VAR}` — sem nenhuma credencial |

---

### Como configurar e-mail (Gmail)

1. Acesse [myaccount.google.com/apppasswords](https://myaccount.google.com/apppasswords)
2. Crie uma senha de app para "Scriba"
3. Copie o arquivo de exemplo e preencha as variáveis:

```bash
cp .env.example .env
# Edite o .env com seu editor favorito
```

```bash
# .env — este arquivo NÃO entra no Git
MAIL_ENABLED=true
MAIL_USER=seu-email@gmail.com
MAIL_APP_PASSWORD=xxxx-xxxx-xxxx-xxxx
```

> As notificações por e-mail são **opcionais**. O sistema funciona 100% sem elas.

---

## 🏗️ Arquitetura

### Stack

| Camada | Tecnologia | Versão |
|---|---|---|
| Linguagem | Java | 25 |
| Framework | Spring Boot | 4.0.5 |
| Banco de dados | SQLite | 3.45+ |
| Migração de schema | Flyway | 11+ |
| Templates | Thymeleaf | 3.x |
| Interatividade | HTMX | 1.9 |
| CSS | Tailwind CSS | 3.x (CDN) |
| Segurança | Spring Security | 7.x |
| E-mail | Java Mail Sender | (spring-boot-starter-mail) |
| Build | Maven | 3.9+ |

### Por que SQLite?

Para bibliotecas com 1–20 usuários simultâneos e acervo de até 10.000 obras, o SQLite é a escolha ideal: **não requer processo servidor**, o banco é um único arquivo `.db` (fácil de fazer backup por pendrive) e funciona em qualquer computador dos últimos 15 anos.

### Por que Thymeleaf + HTMX em vez de Vue/React?

Um único projeto Java — sem Node.js, sem pipeline de build de frontend, sem dois repositórios para manter. O HTMX entrega a experiência de SPA (busca ao vivo, atualizações parciais de página) com zero JavaScript escrito pelo desenvolvedor.

### Estrutura de Pacotes (DDD Pragmático)

```
br.dev.lourenco.scriba/
├── domain/          # Entidades, regras de negócio — zero dependência de Spring
│   ├── acervo/      # Book, Copy, Author, BookCategory
│   ├── emprestimo/  # Loan, LoanItem, Fine, LoanPolicy
│   └── membro/      # Member, MemberCard, MemberStatus
├── application/     # Use Cases, DTOs, orquestração
│   ├── acervo/      # CadastrarLivroUseCase, BuscarAcervoUseCase
│   ├── emprestimo/  # RealizarEmprestimoUseCase, DevolverLivroUseCase
│   └── membro/      # CadastrarMembroUseCase
└── infra/           # Controllers, Repositories, E-mail, Config Spring
    ├── persistence/ # JdbcBookRepository, JdbcLoanRepository
    ├── email/       # SmtpNotificacaoService
    └── web/         # AcervoController, EmprestimoController
```

---

## 🛠️ Desenvolvimento

### Pré-requisitos para desenvolver

```bash
# Java 25
sdk install java 25-tem

# Maven 3.9+
sdk install maven

# Clonar o repositório
# HTTPS
git clone https://github.com/LuizLourenco/Scriba.git

# SSH
git clone git@github.com:LuizLourenco/Scriba.git
cd scriba
```

### Rodando em modo desenvolvimento

```bash
# Compila e sobe com hot reload (Spring DevTools)
mvn spring-boot:run

# Acesse: http://localhost:8080
# O banco de desenvolvimento é criado em: ~/scriba/biblioteca-dev.db
```

### Gerando o JAR de distribuição

```bash
mvn clean package -DskipTests

# Artefato gerado:
target/scriba-1.0.0.jar
```

### Rodando os testes

```bash
# Todos os testes
mvn test

# Apenas testes de domínio (rápidos, sem Spring)
mvn test -pl . -Dtest="br.dev.lourenco.scriba.domain.**"

# Apenas testes de controller
mvn test -pl . -Dtest="br.dev.lourenco.scriba.infra.web.**"
```

---

## 💾 Backup e Restauração

O banco de dados é um único arquivo. Backup é simples:

```bash
# Backup manual
cp ~/scriba/biblioteca.db ~/scriba/backup-$(date +%Y%m%d).db

# Restaurar
cp ~/scriba/backup-20250101.db ~/scriba/biblioteca.db
# Reiniciar o Scriba
```

O botão **"Fazer Backup"** no painel admin automatiza esse processo e salva o arquivo na pasta Downloads.

---

## 🤝 Contribuindo

O Scriba é um projeto comunitário. Contribuições são muito bem-vindas!

### Como contribuir

1. Leia o [Guia de Contribuição](https://github.com/LuizLourenco/Scriba/blob/main/CONTRIBUTING.md)
2. Veja as [issues abertas](https://github.com/LuizLourenco/Scriba/issues) — especialmente as marcadas com `ideal-para-contribuição`
3. Faça um fork, crie uma branch e abra um Pull Request

### Tipos de contribuição que precisamos

- 🐛 Reporte de bugs
- 💡 Sugestões de funcionalidades
- 🌐 Tradução da interface
- 📖 Melhoria da documentação
- 🧪 Escrita de testes
- 🎨 Melhorias de UX/UI
- 🏢 Teste em bibliotecas reais e feedback de uso

### Padrões do projeto

- **Idioma do código:** inglês para classes/métodos técnicos, português para use cases e templates
- **Commits:** [Conventional Commits](https://www.conventionalcommits.org/pt-br/) (`feat:`, `fix:`, `docs:`, etc.)
- **Testes:** toda nova funcionalidade deve ter testes de domínio
- **Formatação:** Google Java Format (`mvn fmt:format`)

---

## 📋 Roadmap

Acompanhe o planejamento completo no [GitHub Projects](https://github.com/LuizLourenco/Scriba/projects).

| Versão | Status | Previsão |
|---|---|---|
| **v1.0 — MVP** | 🚧 Em desenvolvimento | 2025 |
| **v1.1 — WhatsApp + Relatórios** | 📋 Planejado | 2026 |
| **v2.0 — Multi-tenancy** | 💭 Concepção | 2026+ |

---

## 🙋 Suporte

- **Dúvidas de uso:** abra uma [Discussion](https://github.com/LuizLourenco/Scriba/discussions)
- **Bugs:** abra uma [Issue](https://github.com/LuizLourenco/Scriba/issues)
- **Contato:** [luiz.lourenco@outlook.com.br](mailto:luiz.lourenco@outlook.com.br)

---

## 📄 Licença

Distribuído sob a licença **GNU Affero General Public License v3.0 (AGPL-3.0)**.

Isso significa que:
- ✅ Você pode usar gratuitamente em qualquer instituição
- ✅ Você pode modificar e adaptar para suas necessidades
- ✅ Você pode distribuir cópias
- ⚠️ Se distribuir ou oferecer como serviço online, **deve** publicar o código-fonte das suas modificações sob a mesma licença

Veja o arquivo [LICENSE](LICENSE) para o texto completo.

Copyright © 2025 Luiz Lourenco — [luiz.lourenco@outlook.com.br](mailto:luiz.lourenco@outlook.com.br)

---

<div align="center">

Feito com 💙 para o Movimento Espírita brasileiro

**[github.com/LuizLourenco/Scriba](https://github.com/LuizLourenco/Scriba)**

</div>
