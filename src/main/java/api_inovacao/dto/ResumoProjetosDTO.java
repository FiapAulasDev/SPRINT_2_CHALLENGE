package api_inovacao.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

// Números somados dos projetos: é daqui que saem o ROI total e o lucro do painel do Líder
public record ResumoProjetosDTO(
        long total,
        Map<String, Long> porStatus,
        Map<String, Long> porEtapa,
        BigDecimal investimentoTotal,
        BigDecimal retornoTotal,
        BigDecimal lucroTotal,          // retorno - investimento
        BigDecimal roiTotalPercentual,  // lucro / investimento * 100
        List<ProjetoResponseDTO> topProjetosPorRoi
) {
}
