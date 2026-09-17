# 🔩 Peças Service

Microserviço de negócio do sistema **MicroManager**, responsável pelo gerenciamento do cadastro de peças.

## 📋 Informações Gerais

| Item | Valor |
|------|-------|
| **Módulo** | `pecas-service` |
| **Porta** | `8081` |
| **Banco de Dados** | H2 em memória (`pecasdb`) |
| **Padrão** | Microserviço com Database per Service |
| **Classe Principal** | `PecasServiceApplication.java` |
| **Base URL** (via Gateway) | `http://localhost:8080/api/pecas` |
| **Base URL** (direto) | `http://localhost:8081/api/pecas` |

## 🏗️ Papel na Arquitetura

O Peças Service é um dos 3 microserviços de negócio do sistema. Ele:
- Registra-se automaticamente no **Eureka Server** para ser descoberto pelo Gateway
- Busca configurações compartilhadas do **Config Server**
- Possui seu **próprio banco de dados** H2 em memória (padrão Database per Service)
- É acessado externamente **somente via API Gateway**

```
Cliente → API Gateway (:8080) → /api/pecas/** → Peças Service (:8081) → H2 pecasdb
```

## 📂 Estrutura de Arquivos

```
pecas-service/
├── Dockerfile
├── pom.xml
└── src/
    └── main/
        ├── java/com/microservicos/pecas/
        │   ├── PecasServiceApplication.java           ← Classe principal
        │   ├── model/
        │   │   └── Peca.java                          ← Entidade JPA
        │   ├── repository/
        │   │   └── PecaRepository.java                ← Repositório (Spring Data)
        │   ├── service/
        │   │   └── PecaService.java                   ← Camada de serviço
        │   └── controller/
        │       └── PecaController.java                ← REST Controller
        └── resources/
            └── application.yml                         ← Configurações
```

## 💾 Modelo de Dados

### Entidade `Peca`

| Campo | Tipo | Restrições | Descrição |
|-------|------|------------|-----------|
| `id` | `Long` | PK, auto-incremento | Identificador único |
| `nome` | `String` | NOT NULL, `@NotBlank` | Nome da peça |
| `descricao` | `String` | Max 500 chars, opcional | Descrição da peça |

```java
@Entity
@Table(name = "pecas")
public class Peca {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    @Column(nullable = false)
    private String nome;

    @Column(length = 500)
    private String descricao;
}
```

## 📡 API Endpoints

Todos os endpoints são acessíveis via API Gateway (`http://localhost:8080`) ou diretamente (`http://localhost:8081`).

### Cadastrar Peça

```
POST /api/pecas
```

**Request Body:**
```json
{
    "nome": "Parafuso M8",
    "descricao": "Parafuso de aço inox M8"
}
```

**Response** (`201 Created`):
```json
{
    "id": 1,
    "nome": "Parafuso M8",
    "descricao": "Parafuso de aço inox M8"
}
```

### Listar Todas as Peças

```
GET /api/pecas
```

**Response** (`200 OK`):
```json
[
    {
        "id": 1,
        "nome": "Parafuso M8",
        "descricao": "Parafuso de aço inox M8"
    },
    {
        "id": 2,
        "nome": "Porca Sextavada",
        "descricao": "Porca sextavada 3/8"
    }
]
```

### Buscar por ID

```
GET /api/pecas/{id}
```

**Response** (`200 OK`):
```json
{
    "id": 1,
    "nome": "Parafuso M8",
    "descricao": "Parafuso de aço inox M8"
}
```

**Response** (`404 Not Found`): quando a peça não existe.

### Buscar por Nome

```
GET /api/pecas/nome/{nome}
```

Busca case-insensitive por peças cujo nome contém o termo informado.

**Exemplo**: `GET /api/pecas/nome/parafuso`

**Response** (`200 OK`):
```json
[
    {
        "id": 1,
        "nome": "Parafuso M8",
        "descricao": "Parafuso de aço inox M8"
    }
]
```

**Response** (`404 Not Found`): quando nenhuma peça é encontrada.

## 🏛️ Arquitetura de Camadas

### Controller → Service → Repository

```
PecaController          PecaService          PecaRepository
@RestController         @Service             @Repository
     │                       │                      │
     │  cadastrar()          │  salvar()            │  save()
     │  listarTodas()        │  listarTodas()       │  findAll()
     │  buscarPorId()        │  buscarPorId()       │  findById()
     │  buscarPorNome()      │  buscarPorNome()     │  findByNomeContainingIgnoreCase()
     │                       │                      │
     ▼                       ▼                      ▼
  HTTP Request/Response   Business Logic         JPA/H2 Database
```

### Repository

```java
@Repository
public interface PecaRepository extends JpaRepository<Peca, Long> {
    List<Peca> findByNomeContainingIgnoreCase(String nome);
}
```

Utiliza **Spring Data JPA** com query method derivado automaticamente do nome do método.

### Service

```java
@Service
public class PecaService {
    private final PecaRepository repository;

    // Injeção de dependência via construtor
    public PecaService(PecaRepository repository) {
        this.repository = repository;
    }

    public Peca salvar(Peca peca) { return repository.save(peca); }
    public List<Peca> listarTodas() { return repository.findAll(); }
    public Optional<Peca> buscarPorId(Long id) { return repository.findById(id); }
    public List<Peca> buscarPorNome(String nome) { return repository.findByNomeContainingIgnoreCase(nome); }
}
```

### Controller

```java
@RestController
@RequestMapping("/api/pecas")
public class PecaController {
    private final PecaService service;

    @PostMapping
    public ResponseEntity<Peca> cadastrar(@Valid @RequestBody Peca peca) { ... }

    @GetMapping
    public ResponseEntity<List<Peca>> listarTodas() { ... }

    @GetMapping("/{id}")
    public ResponseEntity<Peca> buscarPorId(@PathVariable Long id) { ... }

    @GetMapping("/nome/{nome}")
    public ResponseEntity<List<Peca>> buscarPorNome(@PathVariable String nome) { ... }
}
```

- Usa `@Valid` para acionar a validação Bean Validation (`@NotBlank`) na criação.
- Retorna `ResponseEntity` com códigos HTTP apropriados (201, 200, 404).

## 🔧 Configuração

### `application.yml`

```yaml
spring:
  application:
    name: pecas-service                              # Nome registrado no Eureka
  config:
    import: "optional:configserver:http://localhost:8888"
  datasource:
    url: jdbc:h2:mem:pecasdb;DB_CLOSE_DELAY=-1       # Banco em memória
    driver-class-name: org.h2.Driver
    username: sa
    password:
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: create-drop                           # Recria tabelas ao iniciar
    show-sql: true
  h2:
    console:
      enabled: true
      path: /h2-console

server:
  port: 8081

eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/
  instance:
    preferIpAddress: true
```

**Console H2**: Acessível em `http://localhost:8081/h2-console` para inspecionar o banco em memória.

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
cd pecas-service
mvn spring-boot:run
```

### Via Docker Compose
```bash
docker-compose up pecas-service
```

## 🔍 Verificação

```bash
# Health check
curl http://localhost:8081/actuator/health

# Cadastrar uma peça
curl -X POST http://localhost:8081/api/pecas \
  -H "Content-Type: application/json" \
  -d '{"nome": "Parafuso M8", "descricao": "Parafuso de aço inox M8"}'

# Listar peças
curl http://localhost:8081/api/pecas
```

## 🐳 Dockerfile

```dockerfile
FROM eclipse-temurin:17-jre-alpine
VOLUME /tmp
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```
