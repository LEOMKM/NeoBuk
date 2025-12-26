package com.neobuk.app.ui.screens.sales

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neobuk.app.data.repositories.CartItem
import com.neobuk.app.data.repositories.Product
import com.neobuk.app.ui.theme.NeoBukCyan
import com.neobuk.app.ui.theme.NeoBukTeal
import com.neobuk.app.ui.theme.Tokens
import com.neobuk.app.ui.theme.AppTextStyles
import com.neobuk.app.viewmodels.ProductsViewModel
import com.neobuk.app.viewmodels.SalesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewSaleScreen(
    salesViewModel: SalesViewModel,
    productsViewModel: ProductsViewModel,
    onDismiss: () -> Unit,
    onCompleteSale: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var customerName by remember { mutableStateOf("") }
    var selectedPaymentMethod by remember { mutableStateOf("Cash") }
    
    // Create Sale State - observe from ViewModel
    val cartItems by salesViewModel.cartItems.collectAsState()
    val cartTotal by salesViewModel.cartTotal.collectAsState()
    
    // Product Data
    val availableProducts by productsViewModel.products.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "New Sale",
                style = AppTextStyles.pageTitle
            )
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }

        // Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = NeoBukTeal,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = NeoBukTeal,
                    height = 3.dp
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.QrCodeScanner,
                            null,
                            modifier = Modifier.size(18.dp),
                            tint = if (selectedTab == 0) NeoBukTeal else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Scan", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.GridView,
                            null,
                            modifier = Modifier.size(18.dp),
                            tint = if (selectedTab == 1) NeoBukTeal else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Browse", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            )
        }

        // Tab Content
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (selectedTab) {
                0 -> ScanTab(
                    productsViewModel = productsViewModel,
                    onProductFound = { product ->
                        salesViewModel.addProductToCart(product)
                    }
                )
                1 -> BrowseTab(
                    products = availableProducts,
                    onProductSelected = { product ->
                        salesViewModel.addProductToCart(product)
                    }
                )
            }
        }

        // Cart & Checkout Section (Always visible at bottom)
        CartAndCheckoutSection(
            cartItems = cartItems,
            cartTotal = cartTotal,
            customerName = customerName,
            onCustomerNameChange = { customerName = it },
            selectedPaymentMethod = selectedPaymentMethod,
            onPaymentMethodChange = { selectedPaymentMethod = it },
            onUpdateQuantity = { index, newQty ->
                salesViewModel.updateCartItemQuantity(index, newQty.toDouble())
            },
            onRemoveItem = { index -> salesViewModel.removeCartItem(index) },
            onCompleteSale = {
                salesViewModel.checkout(
                    paymentMethod = selectedPaymentMethod,
                    customerName = customerName.ifBlank { "Walk-in Customer" },
                    onSuccess = {
                        onCompleteSale()
                    },
                    onError = {
                        // Handle error (show Snackbar, etc - can be done via ViewModel error state in parent)
                    }
                )
            }
        )
    }
}

@Composable
fun ScanTab(
    productsViewModel: ProductsViewModel,
    onProductFound: (Product) -> Unit
) {
    var manualBarcode by remember { mutableStateOf("") }
    // In a real app, integrate CameraX here for scanning
    
    // For now, simulate scanning by searching by barcode manually mostly
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Camera Preview Placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1A1A1A)),
            contentAlignment = Alignment.Center
        ) {
            // Scan Frame
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .drawBehind {
                        val stroke = Stroke(
                            width = 4.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(30f, 15f), 0f)
                        )
                        drawRoundRect(
                            color = NeoBukCyan,
                            cornerRadius = CornerRadius(16.dp.toPx()),
                            style = stroke
                        )
                    }
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.QrCodeScanner,
                        null,
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Point camera at barcode",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 13.sp
                    )
                }
            }

            // Simulate Scan Buttons (for testing)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
               // No mocked button needed if we use manual entry, but keeping for dev feel if needed
               // Or we can add a "Scan Random Product" for demo
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Manual Entry
        Text(
            "Or enter barcode manually",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = manualBarcode,
                onValueChange = { manualBarcode = it },
                placeholder = { Text("Enter barcode...") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedBorderColor = NeoBukTeal
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    if (manualBarcode.isNotBlank()) {
                        productsViewModel.findProductByBarcode(
                            barcode = manualBarcode,
                            onFound = { 
                                onProductFound(it)
                                manualBarcode = "" 
                            },
                            onNotFound = { /* Show error */ }
                        )
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeoBukTeal),
                modifier = Modifier.height(56.dp)
            ) {
                Icon(Icons.Default.Search, null)
            }
        }
    }
}

@Composable
fun BrowseTab(
    products: List<Product>,
    onProductSelected: (Product) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredProducts = products.filter {
        it.name.contains(searchQuery, ignoreCase = true) || 
        (it.barcode?.contains(searchQuery) == true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search products...") },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedBorderColor = NeoBukTeal,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Products Grid/List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredProducts) { product ->
                ProductSelectCard(
                    product = product,
                    onSelect = { onProductSelected(product) }
                )
            }
        }
    }
}

@Composable
fun ProductSelectCard(
    product: Product,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                // Product Icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(NeoBukTeal.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.Inventory2,
                        null,
                        tint = NeoBukTeal,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        product.name,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row {
                        Text(
                            "KES ${String.format("%,.0f", product.sellingPrice)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeoBukTeal
                        )
                        Text(
                            " • ${product.quantity.toInt()} in stock",
                            fontSize = 12.sp,
                            color = if (product.isLowStock) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Add Button
            IconButton(
                onClick = onSelect,
                modifier = Modifier
                    .size(40.dp)
                    .background(NeoBukTeal, RoundedCornerShape(10.dp))
            ) {
                Icon(Icons.Default.Add, null, tint = Color.White)
            }
        }
    }
}

@Composable
fun CartAndCheckoutSection(
    cartItems: List<CartItem>,
    cartTotal: Double,
    customerName: String,
    onCustomerNameChange: (String) -> Unit,
    selectedPaymentMethod: String,
    onPaymentMethodChange: (String) -> Unit,
    onUpdateQuantity: (Int, Int) -> Unit,
    onRemoveItem: (Int) -> Unit,
    onCompleteSale: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Cart Header (Clickable to expand/collapse)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.ShoppingCart,
                        null,
                        tint = NeoBukTeal,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Cart (${cartItems.size} items)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "KES ${String.format("%,.0f", cartTotal)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = NeoBukTeal
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        if (isExpanded) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                        null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (isExpanded && cartItems.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))

                // Cart Items List
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    cartItems.forEachIndexed { index, item ->
                        CartItemRow(
                            item = item,
                            onQuantityChange = { newQty -> onUpdateQuantity(index, newQty) },
                            onRemove = { onRemoveItem(index) }
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                // Customer Name
                OutlinedTextField(
                    value = customerName,
                    onValueChange = onCustomerNameChange,
                    placeholder = { Text("Customer name (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Outlined.Person, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedBorderColor = NeoBukTeal,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Payment Methods
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Cash", "M-PESA", "Card", "Credit").forEach { method ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedPaymentMethod == method) {
                                if (method == "Credit") Color(0xFFF59E0B) else NeoBukTeal
                            } else MaterialTheme.colorScheme.surface,
                            border = if (selectedPaymentMethod != method)
                                androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline) else null,
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clickable { onPaymentMethodChange(method) }
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Text(
                                    method,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (selectedPaymentMethod == method) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
                
                // Profit & Margin Section
                if (cartItems.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Calculate total cost and profit
                    val totalCost = cartItems.sumOf { it.unitCost * it.quantity }
                    val totalRevenue = cartTotal
                    val totalProfit = totalRevenue - totalCost
                    val profitMargin = if (totalRevenue > 0) (totalProfit / totalRevenue) * 100 else 0.0
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (totalProfit >= 0) 
                                Color(0xFF10B981).copy(alpha = 0.1f) 
                            else 
                                Color(0xFFEF4444).copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "Profit",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "KES ${String.format("%,.0f", totalProfit)}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (totalProfit >= 0) Color(0xFF10B981) else Color(0xFFEF4444)
                                )
                            }
                            
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "Margin",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "${String.format("%.1f", profitMargin)}%",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (totalProfit >= 0) Color(0xFF10B981) else Color(0xFFEF4444)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Complete Sale Button
            Button(
                onClick = onCompleteSale,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeoBukTeal),
                enabled = cartItems.isNotEmpty()
            ) {
                Icon(Icons.Default.Done, null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (cartItems.isEmpty()) "Add items to cart"
                    else "Complete Sale • KES ${String.format("%,.0f", cartTotal)}",
                    style = AppTextStyles.buttonMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun CartItemRow(
    item: CartItem,
    onQuantityChange: (Int) -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Product Name
        Text(
            item.displayName,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )

        // Quantity Controls (- and + only)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            IconButton(
                onClick = { onQuantityChange(item.quantity.toInt() - 1) },
                modifier = Modifier
                    .size(32.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest, RoundedCornerShape(6.dp))
            ) {
                Icon(Icons.Default.Remove, null, modifier = Modifier.size(16.dp))
            }

            Text(
                text = "${item.quantity.toInt()}",
                fontWeight = FontWeight.Bold
            )

            IconButton(
                onClick = { onQuantityChange(item.quantity.toInt() + 1) },
                modifier = Modifier
                    .size(32.dp)
                    .background(NeoBukTeal, RoundedCornerShape(6.dp))
            ) {
                Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Delete Icon
        IconButton(
            onClick = onRemove,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                Icons.Outlined.Delete,
                contentDescription = "Remove",
                tint = Color(0xFFEF4444),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
