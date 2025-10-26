#!/bin/bash

echo "=========================================="
echo "  📨 Produtor de Mensagens de Teste"
echo "=========================================="
echo ""

# Verificar se Kafka está rodando
if ! docker ps | grep -q normalize-kafka; then
    echo "❌ Kafka não está rodando!"
    echo "Execute: docker-compose up -d"
    exit 1
fi

echo "✅ Kafka detectado!"
echo ""

# Criar tópico se não existir
echo "📋 Verificando tópico 'vehicle_analysis_log'..."
docker exec normalize-kafka kafka-topics --create \
    --topic vehicle_analysis_log \
    --bootstrap-server localhost:9092 \
    --partitions 1 \
    --replication-factor 1 \
    --if-not-exists 2>/dev/null

echo ""
echo "🚀 Produzindo mensagens de teste..."
echo ""

# Mensagem 1: Placa sem restrições
MESSAGE1='{
  "id": "550e8400-e29b-41d4-a716-446655440001",
  "timestamp": "'$(date -u +%Y-%m-%dT%H:%M:%S.000Z)'",
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
  "traceId": "trace-'$(date +%s)'-001"
}'

echo "$MESSAGE1" | docker exec -i normalize-kafka kafka-console-producer \
    --topic vehicle_analysis_log \
    --bootstrap-server localhost:9092

echo "✅ Mensagem 1 enviada: PLACA ABC1234 (sem restrições)"
sleep 1

# Mensagem 2: RENAVAM com restrições
MESSAGE2='{
  "id": "550e8400-e29b-41d4-a716-446655440002",
  "timestamp": "'$(date -u +%Y-%m-%dT%H:%M:%S.000Z)'",
  "idInputType": "RENAVAM",
  "idInputValue": "12345678901",
  "vinCanonical": "2HGBH41JXMN109187",
  "supplierCalls": {
    "F1": {"status": "SUCCESS", "latencyMs": 150, "error": null},
    "F2": {"status": "SUCCESS", "latencyMs": 95, "error": null},
    "F3": {"status": "FAILED", "latencyMs": 5000, "error": "Timeout"}
  },
  "hasConstraints": true,
  "estimatedCostCents": 450,
  "traceId": "trace-'$(date +%s)'-002"
}'

echo "$MESSAGE2" | docker exec -i normalize-kafka kafka-console-producer \
    --topic vehicle_analysis_log \
    --bootstrap-server localhost:9092

echo "✅ Mensagem 2 enviada: RENAVAM 12345678901 (com restrições)"
sleep 1

# Mensagem 3: VIN direto
MESSAGE3='{
  "id": "550e8400-e29b-41d4-a716-446655440003",
  "timestamp": "'$(date -u +%Y-%m-%dT%H:%M:%S.000Z)'",
  "idInputType": "VIN",
  "idInputValue": "3HGBH41JXMN109188",
  "vinCanonical": "3HGBH41JXMN109188",
  "supplierCalls": {
    "F1": {"status": "SUCCESS", "latencyMs": 80, "error": null},
    "F2": {"status": "SUCCESS", "latencyMs": 110, "error": null},
    "F3": {"status": "SUCCESS", "latencyMs": 180, "error": null}
  },
  "hasConstraints": false,
  "estimatedCostCents": 300,
  "traceId": "trace-'$(date +%s)'-003"
}'

echo "$MESSAGE3" | docker exec -i normalize-kafka kafka-console-producer \
    --topic vehicle_analysis_log \
    --bootstrap-server localhost:9092

echo "✅ Mensagem 3 enviada: VIN 3HGBH41JXMN109188 (sem restrições)"
sleep 1

# Mensagem 4: Placa com erro em fornecedor
MESSAGE4='{
  "id": "550e8400-e29b-41d4-a716-446655440004",
  "timestamp": "'$(date -u +%Y-%m-%dT%H:%M:%S.000Z)'",
  "idInputType": "PLACA",
  "idInputValue": "XYZ9876",
  "vinCanonical": "4HGBH41JXMN109189",
  "supplierCalls": {
    "F1": {"status": "SUCCESS", "latencyMs": 100, "error": null},
    "F2": {"status": "FAILED", "latencyMs": 3000, "error": "Connection refused"},
    "F3": {"status": "SUCCESS", "latencyMs": 200, "error": null}
  },
  "hasConstraints": true,
  "estimatedCostCents": 500,
  "traceId": "trace-'$(date +%s)'-004"
}'

echo "$MESSAGE4" | docker exec -i normalize-kafka kafka-console-producer \
    --topic vehicle_analysis_log \
    --bootstrap-server localhost:9092

echo "✅ Mensagem 4 enviada: PLACA XYZ9876 (com restrições e erro em F2)"

echo ""
echo "=========================================="
echo "  ✅ 4 mensagens de teste enviadas!"
echo "=========================================="
echo ""
echo "🎨 Abra o dashboard para visualizar:"
echo "   http://localhost:8081"
echo ""
echo "🔍 Ou use a API:"
echo "   curl http://localhost:8081/api/logs"
echo ""
echo "📊 Ver estatísticas:"
echo "   curl http://localhost:8081/api/logs/stats"
echo ""

