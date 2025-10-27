package com.example.dizimowg.core.network

import com.example.dizimowg.core.util.SessionManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        // Pega a requisição original
        val originalRequest = chain.request()

        // Pega o token do nosso SessionManager
        val authToken = SessionManager.authToken

        // Se não tiver token, apenas continua com a requisição original (ex: para o login)
        if (authToken == null) {
            return chain.proceed(originalRequest)
        }

        // Se TEM um token, cria uma nova requisição com o cabeçalho Authorization
        val newRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $authToken")
            .build()

        // Continua o fluxo com a NOVA requisição (autenticada)
        return chain.proceed(newRequest)
    }
}