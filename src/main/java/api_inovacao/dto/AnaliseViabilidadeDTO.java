package api_inovacao.dto;

// Resultado que a IA devolve para uma ideia
public record AnaliseViabilidadeDTO(
        Integer pontuacao,      // 0 a 100: quanto maior, mais viável
        String justificativa,
        String recomendacao
) {
}
