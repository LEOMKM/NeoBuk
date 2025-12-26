package com.neobuk.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.neobuk.app.ui.theme.NeoBukTeal

@Composable
fun EmptyState(
    title: String,
    description: String,
    imageId: Int,
    buttonText: String? = null,
    onButtonClick: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val isDark = androidx.compose.foundation.isSystemInDarkTheme()
        // Invert colors in dark mode for correct visibility of black-line illustrations
        val colorFilter = if (isDark) {
             androidx.compose.ui.graphics.ColorFilter.colorMatrix(
                 androidx.compose.ui.graphics.ColorMatrix(
                     floatArrayOf(
                         -1f, 0f, 0f, 0f, 255f,
                         0f, -1f, 0f, 0f, 255f,
                         0f, 0f, -1f, 0f, 255f,
                         0f, 0f, 0f, 1f, 0f
                     )
                 )
             )
        } else null

        Image(
            painter = painterResource(id = imageId),
            contentDescription = null,
            modifier = Modifier.size(200.dp),
            contentScale = ContentScale.Fit,
            colorFilter = colorFilter,
            alpha = if (isDark) 0.6f else 1.0f
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (buttonText != null && onButtonClick != null) {
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onButtonClick,
                colors = ButtonDefaults.buttonColors(containerColor = NeoBukTeal),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Text(buttonText, color = Color.White)
            }
        }
    }
}
