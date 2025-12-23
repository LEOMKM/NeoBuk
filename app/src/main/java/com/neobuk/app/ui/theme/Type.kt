package com.neobuk.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Optimized Small-Device Typography (5.8–6.4" screens)
 * 
 * Hierarchy:
 * - Page Title: 20-22sp, semibold, tight line height (1.2-1.3×)
 * - Section Title: 16sp, medium, tight line height
 * - Body/Primary: 14sp, regular, comfortable line height (1.4-1.5×)
 * - Secondary/Meta: 12sp, regular
 * - Labels/Captions: 11sp, medium
 * - Buttons: Medium 15sp, Large 16sp
 * - Amounts/Totals: 16-18sp, bold
 */
val Typography = Typography(
    // Page Title - 22sp semibold (tight line height 1.25×)
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp, // 1.27× ratio
        letterSpacing = 0.sp
    ),
    
    // Page Title (smaller) - 20sp semibold
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp, // 1.3× ratio
        letterSpacing = 0.sp
    ),
    
    // Section Title - 16sp medium (tight line height)
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 21.sp, // 1.3× ratio
        letterSpacing = 0.sp
    ),
    
    // Section Title (smaller) - 15sp medium
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),
    
    // Body/Primary Text - 14sp regular (comfortable line height 1.5×)
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp, // 1.5× ratio - calm reading
        letterSpacing = 0.sp
    ),
    
    // Secondary/Meta Text - 12sp regular
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 17.sp, // 1.4× ratio
        letterSpacing = 0.sp
    ),
    
    // Small Text - 11sp
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 15.sp,
        letterSpacing = 0.sp
    ),
    
    // Labels/Captions - 11sp medium
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp
    ),
    
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 15.sp,
        letterSpacing = 0.sp
    ),
    
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.sp
    )
)

/**
 * App-specific text styles for common patterns
 */
object AppTextStyles {
    // Page Title - 20sp semibold
    val pageTitle = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp
    )
    
    // Section Title - 16sp medium
    val sectionTitle = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 21.sp
    )
    
    // Body/Primary - 14sp regular
    val body = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp
    )
    
    // Body Bold - 14sp medium
    val bodyBold = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 21.sp
    )
    
    // Secondary/Meta - 12sp regular
    val secondary = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 17.sp
    )
    
    // Caption/Label - 11sp medium
    val caption = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 15.sp
    )

    // Label Large - 12sp medium
    val labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    )
    
    // Button Medium - 15sp medium
    val buttonMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 20.sp
    )
    
    // Button Large - 16sp semibold
    val buttonLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 21.sp
    )
    
    // Amount/Total Small - 14sp bold
    val amountSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 19.sp
    )
    
    // Amount/Total Large - 16sp bold
    val amountLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 21.sp
    )
    
    // Price display - 14sp bold (for inline prices)
    val price = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 19.sp
    )
}
