package com.microservicos.representantes.repository;

import com.microservicos.representantes.model.Representante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepresentanteRepository extends JpaRepository<Representante, String> {
    List<Representante> findByNomeContainingIgnoreCase(String nome);
}
