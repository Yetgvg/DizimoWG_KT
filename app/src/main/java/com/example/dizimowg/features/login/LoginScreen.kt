package com.example.dizimowg.features.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dizimowg.R

// --- Paleta de Cores (mantida do nosso design) ---
val primaryRed = Color(0xFFb00020)
val whiteText = Color.White
val whiteBackground = Color.White

// A tela continua "burra", recebendo o estado e as funções de fora
@Composable
fun LoginScreen(
    uiState: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onLoginClicked: () -> Unit
) {
    // A tela agora é 100% controlada pelo uiState vindo do ViewModel

    Box(modifier = Modifier.fillMaxSize().background(whiteBackground)) {

        // --- Camada Superior (Logo) ---
        Column(
            modifier = Modifier.fillMaxWidth().height(280.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier.size(160.dp),
                contentScale = ContentScale.Fit
            )
        }

        // --- Camada Inferior (Formulário) ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 220.dp)
                .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
                .background(primaryRed)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Login",
                color = whiteText,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 40.dp)
            )

            val textFieldColors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = whiteText,
                unfocusedBorderColor = whiteText.copy(alpha = 0.7f),
                focusedLabelColor = whiteText,
                unfocusedLabelColor = whiteText.copy(alpha = 0.7f),
                cursorColor = whiteText,
                focusedTextColor = whiteText,
                unfocusedTextColor = whiteText,
                errorBorderColor = MaterialTheme.colorScheme.error,
                errorLabelColor = MaterialTheme.colorScheme.error
            )

            OutlinedTextField(
                value = uiState.email, // Conectado ao ViewModel
                onValueChange = onEmailChange, // Conectado ao ViewModel
                label = { Text("E-mail") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                colors = textFieldColors,
                isError = uiState.errorMessage != null
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.password, // Conectado ao ViewModel
                onValueChange = onPasswordChange, // Conectado ao ViewModel
                label = { Text("Senha") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = if (uiState.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (uiState.isPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility
                    IconButton(onClick = onTogglePasswordVisibility) { // Conectado ao ViewModel
                        Icon(imageVector = image, contentDescription = "Mostrar senha", tint = whiteText)
                    }
                },
                colors = textFieldColors,
                isError = uiState.errorMessage != null
            )

            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage,
                    color = MaterialTheme.colorScheme.onErrorContainer, // Cor de erro mais adequada
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp).align(Alignment.Start)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onLoginClicked, // Conectado ao ViewModel
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !uiState.isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = whiteBackground,
                    contentColor = primaryRed
                )
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = primaryRed)
                } else {
                    Text("Entrar", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

// O Preview agora passa um estado de exemplo para a tela "burra"
@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen(
        uiState = LoginUiState(email = "teste@email.com", errorMessage = "Senha inválida"),
        onEmailChange = {},
        onPasswordChange = {},
        onTogglePasswordVisibility = {},
        onLoginClicked = {}
    )
}