# 📊 Observabilidade — Prometheus + Grafana

Documentação detalhada da stack de monitoramento do projeto MicroManager.

## Visão Geral

A observabilidade do projeto é composta por três camadas:

1. **Micrometer** — biblioteca integrada aos serviços Spring Boot que coleta métricas da JVM, HTTP, e outras
2. **Prometheus** — servidor de coleta que faz scrape das métricas de cada serviço a cada 5 segundos
3. **Grafana** — plataforma de visualização com dashboard pré-configurado

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│  Spring Boot    │     │                 │     │                 │
│  + Micrometer   │────▶│   Prometheus    │────▶│    Grafana      │
│  /actuator/     │     │   :9090         │     │    :3001        │
│   prometheus    │     │                 │     │                 │
└─────────────────┘     └─────────────────┘     └─────────────────┘
   (cada serviço)          (scrape 5s)           (dashboard auto)
```

## Componentes

### Micrometer Registry Prometheus

Cada serviço Spring Boot inclui a dependência `micrometer-registry-prometheus` que automaticamente expõe métricas no formato Prometheus via Spring Boot Actuator.

**Dependência (pom.xml de cada serviço):**
```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
    <scope>runtime</scope>
</dependency>
```

**Configuração (application.yml de cada serviço):**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus
  metrics:
    tags:
      application: ${spring.application.name}
```

A tag `application` é adicionada a **todas** as métricas, permitindo filtrar por serviço no Grafana.

### Prometheus

O Prometheus é configurado via `prometheus/prometheus.yml` e executa dentro de um container Docker.

**Targets monitorados:**

| Serviço | Endpoint |
|---------|----------|
| config-server | `http://config-server:8888/actuator/prometheus` |
| eureka-server | `http://eureka-server:8761/actuator/prometheus` |
| api-gateway | `http://api-gateway:8080/actuator/prometheus` |
| pecas-service | `http://pecas-service:8081/actuator/prometheus` |
| clientes-service | `http://clientes-service:8082/actuator/prometheus` |
| representantes-service | `http://representantes-service:8083/actuator/prometheus` |

**Acesso:** http://localhost:9090

**Verificar status dos targets:** http://localhost:9090/targets

### Grafana

O Grafana é provisionado automaticamente com:
- **Datasource**: Prometheus (configurado em `grafana/provisioning/datasources/datasource.yml`)
- **Dashboard**: "Microserviços Spring Cloud" (configurado em `grafana/dashboards/microservicos-dashboard.json`)

**Acesso:** http://localhost:3001  
**Credenciais:** `admin` / `admin`

## Estrutura de Arquivos

```
microservicos-springcloud/
├── prometheus/
│   └── prometheus.yml                          # Configuração de scrape
├── grafana/
│   ├── provisioning/
│   │   ├── datasources/
│   │   │   └── datasource.yml                  # Datasource Prometheus
│   │   └── dashboards/
│   │       └── dashboard.yml                   # Provider de dashboards
│   └── dashboards/
│       └── microservicos-dashboard.json         # Dashboard JSON
└── docker-compose.yml                           # Containers Prometheus + Grafana
```

## Dashboard — Painéis Disponíveis

### 1. Visão Geral dos Serviços
- **Status dos Serviços** — Indicadores UP/DOWN com cores (verde/vermelho) para cada serviço

### 2. Requisições HTTP
- **Taxa de Requisições HTTP (por serviço)** — Gráfico de linha com req/s agrupado por serviço
- **Requisições HTTP por Status Code** — Gráfico de barras empilhadas por código HTTP (200, 404, 500, etc.)

### 3. Latência
- **Latência HTTP (Percentis)** — Percentis p50, p95 e p99 por serviço
- **Latência Média por Endpoint** — Latência média detalhada por método e URI

### 4. JVM — Memória
- **Memória Heap JVM Usada** — Uso de memória heap por pool (Eden, Survivor, Old Gen)
- **Memória Non-Heap JVM Usada** — Uso de memória non-heap (Metaspace, Code Cache, etc.)

### 5. JVM — CPU & Threads
- **Uso de CPU do Processo** — Percentual de CPU usado por cada serviço
- **Uso de CPU do Sistema** — CPU total do sistema reportada por cada serviço
- **Threads Ativas JVM** — Número de threads ativas em cada JVM

### 6. Garbage Collector
- **GC Pausas por Segundo** — Taxa de pausas do garbage collector
- **Tempo de GC por Segundo** — Tempo gasto em coleta de lixo

### Variável de Filtro

O dashboard inclui a variável **`$application`** no topo que permite:
- Selecionar **todos** os serviços (padrão)
- Filtrar por **um ou mais** serviços específicos

## Métricas Disponíveis

### Métricas HTTP
| Métrica | Descrição |
|---------|-----------|
| `http_server_requests_seconds_count` | Total de requisições HTTP recebidas |
| `http_server_requests_seconds_sum` | Tempo total gasto processando requisições |
| `http_server_requests_seconds_bucket` | Histograma de latência (para percentis) |

### Métricas JVM
| Métrica | Descrição |
|---------|-----------|
| `jvm_memory_used_bytes` | Memória JVM usada (heap e non-heap) |
| `jvm_memory_max_bytes` | Memória JVM máxima disponível |
| `jvm_threads_live_threads` | Threads ativas na JVM |
| `jvm_gc_pause_seconds_count` | Contagem de pausas do GC |
| `jvm_gc_pause_seconds_sum` | Tempo total de pausas do GC |

### Métricas de Sistema
| Métrica | Descrição |
|---------|-----------|
| `process_cpu_usage` | Uso de CPU do processo Java |
| `system_cpu_usage` | Uso total de CPU do sistema |
| `process_uptime_seconds` | Tempo de atividade do processo |

## Queries PromQL Úteis

```promql
# Taxa de requisições por serviço (último minuto)
sum(rate(http_server_requests_seconds_count[1m])) by (application)

# Latência p95 por serviço
histogram_quantile(0.95, sum(rate(http_server_requests_seconds_bucket[1m])) by (le, application))

# Requisições com erro (status 5xx)
sum(rate(http_server_requests_seconds_count{status=~"5.."}[1m])) by (application)

# Memória heap usada em MB
jvm_memory_used_bytes{area="heap"} / 1024 / 1024

# Serviços que estão UP
up == 1
```

## Personalização

### Adicionar Novo Serviço ao Monitoramento

1. Adicionar `micrometer-registry-prometheus` ao `pom.xml` do serviço
2. Configurar o `application.yml` com os endpoints e tags
3. Adicionar o target ao `prometheus/prometheus.yml`:
   ```yaml
   - job_name: 'novo-servico'
     metrics_path: '/actuator/prometheus'
     static_configs:
       - targets: ['novo-servico:PORTA']
   ```
4. Reiniciar o Prometheus: `docker compose restart prometheus`

### Adicionar Novo Dashboard

1. Criar o arquivo JSON do dashboard em `grafana/dashboards/`
2. Reiniciar o Grafana: `docker compose restart grafana`

### Alterar Intervalo de Scrape

Editar `prometheus/prometheus.yml`:
```yaml
global:
  scrape_interval: 15s   # alterar de 5s para 15s (menos carga)
```

## Troubleshooting

### Todos os serviços aparecem como DOWN no Prometheus

- Verificar se os containers foram construídos **após** a adição da dependência `micrometer-registry-prometheus`:
  ```bash
  docker compose up --build
  ```
- Verificar os targets: http://localhost:9090/targets

### Endpoint /actuator/prometheus retorna 404

- Confirmar que a dependência `micrometer-registry-prometheus` está no `pom.xml`
- Confirmar que `prometheus` está listado no `management.endpoints.web.exposure.include` do `application.yml`

### Dashboard sem dados no Grafana

- Verificar se o datasource Prometheus está configurado: Grafana → Configuration → Data Sources
- Verificar se o Prometheus está coletando métricas: http://localhost:9090/targets
- Aguardar alguns segundos para os dados serem coletados (scrape interval = 5s)
