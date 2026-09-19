package api_inovacao.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record EstrategiaRequestDTO(
        @NotBlank String titulo,
        @NotBlank String descricao,
        @NotNull LocalDate dataInicio,
        LocalDate dataFim,
        boolean vigente
) {
}
