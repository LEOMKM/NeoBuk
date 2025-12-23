package com.neobuk.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neobuk.app.ui.theme.AppTextStyles
import com.neobuk.app.ui.theme.NeoBukTeal
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayEndClosureSheet(
    onDismiss: () -> Unit,
    onConfirmClosure: () -> Unit
) {
    var isConfirmed by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .padding(bottom = 32.dp) // Extra padding
    ) {
        if (!isConfirmed) {
            ClosureSummaryContent(onConfirm = { isConfirmed = true })
        } else {
            ClosureSuccessContent(onDismiss = {
                onConfirmClosure()
                onDismiss() // This dismisses the parent sheet provided by MainActivity
            })
        }
        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
fun ClosureSummaryContent(onConfirm: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Filled.LockClock,
            contentDescription = null,
            tint = NeoBukTeal,
            modifier = Modifier.size(48.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Close Today?",
            style = AppTextStyles.pageTitle,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "Ready to wrap up? Here is how you did today.",
            style = AppTextStyles.body,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Summary Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ClosureMetricCard(
                title = "Expenses",
                amount = "KES 3,200",
                color = Color(0xFFF59E0B),
                modifier = Modifier.weight(1f)
            )
            ClosureMetricCard(
                title = "Sales",
                amount = "KES 12,450",
                color = NeoBukTeal,
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.2f)), // Light version of tertiary
            shape = RoundedCornerShape(16.dp)
        ) {
             Column(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                 Text("Today's Profit", style = AppTextStyles.body, color = MaterialTheme.colorScheme.onSurfaceVariant)
                 Text("KES 9,250", style = AppTextStyles.amountLarge, color = MaterialTheme.colorScheme.tertiary)
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onConfirm,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NeoBukTeal)
        ) {
            Text("Confirm Day Closed", style = AppTextStyles.buttonLarge)
        }
    }
}

@Composable
fun ClosureSuccessContent(onDismiss: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        visible = true
        delay(2000) // Auto dismiss after 2 seconds
        onDismiss()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = scaleIn(animationSpec = tween(500, easing = LinearOutSlowInEasing)) + fadeIn()
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = NeoBukTeal,
                modifier = Modifier.size(80.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text("Day Closed!", style = AppTextStyles.pageTitle, textAlign = TextAlign.Center)
        Text("Great job today. See you tomorrow!", style = AppTextStyles.body, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun ClosureMetricCard(
    title: String,
    amount: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(16.dp)
    ) {
         Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
             Text(title, style = AppTextStyles.caption, color = MaterialTheme.colorScheme.onSurfaceVariant)
             Text(amount, style = AppTextStyles.bodyBold.copy(fontSize = 18.sp), color = color, fontWeight = FontWeight.Black)
        }
    }
}
