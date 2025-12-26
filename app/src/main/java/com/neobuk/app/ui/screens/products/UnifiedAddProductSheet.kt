package com.neobuk.app.ui.screens.products

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import com.neobuk.app.ui.theme.NeoBukTeal
import com.neobuk.app.ui.theme.AppTextStyles

@Composable
fun UnifiedAddProductSheet(
    onDismiss: () -> Unit,
    onSubmit: (ProductDraft) -> Unit,
    checkProductExists: (String) -> Boolean = { false }, // Optional: separate check?
    onProductExists: (String) -> Unit = {} // If scanned product exists
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var scannedBarcode by remember { mutableStateOf<String?>(null) }
    
    // Wrapper Column
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.95f) // Take up most of screen height
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // 1. Common Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Add Product",
                style = AppTextStyles.pageTitle,
                color = MaterialTheme.colorScheme.onSurface
            )
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }
        
        // 2. Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = NeoBukTeal,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = NeoBukTeal
                )
            },
            divider = { HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant) }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Scan Product") },
                icon = { Icon(Icons.Default.QrCodeScanner, null) },
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Manual Entry") },
                icon = { Icon(Icons.Default.Edit, null) }, // Or explicit edit icon
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // 3. Content
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> {
                    ScanStockContent(
                        onBarcodeScanned = { barcode ->
                            if (checkProductExists(barcode)) {
                                onProductExists(barcode)
                            } else {
                                scannedBarcode = barcode
                                selectedTab = 1 // Switch to Manual
                            }
                        },
                        onManualEntry = { selectedTab = 1 }, // Switch to manual
                        onDismiss = onDismiss, // Won't be clicked as header is hidden
                        showHeader = false,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                1 -> {
                    AddProductContent(
                        initialBarcode = scannedBarcode,
                        onDismiss = onDismiss,
                        onSubmit = onSubmit,
                        showHeader = false,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 0.dp) // Reset any padding if needed
                    )
                }
            }
        }
    }
}
