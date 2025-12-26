package com.neobuk.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.neobuk.app.ui.theme.AppTextStyles
import com.neobuk.app.ui.theme.NeoBukTeal

@Composable
fun MetricDetailSheet(
    title: String,
    description: String,
    formula: String,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(bottom = 32.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, style = AppTextStyles.sectionTitle, color = MaterialTheme.colorScheme.onSurface)
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, null)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = description,
            style = AppTextStyles.body,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(NeoBukTeal.copy(alpha = 0.1f))
                .padding(16.dp)
        ) {
            Column {
                Text(
                    "Calculation Formula:",
                    style = AppTextStyles.caption,
                    color = NeoBukTeal,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    formula,
                    style = AppTextStyles.bodyBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NeoBukTeal)
        ) {
            Text("Got it", style = AppTextStyles.buttonLarge)
        }
    }
}
