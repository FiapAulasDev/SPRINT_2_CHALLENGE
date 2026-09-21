# API Inovação Águia Branca — Documentação Técnica

Projeto acadêmico (FIAP) composto por dois módulos que se comunicam entre si:

| Módulo | Pasta | Stack |
|---|---|---|
| Backend | [`backend_aguia_branca_sprint2/`](backend_aguia_branca_sprint2/) | Java 17 · Spring Boot 4.1.1 · Spring Security · MongoDB · Google Gemini |
| App Android | [`app_aguia_branca_sprint1/`](app_aguia_branca_sprint1/) | Kotlin · Jetpack Compose · Retrofit/OkHttp |

O sistema apoia o processo de inovação da Águia Branca (transporte de passageiros e logística): um Líder define a **estratégia vigente**, Operadores sugerem **ideias**, o Gestor pede uma **análise de viabilidade por IA**, aprova/rejeita e transforma ideias aprovadas em **projetos**, e o Líder acompanha tudo em um **dashboard** consolidado.

> **Nota sobre a estrutura do repositório:** existem uma pasta `src/` e um `pom.xml` na raiz do repositório, além de `Dockerfile`, `ETAPA2-LUCAS.md` e `insomnia-etapa2-aguia-branca.json`. Esses arquivos são um **snapshot mais antigo** do backend, herdado de um merge de branches anterior (histórico do Git mostra commits `"Merge nested app/backend repos into root repository"` e `"Merge remote-tracking branch 'airam/main'"`). Eles não possuem os endpoints de `usuarios`/`register`/gamificação e não são mais mantidos. **O backend oficial, que o app Android realmente consome, é o que está em `backend_aguia_branca_sprint2/`.** Esta documentação descreve exclusivamente `backend_aguia_branca_sprint2/` e `app_aguia_branca_sprint1/`.

---

## Sumário

1. [Arquitetura geral](#1-arquitetura-geral)
2. [Perfis e permissões](#2-perfis-e-permissões)
3. [Modelo de dados (backend)](#3-modelo-de-dados-backend)
4. [Endpoints da API](#4-endpoints-da-api)
5. [Regras de negócio](#5-regras-de-negócio)
6. [Integração com IA (Google Gemini)](#6-integração-com-ia-google-gemini)
7. [Dashboard](#7-dashboard)
8. [Segurança](#8-segurança)
9. [Tratamento de erros](#9-tratamento-de-erros)
10. [Configuração e execução do backend](#10-configuração-e-execução-do-backend)
11. [App Android — arquitetura](#11-app-android--arquitetura)
12. [App Android — telas por perfil](#12-app-android--telas-por-perfil)
13. [Configuração e execução do app](#13-configuração-e-execução-do-app)
14. [Usuários de teste](#14-usuários-de-teste)
15. [Testando a API (Insomnia)](#15-testando-a-api-insomnia)
16. [Observações e limitações conhecidas](#16-observações-e-limitações-conhecidas)

---

## 1. Arquitetura geral

```
┌───────────────────────────┐
│   App Android (Kotlin)    │
│   Jetpack Compose          │
│   Retrofit + OkHttp + Gson │
└──────────────┬─────────────┘
               │  HTTP/JSON
               │  Authorization: Bearer <JWT>
               ▼
┌─────────────────────────────────────────────┐
│        Backend Spring Boot (Java 17)         │
│        pacote base: api_inovacao             │
│                                               │
│   controller → service → repository          │
│                                               │
│   Segurança: Spring Security + JWT (auth0)   │
└───────┬───────────────────┬──────────────────┘
        │                   │
        ▼                   ▼
┌───────────────┐   ┌──────────────────────────┐
│  MongoDB       │   │  Google Gemini API       │
│  (Atlas ou     │   │  (generativelanguage     │
│   local)       │   │   .googleapis.com)       │
└───────────────┘   └──────────────────────────┘
```

Camadas do backend, por pacote (`backend_aguia_branca_sprint2/src/main/java/api_inovacao/`):

| Pacote | Responsabilidade |
|---|---|
| `controller` | Endpoints REST (`AuthController`, `UsuarioController`, `EstrategiaController`, `IdeiaController`, `ProjetoController`, `DashboardController`) |
| `service` | Regras de negócio (`UsuarioService`, `EstrategiaService`, `IdeiaService`, `ProjetoService`, `DashboardService`, `GeminiService`, `AuthorizationService`) |
| `repository` | Acesso a dados — interfaces `MongoRepository<T, String>` |
| `model` | Entidades e enums persistidos no MongoDB |
| `dto` | Objetos de requisição/resposta (records Java) |
| `security` | `SecurityConfig`, `SecurityFilter` (filtro JWT), `TokenService` (geração/validação do token) |
| `exception` | `GlobalExceptionHandler` (`@RestControllerAdvice`) |

---

## 2. Perfis e permissões

Três perfis (`Role`): **OPERADOR**, **GESTOR**, **LIDER**. As regras abaixo são aplicadas em `SecurityConfig` (a ordem importa — vale a primeira regra que casar com a requisição):

| Rota | OPERADOR | GESTOR | LIDER |
|---|:--:|:--:|:--:|
| `POST /api/auth/login`, `/register` | público | público | público |
| `GET /api/usuarios/**` | ✅ | ✅ | ✅ |
| `GET /api/estrategias/**` | ✅ | ✅ | ✅ |
| `POST/PUT/DELETE /api/estrategias/**` | ❌ | ❌ | ✅ |
| `GET /api/ideias/**` | ✅ | ✅ | ✅ |
| `PATCH /api/ideias/{id}/avaliacao` | ❌ | ✅ | ❌ |
| `POST /api/ideias/{id}/analise-ia` | ❌ | ✅ | ❌ |
| `POST/PUT/DELETE /api/ideias` (demais) | ✅ | ❌ | ❌ |
| `GET /api/projetos/**` | ✅ | ✅ | ✅ |
| `POST/PUT/PATCH/DELETE /api/projetos/**` | ❌ | ✅ | ❌ |
| `GET /api/dashboard/**` | ❌ | ❌ | ✅ |

No app Android não existem rotas de navegação distintas por perfil — há uma única tela `home` que renderiza `OperadorHomeContent`, `GestorHomeContent` ou `LiderHomeContent` conforme o `role` do usuário logado (`HomeScreen.kt`). A seção de estratégias (`EstrategiasSection`) é compartilhada e só habilita criar/editar/excluir quando `podeGerenciar = true` (apenas para o Líder).

---

## 3. Modelo de dados (backend)

Banco: MongoDB (`spring.mongodb.database=aguia_branca_db`). Sem uso de `@DBRef` — os vínculos entre coleções são feitos por cópia manual de `id`/`titulo` (desnormalização).

### `User` (coleção `users`)
| Campo | Tipo | Observação |
|---|---|---|
| `id` | String | `@Id` |
| `email` | String | login (username) |
| `senha` | String | hash BCrypt |
| `role` | `Role` | OPERADOR / GESTOR / LIDER |
| `nome` | String | |
| `pontos` | int | gamificação — só operadores acumulam |

### `Estrategia` (coleção `estrategias`)
`id`, `titulo`, `descricao`, `dataInicio`, `dataFim`, `vigente` (boolean — apenas uma pode ser `true` por vez), `criadoPor` (e-mail do Líder), `criadoEm`, `atualizadoEm`.

### `Ideia` (coleção `ideias`)
`id`, `titulo`, `descricao`, `beneficioEsperado`, `status` (`StatusIdeia`), `autorEmail`, `criadoEm`, `atualizadoEm`, `avaliadorEmail`, `comentarioAvaliacao`, `avaliadoEm`, `estrategiaId`/`estrategiaTitulo` (preenchidos só na aprovação, copiados da estratégia vigente), `pontuacaoViabilidade` (0–100), `justificativaIa`, `recomendacaoIa`, `analisadoIaEm`.

### `Projeto` (coleção `projetos`)
`id`, `titulo`, `descricao`, `etapa` (`EtapaProjeto`), `status` (`StatusProjeto`), `investimentoPrevisto`/`retornoEsperado` (`BigDecimal`), `roiPercentual` (`BigDecimal`, **sempre calculado no backend**, nunca aceito do cliente), `prazoInicio`/`prazoFim`, `responsavelEmail` (Gestor), `ideiaId`/`ideiaTitulo` (opcional), `estrategiaId`/`estrategiaTitulo` (herdados da ideia ou da estratégia vigente), `criadoEm`, `atualizadoEm`.

### Enums
```java
enum Role          { OPERADOR, GESTOR, LIDER }
enum StatusIdeia   { PENDENTE, APROVADA, REJEITADA }
enum StatusProjeto { NAO_INICIADO, EM_ANDAMENTO, CONCLUIDO, CANCELADO }
enum EtapaProjeto  { PLANEJAMENTO, EXECUCAO, MONITORAMENTO, ENCERRAMENTO }
```

---

## 4. Endpoints da API

Base path: `/api`. Todas as rotas autenticadas exigem o header `Authorization: Bearer <token>`.

### Autenticação (`/api/auth`) — público

| Método | Rota | Payload | Resposta |
|---|---|---|---|
| POST | `/api/auth/login` | `{ "email": "...", "senha": "..." }` | `201`/`200` → `{ "token": "<jwt>" }` |
| POST | `/api/auth/register` | `{ "nome", "email", "senha", "role" }` | `201` → `{ "email", "nome", "role", "pontos" }` |

### Usuários (`/api/usuarios`) — qualquer perfil autenticado

| Método | Rota | Resposta |
|---|---|---|
| GET | `/api/usuarios/me` | Perfil do usuário logado, incluindo `pontos` |
| GET | `/api/usuarios/ranking` | Top 5 **Operadores** por pontos (gamificação — só operador pontua) |

### Estratégias (`/api/estrategias`) — leitura para todos, escrita só LIDER

| Método | Rota | Payload | Resposta |
|---|---|---|---|
| GET | `/api/estrategias` | — | `List<Estrategia>` |
| GET | `/api/estrategias/vigente` | — | `Estrategia` vigente (404 se nenhuma) |
| GET | `/api/estrategias/{id}` | — | `Estrategia` (404) |
| POST | `/api/estrategias` | `{ titulo, descricao, dataInicio, dataFim, vigente }` | `201` |
| PUT | `/api/estrategias/{id}` | idem | `200` |
| DELETE | `/api/estrategias/{id}` | — | `204` |

### Ideias (`/api/ideias`) — leitura para todos; criação/edição OPERADOR; avaliação/IA GESTOR

| Método | Rota | Payload | Resposta |
|---|---|---|---|
| GET | `/api/ideias?status=` | — | lista (filtro opcional) |
| GET | `/api/ideias/minhas` | — | ideias do usuário logado |
| GET | `/api/ideias/ranking?status=` | — | ideias ordenadas pela nota da IA (maior primeiro) |
| GET | `/api/ideias/{id}` | — | detalhe (404) |
| POST | `/api/ideias` | `{ titulo, descricao, beneficioEsperado }` | `201` — status força `PENDENTE`, autor = e-mail do token |
| PUT | `/api/ideias/{id}` | idem | só o autor, só se `PENDENTE` |
| DELETE | `/api/ideias/{id}` | — | só o autor, só se `PENDENTE` |
| POST | `/api/ideias/{id}/analise-ia` | — | chama o Gemini, grava `pontuacaoViabilidade`/`justificativaIa`/`recomendacaoIa` |
| PATCH | `/api/ideias/{id}/avaliacao` | `{ status: APROVADA\|REJEITADA, comentario }` | ao aprovar: vincula estratégia vigente + `+100` pontos ao autor |

### Projetos (`/api/projetos`) — leitura para todos; escrita só GESTOR

| Método | Rota | Payload | Resposta |
|---|---|---|---|
| GET | `/api/projetos?status=&etapa=` | — | lista (filtros combináveis) |
| GET | `/api/projetos/{id}` | — | detalhe (404) |
| POST | `/api/projetos` | `{ titulo, descricao, etapa, status, investimentoPrevisto, retornoEsperado, prazoInicio, prazoFim, ideiaId? }` | `201` — `roiPercentual` calculado no servidor |
| PUT | `/api/projetos/{id}` | idem | recalcula ROI |
| PATCH | `/api/projetos/{id}/andamento` | `{ etapa, status }` | atualiza só etapa+status |
| DELETE | `/api/projetos/{id}` | — | `204` |

### Dashboard (`/api/dashboard`) — só LIDER

| Método | Rota | Resposta |
|---|---|---|
| GET | `/api/dashboard/resumo` | `{ estrategiaVigente, ideias: ResumoIdeias, projetos: ResumoProjetos }` |
| GET | `/api/dashboard/ideias` | só o funil de ideias |
| GET | `/api/dashboard/projetos` | só os números de projetos |

---

## 5. Regras de negócio

**Ideias**
- Toda ideia nasce `PENDENTE`; o autor é sempre o e-mail extraído do token (o cliente não escolhe).
- O autor só edita/exclui a **própria** ideia (`403` caso contrário) e só enquanto `PENDENTE` (`409` caso contrário).
- Avaliar exige `APROVADA` ou `REJEITADA` (`400` se enviar `PENDENTE`).
- Rejeitar sem comentário → `400` (o app também valida isso no cliente antes de chamar a API).
- Aprovar sem estratégia vigente cadastrada → `422`. Com estratégia, a ideia copia `estrategiaId`/`estrategiaTitulo`.
- Ideia já avaliada não pode ser reavaliada (`409`).
- **Gamificação:** aprovar concede `+100` pontos ao autor (`UsuarioService.PONTOS_POR_IDEIA_APROVADA`); rejeitar não pontua.

**Projetos**
- `prazoFim` anterior a `prazoInicio` → `400`.
- Se `ideiaId` informado: a ideia precisa existir (`404`) e estar `APROVADA` (`422`); uma ideia só pode gerar **um** projeto (`409`).
- O projeto herda a estratégia da ideia vinculada ou, na ausência de ideia, a estratégia vigente no momento.
- `roiPercentual` é **sempre** calculado no backend: `(retornoEsperado − investimentoPrevisto) ÷ investimentoPrevisto × 100`, usando `BigDecimal` para não perder centavos.
- Projeto `CANCELADO` não pode ter o andamento alterado (`409`).

---

## 6. Integração com IA (Google Gemini)

- Endpoint chamado: `POST {gemini.api.url}/models/{modelo}:generateContent` (`gemini.api.url = https://generativelanguage.googleapis.com/v1beta`), autenticado via header `x-goog-api-key`.
- Modelo padrão: `gemini-2.5-flash` (configurável por `GEMINI_MODELO` no `.env`).
- `generationConfig` usa `temperature: 0.2`, `responseMimeType: application/json` e `thinkingConfig.thinkingBudget: 0` (desativa o "raciocínio estendido" do Gemini para responder em segundos em vez de quase um minuto).
- Prompt monta o contexto da ideia + estratégia vigente e pede um JSON estrito:
  ```json
  { "pontuacao": 0-100, "justificativa": "até 3 frases", "recomendacao": "uma frase de próximo passo" }
  ```
- Resultado é salvo na própria `Ideia` (`pontuacaoViabilidade`, `justificativaIa`, `recomendacaoIa`, `analisadoIaEm`).
- Sem `GEMINI_API_KEY` configurada, **somente** o endpoint de análise fica indisponível — retorna `503` com mensagem explicativa; o resto da API funciona normalmente. Erros de comunicação com o Gemini (HTTP ou rede) retornam `502`.

---

## 7. Dashboard

`GET /api/dashboard/resumo` consolida em uma única chamada:
- **Estratégia vigente** (ou `null`).
- **Funil de ideias**: total, pendentes, aprovadas, rejeitadas, `% de aprovação`, quantas já passaram pela IA e a média das notas.
- **Números de projetos**: total, contagem por `status` e por `etapa`, investimento total, retorno total, **lucro total** (retorno − investimento) e **ROI total consolidado** — calculado sobre os totais agregados, não é a média dos ROIs individuais — além do top 3 projetos por ROI.

---

## 8. Segurança

- **Stateless**: `SessionCreationPolicy.STATELESS`, CSRF desabilitado, autenticação via JWT em todas as rotas privadas.
- **Login**: `AuthenticationManager` do Spring Security valida e-mail/senha (`BCryptPasswordEncoder`) via `AuthorizationService` (`UserDetailsService`).
- **Token JWT** (`TokenService`, biblioteca `com.auth0:java-jwt`): `HMAC256` com segredo de `JWT_SECRET`, `issuer = "api-inovacao"`, `subject = email`, claim `role`, **expiração de 2 horas**.
- **Filtro** (`SecurityFilter`): lê o header `Authorization`, remove o prefixo `Bearer `, valida o token e carrega o `User` do Mongo por e-mail a cada requisição.
- Respostas de erro de autenticação (`401`) e autorização (`403`) são padronizadas no mesmo formato JSON usado pelo `GlobalExceptionHandler` (ver seção 9).
- Sem configuração de CORS no backend (nenhum `CorsConfigurationSource`/`@CrossOrigin`) — um front-end web em outra origem seria bloqueado pelo navegador.

---

## 9. Tratamento de erros

`GlobalExceptionHandler` (`@RestControllerAdvice`) padroniza toda resposta de erro:

```json
{
  "timestamp": "2026-09-15T20:51:37",
  "status": 422,
  "erro": "Unprocessable Content",
  "mensagem": "Só ideias APROVADAS podem virar projeto (status atual: PENDENTE)",
  "caminho": "/api/projetos"
}
```

| Código | Quando ocorre |
|---|---|
| `400` | validação de campo (`@Valid`), JSON malformado, enum/parâmetro de query inválido, datas inconsistentes |
| `401` | login com credenciais inválidas, token ausente/inválido |
| `403` | perfil sem permissão para a operação, ou tentando alterar recurso de outro usuário |
| `404` | recurso não encontrado (ideia, projeto, estratégia) |
| `409` | conflito de estado (ideia já avaliada, ideia fora de `PENDENTE`, projeto já criado para a ideia, e-mail já cadastrado) |
| `422` | regra de negócio violada (aprovar sem estratégia vigente, projeto a partir de ideia não aprovada) |
| `502` | falha de comunicação com o Gemini |
| `503` | Gemini não configurado (`GEMINI_API_KEY` ausente) |
| `500` | erro inesperado (catch-all) |

---

## 10. Configuração e execução do backend

### Pré-requisitos
- **JDK 17** (definido em `pom.xml` como `java.version`; a equipe recomenda ter o JDK 21 instalado localmente, compatível já que o bytecode alvo continua sendo 17).
- Não é necessário instalar o Maven — use o wrapper (`./mvnw` no Git Bash / `.\mvnw` no PowerShell).
- MongoDB — local via Docker ou Atlas.
- Chave gratuita do Google Gemini (opcional — só é exigida pelo endpoint de análise de IA): <https://aistudio.google.com/apikey>.

### Passo a passo

```bash
cd backend_aguia_branca_sprint2
cp .env.example .env
```

Preencha o `.env`:

```properties
MONGODB_URI=mongodb://localhost:27017
# ou Atlas: mongodb+srv://USUARIO:SENHA@clusterfiap.rrkjvjw.mongodb.net/?appName=ClusterFiap

JWT_SECRET=qualquer-texto-longo-secreto
GEMINI_API_KEY=sua-chave-do-google-ai-studio
GEMINI_MODELO=gemini-2.5-flash
```

Subir um Mongo local (se não for usar Atlas):

```bash
docker run -d --name mongo-teste-aguia -p 27017:27017 mongo:7
```

Rodar a aplicação:

```bash
./mvnw spring-boot:run      # Git Bash
.\mvnw spring-boot:run      # PowerShell
```

A API sobe em `http://localhost:8080` (porta padrão do Spring Boot — não há `server.port` customizado). Na primeira execução, os 3 usuários de teste são criados automaticamente (seção 14).

### Via Docker

```bash
docker build -t api-inovacao -f backend_aguia_branca_sprint2/Dockerfile backend_aguia_branca_sprint2
docker run -p 8080:8080 --env-file backend_aguia_branca_sprint2/.env api-inovacao
```

O `Dockerfile` faz build multi-stage (`maven:3.9.4-eclipse-temurin-17` → `eclipse-temurin:17-jdk-alpine`) e expõe a porta `8080`.

### `application.properties` → variáveis de ambiente

| Propriedade | Variável de ambiente | Padrão |
|---|---|---|
| `spring.mongodb.uri` | `MONGODB_URI` | obrigatória |
| `spring.mongodb.database` | — | `aguia_branca_db` (fixo) |
| `api.security.token.secret` | `JWT_SECRET` | obrigatória |
| `gemini.api.key` | `GEMINI_API_KEY` | vazio (desativa a análise de IA) |
| `gemini.api.modelo` | `GEMINI_MODELO` | `gemini-2.5-flash` |

---

## 11. App Android — arquitetura

Kotlin + Jetpack Compose, sem framework de injeção de dependência (instanciação manual de repositórios dentro dos ViewModels).

```
com.aguia_branca.app/
├── data/
│   ├── local/       → TokenStore (SharedPreferences — só guarda o JWT)
│   ├── model/       → User (modelo de UI)
│   ├── remote/      → ApiService, RetrofitClient, AuthInterceptor, JwtUtils, ApiException, SafeApiCall
│   │   └── dto/     → DTOs de request/response + enums
│   └── repository/  → AuthRepository, EstrategiaRepository, IdeiaRepository, ProjetoRepository, DashboardRepository
├── navigation/      → AppNavigation (rotas: login, register, home)
├── screens/
│   ├── auth/        → LoginScreen, RegisterScreen
│   └── home/        → HomeScreen + OperadorHomeContent, GestorHomeContent, LiderHomeContent, EstrategiasSection
├── ui/theme/        → tema Compose (template padrão, pouco usado — cores reais estão hardcoded nas telas)
└── viewmodel/       → AuthViewModel, IdeiaViewModel, GestorViewModel, LiderViewModel
```

### Rede
- **Retrofit** (`retrofit:2.11.0`) + **OkHttp** (`4.12.0`) + **Gson**, com adaptadores customizados para `LocalDate`/`LocalDateTime`.
- `AuthInterceptor`: injeta `Authorization: Bearer <token>` em toda chamada exceto `login`/`register`; em qualquer resposta `401` limpa o token e dispara `SessionEvents.sessionExpired`, forçando logout global (observado em `AppNavigation`).
- `SafeApiCall`: encapsula toda chamada em `Result<T>`, convertendo `HttpException` no `ErroResponse` do backend (usa o campo `mensagem`) e `IOException` em `"Falha de conexão. Verifique sua internet."`.
- Timeouts: 30s (connect/read/write). Logging (`HttpLoggingInterceptor.BODY`) apenas em build `DEBUG`.

### Armazenamento local
Apenas `SharedPreferences` (arquivo `auth_prefs`) guardando o token JWT. Perfil/nome/pontos não são persistidos — são obtidos via `GET /api/usuarios/me` a cada abertura do app ou após ações que mudam pontos. `JwtUtils` decodifica o payload do JWT localmente (sem checar assinatura) só para verificar expiração antes de tentar reaproveitar a sessão.

### Build
- `applicationId`: `com.aguia_branca.app` · `compileSdk 36` · `minSdk 28` · `targetSdk 36` · `versionName "1.0"`.
- Kotlin `2.0.21`, AGP `9.0.1`, Compose BOM `2024.09.00`.

---

## 12. App Android — telas por perfil

- **Login/Registro** (`screens/auth`): login com e-mail/senha; registro com nome, e-mail, senha (mín. 6 caracteres) e **seleção de perfil por radio button** (Operador/Gestor/Líder) — o app permite o usuário escolher livremente qualquer um dos três perfis no cadastro.
- **Operador** (`OperadorHomeContent`): visualiza a estratégia vigente (somente leitura), ranking de inovação (pontos), formulário "Sugira uma Inovação" e lista das próprias ideias com status colorido; só pode editar/excluir ideias ainda `PENDENTE`.
- **Gestor** (`GestorHomeContent`): visualiza a estratégia vigente (somente leitura), triagem de ideias pendentes com botões "Analisar com IA" e "Avaliar" (aprovar/rejeitar com comentário obrigatório na rejeição — validado também no app antes de chamar a API), ranking de viabilidade por nota da IA, e gestão de projetos (criar vinculando a uma ideia aprovada, atualizar andamento).
- **Líder** (`LiderHomeContent`): gerencia estratégias (criar/editar/excluir/marcar vigente) e visualiza o dashboard consolidado (cards de ROI/lucro/investimento/retorno, funil de ideias, top projetos por ROI).

---

## 13. Configuração e execução do app

- **URL base da API**: definida em `app/build.gradle.kts` como `BuildConfig.BASE_URL`, com padrão **`http://10.0.2.2:8080/`** (alias do emulador Android para o `localhost` da máquina host, onde o backend deve estar rodando). Pode ser sobrescrita passando a propriedade Gradle `API_BASE_URL` (ex.: `-PAPI_BASE_URL=http://192.168.0.10:8080/` para testar em um dispositivo físico na mesma rede, ou o IP público do backend em produção).
- `res/xml/network_security_config.xml` libera tráfego HTTP (cleartext) apenas para `10.0.2.2`, `localhost` e `127.0.0.1` — qualquer outro host precisa ser HTTPS.
- Rodar: abrir `app_aguia_branca_sprint1/` no Android Studio, sincronizar o Gradle e rodar no emulador (com o backend já rodando em `localhost:8080`).
- **Gerar o APK**: no Android Studio, `Build > Build Bundle(s) / APK(s) > Build APK(s)` (o `.apk` de debug é gerado em `app/build/outputs/apk/debug/`).

---

## 14. Usuários de teste

Criados automaticamente pelo backend na primeira execução (`ApiInovacaoApplication`, `CommandLineRunner`), senha `123456`:

| E-mail | Perfil |
|---|---|
| `lider@aguiabranca.com` | LIDER |
| `gestor@aguiabranca.com` | GESTOR |
| `operador@aguiabranca.com` | OPERADOR |

---

## 15. Testando a API (Insomnia)

Há uma collection pronta em `backend_aguia_branca_sprint2/insomnia-etapa2-aguia-branca.json` com as requisições organizadas em pastas numeradas (login → estratégias → ideias → IA/avaliação → projetos → dashboard → erros esperados → limpeza), com encadeamento automático de token/IDs entre requisições. Confira a variável `base_url` do "Base Environment" (`http://localhost:8080`) antes de rodar.

---

## 16. Observações e limitações conhecidas

- **Cadastro público sem restrição de perfil**: `POST /api/auth/register` exige um campo `role` e o `UsuarioService` grava exatamente o perfil enviado, sem restringir a OPERADOR — ou seja, qualquer pessoa não autenticada pode se registrar diretamente como GESTOR ou LIDER (o app também expõe essa escolha via radio button na tela de cadastro). Vale a pena documentar essa simplificação na apresentação/entrega, já que em um cenário real seria mais coerente restringir o autocadastro a OPERADOR e promover Gestor/Líder por um fluxo administrativo.
- **Sem CORS configurado** no backend — só relevante se um front-end web (fora do app Android) for consumir a API a partir de outra origem.
- **Sem testes automatizados reais** em nenhum dos dois módulos (apenas os testes de exemplo gerados pelos templates do Spring Initializr e do Android Studio).
- **Persistência de perfil no MongoDB, não JPA/Hibernate**: o backend usa `spring-boot-starter-data-mongodb` (Spring Data MongoDB) para persistência em banco NoSQL, conforme permitido pelo enunciado da sprint — não há uso de JPA/Hibernate, que é tipicamente associado a bancos relacionais.
- **Duplicação histórica no repositório**: a pasta `src/`, o `pom.xml` e o `Dockerfile` na raiz refletem uma versão anterior do backend (sem os endpoints de usuário/gamificação) preservada por causa de um merge de branches; o código-fonte de referência é sempre `backend_aguia_branca_sprint2/`.
- **Tema visual do app não centralizado**: as cores de marca e de status são definidas diretamente nas telas (`HomeScreen.kt`, `LoginScreen.kt`, `RegisterScreen.kt`) em vez de em `ui/theme/Color.kt`; o Material You (cor dinâmica) fica habilitado em paralelo em Android 12+.
- **`google-services.json`** presente no módulo do app sem nenhum plugin/dependência do Firebase configurada — arquivo não utilizado atualmente.
