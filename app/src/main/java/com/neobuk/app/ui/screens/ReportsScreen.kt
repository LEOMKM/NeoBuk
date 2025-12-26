package com.neobuk.app.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.neobuk.app.ui.theme.NeoBukSuccess
import com.neobuk.app.ui.theme.NeoBukWarning
import com.neobuk.app.ui.theme.NeoBukError
import com.neobuk.app.ui.theme.AppTextStyles
import android.graphics.Color as AndroidColor
import android.graphics.Typeface
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.content.Context
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import org.koin.androidx.compose.koinViewModel
import com.neobuk.app.viewmodels.ReportsViewModel
import com.neobuk.app.viewmodels.AuthViewModel
import com.neobuk.app.data.repositories.ReportSummary
import com.neobuk.app.data.repositories.TopProductReportItem
import com.neobuk.app.data.repositories.PaymentMethodReportItem


// UI Helper Data Classes
data class ReportKPI(
    val title: String,
    val value: String,
    val subtitle: String,
    val changePercent: Double = 0.0, // Hard to calculate without prev period, ignore for now
    val isPositive: Boolean = true
)

data class UI_PaymentMethodData(
    val method: String,
    val percentage: Int,
    val color: Color,
    val total: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    reportsViewModel: ReportsViewModel = koinViewModel(),
    authViewModel: AuthViewModel = koinViewModel()
) {
    // Observe Business State
    val currentBusiness by authViewModel.currentBusiness.collectAsState()
    
    // Initialize
    LaunchedEffect(currentBusiness) {
        currentBusiness?.let {
            reportsViewModel.setBusinessId(it.id)
        }
    }

    // Observe Report State
    val summary by reportsViewModel.summary.collectAsState()
    val topProductsRaw by reportsViewModel.topProducts.collectAsState()
    val paymentMethodsRaw by reportsViewModel.paymentMethods.collectAsState()
    val salesTrendRaw by reportsViewModel.salesTrend.collectAsState()
    val selectedFilter by reportsViewModel.selectedFilter.collectAsState()
    val isLoading by reportsViewModel.isLoading.collectAsState()

    // Map Data to UI
    val kpis = remember(summary) {
        listOf(
            ReportKPI("KES", String.format("%,.0f", summary.totalSales), "Total Sales"),
            ReportKPI("", "${summary.salesCount}", "Sales Count"),
            ReportKPI("KES", String.format("%,.0f", summary.avgSaleValue), "Avg. Sale"),
            ReportKPI("KES", String.format("%,.0f", summary.netProfit), "Net Profit")
        )
    }

    val paymentMethods = remember(paymentMethodsRaw) {
        val total = paymentMethodsRaw.sumOf { it.totalAmount }
        paymentMethodsRaw.mapIndexed { index, item ->
            val percent = if (total > 0) ((item.totalAmount / total) * 100).toInt() else 0
            val color = when(index % 4) {
                0 -> NeoBukTeal
                1 -> NeoBukWarning
                2 -> Color(0xFFA855F7)
                else -> NeoBukSuccess
            }
            UI_PaymentMethodData(item.paymentMethod, percent, color, item.totalAmount)
        }
    }

    val salesTrendData = remember(salesTrendRaw) {
        if (salesTrendRaw.isEmpty()) listOf(0f) 
        else salesTrendRaw.map { it.dailyTotal.toFloat() }
    }

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
                    Text("Reports", style = AppTextStyles.pageTitle)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Filter Chips
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val filters = listOf("Today", "This Week", "This Month", "Custom")
                        filters.forEach { filter ->
                            item {
                                ReportFilterChip(
                                    text = filter,
                                    selected = selectedFilter == filter,
                                    onClick = { 
                                        reportsViewModel.setFilter(filter)
                                        // Handle custom range if needed (future)
                                    }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Export Buttons
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val context = LocalContext.current
                        val businessName = currentBusiness?.businessName ?: "Business"
                        
                        OutlinedButton(
                            onClick = { 
                                ReportUtils.downloadReportPdf(context, businessName, selectedFilter, kpis, topProductsRaw, paymentMethods)
                            },
                            modifier = Modifier.weight(1f).height(40.dp),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444)),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Icon(Icons.Default.PictureAsPdf, null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("PDF", color = Color(0xFFEF4444), style = AppTextStyles.body, maxLines = 1)
                        }

                        // CSV logic same
                        OutlinedButton(
                            onClick = { Toast.makeText(context, "CSV Export coming soon", Toast.LENGTH_SHORT).show() },
                            modifier = Modifier.weight(1f).height(40.dp),
                            shape = RoundedCornerShape(10.dp),
                             border = androidx.compose.foundation.BorderStroke(1.dp, NeoBukTeal),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Icon(Icons.Default.TableChart, null, tint = NeoBukTeal, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Excel", color = NeoBukTeal, style = AppTextStyles.body, maxLines = 1)
                        }
                    }
                }
            }
            
            if (isLoading) {
                 item {
                     Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                         CircularProgressIndicator(color = NeoBukTeal)
                     }
                 }
            } else {

                // 2. KPI Cards Grid
                if (summary.totalSales == 0.0 && summary.salesCount == 0L) {
                     item {
                         com.neobuk.app.ui.components.EmptyState(
                            title = "No Sales Data",
                            description = "Records sales to see detailed reports and insights here.",
                            imageId = com.neobuk.app.R.drawable.empty_reports
                         )
                     }
                } else {
                    item {
                        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                KPICard(kpi = kpis[0], modifier = Modifier.weight(1f))
                                KPICard(kpi = kpis[3], modifier = Modifier.weight(1f)) // Net Profit
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                KPICard(kpi = kpis[1], modifier = Modifier.weight(1f))
                                KPICard(kpi = kpis[2], modifier = Modifier.weight(1f))
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(24.dp)) }

                    // 3. Sales Trends Chart
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Sales Trends", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(16.dp))
                                if (salesTrendRaw.isNotEmpty()) {
                                    SalesTrendChart(data = salesTrendData, modifier = Modifier.fillMaxWidth().height(180.dp))
                                } else {
                                    Box(modifier = Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
                                        Text("No sales trends data available", style = AppTextStyles.caption, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(24.dp)) }

                    // 4. Payment Methods
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Sales by Payment Method", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(16.dp))
                                if (paymentMethods.isNotEmpty()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        DonutChart(data = paymentMethods, modifier = Modifier.size(120.dp))
                                        Spacer(modifier = Modifier.width(24.dp))
                                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                            paymentMethods.forEach { method ->
                                                PaymentLegendItem(color = method.color, label = method.method, percentage = method.percentage)
                                            }
                                        }
                                    }
                                } else {
                                    Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                                        Text("No payment method data available", style = AppTextStyles.caption, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(24.dp)) }

                    // 5. Top Products
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Best Sellers", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(16.dp))
                                if (topProductsRaw.isNotEmpty()) {
                                    topProductsRaw.forEachIndexed { index, product ->
                                        TopProductItem(product = product, rank = index + 1)
                                        if (index < topProductsRaw.size - 1) Spacer(modifier = Modifier.height(16.dp))
                                    }
                                } else {
                                    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                                        Text("No top products data available", style = AppTextStyles.caption, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

// Reuse Composables logic from original file but updated imports

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
        modifier = Modifier.height(36.dp).clickable(onClick = onClick)
    ) {
        Box(modifier = Modifier.padding(horizontal = 16.dp), contentAlignment = Alignment.Center) {
            Text(text, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun KPICard(kpi: ReportKPI, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (kpi.title.isNotEmpty()) "${kpi.title} ${kpi.value}" else kpi.value,
                style = AppTextStyles.amountLarge.copy(fontSize = 20.sp),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(kpi.subtitle, style = AppTextStyles.secondary, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun SalesTrendChart(data: List<Float>, modifier: Modifier = Modifier) {
    val maxValue = (data.maxOrNull() ?: 1f).takeIf { it > 0 } ?: 1f
    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(data) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(1f, tween(1000, easing = LinearOutSlowInEasing))
    }

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val spacing = if (data.size > 1) width / (data.size - 1) else width
        val progress = animationProgress.value

        if (progress > 0 && data.isNotEmpty()) {
            val path = Path()
            data.forEachIndexed { index, value ->
                val x = index * spacing
                val targetY = height - (value / maxValue * height * 0.8f)
                val currentY = height + (targetY - height) * progress 
                if (index == 0) path.moveTo(x, currentY) else path.lineTo(x, currentY)
            }

            drawPath(path, NeoBukTeal, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
            
            val areaPath = Path()
            areaPath.addPath(path)
            areaPath.lineTo(if(data.size > 1) width else 0f, height)
            areaPath.lineTo(0f, height)
            areaPath.close()
            drawPath(areaPath, NeoBukTeal.copy(alpha = 0.1f * progress))
            
            data.forEachIndexed { index, value ->
                 val x = index * spacing
                 val targetY = height - (value / maxValue * height * 0.8f)
                 val currentY = height + (targetY - height) * progress
                 drawCircle(Color.White, 6.dp.toPx() * progress, Offset(x, currentY))
                 drawCircle(NeoBukTeal, 4.dp.toPx() * progress, Offset(x, currentY))
            }
        }
    }
}

@Composable
fun DonutChart(data: List<UI_PaymentMethodData>, modifier: Modifier = Modifier) {
    val transitionProgress = remember { Animatable(0f) }
    LaunchedEffect(data) {
        transitionProgress.snapTo(0f)
        transitionProgress.animateTo(1f, tween(1000, easing = FastOutSlowInEasing))
    }

    Canvas(modifier = modifier) {
        val size = size.minDimension
        val strokeWidth = size * 0.25f
        var startAngle = -90f
        val progress = transitionProgress.value

        data.forEach { item ->
            val totalSweep = item.percentage * 360f / 100f
            val animatedSweep = totalSweep * progress
            if (animatedSweep > 0) {
                 drawArc(
                    color = item.color,
                    startAngle = startAngle,
                    sweepAngle = animatedSweep - 2f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                    topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                    size = Size(size - strokeWidth, size - strokeWidth)
                )
            }
            startAngle += totalSweep
        }
    }
}

@Composable
fun PaymentLegendItem(color: Color, label: String, percentage: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(12.dp).background(color, CircleShape))
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, style = AppTextStyles.body, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.width(60.dp))
        Text("$percentage%", style = AppTextStyles.body, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun TopProductItem(product: TopProductReportItem, rank: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier.size(44.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Inventory2, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(product.productName, style = AppTextStyles.bodyBold, color = MaterialTheme.colorScheme.onSurface)
                Text("${product.unitsSold.toInt()} units sold", style = AppTextStyles.secondary, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("KES ${String.format("%,.0f", product.revenue)}", style = AppTextStyles.price, color = MaterialTheme.colorScheme.onSurface)
            Text("#$rank", style = AppTextStyles.labelLarge.copy(fontWeight = FontWeight.Bold), color = NeoBukTeal)
        }
    }
}

object ReportUtils {
    fun downloadReportPdf(
        context: Context, 
        businessName: String, 
        period: String, 
        kpis: List<ReportKPI>, 
        topProducts: List<TopProductReportItem>, 
        paymentMethods: List<UI_PaymentMethodData>
    ) {
         val pdfDocument = PdfDocument()
         val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
         val page = pdfDocument.startPage(pageInfo)
         val canvas = page.canvas
         
         val titlePaint = Paint().apply { color = AndroidColor.BLACK; textSize = 24f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); textAlign = Paint.Align.CENTER }
         val headerPaint = Paint().apply { color = AndroidColor.rgb(0, 121, 107); textSize = 18f; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.LEFT }
         
         var y = 60f
         val margin = 50f
         
         canvas.drawText(businessName, 595f/2, y, titlePaint)
         y+=40
         canvas.drawText("Report: $period", 595f/2, y, Paint().apply { color=AndroidColor.GRAY; textSize=14f; textAlign=Paint.Align.CENTER })
         
         y+=60
         canvas.drawText("Key Metrics", margin, y, headerPaint)
         y+=30
         val rightAlign = Paint().apply { textAlign=Paint.Align.RIGHT; textSize=14f }
         kpis.forEach { kpi ->
             canvas.drawText(kpi.subtitle, margin, y, Paint().apply { textSize=12f })
             canvas.drawText("${kpi.title} ${kpi.value}", 595f-margin, y, rightAlign)
             y+=20
         }
         
         y+=40
         canvas.drawText("Top Products", margin, y, headerPaint)
         y+=30
         topProducts.forEach { p ->
             canvas.drawText(p.productName, margin, y, Paint().apply { textSize=12f })
             canvas.drawText("KES ${String.format("%,.0f", p.revenue)}", 595f-margin, y, rightAlign)
             y+=20
         }
         
         pdfDocument.finishPage(page)
         
         // Save logic (Downloads folder)
         val file = java.io.File(android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS), "NeoBuk_Report_${java.lang.System.currentTimeMillis()}.pdf")
         try {
             pdfDocument.writeTo(java.io.FileOutputStream(file))
             Toast.makeText(context, "Report saved to Downloads", Toast.LENGTH_LONG).show()
         } catch (e: Exception) {
             Toast.makeText(context, "Error saving report: ${e.message}", Toast.LENGTH_LONG).show()
         }
         pdfDocument.close()
    }
    
    fun downloadReportCsv(
        context: Context, 
        businessName: String, 
        period: String, 
        kpis: List<ReportKPI>, 
        topProducts: List<TopProductReportItem>, 
        paymentMethods: List<UI_PaymentMethodData>
    ) {
        // Implementation for CSV
    }
}
