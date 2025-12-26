package com.neobuk.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neobuk.app.ui.theme.NeoBukCyan
import com.neobuk.app.ui.theme.NeoBukTeal
import com.neobuk.app.ui.theme.Tokens
import com.neobuk.app.ui.theme.AppTextStyles
import com.neobuk.app.data.repositories.Expense
import com.neobuk.app.data.repositories.ExpenseCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(
    expensesViewModel: com.neobuk.app.viewmodels.ExpensesViewModel = org.koin.androidx.compose.koinViewModel(),
    authViewModel: com.neobuk.app.viewmodels.AuthViewModel = org.koin.androidx.compose.koinViewModel()
) {
    var showAddExpenseSheet by remember { mutableStateOf(false) }
    var selectedExpense by remember { mutableStateOf<Expense?>(null) }
    var selectedFilter by remember { mutableStateOf("All") }
    var showAllExpenses by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Observe state
    val currentBusiness by authViewModel.currentBusiness.collectAsState()
    val expenses by expensesViewModel.expenses.collectAsState()
    val categories by expensesViewModel.categories.collectAsState()
    val isLoading by expensesViewModel.isLoading.collectAsState()
    
    // Initialize data when business is available
    LaunchedEffect(currentBusiness) {
        currentBusiness?.let { business ->
            expensesViewModel.setBusinessId(business.id)
        }
    }

    // Calculate stats from real data
    val totalExpenses = expenses.sumOf { it.amount }
    val thisMonthExpenses = remember(expenses) {
        val startOfMonth = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.DAY_OF_MONTH, 1)
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
        }.timeInMillis
        expenses.filter { it.expenseDate >= startOfMonth }.sumOf { it.amount }
    }
    val categoryBreakdown = remember(expenses) {
        expenses.groupBy { it.categoryName }
            .mapValues { it.value.sumOf { exp -> exp.amount } }
            .toList()
            .sortedByDescending { it.second }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // 1. Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                // Header removed (moved to Toolbar)
                Spacer(modifier = Modifier.height(16.dp))
                
                // Prominent Add Expense Button
                Button(
                    onClick = { showAddExpenseSheet = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFEF4444) // Red to emphasize expense
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Record New Expense",
                        style = AppTextStyles.buttonLarge,
                        color = Color.White
                    )
                }
            }
        }

        // 2. Summary Cards
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Total Expenses Card
                ExpenseSummaryCard(
                    title = "This Month",
                    amount = "KES ${String.format("%,.0f", thisMonthExpenses)}",
                    subtitle = "Total Expenses",
                    icon = Icons.Outlined.TrendingDown,
                    color = Color(0xFFEF4444),
                    modifier = Modifier.weight(1f)
                )

                // Expense Count Card
                ExpenseSummaryCard(
                    title = "Transactions",
                    amount = expenses.size.toString(),
                    subtitle = "This month",
                    icon = Icons.Outlined.Receipt,
                    color = NeoBukTeal,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        // 3. Category Breakdown
        item {
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text(
                    text = "By Category",
                    style = AppTextStyles.sectionTitle,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        categoryBreakdown.take(4).forEach { (category, total) ->
                            CategoryBreakdownRow(
                                category = category,
                                amount = total,
                                percentage = (total / totalExpenses * 100).toInt()
                            )
                            if (category != categoryBreakdown.take(4).last().first) {
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        // 4. Filter Chips
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    ExpenseFilterChip(
                        text = "All",
                        selected = selectedFilter == "All",
                        onClick = { selectedFilter = "All" }
                    )
                }
                items(categories) { category ->
                    ExpenseFilterChip(
                        text = category.name,
                        selected = selectedFilter == category.name,
                        onClick = { selectedFilter = category.name }
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        // 5. Expenses List Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Expenses",
                    style = AppTextStyles.sectionTitle,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(onClick = { showAllExpenses = true }) {
                    Text("View All", style = AppTextStyles.buttonMedium, color = NeoBukTeal)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = NeoBukTeal
                    )
                }
            }
        }

        // 6. Expense Items
        val filteredExpenses = if (selectedFilter == "All") {
            expenses
        } else {
            expenses.filter { it.categoryName == selectedFilter }
        }

        // Loading state
        if (isLoading && expenses.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = NeoBukTeal)
                }
            }
        } else if (filteredExpenses.isEmpty()) {
            // Empty state
            item {
                com.neobuk.app.ui.components.EmptyState(
                    title = if (selectedFilter == "All") "No Expenses Yet" else "No $selectedFilter Expenses",
                    description = if (selectedFilter == "All") 
                        "You haven't recorded any expenses yet. Start tracking your expenses to manage your finances better."
                    else
                        "No expenses found in this category. Try selecting a different category or record a new expense.",
                    imageId = com.neobuk.app.R.drawable.empty_sales, // Reusing sales empty drawable
                    buttonText = "Record First Expense",
                    onButtonClick = { showAddExpenseSheet = true }
                )
            }
        } else {
            // Display expense items
            items(filteredExpenses) { expense ->
                ExpenseItem(
                    expense = expense,
                    onClick = { selectedExpense = expense }
                )
            }
        }
    }

    // Add Expense Bottom Sheet
    if (showAddExpenseSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddExpenseSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            AddExpenseSheet(
                onDismiss = { showAddExpenseSheet = false },
                onSubmit = { expense ->
                    // Create expense via ViewModel
                    expensesViewModel.createExpense(
                        title = expense.title,
                        categoryId = expense.categoryId,
                        amount = expense.amount,
                        description = expense.description,
                        paymentMethod = expense.paymentMethod
                    )
                    showAddExpenseSheet = false
                },
                categories = categories
            )
        }
    }

    // Expense Detail Sheet
    if (selectedExpense != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedExpense = null },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            ExpenseDetailSheet(
                expense = selectedExpense!!,
                onClose = { selectedExpense = null }
            )
        }
    }
    
    // All Expenses Full Screen
    if (showAllExpenses) {
        AllExpensesScreen(
            expenses = expenses,
            categories = categories,
            isLoading = isLoading,
            onBack = { showAllExpenses = false },
            onExpenseClick = { expense ->
                selectedExpense = expense
                showAllExpenses = false
            }
        )
    }
}

@Composable
fun ExpenseSummaryCard(
    title: String,
    amount: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = AppTextStyles.secondary,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(color.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = amount,
                style = AppTextStyles.amountLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = AppTextStyles.secondary,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun CategoryBreakdownRow(
    category: String,
    amount: Double,
    percentage: Int
) {
    // Map category names to colors (simplified)
    val categoryColor = when(category) {
        "Utilities" -> Color(0xFFF59E0B)
        "Rent" -> Color(0xFF6366F1)
        "Supplies" -> Color(0xFF10B981)
        "Transport" -> Color(0xFF3B82F6)
        "Salary" -> Color(0xFFEC4899)
        "Maintenance" -> Color(0xFF8B5CF6)
        else -> Color(0xFF6B7280)
    }
    
    val categoryIcon = when(category) {
        "Utilities" -> Icons.Outlined.Bolt
        "Rent" -> Icons.Outlined.Home
        "Supplies" -> Icons.Outlined.ShoppingCart
        "Transport" -> Icons.Outlined.LocalShipping
        "Salary" -> Icons.Outlined.People
        "Maintenance" -> Icons.Outlined.Build
        else -> Icons.Outlined.MoreHoriz
    }
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(categoryColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    categoryIcon,
                    contentDescription = null,
                    tint = categoryColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = category,
                    style = AppTextStyles.bodyBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "$percentage% of total",
                    style = AppTextStyles.secondary,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Text(
            text = "KES ${String.format("%,.0f", amount)}",
            style = AppTextStyles.price,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun ExpenseFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (selected) NeoBukTeal else MaterialTheme.colorScheme.surface,
        border = if (!selected) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline) else null,
        modifier = Modifier
            .height(36.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = AppTextStyles.body,
                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun ExpenseItem(
    expense: Expense,
    onClick: () -> Unit
) {
    // Map category names to colors and icons
    val categoryColor = when(expense.categoryName) {
        "Utilities" -> Color(0xFFF59E0B)
        "Rent" -> Color(0xFF6366F1)
        "Supplies" -> Color(0xFF10B981)
        "Transport" -> Color(0xFF3B82F6)
        "Salary" -> Color(0xFFEC4899)
        "Maintenance" -> Color(0xFF8B5CF6)
        else -> Color(0xFF6B7280)
    }
    
    val categoryIcon = when(expense.categoryName) {
        "Utilities" -> Icons.Outlined.Bolt
        "Rent" -> Icons.Outlined.Home
        "Supplies" -> Icons.Outlined.ShoppingCart
        "Transport" -> Icons.Outlined.LocalShipping
        "Salary" -> Icons.Outlined.People
        "Maintenance" -> Icons.Outlined.Build
        else -> Icons.Outlined.MoreHoriz
    }
    
    // Format date
    val dateFormat = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
    val formattedDate = dateFormat.format(java.util.Date(expense.expenseDate))
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(categoryColor.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        categoryIcon,
                        contentDescription = null,
                        tint = categoryColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = expense.title,
                        style = AppTextStyles.bodyBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Text(
                        text = "${expense.categoryName} • $formattedDate",
                        style = AppTextStyles.secondary,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "-KES ${String.format("%,.0f", expense.amount)}",
                    style = AppTextStyles.price,
                    color = Color(0xFFEF4444)
                )
                Box(
                    modifier = Modifier
                        .background(
                            when (expense.paymentMethod) {
                                "M-PESA" -> Color(0xFFE8F5E9)
                                "Bank Transfer" -> Color(0xFFE3F2FD)
                                else -> Color(0xFFF5F5F5)
                            },
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = expense.paymentMethod,
                        style = AppTextStyles.caption,
                        color = when (expense.paymentMethod) {
                            "M-PESA" -> Color(0xFF2E7D32)
                            "Bank Transfer" -> Color(0xFF1565C0)
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseSheet(
    onDismiss: () -> Unit,
    onSubmit: (Expense) -> Unit,
    categories: List<ExpenseCategory> = emptyList()
) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<ExpenseCategory?>(categories.firstOrNull()) }
    var description by remember { mutableStateOf("") }
    var selectedPaymentMethod by remember { mutableStateOf("Cash") }

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
                text = "Add Expense",
                style = AppTextStyles.sectionTitle,
                color = MaterialTheme.colorScheme.onSurface
            )
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }

        HorizontalDivider(
            color = Color.LightGray.copy(alpha = 0.2f),
            modifier = Modifier.padding(vertical = 16.dp)
        )

        // Title
        Text("Expense Title", style = AppTextStyles.bodyBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            placeholder = { Text("e.g., Electricity Bill") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                focusedBorderColor = NeoBukTeal
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Amount
        Text("Amount (KES)", style = AppTextStyles.bodyBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
            placeholder = { Text("0.00") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                focusedBorderColor = NeoBukTeal
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Category
        Text("Category", style = AppTextStyles.bodyBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(categories) { category ->
                CategoryChip(
                    category = category,
                    selected = selectedCategory?.id == category.id,
                    onClick = { selectedCategory = category }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Payment Method
        Text("Payment Method", style = AppTextStyles.bodyBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Cash", "M-PESA", "Bank Transfer").forEach { method ->
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
                            text = method,
                            style = AppTextStyles.body,
                            color = if (selectedPaymentMethod == method) NeoBukTeal else MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Description
        Text("Description (Optional)", style = AppTextStyles.bodyBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            placeholder = { Text("Add notes...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                focusedBorderColor = NeoBukTeal
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Submit Button
        Button(
            onClick = {
                selectedCategory?.let { category ->
                    val expense = Expense(
                        id = "",  // Will be generated by backend
                        categoryId = category.id,
                        categoryName = category.name,
                        title = title,
                        amount = amount.toDoubleOrNull() ?: 0.0,
                        description = description,
                        paymentMethod = selectedPaymentMethod,
                        receiptUrl = null,
                        expenseDate = System.currentTimeMillis(),
                        createdAt = System.currentTimeMillis()
                    )
                    onSubmit(expense)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NeoBukTeal),
            enabled = title.isNotBlank() && amount.isNotBlank() && selectedCategory != null
        ) {
            Text("Add Expense", style = AppTextStyles.buttonLarge)
        }
    }
}

@Composable
fun CategoryChip(
    category: ExpenseCategory,
    selected: Boolean,
    onClick: () -> Unit
) {
    // Map category names to colors and icons
    val categoryColor = when(category.name) {
        "Utilities" -> Color(0xFFF59E0B)
        "Rent" -> Color(0xFF6366F1)
        "Supplies" -> Color(0xFF10B981)
        "Transport" -> Color(0xFF3B82F6)
        "Salary" -> Color(0xFFEC4899)
        "Maintenance" -> Color(0xFF8B5CF6)
        else -> Color(0xFF6B7280)
    }
    
    val categoryIcon = when(category.name) {
        "Utilities" -> Icons.Outlined.Bolt
        "Rent" -> Icons.Outlined.Home
        "Supplies" -> Icons.Outlined.ShoppingCart
        "Transport" -> Icons.Outlined.LocalShipping
        "Salary" -> Icons.Outlined.People
        "Maintenance" -> Icons.Outlined.Build
        else -> Icons.Outlined.MoreHoriz
    }
    
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (selected) categoryColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
        border = if (selected) androidx.compose.foundation.BorderStroke(1.dp, categoryColor) else null,
        modifier = Modifier
            .height(40.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                categoryIcon,
                contentDescription = null,
                tint = if (selected) categoryColor else Tokens.TextMuted,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = category.name,
                style = AppTextStyles.labelLarge,
                color = if (selected) categoryColor else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun ExpenseDetailSheet(
    expense: Expense,
    onClose: () -> Unit
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
                "Expense Details",
                style = AppTextStyles.pageTitle,
                color = MaterialTheme.colorScheme.onSurface
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }

        HorizontalDivider(
            color = Color.LightGray.copy(alpha = 0.2f),
            modifier = Modifier.padding(vertical = 16.dp)
        )

        // Amount Highlight
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Amount", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "KES ${String.format("%,.0f", expense.amount)}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFEF4444)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Details Grid
        DetailRow("Title", expense.title)
        DetailRow("Category", expense.categoryName)
        DetailRow("Date", java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date(expense.expenseDate)))
        DetailRow("Payment Method", expense.paymentMethod)
        if (expense.description.isNotBlank()) {
            DetailRow("Description", expense.description)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Actions
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(
                onClick = { /* Edit */ },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Edit")
            }

            Button(
                onClick = { /* Delete */ },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
            ) {
                Icon(Icons.Default.Delete, null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Delete", color = Color.White)
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
        Text(value, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun AllExpensesScreen(
    expenses: List<Expense>,
    categories: List<ExpenseCategory>,
    isLoading: Boolean,
    onBack: () -> Unit,
    onExpenseClick: (Expense) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("All") }

    val filteredExpenses = expenses.filter { expense ->
        val matchesSearch = expense.title.contains(searchQuery, ignoreCase = true) ||
                expense.categoryName.contains(searchQuery, ignoreCase = true) ||
                expense.description.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedCategoryFilter == "All" || expense.categoryName == selectedCategoryFilter
        matchesSearch && matchesCategory
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                    Text(
                        text = "All Expenses",
                        style = AppTextStyles.pageTitle,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Search expenses...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = if (searchQuery.isNotEmpty()) {
                        {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    } else null,
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeoBukTeal,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                )

                // Category Filter
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategoryFilter == "All",
                            onClick = { selectedCategoryFilter = "All" },
                            label = { Text("All") }
                        )
                    }
                    items(categories) { category ->
                        FilterChip(
                            selected = selectedCategoryFilter == category.name,
                            onClick = { selectedCategoryFilter = category.name },
                            label = { Text(category.name) }
                        )
                    }
                }
                
                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            if (isLoading && expenses.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = NeoBukTeal)
                    }
                }
            } else if (filteredExpenses.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Outlined.ReceiptLong,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Tokens.TextMuted.copy(alpha = 0.3f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "No expenses found",
                                style = AppTextStyles.sectionTitle,
                                color = Tokens.TextMuted
                            )
                            if (searchQuery.isNotEmpty() || selectedCategoryFilter != "All") {
                                Text(
                                    "Try adjusting your filters",
                                    style = AppTextStyles.body,
                                    color = Tokens.TextMuted
                                )
                            }
                        }
                    }
                }
            } else {
                items(filteredExpenses) { expense ->
                    ExpenseItem(
                        expense = expense,
                        onClick = { onExpenseClick(expense) }
                    )
                }
            }
        }
    }
}
