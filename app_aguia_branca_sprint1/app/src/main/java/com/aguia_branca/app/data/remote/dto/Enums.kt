package com.aguia_branca.app.data.remote.dto

enum class Role {
    OPERADOR, GESTOR, LIDER
}

enum class StatusIdeia {
    PENDENTE, APROVADA, REJEITADA
}

enum class EtapaProjeto {
    PLANEJAMENTO, EXECUCAO, MONITORAMENTO, ENCERRAMENTO
}

enum class StatusProjeto {
    NAO_INICIADO, EM_ANDAMENTO, CONCLUIDO, CANCELADO
}
