package com.neobuk.app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neobuk.app.data.repositories.DashboardMetrics
import com.neobuk.app.data.repositories.DashboardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val dashboardRepository: DashboardRepository
) : ViewModel() {

    private var currentBusinessId: String? = null

    private val _metrics = MutableStateFlow<DashboardMetrics?>(null)
    val metrics: StateFlow<DashboardMetrics?> = _metrics.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _weeklyPerformance = MutableStateFlow<List<com.neobuk.app.data.repositories.WeeklyPerformance>>(emptyList())
    val weeklyPerformance: StateFlow<List<com.neobuk.app.data.repositories.WeeklyPerformance>> = _weeklyPerformance.asStateFlow()

    fun setBusinessId(businessId: String) {
        // Always refresh on set, or check if changed. Ideally force refresh if stale.
        if (currentBusinessId == businessId && _metrics.value != null) {
            // Optional: check if data is old? For now, we trust the caller (HomeScreen) to handle lifecycle
            // But if user says "sales shows zero", we probably want to support force refresh.
        }
        currentBusinessId = businessId
        fetchMetrics()
    }

    fun fetchMetrics() {
        val businessId = currentBusinessId ?: return
        viewModelScope.launch {
            _isLoading.value = true
            
            // 1. Fetch Metrics
            dashboardRepository.getDashboardMetrics(businessId).onSuccess {
                _metrics.value = it
            }
            
            // 2. Fetch Weekly Performance
            val weekly = dashboardRepository.getWeeklyPerformance(businessId)
            _weeklyPerformance.value = weekly
            
            _isLoading.value = false
        }
    }
}
