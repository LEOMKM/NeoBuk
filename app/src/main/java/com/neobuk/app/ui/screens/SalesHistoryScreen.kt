package com.neobuk.app.ui.screens

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesHistoryScreen() {
    var showDetailSheet by remember { mutableStateOf(false) }
    var showNewSaleSheet by remember { mutableStateOf(false) }
    var showReceiptView by remember { mutableStateOf(false) }
    var selectedTransaction by remember { mutableStateOf<TransactionData?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val transactions = listOf(
        TransactionData("Multiple Items (5)", "M-PESA", Color(0xFFE8F5E9), Color(0xFF2E7D32), "KES 2,450", "May 15, 2025 • 14:35", "Jane Wambui", "QK29X7A451"),
        TransactionData("Sugar (5kg), Cooking Oil (2L)", "Cash", Color(0xFFE3F2FD), Color(0xFF1565C0), "KES 1,350", "May 15, 2025 • 13:22", "Walk-in Customer"),
        TransactionData("Maize Flour (10kg)", "M-PESA", Color(0xFFE8F5E9), Color(0xFF2E7D32), "KES 1,200", "May 15, 2025 • 11:47", "Mama Njeri", "QK29X7B892"),
        TransactionData("Tea Leaves (500g), Sugar...", "Cash", Color(0xFFE3F2FD), Color(0xFF1565C0), "KES 750", "May 15, 2025 • 10:15", "John Kamau")
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // 1. Header Section
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header removed (moved to Toolbar)

                
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
            SummaryCard()
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
        items(transactions) { trans ->
            TransactionItem(
                data = trans,
                onClick = {
                    selectedTransaction = trans
                    showDetailSheet = true
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    if (showDetailSheet && selectedTransaction != null) {
        ModalBottomSheet(
            onDismissRequest = { showDetailSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            SalesDetailSheet(
                transaction = selectedTransaction!!,
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
                onDismiss = { showNewSaleSheet = false },
                onCompleteSale = { _, _, _ ->
                }
            )
        }
    }

    if (showReceiptView && selectedTransaction != null) {
        // Mock mapping for demo purposes
        val mockItems = listOf(
            ReceiptItem("Maize Flour", 2, "120", "240"),
            ReceiptItem("Sugar", 3, "150", "450"),
            ReceiptItem("Cooking Oil", 2, "220", "440")
        )
        val receiptData = ReceiptData(
            businessName = "Mama Njeri's Shop", // Pull from user pref in real app
            receiptId = "SALE-${selectedTransaction!!.date.filter { it.isDigit() }.take(8)}-001",
            date = selectedTransaction!!.date,
            paymentMethod = selectedTransaction!!.paymentMethod,
            customerName = selectedTransaction!!.customer,
            totalAmount = selectedTransaction!!.amount,

            items = mockItems,
            paymentRef = selectedTransaction!!.paymentRef
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

data class TransactionData(
    val title: String,
    val paymentMethod: String,
    val paymentColor: Color,
    val paymentTextColor: Color,
    val amount: String,
    val date: String,

    val customer: String,
    val paymentRef: String? = null
)

@Composable
fun SalesDetailSheet(
    transaction: TransactionData, 
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
                Text("#SALE-20250515-001", style = AppTextStyles.bodyBold)
            }
            Box(
                modifier = Modifier
                    .background(Color(0xFFE8F5E9), RoundedCornerShape(16.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text("Recorded", style = AppTextStyles.caption, color = Color(0xFF2E7D32))
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
                Text(transaction.date, style = AppTextStyles.body)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Payment Method", style = AppTextStyles.caption, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(transaction.paymentMethod, style = AppTextStyles.body)
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
                Text(transaction.customer, style = AppTextStyles.bodyBold)
                Text("+254 712 345 678", style = AppTextStyles.caption, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Text("Items", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        
        // Items List (Mock)
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            ItemRow("Maize Flour", "2 × KES 120", "KES 240")
            Spacer(modifier = Modifier.height(12.dp))
            ItemRow("Sugar", "3 × KES 150", "KES 450")
            Spacer(modifier = Modifier.height(12.dp))
            ItemRow("Cooking Oil", "2 × KES 220", "KES 440")
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray.copy(alpha = 0.3f))
            
            // Totals
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Subtotal", color = Color.Gray)
                Text("KES 1,130", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("VAT (16%)", color = Color.Gray)
                Text("KES 0", fontWeight = FontWeight.Bold) // Simplified
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total", style = AppTextStyles.amountLarge)
                Text(transaction.amount, style = AppTextStyles.amountLarge)
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
                     ReceiptUtils.shareText(context, "Receipt for ${transaction.amount} from Mama Njeri's Shop. Paid via ${transaction.paymentMethod}.")
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
fun SummaryCard() {
    var chartVisible by remember { mutableStateOf(false) }
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
                text = "KES 12,450",
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
                MiniStatItem(Modifier.weight(1f), "KES 1,035", "Avg. Sale")
                MiniStatItem(Modifier.weight(1f), "12", "Sales Count")
                MiniStatItem(Modifier.weight(1f), "Maize Flour", "Top Product")
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Chart Area (Visual Clone)
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
    val animatedHeight by androidx.compose.animation.core.animateFloatAsState(
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
    data: TransactionData,
    onClick: () -> Unit
) {
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
                        text = data.title.take(18) + if(data.title.length > 18) "..." else "",
                        style = AppTextStyles.bodyBold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(data.paymentColor, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = data.paymentMethod,
                            style = MaterialTheme.typography.labelSmall,
                            color = data.paymentTextColor
                        )
                    }
                }
                // Right side: Amount
                Text(
                    text = data.amount,
                    style = AppTextStyles.price
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Date
            Text(
                text = data.date,
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
                    text = data.customer,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewSaleSheet(
    onDismiss: () -> Unit,
    onSubmit: () -> Unit
) {
    var customerName by remember { mutableStateOf("") }
    var selectedPaymentMethod by remember { mutableStateOf("Cash") }
    
    // Mock cart items
    val cartItems = remember {
        listOf(
            CartItem("Maize Flour", 2, 120.0),
            CartItem("Sugar 1kg", 3, 150.0),
            CartItem("Cooking Oil 2L", 1, 350.0)
        )
    }
    val subtotal = cartItems.sumOf { it.quantity * it.price }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(bottom = 32.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "New Sale",
                style = AppTextStyles.pageTitle
            )
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }
        
        HorizontalDivider(
            color = Color.LightGray.copy(alpha = 0.2f),
            modifier = Modifier.padding(vertical = 16.dp)
        )
        
        // Customer Name (Optional)
        Text("Customer Name (Optional)", style = AppTextStyles.bodyBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = customerName,
            onValueChange = { customerName = it },
            placeholder = { Text("Walk-in Customer") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
                focusedBorderColor = NeoBukTeal
            )
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Cart Items
        Text("Items", style = AppTextStyles.bodyBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                cartItems.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${item.name} × ${item.quantity}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("KES ${String.format("%,.0f", item.quantity * item.price)}", fontWeight = FontWeight.Medium)
                    }
                    if (item != cartItems.last()) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray.copy(alpha = 0.3f))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total", style = AppTextStyles.amountLarge)
                    Text("KES ${String.format("%,.0f", subtotal)}", style = AppTextStyles.amountLarge, color = NeoBukTeal)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Add Items Button
        OutlinedButton(
            onClick = { /* Open product scanner/selector */ },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, NeoBukTeal.copy(alpha = 0.5f))
        ) {
            Icon(Icons.Default.Add, null, tint = NeoBukTeal, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add / Scan Products", color = NeoBukTeal)
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Payment Method
        Text("Payment Method", style = AppTextStyles.bodyBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Cash", "M-PESA", "Card").forEach { method ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (selectedPaymentMethod == method) NeoBukTeal.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant,
                    border = if (selectedPaymentMethod == method) 
                        androidx.compose.foundation.BorderStroke(1.dp, NeoBukTeal) else null,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clickable { selectedPaymentMethod = method }
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(
                            method,
                            style = AppTextStyles.body,
                            color = if (selectedPaymentMethod == method) NeoBukTeal else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Submit Button
        Button(
            onClick = onSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NeoBukTeal),
            enabled = cartItems.isNotEmpty()
        ) {
            Icon(
                Icons.Default.ShoppingCart,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                "Complete Sale • KES ${String.format("%,.0f", subtotal)}",
                style = AppTextStyles.buttonLarge
            )
        }
    }
}

data class CartItem(
    val name: String,
    val quantity: Int,
    val price: Double
)
