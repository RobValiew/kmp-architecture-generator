package com.vali.shared.features.auth.presentation

data class AuthState(
    val isLoading: Boolean = false,
    val error: String? = null
)