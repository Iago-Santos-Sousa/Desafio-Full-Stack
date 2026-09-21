# Execução local do back-end

Este documento descreve execução local do back-end implementado. Todo build ocorre via Docker; comandos não exigem Java ou Maven no host.

## Índice

- [Execução local do back-end](#execução-local-do-back-end)
  - [Índice](#índice)
  - [Pré-requisitos](#pré-requisitos)
  - [Configuração](#configuração)
  - [Arquitetura e decisões de performance](#arquitetura-e-decisões-de-performance)
  - [Migrations PostgreSQL e criação do schema](#migrations-postgresql-e-criação-do-schema)
  - [Subir somente dependências e back-end](#subir-somente-dependências-e-back-end)
  - [Documentação OpenAPI](#documentação-openapi)
  - [Fluxo mínimo](#fluxo-mínimo)
  - [IntelliJ IDEA: formatação e lint](#intellij-idea-formatação-e-lint)
    - [1. Abrir e configurar Java 21](#1-abrir-e-configurar-java-21)
    - [2. Instalar plugins](#2-instalar-plugins)
    - [3. Apontar CheckStyle-IDEA para regra versionada](#3-apontar-checkstyle-idea-para-regra-versionada)
    - [4. Ativar Google Java Format](#4-ativar-google-java-format)
    - [5. Validar localmente](#5-validar-localmente)
  - [Benchmark e evidências](#benchmark-e-evidências)
  - [Gerar massa de teste](#gerar-massa-de-teste)
  - [Parar ambiente](#parar-ambiente)
  - [Versões do back-end](#versões-do-back-end)
  - [APIs e avisos de depreciação](#apis-e-avisos-de-depreciação)
  - [Estado atual](#estado-atual)
  - [Problemas comuns](#problemas-comuns)
  - [Observabilidade e CSV](#observabilidade-e-csv)
  - [Dashboard por jobs e datas](#dashboard-por-jobs-e-datas)

Subtópicos de configuração da IDE:

- [Abrir e configurar Java 21](#1-abrir-e-configurar-java-21)
- [Instalar plugins](#2-instalar-plugins)
- [Apontar CheckStyle-IDEA para regra versionada](#3-apontar-checkstyle-idea-para-regra-versionada)
- [Ativar Google Java Format](#4-ativar-google-java-format)
- [Validar localmente](#5-validar-localmente)

## Pré-requisitos

- Docker Engine/Desktop com Docker Compose v2.
- Portas livres: `8080` para API, `5432` para PostgreSQL e `15672` para painel RabbitMQ.
- Java e Maven locais não são necessários. Build ocorre em container multi-stage.

## Configuração

Na raiz do repositório, copie `.env.example` para `.env` e ajuste somente quando necessário. Valores esperados:

```dotenv
POSTGRES_DB=large_data
POSTGRES_USER=app
POSTGRES_PASSWORD=change-me-local
RABBITMQ_DEFAULT_USER=app
RABBITMQ_DEFAULT_PASS=change-me-local
BACKEND_PORT=8080
SERVER_PORT=8080
APP_BUSINESS_TIME_ZONE=America/Sao_Paulo
```

`.env` contém credenciais locais e não deve ser versionado. `.env.example` terá valores seguros de exemplo.

Ao executar Java/Maven diretamente dentro de `back-end`, copie `back-end/.env.example` para `back-end/.env`. Spring Boot importa esse arquivo apenas se existir; executando a partir da raiz, o mesmo caminho também é reconhecido. Variáveis exportadas no processo têm prioridade. No Docker Compose, a raiz `.env` continua sendo interpolada e injetada pelo Compose.

`SERVER_PORT` define porta interna da API e usa `8080` como fallback. `BACKEND_PORT` define porta publicada na máquina host.

## Arquitetura e decisões de performance

Java 21 + Spring Boot 4.1 atende API REST. JPA/Hibernate é usado para metadados e entidades; TypeORM não se aplica ao ecossistema Java. Spring Batch mantém leitura CSV em streaming e chunks de 2.000; `JdbcBatchItemWriter` grava registros sem insert individual. RabbitMQ recebe somente `jobId` por outbox transacional, com retry limitado, exchange durável, DLX e DLQ.

Fluxo resumido: o multipart é copiado para disco, o job é persistido e RabbitMQ transporta apenas seu identificador. O worker lê o CSV linha a linha, valida registros e grava cada chunk de 2.000 em batch; após o commit, atualiza contadores e segue para o próximo lote. Assim, memória usada depende do chunk, não do tamanho total do arquivo.

Upload multipart grava em disco (`file-size-threshold=0B`) e aceita no máximo 2 GiB. `BACKEND_MEMORY_LIMIT` limita o container da API, com fallback `2g`; ausência de OOM deve ser confirmada por benchmark, não presumida.

O índice `(ingestion_job_id, id)` atende cursor/keyset. A consulta de registros busca IDs primeiro e carrega `row_number`/`data JSONB` apenas para a página. Assim, payload dinâmico não força varredura de registros de outros jobs. Agregações usam métricas persistidas, não `GROUP BY` integral a cada refresh.

- `controller`: HTTP, validação de entrada e Problem Details.
- `service`: casos de uso e orquestração.
- `repository`: JPA/JDBC e consultas keyset.
- `entity`: persistência JPA.
- `batch`: reader streaming, chunks e writer JDBC.
- `messaging`: outbox, publisher confirms, retry, DLX e DLQ.
- `domain`: invariantes, value objects e políticas testáveis.

Controllers não acessam SQL ou `JdbcTemplate`. Entidades JPA não são retornadas diretamente; DTOs protegem o contrato REST.

## Migrations PostgreSQL e criação do schema

Flyway é a única ferramenta responsável por criar e alterar o schema. A API usa
`spring.jpa.hibernate.ddl-auto=validate`: Hibernate valida o mapeamento, mas não
cria nem altera tabelas. Spring Batch também usa metadados JDBC versionados pela
migration `V8__spring_batch_metadata.sql`; por isso
`spring.batch.jdbc.initialize-schema=never` evita dois inicializadores concorrentes.

No `docker compose up --build` completo, PostgreSQL cria primeiro banco, usuário e
volume. Depois do healthcheck, `back-end` executa migrations Flyway pendentes e só
então inicializa JPA. Em volume novo, V1 até V8 criam as tabelas de negócio,
índices, sequences e tabelas `BATCH_*`. Em volume existente, apenas versões ainda
não aplicadas são executadas; dados e tabelas existentes são preservados.

Subir somente `postgres` não executa Flyway e não cria tabelas da aplicação. Se uma
migration falhar, a API não deve iniciar com schema parcial. Não edite migration já
aplicada: Flyway valida checksum e exige nova migration (`V9`, `V10` etc.) para
qualquer alteração posterior. A migration V4 é destrutiva por decisão aprovada e
remove tabelas financeiras antigas durante a migração para registros CSV dinâmicos.

Verificar histórico e tabelas dentro do PostgreSQL:

```bash
docker compose exec postgres sh -c 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c "SELECT installed_rank, version, description, success FROM flyway_schema_history ORDER BY installed_rank;"'
docker compose exec postgres sh -c 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c "SELECT table_name FROM information_schema.tables WHERE table_schema = '\''public'\'' AND table_name IN ('\''ingestion_job'\'','\''csv_record'\'','\''ingestion_job_metric'\'','\''ingestion_event_outbox'\'','\''batch_job_instance'\'','\''batch_job_execution'\'','\''batch_step_execution'\'') ORDER BY table_name;"'
```

No PowerShell, use os valores definidos no `.env` caso `$POSTGRES_USER` e
`$POSTGRES_DB` não estejam exportados:

```powershell
docker compose exec postgres psql -U app -d large_data -c "SELECT installed_rank, version, description, success FROM flyway_schema_history ORDER BY installed_rank;"
```

Para testar banco vazio, use projeto/volume Compose isolado. `docker compose down -v`
remove o volume PostgreSQL e todos os dados locais; não execute em ambiente que
precise ser preservado.

## Subir somente dependências e back-end

Execute na raiz:

```bash
docker compose up --build -d postgres rabbitmq back-end
```

Verifique containers:

```bash
docker compose ps
```

Veja logs recentes sem carregar saída inteira:

```bash
docker compose logs --tail=150 -f back-end
```

API ficará disponível em `http://localhost:8080`. Swagger UI ficará em `http://localhost:8080/swagger-ui.html`; especificação OpenAPI, em `http://localhost:8080/v3/api-docs`.

## Documentação OpenAPI

Swagger documenta os grupos `Ingestions`, `Analytics` e `CSV records` com descrições em português, parâmetros, limites, exemplos de sucesso e respostas Problem Details.

- `POST /api/v1/ingestions`: upload multipart de CSV até 2 GiB; retorna `202` com `jobId`, status inicial e `statusUrl`.
- `GET /api/v1/ingestions`: pagina jobs por cursor (`size` entre 1 e 50; padrão 10).
- `GET /api/v1/ingestions/{jobId}` e `/active`: exibem progresso, estados, contadores, timestamps e erros resumidos.
- `GET /api/v1/ingestions/{jobId}/records`: retorna colunas e valores dinâmicos com cursor (`size` entre 1 e 200; padrão 25), `totalRecords` e `totalPages`. Totais usam `validRows` persistido no job; não executam `COUNT(*)` por página.
- `GET /api/v1/analytics/summary` e `/monthly`: aceitam `from`/`to` em `YYYY-MM-DD`, com intervalo inclusivo no fuso `America/Sao_Paulo`.

Exemplos de erros mostram `status`, `code`, `title`, `detail`, `timestamp` e `traceId`. A especificação é somente documentação: contratos REST, status HTTP, paginação e payloads reais permanecem definidos pelo código.

Health check:

```bash
curl http://localhost:8080/actuator/health
```

## Fluxo mínimo

Enviar CSV:

```bash
curl -i -F "file=@./csv_tests/transactions-1m.csv" http://localhost:8080/api/v1/ingestions
```

Resposta esperada: `202 Accepted`, `jobId`, status inicial e URL de acompanhamento. Recebimento do arquivo já terminou; processamento continua em background.

Limite de upload: 2GiB por arquivo (`MAX_FILE_SIZE=2GB`). O limite da requisição é `MAX_REQUEST_SIZE=2056MB` para acomodar overhead multipart. Spring Multipart grava o corpo em disco (`file-size-threshold=0B`), e o serviço copia o conteúdo com streams sem carregar o CSV na RAM. Excesso retorna `413 UPLOAD_TOO_LARGE`.

Consultar progresso:

```bash
curl http://localhost:8080/api/v1/ingestions/SEU_JOB_ID
```

RabbitMQ publica jobs via outbox transacional após commit PostgreSQL. A topologia usa `ingestion.exchange`, `ingestion.jobs`, `ingestion.dlx` e `ingestion.jobs.dlq`; consumidor aplica três tentativas com backoff e encaminha falhas finais para a DLQ, mantendo compatibilidade com volumes RabbitMQ existentes.

`ingestion.jobs` possui política de backpressure em `rabbitmq/definitions.json`: no máximo cinco mensagens ficam prontas e novas publicações são rejeitadas (`reject-publish`) até haver capacidade. O limite não conta mensagens já entregues ao consumidor. `rabbitmq/init-policy.sh` importa política depois do bootstrap padrão, preservando usuário, vhost e credenciais definidos por ambiente.

Confira política e ocupação:

```bash
docker compose exec rabbitmq rabbitmqctl list_policies
docker compose exec rabbitmq rabbitmqctl list_queues name messages_ready messages_unacknowledged consumers
```

A API habilita compressão HTTP para respostas JSON e Problem Details maiores que 2KB. CORS aceita somente `APP_CORS_ALLOWED_ORIGIN` (padrão `http://localhost:5173`), sem credenciais, e expõe `X-Trace-Id`. Swagger não usa `/api/**`, então continua acessível em `/swagger-ui.html` e `/v3/api-docs`.

Inspecionar filas e mensagens:

```bash
docker compose exec rabbitmq rabbitmqctl list_queues name messages messages_ready messages_unacknowledged consumers
docker compose exec rabbitmq rabbitmqctl list_bindings source_name destination_name routing_key
```

Se o volume RabbitMQ já foi usado por uma versão experimental, filas antigas com sufixo `.v2` podem aparecer. A aplicação atual usa somente `ingestion.jobs` e `ingestion.jobs.dlq`; remova volumes apenas se aceitar perder dados locais.

Listar registros dinâmicos com cursor:

```bash
curl "http://localhost:8080/api/v1/ingestions/SEU_JOB_ID/records?size=50"
```

Consultar agregação mensal:

```bash
curl "http://localhost:8080/api/v1/analytics/monthly"
```

## IntelliJ IDEA: formatação e lint

O repositório já versiona a regra em `back-end/config/checkstyle/checkstyle.xml`. IntelliJ fornece feedback local; Maven mantém validação opcional pelo profile `quality`, e Docker não bloqueia a imagem por divergências de lint/formatação.

### 1. Abrir e configurar Java 21

1. Abra a raiz `Desafio_Full_Stack` no IntelliJ e marque o projeto como confiável.
2. Acesse `File > Project Structure > Project` e selecione Java 21 como `Project SDK`.
3. Acesse `Settings > Build, Execution, Deployment > Build Tools > Maven` e selecione Java 21 para o Maven importer.
4. Recarregue o projeto Maven pelo painel `Maven`.

### 2. Instalar plugins

Em `Settings > Plugins > Marketplace`, instale e reinicie o IntelliJ:

- `CheckStyle-IDEA`.
- `google-java-format`, compatível com formatter `1.35.0`.

### 3. Apontar CheckStyle-IDEA para regra versionada

1. Acesse `Settings > Tools > Checkstyle`.
2. Clique em `+` e escolha `Use a local Checkstyle file`.
3. Preencha a descrição com `DataPulse Checkstyle`.
4. Selecione o caminho `$PROJECT_DIR$/back-end/config/checkstyle/checkstyle.xml`.
5. Se o plugin solicitar a versão do engine, selecione `14.1.0`, igual à dependência do Maven.
6. Selecione escopo de fontes Java incluindo testes.
7. Marque a configuração como ativa e confirme em `Apply`/`OK`.
8. Abra a janela `Checkstyle` e execute `Scan` para verificar o projeto.

Se `back-end` for aberto como projeto separado, use `$PROJECT_DIR$/config/checkstyle/checkstyle.xml`. Não escolha uma regra Google/Sun bundled: ela não substitui a configuração do repositório.

### 4. Ativar Google Java Format

1. Acesse `Settings > Other Settings > google-java-format Settings`.
2. Marque `Enable google-java-format`.
3. Se o plugin solicitar acesso aos módulos internos do compilador, abra `Help > Edit Custom VM Options`, adicione as linhas abaixo e reinicie:

```text
--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED
--add-exports=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED
--add-exports=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED
--add-exports=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED
--add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED
--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED
```

O plugin substitui as ações `Code > Reformat Code` e `Code > Optimize Imports`. Se desejar formatação ao salvar, habilite essas ações em `Actions on Save`. Não misture com outro formatter Java nativo.

### 5. Validar localmente

No terminal, a partir da raiz do repositório:

```bash
cd back-end
mvn spotless:apply
mvn spotless:check
mvn checkstyle:check
mvn -DskipTests package
mvn -Pquality verify
```

`mvn -Pquality verify` é o gate opcional completo. Sem Maven local, use container Java 21 para aplicar formato:

```powershell
docker run --rm -v "${PWD}\back-end:/workspace" -w /workspace maven:3.9.11-eclipse-temurin-21 mvn -B spotless:apply
```

Para executar o gate opcional completo pelo container:

```powershell
docker run --rm -v "${PWD}\back-end:/workspace" -w /workspace maven:3.9.11-eclipse-temurin-21 mvn -B -Pquality verify
```

Contrato CSV:

```csv
column_a,column_b,column_c
2026-01-15T10:30:00Z,food,42.90,Lunch
```

## Benchmark e evidências

Gerar e processar 1 milhão de linhas em streaming:

```bash
MSYS_NO_PATHCONV=1 docker compose --env-file .env.example --profile tools run --rm \
  -e OUTPUT=/data/benchmark-1000000.csv \
  --entrypoint node csv-generator benchmark-ingestion.mjs 1000000
```

No Git Bash, prefixe com `MSYS_NO_PATHCONV=1` para impedir conversão de `/data/...` em `C:/Program Files/Git/...`. O volume do serviço mapeia `/data` para `./csv_tests`; portanto, o arquivo fica em `csv_tests/benchmark-1000000.csv`. Em PowerShell, o comando acima pode ser executado sem o prefixo.

Em outro terminal:

```bash
docker stats datapulse-api datapulse-postgres datapulse-rabbitmq
```

Execução validada: 1.000.000 linhas, arquivo de 64.777.936 bytes, upload 2.308 ms, processamento 126.809 ms, 1.000.000 válidas, zero inválidas e zero OOM. Pico observado: API 527,2 MiB de 2 GiB; PostgreSQL 319,7 MiB; RabbitMQ 175,8 MiB. `OOMKilled=false`, `ExitCode=0`, registros e contadores coincidiram.

Teste anterior com 2.000.000 linhas também concluiu sem OOM. Métricas dependem de hardware, volume Docker, chunk e concorrência; não representam garantia universal.

Datasets adicionais podem ser baixados em [Datablist](https://www.datablist.com/learn/csv/download-sample-csv-files). Arquivos locais ficam em `../csv_tests`.

## Gerar massa de teste

Gerador planejado será executado em container, sem Node local:

```powershell
docker compose --env-file .env.example --profile tools run --rm csv-generator 1000000 /data/transactions-1m.csv
```

No Git Bash:

```bash
MSYS_NO_PATHCONV=1 docker compose --env-file .env.example --profile tools run --rm csv-generator 1000000 /data/transactions-1m.csv
```

## Parar ambiente

Preservar volume do PostgreSQL:

```bash
docker compose stop back-end rabbitmq postgres
```

Remover containers, mantendo volumes:

```bash
docker compose down
```

`docker compose down -v` apaga banco local e dados de forma irreversível; use somente quando reset total for intencional.

## Versões do back-end

- Java 21 LTS (Eclipse Temurin 21 no build e no runtime Docker).
- Spring Boot 4.1.0, definido no parent de `back-end/pom.xml`.
- A propriedade Maven `<java.version>` está fixada em `21`.

## APIs e avisos de depreciação

O back-end usa Java 21, Spring Boot 4.1.0, Spring AMQP 4.1.0, Spring Batch 6.0.4 e Commons CSV 1.14.0. O código utiliza `CSVFormat.Builder.get()`, `CorrelationData.Confirm.ack()`/`reason()` e `JobOperator.start(...)`, APIs atuais dessas versões.

Confira versões efetivas sem instalar Maven no host:

```powershell
docker run --rm -v "${PWD}\back-end:/workspace" -w /workspace maven:3.9.11-eclipse-temurin-21 mvn -B dependency:tree -Dincludes=org.apache.commons:commons-csv,org.springframework.amqp:spring-rabbit,org.springframework.batch:spring-batch-core
```

`published_at` em `ingestion_event_outbox` registra auditoria de confirmação RabbitMQ. Eventos `DEAD` não são marcados como publicados.

## Estado atual

Back-end está implementado e executa em container Spring Boot; host precisa somente de Docker Compose.

## Problemas comuns

- Porta ocupada: altere mapeamento via variável suportada ou encerre processo conflitante.
- API não sobe: confira health de PostgreSQL/RabbitMQ e últimos 150 logs do back-end.
- Job parado em `QUEUED`: verifique outbox, consumidor e DLQ com os comandos acima. Após 10 minutos sem início, reconciliador marca job como `FAILED` com `MESSAGE_DELIVERY_TIMEOUT`.
- Upload rejeitado: confira limite configurado, cabeçalho CSV e encoding UTF-8.
- Upload acima de 2GiB: confira `MAX_FILE_SIZE`, `MAX_REQUEST_SIZE` e, se houver proxy reverso, limite equivalente de corpo (`client_max_body_size 2g`).
- Swagger incompatível: confirme versão de `springdoc-openapi` suportada pela versão fixada do Spring Boot.

## Observabilidade e CSV

## Dashboard por jobs e datas

- `GET /api/v1/ingestions?size=10&cursor=` lista jobs com cursor keyset.
- `GET /api/v1/ingestions/{jobId}/records?cursor=&size=` lista registros dinâmicos, retorna `columns`, `totalRecords`, `totalPages` e `nextCursor`. Número da página atual e cursores de páginas já visitadas são controlados pelo cliente; páginas novas devem avançar pelo `nextCursor`.
- Analytics aceita `from` e `to` inclusivos em `YYYY-MM-DD`; padrao usa `America/Sao_Paulo`.
- Limite de `to` equivale ao proximo dia `00:00` exclusivo, evitando perda do ultimo dia.

Jobs que falham depois de iniciar podem conter registros válidos de chunks já confirmados. Esses registros continuam disponíveis em `GET /api/v1/ingestions/{jobId}/records`, inclusive quando status é `FAILED`; linhas inválidas não são persistidas. O recovery marca processamento abandonado como `FAILED` após timeout e preserva o último checkpoint.

Os campos `totalRows`, `processedRows`, `validRows` e `invalidRows` são atualizados uma vez por chunk confirmado (2.000 linhas), nunca por linha. Em job `FAILED`, eles representam o último checkpoint; não representam necessariamente total físico do arquivo. Essa estratégia adiciona uma atualização curta por chunk, sem carregar o CSV na memória.

O checkpoint ocorre na mesma transação da gravação do chunk em `csv_record`. Assim, uma parada do container não deve deixar registros confirmados com contadores zerados; em rollback, registros e contadores são revertidos juntos.

Recovery de processamento abandonado consulta apenas metadados do Spring Batch (`BATCH_STEP_EXECUTION`) para recuperar o último checkpoint confirmado. Não relê o CSV, não executa `COUNT(csv_record)` e não materializa linhas em memória. Se o update de checkpoint afetar zero linhas, o back-end registra erro e falha a transação para evitar divergência silenciosa.

Qualidade Java: Spotless e Checkstyle permanecem versionados, mas não fazem parte do build padrão do container. Use `mvn -Pquality verify` para ativar o gate; `mvn -DskipTests package` valida compilação e empacotamento sem bloquear por estilo. A validação recomendada também executa compilação limpa com `-Xlint:all` para detectar conversões unchecked, tipos raw e APIs depreciadas.

API valida cabeçalho dinâmico não vazio e sem duplicidade antes de criar job. CSV malformado ou fora dos limites retorna `422 CSV_HEADER_INVALID`; registros são persistidos em `csv_record.data` como JSONB.

Progresso é persistido por chunk Spring Batch e consultado por `GET /api/v1/ingestions/{jobId}`. Dashboard usa `GET /api/v1/ingestions/active` para acompanhar jobs em andamento.

Erros são exibidos no console via SLF4J com `jobId` e `traceId`. Respostas usam Problem Details. Containers possuem nomes `datapulse-postgres`, `datapulse-rabbitmq` e `datapulse-api`.

`totalRecords` e `totalPages` da listagem de registros usam `ingestion_job.valid_rows`, mantido junto do chunk confirmado. Não adicionar `COUNT(*)` por página: contagem exata cresce com volume. Também não substituir cursor por `OFFSET`, pois páginas profundas precisam descartar linhas anteriores. O cliente pode voltar ou selecionar páginas cujo cursor já foi obtido; salto direto para página não visitada não faz parte do contrato keyset.
