package com.example.dizimowg

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.dizimowg.core.util.getAlternativeDeviceId
import com.example.dizimowg.features.card.AdicionarCartaoScreen
import com.example.dizimowg.features.card.MeusCartoesScreen
import com.example.dizimowg.features.card.MeusCartoesViewModel
import com.example.dizimowg.features.card.PagamentoSucessoScreen
import com.example.dizimowg.features.doacao.DoacaoScreen
import com.example.dizimowg.features.historico.HistoryScreen
import com.example.dizimowg.features.historico.history.HistoryViewModel
import com.example.dizimowg.features.home.HomeScreen
import com.example.dizimowg.features.login.LoginScreen
import com.example.dizimowg.features.login.LoginViewModel
import com.example.dizimowg.features.pix.PixScreen
import com.example.dizimowg.features.pix.PixViewModel
import com.mercadopago.sdk.android.initializer.MercadoPagoSDK
import com.mercadopago.sdk.android.domain.model.CountryCode

class MainActivity : ComponentActivity() {

    private val loginViewModel: LoginViewModel by viewModels()
    private val pixViewModel: PixViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {

                    val navController = rememberNavController()
                    val loginUiState by loginViewModel.uiState.collectAsStateWithLifecycle()
                    val loggedInUser = loginUiState.user
                    val context = LocalContext.current

                    NavHost(navController = navController, startDestination = Screen.Login.route)
                    {
                        // --- TELA: LOGIN ---
                        composable(Screen.Login.route) {
                        val loginUiState by loginViewModel.uiState.collectAsStateWithLifecycle()

                        LaunchedEffect(loginUiState.loginSuccess) {
                            if (loginUiState.loginSuccess) {
                                Toast.makeText(context, "Login bem-sucedido!", Toast.LENGTH_SHORT).show()
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            }
                        }

                            LoginScreen(
                                uiState = loginUiState,
                                onEmailChange = loginViewModel::onEmailChange,
                                onPasswordChange = loginViewModel::onPasswordChange,
                                onTogglePasswordVisibility = loginViewModel::onTogglePasswordVisibility,
                                onLoginClicked = loginViewModel::onLoginClicked
                            )
                    }

                        // --- TELA: HOME ---
                        composable(Screen.Home.route) {
                            val userName = loginViewModel.uiState.collectAsStateWithLifecycle().value.user?.nome ?: "Visitante"

                            HomeScreen(
                                userName,
                                onNavigateToDoacao = { navController.navigate(Screen.Doacao.route) },
                                onNavigateToHistory = { navController.navigate(Screen.History.route) },
                                onLogout = {
                                    loginViewModel.onLogoutClicked()

                                    navController.navigate(Screen.Login.route) {
                                        popUpTo(Screen.Home.route) { inclusive = true }
                                    }
                                }
                            )
                        }

                        // --- TELA: DOAÇÃO ---
                        composable(Screen.Doacao.route) {
                            DoacaoScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToPix = { amountInCents ->
                                    navController.navigate(Screen.Pix.createRoute(amountInCents))
                                },
                                onNavigateToCartao = { amountInCents ->
                                    navController.navigate(Screen.MeusCartoes.createRoute(amountInCents))
                                }
                            )
                        }

                        // --- TELA: PIX ---
                        composable( route = Screen.Pix.route, arguments = listOf(navArgument("amount") { type = NavType.IntType })) {
                            backStackEntry ->
                            val amount = backStackEntry.arguments?.getInt("amount") ?: 0

                            val pixUiState by pixViewModel.uiState.collectAsStateWithLifecycle()

                            PixScreen(
                                uiState = pixUiState,
                                amountToPay = amount.toDouble() / 100,
                                onCreatePix = {
                                    val amountAsDouble = amount.toDouble() / 100
                                    val userId = loggedInUser?.id ?: -1
                                    pixViewModel.createPixPayment(
                                        amount = amountAsDouble,
                                        userId = userId
                                    )
                                },
                                onCheckStatus = { paymentId ->
                                    pixViewModel.startPaymentStatusCheck(
                                        paymentId
                                    )
                                },
                                onPaymentSuccess = {
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(Screen.Home.route) { inclusive = true }
                                    }
                                }
                            )
                        }

                        // --- TELA: HISTÓRICO ---
                        composable(Screen.History.route) {
                            val historyViewModel: HistoryViewModel by viewModels()
                            val uiState by historyViewModel.uiState.collectAsStateWithLifecycle()

                            HistoryScreen(
                                uiState = uiState,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        // --- TELA: ADICIONAR CARTÃO ---
                        composable(Screen.AdicionarCartao.route) {
                            AdicionarCartaoScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        // --- TELA: MEUS CARTÕES ---
                        composable(
                            route = Screen.MeusCartoes.route, // Rota atualizada (ex: "meus_cartoes_screen/{amountInCents}")
                            arguments = listOf(navArgument("amountInCents") { type = NavType.IntType })
                        ) { backStackEntry ->

                            // Pega o valor da doação que veio pela rota
                            val amountInCents = backStackEntry.arguments?.getInt("amountInCents") ?: 0

                            MeusCartoesScreen(
                                amountInCents = amountInCents, // Passa o valor para a tela
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToAddCard = {
                                    navController.navigate(Screen.AdicionarCartao.route)
                                },
                                onPaymentSuccess = {
                                    // Navega para a tela de sucesso e limpa a pilha de pagamento
                                    navController.navigate(Screen.PagamentoSucesso.route) {
                                        popUpTo(Screen.Home.route) // Volta para a Home
                                    }
                                }
                            )
                        }

                        composable(Screen.PagamentoSucesso.route) {
                            PagamentoSucessoScreen(
                                onNavigateHome = {
                                    // Volta para a home e limpa tudo
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(Screen.Home.route) { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}