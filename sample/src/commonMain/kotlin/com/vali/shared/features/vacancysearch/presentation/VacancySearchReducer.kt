package com.vali.shared.features.vacancysearch.presentation

class VacancySearchReducer {

    fun reduce(
        currentState: VacancySearchState,
        result: VacancySearchResult
    ): VacancySearchState {
        return when (result) {
            VacancySearchResult.Loading -> {
                currentState.copy(
                    isLoading = true,
                    error = null
                )
            }

            VacancySearchResult.Success -> {
                currentState.copy(
                    isLoading = false,
                    error = null
                )
            }

            is VacancySearchResult.Error -> {
                currentState.copy(
                    isLoading = false,
                    error = result.message
                )
            }
        }
    }
}