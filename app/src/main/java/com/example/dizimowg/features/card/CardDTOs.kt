package com.example.dizimowg.features.card

import com.google.gson.annotations.SerializedName

// (Sua classe de request para salvar o cartão)
data class SaveCardRequest(
    val token: String,
    val paymentMethodId: String?,
    val issuerId: String?
)
// (Seu AdicionarCartaoUiState)
data class AdicionarCartaoUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false
)

// --- DTO DO CARTÃO SALVO (ATUALIZADO) ---
data class SavedCard(
    val id: String, // ID do nosso banco (o hash cuid)
    val last4: String,
    val brand: String, // ex: "master"
    @SerializedName("stripePaymentMethodId") val paymentMethodId: String, // ID do cartão no MP (ex: "970...")
    val issuerId: String // <-- ADICIONADO: ID do banco (ex: "12518")
)

// --- ESTADO DA UI ---
data class CardListUiState(
    val cards: List<SavedCard> = emptyList(),
    val isLoading: Boolean = false,
    val isPaymentLoading: Boolean = false,
    val paymentSuccess: Boolean = false,
    val errorMessage: String? = null,
    val showCvvDialog: Boolean = false,
    val selectedCard: SavedCard? = null,
    val pendingAmountInCents: Int? = null
)

// --- DTO DE PAGAMENTO (SEM MUDANÇAS) ---
data class PaymentRequest(
    val amountInCents: Int,
    val token: String
)

// --- DTO DE RESPOSTA (SEM MUDANÇAS) ---
data class PaymentResponse(
    val id: Long,
    val status: String,
    @SerializedName("status_detail") val statusDetail: String
)