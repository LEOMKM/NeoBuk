package com.neobuk.app.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neobuk.app.ui.theme.NeoBukCyan
import com.neobuk.app.ui.theme.NeoBukTeal
import com.neobuk.app.ui.theme.Tokens
import com.neobuk.app.ui.theme.NeoBukSuccess
import com.neobuk.app.ui.theme.NeoBukWarning
import com.neobuk.app.ui.theme.NeoBukError
import com.neobuk.app.ui.theme.AppTextStyles
import android.graphics.Color as AndroidColor
import android.graphics.Typeface
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import android.content.Context
import android.content.Intent
import androidx.compose.ui.platform.LocalContext

// Data Classes
data class ReportKPI(
    val title: String,
    val value: String,
    val subtitle: String,
    val changePercent: Double,
    val isPositive: Boolean
)

data class TopProduct(
    val name: String,
    val unitsSold: Int,
    val revenue: Double
)

data class PaymentMethodData(
    val method: String,
    val percentage: Int,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen() {
    var selectedFilter by remember { mutableStateOf("Today") }

    // Mock Data
    val kpis = listOf(
        ReportKPI("KES", "45,280", "Total Sales", 12.5, true),
        ReportKPI("", "127", "Sales Count", 8.2, true),
        ReportKPI("KES", "356", "Avg. Sale", -2.1, false)
    )

    val paymentMethods = listOf(
        PaymentMethodData("M-PESA", 65, NeoBukTeal),
        PaymentMethodData("Cash", 25, NeoBukWarning),
        PaymentMethodData("Credit", 7, Color(0xFFA855F7)),
        PaymentMethodData("Bank", 3, NeoBukSuccess)
    )

    val topProducts = listOf(
        TopProduct("Maize Flour", 45, 5400.0),
        TopProduct("Sugar", 38, 4560.0),
        TopProduct("Cooking Oil", 32, 7040.0),
        TopProduct("Tea Leaves", 28, 5040.0),
        TopProduct("Milk", 24, 1560.0)
    )

    val salesTrendData = listOf(3f, 5f, 8f, 12f, 18f, 22f, 28f, 32f, 33f)

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // 1. Header
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    // Header Removed


                    // Filter Chips
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val filters = listOf("Today", "This Week", "This Month", "Custom")
                        filters.forEach { filter ->
                            item {
                                ReportFilterChip(
                                    text = filter,
                                    selected = selectedFilter == filter,
                                    onClick = { selectedFilter = filter }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Export Reports (at top for quick access)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val context = LocalContext.current
                        OutlinedButton(
                            onClick = { 
                                val businessName = "Mama Njeri's Shop" // Mock for now
                                ReportUtils.downloadReportPdf(context, businessName, selectedFilter, kpis, topProducts, paymentMethods)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444)),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Icon(
                                Icons.Default.PictureAsPdf,
                                null,
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("PDF", color = Color(0xFFEF4444), style = AppTextStyles.body, maxLines = 1)
                        }

                        OutlinedButton(
                            onClick = { 
                                val businessName = "Mama Njeri's Shop" // Mock
                                ReportUtils.downloadReportCsv(context, businessName, selectedFilter, kpis, topProducts, paymentMethods)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeoBukTeal),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Icon(
                                Icons.Default.TableChart,
                                null,
                                tint = NeoBukTeal,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Excel", color = NeoBukTeal, style = AppTextStyles.body, maxLines = 1)
                        }
                        
                        Button(
                            onClick = { },
                            modifier = Modifier
                                .weight(1.2f) // Give more space
                                .height(40.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NeoBukTeal),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Icon(
                                Icons.Default.Settings,
                                null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Custom", style = AppTextStyles.body, maxLines = 1)
                        }
                    }
                }
            }

            // 2. KPI Cards Grid (2x2)
            item {
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        KPICard(kpi = kpis[0], modifier = Modifier.weight(1f))
                        KPICard(kpi = kpis[1], modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        KPICard(kpi = kpis[2], modifier = Modifier.weight(1f))
                        Box(modifier = Modifier.weight(1f)) // Empty spacer for now
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            // 3. Sales Trends Chart
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Sales Trends",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { }) {
                                Icon(Icons.Default.MoreVert, null, tint = Color.Gray)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Line Chart
                        SalesTrendChart(
                            data = salesTrendData,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                        )

                        // X-axis labels
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach {
                                Text(it, style = AppTextStyles.caption, color = Color.Gray)
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            // 4. Revenue by Payment Method
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Sales by Payment Method",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Donut Chart
                            DonutChart(
                                data = paymentMethods,
                                modifier = Modifier.size(120.dp)
                            )

                            Spacer(modifier = Modifier.width(24.dp))

                            // Legend
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                paymentMethods.forEach { method ->
                                    PaymentLegendItem(
                                        color = method.color,
                                        label = method.method,
                                        percentage = method.percentage
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            // 5. Top Selling Products
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Best Sellers",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        topProducts.forEachIndexed { index, product ->
                            TopProductItem(
                                product = product,
                                rank = index + 1
                            )
                            if (index < topProducts.size - 1) {
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            // Customer statistics removed as per request

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }

        // Download FAB
        val context = LocalContext.current
        FloatingActionButton(
            onClick = { 
                val businessName = "Mama Njeri's Shop" // Mock for now
                ReportUtils.downloadReportPdf(context, businessName, selectedFilter, kpis, topProducts, paymentMethods)
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .padding(bottom = 60.dp),
            containerColor = NeoBukTeal,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Download, contentDescription = "Download")
        }
    }
}

@Composable
fun ReportFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (selected) NeoBukTeal else MaterialTheme.colorScheme.surface,
        border = if (!selected) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline) else null,
        modifier = Modifier
            .height(36.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun KPICard(
    kpi: ReportKPI,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    if (kpi.title.isNotEmpty()) {
                        Text(kpi.title, style = AppTextStyles.secondary, color = Color.Gray)
                    }
                    Text(
                        text = kpi.value,
                        style = AppTextStyles.amountLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }

                // Change indicator
                Text(
                    text = "${if (kpi.isPositive) "↑" else "↓"} ${kotlin.math.abs(kpi.changePercent)}%",
                    style = AppTextStyles.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = if (kpi.isPositive) NeoBukSuccess else NeoBukError
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = kpi.subtitle,
                style = AppTextStyles.secondary,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SalesTrendChart(
    data: List<Float>,
    modifier: Modifier = Modifier
) {
    val maxValue = data.maxOrNull() ?: 1f
    
    // Animation state for line drawing progress
    val animationProgress = remember { Animatable(0f) }
    
    LaunchedEffect(data) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000, easing = LinearOutSlowInEasing)
        )
    }

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val spacing = width / (data.size - 1)
        val progress = animationProgress.value

        // Only draw if we have progress
        if (progress > 0) {
            // Draw gradient area (masked by progress implies complicated path clipping, 
            // simpler to animate opacity or just the line. Let's animate the line drawing explicitly)
            
            // For true line drawing animation, we need a PathMeasure or manual interpolation.
            // Simplified approach: Animate the Y values from 0 or baseline? No, line drawing usually means left-to-right.
            // Let's us animate Y axis scale up for "growth" effect which is easier and looks good for charts.
            
            // Re-evaluating: Prompt asks for "not animating". Let's do a "growth" animation where the line rises up.
            
            val path = Path()
            data.forEachIndexed { index, value ->
                val x = index * spacing
                // Animate Y from baseline (height) to actual position
                val targetY = height - (value / maxValue * height * 0.9f)
                val currentY = height + (targetY - height) * progress 
                
                if (index == 0) {
                    path.moveTo(x, currentY)
                } else {
                    path.lineTo(x, currentY)
                }
            }

            // Draw line
             drawPath(
                path = path,
                color = NeoBukTeal,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            // Draw area below
            val areaPath = Path()
            areaPath.addPath(path)
            areaPath.lineTo(width, height)
            areaPath.lineTo(0f, height)
            areaPath.close()

            drawPath(
                path = areaPath,
                color = NeoBukTeal.copy(alpha = 0.1f * progress) // Fade in area
            )

            // Draw points
            data.forEachIndexed { index, value ->
                val x = index * spacing
                val targetY = height - (value / maxValue * height * 0.9f)
                val currentY = height + (targetY - height) * progress
                
                drawCircle(
                    color = Color.White,
                    radius = 6.dp.toPx() * progress, // Scale point
                    center = Offset(x, currentY)
                )
                drawCircle(
                    color = NeoBukTeal,
                    radius = 4.dp.toPx() * progress,
                    center = Offset(x, currentY)
                )
            }
        }
    }
}

@Composable
fun DonutChart(
    data: List<PaymentMethodData>,
    modifier: Modifier = Modifier
) {
    // Animation for sweep angles
    val transitionProgress = remember { Animatable(0f) }
    
    LaunchedEffect(data) {
        transitionProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        )
    }

    Canvas(modifier = modifier) {
        val size = size.minDimension
        val strokeWidth = size * 0.25f
        //val radius = (size - strokeWidth) / 2

        var startAngle = -90f
        val progress = transitionProgress.value

        data.forEach { item ->
            // Scale the sweep angle by progress
            val totalSweep = item.percentage * 360f / 100f
            val animatedSweep = totalSweep * progress
            
            if (animatedSweep > 0) {
                 drawArc(
                    color = item.color,
                    startAngle = startAngle,
                    sweepAngle = animatedSweep - 2f, // Small gap
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                    topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                    size = Size(size - strokeWidth, size - strokeWidth)
                )
            }

            // For the next segment, we need to advance startAngle by the FULL sweep 
            // so they appear to grow in place rather than rotating wildly, 
            // OR we animate the startAngle too. 
            // Standard donut loading: Segments fill up in order or all at once.
            // Let's do "Fill up in order" or "All grow relative". 
            // "All grow relative" is simpler: startAngle is consistent with final position? 
            // Actually, if we just multiply sweep, we need to adjust startAngle if we want them to stay connected?
            // No, if they all grow from 0 sweep, they need to be positioned correctly.
            // Let's animate the PERCENTAGE multiplier for all of them together.
            // So segment 1 starts at -90, grows to 30. Segment 2 starts at -60 (final pos of 1)??
            // Correct approach for "Simultaneous growth": 
            // StartAngle needs to be interpolated too? 
            // Easier approach: The chart "spins" open or simply fills.
            // Let's stick to the multiplier on sweepAngle. But we must advance startAngle by the animated sweep to keep them touching?
            // If we advance by `animatedSweep`, the whole chart shrinks and expands. 
            // If we advance by `totalSweep`, the segments grow from their final start positions. (Gaps appear until full).
            // Let's advance by `totalSweep` but animate the `sweepAngle` so they grow from their "stations".
            
            startAngle += totalSweep
        }
    }
}

@Composable
fun PaymentLegendItem(
    color: Color,
    label: String,
    percentage: Int
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, style = AppTextStyles.body, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.width(60.dp))
        Text("$percentage%", style = AppTextStyles.body, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun TopProductItem(
    product: TopProduct,
    rank: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            // Product Image Placeholder
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Inventory2,
                    null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    product.name,
                    style = AppTextStyles.bodyBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "${product.unitsSold} units sold",
                    style = AppTextStyles.secondary,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                "KES ${String.format("%,.0f", product.revenue)}",
                style = AppTextStyles.price,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "#$rank",
                style = AppTextStyles.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = NeoBukTeal
            )
        }
    }
}



object ReportUtils {
    fun generatePdfFile(context: Context, businessName: String, period: String, kpis: List<ReportKPI>, topProducts: List<TopProduct>, paymentMethods: List<PaymentMethodData>): java.io.File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        
        // Paints
        val titlePaint = Paint().apply {
            color = AndroidColor.BLACK
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        
        val headerPaint = Paint().apply {
            color = AndroidColor.rgb(0, 121, 107)
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.LEFT
        }
        
        val subHeaderPaint = Paint().apply {
             color = AndroidColor.rgb(0, 121, 107)
             textSize = 18f
             typeface = Typeface.DEFAULT_BOLD
             textAlign = Paint.Align.CENTER
        }
        
        val textPaint = Paint().apply {
            color = AndroidColor.BLACK
            textSize = 12f
        }
        
        val rightTextPaint = Paint().apply {
            color = AndroidColor.BLACK
            textSize = 12f
            textAlign = Paint.Align.RIGHT
        }
        
        val grayPaint = Paint().apply {
            color = AndroidColor.GRAY
            textSize = 10f
        }

        var y = 60f
        val centerX = 595f / 2
        val margin = 50f
        
        // Title
        canvas.drawText(businessName, centerX, y, titlePaint)
        y += 30
        
        canvas.drawText("Business Report", centerX, y, subHeaderPaint)
        y += 30
        canvas.drawText("Period: $period", centerX, y, Paint().apply { 
            color = AndroidColor.GRAY
            textSize = 14f
            textAlign = Paint.Align.CENTER
        })
        y += 40

        // Smart Insight
        val topProduct = topProducts.maxByOrNull { it.revenue }
        val insight = if (topProduct != null) "Sales are moving! ${topProduct.name} is your top revenue product." else "Check your sales data to see insights."
        canvas.drawText(insight, centerX, y, Paint().apply {
            color = AndroidColor.rgb(46, 125, 50) // Dark Green
            textSize = 12f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        })
        y += 50
        
        // KPI Section
        canvas.drawText("Key Metrics", margin, y, headerPaint)
        y += 30
        
        kpis.forEach { kpi ->
            canvas.drawText(kpi.subtitle, margin, y, grayPaint)
            val valueText = "${kpi.title} ${kpi.value}"
            canvas.drawText(valueText, 595f - margin, y, rightTextPaint.apply { typeface = Typeface.DEFAULT_BOLD })
            rightTextPaint.typeface = Typeface.DEFAULT
            y += 20
        }
        y += 20
        
        // Separator
        canvas.drawLine(margin, y, 595f - margin, y, Paint().apply { color = AndroidColor.LTGRAY })
        y += 30
        
        // Top Products
        canvas.drawText("Top Products", margin, y, headerPaint)
        y += 30
        
        // Table Header
        val leftAlignGray = Paint().apply { color = AndroidColor.GRAY; textSize=10f; textAlign=Paint.Align.LEFT }
        val centerAlignGray = Paint().apply { color = AndroidColor.GRAY; textSize=10f; textAlign=Paint.Align.CENTER }
        val rightAlignGray = Paint().apply { color = AndroidColor.GRAY; textSize=10f; textAlign=Paint.Align.RIGHT }
        
        canvas.drawText("Product", margin, y, leftAlignGray)
        canvas.drawText("Units", centerX, y, centerAlignGray)
        canvas.drawText("Revenue", 595f - margin, y, rightAlignGray)
        y += 20
         
        topProducts.forEach { prod ->
            canvas.drawText(prod.name, margin, y, textPaint)
            canvas.drawText(prod.unitsSold.toString(), centerX, y, Paint().apply { color=AndroidColor.BLACK; textSize=12f; textAlign=Paint.Align.CENTER })
            val formattedRevenue = String.format("%,.0f", prod.revenue)
            canvas.drawText("KES $formattedRevenue", 595f - margin, y, rightTextPaint)
            y += 20
        }
         
        y += 20
        canvas.drawLine(margin, y, 595f - margin, y, Paint().apply { color = AndroidColor.LTGRAY })
        y += 30
         
          // Payment Methods
        canvas.drawText("Payment Methods", margin, y, headerPaint)
        y += 30
        
        // Extract Total Sales for Payment Method calculations
        val totalSalesKpi = kpis.find { it.subtitle == "Total Sales" }
        val totalSales = totalSalesKpi?.value?.replace(",", "")?.toDoubleOrNull() ?: 0.0
        
        paymentMethods.forEach { method ->
             canvas.drawText(method.method, margin, y, textPaint)
             val amount = totalSales * (method.percentage / 100.0)
             val formattedAmount = String.format("%,.0f", amount)
             canvas.drawText("${method.percentage}% (KES $formattedAmount)", 595f - margin, y, rightTextPaint)
             y += 20
        }
        
        // Footer
        y = 800f
        // NeoBuk Logo Text
        val logoPaint = Paint().apply {
            color = AndroidColor.rgb(0, 121, 107) // NeoBuk Teal
            textSize = 18f // Slightly larger for cursive
            typeface = Typeface.create("cursive", Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("NeoBuk", centerX, y, logoPaint)
        y += 15
        val dateFormat = java.text.SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", java.util.Locale.getDefault())
        val timestamp = dateFormat.format(java.util.Date())
        
        canvas.drawText("Digital Report • $timestamp", centerX, y, Paint().apply { 
            color = AndroidColor.GRAY
            textSize = 9f
            textAlign = Paint.Align.CENTER
        })
        
        pdfDocument.finishPage(page)
        
        val file = java.io.File(context.cacheDir, "Report_${System.currentTimeMillis()}.pdf")
        try {
            pdfDocument.writeTo(java.io.FileOutputStream(file))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        pdfDocument.close()
        return file
    }
    
    fun downloadReportPdf(context: Context, businessName: String, period: String, kpis: List<ReportKPI>, topProducts: List<TopProduct>, paymentMethods: List<PaymentMethodData>) {
        val file = generatePdfFile(context, businessName, period, kpis, topProducts, paymentMethods)
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            val contentValues = android.content.ContentValues().apply {
                put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, "NeoBuk_Report_${System.currentTimeMillis()}.pdf")
                put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                resolver.openOutputStream(uri)?.use { outputStream ->
                    java.io.FileInputStream(file).use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                Toast.makeText(context, "Report saved to Downloads", Toast.LENGTH_SHORT).show()
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Open Report"))
             }
        }
    }



    fun generateCsvFile(context: Context, businessName: String, period: String, kpis: List<ReportKPI>, topProducts: List<TopProduct>, paymentMethods: List<PaymentMethodData>): java.io.File {
        val fileName = "Report_${System.currentTimeMillis()}.csv"
        val file = java.io.File(context.cacheDir, fileName)
        
        file.printWriter().use { out ->
            out.println("Business Report")
            out.println("Business Name, $businessName")
            out.println("Period, $period")
            out.println()
            
            out.println("Key Metrics")
            out.println("Metric,Value")
            kpis.forEach { kpi ->
                out.println("${kpi.subtitle},${kpi.title} ${kpi.value.replace(",", "")}")
            }
            out.println()
            
            out.println("Top Products")
            out.println("Product,Units Sold,Revenue")
            topProducts.forEach { prod ->
                out.println("${prod.name},${prod.unitsSold},${prod.revenue}")
            }
            out.println()
            
            out.println("Payment Methods")
            out.println("Method,Percentage,Amount")
            
            // Calculate total sales for amount
            val totalSalesKpi = kpis.find { it.subtitle == "Total Sales" }
            val totalSales = totalSalesKpi?.value?.replace(",", "")?.toDoubleOrNull() ?: 0.0
            
            paymentMethods.forEach { method ->
                val amount = totalSales * (method.percentage / 100.0)
                val formattedAmount = String.format("%.0f", amount)
                out.println("${method.method},${method.percentage}%,$formattedAmount")
            }
            out.println()
            out.println("Generated by NeoBuk")
        }
        return file
    }

    fun downloadReportCsv(context: Context, businessName: String, period: String, kpis: List<ReportKPI>, topProducts: List<TopProduct>, paymentMethods: List<PaymentMethodData>) {
        val file = generateCsvFile(context, businessName, period, kpis, topProducts, paymentMethods)
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            val contentValues = android.content.ContentValues().apply {
                put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, "NeoBuk_Report_${System.currentTimeMillis()}.csv")
                put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                resolver.openOutputStream(uri)?.use { outputStream ->
                    java.io.FileInputStream(file).use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                Toast.makeText(context, "CSV saved to Downloads", Toast.LENGTH_SHORT).show()
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "text/csv")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                try {
                    context.startActivity(Intent.createChooser(intent, "Open CSV"))
                } catch (e: Exception) {
                    Toast.makeText(context, "No app found to open CSV", Toast.LENGTH_SHORT).show()
                }
            } else {
                 Toast.makeText(context, "Failed to save CSV", Toast.LENGTH_SHORT).show()
            }
        } else {
             Toast.makeText(context, "Download not supported on this device version yet", Toast.LENGTH_SHORT).show()
        }
    }


}
