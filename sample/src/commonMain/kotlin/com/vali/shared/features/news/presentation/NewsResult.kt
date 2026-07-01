package com.vali.shared.features.news.presentation

sealed interface NewsResult {
    data object Loading : NewsResult
    data object Success : NewsResult
    data class Error(val message: String) : NewsResult
}