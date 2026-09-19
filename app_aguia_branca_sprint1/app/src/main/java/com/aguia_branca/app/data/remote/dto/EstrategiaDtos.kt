package com.aguia_branca.app.data.remote.dto

import java.time.LocalDate
import java.time.LocalDateTime

data class EstrategiaRequest(
    val titulo: String,
    val descricao: String,
    val dataInicio: LocalDate,
    val dataFim: LocalDate?,
    val vigente: Boolean
)

data class EstrategiaResponse(
    val id: String,
    val titulo: String,
    val descricao: String,
    val dataInicio: LocalDate,
    val dataFim: LocalDate?,
    val vigente: Boolean,
    val criadoPor: String?,
    val criadoEm: LocalDateTime?,
    val atualizadoEm: LocalDateTime?
)
