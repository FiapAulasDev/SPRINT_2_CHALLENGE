package com.aguia_branca.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aguia_branca.app.data.remote.ApiException
import com.aguia_branca.app.data.remote.dto.EtapaProjeto
import com.aguia_branca.app.data.remote.dto.IdeiaResponse
import com.aguia_branca.app.data.remote.dto.ProjetoRequest
import com.aguia_branca.app.data.remote.dto.ProjetoResponse
import com.aguia_branca.app.data.remote.dto.StatusIdeia
import com.aguia_branca.app.data.remote.dto.StatusProjeto
import com.aguia_branca.app.data.repository.IdeiaRepository
import com.aguia_branca.app.data.repository.ProjetoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GestorViewModel : ViewModel() {

    private val ideiaRepo = IdeiaRepository()
    private val projetoRepo = ProjetoRepository()

    private val _ideiasPendentes = MutableStateFlow<List<IdeiaResponse>>(emptyList())
    val ideiasPendentes: StateFlow<List<IdeiaResponse>> = _ideiasPendentes.asStateFlow()

    private val _rankingIdeias = MutableStateFlow<List<IdeiaResponse>>(emptyList())
    val rankingIdeias: StateFlow<List<IdeiaResponse>> = _rankingIdeias.asStateFlow()

    private val _ideiasAprovadas = MutableStateFlow<List<IdeiaResponse>>(emptyList())
    val ideiasAprovadas: StateFlow<List<IdeiaResponse>> = _ideiasAprovadas.asStateFlow()

    private val _projetos = MutableStateFlow<List<ProjetoResponse>>(emptyList())
    val projetos: StateFlow<List<ProjetoResponse>> = _projetos.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun carregar() {
        carregarIdeias()
        carregarProjetos()
    }

    private fun carregarIdeias() {
        viewModelScope.launch {
            ideiaRepo.listar(StatusIdeia.PENDENTE)
                .onSuccess { _ideiasPendentes.value = it }
                .onFailure { _errorMessage.value = mensagemDe(it) }

            ideiaRepo.ranking()
                .onSuccess { _rankingIdeias.value = it }
                .onFailure { _errorMessage.value = mensagemDe(it) }

            ideiaRepo.listar(StatusIdeia.APROVADA)
                .onSuccess { _ideiasAprovadas.value = it }
                .onFailure { _errorMessage.value = mensagemDe(it) }
        }
    }

    private fun carregarProjetos() {
        viewModelScope.launch {
            projetoRepo.listar()
                .onSuccess { _projetos.value = it }
                .onFailure { _errorMessage.value = mensagemDe(it) }
        }
    }

    fun analisarComIa(id: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            ideiaRepo.analisarComIa(id)
                .onSuccess { carregarIdeias(); onSuccess() }
                .onFailure { onError(mensagemDe(it)) }
        }
    }

    fun avaliar(id: String, status: StatusIdeia, comentario: String?, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (status == StatusIdeia.REJEITADA && comentario.isNullOrBlank()) {
            onError("Para rejeitar uma ideia é obrigatório informar o comentário")
            return
        }
        viewModelScope.launch {
            ideiaRepo.avaliar(id, status, comentario)
                .onSuccess { carregarIdeias(); onSuccess() }
                .onFailure { onError(mensagemDe(it)) }
        }
    }

    fun criarProjeto(dados: ProjetoRequest, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            projetoRepo.criar(dados)
                .onSuccess { carregarProjetos(); onSuccess() }
                .onFailure { onError(mensagemDe(it)) }
        }
    }

    fun atualizarProjeto(id: String, dados: ProjetoRequest, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            projetoRepo.atualizar(id, dados)
                .onSuccess { carregarProjetos(); onSuccess() }
                .onFailure { onError(mensagemDe(it)) }
        }
    }

    fun atualizarAndamento(id: String, etapa: EtapaProjeto, status: StatusProjeto, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            projetoRepo.atualizarAndamento(id, etapa, status)
                .onSuccess { carregarProjetos(); onSuccess() }
                .onFailure { onError(mensagemDe(it)) }
        }
    }

    fun excluirProjeto(id: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            projetoRepo.excluir(id)
                .onSuccess { carregarProjetos(); onSuccess() }
                .onFailure { onError(mensagemDe(it)) }
        }
    }

    private fun mensagemDe(error: Throwable): String =
        (error as? ApiException)?.message ?: "Erro inesperado"
}
