# Etapa 2 — Regras de Negócio e Inovação (API Inovação Águia Branca)

Responsável: **Lucas** · Branch: `feature/etapa2-lucas` · Base: Etapa 1 (login JWT + CRUD de Estratégias)

Este documento tem 3 partes:
1. [O que foi implementado](#1-o-que-foi-implementado)
2. [Como rodar no seu computador](#4-como-rodar-no-seu-computador)
3. [Como testar (Insomnia)](#5-como-testar-insomnia)

---

## 1. O que foi implementado

| Critério da Etapa 2 | Situação | Onde está |
|---|---|---|
| CRUD de Ideias (operador cadastra, gestor aprova vinculando à estratégia vigente) | ✅ Pronto e testado | `controller/IdeiaController`, `service/IdeiaService` |
| CRUD de Projetos (gestor cadastra e atualiza andamento: etapa, status, ROI, prazo) | ✅ Pronto e testado | `controller/ProjetoController`, `service/ProjetoService` |
| IA gratuita (Google Gemini) dando pontuação de viabilidade das ideias | ✅ Pronto e testado com a chave real (nota, justificativa e recomendação) | `service/GeminiService` |
| Endpoints do Dashboard do Líder (dados consolidados: ROI total, lucro) | ✅ Pronto e testado | `controller/DashboardController`, `service/DashboardService` |
| Extra: gamificação — Operador ganha pontos ao ter ideia aprovada + ranking de inovação | ✅ Pronto e testado | `controller/UsuarioController`, `service/UsuarioService` |
| Extra: erros padronizados em JSON (400/401/403/404/409/422/503) | ✅ Pronto e testado | `exception/GlobalExceptionHandler`, `security/SecurityConfig` |
| Extra: collection do Insomnia com as 42 requisições | ✅ Pronta | `insomnia-etapa2-aguia-branca.json` (raiz do projeto) |

### Fluxo do sistema

```
Líder cadastra ESTRATÉGIA vigente
        ↓
Operador cadastra IDEIA  ──►  status PENDENTE
        ↓
Gestor pede análise da IA (Gemini)  ──►  pontuação 0–100 + justificativa
        ↓
Gestor APROVA ou REJEITA  ──►  ideia aprovada copia id/título da estratégia vigente
        ↓
Gestor transforma a ideia aprovada em PROJETO  ──►  ROI calculado automaticamente
        ↓
Líder acompanha tudo somado no DASHBOARD
```

### Regras de negócio implementadas

**Ideias**
- Toda ideia nasce com status `PENDENTE`, autor = e-mail do token (o cliente não escolhe o autor).
- O Operador só edita/exclui **as próprias** ideias (senão `403`) e só enquanto estiverem `PENDENTE` (senão `409`).
- Avaliar exige status `APROVADA` ou `REJEITADA` (mandar `PENDENTE` dá `400`).
- Rejeitar **sem comentário** dá `400` (o autor precisa saber o motivo).
- Aprovar **sem estratégia vigente cadastrada** dá `422`; com estratégia, a ideia copia `estrategiaId` e `estrategiaTitulo`.
- Ideia já avaliada não pode ser reavaliada (`409`).
- **Gamificação:** ao aprovar, o autor da ideia ganha **+100 pontos** automaticamente (`UsuarioService.adicionarPontos`, chamado por `IdeiaService.avaliar`). Rejeitar não pontua.

**Projetos**
- Só o Gestor cadastra/atualiza/exclui; todos os perfis logados conseguem ler.
- `prazoFim` anterior ao `prazoInicio` dá `400`.
- Se informar `ideiaId`: a ideia precisa existir (`404`) e estar `APROVADA` (`422`); uma ideia gera **um** projeto só (`409`).
- O projeto herda a estratégia da ideia (ou, sem ideia, a estratégia vigente do momento).
- **ROI é calculado pela API**, nunca enviado pelo cliente: `ROI % = (retornoEsperado − investimentoPrevisto) ÷ investimentoPrevisto × 100`.
  Valores em `BigDecimal` para não perder centavos.

**IA (Gemini)**
- `POST /api/ideias/{id}/analise-ia` monta um prompt com a ideia + a estratégia vigente e pede um JSON com `pontuacao` (0–100), `justificativa` e `recomendacao`.
- O resultado fica salvo na própria ideia (`pontuacaoViabilidade`, `justificativaIa`, `recomendacaoIa`, `analisadoIaEm`).
- `GET /api/ideias/ranking` devolve as ideias ordenadas pela nota da IA (maior primeiro) para o Gestor priorizar.
- Sem `GEMINI_API_KEY` no `.env`, só esse endpoint fica indisponível (`503` com mensagem explicando) — o resto da API funciona normalmente.

**Gamificação (Ranking de Inovação)**
- `GET /api/usuarios/me` devolve o perfil do usuário logado, incluindo `pontos` — usado pelo app para mostrar "Meus Pontos".
- `GET /api/usuarios/ranking` devolve os top 5 **Operadores** ordenados por pontos (só operadores pontuam, então só eles aparecem aqui).
- Não existe endpoint de cadastro/edição de usuário: os 3 usuários são pré-semeados (`ApiInovacaoApplication`), e os pontos só mudam via aprovação de ideia.

**Dashboard (só Líder)**
- `GET /api/dashboard/resumo` devolve numa chamada só: estratégia vigente + funil de ideias + números dos projetos.
- Projetos: total, quantidade por status e por etapa, investimento total, retorno total, **lucro total**, **ROI total consolidado** (calculado sobre os totais, não é média dos ROIs) e top 3 projetos por ROI.
- Ideias: total, pendentes, aprovadas, rejeitadas, % de aprovação, quantas já passaram pela IA e média das notas.

---

## 2. Permissões por perfil

| Rota | OPERADOR | GESTOR | LÍDER |
|---|:--:|:--:|:--:|
| `POST /api/auth/login` | público | público | público |
| `GET /api/estrategias/**` | ✅ | ✅ | ✅ |
| `POST/PUT/DELETE /api/estrategias/**` | ❌ | ❌ | ✅ |
| `GET /api/ideias/**` | ✅ | ✅ | ✅ |
| `POST/PUT/DELETE /api/ideias` | ✅ | ❌ | ❌ |
| `PATCH /api/ideias/{id}/avaliacao` | ❌ | ✅ | ❌ |
| `POST /api/ideias/{id}/analise-ia` | ❌ | ✅ | ❌ |
| `GET /api/projetos/**` | ✅ | ✅ | ✅ |
| `POST/PUT/PATCH/DELETE /api/projetos/**` | ❌ | ✅ | ❌ |
| `GET /api/usuarios/**` | ✅ | ✅ | ✅ |
| `GET /api/dashboard/**` | ❌ | ❌ | ✅ |

> A ordem das regras no `SecurityConfig` importa: vale a **primeira** que casar com a requisição.

## 3. Endpoints da Etapa 2

| Método | Rota | O que faz |
|---|---|---|
| GET | `/api/ideias?status=PENDENTE` | Lista ideias (filtro opcional por status) |
| GET | `/api/ideias/minhas` | Ideias do usuário logado |
| GET | `/api/ideias/ranking?status=PENDENTE` | Ideias ordenadas pela nota da IA |
| GET | `/api/ideias/{id}` | Detalhe da ideia |
| POST | `/api/ideias` | Operador cadastra ideia |
| PUT | `/api/ideias/{id}` | Autor edita (só se `PENDENTE`) |
| DELETE | `/api/ideias/{id}` | Autor exclui (só se `PENDENTE`) |
| PATCH | `/api/ideias/{id}/avaliacao` | Gestor aprova/rejeita |
| POST | `/api/ideias/{id}/analise-ia` | Gestor pede a pontuação da IA |
| GET | `/api/projetos?status=&etapa=` | Lista projetos (filtros opcionais) |
| GET | `/api/projetos/{id}` | Detalhe do projeto |
| POST | `/api/projetos` | Gestor cadastra projeto |
| PUT | `/api/projetos/{id}` | Gestor atualiza o projeto inteiro |
| PATCH | `/api/projetos/{id}/andamento` | Gestor atualiza só etapa + status |
| DELETE | `/api/projetos/{id}` | Gestor exclui |
| GET | `/api/usuarios/me` | Perfil do usuário logado (com pontos) |
| GET | `/api/usuarios/ranking` | Top 5 Operadores por pontos (gamificação) |
| GET | `/api/dashboard/resumo` | Painel completo do Líder |
| GET | `/api/dashboard/ideias` | Só o funil de ideias |
| GET | `/api/dashboard/projetos` | Só os números dos projetos |

**Corpos de exemplo**

```jsonc
// POST /api/ideias
{ "titulo": "Roteirização inteligente", "descricao": "Usar IA para montar rotas", "beneficioEsperado": "Reduzir 10% do combustível" }

// PATCH /api/ideias/{id}/avaliacao
{ "status": "APROVADA", "comentario": "Alinhada com a meta de redução de custos" }

// POST /api/projetos  (ideiaId é opcional)
{ "titulo": "Roteirização - piloto", "descricao": "Piloto em 20 ônibus",
  "etapa": "PLANEJAMENTO", "status": "EM_ANDAMENTO",
  "investimentoPrevisto": 200000, "retornoEsperado": 260000,
  "prazoInicio": "2026-02-01", "prazoFim": "2026-08-31", "ideiaId": "<id da ideia aprovada>" }

// PATCH /api/projetos/{id}/andamento
{ "etapa": "EXECUCAO", "status": "EM_ANDAMENTO" }
```

Valores aceitos: `StatusIdeia` = PENDENTE/APROVADA/REJEITADA · `EtapaProjeto` = PLANEJAMENTO/EXECUCAO/MONITORAMENTO/ENCERRAMENTO · `StatusProjeto` = NAO_INICIADO/EM_ANDAMENTO/CONCLUIDO/CANCELADO.

**Formato padrão de erro:**
```json
{"timestamp":"2026-09-15T20:51:37","status":422,"erro":"Unprocessable Content","mensagem":"Só ideias APROVADAS podem virar projeto (status atual: PENDENTE)","caminho":"/api/projetos"}
```

---

## 4. Como rodar no seu computador

### Pré-requisitos
- **Java 21** (o projeto não roda em Java 26; no IntelliJ: `File > Project Structure > Project SDK = 21`)
- **Docker Desktop** (para o MongoDB local) ou acesso liberado ao MongoDB Atlas
- Não precisa instalar Maven — use o wrapper `./mvnw` (Git Bash) ou `.\mvnw` (PowerShell)

### Passo a passo

```bash
# 1. Pegar o código atualizado
git fetch origin
git checkout feature/etapa2-lucas
git pull

# 2. Criar o arquivo .env na raiz do projeto (ele NÃO vem no Git)
cp .env.example .env
```

**3. Preencher o `.env`:**

```properties
# Opção A - Mongo local no Docker (mais simples, cada um com seu banco)
MONGODB_URI=mongodb://localhost:27017

# Opção B - Atlas (já liberado pelo Valter; peça a URI/senha atual para ele)
# MONGODB_URI=mongodb+srv://USUARIO:SENHA@clusterfiap.rrkjvjw.mongodb.net/?appName=ClusterFiap

JWT_SECRET=qualquer-texto-longo-secreto
GEMINI_API_KEY=sua-chave-do-google-ai-studio
```

**4. Se for usar o Mongo local, subir o container:**

```bash
docker run -d --name mongo-teste-aguia -p 27017:27017 mongo:7
# se já existir: docker start mongo-teste-aguia
```

**5. Chave gratuita do Gemini** (só quem for mexer/testar a IA): acesse <https://aistudio.google.com/apikey>, faça login com a conta Google, clique em **Create API key**, copie e cole no `.env`. É gratuito e não precisa de cartão.

**6. Rodar:**

```bash
./mvnw spring-boot:run      # Git Bash
.\mvnw spring-boot:run      # PowerShell
# ou, no IntelliJ: botão ▶️ da classe ApiInovacaoApplication (src/main)
```

Na primeira execução a aplicação cria sozinha os 3 usuários de teste (senha `123456`):

| E-mail | Perfil |
|---|---|
| `lider@aguiabranca.com` | LIDER |
| `gestor@aguiabranca.com` | GESTOR |
| `operador@aguiabranca.com` | OPERADOR |

> ⚠️ Nunca commitar o `.env` (já está no `.gitignore`).
> ⚠️ Se der `Port 8080 was already in use`, existe outra instância rodando — feche-a antes.

---

## 5. Como testar (Insomnia)

Na raiz do projeto tem o arquivo **`insomnia-etapa2-aguia-branca.json`**: é a collection pronta, com as 42 requisições da Etapa 2 já montadas. Não precisa digitar URL, token nem id na mão.

### 5.1. Importar

1. Instale o Insomnia: <https://insomnia.rest/download> (pode usar sem criar conta).
2. Na tela inicial, clique em **Import** (ou `Create` → `Import` → `From File`).
3. Escolha o arquivo `insomnia-etapa2-aguia-branca.json` do projeto.
4. Vai aparecer a collection **API Inovacao Aguia Branca - Etapa 2** com 8 pastas.
5. Confira o environment (canto superior esquerdo, "Base Environment"): `base_url` deve ser `http://localhost:8080`.

> Deixe a API rodando (`.\mvnw spring-boot:run`) antes de disparar as requisições.

### 5.2. Rodar

Clique nas pastas **na ordem, de cima para baixo** (00 → 07). O token do login e os ids de ideia/projeto são preenchidos sozinhos: cada requisição lê a resposta da anterior (recurso *response chaining* do Insomnia). Se o seu Insomnia tiver o botão **Run** na collection, dá para rodar tudo de uma vez.

| Pasta | O que faz | Resultado esperado |
|---|---|---|
| 00 - Login | Pega o token dos 3 perfis (senha `123456`) | `200` com `token` |
| 01 - Estratégias (Líder) | Cria/consulta a estratégia vigente | `201` / `200` |
| 02 - Ideias (Operador) | Cadastra, lista, edita e exclui ideia | `201`, `200`, `204` |
| 03 - IA e avaliação (Gestor) | Nota da IA, ranking, aprovar e rejeitar | `200` com `pontuacaoViabilidade` |
| 04 - Projetos (Gestor) | Cria projeto da ideia aprovada, filtros, PUT e PATCH de andamento | `201` / `200` com `roiPercentual` calculado |
| 05 - Dashboard (Líder) | Painel consolidado | `200` com ROI total e lucro |
| 06 - Erros esperados | Cada requisição **deve falhar** com o código que está no nome | `401`, `403`, `400`, `409`, `422` |
| 07 - Limpeza | Apaga o projeto e a ideia de apoio criados no teste | `204` |

O que vale a pena conferir na tela enquanto roda:

- **02 → Criar ideia:** volta `status: "PENDENTE"` e `autorEmail` com o e-mail do token (mesmo se você mandar outro autor no JSON, a API ignora).
- **03 → Análise da IA:** volta `pontuacaoViabilidade` (0 a 100), `justificativaIa` e `recomendacaoIa`. Leva alguns segundos e só funciona com a `GEMINI_API_KEY` no `.env` — sem a chave devolve `503` com a explicação.
- **03 → Aprovar ideia:** volta `estrategiaId` e `estrategiaTitulo` preenchidos (é a ligação com a estratégia vigente).
- **04 → Criar projeto:** `roiPercentual` volta `30.00` (200.000 → 260.000) mesmo sem ninguém mandar ROI no JSON; o `PUT` depois recalcula para `50.00`.
- **05 → Resumo:** `lucroTotal` = retorno − investimento e `roiTotalPercentual` calculado sobre os totais.

### 5.3. Pasta "06 - Erros esperados"

É a prova de que as regras de negócio funcionam — todas essas requisições **têm que dar erro**:

| Requisição | Código | Mensagem |
|---|:--:|---|
| Login com senha errada | `401` | E-mail ou senha inválidos |
| Requisição sem token | `401` | Token ausente ou inválido |
| Gestor tentando criar ideia | `403` | Seu perfil não tem permissão para esta operação |
| Operador tentando avaliar ideia | `403` | idem |
| Gestor acessando o dashboard | `403` | idem |
| Ideia com título vazio | `400` | titulo: não deve estar em branco |
| Filtro `?status=XPTO` | `400` | Valor inválido para o parâmetro 'status' |
| Rejeitar sem comentário | `400` | Para rejeitar uma ideia é obrigatório informar o comentário |
| Prazo final antes do inicial | `400` | O prazo final não pode ser anterior ao prazo inicial |
| Reavaliar ideia já avaliada | `409` | Esta ideia já foi avaliada (status atual: APROVADA) |
| Dois projetos para a mesma ideia | `409` | Esta ideia já virou o projeto ... |
| Projeto a partir de ideia PENDENTE | `422` | Só ideias APROVADAS podem virar projeto |

Outras regras que a collection não cobre porque precisam de um segundo operador ou de um banco vazio (foram validadas à parte): editar/excluir ideia de **outro** operador dá `403`, aprovar ideia **sem estratégia vigente** dá `422` e ideia já avaliada não pode ser editada/excluída (`409`).

### 5.4. Se quiser testar no terminal

Também dá para usar `curl` no Git Bash — a sequência é a mesma da collection: login → estratégia → ideia → análise da IA → aprovar → projeto → andamento → dashboard, sempre mandando `-H "Authorization: Bearer <token>"`.

---

## 6. Pendências do time

- [x] **Valter:** liberou o acesso no *Network Access* do Atlas. Falta ainda **trocar a senha do banco** (a antiga foi exposta no histórico do Git) e repassar a URI nova para o time.
- [ ] **Todos:** com o Atlas liberado dá para usar a `MONGODB_URI` do cluster; quem preferir continua com o Mongo local no Docker (cada um com o seu banco).
- [ ] **Todos:** criar o `.env` local a partir do `.env.example` (o arquivo não vai para o Git).
- [ ] **Quem estava com a `main` antiga:** `git fetch` + `git reset --hard origin/main` antes de criar branch nova.
- [ ] **Front-end:** consumir `/api/dashboard/resumo` para o painel do Líder e `/api/ideias/ranking` para a fila de priorização do Gestor.
