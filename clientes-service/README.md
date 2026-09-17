# 👥 Clientes Service

Microserviço de negócio do sistema **MicroManager**, responsável pelo gerenciamento do cadastro de clientes.

## 📋 Informações Gerais

| Item | Valor |
|------|-------|
| **Módulo** | `clientes-service` |
| **Porta** | `8082` |
| **Banco de Dados** | H2 em memória (`clientesdb`) |
| **Padrão** | Microserviço com Database per Service |
| **Classe Principal** | `ClientesServiceApplication.java` |
| **Base URL** (via Gateway) | `http://localhost:8080/api/clientes` |
| **Base URL** (direto) | `http://localhost:8082/api/clientes` |

## 🏗️ Papel na Arquitetura

O Clientes Service é um dos 3 microserviços de negócio do sistema. Ele:
- Registra-se automaticamente no **Eureka Server** para ser descoberto pelo Gateway
- Busca configurações compartilhadas do **Config Server**
- Possui seu **próprio banco de dados** H2 em memória (padrão Database per Service)
- É acessado externamente **somente via API Gateway**

```
Cliente → API Gateway (:8080) → /api/clientes/** → Clientes Service (:8082) → H2 clientesdb
```

## 📂 Estrutura de Arquivos

```
clientes-service/
├── Dockerfile
├── pom.xml
└── src/
    └── main/
        ├── java/com/microservicos/clientes/
        │   ├── ClientesServiceApplication.java        ← Classe principal
        │   ├── model/
        │   │   └── Cliente.java                       ← Entidade JPA
        │   ├── repository/
        │   │   └── ClienteRepository.java             ← Repositório (Spring Data)
        │   ├── service/
        │   │   └── ClienteService.java                ← Camada de serviço
        │   └── controller/
        │       └── ClienteController.java             ← REST Controller
        └── resources/
            └── application.yml                         ← Configurações
```

## 💾 Modelo de Dados

### Entidade `Cliente`

| Campo | Tipo | Restrições | Descrição |
|-------|------|------------|-----------|
| `cpf` | `String` | PK, UNIQUE, NOT NULL | CPF do cliente (chave primária) |
| `nome` | `String` | NOT NULL, `@NotBlank` | Nome completo do cliente |

```java
@Entity
@Table(name = "clientes")
public class Cliente {
    @Id
    @Column(nullable = false, unique = true)
    private String cpf;             // CPF como chave primária

    @NotBlank(message = "Nome é obrigatório")
    @Column(nullable = false)
    private String nome;
}
```

> **Nota**: Diferente do Peças Service que usa `Long id` auto-incremento, o Clientes Service usa o **CPF como chave primária natural** (tipo `String`).

## 📡 API Endpoints

Todos os endpoints são acessíveis via API Gateway (`http://localhost:8080`) ou diretamente (`http://localhost:8082`).

### Cadastrar Cliente

```
POST /api/clientes
```

**Request Body:**
```json
{
    "cpf": "123.456.789-00",
    "nome": "João Silva"
}
```

**Response** (`201 Created`):
```json
{
    "cpf": "123.456.789-00",
    "nome": "João Silva"
}
```

### Listar Todos os Clientes

```
GET /api/clientes
```

**Response** (`200 OK`):
```json
[
    {
        "cpf": "123.456.789-00",
        "nome": "João Silva"
    },
    {
        "cpf": "987.654.321-00",
        "nome": "Maria Santos"
    }
]
```

### Buscar por CPF

```
GET /api/clientes/cpf/{cpf}
```

**Exemplo**: `GET /api/clientes/cpf/123.456.789-00`

**Response** (`200 OK`):
```json
{
    "cpf": "123.456.789-00",
    "nome": "João Silva"
}
```

**Response** (`404 Not Found`): quando o CPF não é encontrado.

### Buscar por Nome

```
GET /api/clientes/nome/{nome}
```

Busca case-insensitive por clientes cujo nome contém o termo informado.

**Exemplo**: `GET /api/clientes/nome/joao`

**Response** (`200 OK`):
```json
[
    {
        "cpf": "123.456.789-00",
        "nome": "João Silva"
    }
]
```

**Response** (`404 Not Found`): quando nenhum cliente é encontrado.

## 🏛️ Arquitetura de Camadas

### Controller → Service → Repository

```
ClienteController       ClienteService       ClienteRepository
@RestController         @Service             @Repository
     │                       │                      │
     │  cadastrar()          │  salvar()            │  save()
     │  listarTodos()        │  listarTodos()       │  findAll()
     │  buscarPorCpf()       │  buscarPorCpf()      │  findById()
     │  buscarPorNome()      │  buscarPorNome()     │  findByNomeContainingIgnoreCase()
     │                       │                      │
     ▼                       ▼                      ▼
  HTTP Request/Response   Business Logic         JPA/H2 Database
```

### Repository

```java
@Repository
public interface ClienteRepository extends JpaRepository<Cliente, String> {
    List<Cliente> findByNomeContainingIgnoreCase(String nome);
}
```

- `JpaRepository<Cliente, String>` — O segundo tipo genérico é `String` porque a PK (CPF) é do tipo String.
- `findByNomeContainingIgnoreCase` — Query method derivado pelo Spring Data.

### Service

```java
@Service
public class ClienteService {
    private final ClienteRepository repository;

    public ClienteService(ClienteRepository repository) {
        this.repository = repository;
    }

    public Cliente salvar(Cliente cliente) { return repository.save(cliente); }
    public List<Cliente> listarTodos() { return repository.findAll(); }
    public Optional<Cliente> buscarPorCpf(String cpf) { return repository.findById(cpf); }
    public List<Cliente> buscarPorNome(String nome) { return repository.findByNomeContainingIgnoreCase(nome); }
}
```

### Controller

```java
@RestController
@RequestMapping("/api/clientes")
public class ClienteController {
    private final ClienteService service;

    @PostMapping
    public ResponseEntity<Cliente> cadastrar(@Valid @RequestBody Cliente cliente) { ... }

    @GetMapping
    public ResponseEntity<List<Cliente>> listarTodos() { ... }

    @GetMapping("/cpf/{cpf}")
    public ResponseEntity<Cliente> buscarPorCpf(@PathVariable String cpf) { ... }

    @GetMapping("/nome/{nome}")
    public ResponseEntity<List<Cliente>> buscarPorNome(@PathVariable String nome) { ... }
}
```

## 🔧 Configuração

### `application.yml`

```yaml
spring:
  application:
    name: clientes-service                              # Nome registrado no Eureka
  config:
    import: "optional:configserver:http://localhost:8888"
  datasource:
    url: jdbc:h2:mem:clientesdb;DB_CLOSE_DELAY=-1       # Banco em memória exclusivo
    driver-class-name: org.h2.Driver
    username: sa
    password:
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: create-drop
    show-sql: true
  h2:
    console:
      enabled: true
      path: /h2-console

server:
  port: 8082

eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/
  instance:
    preferIpAddress: true
```

**Console H2**: Acessível em `http://localhost:8082/h2-console` para inspecionar o banco.

## 📦 Dependências

| Dependência | Propósito |
|-------------|-----------|
| `spring-boot-starter-web` | REST API (Spring MVC) |
| `spring-boot-starter-data-jpa` | Persistência com JPA/Hibernate |
| `h2` | Banco de dados em memória |
| `spring-cloud-starter-netflix-eureka-client` | Registro no Eureka |
| `spring-cloud-starter-config` | Busca configurações do Config Server |
| `spring-boot-starter-actuator` | Endpoints de monitoramento |
| `spring-boot-starter-validation` | Bean Validation (`@Valid`, `@NotBlank`) |

## 🚀 Como Executar

### Pré-requisitos
1. **Config Server** rodando (:8888)
2. **Eureka Server** rodando (:8761)

### Localmente
```bash
cd clientes-service
mvn spring-boot:run
```

### Via Docker Compose
```bash
docker-compose up clientes-service
```

## 🔍 Verificação

```bash
# Health check
curl http://localhost:8082/actuator/health

# Cadastrar um cliente
curl -X POST http://localhost:8082/api/clientes \
  -H "Content-Type: application/json" \
  -d '{"cpf": "123.456.789-00", "nome": "João Silva"}'

# Listar clientes
curl http://localhost:8082/api/clientes

# Buscar por CPF
curl http://localhost:8082/api/clientes/cpf/123.456.789-00
```

## 🐳 Dockerfile

```dockerfile
FROM eclipse-temurin:17-jre-alpine
VOLUME /tmp
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```
