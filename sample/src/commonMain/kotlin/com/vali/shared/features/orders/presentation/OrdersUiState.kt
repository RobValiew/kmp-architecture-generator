package com.vali.shared.features.orders.presentation

data class OrdersUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)