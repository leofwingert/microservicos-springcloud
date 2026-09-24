# 04 — Correções de compatibilidade: Docker no Apple Silicon e Mockito no Java 25

## Commit
- **Hash:** `2130e8567c75d4c468c96fd2433daf6c2fd61e77`
- **Autor:** Enzo
- **Data:** 2026-09-16
- **Mensagem:** *Adição de relatórios de resultados de testes para PecaController, PecaRepository e PecaService*

## O que foi feito

A mensagem do commit descreve só uma parte do que entrou nele — na prática, três correções de ambiente local se misturaram com artefatos de build gerados pelo Maven:

**1. Import quebrado no teste do Controller**
`PecaControllerTest.java` importava `org.springframework.boot.test.mock.bean.MockBean` — um pacote que não existe no Spring Boot 3.3.4 (a versão fixada no `pom.xml` raiz). O pacote correto nessa versão é `org.springframework.boot.test.mock.mockito.MockBean`. Sem essa correção, o módulo `pecas-service` nem compilava.

**2. Base image do Docker sem suporte a arm64**
Os 6 Dockerfiles Java trocam `FROM eclipse-temurin:17-jre-alpine` por `FROM eclipse-temurin:17-jre`. A tag `17-jre-alpine` do eclipse-temurin nunca publicou build para `arm64` — só `amd64` — então `docker compose up --build` falhava com `no match for platform in manifest: not found` em qualquer Mac Apple Silicon. A tag `17-jre` (Debian, não Alpine) publica `arm64` nativamente.

**3. Byte Buddy desatualizado para JDKs novos**
O `pom.xml` raiz ganha uma entrada em `dependencyManagement` fixando `net.bytebuddy:byte-buddy` e `byte-buddy-agent` na versão `1.17.8`. O Mockito 5.11.0 (gerenciado pelo Spring Boot 3.3.4) traz transitivamente o Byte Buddy `1.14.19`, que só suporta oficialmente até o Java 23 — em qualquer JDK mais novo (Java 24/25), `@MockBean` falha em tempo de execução com `IllegalArgumentException: Java 25 (69) is not supported by the current version of Byte Buddy`.

**Ruído do commit:** por ter sido feito com `git add -A` num momento em que `target/` ainda não estava no `.gitignore` (isso só chega no [commit 06](06-gitignore-limpeza.md)), o commit também versionou `.class` compilados e relatórios XML/TXT do Surefire (`target/surefire-reports/...`) — artefatos de build que não deveriam estar no Git.

## Como ficou o projeto

Depois deste commit, `mvn test` volta a compilar e passar com qualquer JDK 17–25 instalado na máquina, e `docker compose up --build` volta a funcionar em Mac Apple Silicon — as duas plataformas mais comuns para quem está rodando o projeto localmente numa disciplina em 2026. Sem essas duas correções, o projeto simplesmente não builda nem sobe em boa parte das máquinas dos alunos.

## Por que foi feito assim

- **Fixar a versão do Byte Buddy via `dependencyManagement` no pom pai**, em vez de trocar a versão do Mockito ou do Spring Boot: é a mudança mínima que resolve o problema sem alterar a versão do Spring Boot (3.3.4) ou arriscar quebrar compatibilidade de outras dependências geridas pelo BOM do Spring Cloud.
- **Trocar para `17-jre` em vez de manter Alpine**: a imagem Alpine é menor, mas como o eclipse-temurin não publica Alpine multi-arquitetura para Java 17, a alternativa realista era usar a variante Debian (`17-jre`), que é maior mas roda em qualquer arquitetura.
- Os relatórios de teste e `.class` commitados por engano evidenciam por que o [commit 06](06-gitignore-limpeza.md), cinco dias depois, foi necessário.
