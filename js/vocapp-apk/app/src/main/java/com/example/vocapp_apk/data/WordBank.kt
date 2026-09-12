package com.example.vocapp_apk.data

import android.content.Context
import org.json.JSONArray

data class WordPair(val german: String, val english: String)

fun loadWordBank(context: Context): List<WordPair> {
    val json = context.assets.open("words.json").bufferedReader(Charsets.UTF_8).use {
        it.readText()
    }
    val rows = JSONArray(json)
    require(rows.length() > 0) { "Word bank must contain at least one pair" }
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
