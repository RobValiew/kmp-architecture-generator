package com.vali.shared.features.testfeature.presentation

class TestFeatureReducer {

    fun reduce(
        currentState: TestFeatureState,
        result: TestFeatureResult
    ): TestFeatureState {
        return when (result) {
            TestFeatureResult.Loading -> {
                currentState.copy(
                    isLoading = true,
                    error = null
                )
            }

            TestFeatureResult.Success -> {
                currentState.copy(
                    isLoading = false,
                    error = null
                )
            }

            is TestFeatureResult.Error -> {
                currentState.copy(
                    isLoading = false,
                    error = result.message
                )
            }
        }
    }
}