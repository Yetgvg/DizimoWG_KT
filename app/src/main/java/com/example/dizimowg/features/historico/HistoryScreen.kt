package com.example.dizimowg.features.historico

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dizimowg.features.historico.history.HistoryUiState
import com.example.dizimowg.features.history.Lancamento
import java.text.NumberFormat
import java.util.Locale

// Componente para o ícone
@Composable
fun LancamentoIcon(natureza: String) {
    val icon = when (natureza) {
        "3" -> Icons.Default.CreditCard // Cartão
        "PX" -> Icons.Default.QrCode // Pix
        "Dízimo" -> Icons.Default.VolunteerActivism
        else -> Icons.Default.CardGiftcard // Padrão
    }
    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
}

// Componente para cada item da lista (agora com animação)
@Composable
fun LancamentoItem(item: Lancamento) {
    var isExpanded by remember { mutableStateOf(false) }
    val meses = listOf("Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho", "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro")
    val mesReferencia = item.mes.toIntOrNull()?.let { if (it in 1..12) meses[it - 1] else "N/A" } ?: "N/A"
    val dataLancamento = try { java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).parse(item.data)?.let { java.text.SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")).format(it) } ?: "" } catch (e: Exception) { "" }


    Column(modifier = Modifier.background(Color.White)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer,
                    CircleShape
                ).padding(10.dp)
            ) {
                LancamentoIcon(natureza = item.natureza)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(mesReferencia, fontWeight = FontWeight.Bold)
                Text(dataLancamento, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Text(item.valor.toCurrencyString(), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            if (item.obs.isNotBlank()) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = "Ver detalhes",
                    modifier = Modifier.padding(start = 8.dp),
                    tint = Color.Gray
                )
            }
        }
        // A animação acontece aqui
        AnimatedVisibility(
            visible = isExpanded && item.obs.isNotBlank(),
            enter = expandVertically(animationSpec = tween(durationMillis = 300)),
            exit = shrinkVertically(animationSpec = tween(durationMillis = 300))
        ) {
            Text(
                text = item.obs,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = 72.dp, end = 16.dp, bottom = 16.dp),
                color = Color.Gray
            )
        }
    }
}

// Tela Principal de Histórico
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    uiState: HistoryUiState,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Extrato de Movimentações", style = MaterialTheme.typography.titleMedium)
                        // Exibe o nome e o coddiz, se existirem no state
                        uiState.userName?.let { name ->
                            Text(
                                text = "$name (Cód: ${uiState.userId ?: "N/A"})",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        // Rodapé fixo com o total
        bottomBar = {
            if (!uiState.isLoading && uiState.groupedLancamentos.isNotEmpty()) {
                Surface(shadowElevation = 8.dp) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Total Geral", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        Text(
                            uiState.totalSum.toCurrencyString(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B5E20)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.groupedLancamentos.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Nenhum lançamento encontrado.")
            }
        } else {
            // LazyColumn é o equivalente da SectionList/FlatList
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                uiState.groupedLancamentos.forEach { (year, lancamentos) ->
                    // Cabeçalho da Seção (Ano e Subtotal)
                    stickyHeader {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Ano de ${year}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            val subtotal = lancamentos.sumOf { it.valor }
                            Text(subtotal.toCurrencyString(), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        }
                    }
                    // Itens do Lançamento
                    items(lancamentos) { lancamento ->
                        LancamentoItem(item = lancamento)
                        Divider(color = Color.LightGray.copy(alpha = 0.3f))
                    }
                }
            }
        }
    }
}

// Função auxiliar para formatar moeda
fun Double.toCurrencyString(): String {
    return NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(this)
}