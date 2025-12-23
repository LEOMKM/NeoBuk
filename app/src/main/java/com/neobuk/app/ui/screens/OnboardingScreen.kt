package com.neobuk.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neobuk.app.ui.theme.AppTextStyles
import com.neobuk.app.ui.theme.NeoBukTeal
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onFinishOnboarding: () -> Unit
) {
    val pages = listOf(
        OnboardingPageData(
            title = "Nimeuza ngapi leo?",
            description = "Track your daily sales instantly. No complex accounting, just simple numbers.",
            emoji = "💰"
        ),
        OnboardingPageData(
            title = "Nimetumia ngapi?",
            description = "Record expenses as they happen. Know exactly where your money goes.",
            emoji = "📉"
        ),
        OnboardingPageData(
            title = "Nimebaki na ngapi?",
            description = "See your profit automatically. NeoBuk does the math for you.",
            emoji = "📈"
        ),
        OnboardingPageData(
            title = "Zero Accounting Knowledge",
            description = "No ledgers. No journals. Just Sales, Expenses, and Profit.",
            emoji = "✨"
        ),
        OnboardingPageData(
            title = "Professional Reports",
            description = "Generate daily and monthly reports instantly. Professional PDFs for your business.",
            emoji = "📊"
        ),
        OnboardingPageData(
            title = "Safe & Private",
            description = "✓ Your data is private\n✓ You can export anytime\n✓ No surprise charges",
            emoji = "🛡️"
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Swipeable Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) { pageIndex ->
                OnboardingPage(data = pages[pageIndex])
            }

            // Bottom UI
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Indicators
                Row(
                    modifier = Modifier.padding(bottom = 32.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(pages.size) { index ->
                        Box(
                            modifier = Modifier
                                .size(if (index == pagerState.currentPage) 12.dp else 8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (index == pagerState.currentPage) NeoBukTeal 
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                        )
                    }
                }

                // Primary Button
                Button(
                    onClick = {
                        if (pagerState.currentPage < pages.size - 1) {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            onFinishOnboarding()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeoBukTeal)
                ) {
                    Text(
                        text = if (pagerState.currentPage == pages.size - 1) "Get Started" else "Next",
                        style = AppTextStyles.buttonLarge
                    )
                    if (pagerState.currentPage < pages.size - 1) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Skip Button - Top Right (Moved here to be on top of the Pager)
        if (pagerState.currentPage < pages.size - 1) {
            TextButton(
                onClick = onFinishOnboarding,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Text(
                    "Skip",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = AppTextStyles.bodyBold
                )
            }
        }
    }
}

@Composable
fun OnboardingPage(data: OnboardingPageData) {
    val isTrustPage = data.description.contains("✓")
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        // Emoji Placeholder
        Box(
            modifier = Modifier
                .size(160.dp)
                .background(NeoBukTeal.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(data.emoji, fontSize = 80.sp)
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = data.title,
            style = AppTextStyles.pageTitle.copy(fontSize = 28.sp),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Center the text block, but keep the bullets left-aligned for better readability
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = data.description,
                style = AppTextStyles.body.copy(fontSize = 16.sp, lineHeight = 28.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = if (isTrustPage) TextAlign.Start else TextAlign.Center,
                modifier = if (isTrustPage) Modifier.wrapContentWidth() else Modifier.fillMaxWidth()
            )
        }
    }
}

data class OnboardingPageData(
    val title: String,
    val description: String,
    val emoji: String
)
