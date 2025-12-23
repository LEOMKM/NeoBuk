package com.neobuk.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import com.neobuk.app.ui.theme.Tokens
import com.neobuk.app.ui.theme.AppTextStyles

/**
 * All Expenses Screen - Shows full list of expenses with search and filtering
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllExpensesScreen(
    onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    // Mock expenses data
    val allExpenses = remember {
        listOf(
            Triple("Rent Payment", "Rent", 25000.0),
            Triple("Electricity Bill", "Utilities", 3500.0),
            Triple("Water Bill", "Utilities", 1200.0),
            Triple("Staff Salary - John", "Salaries", 15000.0),
            Triple("Staff Salary - Jane", "Salaries", 12000.0),
            Triple("Stock Purchase - Flour", "Stock", 8500.0),
            Triple("Stock Purchase - Sugar", "Stock", 4200.0),
            Triple("Transport", "Transport", 2000.0),
            Triple("Office Supplies", "Supplies", 1500.0),
            Triple("Internet Bill", "Utilities", 2500.0),
            Triple("Mobile Airtime", "Communication", 500.0),
            Triple("Equipment Repair", "Maintenance", 3000.0)
        )
    }

    val categories = listOf("All", "Rent", "Utilities", "Salaries", "Stock", "Transport", "Supplies", "Communication", "Maintenance")

    val filteredExpenses = allExpenses.filter { expense ->
        (selectedCategory == "All" || expense.second == selectedCategory) &&
        (searchQuery.isEmpty() || expense.first.contains(searchQuery, ignoreCase = true))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Bar
        TopAppBar(
            title = { Text("All Expenses", style = AppTextStyles.pageTitle) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        // Search & Filter
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search expenses...") },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedBorderColor = NeoBukTeal,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Category Filter Chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                categories.forEach { category ->
                    FilterChip(
                        onClick = { selectedCategory = category },
                        label = { Text(category, fontSize = 12.sp) },
                        selected = selectedCategory == category,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeoBukTeal,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Summary Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Total Expenses", style = AppTextStyles.secondary, color = Color.Gray)
                    Text(
                        "KES ${String.format("%,.0f", filteredExpenses.sumOf { it.third })}",
                        style = AppTextStyles.amountLarge,
                        color = Color(0xFFEF4444)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Count", style = AppTextStyles.secondary, color = Color.Gray)
                    Text(
                        "${filteredExpenses.size} items",
                        style = AppTextStyles.bodyBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Expenses List
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredExpenses) { expense ->
                ExpenseListItem(
                    title = expense.first,
                    category = expense.second,
                    amount = expense.third
                )
            }
        }
    }
}

@Composable
private fun ExpenseListItem(
    title: String,
    category: String,
    amount: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFFEE2E2)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.Receipt,
                        null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(title, style = AppTextStyles.body, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                    Text(category, style = AppTextStyles.secondary, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Text(
                "- KES ${String.format("%,.0f", amount)}",
                style = AppTextStyles.price,
                color = Color(0xFFEF4444)
            )
        }
    }
}

