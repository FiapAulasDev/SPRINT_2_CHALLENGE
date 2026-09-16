package api_inovacao.dto;

import jakarta.validation.constraints.NotBlank;

// Só os campos que o Operador envia: status, autor e datas quem define é o IdeiaService
public record IdeiaRequestDTO(
        @NotBlank String titulo,
        @NotBlank String descricao,
        @NotBlank String beneficioEsperado
) {
}
