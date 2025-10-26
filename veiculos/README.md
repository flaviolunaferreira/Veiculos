# 🚗 API de Análise Veicular

Sistema de análise unificada de dados veiculares que integra múltiplas fontes (F1 via SOAP, F2 e F3 via REST) para fornecer informações consolidadas sobre veículos com resiliência, observabilidade e idempotência.

---

## 📋 Índice

- [Tecnologias](#-tecnologias)
- [Pré-requisitos](#-pré-requisitos)
- [Início Rápido](#-início-rápido)
- [Arquitetura](#-arquitetura)
- [Funcionalidades](#-funcionalidades)
- [Portas e Endpoints](#-portas-e-endpoints)
- [Comandos Úteis](#-comandos-úteis)
- [Monitoramento](#-monitoramento)
- [Troubleshooting](#-troubleshooting)

---

## 🚀 Tecnologias

### Core
- **Java 21**
- **Spring Boot 3.2.0**
- **Gradle 8.14.3**

### Integrações
- **Spring Cloud OpenFeign** - Clientes REST (F2, F3)
- **Spring Web Services** - Cliente SOAP (F1)
- **Apache HttpClient 4.5.14** - Transport SOAP
- **Spring Kafka** - Mensageria assíncrona

### Persistência
- **Spring Data MongoDB** - Idempotência (TTL 24h)

### Resiliência
- **Resilience4j** - Circuit Breaker, Retry, Rate Limiter, Bulkhead

### Observabilidade
- **Micrometer + Zipkin** - Tracing distribuído
- **Spring Actuator** - Métricas e health checks

### Documentação
- **Swagger/OpenAPI 3.0** - Documentação interativa

### Infraestrutura
- **Docker & Docker Compose**
- **MongoDB 7**
- **Apache Kafka 7.3.3**
- **Zipkin**
- **WireMock** - Mocks de fornecedores

---

## 📋 Pré-requisitos

- Java 21+
- Docker e Docker Compose
- Gradle 8.x (incluído via wrapper)

---

## ⚡ Início Rápido

### Opção 1: Script Automático (Recomendado)
```bash
./start.sh
```

### Opção 2: Manual
```bash
# 1. Build
./gradlew clean build -x test

# 2. Subir containers
docker-compose up --build -d

# 3. Verificar status
docker-compose ps

# 4. Ver logs
docker-compose logs -f app
```

### Verificar se está funcionando
```bash
# Health check
curl http://localhost:8080/actuator/health

# Swagger UI (abra no navegador)
http://localhost:8080/swagger-ui.html
```

---

## 🏗️ Arquitetura

### Visão Geral
```
┌────────────────────────────────────────────────────┐
│      SPRING BOOT APPLICATION (Port 8080)           │
│                                                    │
│  • REST API + Swagger UI                          │
│  • Resilience4j (CB, Retry, RateLimit, Bulkhead) │
│  • Execução Paralela (CompletableFuture)          │
│  • Normalização de Identificadores                │
│  • Idempotência (MongoDB)                          │
│  • Logging Assíncrono (Kafka)                      │
│  • Tracing Distribuído (Zipkin)                    │
└──────┬─────────┬──────────┬──────────┬────────────┘
       │         │          │          │
   ┌───▼───┐ ┌──▼──┐  ┌────▼────┐  ┌─▼──────┐
   │MongoDB│ │Kafka│  │WireMock │  │Zipkin  │
   │:27017 │ │:9092│  │  :9090  │  │ :9411  │
   └───────┘ └─────┘  └─────────┘  └────────┘
```

### Arquitetura Hexagonal (Ports & Adapters)
```
src/main/java/.../veiculos/
├── api/                    # Controllers (REST)
├── application/
│   ├── port/               # Interfaces (Ports)
│   ├── service/            # Lógica de negócio
│   └── usecase/            # Casos de uso
├── domain/                 # Modelos de domínio
│   ├── exception/
│   └── f1/, f2/, f3/       # DTOs dos fornecedores
└── infrastructure/
    ├── adapters/           # Implementações (Adapters)
    │   ├── messaging/      # Kafka
    │   ├── persistence/    # MongoDB
    │   ├── rest/           # Clientes REST (Feign)
    │   ├── soap/           # Cliente SOAP
    │   └── normalization/  # Normalização de IDs
    ├── config/             # Configurações Spring
    └── mappers/            # Mapeadores de dados
```

---

## ✨ Funcionalidades

### 1. Normalização de Identificadores
- Aceita **Placa** (ABC1234), **RENAVAM** (11 dígitos) ou **VIN** (17 caracteres)
- Converte automaticamente para VIN canônico
- Validação com regex

### 2. Integração com Fornecedores
- **F1 (SOAP):** Consulta restrições via Web Services
- **F2 (REST):** Consulta **condicional** - só executa se F1 detectar restrições
- **F3 (REST):** Consulta infrações em **paralelo** com F1

### 3. Execução Paralela Inteligente
- F1 e F3 executam simultaneamente usando `CompletableFuture`
- F2 só é chamado se F1 retornar restrições (renajud ou recall)
- Consolidação de dados de múltiplas fontes

### 4. Resiliência Completa
- **Circuit Breaker:** Falha rápida quando fornecedor instável
- **Retry:** 3 tentativas com backoff exponencial
- **Rate Limiter:** F1 limitado a 2 req/s (SOAP)
- **Bulkhead:** Isolamento de threads
- **Timeouts:** 350ms para HTTP/SOAP, 400ms total

### 5. Idempotência
- Gerenciada **automaticamente** pelo sistema usando hash SHA-256 do identificador
- Armazenamento no **MongoDB** com TTL de 24h
- **Transparente para o usuário** - não requer headers especiais
- Retorna resposta cacheada para o mesmo identificador dentro de 24h
- Previne processamento duplicado e custos desnecessários

### 6. Observabilidade
- **Métricas:** Latência por fornecedor, custo por análise, SLO
- **Tracing:** Zipkin com TraceId e SpanId
- **Logs:** Estruturados com contexto completo
- **Health Checks:** Actuator com detalhes de componentes

### 7. Logging Assíncrono
- Publica logs de análise no **Kafka**
- Não bloqueia resposta ao cliente
- Informações de custo e performance

---

## 🌐 Portas e Endpoints

### Aplicação Principal
| Serviço | Porta | URL |
|---------|-------|-----|
| **API REST** | 8080 | http://localhost:8080 |
| **Swagger UI** | 8080 | http://localhost:8080/swagger-ui.html |
| **Actuator** | 8080 | http://localhost:8080/actuator |

### Infraestrutura
| Serviço | Porta | Acesso |
|---------|-------|--------|
| **MongoDB** | 27017 | `docker exec -it veiculos_mongo-db_1 mongosh -u user -p pass` |
| **Kafka** | 9092 | localhost:9092 |
| **Zipkin** | 9411 | http://localhost:9411 |
| **WireMock** | 9090 | http://localhost:9090 |
| **Zookeeper** | 2181 | localhost:2181 |

### Endpoints da API

#### Análise de Veículo
```bash
GET /api/v1/veiculos/{idveiculo}/analise
```

**Exemplo com Placa:**
```bash
curl -X GET "http://localhost:8080/api/v1/veiculos/ABC1234/analise"
```

**Exemplo com RENAVAM:**
```bash
curl -X GET "http://localhost:8080/api/v1/veiculos/12345678901/analise"
```

**Exemplo com VIN:**
```bash
curl -X GET "http://localhost:8080/api/v1/veiculos/9BWZZZ3T8DXXXXXX/analise"
```

**Resposta:**
```json
{
  "vin": "VIN_DE_ABC1234",
  "constraints": {
    "renajud": false,
    "recall": false
  },
  "infractions": {
    "totalAmount": 450.75,
    "details": [
      {
        "description": "Excesso de velocidade",
        "amount": 195.23
      }
    ]
  },
  "supplierStatus": {
    "F1": {
      "status": "SUCCESS",
      "latencyMs": 120,
      "error": null
    },
    "F2": {
      "status": "NOT_CALLED",
      "latencyMs": 0,
      "error": null
    },
    "F3": {
      "status": "SUCCESS",
      "latencyMs": 95,
      "error": null
    }
  }
}
```

---

## 🔧 Comandos Úteis

### Gerenciamento de Containers
```bash
# Ver status
docker-compose ps

# Ver logs em tempo real
docker-compose logs -f app

# Parar tudo
docker-compose down

# Parar e limpar volumes
docker-compose down -v

# Reiniciar apenas a app
docker-compose restart app
```

### Build e Deploy
```bash
# Build completo
./gradlew clean build -x test

# Rebuild e restart
docker-compose down
./gradlew clean build -x test
docker-compose up --build -d
```

### Desenvolvimento
```bash
# Rodar localmente (sem Docker)
./gradlew bootRun

# Rodar testes
./gradlew test

# Ver cobertura
./gradlew test jacocoTestReport
```

---

## 📊 Monitoramento

### Health Check
```bash
curl http://localhost:8080/actuator/health | jq
```

### Métricas
```bash
# Listar todas
curl http://localhost:8080/actuator/metrics | jq

# Latência dos fornecedores
curl http://localhost:8080/actuator/metrics/supplier.latency | jq

# Custo das análises
curl http://localhost:8080/actuator/metrics/analysis.cost.cents | jq

# Requisições HTTP
curl http://localhost:8080/actuator/metrics/http.server.requests | jq
```

### Logs
```bash
# Todos os logs
docker-compose logs -f

# Apenas erros
docker-compose logs app | grep ERROR

# Filtrar por VIN
docker-compose logs app | grep "VIN:ABC1234"

# Últimas 100 linhas
docker-compose logs --tail=100 app
```

### Zipkin (Tracing)
1. Acesse: http://localhost:9411
2. Clique em "Run Query" para ver traces
3. Selecione um trace para ver detalhes de spans, latências e erros

### Kafka (Logs Assíncronos)
```bash
# Listar tópicos
docker exec veiculos_kafka_1 kafka-topics.sh \
  --list --bootstrap-server localhost:29092

# Consumir mensagens
docker exec veiculos_kafka_1 kafka-console-consumer.sh \
  --bootstrap-server localhost:29092 \
  --topic vehicle-analysis-logs \
  --from-beginning
```

### MongoDB (Idempotência)
```bash
# Conectar
docker exec -it veiculos_mongo-db_1 mongosh -u user -p pass

# Dentro do MongoDB
use idempotency_store
db.idempotency_store.find().pretty()
db.idempotency_store.count()
```

---

## 🐛 Troubleshooting

### Aplicação não inicia
```bash
# Ver logs de erro
docker-compose logs app | grep ERROR

# Verificar se JAR foi gerado
ls -lh build/libs/

# Rebuild completo
./gradlew clean build -x test
docker-compose up --build
```

### Porta 8080 já em uso
```bash
# Ver o que está usando a porta
lsof -i :8080

# Parar processo específico
kill -9 <PID>
```

### MongoDB não conecta
```bash
# Verificar se está rodando
docker ps | grep mongo

# Ver logs
docker-compose logs mongo-db

# Reiniciar
docker-compose restart mongo-db
```

### Kafka não recebe mensagens
```bash
# Verificar logs
docker-compose logs kafka

# Criar tópico manualmente
docker exec veiculos_kafka_1 kafka-topics.sh \
  --create --bootstrap-server localhost:29092 \
  --topic vehicle-analysis-logs \
  --partitions 3 --replication-factor 1
```

### Health check retorna DOWN
```bash
# Ver detalhes
curl http://localhost:8080/actuator/health | jq

# Verificar componentes
docker-compose ps
```

---

## 🧪 Testando a API

### 1. Testar Idempotência (Automática)
```bash
# Primeira chamada - processa e cacheia
curl -X GET "http://localhost:8080/api/v1/veiculos/ABC1234/analise"

# Segunda chamada - retorna do cache (idempotência automática)
curl -X GET "http://localhost:8080/api/v1/veiculos/ABC1234/analise"

# O mesmo identificador dentro de 24h sempre retorna do cache
# Isso economiza custos e tempo de processamento
```

### 2. Testar Diferentes Identificadores
```bash
# Placa (será convertida para VIN)
curl -X GET "http://localhost:8080/api/v1/veiculos/ABC1234/analise"

# RENAVAM (será convertido para VIN)
curl -X GET "http://localhost:8080/api/v1/veiculos/12345678901/analise"

# VIN (já é o identificador canônico)
curl -X GET "http://localhost:8080/api/v1/veiculos/9BWZZZ3T8DXXXXXX/analise"
```

### 3. Via Swagger UI
1. Acesse: http://localhost:8080/swagger-ui.html
2. Expanda o endpoint `/api/v1/veiculos/{idveiculo}/analise`
3. Clique em "Try it out"
4. Preencha os campos e clique em "Execute"

---

## 📈 Fluxo de Execução

```
1. Cliente faz requisição com identificador (Placa/RENAVAM/VIN)
                ↓
2. Sistema gera chave de idempotência automaticamente (SHA-256)
                ↓
3. Verifica cache no MongoDB usando a chave gerada
   ├─ Se existe (dentro de 24h) → Retorna resposta cacheada
   └─ Se não existe → Continua processamento
                ↓
4. Normaliza identificador para VIN canônico
                ↓
5. Executa chamadas aos fornecedores:
   ├─ F1 (SOAP) e F3 (REST) em PARALELO
   └─ F2 (REST) CONDICIONAL (só se F1 detectar restrições)
                ↓
6. Consolida dados de todos os fornecedores
                ↓
7. Salva no MongoDB com chave de idempotência (TTL 24h)
                ↓
8. Publica log no Kafka (assíncrono)
                ↓
9. Retorna resposta ao cliente
```

---

## 📝 Variáveis de Ambiente

Configure no `docker-compose.yml` ou `.env`:

```bash
# MongoDB
SPRING_DATA_MONGODB_URI=mongodb://user:pass@mongo-db:27017/idempotency_store?authSource=admin

# Kafka
KAFKA_BOOTSTRAP_SERVERS=kafka:29092

# Fornecedores (Mocks via WireMock)
SUPPLIERS_F1_URL=http://mocks:8080/ws/f1
SUPPLIERS_F2_URL=http://mocks:8080/api/f2
SUPPLIERS_F3_URL=http://mocks:8080/api/f3
```

---

## 🎯 Próximos Passos

- [ ] Implementar testes unitários e de integração
- [ ] Configurar mocks no WireMock para F1, F2 e F3
- [ ] Implementar autenticação JWT real
- [ ] Adicionar health checks customizados
- [ ] Configurar CI/CD pipeline
- [ ] Adicionar mais métricas de negócio

---

## 📄 Licença

Este projeto foi desenvolvido como parte de um desafio técnico.

---

## 👥 Suporte

Para dúvidas ou problemas:
1. Verifique os logs: `docker-compose logs -f app`
2. Consulte o Swagger UI: http://localhost:8080/swagger-ui.html
3. Verifique o health check: `curl http://localhost:8080/actuator/health`

---

**Sistema totalmente funcional e pronto para uso!** 🚀

**Nota:** Certifique-se de ajustar as variáveis de ambiente no `application.properties` para apontar para os serviços locais.

## 📚 Documentação da API

Após iniciar a aplicação, acesse:

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI Spec:** http://localhost:8080/v3/api-docs

## 🔐 Autenticação

A API usa autenticação JWT via Bearer Token. Para testar:

1. Adicione o header `Authorization: Bearer <seu-token-jwt>`
2. Adicione o header `Idempotency-Key: <uuid-unico>` para garantir idempotência

## 📡 Endpoint Principal

### Análise de Veículo

```http
GET /api/v1/veiculos/{idveiculo}/analise
Headers:
  - Authorization: Bearer <token>
  - Idempotency-Key: <uuid>
```

**Parâmetros:**
- `idveiculo`: Placa (ABC1234), RENAVAM (11 dígitos) ou VIN (17 caracteres)

**Resposta:**
```json
{
  "vin": "9BWZZZ3T8D",
  "constraints": {
    "renajud": true,
    "recall": false
  },
  "infractions": {
    "totalAmount": 450.75,
    "details": [
      {
        "description": "Excesso de velocidade",
        "amount": 195.23
      }
    ]
  },
  "supplierStatus": {
    "F1": {
      "status": "SUCCESS",
      "latencyMs": 120,
      "error": null
    },
    "F2": {
      "status": "SUCCESS",
      "latencyMs": 85,
      "error": null
    },
    "F3": {
      "status": "SUCCESS",
      "latencyMs": 95,
      "error": null
    }
  }
}
```

## 🏗️ Arquitetura

O projeto segue uma arquitetura hexagonal (Ports & Adapters):

```
src/main/java/br/com/desafio/veiculos/
├── api/                    # Controllers (REST)
├── application/
│   ├── port/               # Interfaces (Ports)
│   ├── service/            # Lógica de negócio
│   └── usecase/            # Casos de uso
├── domain/                 # Modelos de domínio
│   ├── exception/
│   ├── f1/, f2/, f3/      # DTOs dos fornecedores
└── infrastructure/
    ├── adapters/           # Implementações (Adapters)
    │   ├── messaging/      # Kafka
    │   ├── persistence/    # MongoDB
    │   ├── rest/           # Clientes REST (Feign)
    │   ├── soap/           # Cliente SOAP
    │   └── normalization/  # Normalização de identificadores
    ├── config/             # Configurações Spring
    └── mappers/            # Mapeadores de dados
```

## 🔄 Fluxo de Execução

1. **Validação de Idempotência:** Verifica se a requisição já foi processada
2. **Normalização do Identificador:** Converte Placa/RENAVAM para VIN
3. **Consulta Paralela:**
   - F1 (SOAP): Busca restrições básicas
   - F3 (REST): Busca infrações
   - F2 (REST): **Condicional** - só é chamado se F1 detectar restrições
4. **Consolidação:** Merge dos dados de todos os fornecedores
5. **Log Assíncrono:** Envia log para Kafka
6. **Armazenamento:** Salva resultado no MongoDB para idempotência

## 🛡️ Resiliência

O sistema implementa múltiplos padrões de resiliência:

- **Circuit Breaker:** Falha rápida quando um fornecedor está instável
- **Retry:** Retenta chamadas com backoff exponencial
- **Rate Limiter:** Limita chamadas ao F1 (2 req/s)
- **Bulkhead:** Isola threads para evitar esgotamento de recursos
- **Timeouts:** 350ms para chamadas HTTP/SOAP, 400ms para tempo total
- **Fallback:** Retorna dados parciais em caso de falha

## 📊 Observabilidade

- **Metrics:** Métricas exportadas via Actuator (`/actuator/metrics`)
- **Tracing:** Rastreamento distribuído via Zipkin
- **Logs:** Estruturados com TraceId e SpanId
- **SLO Tracking:** Métricas de latência e taxa de sucesso

## 🧪 Testes

```bash
# Executar todos os testes
./gradlew test

# Executar testes com coverage
./gradlew test jacocoTestReport
```

## 🐛 Troubleshooting

### Erro ao conectar no MongoDB
```bash
# Verificar se o MongoDB está rodando
docker ps | grep mongo
```

### Kafka não está processando mensagens
```bash
# Verificar logs do Kafka
docker-compose logs kafka
```

### Fornecedores não respondem
```bash
# Verificar se o WireMock está rodando
curl http://localhost:9090/__admin/mappings
```

## 📝 Variáveis de Ambiente

Principais variáveis configuráveis:

```properties
# Banco de Dados
spring.data.mongodb.uri=mongodb://user:pass@localhost:27017/idempotency_store

# Kafka
spring.kafka.producer.bootstrap-servers=localhost:9092

# Fornecedores
suppliers.f1.url=http://f1-supplier.com/ws
suppliers.f2.url=http://f2-supplier.com/api/v1
suppliers.f3.url=http://f3-supplier.com/api

# Segurança
jwt.secret.key=<sua-chave-secreta>
```

## 📄 Licença

Este projeto foi desenvolvido como parte de um desafio técnico.

## 👥 Autor

Desenvolvido por [Seu Nome]

