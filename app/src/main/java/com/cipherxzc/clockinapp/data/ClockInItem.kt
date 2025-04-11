package com.cipherxzc.clockinapp.data


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clock_in_items")
data class ClockInItem(
    @PrimaryKey(autoGenerate = true) val itemId: Int = 0,
    val name: String,
    val description: String?,
    var clockInCount: Int = 0
)