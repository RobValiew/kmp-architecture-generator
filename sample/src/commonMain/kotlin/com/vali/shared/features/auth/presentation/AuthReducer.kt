package com.vali.shared.features.auth.presentation

class AuthReducer {

    fun reduce(
        currentState: AuthState,
        result: AuthResult
    ): AuthState {
        return when (result) {
            AuthResult.Loading -> {
                currentState.copy(
                    isLoading = true,
                    error = null
                )
            }

            AuthResult.Success -> {
                currentState.copy(
                    isLoading = false,
                    error = null
                )
            }

            is AuthResult.Error -> {
                currentState.copy(
                    isLoading = false,
                    error = result.message
                )
            }
        }
    }
}