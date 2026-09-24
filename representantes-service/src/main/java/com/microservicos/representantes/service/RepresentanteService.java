package com.microservicos.representantes.service;

import com.microservicos.representantes.model.Representante;
import com.microservicos.representantes.repository.RepresentanteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RepresentanteService {

    private final RepresentanteRepository repository;

    public RepresentanteService(RepresentanteRepository repository) {
        this.repository = repository;
    }

    public Representante salvar(Representante representante) {
        return repository.save(representante);
    }

    public List<Representante> listarTodos() {
        return repository.findAll();
    }

    public Optional<Representante> buscarPorCpf(String cpf) {
        return repository.findById(cpf);
    }

    public List<Representante> buscarPorNome(String nome) {
        return repository.findByNomeContainingIgnoreCase(nome);
    }

    public Optional<Representante> atualizar(String cpf, Representante dadosAtualizados) {
        return repository.findById(cpf).map(representante -> {
            representante.setNome(dadosAtualizados.getNome());
            return repository.save(representante);
        });
    }

    public boolean deletar(String cpf) {
        if (!repository.existsById(cpf)) {
            return false;
        }
        repository.deleteById(cpf);
        return true;
    }
}
