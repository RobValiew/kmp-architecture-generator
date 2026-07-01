package com.vali.shared.features.vacancysearch.presentation

data class VacancySearchState(
    val isLoading: Boolean = false,
    val error: String? = null
)