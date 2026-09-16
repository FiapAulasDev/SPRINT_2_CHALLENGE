package api_inovacao.dto;

import java.time.LocalDateTime;

// Formato único de erro da API, para o front sempre saber onde ler a mensagem
public record ErroResponseDTO(
        LocalDateTime timestamp,
        int status,
        String erro,
        String mensagem,
        String caminho
) {
}
