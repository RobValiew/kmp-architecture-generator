package com.vali.shared.features.vacancysearch.presentation

sealed interface VacancySearchResult {
    data object Loading : VacancySearchResult
    data object Success : VacancySearchResult
    data class Error(val message: String) : VacancySearchResult
}