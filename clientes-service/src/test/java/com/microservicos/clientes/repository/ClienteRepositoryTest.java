package com.microservicos.clientes.repository;

import com.microservicos.clientes.model.Cliente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de persistência para ClienteRepository.
 * Usa @DataJpaTest para isolar a camada de persistência — levanta apenas
 * o contexto JPA com H2 em memória, sem servidor web nem Spring Cloud.
 */
@DataJpaTest
class ClienteRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ClienteRepository repository;

    private Cliente joao;
    private Cliente maria;

    @BeforeEach
    void setUp() {
        // Limpa dados entre testes para evitar conflitos de chave
        repository.deleteAll();
        entityManager.flush();

        // Insere dados diretamente via EntityManager (não via repository)
        joao = entityManager.persistAndFlush(new Cliente("12345678900", "João Silva"));
        maria = entityManager.persistAndFlush(new Cliente("98765432100", "Maria Souza"));
    }

    // ---------- findAll ----------

    @Test
    @DisplayName("Deve retornar todos os clientes persistidos")
    void deveRetornarTodosClientes() {
        List<Cliente> clientes = repository.findAll();

        assertEquals(2, clientes.size());
    }

    // ---------- findById (por CPF) ----------

    @Test
    @DisplayName("Deve encontrar cliente por CPF")
    void deveEncontrarClientePorCpf() {
        Optional<Cliente> resultado = repository.findById(joao.getCpf());

        assertTrue(resultado.isPresent());
        assertEquals("João Silva", resultado.get().getNome());
    }

    @Test
    @DisplayName("Deve retornar vazio para CPF inexistente")
    void deveRetornarVazioParaCpfInexistente() {
        Optional<Cliente> resultado = repository.findById("00000000000");

        assertTrue(resultado.isEmpty());
    }

    // ---------- save ----------

    @Test
    @DisplayName("Deve persistir um novo cliente")
    void devePersistirNovoCliente() {
        Cliente pedro = new Cliente("11122233344", "Pedro Santos");
        Cliente salvo = repository.save(pedro);

        assertNotNull(salvo);
        assertEquals("11122233344", salvo.getCpf());
        assertEquals("Pedro Santos", salvo.getNome());

        // Verifica que realmente persistiu no banco
        Cliente encontrado = entityManager.find(Cliente.class, salvo.getCpf());
        assertNotNull(encontrado);
        assertEquals("Pedro Santos", encontrado.getNome());
    }

    // ---------- findByNomeContainingIgnoreCase ----------

    @Test
    @DisplayName("Deve encontrar clientes por nome parcial (case-insensitive)")
    void deveEncontrarClientesPorNomeParcial() {
        List<Cliente> resultado = repository.findByNomeContainingIgnoreCase("joão");

        assertEquals(1, resultado.size());
        assertEquals("João Silva", resultado.get(0).getNome());
    }

    @Test
    @DisplayName("Deve encontrar clientes por nome em maiúsculas (case-insensitive)")
    void deveEncontrarClientesPorNomeMaiusculo() {
        List<Cliente> resultado = repository.findByNomeContainingIgnoreCase("MARIA");

        assertEquals(1, resultado.size());
        assertEquals("Maria Souza", resultado.get(0).getNome());
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando nome não encontrado")
    void deveRetornarListaVaziaQuandoNomeNaoEncontrado() {
        List<Cliente> resultado = repository.findByNomeContainingIgnoreCase("inexistente");

        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("Deve encontrar múltiplos clientes com termo genérico")
    void deveEncontrarMultiplosClientesComTermoGenerico() {
        entityManager.persistAndFlush(new Cliente("55566677788", "João Pedro"));

        List<Cliente> resultado = repository.findByNomeContainingIgnoreCase("joão");

        assertEquals(2, resultado.size());
    }
}
