package com.vali.shared.features.products.presentation

data class ProductsUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)