package com.example.dizimowg.core.network

import com.example.dizimowg.features.card.PaymentRequest
import com.example.dizimowg.features.card.PaymentResponse
import com.example.dizimowg.features.card.SaveCardRequest
import com.example.dizimowg.features.card.SavedCard
import com.example.dizimowg.features.history.HistoryResponse
import com.example.dizimowg.features.login.LoginRequest
import com.example.dizimowg.features.login.LoginResponse
import com.example.dizimowg.features.pix.CreatePixRequest
import com.example.dizimowg.features.pix.CreatePixResponse
import com.example.dizimowg.features.pix.StatusResponse

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

private const val BASE_URL = "http://10.0.2.2:3000/api/"

// --- Interface da API (agora com TODAS as rotas) ---
interface ApiService {
    // Rota de Login
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    // Rota para criar o PIX
    @POST("mercadopago/create-pix")
    suspend fun createPixPayment(@Body request: CreatePixRequest): CreatePixResponse

    // Rota para verificar o status do PIX
    @GET("mercadopago/status/{paymentId}")
    suspend fun getPaymentStatus(@Path("paymentId") paymentId: String): StatusResponse

    // Rota para buscar todo o histórico
    @GET("donations/history/all")
    suspend fun getHistory(): HistoryResponse

    // Rota para salvar o cartão
    @POST("mercadopago/save-card")
    suspend fun saveCard(@Body request: SaveCardRequest)

    // Rota para buscar os cartões do usuário
    @GET("mercadopago/my-cards")
    suspend fun getMyCards(): List<SavedCard>

    @POST("mercadopago/create-payment")
    suspend fun createCardPayment(@Body request: PaymentRequest): PaymentResponse
}

// --- Objeto ApiClient ---
object ApiClient {
    val apiService: ApiService by lazy {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(AuthInterceptor())
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}