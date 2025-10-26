#!/bin/bash

echo "=========================================="
echo "  🧪 Teste de Integração Completa"
echo "=========================================="
echo ""

# Verificar se as APIs estão UP
echo "[1/4] Verificando saúde dos serviços..."
echo ""

echo -n "  Veículos API... "
VEICULOS_HEALTH=$(curl -s http://localhost:8080/actuator/health | grep -o '"status":"UP"' || echo "DOWN")
if [ "$VEICULOS_HEALTH" == "DOWN" ]; then
    echo "❌ OFFLINE"
    echo ""
    echo "Execute: docker-compose -f docker-compose-integrado.yml logs veiculos-app"
    exit 1
else
    echo "✅ ONLINE"
fi

echo -n "  Normalize API... "
NORMALIZE_HEALTH=$(curl -s http://localhost:8081/actuator/health | grep -o '"status":"UP"' || echo "DOWN")
if [ "$NORMALIZE_HEALTH" == "DOWN" ]; then
    echo "❌ OFFLINE"
    echo ""
    echo "Execute: docker-compose -f docker-compose-integrado.yml logs normalize-app"
    exit 1
else
    echo "✅ ONLINE"
fi

echo ""
echo "✅ Todos os serviços estão saudáveis!"
echo ""

# Verificar logs iniciais no Normalize
echo "[2/4] Verificando quantidade atual de logs no Normalize..."
INITIAL_COUNT=$(curl -s http://localhost:8081/api/logs/stats | grep -o '"totalLogs":[0-9]*' | grep -o '[0-9]*')
echo "  Logs atuais: $INITIAL_COUNT"
echo ""

# Fazer uma requisição ao Veículos API
echo "[3/4] Enviando requisição de análise de veículo..."
echo ""

RESPONSE=$(curl -s -X POST http://localhost:8080/api/vehicles/analysis \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": {
      "type": "PLACA",
      "value": "ABC1234"
    }
  }')

echo "Resposta da API Veículos:"
echo "$RESPONSE" | jq '.' 2>/dev/null || echo "$RESPONSE"
echo ""

# Aguardar processamento
echo "⏳ Aguardando mensagem ser consumida pelo Normalize (10 segundos)..."
sleep 10

# Verificar se log foi persistido no Normalize
echo ""
echo "[4/4] Verificando se log foi persistido no Normalize..."
NEW_COUNT=$(curl -s http://localhost:8081/api/logs/stats | grep -o '"totalLogs":[0-9]*' | grep -o '[0-9]*')
echo "  Logs atuais: $NEW_COUNT"
echo ""

if [ "$NEW_COUNT" -gt "$INITIAL_COUNT" ]; then
    echo "✅ SUCESSO! Log foi persistido no Normalize!"
    echo ""
    echo "Novo log adicionado:"
    curl -s http://localhost:8081/api/logs/latest | jq '.'
else
    echo "❌ FALHA! Nenhum log novo foi adicionado."
    echo ""
    echo "Possíveis problemas:"
    echo "  1. Veículos não está produzindo no Kafka"
    echo "  2. Tópico Kafka diferente"
    echo "  3. Normalize não está consumindo"
    echo ""
    echo "Debug:"
    echo "  # Ver logs do Veículos"
    echo "  docker-compose -f docker-compose-integrado.yml logs veiculos-app | grep -i kafka"
    echo ""
    echo "  # Ver logs do Normalize"
    echo "  docker-compose -f docker-compose-integrado.yml logs normalize-app | grep -i 'Recebido'"
    echo ""
    echo "  # Verificar tópico no Kafka"
    echo "  docker exec shared-kafka kafka-topics --list --bootstrap-server localhost:9092"
    exit 1
fi

echo ""
echo "=========================================="
echo "  🎉 INTEGRAÇÃO FUNCIONANDO!"
echo "=========================================="
echo ""
echo "📊 Visualize no Dashboard:"
echo "   http://localhost:8081"
echo ""
echo "🔍 Veja os traces no Zipkin:"
echo "   http://localhost:9411"
echo ""
echo "🔄 Fluxo completo testado:"
echo "   ✅ Requisição HTTP → Veículos API"
echo "   ✅ Análise processada"
echo "   ✅ Log produzido no Kafka"
echo "   ✅ Log consumido pelo Normalize"
echo "   ✅ Log persistido no PostgreSQL"
echo "   ✅ Disponível na API e Dashboard"
echo ""
echo "🧪 Quer testar mais?"
echo ""
echo "   # Testar com RENAVAM"
echo "   curl -X POST http://localhost:8080/api/vehicles/analysis \\"
echo "     -H 'Content-Type: application/json' \\"
echo "     -d '{\"identifier\":{\"type\":\"RENAVAM\",\"value\":\"12345678901\"}}'"
echo ""
echo "   # Testar com VIN"
echo "   curl -X POST http://localhost:8080/api/vehicles/analysis \\"
echo "     -H 'Content-Type: application/json' \\"
echo "     -d '{\"identifier\":{\"type\":\"VIN\",\"value\":\"1HGBH41JXMN109186\"}}'"
echo ""

