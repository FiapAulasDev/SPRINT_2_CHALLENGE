package api_inovacao.controller;

import api_inovacao.dto.AvaliacaoIdeiaDTO;
import api_inovacao.dto.IdeiaRequestDTO;
import api_inovacao.dto.IdeiaResponseDTO;
import api_inovacao.model.StatusIdeia;
import api_inovacao.model.User;
import api_inovacao.service.IdeiaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ideias")
public class IdeiaController {

    @Autowired
    private IdeiaService ideiaService;

    // Leitura: qualquer usuário logado (Operador, Gestor e Líder)
    // Filtro opcional: /api/ideias?status=PENDENTE (é a tela do Gestor)
    @GetMapping
    public ResponseEntity<List<IdeiaResponseDTO>> listar(@RequestParam(required = false) StatusIdeia status) {
        return ResponseEntity.ok(ideiaService.listar(status));
    }

    // Vem antes de /{id} no arquivo por organização; o Spring já dá prioridade ao caminho fixo
    @GetMapping("/minhas")
    public ResponseEntity<List<IdeiaResponseDTO>> listarMinhas(@AuthenticationPrincipal User usuarioLogado) {
        return ResponseEntity.ok(ideiaService.listarMinhas(usuarioLogado));
    }

    // Ranking de priorização: ordenado pela nota da IA (maior primeiro)
    @GetMapping("/ranking")
    public ResponseEntity<List<IdeiaResponseDTO>> ranking(@RequestParam(required = false) StatusIdeia status) {
        return ResponseEntity.ok(ideiaService.ranking(status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<IdeiaResponseDTO> buscarPorId(@PathVariable String id) {
        return ResponseEntity.ok(ideiaService.buscarPorId(id));
    }

    // Escrita: somente Operador (regra definida no SecurityConfig)
    @PostMapping
    public ResponseEntity<IdeiaResponseDTO> criar(@RequestBody @Valid IdeiaRequestDTO dados,
                                                  @AuthenticationPrincipal User usuarioLogado) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ideiaService.criar(dados, usuarioLogado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<IdeiaResponseDTO> atualizar(@PathVariable String id,
                                                      @RequestBody @Valid IdeiaRequestDTO dados,
                                                      @AuthenticationPrincipal User usuarioLogado) {
        return ResponseEntity.ok(ideiaService.atualizar(id, dados, usuarioLogado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable String id,
                                        @AuthenticationPrincipal User usuarioLogado) {
        ideiaService.excluir(id, usuarioLogado);
        return ResponseEntity.noContent().build();
    }

    // IA: somente Gestor. Chama o Gemini e grava a pontuação de viabilidade na ideia
    @PostMapping("/{id}/analise-ia")
    public ResponseEntity<IdeiaResponseDTO> analisarComIa(@PathVariable String id) {
        return ResponseEntity.ok(ideiaService.analisarViabilidade(id));
    }

    // Avaliação: somente Gestor (regra definida no SecurityConfig)
    @PatchMapping("/{id}/avaliacao")
    public ResponseEntity<IdeiaResponseDTO> avaliar(@PathVariable String id,
                                                    @RequestBody @Valid AvaliacaoIdeiaDTO dados,
                                                    @AuthenticationPrincipal User usuarioLogado) {
        return ResponseEntity.ok(ideiaService.avaliar(id, dados, usuarioLogado));
    }
}
