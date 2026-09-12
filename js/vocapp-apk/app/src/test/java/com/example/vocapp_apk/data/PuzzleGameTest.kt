package com.example.vocapp_apk.data

import org.junit.Assert.*
import org.junit.Test

class PuzzleGameTest {
    private val bank = listOf(WordPair("Haus", "house"), WordPair("Katze", "cat"))

    private fun match(game: PuzzleGame, now: Long) {
        val l = game.left.indexOfFirst { it != null }
        val r = game.right.indexOfFirst { it?.pairId == game.left[l]?.pairId }
        game.select(true, l, now)
        game.select(false, r, now)
    }

    @Test fun defaultsMatchSettingsSpecification() {
        assertEquals(PuzzleSettings(10, 5, 7), PuzzleSettings())
    }

    @Test fun replacementsAllowMoreMatchesThanBankSizeAndFinishAtTarget() {
        val game = PuzzleGame(PuzzleSettings(30, 5, 7), bank, 1000L)
        assertEquals(7, game.left.size)
        repeat(30) { match(game, 1100L + it * 100L) }
        assertEquals(30, game.matched)
        assertEquals(3.0, game.result!!.seconds, 0.0001)
        assertTrue(game.left.all { it == null })
        assertTrue(game.right.all { it == null })
        game.tick(10000L)
        assertEquals(30, game.result!!.matched)
    }

    @Test fun replacementsStopOnceConfiguredTotalHasBeenLoaded() {
        val game = PuzzleGame(PuzzleSettings(10, 5, 7),
            List(50) { WordPair("German $it", "English $it") }, 1000L)
        repeat(3) { count ->
            val oldLeft = game.left[0]!!
            val rightIndex = game.right.indexOfFirst { it?.pairId == oldLeft.pairId }
            val oldRight = game.right[rightIndex]!!
            match(game, 1100L + count * 100L)
            assertNotEquals(oldLeft.text, game.left[0]!!.text)
            assertNotEquals(oldRight.text, game.right[rightIndex]!!.text)
            assertEquals(game.left[0]!!.pairId, game.right[rightIndex]!!.pairId)
            assertEquals(7, game.left.count { it != null })
            assertEquals(7, game.right.count { it != null })
            assertNull(game.result)
        }
        repeat(6) { count ->
            val leftIndex = game.left.indexOfFirst { it != null }
            val rightIndex = game.right.indexOfFirst {
                it?.pairId == game.left[leftIndex]!!.pairId
            }
            match(game, 1400L + count * 100L)
            assertNull(game.left[leftIndex])
            assertNull(game.right[rightIndex])
            assertEquals(6 - count, game.left.count { it != null })
            assertEquals(6 - count, game.right.count { it != null })
            assertNull(game.result)
        }
        match(game, 2100L)
        assertEquals(10, game.result!!.matched)
        assertTrue(game.left.all { it == null })
        assertTrue(game.right.all { it == null })
    }

    @Test fun matchAtDeadlineLosesAndDoesNotCount() {
        val game = PuzzleGame(PuzzleSettings(1, 5, 7), bank, 1000L)
        game.select(true, 0, 5999L)
        game.select(false, 0, 6000L)
        assertEquals(0, game.matched)
        assertEquals(5.0, game.result!!.seconds, 0.0001)
    }

    @Test fun earlyWinRecordsActualTime() {
        val game = PuzzleGame(PuzzleSettings(1, 5, 7), bank, 1000L)
        match(game, 2250L)
        assertEquals(1, game.result!!.matched)
        assertEquals(1.25, game.result!!.seconds, 0.0001)
    }

    @Test fun delayedTickRecordsConfiguredDuration() {
        val game = PuzzleGame(PuzzleSettings(), bank, 1000L)
        match(game, 2000L)
        game.tick(20000L)
        assertEquals(1, game.result!!.matched)
        assertEquals(5.0, game.result!!.seconds, 0.0001)
    }

    @Test fun wrongMatchClearsAndAllowsRetry() {
        val game = PuzzleGame(PuzzleSettings(2, 5, 2), bank, 1000L)
        val wrong = game.right.indexOfFirst { it?.pairId != game.left[0]?.pairId }
        game.select(true, 0, 1100L)
        game.select(false, wrong, 1100L)
        assertEquals(0, game.matched)
        game.tick(1450L)
        assertNull(game.selectedLeft)
        assertNull(game.selectedRight)
        match(game, 1500L)
        assertEquals(1, game.matched)
    }

    @Test fun writingChecksOnSubmissionAndAllowsCorrection() {
        val game = PuzzleGame(PuzzleSettings(2, 5, 2), bank, 1000L, writing = true)
        val card = game.left[0]!!
        assertEquals(card.pairId, game.right[0]!!.pairId)
        game.editAnswer(card.pairId, "wrong")
        assertNull(game.incorrect[card.pairId])
        game.submitAnswer(card.pairId, 1100L)
        assertEquals(true, game.incorrect[card.pairId])
        assertEquals(0, game.matched)
        game.editAnswer(card.pairId, "  ${card.text.uppercase()}  ")
        game.submitAnswer(card.pairId, 1200L)
        assertNull(game.left[0])
        assertNull(game.right[0])
        assertEquals(1, game.matched)
        game.submitAnswer(card.pairId, 1300L)
        assertEquals(1, game.matched)
    }

    @Test fun writingLoadsNextPageAndFinishesAtConfiguredTarget() {
        val game = PuzzleGame(PuzzleSettings(3, 5, 2), bank, 1000L, writing = true)
        repeat(3) { count ->
            val card = game.left.first { it != null }!!
            game.editAnswer(card.pairId, card.text)
            game.submitAnswer(card.pairId, 1100L + count * 100L)
        }
        assertEquals(3, game.result!!.matched)
        assertTrue(game.left.all { it == null })
        assertTrue(game.right.all { it == null })
    }

    @Test fun writingCannotScoreAfterDeadline() {
        val game = PuzzleGame(PuzzleSettings(1, 5, 1), bank, 1000L, writing = true)
        val card = game.left[0]!!
        game.editAnswer(card.pairId, card.text)
        game.submitAnswer(card.pairId, 6000L)
        assertEquals(0, game.result!!.matched)
    }

    @Test fun rankingUsesPairsPerSecond() {
        val fast = Performance(2, 0.5)
        val slow = Performance(10, 5.0)
        assertTrue(fast.score > slow.score)
    }
}
