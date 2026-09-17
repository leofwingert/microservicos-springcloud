# 🌐 API Gateway

Ponto de entrada único do sistema **MicroManager**, responsável por rotear todas as requisições externas para os microserviços internos, implementando o padrão **Gateway**.

## 📋 Informações Gerais

| Item | Valor |
|------|-------|
| **Módulo** | `api-gateway` |
| **Porta** | `8080` |
| **Padrão** | Gateway (API Gateway) |
| **Framework** | Spring Cloud Gateway |
| **Classe Principal** | `ApiGatewayApplication.java` |

## 🏗️ Papel na Arquitetura

O API Gateway implementa o padrão **Gateway**, atuando como um único ponto de entrada para toda a aplicação. Ele recebe as requisições do frontend e as encaminha para o microserviço correto, utilizando **load balancing** via integração com o Eureka Server.

```
  Frontend (:3000)          Clientes externos (curl, Postman)
       │                              │
       ▼                              ▼
  ┌─────────────────────────────────────────┐
  │          API Gateway (:8080)            │
  │                                         │
  │  /api/pecas/**     → pecas-service     │
  │  /api/clientes/**  → clientes-service  │
  │  /api/representantes/** → reps-service │
  │                                         │
  │  CORS: *  │  Load Balancer: lb://      │
  └─────────────────────────────────────────┘
       │               │               │
  pecas-service   clientes-service  representantes
    (:8081)          (:8082)          (:8083)
```

### Responsabilidades:
- **Roteamento**: Direciona requisições com base no path da URL
- **Load Balancing**: Distribui requisições entre instâncias via Eureka (`lb://`)
- **CORS**: Gerencia Cross-Origin Resource Sharing globalmente
- **Ponto único de entrada**: Simplifica a comunicação do frontend

## 📂 Estrutura de Arquivos

```
api-gateway/
├── Dockerfile
├── pom.xml
└── src/
    └── main/
        ├── java/com/microservicos/gateway/
        │   └── ApiGatewayApplication.java         ← Classe principal
        └── resources/
            └── application.yml                     ← Rotas e configuração
```

## 🔧 Configuração

### `application.yml`

```yaml
spring:
  application:
    name: api-gateway
  config:
    import: "optional:configserver:http://localhost:8888"
  cloud:
    gateway:
      globalcors:
        corsConfigurations:
          '[/**]':
            allowedOrigins: "*"
            allowedMethods: "*"
            allowedHeaders: "*"
      routes:
        - id: pecas-route
          uri: lb://pecas-service           # Load balanced via Eureka
          predicates:
            - Path=/api/pecas/**

        - id: clientes-route
          uri: lb://clientes-service
          predicates:
            - Path=/api/clientes/**

        - id: representantes-route
          uri: lb://representantes-service
          predicates:
            - Path=/api/representantes/**

server:
  port: 8080

eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/
  instance:
    preferIpAddress: true
```

### Detalhes das Rotas

| ID da Rota | Path Pattern | Destino (URI) | Serviço Alvo |
|------------|-------------|---------------|--------------|
| `pecas-route` | `/api/pecas/**` | `lb://pecas-service` | Peças Service (:8081) |
| `clientes-route` | `/api/clientes/**` | `lb://clientes-service` | Clientes Service (:8082) |
| `representantes-route` | `/api/representantes/**` | `lb://representantes-service` | Representantes Service (:8083) |

**`lb://`** — Protocolo especial do Spring Cloud que resolve o nome do serviço via Eureka e aplica load balancing automaticamente. Isto significa que o Gateway não precisa saber o IP/porta de nenhum serviço, apenas o **nome registrado no Eureka**.

### CORS (Cross-Origin Resource Sharing)

```yaml
globalcors:
  corsConfigurations:
    '[/**]':
      allowedOrigins: "*"       # Aceita qualquer origem
      allowedMethods: "*"       # Aceita qualquer método HTTP
      allowedHeaders: "*"       # Aceita qualquer header
```

Configuração global permissiva para ambiente de desenvolvimento. Em produção, as origens devem ser restritas.

## 💻 Código Principal

### `ApiGatewayApplication.java`

```java
@SpringBootApplication
public class ApiGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
```

O Gateway não precisa de anotações adicionais — toda a configuração de rotas é feita via YAML. O Spring Cloud Gateway é baseado em **WebFlux** (reativo), não em Spring MVC tradicional.

## 📦 Dependências

| Dependência | Propósito |
|-------------|-----------|
| `spring-cloud-starter-gateway` | Core do Spring Cloud Gateway (WebFlux + roteamento) |
| `spring-cloud-starter-netflix-eureka-client` | Registro e descoberta de serviços via Eureka |
| `spring-cloud-starter-config` | Busca configurações do Config Server |
| `spring-boot-starter-actuator` | Endpoints de monitoramento e rotas ativas |

## 🚀 Como Executar

### Pré-requisitos
1. **Config Server** rodando na porta 8888
2. **Eureka Server** rodando na porta 8761
3. **Microserviços de negócio** registrados no Eureka

> O Gateway deve ser o **último serviço** a ser iniciado.

### Localmente
```bash
cd api-gateway
mvn spring-boot:run
```

### Via Docker
```bash
docker build -t api-gateway .
docker run -p 8080:8080 api-gateway
```

### Via Docker Compose (com todo o sistema)
```bash
docker-compose up api-gateway
```

## 🔍 Verificação

```bash
# Health check do Gateway
curl http://localhost:8080/actuator/health

# Listar rotas ativas do Gateway
curl http://localhost:8080/actuator/gateway/routes

# Testar roteamento para cada serviço
curl http://localhost:8080/api/pecas
curl http://localhost:8080/api/clientes
curl http://localhost:8080/api/representantes
```

## 🐳 Dockerfile

```dockerfile
FROM eclipse-temurin:17-jre-alpine
VOLUME /tmp
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

## ⚠️ Observações

- O Actuator expõe os endpoints `health`, `info` e `gateway` (este último permite inspecionar as rotas ativas via `/actuator/gateway/routes`).
- No Docker Compose, as URLs do Config Server e Eureka são substituídas por nomes de container via variáveis de ambiente:
  ```yaml
  environment:
    - SPRING_CONFIG_IMPORT=optional:configserver:http://config-server:8888
    - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/
  ```
