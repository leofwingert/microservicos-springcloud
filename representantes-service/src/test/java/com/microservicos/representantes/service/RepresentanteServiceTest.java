package com.microservicos.representantes.service;

import com.microservicos.representantes.model.Representante;
import com.microservicos.representantes.repository.RepresentanteRepository;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para RepresentanteService.
 * Usa Mockito para isolar o repositório — nenhum banco de dados é acessado.
 */
@ExtendWith(MockitoExtension.class)
class RepresentanteServiceTest {

    @Mock
    private RepresentanteRepository repository;

    @InjectMocks
    private RepresentanteService service;

    private Representante representante;

    @BeforeEach
    void setUp() {
        representante = new Representante("12345678900", "Carlos Lima");
    }

    // ---------- salvar ----------

    @Test
    @DisplayName("Deve salvar um representante e retornar o representante salvo")
    void deveSalvarRepresentante() {
        when(repository.save(any(Representante.class))).thenReturn(representante);

        Representante resultado = service.salvar(new Representante("12345678900", "Carlos Lima"));

        assertNotNull(resultado);
        assertEquals("Carlos Lima", resultado.getNome());
        assertEquals("12345678900", resultado.getCpf());
        verify(repository, times(1)).save(any(Representante.class));
    }

    // ---------- listarTodos ----------

    @Test
    @DisplayName("Deve listar todos os representantes")
    void deveListarTodosRepresentantes() {
        Representante rep2 = new Representante("98765432100", "Ana Costa");
        when(repository.findAll()).thenReturn(Arrays.asList(representante, rep2));

        List<Representante> resultado = service.listarTodos();

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("Carlos Lima", resultado.get(0).getNome());
        assertEquals("Ana Costa", resultado.get(1).getNome());
        verify(repository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há representantes")
    void deveRetornarListaVaziaQuandoNaoHaRepresentantes() {
        when(repository.findAll()).thenReturn(Collections.emptyList());

        List<Representante> resultado = service.listarTodos();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(repository, times(1)).findAll();
    }

    // ---------- buscarPorCpf ----------

    @Test
    @DisplayName("Deve retornar representante quando encontrado por CPF")
    void deveRetornarRepresentanteQuandoEncontradoPorCpf() {
        when(repository.findById("12345678900")).thenReturn(Optional.of(representante));

        Optional<Representante> resultado = service.buscarPorCpf("12345678900");

        assertTrue(resultado.isPresent());
        assertEquals("Carlos Lima", resultado.get().getNome());
        assertEquals("12345678900", resultado.get().getCpf());
        verify(repository, times(1)).findById("12345678900");
    }

    @Test
    @DisplayName("Deve retornar Optional vazio quando representante não encontrado por CPF")
    void deveRetornarVazioQuandoRepresentanteNaoEncontradoPorCpf() {
        when(repository.findById(anyString())).thenReturn(Optional.empty());

        Optional<Representante> resultado = service.buscarPorCpf("00000000000");

        assertTrue(resultado.isEmpty());
        verify(repository, times(1)).findById("00000000000");
    }

    // ---------- buscarPorNome ----------

    @Test
    @DisplayName("Deve retornar representantes ao buscar por nome")
    void deveRetornarRepresentantesAoBuscarPorNome() {
        when(repository.findByNomeContainingIgnoreCase("carlos"))
                .thenReturn(Arrays.asList(representante));

        List<Representante> resultado = service.buscarPorNome("carlos");

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Carlos Lima", resultado.get(0).getNome());
        verify(repository, times(1)).findByNomeContainingIgnoreCase("carlos");
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando nenhum representante encontrado por nome")
    void deveRetornarListaVaziaQuandoNenhumRepresentanteEncontradoPorNome() {
        when(repository.findByNomeContainingIgnoreCase(anyString()))
                .thenReturn(Collections.emptyList());

        List<Representante> resultado = service.buscarPorNome("inexistente");

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(repository, times(1)).findByNomeContainingIgnoreCase("inexistente");
    }
}
