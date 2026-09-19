package api_inovacao.repository;

import api_inovacao.model.Ideia;
import api_inovacao.model.StatusIdeia;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface IdeiaRepository extends MongoRepository<Ideia, String> {

    List<Ideia> findByStatus(StatusIdeia status); //findByStatus busca onde status é igual ao valor recebido

    List<Ideia> findByAutorEmail(String autorEmail); //findByAutorEmail busca onde autorEmail é igual ao valor recebido
}