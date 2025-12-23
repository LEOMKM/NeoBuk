package com.neobuk.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neobuk.app.data.models.PlanType
import com.neobuk.app.ui.theme.AppTextStyles
import com.neobuk.app.ui.theme.NeoBukTeal
import com.neobuk.app.ui.theme.NeoBukWarning
import com.neobuk.app.ui.theme.NeoBukSuccess

@Composable
fun PlanSelectionList(
    selectedPlan: PlanType?,
    onPlanSelected: (PlanType) -> Unit,
    showTrial: Boolean = true,
    currentPlan: PlanType? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (showTrial) {
            PlanCard(
                title = "Free Trial",
                description = "Try NeoBuk free for 30 days",
                price = "KES 0",
                helperText = "No card. No commitment.",
                features = listOf(
                    "Record sales and expenses",
                    "Manage products or services",
                    "See your daily performance"
                ),
                selected = selectedPlan == PlanType.FREE_TRIAL,
                onClick = { onPlanSelected(PlanType.FREE_TRIAL) },
                isCurrent = currentPlan == PlanType.FREE_TRIAL
            )
        }

        PlanCard(
            title = "Monthly Plan",
            description = "Pay as you go.",
            price = "KES 249",
            period = "/ month",
            helperText = "Charged monthly via M-Pesa or Paystack.",
            features = listOf(
                "Full access to all features",
                "Sales, expenses, products & services",
                "Reports and insights",
                "Cancel anytime"
            ),
            selected = selectedPlan == PlanType.MONTHLY,
            onClick = { onPlanSelected(PlanType.MONTHLY) },
            isCurrent = currentPlan == PlanType.MONTHLY
        )

        PlanCard(
            title = "Yearly Plan",
            description = "Save KES 498",
            price = "KES 2,490",
            period = "/ year",
            helperText = "Paid once per year.",
            features = listOf(
                "Best value if you use NeoBuk daily.",
                "Everything in Monthly",
                "Lower overall cost",
                "One payment for the whole year"
            ),
            selected = selectedPlan == PlanType.YEARLY,
            onClick = { onPlanSelected(PlanType.YEARLY) },
            isCurrent = currentPlan == PlanType.YEARLY,
            isRecommended = true,
            badge = "Best value"
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Micro-copy trust builder
        Text(
            text = "Your data is yours. Export anytime.",
            style = AppTextStyles.caption,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
fun PlanCard(
    title: String,
    description: String,
    price: String,
    period: String? = null,
    helperText: String,
    features: List<String>,
    selected: Boolean,
    onClick: () -> Unit,
    isCurrent: Boolean = false,
    badge: String? = null,
    isRecommended: Boolean = false
) {
    val borderColor = if (selected) NeoBukTeal else if (isRecommended) NeoBukTeal.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outlineVariant
    val backgroundColor = if (selected) NeoBukTeal.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(if (selected) 2.dp else 1.dp, borderColor),
        elevation = CardDefaults.cardElevation(if (selected) 4.dp else 0.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            if (badge != null) {
                Surface(
                    color = NeoBukSuccess,
                    shape = RoundedCornerShape(bottomStart = 12.dp),
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Text(
                        text = badge,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = AppTextStyles.caption.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                        color = Color.White // Badge text remains white for contrast
                    )
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    RadioButton(
                        selected = selected,
                        onClick = onClick,
                        colors = RadioButtonDefaults.colors(selectedColor = NeoBukTeal)
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(title, style = AppTextStyles.bodyBold)
                            if (isCurrent) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = NeoBukTeal.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        "CURRENT",
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                        style = AppTextStyles.caption.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                        color = NeoBukTeal
                                    )
                                }
                            }
                        }
                        Text(description, style = AppTextStyles.secondary, color = if (selected) NeoBukTeal else MaterialTheme.colorScheme.onSurfaceVariant)
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Features List
                        features.forEach { feature ->
                            Row(
                                modifier = Modifier.padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = if (selected) NeoBukTeal else NeoBukSuccess,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = feature,
                                    style = AppTextStyles.caption,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(price, style = AppTextStyles.pageTitle.copy(fontSize = 22.sp), color = NeoBukTeal)
                            if (period != null) {
                                Text(
                                    period,
                                    style = AppTextStyles.secondary,
                                    modifier = Modifier.padding(bottom = 2.dp, start = 4.dp)
                                )
                            }
                        }
                        
                        Text(
                            text = helperText,
                            style = AppTextStyles.caption.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (selected) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = NeoBukTeal,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}
