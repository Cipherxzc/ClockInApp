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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ScaffoldExampleTheme {
                ClockInApp()
            }
        }
    }
}