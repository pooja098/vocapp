package com.example.vocapp_apk.ui.theme

import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun appButtonColors() = ButtonDefaults.buttonColors(
    containerColor = Color(0xFF30343B),
    contentColor = Color.White,
    disabledContainerColor = Color.Black,
    disabledContentColor = Color(0xFF595D63)
)
