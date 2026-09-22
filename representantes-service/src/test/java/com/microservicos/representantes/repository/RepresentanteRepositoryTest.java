package com.microservicos.representantes.repository;

import com.microservicos.representantes.model.Representante;
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
 * Testes de persistência para RepresentanteRepository.
 * Usa @DataJpaTest para isolar a camada de persistência — levanta apenas
 * o contexto JPA com H2 em memória, sem servidor web nem Spring Cloud.
 */
@DataJpaTest
class RepresentanteRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RepresentanteRepository repository;

    private Representante carlos;
    private Representante ana;

    @BeforeEach
    void setUp() {
        // Limpa dados entre testes para evitar conflitos de chave
        repository.deleteAll();
        entityManager.flush();

        // Insere dados diretamente via EntityManager (não via repository)
        carlos = entityManager.persistAndFlush(new Representante("12345678900", "Carlos Lima"));
        ana = entityManager.persistAndFlush(new Representante("98765432100", "Ana Costa"));
    }

    // ---------- findAll ----------

    @Test
    @DisplayName("Deve retornar todos os representantes persistidos")
    void deveRetornarTodosRepresentantes() {
        List<Representante> representantes = repository.findAll();

        assertEquals(2, representantes.size());
    }

    // ---------- findById (por CPF) ----------

    @Test
    @DisplayName("Deve encontrar representante por CPF")
    void deveEncontrarRepresentantePorCpf() {
        Optional<Representante> resultado = repository.findById(carlos.getCpf());

        assertTrue(resultado.isPresent());
        assertEquals("Carlos Lima", resultado.get().getNome());
    }

    @Test
    @DisplayName("Deve retornar vazio para CPF inexistente")
    void deveRetornarVazioParaCpfInexistente() {
        Optional<Representante> resultado = repository.findById("00000000000");

        assertTrue(resultado.isEmpty());
    }

    // ---------- save ----------

    @Test
    @DisplayName("Deve persistir um novo representante")
    void devePersistirNovoRepresentante() {
        Representante pedro = new Representante("11122233344", "Pedro Santos");
        Representante salvo = repository.save(pedro);

        assertNotNull(salvo);
        assertEquals("11122233344", salvo.getCpf());
        assertEquals("Pedro Santos", salvo.getNome());

        // Verifica que realmente persistiu no banco
        Representante encontrado = entityManager.find(Representante.class, salvo.getCpf());
        assertNotNull(encontrado);
        assertEquals("Pedro Santos", encontrado.getNome());
    }

    // ---------- findByNomeContainingIgnoreCase ----------

    @Test
    @DisplayName("Deve encontrar representantes por nome parcial (case-insensitive)")
    void deveEncontrarRepresentantesPorNomeParcial() {
        List<Representante> resultado = repository.findByNomeContainingIgnoreCase("carlos");

        assertEquals(1, resultado.size());
        assertEquals("Carlos Lima", resultado.get(0).getNome());
    }

    @Test
    @DisplayName("Deve encontrar representantes por nome em maiúsculas (case-insensitive)")
    void deveEncontrarRepresentantesPorNomeMaiusculo() {
        List<Representante> resultado = repository.findByNomeContainingIgnoreCase("ANA");

        assertEquals(1, resultado.size());
        assertEquals("Ana Costa", resultado.get(0).getNome());
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando nome não encontrado")
    void deveRetornarListaVaziaQuandoNomeNaoEncontrado() {
        List<Representante> resultado = repository.findByNomeContainingIgnoreCase("inexistente");

        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("Deve encontrar múltiplos representantes com termo genérico")
    void deveEncontrarMultiplosRepresentantesComTermoGenerico() {
        entityManager.persistAndFlush(new Representante("55566677788", "Carlos Eduardo"));

        List<Representante> resultado = repository.findByNomeContainingIgnoreCase("carlos");

        assertEquals(2, resultado.size());
    }
}
