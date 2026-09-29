package com.maduhansa.wellbeingtracker.usage

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.maduhansa.wellbeingtracker.data.AppDatabase
import com.maduhansa.wellbeingtracker.data.UsageDao
import java.time.LocalDate

@Composable
fun UsageScreen(resumeCount: Int, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val dao = remember { AppDatabase.getDatabase(context).usageDao() }
    var hasAccess by remember { mutableStateOf(UsagePermission.hasUsageAccess(context)) }
    var isLoading by remember { mutableStateOf(false) }

    // Runs when the screen opens AND every time the user returns to the app
    // (e.g. after granting access in Settings).
    LaunchedEffect(resumeCount) {
        hasAccess = UsagePermission.hasUsageAccess(context)
        if (hasAccess) {
            isLoading = true
            UsageCollector.collectRecentDays(context, dao)
            isLoading = false
        }
    }

    if (hasAccess) {
        UsageSummary(
            dao = dao,
            isLoading = isLoading,
            onManageAccess = { UsagePermission.openSettings(context) },
            modifier = modifier
        )
    } else {
        ConsentScreen(onGrant = { UsagePermission.openSettings(context) }, modifier = modifier)
    }
}

@Composable
fun ConsentScreen(onGrant: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Screen-time tracking", style = MaterialTheme.typography.headlineSmall)
        Text("To explore how phone habits relate to mood, this app can read how long you use apps each day.")

        Text("What is collected", style = MaterialTheme.typography.titleSmall)
        Text("• Minutes per day in each app category (social, video, messaging…)")

        Text("What is NOT collected", style = MaterialTheme.typography.titleSmall)
        Text("• Messages, app content, browsing history, contacts, location or keystrokes")
        Text("• Names of individual apps are not saved")

        Text("Where it is stored", style = MaterialTheme.typography.titleSmall)
        Text("• Only on this phone. You can turn this off at any time.")

        Button(onClick = onGrant, modifier = Modifier.fillMaxWidth()) {
            Text("Grant usage access")
        }
        Text(
            "In Settings, find WellbeingTracker, turn the switch on, then come back.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun UsageSummary(
    dao: UsageDao,
    isLoading: Boolean,
    onManageAccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val today = remember { LocalDate.now().toString() }
    val todayFlow = remember { dao.getForDate(today) }
    val todayUsage by todayFlow.collectAsState(initial = emptyList())
    val totalsFlow = remember { dao.getDailyTotals() }
    val dailyTotals by totalsFlow.collectAsState(initial = emptyList())

    LazyColumn(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Column {
                Text("Today", style = MaterialTheme.typography.headlineSmall)
                Text(
                    "Total: ${formatMinutes(todayUsage.sumOf { it.minutes })}",
                    style = MaterialTheme.typography.titleMedium
                )
                if (isLoading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
        items(todayUsage, key = { it.category }) { usage ->
            LabelValueRow(usage.category, formatMinutes(usage.minutes))
        }
        item {
            Column {
                HorizontalDivider()
                Text("Last 7 days", style = MaterialTheme.typography.titleMedium)
            }
        }
        items(dailyTotals, key = { it.date }) { day ->
            LabelValueRow(day.date, formatMinutes(day.totalMinutes))
        }
        item {
            Column {
                HorizontalDivider()
                Text(
                    "Only category totals are stored, and only on this phone.",
                    style = MaterialTheme.typography.bodySmall
                )
                TextButton(onClick = onManageAccess) {
                    Text("Manage or revoke usage access")
                }
            }
        }
    }
}

@Composable
fun LabelValueRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label)
        Text(value)
    }
}

// 135 -> "2h 15m", 40 -> "40m"
fun formatMinutes(total: Int): String {
    val hours = total / 60
    val minutes = total % 60
    return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
}