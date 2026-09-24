# 05 — Paridade de testes: clientes-service e representantes-service

## Commit
- **Hash:** `f91d28fb9227a07d2e4e282507165b0833f5042a`
- **Autor:** leoo
- **Data:** 2026-09-21
- **Mensagem:** *test: adiciona testes unitários para clientes-service e representantes-service*

## O que foi feito

Fecha o gap deixado pelo [commit 03](03-testes-pecas-e-documentacao.md): `clientes-service` e `representantes-service` ganham exatamente a mesma estrutura de testes que `pecas-service` já tinha, replicada 1:1 (trocando `Peca`/`id` por `Cliente`/`Representante` + `cpf`):

- `ClienteControllerTest` / `RepresentanteControllerTest` (`@WebMvcTest`, Service mockado) — 8 testes cada, cobrindo `POST` (sucesso e 400 com nome vazio), `GET` listar (com e sem resultados), `GET` por CPF (encontrado e 404) e `GET` por nome (encontrado e 404).
- `ClienteServiceTest` / `RepresentanteServiceTest` (Mockito puro, Repository mockado) — 7 testes cada, cobrindo `salvar`, `listarTodos`, `buscarPorCpf` e `buscarPorNome`, incluindo os casos de "não encontrado".
- `ClienteRepositoryTest` / `RepresentanteRepositoryTest` (`@DataJpaTest`, H2 real) — 8 testes cada, cobrindo `findAll`, `findById`, `save` e a busca por nome parcial/case-insensitive.

Nenhuma classe de produção é alterada neste commit — é puramente adição de testes sobre o código que já existia desde o [commit 01](01-arquitetura-inicial.md).

## Como ficou o projeto

Os 3 microsserviços de negócio passam a ter paridade total: 23 testes cada (69 no total), mesma cobertura de camadas, mesmo padrão de nomenclatura (`@DisplayName` descritivo em português, casos felizes + casos de borda). Antes deste commit, só `pecas-service` tinha essa rede de segurança — uma mudança em `ClienteService` ou `RepresentanteService` podia quebrar em produção sem nenhum teste para avisar. Depois dele, os 3 serviços têm o mesmo nível de confiança para refatoração.

## Por que foi feito assim

- **Replicar a estrutura do `pecas-service` em vez de desenhar algo novo**: os 3 serviços são estruturalmente idênticos (Controller → Service → Repository → Model), então a forma mais rápida e consistente de fechar o gap é copiar o padrão já validado, trocando os nomes de domínio — evita divergência de estilo entre os 3 módulos.
- **Sem mudar código de produção**: como o objetivo era só cobertura de teste, manter o commit restrito a `src/test/` deixa o histórico mais fácil de revisar — quem olhar este commit sabe que nenhum comportamento mudou, só a garantia sobre o comportamento existente.
