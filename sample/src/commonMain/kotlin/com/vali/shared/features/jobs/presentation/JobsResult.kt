package com.vali.shared.features.jobs.presentation

sealed interface JobsResult {
    data object Loading : JobsResult
    data object Success : JobsResult
    data class Error(val message: String) : JobsResult
}