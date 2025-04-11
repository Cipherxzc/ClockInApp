package com.cipherxzc.clockinapp.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ClockInRecordDao {
    @Insert
    suspend fun insert(record: ClockInRecord)

    @Query("SELECT * FROM clock_in_records WHERE itemId = :itemId ORDER BY timestamp DESC")
    suspend fun getAllRecordsForItem(itemId: Int): List<ClockInRecord>

    @Query("SELECT * FROM clock_in_records WHERE itemId = :itemId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getMostRecentRecordForItem(itemId: Int): ClockInRecord?

    @Delete
    suspend fun deleteRecord(record: ClockInRecord)

    @Query("DELETE FROM clock_in_records WHERE itemId = :itemId")
    suspend fun deleteAllRecordsForItem(itemId: Int)

    @Query("DELETE FROM clock_in_records WHERE recordId = (SELECT recordId FROM clock_in_records WHERE itemId = :itemId ORDER BY timestamp DESC LIMIT 1)")
    suspend fun deleteMostRecentRecordForItem(itemId: Int)
}
