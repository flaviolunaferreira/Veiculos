#!/bin/bash

echo "=========================================="
echo "  Iniciando Sistema de Análise Veicular"
echo "=========================================="
echo ""

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 1. Limpar ambiente
echo -e "${YELLOW}[1/5] Limpando ambiente anterior...${NC}"
docker-compose down -v --remove-orphans 2>/dev/null

# 2. Build do projeto
echo -e "${YELLOW}[2/5] Buildando aplicação Java...${NC}"
./gradlew clean build -x test --no-daemon
if [ $? -ne 0 ]; then
    echo -e "${RED}Erro no build do projeto!${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Build concluído com sucesso${NC}"
echo ""

# 3. Verificar se o JAR foi gerado
if [ ! -f "build/libs/veiculos-0.0.1-SNAPSHOT.jar" ]; then
    echo -e "${RED}Erro: JAR não foi gerado!${NC}"
    exit 1
fi
echo -e "${GREEN}✓ JAR encontrado: $(ls -lh build/libs/*.jar | awk '{print $9, $5}')${NC}"
echo ""

# 4. Subir containers
echo -e "${YELLOW}[3/5] Iniciando containers Docker...${NC}"
docker-compose up -d
if [ $? -ne 0 ]; then
    echo -e "${RED}Erro ao subir containers!${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Containers iniciados${NC}"
echo ""

# 5. Aguardar serviços ficarem prontos
echo -e "${YELLOW}[4/5] Aguardando serviços iniciarem...${NC}"
echo "   - MongoDB..."
sleep 5
echo "   - Kafka..."
sleep 5
echo "   - Zipkin..."
sleep 3
echo "   - WireMock..."
sleep 2
echo "   - Aplicação Spring Boot..."
sleep 10

# 6. Verificar status
echo -e "${YELLOW}[5/5] Verificando status dos serviços...${NC}"
echo ""
docker-compose ps

echo ""
echo "=========================================="
echo -e "${GREEN}Sistema iniciado com sucesso!${NC}"
echo "=========================================="
echo ""
echo "URLs disponíveis:"
echo "  • API REST:      http://localhost:8080"
echo "  • Swagger UI:    http://localhost:8080/swagger-ui.html"
echo "  • Health Check:  http://localhost:8080/actuator/health"
echo "  • Zipkin:        http://localhost:9411"
echo "  • WireMock:      http://localhost:9090"
echo ""
echo "Para ver logs:"
echo "  docker-compose logs -f app"
echo ""
echo "Para testar a API:"
echo "  curl -X GET \"http://localhost:8080/api/v1/veiculos/ABC1234/analise\" \\"
echo "    -H \"Idempotency-Key: \$(uuidgen)\""
echo ""

