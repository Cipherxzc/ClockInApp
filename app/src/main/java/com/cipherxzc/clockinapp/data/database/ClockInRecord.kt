package com.cipherxzc.clockinapp.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp
import java.time.LocalDateTime

@Entity(tableName = "clock_in_records")
data class ClockInRecord(
    @PrimaryKey val recordId: String,
    val userId: String,
    val itemId: String,
    val timestamp: Timestamp = Timestamp.now(),
    val lastModified: Timestamp = Timestamp.now(),
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false,
)
