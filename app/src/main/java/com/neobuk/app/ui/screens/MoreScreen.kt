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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.neobuk.app.ui.theme.NeoBukTeal
import com.neobuk.app.ui.theme.AppTextStyles
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.res.vectorResource
import androidx.compose.material.icons.automirrored.outlined.Chat

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
        // Header removed (moved to Toolbar)

        // Menu Items
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 1. One-tap Support (Prominent at the top for trust)
            item {
                SupportSection()
                Spacer(modifier = Modifier.height(16.dp))
            }

            items(menuItems.size) { index ->
                MoreMenuItem(item = menuItems[index])
            }
            
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun SupportSection() {
    val context = LocalContext.current
    
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = NeoBukTeal.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Need help? Talk to us",
                style = AppTextStyles.bodyBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "We are available to support your business.",
                style = AppTextStyles.secondary,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SupportAction(
                    icon = Icons.AutoMirrored.Outlined.Chat,
                    label = "WhatsApp",
                    color = Color(0xFF25D366),
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/254700000000"))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.weight(1.3f) // More weight for WhatsApp
                )
                
                SupportAction(
                    icon = Icons.Outlined.Phone,
                    label = "Call",
                    color = NeoBukTeal,
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+254700000000"))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.weight(0.85f)
                )

                SupportAction(
                    icon = Icons.Outlined.Email,
                    label = "Email",
                    color = Color(0xFFEA4335),
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:support@neobuk.co.ke")
                            putExtra(Intent.EXTRA_SUBJECT, "NeoBuk Support Request")
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.weight(0.85f)
                )
            }
        }
    }
}

@Composable
fun SupportAction(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(horizontal = 8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f)),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = color)
    ) {
        Icon(icon, null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
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
