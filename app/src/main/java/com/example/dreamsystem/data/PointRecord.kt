package com.example.dreamsystem.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "point_records")
data class PointRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val taskDescription: String,
    val points: Int,
    val timestamp: Long = System.currentTimeMillis()
)