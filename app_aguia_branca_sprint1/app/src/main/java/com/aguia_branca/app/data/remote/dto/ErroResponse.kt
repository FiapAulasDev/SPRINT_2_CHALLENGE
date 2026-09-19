package com.aguia_branca.app.data.remote.dto

data class ErroResponse(
    val timestamp: String?,
    val status: Int,
    val erro: String?,
    val mensagem: String?,
    val caminho: String?
)
