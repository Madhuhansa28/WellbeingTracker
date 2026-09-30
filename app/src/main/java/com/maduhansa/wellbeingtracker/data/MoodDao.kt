package com.maduhansa.wellbeingtracker.data


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MoodDao {

    // Save a new check-in. "suspend" means it runs in the background,
    // so the screen doesn't freeze while saving.
    @Insert
    suspend fun insert(checkIn: MoodCheckIn)

    // Get all check-ins, newest first. "Flow" means the screen
    // updates automatically whenever a new check-in is saved.
    @Query("SELECT * FROM mood_checkins ORDER BY timestamp DESC")
    fun getAll(): Flow<List<MoodCheckIn>>

    // All check-ins at once, oldest first (for export).
    @Query("SELECT * FROM mood_checkins ORDER BY timestamp ASC")
    suspend fun getAllOnce(): List<MoodCheckIn>

    // Number of check-ins (updates live).
    @Query("SELECT COUNT(*) FROM mood_checkins")
    fun count(): Flow<Int>

    // Permanently delete all check-ins.
    @Query("DELETE FROM mood_checkins")
    suspend fun deleteAll()
}