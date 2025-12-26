package com.neobuk.app.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neobuk.app.ui.theme.NeoBukTeal
import com.neobuk.app.ui.theme.NeoBukCyan
import com.neobuk.app.ui.theme.AppTextStyles
import com.neobuk.app.ui.screens.sales.NewSaleScreen
import com.neobuk.app.ui.screens.receipt.ReceiptScreen
import com.neobuk.app.ui.screens.receipt.ReceiptData
import com.neobuk.app.ui.screens.receipt.ReceiptItem
import com.neobuk.app.ui.screens.receipt.ReceiptUtils
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.koin.androidx.compose.koinViewModel
import com.neobuk.app.viewmodels.SalesViewModel
import com.neobuk.app.viewmodels.ProductsViewModel
import com.neobuk.app.viewmodels.AuthViewModel
import com.neobuk.app.data.repositories.Sale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesHistoryScreen(
    salesViewModel: SalesViewModel = koinViewModel(),
    productsViewModel: ProductsViewModel = koinViewModel(),
    authViewModel: AuthViewModel = koinViewModel()
) {
    var showDetailSheet by remember { mutableStateOf(false) }
    var showNewSaleSheet by remember { mutableStateOf(false) }
    var showReceiptView by remember { mutableStateOf(false) }
    var selectedSale by remember { mutableStateOf<Sale?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Observe Business State
    val currentBusiness by authViewModel.currentBusiness.collectAsState()

    // Observe Sales State
    val sales by salesViewModel.sales.collectAsState()
    val todaySales by salesViewModel.todaySales.collectAsState()
    val isLoading by salesViewModel.isLoading.collectAsState()

    // Initialize data when business is available
    LaunchedEffect(currentBusiness) {
        currentBusiness?.let { business ->
            salesViewModel.setBusinessId(business.id)
            productsViewModel.setBusinessId(business.id)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // 1. Header Section
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                // Prominent New Sale Button
                Button(
                    onClick = { showNewSaleSheet = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeoBukTeal
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Icon(
                        Icons.Default.ShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Record New Sale",
                        style = AppTextStyles.buttonLarge,
                        color = Color.White
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Filter Chips
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChipItem("Today", selected = true)
                    FilterChipItem("This Week", selected = false)
                    FilterChipItem("This Month", selected = false)
                    FilterChipItem("Custom", selected = false)
                }
            }
        }

        // 2. Summary Card with Chart
        item {
            SummaryCard(salesViewModel)
        }

        // 3. Transactions Header & Search
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sales List",
                        style = AppTextStyles.sectionTitle
                    )
                    TextButton(onClick = { }) {
                        Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Filter")
                    }
                }
                
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    placeholder = { Text("Search sales...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                )
            }
        }

        // 4. Transaction List
        if (isLoading && sales.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = NeoBukTeal)
                }
            }
        } else if (sales.isEmpty()) {
             item {
                 com.neobuk.app.ui.components.EmptyState(
                     title = "No Sales Yet",
                     description = "Your sales history is empty. Start recording transactions to see them here.",
                     imageId = com.neobuk.app.R.drawable.empty_sales,
                     buttonText = "Record First Sale",
                     onButtonClick = { showNewSaleSheet = true }
                 )
            }
        } else {
            items(sales) { sale ->
                TransactionItem(
                    sale = sale,
                    onClick = {
                        selectedSale = sale
                        showDetailSheet = true
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    if (showDetailSheet && selectedSale != null) {
        ModalBottomSheet(
            onDismissRequest = { showDetailSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            SalesDetailSheet(
                sale = selectedSale!!,
                onClose = { showDetailSheet = false },
                onViewReceipt = {
                    showDetailSheet = false // Close sheet
                    showReceiptView = true // Open full screen receipt
                }
            )
        }
    }
    
    // New Sale Full Screen
    if (showNewSaleSheet) {
        ModalBottomSheet(
            onDismissRequest = { showNewSaleSheet = false },
            sheetState = sheetState,
            containerColor = Color.White,
            modifier = Modifier.fillMaxHeight(0.95f)
        ) {
            NewSaleScreen(
                salesViewModel = salesViewModel,
                productsViewModel = productsViewModel,
                onDismiss = { showNewSaleSheet = false },
                onCompleteSale = {
                    showNewSaleSheet = false
                    // Optional: Show success message via Snackbar
                }
            )
        }
    }

    if (showReceiptView && selectedSale != null) {
        // Map Sale to ReceiptData
        val sale = selectedSale!!
        val receiptItems = sale.items.map { 
             ReceiptItem(
                 name = it.displayName, 
                 quantity = it.quantity.toInt(),
                 price = String.format("%.0f", it.unitPrice),
                 total = String.format("%.0f", it.totalPrice)
             )
        }
        
        val receiptData = ReceiptData(
            businessName = "My Business", // Should be fetched from BusinessRepo
            receiptId = sale.saleNumber,
            date = formatDateTime(sale.saleDate),
            paymentMethod = sale.paymentMethod,
            customerName = sale.customerName ?: "Walk-in Customer",
            totalAmount = "KES ${String.format("%,.0f", sale.totalAmount)}",
            items = receiptItems,
            paymentRef = sale.paymentReference
        )

        Dialog(
            onDismissRequest = { showReceiptView = false },
            properties = DialogProperties(usePlatformDefaultWidth = false) // Full screen
        ) {
            ReceiptScreen(
                data = receiptData,
                onBack = { showReceiptView = false }
            )
        }
    }
}

// Helper for date formatting
fun formatDateTime(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("MMM dd, yyyy • HH:mm", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}

@Composable
fun SalesDetailSheet(
    sale: Sale, 
    onClose: () -> Unit,
    onViewReceipt: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Sale Details",
                style = AppTextStyles.pageTitle
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }
        
        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 16.dp))
        
        // Transaction ID & Status
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Sale ID", style = AppTextStyles.caption, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("#${sale.saleNumber}", style = AppTextStyles.bodyBold)
            }
            Box(
                modifier = Modifier
                    .background(Color(0xFFE8F5E9), RoundedCornerShape(16.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(sale.paymentStatus.name, style = AppTextStyles.caption, color = Color(0xFF2E7D32))
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Date & Method
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Date & Time", style = AppTextStyles.caption, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(formatDateTime(sale.saleDate), style = AppTextStyles.body)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Payment Method", style = AppTextStyles.caption, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(sale.paymentMethod, style = AppTextStyles.body)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Customer
        Text("Customer", style = AppTextStyles.caption, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Person, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(sale.customerName ?: "Walk-in Customer", style = AppTextStyles.bodyBold)
                if (sale.customerPhone != null) {
                    Text(sale.customerPhone, style = AppTextStyles.caption, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Text("Items", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        
        // Items List
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            sale.items.forEach { item ->
                ItemRow(
                    name = item.displayName, 
                    calc = "${item.quantity.toInt()} × KES ${String.format("%,.0f", item.unitPrice)}", 
                    price = "KES ${String.format("%,.0f", item.totalPrice)}"
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray.copy(alpha = 0.3f))
            
            // Totals
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Subtotal", color = Color.Gray)
                Text("KES ${String.format("%,.0f", sale.subtotal)}", fontWeight = FontWeight.Bold)
            }
            if (sale.discountAmount > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Discount", color = Color.Red)
                    Text("- KES ${String.format("%,.0f", sale.discountAmount)}", fontWeight = FontWeight.Bold, color = Color.Red)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total", style = AppTextStyles.amountLarge)
                Text("KES ${String.format("%,.0f", sale.totalAmount)}", style = AppTextStyles.amountLarge)
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Actions
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = onViewReceipt,
                modifier = Modifier.weight(1f).height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Icon(Icons.Default.Print, null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("View Receipt", color = MaterialTheme.colorScheme.onSurface)
            }
            
            val context = LocalContext.current
            Button(
                onClick = {
                     // Quick Share Text
                     ReceiptUtils.shareText(context, "Receipt for KES ${sale.totalAmount} from My Business. Paid via ${sale.paymentMethod}.")
                },
                modifier = Modifier.weight(1f).height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeoBukCyan)
            ) {
                Icon(Icons.Default.Email, null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Quick Share", color = Color.White)
            }
        }
    }
}

@Composable
fun ItemRow(name: String, calc: String, price: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(4.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
               // Placeholder for item image
               Icon(Icons.Default.ShoppingBag, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(name, style = AppTextStyles.bodyBold)
                Text(calc, style = AppTextStyles.caption, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Text(price, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun FilterChipItem(text: String, selected: Boolean) {
    if (selected) {
        Button(
            onClick = {},
            colors = ButtonDefaults.buttonColors(containerColor = NeoBukCyan),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
            modifier = Modifier.height(32.dp)
        ) {
            Text(text, color = Color.White, style = MaterialTheme.typography.labelMedium)
        }
    } else {
        FilledTonalButton(
            onClick = {},
            colors = ButtonDefaults.filledTonalButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
            modifier = Modifier.height(32.dp)
        ) {
            Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant, style = AppTextStyles.body)
        }
    }
}

@Composable
fun SummaryCard(salesViewModel: SalesViewModel) {
    var chartVisible by remember { mutableStateOf(false) }
    
    val todaySalesTotal by remember(salesViewModel) { 
        derivedStateOf { salesViewModel.getTodayTotalSales() } 
    }
    val todaySalesCount by remember(salesViewModel) {
        derivedStateOf { salesViewModel.getTodaySalesCount() }
    }
    
    LaunchedEffect(Unit) {
        chartVisible = true
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "KES ${String.format("%,.0f", todaySalesTotal)}",
                style = AppTextStyles.amountLarge
            )
            Text(
                text = "Total Sales Today",
                style = AppTextStyles.body,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Mini Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val avg = if (todaySalesCount > 0) todaySalesTotal / todaySalesCount else 0.0
                MiniStatItem(Modifier.weight(1f), "KES ${String.format("%,.0f", avg)}", "Avg. Sale")
                MiniStatItem(Modifier.weight(1f), "$todaySalesCount", "Sales Count")
                MiniStatItem(Modifier.weight(1f), "N/A", "Top Product") // Placeholder
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Chart Area (Visual Clone - Mock Data for now)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Y-Axis labels (Left side)
                Column(
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxHeight().padding(bottom = 24.dp)
                ) {
                    Text("4 K", style = AppTextStyles.caption, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("3 K", style = AppTextStyles.caption, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("2 K", style = AppTextStyles.caption, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("1 K", style = AppTextStyles.caption, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("0 K", style = AppTextStyles.caption, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                
                // Bars
                ChartBar(0.3f, "8AM", chartVisible, 0)
                ChartBar(0.6f, "", chartVisible, 1)
                ChartBar(0.9f, "12PM", chartVisible, 2)
                ChartBar(0.5f, "", chartVisible, 3)
                ChartBar(0.45f, "4PM", chartVisible, 4)
                ChartBar(0.25f, "", chartVisible, 5)
                ChartBar(0.15f, "8PM", chartVisible, 6)
            }
        }
    }
}

@Composable
fun MiniStatItem(modifier: Modifier, value: String, label: String) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Text(value, style = AppTextStyles.bodyBold)
        Text(label, style = AppTextStyles.caption, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun ChartBar(height: Float, label: String, isVisible: Boolean = true, index: Int = 0) {
    val animatedHeight by animateFloatAsState(
        targetValue = if (isVisible) height else 0f,
        animationSpec = androidx.compose.animation.core.tween(
            durationMillis = 600,
            delayMillis = index * 100,
            easing = androidx.compose.animation.core.FastOutSlowInEasing
        ),
        label = "BarHeightAnimation"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
        modifier = Modifier.fillMaxHeight()
    ) {
        Box(
            modifier = Modifier
                .width(20.dp)
                .fillMaxHeight(animatedHeight)
                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                .background(NeoBukCyan)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = AppTextStyles.caption,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.height(16.dp)
        )
    }
}

@Composable
fun TransactionItem(
    sale: Sale,
    onClick: () -> Unit
) {
    // Determine colors
    val (paymentBg, paymentText) = when(sale.paymentMethod.uppercase()) {
        "M-PESA" -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        "CASH" -> Color(0xFFE3F2FD) to Color(0xFF1565C0)
        else -> Color(0xFFF3E5F5) to Color(0xFF7B1FA2)
    }
    
    // Determine title (e.g., "Multiple Items (5)" or "Item Name")
    val title = if (sale.itemCount > 1) {
        "Multiple Items (${sale.itemCount})"
    } else {
        sale.items.firstOrNull()?.displayName ?: "Sale"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Row: Title + Pill + Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left side: Title + Pill
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title.take(18) + if(title.length > 18) "..." else "",
                        style = AppTextStyles.bodyBold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(paymentBg, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = sale.paymentMethod,
                            style = MaterialTheme.typography.labelSmall,
                            color = paymentText
                        )
                    }
                }
                // Right side: Amount
                Text(
                    text = "KES ${String.format("%,.0f", sale.totalAmount)}",
                    style = AppTextStyles.price
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Date
            Text(
                text = formatDateTime(sale.saleDate),
                style = AppTextStyles.secondary,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Customer
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = sale.customerName ?: "Walk-in Customer",
                    style = AppTextStyles.secondary,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Eye Icon Box
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Visibility,
                            contentDescription = "View",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    // Email Icon Box
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Email,
                            contentDescription = "Email",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                TextButton(onClick = onClick) {
                    Text(
                        text = "View Details",
                        color = NeoBukCyan,
                        style = AppTextStyles.buttonMedium
                    )
                }
            }
        }
    }
}
