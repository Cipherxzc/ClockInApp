package com.cipherxzc.clockinapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
    var items by remember { mutableStateOf(listOf<ClockInItem>()) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch(Dispatchers.IO) {
            items = clockInItemDao.getAllItems()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Clock-In App") }
            )
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
        },
        content = { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                ClockInItemList(
                    items = items,
                ) { item ->
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
            }
        }
    )
}

@Composable
fun ClockInItemList(
    items: List<ClockInItem>,
    onClockIn: (ClockInItem) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        LazyColumn {
            items(items) { item ->
                Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(item.name)
                        IconButton(onClick = { onClockIn(item) }) {
                            Icon(imageVector = Icons.Filled.Add, contentDescription = "Clock In")
                        }
                    }
                }
            }
        }
    }
}
