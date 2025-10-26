# Normalize - Log Consumer Service

Serviço independente de consumo de logs de análise de veículos, baseado em Kafka e PostgreSQL.

## Tecnologias

- **Java 21**
- **Spring Boot 3.2.0**
- **Spring Kafka** - Consumer de mensagens
- **Spring Data JPA** - Persistência
- **PostgreSQL 16** - Banco de dados
- **Micrometer Tracing** - Rastreamento distribuído
- **Zipkin** - UI de tracing
- **Docker & Docker Compose** - Containerização
- **Gradle** - Build tool
- **Lombok** - Redução de boilerplate

## Estrutura do Projeto

```
normalize/
├── src/
│   ├── main/
│   │   ├── java/br/com/desafio/normalize/
│   │   │   ├── NormalizeApplication.java         # Classe principal
│   │   │   ├── consumer/
│   │   │   │   └── AnalysisLogConsumer.java      # Consumer Kafka
│   │   │   ├── domain/
│   │   │   │   ├── VehicleAnalysisLog.java       # DTO Kafka
│   │   │   │   └── SupplierStatus.java           # DTO aninhado
│   │   │   └── persistence/
│   │   │       ├── VehicleAnalysisLogEntity.java # Entidade JPA
│   │   │       └── VehicleAnalysisLogRepository.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
├── build.gradle
├── settings.gradle
├── Dockerfile
└── docker-compose.yml
```

## Configuração

### Variáveis de Ambiente

- `SPRING_DATASOURCE_URL`: URL do PostgreSQL (default: jdbc:postgresql://localhost:5433/normalize_db)
- `SPRING_DATASOURCE_USERNAME`: Usuário do banco (default: normalize_user)
- `SPRING_DATASOURCE_PASSWORD`: Senha do banco (default: normalize_pass)
- `KAFKA_BOOTSTRAP_SERVERS`: Servidores Kafka (default: localhost:9092)
- `ZIPKIN_ENDPOINT`: Endpoint do Zipkin (default: http://zipkin:9411/api/v2/spans)

### Tópico Kafka

O serviço consome mensagens do tópico: `vehicle_analysis_log`

## Executar Localmente

### 1. Build do projeto

```bash
./gradlew clean build
```

### 2. Subir infraestrutura com Docker Compose

```bash
docker-compose up -d
```

Isto irá iniciar:
- PostgreSQL (porta 5433)
- Kafka (porta 9092)
- Zipkin (porta 9411)
- Aplicação normalize (porta 8081)

### 3. Verificar logs

```bash
docker-compose logs -f normalize-app
```

### 4. Acessar Interfaces de Visualização

**Dashboard Principal (recomendado):**
```
http://localhost:8081
```

Visualize em tempo real:
- 📊 Total de logs processados
- ⏱️ Último log recebido
- 📋 Lista dos 10 logs mais recentes
- 🔄 Auto-refresh a cada 3 segundos
- 🎨 Interface visual moderna e intuitiva

**API REST para consultas:**
```bash
# Listar todos os logs (paginado)
curl http://localhost:8081/api/logs

# Ver estatísticas
curl http://localhost:8081/api/logs/stats

# Último log recebido
curl http://localhost:8081/api/logs/latest

# Buscar log específico
curl http://localhost:8081/api/logs/{uuid}
```

**Zipkin UI (rastreamento distribuído):**
```
http://localhost:9411
```

**Health Check:**
```
http://localhost:8081/actuator/health
```

## Build e Deploy

### Build da imagem Docker

```bash
./gradlew clean bootJar
docker build -t normalize:latest .
```

### Executar apenas o container da aplicação

```bash
docker run -p 8081:8081 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5433/normalize_db \
  -e KAFKA_BOOTSTRAP_SERVERS=host.docker.internal:9092 \
  normalize:latest
```

## Health Check

```bash
curl http://localhost:8081/actuator/health
```

## Parar e Limpar

```bash
docker-compose down
docker-compose down -v  # Remove volumes também
```

## Desenvolvimento

### Requisitos

- Java 21
- Gradle 8.x (ou use o wrapper incluído)
- Docker & Docker Compose

### Executar testes

```bash
./gradlew test
```

## Características

- ✅ Consumo assíncrono de mensagens Kafka
- ✅ Persistência em PostgreSQL com suporte a JSONB
- ✅ Rastreamento distribuído com Micrometer e Zipkin
- ✅ **Dashboard Web interativo com auto-refresh**
- ✅ **API REST para consulta de logs**
- ✅ **Visualização em tempo real** (atualiza a cada 3s)
- ✅ Health checks via Actuator
- ✅ Totalmente containerizado
- ✅ Configuração via variáveis de ambiente
- ✅ Independente de outros serviços

## Visualização e Monitoramento

### 🎨 Dashboard Web

Acesse `http://localhost:8081` para ver:

1. **Estatísticas em Tempo Real:**
   - Total de logs processados
   - Tempo desde o último log
   - Status da conexão
   - Estado do auto-refresh

2. **Tabela de Logs:**
   - 10 logs mais recentes
   - Timestamp de cada análise
   - Tipo de identificador (PLACA/RENAVAM/VIN)
   - VIN canônico encontrado
   - Se há restrições
   - Custo estimado
   - Trace ID para correlação

3. **Controles:**
   - Atualizar manualmente
   - Pausar/Iniciar auto-refresh
   - Links para Health Check e Zipkin

### 🔍 Endpoints da API

```bash
# Listar logs (paginado)
GET /api/logs?page=0&size=20

# Estatísticas gerais
GET /api/logs/stats

# Último log processado
GET /api/logs/latest

# Log específico por ID
GET /api/logs/{uuid}
```

### 📊 Zipkin (Tracing Distribuído)

Acesse `http://localhost:9411` para:
- Rastrear requisições entre serviços
- Ver latências de cada operação
- Debugar problemas de performance
- Correlacionar logs usando traceId

## Notas

- Este serviço é **totalmente independente** do projeto `veiculos`
- Usa PostgreSQL em vez de outros bancos de dados
- Porta padrão: 8081 (diferente do serviço veículos)
- Banco de dados: normalize_db (porta 5433 para evitar conflitos)

