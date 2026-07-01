package com.vali.shared.features.companysearch.presentation

sealed interface CompanySearchResult {
    data object Loading : CompanySearchResult
    data object Success : CompanySearchResult
    data class Error(val message: String) : CompanySearchResult
}