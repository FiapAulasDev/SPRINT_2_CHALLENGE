package api_inovacao.service;

import api_inovacao.dto.EstrategiaRequestDTO;
import api_inovacao.dto.EstrategiaResponseDTO;
import api_inovacao.model.Estrategia;
import api_inovacao.model.User;
import api_inovacao.repository.EstrategiaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EstrategiaService {

    @Autowired
    private EstrategiaRepository repository;

    public List<EstrategiaResponseDTO> listar() {
        return repository.findAll().stream()
                .map(EstrategiaResponseDTO::new)
                .toList();
    }

    public EstrategiaResponseDTO buscarPorId(String id) {
        return new EstrategiaResponseDTO(buscarEntidade(id));
    }

    public EstrategiaResponseDTO buscarVigente() {
        return repository.findFirstByVigenteTrue()
                .map(EstrategiaResponseDTO::new)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nenhuma estratégia vigente cadastrada"));
    }

    public EstrategiaResponseDTO criar(EstrategiaRequestDTO dados, User lider) {
        validarDatas(dados);
        if (dados.vigente()) {
            desmarcarVigentes();
        }

        Estrategia estrategia = new Estrategia();
        preencher(estrategia, dados);
        estrategia.setCriadoPor(lider.getEmail());
        estrategia.setCriadoEm(LocalDateTime.now());

        return new EstrategiaResponseDTO(repository.save(estrategia));
    }

    public EstrategiaResponseDTO atualizar(String id, EstrategiaRequestDTO dados) {
        validarDatas(dados);
        Estrategia estrategia = buscarEntidade(id);
        if (dados.vigente() && !estrategia.isVigente()) {
            desmarcarVigentes();
        }

        preencher(estrategia, dados);
        estrategia.setAtualizadoEm(LocalDateTime.now());

        return new EstrategiaResponseDTO(repository.save(estrategia));
    }

    public void excluir(String id) {
        repository.delete(buscarEntidade(id));
    }

    private Estrategia buscarEntidade(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Estratégia não encontrada: " + id));
    }

    private void preencher(Estrategia estrategia, EstrategiaRequestDTO dados) {
        estrategia.setTitulo(dados.titulo());
        estrategia.setDescricao(dados.descricao());
        estrategia.setDataInicio(dados.dataInicio());
        estrategia.setDataFim(dados.dataFim());
        estrategia.setVigente(dados.vigente());
    }

    private void validarDatas(EstrategiaRequestDTO dados) {
        if (dados.dataFim() != null && dados.dataFim().isBefore(dados.dataInicio())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A data de fim não pode ser anterior à data de início");
        }
    }

    // Garante a regra de ter só uma estratégia vigente por vez
    private void desmarcarVigentes() {
        List<Estrategia> vigentes = repository.findByVigenteTrue();
        vigentes.forEach(e -> e.setVigente(false));
        repository.saveAll(vigentes);
    }
}
