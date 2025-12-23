package com.neobuk.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neobuk.app.ui.theme.NeoBukTheme

/**
 * Custom NeoBuk Toolbar matching the design specifications.
 * Defaults to white surface with branded logo.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NeoBukToolbar(
    modifier: Modifier = Modifier,
    // Default to the Logo, but allow overriding with text (e.g. "Sales History")
    title: @Composable () -> Unit = { NeoBukLogo() },
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    centerTitle: Boolean = false,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    if (centerTitle) {
        CenterAlignedTopAppBar(
            title = title,
            navigationIcon = navigationIcon,
            actions = actions,
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = containerColor,
                titleContentColor = contentColor,
                scrolledContainerColor = containerColor
            ),
            scrollBehavior = scrollBehavior,
            modifier = modifier
        )
    } else {
        TopAppBar(
            title = title,
            navigationIcon = navigationIcon,
            actions = actions,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = containerColor,
                titleContentColor = contentColor,
                scrolledContainerColor = containerColor
            ),
            scrollBehavior = scrollBehavior,
            modifier = modifier
        )
    }
}

@Composable
fun NeoBukLogo(subtitle: String? = null) {
    if (subtitle != null) {
        androidx.compose.foundation.layout.Column {
            Text(
                text = "NeoBuk",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = FontFamily.Cursive,
                    fontWeight = FontWeight.Bold,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF009688), // NeoBukTeal
                            Color(0xFF26A69A)  // Lighter Teal
                        )
                    ),
                    lineHeight = 28.sp // Tighter line height for stacked text
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        // Cursive styling as requested
        Text(
            text = "NeoBuk",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontFamily = FontFamily.Cursive,
                fontWeight = FontWeight.Bold,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF009688), // NeoBukTeal
                        Color(0xFF26A69A)  // Lighter Teal
                    )
                )
            )
        )
    }
}

/**
 * Standard Home Action Buttons: Notification Bell + Profile Pic
 */
@Composable
fun HomeActions(onAddClick: (() -> Unit)? = null, 
    onNotificationClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    if (onAddClick != null) { 
        IconButton(onClick = onAddClick) { 
            Icon(Icons.Default.Add, "Add", tint = MaterialTheme.colorScheme.primary) 
        } 
    }
    
    IconButton(onClick = onNotificationClick) {
        Icon(
            imageVector = Icons.Outlined.Notifications,
            contentDescription = "Notifications",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    
    IconButton(onClick = onProfileClick) {
        // Profile Avatar with Initials
        Box(
            modifier = Modifier
                .size(36.dp) // Slightly larger touch target
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Text(
                "LM", // Mock Initials (Leonard Mutugi)
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun PreviewNeoBukToolbar() {
    NeoBukTheme {
        NeoBukToolbar(
            actions = { HomeActions() }
        )
    }
}
