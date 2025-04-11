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
import androidx.compose.runtime.MutableState
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
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime

data class ClockInStatus(
    val clockedInItems: MutableState<List<ClockInItem>> = mutableStateOf(emptyList()),
    val unClockedInItems: MutableState<List<ClockInItem>> = mutableStateOf(emptyList())
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClockInItemListScreen(
    onItemClicked: (Int) -> Unit
) {
    val itemsState = remember { ClockInStatus() }
    val showDialogState = remember { mutableStateOf(false) }

    // 整体页面结构：Scaffold 包含 TopAppBar 和 FloatingActionButton
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Clock-In App") })
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // 点击添加按钮后显示输入对话框
                    showDialogState.value = true
                }
            ) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "Add New Item")
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            ClockInItemList(
                itemsState = itemsState,
                onItemClicked = onItemClicked
            )

            AddItemDialog(
                itemsState = itemsState,
                showDialogState = showDialogState
            )
        }
    }
}

suspend fun getFilteredItems(
    clockInItemDao: ClockInItemDao,
    clockInRecordDao: ClockInRecordDao
): ClockInStatus = withContext(Dispatchers.IO) {
    val allItems = clockInItemDao.getAllItems()
    val today = LocalDate.now()

    val clockedInItems = mutableListOf<ClockInItem>()
    val unClockedInItems = mutableListOf<ClockInItem>()

    // 并发检查每个条目的最新打卡记录，过滤出未在今日打卡的条目
    val filteredItems = allItems.map { item ->
        async {
            val mostRecentRecord = clockInRecordDao.getMostRecentRecordForItem(item.itemId)
            // 如果没有打卡记录或最后一次打卡的日期不是今天，则未打卡
            if (mostRecentRecord == null || mostRecentRecord.timestamp.toLocalDate() != today) {
                unClockedInItems.add(item)
            } else {
                clockedInItems.add(item)
            }
        }
    }.awaitAll()

    ClockInStatus(mutableStateOf(clockedInItems), mutableStateOf(unClockedInItems))
}

@Composable
fun ClockInItemList(
    itemsState: ClockInStatus,
    onItemClicked: (Int) -> Unit
) {
    val clockInItemDao = LocalClockInItemDao.current
    val clockInRecordDao = LocalClockInRecordDao.current
    val coroutineScope = rememberCoroutineScope()

    // 初始加载数据，只显示今日未打卡条目
    LaunchedEffect(Unit) {
        coroutineScope.launch(Dispatchers.IO) {
            val filteredItems = getFilteredItems(
                clockInItemDao = clockInItemDao,
                clockInRecordDao = clockInRecordDao
            )

            withContext(Dispatchers.Main) {
                itemsState.clockedInItems.value = filteredItems.clockedInItems.value
                itemsState.unClockedInItems.value = filteredItems.unClockedInItems.value
            }
        }
    }

    // 打卡操作，打卡后移除该条目
    val onClockIn: (ClockInItem) -> Unit = { item ->
        coroutineScope.launch(Dispatchers.IO) {
            // 更新打卡次数
            clockInItemDao.incrementClockInCount(item.itemId)
            // 插入打卡记录，记录当前时间
            val record = ClockInRecord(
                itemId = item.itemId,
                timestamp = LocalDateTime.now()
            )
            clockInRecordDao.insert(record)
            // 切换到主线程后从列表中移除该条目
            withContext(Dispatchers.Main) {
                itemsState.unClockedInItems.value.find { it.itemId == item.itemId }?.let { itemToMove ->
                    val unClockedInItems = itemsState.unClockedInItems.value.toMutableList().apply {
                        remove(itemToMove)
                    }
                    val clockedInItems = itemsState.clockedInItems.value.toMutableList().apply {
                        add(itemToMove)
                    }

                    itemsState.unClockedInItems.value = unClockedInItems
                    itemsState.clockedInItems.value = clockedInItems
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 未打卡列表部分
        if (itemsState.unClockedInItems.value.isNotEmpty()) {
            item {
                Text(
                    text = "未打卡",
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            items(itemsState.unClockedInItems.value) { item ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = item.name)
                        IconButton(onClick = { onItemClicked(item.itemId) }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.List,
                                contentDescription = "查看详情"
                            )
                        }
                        IconButton(onClick = { onClockIn(item) }) {
                            Icon(
                                imageVector = Icons.Filled.Done,
                                contentDescription = "打卡"
                            )
                        }
                    }
                }
            }
        }
        // 已打卡列表部分
        if (itemsState.clockedInItems.value.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "已打卡",
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            items(itemsState.clockedInItems.value) { item ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = item.name)
                        IconButton(onClick = { onItemClicked(item.itemId) }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.List,
                                contentDescription = "查看详情"
                            )
                        }
                        // 已打卡条目无需显示打卡按钮
                    }
                }
            }
        }
    }
}

@Composable
fun AddItemDialog(
    itemsState: ClockInStatus,
    showDialogState: MutableState<Boolean>
){
    val clockInItemDao = LocalClockInItemDao.current
    val clockInRecordDao = LocalClockInRecordDao.current

    var newItemName by remember { mutableStateOf("") }
    var newItemDescription by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    // 当 showAddDialog 为 true 时显示 AlertDialog 对话框
    if (showDialogState.value) {
        AlertDialog(
            onDismissRequest = { showDialogState.value = false },
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

                                val filteredItems = getFilteredItems(
                                    clockInItemDao = clockInItemDao,
                                    clockInRecordDao = clockInRecordDao
                                )

                                withContext(Dispatchers.Main) {
                                    itemsState.unClockedInItems.value = itemsState.unClockedInItems.value + newItem
                                    // 清空输入，并关闭对话框
                                    newItemName = ""
                                    newItemDescription = ""
                                    showDialogState.value = false
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
                        showDialogState.value = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}