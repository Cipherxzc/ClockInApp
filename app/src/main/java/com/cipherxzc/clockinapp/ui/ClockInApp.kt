package com.cipherxzc.clockinapp.ui

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
import com.cipherxzc.clockinapp.ui.main.ClockInItemDetailScreen
import com.cipherxzc.clockinapp.ui.main.ClockInItemListScreen
import com.cipherxzc.clockinapp.ui.main.MainNavGraph

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
        MainNavGraph(navController)
    }
}
