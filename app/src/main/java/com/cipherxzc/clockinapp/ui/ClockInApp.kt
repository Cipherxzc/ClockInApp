package com.cipherxzc.clockinapp.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cipherxzc.clockinapp.data.ClockInItemDao
import com.cipherxzc.clockinapp.data.ClockInRecordDao
import com.cipherxzc.clockinapp.ui.auth.AuthNavGraph
import com.cipherxzc.clockinapp.ui.viewmodel.AuthViewModel
import com.cipherxzc.clockinapp.ui.main.ErrorScreen
import com.cipherxzc.clockinapp.ui.main.MainNavGraph
import com.cipherxzc.clockinapp.ui.viewmodel.DatabaseViewModel
import com.google.firebase.auth.FirebaseUser

val LocalClockInItemDao = compositionLocalOf<ClockInItemDao> { error("No ClockInItemDao provided") }
val LocalClockInRecordDao = compositionLocalOf<ClockInRecordDao> { error("No ClockInRecordDao provided") }
val LocalCurrentUser = compositionLocalOf<FirebaseUser> { error("No CurrentUser provided") }

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
                insertDefaultData = databaseViewModel::insertDefaultData
            )
        }
        // main 模块
        composable("main") {
            val currentUser = authViewModel.currentUser()
            if (currentUser == null) {
                ErrorScreen()
            } else{
                CompositionLocalProvider(
                    LocalClockInItemDao provides databaseViewModel.clockInItemDao,
                    LocalClockInRecordDao provides databaseViewModel.clockInRecordDao,
                    LocalCurrentUser provides currentUser
                ) {
                    MainNavGraph(
                        onLogout = {
                            authViewModel.logout()
                            navController.navigate("auth") {
                                popUpTo("main") { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
    }
}
