package com.maduhansa.wellbeingtracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UsageDao {

    // Save rows. If a row for the same date + category exists, replace it
    // (so re-reading today's usage updates the numbers instead of duplicating).
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<DailyUsage>)

    // All categories for one day, biggest first.
    @Query("SELECT * FROM daily_usage WHERE date = :date ORDER BY minutes DESC")
    fun getForDate(date: String): Flow<List<DailyUsage>>

    // Total minutes per day for the last 7 days that have data.
    @Query("SELECT date, SUM(minutes) AS totalMinutes FROM daily_usage GROUP BY date ORDER BY date DESC LIMIT 7")
    fun getDailyTotals(): Flow<List<DayTotal>>
}