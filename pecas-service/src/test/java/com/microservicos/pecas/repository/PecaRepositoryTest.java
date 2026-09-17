package com.microservicos.pecas.repository;

import com.microservicos.pecas.model.Peca;
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
 * Testes de persistência para PecaRepository.
 * Usa @DataJpaTest para isolar a camada de persistência — levanta apenas
 * o contexto JPA com H2 em memória, sem servidor web nem Spring Cloud.
 */
@DataJpaTest
class PecaRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PecaRepository repository;

    private Peca parafuso;
    private Peca porca;

    @BeforeEach
    void setUp() {
        // Insere dados diretamente via EntityManager (não via repository)
        parafuso = entityManager.persistAndFlush(new Peca("Parafuso M8", "Parafuso de aço inox"));
        porca = entityManager.persistAndFlush(new Peca("Porca Sextavada", "Porca 3/8"));
    }

    // ---------- findAll ----------

    @Test
    @DisplayName("Deve retornar todas as peças persistidas")
    void deveRetornarTodasPecas() {
        List<Peca> pecas = repository.findAll();

        assertEquals(2, pecas.size());
    }

    // ---------- findById ----------

    @Test
    @DisplayName("Deve encontrar peça por ID")
    void deveEncontrarPecaPorId() {
        Optional<Peca> resultado = repository.findById(parafuso.getId());

        assertTrue(resultado.isPresent());
        assertEquals("Parafuso M8", resultado.get().getNome());
    }

    @Test
    @DisplayName("Deve retornar vazio para ID inexistente")
    void deveRetornarVazioParaIdInexistente() {
        Optional<Peca> resultado = repository.findById(999L);

        assertTrue(resultado.isEmpty());
    }

    // ---------- save ----------

    @Test
    @DisplayName("Deve persistir uma nova peça com ID gerado")
    void devePersistirNovaPeca() {
        Peca arruela = new Peca("Arruela Lisa", "Arruela de pressão 1/4");
        Peca salva = repository.save(arruela);

        assertNotNull(salva.getId());
        assertEquals("Arruela Lisa", salva.getNome());
        assertEquals("Arruela de pressão 1/4", salva.getDescricao());

        // Verifica que realmente persistiu no banco
        Peca encontrada = entityManager.find(Peca.class, salva.getId());
        assertNotNull(encontrada);
        assertEquals("Arruela Lisa", encontrada.getNome());
    }

    // ---------- findByNomeContainingIgnoreCase ----------

    @Test
    @DisplayName("Deve encontrar peças por nome parcial (case-insensitive)")
    void deveEncontrarPecasPorNomeParcial() {
        List<Peca> resultado = repository.findByNomeContainingIgnoreCase("parafuso");

        assertEquals(1, resultado.size());
        assertEquals("Parafuso M8", resultado.get(0).getNome());
    }

    @Test
    @DisplayName("Deve encontrar peças por nome em maiúsculas (case-insensitive)")
    void deveEncontrarPecasPorNomeMaiusculo() {
        List<Peca> resultado = repository.findByNomeContainingIgnoreCase("PORCA");

        assertEquals(1, resultado.size());
        assertEquals("Porca Sextavada", resultado.get(0).getNome());
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando nome não encontrado")
    void deveRetornarListaVaziaQuandoNomeNaoEncontrado() {
        List<Peca> resultado = repository.findByNomeContainingIgnoreCase("inexistente");

        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("Deve encontrar múltiplas peças com termo genérico")
    void deveEncontrarMultiplasPecasComTermoGenerico() {
        entityManager.persistAndFlush(new Peca("Parafuso M10", "Parafuso maior"));

        List<Peca> resultado = repository.findByNomeContainingIgnoreCase("parafuso");

        assertEquals(2, resultado.size());
    }
}
