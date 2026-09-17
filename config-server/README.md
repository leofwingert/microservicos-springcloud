
# ⚙️ Config Server

Servidor de configuração centralizada do sistema **MicroManager**, responsável por distribuir configurações comuns a todos os microserviços da arquitetura.

## 📋 Informações Gerais

| Item | Valor |
|------|-------|
| **Módulo** | `config-server` |
| **Porta** | `8888` |
| **Padrão** | Configuração Centralizada (Centralized Configuration) |
| **Framework** | Spring Cloud Config Server |
| **Classe Principal** | `ConfigServerApplication.java` |

## 🏗️ Papel na Arquitetura

O Config Server implementa o padrão **Centralized Configuration**, um dos pilares de uma arquitetura de microserviços. Ele atua como uma fonte única de verdade para configurações compartilhadas entre todos os serviços.

```
                    Config Server (:8888)
                          │
          ┌───────────────┼───────────────┐
          │               │               │
     Peças Service   Clientes Service  Representantes Service
       (:8081)          (:8082)           (:8083)
          │               │               │
          └───────────────┼───────────────┘
                          │
                    API Gateway (:8080)
                    Eureka Server (:8761)
```

> **Importante**: Este é o **primeiro serviço** que deve ser iniciado, pois todos os demais dependem dele para carregar suas configurações.

## 📂 Estrutura de Arquivos

```
config-server/
├── Dockerfile
├── pom.xml
└── src/
    └── main/
        ├── java/com/microservicos/configserver/
        │   └── ConfigServerApplication.java      ← Classe principal
        └── resources/
            ├── application.yml                    ← Configuração do servidor
            └── config-repo/
                └── application.yml                ← Configurações compartilhadas
```

## 🔧 Configuração

### `application.yml` (Servidor)

```yaml
server:
  port: 8888

spring:
  application:
    name: config-server
  profiles:
    active: native                              # Usa filesystem local (não Git)
  cloud:
    config:
      server:
        native:
          search-locations: classpath:/config-repo   # Onde estão os configs
```

- **Profile `native`**: O Config Server busca configurações em arquivos locais no classpath, ao invés de um repositório Git remoto. Ideal para desenvolvimento e projetos acadêmicos.
- **`search-locations`**: Aponta para o diretório `config-repo/` dentro dos resources.

### `config-repo/application.yml` (Configurações Compartilhadas)

Este arquivo contém configurações **comuns a todos os microserviços** que se conectam ao Config Server:

```yaml
eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/
  instance:
    preferIpAddress: true

management:
  endpoints:
    web:
      exposure:
        include: health,info
```

Qualquer serviço que importe `optional:configserver:http://localhost:8888` recebe automaticamente estas propriedades.

## 💻 Código Principal

### `ConfigServerApplication.java`

```java
@SpringBootApplication
@EnableConfigServer          // Ativa o Spring Cloud Config Server
public class ConfigServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}
```

A anotação `@EnableConfigServer` transforma a aplicação Spring Boot em um servidor de configuração, expondo endpoints REST que os clientes utilizam para buscar suas propriedades.

## 📦 Dependências

| Dependência | Propósito |
|-------------|-----------|
| `spring-cloud-config-server` | Funcionalidade core do Config Server |
| `spring-boot-starter-actuator` | Endpoints de monitoramento (`/actuator/health`) |

## 🚀 Como Executar

### Localmente
```bash
cd config-server
mvn spring-boot:run
```

### Via Docker
```bash
# Build da imagem
docker build -t config-server .

# Execução
docker run -p 8888:8888 config-server
```

### Via Docker Compose (com todo o sistema)
```bash
docker-compose up config-server
```

## 🔍 Verificação

Após iniciar, verifique se está funcionando:

```bash
# Health check
curl http://localhost:8888/actuator/health

# Buscar configurações compartilhadas (qualquer application-name)
curl http://localhost:8888/application/default
```

## 🐳 Dockerfile

```dockerfile
FROM eclipse-temurin:17-jre-alpine
VOLUME /tmp
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

Utiliza a imagem **Eclipse Temurin 17 JRE Alpine** para manter o container leve (~200MB).

## 📡 Integração com Outros Serviços

Cada microserviço cliente se conecta ao Config Server adicionando ao seu `application.yml`:

```yaml
spring:
  config:
    import: "optional:configserver:http://localhost:8888"
```

O prefixo `optional:` garante que o serviço não falhe caso o Config Server esteja indisponível — as configurações locais serão usadas como fallback.
