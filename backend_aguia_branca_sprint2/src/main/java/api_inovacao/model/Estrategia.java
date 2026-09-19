package api_inovacao.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "estrategias")
public class Estrategia {

    @Id
    private String id;

    private String titulo;
    private String descricao;
    private LocalDate dataInicio;
    private LocalDate dataFim;

    // Apenas uma estratégia pode estar vigente por vez (as Ideias aprovadas são vinculadas a ela)
    private boolean vigente;

    private String criadoPor; // e-mail do Líder que cadastrou
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;
}
