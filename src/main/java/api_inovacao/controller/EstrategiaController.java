package api_inovacao.controller;

import api_inovacao.dto.EstrategiaRequestDTO;
import api_inovacao.dto.EstrategiaResponseDTO;
import api_inovacao.model.User;
import api_inovacao.service.EstrategiaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/estrategias")
public class EstrategiaController {

    @Autowired
    private EstrategiaService estrategiaService;

    // Leitura: qualquer usuário logado (Operador, Gestor e Líder)
    @GetMapping
    public ResponseEntity<List<EstrategiaResponseDTO>> listar() {
        return ResponseEntity.ok(estrategiaService.listar());
    }

    @GetMapping("/vigente")
    public ResponseEntity<EstrategiaResponseDTO> buscarVigente() {
        return ResponseEntity.ok(estrategiaService.buscarVigente());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EstrategiaResponseDTO> buscarPorId(@PathVariable String id) {
        return ResponseEntity.ok(estrategiaService.buscarPorId(id));
    }

    // Escrita: somente Líder (regra definida no SecurityConfig)
    @PostMapping
    public ResponseEntity<EstrategiaResponseDTO> criar(@RequestBody @Valid EstrategiaRequestDTO dados,
                                                       @AuthenticationPrincipal User usuarioLogado) {
        return ResponseEntity.status(HttpStatus.CREATED).body(estrategiaService.criar(dados, usuarioLogado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EstrategiaResponseDTO> atualizar(@PathVariable String id,
                                                           @RequestBody @Valid EstrategiaRequestDTO dados) {
        return ResponseEntity.ok(estrategiaService.atualizar(id, dados));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable String id) {
        estrategiaService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
