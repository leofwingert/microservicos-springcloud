# Specs — histórico do projeto

Esta pasta documenta a evolução do projeto commit a commit, na ordem em que aconteceu. Cada arquivo cobre um commit importante do histórico: o que foi feito, como o projeto ficou depois e por que a mudança foi necessária naquele momento.

O objetivo é servir de referência para entender **por que** o projeto está do jeito que está hoje, sem precisar reconstruir esse raciocínio a partir só do `git log`.

## Linha do tempo

| # | Commit | Data | O que marca |
|---|--------|------|--------------|
| [01](01-arquitetura-inicial.md) | [`7bc30cb`](https://github.com/leofwingert/microservicos-springcloud/commit/7bc30cbd033fbb37ff87d39c24960b99ab9a7362) | 2026-09-02 | Arquitetura inicial: Config Server, Eureka, Gateway e os 3 microsserviços de negócio |
| [02](02-nginx-reverse-proxy.md) | [`bbdc556`](https://github.com/leofwingert/microservicos-springcloud/commit/bbdc556a2e9ca1fd15984cfe50b00a2066168804) | 2026-09-08 | Nginx como reverse proxy do frontend |
| [03](03-testes-pecas-e-documentacao.md) | [`d38c5d7`](https://github.com/leofwingert/microservicos-springcloud/commit/d38c5d75062edc88f2cac24e57d343f8224ec825) | 2026-09-16 | Primeiros testes automatizados (pecas-service) + README por módulo |
| [04](04-correcao-docker-jdk-bytebuddy.md) | [`2130e85`](https://github.com/leofwingert/microservicos-springcloud/commit/2130e8567c75d4c468c96fd2433daf6c2fd61e77) | 2026-09-16 | Correções de compatibilidade: Docker no Apple Silicon e Mockito no Java 25 |
| [05](05-testes-clientes-representantes.md) | [`f91d28f`](https://github.com/leofwingert/microservicos-springcloud/commit/f91d28fb9227a07d2e4e282507165b0833f5042a) | 2026-09-21 | Paridade de testes: clientes-service e representantes-service |
| [06](06-gitignore-limpeza.md) | [`d6b908b`](https://github.com/leofwingert/microservicos-springcloud/commit/d6b908b5f9f46841f541169ae1c6362f040a599d) | 2026-09-21 | `.gitignore` e remoção de artefatos de build do versionamento |
| [07](07-crud-completo-testes-integracao-mutacao.md) | [`01ef59d`](https://github.com/leofwingert/microservicos-springcloud/commit/01ef59d42769969d4bfa44d341ee16eaf36bd40f) | 2026-09-23 | CRUD completo (PUT/DELETE), testes de integração fim a fim e teste de mutação |

## Como ler

Cada arquivo segue a mesma estrutura:

- **Commit** — hash, autor, data e mensagem original.
- **O que foi feito** — as mudanças concretas (arquivos, código, config).
- **Como ficou o projeto** — o estado resultante, e como essa etapa se encaixa nas anteriores/seguintes.
- **Por que foi feito assim** — o raciocínio ou problema que motivou a mudança, quando é possível inferir do próprio código/contexto.
