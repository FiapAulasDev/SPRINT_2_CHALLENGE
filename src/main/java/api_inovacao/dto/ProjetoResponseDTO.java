package api_inovacao.dto;

import api_inovacao.model.EtapaProjeto;
import api_inovacao.model.Projeto;
import api_inovacao.model.StatusProjeto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ProjetoResponseDTO(
        String id,
        String titulo,
        String descricao,
        EtapaProjeto etapa,
        StatusProjeto status,
        BigDecimal investimentoPrevisto,
        BigDecimal retornoEsperado,
        BigDecimal roiPercentual,
        LocalDate prazoInicio,
        LocalDate prazoFim,
        String responsavelEmail,
        String ideiaId,
        String ideiaTitulo,
        String estrategiaId,
        String estrategiaTitulo,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm
) {
    // Converte a entidade do banco no DTO que vai para o cliente
    public ProjetoResponseDTO(Projeto projeto) {
        this(projeto.getId(), projeto.getTitulo(), projeto.getDescricao(), projeto.getEtapa(), projeto.getStatus(),
                projeto.getInvestimentoPrevisto(), projeto.getRetornoEsperado(), projeto.getRoiPercentual(),
                projeto.getPrazoInicio(), projeto.getPrazoFim(), projeto.getResponsavelEmail(),
                projeto.getIdeiaId(), projeto.getIdeiaTitulo(), projeto.getEstrategiaId(), projeto.getEstrategiaTitulo(),
                projeto.getCriadoEm(), projeto.getAtualizadoEm());
    }
}
