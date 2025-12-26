package com.neobuk.app.data.repositories

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Serializable
data class DayClosure(
    val id: String,
    @SerialName("business_id") val businessId: String,
    @SerialName("closure_date") val closureDate: String,
    @SerialName("total_sales_amount") val totalSalesAmount: Double,
    @SerialName("total_sales_count") val totalSalesCount: Long,
    @SerialName("total_expenses_amount") val totalExpensesAmount: Double,
    @SerialName("total_expenses_count") val totalExpensesCount: Long,
    @SerialName("cash_in_hand_expected") val cashInHandExpected: Double,
    @SerialName("cash_in_hand_actual") val cashInHandActual: Double,
    @SerialName("discrepancy") val discrepancy: Double? = 0.0,
    @SerialName("created_at") val createdAt: String? = null
)

class DayClosureRepository(private val supabase: SupabaseClient) {

    suspend fun checkClosureStatus(businessId: String, date: LocalDate): Result<Boolean> {
        return try {
            val isClosed = supabase.postgrest.rpc(
                "check_day_closure_status",
                buildJsonObject {
                    put("p_business_id", businessId)
                    put("p_date", date.format(DateTimeFormatter.ISO_LOCAL_DATE))
                }
            ).decodeSingle<Boolean>()
            Result.success(isClosed)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun performClosure(
        businessId: String,
        date: LocalDate,
        cashActual: Double,
        notes: String? = null
    ): Result<DayClosure> {
        return try {
            val closure = supabase.postgrest.rpc(
                "perform_day_closure",
                buildJsonObject {
                    put("p_business_id", businessId)
                    put("p_date", date.format(DateTimeFormatter.ISO_LOCAL_DATE))
                    put("p_cash_actual", cashActual)
                    put("p_notes", notes)
                }
            ).decodeSingle<DayClosure>()
            Result.success(closure)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getClosures(businessId: String): Result<List<DayClosure>> {
        return try {
            val closures = supabase.postgrest
                .from("day_closures")
                .select {
                    filter {
                        eq("business_id", businessId)
                    }
                    order("closure_date", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                }
                .decodeList<DayClosure>()
            Result.success(closures)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
