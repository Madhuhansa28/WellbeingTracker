package com.maduhansa.wellbeingtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import com.maduhansa.wellbeingtracker.checkin.MoodCheckInScreen
import com.maduhansa.wellbeingtracker.ui.theme.WellbeingTrackerTheme
import com.maduhansa.wellbeingtracker.usage.UsageScreen
import com.maduhansa.wellbeingtracker.mydata.MyDataScreen

class MainActivity : ComponentActivity() {

    // Increases by 1 every time the app comes back on screen.
    private var resumeCount by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WellbeingTrackerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        resumeCount = resumeCount,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        resumeCount++
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(resumeCount: Int, modifier: Modifier = Modifier) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val tabs = listOf("Check-in", "Screen time", "My data")

    Column(modifier = modifier.fillMaxSize()) {
        PrimaryTabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }
        when (selectedTab) {
            0 -> MoodCheckInScreen()
            1 -> UsageScreen(resumeCount = resumeCount)
            2 -> MyDataScreen()
        }
    }
}