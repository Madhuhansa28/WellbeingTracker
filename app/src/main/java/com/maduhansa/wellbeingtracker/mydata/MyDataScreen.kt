package com.maduhansa.wellbeingtracker.mydata

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.maduhansa.wellbeingtracker.data.AppDatabase
import com.maduhansa.wellbeingtracker.data.DataExporter
import com.maduhansa.wellbeingtracker.data.ParticipantId
import com.maduhansa.wellbeingtracker.usage.UsagePermission
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun MyDataScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val scope = rememberCoroutineScope()

    var participantId by remember { mutableStateOf(ParticipantId.get(context)) }
    var includeNotes by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val checkInCountFlow = remember { db.moodDao().count() }
    val checkInCount by checkInCountFlow.collectAsState(initial = 0)
    val usageDaysFlow = remember { db.usageDao().countDays() }
    val usageDays by usageDaysFlow.collectAsState(initial = 0)

    // Opens Android's "Save as" screen; runs the export when the user picks a location.
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    DataExporter.exportZip(context, uri, includeNotes)
                    Toast.makeText(context, "Export saved", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("My data", style = MaterialTheme.typography.headlineSmall)

        // ---------- What is stored ----------
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Participant ID: $participantId", style = MaterialTheme.typography.titleMedium)
                Text(
                    "A random code used in exports instead of your name.",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(8.dp))
                Text("Stored on this phone:")
                Text("• $checkInCount mood check-ins")
                Text("• $usageDays days of screen-time data")
            }
        }

        // ---------- Export ----------
        Text("Export", style = MaterialTheme.typography.titleMedium)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Include notes")
                Text(
                    "Notes are free text and may contain personal details.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Switch(checked = includeNotes, onCheckedChange = { includeNotes = it })
        }
        Button(
            onClick = { exportLauncher.launch("wellbeing_${participantId}_${LocalDate.now()}.zip") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Export data (.zip)")
        }

        HorizontalDivider()

        // ---------- Delete & revoke ----------
        Text("Delete", style = MaterialTheme.typography.titleMedium)
        OutlinedButton(
            onClick = { showDeleteDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text("Delete all my data")
        }
        TextButton(onClick = { UsagePermission.openSettings(context) }) {
            Text("Revoke usage access")
        }
    }

    // ---------- Confirmation dialog ----------
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete all data?") },
            text = {
                Text(
                    "This permanently deletes all check-ins and screen-time data from this " +
                            "phone and gives you a new participant ID. This cannot be undone.\n\n" +
                            "Screen-time data will be collected again unless you also revoke usage access."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    scope.launch {
                        db.moodDao().deleteAll()
                        db.usageDao().deleteAll()
                        ParticipantId.reset(context)
                        participantId = ParticipantId.get(context)
                        Toast.makeText(context, "All data deleted", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}