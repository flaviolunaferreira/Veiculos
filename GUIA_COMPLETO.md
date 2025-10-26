# 📘 Guia Completo - Sistema de Análise de Veículos

## Índice

1. [Visão Geral do Sistema](#visão-geral-do-sistema)
2. [Início Rápido](#início-rápido)
3. [Arquitetura e Integração](#arquitetura-e-integração)
4. [Como Executar](#como-executar)
5. [Visualização e Monitoramento](#visualização-e-monitoramento)
6. [Como Ver Logs](#como-ver-logs)
7. [Teste da Integração](#teste-da-integração)
8. [Troubleshooting](#troubleshooting)
9. [Referência Técnica](#referência-técnica)
10. [Melhorias de Segurança e Resiliência](#melhorias-implementadas)

> 📘 **Novo:** Para detalhes sobre Bulkhead e Redaction de PII, consulte [MELHORIAS_IMPLEMENTADAS.md](MELHORIAS_IMPLEMENTADAS.md)

---

## Visão Geral do Sistema

Sistema completo de análise de veículos com arquitetura de microsserviços, mensageria assíncrona e observabilidade distribuída.

### Componentes Principais

#### 1. Veículos API (Producer)
- **Porta:** 8080
- **Função:** API REST que recebe requisições de análise de veículos
- **Tecnologia:** Spring Boot 3.2, Java 21
- **Banco de Dados:** MongoDB (idempotência)
- **Responsabilidade:** Processa análises e produz logs no Kafka

#### 2. Normalize (Consumer)
- **Porta:** 8081
- **Função:** Consome logs do Kafka e persiste no banco
- **Tecnologia:** Spring Boot 3.2, Java 21
- **Banco de Dados:** PostgreSQL
- **Responsabilidade:** Persiste logs e disponibiliza dashboard

### Fluxo de Dados

```
Cliente (HTTP) → Veículos API → Kafka → Normalize → PostgreSQL → Dashboard
```

### Tecnologias Utilizadas

- ☕ Java 21
- 🍃 Spring Boot 3.2.0
- 📨 Apache Kafka (mensageria)
- 🗄️ MongoDB (idempotência)
- 🐘 PostgreSQL (persistência de logs)
- 🔍 Zipkin (tracing distribuído)
- 🐋 Docker & Docker Compose

---

## Início Rápido

### Pré-requisitos

- Docker e Docker Compose instalados
- Portas livres: 8080, 8081, 9092, 5433, 27017, 9411

### Execução em 3 Passos

#### 1. Execute o Script Automatizado

```bash
cd /home/flavio/Documentos/Veiculos
./run-all.sh
```

O script irá:
- ✅ Verificar pré-requisitos
- ✅ Compilar os projetos (se necessário)
- ✅ Subir toda a infraestrutura
- ✅ Verificar saúde dos serviços
- ✅ Exibir relatório completo

**Aguarde aproximadamente 2 minutos até ver:**
```
✅ SUCESSO! Todos os serviços estão rodando!
```

#### 2. Acesse os Serviços

- **Dashboard Normalize:** http://localhost:8081
- **API Veículos:** http://localhost:8080
- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **Zipkin (Tracing):** http://localhost:9411

#### 3. Teste a Integração

```bash
./test-integracao.sh
```

Este script:
- Verifica saúde dos serviços
- Envia uma requisição de teste
- Confirma que o log foi persistido
- Valida a integração completa

---

## Arquitetura e Integração

### Diagrama de Arquitetura

```
┌────────────────┐
│   Cliente      │
└───────┬────────┘
        │ POST /api/vehicles/analysis
        ▼
┌────────────────┐
│ Veículos API   │ → Normaliza identificador (PLACA→VIN)
│ (porta 8080)   │ → Consulta fornecedores F1, F2, F3
└───────┬────────┘ → Identifica restrições
        │           → Salva idempotência (MongoDB)
        │ Produz mensagem
        ▼
┌────────────────┐
│  Apache Kafka  │ Tópico: vehicle_analysis_log
│ (porta 9092)   │ Partições: 3 | Replicação: 1
└───────┬────────┘
        │ Consome mensagem
        │ Group: analysis-log-persister
        ▼
┌────────────────┐
│   Normalize    │ → Deserializa JSON
│ (porta 8081)   │ → Persiste no PostgreSQL
└───────┬────────┘ → Disponibiliza via API/Dashboard
        │
        ▼
┌────────────────┐
│  PostgreSQL    │ Database: vehicle_analysis
│ (porta 5433)   │ Tabela: vehicle_analysis_log
└────────────────┘
        │
        ▼
┌────────────────┐
│   Dashboard    │ Auto-refresh a cada 3 segundos
│  Web Interativo│ Visualização em tempo real
└────────────────┘
```

### Formato da Mensagem Kafka

**Tópico:** `vehicle_analysis_log`

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "timestamp": "2025-10-26T15:30:00.000Z",
  "idInputType": "PLACA",
  "idInputValue": "ABC1234",
  "vinCanonical": "1HGBH41JXMN109186",
  "supplierCalls": {
    "F1": {"status": "SUCCESS", "latencyMs": 120, "error": null},
    "F2": {"status": "SUCCESS", "latencyMs": 85, "error": null},
    "F3": {"status": "SUCCESS", "latencyMs": 210, "error": null}
  },
  "hasConstraints": false,
  "estimatedCostCents": 350,
  "traceId": "trace-abc123"
}
```

---

## Como Executar

### Opção 1: Script Completo (Recomendado)

```bash
./run-all.sh
```

**Fases de Execução:**

1. **Verificações Iniciais** - Docker, Java, portas
2. **Build dos Projetos** - Compila se necessário
3. **Limpeza de Ambiente** - Remove containers antigos
4. **Infraestrutura** - Sobe Kafka, bancos, Zipkin
5. **Aplicações** - Sobe Veículos e Normalize
6. **Health Checks** - Valida todos os serviços
7. **Relatório Final** - Mostra URLs e status

### Opção 2: Docker Compose Direto

```bash
docker-compose -f docker-compose-integrado.yml up -d
```

### Opção 3: Projetos Individuais

**Veículos (Terminal 1):**
```bash
cd veiculos
./start.sh
```

**Normalize (Terminal 2):**
```bash
cd normalize
./start.sh
```

> ⚠️ **Nota:** Projetos individuais terão infraestruturas separadas (2 Kafkas diferentes)

### Rebuild Forçado

```bash
# Limpar builds
rm -rf veiculos/build/libs/*.jar
rm -rf normalize/build/libs/*.jar

# Executar
./run-all.sh
```

---

## Base de Dados Mock de Veículos

### VehicleDatabase

Componente que simula uma base de **30+ veículos reais** para desenvolvimento e testes.

#### Veículos Incluídos:

| Categoria | Exemplos | Quantidade |
|-----------|----------|------------|
| **Populares** | Gol, Onix, Uno, Corolla | 5 |
| **SUVs** | Tiguan, Compass, RAV4, Equinox | 5 |
| **Picapes** | Hilux, S10, Amarok, Toro, SW4 | 4 |
| **Compactos** | Prisma, Mobi, Up!, Etios | 4 |
| **Sedans Médios** | Cruze, Logan, Jetta | 3 |
| **Importados/Luxo** | Honda Civic, BMW 320i, Mercedes C180, Audi A3 | 5 |
| **Com Restrições** | Fox (RENAJUD), Hilux (RENAJUD), Sandero (RENAJUD) | 3 |
| **Com Recall** | Virtus, Tracker, Argo | 3 |
| **Ambas Restrições** | T-Cross, Yaris | 2 |

#### Como Funciona:

```java
// O sistema aceita qualquer dos 3 identificadores:
// PLACA: "ABC1234"
// RENAVAM: "12345678901"
// VIN: "9BWZZZ377VT004251"

// Todos os 3 apontam para o mesmo veículo na base mock
```

#### Exemplo de Registro:

```json
{
  "placa": "ABC1234",
  "renavam": "12345678901",
  "vin": "9BWZZZ377VT004251",
  "marca": "Volkswagen",
  "modelo": "Gol",
  "ano": 2020,
  "renajud": false,
  "recall": false
}
```

#### Fluxo de Conversão para VIN:

```
1. Cliente envia: PLACA "ABC1234"
   ↓
2. F1/F3 consultam VehicleDatabase
   ↓
3. Retornam: VIN "9BWZZZ377VT004251" + dados do veículo
   ↓
4. Sistema extrai VIN e usa como identificador canônico
   ↓
5. Todas as consultas subsequentes usam o VIN
```

#### Testar Diferentes Cenários:

**Veículo Normal (sem restrições):**
```bash
curl -X POST http://localhost:8080/api/vehicles/analysis \
  -H "Content-Type: application/json" \
  -d '{"identifier": {"type": "PLACA", "value": "ABC1234"}}'
# Resultado: hasConstraints = false, F2 não é chamado
```

**Veículo com RENAJUD:**
```bash
curl -X POST http://localhost:8080/api/vehicles/analysis \
  -H "Content-Type: application/json" \
  -d '{"identifier": {"type": "PLACA", "value": "MNO1234"}}'
# Resultado: hasConstraints = true, F2 é chamado
```

**Veículo com Recall:**
```bash
curl -X POST http://localhost:8080/api/vehicles/analysis \
  -H "Content-Type: application/json" \
  -d '{"identifier": {"type": "PLACA", "value": "VWX3456"}}'
# Resultado: hasConstraints = true (recall), F2 é chamado
```

**Veículo com Ambas Restrições:**
```bash
curl -X POST http://localhost:8080/api/vehicles/analysis \
  -H "Content-Type: application/json" \
  -d '{"identifier": {"type": "PLACA", "value": "EFG5678"}}'
# Resultado: hasConstraints = true (RENAJUD + recall), F2 é chamado
```

---

## Idempotência Automática

### Como Funciona

O sistema **gera automaticamente** uma chave de idempotência (`Idempotency-Key`) para cada requisição, garantindo que requisições duplicadas não sejam reprocessadas.

#### Comportamento:

| Cenário | Ação |
|---------|------|
| Cliente **envia** `Idempotency-Key` | Sistema **usa a chave fornecida** |
| Cliente **não envia** chave | Sistema **gera automaticamente** (hash do body) |

#### Geração Automática:

A chave é gerada usando **SHA-256** do conteúdo da requisição:

```java
// Mesmo body = mesma chave
POST /api/vehicles/analysis
{"identifier": {"type": "PLACA", "value": "ABC1234"}}

// Gera chave: a1b2c3d4e5f6...
```

#### Persistência:

As chaves são armazenadas no **MongoDB** com TTL de 24 horas:

```javascript
{
  "_id": ObjectId("..."),
  "key": "a1b2c3d4e5f6...",
  "response": { /* resposta completa */ },
  "createdAt": ISODate("2025-10-26T15:30:00Z"),
  "expiresAt": ISODate("2025-10-27T15:30:00Z")
}
```

#### Fluxo Completo:

```
1. Requisição chega sem Idempotency-Key
   ↓
2. IdempotencyFilter intercepta
   ↓
3. Lê body da requisição
   ↓
4. Gera hash SHA-256 do body
   ↓
5. Adiciona header Idempotency-Key: <hash>
   ↓
6. VehicleAnalysisService verifica MongoDB
   - Chave existe? → Retorna resposta em cache
   - Não existe? → Processa normalmente
   ↓
7. Salva resposta no MongoDB com a chave
```

#### Teste de Idempotência:

**Primeira Requisição (processa):**
```bash
curl -X POST http://localhost:8080/api/vehicles/analysis \
  -H "Content-Type: application/json" \
  -d '{"identifier": {"type": "PLACA", "value": "ABC1234"}}'
# Tempo: ~500ms (consulta F1, F3)
```

**Segunda Requisição Idêntica (retorna cache):**
```bash
curl -X POST http://localhost:8080/api/vehicles/analysis \
  -H "Content-Type: application/json" \
  -d '{"identifier": {"type": "PLACA", "value": "ABC1234"}}'
# Tempo: ~10ms (cache MongoDB)
```

**Requisição com Chave Manual:**
```bash
curl -X POST http://localhost:8080/api/vehicles/analysis \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: minha-chave-123" \
  -d '{"identifier": {"type": "PLACA", "value": "ABC1234"}}'
# Usa "minha-chave-123" ao invés de gerar automaticamente
```

#### Verificar Chaves no MongoDB:

```bash
# Conectar ao MongoDB
docker exec -it integrado-mongo mongosh -u user -p pass

# Dentro do mongosh:
use idempotency_store
db.idempotency_keys.find().pretty()

# Ver quantas chaves existem
db.idempotency_keys.countDocuments()

# Ver chaves que expiram em breve
db.idempotency_keys.find({
  "expiresAt": {$lt: new Date(Date.now() + 3600000)}
})
```

#### Benefícios:

✅ **Transparente:** Cliente não precisa se preocupar com chaves  
✅ **Determinístico:** Mesmo body = mesma chave  
✅ **Performance:** Respostas em cache são instantâneas  
✅ **Compatível:** Aceita chaves manuais se fornecidas  
✅ **Automático:** Zero configuração necessária  

---

## Persistência: MongoDB vs PostgreSQL

### MongoDB (NoSQL) - Porta 27017

**Função:** Cache de idempotência

**O que armazena:**
- ✅ Chaves de idempotência (Idempotency-Key)
- ✅ Respostas completas em cache
- ✅ TTL de 24 horas (expira automaticamente)

**Quando usa:**
- Verificar se requisição já foi processada
- Retornar resposta em cache
- Evitar reprocessamento

**Collection:** `idempotency_keys`

**Estrutura:**
```javascript
{
  "_id": ObjectId("..."),
  "key": "a1b2c3d4e5f6...",
  "response": {
    "idInputType": "PLACA",
    "idInputValue": "ABC1234",
    "vinCanonical": "9BWZZZ377VT004251",
    "supplierCalls": {...},
    "hasConstraints": false,
    "estimatedCostCents": 350
  },
  "createdAt": ISODate("2025-10-26T15:30:00Z"),
  "expiresAt": ISODate("2025-10-27T15:30:00Z")
}
```

---

### PostgreSQL (SQL) - Porta 5433

**Função:** Auditoria e analytics

**O que armazena:**
- ✅ **TODOS** os logs de análise (histórico completo)
- ✅ Dados consolidados de F1, F2, F3
- ✅ Métricas (latência, custo)
- ✅ TraceID para correlação

**Quando usa:**
- Armazenar histórico permanente
- Analytics e relatórios
- Dashboard web
- Auditoria

**Tabela:** `vehicle_analysis_log`

**Estrutura:**
```sql
CREATE TABLE vehicle_analysis_log (
    id UUID PRIMARY KEY,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    id_input_type VARCHAR(20) NOT NULL,
    id_input_value VARCHAR(50) NOT NULL,
    vin_canonical VARCHAR(17) NOT NULL,
    supplier_calls JSONB NOT NULL,
    has_constraints BOOLEAN NOT NULL,
    estimated_cost_cents INTEGER NOT NULL,
    trace_id VARCHAR(100) NOT NULL
);
```

---

### Comparação:

| Aspecto | MongoDB | PostgreSQL |
|---------|---------|------------|
| **Função** | Cache temporário | Persistência permanente |
| **Dados** | Chaves + respostas | Logs de análise |
| **Retenção** | 24 horas (TTL) | Indefinido |
| **Performance** | Ultra-rápido (cache) | Rápido (indexado) |
| **Uso** | Idempotência | Analytics/Auditoria |
| **Tamanho** | Pequeno (cache) | Cresce com o tempo |

---

## Visualização e Monitoramento

### 1. Dashboard Web Interativo (Principal)

**URL:** http://localhost:8081

**Recursos:**
- 📊 Estatísticas em tempo real (total de logs, último log)
- 📋 Tabela com 10 logs mais recentes
- 🔄 Auto-refresh a cada 3 segundos
- 🎨 Interface moderna com cores e badges
- 🎛️ Controles para pausar/retomar atualização

**O que você vê:**

| Timestamp | Tipo/Valor | VIN | Restrições | Custo | Trace ID |
|-----------|------------|-----|------------|-------|----------|
| 26/10/2025 15:30:45 | PLACA ABC1234 | 1HG...186 | ✅ Não | R$ 3,50 | trace-... |
| 26/10/2025 15:30:46 | RENAVAM 123... | 2HG...187 | ⚠️ Sim | R$ 4,50 | trace-... |

### 2. API REST

**Endpoints disponíveis:**

```bash
# Listar logs (paginado)
GET http://localhost:8081/api/logs?page=0&size=20

# Estatísticas
GET http://localhost:8081/api/logs/stats

# Último log
GET http://localhost:8081/api/logs/latest

# Log específico
GET http://localhost:8081/api/logs/{uuid}
```

**Exemplo:**
```bash
curl http://localhost:8081/api/logs/stats
# Retorna: {"totalLogs": 42, "timestamp": "2025-10-26T10:30:00Z"}
```

### 3. Zipkin (Tracing Distribuído)

**URL:** http://localhost:9411

**Como usar:**
1. Clique em "Run Query"
2. Filtre por serviço: `normalize-log-consumer` ou `veiculo-analysis-api`
3. Cole um TraceID do dashboard para detalhes
4. Analise latências e timeline de execução

**Útil para:**
- 🐛 Debug de problemas
- ⏱️ Análise de performance
- 🔗 Correlação entre serviços

### 4. Health Checks

```bash
# Veículos
curl http://localhost:8080/actuator/health

# Normalize
curl http://localhost:8081/actuator/health

# Resposta esperada: {"status":"UP"}
```

---

## Como Ver Logs

### Ver Logs em Tempo Real

**Todos os serviços:**
```bash
docker-compose -f docker-compose-integrado.yml logs -f
```

**Veículos API:**
```bash
docker-compose -f docker-compose-integrado.yml logs -f veiculos-app
```

**Normalize Consumer:**
```bash
docker-compose -f docker-compose-integrado.yml logs -f normalize-app
```

**Kafka:**
```bash
docker-compose -f docker-compose-integrado.yml logs -f kafka
```

**Pressione Ctrl+C para sair**

### Ver Últimas N Linhas

```bash
# Últimas 50 linhas de todos
docker-compose -f docker-compose-integrado.yml logs --tail=50

# Últimas 100 do Veículos
docker-compose -f docker-compose-integrado.yml logs --tail=100 veiculos-app
```

### Filtrar Logs

```bash
# Buscar erros
docker-compose -f docker-compose-integrado.yml logs | grep -i error

# Ver logs de Kafka no Veículos
docker-compose -f docker-compose-integrado.yml logs veiculos-app | grep -i kafka

# Ver logs de persistência no Normalize
docker-compose -f docker-compose-integrado.yml logs normalize-app | grep "persistido"
```

### Salvar Logs em Arquivo

```bash
docker-compose -f docker-compose-integrado.yml logs > logs-completos.txt
```

### Ver Status dos Containers

```bash
docker-compose -f docker-compose-integrado.yml ps
```

ou

```bash
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
```

---

## Teste da Integração

### Teste Automático

```bash
./test-integracao.sh
```

**O que o script faz:**
1. ✅ Verifica saúde dos serviços (8080, 8081)
2. ✅ Conta logs atuais no Normalize
3. ✅ Envia POST para Veículos API
4. ✅ Aguarda 10 segundos
5. ✅ Verifica se log foi persistido
6. ✅ Exibe resultado

**Resultado esperado:**
```
✅ SUCESSO! Log foi persistido no Normalize!
🎉 INTEGRAÇÃO FUNCIONANDO!
```

### Teste Manual

#### 1. Enviar Requisição

```bash
curl -X POST http://localhost:8080/api/vehicles/analysis \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": {
      "type": "PLACA",
      "value": "ABC1234"
    }
  }'
```

#### 2. Verificar no Dashboard

Abra http://localhost:8081 e aguarde até 3 segundos. O log aparecerá automaticamente.

#### 3. Consultar via API

```bash
# Ver último log
curl http://localhost:8081/api/logs/latest | jq

# Ver todos
curl http://localhost:8081/api/logs | jq
```

#### 4. Verificar no Zipkin

1. Acesse http://localhost:9411
2. Clique em "Run Query"
3. Veja o trace da análise
4. Copie o TraceID do dashboard e busque no Zipkin

---

## Troubleshooting

### Serviço Não Sobe

**Verificar:**
```bash
docker-compose -f docker-compose-integrado.yml ps
docker-compose -f docker-compose-integrado.yml logs <serviço>
```

**Solução:**
```bash
# Restartar serviço específico
docker-compose -f docker-compose-integrado.yml restart veiculos-app

# Recomeçar do zero
docker-compose -f docker-compose-integrado.yml down -v
./run-all.sh
```

### Portas Ocupadas

**Verificar quem está usando:**
```bash
lsof -i :8080
lsof -i :8081
```

**Solução:**
```bash
# Matar processo
kill -9 <PID>

# Ou parar Docker antigo
docker-compose -f docker-compose-integrado.yml down
```

### Mensagens Não Chegam no Normalize

**Diagnóstico:**

1. **Veículos está produzindo?**
```bash
docker-compose -f docker-compose-integrado.yml logs veiculos-app | grep "Kafka"
```

2. **Há mensagens no Kafka?**
```bash
docker exec integrado-kafka kafka-console-consumer \
  --topic vehicle_analysis_log \
  --bootstrap-server localhost:9092 \
  --from-beginning \
  --max-messages 1
```

3. **Normalize está consumindo?**
```bash
docker-compose -f docker-compose-integrado.yml logs normalize-app | grep "Recebido"
```

4. **Verificar consumer group:**
```bash
docker exec integrado-kafka kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --describe \
  --group analysis-log-persister
```

### Build Falha

```bash
# Ver log completo
cat /tmp/veiculos-build.log

# Rebuild manual com stacktrace
cd veiculos
./gradlew clean build --stacktrace

# Verificar dependências
./gradlew dependencies
```

### Dashboard Não Abre

```bash
# Verificar se container está UP
docker ps | grep normalize

# Verificar porta
curl http://localhost:8081/actuator/health

# Ver logs de erro
docker-compose -f docker-compose-integrado.yml logs normalize-app | tail -50
```

### Limpar Tudo e Recomeçar

```bash
# Parar tudo
docker-compose -f docker-compose-integrado.yml down -v

# Remover containers órfãos
docker ps -a | grep -E "veiculos|normalize|integrado" | awk '{print $1}' | xargs -r docker rm -f

# Limpar redes e volumes
docker network prune -f
docker volume prune -f

# Executar novamente
./run-all.sh
```

---

## Referência Técnica

### Serviços e Portas

| Serviço | Porta | URL | Descrição |
|---------|-------|-----|-----------|
| Veículos API | 8080 | http://localhost:8080 | API REST principal |
| Swagger UI | 8080 | http://localhost:8080/swagger-ui.html | Documentação API |
| Normalize Dashboard | 8081 | http://localhost:8081 | Dashboard web |
| Normalize API | 8081 | http://localhost:8081/api/logs | API de consulta |
| Kafka | 9092 | localhost:9092 | Broker Kafka |
| ZooKeeper | 2181 | localhost:2181 | Coordenação Kafka |
| MongoDB | 27017 | localhost:27017 | Idempotência |
| PostgreSQL | 5433 | localhost:5433 | Logs persistidos |
| Zipkin | 9411 | http://localhost:9411 | Tracing UI |
| WireMock | 9090 | http://localhost:9090 | Mocks fornecedores |

### Estrutura de Diretórios

```
/home/flavio/Documentos/Veiculos/
├── veiculos/                    # Microsserviço Producer
│   ├── src/                     # Código fonte
│   ├── build.gradle             # Dependências
│   ├── docker-compose.yml       # Standalone
│   └── start.sh                 # Script inicialização
│
├── normalize/                   # Microsserviço Consumer
│   ├── src/                     # Código fonte
│   ├── build.gradle             # Dependências
│   ├── docker-compose.yml       # Standalone
│   ├── start.sh                 # Script inicialização
│   └── test-messages.sh         # Mensagens de teste
│
├── docker-compose-integrado.yml # Orquestração integrada
├── run-all.sh                   # Script completo (PRINCIPAL)
└── test-integracao.sh           # Teste integração E2E
```

### Comandos Docker Úteis

```bash
# Subir sistema
./run-all.sh

# Ver status
docker-compose -f docker-compose-integrado.yml ps

# Ver logs
docker-compose -f docker-compose-integrado.yml logs -f

# Parar tudo
docker-compose -f docker-compose-integrado.yml down

# Parar e remover volumes
docker-compose -f docker-compose-integrado.yml down -v

# Restart específico
docker-compose -f docker-compose-integrado.yml restart veiculos-app

# Ver uso de recursos
docker stats
```

### Exemplos de Requisições

**Análise por PLACA:**
```bash
curl -X POST http://localhost:8080/api/vehicles/analysis \
  -H "Content-Type: application/json" \
  -d '{"identifier": {"type": "PLACA", "value": "ABC1234"}}'
```

**Análise por RENAVAM:**
```bash
curl -X POST http://localhost:8080/api/vehicles/analysis \
  -H "Content-Type: application/json" \
  -d '{"identifier": {"type": "RENAVAM", "value": "12345678901"}}'
```

**Análise por VIN:**
```bash
curl -X POST http://localhost:8080/api/vehicles/analysis \
  -H "Content-Type: application/json" \
  -d '{"identifier": {"type": "VIN", "value": "1HGBH41JXMN109186"}}'
```

### Consultas PostgreSQL

```bash
# Conectar ao banco
docker exec -it integrado-postgres psql -U user -d vehicle_analysis

# Dentro do psql:
SELECT COUNT(*) FROM vehicle_analysis_log;

SELECT * FROM vehicle_analysis_log 
ORDER BY timestamp DESC 
LIMIT 10;

SELECT id_input_type, COUNT(*) 
FROM vehicle_analysis_log 
GROUP BY id_input_type;

\q  # Sair
```

### Consultas Kafka

```bash
# Listar tópicos
docker exec integrado-kafka kafka-topics --list --bootstrap-server localhost:9092

# Ver mensagens
docker exec integrado-kafka kafka-console-consumer \
  --topic vehicle_analysis_log \
  --bootstrap-server localhost:9092 \
  --from-beginning \
  --max-messages 5

# Ver consumer groups
docker exec integrado-kafka kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --list
```

---

## Resumo dos Comandos Principais

```bash
# Executar sistema completo
./run-all.sh

# Testar integração
./test-integracao.sh

# Ver logs
docker-compose -f docker-compose-integrado.yml logs -f

# Ver status
docker-compose -f docker-compose-integrado.yml ps

# Parar tudo
docker-compose -f docker-compose-integrado.yml down

# Acessar dashboards
# - Normalize: http://localhost:8081
# - Veículos Swagger: http://localhost:8080/swagger-ui.html
# - Zipkin: http://localhost:9411
```

---

**Sistema pronto para uso! 🚀**

Versão: 1.0 | Data: Outubro 2025

