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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.cipherxzc.clockinapp.data.ClockInRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClockInItemListScreen(
    onItemClicked: (Int) -> Unit
) {
    val clockInItemDao = LocalClockInItemDao.current
    val clockInRecordDao = LocalClockInRecordDao.current

    var items by remember { mutableStateOf(listOf<ClockInItem>()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var newItemName by remember { mutableStateOf("") }
    var newItemDescription by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    // 初始加载数据
    LaunchedEffect(Unit) {
        coroutineScope.launch(Dispatchers.IO) {
            items = clockInItemDao.getAllItems()
        }
    }

    // 打卡操作
    val onClockIn: (ClockInItem) -> Unit = { item ->
        coroutineScope.launch(Dispatchers.IO) {
            clockInItemDao.incrementClockInCount(item.id)
            val record = ClockInRecord(
                itemId = item.id,
                timestamp = LocalDateTime.now()
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

    // 整体页面结构：Scaffold 包含 TopAppBar 和 FloatingActionButton
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Clock-In App") })
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // 点击添加按钮后显示输入对话框
                    showAddDialog = true
                }
            ) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "Add New Item")
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                items(items) { item ->
                    Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(item.name)
                            IconButton(onClick = { onItemClicked(item.id) }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.List,
                                    contentDescription = "View Details"
                                )
                            }
                            IconButton(onClick = { onClockIn(item) }) {
                                Icon(
                                    imageVector = Icons.Filled.Done,
                                    contentDescription = "Clock In"
                                )
                            }
                        }
                    }
                }
            }

            // 当 showAddDialog 为 true 时显示 AlertDialog 对话框
            if (showAddDialog) {
                AlertDialog(
                    onDismissRequest = { showAddDialog = false },
                    title = { Text("Add New Clock-In Item") },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = newItemName,
                                onValueChange = { newItemName = it },
                                label = { Text("Name") },
                                placeholder = { Text("Enter item name") }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = newItemDescription,
                                onValueChange = { newItemDescription = it },
                                label = { Text("Description (Optional)") },
                                placeholder = { Text("Enter description") }
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                // 当用户输入的 name 非空时，进行添加操作
                                if (newItemName.isNotBlank()) {
                                    coroutineScope.launch(Dispatchers.IO) {
                                        val newItem = ClockInItem(
                                            name = newItemName,
                                            description = newItemDescription
                                        )
                                        clockInItemDao.insert(newItem)
                                        val newItems = clockInItemDao.getAllItems()
                                        withContext(Dispatchers.Main) {
                                            items = newItems
                                            // 清空输入，并关闭对话框
                                            newItemName = ""
                                            newItemDescription = ""
                                            showAddDialog = false
                                        }
                                    }
                                } else {
                                    // 可选：可以给用户提示名称为必填项
                                }
                            }
                        ) {
                            Text("Add")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                // 取消操作，清空输入并关闭对话框
                                newItemName = ""
                                newItemDescription = ""
                                showAddDialog = false
                            }
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}
