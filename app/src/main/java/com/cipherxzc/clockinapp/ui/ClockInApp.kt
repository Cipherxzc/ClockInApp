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
import com.cipherxzc.clockinapp.ui.auth.AuthViewModel
import com.cipherxzc.clockinapp.ui.main.MainNavGraph

val LocalClockInItemDao = compositionLocalOf<ClockInItemDao> { error("No ClockInItemDao provided") }
val LocalClockInRecordDao = compositionLocalOf<ClockInRecordDao> { error("No ClockInRecordDao provided") }

@Composable
fun ClockInApp(
    clockInItemDao: ClockInItemDao,
    clockInRecordDao: ClockInRecordDao
) {
    val navController = rememberNavController()
    val viewModel: AuthViewModel = viewModel()

    val startRoute = if (viewModel.currentUser() != null) "main" else "auth"

    NavHost(navController, startDestination = startRoute) {
        // auth 模块
        composable("auth") {
            AuthNavGraph(
                viewModel=viewModel,
                onLoginSuccess = {
                    navController.navigate("main") {
                        popUpTo("auth") { inclusive = true }
                    }
                }
            )
        }
        // main 模块
        composable("main") {
            CompositionLocalProvider(
                LocalClockInItemDao provides clockInItemDao,
                LocalClockInRecordDao provides clockInRecordDao
            ) {
                val currentUser = viewModel.currentUser()
                MainNavGraph(
                    currentUser = currentUser,
                    onLogout = {
                        viewModel.logout()
                        navController.navigate("auth") {
                            popUpTo("main") { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
