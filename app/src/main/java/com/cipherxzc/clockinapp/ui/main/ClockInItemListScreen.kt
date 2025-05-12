package com.cipherxzc.clockinapp.ui.main

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cipherxzc.clockinapp.ui.viewmodel.ItemListViewModel
import com.cipherxzc.clockinapp.ui.viewmodel.SyncViewModel
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClockInItemListScreen(
    userName: String,
    itemListViewModel: ItemListViewModel,
    syncViewModel: SyncViewModel,
    onItemClicked: (String) -> Unit,
    onLogout: () -> Unit
) {
    val isSyncing by syncViewModel.isSyncing.collectAsState()

    // 整体页面结构：Scaffold 包含 TopAppBar 和 FloatingActionButton
    Scaffold(
        contentWindowInsets = WindowInsets.systemBars
            .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 15.dp, vertical = 0.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = userName)
                        Button(onClick = onLogout) {
                            Text("登出")
                        }
                    }
                })
        },
        bottomBar = {
            BottomAppBar {
                val context = LocalContext.current
                Button(
                    onClick = {
                        syncViewModel.sync(
                            onError = { throwable ->
                                Toast.makeText(context, "同步失败：${throwable.message}", Toast.LENGTH_LONG).show()
                            },
                            onComplete = {
                                Toast.makeText(context, "同步完成！", Toast.LENGTH_SHORT).show()
                                itemListViewModel.loadItems()
                            }
                        )
                    },
                    enabled = !isSyncing
                ) {
                    Text(text = if (isSyncing) "同步中..." else "同步")
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // 点击添加按钮后显示输入对话框
                    itemListViewModel.showDialog()
                }
            ) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "Add New Item")
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier
            .padding(innerPadding)
            .navigationBarsPadding()
        ) {
            ClockInItemList(
                itemListViewModel = itemListViewModel,
                onItemClicked = onItemClicked
            )

            AddItemDialog(
                itemListViewModel = itemListViewModel
            )
        }
    }
}
