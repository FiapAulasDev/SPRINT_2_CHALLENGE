package api_inovacao.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "projetos")
public class Projeto {

    @Id
    private String id;

    private String titulo;
    private String descricao;

    // Andamento
    private EtapaProjeto etapa;
    private StatusProjeto status;

    // Números do projeto (BigDecimal evita erro de arredondamento do double com dinheiro)
    private BigDecimal investimentoPrevisto;
    private BigDecimal retornoEsperado;
    private BigDecimal roiPercentual; // calculado pelo service, nunca enviado pelo cliente

    // Prazo
    private LocalDate prazoInicio;
    private LocalDate prazoFim;

    private String responsavelEmail; // e-mail do Gestor que cadastrou

    // Origem: a ideia aprovada que virou este projeto (opcional)
    private String ideiaId;
    private String ideiaTitulo;

    // Estratégia herdada da ideia (ou a vigente no momento do cadastro)
    private String estrategiaId;
    private String estrategiaTitulo;

    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;
}
