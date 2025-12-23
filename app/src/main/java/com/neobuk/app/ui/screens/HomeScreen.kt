package com.neobuk.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import com.neobuk.app.data.models.SubscriptionStatus
import com.neobuk.app.ui.components.TrialGracePeriodBanner
import com.neobuk.app.ui.components.TrialEndedModal
import com.neobuk.app.ui.theme.AppTextStyles
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.neobuk.app.ui.theme.NeoBukCyan
import com.neobuk.app.ui.theme.NeoBukTeal
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Receipt

import com.neobuk.app.ui.theme.NeoBukWarning
import com.neobuk.app.ui.theme.NeoBukSuccess
import androidx.compose.material.icons.filled.LockClock

@Composable
fun HomeScreen(
    subscriptionStatus: SubscriptionStatus = SubscriptionStatus.ACTIVE,
    onViewInventory: () -> Unit = {},
    onViewReports: () -> Unit = {},
    onViewSales: () -> Unit = {},
    onRecordSale: () -> Unit = {},
    onRecordExpense: () -> Unit = {},
    onCloseDay: () -> Unit = {},
    onSubscribeClick: () -> Unit = {}
) {
    var showTrialEndedModal by remember { mutableStateOf(false) }

    if (showTrialEndedModal) {
        TrialEndedModal(
            onDismiss = { showTrialEndedModal = false },
            onSubscribe = { 
                showTrialEndedModal = false 
                onSubscribeClick() 
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp) // Space for FAB/BottomBar
    ) {
        // 0. Trial Grace Period Banner
        if (subscriptionStatus == SubscriptionStatus.GRACE_PERIOD) {
            item {
                TrialGracePeriodBanner(onSubscribeClick = onSubscribeClick)
            }
        }

        // 1. Hero Section (Welcome + AI Insight)
        item {
            HeroSection()
        }

        // 2. Quick Action Buttons
        item {
            QuickActionsRow(
                onRecordSale = {
                    if (subscriptionStatus == SubscriptionStatus.GRACE_PERIOD) {
                        showTrialEndedModal = true
                    } else {
                        onRecordSale()
                    }
                },
                onRecordExpense = { 
                     if (subscriptionStatus == SubscriptionStatus.GRACE_PERIOD) {
                        showTrialEndedModal = true
                    } else {
                        onRecordExpense()
                    }
                },
                isEnabled = subscriptionStatus != SubscriptionStatus.GRACE_PERIOD,
                onCloseDay = onCloseDay
            )
        }

        // 3. Key Metrics Row (Sales, Expenses, Profit)
        item {
            MetricsRow(onViewSales = onViewSales)
        }

        // Spacer between KPIs and Weekly Performance
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 4. Weekly Performance Chart
        item {
            WeeklyPerformanceCard(onViewAll = onViewReports)
        }

        // 4. Inventory Status
        item {
            SectionHeader(
                title = "My Stock",
                actionText = "View All",
                onActionClick = onViewInventory
            )
            InventoryItem(
                name = "Maize Flour",
                price = "KES 120 per kg",
                stockStatus = "Running Low",
                stockColor = MaterialTheme.colorScheme.error,
                quantity = "5 units left",
                iconBg = MaterialTheme.colorScheme.surfaceVariant
            )
            HorizontalDivider(
                color = Color.LightGray.copy(alpha = 0.2f),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            InventoryItem(
                name = "Sugar",
                price = "KES 150 per kg",
                stockStatus = "Available",
                stockColor = NeoBukSuccess,
                quantity = "32 units left",
                iconBg = MaterialTheme.colorScheme.surfaceVariant
            )
        }
        
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun HeroSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp) // Reduced height for the banner
    ) {
        // Background Gradient/Pattern
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            NeoBukCyan.copy(alpha = 0.3f),
                            NeoBukTeal.copy(alpha = 0.1f)
                        )
                    )
                )
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Welcome Text
            Text(
                text = "Karibu, Mama Njeri's Shop",
                style = AppTextStyles.pageTitle,
                color = NeoBukTeal
            )
            Text(
                text = "Thursday, May 15, 2025", // Hardcoded date matching design
                style = AppTextStyles.secondary,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // AI Insight Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = NeoBukTeal.copy(alpha = 0.15f)
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Insight Icon
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Timeline,
                            contentDescription = "AI",
                            tint = NeoBukTeal,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column {
                        Text(
                            text = "AI INSIGHT",
                            style = MaterialTheme.typography.labelSmall,
                            color = NeoBukTeal,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Your sales are up 12% from last week!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = NeoBukTeal,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionsRow(
    onRecordSale: () -> Unit,
    onRecordExpense: () -> Unit,
    onCloseDay: () -> Unit,
    isEnabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .offset(y = (-12).dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Sales Button
        Button(
            onClick = onRecordSale,
            modifier = Modifier
                .weight(1f)
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isEnabled) NeoBukTeal else MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            Icon(
                Icons.Outlined.ShoppingCart,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                "Sales",
                style = AppTextStyles.buttonMedium,
                color = Color.White,
                maxLines = 1
            )
        }

        // Expenses Button
        Button(
            onClick = onRecordExpense,
            modifier = Modifier
                .weight(1.1f) // Give expenses slightly more weight as it is a longer word
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isEnabled) NeoBukWarning else MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            Icon(
                Icons.Outlined.Receipt,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                "Expenses",
                style = AppTextStyles.buttonMedium,
                color = Color.White,
                maxLines = 1
            )
        }
        
        // End Day Button (Ritual)
        Button(
            onClick = onCloseDay,
            modifier = Modifier
                .weight(0.9f) // Slightly less weight for 'Close'
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF546E7A) // Blue Gray
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
            enabled = isEnabled,
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            Icon(
                Icons.Filled.LockClock,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                "Funga Siku",
                style = AppTextStyles.buttonMedium,
                color = Color.White,
                maxLines = 1
            )
        }
    }
}

@Composable
fun MetricsRow(onViewSales: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MetricCard(
            modifier = Modifier.weight(1f),
            title = "Today's Sales",
            value = "KES",
            amount = "12,450",
            icon = Icons.Filled.ShoppingBag,
            iconColor = NeoBukTeal,
            iconBg = NeoBukCyan.copy(alpha = 0.2f),
            index = 0,
            onClick = onViewSales
        )
        MetricCard(
            modifier = Modifier.weight(1f),
            title = "Expenses",
            value = "KES",
            amount = "3,200",
            icon = Icons.Outlined.AttachMoney,
            iconColor = NeoBukWarning,
            iconBg = NeoBukWarning.copy(alpha = 0.15f),
            index = 1
        )
        MetricCard(
            modifier = Modifier.weight(1f),
            title = "Profit",
            value = "KES",
            amount = "9,250",
            icon = Icons.Filled.Timeline,
            iconColor = NeoBukSuccess,
            iconBg = NeoBukSuccess.copy(alpha = 0.15f),
            index = 2
        )
    }
}

@Composable
fun MetricCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    amount: String,
    icon: ImageVector,
    iconColor: Color,
    iconBg: Color,
    index: Int = 0, // For stagger delay
    onClick: (() -> Unit)? = null
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(index * 40L) // Stagger 40ms
        visible = true
    }

    androidx.compose.animation.AnimatedVisibility(
        visible = visible,
        enter = androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(300)) + 
                androidx.compose.animation.slideInVertically(initialOffsetY = { 20 }, animationSpec = androidx.compose.animation.core.tween(300)),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(), // Applied internally to ensure fill width within AnimatedVisibility
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(12.dp),
            onClick = { onClick?.invoke() },
            enabled = onClick != null
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                Column {
                    Text(
                        text = title,
                        style = AppTextStyles.caption,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                    Text(
                        text = value,
                        style = AppTextStyles.caption,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = amount,
                        style = AppTextStyles.amountSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun WeeklyPerformanceCard(onViewAll: () -> Unit = {}) {
    var chartVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        chartVisible = true
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 0.dp), 
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Weekly Performance",
                    style = AppTextStyles.sectionTitle,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "View All",
                    style = AppTextStyles.buttonMedium,
                    color = NeoBukTeal,
                    modifier = Modifier.clickable { onViewAll() }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Simplified Chart Representation using Row of Bars
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                val heights = listOf(0.6f, 0.7f, 0.55f, 0.8f, 0.9f, 0.75f, 0.7f)
                
                days.forEachIndexed { index, day ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        // Bar Growth Animation
                        val animatedHeight by androidx.compose.animation.core.animateFloatAsState(
                            targetValue = if (chartVisible) heights[index] else 0f,
                            animationSpec = androidx.compose.animation.core.tween(
                                durationMillis = 300,
                                delayMillis = index * 50, // Staggered growth
                                easing = androidx.compose.animation.core.FastOutSlowInEasing
                            ),
                            label = "BarHeightAnimation"
                        )

                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .fillMaxHeight(animatedHeight)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(NeoBukTeal.copy(alpha = 0.8f))
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = day,
                            style = AppTextStyles.caption,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                LegendItem("Sales", NeoBukTeal)
                Spacer(modifier = Modifier.width(16.dp))
                LegendItem("Profit", NeoBukWarning)
            }
        }
    }
}

@Composable
fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    actionText: String,
    onActionClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = AppTextStyles.sectionTitle
        )
        Text(
            text = actionText,
            style = AppTextStyles.buttonMedium,
            color = NeoBukTeal,
            modifier = Modifier.clickable { onActionClick() }
        )
    }
}

@Composable
fun InventoryItem(
    name: String,
    price: String,
    stockStatus: String,
    stockColor: Color,
    quantity: String,
    iconBg: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Product Image Placeholder
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
           // Icon placeholder if no image
           Icon(Icons.Filled.ShoppingBag, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = AppTextStyles.bodyBold
            )
            Text(
                text = price,
                style = AppTextStyles.secondary,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = stockStatus,
                style = AppTextStyles.labelLarge, // or caption
                color = stockColor
            )
            Text(
                text = quantity,
                style = AppTextStyles.secondary,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
