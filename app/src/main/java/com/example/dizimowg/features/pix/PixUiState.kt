package com.example.dizimowg.features.pix

data class PixData(
    val paymentId: String,
    val qrCodeBase64: String,
    val copiaECola: String
)

interface PixUiState {
    object Idle : PixUiState
    object Loading : PixUiState
    data class PixReady(val data: PixData) : PixUiState
    object Approved : PixUiState
    data class Error(val message: String) : PixUiState
}