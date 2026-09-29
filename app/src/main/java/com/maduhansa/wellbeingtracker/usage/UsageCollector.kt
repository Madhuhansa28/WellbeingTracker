package com.maduhansa.wellbeingtracker.usage

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.maduhansa.wellbeingtracker.data.DailyUsage
import com.maduhansa.wellbeingtracker.data.UsageDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId

object UsageCollector {

    // Event types from Android's usage log
    private const val APP_OPENED = 1  // activity came to the foreground
    private const val APP_CLOSED = 2  // activity left the foreground

    // Many apps don't declare a category, so we label common ones ourselves.
    // You can add more apps here (find package names in the Play Store URL).
    private val knownApps = mapOf(
        "com.whatsapp" to "messaging",
        "com.whatsapp.w4b" to "messaging",
        "com.facebook.orca" to "messaging",
        "org.telegram.messenger" to "messaging",
        "com.viber.voip" to "messaging",
        "com.google.android.apps.messaging" to "messaging",
        "com.samsung.android.messaging" to "messaging",
        "com.facebook.katana" to "social",
        "com.facebook.lite" to "social",
        "com.instagram.android" to "social",
        "com.twitter.android" to "social",
        "com.snapchat.android" to "social",
        "com.zhiliaoapp.musically" to "social",      // TikTok
        "com.google.android.youtube" to "video",
        "com.netflix.mediaclient" to "video",
        "com.android.chrome" to "browsing",
        "com.sec.android.app.sbrowser" to "browsing" // Samsung Internet
    )

    // Reads the last few days and saves category totals to the database.
    suspend fun collectRecentDays(context: Context, dao: UsageDao, days: Int = 7) =
        withContext(Dispatchers.IO) {  // run in the background, not on the screen thread
            val today = LocalDate.now()
            for (i in 0 until days) {
                val usage = readDay(context, today.minusDays(i.toLong()))
                if (usage.isNotEmpty()) dao.upsertAll(usage)
            }
        }

    private fun readDay(context: Context, date: LocalDate): List<DailyUsage> {
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val zone = ZoneId.systemDefault()
        val dayStart = date.atStartOfDay(zone).toInstant().toEpochMilli()
        val dayEnd = minOf(
            date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli(),
            System.currentTimeMillis()
        )

        val openedAt = HashMap<String, Long>()     // screen currently open -> when it opened
        val seen = HashSet<String>()                // screens we've already seen today
        val msByPackage = HashMap<String, Long>()   // app -> total milliseconds

        val events = usm.queryEvents(dayStart, dayEnd)
        val event = UsageEvents.Event()
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            val pkg = event.packageName ?: continue
            val key = "$pkg|${event.className}"   // one app can have several screens

            when (event.eventType) {
                APP_OPENED -> {
                    openedAt[key] = event.timeStamp
                    seen.add(key)
                }
                APP_CLOSED -> {
                    // If the first thing we see is "closed", it was opened before midnight,
                    // so we count from the start of the day.
                    val start = openedAt.remove(key) ?: if (seen.add(key)) dayStart else null
                    if (start != null) {
                        msByPackage[pkg] = (msByPackage[pkg] ?: 0L) + (event.timeStamp - start)
                    }
                }
            }
        }
        // Screens still open at the end of the period: count until the end.
        for ((key, start) in openedAt) {
            val pkg = key.substringBefore("|")
            msByPackage[pkg] = (msByPackage[pkg] ?: 0L) + (dayEnd - start)
        }

        // Convert apps -> categories. App names are NOT saved, only category totals.
        val msByCategory = HashMap<String, Long>()
        for ((pkg, ms) in msByPackage) {
            if (pkg == context.packageName) continue          // don't count our own app
            val category = categoryOf(context, pkg) ?: continue
            msByCategory[category] = (msByCategory[category] ?: 0L) + ms
        }

        return msByCategory
            .map { (category, ms) -> DailyUsage(date.toString(), category, (ms / 60_000).toInt()) }
            .filter { it.minutes > 0 }
    }

    // Returns a category for user-facing apps, or null for background system parts.
    private fun categoryOf(context: Context, pkg: String): String? {
        val pm = context.packageManager
        if (pm.getLaunchIntentForPackage(pkg) == null) return null
        knownApps[pkg]?.let { return it }

        val info = try {
            pm.getApplicationInfo(pkg, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            return null
        }
        return when (info.category) {
            ApplicationInfo.CATEGORY_SOCIAL -> "social"
            ApplicationInfo.CATEGORY_VIDEO -> "video"
            ApplicationInfo.CATEGORY_GAME -> "games"
            ApplicationInfo.CATEGORY_AUDIO -> "music_audio"
            ApplicationInfo.CATEGORY_NEWS -> "news"
            ApplicationInfo.CATEGORY_PRODUCTIVITY -> "productivity"
            ApplicationInfo.CATEGORY_MAPS -> "maps"
            ApplicationInfo.CATEGORY_IMAGE -> "photos"
            else -> "other"
        }
    }
}