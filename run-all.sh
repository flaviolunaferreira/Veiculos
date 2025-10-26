#!/bin/bash

# ===================================================================
# Script de Inicialização Completa - Sistema Integrado
# Projetos: Veículos API + Normalize Consumer
# ===================================================================

set -e  # Para em caso de erro

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color
BOLD='\033[1m'

# Função para imprimir mensagens
print_header() {
    echo ""
    echo -e "${BLUE}${BOLD}=========================================="
    echo -e "$1"
    echo -e "==========================================${NC}"
    echo ""
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

# Variável para rastrear erros
ERRORS=0
WARNINGS=0

# ===================================================================
# FASE 1: Verificações Iniciais
# ===================================================================

print_header "🔍 FASE 1: Verificações Iniciais"

# Verificar se Docker está instalado
print_info "Verificando Docker..."
if ! command -v docker &> /dev/null; then
    print_error "Docker não está instalado!"
    echo "Instale o Docker: https://docs.docker.com/get-docker/"
    exit 1
fi
print_success "Docker instalado: $(docker --version)"

# Verificar se Docker Compose está instalado
print_info "Verificando Docker Compose..."
if ! command -v docker-compose &> /dev/null; then
    print_error "Docker Compose não está instalado!"
    echo "Instale o Docker Compose: https://docs.docker.com/compose/install/"
    exit 1
fi
print_success "Docker Compose instalado: $(docker-compose --version)"

# Verificar se Docker está rodando
print_info "Verificando se Docker está rodando..."
if ! docker ps &> /dev/null; then
    print_error "Docker não está rodando!"
    echo "Inicie o Docker e tente novamente."
    exit 1
fi
print_success "Docker está rodando"

# Verificar se Java está instalado
print_info "Verificando Java..."
if ! command -v java &> /dev/null; then
    print_warning "Java não encontrado (necessário apenas para build local)"
    WARNINGS=$((WARNINGS + 1))
else
    JAVA_VERSION=$(java -version 2>&1 | head -n 1)
    print_success "Java instalado: $JAVA_VERSION"
fi

# Verificar se Gradle Wrapper existe
print_info "Verificando Gradle Wrappers..."
if [ ! -f "./veiculos/gradlew" ]; then
    print_error "Gradle wrapper não encontrado em ./veiculos/"
    ERRORS=$((ERRORS + 1))
else
    print_success "Gradle wrapper veículos: OK"
fi

if [ ! -f "./normalize/gradlew" ]; then
    print_error "Gradle wrapper não encontrado em ./normalize/"
    ERRORS=$((ERRORS + 1))
else
    print_success "Gradle wrapper normalize: OK"
fi

# Verificar se docker-compose-integrado.yml existe
print_info "Verificando docker-compose-integrado.yml..."
if [ ! -f "./docker-compose-integrado.yml" ]; then
    print_error "Arquivo docker-compose-integrado.yml não encontrado!"
    ERRORS=$((ERRORS + 1))
else
    print_success "docker-compose-integrado.yml: OK"
fi

# Se houver erros críticos, parar aqui
if [ $ERRORS -gt 0 ]; then
    print_error "Encontrados $ERRORS erros críticos. Corrija-os antes de continuar."
    exit 1
fi

if [ $WARNINGS -gt 0 ]; then
    print_warning "Encontrados $WARNINGS avisos (não críticos)"
fi

# ===================================================================
# FASE 2: Build dos Projetos
# ===================================================================

print_header "🔨 FASE 2: Build dos Projetos"

# Build do Veículos
print_info "Compilando projeto Veículos..."
cd veiculos

if [ ! -f "./build/libs/veiculos-0.0.1-SNAPSHOT.jar" ]; then
    print_info "JAR não encontrado. Executando build completo..."
    if ! ./gradlew clean bootJar 2>&1 | tee /tmp/veiculos-build.log; then
        print_error "Falha no build do projeto Veículos!"
        echo ""
        echo "===== ÚLTIMAS 30 LINHAS DO LOG DE ERRO ====="
        tail -30 /tmp/veiculos-build.log
        echo "============================================="
        echo ""
        echo "Log completo salvo em: /tmp/veiculos-build.log"
        ERRORS=$((ERRORS + 1))
        cd ..
        exit 1
    fi
else
    print_info "JAR existente encontrado. Pulando build..."
    print_warning "Use './gradlew clean bootJar' manualmente para rebuild completo"
fi

cd ..
print_success "Build do Veículos: OK"

# Build do Normalize
print_info "Compilando projeto Normalize..."
cd normalize

if [ ! -f "./build/libs/normalize-0.0.1-SNAPSHOT.jar" ]; then
    print_info "JAR não encontrado. Executando build completo..."
    if ! ./gradlew clean bootJar 2>&1 | tee /tmp/normalize-build.log; then
        print_error "Falha no build do projeto Normalize!"
        echo ""
        echo "===== ÚLTIMAS 30 LINHAS DO LOG DE ERRO ====="
        tail -30 /tmp/normalize-build.log
        echo "============================================="
        echo ""
        echo "Log completo salvo em: /tmp/normalize-build.log"
        ERRORS=$((ERRORS + 1))
        cd ..
        exit 1
    fi
else
    print_info "JAR existente encontrado. Pulando build..."
    print_warning "Use './gradlew clean bootJar' manualmente para rebuild completo"
fi

cd ..
print_success "Build do Normalize: OK"

# ===================================================================
# FASE 3: Parar Containers Antigos
# ===================================================================

print_header "🛑 FASE 3: Limpeza de Ambiente"

print_info "Parando containers antigos (se existirem)..."
if docker-compose -f docker-compose-integrado.yml ps -q 2>/dev/null | grep -q .; then
    print_info "Containers encontrados. Parando..."
    if ! docker-compose -f docker-compose-integrado.yml down -v 2>&1 | tee /tmp/docker-down.log; then
        print_warning "Erro ao parar containers (não crítico)"
        tail -10 /tmp/docker-down.log
    else
        print_success "Containers antigos parados e volumes removidos"
    fi
else
    print_info "Nenhum container rodando"
fi

# Remover containers órfãos específicos
print_info "Removendo containers órfãos..."
docker ps -a | grep -E "normalize-postgres|veiculos-api|normalize-consumer|shared-|mongo-idempotency|supplier-mocks" | awk '{print $1}' | xargs -r docker rm -f 2>/dev/null || true

# Limpar redes órfãs
print_info "Limpando redes Docker órfãs..."
docker network prune -f > /dev/null 2>&1

# Tentar liberar portas se estiverem ocupadas
print_info "Verificando se portas estão livres..."
if command -v lsof &> /dev/null; then
    PIDS=$(lsof -ti:8080,8081,9092,5433,27017,9411 2>/dev/null || true)
    if [ -n "$PIDS" ]; then
        print_warning "Encontrados processos usando portas necessárias. Tentando liberar..."
        echo "$PIDS" | xargs -r kill -9 2>/dev/null || true
        sleep 2
    fi
fi

print_success "Limpeza completa realizada"

# Verificar portas ocupadas
print_info "Verificando portas necessárias..."
PORTS_OK=true

check_port() {
    PORT=$1
    SERVICE=$2
    if lsof -Pi :$PORT -sTCP:LISTEN -t >/dev/null 2>&1; then
        print_warning "Porta $PORT ($SERVICE) está ocupada!"
        echo "  Processo: $(lsof -Pi :$PORT -sTCP:LISTEN | tail -n 1)"
        WARNINGS=$((WARNINGS + 1))
        PORTS_OK=false
    fi
}

check_port 8080 "Veículos API"
check_port 8081 "Normalize Dashboard"
check_port 9092 "Kafka"
check_port 5433 "PostgreSQL"
check_port 27017 "MongoDB"
check_port 9411 "Zipkin"

if [ "$PORTS_OK" = false ]; then
    print_warning "Algumas portas estão ocupadas. Tentando continuar..."
    echo ""
    read -p "Deseja continuar mesmo assim? (s/N): " -n 1 -r
    echo ""
    if [[ ! $REPLY =~ ^[Ss]$ ]]; then
        print_info "Abortado pelo usuário"
        exit 0
    fi
fi

# ===================================================================
# FASE 4: Subir Infraestrutura
# ===================================================================

print_header "🚀 FASE 4: Iniciando Infraestrutura"

print_info "Subindo ZooKeeper, Kafka, Bancos de Dados e Zipkin..."
if ! docker-compose -f docker-compose-integrado.yml up -d zookeeper kafka mongo-db normalize-postgres zipkin mocks 2>&1 | tee /tmp/docker-infra.log; then
    print_error "Falha ao subir infraestrutura!"
    echo ""
    echo "===== LOG DE ERRO ====="
    tail -50 /tmp/docker-infra.log
    echo "======================="
    ERRORS=$((ERRORS + 1))
    exit 1
fi

print_success "Infraestrutura iniciada"

# Aguardar serviços ficarem prontos
print_info "Aguardando serviços ficarem prontos..."
for i in {1..30}; do
    echo -n "."
    sleep 1
done
echo ""

# Verificar saúde dos containers de infraestrutura
print_info "Verificando saúde dos serviços de infraestrutura..."
INFRA_OK=true

check_container() {
    CONTAINER=$1
    NAME=$2
    if ! docker ps | grep -q "$CONTAINER"; then
        print_error "Container $NAME não está rodando!"
        ERRORS=$((ERRORS + 1))
        INFRA_OK=false
    else
        print_success "Container $NAME: OK"
    fi
}

check_container "integrado-kafka" "Kafka"
check_container "integrado-zookeeper" "ZooKeeper"
check_container "integrado-mongo" "MongoDB"
check_container "integrado-postgres" "PostgreSQL"
check_container "integrado-zipkin" "Zipkin"

if [ "$INFRA_OK" = false ]; then
    print_error "Alguns serviços de infraestrutura falharam!"
    echo ""
    echo "Status dos containers:"
    docker-compose -f docker-compose-integrado.yml ps
    echo ""
    echo "Logs dos serviços:"
    docker-compose -f docker-compose-integrado.yml logs --tail=50
    exit 1
fi

# Criar tópico Kafka
print_info "Criando tópico Kafka 'vehicle_analysis_log'..."
if docker exec integrado-kafka kafka-topics --create \
    --topic vehicle_analysis_log \
    --bootstrap-server localhost:9092 \
    --partitions 3 \
    --replication-factor 1 \
    --if-not-exists 2>&1 | tee /tmp/kafka-topic.log; then
    print_success "Tópico Kafka criado/verificado"
else
    print_warning "Erro ao criar tópico (pode já existir)"
    tail -10 /tmp/kafka-topic.log
fi

# ===================================================================
# FASE 5: Subir Aplicações
# ===================================================================

print_header "🚀 FASE 5: Iniciando Aplicações"

print_info "Subindo Veículos API e Normalize Consumer..."
if ! docker-compose -f docker-compose-integrado.yml up -d veiculos-app normalize-app 2>&1 | tee /tmp/docker-apps.log; then
    print_error "Falha ao subir aplicações!"
    echo ""
    echo "===== LOG DE ERRO ====="
    tail -50 /tmp/docker-apps.log
    echo "======================="
    ERRORS=$((ERRORS + 1))
    exit 1
fi

print_success "Aplicações iniciadas"

# Aguardar aplicações ficarem prontas
print_info "Aguardando aplicações iniciarem (20 segundos)..."
for i in {1..20}; do
    echo -n "."
    sleep 1
done
echo ""

# ===================================================================
# FASE 6: Verificar Saúde das Aplicações
# ===================================================================

print_header "🏥 FASE 6: Verificação de Saúde"

print_info "Verificando containers das aplicações..."
APPS_OK=true

check_container "integrado-veiculos-api" "Veículos API"
check_container "integrado-normalize-consumer" "Normalize Consumer"

if [ "$APPS_OK" = false ]; then
    print_error "Alguns containers de aplicação falharam!"
    echo ""
    echo "Status completo:"
    docker-compose -f docker-compose-integrado.yml ps
    ERRORS=$((ERRORS + 1))
fi

# Health check HTTP das aplicações
print_info "Verificando health checks HTTP..."

# Tentar Veículos API
print_info "Testando Veículos API (http://localhost:8080/actuator/health)..."
VEICULOS_RETRY=0
VEICULOS_OK=false
while [ $VEICULOS_RETRY -lt 10 ]; do
    if curl -s http://localhost:8080/actuator/health | grep -q '"status":"UP"'; then
        print_success "Veículos API: HEALTHY"
        VEICULOS_OK=true
        break
    fi
    VEICULOS_RETRY=$((VEICULOS_RETRY + 1))
    echo -n "."
    sleep 2
done
echo ""

if [ "$VEICULOS_OK" = false ]; then
    print_error "Veículos API não respondeu ao health check!"
    echo ""
    echo "Logs do Veículos API:"
    docker-compose -f docker-compose-integrado.yml logs --tail=50 veiculos-app
    ERRORS=$((ERRORS + 1))
fi

# Tentar Normalize
print_info "Testando Normalize API (http://localhost:8081/actuator/health)..."
NORMALIZE_RETRY=0
NORMALIZE_OK=false
while [ $NORMALIZE_RETRY -lt 10 ]; do
    if curl -s http://localhost:8081/actuator/health | grep -q '"status":"UP"'; then
        print_success "Normalize API: HEALTHY"
        NORMALIZE_OK=true
        break
    fi
    NORMALIZE_RETRY=$((NORMALIZE_RETRY + 1))
    echo -n "."
    sleep 2
done
echo ""

if [ "$NORMALIZE_OK" = false ]; then
    print_error "Normalize API não respondeu ao health check!"
    echo ""
    echo "Logs do Normalize:"
    docker-compose -f docker-compose-integrado.yml logs --tail=50 normalize-app
    ERRORS=$((ERRORS + 1))
fi

# ===================================================================
# FASE 7: Relatório Final
# ===================================================================

print_header "📊 RELATÓRIO FINAL"

if [ $ERRORS -eq 0 ] && [ "$VEICULOS_OK" = true ] && [ "$NORMALIZE_OK" = true ]; then
    print_success "SUCESSO! Todos os serviços estão rodando!"
    echo ""
    echo -e "${GREEN}${BOLD}✅ SISTEMA INTEGRADO OPERACIONAL${NC}"
    echo ""
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
    echo -e "${BOLD}📊 SERVIÇOS DISPONÍVEIS:${NC}"
    echo ""
    echo "┌─────────────────────────────────────────────┐"
    echo "│ 🚗 Veículos API                             │"
    echo "│    http://localhost:8080                    │"
    echo "│    Swagger: /swagger-ui.html                │"
    echo "│                                             │"
    echo "│ 📊 Normalize Dashboard                      │"
    echo "│    http://localhost:8081                    │"
    echo "│    API: /api/logs                           │"
    echo "│                                             │"
    echo "│ 🔍 Zipkin (Tracing)                         │"
    echo "│    http://localhost:9411                    │"
    echo "│                                             │"
    echo "│ 🧪 WireMock (Mocks)                         │"
    echo "│    http://localhost:9090                    │"
    echo "└─────────────────────────────────────────────┘"
    echo ""
    echo -e "${BOLD}🔧 INFRAESTRUTURA:${NC}"
    echo "  • Kafka:      localhost:9092"
    echo "  • ZooKeeper:  localhost:2181"
    echo "  • MongoDB:    localhost:27017"
    echo "  • PostgreSQL: localhost:5433"
    echo ""
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
    echo -e "${BOLD}🧪 PRÓXIMOS PASSOS:${NC}"
    echo ""
    echo "1. Testar integração:"
    echo "   ./test-integracao.sh"
    echo ""
    echo "2. Ver logs em tempo real:"
    echo "   docker-compose -f docker-compose-integrado.yml logs -f"
    echo ""
    echo "3. Ver logs de serviço específico:"
    echo "   docker-compose -f docker-compose-integrado.yml logs -f veiculos-app"
    echo "   docker-compose -f docker-compose-integrado.yml logs -f normalize-app"
    echo ""
    echo "4. Parar tudo:"
    echo "   docker-compose -f docker-compose-integrado.yml down"
    echo ""
    echo "5. Ver status:"
    echo "   docker-compose -f docker-compose-integrado.yml ps"
    echo ""
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""

    exit 0
else
    echo ""
    echo -e "${RED}${BOLD}❌ FALHA NA INICIALIZAÇÃO${NC}"
    echo ""
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
    echo -e "${BOLD}📊 RESUMO DE ERROS:${NC}"
    echo "  • Total de erros: $ERRORS"
    echo "  • Total de avisos: $WARNINGS"
    echo ""

    if [ "$VEICULOS_OK" = false ]; then
        echo -e "${RED}  ✗ Veículos API: FALHOU${NC}"
    else
        echo -e "${GREEN}  ✓ Veículos API: OK${NC}"
    fi

    if [ "$NORMALIZE_OK" = false ]; then
        echo -e "${RED}  ✗ Normalize Consumer: FALHOU${NC}"
    else
        echo -e "${GREEN}  ✓ Normalize Consumer: OK${NC}"
    fi
    echo ""
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
    echo -e "${BOLD}🔍 DIAGNÓSTICO:${NC}"
    echo ""

    echo "1. Ver status de todos os containers:"
    echo "   docker-compose -f docker-compose-integrado.yml ps"
    echo ""

    echo "2. Ver logs completos:"
    echo "   docker-compose -f docker-compose-integrado.yml logs"
    echo ""

    echo "3. Ver logs de serviço específico:"
    echo "   docker-compose -f docker-compose-integrado.yml logs veiculos-app"
    echo "   docker-compose -f docker-compose-integrado.yml logs normalize-app"
    echo ""

    echo "4. Recomeçar do zero:"
    echo "   docker-compose -f docker-compose-integrado.yml down -v"
    echo "   ./run-all.sh"
    echo ""

    echo "5. Logs salvos em:"
    echo "   /tmp/veiculos-build.log"
    echo "   /tmp/normalize-build.log"
    echo "   /tmp/docker-*.log"
    echo ""

    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""

    # Mostrar logs dos serviços com falha
    if [ "$VEICULOS_OK" = false ]; then
        echo -e "${RED}${BOLD}Logs do Veículos API (últimas 50 linhas):${NC}"
        echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        docker-compose -f docker-compose-integrado.yml logs --tail=50 veiculos-app 2>&1 || echo "Não foi possível obter logs"
        echo ""
    fi

    if [ "$NORMALIZE_OK" = false ]; then
        echo -e "${RED}${BOLD}Logs do Normalize (últimas 50 linhas):${NC}"
        echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        docker-compose -f docker-compose-integrado.yml logs --tail=50 normalize-app 2>&1 || echo "Não foi possível obter logs"
        echo ""
    fi

    exit 1
fi

