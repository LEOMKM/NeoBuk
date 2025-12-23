package com.neobuk.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
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
import com.neobuk.app.data.models.CommissionType
import com.neobuk.app.data.models.ServiceDefinition
import com.neobuk.app.data.models.ServiceProvider
import com.neobuk.app.data.models.ServiceRecord
import com.neobuk.app.ui.theme.NeoBukTeal
import com.neobuk.app.ui.theme.Tokens
import com.neobuk.app.ui.theme.AppTextStyles
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicesScreen() {
    var showRecordServiceSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    // Mock data - in production, this would come from ViewModel
    val services = remember {
        mutableStateListOf(
            ServiceDefinition("1", "Haircut", 500.0),
            ServiceDefinition("2", "Beard Trim", 200.0),
            ServiceDefinition("3", "Manicure", 800.0),
            ServiceDefinition("4", "Pedicure", 1000.0),
            ServiceDefinition("5", "Full Grooming", 1500.0)
        )
    }
    
    val providers = remember {
        listOf(
            ServiceProvider(id = "1", fullName = "John Mwangi", commissionRate = 30.0),
            ServiceProvider(id = "2", fullName = "Mary Wanjiku", commissionRate = 25.0),
            ServiceProvider(id = "3", fullName = "Peter Ochieng", commissionRate = 35.0)
        )
    }
    
    val serviceHistory = remember {
        mutableStateListOf(
            ServiceRecord.create(services[0], providers[0]),
            ServiceRecord.create(services[1], providers[1]),
            ServiceRecord.create(services[2], providers[2])
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Services",
                style = AppTextStyles.pageTitle,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Record services and view history",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Record Service Button
            Button(
                onClick = { showRecordServiceSheet = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeoBukTeal)
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Record Service", style = AppTextStyles.buttonLarge)
            }
        }

        // Service History
        ServiceHistoryTab(serviceHistory)
    }

    // Record Service Bottom Sheet
    if (showRecordServiceSheet) {
        ModalBottomSheet(
            onDismissRequest = { showRecordServiceSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            RecordServiceSheet(
                services = services.filter { it.isActive },
                providers = providers,
                onSave = { record ->
                    serviceHistory.add(0, record)
                    showRecordServiceSheet = false
                },
                onDismiss = { showRecordServiceSheet = false }
            )
        }
    }
}

@Composable
private fun ServiceHistoryTab(history: List<ServiceRecord>) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }
    
    if (history.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Outlined.History,
                    null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("No services recorded yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(history) { record ->
                ServiceHistoryCard(record, dateFormat)
            }
        }
    }
}

@Composable
private fun ServiceHistoryCard(record: ServiceRecord, dateFormat: SimpleDateFormat) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Top row: Service name + Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    record.serviceName,
                    style = AppTextStyles.body,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "KES ${String.format("%,.0f", record.servicePrice)}",
                    style = AppTextStyles.price,
                    color = NeoBukTeal
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Provider + Date
            Text(
                "${record.serviceProviderName} · ${dateFormat.format(Date(record.dateOffered))}",
                style = AppTextStyles.secondary,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Commission breakdown
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Commission", style = AppTextStyles.caption, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "KES ${String.format("%,.0f", record.commissionAmount)}",
                        style = AppTextStyles.bodyBold,
                        color = Color(0xFFF59E0B)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Business", style = AppTextStyles.caption, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "KES ${String.format("%,.0f", record.businessAmount)}",
                        style = AppTextStyles.bodyBold,
                        color = NeoBukTeal
                    )
                }
            }
        }
    }
}

@Composable
private fun ManageServicesTab(
    services: List<ServiceDefinition>,
    onAddService: () -> Unit,
    onEditService: (ServiceDefinition) -> Unit,
    onToggleActive: (ServiceDefinition) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Add Service Button
        item {
            OutlinedButton(
                onClick = onAddService,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeoBukTeal)
            ) {
                Icon(Icons.Default.Add, null, tint = NeoBukTeal)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Service", style = AppTextStyles.buttonMedium, color = NeoBukTeal)
            }
        }
        
        items(services) { service ->
            ServiceDefinitionCard(
                service = service,
                onEdit = { onEditService(service) },
                onToggleActive = { onToggleActive(service) }
            )
        }
    }
}

@Composable
private fun ServiceDefinitionCard(
    service: ServiceDefinition,
    onEdit: () -> Unit,
    onToggleActive: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (service.isActive) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
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
                        fontWeight = FontWeight.Medium,
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
private fun RecordServiceSheet(
    services: List<ServiceDefinition>,
    providers: List<ServiceProvider>,
    onSave: (ServiceRecord) -> Unit,
    onDismiss: () -> Unit
) {
    var currentStep by remember { mutableIntStateOf(0) }
    var selectedService by remember { mutableStateOf<ServiceDefinition?>(null) }
    var selectedProvider by remember { mutableStateOf<ServiceProvider?>(null) }
    var customPrice by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        // Title
        Text(
            text = when (currentStep) {
                0 -> "Select Service"
                1 -> "Select Provider"
                else -> "Confirm Details"
            },
            style = AppTextStyles.pageTitle
        )
        
        Text(
            text = when (currentStep) {
                0 -> "Choose the service rendered"
                1 -> "Who offered this service?"
                else -> "Review and confirm"
            },
            style = AppTextStyles.secondary,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Step Indicators
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(3) { step ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            if (step <= currentStep) NeoBukTeal else MaterialTheme.colorScheme.surfaceVariant
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Step Content
        when (currentStep) {
            0 -> {
                // Step 1: Select Service
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(services.filter { it.isActive }) { service ->
                        SelectableCard(
                            title = service.name,
                            subtitle = "KES ${String.format("%,.0f", service.basePrice)}",
                            isSelected = selectedService == service,
                            onClick = { selectedService = service }
                        )
                    }
                }
            }
            1 -> {
                // Step 2: Select Provider
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(providers.filter { it.isActive }) { provider ->
                        SelectableCard(
                            title = provider.fullName,
                            subtitle = "${provider.commissionRate.toInt()}% commission",
                            isSelected = selectedProvider == provider,
                            onClick = { selectedProvider = provider }
                        )
                    }
                }
            }
            2 -> {
                // Step 3: Confirm
                val service = selectedService!!
                val provider = selectedProvider!!
                val price = customPrice.toDoubleOrNull() ?: service.basePrice
                val commissionAmount = price * (provider.commissionRate / 100.0)
                val businessAmount = price - commissionAmount

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Summary Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            SummaryRow("Service", service.name)
                            SummaryRow("Provider", provider.fullName)
                            SummaryRow("Price", "KES ${String.format("%,.0f", price)}")
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            SummaryRow(
                                "Commission (${provider.commissionRate.toInt()}%)",
                                "KES ${String.format("%,.0f", commissionAmount)}",
                                valueColor = Color(0xFFF59E0B)
                            )
                            SummaryRow(
                                "Business Amount",
                                "KES ${String.format("%,.0f", businessAmount)}",
                                valueColor = NeoBukTeal
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Navigation Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (currentStep > 0) {
                OutlinedButton(
                    onClick = { currentStep-- },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
                ) {
                    Text("Back", style = AppTextStyles.buttonLarge)
                }
            }

            Button(
                onClick = {
                    when (currentStep) {
                        0 -> if (selectedService != null) currentStep++
                        1 -> if (selectedProvider != null) currentStep++
                        2 -> {
                            val record = ServiceRecord.create(
                                service = selectedService!!,
                                provider = selectedProvider!!,
                                priceOverride = customPrice.toDoubleOrNull()
                            )
                            onSave(record)
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeoBukTeal),
                enabled = when (currentStep) {
                    0 -> selectedService != null
                    1 -> selectedProvider != null
                    else -> true
                }
            ) {
                Text(
                    text = if (currentStep == 2) "Save Record" else "Continue",
                    style = AppTextStyles.buttonLarge
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SelectableCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) NeoBukTeal.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(2.dp, NeoBukTeal)
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(title, style = AppTextStyles.body, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                Text(subtitle, style = AppTextStyles.secondary, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    null,
                    tint = NeoBukTeal,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    valueColor: Color = Color.Unspecified
) {
    val resolvedValueColor = if (valueColor == Color.Unspecified) MaterialTheme.colorScheme.onSurface else valueColor
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = AppTextStyles.body, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = AppTextStyles.bodyBold, color = resolvedValueColor)
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
                Text(
                    text = if (existingService != null) "Update" else "Add Service",
                    style = AppTextStyles.buttonLarge
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
