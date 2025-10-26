# 🚗 Sistema de Análise de Veículos

Sistema completo de análise de veículos com arquitetura de microsserviços.

## 📚 Documentação

**Toda a documentação está consolidada em um único arquivo:**

### [📘 GUIA_COMPLETO.md](GUIA_COMPLETO.md)

Este guia contém tudo o que você precisa:
- ✅ Visão geral do sistema
- ✅ Início rápido (3 passos)
- ✅ Arquitetura e integração
- ✅ Como executar
- ✅ Visualização e monitoramento
- ✅ Como ver logs
- ✅ Teste da integração
- ✅ Troubleshooting completo
- ✅ Referência técnica

---

## ⚡ Início Ultra-Rápido

```bash
cd /home/flavio/Documentos/Veiculos
./run-all.sh
```

Aguarde 2 minutos e acesse:
- **Dashboard:** http://localhost:8081
- **API Veículos:** http://localhost:8080
- **Zipkin:** http://localhost:9411

---

## 🧪 Testar Integração

```bash
./test-integracao.sh
```

---

## 📋 Ver Logs

```bash
docker-compose -f docker-compose-integrado.yml logs -f
```

Pressione Ctrl+C para sair.

---

## 🛑 Parar Sistema

```bash
docker-compose -f docker-compose-integrado.yml down
```

---

**Para informações detalhadas, consulte o [GUIA_COMPLETO.md](GUIA_COMPLETO.md)**

---

Versão: 1.0 | Data: Outubro 2025

### Dois Microsserviços Independentes:

#### 1. **Veículos API** (Producer)
- **Porta:** 8080
- **Função:** API REST que recebe requisições de análise de veículos
- **Tecnologia:** Spring Boot 3.2, Java 21
- **Banco:** MongoDB (idempotência)
- **Produz:** Logs de análise no Kafka

#### 2. **Normalize** (Consumer)
- **Porta:** 8081
- **Função:** Consome logs do Kafka e persiste no banco
- **Tecnologia:** Spring Boot 3.2, Java 21
- **Banco:** PostgreSQL
- **Consome:** Logs de análise do Kafka
- **Interface:** Dashboard Web interativo

### Integração:
```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│   Cliente    │────▶│  Veículos    │────▶│    Kafka     │
│ (HTTP POST)  │     │     API      │     │              │
└──────────────┘     └──────────────┘     └──────┬───────┘
                                                  │
                                                  ▼
                                          ┌──────────────┐
                                          │  Normalize   │
                                          │   Consumer   │
                                          └──────┬───────┘
                                                  │
                                                  ▼
                                          ┌──────────────┐
                                          │ PostgreSQL   │
                                          │  Dashboard   │
                                          └──────────────┘
```

---

## 🚀 Início Rápido

### Opção 1: Script Completo com Verificação de Erros (RECOMENDADO)

Execute tudo com verificação completa de erros:

```bash
./run-all.sh
```

Este script:
- ✅ Verifica todos os pré-requisitos (Docker, Java, etc.)
- ✅ Compila ambos os projetos (se necessário)
- ✅ Sobe toda a infraestrutura
- ✅ Verifica saúde de todos os serviços
- ✅ Mostra logs de erro se algo falhar
- ✅ Exibe relatório completo ao final

### Opção 2: Script Simples (Alternativa)

```bash
./start-integrado.sh
```

Aguarde ~1 minuto e acesse:
- 🎨 **Dashboard Normalize:** http://localhost:8081
- 🚗 **API Veículos:** http://localhost:8080
- 🔍 **Zipkin:** http://localhost:9411

### Opção 3: Projetos Separados

**Terminal 1 - Veículos:**
```bash
cd veiculos
./start.sh
```

**Terminal 2 - Normalize:**
```bash
cd normalize
./start.sh
```

> ⚠️ Neste modo você terá 2 Kafkas separados. Use Opção 1 para integração real.

---

## 🧪 Testar a Integração

### Teste Automático

```bash
./test-integracao.sh
```

### Teste Manual

**1. Enviar análise de veículo:**

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

**2. Ver resultado no Dashboard:**

Abra http://localhost:8081 e veja o log aparecer em até 3 segundos!

---

## 📁 Estrutura do Projeto

```
/home/flavio/Documentos/Veiculos/
├── veiculos/                    # Microsserviço Producer
│   ├── src/                     # Código fonte
│   ├── build.gradle             # Dependências
│   ├── docker-compose.yml       # Standalone
│   └── start.sh                 # Script de inicialização
│
├── normalize/                   # Microsserviço Consumer
│   ├── src/                     # Código fonte
│   ├── build.gradle             # Dependências
│   ├── docker-compose.yml       # Standalone
│   ├── start.sh                 # Script de inicialização
│   └── test-messages.sh         # Mensagens de teste
│
├── docker-compose-integrado.yml # Orquestração integrada
├── start-integrado.sh           # Iniciar sistema completo
├── test-integracao.sh           # Testar integração
│
└── Documentação:
    ├── INTEGRACAO.md            # Documentação completa da integração
    ├── veiculos/README.md       # Docs do projeto Veículos
    └── normalize/
        ├── README.md            # Docs do projeto Normalize
        ├── INICIO_RAPIDO.md     # Guia rápido
        ├── FORMAS_DE_VISUALIZACAO.md
        ├── GUIA_VISUALIZACAO.md
        └── INDICE.md
```

---

## 🔧 Tecnologias Utilizadas

### Ambos os Projetos:
- ☕ **Java 21**
- 🍃 **Spring Boot 3.2.0**
- 📨 **Apache Kafka** (mensageria)
- 🔍 **Zipkin** (tracing distribuído)
- 🐋 **Docker & Docker Compose**
- 🔨 **Gradle** (build tool)
- 📦 **Lombok**

### Veículos (Producer):
- 🗄️ **MongoDB** (idempotência)
- 🌐 **Spring WebFlux** (chamadas assíncronas)
- 🔌 **SOAP & REST Clients**
- 🧪 **WireMock** (mocks)

### Normalize (Consumer):
- 🐘 **PostgreSQL** (persistência)
- 🎨 **Thymeleaf** (dashboard)
- 📊 **Spring Data JPA**
- 🌐 **Spring Web**

---

## 📊 Serviços e Portas

| Serviço | Porta | Acesso |
|---------|-------|--------|
| **Veículos API** | 8080 | http://localhost:8080 |
| **Normalize Dashboard** | 8081 | http://localhost:8081 |
| **Kafka** | 9092 | localhost:9092 |
| **ZooKeeper** | 2181 | localhost:2181 |
| **MongoDB** | 27017 | localhost:27017 |
| **PostgreSQL** | 5433 | localhost:5433 |
| **Zipkin** | 9411 | http://localhost:9411 |
| **WireMock** | 9090 | http://localhost:9090 |

---

## 🎨 Visualização

### Dashboard Web (Normalize)

**URL:** http://localhost:8081

Mostra em tempo real:
- ✅ Total de logs processados
- ✅ Último log recebido
- ✅ Tabela com 10 logs mais recentes
- ✅ Auto-refresh a cada 3 segundos
- ✅ Interface moderna e interativa

### API REST (Normalize)

```bash
# Listar logs
curl http://localhost:8081/api/logs | jq

# Estatísticas
curl http://localhost:8081/api/logs/stats

# Último log
curl http://localhost:8081/api/logs/latest | jq
```

### Zipkin (Tracing)

**URL:** http://localhost:9411

Mostra traces distribuídos entre Veículos e Normalize.

---

## 🔗 Como Funciona a Integração

### 1️⃣ Cliente faz requisição HTTP

```bash
POST http://localhost:8080/api/vehicles/analysis
{
  "identifier": {
    "type": "PLACA",
    "value": "ABC1234"
  }
}
```

### 2️⃣ Veículos API processa

- Normaliza identificador (PLACA → VIN)
- Consulta fornecedores F1, F2, F3
- Agrega resultados
- Retorna resposta ao cliente
- **Produz log no Kafka**

### 3️⃣ Mensagem vai para o Kafka

**Tópico:** `vehicle_analysis_log`

```json
{
  "id": "uuid",
  "timestamp": "2025-10-26T15:30:00Z",
  "idInputType": "PLACA",
  "idInputValue": "ABC1234",
  "vinCanonical": "1HGBH41JXMN109186",
  "supplierCalls": {...},
  "hasConstraints": false,
  "estimatedCostCents": 350,
  "traceId": "trace-123"
}
```

### 4️⃣ Normalize consome do Kafka

- Deserializa mensagem JSON
- Converte para entidade JPA
- Persiste no PostgreSQL
- Log disponível imediatamente

### 5️⃣ Visualização

- Dashboard atualiza automaticamente (3s)
- API REST retorna dados
- Zipkin mostra trace completo

---

## 🐛 Troubleshooting

### Ver logs dos serviços

```bash
# Veículos
docker-compose -f docker-compose-integrado.yml logs -f veiculos-app

# Normalize
docker-compose -f docker-compose-integrado.yml logs -f normalize-app

# Kafka
docker-compose -f docker-compose-integrado.yml logs -f kafka
```

### Verificar saúde

```bash
# Veículos
curl http://localhost:8080/actuator/health

# Normalize
curl http://localhost:8081/actuator/health
```

### Verificar Kafka

```bash
# Listar tópicos
docker exec shared-kafka kafka-topics --list --bootstrap-server localhost:9092

# Ver mensagens
docker exec shared-kafka kafka-console-consumer \
  --topic vehicle_analysis_log \
  --bootstrap-server localhost:9092 \
  --from-beginning \
  --max-messages 5
```

### Reiniciar tudo

```bash
docker-compose -f docker-compose-integrado.yml down
./start-integrado.sh
```

---

## 📚 Documentação Completa

### Documentação Geral:
- **[RUN_ALL_DOC.md](RUN_ALL_DOC.md)** - Documentação completa do script run-all.sh
- **[INTEGRACAO.md](INTEGRACAO.md)** - Documentação completa da integração entre os projetos
- **[CONFORMIDADE_DESAFIO.md](CONFORMIDADE_DESAFIO.md)** - Validação contra requisitos técnicos
- **[CORRECOES.md](CORRECOES.md)** - Lista de correções aplicadas

### Projeto Veículos:
- **[veiculos/README.md](veiculos/README.md)** - Documentação do projeto Veículos

### Projeto Normalize:
- **[normalize/INDICE.md](normalize/INDICE.md)** - Índice navegável
- **[normalize/INICIO_RAPIDO.md](normalize/INICIO_RAPIDO.md)** - Guia de início rápido
- **[normalize/FORMAS_DE_VISUALIZACAO.md](normalize/FORMAS_DE_VISUALIZACAO.md)** - Todas as formas de visualização
- **[normalize/GUIA_VISUALIZACAO.md](normalize/GUIA_VISUALIZACAO.md)** - Guia detalhado de visualização

---

## 🎯 Casos de Uso

### 1. Desenvolvimento Local

```bash
./start-integrado.sh
# Desenvolva e teste localmente
```

### 2. Demonstração

```bash
./start-integrado.sh
# Aguarde 1 minuto
# Abra http://localhost:8081 em projetor
./test-integracao.sh
# Mostre dashboard atualizando
```

### 3. Debugging

```bash
# Terminal 1: Logs do Veículos
docker-compose -f docker-compose-integrado.yml logs -f veiculos-app

# Terminal 2: Logs do Normalize
docker-compose -f docker-compose-integrado.yml logs -f normalize-app

# Terminal 3: Dashboard
open http://localhost:8081

# Terminal 4: Zipkin
open http://localhost:9411
```

### 4. Testes de Carga

```bash
# Use o script ou faça múltiplas requisições
for i in {1..100}; do
  curl -X POST http://localhost:8080/api/vehicles/analysis \
    -H "Content-Type: application/json" \
    -d "{\"identifier\":{\"type\":\"PLACA\",\"value\":\"ABC${i}\"}}"
done

# Veja o dashboard processando em tempo real!
```

---

## ✅ Checklist de Verificação

Para confirmar que tudo está funcionando:

- [ ] `./start-integrado.sh` executou sem erros
- [ ] `docker-compose -f docker-compose-integrado.yml ps` mostra todos UP
- [ ] http://localhost:8080/actuator/health retorna UP
- [ ] http://localhost:8081/actuator/health retorna UP
- [ ] http://localhost:8081 mostra dashboard
- [ ] `./test-integracao.sh` passa com sucesso
- [ ] Dashboard atualiza após POST no Veículos
- [ ] Zipkin mostra traces de ambos os serviços

---

## 🤝 Contribuindo

### Adicionar nova funcionalidade no Veículos:

1. Implemente no projeto `veiculos/`
2. Garanta que o log é produzido no Kafka
3. Rebuild: `cd veiculos && ./gradlew clean bootJar`
4. Teste: `./test-integracao.sh`

### Adicionar nova visualização no Normalize:

1. Implemente no projeto `normalize/`
2. Adicione endpoint ou página
3. Rebuild: `cd normalize && ./gradlew clean bootJar`
4. Teste no dashboard

---

## 📝 Notas Importantes

- ✅ **Projetos Independentes:** Cada um pode ser desenvolvido separadamente
- ✅ **Integração via Kafka:** Comunicação assíncrona e desacoplada
- ✅ **Observabilidade:** Zipkin rastreia toda a jornada
- ✅ **Escalável:** Cada projeto pode ter múltiplas instâncias
- ✅ **Testável:** Scripts automatizados facilitam testes

---

## 🎉 Pronto para Usar!

O sistema está **totalmente integrado** e pronto para:

- ✅ Receber análises de veículos
- ✅ Processar em tempo real
- ✅ Registrar logs no Kafka
- ✅ Persistir no PostgreSQL
- ✅ Visualizar no Dashboard
- ✅ Rastrear no Zipkin

**Bom desenvolvimento!** 🚀

---

## 📞 Suporte

Se algo não funcionar:

1. Veja os logs: `docker-compose -f docker-compose-integrado.yml logs`
2. Consulte: [INTEGRACAO.md](INTEGRACAO.md) seção Troubleshooting
3. Recrie o ambiente: `docker-compose -f docker-compose-integrado.yml down -v && ./start-integrado.sh`

---

**Desenvolvido com ❤️ usando Spring Boot, Kafka e Docker**

