package com.microservicos.it;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Percorre o caminho real de uma requisição de Clientes através do Gateway
 * até o clientes-service, sem mockar nenhuma camada.
 */
class ClienteGatewayIT extends AbstractGatewayIT {

    @Test
    @DisplayName("Deve cadastrar, buscar, atualizar e remover um cliente via Gateway")
    void deveFazerCrudCompletoViaGateway() throws Exception {
        String cpf = cpfUnico();
        String nome = "Cliente IT " + cpf;
        String json = """
                {"cpf": "%s", "nome": "%s"}
                """.formatted(cpf, nome);

        HttpResponse<String> cadastro = post("/api/clientes", json);
        assertThat(cadastro.statusCode()).isEqualTo(201);
        assertThat(cadastro.body()).contains(cpf).contains(nome);

        HttpResponse<String> porCpf = get("/api/clientes/cpf/" + cpf);
        assertThat(porCpf.statusCode()).isEqualTo(200);
        assertThat(porCpf.body()).contains(nome);

        HttpResponse<String> porNome = get("/api/clientes/nome/" + nome.replace(" ", "%20"));
        assertThat(porNome.statusCode()).isEqualTo(200);
        assertThat(porNome.body()).contains(cpf);

        HttpResponse<String> todos = get("/api/clientes");
        assertThat(todos.statusCode()).isEqualTo(200);
        assertThat(todos.body()).contains(cpf);

        String nomeAtualizado = nome + " Atualizado";
        HttpResponse<String> atualizacao = put("/api/clientes/" + cpf,
                """
                {"cpf": "%s", "nome": "%s"}
                """.formatted(cpf, nomeAtualizado));
        assertThat(atualizacao.statusCode()).isEqualTo(200);
        assertThat(atualizacao.body()).contains(nomeAtualizado);

        HttpResponse<String> remocao = delete("/api/clientes/" + cpf);
        assertThat(remocao.statusCode()).isEqualTo(204);

        HttpResponse<String> apagado = get("/api/clientes/cpf/" + cpf);
        assertThat(apagado.statusCode()).isEqualTo(404);
    }

    @Test
    @DisplayName("Deve retornar 404 via Gateway ao atualizar ou remover cliente inexistente")
    void deveRetornar404ParaClienteInexistente() throws Exception {
        String cpfInexistente = cpfUnico();

        HttpResponse<String> atualizacao = put("/api/clientes/" + cpfInexistente,
                """
                {"cpf": "%s", "nome": "Fantasma"}
                """.formatted(cpfInexistente));
        assertThat(atualizacao.statusCode()).isEqualTo(404);

        HttpResponse<String> remocao = delete("/api/clientes/" + cpfInexistente);
        assertThat(remocao.statusCode()).isEqualTo(404);
    }

    private static String cpfUnico() {
        long sufixo = System.nanoTime() % 100_000_000L;
        return "999.%03d.%03d-%02d".formatted(sufixo / 100_000, (sufixo / 100) % 1000, sufixo % 100);
    }
}
