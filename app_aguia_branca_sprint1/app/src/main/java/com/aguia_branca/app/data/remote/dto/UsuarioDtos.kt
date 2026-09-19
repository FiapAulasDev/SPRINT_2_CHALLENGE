package com.aguia_branca.app.data.remote.dto

data class UsuarioResponse(
    val email: String,
    val nome: String,
    val role: Role,
    val pontos: Int
)
