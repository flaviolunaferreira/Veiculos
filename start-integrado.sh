#!/bin/bash

echo "=========================================="
echo "  🚀 Inicializando Sistema Integrado"
echo "=========================================="
echo ""
echo "Este script irá subir AMBOS os projetos integrados:"
echo "  • Veículos (Producer) - porta 8080"
echo "  • Normalize (Consumer) - porta 8081"
echo "  • Kafka compartilhado"
echo "  • Zipkin compartilhado"
echo ""
read -p "Pressione ENTER para continuar..."

# Verificar se os builds existem
echo ""
echo "[1/5] Verificando builds..."

if [ ! -f "./veiculos/build/libs/veiculos-0.0.1-SNAPSHOT.jar" ]; then
    echo "⚠️  Build do veículos não encontrado. Compilando..."
    cd veiculos
    ./gradlew clean bootJar
    cd ..
fi

if [ ! -f "./normalize/build/libs/normalize-0.0.1-SNAPSHOT.jar" ]; then
    echo "⚠️  Build do normalize não encontrado. Compilando..."
    cd normalize
    ./gradlew clean bootJar
    cd ..
fi

echo "✅ Builds verificados!"

# Parar containers antigos
echo ""
echo "[2/5] Parando containers antigos (se existirem)..."
docker-compose -f docker-compose-integrado.yml down 2>/dev/null || true

# Remover volumes antigos (opcional - comentado para preservar dados)
# docker volume rm veiculos_mongo_data veiculos_postgres_data 2>/dev/null || true

# Subir infraestrutura (bancos, kafka, zipkin)
echo ""
echo "[3/5] Subindo infraestrutura (Kafka, ZooKeeper, Bancos, Zipkin)..."
docker-compose -f docker-compose-integrado.yml up -d zookeeper kafka mongo-db normalize-postgres zipkin mocks

echo "⏳ Aguardando infraestrutura ficar pronta (30 segundos)..."
sleep 30

# Criar tópico Kafka se não existir
echo ""
echo "[4/5] Criando tópico Kafka 'vehicle_analysis_log'..."
docker exec shared-kafka kafka-topics --create \
    --topic vehicle_analysis_log \
    --bootstrap-server localhost:9092 \
    --partitions 3 \
    --replication-factor 1 \
    --if-not-exists 2>/dev/null || echo "Tópico já existe ou será criado automaticamente"

# Subir aplicações
echo ""
echo "[5/5] Subindo aplicações (Veículos + Normalize)..."
docker-compose -f docker-compose-integrado.yml up -d veiculos-app normalize-app

echo ""
echo "⏳ Aguardando aplicações iniciarem (20 segundos)..."
sleep 20

echo ""
echo "=========================================="
echo "  ✅ Sistema Integrado Iniciado!"
echo "=========================================="
echo ""
echo "📊 SERVIÇOS DISPONÍVEIS:"
echo ""
echo "┌─────────────────────────────────────────┐"
echo "│ APLICAÇÕES                              │"
echo "├─────────────────────────────────────────┤"
echo "│ Veículos API:      http://localhost:8080│"
echo "│ Normalize Dashboard: http://localhost:8081│"
echo "└─────────────────────────────────────────┘"
echo ""
echo "┌─────────────────────────────────────────┐"
echo "│ OBSERVABILIDADE                         │"
echo "├─────────────────────────────────────────┤"
echo "│ Zipkin:           http://localhost:9411│"
echo "│ WireMock (Mocks): http://localhost:9090│"
echo "└─────────────────────────────────────────┘"
echo ""
echo "┌─────────────────────────────────────────┐"
echo "│ INFRAESTRUTURA                          │"
echo "├─────────────────────────────────────────┤"
echo "│ Kafka:            localhost:9092        │"
echo "│ ZooKeeper:        localhost:2181        │"
echo "│ MongoDB:          localhost:27017       │"
echo "│ PostgreSQL:       localhost:5433        │"
echo "└─────────────────────────────────────────┘"
echo ""
echo "🔄 FLUXO DE INTEGRAÇÃO:"
echo "  1️⃣  POST em Veículos API → Análise de veículo"
echo "  2️⃣  Veículos produz log no Kafka"
echo "  3️⃣  Normalize consome e persiste no PostgreSQL"
echo "  4️⃣  Visualize no Dashboard do Normalize"
echo ""
echo "📋 COMANDOS ÚTEIS:"
echo ""
echo "  # Ver logs do Veículos"
echo "  docker-compose -f docker-compose-integrado.yml logs -f veiculos-app"
echo ""
echo "  # Ver logs do Normalize"
echo "  docker-compose -f docker-compose-integrado.yml logs -f normalize-app"
echo ""
echo "  # Ver logs do Kafka"
echo "  docker-compose -f docker-compose-integrado.yml logs -f kafka"
echo ""
echo "  # Status de todos os serviços"
echo "  docker-compose -f docker-compose-integrado.yml ps"
echo ""
echo "  # Parar tudo"
echo "  docker-compose -f docker-compose-integrado.yml down"
echo ""
echo "  # Health checks"
echo "  curl http://localhost:8080/actuator/health"
echo "  curl http://localhost:8081/actuator/health"
echo ""
echo "🧪 TESTAR INTEGRAÇÃO:"
echo ""
echo "  Execute o script de teste:"
echo "  ./test-integracao.sh"
echo ""
echo "=========================================="

