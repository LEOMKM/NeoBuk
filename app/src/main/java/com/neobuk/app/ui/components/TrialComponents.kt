package com.neobuk.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.neobuk.app.ui.theme.AppTextStyles
import com.neobuk.app.ui.theme.NeoBukTeal
import com.neobuk.app.ui.theme.Tokens

@Composable
fun TrialGracePeriodBanner(onSubscribeClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)), // Light orange
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Your trial has ended",
                style = AppTextStyles.bodyBold,
                color = Color(0xFFE65100) // Dark Orange
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Choose a plan to continue recording sales and expenses.",
                style = AppTextStyles.body,
                color = Color(0xFFE65100)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onSubscribeClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Subscribe Now", color = Color.White)
            }
        }
    }
}

@Composable
fun TrialEndedModal(
    onDismiss: () -> Unit,
    onSubscribe: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Trial Ended", style = AppTextStyles.pageTitle)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Your free trial has ended.\nSubscribe to continue running your business on NeoBuk.",
                    style = AppTextStyles.body,
                    color = Tokens.TextMuted,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onSubscribe,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeoBukTeal),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Choose Plan", style = AppTextStyles.buttonMedium)
                }
                Spacer(modifier = Modifier.height(12.dp))
                androidx.compose.material3.TextButton(onClick = onDismiss) {
                    Text("Maybe later", color = Tokens.TextMuted)
                }
            }
        }
    }
}
