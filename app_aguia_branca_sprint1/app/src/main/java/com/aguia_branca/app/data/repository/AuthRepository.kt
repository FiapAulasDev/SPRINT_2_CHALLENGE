package com.aguia_branca.app.data.repository

import com.aguia_branca.app.data.local.TokenStore
import com.aguia_branca.app.data.model.User
import com.aguia_branca.app.data.remote.JwtUtils
import com.aguia_branca.app.data.remote.RetrofitClient
import com.aguia_branca.app.data.remote.dto.AuthenticationRequest
import com.aguia_branca.app.data.remote.dto.UsuarioResponse
import com.aguia_branca.app.data.remote.safeApiCall

class AuthRepository {

    suspend fun login(email: String, senha: String): Result<User> {
        val loginResult = safeApiCall { RetrofitClient.api.login(AuthenticationRequest(email, senha)) }
        val token = loginResult.getOrElse { return Result.failure(it) }.token
        TokenStore.saveToken(token)
        return meuPerfil()
    }

    fun logout() {
        TokenStore.clearToken()
    }

    fun hasStoredSession(): Boolean {
        val token = TokenStore.getToken() ?: return false
        val payload = JwtUtils.decode(token) ?: return false
        if (JwtUtils.isExpired(payload)) {
            TokenStore.clearToken()
            return false
        }
        return true
    }

    suspend fun meuPerfil(): Result<User> =
        safeApiCall { RetrofitClient.api.meuUsuario() }.map { it.toUser() }

    suspend fun ranking(): Result<List<User>> =
        safeApiCall { RetrofitClient.api.rankingUsuarios() }.map { lista -> lista.map { it.toUser() } }

    private fun UsuarioResponse.toUser() = User(email = email, nome = nome, role = role.name, pontos = pontos)
}
