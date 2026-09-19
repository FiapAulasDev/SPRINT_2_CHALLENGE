package com.aguia_branca.app.data.repository

import com.aguia_branca.app.data.remote.RetrofitClient
import com.aguia_branca.app.data.remote.dto.AvaliacaoIdeiaRequest
import com.aguia_branca.app.data.remote.dto.IdeiaRequest
import com.aguia_branca.app.data.remote.dto.IdeiaResponse
import com.aguia_branca.app.data.remote.dto.StatusIdeia
import com.aguia_branca.app.data.remote.safeApiCall

class IdeiaRepository {

    suspend fun listar(status: StatusIdeia? = null): Result<List<IdeiaResponse>> =
        safeApiCall { RetrofitClient.api.listarIdeias(status) }

    suspend fun listarMinhas(): Result<List<IdeiaResponse>> =
        safeApiCall { RetrofitClient.api.listarMinhasIdeias() }

    suspend fun ranking(status: StatusIdeia? = null): Result<List<IdeiaResponse>> =
        safeApiCall { RetrofitClient.api.rankingIdeias(status) }

    suspend fun criar(dados: IdeiaRequest): Result<IdeiaResponse> =
        safeApiCall { RetrofitClient.api.criarIdeia(dados) }

    suspend fun atualizar(id: String, dados: IdeiaRequest): Result<IdeiaResponse> =
        safeApiCall { RetrofitClient.api.atualizarIdeia(id, dados) }

    suspend fun excluir(id: String): Result<Unit> =
        safeApiCall { RetrofitClient.api.excluirIdeia(id) }

    suspend fun analisarComIa(id: String): Result<IdeiaResponse> =
        safeApiCall { RetrofitClient.api.analisarIdeiaComIa(id) }

    suspend fun avaliar(id: String, status: StatusIdeia, comentario: String?): Result<IdeiaResponse> =
        safeApiCall { RetrofitClient.api.avaliarIdeia(id, AvaliacaoIdeiaRequest(status, comentario)) }
}
