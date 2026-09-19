package com.aguia_branca.app.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.aguia_branca.app.data.remote.dto.Role
import com.aguia_branca.app.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(navController: NavController, authViewModel: AuthViewModel) {
    var nome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var confirmarSenha by remember { mutableStateOf("") }
    var role by remember { mutableStateOf<Role?>(null) }
    var mensagem by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val corFundo = Color(0xFFF4F7FA)
    val corPrimaria = Color(0xFF0F3460)

    Column(
        modifier = Modifier.fillMaxSize().background(corFundo).padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Criar conta", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = corPrimaria)
        Text("Escolha o perfil do novo usuário", fontSize = 14.sp, color = Color.Gray)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(nome, { nome = it }, label = { Text("Nome completo") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(email, { email = it }, label = { Text("E-mail corporativo") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(senha, { senha = it }, label = { Text("Senha (mínimo 6 caracteres)") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(confirmarSenha, { confirmarSenha = it }, label = { Text("Confirmar senha") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(12.dp))
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Perfil", fontWeight = FontWeight.SemiBold, color = corPrimaria)
            RoleOption("Operador", Role.OPERADOR, role) { role = it }
            RoleOption("Líder", Role.LIDER, role) { role = it }
            RoleOption("Gestor", Role.GESTOR, role) { role = it }
        }
        Spacer(Modifier.height(8.dp))
        if (mensagem.isNotEmpty()) {
            Text(mensagem, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 12.dp))
        }
        Button(
            onClick = {
                val erro = when {
                    nome.isBlank() || email.isBlank() || senha.isBlank() || confirmarSenha.isBlank() -> "Preencha todos os campos."
                    !email.contains("@") -> "Informe um e-mail válido."
                    senha.length < 6 -> "A senha deve ter no mínimo 6 caracteres."
                    senha != confirmarSenha -> "As senhas não conferem."
                    role == null -> "Selecione o perfil do usuário."
                    else -> ""
                }
                mensagem = erro
                if (erro.isEmpty()) {
                    isLoading = true
                    authViewModel.register(nome, email, senha, role!!,
                        onSuccess = {
                            isLoading = false
                            navController.navigate("login") { popUpTo("register") { inclusive = true } }
                        },
                        onError = { isLoading = false; mensagem = it }
                    )
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(corPrimaria),
            enabled = !isLoading
        ) {
            if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.height(24.dp))
            else Text("Criar conta", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        TextButton(onClick = { navController.popBackStack() }, modifier = Modifier.padding(top = 8.dp)) {
            Text("Voltar para o login", color = corPrimaria, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun RoleOption(label: String, value: Role, selected: Role?, onSelected: (Role) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected == value, onClick = { onSelected(value) })
        Text(label, modifier = Modifier.padding(start = 4.dp))
    }
}
