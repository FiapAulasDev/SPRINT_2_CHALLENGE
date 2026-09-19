package com.aguia_branca.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aguia_branca.app.data.remote.ApiException
import com.aguia_branca.app.data.remote.dto.IdeiaRequest
import com.aguia_branca.app.data.remote.dto.IdeiaResponse
import com.aguia_branca.app.data.repository.IdeiaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class IdeiaViewModel : ViewModel() {

    private val repository = IdeiaRepository()

    private val _minhasIdeias = MutableStateFlow<List<IdeiaResponse>>(emptyList())
    val minhasIdeias: StateFlow<List<IdeiaResponse>> = _minhasIdeias.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun carregar() {
        viewModelScope.launch {
            repository.listarMinhas()
                .onSuccess { _minhasIdeias.value = it }
                .onFailure { _errorMessage.value = mensagemDe(it) }
        }
    }

    fun criar(
        titulo: String,
        descricao: String,
        beneficioEsperado: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            repository.criar(IdeiaRequest(titulo, descricao, beneficioEsperado))
                .onSuccess { carregar(); onSuccess() }
                .onFailure { onError(mensagemDe(it)) }
        }
    }

    fun atualizar(
        id: String,
        titulo: String,
        descricao: String,
        beneficioEsperado: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            repository.atualizar(id, IdeiaRequest(titulo, descricao, beneficioEsperado))
                .onSuccess { carregar(); onSuccess() }
                .onFailure { onError(mensagemDe(it)) }
        }
    }

    fun excluir(id: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            repository.excluir(id)
                .onSuccess { carregar(); onSuccess() }
                .onFailure { onError(mensagemDe(it)) }
        }
    }

    private fun mensagemDe(error: Throwable): String =
        (error as? ApiException)?.message ?: "Erro inesperado"
}
