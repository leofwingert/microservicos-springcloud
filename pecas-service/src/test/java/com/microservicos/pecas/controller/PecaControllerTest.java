package com.microservicos.pecas.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservicos.pecas.model.Peca;
import com.microservicos.pecas.service.PecaService;
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
 * Testes unitários para PecaController.
 * Usa @WebMvcTest para isolar a camada web — o Service é mockado,
 * nenhum banco de dados ou servidor real é levantado.
 */
@WebMvcTest(PecaController.class)
class PecaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PecaService service;

    @Autowired
    private ObjectMapper objectMapper;

    private Peca peca;

    @BeforeEach
    void setUp() {
        peca = new Peca("Parafuso M8", "Parafuso de aço inox M8");
        peca.setId(1L);
    }

    // ---------- POST /api/pecas ----------

    @Test
    @DisplayName("POST /api/pecas - Deve cadastrar peça e retornar 201")
    void deveCadastrarPeca() throws Exception {
        when(service.salvar(any(Peca.class))).thenReturn(peca);

        String json = objectMapper.writeValueAsString(new Peca("Parafuso M8", "Parafuso de aço inox M8"));

        mockMvc.perform(post("/api/pecas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Parafuso M8"))
                .andExpect(jsonPath("$.descricao").value("Parafuso de aço inox M8"));

        verify(service, times(1)).salvar(any(Peca.class));
    }

    @Test
    @DisplayName("POST /api/pecas - Deve retornar 400 quando nome é vazio")
    void deveRetornar400QuandoNomeVazio() throws Exception {
        String json = "{\"nome\": \"\", \"descricao\": \"Teste\"}";

        mockMvc.perform(post("/api/pecas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verify(service, never()).salvar(any(Peca.class));
    }

    // ---------- GET /api/pecas ----------

    @Test
    @DisplayName("GET /api/pecas - Deve listar todas as peças e retornar 200")
    void deveListarTodasPecas() throws Exception {
        Peca peca2 = new Peca("Porca Sextavada", "Porca 3/8");
        peca2.setId(2L);
        when(service.listarTodas()).thenReturn(Arrays.asList(peca, peca2));

        mockMvc.perform(get("/api/pecas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nome").value("Parafuso M8"))
                .andExpect(jsonPath("$[1].nome").value("Porca Sextavada"));
    }

    @Test
    @DisplayName("GET /api/pecas - Deve retornar lista vazia quando não há peças")
    void deveRetornarListaVazia() throws Exception {
        when(service.listarTodas()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/pecas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ---------- GET /api/pecas/{id} ----------

    @Test
    @DisplayName("GET /api/pecas/{id} - Deve retornar peça quando encontrada")
    void deveRetornarPecaPorId() throws Exception {
        when(service.buscarPorId(1L)).thenReturn(Optional.of(peca));

        mockMvc.perform(get("/api/pecas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Parafuso M8"));
    }

    @Test
    @DisplayName("GET /api/pecas/{id} - Deve retornar 404 quando não encontrada")
    void deveRetornar404QuandoPecaNaoEncontrada() throws Exception {
        when(service.buscarPorId(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/pecas/99"))
                .andExpect(status().isNotFound());
    }

    // ---------- GET /api/pecas/nome/{nome} ----------

    @Test
    @DisplayName("GET /api/pecas/nome/{nome} - Deve retornar peças por nome")
    void deveRetornarPecasPorNome() throws Exception {
        when(service.buscarPorNome("parafuso")).thenReturn(Arrays.asList(peca));

        mockMvc.perform(get("/api/pecas/nome/parafuso"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nome").value("Parafuso M8"));
    }

    @Test
    @DisplayName("GET /api/pecas/nome/{nome} - Deve retornar 404 quando nenhuma encontrada")
    void deveRetornar404QuandoNenhumaPecaPorNome() throws Exception {
        when(service.buscarPorNome("inexistente")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/pecas/nome/inexistente"))
                .andExpect(status().isNotFound());
    }
}
