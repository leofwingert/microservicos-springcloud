package com.microservicos.it;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Base para testes de integração de caixa-preta: assume que o sistema real
 * está no ar via "docker compose up --build -d" e fala com ele por HTTP,
 * exatamente como um cliente externo faria — nenhuma camada é mockada.
 *
 * Se a stack não estiver de pé, os testes são pulados (não falham) com uma
 * mensagem explicando como subi-la — assim "mvn verify" não quebra apenas
 * porque o Docker não está rodando.
 */
abstract class AbstractGatewayIT {

    protected static final String GATEWAY_URL = "http://localhost:8080";
    protected static final String EUREKA_URL = "http://localhost:8761";
    protected static final String CONFIG_SERVER_URL = "http://localhost:8888";

    private static final Duration TIMEOUT = Duration.ofSeconds(5);
    private static final int MAX_TENTATIVAS = 60;
    private static final Duration INTERVALO = Duration.ofSeconds(2);

    protected static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(TIMEOUT)
            .build();

    // O Failsafe roda todas as classes *IT na mesma JVM (reuseForks=true por padrão),
    // então o resultado da espera é cacheado aqui: sem isso, cada classe de teste
    // esperaria os MAX_TENTATIVAS * INTERVALO segundos por conta própria quando a
    // stack está fora do ar, multiplicando o tempo de "mvn verify" por classe de teste.
    private static volatile Boolean stackPronta;

    @BeforeAll
    static synchronized void garantirStackNoAr() throws InterruptedException {
        if (stackPronta == null) {
            stackPronta = aguardarStackFicarPronta();
        }

        Assumptions.assumeTrue(stackPronta, """
                Stack não está no ar em localhost (config-server:8888, eureka:8761, gateway:8080).
                Suba antes de rodar os testes de integração:
                    docker compose up --build -d
                E confira com:
                    docker compose ps
                """);
    }

    private static final String[] SERVICOS_ESPERADOS = {
            "API-GATEWAY", "PECAS-SERVICE", "CLIENTES-SERVICE", "REPRESENTANTES-SERVICE"
    };

    private static final String[] ROTAS_GATEWAY = {"/api/pecas", "/api/clientes", "/api/representantes"};

    private static boolean aguardarStackFicarPronta() throws InterruptedException {
        for (int tentativa = 1; tentativa <= MAX_TENTATIVAS; tentativa++) {
            boolean saudavel = estaSaudavel(CONFIG_SERVER_URL) && estaSaudavel(EUREKA_URL) && estaSaudavel(GATEWAY_URL);
            // Health UP só significa que a JVM subiu — o registro no Eureka e a
            // propagação para o cache do DiscoveryClient que o Gateway usa para o
            // roteamento "lb://" levam mais alguns segundos. Por isso confirmamos
            // registro no Eureka E que o Gateway já roteia de fato (sem 503) antes
            // de considerar a stack pronta.
            if (saudavel && todosRegistradosNoEureka() && gatewayRoteiaParaTodos()) {
                return true;
            }
            Thread.sleep(INTERVALO.toMillis());
        }
        return false;
    }

    private static boolean gatewayRoteiaParaTodos() {
        for (String rota : ROTAS_GATEWAY) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(GATEWAY_URL + rota))
                        .timeout(TIMEOUT)
                        .GET()
                        .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 503) {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    private static boolean estaSaudavel(String baseUrl) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/actuator/health"))
                    .timeout(TIMEOUT)
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200 && response.body().contains("\"UP\"");
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean todosRegistradosNoEureka() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(EUREKA_URL + "/eureka/apps"))
                    .header("Accept", "application/json")
                    .timeout(TIMEOUT)
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                return false;
            }
            String body = response.body();
            for (String servico : SERVICOS_ESPERADOS) {
                if (!body.contains(servico)) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    protected HttpResponse<String> get(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GATEWAY_URL + path))
                .timeout(TIMEOUT)
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    protected HttpResponse<String> post(String path, String jsonBody) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GATEWAY_URL + path))
                .timeout(TIMEOUT)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    protected HttpResponse<String> put(String path, String jsonBody) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GATEWAY_URL + path))
                .timeout(TIMEOUT)
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    protected HttpResponse<String> delete(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GATEWAY_URL + path))
                .timeout(TIMEOUT)
                .DELETE()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
