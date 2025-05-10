package com.cipherxzc.clockinapp.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
    interface ClockInRecordDao {
        @Insert
        suspend fun insert(record: ClockInRecord)

        @Delete
        suspend fun deleteRecord(record: ClockInRecord)

        @Query("DELETE FROM clock_in_records WHERE itemId = :itemId AND userId = :userId")
        suspend fun deleteRecordsByItem(userId: String, itemId: Int)

        @Query("SELECT * FROM clock_in_records WHERE itemId = :itemId AND userId = :userId ORDER BY timestamp DESC")
        suspend fun getRecordsByItem(userId: String, itemId: Int): List<ClockInRecord>

        @Query("SELECT * FROM clock_in_records WHERE itemId = :itemId AND userId = :userId ORDER BY timestamp DESC LIMIT 1")
        suspend fun getMostRecentRecordByItem(userId: String, itemId: Int): ClockInRecord?

        @Query("DELETE FROM clock_in_records WHERE recordId = (SELECT recordId FROM clock_in_records WHERE itemId = :itemId AND userId = :userId ORDER BY timestamp DESC LIMIT 1)")
        suspend fun deleteMostRecentRecordByItem(userId: String, itemId: Int)
    }
