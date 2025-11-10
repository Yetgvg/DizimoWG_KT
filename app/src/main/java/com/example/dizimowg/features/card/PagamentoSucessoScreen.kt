package com.example.dizimowg.features.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dizimowg.features.login.primaryRed // (Sua cor)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PagamentoSucessoScreen(
    onNavigateHome: () -> Unit // Callback para voltar ao início
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pagamento Concluído") },
                // Opcional: Remova o botão de voltar para forçar o fluxo
                // navigationIcon = {
                //     IconButton(onClick = onNavigateHome) {
                //         Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                //     }
                // }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = "Sucesso",
                modifier = Modifier.size(120.dp),
                tint = Color(0xFF00C853) // Um verde sucesso
            )
            Spacer(Modifier.height(24.dp))

            Text(
                "Pagamento Aprovado!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))

            Text(
                "Sua doação foi registrada com sucesso. Obrigado!",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(40.dp))

            Button(
                onClick = onNavigateHome,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryRed)
            ) {
                Text("Voltar ao Início", fontSize = 16.sp)
            }
        }
    }
}