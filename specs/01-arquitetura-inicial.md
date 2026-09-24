# 01 — Arquitetura inicial dos microsserviços

## Commit
- **Hash:** `7bc30cbd033fbb37ff87d39c24960b99ab9a7362`
- **Autor:** leoo
- **Data:** 2026-09-02
- **Mensagem:** *feat: implement Spring Cloud microservices architecture including configuration, eureka, gateway, and base services*

## O que foi feito

Este é o commit fundacional — o projeto inteiro nasce aqui, como um Maven multi-módulo com 6 módulos:

- **`config-server`** (porta 8888) — Spring Cloud Config Server em modo `native`, servindo configuração comum a partir de `classpath:/config-repo`.
- **`eureka-server`** (porta 8761) — Service Discovery via Netflix Eureka.
- **`api-gateway`** (porta 8080) — Spring Cloud Gateway, com uma rota por domínio (`Path=/api/pecas/**`, `/api/clientes/**`, `/api/representantes/**`) apontando para `lb://<nome-do-serviço>` (roteamento via Eureka, não IP fixo).
- **`pecas-service`** (porta 8081), **`clientes-service`** (porta 8082), **`representantes-service`** (porta 8083) — os 3 microsserviços de negócio, cada um com:
  - `Model` (entidade JPA — `Peca` com ID numérico gerado; `Cliente`/`Representante` com CPF como chave natural).
  - `Repository` (`JpaRepository` + um `findByNomeContainingIgnoreCase`).
  - `Service` (camada fina: cadastrar, listar, buscar por ID/CPF, buscar por nome).
  - `Controller` (`@RestController` com `POST` e `GET`, validação via `@Valid`/`@NotBlank`).
  - Banco H2 em memória próprio (`ddl-auto: create-drop`) — cada serviço é dono do seu dado.
- **`frontend`** — HTML/CSS/JS estático servido por Nginx, consumindo a API diretamente pelo Gateway (`http://localhost:8080`).
- **`docker-compose.yml`** — orquestra os 6 serviços + frontend, com `depends_on` encadeado (config-server → eureka-server → serviços de negócio → gateway) e um `healthcheck` no `config-server`.
- `Dockerfile` por serviço, todos usando `eclipse-temurin:17-jre-alpine` como base.

## Como ficou o projeto

Ao final deste commit, a arquitetura clássica de microsserviços com Spring Cloud já está completa:

```
Frontend → API Gateway (:8080) → Eureka (:8761) → [Peças | Clientes | Representantes]
                                                              ↑
                                                     Config Server (:8888)
```

Os 3 padrões centrais do curso já estão implementados desde o primeiro commit: **Service Discovery**, **API Gateway** e **Configuração Centralizada**. O código dos 3 serviços de negócio é deliberadamente idêntico em estrutura — só muda o nome do domínio — o que facilita manter os 3 em paridade nas etapas seguintes.

## Por que foi feito assim

- **Config Server em modo `native`** em vez de apontar para um repositório Git externo: simplifica o setup para rodar localmente/no Docker sem depender de uma URL Git externa configurada — comum em projetos de disciplina onde o objetivo é demonstrar o padrão, não operar um Config Server real em produção.
- **Roteamento via `lb://`** em vez de URLs fixas no Gateway: é o que de fato exercita o Service Discovery — se o Gateway apontasse direto para `http://pecas-service:8081`, o Eureka seria decorativo.
- **H2 em memória por serviço**: reforça o princípio de "database per service" da arquitetura de microsserviços, e evita a complexidade de configurar um banco externo só para os testes da disciplina.
