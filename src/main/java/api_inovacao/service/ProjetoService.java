package api_inovacao.service;

import api_inovacao.dto.AndamentoProjetoDTO;
import api_inovacao.dto.ProjetoRequestDTO;
import api_inovacao.dto.ProjetoResponseDTO;
import api_inovacao.model.EtapaProjeto;
import api_inovacao.model.Ideia;
import api_inovacao.model.Projeto;
import api_inovacao.model.StatusIdeia;
import api_inovacao.model.StatusProjeto;
import api_inovacao.model.User;
import api_inovacao.repository.EstrategiaRepository;
import api_inovacao.repository.IdeiaRepository;
import api_inovacao.repository.ProjetoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProjetoService {

    @Autowired
    private ProjetoRepository repository;

    @Autowired
    private IdeiaRepository ideiaRepository;

    @Autowired
    private EstrategiaRepository estrategiaRepository;

    // Os filtros sao opcionais e combinaveis: ?status=EM_ANDAMENTO&etapa=EXECUCAO
    public List<ProjetoResponseDTO> listar(StatusProjeto status, EtapaProjeto etapa) {
        List<Projeto> projetos;
        if (status != null) {
            projetos = repository.findByStatus(status);
        } else if (etapa != null) {
            projetos = repository.findByEtapa(etapa);
        } else {
            projetos = repository.findAll();
        }

        return projetos.stream()
                .filter(p -> etapa == null || p.getEtapa() == etapa)
                .filter(p -> status == null || p.getStatus() == status)
                .map(ProjetoResponseDTO::new)
                .toList();
    }

    public ProjetoResponseDTO buscarPorId(String id) {
        return new ProjetoResponseDTO(buscarEntidade(id));
    }

    public ProjetoResponseDTO criar(ProjetoRequestDTO dados, User gestor) {
        validarPrazo(dados);

        Projeto projeto = new Projeto();
        preencher(projeto, dados);
        vincularOrigem(projeto, dados.ideiaId());
        projeto.setResponsavelEmail(gestor.getEmail());
        projeto.setCriadoEm(LocalDateTime.now());

        return new ProjetoResponseDTO(repository.save(projeto));
    }

    public ProjetoResponseDTO atualizar(String id, ProjetoRequestDTO dados) {
        validarPrazo(dados);
        Projeto projeto = buscarEntidade(id);

        preencher(projeto, dados);
        // Só revincula se o cliente mandou uma ideia diferente da que já estava salva
        if (dados.ideiaId() != null && !dados.ideiaId().equals(projeto.getIdeiaId())) {
            vincularOrigem(projeto, dados.ideiaId());
        }
        projeto.setAtualizadoEm(LocalDateTime.now());

        return new ProjetoResponseDTO(repository.save(projeto));
    }

    // Atalho do dia a dia do Gestor: mexer só na etapa e no status
    public ProjetoResponseDTO atualizarAndamento(String id, AndamentoProjetoDTO dados) {
        Projeto projeto = buscarEntidade(id);
        if (projeto.getStatus() == StatusProjeto.CANCELADO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Projeto cancelado não pode ter o andamento alterado");
        }

        projeto.setEtapa(dados.etapa());
        projeto.setStatus(dados.status());
        projeto.setAtualizadoEm(LocalDateTime.now());

        return new ProjetoResponseDTO(repository.save(projeto));
    }

    public void excluir(String id) {
        repository.delete(buscarEntidade(id));
    }

    private Projeto buscarEntidade(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Projeto não encontrado: " + id));
    }

    private void preencher(Projeto projeto, ProjetoRequestDTO dados) {
        projeto.setTitulo(dados.titulo());
        projeto.setDescricao(dados.descricao());
        projeto.setEtapa(dados.etapa());
        projeto.setStatus(dados.status());
        projeto.setInvestimentoPrevisto(dados.investimentoPrevisto());
        projeto.setRetornoEsperado(dados.retornoEsperado());
        projeto.setPrazoInicio(dados.prazoInicio());
        projeto.setPrazoFim(dados.prazoFim());
        projeto.setRoiPercentual(calcularRoi(dados.investimentoPrevisto(), dados.retornoEsperado()));
    }

    // ROI em % = (retorno - investimento) / investimento * 100
    private BigDecimal calcularRoi(BigDecimal investimento, BigDecimal retorno) {
        if (investimento == null || retorno == null || investimento.compareTo(BigDecimal.ZERO) == 0) {
            return null; // sem investimento não existe ROI para calcular (evita divisão por zero)
        }
        return retorno.subtract(investimento)
                .divide(investimento, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    // Copia os dados da ideia aprovada (e da estratégia dela) para dentro do projeto
    private void vincularOrigem(Projeto projeto, String ideiaId) {
        if (ideiaId == null || ideiaId.isBlank()) {
            // Projeto sem ideia de origem: ainda assim tentamos ligar na estratégia vigente
            estrategiaRepository.findFirstByVigenteTrue().ifPresent(estrategia -> {
                projeto.setEstrategiaId(estrategia.getId());
                projeto.setEstrategiaTitulo(estrategia.getTitulo());
            });
            return;
        }

        Ideia ideia = ideiaRepository.findById(ideiaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ideia não encontrada: " + ideiaId));

        if (ideia.getStatus() != StatusIdeia.APROVADA) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Só ideias APROVADAS podem virar projeto (status atual: " + ideia.getStatus() + ")");
        }

        // Uma ideia aprovada gera um projeto só
        repository.findByIdeiaId(ideiaId).ifPresent(existente -> {
            if (!existente.getId().equals(projeto.getId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Esta ideia já virou o projeto " + existente.getId());
            }
        });

        projeto.setIdeiaId(ideia.getId());
        projeto.setIdeiaTitulo(ideia.getTitulo());
        projeto.setEstrategiaId(ideia.getEstrategiaId());
        projeto.setEstrategiaTitulo(ideia.getEstrategiaTitulo());
    }

    private void validarPrazo(ProjetoRequestDTO dados) {
        if (dados.prazoFim().isBefore(dados.prazoInicio())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O prazo final não pode ser anterior ao prazo inicial");
        }
    }
}
