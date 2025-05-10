package com.cipherxzc.clockinapp.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.cipherxzc.clockinapp.data.ClockInItem
import com.cipherxzc.clockinapp.data.ClockInItemDao
import com.cipherxzc.clockinapp.data.ClockInRecord
import com.cipherxzc.clockinapp.data.ClockInRecordDao
import com.cipherxzc.clockinapp.ui.LocalClockInItemDao
import com.cipherxzc.clockinapp.ui.LocalClockInRecordDao
import com.cipherxzc.clockinapp.ui.LocalCurrentUser
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
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
    val currentUser = LocalCurrentUser.current
    val coroutineScope = rememberCoroutineScope()

    val userId = currentUser.uid

    // 初始加载数据，只显示今日未打卡条目
    LaunchedEffect(Unit) {
        coroutineScope.launch(Dispatchers.IO) {
            val filteredItems = getFilteredItems(clockInItemDao, clockInRecordDao, userId)

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
                userId = userId,
                timestamp = LocalDateTime.now()
            )
            clockInRecordDao.insert(record)
            val newItemState = getFilteredItems(clockInItemDao, clockInRecordDao, userId)
            // 切换到主线程后将该条目从未打卡列表移动到已打卡列表
            withContext(Dispatchers.Main) {
                itemsState.clockedInItems.value = newItemState.clockedInItems.value
                itemsState.unClockedInItems.value = newItemState.unClockedInItems.value
            }
        }
    }
    // 撤销打卡操作
    val onWithdraw: (ClockInItem) -> Unit = { item ->
        coroutineScope.launch(Dispatchers.IO) {
            // 递减打卡次数
            clockInItemDao.decrementClockInCount(item.itemId)
            // 删除最新的打卡记录
            clockInRecordDao.deleteMostRecentRecordByItem(userId, item.itemId)
            val newItemState = getFilteredItems(clockInItemDao, clockInRecordDao, userId)
            // 切换到主线程后将该条目从未打卡列表移动到已打卡列表
            withContext(Dispatchers.Main) {
                itemsState.clockedInItems.value = newItemState.clockedInItems.value
                itemsState.unClockedInItems.value = newItemState.unClockedInItems.value
            }
        }
    }
    // 删除条目处理函数
    val onDeleteItem: (ClockInItem) -> Unit = { item ->
        coroutineScope.launch(Dispatchers.IO) {
            // 删除条目及关联记录
            clockInItemDao.delete(item)
            clockInRecordDao.deleteRecordsByItem(userId, item.itemId)

            // 更新列表状态
            val newItemState = getFilteredItems(clockInItemDao, clockInRecordDao, userId)
            withContext(Dispatchers.Main) {
                itemsState.clockedInItems.value = newItemState.clockedInItems.value
                itemsState.unClockedInItems.value = newItemState.unClockedInItems.value
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
            items(itemsState.unClockedInItems.value, key = { it.itemId }) { item ->
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically() + fadeIn(),
                    exit = slideOutVertically() + fadeOut()
                ) {
                    ItemEntry(
                        modifier = Modifier.animateItem(),
                        item = item,
                        onItemClicked = onItemClicked,
                        onDismiss = onClockIn,
                        onDelete = onDeleteItem,
                        reverseSwipe = false
                    ) {
                        // 定义在滑动时显示的背景区域
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    shape = RoundedCornerShape(6.dp)
                                )
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
            items(itemsState.clockedInItems.value, key = { it.itemId }) { item ->
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically() + fadeIn(),
                    exit = slideOutVertically() + fadeOut()
                ) {
                    ItemEntry(
                        modifier = Modifier.animateItem(),
                        item = item,
                        onItemClicked = onItemClicked,
                        onDismiss = onWithdraw,
                        onDelete = onDeleteItem,
                        reverseSwipe = true
                    ) {
                        // 定义在滑动时显示的背景区域
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    color = MaterialTheme.colorScheme.error,
                                    shape = RoundedCornerShape(6.dp)
                                )
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
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun ItemEntry(
    modifier: Modifier,
    item: ClockInItem,
    onItemClicked: (Int) -> Unit,
    onDismiss: (ClockInItem) -> Unit,
    onDelete: (ClockInItem) -> Unit,
    reverseSwipe: Boolean = false,
    background: @Composable RowScope.()->Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    // 删除确认对话框
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除条目") },
            text = { Text("确定要永久删除该条目吗？此操作不可撤销。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(item)
                        showDeleteDialog = false
                    }
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("取消")
                }
            }
        )
    }
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

    Surface(
        modifier = Modifier
            .combinedClickable(
                onClick = { onItemClicked(item.itemId) },
                onLongClick = { showDeleteDialog = true }
            ),
        color = Color.Transparent,
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            shape = MaterialTheme.shapes.medium,
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            SwipeToDismiss(
                state = dismissState,
                directions = directions,
                background = background,
                dismissContent = {
                    // 列表项的主要内容
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(6.dp)
                            )
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

            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}

suspend fun getFilteredItems(
    clockInItemDao: ClockInItemDao,
    clockInRecordDao: ClockInRecordDao,
    userId: String
): ClockInStatus = withContext(Dispatchers.IO) {

    val allItems = clockInItemDao.getItemsByUser(userId)
    val today = LocalDate.now()

    val clockedInItems = mutableListOf<ClockInItem>()
    val unClockedInItems = mutableListOf<ClockInItem>()

    // 并发检查每个条目的最新打卡记录，过滤出未在今日打卡的条目
    val filteredItems = allItems.map { item ->
        val mostRecentRecord = clockInRecordDao.getMostRecentRecordByItem(userId, item.itemId)
        // 如果没有打卡记录或最后一次打卡的日期不是今天，则未打卡
        if (mostRecentRecord == null || mostRecentRecord.timestamp.toLocalDate() != today) {
            unClockedInItems.add(item)
        } else {
            clockedInItems.add(item)
        }
    }

    ClockInStatus(
        mutableStateOf(clockedInItems.toList()),
        mutableStateOf(unClockedInItems.toList())
    )
}
