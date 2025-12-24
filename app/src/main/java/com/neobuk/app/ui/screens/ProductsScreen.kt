package com.neobuk.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neobuk.app.ui.theme.NeoBukTeal
import com.neobuk.app.ui.theme.Tokens
import com.neobuk.app.ui.theme.AppTextStyles
import com.neobuk.app.ui.theme.NeoBukCyan

import com.neobuk.app.data.models.Product
import com.neobuk.app.viewmodels.InventoryViewModel


@Composable
fun ProductsScreen(
    viewModel: InventoryViewModel,
    onAddProduct: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All Items") }
    
    val products by viewModel.products.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 1. Header & Search
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            // Header removed (moved to Toolbar)

            
            Spacer(modifier = Modifier.height(16.dp))
            
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search products...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                trailingIcon = { 
                    IconButton(onClick = { /* Filter */ }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = MaterialTheme.colorScheme.onSurface)   
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    disabledContainerColor = MaterialTheme.colorScheme.surface,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Add Product Button
            Button(
                onClick = onAddProduct,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeoBukTeal
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Add New Product",
                    style = AppTextStyles.buttonLarge,
                    color = Color.White
                )
            }
        }

        // Scrollable Content
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp) // Space for FAB if needed
        ) {
            // 2. Stats Cards
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        icon = Icons.Outlined.Inventory2,
                        label = "Total Items",
                        count = products.size.toString(),
                        color = NeoBukTeal,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        icon = Icons.Outlined.Warning,
                        label = "Running Low",
                        count = products.count { it.quantity < 10 && it.quantity > 0.0 }.toString(),
                        color = Color(0xFFF59E0B), // Amber/Orange
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        icon = Icons.Default.ViewList,
                        label = "Out of Stock",
                        count = products.count { it.quantity == 0.0 }.toString(),
                        color = Color(0xFFEF4444),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            item { Spacer(modifier = Modifier.height(24.dp)) }
            
            // 3. Filters
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item { FilterChip(text = "All Items", selected = selectedFilter == "All Items", onClick = { selectedFilter = "All Items" }) }
                    item { FilterChip(text = "Running Low", selected = selectedFilter == "Running Low", onClick = { selectedFilter = "Running Low" }) }
                    item { FilterChip(text = "Out of Stock", selected = selectedFilter == "Out of Stock", onClick = { selectedFilter = "Out of Stock" }) }
                    item { 
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier.height(32.dp),
                            onClick = {}
                        ) {
                           Row(
                               verticalAlignment = Alignment.CenterVertically,
                               modifier = Modifier.padding(horizontal = 12.dp)
                           ) {
                               Icon(Icons.Default.Sort, null, modifier = Modifier.size(16.dp))
                               Spacer(modifier = Modifier.width(4.dp))
                               Text("Sort", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                           }
                        }
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(24.dp)) }
            
            // 4. Products Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Items (${products.size})",
                        style = AppTextStyles.sectionTitle,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Row(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(NeoBukTeal.copy(alpha = 0.1f), RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.GridView, null, tint = NeoBukTeal, modifier = Modifier.size(20.dp))
                        }
                        Box(
                            modifier = Modifier
                                .size(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.ViewList, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(16.dp)) }
            
            // 5. Product Grid (Using FlowRow logic for simplicity inside LazyColumn or fixed inner grid)
            // Since we are inside a LazyColumn, we can't easily put a LazyVerticalGrid. 
            // We'll simulate a 2-column grid manually for this layout.
            item {
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    val rows = products.chunked(2)
                    for (rowItems in rows) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            for (product in rowItems) {
                                Box(modifier = Modifier.weight(1f)) {
                                    ProductCard(product)
                                }
                            }
                            if (rowItems.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun FilterChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (selected) NeoBukTeal else MaterialTheme.colorScheme.surface,
        border = if (selected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.height(32.dp),
        onClick = onClick
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = text,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun StatCard(
    icon: ImageVector,
    label: String,
    count: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                label,
                style = AppTextStyles.secondary,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(count, style = AppTextStyles.amountLarge, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun ProductCard(product: Product) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Image Placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Placeholder Image
                Icon(Icons.Outlined.Inventory2, null, tint = Tokens.TextMuted.copy(alpha = 0.3f), modifier = Modifier.size(48.dp))
                
                // Kebab Menu
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopEnd) {
                    IconButton(onClick = { }, modifier = Modifier.size(24.dp).padding(4.dp)) {
                        Icon(Icons.Default.MoreVert, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Name
            Text(
                text = product.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Price
            Text(
                text = "KES ${String.format("%.0f", product.sellingPrice)} per ${product.unit}",
                style = AppTextStyles.secondary,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Margin Badge
            if (product.costPrice > 0 && product.sellingPrice > 0) {
                 val profit = product.sellingPrice - product.costPrice
                 val margin = (profit / product.sellingPrice) * 100
                 val isProfitable = profit >= 0
                 val marginColor = if (isProfitable) Color(0xFF10B981) else Color(0xFFEF4444)
                 
                 Spacer(modifier = Modifier.height(4.dp))
                 Text(
                    text = "Margin: ${String.format("%.0f", margin)}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = marginColor,
                    fontWeight = FontWeight.Medium
                 )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Stock Badge
            val quantity = product.quantity.toInt()
            val stockColor = when {
                quantity == 0 -> Tokens.TextMuted
                quantity < 10 -> Color(0xFFEF4444)
                else -> Color(0xFF10B981)
            }
            val stockBg = stockColor.copy(alpha = 0.1f)
            
            Box(
                modifier = Modifier
                    .background(stockBg, RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "$quantity left",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = stockColor
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Actions
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                
                // Edit Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(32.dp)
                        .background(NeoBukTeal.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Edit", fontSize = 12.sp, color = NeoBukTeal, fontWeight = FontWeight.SemiBold)
                }
                
                // Restock Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(32.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Add Stock", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
