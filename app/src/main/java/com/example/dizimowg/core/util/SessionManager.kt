package com.example.dizimowg.core.util

// Um objeto singleton que viverá durante todo o ciclo de vida do app.
// Ele serve como nosso "cofre" em memória para o token.
object SessionManager {
    var authToken: String? = null
}