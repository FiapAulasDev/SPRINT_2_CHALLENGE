package api_inovacao.dto;

import api_inovacao.model.StatusIdeia;
import jakarta.validation.constraints.NotNull;

// Corpo do PATCH /api/ideias/{id}/avaliacao (só o Gestor usa)
// O comentário é opcional aqui: a regra "REJEITADA exige comentário" fica no service
public record AvaliacaoIdeiaDTO(
        @NotNull StatusIdeia status,
        String comentario
) {
}
