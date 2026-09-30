package com.maduhansa.wellbeingtracker.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object DataExporter {

    // Writes all data into a .zip file at the location the user chose.
    suspend fun exportZip(context: Context, uri: Uri, includeNotes: Boolean) {
        withContext(Dispatchers.IO) {  // file work runs in the background
            val db = AppDatabase.getDatabase(context)
            val checkIns = db.moodDao().getAllOnce()
            val usage = db.usageDao().getAllOnce()
            val participantId = ParticipantId.get(context)
            val zone = ZoneId.systemDefault()

            // ---- mood_checkins.csv ----
            val moodCsv = buildString {
                append("participant_id,checkin_id,timestamp_ms,local_datetime,date,mood,energy,stress,sleep_hours")
                if (includeNotes) append(",note")
                append("\n")
                for (c in checkIns) {
                    val time = Instant.ofEpochMilli(c.timestamp).atZone(zone)
                    append("$participantId,${c.id},${c.timestamp},${time.toOffsetDateTime()},${time.toLocalDate()},")
                    append("${c.mood},${c.energy},${c.stress},${c.sleepHours}")
                    if (includeNotes) append(",${csvField(c.note ?: "")}")
                    append("\n")
                }
            }

            // ---- daily_usage.csv ----
            val usageCsv = buildString {
                append("participant_id,date,category,minutes\n")
                for (u in usage) {
                    append("$participantId,${u.date},${u.category},${u.minutes}\n")
                }
            }

            // ---- export_info.txt (metadata) ----
            val info = buildString {
                append("Wellbeing Tracker data export\n")
                append("participant_id: $participantId\n")
                append("exported_at: ${ZonedDateTime.now(zone).toOffsetDateTime()}\n")
                append("time_zone: $zone\n")
                append("database_version: 2\n")
                append("mood_checkins_rows: ${checkIns.size}\n")
                append("daily_usage_rows: ${usage.size}\n")
                append("notes_included: $includeNotes\n")
            }

            // ---- write everything into one zip file ----
            val output = context.contentResolver.openOutputStream(uri)
                ?: throw IllegalStateException("Could not open the file for writing")
            ZipOutputStream(output).use { zip ->
                addFile(zip, "mood_checkins.csv", moodCsv)
                addFile(zip, "daily_usage.csv", usageCsv)
                addFile(zip, "export_info.txt", info)
            }
        }
    }

    private fun addFile(zip: ZipOutputStream, name: String, content: String) {
        zip.putNextEntry(ZipEntry(name))
        zip.write(content.toByteArray(Charsets.UTF_8))
        zip.closeEntry()
    }

    // Wraps text in quotes if it contains commas, quotes or line breaks,
    // so it doesn't break the CSV columns.
    private fun csvField(value: String): String =
        if (value.any { it == ',' || it == '"' || it == '\n' || it == '\r' }) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
}