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
import java.util.UUID

// Data Classes
data class Expense(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val category: ExpenseCategory,
    val amount: Double,
    val date: String,
    val description: String = "",
    val paymentMethod: String = "Cash",
    val receiptUrl: String? = null
)

enum class ExpenseCategory(val displayName: String, val icon: ImageVector, val color: Color) {
    UTILITIES("Utilities", Icons.Outlined.Bolt, Color(0xFFF59E0B)),
    RENT("Rent", Icons.Outlined.Home, Color(0xFF6366F1)),
    SUPPLIES("Supplies", Icons.Outlined.ShoppingCart, Color(0xFF10B981)),
    TRANSPORT("Transport", Icons.Outlined.LocalShipping, Color(0xFF3B82F6)),
    SALARY("Salary", Icons.Outlined.People, Color(0xFFEC4899)),
    MAINTENANCE("Maintenance", Icons.Outlined.Build, Color(0xFF8B5CF6)),
    OTHER("Other", Icons.Outlined.MoreHoriz, Color(0xFF6B7280))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen() {
    var showAddExpenseSheet by remember { mutableStateOf(false) }
    var selectedExpense by remember { mutableStateOf<Expense?>(null) }
    var selectedFilter by remember { mutableStateOf("All") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Mock Data
    val expenses = remember {
        listOf(
            Expense(
                title = "Electricity Bill",
                category = ExpenseCategory.UTILITIES,
                amount = 4500.0,
                date = "Dec 20, 2024",
                description = "Monthly KPLC bill",
                paymentMethod = "M-PESA"
            ),
            Expense(
                title = "Shop Rent",
                category = ExpenseCategory.RENT,
                amount = 25000.0,
                date = "Dec 15, 2024",
                description = "December rent payment",
                paymentMethod = "Bank Transfer"
            ),
            Expense(
                title = "Cleaning Supplies",
                category = ExpenseCategory.SUPPLIES,
                amount = 1200.0,
                date = "Dec 18, 2024",
                description = "Detergent, mops, brushes",
                paymentMethod = "Cash"
            ),
            Expense(
                title = "Delivery Fuel",
                category = ExpenseCategory.TRANSPORT,
                amount = 2500.0,
                date = "Dec 19, 2024",
                description = "Bike fuel for deliveries",
                paymentMethod = "M-PESA"
            ),
            Expense(
                title = "Staff Salary - John",
                category = ExpenseCategory.SALARY,
                amount = 15000.0,
                date = "Dec 14, 2024",
                description = "December salary payment",
                paymentMethod = "Bank Transfer"
            ),
            Expense(
                title = "Refrigerator Repair",
                category = ExpenseCategory.MAINTENANCE,
                amount = 3500.0,
                date = "Dec 12, 2024",
                description = "Compressor replacement",
                paymentMethod = "Cash"
            )
        )
    }

    // Calculate stats
    val totalExpenses = expenses.sumOf { it.amount }
    val thisMonthExpenses = expenses.sumOf { it.amount } // Simplified for mock
    val categoryBreakdown = expenses.groupBy { it.category }
        .mapValues { it.value.sumOf { exp -> exp.amount } }
        .toList()
        .sortedByDescending { it.second }

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
                ExpenseCategory.values().forEach { category ->
                    item {
                        ExpenseFilterChip(
                            text = category.displayName,
                            selected = selectedFilter == category.displayName,
                            onClick = { selectedFilter = category.displayName }
                        )
                    }
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
                TextButton(onClick = { /* View all expenses */ }) {
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
            expenses.filter { it.category.displayName == selectedFilter }
        }

        items(filteredExpenses) { expense ->
            ExpenseItem(
                expense = expense,
                onClick = { selectedExpense = expense }
            )
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
                onSubmit = { /* Handle expense submission */ showAddExpenseSheet = false }
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
    category: ExpenseCategory,
    amount: Double,
    percentage: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(category.color.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    category.icon,
                    contentDescription = null,
                    tint = category.color,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = category.displayName,
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
                        .background(expense.category.color.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        expense.category.icon,
                        contentDescription = null,
                        tint = expense.category.color,
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
                        text = "${expense.category.displayName} • ${expense.date}",
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
    onSubmit: (Expense) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ExpenseCategory.OTHER) }
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
            items(ExpenseCategory.values().toList()) { category ->
                CategoryChip(
                    category = category,
                    selected = selectedCategory == category,
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
                val expense = Expense(
                    title = title,
                    category = selectedCategory,
                    amount = amount.toDoubleOrNull() ?: 0.0,
                    date = "Dec 23, 2024",
                    description = description,
                    paymentMethod = selectedPaymentMethod
                )
                onSubmit(expense)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NeoBukTeal),
            enabled = title.isNotBlank() && amount.isNotBlank()
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
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (selected) category.color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
        border = if (selected) androidx.compose.foundation.BorderStroke(1.dp, category.color) else null,
        modifier = Modifier
            .height(40.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                category.icon,
                contentDescription = null,
                tint = if (selected) category.color else Tokens.TextMuted,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = category.displayName,
                style = AppTextStyles.labelLarge,
                color = if (selected) category.color else MaterialTheme.colorScheme.onSurface
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
        DetailRow("Category", expense.category.displayName)
        DetailRow("Date", expense.date)
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
