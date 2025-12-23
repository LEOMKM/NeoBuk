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
        containerColor = NeoBukTeal, // Background similar to image header
        bottomBar = {
            // Action Buttons pinned to bottom
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NeoBukTeal) // Match background
                    .padding(16.dp), // Padding around buttons
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Share Button (Text/WhatsApp)
                Button(
                    onClick = { ReceiptUtils.shareText(context, ReceiptUtils.generatePlainText(data)) },
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                   Icon(Icons.Default.Share, null, tint = NeoBukTeal)
                   Spacer(modifier = Modifier.width(8.dp))
                   Text("Share", color = NeoBukTeal, style = AppTextStyles.buttonMedium)
                }
                
                // Print/PDF Button
                Button(
                    onClick = { ReceiptUtils.printReceipt(context, data) },
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black), // Dark button like design
                    shape = RoundedCornerShape(16.dp)
                ) {
                   Icon(Icons.Default.Print, null, tint = Color.White)
                   Spacer(modifier = Modifier.width(8.dp))
                   Text("Download / Print", color = Color.White, style = AppTextStyles.buttonMedium.copy(fontSize = 14.sp))
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally // Center the card
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            
            // Receipt Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Fill available space but leave room for buttons if needed, or scroll
                    .verticalScroll(rememberScrollState()),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
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
                        Text(data.date, style = AppTextStyles.bodyBold.copy(color = Color.Black))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Method", style = AppTextStyles.body, color = Color.Gray)
                        Column(horizontalAlignment = Alignment.End) {
                            Text(data.paymentMethod, style = AppTextStyles.bodyBold.copy(color = Color.Black))
                            if (!data.paymentRef.isNullOrEmpty()) {
                                Text("Ref: ${data.paymentRef}", style = AppTextStyles.caption.copy(color = Color.Gray))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Business", style = AppTextStyles.body, color = Color.Gray)
                        Text(data.businessName, style = AppTextStyles.bodyBold.copy(color = Color.Black))
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
        val sb = StringBuilder()
        sb.appendLine("🧾 *RECEIPT* from ${data.businessName}")
        sb.appendLine("Date: ${data.date}")
        sb.appendLine("Receipt No: ${data.receiptId}")
        sb.appendLine("--------------------------------")
        
        data.items.forEach { item ->
            sb.appendLine("${item.name} (x${item.quantity})")
            sb.appendLine("  @ ${item.price} = ${item.total}")
        }
        
        sb.appendLine("--------------------------------")
        sb.appendLine("*TOTAL: ${data.totalAmount}*")
        sb.appendLine("Paid via: ${data.paymentMethod}")
        if (!data.paymentRef.isNullOrEmpty()) sb.appendLine("Ref: ${data.paymentRef}")
        sb.appendLine("--------------------------------")
        if (data.customerName != null) sb.appendLine("Customer: ${data.customerName}")
        sb.appendLine("Thank you for your business!")
        
        return sb.toString()
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = java.io.ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return android.util.Base64.encodeToString(byteArray, android.util.Base64.NO_WRAP)
    }

    fun generateHtml(data: ReceiptData): String {
        // Generate barcode for HTML
        val barcodeBitmap = generateBarcodeBitmap(data.receiptId, 400, 100)
        val barcodeBase64 = barcodeBitmap?.let { bitmapToBase64(it) }

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
                    .barcode { text-align: center; margin-top: 20px; }
                    .barcode img { max-width: 100%; height: auto; }
                </style>
            </head>
            <body>
                <div class="header">
                    <div class="title">${data.businessName}</div>
                    <div class="meta">Receipt No: ${data.receiptId}</div>
                    <div class="meta">${data.date}</div>
                    ${if (data.customerName != null) "<div class='meta'>Customer: ${data.customerName}</div>" else ""}
                </div>

                <table>
                    ${itemsHtml}
                </table>

                <div class="totals">
                    <div class="total-row">TOTAL: ${data.totalAmount}</div>
                    <div class="meta" style="margin-top: 5px;">Paid via ${data.paymentMethod}</div>
                    ${if (!data.paymentRef.isNullOrEmpty()) "<div class='meta'>Ref: ${data.paymentRef}</div>" else ""}
                </div>

                ${if (barcodeBase64 != null) """
                <div class="barcode">
                    <img src="data:image/png;base64,$barcodeBase64" alt="Barcode" />
                    <div style="font-size: 10px; margin-top: 4px;">${data.receiptId}</div>
                </div>
                """ else ""}

                <div class="footer">
                    Thank you!<br>
                    Generated by NeoBuk
                </div>
            </body>
            </html>
        """.trimIndent()
    }

    fun shareText(context: Context, text: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Receipt")
        context.startActivity(shareIntent)
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
}
