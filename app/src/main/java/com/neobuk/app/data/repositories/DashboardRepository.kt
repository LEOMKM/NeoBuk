package com.neobuk.app.data.repositories

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Serializable
data class DashboardMetrics(
    @SerialName("today_sales") val todaySales: Double,
    @SerialName("today_expenses") val todayExpenses: Double,
    @SerialName("today_profit") val todayProfit: Double,
    @SerialName("sales_growth") val salesGrowth: String,
    @SerialName("net_profit_margin") val netProfitMargin: Double
)

class DashboardRepository(private val supabase: SupabaseClient) {

    suspend fun getDashboardMetrics(businessId: String): Result<DashboardMetrics> {
        return try {
            val now = LocalDate.now()
            val startOfDay = now.atStartOfDay(ZoneId.systemDefault()).toInstant().toString()
            val endOfDay = now.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toString()

            val metrics = supabase.postgrest.rpc(
                "get_dashboard_metrics",
                buildJsonObject {
                    put("p_business_id", businessId)
                    put("p_start_date", startOfDay)
                    put("p_end_date", endOfDay)
                }
            ).decodeSingle<DashboardMetrics>()
            Result.success(metrics)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
