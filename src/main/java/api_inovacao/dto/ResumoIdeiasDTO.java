package api_inovacao.dto;

// Funil de ideias consolidado para o painel do Líder
public record ResumoIdeiasDTO(
        long total,
        long pendentes,
        long aprovadas,
        long rejeitadas,
        double percentualAprovacao,
        long analisadasPelaIa,
        Double mediaPontuacaoIa
) {
}
