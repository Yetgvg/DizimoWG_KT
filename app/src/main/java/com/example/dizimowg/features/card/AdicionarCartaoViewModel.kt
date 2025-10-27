package com.example.dizimowg.features.card

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dizimowg.core.network.ApiClient
import com.example.dizimowg.core.network.SaveCardRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddCardUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val saveSuccess: Boolean = false
)

class AdicionarCartaoViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AddCardUiState())
    val uiState = _uiState.asStateFlow()

    fun sendTokenToBackend(token: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                // 1. Prepara a requisição para a nova rota
                val request = SaveCardRequest(token = token)

                // 2. Chama a nova rota /api/mercadopago/save-card
                // O AuthInterceptor vai adicionar o token de login automaticamente
                ApiClient.apiService.saveCard(request)

                // 3. Sucesso!
                println("Token $token enviado e cartão salvo no backend!")
                _uiState.update { it.copy(isLoading = false, saveSuccess = true) }

            } catch (e: Exception) {
                println("Erro ao salvar cartão no backend: ${e.message}")
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }
}