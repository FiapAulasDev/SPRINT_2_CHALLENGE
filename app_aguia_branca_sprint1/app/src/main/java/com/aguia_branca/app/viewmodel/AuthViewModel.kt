package com.aguia_branca.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aguia_branca.app.data.model.User
import com.aguia_branca.app.data.remote.ApiException
import com.aguia_branca.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _ranking = MutableStateFlow<List<User>>(emptyList())
    val ranking: StateFlow<List<User>> = _ranking.asStateFlow()

    init {
        if (repository.hasStoredSession()) {
            viewModelScope.launch {
                repository.meuPerfil()
                    .onSuccess { _currentUser.value = it }
                    .onFailure { repository.logout() }
            }
        }
    }

    fun login(email: String, senha: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            repository.login(email, senha)
                .onSuccess { user ->
                    _currentUser.value = user
                    onSuccess()
                }
                .onFailure { error ->
                    onError((error as? ApiException)?.message ?: "Erro ao entrar")
                }
        }
    }

    fun logout() {
        repository.logout()
        _currentUser.value = null
    }

    // Recarrega o perfil do usuário logado (ex.: para atualizar os pontos após uma ideia ser avaliada)
    fun recarregarUsuario() {
        viewModelScope.launch {
            repository.meuPerfil().onSuccess { _currentUser.value = it }
        }
    }

    // Ranking de Inovação (gamificação): top 5 Operadores por pontos
    fun carregarRanking() {
        viewModelScope.launch {
            repository.ranking().onSuccess { _ranking.value = it }
        }
    }
}
