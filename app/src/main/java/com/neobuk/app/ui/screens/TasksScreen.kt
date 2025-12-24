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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import java.util.Calendar
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neobuk.app.data.models.Task
import com.neobuk.app.data.models.TaskStatus
import com.neobuk.app.ui.theme.AppTextStyles
import com.neobuk.app.ui.theme.NeoBukCyan
import com.neobuk.app.ui.theme.NeoBukTeal
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(onBack: () -> Unit) {
    var selectedStatus by remember { mutableStateOf<TaskStatus?>(null) } // null = All
    val today = remember { Date() }

    // Mock Data
    val tasks = remember {
        mutableStateListOf(
            Task(title = "Follow up unpaid invoice", dueDate = today.time, status = TaskStatus.TODO, relatedLink = "Invoice #1023"),
            Task(title = "Restock maize flour (5 units left)", dueDate = today.time, status = TaskStatus.TODO, relatedLink = "Stock Alert"),
            Task(title = "Call supplier (Juma)", dueDate = today.time + 86400000, status = TaskStatus.IN_PROGRESS, relatedLink = "Supplier"),
            Task(title = "Pay electricity bill", dueDate = today.time - 86400000, status = TaskStatus.DONE)
        )
    }

    val filteredTasks = tasks.filter {
        selectedStatus == null || it.status == selectedStatus
    }
    var showAddDialog by remember { mutableStateOf(false) } // Ensure this is inside TasksScreen but outside Scaffold if possible or at top level. Moved it up in previous edits, checking here.
    // Actually, showAddDialog is properly declared at line 65 in the file view.
    // So we just need to add the Dialog call inside the content block.

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                 // Custom Top Bar (Removed to avoid duplication with MainActivity Toolbar)
                 
                 // Date Strip (Simple Mock)
                 DateStrip()
                 
                 // Status Filters
                 Row(
                     modifier = Modifier
                         .fillMaxWidth()
                         .padding(horizontal = 16.dp, vertical = 8.dp),
                     horizontalArrangement = Arrangement.spacedBy(8.dp)
                 ) {
                     FilterChip(
                         selected = selectedStatus == null,
                         onClick = { selectedStatus = null },
                         label = { Text("All") }
                     )
                     FilterChip(
                         selected = selectedStatus == TaskStatus.TODO,
                         onClick = { selectedStatus = TaskStatus.TODO },
                         label = { Text("To-do") }
                     )
                     FilterChip(
                         selected = selectedStatus == TaskStatus.IN_PROGRESS,
                         onClick = { selectedStatus = TaskStatus.IN_PROGRESS },
                         label = { Text("In Progress") }
                     )
                     FilterChip(
                         selected = selectedStatus == TaskStatus.DONE,
                         onClick = { selectedStatus = TaskStatus.DONE },
                         label = { Text("Done") }
                     )
                 }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = NeoBukTeal,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, "Add Task")
            }
        }
    ) { padding ->
        if (showAddDialog) {
            AddTaskDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { title, dueDateMillis, link ->
                    tasks.add(
                        Task(
                            title = title,
                            dueDate = dueDateMillis,
                            status = TaskStatus.TODO,
                            relatedLink = if (link.isBlank()) null else link
                        )
                    )
                    showAddDialog = false
                }
            )
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Group By Day Logic (still simple)
            val grouped = filteredTasks.groupBy {
                val fmt = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
                fmt.format(Date(it.dueDate ?: 0))
            }

            if (filteredTasks.isEmpty()) {
                item {
                    EmptyState()
                }
            } else {
                item {
                    Text("Today", style = AppTextStyles.sectionTitle, modifier = Modifier.padding(bottom = 8.dp))
                }
                items(filteredTasks) { task ->
                    TaskCard(
                        task = task,
                        onStatusChange = { newStatus ->
                            val index = tasks.indexOf(task)
                            if (index != -1) {
                                tasks[index] = tasks[index].copy(status = newStatus)
                            }
                        },
                        onSnooze = { t, newDue ->
                            val idx = tasks.indexOf(t)
                            if (idx != -1) {
                                tasks[idx] = tasks[idx].copy(dueDate = newDue)
                            }
                        },
                        onDelete = { t ->
                            tasks.remove(t)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DateStrip() {
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    val dates = listOf("11", "12", "13", "14", "15", "16")
    val selectedIndex = 3 // Thu 14

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(days.size) { index ->
            val isSelected = index == selectedIndex
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) NeoBukTeal else Color.Transparent)
                    .padding(vertical = 12.dp, horizontal = 16.dp)
            ) {
                Text(
                    text = days[index],
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = dates[index],
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                )
                if (isSelected) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(modifier = Modifier.size(4.dp).background(Color.White, CircleShape))
                }
            }
        }
    }
}

@Composable
fun TaskCard(
    task: Task,
    onStatusChange: (TaskStatus) -> Unit,
    onSnooze: (Task, Long) -> Unit,
    onDelete: (Task) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left side: Checkbox and content
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.weight(1f)
            ) {
                // Checkbox Area
                IconButton(
                    onClick = {
                        val nextStatus = if (task.status == TaskStatus.DONE) TaskStatus.TODO else TaskStatus.DONE
                        onStatusChange(nextStatus)
                    },
                    modifier = Modifier.size(24.dp).offset(y = 2.dp)
                ) {
                    if (task.status == TaskStatus.DONE) {
                        Icon(Icons.Outlined.CheckCircle, null, tint = NeoBukTeal)
                    } else {
                        Icon(Icons.Outlined.Circle, null, tint = MaterialTheme.colorScheme.outline)
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = task.title,
                        style = AppTextStyles.bodyBold,
                        textDecoration = if (task.status == TaskStatus.DONE) TextDecoration.LineThrough else null,
                        color = if (task.status == TaskStatus.DONE) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                    )
                    if (task.relatedLink != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Link, null, modifier = Modifier.size(12.dp), tint = NeoBukCyan)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = task.relatedLink,
                                style = MaterialTheme.typography.bodySmall,
                                color = NeoBukCyan
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row {
                        // Time pill
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "10:00 AM",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (task.status == TaskStatus.IN_PROGRESS) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = Color(0xFFFFF3E0),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    "In Progress",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    color = Color(0xFFF57C00)
                                )
                            }
                        }
                    }
                }
            }
            // Right side: More menu
            IconButton(onClick = { menuExpanded = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "More")
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {

                if (task.status != TaskStatus.IN_PROGRESS && task.status != TaskStatus.DONE) {
                    DropdownMenuItem(
                        text = { Text("Mark as In Progress") },
                        onClick = {
                            menuExpanded = false
                            onStatusChange(TaskStatus.IN_PROGRESS)
                        }
                    )
                }
                if (task.status != TaskStatus.DONE) {
                    DropdownMenuItem(
                        text = { Text("Mark as Done") },
                        onClick = {
                            menuExpanded = false
                            onStatusChange(TaskStatus.DONE)
                        }
                    )
                }
                if (task.status == TaskStatus.DONE || task.status == TaskStatus.IN_PROGRESS) {
                    DropdownMenuItem(
                        text = { Text("Mark as To-do") },
                        onClick = {
                            menuExpanded = false
                            onStatusChange(TaskStatus.TODO)
                        }
                    )
                }
                // Hidden Delete option (still accessible via menu)
                DropdownMenuItem(
                    text = { Text("Delete") },
                    onClick = {
                        menuExpanded = false
                        onDelete(task)
                    }
                )
            }
        }
    }
}
@Composable
fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(48.dp)
        )
        Text(
            text = "Nothing to do yet",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Add reminders for follow‑ups, restocking, or payments so nothing slips through.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onAdd: (title: String, dueDate: Long?, link: String) -> Unit
) {
    var titleState by remember { mutableStateOf("") }
    var linkState by remember { mutableStateOf("") }
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDateMillis ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedDateMillis = datePickerState.selectedDateMillis
                        showDatePicker = false
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Task") },
        text = {
            Column {
                TextField(
                    value = titleState,
                    onValueChange = { titleState = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                TextField(
                    value = linkState,
                    onValueChange = { linkState = it },
                    label = { Text("Details / Link (anything helpful)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                // Date Selection Button
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(4.dp) // Match TextField shape roughly
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (selectedDateMillis != null) {
                            SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(Date(selectedDateMillis!!))
                        } else {
                            "Select Due Date (Optional)"
                        },
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onAdd(titleState, selectedDateMillis, linkState)
            }) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
