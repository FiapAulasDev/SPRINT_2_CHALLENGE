package api_inovacao.controller;

import api_inovacao.dto.AndamentoProjetoDTO;
import api_inovacao.dto.ProjetoRequestDTO;
import api_inovacao.dto.ProjetoResponseDTO;
import api_inovacao.model.EtapaProjeto;
import api_inovacao.model.StatusProjeto;
import api_inovacao.model.User;
import api_inovacao.service.ProjetoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projetos")
public class ProjetoController {

    @Autowired
    private ProjetoService projetoService;

    // Leitura: qualquer usuário logado. Ex.: /api/projetos?status=EM_ANDAMENTO&etapa=EXECUCAO
    @GetMapping
    public ResponseEntity<List<ProjetoResponseDTO>> listar(@RequestParam(required = false) StatusProjeto status,
                                                           @RequestParam(required = false) EtapaProjeto etapa) {
        return ResponseEntity.ok(projetoService.listar(status, etapa));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjetoResponseDTO> buscarPorId(@PathVariable String id) {
        return ResponseEntity.ok(projetoService.buscarPorId(id));
    }

    // Escrita: somente Gestor (regra definida no SecurityConfig)
    @PostMapping
    public ResponseEntity<ProjetoResponseDTO> criar(@RequestBody @Valid ProjetoRequestDTO dados,
                                                    @AuthenticationPrincipal User usuarioLogado) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projetoService.criar(dados, usuarioLogado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjetoResponseDTO> atualizar(@PathVariable String id,
                                                        @RequestBody @Valid ProjetoRequestDTO dados) {
        return ResponseEntity.ok(projetoService.atualizar(id, dados));
    }

    // Atualiza só o andamento (etapa + status), sem reenviar o projeto inteiro
    @PatchMapping("/{id}/andamento")
    public ResponseEntity<ProjetoResponseDTO> atualizarAndamento(@PathVariable String id,
                                                                 @RequestBody @Valid AndamentoProjetoDTO dados) {
        return ResponseEntity.ok(projetoService.atualizarAndamento(id, dados));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable String id) {
        projetoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
