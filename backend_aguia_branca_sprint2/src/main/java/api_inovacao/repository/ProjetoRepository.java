package api_inovacao.repository;

import api_inovacao.model.EtapaProjeto;
import api_inovacao.model.Projeto;
import api_inovacao.model.StatusProjeto;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ProjetoRepository extends MongoRepository<Projeto, String> {

    List<Projeto> findByStatus(StatusProjeto status);

    List<Projeto> findByEtapa(EtapaProjeto etapa);

    // Usado para impedir dois projetos nascidos da mesma ideia
    Optional<Projeto> findByIdeiaId(String ideiaId);
}
