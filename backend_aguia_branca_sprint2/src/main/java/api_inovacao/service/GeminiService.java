package api_inovacao.service;

import api_inovacao.dto.AnaliseViabilidadeDTO;
import api_inovacao.model.Estrategia;
import api_inovacao.model.Ideia;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Integração com a API gratuita do Google Gemini.
 * Recebe uma ideia e devolve uma pontuação de viabilidade (0 a 100) para o Gestor priorizar.
 */
@Service
public class GeminiService {

    private final RestClient restClient;
    private final String apiKey;
    private final String modelo;

    @Autowired
    private ObjectMapper objectMapper;

    public GeminiService(@Value("${gemini.api.url}") String url,
                         @Value("${gemini.api.key:}") String apiKey,
                         @Value("${gemini.api.modelo}") String modelo) {
        this.apiKey = apiKey;
        this.modelo = modelo;

        // Timeouts para a API nunca ficar travada esperando o Google responder
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofSeconds(60)); // o Gemini as vezes passa de 30s; abaixo disso a chamada era cancelada

        this.restClient = RestClient.builder()
                .requestFactory(factory)
                .baseUrl(url)
                .build();
    }

    public AnaliseViabilidadeDTO analisarViabilidade(Ideia ideia, Estrategia estrategiaVigente) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "IA não configurada: defina GEMINI_API_KEY no arquivo .env e reinicie a aplicação");
        }

        Map<String, Object> corpo = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", montarPrompt(ideia, estrategiaVigente))))),
                "generationConfig", Map.of(
                        "temperature", 0.2,              // respostas mais estáveis, menos "criativas"
                        "responseMimeType", "application/json", // obriga o Gemini a responder em JSON
                        // Desliga o "raciocínio" do modelo: a nota sai em segundos em vez de quase um minuto
                        "thinkingConfig", Map.of("thinkingBudget", 0)
                )
        );

        String respostaBruta;
        try {
            respostaBruta = restClient.post()
                    .uri("/models/{modelo}:generateContent", modelo)
                    .header("x-goog-api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(corpo)
                    .retrieve()
                    .body(String.class);
        } catch (RestClientResponseException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "O Gemini recusou a requisição (HTTP " + ex.getStatusCode().value() + "). Verifique a GEMINI_API_KEY e o modelo configurado.");
        } catch (RestClientException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Não foi possível falar com o Gemini: " + ex.getMessage());
        }

        return extrairAnalise(respostaBruta);
    }

    private String montarPrompt(Ideia ideia, Estrategia estrategiaVigente) {
        String contextoEstrategia = (estrategiaVigente == null)
                ? "A empresa não tem estratégia vigente cadastrada no momento."
                : "Estratégia vigente da empresa: " + estrategiaVigente.getTitulo() + " - " + estrategiaVigente.getDescricao();

        return """
                Você é um analista de inovação da Águia Branca, empresa de transporte de passageiros e logística.
                Avalie a viabilidade da ideia abaixo considerando custo, esforço de implantação, risco e alinhamento com a estratégia.

                %s

                Ideia:
                - Título: %s
                - Descrição: %s
                - Benefício esperado: %s

                Responda SOMENTE com um JSON neste formato, sem texto fora do JSON:
                {"pontuacao": <numero inteiro de 0 a 100>, "justificativa": "<até 3 frases>", "recomendacao": "<uma frase de próximo passo>"}
                """.formatted(contextoEstrategia, ideia.getTitulo(), ideia.getDescricao(), ideia.getBeneficioEsperado());
    }

    // A resposta do Gemini vem aninhada: candidates[0].content.parts[0].text traz o JSON que pedimos
    private AnaliseViabilidadeDTO extrairAnalise(String respostaBruta) {
        try {
            JsonNode raiz = objectMapper.readTree(respostaBruta);
            String textoJson = raiz.path("candidates").path(0).path("content").path("parts").path(0).path("text").asText();

            JsonNode analise = objectMapper.readTree(textoJson);
            int pontuacao = Math.max(0, Math.min(100, analise.path("pontuacao").asInt()));

            return new AnaliseViabilidadeDTO(
                    pontuacao,
                    analise.path("justificativa").asText(null),
                    analise.path("recomendacao").asText(null));
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Não entendi a resposta da IA (formato inesperado): " + ex.getMessage());
        }
    }
}
