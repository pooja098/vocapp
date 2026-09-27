package com.example.vocapp_apk.ui

import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.example.vocapp_apk.data.PuzzleGame
import kotlinx.coroutines.delay
import kotlin.math.ceil

@Composable
fun PuzzleScreen(game: PuzzleGame, onResult: () -> Unit, onHome: () -> Unit) {
    var now by remember(game) { mutableLongStateOf(SystemClock.elapsedRealtime()) }
    val currentOnResult by rememberUpdatedState(onResult)
    val currentOnHome by rememberUpdatedState(onHome)
    // A run continues against a monotonic clock, including time spent in the background.
    LaunchedEffect(game) {
        while (game.result == null) {
            now = SystemClock.elapsedRealtime()
            game.tick(now)
            if (game.result == null) delay(16)
        }
        currentOnResult()
        delay(1000)
        currentOnHome()
    }
    BackHandler { /* Finish the active run before returning home. */ }
    val focusManager = LocalFocusManager.current
    val result = game.result
    Column(Modifier.fillMaxSize().imePadding().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LinearProgressIndicator(
                progress = { game.remainingMs(now).toFloat() / game.durationMs },
                modifier = Modifier.weight(1f).height(12.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text("${ceil(game.remainingMs(now) / 1000.0).toInt()} s")
        }
        Text("${game.matched} / ${game.settings.words} words", style = MaterialTheme.typography.titleLarge)
        Text(if (game.writing) "Write the German word, then tap another cell" else "Tap the matching pairs")
        Row(Modifier.fillMaxWidth()) {
            Text("German", Modifier.weight(1f))
            Text("English", Modifier.weight(1f))
        }
        BoxWithConstraints(Modifier.weight(1f)) {
            val rowHeight = ((maxHeight - 8.dp * (game.left.size - 1)) / game.left.size)
                .coerceIn(48.dp, 100.dp)
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(game.left.indices.toList()) { index ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        listOf(true, false).forEach { isLeft ->
                            val card = (if (isLeft) game.left else game.right)[index]
                            val selected = (if (isLeft) game.selectedLeft else game.selectedRight) == index
                            if (card == null) {
                                Spacer(Modifier.weight(1f).height(rowHeight))
                            } else if (game.writing && isLeft) {
                                key(card.pairId) {
                                    var wasFocused by remember { mutableStateOf(false) }
                                    OutlinedTextField(
                                        value = game.answers[card.pairId].orEmpty(),
                                        onValueChange = { game.editAnswer(card.pairId, it) },
                                        enabled = result == null,
                                        singleLine = true,
                                        isError = game.incorrect[card.pairId] == true,
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            errorBorderColor = Color(0xFFE6A0A0),
                                            errorCursorColor = MaterialTheme.colorScheme.onSurface
                                        ),
                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                                        modifier = Modifier.weight(1f).height(rowHeight).onFocusChanged {
                                            if (wasFocused && !it.isFocused) {
                                                game.submitAnswer(card.pairId, SystemClock.elapsedRealtime())
                                            }
                                            wasFocused = it.isFocused
                                        }
                                    )
                                }
                            } else {
                                OutlinedButton(
                                    onClick = {
                                        now = SystemClock.elapsedRealtime()
                                        if (game.writing) focusManager.clearFocus()
                                        else game.select(isLeft, index, now)
                                    },
                                    enabled = result == null,
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(
                                        width = if (selected) 3.dp else 1.dp,
                                        color = if (selected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.outline
                                    ),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer
                                            else MaterialTheme.colorScheme.surface
                                    ),
                                    contentPadding = PaddingValues(4.dp),
                                    modifier = Modifier.weight(1f).height(rowHeight)
                                ) { Text(card.text) }
                            }
                        }
                    }
                }
            }
        }
    }
    if (result != null) {
        AlertDialog(
            onDismissRequest = {},
            confirmButton = {},
            title = { Text(if (result.matched == game.settings.words) "You win!" else "You lose!") },
            text = { Text("${result.matched} / ${game.settings.words} words matched") },
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        )
    }
}
