package api_inovacao.dto;

// Resposta única do painel do Líder: estratégia vigente + funil de ideias + números dos projetos
public record DashboardResumoDTO(
        EstrategiaResponseDTO estrategiaVigente,
        ResumoIdeiasDTO ideias,
        ResumoProjetosDTO projetos
) {
}
