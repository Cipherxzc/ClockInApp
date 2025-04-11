package com.cipherxzc.clockinapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clock_in_records")
data class ClockInRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val itemId: Int,
    val timestamp: Long
)
