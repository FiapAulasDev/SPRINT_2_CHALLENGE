package com.aguia_branca.app.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.aguia_branca.app.viewmodel.AuthViewModel
import com.aguia_branca.app.viewmodel.GestorViewModel
import com.aguia_branca.app.viewmodel.IdeiaViewModel
import com.aguia_branca.app.viewmodel.LiderViewModel
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

val CorFundo = Color(0xFFF4F7FA)
val CorPrimaria = Color(0xFF0F3460)
val CorPendente = Color(0xFFF57F17)
val CorAprovada = Color(0xFF2E7D32)
val CorRejeitada = Color(0xFFD32F2F)

fun formatarMoeda(valor: BigDecimal?): String {
    val format = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    return format.format(valor ?: BigDecimal.ZERO)
}

fun formatarPercentual(valor: Double?): String {
    return "${String.format(Locale("pt", "BR"), "%.1f", valor ?: 0.0)}%"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, authViewModel: AuthViewModel) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val ideiaViewModel: IdeiaViewModel = viewModel()
    val gestorViewModel: GestorViewModel = viewModel()
    val liderViewModel: LiderViewModel = viewModel()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val onShowMessage: (String) -> Unit = { scope.launch { snackbarHostState.showSnackbar(it) } }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Águia Branca", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        if (currentUser?.role == "OPERADOR") {
                            Text("Meus Pontos: ${currentUser?.pontos ?: 0}", fontSize = 12.sp, color = Color.White.copy(0.8f))
                        } else {
                            Text(currentUser?.role ?: "", fontSize = 12.sp, color = Color.White.copy(0.8f))
                        }
                    }
                },
                actions = {
                    IconButton(onClick = {
                        authViewModel.logout()
                        navController.navigate("login") { popUpTo(0) }
                    }) { Icon(Icons.AutoMirrored.Filled.ExitToApp, "Sair", tint = Color.White) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CorPrimaria)
            )
        },
        containerColor = CorFundo
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
            when (currentUser?.role) {
                "OPERADOR" -> OperadorHomeContent(authViewModel, ideiaViewModel, liderViewModel, onShowMessage)
                "GESTOR" -> GestorHomeContent(gestorViewModel, liderViewModel, onShowMessage)
                "LIDER" -> LiderHomeContent(liderViewModel, onShowMessage)
                null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = CorPrimaria) }
                else -> Text("Perfil não reconhecido.", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
fun MetricCard(label: String, value: String, color: Color, modifier: Modifier) {
    Card(modifier.padding(4.dp), colors = CardDefaults.cardColors(containerColor = color)) {
        Column(Modifier.padding(12.dp)) {
            Text(label, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}
