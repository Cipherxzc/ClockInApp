package com.cipherxzc.clockinapp.data


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clock_in_items")
data class ClockInItem(
    @PrimaryKey(autoGenerate = true) val itemId: Int = 0,
    val userId: String,
    val name: String,
    val description: String? = null,
    var clockInCount: Int = 0
)