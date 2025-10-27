package com.example.dizimowg.features.pix

import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale

// A função agora só precisa do uiState e da função de checar o status
@Composable
fun PixScreen(
    uiState: PixUiState,
    amountToPay: Double,
    onCreatePix: () -> Unit,
    onCheckStatus: (String) -> Unit,
    onPaymentSuccess: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    LaunchedEffect(key1 = Unit) {
        if (uiState is PixUiState.Idle) {
            onCreatePix()
        }
    }
    // -------------------------

    // O "when" agora trata o Idle e o Loading da mesma forma: mostrando um spinner
    when (uiState) {
        is PixUiState.Idle, is PixUiState.Loading -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Gerando PIX para ${amountToPay.toCurrencyString()}...")
            }
        }

        is PixUiState.PixReady -> {
            // Inicia a verificação do status assim que o PIX for exibido
            LaunchedEffect(key1 = uiState.data.paymentId) {
                onCheckStatus(uiState.data.paymentId)
            }

            PendingPaymentContent(
                qrCodeBase64 = uiState.data.qrCodeBase64,
                copiaECola = uiState.data.copiaECola,
                onCopy = {
                    clipboardManager.setText(AnnotatedString(uiState.data.copiaECola))
                    Toast.makeText(context, "Código PIX copiado!", Toast.LENGTH_SHORT).show()
                }
            )
        }

        is PixUiState.Approved -> {
            ApprovedPaymentContent(onFinish = onPaymentSuccess)
        }

        is PixUiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Ocorreu um erro: ${uiState.message}",
                    color = Color.Red,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun PendingPaymentContent(
    qrCodeBase64: String,
    copiaECola: String,
    onCopy: () -> Unit
) {
    val imageBitmap = remember(qrCodeBase64) {
        try {
            val decodedBytes = Base64.decode(qrCodeBase64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size).asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Pague com PIX para concluir", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        if (imageBitmap != null) {
            Image(bitmap = imageBitmap, contentDescription = "QR Code PIX", modifier = Modifier.size(250.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "Escaneie o QR Code ou...", fontSize = 16.sp)
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onCopy) {
            Text("Copiar código PIX")
        }
    }
}

@Composable
fun ApprovedPaymentContent(onFinish: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Pagamento Aprovado!", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF009688))
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Sua doação foi confirmada com sucesso.", fontSize = 18.sp, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = onFinish) {
            Text("Voltar para o Início")
        }
    }
}

fun Double.toCurrencyString(): String {
    return NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(this)
}