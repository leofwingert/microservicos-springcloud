package com.microservicos.it;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Percorre o caminho real de uma requisição de Peças: cliente HTTP → Gateway
 * (porta 8080) → roteamento via Eureka → pecas-service → H2 do próprio serviço.
 * Nenhuma camada é mockada.
 */
class PecaGatewayIT extends AbstractGatewayIT {

    @Test
    @DisplayName("Deve cadastrar, buscar, atualizar e remover uma peça via Gateway")
    void deveFazerCrudCompletoViaGateway() throws Exception {
        String nomeUnico = "Parafuso IT " + System.currentTimeMillis();
        String json = """
                {"nome": "%s", "descricao": "Criada pelo teste de integração"}
                """.formatted(nomeUnico);

        HttpResponse<String> cadastro = post("/api/pecas", json);
        assertThat(cadastro.statusCode()).isEqualTo(201);
        assertThat(cadastro.body()).contains(nomeUnico);

        Long id = extrairId(cadastro.body());

        HttpResponse<String> porId = get("/api/pecas/" + id);
        assertThat(porId.statusCode()).isEqualTo(200);
        assertThat(porId.body()).contains(nomeUnico);

        HttpResponse<String> porNome = get("/api/pecas/nome/" + nomeUnico.replace(" ", "%20"));
        assertThat(porNome.statusCode()).isEqualTo(200);
        assertThat(porNome.body()).contains(nomeUnico);

        HttpResponse<String> todas = get("/api/pecas");
        assertThat(todas.statusCode()).isEqualTo(200);
        assertThat(todas.body()).contains(nomeUnico);

        String nomeAtualizado = nomeUnico + " Atualizado";
        String jsonAtualizado = """
                {"nome": "%s", "descricao": "Descrição atualizada pelo teste"}
                """.formatted(nomeAtualizado);
        HttpResponse<String> atualizacao = put("/api/pecas/" + id, jsonAtualizado);
        assertThat(atualizacao.statusCode()).isEqualTo(200);
        assertThat(atualizacao.body()).contains(nomeAtualizado);

        HttpResponse<String> remocao = delete("/api/pecas/" + id);
        assertThat(remocao.statusCode()).isEqualTo(204);

        HttpResponse<String> apagada = get("/api/pecas/" + id);
        assertThat(apagada.statusCode()).isEqualTo(404);
    }

    @Test
    @DisplayName("Deve retornar 400 via Gateway quando o nome da peça é vazio")
    void deveRetornar400QuandoNomeVazio() throws Exception {
        HttpResponse<String> resposta = post("/api/pecas", "{\"nome\": \"\", \"descricao\": \"sem nome\"}");
        assertThat(resposta.statusCode()).isEqualTo(400);
    }

    @Test
    @DisplayName("Deve retornar 404 via Gateway ao atualizar ou remover peça inexistente")
    void deveRetornar404ParaPecaInexistente() throws Exception {
        long idInexistente = 999_999_999L;

        HttpResponse<String> atualizacao = put("/api/pecas/" + idInexistente,
                "{\"nome\": \"Fantasma\", \"descricao\": \"não existe\"}");
        assertThat(atualizacao.statusCode()).isEqualTo(404);

        HttpResponse<String> remocao = delete("/api/pecas/" + idInexistente);
        assertThat(remocao.statusCode()).isEqualTo(404);
    }

    private static Long extrairId(String jsonBody) {
        var matcher = java.util.regex.Pattern.compile("\"id\"\\s*:\\s*(\\d+)").matcher(jsonBody);
        if (!matcher.find()) {
            throw new IllegalStateException("Resposta não contém id: " + jsonBody);
        }
        return Long.valueOf(matcher.group(1));
    }
}
