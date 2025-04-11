package com.cipherxzc.clockinapp.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.cipherxzc.clockinapp.data.ClockInItem

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
        contentWindowInsets = WindowInsets.systemBars
            .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
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
        Box(modifier = Modifier
            .padding(innerPadding)
            .navigationBarsPadding()
        ) {
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
