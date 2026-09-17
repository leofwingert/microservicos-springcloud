package com.microservicos.pecas.service;

import com.microservicos.pecas.model.Peca;
import com.microservicos.pecas.repository.PecaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para PecaService.
 * Usa Mockito para isolar o repositório — nenhum banco de dados é acessado.
 */
@ExtendWith(MockitoExtension.class)
class PecaServiceTest {

    @Mock
    private PecaRepository repository;

    @InjectMocks
    private PecaService service;

    private Peca peca;

    @BeforeEach
    void setUp() {
        peca = new Peca("Parafuso M8", "Parafuso de aço inox M8");
        peca.setId(1L);
    }

    // ---------- salvar ----------

    @Test
    @DisplayName("Deve salvar uma peça e retornar a peça salva")
    void deveSalvarPeca() {
        when(repository.save(any(Peca.class))).thenReturn(peca);

        Peca resultado = service.salvar(new Peca("Parafuso M8", "Parafuso de aço inox M8"));

        assertNotNull(resultado);
        assertEquals("Parafuso M8", resultado.getNome());
        assertEquals("Parafuso de aço inox M8", resultado.getDescricao());
        assertEquals(1L, resultado.getId());
        verify(repository, times(1)).save(any(Peca.class));
    }

    // ---------- listarTodas ----------

    @Test
    @DisplayName("Deve listar todas as peças")
    void deveListarTodasPecas() {
        Peca peca2 = new Peca("Porca Sextavada", "Porca 3/8");
        peca2.setId(2L);
        when(repository.findAll()).thenReturn(Arrays.asList(peca, peca2));

        List<Peca> resultado = service.listarTodas();

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("Parafuso M8", resultado.get(0).getNome());
        assertEquals("Porca Sextavada", resultado.get(1).getNome());
        verify(repository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há peças")
    void deveRetornarListaVaziaQuandoNaoHaPecas() {
        when(repository.findAll()).thenReturn(Collections.emptyList());

        List<Peca> resultado = service.listarTodas();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(repository, times(1)).findAll();
    }

    // ---------- buscarPorId ----------

    @Test
    @DisplayName("Deve retornar peça quando encontrada por ID")
    void deveRetornarPecaQuandoEncontradaPorId() {
        when(repository.findById(1L)).thenReturn(Optional.of(peca));

        Optional<Peca> resultado = service.buscarPorId(1L);

        assertTrue(resultado.isPresent());
        assertEquals("Parafuso M8", resultado.get().getNome());
        verify(repository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Deve retornar Optional vazio quando peça não encontrada por ID")
    void deveRetornarVazioQuandoPecaNaoEncontradaPorId() {
        when(repository.findById(anyLong())).thenReturn(Optional.empty());

        Optional<Peca> resultado = service.buscarPorId(99L);

        assertTrue(resultado.isEmpty());
        verify(repository, times(1)).findById(99L);
    }

    // ---------- buscarPorNome ----------

    @Test
    @DisplayName("Deve retornar peças ao buscar por nome")
    void deveRetornarPecasAoBuscarPorNome() {
        when(repository.findByNomeContainingIgnoreCase("parafuso"))
                .thenReturn(Arrays.asList(peca));

        List<Peca> resultado = service.buscarPorNome("parafuso");

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Parafuso M8", resultado.get(0).getNome());
        verify(repository, times(1)).findByNomeContainingIgnoreCase("parafuso");
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando nenhuma peça encontrada por nome")
    void deveRetornarListaVaziaQuandoNenhumaPecaEncontradaPorNome() {
        when(repository.findByNomeContainingIgnoreCase(anyString()))
                .thenReturn(Collections.emptyList());

        List<Peca> resultado = service.buscarPorNome("inexistente");

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(repository, times(1)).findByNomeContainingIgnoreCase("inexistente");
    }
}
