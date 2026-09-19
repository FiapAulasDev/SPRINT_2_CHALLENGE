package com.aguia_branca.app.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.aguia_branca.app.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController, authViewModel: AuthViewModel) {
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var mensagemErro by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val corFundo = Color(0xFFF4F7FA)
    val corPrimaria = Color(0xFF0F3460)

    Column(
        modifier = Modifier.fillMaxSize().background(corFundo).padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Águia Branca", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = corPrimaria)
        Text("Inovação e Estratégia", fontSize = 16.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(48.dp))

        OutlinedTextField(
            value = email, onValueChange = { email = it },
            label = { Text("E-mail corporativo") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = senha, onValueChange = { senha = it },
            label = { Text("Senha") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (mensagemErro.isNotEmpty()) {
            Text(text = mensagemErro, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 16.dp))
        }

        Button(
            onClick = {
                if (email.isNotBlank() && senha.isNotBlank()) {
                    isLoading = true
                    mensagemErro = ""
                    authViewModel.login(email, senha,
                        onSuccess = {
                            isLoading = false
                            navController.navigate("home") { popUpTo(0) }
                        },
                        onError = {
                            isLoading = false
                            mensagemErro = it
                        }
                    )
                } else {
                    mensagemErro = "Preencha todos os campos."
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(corPrimaria),
            enabled = !isLoading
        ) {
            if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            else Text("Entrar", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        TextButton(onClick = { navController.navigate("register") }, modifier = Modifier.padding(top = 16.dp)) {
            Text("Criar nova conta", color = corPrimaria)
        }
    }
}
