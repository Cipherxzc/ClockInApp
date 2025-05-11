package com.cipherxzc.clockinapp.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import java.time.LocalDateTime

@Dao
interface ClockInItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(item: ClockInItem)

    @Delete
    suspend fun delete(item: ClockInItem)

    @Query("SELECT * FROM clock_in_items WHERE userId = :userId AND isDeleted = 0 ORDER BY itemId")
    suspend fun getItemsByUser(userId: String): List<ClockInItem>

    @Query("SELECT * FROM clock_in_items WHERE itemId = :itemId LIMIT 1")
    suspend fun getItemById(itemId: String): ClockInItem?

    @Query("SELECT * FROM clock_in_items WHERE userId = :userId AND isSynced = 0")
    suspend fun getUnsyncedItems(userId: String): List<ClockInItem>

    @Query("UPDATE clock_in_items SET clockInCount = clockInCount + 1 WHERE itemId = :itemId")
    suspend fun incrementClockInCount(itemId: String)

    @Query("UPDATE clock_in_items SET clockInCount = clockInCount - 1 WHERE itemId = :itemId")
    suspend fun decrementClockInCount(itemId: String)

    @Query("UPDATE clock_in_items SET isDeleted = 1 WHERE itemId = :itemId")
    suspend fun markDeleted(itemId: String)
}
