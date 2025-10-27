package com.example.dizimowg

sealed class Screen(val route: String) {
    object Login : Screen("login_screen")
    object Home : Screen( "home_screen")
    object Doacao : Screen("doacao_screen")
    object Pix : Screen("pix_screen/{amount}") {
        fun createRoute(amount: Int) = "pix_screen/$amount"
    }
    object History : Screen( "history_screen")
    object AdicionarCartao : Screen("adicionar_cartao_screen")
}