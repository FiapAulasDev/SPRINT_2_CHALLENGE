package api_inovacao.repository;

import api_inovacao.model.Estrategia;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface EstrategiaRepository extends MongoRepository<Estrategia, String> {

    // Usado para vincular as Ideias aprovadas à estratégia atual
    Optional<Estrategia> findFirstByVigenteTrue();

    List<Estrategia> findByVigenteTrue();
}
