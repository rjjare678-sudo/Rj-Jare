package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val isCompleted: Boolean = false,
    val category: String = "General",
    val priority: Int = 1, // 0 = Low, 1 = Medium, 2 = High
    val createdAt: Long = System.currentTimeMillis(),
    
    // Advanced Reminders
    val reminderTime: Long? = null,
    val recurrence: String = "None", // None, Daily, Weekly, Monthly
    
    // Location-based Reminders
    val locationName: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val radiusInMeters: Float? = null,
    
    // Productivity Trends Track
    val completedAt: Long? = null,
    val hasTriggeredReminder: Boolean = false
)
