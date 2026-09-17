# 🖥️ Frontend

Interface web do sistema **MicroManager**, construída com HTML5, CSS3 e JavaScript vanilla, servida via **Nginx** que atua como reverse proxy para o API Gateway.

## 📋 Informações Gerais

| Item | Valor |
|------|-------|
| **Módulo** | `frontend` |
| **Porta** | `3000` (Docker) / arquivo local no navegador |
| **Tecnologias** | HTML5 + CSS3 + JavaScript (vanilla) |
| **Servidor** | Nginx Alpine |
| **Proxy para** | API Gateway (:8080) |

## 🏗️ Papel na Arquitetura

O Frontend é a camada de apresentação do sistema. Ele **não acessa os microserviços diretamente** — todas as requisições passam pelo Nginx, que faz reverse proxy para o API Gateway.

```
  Navegador do Usuário
         │
         ▼ :3000 (Docker) ou arquivo local
  ┌──────────────────────┐
  │  Nginx               │
  │  ├── /           → HTML/CSS/JS estáticos     │
  │  ├── /api/       → proxy para Gateway :8080  │
  │  └── /actuator/  → proxy para Gateway :8080  │
  └──────────────────────┘
         │
         ▼
  API Gateway (:8080) → Microserviços
```

### Duas formas de usar:
1. **Via Docker Compose** (porta 3000) — Nginx serve os arquivos e faz proxy automático
2. **Abrindo `index.html` diretamente** — Neste caso, as requisições vão direto para `http://localhost:8080` (Gateway)

## 📂 Estrutura de Arquivos

```
frontend/
├── Dockerfile          ← Build da imagem Nginx
├── nginx.conf          ← Configuração do reverse proxy
├── index.html          ← Página principal (266 linhas)
├── style.css           ← Estilos (714 linhas, dark mode premium)
└── app.js              ← Lógica da aplicação (324 linhas)
```

## 🎨 Interface

### Funcionalidades

A interface é organizada em **3 abas** com navegação por tabs:

| Aba | Ícone | Funcionalidades |
|-----|-------|-----------------|
| **Peças** | 🔩 | Cadastrar peça (nome + descrição), buscar por ID, buscar por nome, listar todas |
| **Clientes** | 👥 | Cadastrar cliente (CPF + nome), buscar por CPF, buscar por nome, listar todos |
| **Representantes** | 💼 | Cadastrar representante (CPF + nome), buscar por CPF, buscar por nome, listar todos |

### Elementos da Interface

- **Header**: Logo animado, status do Gateway (online/offline com heartbeat a cada 15s), link para Eureka Dashboard
- **Tabs**: Navegação entre as 3 seções com animação de bounce no ícone
- **Cards**: Formulários de cadastro e busca em layout grid responsivo
- **Resultados**: Grid de cards com animação de slide-up ao aparecer
- **Toast**: Notificações de sucesso/erro/info no canto inferior direito
- **Loading**: Overlay com spinner durante requisições

### Design

| Aspecto | Implementação |
|---------|---------------|
| **Tema** | Dark mode com fundo `#0a0e1a` |
| **Tipografia** | Google Fonts — Inter (300-800) |
| **Efeitos** | Glassmorphism, gradientes, glow effects |
| **Animações** | Fade-in, slide-up, pulse, bounce, spin, hover effects |
| **Responsivo** | Media queries para mobile (< 640px) e tablet (< 900px) |
| **Scrollbar** | Custom scrollbar estilizada |

## 📄 Detalhes dos Arquivos

### `index.html`

Página single-page com 3 seções (tabs), cada uma contendo:
- Card de formulário para cadastro
- Card de busca com múltiplos filtros
- Card de resultados (inicialmente oculto)

Recursos:
- SEO: `<meta>` tags de charset, viewport e description
- Font preconnect para otimizar carregamento do Google Fonts
- IDs únicos em todos os elementos interativos

### `app.js`

| Função | Responsabilidade |
|--------|-----------------|
| `switchTab(tab)` | Navegação entre abas Peças/Clientes/Representantes |
| `checkGatewayStatus()` | Health check do Gateway via `/actuator/health` (a cada 15s) |
| `showToast(message, type)` | Notificações animadas (success, error, info) |
| `setLoading(show)` | Exibe/oculta overlay de loading |
| `apiRequest(url, options)` | Helper centralizado para chamadas HTTP com tratamento de erros |
| `renderItems(...)` | Renderiza lista de itens em cards HTML |
| `escapeHtml(text)` | Sanitização contra XSS |
| `cadastrarPeca(event)` | POST para `/api/pecas` |
| `listarPecas()` | GET para `/api/pecas` |
| `buscarPecaPorId()` | GET para `/api/pecas/{id}` |
| `buscarPecaPorNome()` | GET para `/api/pecas/nome/{nome}` |
| `cadastrarCliente(event)` | POST para `/api/clientes` |
| `listarClientes()` | GET para `/api/clientes` |
| `buscarClientePorCpf()` | GET para `/api/clientes/cpf/{cpf}` |
| `buscarClientePorNome()` | GET para `/api/clientes/nome/{nome}` |
| `cadastrarRepresentante(event)` | POST para `/api/representantes` |
| `listarRepresentantes()` | GET para `/api/representantes` |
| `buscarRepresentantePorCpf()` | GET para `/api/representantes/cpf/{cpf}` |
| `buscarRepresentantePorNome()` | GET para `/api/representantes/nome/{nome}` |

**Segurança**: Todas as strings renderizadas na página passam pela função `escapeHtml()` para prevenir ataques XSS (Cross-Site Scripting).

**Gateway URL**: A constante `GATEWAY` é definida como string vazia (`''`), fazendo com que as requisições sejam relativas à mesma origem — funciona tanto via Nginx (Docker) quanto abrindo o HTML diretamente (neste caso, requer o Gateway na porta 8080).

### `style.css`

CSS com sistema de design tokens via variáveis CSS:

```css
:root {
    --bg-primary: #0a0e1a;
    --color-primary: #6366f1;        /* Indigo */
    --color-secondary: #06b6d4;      /* Cyan */
    --color-accent: #f472b6;         /* Pink */
    --success: #10b981;              /* Emerald */
    --error: #ef4444;                /* Red */
    /* ... */
}
```

Animações definidas:
- `spin-slow` — Rotação do logo (10s)
- `pulse` — Pulsação do indicador de status
- `fadeIn` — Entrada das seções de tab
- `slideUp` — Entrada dos resultados
- `itemAppear` — Entrada individual de cada card de resultado
- `tab-bounce` — Bounce no ícone da tab ativa
- `spin` — Rotação do loading spinner

### `nginx.conf`

```nginx
server {
    listen 80;

    # Arquivos estáticos
    location / {
        root   /usr/share/nginx/html;
        index  index.html;
        try_files $uri $uri/ /index.html;
        add_header Cache-Control "no-cache, no-store, must-revalidate";
    }

    # Proxy para API Gateway
    location /api/ {
        proxy_pass http://api-gateway:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_connect_timeout 10s;
        proxy_read_timeout 30s;
    }

    # Proxy para Actuator
    location /actuator/ {
        proxy_pass http://api-gateway:8080;
        # ... mesmos headers
    }
}
```

O Nginx atua como **BFF (Backend for Frontend)**, eliminando problemas de CORS ao servir tudo pela mesma origem.

## 🚀 Como Executar

### Opção 1 — Abrindo o HTML diretamente
```bash
# Com todos os serviços rodando localmente
xdg-open frontend/index.html    # Linux
open frontend/index.html        # macOS
```
> Requer que o API Gateway esteja rodando em `http://localhost:8080`.

### Opção 2 — Via Docker Compose
```bash
docker-compose up frontend
# Acesse http://localhost:3000
```

## 🐳 Dockerfile

```dockerfile
FROM nginx:alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY index.html /usr/share/nginx/html/index.html
COPY style.css /usr/share/nginx/html/style.css
COPY app.js /usr/share/nginx/html/app.js
EXPOSE 80
```

Utiliza a imagem **Nginx Alpine** (~25MB) e copia os arquivos estáticos + configuração do proxy.

## 🔍 Verificação

1. Abra a interface no navegador
2. Verifique se o indicador **"Gateway Online"** aparece no header (bolinha verde pulsante)
3. Cadastre uma peça de teste para validar a comunicação com o backend
4. Navegue entre as abas para testar a interface
