package com.example.dizimowg.features.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dizimowg.R

// --- Componentes Auxiliares ---

@Composable
fun UserAvatar(name: String, modifier: Modifier = Modifier) {
    val initials = name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("").toUpperCase()
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Text(text = initials, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeHeader(userName: String, onLogoutClick: () -> Unit, onHistoryClick: () -> Unit) {
    var menuExpanded by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Column {
                Text("Olá, ${userName.split(" ")[0]}!", style = MaterialTheme.typography.titleMedium)
                Text("Seja bem-vindo(a)", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        },
        navigationIcon = {
            UserAvatar(name = userName, modifier = Modifier.padding(start = 16.dp))
        },
        actions = {
            IconButton(onClick = { menuExpanded = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Menu")
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                DropdownMenuItem(text = { Text("Histórico") }, onClick = onHistoryClick)
                DropdownMenuItem(text = { Text("Perfil") }, onClick = { /* TODO: Navegar para Perfil */ menuExpanded = false })
                DropdownMenuItem(text = { Text("Sair") }, onClick = onLogoutClick)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
    )
}

// --- Tela Principal ---

@Composable
fun HomeScreen(userName: String, onNavigateToDoacao: () -> Unit, onLogout: () -> Unit, onNavigateToHistory: () -> Unit) {

    Scaffold(
        topBar = {
            HomeHeader(userName = userName, onLogoutClick = onLogout, onHistoryClick = onNavigateToHistory)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        )
        {
            Text("Área de Contribuição", style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
            Text(
                "Sua generosidade transforma vidas.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Image(
                painter = painterResource(id = R.drawable.logo), // Usando a mesma logo por enquanto
                contentDescription = "Ilustração",
                modifier = Modifier.height(150.dp).padding(vertical = 24.dp)
            )

            // Botão Primário
            Button(
                onClick = onNavigateToDoacao,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Fazer Doação", fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botão Secundário
            OutlinedButton(
                onClick = { /* TODO: Ação do Dízimo */ },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Entregar Dízimo", fontSize = 16.sp)
            }
        }
    }
}

// --- Preview ---

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    MaterialTheme {
        HomeScreen(userName = "Felipe G.", onNavigateToDoacao = {}, onLogout = {}, onNavigateToHistory = {})
    }
}