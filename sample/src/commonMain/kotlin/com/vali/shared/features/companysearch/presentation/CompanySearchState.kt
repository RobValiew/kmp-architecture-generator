package com.vali.shared.features.companysearch.presentation

data class CompanySearchState(
    val isLoading: Boolean = false,
    val error: String? = null
)