package com.neobuk.app.ui.screens.receipt

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.neobuk.app.ui.components.NeoBukLogo
import com.neobuk.app.ui.theme.AppTextStyles
import com.neobuk.app.ui.theme.NeoBukTeal

data class ReceiptData(
    val businessName: String,
    val receiptId: String,
    val date: String,
    val items: List<ReceiptItem>,
    val totalAmount: String,
    val paymentMethod: String,
    val paymentRef: String? = null,
    val customerName: String? = null
)

data class ReceiptItem(
    val name: String,
    val quantity: Int,
    val price: String, // Unit price formatted
    val total: String // Line total formatted
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptScreen(
    data: ReceiptData,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    
    // Generate Barcode Bitmap
    val barcodeBitmap = remember(data.receiptId) {
        ReceiptUtils.generateBarcodeBitmap(data.receiptId, 600, 150)
    }
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("E-Receipt", style = AppTextStyles.bodyBold.copy(color = Color.White)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NeoBukTeal)
            )
        },
        containerColor = NeoBukTeal
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            
            // Receipt Card - Wrap Content
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(vertical = 24.dp, horizontal = 56.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Success Icon
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8F5E9)), // Light Green
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32), // Success Green
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("Payment Received", style = AppTextStyles.pageTitle.copy(color = Color.Black), textAlign = TextAlign.Center)
                    Text(data.totalAmount, style = AppTextStyles.amountLarge, color = NeoBukTeal, modifier = Modifier.padding(top = 8.dp))
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Details Table
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Date", style = AppTextStyles.body, color = Color.Gray)
                        Text(
                            data.date, 
                            style = AppTextStyles.bodyBold.copy(color = Color.Black),
                            textAlign = TextAlign.End
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Method", style = AppTextStyles.body, color = Color.Gray)
                        Column(
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier.weight(1f).padding(start = 32.dp)
                        ) {
                            Text(
                                data.paymentMethod, 
                                style = AppTextStyles.bodyBold.copy(color = Color.Black),
                                textAlign = TextAlign.End
                            )
                            if (!data.paymentRef.isNullOrEmpty()) {
                                Text(
                                    "Ref: ${data.paymentRef}", 
                                    style = AppTextStyles.caption.copy(color = Color.Gray),
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Business", style = AppTextStyles.body, color = Color.Gray)
                        Text(
                            data.businessName, 
                            style = AppTextStyles.bodyBold.copy(color = Color.Black),
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(1f).padding(start = 32.dp)
                        )
                    }
                    
                    if (data.customerName != null && data.paymentMethod.contains("M-PESA", ignoreCase = true)) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Customer", style = AppTextStyles.body, color = Color.Gray)
                            Text(
                                data.customerName, 
                                style = AppTextStyles.bodyBold.copy(color = Color.Black),
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1f).padding(start = 32.dp)
                            )
                        }
                    }
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp), color = MaterialTheme.colorScheme.outlineVariant)
                    
                    // Items Header
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Item", style = AppTextStyles.caption, color = Color.Gray)
                        Text("Cost", style = AppTextStyles.caption, color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    data.items.forEach { item ->
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.name, style = AppTextStyles.bodyBold.copy(color = Color.Black))
                                Text("${item.quantity} x ${item.price}", style = AppTextStyles.caption, color = Color.Gray)
                            }
                            Text(item.total, style = AppTextStyles.body.copy(color = Color.Black))
                        }
                    }
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp), color = MaterialTheme.colorScheme.outlineVariant)
                    
                    // Barcode Image
                    if (barcodeBitmap != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .background(Color.White, RoundedCornerShape(4.dp))
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                bitmap = barcodeBitmap.asImageBitmap(),
                                contentDescription = "Receipt ID Barcode",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.FillBounds
                            )
                        }
                    } else {
                         // Fallback text if generic fails
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .background(Color(0xFFEEEEEE), RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("||| | |||| ||| || |||| |||", style = AppTextStyles.pageTitle.copy(letterSpacing = 4.sp), color = Color.Black)
                        }
                    }
                    Text("Receipt No: ${data.receiptId}", style = AppTextStyles.caption.copy(color = Color.Black), modifier = Modifier.padding(top = 4.dp))
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Brand Logo
                    Box(modifier = Modifier.scale(0.8f)) { // Reduce prominence
                        NeoBukLogo()
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Digital Receipt", style = AppTextStyles.caption.copy(fontSize = 10.sp), color = Color.Gray)
                }
            }
            
            // Action Buttons - Inside Teal Background
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Share Button
                OutlinedButton(
                    onClick = { ReceiptUtils.sharePdf(context, data) },
                    modifier = Modifier.weight(1f).height(54.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = NeoBukTeal
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White)
                ) {
                   Icon(Icons.Default.Share, null, tint = NeoBukTeal)
                   Spacer(modifier = Modifier.width(6.dp))
                   Text("Share", color = NeoBukTeal, style = AppTextStyles.buttonMedium)
                }
                
                // Download Button
                Button(
                    onClick = { ReceiptUtils.downloadPdf(context, data) },
                    modifier = Modifier.weight(1f).height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                   Icon(Icons.Default.Print, null, tint = NeoBukTeal)
                   Spacer(modifier = Modifier.width(6.dp))
                   Text("Download", color = NeoBukTeal, style = AppTextStyles.buttonMedium)
                }
            }
        }
    }
}

object ReceiptUtils {

    fun generateBarcodeBitmap(contents: String, width: Int, height: Int): Bitmap? {
        return try {
            val writer = MultiFormatWriter()
            val bitMatrix: BitMatrix = writer.encode(contents, BarcodeFormat.CODE_128, width, height)
            val w = bitMatrix.width
            val h = bitMatrix.height
            val pixels = IntArray(w * h)
            for (y in 0 until h) {
                for (x in 0 until w) {
                    pixels[y * w + x] = if (bitMatrix[x, y]) AndroidColor.BLACK else AndroidColor.WHITE
                }
            }
            Bitmap.createBitmap(pixels, w, h, Bitmap.Config.ARGB_8888)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun generatePlainText(data: ReceiptData): String {
        return generateHtml(data) // Unused now but kept for compatibility or fallback
    }

    private fun startPdfPage(): android.graphics.pdf.PdfDocument {
        return android.graphics.pdf.PdfDocument()
    }

    fun generatePdfFile(context: Context, data: ReceiptData): java.io.File {
        val pdfDocument = android.graphics.pdf.PdfDocument()
        // A4 size in points (approx 595x842)
        val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = android.graphics.Paint()
        val titlePaint = android.graphics.Paint().apply {
            color = AndroidColor.BLACK
            textSize = 24f
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            textAlign = android.graphics.Paint.Align.CENTER
        }
        val headerPaint = android.graphics.Paint().apply {
            color = AndroidColor.rgb(0, 121, 107) // NeoBuk Teal
            textSize = 18f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            textAlign = android.graphics.Paint.Align.CENTER
        }
        val textPaint = android.graphics.Paint().apply {
            color = AndroidColor.BLACK
            textSize = 14f
        }
        val rightTextPaint = android.graphics.Paint().apply {
            color = AndroidColor.BLACK
            textSize = 14f
            textAlign = android.graphics.Paint.Align.RIGHT
        }
        val grayPaint = android.graphics.Paint().apply {
            color = AndroidColor.GRAY
            textSize = 12f
        }

        var y = 60f
        val centerX = 595f / 2
        val margin = 150f

        // Success Checkmark Icon (Circle with check)
        val checkCircleRadius = 30f
        val checkCirclePaint = android.graphics.Paint().apply {
            color = AndroidColor.rgb(232, 245, 233) // Light green background
            style = android.graphics.Paint.Style.FILL
        }
        canvas.drawCircle(centerX, y + checkCircleRadius, checkCircleRadius, checkCirclePaint)
        
        // Draw checkmark inside circle
        val checkPaint = android.graphics.Paint().apply {
            color = AndroidColor.rgb(46, 125, 50) // Dark green
            strokeWidth = 4f
            style = android.graphics.Paint.Style.STROKE
            strokeCap = android.graphics.Paint.Cap.ROUND
            strokeJoin = android.graphics.Paint.Join.ROUND
        }
        val checkPath = android.graphics.Path()
        checkPath.moveTo(centerX - 12f, y + checkCircleRadius)
        checkPath.lineTo(centerX - 4f, y + checkCircleRadius + 8f)
        checkPath.lineTo(centerX + 12f, y + checkCircleRadius - 8f)
        canvas.drawPath(checkPath, checkPaint)
        
        y += checkCircleRadius * 2 + 20

        // Payment Received Title
        canvas.drawText("Payment Received", centerX, y, titlePaint)
        y += 30
        
        // Total Amount (prominent)
        val amountPaint = android.graphics.Paint().apply {
            color = AndroidColor.rgb(0, 121, 107) // NeoBuk Teal
            textSize = 28f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            textAlign = android.graphics.Paint.Align.CENTER
        }
        canvas.drawText(data.totalAmount, centerX, y, amountPaint)
        
        y += 30
        
        // Meta Data
        canvas.drawText("Date", margin, y, grayPaint.apply { textAlign = android.graphics.Paint.Align.LEFT })
        canvas.drawText(data.date, 595f - margin, y, rightTextPaint)
        y += 20
        canvas.drawText("Method", margin, y, grayPaint)
        
        // Payment method with reference (if exists)
        if (!data.paymentRef.isNullOrEmpty()) {
            canvas.drawText(data.paymentMethod, 595f - margin, y, rightTextPaint)
            y += 15
            canvas.drawText("Ref: ${data.paymentRef}", 595f - margin, y, grayPaint.apply { 
                textAlign = android.graphics.Paint.Align.RIGHT
                textSize = 10f 
            })
            grayPaint.textSize = 12f // Reset
            grayPaint.textAlign = android.graphics.Paint.Align.LEFT
            y += 5
        } else {
            canvas.drawText(data.paymentMethod, 595f - margin, y, rightTextPaint)
        }
        
        y += 20
        canvas.drawText("Business", margin, y, grayPaint)
        canvas.drawText(data.businessName, 595f - margin, y, rightTextPaint)
        y += 20
        
        if (data.customerName != null && data.paymentMethod.contains("M-PESA", ignoreCase = true)) {
            canvas.drawText("Customer", margin, y, grayPaint)
            canvas.drawText(data.customerName, 595f - margin, y, rightTextPaint)
            y += 20
        }

        y += 10
        paint.color = AndroidColor.LTGRAY
        paint.strokeWidth = 1f
        canvas.drawLine(margin, y, 595f - margin, y, paint)
        y += 30

        // Items Header
        canvas.drawText("Item", margin, y, grayPaint.apply { textAlign = android.graphics.Paint.Align.LEFT })
        canvas.drawText("Cost", 595f - margin, y, grayPaint.apply { textAlign = android.graphics.Paint.Align.RIGHT })
        y += 20

        data.items.forEach { item ->
            canvas.drawText(item.name, margin, y, textPaint.apply { typeface = android.graphics.Typeface.DEFAULT_BOLD })
            canvas.drawText(item.total, 595f - margin, y, rightTextPaint.apply { typeface = android.graphics.Typeface.DEFAULT_BOLD })
            y += 15
            canvas.drawText("${item.quantity} x ${item.price}", margin, y, grayPaint.apply { textAlign = android.graphics.Paint.Align.LEFT })
            textPaint.typeface = android.graphics.Typeface.DEFAULT // Reset
            rightTextPaint.typeface = android.graphics.Typeface.DEFAULT
            y += 25
        }

        y += 10
        canvas.drawLine(margin, y, 595f - margin, y, paint)
        y += 40

        // Barcode
        val barcodeBitmap = generateBarcodeBitmap(data.receiptId, 300, 60)
        if (barcodeBitmap != null) {
             val barcodeX = (595f - 300f) / 2
             canvas.drawBitmap(barcodeBitmap, barcodeX, y, null)
             y += 70
        }
        
        canvas.drawText("Receipt No: ${data.receiptId}", centerX, y, grayPaint.apply { 
            textAlign = android.graphics.Paint.Align.CENTER
            textSize = 10f
        })
        y += 30
        
        // NeoBuk Logo Text (gradient effect simulated with bold teal)
        val logoPaint = android.graphics.Paint().apply {
            color = AndroidColor.rgb(0, 121, 107) // NeoBuk Teal
            textSize = 18f // Slightly larger for cursive
            typeface = android.graphics.Typeface.create("cursive", android.graphics.Typeface.BOLD)
            textAlign = android.graphics.Paint.Align.CENTER
        }
        canvas.drawText("NeoBuk", centerX, y, logoPaint)
        y += 15
        canvas.drawText("Digital Receipt", centerX, y, grayPaint.apply { 
            textAlign = android.graphics.Paint.Align.CENTER
            textSize = 9f
        })

        pdfDocument.finishPage(page)

        val file = java.io.File(context.cacheDir, "Receipt_${data.receiptId}.pdf")
        try {
            pdfDocument.writeTo(java.io.FileOutputStream(file))
        } catch (e: java.io.IOException) {
            e.printStackTrace()
        }
        pdfDocument.close()
        return file
    }

    fun shareText(context: Context, text: String) {
       // Only used as fallback now
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Receipt")
        context.startActivity(shareIntent)
    }

    fun sharePdf(context: Context, data: ReceiptData) {
        val file = generatePdfFile(context, data)
        val uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
        
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Receipt from ${data.businessName}")
            putExtra(Intent.EXTRA_TEXT, "Here is your receipt for ${data.totalAmount}.")
            // padding = 10 removed
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Receipt PDF"))
    }
    
    fun downloadPdf(context: Context, data: ReceiptData) {
        // Direct download using MediaStore is best but verbose.
        // For simple user delight, we can try to copy to a visible public folder directly.
        // OR fallback to PrintManager if file operations fail.
        
        val pdfFile = generatePdfFile(context, data)
        
        // Android 10+ (Q) uses MediaStore
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            val contentValues = android.content.ContentValues().apply {
                put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, "Receipt_${data.receiptId}.pdf")
                put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_DOWNLOADS)
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                resolver.openOutputStream(uri)?.use { outputStream ->
                    java.io.FileInputStream(pdfFile).use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                android.widget.Toast.makeText(context, "Saved to Downloads", android.widget.Toast.LENGTH_SHORT).show()
                // view intent
                val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                // context.startActivity(viewIntent) // Optional
            } else {
                 android.widget.Toast.makeText(context, "Failed to save PDF", android.widget.Toast.LENGTH_SHORT).show()
            }
        } else {
             // Legacy External Storage (needs permission, usually granted in dev environment or we fallback to print)
             printReceipt(context, data) // Fallback for older devices/no permission
        }
    }

    fun printReceipt(context: Context, data: ReceiptData) {
        val webView = WebView(context)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                createWebPrintJob(context, view!!)
            }
        }
        val html = generateHtml(data)
        webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
    }

    private fun createWebPrintJob(context: Context, webView: WebView) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
        printManager?.let {
            val printAdapter = webView.createPrintDocumentAdapter("NeoBuk_Receipt")
            val builder = PrintAttributes.Builder()
            builder.setMediaSize(PrintAttributes.MediaSize.ISO_A4)
            it.print("NeoBuk_Receipt", printAdapter, builder.build())
        }
    }

    // Keep this for Print function if needed
    fun generateHtml(data: ReceiptData): String {
        // Reuse previous logic but kept internal if only needed for PrintManager fallback
         val itemsHtml = data.items.joinToString("") { item ->
            """
            <tr>
                <td style="padding: 8px 0; border-bottom: 1px solid #eee;">
                    <div style="font-weight: bold;">${item.name}</div>
                    <div style="font-size: 12px; color: #666;">${item.quantity} x ${item.price}</div>
                </td>
                <td style="padding: 8px 0; border-bottom: 1px solid #eee; text-align: right; vertical-align: top;">
                    ${item.total}
                </td>
            </tr>
            """
        }
        return """
            <html>
            <head>
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body { font-family: 'Helvetica', sans-serif; padding: 20px; color: #333; max-width: 400px; margin: 0 auto; background-color: #fff; }
                    .header { text-align: center; margin-bottom: 20px; }
                    .title { font-size: 24px; font-weight: bold; margin-bottom: 5px; color: #00796B; }
                    .meta { font-size: 14px; color: #666; margin-bottom: 5px; }
                    table { width: 100%; border-collapse: collapse; margin-bottom: 20px; }
                    .totals { text-align: right; margin-top: 20px; border-top: 2px solid #333; padding-top: 10px; }
                    .total-row { font-size: 18px; font-weight: bold; color: #000; }
                    .footer { text-align: center; margin-top: 40px; font-size: 12px; color: #999; }
                </style>
            </head>
            <body>
                <div class="header">
                    <div class="title">${data.businessName}</div>
                    <div class="meta">Receipt No: ${data.receiptId}</div>
                    <div class="meta">${data.date}</div>
                </div>
                <table>${itemsHtml}</table>
                <div class="totals"><div class="total-row">TOTAL: ${data.totalAmount}</div></div>
                <div class="footer">Generated by NeoBuk</div>
            </body>
            </html>
        """.trimIndent()
    }
    
    private fun bitmapToBase64(bitmap: Bitmap): String {
        return "" // deprecated
    }
}
