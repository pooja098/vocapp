package com.example.vocapp_apk.data

import android.app.Application
import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import org.json.JSONArray

class AppState(application: Application) : AndroidViewModel(application) {
    private val preferences = application.getSharedPreferences("vocapp", 0)
    var matchSettings by mutableStateOf(loadSettings("match"))
        private set
    var writeSettings by mutableStateOf(loadSettings("write"))
        private set

    private fun loadSettings(mode: String) = PuzzleSettings(
        preferences.getInt("$mode.words", preferences.getInt("words", 10)).coerceAtLeast(1),
        preferences.getInt("$mode.timer", preferences.getInt("timer", 5)).coerceAtLeast(1),
        preferences.getInt("$mode.page-size", preferences.getInt("page-size", 7)).coerceAtLeast(1)
    )
    var performances by mutableStateOf(loadPerformances())
        private set
    var game by mutableStateOf<PuzzleGame?>(null)
        private set
    var page by mutableStateOf("home")
    private val wordStore = WordBankStore(application)
    var wordLists by mutableStateOf(wordStore.load())
        private set
    var wordError by mutableStateOf<String?>(null)
        private set
    private var recorded = false

    private fun saveWords(value: WordLists): Boolean = try {
        wordStore.save(value)
        wordLists = value
        wordError = null
        true
    } catch (error: Exception) {
        // Recover a committed move if writing either materialized file was interrupted.
        runCatching { wordStore.load() }.onSuccess { wordLists = it }
        wordError = "Could not save words. Please try again."
        false
    }

    fun addWord(german: String, english: String): Boolean {
        if (german.isBlank() || english.isBlank()) return false
        return saveWords(wordLists.copy(words = wordLists.words + WordPair(german.trim(), english.trim())))
    }

    fun archiveWords(selected: Set<Int>): Boolean = saveWords(WordLists(
        wordLists.words.filterIndexed { index, _ -> index !in selected },
        wordLists.archive + wordLists.words.filterIndexed { index, _ -> index in selected }
    ))

    fun unarchiveWords(selected: Set<Int>): Boolean = saveWords(WordLists(
        wordLists.words + wordLists.archive.filterIndexed { index, _ -> index in selected },
        wordLists.archive.filterIndexed { index, _ -> index !in selected }
    ))

    fun deleteArchivedWords(selected: Set<Int>): Boolean = saveWords(wordLists.copy(
        archive = wordLists.archive.filterIndexed { index, _ -> index !in selected }
    ))

    fun saveSettings(write: PuzzleSettings, match: PuzzleSettings) {
        writeSettings = write
        matchSettings = match
        preferences.edit()
            .putInt("write.words", write.words).putInt("write.timer", write.timer)
            .putInt("write.page-size", write.pageSize)
            .putInt("match.words", match.words).putInt("match.timer", match.timer)
            .putInt("match.page-size", match.pageSize).apply()
    }

    fun start(writing: Boolean = false) {
        if (wordLists.words.isEmpty()) return
        game = PuzzleGame(if (writing) writeSettings else matchSettings, wordLists.words, SystemClock.elapsedRealtime(), writing)
        recorded = false
        page = "puzzle"
    }

    fun recordResult() {
        val result = game?.result ?: return
        if (recorded) return
        recorded = true
        performances = (performances + result).sortedWith(
            compareByDescending<Performance> { it.score }.thenByDescending { it.matched }
                .thenBy { it.seconds }
        ).take(5)
        val rows = JSONArray()
        performances.forEach { rows.put(JSONArray().put(it.matched).put(it.seconds)) }
        preferences.edit().putString("performances", rows.toString()).apply()
    }

    private fun loadPerformances(): List<Performance> = runCatching {
        val rows = JSONArray(preferences.getString("performances", "[]"))
        List(rows.length()) { index ->
            val row = rows.getJSONArray(index)
            Performance(row.getInt(0), row.getDouble(1))
        }.sortedByDescending { it.score }.take(5)
    }.getOrDefault(emptyList())
}
