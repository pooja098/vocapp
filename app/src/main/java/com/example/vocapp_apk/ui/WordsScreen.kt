package com.example.vocapp_apk.ui

import com.example.vocapp_apk.ui.theme.appButtonColors
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.example.vocapp_apk.data.AppState

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WordsScreen(state: AppState, archived: Boolean) {
    val words = if (archived) state.wordLists.archive else state.wordLists.words
    var selection by rememberSaveable { mutableStateOf(arrayListOf<Int>()) }
    var german by rememberSaveable { mutableStateOf("") }
    var english by rememberSaveable { mutableStateOf("") }
    val goBack = { state.page = if (archived) "words" else "home" }
    BackHandler(onBack = goBack)
    Column(Modifier.fillMaxSize().imePadding().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (archived) IconButton(onClick = goBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to words")
            }
            Text(if (archived) "Archive" else "Words", style = MaterialTheme.typography.headlineMedium)
        }
        state.wordError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Text(
            if (selection.isEmpty()) "Long press a row to select it" else "${selection.size} selected",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Column(Modifier.weight(1f).border(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
            Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)
                .background(MaterialTheme.colorScheme.surfaceVariant)) {
                RowNumberCell("#")
                VerticalDivider(Modifier.fillMaxHeight())
                Text("German", Modifier.weight(1f).padding(12.dp), style = MaterialTheme.typography.labelLarge)
                VerticalDivider(Modifier.fillMaxHeight())
                Text("English", Modifier.weight(1f).padding(12.dp), style = MaterialTheme.typography.labelLarge)
            }
            HorizontalDivider()
            LazyColumn(Modifier.weight(1f)) {
                itemsIndexed(words) { index, pair ->
                    val isSelected = index in selection
                    Row(
                        Modifier.fillMaxWidth().height(IntrinsicSize.Min)
                            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surface)
                            .semantics { selected = isSelected }
                            .combinedClickable(
                                onLongClickLabel = "Select row",
                                onLongClick = {
                                    if (!isSelected) selection = ArrayList(selection + index)
                                },
                                onClick = {
                                    if (selection.isNotEmpty()) {
                                        selection = ArrayList(if (isSelected) selection - index else selection + index)
                                    }
                                }
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RowNumberCell((index + 1).toString())
                        VerticalDivider(Modifier.fillMaxHeight())
                        val textColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurface
                        Text(pair.german, Modifier.weight(1f).padding(12.dp), color = textColor)
                        VerticalDivider(Modifier.fillMaxHeight())
                        Text(pair.english, Modifier.weight(1f).padding(12.dp), color = textColor)
                    }
                    HorizontalDivider()
                }
                if (!archived) item {
                    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                        verticalAlignment = Alignment.CenterVertically) {
                        RowNumberCell((words.size + 1).toString())
                        VerticalDivider(Modifier.fillMaxHeight())
                        WordInput(german, { german = it }, "Deutsch", Modifier.weight(1f))
                        VerticalDivider(Modifier.fillMaxHeight())
                        Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            WordInput(english, { english = it }, "English", Modifier.weight(1f))
                            TextButton(
                                onClick = {
                                    if (state.addWord(german, english)) {
                                        german = ""
                                        english = ""
                                    }
                                },
                                enabled = german.isNotBlank() && english.isNotBlank(),
                                modifier = Modifier.width(48.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) { Text("+") }
                        }
                    }
                    HorizontalDivider()
                }
                if (words.isEmpty()) item {
                    Text(if (archived) "No archived words." else "Enter both words above to add a pair.",
                        Modifier.padding(12.dp))
                }
            }
        }
        if (archived) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(colors = appButtonColors(), enabled = selection.isNotEmpty(), onClick = {
                    if (state.unarchiveWords(selection.toSet())) selection = arrayListOf()
                }) { Text("Unarchive") }
                Button(colors = appButtonColors(), enabled = selection.isNotEmpty(), onClick = {
                    if (state.deleteArchivedWords(selection.toSet())) selection = arrayListOf()
                }) { Text("Delete") }
            }
        } else {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(colors = appButtonColors(), enabled = selection.isNotEmpty(), modifier = Modifier.weight(1f), onClick = {
                    if (state.archiveWords(selection.toSet())) selection = arrayListOf()
                }) { Text("Archive") }
                Button(colors = appButtonColors(), onClick = { state.page = "archive" }, modifier = Modifier.weight(1f)) {
                    Text("View archive")
                }
            }
        }
    }
}

@Composable
private fun RowNumberCell(label: String) {
    Box(
        Modifier.width(40.dp).fillMaxHeight().background(Color(0xFF737982)).padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = Color.White, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun WordInput(value: String, onChange: (String) -> Unit, label: String, modifier: Modifier) {
    BasicTextField(
        value = value,
        onValueChange = onChange,
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
        modifier = modifier.padding(12.dp),
        decorationBox = { field ->
            Box {
                if (value.isEmpty()) Text(label, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f))
                field()
            }
        }
    )
}
