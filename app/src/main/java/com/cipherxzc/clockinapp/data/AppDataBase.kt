package com.cipherxzc.clockinapp.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ClockInItem::class, ClockInRecord::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun clockInItemDao(): ClockInItemDao
    abstract fun clockInRecordDao(): ClockInRecordDao
}
