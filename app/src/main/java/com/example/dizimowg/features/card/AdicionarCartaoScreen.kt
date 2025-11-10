package com.example.dizimowg.features.card

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdicionarCartaoScreen(
    onNavigateBack: () -> Unit,
    viewModel: AdicionarCartaoViewModel = viewModel() // Use a ViewModel SIMPLES
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val webView = remember { mutableStateOf<WebView?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    // --- HTML V2 (CORRIGIDO PARA GERAR UM TOKEN 'LIMPO') ---
    val htmlContent = """
    <!DOCTYPE html>
    <html lang="pt-BR">
    <head>
      <meta name="viewport" content="width=device-width, initial-scale=1.0">
      <style>
        :root {
          --primary-red: #E53935;
          --light-gray: #BDBDBD;
          --dark-text: #333;
        }
    
        body {
          font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
          margin: 0;
          padding: 16px;
          background-color: #f7f8fc;
        }
    
        .field-container {
          margin-bottom: 16px;
        }
    
        label {
          display: block;
          margin-bottom: 6px;
          font-weight: 500;
          font-size: 14px;
          color: var(--dark-text);
        }
    
        input, select {
          width: 100%;
          padding: 14px;
          font-size: 16px;
          color: var(--dark-text);
          background-color: #fff;
          border: 1px solid var(--light-gray);
          border-radius: 8px;
          box-sizing: border-box;
          transition: border-color 0.2s ease-in-out;
        }
    
        input:focus, select:focus {
          border-color: var(--primary-red);
          outline: none;
        }
    
        button {
          width: 100%;
          background-color: var(--primary-red);
          color: #fff;
          padding: 16px;
          border: none;
          border-radius: 8px;
          font-size: 16px;
          font-weight: bold;
          cursor: pointer;
          transition: background-color 0.2s ease-in-out;
        }
    
        button:hover {
          background-color: #c62828;
        }
      </style>
    
      <script src="https://sdk.mercadopago.com/js/v2"></script>
    
      <script>
        const mp = new MercadoPago('APP_USR-21e84916-3118-4cc9-bf93-c4b20fea905f', { locale: 'pt-BR' });
    
        window.onload = function() {
          const cardForm = mp.cardForm({
            amount: "1.00",
            autoMount: true,
            form: {
              id: "form-checkout",
              cardholderName: {
                id: "form-checkout__cardholderName",
                placeholder: "Nome no cartão",
              },
              cardNumber: {
                id: "form-checkout__cardNumber",
                placeholder: "Número do cartão",
              },
              expirationDate: {
                id: "form-checkout__expirationDate",
                placeholder: "MM/AA",
              },
              securityCode: {
                id: "form-checkout__securityCode",
                placeholder: "CVC",
              },
              identificationType: {
                id: "form-checkout__identificationType",
              },
              identificationNumber: {
                id: "form-checkout__identificationNumber",
                placeholder: "Documento",
              },
              issuer: { id: "form-checkout__issuer" },
              installments: { id: "form-checkout__installments" },
            },
            callbacks: {
              onFormMounted: error => {
                if (error) return KotlinApp.onErrorReceived("Erro ao montar formulário: " + JSON.stringify(error));
              },
              onSubmit: event => {
                event.preventDefault();
                const {
                  token,
                } = cardForm.getCardFormData();
    
                  if (!token) {
                    return KotlinApp.onErrorReceived("Erro: token não gerado.");
                  }

                KotlinApp.onTokenReceived(JSON.stringify({ token }));
              },
              onError: error => {
                KotlinApp.onErrorReceived("Erro: " + JSON.stringify(error));
              }
            },
          });
        }
      </script>
    </head>
    
    <body>
      <form id="form-checkout">
        <div class="field-container">
          <label for="form-checkout__cardholderName">Nome no Cartão</label>
          <input type="text" id="form-checkout__cardholderName" />
        </div>
    
        <div class="field-container">
          <label for="form-checkout__cardNumber">Número do Cartão</label>
          <input type="text" id="form-checkout__cardNumber" />
        </div>
    
        <div class="field-container">
          <label for="form-checkout__expirationDate">Validade (MM/AA)</label>
          <input type="text" id="form-checkout__expirationDate" />
        </div>
    
        <div class="field-container">
          <label for="form-checkout__securityCode">CVC</label>
          <input type="text" id="form-checkout__securityCode" />
        </div>
    
        <div class="field-container">
          <label for="form-checkout__identificationType">Tipo de Documento</label>
          <select id="form-checkout__identificationType"></select>
        </div>
    
        <div class="field-container">
          <label for="form-checkout__identificationNumber">Número do Documento</label>
          <input type="text" id="form-checkout__identificationNumber" />
        </div>
        
        <div class="hidden-field" style="display:none">
          <select id="form-checkout__issuer"></select>
          <select id="form-checkout__installments"></select>
        </div>
        
      </form>
    </body>
    </html>
    """.trimIndent()

    class JavaScriptBridge(val viewModel: AdicionarCartaoViewModel) {
        @JavascriptInterface
        fun onTokenReceived(jsonData: String) {
            println("Token recebido do WebView: $jsonData")
            viewModel.sendTokenToBackend(jsonData)
        }
        @JavascriptInterface
        fun onErrorReceived(message: String) {
            println("Erro recebido do WebView: $message")
            viewModel.onError(message)
        }
    }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            snackbarHostState.showSnackbar(message = "Cartão salvo com sucesso!", duration = SnackbarDuration.Short)
            onNavigateBack()
        }
    }
    LaunchedEffect(uiState.error) {
        uiState.error?.let { message ->
            snackbarHostState.showSnackbar(message = message, duration = SnackbarDuration.Long)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Adicionar Cartão") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            Button(
                onClick = { webView.value?.evaluateJavascript("document.getElementById('form-checkout').requestSubmit()", null) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Salvar Cartão")
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { innerPadding ->

        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    webViewClient = WebViewClient()
                    @SuppressLint("SetJavaScriptEnabled")
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    addJavascriptInterface(JavaScriptBridge(viewModel), "KotlinApp")
                    WebView.setWebContentsDebuggingEnabled(true)

                    loadDataWithBaseURL(
                        "https://mercadopago.com",
                        htmlContent,
                        "text/html",
                        "UTF-8",
                        null
                    )

                    webView.value = this
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        )
    }
}