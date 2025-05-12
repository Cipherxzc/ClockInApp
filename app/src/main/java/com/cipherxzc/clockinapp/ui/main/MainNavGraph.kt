package com.cipherxzc.clockinapp.ui.main

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cipherxzc.clockinapp.ui.viewmodel.DatabaseViewModel
import com.cipherxzc.clockinapp.ui.viewmodel.ItemDetailViewModel
import com.cipherxzc.clockinapp.ui.viewmodel.ItemDetailViewModelFactory
import com.cipherxzc.clockinapp.ui.viewmodel.ItemListViewModel
import com.cipherxzc.clockinapp.ui.viewmodel.ItemListViewModelFactory
import com.cipherxzc.clockinapp.ui.viewmodel.SyncViewModel
import com.cipherxzc.clockinapp.ui.viewmodel.SyncViewModelFactory

@Composable
fun MainNavGraph(
    userName: String,
    databaseViewModel: DatabaseViewModel,
    onLogout: () -> Unit
){
    val itemListViewModel: ItemListViewModel = viewModel(
        factory = ItemListViewModelFactory(databaseViewModel)
    )
    val itemDetailViewModel: ItemDetailViewModel = viewModel(
        factory = ItemDetailViewModelFactory(databaseViewModel)
    )
    val syncViewModel: SyncViewModel = viewModel(
        factory = SyncViewModelFactory(LocalContext.current.applicationContext as Application, databaseViewModel)
    )

    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "itemList") {
        composable("itemList") {
            ClockInItemListScreen(
                userName = userName,
                itemListViewModel = itemListViewModel,
                syncViewModel = syncViewModel,
                onItemClicked = { itemId ->
                    navController.navigate("itemDetail/$itemId")
                },
                onLogout = onLogout
            )
        }
        composable(
            "itemDetail/{itemId}",
            arguments = listOf(navArgument("itemId") { type = NavType.StringType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId") ?: return@composable
            ClockInItemDetailScreen(
                itemId = itemId,
                itemDetailViewModel = itemDetailViewModel,
            )
        }
    }
}