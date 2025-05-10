package com.cipherxzc.clockinapp.ui.main

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseUser

@Composable
fun MainNavGraph(
    currentUser: FirebaseUser?,
    onLogout: () -> Unit
){
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "itemList") {
        composable("itemList") {
            ClockInItemListScreen(
                currentUser = currentUser,
                onItemClicked = { itemId ->
                    navController.navigate("itemDetail/$itemId")
                },
                onLogout = onLogout
            )
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