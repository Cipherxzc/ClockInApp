package com.cipherxzc.clockinapp.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ClockInItemDao {
    @Insert
    suspend fun insert(item: ClockInItem)

    @Delete
    suspend fun delete(item: ClockInItem)

    @Query("SELECT * FROM clock_in_items WHERE userId = :userId ORDER BY itemId")
    suspend fun getItemsByUser(userId: String): List<ClockInItem>

    @Query("SELECT * FROM clock_in_items WHERE itemId = :itemId LIMIT 1")
    suspend fun getItemById(itemId: Int): ClockInItem?

    @Query("UPDATE clock_in_items SET clockInCount = clockInCount + 1 WHERE itemId = :itemId")
    suspend fun incrementClockInCount(itemId: Int)

    @Query("UPDATE clock_in_items SET clockInCount = clockInCount - 1 WHERE itemId = :itemId")
    suspend fun decrementClockInCount(itemId: Int)
}
