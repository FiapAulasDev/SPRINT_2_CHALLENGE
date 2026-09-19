package api_inovacao.service;

import api_inovacao.dto.AnaliseViabilidadeDTO;
import api_inovacao.dto.AvaliacaoIdeiaDTO;
import api_inovacao.dto.IdeiaRequestDTO;
import api_inovacao.dto.IdeiaResponseDTO;
import api_inovacao.model.Estrategia;
import api_inovacao.model.Ideia;
import api_inovacao.model.StatusIdeia;
import api_inovacao.model.User;
import api_inovacao.repository.EstrategiaRepository;
import api_inovacao.repository.IdeiaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class IdeiaService {

    @Autowired
    private IdeiaRepository repository;

    @Autowired
    private EstrategiaRepository estrategiaRepository;

    @Autowired
    private GeminiService geminiService;

    @Autowired
    private UsuarioService usuarioService;

    // Quando o filtro ?status= não é informado, devolve todas as ideias
    public List<IdeiaResponseDTO> listar(StatusIdeia status) {
        List<Ideia> ideias = (status == null) ? repository.findAll() : repository.findByStatus(status);
        return ideias.stream()
                .map(IdeiaResponseDTO::new)
                .toList();
    }

    // O e-mail vem do token (usuário logado), nunca do JSON enviado pelo cliente
    public List<IdeiaResponseDTO> listarMinhas(User autor) {
        return repository.findByAutorEmail(autor.getEmail()).stream()
                .map(IdeiaResponseDTO::new)
                .toList();
    }

    public IdeiaResponseDTO buscarPorId(String id) {
        return new IdeiaResponseDTO(buscarEntidade(id));
    }

    public IdeiaResponseDTO criar(IdeiaRequestDTO dados, User autor) {
        Ideia ideia = new Ideia();
        preencher(ideia, dados);
        ideia.setStatus(StatusIdeia.PENDENTE); // toda ideia nasce aguardando avaliação do Gestor
        ideia.setAutorEmail(autor.getEmail());
        ideia.setCriadoEm(LocalDateTime.now());

        return new IdeiaResponseDTO(repository.save(ideia));
    }

    public IdeiaResponseDTO atualizar(String id, IdeiaRequestDTO dados, User usuarioLogado) {
        Ideia ideia = buscarEntidade(id);
        validarAutor(ideia, usuarioLogado);
        validarPendente(ideia, "Só é possível editar uma ideia que ainda está PENDENTE");

        preencher(ideia, dados);
        ideia.setAtualizadoEm(LocalDateTime.now());

        return new IdeiaResponseDTO(repository.save(ideia));
    }

    public void excluir(String id, User usuarioLogado) {
        Ideia ideia = buscarEntidade(id);
        validarAutor(ideia, usuarioLogado);
        validarPendente(ideia, "Só é possível excluir uma ideia que ainda está PENDENTE");

        repository.delete(ideia);
    }

    public IdeiaResponseDTO avaliar(String id, AvaliacaoIdeiaDTO dados, User gestor) {
        if (dados.status() == StatusIdeia.PENDENTE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A avaliação deve ser APROVADA ou REJEITADA");
        }
        if (dados.status() == StatusIdeia.REJEITADA && (dados.comentario() == null || dados.comentario().isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Para rejeitar uma ideia é obrigatório informar o comentário");
        }

        Ideia ideia = buscarEntidade(id);
        validarPendente(ideia, "Esta ideia já foi avaliada (status atual: " + ideia.getStatus() + ")");

        if (dados.status() == StatusIdeia.APROVADA) {
            // Regra do negócio: ideia aprovada nasce ligada à estratégia vigente do momento
            Estrategia vigente = estrategiaRepository.findFirstByVigenteTrue()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                            "Não há estratégia vigente cadastrada: peça ao Líder para cadastrar antes de aprovar ideias"));
            ideia.setEstrategiaId(vigente.getId());
            ideia.setEstrategiaTitulo(vigente.getTitulo());

            // Gamificação: recompensa o autor da ideia por ter tido uma sugestão aprovada
            usuarioService.adicionarPontos(ideia.getAutorEmail(), UsuarioService.PONTOS_POR_IDEIA_APROVADA);
        }

        ideia.setStatus(dados.status());
        ideia.setComentarioAvaliacao(dados.comentario());
        ideia.setAvaliadorEmail(gestor.getEmail());
        ideia.setAvaliadoEm(LocalDateTime.now());
        ideia.setAtualizadoEm(LocalDateTime.now());

        return new IdeiaResponseDTO(repository.save(ideia));
    }

    // Chama a IA (Gemini) e guarda a nota de viabilidade dentro da própria ideia
    public IdeiaResponseDTO analisarViabilidade(String id) {
        Ideia ideia = buscarEntidade(id);
        Estrategia vigente = estrategiaRepository.findFirstByVigenteTrue().orElse(null);

        AnaliseViabilidadeDTO analise = geminiService.analisarViabilidade(ideia, vigente);
        ideia.setPontuacaoViabilidade(analise.pontuacao());
        ideia.setJustificativaIa(analise.justificativa());
        ideia.setRecomendacaoIa(analise.recomendacao());
        ideia.setAnalisadoIaEm(LocalDateTime.now());

        return new IdeiaResponseDTO(repository.save(ideia));
    }

    // Lista para o Gestor priorizar: maior nota da IA primeiro, ideias ainda sem nota no fim
    public List<IdeiaResponseDTO> ranking(StatusIdeia status) {
        List<Ideia> ideias = (status == null) ? repository.findAll() : repository.findByStatus(status);
        return ideias.stream()
                .sorted(Comparator.comparing(Ideia::getPontuacaoViabilidade,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .map(IdeiaResponseDTO::new)
                .toList();
    }

    private Ideia buscarEntidade(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ideia não encontrada: " + id));
    }

    private void preencher(Ideia ideia, IdeiaRequestDTO dados) {
        ideia.setTitulo(dados.titulo());
        ideia.setDescricao(dados.descricao());
        ideia.setBeneficioEsperado(dados.beneficioEsperado());
    }

    // Um Operador não pode mexer na ideia de outro Operador
    private void validarAutor(Ideia ideia, User usuarioLogado) {
        if (!ideia.getAutorEmail().equals(usuarioLogado.getEmail())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Esta ideia pertence a outro usuário");
        }
    }

    // Depois de avaliada, a ideia vira histórico: ninguém edita, exclui ou reavalia
    private void validarPendente(Ideia ideia, String mensagem) {
        if (ideia.getStatus() != StatusIdeia.PENDENTE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, mensagem);
        }
    }
}
