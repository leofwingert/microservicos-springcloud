package com.microservicos.representantes.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservicos.representantes.model.Representante;
import com.microservicos.representantes.service.RepresentanteService;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes unitários para RepresentanteController.
 * Usa @WebMvcTest para isolar a camada web — o Service é mockado,
 * nenhum banco de dados ou servidor real é levantado.
 */
@WebMvcTest(RepresentanteController.class)
class RepresentanteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RepresentanteService service;

    @Autowired
    private ObjectMapper objectMapper;

    private Representante representante;

    @BeforeEach
    void setUp() {
        representante = new Representante("12345678900", "Carlos Lima");
    }

    // ---------- POST /api/representantes ----------

    @Test
    @DisplayName("POST /api/representantes - Deve cadastrar representante e retornar 201")
    void deveCadastrarRepresentante() throws Exception {
        when(service.salvar(any(Representante.class))).thenReturn(representante);

        String json = objectMapper.writeValueAsString(new Representante("12345678900", "Carlos Lima"));

        mockMvc.perform(post("/api/representantes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cpf").value("12345678900"))
                .andExpect(jsonPath("$.nome").value("Carlos Lima"));

        verify(service, times(1)).salvar(any(Representante.class));
    }

    @Test
    @DisplayName("POST /api/representantes - Deve retornar 400 quando nome é vazio")
    void deveRetornar400QuandoNomeVazio() throws Exception {
        String json = "{\"cpf\": \"12345678900\", \"nome\": \"\"}";

        mockMvc.perform(post("/api/representantes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verify(service, never()).salvar(any(Representante.class));
    }

    // ---------- GET /api/representantes ----------

    @Test
    @DisplayName("GET /api/representantes - Deve listar todos os representantes e retornar 200")
    void deveListarTodosRepresentantes() throws Exception {
        Representante rep2 = new Representante("98765432100", "Ana Costa");
        when(service.listarTodos()).thenReturn(Arrays.asList(representante, rep2));

        mockMvc.perform(get("/api/representantes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nome").value("Carlos Lima"))
                .andExpect(jsonPath("$[1].nome").value("Ana Costa"));
    }

    @Test
    @DisplayName("GET /api/representantes - Deve retornar lista vazia quando não há representantes")
    void deveRetornarListaVazia() throws Exception {
        when(service.listarTodos()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/representantes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ---------- GET /api/representantes/cpf/{cpf} ----------

    @Test
    @DisplayName("GET /api/representantes/cpf/{cpf} - Deve retornar representante quando encontrado")
    void deveRetornarRepresentantePorCpf() throws Exception {
        when(service.buscarPorCpf("12345678900")).thenReturn(Optional.of(representante));

        mockMvc.perform(get("/api/representantes/cpf/12345678900"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cpf").value("12345678900"))
                .andExpect(jsonPath("$.nome").value("Carlos Lima"));
    }

    @Test
    @DisplayName("GET /api/representantes/cpf/{cpf} - Deve retornar 404 quando não encontrado")
    void deveRetornar404QuandoRepresentanteNaoEncontrado() throws Exception {
        when(service.buscarPorCpf("00000000000")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/representantes/cpf/00000000000"))
                .andExpect(status().isNotFound());
    }

    // ---------- GET /api/representantes/nome/{nome} ----------

    @Test
    @DisplayName("GET /api/representantes/nome/{nome} - Deve retornar representantes por nome")
    void deveRetornarRepresentantesPorNome() throws Exception {
        when(service.buscarPorNome("carlos")).thenReturn(Arrays.asList(representante));

        mockMvc.perform(get("/api/representantes/nome/carlos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nome").value("Carlos Lima"));
    }

    @Test
    @DisplayName("GET /api/representantes/nome/{nome} - Deve retornar 404 quando nenhum encontrado")
    void deveRetornar404QuandoNenhumRepresentantePorNome() throws Exception {
        when(service.buscarPorNome("inexistente")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/representantes/nome/inexistente"))
                .andExpect(status().isNotFound());
    }

    // ---------- PUT /api/representantes/{cpf} ----------

    @Test
    @DisplayName("PUT /api/representantes/{cpf} - Deve atualizar representante e retornar 200")
    void deveAtualizarRepresentante() throws Exception {
        Representante atualizado = new Representante("12345678900", "Carlos Lima Atualizado");
        when(service.atualizar(eq("12345678900"), any(Representante.class))).thenReturn(Optional.of(atualizado));

        String json = objectMapper.writeValueAsString(new Representante("12345678900", "Carlos Lima Atualizado"));

        mockMvc.perform(put("/api/representantes/12345678900")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Carlos Lima Atualizado"));

        verify(service, times(1)).atualizar(eq("12345678900"), any(Representante.class));
    }

    @Test
    @DisplayName("PUT /api/representantes/{cpf} - Deve retornar 404 quando representante não encontrado")
    void deveRetornar404AoAtualizarRepresentanteInexistente() throws Exception {
        when(service.atualizar(eq("00000000000"), any(Representante.class))).thenReturn(Optional.empty());

        String json = objectMapper.writeValueAsString(new Representante("00000000000", "Inexistente"));

        mockMvc.perform(put("/api/representantes/00000000000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/representantes/{cpf} - Deve retornar 400 quando nome é vazio")
    void deveRetornar400AoAtualizarComNomeVazio() throws Exception {
        mockMvc.perform(put("/api/representantes/12345678900")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cpf\": \"12345678900\", \"nome\": \"\"}"))
                .andExpect(status().isBadRequest());

        verify(service, never()).atualizar(anyString(), any(Representante.class));
    }

    // ---------- DELETE /api/representantes/{cpf} ----------

    @Test
    @DisplayName("DELETE /api/representantes/{cpf} - Deve remover representante e retornar 204")
    void deveRemoverRepresentante() throws Exception {
        when(service.deletar("12345678900")).thenReturn(true);

        mockMvc.perform(delete("/api/representantes/12345678900"))
                .andExpect(status().isNoContent());

        verify(service, times(1)).deletar("12345678900");
    }

    @Test
    @DisplayName("DELETE /api/representantes/{cpf} - Deve retornar 404 quando representante não encontrado")
    void deveRetornar404AoRemoverRepresentanteInexistente() throws Exception {
        when(service.deletar("00000000000")).thenReturn(false);

        mockMvc.perform(delete("/api/representantes/00000000000"))
                .andExpect(status().isNotFound());
    }
}
