package com.cipherxzc.clockinapp

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.cipherxzc.clockinapp.data.AppDatabase
import com.cipherxzc.clockinapp.data.ClockInItem
import com.cipherxzc.clockinapp.data.ClockInItemDao
import com.cipherxzc.clockinapp.data.ClockInRecordDao
import com.cipherxzc.clockinapp.ui.ClockInApp
import com.cipherxzc.clockinapp.ui.theme.ScaffoldExampleTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.core.content.edit
import com.cipherxzc.clockinapp.data.ClockInRecord
import java.time.LocalDateTime
import java.time.Month

class MainActivity : ComponentActivity() {
    private lateinit var database: AppDatabase
    private lateinit var clockInItemDao: ClockInItemDao
    private lateinit var clockInRecordDao: ClockInRecordDao
    private lateinit var sharedPreferences: SharedPreferences

    // 定义 SharedPreferences 的 Key
    companion object {
        private const val PREFS_NAME = "app_prefs"
        private const val KEY_IS_INITIALIZED = "is_initialized"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "clock-in_database"
        ).build()

        clockInItemDao = database.clockInItemDao()
        clockInRecordDao = database.clockInRecordDao()

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        // 首次启动则插入默认数据
        if (!sharedPreferences.getBoolean(KEY_IS_INITIALIZED, false)) {
            lifecycleScope.launch(Dispatchers.IO) {
                insertDefaultData(clockInItemDao, clockInRecordDao)
                // 标记为已初始化
                sharedPreferences.edit() { putBoolean(KEY_IS_INITIALIZED, true) }
            }
        }

        setContent {
            ScaffoldExampleTheme {
                ClockInApp(clockInItemDao, clockInRecordDao)
            }
        }
    }
}

private suspend fun insertDefaultData(
    clockInItemDao: ClockInItemDao,
    clockInRecordDao: ClockInRecordDao
) {
    val defaultItems = listOf(
        ClockInItem(name = "早起", clockInCount = 8, description = "早睡早起身体好！"),
        ClockInItem(name = "锻炼", clockInCount = 1, description = "无体育，不清华！"),
        ClockInItem(name = "读书", clockInCount = 3, description = "书山有路勤为径"),
        ClockInItem(name = "背单词", clockInCount = 0, description = "目标托福105分！")
    )
    val defaultRecords = listOf(
        ClockInRecord(
            itemId = 1,
            timestamp = LocalDateTime.of(2025, Month.APRIL, 1, 9, 0)
        ),
        ClockInRecord(
            itemId = 1,
            timestamp = LocalDateTime.of(2025, Month.APRIL, 7, 9, 0)
        ),
        ClockInRecord(
            itemId = 1,
            timestamp = LocalDateTime.of(2025, Month.APRIL, 8, 9, 0)
        ),
        ClockInRecord(
            itemId = 1,
            timestamp = LocalDateTime.of(2025, Month.APRIL, 9, 9, 0)
        ),
        ClockInRecord(
            itemId = 1,
            timestamp = LocalDateTime.of(2025, Month.APRIL, 10, 9, 0)
        ),
        ClockInRecord(
            itemId = 1,
            timestamp = LocalDateTime.of(2025, Month.APRIL, 11, 9, 0)
        ),
        ClockInRecord(
            itemId = 1,
            timestamp = LocalDateTime.of(2025, Month.APRIL, 12, 9, 0)
        ),
        ClockInRecord(
            itemId = 1,
            timestamp = LocalDateTime.of(2025, Month.APRIL, 13, 9, 0)
        ),
        ClockInRecord(
            itemId = 2,
            timestamp = LocalDateTime.of(2025, Month.APRIL, 10, 9, 0)
        ),
        ClockInRecord(
            itemId = 3,
            timestamp = LocalDateTime.of(2025, Month.APRIL, 4, 9, 0)
        ),
        ClockInRecord(
            itemId = 3,
            timestamp = LocalDateTime.of(2025, Month.APRIL, 10, 9, 0)
        ),
        ClockInRecord(
            itemId = 3,
            timestamp = LocalDateTime.of(2025, Month.APRIL, 13, 9, 0)
        )
    )

    defaultItems.forEach {
        clockInItemDao.insert(it)
    }
    defaultRecords.forEach {
        clockInRecordDao.insert(it)
    }
}