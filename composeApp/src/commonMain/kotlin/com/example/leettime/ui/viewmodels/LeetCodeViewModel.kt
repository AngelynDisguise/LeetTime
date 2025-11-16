package com.example.leettime.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.leettime.data.model.Problem
import com.example.leettime.data.repository.LeetCodeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LeetCodeViewModel(
    private val repository: LeetCodeRepository
) : ViewModel() {

    private val _currentProblem = MutableStateFlow<Problem?>(null)
    val currentProblem: StateFlow<Problem?> = _currentProblem.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /**
     * Load a problem by ID
     * First checks cache, then fetches from API if not found
     */
    fun loadProblem(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val problem = repository.getProblem(id)

            if (problem != null) {
                _currentProblem.value = problem
            } else {
                _error.value = "Problem not found"
            }

            _isLoading.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }
}