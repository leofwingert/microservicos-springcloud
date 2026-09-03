#!/bin/bash
# run-service.sh - Executa um serviço Spring Boot garantindo que persista

SERVICE_JAR="$1"
LOG_FILE="$2"

# Double-fork trick para desanexar completamente do processo pai
(
    exec java -jar "$SERVICE_JAR" >> "$LOG_FILE" 2>&1
) &
disown $!
echo $!
