package com.example.vocapp_apk.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateMapOf

data class PuzzleSettings(val words: Int = 10, val timer: Int = 5, val pageSize: Int = 7)
data class Performance(val matched: Int, val seconds: Double) {
    val score: Double get() = matched / seconds.coerceAtLeast(0.001)
}
data class GameCard(val pairId: Int, val text: String)

class PuzzleGame(
    val settings: PuzzleSettings,
    private val bank: List<WordPair>,
    val startedAt: Long,
    val writing: Boolean = false
) {
    private var nextId = 0
    var matched by mutableIntStateOf(0)
        private set
    var result by mutableStateOf<Performance?>(null)
        private set
    var finishedAt: Long? = null
        private set
    var selectedLeft by mutableStateOf<Int?>(null)
        private set
    var selectedRight by mutableStateOf<Int?>(null)
        private set
    private var mismatchUntil: Long? = null
    val left = mutableStateListOf<GameCard?>()
    val right = mutableStateListOf<GameCard?>()
    val answers = mutableStateMapOf<Int, String>()
    val incorrect = mutableStateMapOf<Int, Boolean>()
    val durationMs = settings.timer.toLong() * 1000L

    init {
        require(bank.isNotEmpty())
        require(settings.words > 0 && settings.timer > 0 && settings.pageSize > 0)
        repeat(minOf(settings.pageSize, settings.words)) {
            val pair = nextPair()
            left.add(GameCard(nextId, pair.german))
            right.add(GameCard(nextId++, pair.english))
        }
        if (!writing) right.shuffle()
    }

    fun remainingMs(now: Long) = (durationMs - (now - startedAt)).coerceIn(0L, durationMs)

    private fun nextPair(): WordPair {
        val available = bank.filter { pair ->
            left.none { it?.text == pair.german } && right.none { it?.text == pair.english }
        }
        return (available.ifEmpty { bank }).random()
    }

    fun tick(now: Long) {
        if (result != null) return
        if (remainingMs(now) == 0L) {
            finish(startedAt + durationMs)
        } else if (mismatchUntil?.let { now >= it } == true) {
            selectedLeft = null
            selectedRight = null
            mismatchUntil = null
        }
    }

    fun select(isLeft: Boolean, index: Int, now: Long) {
        tick(now)
        if (writing || result != null || mismatchUntil != null) return
        if ((if (isLeft) left else right).getOrNull(index) == null) return
        if (isLeft) selectedLeft = index else selectedRight = index
        val l = selectedLeft ?: return
        val r = selectedRight ?: return
        if (left[l]?.pairId != right[r]?.pairId) {
            mismatchUntil = now + 350L
            return
        }
        matched++
        selectedLeft = null
        selectedRight = null
        if (nextId < settings.words) {
            val pair = nextPair()
            left[l] = GameCard(nextId, pair.german)
            right[r] = GameCard(nextId++, pair.english)
        } else {
            left[l] = null
            right[r] = null
        }
        if (matched == settings.words) finish(now)
    }

    fun editAnswer(pairId: Int, text: String) {
        if (result == null) answers[pairId] = text
    }

    fun submitAnswer(pairId: Int, now: Long) {
        tick(now)
        if (!writing || result != null) return
        val index = left.indexOfFirst { it?.pairId == pairId }
        if (index < 0) return
        val answer = answers[pairId].orEmpty().trim()
        if (answer.isEmpty()) return
        if (!answer.equals(left[index]!!.text.trim(), ignoreCase = true)) {
            incorrect[pairId] = true
            return
        }
        matched++
        left[index] = null
        right[index] = null
        answers.remove(pairId)
        incorrect.remove(pairId)
        if (matched == settings.words) {
            finish(now)
        } else if (left.all { it == null }) {
            // Keep solved rows empty until the next page is needed.
            repeat(minOf(left.size, settings.words - nextId)) { row ->
                val pair = nextPair()
                left[row] = GameCard(nextId, pair.german)
                right[row] = GameCard(nextId++, pair.english)
            }
        }
    }

    private fun finish(now: Long) {
        finishedAt = now
        result = Performance(matched, ((now - startedAt) / 1000.0).coerceAtLeast(0.001))
    }
}
