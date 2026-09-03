/* ===================================================
   MicroManager — JavaScript
   Gateway: http://localhost:8080
   =================================================== */

const GATEWAY = 'http://localhost:8080';

// ---- Tab Navigation ----
function switchTab(tab) {
    document.querySelectorAll('.tab-section').forEach(s => s.classList.remove('active'));
    document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));

    document.getElementById(`section-${tab}`).classList.add('active');
    document.getElementById(`tab-${tab}`).classList.add('active');
}

// ---- Gateway Status Check ----
async function checkGatewayStatus() {
    const dot = document.getElementById('gateway-status');
    const label = document.getElementById('gateway-label');
    try {
        const res = await fetch(`${GATEWAY}/actuator/health`, { signal: AbortSignal.timeout(3000) });
        if (res.ok) {
            dot.className = 'status-dot online';
            label.textContent = 'Gateway Online (:8080)';
        } else {
            throw new Error();
        }
    } catch {
        dot.className = 'status-dot offline';
        label.textContent = 'Gateway Offline';
    }
}

// ---- Toast Notifications ----
function showToast(message, type = 'info') {
    const toast = document.getElementById('toast');
    toast.textContent = message;
    toast.className = `toast ${type} show`;
    setTimeout(() => { toast.className = 'toast'; }, 3500);
}

// ---- Loading ----
function setLoading(show) {
    const overlay = document.getElementById('loading-overlay');
    overlay.classList.toggle('active', show);
}

// ---- HTTP Helper ----
async function apiRequest(url, options = {}) {
    setLoading(true);
    try {
        const response = await fetch(url, {
            ...options,
            headers: { 'Content-Type': 'application/json', ...options.headers }
        });
        const text = await response.text();
        const data = text ? JSON.parse(text) : null;
        if (!response.ok) {
            const msg = data?.message || `Erro HTTP ${response.status}`;
            throw new Error(msg);
        }
        return data;
    } finally {
        setLoading(false);
    }
}

// ---- Render Helpers ----
function renderItems(containerId, resultsId, items, renderFn) {
    const container = document.getElementById(containerId);
    const results = document.getElementById(resultsId);
    results.style.display = 'block';

    if (!items || (Array.isArray(items) && items.length === 0)) {
        container.innerHTML = `
            <div class="empty-state">
                <div class="empty-state-icon">🔍</div>
                <p>Nenhum resultado encontrado.</p>
            </div>`;
        return;
    }

    const list = Array.isArray(items) ? items : [items];
    container.innerHTML = list.map(renderFn).join('');
}

function fecharResultados(section) {
    document.getElementById(`results-${section}`).style.display = 'none';
}

// ================================================================
// ========================== PEÇAS ===============================
// ================================================================

async function cadastrarPeca(event) {
    event.preventDefault();
    const nome = document.getElementById('peca-nome').value.trim();
    const descricao = document.getElementById('peca-descricao').value.trim();

    if (!nome) { showToast('Nome da peça é obrigatório!', 'error'); return; }

    try {
        const btn = document.getElementById('btn-cadastrar-peca');
        btn.disabled = true;
        const peca = await apiRequest(`${GATEWAY}/api/pecas`, {
            method: 'POST',
            body: JSON.stringify({ nome, descricao })
        });
        showToast(`✅ Peça "${peca.nome}" cadastrada! ID: ${peca.id}`, 'success');
        document.getElementById('form-peca').reset();
    } catch (err) {
        showToast(`❌ Erro: ${err.message}`, 'error');
    } finally {
        document.getElementById('btn-cadastrar-peca').disabled = false;
    }
}

async function listarPecas() {
    try {
        const pecas = await apiRequest(`${GATEWAY}/api/pecas`);
        renderItems('pecas-list', 'results-pecas', pecas, renderPeca);
        showToast(`📋 ${pecas.length} peça(s) encontrada(s)`, 'info');
    } catch (err) {
        showToast(`❌ ${err.message}`, 'error');
    }
}

async function buscarPecaPorId() {
    const id = document.getElementById('peca-search-id').value.trim();
    if (!id) { showToast('Informe o ID da peça!', 'error'); return; }
    try {
        const peca = await apiRequest(`${GATEWAY}/api/pecas/${id}`);
        renderItems('pecas-list', 'results-pecas', peca, renderPeca);
    } catch (err) {
        showToast(`❌ ${err.message}`, 'error');
        renderItems('pecas-list', 'results-pecas', [], renderPeca);
    }
}

async function buscarPecaPorNome() {
    const nome = document.getElementById('peca-search-nome').value.trim();
    if (!nome) { showToast('Informe o nome da peça!', 'error'); return; }
    try {
        const pecas = await apiRequest(`${GATEWAY}/api/pecas/nome/${encodeURIComponent(nome)}`);
        renderItems('pecas-list', 'results-pecas', pecas, renderPeca);
        if (pecas.length > 0) showToast(`🔩 ${pecas.length} peça(s) encontrada(s)`, 'info');
    } catch (err) {
        showToast(`❌ ${err.message}`, 'error');
        renderItems('pecas-list', 'results-pecas', [], renderPeca);
    }
}

function renderPeca(peca) {
    return `
        <div class="item-card">
            <div class="item-info">
                <div class="item-title">🔩 ${escapeHtml(peca.nome)}</div>
                <div class="item-subtitle">${peca.descricao ? escapeHtml(peca.descricao) : '<em>Sem descrição</em>'}</div>
            </div>
            <div class="item-badge">#${peca.id}</div>
        </div>`;
}

// ================================================================
// ======================== CLIENTES ==============================
// ================================================================

async function cadastrarCliente(event) {
    event.preventDefault();
    const cpf = document.getElementById('cliente-cpf').value.trim();
    const nome = document.getElementById('cliente-nome').value.trim();

    if (!cpf || !nome) { showToast('CPF e Nome são obrigatórios!', 'error'); return; }

    try {
        const btn = document.getElementById('btn-cadastrar-cliente');
        btn.disabled = true;
        const cliente = await apiRequest(`${GATEWAY}/api/clientes`, {
            method: 'POST',
            body: JSON.stringify({ cpf, nome })
        });
        showToast(`✅ Cliente "${cliente.nome}" cadastrado!`, 'success');
        document.getElementById('form-cliente').reset();
    } catch (err) {
        showToast(`❌ Erro: ${err.message}`, 'error');
    } finally {
        document.getElementById('btn-cadastrar-cliente').disabled = false;
    }
}

async function listarClientes() {
    try {
        const clientes = await apiRequest(`${GATEWAY}/api/clientes`);
        renderItems('clientes-list', 'results-clientes', clientes, renderCliente);
        showToast(`📋 ${clientes.length} cliente(s) encontrado(s)`, 'info');
    } catch (err) {
        showToast(`❌ ${err.message}`, 'error');
    }
}

async function buscarClientePorCpf() {
    const cpf = document.getElementById('cliente-search-cpf').value.trim();
    if (!cpf) { showToast('Informe o CPF!', 'error'); return; }
    try {
        const cliente = await apiRequest(`${GATEWAY}/api/clientes/cpf/${encodeURIComponent(cpf)}`);
        renderItems('clientes-list', 'results-clientes', cliente, renderCliente);
    } catch (err) {
        showToast(`❌ ${err.message}`, 'error');
        renderItems('clientes-list', 'results-clientes', [], renderCliente);
    }
}

async function buscarClientePorNome() {
    const nome = document.getElementById('cliente-search-nome').value.trim();
    if (!nome) { showToast('Informe o nome!', 'error'); return; }
    try {
        const clientes = await apiRequest(`${GATEWAY}/api/clientes/nome/${encodeURIComponent(nome)}`);
        renderItems('clientes-list', 'results-clientes', clientes, renderCliente);
        if (clientes.length > 0) showToast(`👥 ${clientes.length} cliente(s) encontrado(s)`, 'info');
    } catch (err) {
        showToast(`❌ ${err.message}`, 'error');
        renderItems('clientes-list', 'results-clientes', [], renderCliente);
    }
}

function renderCliente(cliente) {
    return `
        <div class="item-card">
            <div class="item-info">
                <div class="item-title">👤 ${escapeHtml(cliente.nome)}</div>
                <div class="item-subtitle">CPF: ${escapeHtml(cliente.cpf)}</div>
            </div>
            <div class="item-badge cpf">${escapeHtml(cliente.cpf)}</div>
        </div>`;
}

// ================================================================
// ==================== REPRESENTANTES ============================
// ================================================================

async function cadastrarRepresentante(event) {
    event.preventDefault();
    const cpf = document.getElementById('rep-cpf').value.trim();
    const nome = document.getElementById('rep-nome').value.trim();

    if (!cpf || !nome) { showToast('CPF e Nome são obrigatórios!', 'error'); return; }

    try {
        const btn = document.getElementById('btn-cadastrar-rep');
        btn.disabled = true;
        const rep = await apiRequest(`${GATEWAY}/api/representantes`, {
            method: 'POST',
            body: JSON.stringify({ cpf, nome })
        });
        showToast(`✅ Representante "${rep.nome}" cadastrado!`, 'success');
        document.getElementById('form-representante').reset();
    } catch (err) {
        showToast(`❌ Erro: ${err.message}`, 'error');
    } finally {
        document.getElementById('btn-cadastrar-rep').disabled = false;
    }
}

async function listarRepresentantes() {
    try {
        const reps = await apiRequest(`${GATEWAY}/api/representantes`);
        renderItems('representantes-list', 'results-representantes', reps, renderRepresentante);
        showToast(`📋 ${reps.length} representante(s) encontrado(s)`, 'info');
    } catch (err) {
        showToast(`❌ ${err.message}`, 'error');
    }
}

async function buscarRepresentantePorCpf() {
    const cpf = document.getElementById('rep-search-cpf').value.trim();
    if (!cpf) { showToast('Informe o CPF!', 'error'); return; }
    try {
        const rep = await apiRequest(`${GATEWAY}/api/representantes/cpf/${encodeURIComponent(cpf)}`);
        renderItems('representantes-list', 'results-representantes', rep, renderRepresentante);
    } catch (err) {
        showToast(`❌ ${err.message}`, 'error');
        renderItems('representantes-list', 'results-representantes', [], renderRepresentante);
    }
}

async function buscarRepresentantePorNome() {
    const nome = document.getElementById('rep-search-nome').value.trim();
    if (!nome) { showToast('Informe o nome!', 'error'); return; }
    try {
        const reps = await apiRequest(`${GATEWAY}/api/representantes/nome/${encodeURIComponent(nome)}`);
        renderItems('representantes-list', 'results-representantes', reps, renderRepresentante);
        if (reps.length > 0) showToast(`💼 ${reps.length} representante(s) encontrado(s)`, 'info');
    } catch (err) {
        showToast(`❌ ${err.message}`, 'error');
        renderItems('representantes-list', 'results-representantes', [], renderRepresentante);
    }
}

function renderRepresentante(rep) {
    return `
        <div class="item-card">
            <div class="item-info">
                <div class="item-title">💼 ${escapeHtml(rep.nome)}</div>
                <div class="item-subtitle">CPF: ${escapeHtml(rep.cpf)}</div>
            </div>
            <div class="item-badge cpf">${escapeHtml(rep.cpf)}</div>
        </div>`;
}

// ---- Security ----
function escapeHtml(text) {
    if (!text) return '';
    return String(text)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;');
}

// ---- Init ----
checkGatewayStatus();
setInterval(checkGatewayStatus, 15000);
