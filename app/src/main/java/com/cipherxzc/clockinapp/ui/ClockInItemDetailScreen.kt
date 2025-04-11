package com.cipherxzc.clockinapp.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import com.cipherxzc.clockinapp.data.ClockInRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClockInItemDetailScreen(
    itemId: Int,
) {
    val clockInItemDao = LocalClockInItemDao.current
    val clockInRecordDao = LocalClockInRecordDao.current

    var item by remember { mutableStateOf<ClockInItem?>(null) }
    var records by remember { mutableStateOf<List<ClockInRecord>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(itemId) {
        coroutineScope.launch(Dispatchers.IO) {
            item = clockInItemDao.getItemById(itemId)
            records = clockInRecordDao.getAllRecordsForItem(itemId)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.systemBars
            .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
        topBar = {
            TopAppBar(title = { Text(item?.name?: "error") })
        }
    ) { innerPadding ->
        Box(modifier = Modifier
            .padding(innerPadding)
            .navigationBarsPadding()
        ) {
            if (item != null) {
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("📝 描述", style = MaterialTheme.typography.labelMedium)
                            Text(item?.description ?: "暂无描述")
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("已打卡天数: ${records.size}")
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("打卡记录:")
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
