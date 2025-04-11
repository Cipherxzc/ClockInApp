package com.cipherxzc.clockinapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
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

@OptIn(ExperimentalMaterialApi::class)
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

    // 打卡操作
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
            // 切换到主线程后将该条目从未打卡列表移动到已打卡列表
            withContext(Dispatchers.Main) {
                itemsState.unClockedInItems.value.find { it.itemId == item.itemId }?.let { itemToMove ->
                    moveItem(
                        itemListFrom = itemsState.unClockedInItems,
                        itemListTo = itemsState.clockedInItems,
                        itemToMove = itemToMove
                    ){ it.clockInCount++ }
                }
            }
        }
    }
    // 撤销打卡操作
    val onWithdraw: (ClockInItem) -> Unit = { item ->
        coroutineScope.launch(Dispatchers.IO) {
            // 递减打卡次数
            clockInItemDao.decrementClockInCount(item.itemId)
            // 删除最新的打卡记录
            clockInRecordDao.deleteMostRecentRecordForItem(item.itemId)
            // 切换到主线程后将该条目从未打卡列表移动到已打卡列表
            withContext(Dispatchers.Main) {
                itemsState.clockedInItems.value.find { it.itemId == item.itemId }?.let { itemToMove ->
                    moveItem(
                        itemListFrom = itemsState.clockedInItems,
                        itemListTo = itemsState.unClockedInItems,
                        itemToMove = itemToMove
                    ){ it.clockInCount-- }
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
                ItemEntry(
                    modifier = Modifier.animateItem(),
                    item = item,
                    onItemClicked = onItemClicked,
                    onDismiss = onClockIn,
                    reverseSwipe = false
                ){
                    // 定义在滑动时显示的背景区域
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color = MaterialTheme.colorScheme.error, shape = RoundedCornerShape(6.dp))
                            .padding(horizontal = 20.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Done,
                            contentDescription = "打卡"
                        )
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
                ItemEntry(
                    modifier = Modifier.animateItem(),
                    item = item,
                    onItemClicked = onItemClicked,
                    onDismiss = onWithdraw,
                    reverseSwipe = true
                ){
                    // 定义在滑动时显示的背景区域
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color = MaterialTheme.colorScheme.error, shape = RoundedCornerShape(6.dp))
                            .padding(horizontal = 20.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Clear,
                            contentDescription = "撤销打卡"
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ItemEntry(
    modifier: Modifier,
    item: ClockInItem,
    onItemClicked: (Int) -> Unit,
    onDismiss: (ClockInItem) -> Unit,
    reverseSwipe: Boolean = false,
    background: @Composable (RowScope.()->Unit)
){
    // 使用 SwipeToDismiss 来包装整个列表项
    val dismissState = rememberDismissState(
        confirmStateChange = { dismissValue ->
            // 根据滑动方向判断是到start还是end
            if (!reverseSwipe) {
                // 从右往左滑 DismissDirection.EndToStart
                if (dismissValue == DismissValue.DismissedToStart) {
                    onDismiss(item)
                }
            } else {
                // 从左往右滑 DismissDirection.StartToEnd
                if (dismissValue == DismissValue.DismissedToEnd) {
                    onDismiss(item)
                }
            }
            // 不能使用true，因为我已经自己删除了打卡项，返回true会导致二次删除引起ui出错！！！
            false
        }
    )

    // 根据 reverseSwipe 来设定滑动方向
    val directions = if (!reverseSwipe) {
        setOf(DismissDirection.EndToStart)
    } else {
        setOf(DismissDirection.StartToEnd)
    }

    SwipeToDismiss(
        state = dismissState,
        directions = directions,
        background = background,
        dismissContent = {
            // 列表项的主要内容
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(6.dp))
                    .padding(8.dp)  // 添加内边距使整体效果更好
            ) {
                // 第一行: 条目名称与详情按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.name,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp)
                    )
                    IconButton(onClick = { onItemClicked(item.itemId) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.List,
                            contentDescription = "查看详情"
                        )
                    }
                }
                // 第二行: 打卡天数标识
                Text(
                    text = "已打卡 ${item.clockInCount} 天",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        modifier = modifier
    )

    Spacer(modifier = Modifier.height(10.dp))
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

fun moveItem(
    itemListFrom: MutableState<List<ClockInItem>>,
    itemListTo: MutableState<List<ClockInItem>>,
    itemToMove: ClockInItem,
    updateItem: (ClockInItem) -> Unit
){
    val newItemsFrom = itemListFrom.value.toMutableList().apply {
        remove(itemToMove)
    }
    updateItem(itemToMove)
    val newItemsTo = itemListTo.value.toMutableList().apply {
        add(itemToMove)
    }

    itemListFrom.value = newItemsFrom
    itemListTo.value = newItemsTo
}
