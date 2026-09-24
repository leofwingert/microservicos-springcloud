# 07 — CRUD completo, testes de integração fim a fim e teste de mutação

## Commit
- **Hash:** `01ef59d42769969d4bfa44d341ee16eaf36bd40f`
- **Autor:** Enzo
- **Data:** 2026-09-23
- **Mensagem:** *feat: add integration tests for microservices architecture with Docker*

## O que foi feito

Este commit reúne três frentes de trabalho sobre os 3 microsserviços de negócio:

**1. CRUD completo (`PUT`/`DELETE`)**
Até aqui a API só permitia criar (`POST`) e consultar (`GET`) — não havia como editar ou remover um registro. `PecaController`/`ClienteController`/`RepresentanteController` ganham:
- `PUT /{id}` (ou `/{cpf}`) — atualiza um registro existente; retorna `200` com o registro atualizado, ou `404` se o ID/CPF não existir.
- `DELETE /{id}` (ou `/{cpf}`) — remove um registro; retorna `204 No Content`, ou `404` se não existir.

Cada `Service` ganha os métodos correspondentes, seguindo o padrão já estabelecido no projeto: `atualizar(id, dadosAtualizados)` retorna `Optional<T>` (vazio se o registro não existe, sem lançar exceção), e `deletar(id)` retorna `boolean` (verifica `existsById` antes de deletar, para poder diferenciar "removido" de "não existia"). Como as rotas do Gateway usam `Path=/api/{recurso}/**` sem filtro de método HTTP, `PUT`/`DELETE` já passam a funcionar através do Gateway sem precisar tocar em `api-gateway`.

Os testes unitários (`Controller` e `Service`) de cada serviço ganham os casos correspondentes: atualização com sucesso, atualização de registro inexistente (404), atualização com dado inválido (400), remoção com sucesso (204) e remoção de registro inexistente (404) — subindo de 23 para 32 testes por serviço (96 no total).

**2. Módulo `integration-tests` (testes de integração fim a fim)**
Um módulo Maven novo, registrado no `pom.xml` raiz, com testes de **caixa-preta**: sobem a stack real via `docker compose up --build -d` e validam o sistema através do Gateway de verdade — nenhuma camada é mockada, ao contrário dos testes unitários dos outros módulos.

- `AbstractGatewayIT` — classe base com um cliente HTTP (`java.net.http.HttpClient`) e um `@BeforeAll` que só libera os testes quando a stack está genuinamente pronta: não basta o `/actuator/health` de cada serviço responder `UP` (isso só confirma que a JVM subiu) — o `@BeforeAll` também confirma que os 4 serviços aparecem registrados em `/eureka/apps` **e** que o Gateway já consegue rotear de fato para os 3 domínios (sem `503`), porque o cache do `DiscoveryClient` que o Gateway usa para resolver `lb://` demora alguns segundos a mais para se popular depois do registro no Eureka. Se a stack não estiver no ar, os testes são pulados (`Assumptions.assumeTrue`) com uma mensagem explicando como subi-la, em vez de falhar.
- `PecaGatewayIT`, `ClienteGatewayIT`, `RepresentanteGatewayIT` — fluxo completo por domínio: `POST` → `GET` por ID/CPF → `GET` por nome → `GET` listar → `PUT` → `DELETE` → `GET` confirmando `404` depois de removido. Cada um também cobre `400` (validação) e `404` (registro inexistente) para `PUT`/`DELETE`.
- `EurekaDiscoveryIT` — confirma que `api-gateway`, `pecas-service`, `clientes-service` e `representantes-service` aparecem registrados e `UP` no Eureka.

O módulo é configurado para **não** rodar com `mvn test` (Surefire desabilitado nele) nem afetar `mvn package` (o `spring-boot-maven-plugin` herdado do pom pai é desativado, já que este módulo não é uma aplicação Spring Boot). Ele só roda com `mvn verify`, via `maven-failsafe-plugin`, que segue a convenção de nome `*IT` para diferenciar testes de integração dos testes unitários (`*Test`).

**3. Teste de mutação (Pitest)**
`pecas-service`, `clientes-service` e `representantes-service` ganham o plugin `pitest-maven` configurado (com `pitest-junit5-plugin` para reconhecer os testes JUnit 5), mirando as classes de `service` e `controller` de cada um. Assim como o módulo de integração, não roda automaticamente — só sob demanda com `mvn org.pitest:pitest-maven:mutationCoverage`. Resultado obtido ao configurar: **100% de mutation score nos 3 serviços** (21–23 mutantes gerados, todos mortos pelos testes existentes).

**4. Documentação**
`README.md` raiz ganha a lista atualizada de endpoints (`PUT`/`DELETE` nos 3 domínios) e uma seção nova explicando as três camadas de teste e como rodar cada uma. A pasta `specs/` (este arquivo incluso) também entra neste commit.

## Como ficou o projeto

A API deixa de ser só create-and-read e passa a ter CRUD completo nos 3 domínios. O projeto passa a ter as três camadas de teste que a disciplina pede, cada uma com um objetivo diferente e nenhuma sobrepondo as outras:

| Camada | O que verifica | Como roda | Isolamento |
|---|---|---|---|
| Unitário | Lógica de cada classe | `mvn test` | Camadas vizinhas mockadas |
| Mutação | Se os testes unitários pegam bugs de verdade | `mvn pitest:mutationCoverage` | Roda os testes unitários contra código propositalmente quebrado |
| Integração (E2E) | O sistema real, com Gateway/Eureka/Config de verdade | `docker compose up` + `mvn verify` | Nada mockado — é o sistema rodando |

## Por que foi feito assim

- **`Optional<T>` no `atualizar` e `boolean` no `deletar`** em vez de lançar exceção: segue exatamente o padrão que `buscarPorId`/`buscarPorCpf` já usavam desde o [commit 01](01-arquitetura-inicial.md) — o Controller decide o código HTTP (`200`/`404`) a partir do retorno, sem precisar de `try/catch` nem de um `@ControllerAdvice` que o projeto não tem.
- **Testes de integração num módulo Maven separado, não dentro de cada serviço**: como esses testes validam o sistema inteiro através do Gateway (não um serviço isolado), colocá-los num módulo à parte evita a pergunta "de qual serviço é esse teste?" e deixa claro que a dependência deles é a stack Docker completa, não só o classpath de um serviço.
- **Checagem de prontidão em 3 camadas (health → Eureka → roteamento do Gateway)**: a primeira versão do `@BeforeAll` checava só `/actuator/health`, e os testes falhavam de forma inconsistente com `503` porque o Gateway ainda não tinha atualizado seu cache de instâncias — extrair essa lógica caso a caso teria escondido um problema real de *timing* entre serviços, então a espera passou a verificar exatamente a condição que os testes precisam (`GET` através do Gateway sem `503`).
- **Pitest restrito a `service` e `controller`, sem incluir `repository`**: as interfaces `JpaRepository` não têm lógica própria para mutar além do método de busca customizado — incluí-las só infla o relatório sem agregar sinal sobre a qualidade dos testes.
- **Mutation coverage e testes de integração fora de `mvn test`/`mvn verify` padrão**: ambos são mais lentos que os testes unitários (o Pitest recompila e roda a suíte várias vezes; a integração depende de subir containers Docker) — deixá-los como comandos separados mantém o ciclo rápido de `mvn test` para o dia a dia, sem tirar a possibilidade de rodá-los quando fizer sentido (antes de um PR, por exemplo).
