package com.neobuk.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neobuk.app.data.models.PlanType
import com.neobuk.app.data.models.SubscriptionStatus
import com.neobuk.app.ui.theme.AppTextStyles
import com.neobuk.app.ui.theme.NeoBukTeal
import com.neobuk.app.ui.theme.Tokens
import com.neobuk.app.viewmodels.SubscriptionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(
    onBack: () -> Unit,
    viewModel: SubscriptionViewModel
) {
    val status by viewModel.status.collectAsState()
    val subscription by viewModel.subscription.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onSurface)
            }
            Text(
                "Subscription",
                style = AppTextStyles.pageTitle,
                modifier = Modifier.weight(1f)
            )
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status Card
            item {
                StatusCard(status, subscription?.planType)
            }

            // Plan Details
            item {
                Text("Current Plan", style = AppTextStyles.sectionTitle, modifier = Modifier.padding(bottom = 8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text(
                                if (subscription?.planType == PlanType.YEARLY) "Yearly Plan" 
                                else if (subscription?.planType == PlanType.MONTHLY) "Monthly Plan" 
                                else "Free Trial",
                                style = AppTextStyles.bodyBold
                            )
                            Text(
                                if (subscription?.planType == PlanType.FREE_TRIAL) "Free" 
                                else "KES ${subscription?.price?.toInt()}",
                                style = AppTextStyles.bodyBold,
                                color = NeoBukTeal
                            )
                        }
                        if (subscription?.planType == PlanType.FREE_TRIAL) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Trial ends in 23 days", // Mock
                                style = AppTextStyles.secondary,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Upgrade Options
            if (status != SubscriptionStatus.ACTIVE || (subscription?.planType == PlanType.MONTHLY)) {
                item {
                    Text(
                        if (status == SubscriptionStatus.ACTIVE) "Upgrade to Yearly" else "Choose a Plan", 
                        style = AppTextStyles.sectionTitle, 
                        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                    )
                    
                    val pushYearly = status == SubscriptionStatus.ACTIVE
                    
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Monthly Plan
                        if (subscription?.planType != PlanType.MONTHLY) {
                            PlanOptionCard(
                                title = "Monthly Plan",
                                price = "KES 249",
                                period = "/mo",
                                isRecommended = !pushYearly,
                                onClick = { viewModel.upgradeToActive() }
                            )
                        }

                        // Yearly Plan
                        PlanOptionCard(
                            title = "Yearly Plan",
                            price = "KES 2,490",
                            period = "/yr",
                            badge = "Save 2 months",
                            isRecommended = pushYearly,
                            onClick = { viewModel.upgradeToYearly() }
                        )
                    }
                }
            }
            
            // Dev Tools
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text("Developer Tools (Test States)", style = AppTextStyles.caption, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { viewModel.simulateGracePeriod() }, modifier = Modifier.weight(1f)) {
                        Text("Grace Period", fontSize = 10.sp) // sp needs import or property check
                    }
                    Button(onClick = { viewModel.simulateLocked() }, modifier = Modifier.weight(1f)) {
                        Text("Locked", fontSize = 10.sp)
                    }
                    Button(onClick = { viewModel.simulateActiveTrial() }, modifier = Modifier.weight(1f)) {
                         Text("Reset Trial", fontSize = 10.sp)
                    }
                    Button(onClick = { viewModel.upgradeToActive() }, modifier = Modifier.weight(1f)) {
                         Text("Simulate Active", fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun PlanOptionCard(
    title: String,
    price: String,
    period: String,
    badge: String? = null,
    isRecommended: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isRecommended) NeoBukTeal.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
        ),
        border = if (isRecommended) androidx.compose.foundation.BorderStroke(2.dp, NeoBukTeal) else null,
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            if (badge != null) {
                Surface(
                    color = Color(0xFFFACC15), // Yellow for attention
                    shape = RoundedCornerShape(bottomStart = 12.dp),
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Text(
                        text = badge,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = AppTextStyles.caption.copy(fontWeight = FontWeight.Bold),
                        color = Color.Black
                    )
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(title, style = AppTextStyles.bodyBold)
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(price, style = AppTextStyles.pageTitle.copy(fontSize = 24.sp), color = NeoBukTeal)
                            Text(period, style = AppTextStyles.secondary, modifier = Modifier.padding(bottom = 4.dp))
                        }
                    }
                    
                    Icon(
                        Icons.Default.CheckCircle, 
                        contentDescription = null, 
                        tint = if (isRecommended) NeoBukTeal else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )
                }
            }
        }
    }
}

@Composable
fun StatusCard(status: SubscriptionStatus, plan: PlanType?) {
    val (color, icon, text) = when (status) {
        SubscriptionStatus.TRIALING -> Triple(NeoBukTeal, Icons.Default.CheckCircle, "Trial Active")
        SubscriptionStatus.ACTIVE -> Triple(NeoBukTeal, Icons.Default.CheckCircle, "Active")
        SubscriptionStatus.GRACE_PERIOD -> Triple(MaterialTheme.colorScheme.error, Icons.Default.Warning, "Grace Period") // Orange
        SubscriptionStatus.LOCKED -> Triple(MaterialTheme.colorScheme.error, Icons.Default.Warning, "Locked")
        else -> Triple(MaterialTheme.colorScheme.onSurfaceVariant, Icons.Default.Warning, "Unknown")
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("Status", style = AppTextStyles.caption, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text, style = AppTextStyles.bodyBold, color = color)
            }
        }
    }
}
