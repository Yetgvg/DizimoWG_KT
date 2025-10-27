package com.example.dizimowg.features.pix

import com.google.gson.annotations.SerializedName

// Data classes movidas para cá
data class CreatePixRequest(val userId: Int, val amount: Int)
data class CreatePixResponse(
    @SerializedName("paymentId") val paymentId: Long,
    @SerializedName("status") val status: String,
    @SerializedName("qrCodeBase64") val qrCodeBase64: String,
    @SerializedName("copiaECola") val copiaECola: String
)
data class StatusResponse(val id: Long, val status: String)

// A PixData que é usada na UI já está no seu PixUiState.kt, o que está perfeito.