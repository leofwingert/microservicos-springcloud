package com.microservicos.pecas.service;

import com.microservicos.pecas.model.Peca;
import com.microservicos.pecas.repository.PecaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PecaService {

    private final PecaRepository repository;

    public PecaService(PecaRepository repository) {
        this.repository = repository;
    }

    public Peca salvar(Peca peca) {
        return repository.save(peca);
    }

    public List<Peca> listarTodas() {
        return repository.findAll();
    }

    public Optional<Peca> buscarPorId(Long id) {
        return repository.findById(id);
    }

    public List<Peca> buscarPorNome(String nome) {
        return repository.findByNomeContainingIgnoreCase(nome);
    }
}
