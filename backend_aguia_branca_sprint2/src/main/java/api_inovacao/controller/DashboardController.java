package api_inovacao.controller;

import api_inovacao.dto.DashboardResumoDTO;
import api_inovacao.dto.ResumoIdeiasDTO;
import api_inovacao.dto.ResumoProjetosDTO;
import api_inovacao.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Painel do Líder: somente quem tem a role LIDER acessa (regra no SecurityConfig)
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    // Uma chamada só, com tudo que a tela do Líder precisa
    @GetMapping("/resumo")
    public ResponseEntity<DashboardResumoDTO> resumo() {
        return ResponseEntity.ok(dashboardService.resumoGeral());
    }

    @GetMapping("/ideias")
    public ResponseEntity<ResumoIdeiasDTO> ideias() {
        return ResponseEntity.ok(dashboardService.resumoIdeias());
    }

    @GetMapping("/projetos")
    public ResponseEntity<ResumoProjetosDTO> projetos() {
        return ResponseEntity.ok(dashboardService.resumoProjetos());
    }
}
