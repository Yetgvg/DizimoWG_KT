package com.example.dizimowg.features.card

import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.json.JSONObject
import java.text.NumberFormat
import java.util.Locale

// Componente para exibir cada item da lista de cartões
@Composable
fun CardItem(card: SavedCard, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable() { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.CreditCard, contentDescription = card.brand, modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${card.brand.uppercase()} terminado em ${card.last4}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(text = "Cartão de Crédito", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            }
        }
    }
}

// --- Tela Principal ---

// Função helper para formatar moeda
fun formatCurrency(amountInCents: Int): String {
    val amount = amountInCents / 100.0
    return NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(amount)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeusCartoesScreen(
    amountInCents: Int,
    onNavigateBack: () -> Unit,
    onNavigateToAddCard: () -> Unit,
    onPaymentSuccess: () -> Unit,
    viewModel: MeusCartoesViewModel = viewModel()
) {
    // Coleta o estado do ViewModel
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val formattedAmount = remember(amountInCents) { formatCurrency(amountInCents) }

    // --- 3. EFEITOS PARA SNACKBAR E NAVEGAÇÃO ---
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.paymentSuccess) {
        if (uiState.paymentSuccess) {
            onPaymentSuccess() // Navega para a tela de sucesso
        }
    }

    val webViewRef = remember { mutableStateOf<WebView?>(null) }

    AndroidView(factory = { context ->
        WebView(context).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            webViewClient = WebViewClient()

            addJavascriptInterface(object {
                @JavascriptInterface
                fun onToken(json: String) {
                    val data = JSONObject(json)
                    if (data.optBoolean("ok", false)) {
                        val token = data.optString("token")
                        viewModel.onFrontendTokenGenerated(token)
                    } else {
                        val error = data.optString("error", "Erro ao gerar token.")
                        viewModel.onFrontendTokenError(error)
                    }
                }
            }, "Android")

            loadUrl("file:///android_asset/generateToken.html")
            webViewRef.value = this
        }
    }, modifier = Modifier.size(1.dp))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pagar com Cartão") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddCard) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Novo Cartão")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->

        if (uiState.isPaymentLoading) {
            // Mostra um loading em tela cheia
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Processando pagamento...", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(16.dp))
                    CircularProgressIndicator()
                }
            }
        }
        else {
            // Exibe o conteúdo com base no estado
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
            ) {
                when {
                    uiState.isLoading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    uiState.cards.isEmpty() && uiState.errorMessage == null -> {
                        Text(
                            text = "Nenhum cartão salvo.\nAdicione um novo para continuar.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    else -> {
                        LazyColumn(
                            contentPadding = PaddingValues(vertical = 16.dp)
                        ) {
                            // --- 5. MOSTRA O VALOR ---
                            item {
                                Text(
                                    "Valor da Doação:",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = formattedAmount,
                                    style = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.height(24.dp))
                                Text(
                                    "Selecione um cartão:",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(Modifier.height(8.dp))
                            }

                            // --- 6. CHAMA O PAGAMENTO AO CLICAR ---
                            items(uiState.cards) { card ->
                                CardItem(
                                    card = card,
                                    onClick = {
                                        viewModel.openCvvDialog(card)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (uiState.showCvvDialog && uiState.selectedCard != null) {
        uiState.selectedCard?.let { selected ->
            CvvDialog(
                card = selected,
                onDismiss = { viewModel.closeCvvDialog() },
                onConfirm = { cvv ->
                    viewModel.confirmCvvAndPay(cvv, amountInCents)

                    val jsCommand = "window.generateToken('${selected.paymentMethodId}', '${cvv}')"
                    webViewRef.value?.evaluateJavascript(jsCommand, null)
                }
            )
        }
    }
}

@Composable
fun CvvDialog(
    card: SavedCard,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var cvv by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Código de segurança") },
        text = {
            Column {
                Text("Digite o CVV do cartão terminado em ${card.last4}")
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = cvv,
                    onValueChange = { if (it.length <= 4) cvv = it },
                    placeholder = { Text("CVV") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(cvv) }, enabled = cvv.length >= 3) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}


@Preview(showBackground = true)
@Composable
fun MeusCartoesScreenPreview() {
    MaterialTheme {
        MeusCartoesScreen(
            amountInCents = 5000,     // Passa um valor de exemplo (ex: R$ 50,00)
            onNavigateBack = {},
            onNavigateToAddCard = {},
            onPaymentSuccess = {}     // Adiciona o novo callback
        )
    }
}