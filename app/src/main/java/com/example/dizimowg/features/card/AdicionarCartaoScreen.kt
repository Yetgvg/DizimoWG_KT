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
import androidx.compose.runtime.getValue
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
    viewModel: AdicionarCartaoViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    lateinit var webViewInstance: WebView

    // --- HTML COM A ORDEM CORRETA DOS SCRIPTS ---
    val htmlContent = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
                body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; margin: 0; padding: 16px; background-color: #f5f5f5; }
                div { margin-bottom: 12px; }
                input, select {
                    width: 100%;
                    padding: 14px;
                    border: 1px solid #ccc;
                    border-radius: 4px;
                    box-sizing: border-box;
                    font-size: 16px;
                }
                label { display: block; margin-bottom: 4px; font-weight: 500; color: #333; }
            </style>
            
            <script>
                function initMercadoPago() {
                    try {
                        const mp = new MercadoPago('APP_USR-8b97608b-5799-4f72-a0d9-22cf24caa933', {
                            locale: 'pt-BR'
                        });

                        (async function getIdentificationTypes() {
                            try {
                                const identificationTypes = await mp.getIdentificationTypes();
                                const elem = document.getElementById('form-checkout__identificationType');
                                identificationTypes.forEach(option => {
                                    const opt = document.createElement('option');
                                    opt.value = option.id;
                                    opt.textContent = option.name;
                                    elem.appendChild(opt);
                                });
                            } catch (e) {
                                console.error('Erro ao buscar tipos de doc:', e);
                            }
                        })();

                        window.submitForm = async () => {
                            try {
                                const cardForm = document.getElementById('form-checkout');
                                const token = await mp.createCardToken(cardForm); 
                                KotlinApp.onTokenReceived(token.id);
                            } catch (e) {
                                console.error('Erro ao criar token:', e.message || JSON.stringify(e));
                                const errorMsg = (Array.isArray(e) && e.length > 0) ? e[0].message : 'Erro ao criar token';
                                KotlinApp.onErrorReceived(errorMsg);
                            }
                        };
                        
                        console.log("Formulário simples (sem iframe) montado com sucesso!");

                    } catch (e) {
                        console.error('Erro fatal na inicialização:', e.message || JSON.stringify(e));
                        KotlinApp.onErrorReceived(e.message || 'Erro fatal no MP');
                    }
                }
            </script>
            
            <script src="https://sdk.mercadopago.com/js/v2" onload="initMercadoPago()"></script>
        </head>
        <body style="margin: 0; padding: 16px;">
            
            <form id="form-checkout">
                <div>
                    <label for="form-checkout__cardNumber">Número do Cartão</label>
                    <input id="form-checkout__cardNumber" type="text" />
                </div>
                <div>
                    <label for="form-checkout__cardExpirationMonth">Mês (MM)</label>
                    <input id="form-checkout__cardExpirationMonth" type="text" />
                </div>
                <div>
                    <label for="form-checkout__cardExpirationYear">Ano (AAAA)</label>
                    <input id="form-checkout__cardExpirationYear" type="text" />
                </div>
                <div>
                    <label for="form-checkout__cardholderName">Nome no Cartão</label>
                    <input id="form-checkout__cardholderName" type="text" />
                </div>
                <div>
                    <label for="form-checkout__cardholderEmail">E-mail</label>
                    <input id="form-checkout__cardholderEmail" type="email" />
                </div>
                <div>
                    <label for="form-checkout__securityCode">CVC</label>
                    <input id="form-checkout__securityCode" type="text" />
                </div>
                <div>
                    <label for="form-checkout__identificationType">Tipo de Documento</label>
                    <select id="form-checkout__identificationType"></select>
                </div>
                <div>
                    <label for="form-checkout__identificationNumber">Número do Documento</label>
                    <input id="form-checkout__identificationNumber" type="text" />
                </div>
            </form>

        </body>
        </html>
    """.trimIndent()

    /**
     * A "Ponte" de comunicação.
     */
    class JavaScriptBridge(val viewModel: AdicionarCartaoViewModel) {
        @JavascriptInterface
        fun onTokenReceived(token: String) {
            println("Token recebido do WebView: $token")
            viewModel.sendTokenToBackend(token)
        }
        @JavascriptInterface
        fun onErrorReceived(message: String) {
            println("Erro recebido do WebView: $message")
            // TODO: Atualizar o uiState com a mensagem de erro
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
        floatingActionButton = {
            Button(
                onClick = {
                    webViewInstance.evaluateJavascript("window.submitForm()", null)
                },
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

        if (uiState.saveSuccess) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Cartão salvo com sucesso!")
            }
        } else {
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

                        webViewInstance = this
                    }
                },
                modifier = Modifier.fillMaxSize().padding(innerPadding)
            )
        }
    }
}