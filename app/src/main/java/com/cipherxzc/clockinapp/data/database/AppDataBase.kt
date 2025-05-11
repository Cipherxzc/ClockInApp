package com.cipherxzc.clockinapp.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [ClockInItem::class, ClockInRecord::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun clockInItemDao(): ClockInItemDao
    abstract fun clockInRecordDao(): ClockInRecordDao
}
