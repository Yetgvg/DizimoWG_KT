package com.example.dizimowg.features.card

import com.google.gson.annotations.SerializedName

// Data classes movidas para cá
data class SaveCardRequest(
    val token: String
)
data class SavedCard(
    val id: String,
    val last4: String,
    val brand: String,
    @SerializedName("stripePaymentMethodId") val paymentMethodId: String
)