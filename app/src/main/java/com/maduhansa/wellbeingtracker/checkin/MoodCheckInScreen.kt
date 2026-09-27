package com.maduhansa.wellbeingtracker.checkin

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.maduhansa.wellbeingtracker.data.AppDatabase
import com.maduhansa.wellbeingtracker.data.MoodCheckIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun MoodCheckInScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val dao = remember { AppDatabase.getDatabase(context).moodDao() }
    val scope = rememberCoroutineScope()

    // Past check-ins from the database (updates automatically).
    val historyFlow = remember { dao.getAll() }
    val history by historyFlow.collectAsState(initial = emptyList())

    // Current slider values. "remember" keeps them while the screen is shown.
    var mood by remember { mutableFloatStateOf(4f) }
    var energy by remember { mutableFloatStateOf(4f) }
    var stress by remember { mutableFloatStateOf(4f) }
    var sleep by remember { mutableFloatStateOf(7f) }
    var note by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ---------- The check-in form ----------
        item {
            Text("How are you today?", style = MaterialTheme.typography.headlineSmall)
        }
        item { ScaleSlider("Mood", "very bad", "very good", mood) { mood = it } }
        item { ScaleSlider("Energy", "very low", "very high", energy) { energy = it } }
        item { ScaleSlider("Stress", "very low", "very high", stress) { stress = it } }
        item {
            Text("Sleep last night: $sleep hours")
            Slider(
                value = sleep,
                onValueChange = { sleep = (it * 2).roundToInt() / 2f }, // round to 0.5
                valueRange = 0f..12f,
                steps = 23 // allows half-hour steps
            )
        }
        item {
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note (optional)") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Button(
                onClick = {
                    scope.launch {
                        dao.insert(
                            MoodCheckIn(
                                timestamp = System.currentTimeMillis(),
                                mood = mood.roundToInt(),
                                energy = energy.roundToInt(),
                                stress = stress.roundToInt(),
                                sleepHours = sleep,
                                note = note.ifBlank { null }
                            )
                        )
                        note = ""
                        Toast.makeText(context, "Check-in saved", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save check-in")
            }
        }

        // ---------- History of past check-ins ----------
        item {
            HorizontalDivider()
            Text("Past check-ins", style = MaterialTheme.typography.titleMedium)
        }
        items(history, key = { it.id }) { checkIn ->
            HistoryRow(checkIn)
        }
    }
}

// A reusable 1–7 slider with a label, so we don't repeat the same code three times.
@Composable
fun ScaleSlider(
    label: String,
    lowText: String,
    highText: String,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    Column {
        Text("$label: ${value.roundToInt()} / 7")
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 1f..7f,
            steps = 5 // 1, 2, 3, 4, 5, 6, 7
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(lowText, style = MaterialTheme.typography.bodySmall)
            Text(highText, style = MaterialTheme.typography.bodySmall)
        }
    }
}

// One card in the history list.
@Composable
fun HistoryRow(checkIn: MoodCheckIn) {
    val dateText = remember(checkIn.timestamp) {
        SimpleDateFormat("EEE d MMM, HH:mm", Locale.getDefault()).format(Date(checkIn.timestamp))
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(dateText, style = MaterialTheme.typography.labelMedium)
            Text(
                "Mood ${checkIn.mood} · Energy ${checkIn.energy} · " +
                        "Stress ${checkIn.stress} · Sleep ${checkIn.sleepHours}h"
            )
            checkIn.note?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
        }
    }
}