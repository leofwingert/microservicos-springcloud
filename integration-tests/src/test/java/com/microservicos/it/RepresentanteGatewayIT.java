package com.microservicos.it;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Percorre o caminho real de uma requisição de Representantes através do
 * Gateway até o representantes-service, sem mockar nenhuma camada.
 */
class RepresentanteGatewayIT extends AbstractGatewayIT {

    @Test
    @DisplayName("Deve cadastrar, buscar, atualizar e remover um representante via Gateway")
    void deveFazerCrudCompletoViaGateway() throws Exception {
        String cpf = cpfUnico();
        String nome = "Representante IT " + cpf;
        String json = """
                {"cpf": "%s", "nome": "%s"}
                """.formatted(cpf, nome);

        HttpResponse<String> cadastro = post("/api/representantes", json);
        assertThat(cadastro.statusCode()).isEqualTo(201);
        assertThat(cadastro.body()).contains(cpf).contains(nome);

        HttpResponse<String> porCpf = get("/api/representantes/cpf/" + cpf);
        assertThat(porCpf.statusCode()).isEqualTo(200);
        assertThat(porCpf.body()).contains(nome);

        HttpResponse<String> porNome = get("/api/representantes/nome/" + nome.replace(" ", "%20"));
        assertThat(porNome.statusCode()).isEqualTo(200);
        assertThat(porNome.body()).contains(cpf);

        HttpResponse<String> todos = get("/api/representantes");
        assertThat(todos.statusCode()).isEqualTo(200);
        assertThat(todos.body()).contains(cpf);

        String nomeAtualizado = nome + " Atualizado";
        HttpResponse<String> atualizacao = put("/api/representantes/" + cpf,
                """
                {"cpf": "%s", "nome": "%s"}
                """.formatted(cpf, nomeAtualizado));
        assertThat(atualizacao.statusCode()).isEqualTo(200);
        assertThat(atualizacao.body()).contains(nomeAtualizado);

        HttpResponse<String> remocao = delete("/api/representantes/" + cpf);
        assertThat(remocao.statusCode()).isEqualTo(204);

        HttpResponse<String> apagado = get("/api/representantes/cpf/" + cpf);
        assertThat(apagado.statusCode()).isEqualTo(404);
    }

    @Test
    @DisplayName("Deve retornar 404 via Gateway ao atualizar ou remover representante inexistente")
    void deveRetornar404ParaRepresentanteInexistente() throws Exception {
        String cpfInexistente = cpfUnico();

        HttpResponse<String> atualizacao = put("/api/representantes/" + cpfInexistente,
                """
                {"cpf": "%s", "nome": "Fantasma"}
                """.formatted(cpfInexistente));
        assertThat(atualizacao.statusCode()).isEqualTo(404);

        HttpResponse<String> remocao = delete("/api/representantes/" + cpfInexistente);
        assertThat(remocao.statusCode()).isEqualTo(404);
    }

    private static String cpfUnico() {
        long sufixo = System.nanoTime() % 100_000_000L;
        return "888.%03d.%03d-%02d".formatted(sufixo / 100_000, (sufixo / 100) % 1000, sufixo % 100);
    }
}
