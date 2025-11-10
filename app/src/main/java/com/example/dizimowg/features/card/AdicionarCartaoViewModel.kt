package com.example.dizimowg.features.card

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dizimowg.core.network.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject



class AdicionarCartaoViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AdicionarCartaoUiState())
    val uiState = _uiState.asStateFlow()

    fun sendTokenToBackend(jsonData: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val data = JSONObject(jsonData)
                val token = data.getString("token")
                val paymentMethodId = data.optString("paymentMethodId", null)
                val issuerId = data.optString("issuerId", null)

                val request = SaveCardRequest(
                    token = token,
                    paymentMethodId = paymentMethodId,
                    issuerId = issuerId
                )

                ApiClient.apiService.saveCard(request)

                println("✅ Token $token enviado com sucesso para o backend!")
                _uiState.update { it.copy(isLoading = false, saveSuccess = true) }

            } catch (e: Exception) {
                println("❌ Erro ao salvar cartão no backend: ${e.message}")
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onError(message: String) {
        _uiState.update { it.copy(isLoading = false, error = message) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}