package com.microservicos.representantes.controller;

import com.microservicos.representantes.model.Representante;
import com.microservicos.representantes.service.RepresentanteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/representantes")

public class RepresentanteController {

    private final RepresentanteService service;

    public RepresentanteController(RepresentanteService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Representante> cadastrar(@Valid @RequestBody Representante representante) {
        Representante salvo = service.salvar(representante);
        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
    }

    @GetMapping
    public ResponseEntity<List<Representante>> listarTodos() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/cpf/{cpf}")
    public ResponseEntity<Representante> buscarPorCpf(@PathVariable String cpf) {
        return service.buscarPorCpf(cpf)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/nome/{nome}")
    public ResponseEntity<List<Representante>> buscarPorNome(@PathVariable String nome) {
        List<Representante> resultado = service.buscarPorNome(nome);
        if (resultado.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(resultado);
    }

    @PutMapping("/{cpf}")
    public ResponseEntity<Representante> atualizar(@PathVariable String cpf, @Valid @RequestBody Representante representante) {
        return service.atualizar(cpf, representante)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{cpf}")
    public ResponseEntity<Void> deletar(@PathVariable String cpf) {
        return service.deletar(cpf)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
