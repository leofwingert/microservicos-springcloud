#!/bin/bash
# stop-all.sh — Para todos os microserviços

BASE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LOG_DIR="$BASE_DIR/logs"

echo "🛑 Parando todos os microserviços..."

for service in api-gateway pecas-service clientes-service representantes-service eureka-server config-server; do
    PID_FILE="$LOG_DIR/$service.pid"
    if [ -f "$PID_FILE" ]; then
        PID=$(cat "$PID_FILE")
        if kill -0 "$PID" 2>/dev/null; then
            kill "$PID"
            echo "  ✅ $service (PID $PID) encerrado"
        fi
        rm -f "$PID_FILE"
    fi
done

echo ""
echo "✅ Todos os serviços encerrados."
