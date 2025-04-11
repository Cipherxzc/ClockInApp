package com.cipherxzc.clockinapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cipherxzc.clockinapp.data.ClockInItem
import com.cipherxzc.clockinapp.data.ClockInItemDao
import com.cipherxzc.clockinapp.data.ClockInRecord
import com.cipherxzc.clockinapp.data.ClockInRecordDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyApp(
    clockInItemDao: ClockInItemDao,
    clockInRecordDao: ClockInRecordDao
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "itemList") {
        composable("itemList") {
            ClockInItemListScreen(
                clockInItemDao = clockInItemDao,
                clockInRecordDao = clockInRecordDao,
                onItemClicked = { itemId ->
                    navController.navigate("itemDetail/$itemId")
                }
            )
        }
        composable(
            "itemDetail/{itemId}",
            arguments = listOf(navArgument("itemId") { type = NavType.IntType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getInt("itemId") ?: return@composable
            ClockInItemDetailScreen(
                itemId = itemId,
                clockInItemDao = clockInItemDao,
                clockInRecordDao = clockInRecordDao
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClockInItemListScreen(
    clockInItemDao: ClockInItemDao,
    clockInRecordDao: ClockInRecordDao,
    onItemClicked: (Int) -> Unit
) {
    var items by remember { mutableStateOf(listOf<ClockInItem>()) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch(Dispatchers.IO) {
            items = clockInItemDao.getAllItems()
        }
    }

    val onClockIn: (ClockInItem) -> Unit = { item ->
        coroutineScope.launch(Dispatchers.IO) {
            clockInItemDao.incrementClockInCount(item.id)
            val record = ClockInRecord(
                itemId = item.id,
                timestamp = System.currentTimeMillis()
            )
            clockInRecordDao.insert(record)
            val updatedItem = clockInItemDao.getAllItems().first { it.id == item.id }
            withContext(Dispatchers.Main) {
                items = items.map {
                    if (it.id == item.id) updatedItem else it
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Clock-In App") })
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    coroutineScope.launch(Dispatchers.IO) {
                        val newItem = ClockInItem(name = "New Item", description = "Description here")
                        clockInItemDao.insert(newItem)
                        val newItems = clockInItemDao.getAllItems()
                        withContext(Dispatchers.Main) {
                            items = newItems
                        }
                    }
                }
            ) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "Add New Item")
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                items(items) { item ->
                    Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(item.name)
                            IconButton(onClick = { onItemClicked(item.id) }) {
                                Icon(imageVector = Icons.AutoMirrored.Filled.List, contentDescription = "View Details")
                            }
                            IconButton(onClick = { onClockIn(item) }) {
                                Icon(imageVector = Icons.Filled.Done, contentDescription = "Clock In")
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClockInItemDetailScreen(
    itemId: Int,
    clockInItemDao: ClockInItemDao,
    clockInRecordDao: ClockInRecordDao
) {
    var item by remember { mutableStateOf<ClockInItem?>(null) }
    var records by remember { mutableStateOf<List<ClockInRecord>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(itemId) {
        coroutineScope.launch(Dispatchers.IO) {
            item = clockInItemDao.getAllItems().firstOrNull { it.id == itemId }
            records = clockInRecordDao.getAllRecordsForItem(itemId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Item Details") })
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            if (item != null) {
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    Text("Name: ${item?.name}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Description: ${item?.description ?: "No description"}")
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Clock-In Records:")
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn {
                        items(records) { record ->
                            Text("Clock-In at: ${record.timestamp}")
                        }
                    }
                }
            } else {
                Text("Loading...")
            }
        }
    }
}
