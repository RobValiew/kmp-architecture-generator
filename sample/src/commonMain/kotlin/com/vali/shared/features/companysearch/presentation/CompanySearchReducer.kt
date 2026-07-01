package com.vali.shared.features.companysearch.presentation

class CompanySearchReducer {

    fun reduce(
        currentState: CompanySearchState,
        result: CompanySearchResult
    ): CompanySearchState {
        return when (result) {
            CompanySearchResult.Loading -> {
                currentState.copy(
                    isLoading = true,
                    error = null
                )
            }

            CompanySearchResult.Success -> {
                currentState.copy(
                    isLoading = false,
                    error = null
                )
            }

            is CompanySearchResult.Error -> {
                currentState.copy(
                    isLoading = false,
                    error = result.message
                )
            }
        }
    }
}