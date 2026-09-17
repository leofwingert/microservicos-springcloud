# 🔍 Eureka Server

Servidor de descoberta de serviços (Service Discovery) do sistema **MicroManager**, responsável por registrar e localizar dinamicamente todos os microserviços da arquitetura.

## 📋 Informações Gerais

| Item | Valor |
|------|-------|
| **Módulo** | `eureka-server` |
| **Porta** | `8761` |
| **Padrão** | Service Discovery |
| **Framework** | Spring Cloud Netflix Eureka |
| **Classe Principal** | `EurekaServerApplication.java` |
| **Dashboard** | http://localhost:8761 |

## 🏗️ Papel na Arquitetura

O Eureka Server implementa o padrão **Service Discovery**, permitindo que os microserviços se registrem automaticamente e sejam descobertos pelo API Gateway sem necessidade de configurar IPs/portas fixas.

```
              Eureka Server (:8761)
              ┌──── Registry ────┐
              │                  │
              │  pecas-service   │
              │  clientes-service│
              │  representantes  │
              │  api-gateway     │
              │                  │
              └──────────────────┘
                      ▲
                      │  Registro automático
          ┌───────────┼───────────┐
          │           │           │
     pecas-service  clientes   representantes
       (:8081)     (:8082)      (:8083)
```

### Fluxo de funcionamento:

1. **Registro**: Cada microserviço, ao iniciar, se registra no Eureka informando seu nome e endereço.
2. **Heartbeat**: Os serviços enviam batimentos cardíacos periódicos para confirmar que estão vivos.
3. **Descoberta**: O API Gateway consulta o Eureka para descobrir as instâncias disponíveis de cada serviço (via `lb://nome-servico`).
4. **Balanceamento**: Com múltiplas instâncias registradas, o load balancer distribui as requisições automaticamente.

## 📂 Estrutura de Arquivos

```
eureka-server/
├── Dockerfile
├── pom.xml
└── src/
    └── main/
        ├── java/com/microservicos/eurekaserver/
        │   └── EurekaServerApplication.java      ← Classe principal
        └── resources/
            └── application.yml                    ← Configuração do Eureka
```

## 🔧 Configuração

### `application.yml`

```yaml
server:
  port: 8761

spring:
  application:
    name: eureka-server

eureka:
  instance:
    hostname: localhost
  client:
    registerWithEureka: false       # Não se registra em si mesmo
    fetchRegistry: false            # Não busca registro de outros Eureka
    serviceUrl:
      defaultZone: http://${eureka.instance.hostname}:${server.port}/eureka/
```

**Parâmetros chave:**
- `registerWithEureka: false` — Como é o único servidor Eureka (standalone), ele não precisa se registrar em outro Eureka.
- `fetchRegistry: false` — Não precisa buscar registros de outros servidores Eureka (não é um cluster).
- `defaultZone` — URL onde o próprio Eureka escuta por registros.

## 💻 Código Principal

### `EurekaServerApplication.java`

```java
@SpringBootApplication
@EnableEurekaServer          // Ativa o servidor Eureka
public class EurekaServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
```

A anotação `@EnableEurekaServer` transforma a aplicação em um servidor de registro e descoberta de serviços Netflix Eureka.

## 📦 Dependências

| Dependência | Propósito |
|-------------|-----------|
| `spring-cloud-starter-netflix-eureka-server` | Funcionalidade core do Eureka Server |
| `spring-boot-starter-actuator` | Endpoints de monitoramento (`/actuator/health`) |

## 🚀 Como Executar

### Pré-requisito
O **Config Server** deve estar rodando na porta 8888 antes de iniciar o Eureka.

### Localmente
```bash
cd eureka-server
mvn spring-boot:run
```

### Via Docker
```bash
docker build -t eureka-server .
docker run -p 8761:8761 eureka-server
```

### Via Docker Compose (com todo o sistema)
```bash
docker-compose up eureka-server
```

## 🔍 Verificação

### Dashboard Web
Abra o navegador e acesse: **http://localhost:8761**

O dashboard mostra:
- Serviços registrados e suas instâncias
- Status de cada instância (UP/DOWN)
- Informações do ambiente

### Via API
```bash
# Health check
curl http://localhost:8761/actuator/health

# Listar serviços registrados (XML)
curl http://localhost:8761/eureka/apps
```

## 🐳 Dockerfile

```dockerfile
FROM eclipse-temurin:17-jre-alpine
VOLUME /tmp
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

## 📡 Integração com Outros Serviços

Os microserviços clientes se registram no Eureka adicionando ao seu `application.yml`:

```yaml
eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/
  instance:
    preferIpAddress: true
```

E adicionando a dependência no `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

> **Nota**: No ambiente Docker, a URL do Eureka é substituída por `http://eureka-server:8761/eureka/` via variáveis de ambiente no `docker-compose.yml`.
