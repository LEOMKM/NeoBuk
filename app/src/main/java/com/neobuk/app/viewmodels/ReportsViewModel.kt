package com.neobuk.app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neobuk.app.data.repositories.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

class ReportsViewModel(
    private val reportsRepository: ReportsRepository
) : ViewModel() {

    private var currentBusinessId: String? = null

    // State definitions
    private val _summary = MutableStateFlow(ReportSummary())
    val summary: StateFlow<ReportSummary> = _summary.asStateFlow()

    private val _topProducts = MutableStateFlow<List<TopProductReportItem>>(emptyList())
    val topProducts: StateFlow<List<TopProductReportItem>> = _topProducts.asStateFlow()

    private val _paymentMethods = MutableStateFlow<List<PaymentMethodReportItem>>(emptyList())
    val paymentMethods: StateFlow<List<PaymentMethodReportItem>> = _paymentMethods.asStateFlow()

    private val _salesTrend = MutableStateFlow<List<SalesTrendItem>>(emptyList())
    val salesTrend: StateFlow<List<SalesTrendItem>> = _salesTrend.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _selectedFilter = MutableStateFlow("Today")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    // For Custom Date Range
    private var customStartDate: Long? = null
    private var customEndDate: Long? = null

    fun setBusinessId(businessId: String) {
        currentBusinessId = businessId
        loadReportData()
    }

    fun setFilter(filter: String) {
        _selectedFilter.value = filter
        loadReportData()
    }

    fun setCustomRange(start: Long, end: Long) {
        _selectedFilter.value = "Custom"
        customStartDate = start
        customEndDate = end
        loadReportData()
    }

    fun refreshData() {
        loadReportData()
    }

    private fun loadReportData() {
        val businessId = currentBusinessId ?: return

        viewModelScope.launch {
            _isLoading.value = true

            val (start, end) = calculateDateRange(_selectedFilter.value)

            // Launch all fetches in parallel and wait for all to complete
            kotlinx.coroutines.coroutineScope {
                launch {
                    reportsRepository.getReportSummary(businessId, start, end).onSuccess {
                        _summary.value = it
                    }
                }
                
                launch {
                    reportsRepository.getTopSellingProducts(businessId, start, end).onSuccess {
                        _topProducts.value = it
                    }
                }
                
                launch {
                    reportsRepository.getSalesByPaymentMethod(businessId, start, end).onSuccess {
                        _paymentMethods.value = it
                    }
                }
                
                launch {
                    reportsRepository.getSalesTrend(businessId, start, end).onSuccess {
                        _salesTrend.value = it
                    }
                }
            }

            _isLoading.value = false
        }
    }

    private fun calculateDateRange(filter: String): Pair<Long, Long> {
        val now = LocalDateTime.now()
        val zone = ZoneId.systemDefault()
        
        val end = now.atZone(zone).toInstant().toEpochMilli()

        val start = when (filter) {
            "Today" -> {
                now.with(LocalTime.MIN).atZone(zone).toInstant().toEpochMilli()
            }
            "This Week" -> {
                // Start of week (Monday)
                now.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
                    .with(LocalTime.MIN)
                    .atZone(zone).toInstant().toEpochMilli()
            }
            "This Month" -> {
                // Start of month
                now.with(TemporalAdjusters.firstDayOfMonth())
                    .with(LocalTime.MIN)
                    .atZone(zone).toInstant().toEpochMilli()
            }
            "Custom" -> {
                customStartDate ?: now.with(LocalTime.MIN).atZone(zone).toInstant().toEpochMilli()
            }
            else -> now.minusDays(1).atZone(zone).toInstant().toEpochMilli()
        }
        
        val effectiveEnd = if (filter == "Custom") customEndDate ?: end else end

        return Pair(start, effectiveEnd)
    }
}
