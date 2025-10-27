package com.example.dizimowg.features.history

import com.google.gson.annotations.SerializedName

// Data classes movidas para cá
data class Lancamento(
    val codigo: Int,
    val data: String,
    val valor: Double,
    @SerializedName("Natureza") val natureza: String,
    val obs: String,
    val mes: String,
    val ano: String
)
data class HistoryResponse(
    val userName: String,
    val userId: Int,
    val lancamentos: List<Lancamento>
)