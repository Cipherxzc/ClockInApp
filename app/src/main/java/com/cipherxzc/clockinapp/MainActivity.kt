package com.cipherxzc.clockinapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.room.Room
import com.cipherxzc.clockinapp.data.AppDatabase
import com.cipherxzc.clockinapp.data.ClockInItemDao
import com.cipherxzc.clockinapp.data.ClockInRecordDao
import com.cipherxzc.clockinapp.ui.ClockInApp
import com.cipherxzc.clockinapp.ui.theme.ScaffoldExampleTheme

class MainActivity : ComponentActivity() {
    private lateinit var database: AppDatabase
    private lateinit var clockInItemDao: ClockInItemDao
    private lateinit var clockInRecordDao: ClockInRecordDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "clock_in_db"
        ).build()
        clockInItemDao = database.clockInItemDao()
        clockInRecordDao = database.clockInRecordDao()

        setContent {
            ScaffoldExampleTheme {
                ClockInApp(clockInItemDao, clockInRecordDao)
            }
        }
    }
}