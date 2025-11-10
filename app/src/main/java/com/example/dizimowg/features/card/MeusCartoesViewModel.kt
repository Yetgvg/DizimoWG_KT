package com.example.dizimowg.features.card

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dizimowg.core.network.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MeusCartoesViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CardListUiState())
    val uiState = _uiState.asStateFlow()

    init {
        fetchSavedCards()
    }

    fun fetchSavedCards() {
        // ... (seu código de fetch está perfeito)
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val cardList = ApiClient.apiService.getMyCards()
                _uiState.update { it.copy(isLoading = false, cards = cardList) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Não foi possível carregar seus cartões.")
                }
            }
        }
    }

    fun openCvvDialog(card: SavedCard) {
        _uiState.update { it.copy(selectedCard = card, showCvvDialog = true) }
    }

    fun onFrontendTokenGenerated(token: String) {
        println("🎟️ Token V2 recebido do frontend: $token")

        val amountInCents = uiState.value.pendingAmountInCents ?: 0
        viewModelScope.launch {
            _uiState.update { it.copy(isPaymentLoading = true, errorMessage = null) }
            try {
                val request = PaymentRequest(
                    amountInCents = amountInCents,
                    token = token
                )

                val response = ApiClient.apiService.createCardPayment(request)

                if (response.status == "approved") {
                    _uiState.update { it.copy(isPaymentLoading = false, paymentSuccess = true) }
                } else {
                    _uiState.update {
                        it.copy(
                            isPaymentLoading = false,
                            errorMessage = response.statusDetail ?: "Pagamento não aprovado."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isPaymentLoading = false,
                        errorMessage = e.message ?: "Erro ao processar pagamento."
                    )
                }
            }
        }
    }

    fun onFrontendTokenError(error: String) {
        _uiState.update { it.copy(errorMessage = "Erro ao gerar token: $error") }
    }


    fun closeCvvDialog() {
        _uiState.update { it.copy(showCvvDialog = false, selectedCard = null) }
    }

    fun confirmCvvAndPay(cvv: String, amountInCents: Int) {
        val card = uiState.value.selectedCard ?: return

        _uiState.update { it.copy(showCvvDialog = false, pendingAmountInCents = amountInCents) }

        println("⚡ Solicitando geração de token via WebView para o cardId=${card.paymentMethodId}")
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

}