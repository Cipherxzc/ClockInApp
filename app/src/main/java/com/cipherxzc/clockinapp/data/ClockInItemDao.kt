package com.cipherxzc.clockinapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ClockInItemDao {
    @Insert
    suspend fun insert(item: ClockInItem)

    @Query("SELECT * FROM clock_in_items")
    suspend fun getAllItems(): List<ClockInItem>

    @Query("UPDATE clock_in_items SET clockInCount = clockInCount + 1 WHERE id = :itemId")
    suspend fun incrementClockInCount(itemId: Int)
}
