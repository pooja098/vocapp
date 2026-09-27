package com.example.vocapp_apk.data

import android.content.Context
import android.util.AtomicFile
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

data class WordPair(val german: String, val english: String)
data class WordLists(val words: List<WordPair>, val archive: List<WordPair>)

internal fun parseWords(json: String): List<WordPair> {
    val rows = JSONArray(json)
    return List(rows.length()) { index ->
        val row = rows.getJSONArray(index)
        require(row.length() == 2) { "Word bank row $index must contain two words" }
        val german = row.get(0)
        val english = row.get(1)
        require(german is String && german.isNotBlank() &&
            english is String && english.isNotBlank()) {
            "Word bank row $index must contain nonblank strings"
        }
        WordPair(german, english)
    }
}

private fun encodeWords(words: List<WordPair>): JSONArray = JSONArray().apply {
    words.forEach { put(JSONArray().put(it.german).put(it.english)) }
}

// Assets seed app-private files. A durable journal makes moves between files recoverable.
class WordBankStore(private val context: Context) {
    private val journal = AtomicFile(File(context.filesDir, "word-bank-transaction.json"))

    fun load(): WordLists {
        recover()
        return WordLists(loadFile("words.json"), loadFile("archive.json"))
    }

    fun save(lists: WordLists) {
        val transaction = JSONObject().put("words", encodeWords(lists.words))
            .put("archive", encodeWords(lists.archive))
        write(journal, transaction.toString())
        recover()
    }

    private fun recover() {
        if (!journal.baseFile.exists()) return
        val transaction = JSONObject(String(journal.readFully(), Charsets.UTF_8))
        write(AtomicFile(File(context.filesDir, "words.json")), transaction.getJSONArray("words").toString())
        write(AtomicFile(File(context.filesDir, "archive.json")), transaction.getJSONArray("archive").toString())
        journal.delete()
    }

    private fun loadFile(name: String): List<WordPair> {
        val file = AtomicFile(File(context.filesDir, name))
        if (!file.baseFile.exists()) {
            val seed = context.assets.open(name).bufferedReader(Charsets.UTF_8).use { it.readText() }
            write(file, seed)
        }
        return parseWords(String(file.readFully(), Charsets.UTF_8))
    }

    private fun write(file: AtomicFile, text: String) {
        val stream = file.startWrite()
        try {
            stream.write(text.toByteArray(Charsets.UTF_8))
            file.finishWrite(stream)
        } catch (error: Exception) {
            file.failWrite(stream)
            throw error
        }
    }
}

fun loadWordBank(context: Context): List<WordPair> = WordBankStore(context).load().words
