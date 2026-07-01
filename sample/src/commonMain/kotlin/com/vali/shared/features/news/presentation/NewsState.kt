package com.vali.shared.features.news.presentation

data class NewsState(
    val isLoading: Boolean = false,
    val error: String? = null
)