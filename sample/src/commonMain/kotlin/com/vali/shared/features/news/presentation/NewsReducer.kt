package com.vali.shared.features.news.presentation

class NewsReducer {

    fun reduce(
        currentState: NewsState,
        result: NewsResult
    ): NewsState {
        return when (result) {
            NewsResult.Loading -> {
                currentState.copy(
                    isLoading = true,
                    error = null
                )
            }

            NewsResult.Success -> {
                currentState.copy(
                    isLoading = false,
                    error = null
                )
            }

            is NewsResult.Error -> {
                currentState.copy(
                    isLoading = false,
                    error = result.message
                )
            }
        }
    }
}