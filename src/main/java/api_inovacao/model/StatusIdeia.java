package api_inovacao.model;

// O enum aceita apenas esses valores: se chegar outro (ex.: "Aprovada") a API retorna 400
public enum StatusIdeia {
    PENDENTE,
    APROVADA,
    REJEITADA
}
