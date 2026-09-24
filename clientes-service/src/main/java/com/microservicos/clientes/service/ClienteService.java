package com.microservicos.clientes.service;

import com.microservicos.clientes.model.Cliente;
import com.microservicos.clientes.repository.ClienteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {

    private final ClienteRepository repository;

    public ClienteService(ClienteRepository repository) {
        this.repository = repository;
    }

    public Cliente salvar(Cliente cliente) {
        return repository.save(cliente);
    }

    public List<Cliente> listarTodos() {
        return repository.findAll();
    }

    public Optional<Cliente> buscarPorCpf(String cpf) {
        return repository.findById(cpf);
    }

    public List<Cliente> buscarPorNome(String nome) {
        return repository.findByNomeContainingIgnoreCase(nome);
    }

    public Optional<Cliente> atualizar(String cpf, Cliente dadosAtualizados) {
        return repository.findById(cpf).map(cliente -> {
            cliente.setNome(dadosAtualizados.getNome());
            return repository.save(cliente);
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
