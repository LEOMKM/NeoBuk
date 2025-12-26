package com.neobuk.app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neobuk.app.data.repositories.DayClosure
import com.neobuk.app.data.repositories.DayClosureRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DayClosureViewModel(
    private val dayClosureRepository: DayClosureRepository
) : ViewModel() {

    private var currentBusinessId: String? = null

    private val _isTodayClosed = MutableStateFlow<Boolean?>(null) // null = unknown
    val isTodayClosed: StateFlow<Boolean?> = _isTodayClosed.asStateFlow()

    private val _closureHistory = MutableStateFlow<List<DayClosure>>(emptyList())
    val closureHistory: StateFlow<List<DayClosure>> = _closureHistory.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun setBusinessId(businessId: String) {
        currentBusinessId = businessId
        checkTodayStatus()
        fetchHistory()
    }

    fun checkTodayStatus() {
        val businessId = currentBusinessId ?: return
        val today = LocalDate.now()
        
        viewModelScope.launch {
            dayClosureRepository.checkClosureStatus(businessId, today).onSuccess {
                _isTodayClosed.value = it
            }
        }
    }

    fun fetchHistory() {
        val businessId = currentBusinessId ?: return
        viewModelScope.launch {
            _isLoading.value = true
            dayClosureRepository.getClosures(businessId).onSuccess {
                _closureHistory.value = it
            }.onFailure {
                // Log error
            }
            _isLoading.value = false
        }
    }

    fun performClosure(
        cashActual: Double,
        notes: String?,
        onSuccess: (DayClosure) -> Unit,
        onError: (String) -> Unit
    ) {
        val businessId = currentBusinessId ?: return
        val today = LocalDate.now()

        viewModelScope.launch {
            _isLoading.value = true
            dayClosureRepository.performClosure(businessId, today, cashActual, notes)
                .onSuccess {
                    _isTodayClosed.value = true
                    fetchHistory() // Refresh history
                    onSuccess(it)
                }
                .onFailure {
                    onError(it.message ?: "Failed to close day")
                }
            _isLoading.value = false
        }
    }
}
