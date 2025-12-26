package com.neobuk.app.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neobuk.app.ui.theme.NeoBukTeal
import com.neobuk.app.ui.theme.AppTextStyles

data class FAQItem(
    val question: String,
    val answer: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onBack: () -> Unit
) {
    val faqs = remember {
        listOf(
            FAQItem(
                "Do I need internet to use NeoBuk?",
                "No.\nNeoBuk works even when there’s no internet.\n\nYou can record sales, services, expenses, and close your day completely offline. When internet comes back, NeoBuk syncs quietly in the background.\n\nYour business shouldn’t stop because the network did."
            ),
            FAQItem(
                "Is NeoBuk accounting software?",
                "No — and that’s the point.\n\nNeoBuk doesn’t ask you to understand accounting terms or reports. You just record what you sell and what you spend.\n\nNeoBuk tells you:\n• How much you made\n• How much you spent\n• Whether you made a profit today\n\nNothing more. Nothing confusing."
            ),
            FAQItem(
                "How is NeoBuk different from apps like Bumpa or Navus?",
                "Those apps focus on selling online or formal accounting.\n\nNeoBuk focuses on daily truth.\n\nIt answers questions like:\n“Did I actually make money today?”\n“Where did my cash go?”\n“Why does my money not match my sales?”\n\nIf you run a real, day-to-day business — NeoBuk fits."
            ),
            FAQItem(
                "What if I forget to record some sales or expenses?",
                "NeoBuk doesn’t punish you.\n\nAt day close, it helps you compare:\n• What you recorded\n• What you actually have in cash\n\nIf there’s a difference, NeoBuk shows it calmly and lets you add a note.\nThe goal is clarity, not perfection."
            ),
            FAQItem(
                "Is my data safe if I lose my phone?",
                "Yes.\n\nYour data is saved on your phone and backed up securely when you’re online. If you change or lose your phone, you can restore your business and continue where you left off.\n\nNo notebooks lost.\nNo guessing from memory."
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Legal / About", style = AppTextStyles.pageTitle) },
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
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "NeoBuk",
                        style = AppTextStyles.pageTitle.copy(fontSize = 24.sp, color = NeoBukTeal),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Version 1.0.0",
                        style = AppTextStyles.secondary,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "NeoBuk is built for real businesses, not perfect ones.",
                        style = AppTextStyles.bodyBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }

            item {
                Text(
                    "Frequently Asked Questions",
                    style = AppTextStyles.sectionTitle,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            items(faqs) { faq ->
                FAQCard(faq)
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "Legal Information",
                    style = AppTextStyles.sectionTitle,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                LegalRow("Terms of Service")
                LegalRow("Privacy Policy")
                LegalRow("Open Source Licenses")
            }
        }
    }
}

@Composable
fun FAQCard(faq: FAQItem) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    faq.question,
                    style = AppTextStyles.bodyBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    faq.answer,
                    style = AppTextStyles.body,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun LegalRow(title: String) {
    Surface(
        onClick = { /* Navigate to legal doc */ },
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = AppTextStyles.body, color = MaterialTheme.colorScheme.onSurface)
                Icon(Icons.Default.KeyboardArrowDown, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        }
    }
}
