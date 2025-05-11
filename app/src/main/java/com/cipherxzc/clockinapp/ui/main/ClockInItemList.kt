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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.cipherxzc.clockinapp.data.database.ClockInItem
import com.cipherxzc.clockinapp.ui.viewmodel.ItemListViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ClockInItemList(
    itemListViewModel: ItemListViewModel,
    onItemClicked: (String) -> Unit
) {
    val clockedInItems by itemListViewModel.clockedInItemsFlow.collectAsState()
    val unClockedInItems by itemListViewModel.unClockedInItemsFlow.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 未打卡列表部分
        if (unClockedInItems.isNotEmpty()) {
            item {
                Text(
                    text = "未打卡",
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            items(unClockedInItems, key = { it.itemId }) { item ->
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically() + fadeIn(),
                    exit = slideOutVertically() + fadeOut()
                ) {
                    ItemEntry(
                        modifier = Modifier.animateItem(),
                        item = item,
                        onItemClicked = onItemClicked,
                        onDismiss = itemListViewModel::clockIn,
                        onDelete = itemListViewModel::deleteItem,
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
        if (clockedInItems.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "已打卡",
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            items(clockedInItems, key = { it.itemId }) { item ->
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically() + fadeIn(),
                    exit = slideOutVertically() + fadeOut()
                ) {
                    ItemEntry(
                        modifier = Modifier.animateItem(),
                        item = item,
                        onItemClicked = onItemClicked,
                        onDismiss = itemListViewModel::withdraw,
                        onDelete = itemListViewModel::deleteItem,
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
    onItemClicked: (String) -> Unit,
    onDismiss: (String) -> Unit,
    onDelete: (String) -> Unit,
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
                        onDelete(item.itemId)
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
                    onDismiss(item.itemId)
                }
            } else {
                // 从左往右滑 DismissDirection.StartToEnd
                if (dismissValue == DismissValue.DismissedToEnd) {
                    onDismiss(item.itemId)
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