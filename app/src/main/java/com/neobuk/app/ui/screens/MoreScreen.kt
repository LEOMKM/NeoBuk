package com.neobuk.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.neobuk.app.ui.theme.NeoBukTeal
import com.neobuk.app.ui.theme.AppTextStyles

data class MenuItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val onClick: () -> Unit = {}
)

@Composable
fun MoreScreen(
    businessType: com.neobuk.app.BusinessType = com.neobuk.app.BusinessType.RETAIL,
    onNavigateToManageServices: () -> Unit = {},
    onNavigateToSubscription: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToSupport: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {}
) {
    val menuItems = remember(
        businessType,
        onNavigateToManageServices, onNavigateToSubscription, onNavigateToSettings, onNavigateToSupport, onNavigateToAbout
    ) {
        listOf(
            MenuItem("Subscription", "Manage plan and payments", Icons.Outlined.CreditCard, onNavigateToSubscription),
            MenuItem("Manage Services", "Services, Staff & Roles", Icons.Outlined.Build, onNavigateToManageServices),
            MenuItem("Settings", "App preferences and configuration", Icons.Outlined.Settings, onNavigateToSettings),
            MenuItem("Support", "Help center and FAQs", Icons.Outlined.HelpOutline, onNavigateToSupport),
            MenuItem("Legal / About", "Terms, privacy and app info", Icons.Outlined.Info, onNavigateToAbout)
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
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                text = "More",
                style = AppTextStyles.pageTitle,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Additional features and settings",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Menu Items
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(menuItems.size) { index ->
                MoreMenuItem(item = menuItems[index])
            }
        }
    }
}

@Composable
private fun MoreMenuItem(item: MenuItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { item.onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(NeoBukTeal.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    item.icon,
                    contentDescription = null,
                    tint = NeoBukTeal,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Text
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.title,
                    style = AppTextStyles.body,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    item.subtitle,
                    style = AppTextStyles.secondary,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Arrow
            Icon(
                Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
