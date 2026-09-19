package api_inovacao.dto;

import api_inovacao.model.Ideia;
import api_inovacao.model.StatusIdeia;

import java.time.LocalDateTime;

public record IdeiaResponseDTO(
        String id,
        String titulo,
        String descricao,
        String beneficioEsperado,
        StatusIdeia status,
        String autorEmail,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm,
        String avaliadorEmail,
        String comentarioAvaliacao,
        LocalDateTime avaliadoEm,
        String estrategiaId,
        String estrategiaTitulo,
        Integer pontuacaoViabilidade,
        String justificativaIa,
        String recomendacaoIa,
        LocalDateTime analisadoIaEm
) {
    // Converte a entidade do banco no DTO que vai para o cliente
    public IdeiaResponseDTO(Ideia ideia) {
        this(ideia.getId(), ideia.getTitulo(), ideia.getDescricao(), ideia.getBeneficioEsperado(),
                ideia.getStatus(), ideia.getAutorEmail(), ideia.getCriadoEm(), ideia.getAtualizadoEm(),
                ideia.getAvaliadorEmail(), ideia.getComentarioAvaliacao(), ideia.getAvaliadoEm(),
                ideia.getEstrategiaId(), ideia.getEstrategiaTitulo(),
                ideia.getPontuacaoViabilidade(), ideia.getJustificativaIa(), ideia.getRecomendacaoIa(),
                ideia.getAnalisadoIaEm());
    }
}
