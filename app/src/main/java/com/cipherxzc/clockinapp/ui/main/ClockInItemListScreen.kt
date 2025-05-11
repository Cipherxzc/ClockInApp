package com.cipherxzc.clockinapp.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cipherxzc.clockinapp.ui.viewmodel.ItemListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClockInItemListScreen(
    user: String,
    itemListViewModel: ItemListViewModel,
    onItemClicked: (String) -> Unit,
    onLogout: () -> Unit
) {
    // 整体页面结构：Scaffold 包含 TopAppBar 和 FloatingActionButton
    Scaffold(
        contentWindowInsets = WindowInsets.systemBars
            .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
        topBar = {
            TopAppBar(title = { Text("Clock-In App") })
        },
        bottomBar = {
            BottomAppBar(
                modifier = Modifier.height(50.dp)
            ){
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 15.dp, vertical = 0.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = user)
                    Button(onClick = onLogout) {
                        Text("登出")
                    }
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
