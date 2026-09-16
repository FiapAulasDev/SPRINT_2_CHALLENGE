package api_inovacao.dto;

import api_inovacao.model.EtapaProjeto;
import api_inovacao.model.StatusProjeto;
import jakarta.validation.constraints.NotNull;

// Corpo do PATCH /api/projetos/{id}/andamento: atualiza só a evolução do projeto
public record AndamentoProjetoDTO(
        @NotNull EtapaProjeto etapa,
        @NotNull StatusProjeto status
) {
}
