package com.neobuk.app.data.repositories

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Serializable
data class ReportSummary(
    @SerialName("total_sales") val totalSales: Double = 0.0,
    @SerialName("sales_count") val salesCount: Long = 0,
    @SerialName("avg_sale_value") val avgSaleValue: Double = 0.0,
    @SerialName("total_expenses") val totalExpenses: Double = 0.0,
    @SerialName("gross_profit") val grossProfit: Double = 0.0,
    @SerialName("net_profit") val netProfit: Double = 0.0,
    @SerialName("net_profit_margin") val netProfitMargin: Double = 0.0
)

@Serializable
data class TopProductReportItem(
    @SerialName("product_name") val productName: String,
    @SerialName("units_sold") val unitsSold: Double,
    @SerialName("revenue") val revenue: Double
)

@Serializable
data class PaymentMethodReportItem(
    @SerialName("payment_method") val paymentMethod: String,
    @SerialName("total_amount") val totalAmount: Double,
    @SerialName("transaction_count") val transactionCount: Long
)

@Serializable
data class SalesTrendItem(
    @SerialName("period_date") val periodDate: String, // "YYYY-MM-DD"
    @SerialName("daily_total") val dailyTotal: Double
)


class ReportsRepository(private val supabase: SupabaseClient) {

    suspend fun getReportSummary(
        businessId: String,
        startDate: Long,
        endDate: Long
    ): Result<ReportSummary> {
        return try {
            val summary = supabase.postgrest.rpc(
                "get_report_summary",
                buildJsonObject {
                    put("p_business_id", businessId)
                    put("p_start_date", formatIsoDate(startDate))
                    put("p_end_date", formatIsoDate(endDate))
                }
            ).decodeList<ReportSummary>()
            
            // RPC returns a list (table), but we expect 1 row
            Result.success(summary.firstOrNull() ?: ReportSummary())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTopSellingProducts(
        businessId: String,
        startDate: Long,
        endDate: Long,
        limit: Int = 5
    ): Result<List<TopProductReportItem>> {
        return try {
            val products = supabase.postgrest.rpc(
                "get_top_selling_products",
                buildJsonObject {
                    put("p_business_id", businessId)
                    put("p_start_date", formatIsoDate(startDate))
                    put("p_end_date", formatIsoDate(endDate))
                    put("p_limit", limit)
                }
            ).decodeList<TopProductReportItem>()
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getSalesByPaymentMethod(
        businessId: String,
        startDate: Long,
        endDate: Long
    ): Result<List<PaymentMethodReportItem>> {
        return try {
             val methods = supabase.postgrest.rpc(
                "get_sales_by_payment_method_stats",
                buildJsonObject {
                    put("p_business_id", businessId)
                    put("p_start_date", formatIsoDate(startDate))
                    put("p_end_date", formatIsoDate(endDate))
                }
            ).decodeList<PaymentMethodReportItem>()
            Result.success(methods)
        } catch (e: Exception) {
             Result.failure(e)
        }
    }
    
    suspend fun getSalesTrend(
        businessId: String,
        startDate: Long,
        endDate: Long
    ): Result<List<SalesTrendItem>> {
        return try {
            val trend = supabase.postgrest.rpc(
                "get_daily_sales_trend",
                buildJsonObject {
                    put("p_business_id", businessId)
                    put("p_start_date", formatIsoDate(startDate))
                    put("p_end_date", formatIsoDate(endDate))
                }
            ).decodeList<SalesTrendItem>()
            Result.success(trend)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun formatIsoDate(timestamp: Long): String {
        return DateTimeFormatter.ISO_INSTANT
            .format(Instant.ofEpochMilli(timestamp))
    }
}
