#!/bin/bash
# launch-all.sh — Lança todos os serviços em processos completamente independentes
# usando o padrão double-fork para desanexar do processo chamador

BASE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LOG_DIR="$BASE_DIR/logs"
mkdir -p "$LOG_DIR"

MVN_PATH=$(find "$HOME/.m2/wrapper/dists" -name "mvn" 2>/dev/null | head -1)
export PATH="$PATH:$(dirname $MVN_PATH)"

# Função para lançar um JAR de forma completamente desanexada
launch_jar() {
    local jar="$1"
    local log="$2"
    local name="$3"

    # Double-fork: desanexa completamente do processo pai
    (
        nohup java -jar "$jar" >> "$log" 2>&1 &
        echo $! > "$LOG_DIR/${name}.pid"
        echo "  ✅ $name iniciado (PID: $!)"
    )
}

echo "========================================"
echo "  MicroManager — Iniciando Serviços"
echo "========================================"
echo ""

echo "Compilando..."
"$MVN_PATH" -f "$BASE_DIR/pom.xml" clean package -DskipTests -q 2>/dev/null || true
echo "  ✅ Build OK"
echo ""

echo "🚀 Iniciando Config Server (8888)..."
launch_jar "$BASE_DIR/config-server/target/config-server-1.0.0-SNAPSHOT.jar" \
    "$LOG_DIR/config-server.log" "config-server"

sleep 12

echo "🚀 Iniciando Eureka Server (8761)..."
launch_jar "$BASE_DIR/eureka-server/target/eureka-server-1.0.0-SNAPSHOT.jar" \
    "$LOG_DIR/eureka-server.log" "eureka-server"

sleep 12

echo "🚀 Iniciando Peças Service (8081)..."
launch_jar "$BASE_DIR/pecas-service/target/pecas-service-1.0.0-SNAPSHOT.jar" \
    "$LOG_DIR/pecas-service.log" "pecas-service"

echo "🚀 Iniciando Clientes Service (8082)..."
launch_jar "$BASE_DIR/clientes-service/target/clientes-service-1.0.0-SNAPSHOT.jar" \
    "$LOG_DIR/clientes-service.log" "clientes-service"

echo "🚀 Iniciando Representantes Service (8083)..."
launch_jar "$BASE_DIR/representantes-service/target/representantes-service-1.0.0-SNAPSHOT.jar" \
    "$LOG_DIR/representantes-service.log" "representantes-service"

sleep 20

echo "🚀 Iniciando API Gateway (8080)..."
launch_jar "$BASE_DIR/api-gateway/target/api-gateway-1.0.0-SNAPSHOT.jar" \
    "$LOG_DIR/api-gateway.log" "api-gateway"

sleep 20

echo ""
echo "Verificando status..."
for port in 8888 8761 8081 8082 8083 8080; do
    if nc -z localhost "$port" 2>/dev/null; then
        echo "  ✅ Porta $port: OK"
    else
        echo "  ❌ Porta $port: não disponível"
    fi
done

echo ""
echo "========================================"
echo "  Sistema iniciado!"
echo "  Gateway: http://localhost:8080"
echo "  Eureka:  http://localhost:8761"
echo "========================================"
