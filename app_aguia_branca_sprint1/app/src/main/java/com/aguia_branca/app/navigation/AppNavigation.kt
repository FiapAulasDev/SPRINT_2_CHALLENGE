package com.aguia_branca.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aguia_branca.app.data.remote.SessionEvents
import com.aguia_branca.app.screens.auth.LoginScreen
import com.aguia_branca.app.screens.auth.RegisterScreen
import com.aguia_branca.app.screens.home.HomeScreen
import com.aguia_branca.app.viewmodel.AuthViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val currentUser by authViewModel.currentUser.collectAsState()

    // Lógica de Redirecionamento Automático
    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            navController.navigate("home") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    // Token expirado/rejeitado (401) em qualquer chamada força logout e volta ao login
    LaunchedEffect(Unit) {
        SessionEvents.sessionExpired.collect {
            authViewModel.logout()
            navController.navigate("login") { popUpTo(0) }
        }
    }

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(navController, authViewModel)
        }
        composable("register") {
            RegisterScreen(navController)
        }
        composable("home") {
            HomeScreen(navController, authViewModel)
        }
    }
}
