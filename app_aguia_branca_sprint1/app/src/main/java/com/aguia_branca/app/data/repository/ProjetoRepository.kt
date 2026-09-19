package com.aguia_branca.app.data.repository

import com.aguia_branca.app.data.remote.RetrofitClient
import com.aguia_branca.app.data.remote.dto.AndamentoProjetoRequest
import com.aguia_branca.app.data.remote.dto.EtapaProjeto
import com.aguia_branca.app.data.remote.dto.ProjetoRequest
import com.aguia_branca.app.data.remote.dto.ProjetoResponse
import com.aguia_branca.app.data.remote.dto.StatusProjeto
import com.aguia_branca.app.data.remote.safeApiCall

class ProjetoRepository {

    suspend fun listar(status: StatusProjeto? = null, etapa: EtapaProjeto? = null): Result<List<ProjetoResponse>> =
        safeApiCall { RetrofitClient.api.listarProjetos(status, etapa) }

    suspend fun criar(dados: ProjetoRequest): Result<ProjetoResponse> =
        safeApiCall { RetrofitClient.api.criarProjeto(dados) }

    suspend fun atualizar(id: String, dados: ProjetoRequest): Result<ProjetoResponse> =
        safeApiCall { RetrofitClient.api.atualizarProjeto(id, dados) }

    suspend fun atualizarAndamento(id: String, etapa: EtapaProjeto, status: StatusProjeto): Result<ProjetoResponse> =
        safeApiCall { RetrofitClient.api.atualizarAndamentoProjeto(id, AndamentoProjetoRequest(etapa, status)) }

    suspend fun excluir(id: String): Result<Unit> =
        safeApiCall { RetrofitClient.api.excluirProjeto(id) }
}
