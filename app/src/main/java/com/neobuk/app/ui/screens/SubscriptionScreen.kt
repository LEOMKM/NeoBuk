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
import com.neobuk.app.ui.components.PlanSelectionList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(
    onBack: () -> Unit,
    viewModel: SubscriptionViewModel
) {
    val status by viewModel.status.collectAsState()
    val subscription by viewModel.subscription.collectAsState()
    
    var selectedPlan by remember(subscription) { mutableStateOf(subscription?.planType) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header removed (moved to Toolbar)


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

            // Upgrade/Change Options
            item {
                Text(
                    if (status == SubscriptionStatus.ACTIVE) "Change Plan" else "Choose a Plan", 
                    style = AppTextStyles.sectionTitle, 
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                )
                
                PlanSelectionList(
                    selectedPlan = selectedPlan,
                    onPlanSelected = { selectedPlan = it },
                    showTrial = status == SubscriptionStatus.LOCKED || status == SubscriptionStatus.GRACE_PERIOD,
                    currentPlan = subscription?.planType
                )

                if (selectedPlan != null && selectedPlan != subscription?.planType) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            when(selectedPlan) {
                                PlanType.MONTHLY -> viewModel.upgradeToActive()
                                PlanType.YEARLY -> viewModel.upgradeToYearly()
                                PlanType.FREE_TRIAL -> viewModel.simulateActiveTrial()
                                else -> {}
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeoBukTeal)
                    ) {
                        val buttonText = when(selectedPlan) {
                            PlanType.FREE_TRIAL -> "Start Free Trial"
                            PlanType.MONTHLY -> "Subscribe Monthly"
                            PlanType.YEARLY -> "Subscribe Yearly"
                            else -> "Confirm Upgrade"
                        }
                        Text(buttonText, style = AppTextStyles.buttonLarge)
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
