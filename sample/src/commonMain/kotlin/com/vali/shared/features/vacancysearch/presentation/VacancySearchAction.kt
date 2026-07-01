package com.vali.shared.features.vacancysearch.presentation

sealed interface VacancySearchAction {
    data object Load : VacancySearchAction
    data object Retry : VacancySearchAction
}