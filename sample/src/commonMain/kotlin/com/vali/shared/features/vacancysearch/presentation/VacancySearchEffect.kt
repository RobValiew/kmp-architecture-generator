package com.vali.shared.features.vacancysearch.presentation

sealed interface VacancySearchEffect {
    data class ShowMessage(val message: String) : VacancySearchEffect
}