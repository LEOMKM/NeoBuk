package com.neobuk.app.ui.screens.products

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neobuk.app.ui.theme.NeoBukCyan
import com.neobuk.app.ui.theme.NeoBukTeal

@Composable
fun ScanStockSheet(
    onBarcodeScanned: (String) -> Unit,
    onManualEntry: () -> Unit,
    onDismiss: () -> Unit
) {
    var manualBarcode by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurface)
            }
            Text(
                "Scan Product",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            IconButton(onClick = { /* Toggle Flash */ }) {
                Icon(Icons.Default.FlashOn, contentDescription = "Flash", tint = MaterialTheme.colorScheme.onSurface)
            }
        }

        // Camera Preview / Scanner Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Black) // Placeholder for Camera
        ) {
            // Mock Camera UI
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(280.dp)
                    .drawBehind { // Scan Frame
                        val stroke = Stroke(
                            width = 4.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(40f, 20f), 0f)
                        )
                        drawRoundRect(
                            color = NeoBukCyan,
                            cornerRadius = CornerRadius(16.dp.toPx()),
                            style = stroke
                        )
                    }
            )

            // Simulate Scan Button (Temporary)
            Button(
                onClick = { onBarcodeScanned("5012345678900") }, // Matches mock data
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeoBukTeal)
            ) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Simulate Scan (Found)")
            }
            
            Button(
                 onClick = { onBarcodeScanned("9999999999999") }, // New Product
                 modifier = Modifier.align(Alignment.TopCenter).padding(top = 32.dp),
                 colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f))
             ) {
                 Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                 Spacer(modifier = Modifier.width(8.dp))
                 Text("Simulate Scan (New)")
             }
        }

        // Manual Entry
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Or enter barcode manually",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = manualBarcode,
                    onValueChange = { manualBarcode = it },
                    placeholder = { Text("Enter barcode number") },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (manualBarcode.isNotEmpty()) {
                                onBarcodeScanned(manualBarcode)
                                focusManager.clearFocus()
                            }
                        }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedBorderColor = NeoBukTeal,
                        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Button(
                    onClick = {
                        if (manualBarcode.isNotEmpty()) {
                            onBarcodeScanned(manualBarcode)
                        } else {
                            onManualEntry()
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeoBukTeal),
                    modifier = Modifier.height(56.dp)
                ) {
                    Icon(
                        if (manualBarcode.isNotEmpty()) Icons.Default.QrCodeScanner else Icons.Default.Archive,
                        contentDescription = null
                    )
                }
            }
            
            TextButton(onClick = onManualEntry, modifier = Modifier.padding(top = 8.dp)) {
                Text("Detailed Manual Add", color = NeoBukTeal)
            }
        }
    }
}
