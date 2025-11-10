package com.example.dizimowg.features.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dizimowg.core.network.ApiClient
import com.example.dizimowg.core.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val loginSuccess: Boolean = false,
    val user: User? = null
)

class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    // Funções de evento
    fun onEmailChange(newEmail: String) {
        _uiState.update { it.copy(email = newEmail, errorMessage = null) }
    }
    fun onPasswordChange(newPassword: String) {
        _uiState.update { it.copy(password = newPassword, errorMessage = null) }
    }
    fun onTogglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun onLoginClicked() {
        if (_uiState.value.email.isBlank() || _uiState.value.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "E-mail e senha são obrigatórios.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val request = LoginRequest(
                    email = _uiState.value.email,
                    password = _uiState.value.password
                )

                val response = ApiClient.apiService.login(request)

                SessionManager.authToken = response.token

                println("Login bem-sucedido! Token: ${response.token}")

                _uiState.update { it.copy(isLoading = false, loginSuccess = true, user = response.user) }

            } catch (e: Exception) {
                println("Erro no login: ${e.message}")
                _uiState.update { it.copy(isLoading = false, errorMessage = "Usuário ou senha inválidos.") }
            }
        }
    }

    fun onLogoutClicked() {
        SessionManager.authToken = null
        _uiState.update { LoginUiState() }
    }
}