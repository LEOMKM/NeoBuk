package com.neobuk.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.neobuk.app.data.models.CommissionType
import com.neobuk.app.data.models.ServiceDefinition
import com.neobuk.app.data.models.ServiceProvider
import com.neobuk.app.ui.theme.AppTextStyles
import com.neobuk.app.ui.theme.NeoBukTeal
import com.neobuk.app.ui.theme.Tokens
import java.util.*

/**
 * Manage Services Screen - Combined Services and Staff Management
 * Accessible from More → Manage Services
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageServicesScreen(
    onBack: () -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Services", "Staff")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onSurface)
            }
            Text(
                "Manage Services",
                style = AppTextStyles.pageTitle,
                modifier = Modifier.weight(1f)
            )
        }

        // Tabs
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = NeoBukTeal,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = NeoBukTeal
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            title,
                            style = if (selectedTabIndex == index) AppTextStyles.bodyBold else AppTextStyles.body,
                            color = if (selectedTabIndex == index) NeoBukTeal else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }
        }

        // Content
        when (selectedTabIndex) {
            0 -> ServicesContent()
            1 -> StaffContent()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicesContent() {
    var showAddSheet by remember { mutableStateOf(false) }
    var editingService by remember { mutableStateOf<ServiceDefinition?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Mock data
    val services = remember {
        mutableStateListOf(
            ServiceDefinition("1", "Haircut", 500.0),
            ServiceDefinition("2", "Beard Trim", 200.0),
            ServiceDefinition("3", "Manicure", 800.0),
            ServiceDefinition("4", "Pedicure", 1000.0),
            ServiceDefinition("5", "Full Grooming", 1500.0)
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Summary Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SummaryItem(
                        value = services.count { it.isActive }.toString(),
                        label = "Active",
                        color = NeoBukTeal
                    )
                    SummaryItem(
                        value = services.count { !it.isActive }.toString(),
                        label = "Inactive",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    SummaryItem(
                        value = services.size.toString(),
                        label = "Total",
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Services List
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f) // Ensure it takes available space
            ) {
                items(services) { service ->
                    ServiceCard(
                        service = service,
                        onEdit = { editingService = service },
                        onToggleActive = {
                            val index = services.indexOf(service)
                            if (index >= 0) {
                                services[index] = service.copy(isActive = !service.isActive)
                            }
                        }
                    )
                }
                item { Spacer(modifier = Modifier.height(80.dp)) } // Bottom padding for FAB
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = { showAddSheet = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = NeoBukTeal,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, "Add Service")
        }
    }

    // Add/Edit Sheet
    if (showAddSheet || editingService != null) {
        ModalBottomSheet(
            onDismissRequest = {
                showAddSheet = false
                editingService = null
            },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            AddEditServiceSheet(
                existingService = editingService,
                onSave = { service ->
                    if (editingService != null) {
                        val index = services.indexOfFirst { it.id == service.id }
                        if (index >= 0) {
                            services[index] = service
                        }
                    } else {
                        services.add(0, service)
                    }
                    showAddSheet = false
                    editingService = null
                },
                onDismiss = {
                    showAddSheet = false
                    editingService = null
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffContent() {
    var showAddSheet by remember { mutableStateOf(false) }
    var editingProvider by remember { mutableStateOf<ServiceProvider?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Mock data
    val providers = remember {
        mutableStateListOf(
            ServiceProvider(id = "1", fullName = "John Mwangi", role = "Barber", commissionRate = 30.0),
            ServiceProvider(id = "2", fullName = "Mary Wanjiku", role = "Stylist", commissionRate = 25.0),
            ServiceProvider(id = "3", fullName = "Peter Ochieng", role = "Barber", commissionRate = 35.0),
            ServiceProvider(id = "4", fullName = "Grace Akinyi", role = "Manicurist", commissionRate = 40.0, isActive = false)
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Summary Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SummaryItem(
                        value = providers.count { it.isActive }.toString(),
                        label = "Active",
                        color = NeoBukTeal
                    )
                    SummaryItem(
                        value = providers.count { !it.isActive }.toString(),
                        label = "Inactive",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    SummaryItem(
                        value = providers.size.toString(),
                        label = "Total",
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Staff List
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(providers) { provider ->
                    StaffCard(
                        provider = provider,
                        onEdit = { editingProvider = provider },
                        onToggleActive = {
                            val index = providers.indexOf(provider)
                            if (index >= 0) {
                                providers[index] = provider.copy(isActive = !provider.isActive)
                            }
                        }
                    )
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = { showAddSheet = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = NeoBukTeal,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, "Add Staff")
        }
    }

    // Add/Edit Sheet
    if (showAddSheet || editingProvider != null) {
        ModalBottomSheet(
            onDismissRequest = {
                showAddSheet = false
                editingProvider = null
            },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            AddEditStaffSheet(
                existingProvider = editingProvider,
                onSave = { provider ->
                    if (editingProvider != null) {
                        val index = providers.indexOfFirst { it.id == provider.id }
                        if (index >= 0) {
                            providers[index] = provider
                        }
                    } else {
                        providers.add(0, provider)
                    }
                    showAddSheet = false
                    editingProvider = null
                },
                onDismiss = {
                    showAddSheet = false
                    editingProvider = null
                }
            )
        }
    }
}

@Composable
private fun SummaryItem(
    value: String,
    label: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = AppTextStyles.amountLarge,
            color = color
        )
        Text(
            label,
            style = AppTextStyles.secondary,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ServiceCard(
    service: ServiceDefinition,
    onEdit: () -> Unit,
    onToggleActive: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (service.isActive) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        service.name,
                        style = AppTextStyles.body,
                        fontWeight = FontWeight.SemiBold,
                        color = if (service.isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (!service.isActive) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Inactive",
                            style = AppTextStyles.caption,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    "KES ${String.format("%,.0f", service.basePrice)}",
                    style = AppTextStyles.secondary,
                    color = if (service.isActive) NeoBukTeal else MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (service.commissionOverride != null) {
                    Text(
                        "${service.commissionOverride.toInt()}% commission override",
                        style = AppTextStyles.caption,
                        color = Color(0xFFF59E0B)
                    )
                }
            }

            IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Outlined.Edit,
                    "Edit",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            Switch(
                checked = service.isActive,
                onCheckedChange = { onToggleActive() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = NeoBukTeal
                )
            )
        }
    }
}

@Composable
private fun StaffCard(
    provider: ServiceProvider,
    onEdit: () -> Unit,
    onToggleActive: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (provider.isActive) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (provider.isActive) NeoBukTeal.copy(alpha = 0.1f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    provider.fullName.split(" ").take(2).map { it.first() }.joinToString(""),
                    style = AppTextStyles.bodyBold,
                    color = if (provider.isActive) NeoBukTeal else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        provider.fullName,
                        style = AppTextStyles.body,
                        fontWeight = FontWeight.SemiBold,
                        color = if (provider.isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (!provider.isActive) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Inactive",
                            style = AppTextStyles.caption,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    provider.role,
                    style = AppTextStyles.secondary,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "${provider.commissionRate.toInt()}% commission",
                    style = AppTextStyles.secondary,
                    color = if (provider.isActive) NeoBukTeal else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Actions
            IconButton(onClick = onEdit) {
                Icon(
                    Icons.Default.Edit,
                    "Edit",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            Switch(
                checked = provider.isActive,
                onCheckedChange = { onToggleActive() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = NeoBukTeal
                )
            )
        }
    }
}

@Composable
private fun AddEditServiceSheet(
    existingService: ServiceDefinition?,
    onSave: (ServiceDefinition) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(existingService?.name ?: "") }
    var basePrice by remember { mutableStateOf(existingService?.basePrice?.toString() ?: "") }
    var commissionOverride by remember { mutableStateOf(existingService?.commissionOverride?.toString() ?: "") }

    val isValid = name.isNotBlank() && basePrice.toDoubleOrNull() != null

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            if (existingService != null) "Edit Service" else "Add Service",
            style = AppTextStyles.pageTitle
        )
        Text(
            "Enter service details and pricing",
            style = AppTextStyles.secondary,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Service Name
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Service Name") },
            placeholder = { Text("e.g. Haircut, Manicure") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeoBukTeal,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Base Price
        OutlinedTextField(
            value = basePrice,
            onValueChange = { basePrice = it },
            label = { Text("Base Price (KES)") },
            placeholder = { Text("e.g. 500") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            leadingIcon = { Text("KES", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeoBukTeal,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Commission Override (optional)
        OutlinedTextField(
            value = commissionOverride,
            onValueChange = { commissionOverride = it },
            label = { Text("Commission Override % (optional)") },
            placeholder = { Text("Leave empty to use provider's rate") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            trailingIcon = { Text("%", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeoBukTeal,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Commission explanation
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.Info,
                    null,
                    tint = NeoBukTeal,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "If set, this rate overrides the provider's default commission for this service.",
                    style = AppTextStyles.secondary,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cancel", style = AppTextStyles.buttonLarge)
            }

            Button(
                onClick = {
                    val service = ServiceDefinition(
                        id = existingService?.id ?: UUID.randomUUID().toString(),
                        name = name.trim(),
                        basePrice = basePrice.toDoubleOrNull() ?: 0.0,
                        commissionOverride = commissionOverride.toDoubleOrNull(),
                        isActive = existingService?.isActive ?: true
                    )
                    onSave(service)
                },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeoBukTeal),
                enabled = isValid
            ) {
                Text(if (existingService != null) "Update" else "Add Service", style = AppTextStyles.buttonLarge)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun AddEditStaffSheet(
    existingProvider: ServiceProvider?,
    onSave: (ServiceProvider) -> Unit,
    onDismiss: () -> Unit
) {
    var fullName by remember { mutableStateOf(existingProvider?.fullName ?: "") }
    var role by remember { mutableStateOf(existingProvider?.role ?: "Service Provider") }
    var commissionRate by remember { mutableStateOf(existingProvider?.commissionRate?.toString() ?: "30") }
    // var commissionType by remember { mutableStateOf(existingProvider?.commissionType ?: CommissionType.PERCENTAGE) } // Assuming Percentage default

    val isValid = fullName.isNotBlank() && commissionRate.toDoubleOrNull() != null

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            if (existingProvider != null) "Edit Staff Member" else "Add Staff Member",
            style = AppTextStyles.pageTitle
        )
        Text(
            "Enter staff details and commission settings",
            style = AppTextStyles.secondary,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Full Name
        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Full Name") },
            placeholder = { Text("e.g. John Mwangi") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeoBukTeal,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Role
        OutlinedTextField(
            value = role,
            onValueChange = { role = it },
            label = { Text("Role") },
            placeholder = { Text("e.g. Barber, Stylist") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeoBukTeal,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Commission Rate
        OutlinedTextField(
            value = commissionRate,
            onValueChange = { commissionRate = it },
            label = { Text("Commission Rate (%)") },
            placeholder = { Text("e.g. 30") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            trailingIcon = { Text("%", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeoBukTeal,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Commission explanation
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.Info,
                    null,
                    tint = NeoBukTeal,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "This is the percentage of service price the staff member earns.",
                    style = AppTextStyles.secondary,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cancel", style = AppTextStyles.buttonLarge)
            }

            Button(
                onClick = {
                    val provider = ServiceProvider(
                        id = existingProvider?.id ?: UUID.randomUUID().toString(),
                        fullName = fullName.trim(),
                        role = role.trim(),
                        commissionType = CommissionType.PERCENTAGE, // Defaulting as simplified
                        commissionRate = commissionRate.toDoubleOrNull() ?: 0.0,
                        isActive = existingProvider?.isActive ?: true
                    )
                    onSave(provider)
                },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeoBukTeal),
                enabled = isValid
            ) {
                Text(if (existingProvider != null) "Update" else "Add Staff", style = AppTextStyles.buttonLarge)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
