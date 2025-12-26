package com.neobuk.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neobuk.app.data.models.SubscriptionStatus
import com.neobuk.app.data.repositories.DayClosure
import com.neobuk.app.ui.components.TrialEndedModal
import com.neobuk.app.ui.components.TrialGracePeriodBanner
import com.neobuk.app.ui.theme.*
import com.neobuk.app.viewmodels.AuthViewModel
import com.neobuk.app.viewmodels.DayClosureViewModel
import org.koin.androidx.compose.koinViewModel
import java.time.format.DateTimeFormatter
import java.time.LocalDate

import com.neobuk.app.viewmodels.DashboardViewModel
import com.neobuk.app.viewmodels.TasksViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    subscriptionStatus: SubscriptionStatus = SubscriptionStatus.ACTIVE,
    onViewInventory: () -> Unit = {},
    onViewReports: () -> Unit = {},
    onViewSales: () -> Unit = {},
    onRecordSale: () -> Unit = {},
    onRecordExpense: () -> Unit = {},
    // onCloseDay removed from param as it's handled internally now via sheet, 
    // BUT we might still want to expose it if used for navigation. 
    // Let's keep the parameter structure but we'll intercept it.
    onCloseDay: () -> Unit = {}, 

    onSubscribeClick: () -> Unit = {},
    onShowNetProfitInfo: () -> Unit = {},
    onViewTasks: () -> Unit = {},
    
    // Injected ViewModels
    authViewModel: AuthViewModel = koinViewModel(),
    dayClosureViewModel: DayClosureViewModel = koinViewModel(),
    dashboardViewModel: DashboardViewModel = koinViewModel(),
    tasksViewModel: TasksViewModel = koinViewModel()
) {
    var showTrialEndedModal by remember { mutableStateOf(false) }
    var showCloseDayWarning by remember { mutableStateOf(false) }
    var showDayClosureSheet by remember { mutableStateOf(false) }
    
    // Auth & Business State
    val currentBusiness by authViewModel.currentBusiness.collectAsState()
    
    // Closure, Dashboard, Tasks State
    val isTodayClosed by dayClosureViewModel.isTodayClosed.collectAsState()
    val metrics by dashboardViewModel.metrics.collectAsState()
    val isLoadingMetrics by dashboardViewModel.isLoading.collectAsState()
    val pendingTasksCount by tasksViewModel.pendingTaskCount.collectAsState()
    
    // Initialize
    LaunchedEffect(currentBusiness) {
        currentBusiness?.let { 
            dayClosureViewModel.setBusinessId(it.id)
            dashboardViewModel.setBusinessId(it.id)
            // Tasks auto-update via repo observation, no explicit init needed beyond dependency
        }
    }
    
    // Refresh dashboard when screen is resumed/re-composed largely or manually triggered in future
    // For now simple init is fine. Can use Lifecycle event observer for resume.

    if (showTrialEndedModal) {
        TrialEndedModal(
            onDismiss = { showTrialEndedModal = false },
            onSubscribe = { 
                showTrialEndedModal = false 
                onSubscribeClick() 
            }
        )
    }
    
    // Warning Dialog for Pending Tasks
    if (showCloseDayWarning) {
        AlertDialog(
            onDismissRequest = { showCloseDayWarning = false },
            title = { Text("Close Day") },
            text = { Text("You still have $pendingTasksCount things to do today. Continue?") },
            confirmButton = {
                Button(
                    onClick = {
                        showCloseDayWarning = false
                        showDayClosureSheet = true // Proceed to sheet
                    }
                ) { Text("Continue") }
            },
            dismissButton = {
                TextButton(onClick = { showCloseDayWarning = false }) { Text("Cancel") }
            }
        )
    }
    
    // Day Closure Sheet
    if (showDayClosureSheet) {
        ModalBottomSheet(
            onDismissRequest = { showDayClosureSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            DayClosureSheetContent(
                onDismiss = { showDayClosureSheet = false },
                viewModel = dayClosureViewModel,
                onSuccess = { 
                    showDayClosureSheet = false 
                // Maybe show a success snackbar or burst confetti?
                }
            )
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp) 
    ) {
        // 0. Trial Grace Period Banner
        if (subscriptionStatus == SubscriptionStatus.GRACE_PERIOD) {
            item {
                TrialGracePeriodBanner(onSubscribeClick = onSubscribeClick)
            }
        }

        // 1. Hero Section 
        item {
            HeroSection(
                businessName = currentBusiness?.businessName ?: "My Shop",
                profitMargin = metrics?.netProfitMargin
            )
        }

        // 2. Quick Action Buttons
        item {
            QuickActionsRow(
                onRecordSale = {
                    if (subscriptionStatus == SubscriptionStatus.GRACE_PERIOD) showTrialEndedModal = true
                    else onRecordSale()
                },
                onRecordExpense = { 
                     if (subscriptionStatus == SubscriptionStatus.GRACE_PERIOD) showTrialEndedModal = true
                    else onRecordExpense()
                },
                isEnabled = subscriptionStatus != SubscriptionStatus.GRACE_PERIOD,
                onCloseDay = {
                    if (isTodayClosed == true) {
                        // Already closed feedback?
                    } else {
                        if (pendingTasksCount > 0) showCloseDayWarning = true
                        else showDayClosureSheet = true
                    }
                },
                isTodayClosed = isTodayClosed == true
            )
        }

        // 3. Key Metrics Row 
        item {
            val currencyFormatter = NumberFormat.getNumberInstance(Locale.US)
            
            MetricsRow(
                onViewSales = onViewSales,
                todaySales = metrics?.todaySales?.let { currencyFormatter.format(it) } ?: "0",
                todayExpenses = metrics?.todayExpenses?.let { currencyFormatter.format(it) } ?: "0",
                todayProfit = metrics?.todayProfit?.let { currencyFormatter.format(it) } ?: "0"
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Figures update automatically as you record sales and expenses.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(horizontal = 16.dp),
                fontSize = 11.sp
            )
        }

        // Net Profit & Things to do Row
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Net Profit
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Your net profit is ", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    val margin = metrics?.netProfitMargin?.let { "%.1f".format(it) } ?: "0"
                    val profit = metrics?.todayProfit?.let { NumberFormat.getNumberInstance(Locale.US).format(it) } ?: "0"
                    
                    Text("KES $profit ($margin%)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Outlined.Info, "Info", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), modifier = Modifier.size(16.dp).clickable { onShowNetProfitInfo() })
                }

                // Right: Things to do
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onViewTasks() }
                ) {
                    Text("Things to do", style = AppTextStyles.sectionTitle, color = MaterialTheme.colorScheme.primary)
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // 4. Weekly Performance Chart
        item {
            WeeklyPerformanceCard(onViewAll = onViewReports)
        }

        // 5. Inventory Status
        item {
            SectionHeader(title = "My Stock", actionText = "View All", onActionClick = onViewInventory)
            InventoryItem("Maize Flour", "KES 120 per kg", "Running Low", MaterialTheme.colorScheme.error, "5 units left", MaterialTheme.colorScheme.surfaceVariant)
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 16.dp))
            InventoryItem("Sugar", "KES 150 per kg", "Available", NeoBukSuccess, "32 units left", MaterialTheme.colorScheme.surfaceVariant)
        }
        
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
fun DayClosureSheetContent(
    onDismiss: () -> Unit,
    viewModel: DayClosureViewModel,
    onSuccess: () -> Unit
) {
    var cashInHand by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(bottom = 24.dp), // Check internal padding
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Filled.LockClock,
            null,
            tint = NeoBukTeal,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Funga Siku", style = AppTextStyles.pageTitle)
        Text(
            LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMM dd")),
            style = AppTextStyles.secondary,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            "Enter the total cash you have in hand right now. We will compare this with your recorded sales.",
            style = AppTextStyles.body,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        OutlinedTextField(
            value = cashInHand,
            onValueChange = { cashInHand = it },
            label = { Text("Cash In Hand (Actual)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            prefix = { Text("KES ") }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notes (Optional)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 4
        )
        
        if (error != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = {
                val cash = cashInHand.toDoubleOrNull()
                if (cash == null) {
                    error = "Please enter a valid amount"
                    return@Button
                }
                isSubmitting = true
                viewModel.performClosure(
                    cashActual = cash,
                    notes = notes,
                    onSuccess = {
                        isSubmitting = false
                        onSuccess()
                    },
                    onError = { msg ->
                        isSubmitting = false
                        error = msg
                    }
                )
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NeoBukTeal),
            enabled = !isSubmitting
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Confirm & Close Day")
            }
        }
    }
}

// ... Rest of the composables (HeroSection, QuickActionsRow, etc.) need to be preserved or updated slightly
// QuickActionsRow needs an update for "Closed" state styling

@Composable
fun HeroSection(
    businessName: String = "My Shop",
    profitMargin: Double? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
                .background(brush = Brush.verticalGradient(colors = listOf(NeoBukCyan.copy(alpha = 0.3f), NeoBukTeal.copy(alpha = 0.1f))))
        )
        
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text("Karibu, $businessName", style = AppTextStyles.pageTitle, color = NeoBukTeal)
            Text(
                LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMM dd, yyyy")),
                style = AppTextStyles.secondary,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = NeoBukTeal.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.surface), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Timeline, "AI", tint = NeoBukTeal, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("AI INSIGHT", style = MaterialTheme.typography.labelSmall, color = NeoBukTeal, fontWeight = FontWeight.Bold)
                        val text = if (profitMargin != null && profitMargin > 20) "Great job! Your margins are healthy (>20%)." 
                                   else if (profitMargin != null && profitMargin > 0) "Keep pushing! You are profitable today."
                                   else "Start recording sales to see insights."
                        Text(text, style = MaterialTheme.typography.bodyMedium, color = NeoBukTeal, fontWeight = FontWeight.Medium)
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
    isEnabled: Boolean = true,
    isTodayClosed: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).offset(y = (-12).dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onRecordSale,
            modifier = Modifier.weight(0.9f).height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = if (isEnabled && !isTodayClosed) NeoBukTeal else MaterialTheme.colorScheme.surfaceVariant),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
            contentPadding = PaddingValues(horizontal = 4.dp),
            enabled = isEnabled && !isTodayClosed
        ) {
            Icon(Icons.Outlined.ShoppingCart, null, modifier = Modifier.size(18.dp), tint = Color.White)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Sales", style = AppTextStyles.buttonMedium, color = Color.White, maxLines = 1)
        }

        Button(
            onClick = onRecordExpense,
            modifier = Modifier.weight(1.1f).height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = if (isEnabled && !isTodayClosed) NeoBukWarning else MaterialTheme.colorScheme.surfaceVariant),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
            contentPadding = PaddingValues(horizontal = 4.dp),
            enabled = isEnabled && !isTodayClosed
        ) {
            Icon(Icons.Outlined.Receipt, null, modifier = Modifier.size(18.dp), tint = Color.White)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Expenses", style = AppTextStyles.buttonMedium, color = Color.White, maxLines = 1)
        }
        
        Button(
            onClick = onCloseDay,
            modifier = Modifier.weight(1.3f).height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = if (isTodayClosed) NeoBukSuccess else Color(0xFF546E7A)),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
            enabled = isEnabled,
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            Icon(if(isTodayClosed) Icons.Filled.CheckCircle else Icons.Filled.LockClock, null, modifier = Modifier.size(18.dp), tint = Color.White)
            Spacer(modifier = Modifier.width(6.dp))
            Text(if(isTodayClosed) "Day Closed" else "Funga Siku", style = AppTextStyles.buttonMedium, color = Color.White, maxLines = 1)
        }
    }
}

@Composable
fun MetricsRow(
    onViewSales: () -> Unit = {},
    todaySales: String = "0",
    todayExpenses: String = "0",
    todayProfit: String = "0"
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MetricCard(
            modifier = Modifier.weight(1f),
            title = "Today's Sales",
            value = "KES",
            amount = todaySales, // Fetch real data in future
            icon = Icons.Filled.ShoppingBag,
            iconColor = NeoBukTeal,
            iconBg = NeoBukCyan.copy(alpha = 0.2f),
            index = 0,
            onClick = onViewSales
        )
        MetricCard(Modifier.weight(1f), "Expenses", "KES", todayExpenses, Icons.Outlined.AttachMoney, NeoBukWarning, NeoBukWarning.copy(alpha = 0.15f), 1)
        MetricCard(Modifier.weight(1f), "Profit", "KES", todayProfit, Icons.Filled.Timeline, NeoBukSuccess, NeoBukSuccess.copy(alpha = 0.15f), 2)
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
    index: Int = 0,
    onClick: (() -> Unit)? = null
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(index * 40L)
        visible = true
    }

    androidx.compose.animation.AnimatedVisibility(
        visible = visible,
        enter = androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(300)) + 
                androidx.compose.animation.slideInVertically(initialOffsetY = { 20 }, animationSpec = androidx.compose.animation.core.tween(300)),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(12.dp),
            onClick = { onClick?.invoke() },
            enabled = onClick != null
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(iconBg), contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = iconColor, modifier = Modifier.size(18.dp))
                }
                Column {
                    Text(title, style = AppTextStyles.caption, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(value, style = AppTextStyles.caption.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 2.dp, end = 2.dp))
                        Text(amount, style = AppTextStyles.amountSmall, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}

@Composable
fun WeeklyPerformanceCard(onViewAll: () -> Unit = {}) {
    var chartVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { chartVisible = true }
    
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 0.dp), 
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Weekly Performance", style = AppTextStyles.sectionTitle, color = MaterialTheme.colorScheme.onSurface)
                Text("View All", style = AppTextStyles.buttonMedium, color = NeoBukTeal, modifier = Modifier.clickable { onViewAll() })
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth().height(180.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                val heights = listOf(0.6f, 0.7f, 0.55f, 0.8f, 0.9f, 0.75f, 0.7f) // Mock for visual
                days.forEachIndexed { index, day ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom, modifier = Modifier.fillMaxHeight()) {
                        val animatedHeight by androidx.compose.animation.core.animateFloatAsState(
                            targetValue = if (chartVisible) heights[index] else 0f,
                            animationSpec = androidx.compose.animation.core.tween(300, index * 50, androidx.compose.animation.core.FastOutSlowInEasing),
                            label = "BarHeight"
                        )
                        Box(modifier = Modifier.width(24.dp).fillMaxHeight(animatedHeight).clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)).background(NeoBukTeal.copy(alpha = 0.8f)))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(day, style = AppTextStyles.caption, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
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
        Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(4.dp)).background(color))
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun SectionHeader(title: String, actionText: String, onActionClick: () -> Unit = {}) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = AppTextStyles.sectionTitle)
        Text(actionText, style = AppTextStyles.buttonMedium, color = NeoBukTeal, modifier = Modifier.clickable { onActionClick() })
    }
}

@Composable
fun InventoryItem(name: String, price: String, stockStatus: String, stockColor: Color, quantity: String, iconBg: Color) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).background(iconBg), contentAlignment = Alignment.Center) {
           Icon(Icons.Filled.ShoppingBag, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(name, style = AppTextStyles.bodyBold)
            Text(price, style = AppTextStyles.secondary, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(stockStatus, style = AppTextStyles.labelLarge, color = stockColor)
            Text(quantity, style = AppTextStyles.secondary, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
