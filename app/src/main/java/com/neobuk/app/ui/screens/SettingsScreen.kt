package com.neobuk.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.neobuk.app.ui.theme.NeoBukTeal
import com.neobuk.app.ui.theme.AppTextStyles

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit
) {
    var pushNotifications by remember { mutableStateOf(true) }
    var syncInProgress by remember { mutableStateOf(false) }
    var darkMode by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", style = AppTextStyles.pageTitle) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    "Preferences",
                    style = AppTextStyles.sectionTitle,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            item {
                SettingsSwitchRow(
                    title = "Push Notifications",
                    subtitle = "Get alerts for sales and low stock",
                    icon = Icons.Outlined.Notifications,
                    checked = pushNotifications,
                    onCheckedChange = { pushNotifications = it }
                )
            }

            item {
                SettingsSwitchRow(
                    title = "Dark Mode",
                    subtitle = "Switch between light and dark theme",
                    icon = Icons.Outlined.DarkMode,
                    checked = darkMode,
                    onCheckedChange = { darkMode = it }
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            item {
                Text(
                    "Account & Business",
                    style = AppTextStyles.sectionTitle,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            item {
                SettingsClickableRow(
                    title = "Business Profile",
                    subtitle = "Update your business details",
                    icon = Icons.Outlined.Business,
                    onClick = { /* Navigate to profile */ }
                )
            }

            item {
                SettingsClickableRow(
                    title = "Cloud Sync",
                    subtitle = "Manually trigger background sync",
                    icon = Icons.Outlined.Sync,
                    onClick = { syncInProgress = true }
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            item {
                Text(
                    "Danger Zone",
                    style = AppTextStyles.sectionTitle,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            item {
                SettingsClickableRow(
                    title = "Reset App Data",
                    subtitle = "Clear all local data and re-sync",
                    icon = Icons.Outlined.DeleteForever,
                    color = MaterialTheme.colorScheme.error,
                    onClick = { /* Show confirmation dialog */ }
                )
            }
        }
    }
}

@Composable
fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(NeoBukTeal.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = NeoBukTeal, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = AppTextStyles.bodyBold)
                Text(subtitle, style = AppTextStyles.secondary, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = NeoBukTeal
                )
            )
        }
    }
}

@Composable
fun SettingsClickableRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    color: androidx.compose.ui.graphics.Color = NeoBukTeal
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = AppTextStyles.bodyBold)
                Text(subtitle, style = AppTextStyles.secondary, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Outlined.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
