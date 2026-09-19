package com.aguia_branca.app.data.remote.dto

import java.time.LocalDateTime

data class IdeiaRequest(
    val titulo: String,
    val descricao: String,
    val beneficioEsperado: String
)

data class AvaliacaoIdeiaRequest(
    val status: StatusIdeia,
    val comentario: String?
)

data class IdeiaResponse(
    val id: String,
    val titulo: String,
    val descricao: String,
    val beneficioEsperado: String,
    val status: StatusIdeia,
    val autorEmail: String,
    val criadoEm: LocalDateTime?,
    val atualizadoEm: LocalDateTime?,
    val avaliadorEmail: String?,
    val comentarioAvaliacao: String?,
    val avaliadoEm: LocalDateTime?,
    val estrategiaId: String?,
    val estrategiaTitulo: String?,
    val pontuacaoViabilidade: Int?,
    val justificativaIa: String?,
    val recomendacaoIa: String?,
    val analisadoIaEm: LocalDateTime?
)
