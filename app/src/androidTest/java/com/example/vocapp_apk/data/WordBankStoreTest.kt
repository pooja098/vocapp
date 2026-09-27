package com.example.vocapp_apk.data

import android.content.Context
import android.content.ContextWrapper
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.util.UUID

class WordBankStoreTest {
    @Test fun changesSurviveReopeningAndDoNotReseedAnEmptyBank() {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val directory = File(base.cacheDir, "word-test-${UUID.randomUUID()}").apply { mkdirs() }
        val context = object : ContextWrapper(base) {
            override fun getFilesDir(): File = directory
        }
        try {
            val store = WordBankStore(context)
            val seed = store.load()
            assertEquals(50, seed.words.size)
            assertTrue(seed.archive.isEmpty())
            val added = WordPair("grün", "green")
            store.save(seed.copy(words = seed.words + added))
            assertEquals(added, WordBankStore(context).load().words.last())
            store.save(WordLists(emptyList(), seed.words + added))
            val archived = WordBankStore(context).load()
            assertTrue(archived.words.isEmpty())
            assertEquals(51, archived.archive.size)
            store.save(WordLists(listOf(added), seed.words))
            assertEquals(listOf(added), WordBankStore(context).load().words)
            store.save(WordLists(listOf(added), emptyList()))
            assertTrue(WordBankStore(context).load().archive.isEmpty())
        } finally {
            directory.deleteRecursively()
        }
    }

    @Test fun interruptedMoveIsRecovered() {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val directory = File(base.cacheDir, "word-test-${UUID.randomUUID()}").apply { mkdirs() }
        val context = object : ContextWrapper(base) {
            override fun getFilesDir(): File = directory
        }
        try {
            WordBankStore(context).load()
            File(directory, "word-bank-transaction.json").writeText(
                """{"words":[],"archive":[["Tür","door"]]}"""
            )
            val recovered = WordBankStore(context).load()
            assertTrue(recovered.words.isEmpty())
            assertEquals(listOf(WordPair("Tür", "door")), recovered.archive)
            assertFalse(File(directory, "word-bank-transaction.json").exists())
        } finally {
            directory.deleteRecursively()
        }
    }

    @Test fun invalidPairsAreRejected() {
        listOf("[[\"\",\"house\"]]", "[[\"Haus\",\"  \"]]", "[[\"Haus\"]]").forEach { json ->
            assertTrue(runCatching { parseWords(json) }.isFailure)
        }
    }
}
