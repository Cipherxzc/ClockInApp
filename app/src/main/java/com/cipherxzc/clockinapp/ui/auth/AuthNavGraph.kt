package com.cipherxzc.clockinapp.ui.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.cipherxzc.clockinapp.ui.viewmodel.AuthViewModel

@Composable
fun AuthNavGraph(
    authViewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    insertDefaultData: (String) -> Unit
) {
    val navController = rememberNavController()

    NavHost(navController, startDestination = "login") {
        composable("login") {
            val state by authViewModel.loginState.collectAsState()
            LoginScreen(
                onLogin = authViewModel::login,
                onNavigateToRegister = { navController.navigate("register") },
                authState = state,
                onSuccess = onLoginSuccess
            )
        }
        composable("register") {
            val state by authViewModel.registerState.collectAsState()
            RegisterScreen(
                onRegister = authViewModel::register,
                onNavigateToLogin = { navController.popBackStack() },
                authState = state,
                onSuccess = {
                    val userId = authViewModel.currentUser()?.uid
                    if (userId == null) {
                        // Handle error: userId is null
                        return@RegisterScreen
                    }
                    insertDefaultData(userId)
                    onLoginSuccess()
                }
            )
        }
    }
}