package com.cipherxzc.clockinapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "clock_in_records")
data class ClockInRecord(
    @PrimaryKey(autoGenerate = true) val recordId: Int = 0,
    val itemId: Int,
    val timestamp: LocalDateTime
)
