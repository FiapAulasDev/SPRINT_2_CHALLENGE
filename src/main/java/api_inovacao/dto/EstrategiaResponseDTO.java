package api_inovacao.dto;

import api_inovacao.model.Estrategia;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record EstrategiaResponseDTO(
        String id,
        String titulo,
        String descricao,
        LocalDate dataInicio,
        LocalDate dataFim,
        boolean vigente,
        String criadoPor,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm
) {
    // Converte a entidade do banco no DTO que vai para o cliente
    public EstrategiaResponseDTO(Estrategia estrategia) {
        this(estrategia.getId(), estrategia.getTitulo(), estrategia.getDescricao(),
                estrategia.getDataInicio(), estrategia.getDataFim(), estrategia.isVigente(),
                estrategia.getCriadoPor(), estrategia.getCriadoEm(), estrategia.getAtualizadoEm());
    }
}
