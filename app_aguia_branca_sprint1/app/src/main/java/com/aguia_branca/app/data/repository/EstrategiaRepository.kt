package com.aguia_branca.app.data.repository

import com.aguia_branca.app.data.remote.RetrofitClient
import com.aguia_branca.app.data.remote.dto.EstrategiaRequest
import com.aguia_branca.app.data.remote.dto.EstrategiaResponse
import com.aguia_branca.app.data.remote.safeApiCall

class EstrategiaRepository {

    suspend fun listar(): Result<List<EstrategiaResponse>> =
        safeApiCall { RetrofitClient.api.listarEstrategias() }

    suspend fun buscarVigente(): Result<EstrategiaResponse> =
        safeApiCall { RetrofitClient.api.buscarEstrategiaVigente() }

    suspend fun criar(dados: EstrategiaRequest): Result<EstrategiaResponse> =
        safeApiCall { RetrofitClient.api.criarEstrategia(dados) }

    suspend fun atualizar(id: String, dados: EstrategiaRequest): Result<EstrategiaResponse> =
        safeApiCall { RetrofitClient.api.atualizarEstrategia(id, dados) }

    suspend fun excluir(id: String): Result<Unit> =
        safeApiCall { RetrofitClient.api.excluirEstrategia(id) }
}
