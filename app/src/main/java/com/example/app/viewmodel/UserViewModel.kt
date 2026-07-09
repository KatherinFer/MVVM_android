package com.example.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.model.User
import com.example.app.repository.NetworkException
import com.example.app.repository.ServerException
import com.example.app.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException

enum class ErrorType {
    NETWORK, TIMEOUT, SERVER, UNKNOWN
}

sealed class UiState {
    object Loading : UiState()
    data class Success(val users: List<User>) : UiState()
    data class Error(val message: String, val type: ErrorType) : UiState()
}

class UserViewModel(
    private val repository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedUser = MutableStateFlow<User?>(null)
    val selectedUser: StateFlow<User?> = _selectedUser

    init {
        // Observamos los cambios en la base de datos
        viewModelScope.launch {
            repository.allUsers.collect { users ->
                // Solo actualizamos a Success si no estamos mostrando un Error fijo
                if (_uiState.value !is UiState.Error) {
                    filterUsers(users, _searchQuery.value)
                }
            }
        }
        loadUsers()
    }

    fun loadUsers() {
        viewModelScope.launch {
            // Siempre pasamos por Loading al reintentar para limpiar el error previo
            _uiState.value = UiState.Loading
            
            try {
                repository.refreshUsers()
                // Si llegamos aquí, la descarga fue un éxito. 
                // La UI se actualizará sola a través del Flow observado en init.
            } catch (e: Exception) {
                val (message, type) = when (e) {
                    is SocketTimeoutException -> "Error de tiempo: El servidor tardó mucho" to ErrorType.TIMEOUT
                    is NetworkException -> "Error de red: Sin conexión" to ErrorType.NETWORK
                    is ServerException -> "Error de servidor: Problema interno" to ErrorType.SERVER
                    else -> (e.message ?: "Error desconocido") to ErrorType.UNKNOWN
                }
                // Establecemos el error de forma fija
                _uiState.value = UiState.Error(message, type)
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

    fun selectUser(userId: Int) {
        viewModelScope.launch {
            _selectedUser.value = repository.getUserById(userId)
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
