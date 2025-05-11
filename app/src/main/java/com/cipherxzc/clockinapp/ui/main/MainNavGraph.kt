package com.cipherxzc.clockinapp.ui.main

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cipherxzc.clockinapp.ui.viewmodel.DatabaseViewModel
import com.cipherxzc.clockinapp.ui.viewmodel.ItemListViewModel
import com.cipherxzc.clockinapp.ui.viewmodel.ItemListViewModelFactory

@Composable
fun MainNavGraph(
    databaseViewModel: DatabaseViewModel,
    onLogout: () -> Unit
){
    val navController = rememberNavController()

    val itemListViewModel: ItemListViewModel = viewModel(
        factory = ItemListViewModelFactory(databaseViewModel)
    )

    NavHost(navController = navController, startDestination = "itemList") {
        composable("itemList") {
            ClockInItemListScreen(
                user = databaseViewModel.getCurrentUser(),
                itemListViewModel = itemListViewModel,
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
            val itemId = backStackEntry.arguments?.getString("itemId") ?: return@composable
            ClockInItemDetailScreen(itemId)
        }
    }
}