package com.cipherxzc.clockinapp.data.database


import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp

@Entity(tableName = "clock_in_items")
data class ClockInItem(
    @PrimaryKey val itemId: String,
    val userId: String,
    val name: String,
    val description: String? = null,
    val clockInCount: Int = 0,
    val lastModified: Timestamp = Timestamp.now(),
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false,
)