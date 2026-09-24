# 02 — Nginx como reverse proxy do frontend

## Commit
- **Hash:** `bbdc556a2e9ca1fd15984cfe50b00a2066168804`
- **Autor:** leoo
- **Data:** 2026-09-08
- **Mensagem:** *feat: configure Nginx as a reverse proxy for the frontend and update docker-compose services dependency structure*

## O que foi feito

- **`frontend/nginx.conf`** (novo arquivo) — configura o Nginx do container do frontend com duas responsabilidades:
  - Servir os arquivos estáticos (`index.html`, `style.css`, `app.js`) com cache desabilitado (`Cache-Control: no-cache, no-store, must-revalidate`), para sempre entregar a versão mais recente durante o desenvolvimento.
  - Fazer proxy reverso de `/api/` e `/actuator/` para `http://api-gateway:8080`, repassando headers padrão (`X-Real-IP`, `X-Forwarded-For`, `X-Forwarded-Proto`).
- **`frontend/Dockerfile`** — passa a copiar `nginx.conf` para `/etc/nginx/conf.d/default.conf` e cada arquivo estático individualmente (em vez de `COPY . /usr/share/nginx/html`, que copiava a pasta inteira, Dockerfile incluso).
- **`frontend/app.js`** — a constante `GATEWAY` deixa de ser `http://localhost:8080` e passa a ser `''` (string vazia): as chamadas `fetch` agora usam caminho relativo (`/api/pecas`, `/actuator/health`), que o Nginx do próprio container intercepta e repassa ao Gateway.
- **`docker-compose.yml`**:
  - Remove a declaração `version: '3.8'` (obsoleta nas versões recentes do Docker Compose).
  - Remove o `healthcheck` que existia no `config-server` desde o commit anterior.
  - Adiciona `pecas-service`, `clientes-service` e `representantes-service` ao `depends_on` do `api-gateway` (antes ele só dependia de `config-server` e `eureka-server`).

## Como ficou o projeto

O frontend deixa de falar diretamente com `localhost:8080` e passa a depender só do próprio Nginx, que decide para onde rotear internamente na rede do Docker Compose (`api-gateway:8080`). Isso é o que permite ao `docker-compose.yml` mapear o frontend numa porta diferente do Gateway (`3000:80`) sem quebrar CORS ou exigir que o navegador acesse duas origens diferentes — o navegador só enxerga uma origem (`localhost:3000`), e o Nginx esconde a topologia interna dos microsserviços.

## Por que foi feito assim

- **Proxy reverso em vez de CORS**: com `GATEWAY = ''` e o Nginx repassando `/api/`, o navegador nunca faz uma requisição cross-origin de verdade — tudo parece vir de `localhost:3000`. Isso é mais simples e mais parecido com um deploy real (onde o frontend normalmente é servido atrás do mesmo domínio/proxy que a API) do que depender de `allowedOrigins: "*"` no Gateway para sempre funcionar.
- **`depends_on` ampliado no Gateway**: como as rotas do Gateway são resolvidas via Eureka (`lb://`), fazia sentido explicitar que o Gateway só é realmente útil depois que os 3 serviços de negócio também estão de pé — ainda que `depends_on` no Compose garanta só a ordem de start dos containers, não que o serviço já esteja pronto (ver [spec 04](04-correcao-docker-jdk-bytebuddy.md) para os limites disso).
- **Remoção do healthcheck do config-server**: não há registro do motivo exato neste commit; possivelmente o `wget` usado no teste não estava disponível na imagem base naquele momento, ou o healthcheck estava causando reinícios prematuros do container antes do Config Server terminar de subir.
