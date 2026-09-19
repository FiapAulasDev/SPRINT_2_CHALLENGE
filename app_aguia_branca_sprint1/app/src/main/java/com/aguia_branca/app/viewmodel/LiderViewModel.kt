package com.aguia_branca.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aguia_branca.app.data.remote.ApiException
import com.aguia_branca.app.data.remote.dto.DashboardResumoResponse
import com.aguia_branca.app.data.remote.dto.EstrategiaRequest
import com.aguia_branca.app.data.remote.dto.EstrategiaResponse
import com.aguia_branca.app.data.repository.DashboardRepository
import com.aguia_branca.app.data.repository.EstrategiaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class LiderViewModel : ViewModel() {

    private val estrategiaRepo = EstrategiaRepository()
    private val dashboardRepo = DashboardRepository()

    private val _estrategias = MutableStateFlow<List<EstrategiaResponse>>(emptyList())
    val estrategias: StateFlow<List<EstrategiaResponse>> = _estrategias.asStateFlow()

    private val _dashboard = MutableStateFlow<DashboardResumoResponse?>(null)
    val dashboard: StateFlow<DashboardResumoResponse?> = _dashboard.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun carregarEstrategias() {
        viewModelScope.launch {
            estrategiaRepo.listar()
                .onSuccess { _estrategias.value = it }
                .onFailure { _errorMessage.value = mensagemDe(it) }
        }
    }

    fun carregarDashboard() {
        viewModelScope.launch {
            dashboardRepo.resumo()
                .onSuccess { _dashboard.value = it }
                .onFailure { _errorMessage.value = mensagemDe(it) }
        }
    }

    fun criarEstrategia(
        titulo: String,
        descricao: String,
        dataInicio: LocalDate,
        dataFim: LocalDate?,
        vigente: Boolean,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            estrategiaRepo.criar(EstrategiaRequest(titulo, descricao, dataInicio, dataFim, vigente))
                .onSuccess { carregarEstrategias(); onSuccess() }
                .onFailure { onError(mensagemDe(it)) }
        }
    }

    fun atualizarEstrategia(
        id: String,
        titulo: String,
        descricao: String,
        dataInicio: LocalDate,
        dataFim: LocalDate?,
        vigente: Boolean,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            estrategiaRepo.atualizar(id, EstrategiaRequest(titulo, descricao, dataInicio, dataFim, vigente))
                .onSuccess { carregarEstrategias(); onSuccess() }
                .onFailure { onError(mensagemDe(it)) }
        }
    }

    fun excluirEstrategia(id: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            estrategiaRepo.excluir(id)
                .onSuccess { carregarEstrategias(); onSuccess() }
                .onFailure { onError(mensagemDe(it)) }
        }
    }

    private fun mensagemDe(error: Throwable): String =
        (error as? ApiException)?.message ?: "Erro inesperado"
}
