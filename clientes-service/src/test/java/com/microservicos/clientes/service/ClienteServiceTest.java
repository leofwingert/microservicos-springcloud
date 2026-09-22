package com.microservicos.clientes.service;

import com.microservicos.clientes.model.Cliente;
import com.microservicos.clientes.repository.ClienteRepository;
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
 * Testes unitários para ClienteService.
 * Usa Mockito para isolar o repositório — nenhum banco de dados é acessado.
 */
@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository repository;

    @InjectMocks
    private ClienteService service;

    private Cliente cliente;

    @BeforeEach
    void setUp() {
        cliente = new Cliente("12345678900", "João Silva");
    }

    // ---------- salvar ----------

    @Test
    @DisplayName("Deve salvar um cliente e retornar o cliente salvo")
    void deveSalvarCliente() {
        when(repository.save(any(Cliente.class))).thenReturn(cliente);

        Cliente resultado = service.salvar(new Cliente("12345678900", "João Silva"));

        assertNotNull(resultado);
        assertEquals("João Silva", resultado.getNome());
        assertEquals("12345678900", resultado.getCpf());
        verify(repository, times(1)).save(any(Cliente.class));
    }

    // ---------- listarTodos ----------

    @Test
    @DisplayName("Deve listar todos os clientes")
    void deveListarTodosClientes() {
        Cliente cliente2 = new Cliente("98765432100", "Maria Souza");
        when(repository.findAll()).thenReturn(Arrays.asList(cliente, cliente2));

        List<Cliente> resultado = service.listarTodos();

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("João Silva", resultado.get(0).getNome());
        assertEquals("Maria Souza", resultado.get(1).getNome());
        verify(repository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há clientes")
    void deveRetornarListaVaziaQuandoNaoHaClientes() {
        when(repository.findAll()).thenReturn(Collections.emptyList());

        List<Cliente> resultado = service.listarTodos();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(repository, times(1)).findAll();
    }

    // ---------- buscarPorCpf ----------

    @Test
    @DisplayName("Deve retornar cliente quando encontrado por CPF")
    void deveRetornarClienteQuandoEncontradoPorCpf() {
        when(repository.findById("12345678900")).thenReturn(Optional.of(cliente));

        Optional<Cliente> resultado = service.buscarPorCpf("12345678900");

        assertTrue(resultado.isPresent());
        assertEquals("João Silva", resultado.get().getNome());
        assertEquals("12345678900", resultado.get().getCpf());
        verify(repository, times(1)).findById("12345678900");
    }

    @Test
    @DisplayName("Deve retornar Optional vazio quando cliente não encontrado por CPF")
    void deveRetornarVazioQuandoClienteNaoEncontradoPorCpf() {
        when(repository.findById(anyString())).thenReturn(Optional.empty());

        Optional<Cliente> resultado = service.buscarPorCpf("00000000000");

        assertTrue(resultado.isEmpty());
        verify(repository, times(1)).findById("00000000000");
    }

    // ---------- buscarPorNome ----------

    @Test
    @DisplayName("Deve retornar clientes ao buscar por nome")
    void deveRetornarClientesAoBuscarPorNome() {
        when(repository.findByNomeContainingIgnoreCase("joão"))
                .thenReturn(Arrays.asList(cliente));

        List<Cliente> resultado = service.buscarPorNome("joão");

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("João Silva", resultado.get(0).getNome());
        verify(repository, times(1)).findByNomeContainingIgnoreCase("joão");
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando nenhum cliente encontrado por nome")
    void deveRetornarListaVaziaQuandoNenhumClienteEncontradoPorNome() {
        when(repository.findByNomeContainingIgnoreCase(anyString()))
                .thenReturn(Collections.emptyList());

        List<Cliente> resultado = service.buscarPorNome("inexistente");

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(repository, times(1)).findByNomeContainingIgnoreCase("inexistente");
    }
}
