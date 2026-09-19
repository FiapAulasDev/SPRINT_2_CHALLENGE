package com.aguia_branca.app.data.remote.dto

import java.math.BigDecimal

data class ResumoIdeiasResponse(
    val total: Long,
    val pendentes: Long,
    val aprovadas: Long,
    val rejeitadas: Long,
    val percentualAprovacao: Double,
    val analisadasPelaIa: Long,
    val mediaPontuacaoIa: Double?
)

data class ResumoProjetosResponse(
    val total: Long,
    val porStatus: Map<String, Long>,
    val porEtapa: Map<String, Long>,
    val investimentoTotal: BigDecimal,
    val retornoTotal: BigDecimal,
    val lucroTotal: BigDecimal,
    val roiTotalPercentual: BigDecimal?,
    val topProjetosPorRoi: List<ProjetoResponse>
)

data class DashboardResumoResponse(
    val estrategiaVigente: EstrategiaResponse?,
    val ideias: ResumoIdeiasResponse,
    val projetos: ResumoProjetosResponse
)
