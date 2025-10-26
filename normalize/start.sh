#!/bin/bash

echo "=========================================="
echo "  Normalize Log Consumer - Start Script"
echo "=========================================="
echo ""

# Build do projeto
echo "[1/3] Building project with Gradle..."
./gradlew clean bootJar

if [ $? -ne 0 ]; then
    echo "ERROR: Build failed!"
    exit 1
fi

echo ""
echo "[2/3] Starting Docker Compose services..."
docker-compose up -d

if [ $? -ne 0 ]; then
    echo "ERROR: Failed to start Docker services!"
    exit 1
fi

echo ""
echo "[3/3] Waiting for services to be ready..."
sleep 5

echo ""
echo "=========================================="
echo "  Services Started Successfully!"
echo "=========================================="
echo ""
echo "Application:     http://localhost:8081"
echo "Health Check:    http://localhost:8081/actuator/health"
echo "Zipkin UI:       http://localhost:9411"
echo "PostgreSQL:      localhost:5433"
echo ""
echo "To view logs: docker-compose logs -f normalize-app"
echo "To stop:      docker-compose down"
echo ""

