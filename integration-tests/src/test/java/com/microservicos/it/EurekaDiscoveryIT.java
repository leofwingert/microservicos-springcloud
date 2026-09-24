package com.microservicos.it;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifica que os 4 clientes Eureka (gateway + 3 serviços de negócio)
 * realmente se registraram e estão UP — sem isso, o roteamento do
 * Gateway via "lb://" não tem para onde ir.
 */
class EurekaDiscoveryIT extends AbstractGatewayIT {

    @Test
    @DisplayName("Todos os serviços de negócio e o gateway devem estar registrados e UP no Eureka")
    void devemEstarTodosRegistradosEUp() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(EUREKA_URL + "/eureka/apps"))
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).isEqualTo(200);

        String body = response.body();
        assertThat(body)
                .as("resposta do Eureka /eureka/apps")
                .containsIgnoringCase("API-GATEWAY")
                .containsIgnoringCase("PECAS-SERVICE")
                .containsIgnoringCase("CLIENTES-SERVICE")
                .containsIgnoringCase("REPRESENTANTES-SERVICE")
                .contains("\"status\":\"UP\"");
    }
}
