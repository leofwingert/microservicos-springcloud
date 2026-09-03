# MicroManager — Sistema de Microserviços com Spring Cloud

Sistema web com arquitetura de microserviços para gerenciamento de **Peças**, **Clientes** e **Representantes Comerciais**, implementando os padrões **Gateway**, **Service Discovery** e **Configuração Centralizada** com Spring Cloud.

## 🏗️ Arquitetura

```
Frontend (HTML/CSS/JS)
       │
       ▼ :8080
  API Gateway  ──── Service Discovery (Eureka :8761)
       │
  ┌────┴────────────────┐
  │             │        │
:8081         :8082    :8083
Peças       Clientes  Representantes
  │             │        │
  └────┬────────┘────────┘
       │
  Config Server (:8888)
```

## 📦 Módulos

| Módulo | Porta | Padrão |
|--------|-------|--------|
| `config-server` | 8888 | Configuração Centralizada |
| `eureka-server` | 8761 | Service Discovery |
| `api-gateway` | 8080 | Gateway |
| `pecas-service` | 8081 | Microserviço de Peças |
| `clientes-service` | 8082 | Microserviço de Clientes |
| `representantes-service` | 8083 | Microserviço de Representantes |

## 🚀 Como Executar

### Pré-requisitos
- Java 17+
- Maven 3.8+

### Opção 1 — Script automático
```bash
chmod +x start-all.sh stop-all.sh
./start-all.sh
```

### Opção 2 — Manual (na ordem abaixo!)

```bash
# 1. Config Server (primeiro!)
cd config-server && mvn spring-boot:run &

# 2. Eureka Server
cd ../eureka-server && mvn spring-boot:run &

# 3. Microserviços de negócio (em qualquer ordem)
cd ../pecas-service && mvn spring-boot:run &
cd ../clientes-service && mvn spring-boot:run &
cd ../representantes-service && mvn spring-boot:run &

# 4. API Gateway (por último)
cd ../api-gateway && mvn spring-boot:run &
```

> ⚠️ **Importante**: sempre inicie na ordem acima. O Config Server deve estar rodando antes dos demais.

## 🌐 Acessando o Sistema

| URL | Descrição |
|-----|-----------|
| `frontend/index.html` | Interface Web (abrir no navegador) |
| http://localhost:8080 | API Gateway |
| http://localhost:8761 | Eureka Dashboard |
| http://localhost:8888 | Config Server |

## 📋 API Endpoints (via Gateway :8080)

### Peças
```
POST   /api/pecas                    → Cadastrar peça
GET    /api/pecas                    → Listar todas as peças
GET    /api/pecas/{id}               → Buscar por ID
GET    /api/pecas/nome/{nome}        → Buscar por nome
```

### Clientes
```
POST   /api/clientes                 → Cadastrar cliente
GET    /api/clientes                 → Listar todos os clientes
GET    /api/clientes/cpf/{cpf}       → Buscar por CPF
GET    /api/clientes/nome/{nome}     → Buscar por nome
```

### Representantes
```
POST   /api/representantes           → Cadastrar representante
GET    /api/representantes           → Listar todos os representantes
GET    /api/representantes/cpf/{cpf} → Buscar por CPF
GET    /api/representantes/nome/{nome}→ Buscar por nome
```

## 📝 Exemplos de uso com curl

```bash
# Cadastrar uma peça
curl -X POST http://localhost:8080/api/pecas \
  -H "Content-Type: application/json" \
  -d '{"nome": "Parafuso M8", "descricao": "Parafuso de aço inox M8"}'

# Listar todas as peças
curl http://localhost:8080/api/pecas

# Cadastrar um cliente
curl -X POST http://localhost:8080/api/clientes \
  -H "Content-Type: application/json" \
  -d '{"cpf": "123.456.789-00", "nome": "João Silva"}'

# Cadastrar um representante
curl -X POST http://localhost:8080/api/representantes \
  -H "Content-Type: application/json" \
  -d '{"cpf": "987.654.321-00", "nome": "Maria Souza"}'
```

## 🔧 Stack Tecnológica

- **Java 17** + **Spring Boot 3.3.4**
- **Spring Cloud 2023.0.3**
  - Spring Cloud Config Server/Client
  - Spring Cloud Netflix Eureka
  - Spring Cloud Gateway
- **H2** (banco em memória por serviço)
- **Maven** (multi-módulo)
- **Frontend**: HTML5 + CSS3 + JavaScript
