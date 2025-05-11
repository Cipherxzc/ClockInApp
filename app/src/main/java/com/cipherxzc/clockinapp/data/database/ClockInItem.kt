package com.cipherxzc.clockinapp.data.database


import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp
import java.time.LocalDateTime

@Entity(tableName = "clock_in_items")
data class ClockInItem(
    @PrimaryKey val itemId: String,
    val userId: String,
    val name: String,
    val description: String? = null,
    var clockInCount: Int = 0,
    var lastModified: Timestamp = Timestamp.now(),
    var isSynced: Boolean = false,
    var isDeleted: Boolean = false,
)