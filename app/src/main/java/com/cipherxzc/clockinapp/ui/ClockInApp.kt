package com.cipherxzc.clockinapp.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cipherxzc.clockinapp.data.database.ClockInItemDao
import com.cipherxzc.clockinapp.data.database.ClockInRecordDao
import com.cipherxzc.clockinapp.ui.auth.AuthNavGraph
import com.cipherxzc.clockinapp.ui.viewmodel.AuthViewModel
import com.cipherxzc.clockinapp.ui.main.ErrorScreen
import com.cipherxzc.clockinapp.ui.main.MainNavGraph
import com.cipherxzc.clockinapp.ui.viewmodel.DatabaseViewModel
import com.google.firebase.auth.FirebaseUser

@Composable
fun ClockInApp() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val databaseViewModel: DatabaseViewModel = viewModel()

    val startRoute = if (authViewModel.currentUser() != null) "main" else "auth"

    NavHost(navController, startDestination = startRoute) {
        // auth 模块
        composable("auth") {
            AuthNavGraph(
                authViewModel=authViewModel,
                onLoginSuccess = {
                    navController.navigate("main") {
                        popUpTo("auth") { inclusive = true }
                    }
                },
                insertDefaultData = { userId ->
                    databaseViewModel.insertDefaultData(userId)
                }
            )
        }
        // main 模块
        composable("main") {
            val currentUser = authViewModel.currentUser()
            if (currentUser == null) {
                ErrorScreen()
            } else{
                databaseViewModel.setCurrentUser(currentUser.uid)
                MainNavGraph(
                    userName = currentUser.displayName ?: "tourist",
                    databaseViewModel = databaseViewModel,
                    onLogout = {
                        authViewModel.logout()
                        databaseViewModel.resetCurrentUser()
                        navController.navigate("auth") {
                            popUpTo("main") { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
