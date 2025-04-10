package com.cipherxzc.clockinapp.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.cipherxzc.clockinapp.data.ClockInRecord
import com.cipherxzc.clockinapp.data.ClockInRecordDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyApp(clockInRecordDao: ClockInRecordDao) {
    var records by remember { mutableStateOf(listOf<ClockInRecord>()) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch(Dispatchers.IO) {
            records = clockInRecordDao.getAllRecords()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Clock-In App") }
            )
        },
        content = { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    Button(onClick = {
                        coroutineScope.launch(Dispatchers.IO) {
                            val timestamp = System.currentTimeMillis()
                            val record = ClockInRecord(timestamp = timestamp)
                            clockInRecordDao.insert(record)
                            val newRecords = clockInRecordDao.getAllRecords()
                            withContext(Dispatchers.Main) {
                                records = newRecords
                            }
                        }
                    }) {
                        Text("Clock In")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyColumn {
                        items(records) { record ->
                            Text("Clocked in at: ${Date(record.timestamp)}")
                        }
                    }
                }
            }
        }
    )
}