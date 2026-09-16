package api_inovacao.service;

import api_inovacao.dto.DashboardResumoDTO;
import api_inovacao.dto.EstrategiaResponseDTO;
import api_inovacao.dto.ProjetoResponseDTO;
import api_inovacao.dto.ResumoIdeiasDTO;
import api_inovacao.dto.ResumoProjetosDTO;
import api_inovacao.model.Ideia;
import api_inovacao.model.Projeto;
import api_inovacao.model.StatusIdeia;
import api_inovacao.repository.EstrategiaRepository;
import api_inovacao.repository.IdeiaRepository;
import api_inovacao.repository.ProjetoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Monta os números consolidados do painel do Líder.
 * As somas são feitas em Java (BigDecimal) para não perder centavos com arredondamento.
 */
@Service
public class DashboardService {

    @Autowired
    private IdeiaRepository ideiaRepository;

    @Autowired
    private ProjetoRepository projetoRepository;

    @Autowired
    private EstrategiaRepository estrategiaRepository;

    public DashboardResumoDTO resumoGeral() {
        EstrategiaResponseDTO estrategiaVigente = estrategiaRepository.findFirstByVigenteTrue()
                .map(EstrategiaResponseDTO::new)
                .orElse(null);

        return new DashboardResumoDTO(estrategiaVigente, resumoIdeias(), resumoProjetos());
    }

    public ResumoIdeiasDTO resumoIdeias() {
        List<Ideia> ideias = ideiaRepository.findAll();

        long total = ideias.size();
        long pendentes = contarPorStatus(ideias, StatusIdeia.PENDENTE);
        long aprovadas = contarPorStatus(ideias, StatusIdeia.APROVADA);
        long rejeitadas = contarPorStatus(ideias, StatusIdeia.REJEITADA);

        double percentualAprovacao = (total == 0) ? 0.0
                : BigDecimal.valueOf(aprovadas * 100.0 / total).setScale(2, RoundingMode.HALF_UP).doubleValue();

        List<Ideia> analisadas = ideias.stream()
                .filter(ideia -> ideia.getPontuacaoViabilidade() != null)
                .toList();

        OptionalDouble media = analisadas.stream()
                .mapToInt(Ideia::getPontuacaoViabilidade)
                .average();

        Double mediaPontuacao = media.isPresent()
                ? BigDecimal.valueOf(media.getAsDouble()).setScale(2, RoundingMode.HALF_UP).doubleValue()
                : null;

        return new ResumoIdeiasDTO(total, pendentes, aprovadas, rejeitadas, percentualAprovacao,
                analisadas.size(), mediaPontuacao);
    }

    public ResumoProjetosDTO resumoProjetos() {
        List<Projeto> projetos = projetoRepository.findAll();

        Map<String, Long> porStatus = agrupar(projetos, projeto -> projeto.getStatus() == null ? "SEM_STATUS" : projeto.getStatus().name());
        Map<String, Long> porEtapa = agrupar(projetos, projeto -> projeto.getEtapa() == null ? "SEM_ETAPA" : projeto.getEtapa().name());

        BigDecimal investimentoTotal = somar(projetos, Projeto::getInvestimentoPrevisto);
        BigDecimal retornoTotal = somar(projetos, Projeto::getRetornoEsperado);
        BigDecimal lucroTotal = retornoTotal.subtract(investimentoTotal);

        // ROI consolidado: calculado sobre os totais, não é a média dos ROIs individuais
        BigDecimal roiTotal = (investimentoTotal.compareTo(BigDecimal.ZERO) == 0) ? null
                : lucroTotal.divide(investimentoTotal, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP);

        List<ProjetoResponseDTO> topPorRoi = projetos.stream()
                .filter(projeto -> projeto.getRoiPercentual() != null)
                .sorted(Comparator.comparing(Projeto::getRoiPercentual).reversed())
                .limit(3)
                .map(ProjetoResponseDTO::new)
                .toList();

        return new ResumoProjetosDTO(projetos.size(), porStatus, porEtapa,
                investimentoTotal, retornoTotal, lucroTotal, roiTotal, topPorRoi);
    }

    private long contarPorStatus(List<Ideia> ideias, StatusIdeia status) {
        return ideias.stream().filter(ideia -> ideia.getStatus() == status).count();
    }

    // LinkedHashMap mantém a ordem de inserção para o JSON sair sempre igual
    private Map<String, Long> agrupar(List<Projeto> projetos, Function<Projeto, String> chave) {
        return projetos.stream().collect(Collectors.groupingBy(chave, LinkedHashMap::new, Collectors.counting()));
    }

    private BigDecimal somar(List<Projeto> projetos, Function<Projeto, BigDecimal> campo) {
        return projetos.stream()
                .map(campo)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
