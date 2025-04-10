package com.cipherxzc.clockinapp.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase

@Entity(tableName = "clock_in_records")
data class ClockInRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long
)

@Dao
interface ClockInRecordDao {
    @Insert
    suspend fun insert(record: ClockInRecord)

    @Query("SELECT * FROM clock_in_records ORDER BY timestamp DESC")
    suspend fun getAllRecords(): List<ClockInRecord>
}

@Database(entities = [ClockInRecord::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun clockInRecordDao(): ClockInRecordDao
}
