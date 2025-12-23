package com.neobuk.app.ui.screens.products

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neobuk.app.data.models.Product
import com.neobuk.app.ui.theme.NeoBukTeal

@Composable
fun UpdateStockSheet(
    product: Product,
    onConfirm: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var quantityToAdd by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(bottom = 32.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Update Stock",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onDismiss) {
                // Icon(Icons.Default.Close, contentDescription = "Close") // Add import if needed
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        // Product Summary
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(product.name, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Current Stock: ${product.quantity} ${product.unit}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("|", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Barcode: ${product.barcode}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text("Quantity to Add", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = quantityToAdd,
            onValueChange = { quantityToAdd = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("e.g. 10") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedBorderColor = NeoBukTeal
            )
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = {
                val qty = quantityToAdd.toDoubleOrNull()
                if (qty != null && qty > 0) {
                    onConfirm(qty)
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NeoBukTeal),
            enabled = quantityToAdd.toDoubleOrNull() != null
        ) {
            Text("Add Stock", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}
