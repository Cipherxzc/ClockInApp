package com.cipherxzc.clockinapp.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ClockInRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(item: ClockInRecord)

    @Delete
    suspend fun deleteRecord(record: ClockInRecord)

    @Query("SELECT * FROM clock_in_records WHERE recordId = :recordId LIMIT 1")
    suspend fun getRecordById(recordId: String): ClockInRecord?

    @Query("DELETE FROM clock_in_records WHERE itemId = :itemId AND userId = :userId")
    suspend fun deleteRecordsByItem(userId: String, itemId: String)

    @Query("SELECT * FROM clock_in_records WHERE itemId = :itemId AND userId = :userId ORDER BY timestamp DESC")
    suspend fun getRecordsByItem(userId: String, itemId: String): List<ClockInRecord>

    @Query("SELECT * FROM clock_in_records WHERE userId = :userId AND isSynced = 0")
    suspend fun getUnsyncedRecords(userId: String): List<ClockInRecord>

    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM clock_in_records 
            WHERE userId = :userId 
              AND itemId = :itemId 
              AND timestamp BETWEEN :startTime AND :endTime
        )
    """)
    suspend fun hasRecordInRange(userId: String, itemId: String, startTime: Long, endTime: Long): Boolean

    @Query("UPDATE clock_in_records SET isDeleted = 1 WHERE recordId = :recordId")
    suspend fun markDeleted(recordId: String)

    @Query("SELECT * FROM clock_in_records WHERE userId = :userId AND itemId = :itemId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getMostRecentRecord(userId: String, itemId: String): ClockInRecord?
}
