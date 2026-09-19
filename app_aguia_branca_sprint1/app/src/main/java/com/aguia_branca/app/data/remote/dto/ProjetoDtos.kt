package com.aguia_branca.app.data.remote.dto

import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

data class ProjetoRequest(
    val titulo: String,
    val descricao: String,
    val etapa: EtapaProjeto,
    val status: StatusProjeto,
    val investimentoPrevisto: BigDecimal,
    val retornoEsperado: BigDecimal,
    val prazoInicio: LocalDate,
    val prazoFim: LocalDate,
    val ideiaId: String?
)

data class AndamentoProjetoRequest(
    val etapa: EtapaProjeto,
    val status: StatusProjeto
)

data class ProjetoResponse(
    val id: String,
    val titulo: String,
    val descricao: String,
    val etapa: EtapaProjeto,
    val status: StatusProjeto,
    val investimentoPrevisto: BigDecimal,
    val retornoEsperado: BigDecimal,
    val roiPercentual: BigDecimal?,
    val prazoInicio: LocalDate,
    val prazoFim: LocalDate,
    val responsavelEmail: String?,
    val ideiaId: String?,
    val ideiaTitulo: String?,
    val estrategiaId: String?,
    val estrategiaTitulo: String?,
    val criadoEm: LocalDateTime?,
    val atualizadoEm: LocalDateTime?
)
