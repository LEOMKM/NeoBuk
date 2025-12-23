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
import com.neobuk.app.ui.theme.NeoBukCyan
import com.neobuk.app.ui.theme.NeoBukTeal
import com.neobuk.app.ui.theme.Tokens
import com.neobuk.app.ui.theme.AppTextStyles

// Data Classes
data class SaleCartItem(
    val productId: String,
    val name: String,
    val quantity: Int,
    val unitPrice: Double
) {
    val totalPrice: Double get() = quantity * unitPrice
}

data class SaleProduct(
    val id: String,
    val name: String,
    val price: Double,
    val stock: Int,
    val unit: String,
    val barcode: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewSaleScreen(
    onDismiss: () -> Unit,
    onCompleteSale: (List<SaleCartItem>, String, String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var customerName by remember { mutableStateOf("") }
    var selectedPaymentMethod by remember { mutableStateOf("Cash") }
    
    // Cart State
    val cartItems = remember { mutableStateListOf<SaleCartItem>() }
    val cartTotal = cartItems.sumOf { it.totalPrice }

    // Mock Products for browsing
    val availableProducts = remember {
        listOf(
            SaleProduct("1", "Maize Flour 2kg", 120.0, 45, "pcs", "5012345678900"),
            SaleProduct("2", "Sugar 1kg", 150.0, 32, "pcs", "5012345678901"),
            SaleProduct("3", "Cooking Oil 2L", 350.0, 18, "btl", "5012345678902"),
            SaleProduct("4", "Tea Leaves 500g", 180.0, 24, "pkt", "5012345678903"),
            SaleProduct("5", "Milk 500ml", 65.0, 50, "pcs", "5012345678904"),
            SaleProduct("6", "Bread 400g", 55.0, 20, "pcs", "5012345678905"),
            SaleProduct("7", "Rice 2kg", 280.0, 15, "pcs", "5012345678906"),
            SaleProduct("8", "Salt 1kg", 45.0, 40, "pcs", "5012345678907")
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
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
            containerColor = Color.White,
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
                    onProductScanned = { barcode ->
                        // Find product by barcode
                        val product = availableProducts.find { it.barcode == barcode }
                        if (product != null) {
                            // Check if already in cart
                            val existingIndex = cartItems.indexOfFirst { it.productId == product.id }
                            if (existingIndex >= 0) {
                                val existing = cartItems[existingIndex]
                                cartItems[existingIndex] = existing.copy(quantity = existing.quantity + 1)
                            } else {
                                cartItems.add(SaleCartItem(product.id, product.name, 1, product.price))
                            }
                        }
                    }
                )
                1 -> BrowseTab(
                    products = availableProducts,
                    onProductSelected = { product ->
                        // Check if already in cart
                        val existingIndex = cartItems.indexOfFirst { it.productId == product.id }
                        if (existingIndex >= 0) {
                            val existing = cartItems[existingIndex]
                            cartItems[existingIndex] = existing.copy(quantity = existing.quantity + 1)
                        } else {
                            cartItems.add(SaleCartItem(product.id, product.name, 1, product.price))
                        }
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
                if (newQty <= 0) {
                    cartItems.removeAt(index)
                } else {
                    cartItems[index] = cartItems[index].copy(quantity = newQty)
                }
            },
            onRemoveItem = { index -> cartItems.removeAt(index) },
            onCompleteSale = {
                onCompleteSale(cartItems.toList(), customerName, selectedPaymentMethod)
            }
        )
    }
}

@Composable
fun ScanTab(
    onProductScanned: (String) -> Unit
) {
    var manualBarcode by remember { mutableStateOf("") }

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
                Button(
                    onClick = { onProductScanned("5012345678900") }, // Maize Flour
                    colors = ButtonDefaults.buttonColors(containerColor = NeoBukTeal.copy(alpha = 0.9f)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(Icons.Default.FlashOn, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Simulate Scan", fontSize = 12.sp)
                }
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
                    unfocusedBorderColor = Color.LightGray,
                    focusedBorderColor = NeoBukTeal
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    if (manualBarcode.isNotBlank()) {
                        onProductScanned(manualBarcode)
                        manualBarcode = ""
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
    products: List<SaleProduct>,
    onProductSelected: (SaleProduct) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredProducts = products.filter {
        it.name.contains(searchQuery, ignoreCase = true)
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
                unfocusedBorderColor = Color.LightGray,
                focusedBorderColor = NeoBukTeal,
                unfocusedContainerColor = Color(0xFFF8FAFC),
                focusedContainerColor = Color(0xFFF8FAFC)
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
    product: SaleProduct,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        colors = CardDefaults.cardColors(containerColor = Color.White),
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
                        color = Tokens.TextDark
                    )
                    Row {
                        Text(
                            "KES ${String.format("%,.0f", product.price)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeoBukTeal
                        )
                        Text(
                            " • ${product.stock} in stock",
                            fontSize = 12.sp,
                            color = if (product.stock < 10) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant
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
    cartItems: List<SaleCartItem>,
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
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
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
                        "Cart (${cartItems.sumOf { it.quantity }} items)",
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
                    color = Color.LightGray.copy(alpha = 0.5f)
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
                        unfocusedBorderColor = Color.LightGray,
                        focusedBorderColor = NeoBukTeal,
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White
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
                            } else Color.White,
                            border = if (selectedPaymentMethod != method)
                                androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray) else null,
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
                                    color = if (selectedPaymentMethod == method) Color.White else Color.DarkGray
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
    item: SaleCartItem,
    onQuantityChange: (Int) -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Product Name
        Text(
            item.name,
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
                onClick = { onQuantityChange(item.quantity - 1) },
                modifier = Modifier
                    .size(32.dp)
                    .background(Color(0xFFEEEEEE), RoundedCornerShape(6.dp))
            ) {
                Icon(Icons.Default.Remove, null, modifier = Modifier.size(16.dp))
            }

            IconButton(
                onClick = { onQuantityChange(item.quantity + 1) },
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
