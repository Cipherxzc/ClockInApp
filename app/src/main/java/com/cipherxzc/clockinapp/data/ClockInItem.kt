package com.cipherxzc.clockinapp.data


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clock_in_items")
data class ClockInItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String?,
    val clockInCount: Int = 0
)