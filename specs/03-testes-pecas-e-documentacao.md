# 03 — Primeiros testes automatizados e documentação por módulo

## Commit
- **Hash:** `d38c5d75062edc88f2cac24e57d343f8224ec825`
- **Autor:** leoo
- **Data:** 2026-09-16
- **Mensagem:** *a*

## O que foi feito

Apesar da mensagem de commit não descrever nada, este commit traz duas frentes de trabalho:

**1. Testes automatizados para `pecas-service`** (o primeiro serviço a ganhar testes):
- `pom.xml` do `pecas-service`, `clientes-service` e `representantes-service` ganham a dependência `spring-boot-starter-test` — mas só `pecas-service` recebe classes de teste de fato.
- `PecaControllerTest` (`@WebMvcTest`) — testa a camada web com o `PecaService` mockado via `@MockBean`, sem banco nem servidor real.
- `PecaServiceTest` (`@ExtendWith(MockitoExtension.class)`) — testa a lógica do Service com o `PecaRepository` mockado via Mockito puro.
- `PecaRepositoryTest` (`@DataJpaTest`) — testa a camada de persistência com H2 real em memória, usando `TestEntityManager` para popular dados independentemente do repository sob teste.
- `src/test/resources/application.yml` nos 3 serviços — desabilita Eureka (`eureka.client.enabled: false`) e o Config Server (`spring.cloud.config.enabled: false`) durante os testes, para que eles rodem isolados de qualquer infraestrutura externa.

**2. Documentação por módulo:**
- Um `README.md` dedicado para `config-server`, `eureka-server`, `api-gateway`, `pecas-service`, `clientes-service`, `representantes-service` e `frontend` — bem mais detalhados (175 a 329 linhas cada) que o README raiz, cobrindo especificamente o papel e a configuração de cada módulo.

## Como ficou o projeto

Pela primeira vez o projeto tem uma suíte de testes de verdade, e ela já nasce seguindo a pirâmide de testes correta para uma aplicação Spring Boot: teste de unidade isolado no Service, teste de camada web isolado no Controller, e teste de integração restrito à camada de persistência no Repository — cada um usando a ferramenta certa do Spring Test para não precisar subir o contexto inteiro à toa.

O gap fica evidente: `clientes-service` e `representantes-service` ganham a dependência de teste no `pom.xml`, mas ficam sem nenhuma classe de teste — essa paridade só chega no [commit 05](05-testes-clientes-representantes.md), cinco dias depois.

## Por que foi feito assim

- **Três tipos de teste em vez de um `@SpringBootTest` único**: `@WebMvcTest` e `@DataJpaTest` sobem só uma fatia do contexto Spring (bem mais rápido) e isolam a causa de uma falha — se `PecaRepositoryTest` quebra, o problema é na camada de persistência; se `PecaControllerTest` quebra, é na camada web. Um `@SpringBootTest` monolítico teria testes mais lentos e falhas mais difíceis de diagnosticar.
- **Desabilitar Eureka/Config nos testes**: sem isso, cada teste tentaria se registrar num Eureka Server e buscar configuração de um Config Server que não existem durante `mvn test`, fazendo a suíte falhar ou travar esperando timeout de conexão.
