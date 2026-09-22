package com.microservicos.clientes.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservicos.clientes.model.Cliente;
import com.microservicos.clientes.service.ClienteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes unitários para ClienteController.
 * Usa @WebMvcTest para isolar a camada web — o Service é mockado,
 * nenhum banco de dados ou servidor real é levantado.
 */
@WebMvcTest(ClienteController.class)
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClienteService service;

    @Autowired
    private ObjectMapper objectMapper;

    private Cliente cliente;

    @BeforeEach
    void setUp() {
        cliente = new Cliente("12345678900", "João Silva");
    }

    // ---------- POST /api/clientes ----------

    @Test
    @DisplayName("POST /api/clientes - Deve cadastrar cliente e retornar 201")
    void deveCadastrarCliente() throws Exception {
        when(service.salvar(any(Cliente.class))).thenReturn(cliente);

        String json = objectMapper.writeValueAsString(new Cliente("12345678900", "João Silva"));

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cpf").value("12345678900"))
                .andExpect(jsonPath("$.nome").value("João Silva"));

        verify(service, times(1)).salvar(any(Cliente.class));
    }

    @Test
    @DisplayName("POST /api/clientes - Deve retornar 400 quando nome é vazio")
    void deveRetornar400QuandoNomeVazio() throws Exception {
        String json = "{\"cpf\": \"12345678900\", \"nome\": \"\"}";

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verify(service, never()).salvar(any(Cliente.class));
    }

    // ---------- GET /api/clientes ----------

    @Test
    @DisplayName("GET /api/clientes - Deve listar todos os clientes e retornar 200")
    void deveListarTodosClientes() throws Exception {
        Cliente cliente2 = new Cliente("98765432100", "Maria Souza");
        when(service.listarTodos()).thenReturn(Arrays.asList(cliente, cliente2));

        mockMvc.perform(get("/api/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nome").value("João Silva"))
                .andExpect(jsonPath("$[1].nome").value("Maria Souza"));
    }

    @Test
    @DisplayName("GET /api/clientes - Deve retornar lista vazia quando não há clientes")
    void deveRetornarListaVazia() throws Exception {
        when(service.listarTodos()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ---------- GET /api/clientes/cpf/{cpf} ----------

    @Test
    @DisplayName("GET /api/clientes/cpf/{cpf} - Deve retornar cliente quando encontrado")
    void deveRetornarClientePorCpf() throws Exception {
        when(service.buscarPorCpf("12345678900")).thenReturn(Optional.of(cliente));

        mockMvc.perform(get("/api/clientes/cpf/12345678900"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cpf").value("12345678900"))
                .andExpect(jsonPath("$.nome").value("João Silva"));
    }

    @Test
    @DisplayName("GET /api/clientes/cpf/{cpf} - Deve retornar 404 quando não encontrado")
    void deveRetornar404QuandoClienteNaoEncontrado() throws Exception {
        when(service.buscarPorCpf("00000000000")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/clientes/cpf/00000000000"))
                .andExpect(status().isNotFound());
    }

    // ---------- GET /api/clientes/nome/{nome} ----------

    @Test
    @DisplayName("GET /api/clientes/nome/{nome} - Deve retornar clientes por nome")
    void deveRetornarClientesPorNome() throws Exception {
        when(service.buscarPorNome("joão")).thenReturn(Arrays.asList(cliente));

        mockMvc.perform(get("/api/clientes/nome/joão"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nome").value("João Silva"));
    }

    @Test
    @DisplayName("GET /api/clientes/nome/{nome} - Deve retornar 404 quando nenhum encontrado")
    void deveRetornar404QuandoNenhumClientePorNome() throws Exception {
        when(service.buscarPorNome("inexistente")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/clientes/nome/inexistente"))
                .andExpect(status().isNotFound());
    }
}
