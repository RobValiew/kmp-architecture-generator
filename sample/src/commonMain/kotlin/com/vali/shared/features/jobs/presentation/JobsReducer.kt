package com.vali.shared.features.jobs.presentation

class JobsReducer {

    fun reduce(
        currentState: JobsState,
        result: JobsResult
    ): JobsState {
        return when (result) {
            JobsResult.Loading -> {
                currentState.copy(
                    isLoading = true,
                    error = null
                )
            }

            JobsResult.Success -> {
                currentState.copy(
                    isLoading = false,
                    error = null
                )
            }

            is JobsResult.Error -> {
                currentState.copy(
                    isLoading = false,
                    error = result.message
                )
            }
        }
    }
}