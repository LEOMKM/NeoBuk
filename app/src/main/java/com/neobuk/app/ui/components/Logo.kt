package com.neobuk.app.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

@Composable
fun NeoBukLogoLarge() {
    Text(
        text = "NeoBuk",
        style = MaterialTheme.typography.displayMedium.copy(
            brush = Brush.linearGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.colorScheme.secondary
                )
            ),
            fontFamily = FontFamily.Cursive,
            fontWeight = FontWeight.Bold
        )
    )
}
