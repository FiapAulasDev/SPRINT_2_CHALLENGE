package api_inovacao.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "ideias")
public class Ideia {

    @Id
    private String id;

    // Dados preenchidos pelo Operador
    private String titulo;
    private String descricao;
    private String beneficioEsperado;

    // Controle
    private StatusIdeia status;
    private String autorEmail; // e-mail do Operador que cadastrou
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;

    // Avaliação feita pelo Gestor
    private String avaliadorEmail;
    private String comentarioAvaliacao;
    private LocalDateTime avaliadoEm;

    // Vínculo com a estratégia vigente (o título é copiado para evitar buscar a estratégia a cada listagem)
    private String estrategiaId;
    private String estrategiaTitulo;

    // Análise de viabilidade gerada pela IA (Google Gemini) para ajudar o Gestor a priorizar
    private Integer pontuacaoViabilidade; // 0 a 100
    private String justificativaIa;
    private String recomendacaoIa;
    private LocalDateTime analisadoIaEm;
}
