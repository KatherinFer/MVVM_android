package com.example.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.model.User
import com.example.app.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class UiState {
    object Loading : UiState()
    data class Success(val users: List<User>) : UiState()
    data class Error(val message: String) : UiState()
}

class UserViewModel(
    private val repository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    init {
        // Observamos los cambios en la base de datos y filtramos según la búsqueda
        viewModelScope.launch {
            repository.allUsers.collect { users ->
                filterUsers(users, _searchQuery.value)
            }
        }
        loadUsers()
    }

    fun loadUsers() {
        viewModelScope.launch {
            // No cambiamos a Loading si ya hay datos para evitar parpadeos,
            // pero podemos hacerlo si queremos mostrar progreso de red.
            try {
                repository.refreshUsers()
            } catch (e: Exception) {
                if (_uiState.value !is UiState.Success) {
                    _uiState.value = UiState.Error(e.message ?: "Error de red")
                }
            }
        }
    }

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
        viewModelScope.launch {
            repository.allUsers.collect { users ->
                filterUsers(users, newQuery)
            }
        }
    }

    private fun filterUsers(users: List<User>, query: String) {
        val filtered = if (query.isEmpty()) {
            users
        } else {
            users.filter { it.name.contains(query, ignoreCase = true) }
        }
        _uiState.value = UiState.Success(filtered)
    }
}
