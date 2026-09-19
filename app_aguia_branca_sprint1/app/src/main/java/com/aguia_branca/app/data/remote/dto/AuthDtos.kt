package com.aguia_branca.app.data.remote.dto

data class AuthenticationRequest(
    val email: String,
    val senha: String
)

data class RegisterRequest(
    val nome: String,
    val email: String,
    val senha: String,
    val role: Role
)

data class LoginResponse(
    val token: String
)
