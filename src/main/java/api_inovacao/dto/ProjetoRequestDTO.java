package api_inovacao.dto;

import api_inovacao.model.EtapaProjeto;
import api_inovacao.model.StatusProjeto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;

// O ROI não entra aqui: ele é calculado pelo service a partir do investimento e do retorno
public record ProjetoRequestDTO(
        @NotBlank String titulo,
        @NotBlank String descricao,
        @NotNull EtapaProjeto etapa,
        @NotNull StatusProjeto status,
        @NotNull @PositiveOrZero BigDecimal investimentoPrevisto,
        @NotNull @PositiveOrZero BigDecimal retornoEsperado,
        @NotNull LocalDate prazoInicio,
        @NotNull LocalDate prazoFim,
        String ideiaId // opcional: id da ideia APROVADA que originou o projeto
) {
}
