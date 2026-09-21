# Front-end

Visão geral da solução e execução conjunta estão em [../README.md](../README.md). Este documento detalha desenvolvimento e responsabilidades do módulo web.

React 19 + Vite + TypeScript 6. React Router gerencia navegação; TanStack Query gerencia estado remoto e polling; Axios encapsula API; MUI fornece componentes e tema; Tailwind CSS v4 fornece utilitários de layout.

## Índice

- [Rotas](#rotas)
- [Visão geral](#visão-geral)
- [Dados, loading e erros](#dados-loading-e-erros)
- [Estrutura de diretórios](#estrutura-de-diretórios)
- [Desenvolvimento](#desenvolvimento)
- [Por que esta stack](#por-que-esta-stack)
- [Fluxo de alta volumetria](#fluxo-de-alta-volumetria)
- [Execução via Docker](#execução-via-docker)
- [Validações](#validações)
- [Limites de front-end](#limites-de-front-end)

## Rotas

- `/dashboard`: métricas, agregados e transações com paginação por cursor.
- `/ingestions/new`: upload assíncrono de CSV, com limite visível de 2GiB e erro inline para arquivos maiores.
- `/ingestions/:jobId`: acompanhamento de job e estados terminais.

## Visão geral

## Dados, loading e erros

- Dashboard lista jobs paginados; detalhe abre transacoes isoladas por job.
- Datas ficam na URL como `from` e `to`, com padrao primeiro dia do mes ate hoje no fuso `America/Sao_Paulo`.
- Dashboard exibe filtros acima das métricas; o período aparece junto de `Resumo mensal` e meses do gráfico usam `MM/YYYY`.

- Requisições TanStack Query ficam em `src/hooks/api`.
- Dashboard mostra jobs ativos e atualiza summary/transações durante processamento.
- Skeletons representam carregamento inicial; indicador discreto representa refetch.
- `Snackbar` + `Alert` exibem sucesso/erro com duração configurável.
- Axios normaliza `422`, `404`, `5xx`, timeout e falha de rede; logs não incluem payload sensível.

## Estrutura de diretórios

- `app`: router, providers e tema.
- `layouts`: shell compartilhado.
- `pages`: composição das rotas.
- `features`: componentes/hooks por domínio.
- `components`: componentes visuais reutilizáveis.
- `integrations/api`: cliente Axios e endpoints.
- `types`: contratos TypeScript.
- `utils`: formatação e helpers puros.
- Imports internos usam alias absoluto `@/`, por exemplo `@/components/PageHeader` ou `@/hooks/api/useDashboardQueries`.

## Desenvolvimento

```bash
npm install
npm run dev
```

Prettier formata fontes TypeScript/TSX e ESLint valida regras semânticas. Use `npm run format` para formatar, `npm run format:check` para conferir sem alterar arquivos e `npm run lint` para lint. `.prettierrc.json` e `.prettierignore` ficam versionados; Docker não executa formatação automaticamente.

Verificar tipagem estática strict com `npm run typecheck`. O Vite transpila TypeScript; `tsc` executa verificação semântica completa.

Para stack completa, use `docker compose up --build` na raiz. Configure `VITE_API_URL` quando API não estiver em `http://localhost:8080`.

Para desenvolvimento local, copie `.env.example` para `.env`. Vite carrega esse arquivo automaticamente. `VITE_API_URL` e `VITE_MAX_UPLOAD_BYTES` são valores públicos incorporados no bundle; não coloque credenciais.

## Por que esta stack

Vite mantém feedback rápido e baixo custo em localhost. React organiza páginas e composição de componentes; TypeScript strict protege contratos. MUI fornece calendário, tabelas, dialogs, alerts e tokens acessíveis; Tailwind CSS v4 resolve layout responsivo sem duplicar tema; Chart.js recebe somente agregados; TanStack Query controla cache, polling, cancelamento e invalidação; Axios centraliza responses e logs sanitizados.

## Fluxo de alta volumetria

O browser nunca recebe milhões de linhas. Upload retorna `202 Accepted`; o status é consultado por polling; tabelas usam paginação por cursor; registros JSONB são exibidos somente na página aberta no modal. Skeletons aparecem na carga inicial e durante refetch, enquanto toasts MUI informam sucesso ou erro.

O back-end processa o arquivo em streaming e chunks limitados; o front-end recebe somente progresso e páginas de registros, preservando responsividade mesmo com CSVs de milhões de linhas.

Massas CSV para teste são geradas pelo serviço Docker `csv-generator`; os arquivos persistidos ficam em `../csv_tests`. Comandos e comportamento dos scripts estão documentados em [README global — Testar arquivos CSV](../README.md#testar-arquivos-csv).

## Execução via Docker

Na raiz do projeto:

```bash
copy .env.example .env
docker compose up --build
```

Para desenvolver somente o front-end:

```bash
copy .env.example .env
npm install
npm run dev
```

Configure `VITE_API_URL` para API disponível. `VITE_*` não deve conter credenciais.

## Validações

```bash
npm run typecheck
npm run lint
npm run format:check
npm run build
```

Validação visual deve cobrir 320 px, 679 px e 849 px; `/dashboard`, `/ingestions`, `/ingestions/new` e `/ingestions/:jobId`; loading, refetch, erro, toast, tooltip, ícone de processamento e ausência de overflow. HTTP 200 confirma somente roteamento do Nginx, não comportamento visual.

## Limites de front-end

Não renderizar coleção completa no DOM. Usar paginação server-side, cursor e colunas dinâmicas da resposta. Virtualização só deve ser adicionada se uma página visível exceder capacidade do layout.
