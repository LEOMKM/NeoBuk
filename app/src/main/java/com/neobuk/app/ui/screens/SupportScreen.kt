package com.neobuk.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neobuk.app.ui.theme.NeoBukTeal
import com.neobuk.app.ui.theme.AppTextStyles

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Support", style = AppTextStyles.pageTitle) },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    "How can we help you?",
                    style = AppTextStyles.sectionTitle,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            item {
                SupportContactCard(
                    title = "WhatsApp Support",
                    subtitle = "Chat with us instantly",
                    icon = Icons.AutoMirrored.Outlined.Chat,
                    color = Color(0xFF25D366),
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/254700000000"))
                        context.startActivity(intent)
                    }
                )
            }

            item {
                SupportContactCard(
                    title = "Phone Call",
                    subtitle = "Speak with our support team",
                    icon = Icons.Outlined.Phone,
                    color = NeoBukTeal,
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+254700000000"))
                        context.startActivity(intent)
                    }
                )
            }

            item {
                SupportContactCard(
                    title = "Email Support",
                    subtitle = "Send us detailed feedback",
                    icon = Icons.Outlined.Email,
                    color = Color(0xFFEA4335),
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:support@neobuk.co.ke")
                            putExtra(Intent.EXTRA_SUBJECT, "NeoBuk Support Case")
                        }
                        context.startActivity(intent)
                    }
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            item {
                Text(
                    "Self Help",
                    style = AppTextStyles.sectionTitle,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            item {
                HelpResourceRow(
                    title = "Video Tutorials",
                    description = "Learn how to use NeoBuk quickly",
                    icon = Icons.Outlined.PlayCircle
                )
            }

            item {
                HelpResourceRow(
                    title = "Community Forum",
                    description = "Talk to other business owners",
                    icon = Icons.Outlined.Groups
                )
            }

            item {
               Card(
                   modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                   colors = CardDefaults.cardColors(containerColor = NeoBukTeal.copy(alpha = 0.05f)),
                   shape = RoundedCornerShape(16.dp)
               ) {
                   Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                       Text("NeoBuk for Businesses", style = AppTextStyles.bodyBold, color = NeoBukTeal)
                       Text(
                           "Our mission is to help real businesses grow by providing daily truth and clarity.",
                           style = AppTextStyles.secondary,
                           color = MaterialTheme.colorScheme.onSurfaceVariant,
                           textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                           modifier = Modifier.padding(top = 8.dp)
                       )
                   }
               }
            }
        }
    }
}

@Composable
fun SupportContactCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
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
                    .size(48.dp)
                    .background(color.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
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

@Composable
fun HelpResourceRow(
    title: String,
    description: String,
    icon: ImageVector
) {
    Surface(
        onClick = { /* Navigate to link */ },
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, null, tint = NeoBukTeal, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, style = AppTextStyles.bodyBold)
                    Text(description, style = AppTextStyles.secondary, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(Icons.Outlined.OpenInNew, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        }
    }
}
