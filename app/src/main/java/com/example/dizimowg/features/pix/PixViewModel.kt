package com.example.dizimowg.features.pix

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dizimowg.core.network.ApiClient
import com.example.dizimowg.core.network.CreatePixRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PixViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<PixUiState>(PixUiState.Idle)
    val uiState = _uiState.asStateFlow()

    /**
     * Chama o backend para criar um pagamento PIX.
     */
    fun createPixPayment(amount: Double, userId: Int) {
        viewModelScope.launch {
            _uiState.update { PixUiState.Loading }
            try {
                // Converte o valor em Reais para centavos (Int)
                val amountInCents = (amount * 100).toInt()

                // Cria a requisição com os parâmetros corretos
                val request = CreatePixRequest(userId = userId, amount = amountInCents)

                // Chama a API através do nosso ApiClient
                val response = ApiClient.apiService.createPixPayment(request)

                // Mapeia a resposta do backend para o nosso objeto de dados da UI
                val pixData = PixData(
                    paymentId = response.paymentId,
                    copiaECola = response.copiaECola,
                    qrCodeBase64 = response.qrCodeBase64
                )
                _uiState.update { PixUiState.PixReady(pixData) }

            } catch (e: Exception) {
                // Em caso de erro, atualiza o estado da UI com a mensagem
                _uiState.update { PixUiState.Error("Falha ao gerar PIX: ${e.message}") }
            }
        }
    }

    /**
     * Inicia a verificação de status em loop (polling) contra o backend.
     */
    fun startPaymentStatusCheck(paymentId: String) {
        viewModelScope.launch {
            while (true) {
                try {
                    // Espera 5 segundos entre cada verificação
                    delay(5000)

                    val response = ApiClient.apiService.getPaymentStatus(paymentId)

                    if (response.status == "approved" || response.status == "processed") {
                        _uiState.update { PixUiState.Approved }
                        break // Para o loop se o pagamento foi aprovado
                    }
                    // Se o status for "pending" ou outro, o loop continua

                } catch (e: Exception) {
                    _uiState.update { PixUiState.Error("Erro ao verificar status: ${e.message}") }
                    break // Para o loop em caso de erro de rede
                }
            }
        }
    }
}