package com.maduhansa.wellbeingtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.maduhansa.wellbeingtracker.checkin.MoodCheckInScreen
import com.maduhansa.wellbeingtracker.mydata.MyDataScreen
import com.maduhansa.wellbeingtracker.ui.theme.WellbeingTrackerTheme
import com.maduhansa.wellbeingtracker.usage.UsageScreen

class MainActivity : ComponentActivity() {

    // Increases by 1 every time the app comes back on screen.
    private var resumeCount by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WellbeingTrackerTheme {
                MainScreen(resumeCount = resumeCount)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        resumeCount++
    }
}

// One entry in the bottom navigation bar.
private data class NavItem(val label: String, val icon: Int)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(resumeCount: Int) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val navItems = listOf(
        NavItem("Check-in", R.drawable.ic_tab_checkin),
        NavItem("Screen time", R.drawable.ic_tab_screen_time),
        NavItem("My data", R.drawable.ic_tab_my_data)
    )

    Scaffold(
        // Top bar with the app name
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Wellbeing Tracker") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        // Bottom navigation with icons + labels
        bottomBar = {
            NavigationBar {
                navItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(painterResource(item.icon), contentDescription = null) },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        // innerPadding keeps content clear of the top and bottom bars
        val contentModifier = Modifier.padding(innerPadding)
        when (selectedTab) {
            0 -> MoodCheckInScreen(modifier = contentModifier)
            1 -> UsageScreen(resumeCount = resumeCount, modifier = contentModifier)
            2 -> MyDataScreen(modifier = contentModifier)
        }
    }
}