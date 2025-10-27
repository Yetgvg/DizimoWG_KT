package com.example.dizimowg.features.login

// Data classes movidas para cá
data class LoginRequest(val email: String, val password: String)
data class User(val id: Int, val nome: String, val email: String)
data class LoginResponse(val message: String, val token: String, val user: User)