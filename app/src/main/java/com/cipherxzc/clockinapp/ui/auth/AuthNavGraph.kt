package com.cipherxzc.clockinapp.ui.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.runtime.getValue
import androidx.navigation.compose.rememberNavController

@Composable
fun AuthNavGraph(
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit
) {
    val navController = rememberNavController()

    NavHost(navController, startDestination = "login") {
        composable("login") {
            val state by viewModel.loginState.collectAsState()
            LoginScreen(
                onLogin = viewModel::login,
                onNavigateToRegister = { navController.navigate("register") },
                authState = state,
                onSuccess = onLoginSuccess
            )
        }
        composable("register") {
            val state by viewModel.registerState.collectAsState()
            RegisterScreen(
                onRegister = viewModel::register,
                onNavigateToLogin = { navController.popBackStack() },
                authState = state,
                onSuccess = onLoginSuccess
            )
        }
    }
}