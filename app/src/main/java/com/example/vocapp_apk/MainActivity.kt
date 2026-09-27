package com.example.vocapp_apk

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.vocapp_apk.ui.theme.appButtonColors
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.example.vocapp_apk.data.*
import com.example.vocapp_apk.ui.WordsScreen
import com.example.vocapp_apk.ui.PuzzleScreen
import com.example.vocapp_apk.ui.theme.VocappapkTheme
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val state = ViewModelProvider(this)[AppState::class.java]
        setContent { VocappapkTheme { MainActivityScreen(state) } }
    }
}

@Composable
fun MainActivityScreen(state: AppState) {
    Scaffold(bottomBar = {
        if (state.page != "puzzle") {
            NavigationBar {
                NavigationBarItem(selected = state.page == "home",
                    onClick = { state.page = "home" },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Home") })
                NavigationBarItem(selected = state.page == "words" || state.page == "archive",
                    onClick = { state.page = "words" },
                    icon = { Icon(Icons.Default.List, contentDescription = null) },
                    label = { Text("Words") })
                NavigationBarItem(selected = state.page == "settings",
                    onClick = { state.page = "settings" },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text("Settings") })
            }
        }
    }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (state.page) {
                "words", "archive" -> key(state.page) { WordsScreen(state, state.page == "archive") }
                "settings" -> SettingsScreen(state.writeSettings, state.matchSettings, state::saveSettings) { state.page = "home" }
                "puzzle" -> state.game?.let { game ->
                    PuzzleScreen(game, state::recordResult) { state.page = "home" }
                }
                else -> HomeScreen(state.matchPerformances, state.writePerformances, { state.start() }, { state.start(writing = true) }, state.wordLists.words.isNotEmpty())
            }
        }
    }
}

@Composable
fun HomeScreen(matchPerformances: List<Performance>, writePerformances: List<Performance>, onStart: () -> Unit, onWrite: () -> Unit, hasWords: Boolean) {
    BoxWithConstraints(Modifier.fillMaxSize().padding(20.dp)) {
        val availableHeight = maxHeight
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).heightIn(min = availableHeight),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(colors = appButtonColors(), onClick = onStart, enabled = hasWords, modifier = Modifier.fillMaxWidth()) { Text("Match") }
                Button(colors = appButtonColors(), onClick = onWrite, enabled = hasWords, modifier = Modifier.fillMaxWidth()) { Text("Write") }
                if (!hasWords) Text("Add or unarchive words in the Words tab to start a puzzle.")
            }
            Column(
                Modifier.fillMaxWidth().padding(top = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Top 5 wins · ranked by words per second", style = MaterialTheme.typography.bodySmall)
                PerformanceTable("Match", matchPerformances)
                PerformanceTable("Write", writePerformances)
            }
        }
    }
}

@Composable
private fun PerformanceTable(title: String, performances: List<Performance>) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        Row(Modifier.fillMaxWidth()) {
            Text("Words", Modifier.weight(1f))
            Text("Time", Modifier.weight(1f))
            Text("Words/s", Modifier.weight(1f))
        }
        HorizontalDivider()
        if (performances.isEmpty()) Text("Win a puzzle to record your first performance.")
        performances.forEach { result ->
            Row(Modifier.fillMaxWidth()) {
                Text("${result.matched}", Modifier.weight(1f))
                Text(String.format(Locale.getDefault(), "%.2f s", result.seconds), Modifier.weight(1f))
                Text(String.format(Locale.getDefault(), "%.2f", result.score), Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun SettingsScreen(
    writeSettings: PuzzleSettings,
    matchSettings: PuzzleSettings,
    onSave: (PuzzleSettings, PuzzleSettings) -> Unit,
    onBack: () -> Unit
) {
    var writeWords by rememberSaveable { mutableIntStateOf(writeSettings.words) }
    var writeTimer by rememberSaveable { mutableIntStateOf(writeSettings.timer) }
    var writePageSize by rememberSaveable { mutableIntStateOf(writeSettings.pageSize) }
    var matchWords by rememberSaveable { mutableIntStateOf(matchSettings.words) }
    var matchTimer by rememberSaveable { mutableIntStateOf(matchSettings.timer) }
    var matchPageSize by rememberSaveable { mutableIntStateOf(matchSettings.pageSize) }
    val latestWrite by rememberUpdatedState(PuzzleSettings(writeWords, writeTimer, writePageSize))
    val latestMatch by rememberUpdatedState(PuzzleSettings(matchWords, matchTimer, matchPageSize))
    val save by rememberUpdatedState(onSave)
    DisposableEffect(Unit) { onDispose { save(latestWrite, latestMatch) } }
    BackHandler(onBack = onBack)
    Column(Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Settings", style = MaterialTheme.typography.headlineMedium)
            Text("Write", style = MaterialTheme.typography.titleLarge)
            NumericSetting("words", writeWords) { writeWords = it }
            NumericSetting("timer (seconds)", writeTimer) { writeTimer = it }
            NumericSetting("page-size", writePageSize) { writePageSize = it }
            HorizontalDivider()
            Text("Match", style = MaterialTheme.typography.titleLarge)
            NumericSetting("words", matchWords) { matchWords = it }
            NumericSetting("timer (seconds)", matchTimer) { matchTimer = it }
            NumericSetting("page-size", matchPageSize) { matchPageSize = it }
        }
        Text(
            "Changes are auto saved upon exit.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun NumericSetting(label: String, value: Int, onChange: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f))
        TextButton(onClick = { onChange(value - 1) }, enabled = value > 1) { Text("−") }
        Text(value.toString())
        TextButton(onClick = { onChange(value + 1) }, enabled = value < Int.MAX_VALUE) { Text("+") }
    }
}
