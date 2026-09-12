package com.example.vocapp_apk.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vocapp_apk.ui.theme.*

data class WordCard(
    val id: Int,
    val text: String,
    val pairId: Int
)

@Composable
fun PuzzleScreen() {
    val initialWords = remember {
        listOf(
            WordCard(1, "excitement", 101),
            WordCard(2, "hongo", 104),
            WordCard(3, "cookbook", 102),
            WordCard(4, "recetario", 102),
            WordCard(5, "octopus", 103),
            WordCard(6, "pulpo", 103),
            WordCard(7, "mushroom", 104),
            WordCard(8, "hacer enojar", 105),
            WordCard(9, "make someone mad", 105),
            WordCard(10, "emoción", 101)
        ).shuffled()
    }

    var selectedCards by remember { mutableStateOf(setOf<WordCard>()) }
    var matchedPairs by remember { mutableStateOf(setOf<Int>()) }
    val progress = matchedPairs.size.toFloat() / 5f

    LaunchedEffect(selectedCards) {
        if (selectedCards.size == 2) {
            val list = selectedCards.toList()
            if (list[0].pairId == list[1].pairId) {
                matchedPairs = matchedPairs + list[0].pairId
                selectedCards = emptySet()
            } else {
                delay(1000)
                selectedCards = emptySet()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PuzzleBackground)
            .padding(16.dp)
            .statusBarsPadding()
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = Color.Gray.copy(alpha = 0.6f),
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            SegmentedProgressBar(
                progress = progress,
                modifier = Modifier
                    .weight(1.0f)
                    .height(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "10",
                color = Color.Gray,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Tap the matching pairs",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(initialWords) { word ->
                val isSelected = selectedCards.contains(word)
                val isMatched = matchedPairs.contains(word.pairId)

                PuzzleCard(
                    word = word,
                    isSelected = isSelected,
                    isMatched = isMatched,
                    onClick = {
                        if (!isMatched && selectedCards.size < 2 && !selectedCards.contains(word)) {
                            selectedCards = selectedCards + word
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun SegmentedProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    segments: Int = 4
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val centerY = height / 2

        // Background line
        drawLine(
            color = PuzzleProgressBackground,
            start = Offset(0f, centerY),
            end = Offset(width, centerY),
            strokeWidth = height,
            cap = StrokeCap.Round
        )

        // Progress line
        if (progress > 0) {
            drawLine(
                color = PuzzleProgressPrimary,
                start = Offset(0f, centerY),
                end = Offset(width * progress, centerY),
                strokeWidth = height,
                cap = StrokeCap.Round
            )
        }

        // Dots
        for (i in 0..segments) {
            val x = (width / segments) * i
            val isFilled = x <= width * progress
            drawCircle(
                color = if (isFilled) PuzzleProgressPrimary else PuzzleProgressBackground,
                radius = height / 3,
                center = Offset(x, centerY)
            )
            // Add a border to the dot to make it stand out
            drawCircle(
                color = PuzzleBackground,
                radius = height / 3,
                center = Offset(x, centerY),
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }
}

@Composable
fun PuzzleCard(
    word: WordCard,
    isSelected: Boolean,
    isMatched: Boolean,
    onClick: () -> Unit
) {
    val borderColor = when {
        isMatched -> Color.Transparent
        isSelected -> PuzzleProgressPrimary
        else -> PuzzleCardBorder
    }
    
    val backgroundColor = if (isMatched) Color.Transparent else PuzzleCardBackground
    val textColor = if (isMatched) Color.Gray else Color.White

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(backgroundColor, RoundedCornerShape(12.dp))
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(enabled = !isMatched) { onClick() }
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = word.text,
            color = textColor,
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PuzzleScreenPreview() {
    VocappapkTheme(darkTheme = true) {
        PuzzleScreen()
    }
}
