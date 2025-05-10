package com.cipherxzc.clockinapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.cipherxzc.clockinapp.data.AppDatabase
import com.cipherxzc.clockinapp.data.ClockInItem
import com.cipherxzc.clockinapp.data.ClockInRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.Month

class DatabaseViewModel(application: Application) : AndroidViewModel(application) {
    private val database: AppDatabase by lazy {
        Room.databaseBuilder(
            application,
            AppDatabase::class.java,
            "clock-in_database"
        ).build()
    }
    val clockInItemDao by lazy { database.clockInItemDao() }
    val clockInRecordDao by lazy { database.clockInRecordDao() }

    private var currentUserId: String? = null

    fun setCurrentUserId(userId: String) {
        currentUserId = userId
    }

    fun insertDefaultData(userId: String? = currentUserId) {
        if (userId == null) {
            // TODO: userId不能为null
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val defaultItems = listOf(
                ClockInItem(userId = userId, name = "早起", clockInCount = 8, description = "早睡早起身体好！"),
                ClockInItem(userId = userId, name = "锻炼", clockInCount = 1, description = "无体育，不华清！"),
                ClockInItem(userId = userId, name = "读书", clockInCount = 3, description = "书山有路勤为径！"),
                ClockInItem(userId = userId, name = "背单词", clockInCount = 0, description = "目标托福105分！")
            )

            val defaultRecords = listOf(
                ClockInRecord(userId = userId, itemId = 1, timestamp = LocalDateTime.of(2025, Month.APRIL, 1, 9, 0)),
                ClockInRecord(userId = userId, itemId = 1, timestamp = LocalDateTime.of(2025, Month.APRIL, 7, 9, 0)),
                ClockInRecord(userId = userId, itemId = 1, timestamp = LocalDateTime.of(2025, Month.APRIL, 8, 9, 0)),
                ClockInRecord(userId = userId, itemId = 1, timestamp = LocalDateTime.of(2025, Month.APRIL, 9, 9, 0)),
                ClockInRecord(userId = userId, itemId = 1, timestamp = LocalDateTime.of(2025, Month.APRIL, 10, 9, 0)),
                ClockInRecord(userId = userId, itemId = 1, timestamp = LocalDateTime.of(2025, Month.APRIL, 11, 9, 0)),
                ClockInRecord(userId = userId, itemId = 1, timestamp = LocalDateTime.of(2025, Month.APRIL, 12, 9, 0)),
                ClockInRecord(userId = userId, itemId = 1, timestamp = LocalDateTime.of(2025, Month.APRIL, 13, 9, 0)),
                ClockInRecord(userId = userId, itemId = 2, timestamp = LocalDateTime.of(2025, Month.APRIL, 10, 9, 0)),
                ClockInRecord(userId = userId, itemId = 3, timestamp = LocalDateTime.of(2025, Month.APRIL, 4, 9, 0)),
                ClockInRecord(userId = userId, itemId = 3, timestamp = LocalDateTime.of(2025, Month.APRIL, 10, 9, 0)),
                ClockInRecord(userId = userId, itemId = 3, timestamp = LocalDateTime.of(2025, Month.APRIL, 13, 9, 0))
            )

            defaultItems.forEach { clockInItemDao.insert(it) }
            defaultRecords.forEach { clockInRecordDao.insert(it) }
        }
    }
}