# 06 — `.gitignore` e limpeza de artefatos de build

## Commit
- **Hash:** `d6b908b5f9f46841f541169ae1c6362f040a599d`
- **Autor:** leoo
- **Data:** 2026-09-21
- **Mensagem:** *chore: adiciona .gitignore e remove artefatos de build do rastreamento*

## O que foi feito

- Cria o arquivo `.gitignore` na raiz, cobrindo:
  - `target/` (build do Maven)
  - `.idea/`, `*.iml`, `.vscode/` (config de IDE)
  - `*.swp`, `.DS_Store`, `Thumbs.db` (arquivos de sistema/editor)
  - `*.log` (logs de execução)
- Remove do rastreamento do Git tudo que já estava versionado indevidamente dentro de `target/` em todos os módulos: `.jar` gerados (inclusive os `-1.0.0-SNAPSHOT.jar` de dezenas de MB cada), `.class` compilados, `application.yml` copiados para `target/classes`, metadados do `maven-archiver`, e os relatórios de teste (`surefire-reports`) que tinham entrado no [commit 04](04-correcao-docker-jdk-bytebuddy.md).

Importante: isso **remove os arquivos do rastreamento do Git**, não do disco — eles continuam existindo localmente, só deixam de ser versionados a partir de agora.

## Como ficou o projeto

O repositório para de acumular binários gerados a cada build. Antes deste commit, cada `mvn package` de cada módulo adicionava megabytes de `.jar` e dezenas de `.class`/relatórios ao histórico do Git a cada novo commit que alguém fizesse com `git add -A` — o que já tinha acontecido tanto no [commit 01](01-arquitetura-inicial.md) quanto no [commit 04](04-correcao-docker-jdk-bytebuddy.md). Daqui em diante, `target/` fica de fora por padrão, e commits futuros (como os testes de [clientes/representantes](05-testes-clientes-representantes.md), que na verdade foi commitado minutos antes deste) ficam restritos a código-fonte de verdade.

## Por que foi feito assim

- **`.gitignore` na raiz cobrindo todos os módulos**: como é um projeto Maven multi-módulo com `target/` repetido em cada subpasta, um único padrão `target/` na raiz já cobre todos eles — não precisa de um `.gitignore` por módulo.
- **`git rm --cached` (remoção do índice, não do disco)** em vez de apagar os arquivos: garante que ninguém perde o build local já feito, só para de versionar o que é reproduzível a qualquer momento com `mvn package`.
- Essa limpeza só foi possível depois que os problemas de build dos commits anteriores ([03](03-testes-pecas-e-documentacao.md) e [04](04-correcao-docker-jdk-bytebuddy.md)) já tinham sido resolvidos — faz sentido como o "commit de arrumação" que fecha essa sequência de trabalho em testes e infraestrutura.
