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
    var lastModified: Timestamp = Timestamp.now(),
    var isSynced: Boolean = false,
    var isDeleted: Boolean = false,
)
