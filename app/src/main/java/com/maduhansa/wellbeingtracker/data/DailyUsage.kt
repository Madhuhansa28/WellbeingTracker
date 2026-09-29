package com.maduhansa.wellbeingtracker.data

import androidx.room.Entity

// One row = minutes spent in one app category on one day.
@Entity(tableName = "daily_usage", primaryKeys = ["date", "category"])
data class DailyUsage(
    val date: String,      // e.g. "2026-09-28"
    val category: String,  // e.g. "social", "video", "messaging"
    val minutes: Int
)

// Used for the "last 7 days" summary (not a table, just a query result).
data class DayTotal(
    val date: String,
    val totalMinutes: Int
)