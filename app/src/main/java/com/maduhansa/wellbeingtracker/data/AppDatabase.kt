package com.maduhansa.wellbeingtracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [MoodCheckIn::class, DailyUsage::class],
    version = 2,            // was 1 — increased because we added a table
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun moodDao(): MoodDao
    abstract fun usageDao(): UsageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // How to upgrade a version-1 database to version 2:
        // just create the new table. Existing check-ins are kept.
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `daily_usage` (" +
                            "`date` TEXT NOT NULL, " +
                            "`category` TEXT NOT NULL, " +
                            "`minutes` INTEGER NOT NULL, " +
                            "PRIMARY KEY(`date`, `category`))"
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "wellbeing.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}