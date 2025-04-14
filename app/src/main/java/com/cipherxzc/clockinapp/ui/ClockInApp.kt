package com.cipherxzc.clockinapp.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cipherxzc.clockinapp.data.ClockInItemDao
import com.cipherxzc.clockinapp.data.ClockInRecordDao

val LocalClockInItemDao = compositionLocalOf<ClockInItemDao> { error("No ClockInItemDao provided") }
val LocalClockInRecordDao = compositionLocalOf<ClockInRecordDao> { error("No ClockInRecordDao provided") }

@Composable
fun ClockInApp(
    clockInItemDao: ClockInItemDao,
    clockInRecordDao: ClockInRecordDao
) {
    val navController = rememberNavController()

    CompositionLocalProvider(
        LocalClockInItemDao provides clockInItemDao,
        LocalClockInRecordDao provides clockInRecordDao
    ) {
        NavHost(navController = navController, startDestination = "itemList") {
            composable("itemList") {
                ClockInItemListScreen{ itemId ->
                    navController.navigate("itemDetail/$itemId")
                }
            }
            composable(
                "itemDetail/{itemId}",
                arguments = listOf(navArgument("itemId") { type = NavType.IntType })
            ) { backStackEntry ->
                val itemId = backStackEntry.arguments?.getInt("itemId") ?: return@composable
                ClockInItemDetailScreen(itemId)
            }
        }
    }
}
