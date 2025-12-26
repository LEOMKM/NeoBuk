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

    fun setBusinessId(businessId: String) {
        if (currentBusinessId == businessId && _metrics.value != null) return
        currentBusinessId = businessId
        fetchMetrics()
    }

    fun fetchMetrics() {
        val businessId = currentBusinessId ?: return
        viewModelScope.launch {
            _isLoading.value = true
            dashboardRepository.getDashboardMetrics(businessId).onSuccess {
                _metrics.value = it
            }.onFailure {
                // Log and keep old data or set error state
            }
            _isLoading.value = false
        }
    }
}
