package com.example.dizimowg.features.doacao

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale
import androidx.compose.material3.ButtonDefaults
import com.example.dizimowg.features.login.primaryRed


val onPrimaryRed = Color.White

// --- Lógica da Máscara de Moeda (Estilo Nubank) ---
class CurrencyAmountInputVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val symbols = NumberFormat.getCurrencyInstance(Locale("pt", "BR")).currency?.symbol ?: "R$"
        val digits = text.text.filter { it.isDigit() }
        val amount = digits.toLongOrNull() ?: 0L
        val formattedAmount = (amount / 100.0).let {
            NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(it)
        }

        val newText = AnnotatedString(formattedAmount)

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return newText.length
            }
            override fun transformedToOriginal(offset: Int): Int {
                return digits.length
            }
        }

        return TransformedText(newText, offsetMapping)
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoacaoScreen(onNavigateBack: () -> Unit, onNavigateToPix: (Int) -> Unit, onNavigateToCartao: () -> Unit) {
    // O estado agora guarda os centavos como uma string de dígitos
    var amountInCents by remember { mutableStateOf("") }
    val presetAmounts = listOf(1000, 2000, 5000, 10000) // Valores em centavos

    val amountAsCentsInt = amountInCents.toIntOrNull() ?: 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fazer Doação", color = onPrimaryRed) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = onPrimaryRed)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = primaryRed)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Escolha um valor", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(vertical = 16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                presetAmounts.forEach { preset ->
                    OutlinedButton(
                        onClick = { amountInCents = preset.toString() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = primaryRed),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = SolidColor(
                            primaryRed
                        ))
                    ) {
                        Text("R$ ${(preset / 100)}")
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = amountInCents,
                onValueChange = { newValue ->
                    // Permite apenas dígitos e limita o tamanho
                    if (newValue.length <= 9) { // Limite de 999.999,99
                        amountInCents = newValue.filter { it.isDigit() }
                    }
                },
                label = { Text("Outro valor") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                visualTransformation = CurrencyAmountInputVisualTransformation(), // A MÁGICA ACONTECE AQUI
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryRed)
            )
            Spacer(modifier = Modifier.height(40.dp))

            Text("Escolha a forma de pagamento", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))

            Button(
                onClick = { onNavigateToPix(amountAsCentsInt) },
                enabled = amountAsCentsInt > 0,
                modifier = Modifier.fillMaxWidth().height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryRed)
            ) {
                Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Pagar com PIX", fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onNavigateToCartao,
                enabled = amountAsCentsInt > 0,
                modifier = Modifier.fillMaxWidth().height(60.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = primaryRed),
                border = ButtonDefaults.outlinedButtonBorder().copy(brush = SolidColor(primaryRed))
            ) {
                Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Pagar com Cartão", fontSize = 16.sp)
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun DoacaoScreenPreview() {
    MaterialTheme {
        // Passamos um valor em centavos para a função de navegação de exemplo
        DoacaoScreen(onNavigateBack = {}, onNavigateToPix = {}, onNavigateToCartao = {})
    }
}