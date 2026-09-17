# 💼 Representantes Service

Microserviço de negócio do sistema **MicroManager**, responsável pelo gerenciamento do cadastro de representantes comerciais.

## 📋 Informações Gerais

| Item | Valor |
|------|-------|
| **Módulo** | `representantes-service` |
| **Porta** | `8083` |
| **Banco de Dados** | H2 em memória (`representantesdb`) |
| **Padrão** | Microserviço com Database per Service |
| **Classe Principal** | `RepresentantesServiceApplication.java` |
| **Base URL** (via Gateway) | `http://localhost:8080/api/representantes` |
| **Base URL** (direto) | `http://localhost:8083/api/representantes` |

## 🏗️ Papel na Arquitetura

O Representantes Service é um dos 3 microserviços de negócio do sistema. Ele:
- Registra-se automaticamente no **Eureka Server** para ser descoberto pelo Gateway
- Busca configurações compartilhadas do **Config Server**
- Possui seu **próprio banco de dados** H2 em memória (padrão Database per Service)
- É acessado externamente **somente via API Gateway**

```
Cliente → API Gateway (:8080) → /api/representantes/** → Representantes Service (:8083) → H2 representantesdb
```

## 📂 Estrutura de Arquivos

```
representantes-service/
├── Dockerfile
├── pom.xml
└── src/
    └── main/
        ├── java/com/microservicos/representantes/
        │   ├── RepresentantesServiceApplication.java  ← Classe principal
        │   ├── model/
        │   │   └── Representante.java                 ← Entidade JPA
        │   ├── repository/
        │   │   └── RepresentanteRepository.java       ← Repositório (Spring Data)
        │   ├── service/
        │   │   └── RepresentanteService.java          ← Camada de serviço
        │   └── controller/
        │       └── RepresentanteController.java       ← REST Controller
        └── resources/
            └── application.yml                         ← Configurações
```

## 💾 Modelo de Dados

### Entidade `Representante`

| Campo | Tipo | Restrições | Descrição |
|-------|------|------------|-----------|
| `cpf` | `String` | PK, UNIQUE, NOT NULL | CPF do representante (chave primária) |
| `nome` | `String` | NOT NULL, `@NotBlank` | Nome completo do representante |

```java
@Entity
@Table(name = "representantes")
public class Representante {
    @Id
    @Column(nullable = false, unique = true)
    private String cpf;             // CPF como chave primária

    @NotBlank(message = "Nome é obrigatório")
    @Column(nullable = false)
    private String nome;
}
```

> **Nota**: Assim como o Clientes Service, utiliza o **CPF como chave primária natural** (tipo `String`), diferente do Peças Service que usa `Long id` auto-incremento.

## 📡 API Endpoints

Todos os endpoints são acessíveis via API Gateway (`http://localhost:8080`) ou diretamente (`http://localhost:8083`).

### Cadastrar Representante

```
POST /api/representantes
```

**Request Body:**
```json
{
    "cpf": "987.654.321-00",
    "nome": "Maria Souza"
}
```

**Response** (`201 Created`):
```json
{
    "cpf": "987.654.321-00",
    "nome": "Maria Souza"
}
```

### Listar Todos os Representantes

```
GET /api/representantes
```

**Response** (`200 OK`):
```json
[
    {
        "cpf": "987.654.321-00",
        "nome": "Maria Souza"
    },
    {
        "cpf": "111.222.333-44",
        "nome": "Carlos Pereira"
    }
]
```

### Buscar por CPF

```
GET /api/representantes/cpf/{cpf}
```

**Exemplo**: `GET /api/representantes/cpf/987.654.321-00`

**Response** (`200 OK`):
```json
{
    "cpf": "987.654.321-00",
    "nome": "Maria Souza"
}
```

**Response** (`404 Not Found`): quando o CPF não é encontrado.

### Buscar por Nome

```
GET /api/representantes/nome/{nome}
```

Busca case-insensitive por representantes cujo nome contém o termo informado.

**Exemplo**: `GET /api/representantes/nome/maria`

**Response** (`200 OK`):
```json
[
    {
        "cpf": "987.654.321-00",
        "nome": "Maria Souza"
    }
]
```

**Response** (`404 Not Found`): quando nenhum representante é encontrado.

## 🏛️ Arquitetura de Camadas

### Controller → Service → Repository

```
RepresentanteController   RepresentanteService   RepresentanteRepository
@RestController            @Service               @Repository
     │                          │                        │
     │  cadastrar()             │  salvar()              │  save()
     │  listarTodos()           │  listarTodos()         │  findAll()
     │  buscarPorCpf()          │  buscarPorCpf()        │  findById()
     │  buscarPorNome()         │  buscarPorNome()       │  findByNomeContainingIgnoreCase()
     │                          │                        │
     ▼                          ▼                        ▼
  HTTP Request/Response      Business Logic           JPA/H2 Database
```

### Repository

```java
@Repository
public interface RepresentanteRepository extends JpaRepository<Representante, String> {
    List<Representante> findByNomeContainingIgnoreCase(String nome);
}
```

- `JpaRepository<Representante, String>` — PK do tipo `String` (CPF).
- Query method derivado pelo Spring Data para busca parcial por nome.

### Service

```java
@Service
public class RepresentanteService {
    private final RepresentanteRepository repository;

    public RepresentanteService(RepresentanteRepository repository) {
        this.repository = repository;
    }

    public Representante salvar(Representante representante) { return repository.save(representante); }
    public List<Representante> listarTodos() { return repository.findAll(); }
    public Optional<Representante> buscarPorCpf(String cpf) { return repository.findById(cpf); }
    public List<Representante> buscarPorNome(String nome) { return repository.findByNomeContainingIgnoreCase(nome); }
}
```

### Controller

```java
@RestController
@RequestMapping("/api/representantes")
public class RepresentanteController {
    private final RepresentanteService service;

    @PostMapping
    public ResponseEntity<Representante> cadastrar(@Valid @RequestBody Representante representante) { ... }

    @GetMapping
    public ResponseEntity<List<Representante>> listarTodos() { ... }

    @GetMapping("/cpf/{cpf}")
    public ResponseEntity<Representante> buscarPorCpf(@PathVariable String cpf) { ... }

    @GetMapping("/nome/{nome}")
    public ResponseEntity<List<Representante>> buscarPorNome(@PathVariable String nome) { ... }
}
```

## 🔧 Configuração

### `application.yml`

```yaml
spring:
  application:
    name: representantes-service                              # Nome registrado no Eureka
  config:
    import: "optional:configserver:http://localhost:8888"
  datasource:
    url: jdbc:h2:mem:representantesdb;DB_CLOSE_DELAY=-1       # Banco em memória exclusivo
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
  port: 8083

eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/
  instance:
    preferIpAddress: true
```

**Console H2**: Acessível em `http://localhost:8083/h2-console` para inspecionar o banco.

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
cd representantes-service
mvn spring-boot:run
```

### Via Docker Compose
```bash
docker-compose up representantes-service
```

## 🔍 Verificação

```bash
# Health check
curl http://localhost:8083/actuator/health

# Cadastrar um representante
curl -X POST http://localhost:8083/api/representantes \
  -H "Content-Type: application/json" \
  -d '{"cpf": "987.654.321-00", "nome": "Maria Souza"}'

# Listar representantes
curl http://localhost:8083/api/representantes

# Buscar por CPF
curl http://localhost:8083/api/representantes/cpf/987.654.321-00

# Buscar por nome
curl http://localhost:8083/api/representantes/nome/maria
```

## 🐳 Dockerfile

```dockerfile
FROM eclipse-temurin:17-jre-alpine
VOLUME /tmp
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```
