package com.maduhansa.wellbeingtracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// One row in the "mood_checkins" table = one check-in.
@Entity(tableName = "mood_checkins")
data class MoodCheckIn(
    @PrimaryKey(autoGenerate = true) val id: Long = 0, // unique number, created automatically
    val timestamp: Long,      // when the check-in was saved (milliseconds since 1970)
    val mood: Int,            // 1 = very bad ... 7 = very good
    val energy: Int,          // 1 = very low ... 7 = very high
    val stress: Int,          // 1 = very low ... 7 = very high
    val sleepHours: Float,    // hours slept last night
    val note: String? = null  // optional free text (the "?" means it can be empty)
)